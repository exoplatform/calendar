/**
 * Copyright (C) 2015 eXo Platform SAS.
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

import org.exoplatform.services.security.Identity;

import java.util.List;

public interface CalendarHandler {

  Calendar getCalendarById(String calId);

  //TODO: we really need this method? Now, this method allow to load all shared/personal/group calendars of user
  // TODO Don't use Identity object in this API. It may be better to have one Query argument only
  List<Calendar> findAllCalendarOfUser(Identity identity, String[] excludeIds);
  
  /**
   * @param calendar
   */
  Calendar saveCalendar(Calendar calendar);
  
  /**
   * @param calendar
   */
  Calendar updateCalendar(Calendar calendar);
  
  /**
   * @param calendarId
   */
  Calendar removeCalendar(String calendarId);

  Calendar newCalendarInstance(String dsId);
}