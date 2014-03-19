/**
 * Copyright (C) 2003-2012 eXo Platform SAS.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * Created by The eXo Platform SARL 
 * Author : Haiddd 
 *          haidd@exoplatform.com 
 * May 11, 2012
 */

public class ShareCalendarJob implements Job, InterruptableJob {

  private static Log         log                  = ExoLogger.getLogger(ShareCalendarJob.class);

  public ShareCalendarJob() throws Exception {

  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Starting sharing calendar for groups");

    try {
      JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
      
      String user = jobDataMap.getString(Utils.USER_NAME);
      String calendarId = jobDataMap.getString(Utils.CALENDAR_ID);
      List<String> sharedGroups = (List<String>) jobDataMap.get(Utils.SHARED_GROUPS);

      OrganizationService oService = (OrganizationService)PortalContainer.getInstance().getComponentInstance(OrganizationService.class) ;
      CalendarService calendarService = (CalendarService) PortalContainer.getInstance().getComponentInstance(CalendarService.class) ;
      
      Calendar cal = calendarService.getUserCalendar(user, calendarId);
      
      String calendarName = cal.getName();
      
      String startMessage = Utils.buildMessageToSend(Utils.START_SHARE, calendarName, sharedGroups, oService);
      String stopMessage = Utils.buildMessageToSend(Utils.FINISH_SHARE, calendarName, sharedGroups, oService);
      
      ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
          .getComponentInstanceOfType(ContinuationService.class);
      //send notification about starting sharing job
      continuation.sendMessage(user, Utils.SHARE_CAL_CHANEL, startMessage);
      

      Map<String, String> perms = new HashMap<String, String>() ;
      Map<String, String> sharedUsers  = new HashMap<String, String>() ;

      if(cal.getViewPermission() != null) {
        for(String v : cal.getViewPermission()) {
          perms.put(v,String.valueOf(cal.getEditPermission()!= null && Arrays.asList(cal.getEditPermission()).contains(v))) ;
        }
      }

      for(String name : sharedGroups) {
        for (User userTmp : oService.getUserHandler().findUsersByGroup(name.trim()).getAll()) {
          String userId = userTmp.getUserName();
          // checks if already shared
          if(perms.get(userId) == null) {
            sharedUsers.put(userId, userId);
          }
        }
      }
      
      sharedUsers.remove(user);

      calendarService.shareCalendar(user, calendarId, Arrays.asList(sharedUsers.keySet().toArray(new String[sharedUsers.keySet().size()])));
      // send notification about finishing the job
      continuation.sendMessage(user, Utils.SHARE_CAL_CHANEL, stopMessage);
      
      log.info("Finish sharing calendar for groups");
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Cant share calendar by job:" + e);
    }

  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
   
  }
}