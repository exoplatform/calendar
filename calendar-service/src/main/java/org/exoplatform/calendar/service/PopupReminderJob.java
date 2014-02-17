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

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.quartz.JobExecutionContext;

public class PopupReminderJob extends MultiTenancyJob {
  private static Log log_ = ExoLogger.getLogger(PopupReminderJob.class);

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return PopupReminderTask.class;
  }
  
  public class PopupReminderTask extends MultiTenancyTask{

    public PopupReminderTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      super.run();
      SessionProvider provider = SessionProvider.createSystemProvider();
      try {
        if (log_.isDebugEnabled())
          log_.debug("Calendar popup reminder service");
        java.util.Calendar fromCalendar = Utils.getInstanceTempCalendar();
        ContinuationService continuation = (ContinuationService) container.getComponentInstanceOfType(ContinuationService.class);
        OrganizationService orgService = container.getComponentInstanceOfType(OrganizationService.class);
        UserHandler userHandler = orgService.getUserHandler();
        Node calendarHome = Utils.getPublicServiceHome(provider);
        if (calendarHome == null)
          return;
        StringBuffer path = new StringBuffer(getReminderPath(fromCalendar, provider));
        path.append("//element(*,exo:reminder)");
        path.append("[@exo:remindDateTime <= xs:dateTime('" + ISO8601.format(fromCalendar) + "') and @exo:isOver = 'false' and @exo:reminderType = 'popup' ]");
        QueryManager queryManager = Utils.getSession(provider).getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(path.toString(), Query.XPATH);
        QueryResult results = query.execute();
        NodeIterator iter = results.getNodes();
        Node reminder;
        List<Reminder> popupReminders = new ArrayList<Reminder>();
       
        while (iter.hasNext()) {
          reminder = iter.nextNode();
          boolean isRepeat = reminder.getProperty(Utils.EXO_IS_REPEAT).getBoolean();
          long fromTime = reminder.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTimeInMillis();
          long remindTime = reminder.getProperty(Utils.EXO_REMINDER_DATE).getDate().getTimeInMillis();
          long interval = reminder.getProperty(Utils.EXO_TIME_INTERVAL).getLong() * 60 * 1000;
          
          Reminder rmdObj = new Reminder();
          rmdObj.setRepeate(isRepeat);
          rmdObj.setReminderOwner(reminder.getProperty(Utils.EXO_OWNER).getString());
          rmdObj.setId(reminder.getProperty(Utils.EXO_EVENT_ID).getString());
          
          if(isRepeat) {
            long currentTime1 = java.util.Calendar.getInstance().getTimeInMillis();
            long nextRemindTime = getNextRemindTime(remindTime, currentTime1, interval);
            // if it's time to send reminder, add the rmdObj to list of popup reminders
            if(nextRemindTime > 0) {
              popupReminders.add(rmdObj);
              // if the next reminder time is greater than event from time, the reminder is over (exo:isOver = true)
              if(nextRemindTime > fromTime) { 
                reminder.setProperty(Utils.EXO_IS_OVER, true);
              } else {
                // the reminder is continued, set new time of reminder
                reminder.setProperty(Utils.EXO_IS_OVER, false);
                reminder.setProperty(Utils.EXO_REMINDER_DATE, nextRemindTime);
              }
            }
          } else {
            long currentTime2 = java.util.Calendar.getInstance().getTimeInMillis();
            if(isTimeToRemind(remindTime, currentTime2)) {
              popupReminders.add(rmdObj);
              reminder.setProperty(Utils.EXO_IS_OVER, true);
            }
          }
          reminder.save();
        }
        if (!popupReminders.isEmpty()) {
          for (Reminder rmdObj : popupReminders) {
            for (String user : rmdObj.getReminderOwner().split(Utils.COMMA)) {
              if (userHandler.findUserByName(user, UserStatus.DISABLED) == null) { 
                continuation.sendMessage(user, "/eXo/Application/Calendar/messages", rmdObj.getId());
              }  
            }
          }
        }
      } catch (RepositoryException e) {
        if (log_.isDebugEnabled())
          log_.debug("Data base not ready!");
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

  /*
   * Gets next reminder time based on current reminder time, current time and the interval.
   * Current time is time to send the reminder if it is greater than reminder time and 
   * (current time - reminder time) % interval <= delta 
   * (here we choose delta = 15 seconds, it's equals the period between jobs
   * If current time is time to send the reminder, the method returns next reminder time
   * otherwise, it return -1
   */
  private long getNextRemindTime(long remindTime, long currentTime, long interval) {
    long delta = 15000; 
    long diff = currentTime - remindTime;
    long remaining =  diff % interval;
    if(remaining <= delta && currentTime >= remindTime) {
      // because the user can choose start time of reminder is very long before the from time of event, (refer to CAL-422)
      // here we must get the most recent reminder time before adding the interval to avoid sending many unexpected reminders
      return currentTime - remaining + interval; 
    } else {
      return -1;
    }
  }
  
  /*
   * Checks if current time is time to send the reminder, in case the reminder is not repeated (no interval)
   */
  private Boolean isTimeToRemind(long remindTime, long currentTime) {
    long delta = 15000;
    long diff = currentTime - remindTime;
    return diff <= delta && currentTime >= remindTime;
  }
  
  public static String getReminderPath(java.util.Calendar fromCalendar, SessionProvider provider) throws Exception {
    String year = "Y" + String.valueOf(fromCalendar.get(java.util.Calendar.YEAR));
    String month = "M" + String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1);
    String day = "D" + String.valueOf(fromCalendar.get(java.util.Calendar.DATE));
    StringBuffer path = new StringBuffer("/jcr:root");
    path.append(Utils.getPublicServiceHome(provider).getPath());
    path.append(Utils.SLASH).append(year).append(Utils.SLASH).append(month).append(Utils.SLASH).append(day);
    path.append(Utils.SLASH).append(Utils.CALENDAR_REMINDER);
    return path.toString();
  }
}
