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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.ws.rs.core.MediaType;

import org.quartz.*;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.*;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.lucene.IndexOfflineRepositoryException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.ResourceBundleService;

@DisallowConcurrentExecution
public class ReminderJob implements Job {
  private static final Log LOG = ExoLogger.getLogger(ReminderJob.class);

  @SuppressWarnings("deprecation")
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
    ExoContainer container = PortalContainer.getInstance();

    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      SessionProvider provider = SessionProvider.createSystemProvider();
      OrganizationService orgService = container.getComponentInstanceOfType(OrganizationService.class);
      MailService mailService = container.getComponentInstanceOfType(MailService.class);
      CalendarService calendarService = container.getComponentInstanceOfType(CalendarService.class);
      ResourceBundleService rbs = container.getComponentInstanceOfType(ResourceBundleService.class);
      if (LOG.isDebugEnabled())
        LOG.debug("Calendar email reminder service");
      java.util.Calendar fromCalendar = java.util.Calendar.getInstance();
      Node calendarHome = Utils.getPublicServiceHome();
      if (calendarHome == null)
        return;
      StringBuilder path = new StringBuilder(PopupReminderJob.getReminderPath(fromCalendar));
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
        org.exoplatform.services.organization.Query q = new org.exoplatform.services.organization.Query();
        User user = null;
        if (to != null && to.length() > 0) {
          String[] mails = to.split(",");
          for (String mail : mails) {
            q.setEmail(mail);
            ListAccess<User> list = orgService.getUserHandler().findUsersByQuery(q);
            if (list.getSize() == 0) {
              continue;
            }
            user = list.load(0, 1)[0];
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
              CalendarSetting calendarSettings = calendarService.getCalendarSetting(user.getUserName());
              if (calendarSettings != null) {
                String userTimeZone = calendarSettings.getTimeZone();
                message.setBody(buildBodyMessage(calEvent, res, userTimeZone));
              }
            } else {
              message.setBody("");
            }
            message.setFrom(System.getProperty("exo.email.smtp.from"));
            if (isRepeat) {
              if (fromCalendar.getTimeInMillis() >= fromTime) {
                reminder.setProperty(Utils.EXO_IS_OVER, true);
              } else {
                if ((remindTime + interval) > fromTime) {
                  reminder.setProperty(Utils.EXO_IS_OVER, true);
                } else {
                  long currentTime = fromCalendar.getTimeInMillis();
                  long nextReminderTime = remindTime + interval;
                  while (nextReminderTime <= currentTime) {
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("File plan job done");
      }
    } catch (IndexOfflineRepositoryException e) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("An Error occurred while running Calendar ReminderJob: " + e.getMessage(), e);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      RequestLifeCycle.end();
      ExoContainerContext.setCurrentContainer(oldContainer);
    }
  }

  @SuppressWarnings("deprecation")
  private String buildBodyMessage(CalendarEvent calEvent, ResourceBundle res, String userTimezone) {
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
    if (calEvent.getDescription() != null) {
      summary.append(calEvent.getDescription());
    }
    summary.append("<br>");
    if (calEvent.getLocation() != null) {
      summary.append(location);
      summary.append(calEvent.getLocation());
      summary.append("<br>");
    }
    if (userTimezone != null) {
      TimeZone timeZone = TimeZone.getTimeZone(userTimezone);
      fromTime.setTimeZone(timeZone);
      toTime.setTimeZone(timeZone);
    }
    fromTime.setTime(calEvent.getFromDateTime());
    appendDateToSummary(from, fromTime, summary);
    toTime.setTime(calEvent.getToDateTime());
    appendDateToSummary(to, toTime, summary);
    return summary.toString();
  }

  private void appendDateToSummary(String label, java.util.Calendar cal, StringBuilder summary) {
    final DateFormat df = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
    df.setCalendar(cal);
    summary.append(label)
           .append(df.format(cal.getTime()))
           .append("<br>");
  }
}
