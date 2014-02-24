/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
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
 * Created by The eXo Platform SAS
 * Author : vietnq
 *          vietnq@exoplatform.com
 * Nov 20, 2012  
 */
public class DeleteShareJob implements Job, InterruptableJob{

  private static Log         log                  = ExoLogger.getLogger(DeleteShareJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Start un-sharing job");
    
    try {
      ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
          .getComponentInstanceOfType(ContinuationService.class);
      OrganizationService oService = (OrganizationService)PortalContainer.getInstance().getComponentInstance(OrganizationService.class) ;
      CalendarService calendarService = (CalendarService)PortalContainer.getInstance().getComponentInstance(CalendarService.class) ;

      JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

      String username = jobDataMap.getString(Utils.USER_NAME);
      String calendarId = jobDataMap.getString(Utils.CALENDAR_ID);
      List<String> unSharedGroups = (ArrayList<String>) jobDataMap.get(Utils.REMOVED_USERS);
      
      Calendar cal = calendarService.getUserCalendar(username, calendarId);
      
      String calendarName = cal.getName();
      
      String startMessage  = Utils.buildMessageToSend(Utils.START_UN_SHARE, calendarName, unSharedGroups, oService);
      String stopMessage = Utils.buildMessageToSend(Utils.FINISH_UN_SHARE, calendarName, unSharedGroups, oService);
      // send notification about un-sharing job
      continuation.sendMessage(username, Utils.SHARE_CAL_CHANEL, startMessage);
     
      List<String> viewUsers = new ArrayList<String>() ;
      if (cal.getViewPermission() != null) {
        viewUsers = Arrays.asList(cal.getViewPermission()) ;
      }
      
      for(String group : unSharedGroups) {
        for (User user : oService.getUserHandler().findUsersByGroup(group).getAll()) {
          String userId = user.getUserName();
          boolean deleteShared = true ;
          if (!viewUsers.contains(userId)) {
            Object[] groups = oService.getGroupHandler().findGroupsOfUser(userId).toArray() ;
            for (Object object : groups) {
              if (Arrays.asList(cal.getViewPermission()).contains(((Group)object).getId())) {
                deleteShared = false ;
                break ;
              }               
            }
            if (deleteShared) {
              calendarService.removeSharedCalendar(userId, calendarId);
            }
          }
        }
      }
      // send message about finishing the job
      continuation.sendMessage(username, Utils.SHARE_CAL_CHANEL, stopMessage);
      
      log.info("Finish un-sharing job");
    } catch (Exception e) {
      log.debug("Error while un-sharing calendar for groups",e);
    }
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    
  }
  
}
