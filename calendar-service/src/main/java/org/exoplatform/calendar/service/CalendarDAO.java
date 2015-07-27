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

import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;

public interface CalendarDAO {

  /**
   * @param calId
   * @return
   */
  Calendar getCalendarById(String calId);

  /**
   * @param calId
   * @param calType
   * @param params
   * @return
   */
  Calendar getCalendarById(String calId, CalendarType calType);

  /**
   * @param query
   * @return
   */
  ListAccess<Calendar> findCalendarsByQuery(CalendarQuery query);

  /**
   * @param calendar
   * @param isNew
   */
  Calendar saveCalendar(Calendar calendar, boolean isNew);

  /**
   * @param calendarId
   * @param calType
   * @return 
   */
  Calendar removeCalendar(String calendarId, CalendarType calType);

  /**
   * @param type
   * @return
   */
  Calendar newCalendarInstance(CalendarType type);

  Set<Integer> getCalendarTypes();
}
