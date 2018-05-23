/*
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
 */
package org.exoplatform.calendar.service.impl;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {

  private static final Log LOG = ExoLogger.getLogger(NewUserListener.class);

  private CalendarService  cservice;

  public NewUserListener(CalendarService cservice) throws Exception {
    this.cservice = cservice;
  }

  @Override
  public void preDelete(User user) throws Exception {
    // before delete user from portal, remove shared calendar folder of this user
    try {
      cservice.removeSharedCalendarFolder(user.getUserName());
    } catch (Exception e) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Exception occurs when trying to remove shared calendar folder of this user: " + user.getUserName(), e);
      }
    }
  }
}
