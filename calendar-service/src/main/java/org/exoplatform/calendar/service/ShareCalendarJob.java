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

import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.quartz.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SARL 
 * Author : Haiddd 
 *          haidd@exoplatform.com 
 * May 11, 2012
 */

public class ShareCalendarJob implements Job, InterruptableJob {

  private static Log         log                  = ExoLogger.getLogger("cs.service.job");

  public ShareCalendarJob() throws Exception {

  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Starting sharing calendar for groups");
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
        .getComponentInstanceOfType(ContinuationService.class);

    OrganizationService oService = (OrganizationService)PortalContainer.getInstance().getComponentInstance(OrganizationService.class) ;

    CalendarService calendarService = (CalendarService)PortalContainer.getInstance().getComponentInstance(CalendarService.class) ;

    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

    Map<String, String> perms = new HashMap<String, String>() ;

    Map<String, String> sharedUsers  = new HashMap<String, String>() ;

    List<String> sharedGroups = (List<String>) jobDataMap.get(Utils.SHARED_GROUPS);
    String user = jobDataMap.getString(Utils.USER_NAME);
    String calendarId = jobDataMap.getString(Utils.CALENDAR_ID);
    String stopMessage = jobDataMap.getString(Utils.STOP_MESSAGE);
    JCRDataStorage jcrDataStorage = (JCRDataStorage) jobDataMap.get(Utils.JCR_DATA_STORAGE);

    try {
      Calendar cal = calendarService.getUserCalendar(user, calendarId) ;

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
      jcrDataStorage.shareCalendar(user, calendarId, Arrays.asList(sharedUsers.keySet().toArray(new String[sharedUsers.keySet().size()])));
      continuation.sendMessage(user, Utils.SHARE_CAL_CHANEL, stopMessage);
      log.info("Finish sharing calendar for groups");
    } catch (Exception e) {
      log.debug("Exception in method:" + e);
      String errorMessage = jobDataMap.getString(Utils.ERROR_MESSAGE);
      continuation.sendMessage(user,  Utils.SHARE_CAL_CHANEL, errorMessage);
    }
    
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
   
  }


}