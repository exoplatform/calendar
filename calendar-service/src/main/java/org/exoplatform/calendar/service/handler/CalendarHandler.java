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

package org.exoplatform.calendar.service.handler;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarQuery;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.commons.utils.ListAccess;

public interface CalendarHandler {

  /**
   * Gets a calendar by its id
   * 
   * @param calId Id of the calendar
   * @return a {@link Calendar}
   */
  public Calendar getCalendarById(String calId);
  
  /**
   * @param calId
   * @param calType
   * @return
   */
  Calendar getCalendarById(String calId, CalendarType calType);

  /**
   * @param query
   * @return
   */
  ListAccess findCalendarsByQuery(CalendarQuery query);
  
  /**
   * @param calendar
   * @param isNew
   */
  Calendar saveCalendar(Calendar calendar, boolean isNew);
  
  /**
   * @param calendarId
   * @param calType
   */
  Calendar removeCalendar(String calendarId, CalendarType calType);
}