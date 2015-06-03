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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.ws.rs.core.MediaType;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
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
      OrganizationService orgService = container.getComponentInstanceOfType(OrganizationService.class);
      //We have JobEnvironmentConfigListener call request lifecycle methods
      //But it's run in difference thread that create bug with PicketlinkIDM using hibernate session (CAL-1031)
      if (orgService instanceof ComponentRequestLifecycle) {
        ((ComponentRequestLifecycle)orgService).startRequest(ExoContainerContext.getCurrentContainer());          
      }

      try {
        MailService mailService = (MailService) container.getComponentInstanceOfType(MailService.class);        
        
        UserHandler userHandler = orgService.getUserHandler();
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
          boolean isRepeat = reminder.getProperty(Utils.EXO_IS_REPEAT).getBoolean();
          long fromTime = reminder.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTimeInMillis();
          long remindTime = reminder.getProperty(Utils.EXO_REMINDER_DATE).getDate().getTimeInMillis();
          long interval = reminder.getProperty(Utils.EXO_TIME_INTERVAL).getLong() * 60 * 1000;
          String to = reminder.getProperty(Utils.EXO_EMAIL).getString();
          String[] emails = to.split(Utils.COMMA);
          StringBuilder sb = new StringBuilder();
          for (String email : emails) {
              org.exoplatform.services.organization.Query orgQuery = new org.exoplatform.services.organization.Query();
              orgQuery.setEmail(email);
              ListAccess<User> users = userHandler.findUsersByQuery(orgQuery, UserStatus.DISABLED);
              if (users.getSize() > 0) continue;
              sb.append(email).append(Utils.COMMA);
          }
          to = sb.toString();
          if (to != null && to.length() > 0) {
            message = new Message();
            message.setMimeType(MediaType.TEXT_HTML);
            message.setTo(to);
            message.setSubject("[reminder] eXo calendar notify mail !");
            message.setBody(reminder.getProperty(Utils.EXO_DESCRIPTION).getString());
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
      } catch (Exception e) {
        log_.error(e.getMessage(), e);
      } finally {
        if (orgService instanceof ComponentRequestLifecycle) {
          ((ComponentRequestLifecycle)orgService).endRequest(ExoContainerContext.getCurrentContainer());          
        }
        provider.close();
      }
      if (log_.isDebugEnabled())
        log_.debug("File plan job done");
    }
  }
}
