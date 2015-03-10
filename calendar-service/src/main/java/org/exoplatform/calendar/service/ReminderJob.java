/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.calendar.service;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.ws.rs.core.MediaType;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.ResourceBundleService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class ReminderJob extends MultiTenancyJob {
  private static Log log_ = ExoLogger.getLogger(ReminderJob.class);  

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return ReminderTask.class;
  }
  
  public class ReminderTask extends MultiTenancyTask{

    public ReminderTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      super.run();
      SessionProvider provider = SessionProvider.createSystemProvider();
      try {
        MailService mailService = (MailService) container.getComponentInstanceOfType(MailService.class);
        CalendarService calendarService = (CalendarService) container.getComponentInstanceOfType(CalendarService.class);
        ResourceBundleService rbs = (ResourceBundleService) container.getComponentInstanceOfType(ResourceBundleService.class);
        OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
        if (log_.isDebugEnabled())
          log_.debug("Calendar email reminder service");
        java.util.Calendar fromCalendar = GregorianCalendar.getInstance();
        JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
        Node calendarHome = Utils.getPublicServiceHome(provider);
        if (calendarHome == null)
          return;
        StringBuffer path = new StringBuffer(PopupReminderJob.getReminderPath(fromCalendar, provider));
        path.append("//element(*,exo:reminder)");
        path.append("[@exo:remindDateTime <= xs:dateTime('" + ISO8601.format(fromCalendar)
            + "') and @exo:isOver = 'false' and @exo:reminderType = 'email' ]");
        QueryManager queryManager = Utils.getSession(provider).getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(path.toString(), Query.XPATH);
        QueryResult results = query.execute();
        NodeIterator iter = results.getNodes();
        Message message;
        Node reminder;
        while (iter.hasNext()) {
          reminder = iter.nextNode();
          String eventId = reminder.getProperty(Utils.EXO_EVENT_ID).getString();
          CalendarEvent calEvent = calendarService.getEventById(eventId);
          boolean isRepeat = reminder.getProperty(Utils.EXO_IS_REPEAT).getBoolean();
          long fromTime = reminder.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTimeInMillis();
          long remindTime = reminder.getProperty(Utils.EXO_REMINDER_DATE).getDate().getTimeInMillis();
          long interval = reminder.getProperty(Utils.EXO_TIME_INTERVAL).getLong() * 60 * 1000;
          String to = reminder.getProperty(Utils.EXO_EMAIL).getString();
          String language = null;
          if (to != null && to.length() > 0) {
            String [] mails = to.split(",");
            for (String mail:mails) {
              User user = ((UserDAOImpl)orgService.getUserHandler()).findUserByEmail(mail);
              if (user != null) {
                UserProfile profile = orgService.getUserProfileHandler().findUserProfileByName(user.getUserName());
                if (profile != null) {
                  language = profile.getAttribute("user.language");
                }
              }
              if (language == null) {
                language = Utils.LANGUAGE;
              }
              Locale locale = LocaleContextInfo.getLocale(language);
              ResourceBundle res = rbs.getResourceBundle("locale.service.calendar.CalendarService", locale);
              String subject = "[reminder] eXo calendar notify mail !";
              if (res != null) {
                subject = res.getString("Reminder.mail.subject");
              }
              message = new Message();
              message.setMimeType(MediaType.TEXT_HTML);
              message.setTo(mail);
              message.setSubject(subject);
              if (calEvent != null) {
                message.setBody(buildBodyMessage(calEvent, res));
              } else {
                message.setBody("");
              }
              message.setFrom(jdatamap.getString("account"));
              if (isRepeat) {
                if (fromCalendar.getTimeInMillis() >= fromTime) {
                  reminder.setProperty(Utils.EXO_IS_OVER, true);
                } else {
                  if ((remindTime + interval) > fromTime) {
                    reminder.setProperty(Utils.EXO_IS_OVER, true);
                  } else {
                    long currentTime = fromCalendar.getTimeInMillis();
                    long nextReminderTime = remindTime + interval;
                    while(nextReminderTime <= currentTime) {
                      nextReminderTime += interval;
                    }
                    java.util.Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(nextReminderTime);
                    reminder.setProperty(Utils.EXO_REMINDER_DATE, cal);
                    reminder.setProperty(Utils.EXO_IS_OVER, false);
                  }
                }
              } else {
                reminder.setProperty(Utils.EXO_IS_OVER, true);
              }
              reminder.save();
              mailService.sendMessage(message);
            }
          }
        }
      } catch (RepositoryException e) {
        if (log_.isDebugEnabled())
          log_.debug("Data base not ready !");
      } catch (Exception e) {
        if (log_.isDebugEnabled()) {
          log_.debug("Exception in method execute", e);
        }
      } finally {
        provider.close();
      }
      if (log_.isDebugEnabled())
        log_.debug("File plan job done");
    }
  }
  private String buildBodyMessage(CalendarEvent calEvent, ResourceBundle res) {
    java.util.Calendar fromTime = new GregorianCalendar();
    java.util.Calendar toTime = new GregorianCalendar();
    String type = "Type: ";
    String summaryLabel = "Summary: ";
    String description = "Description: ";
    String from = "From: ";
    String to = "To: ";
    String location = "Location: ";
    if (res != null) {
      type = res.getString("Reminder.event.type") + ": ";
      summaryLabel = res.getString("Reminder.event.summary") + ": ";
      description = res.getString("Reminder.event.description") + ": ";
      location = res.getString("Reminder.event.place") + ": ";
      from = res.getString("Reminder.event.from") + ": ";
      to = res.getString("Reminder.event.to") + ": ";
    }
    StringBuilder summary = new StringBuilder(type);
    summary.append(calEvent.getEventType()).append("<br>");
    summary.append(summaryLabel);
    summary.append(calEvent.getSummary()).append("<br>");
    summary.append(description);
    summary.append(calEvent.getDescription());
    summary.append("<br>");
    summary.append(location);
      summary.append(calEvent.getLocation());
    summary.append("<br>");
    fromTime.setTime(calEvent.getFromDateTime());
    appendDateToSummary(from, fromTime, summary);

    toTime.setTime(calEvent.getToDateTime());
    appendDateToSummary(to, toTime, summary);
    return summary.toString();
  }
  private void appendDateToSummary(String label, java.util.Calendar cal, StringBuilder summary) {
    summary.append(label).append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(Utils.COLON);
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ");
    summary.append(cal.get(java.util.Calendar.DATE)).append(Utils.SLASH);
    summary.append(cal.get(java.util.Calendar.MONTH) + 1).append(Utils.SLASH);
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>");
  }
}
