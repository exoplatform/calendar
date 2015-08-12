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

import java.util.List;

import org.exoplatform.calendar.model.query.CalendarQuery;

public interface CalendarHandler {

  /**
   * Return a Calendar with given id, or NULL if it doesn't exist.
   *
   * @param calId
   * @return a Calendar with given id, or NULL if it doesn't exist.
   */
  Calendar getCalendarById(String id);

  /**
   * Find calendars which are matching to query
   * @param query
   * @return a list of calendars satisfied with given query.
   */
  List<Calendar> findCalendars(CalendarQuery query);

  /**
   * Save calendar into database.
   *
   * @param calendar
   */
  Calendar saveCalendar(Calendar calendar);
  
  /**
   * Update an existing Calendar with new information given from Calendar argument.
   *
   * @param calendar
   */
  Calendar updateCalendar(Calendar calendar);
  
  /**
   * @param calendarId
   */
  Calendar removeCalendar(String calendarId);

  Calendar newCalendarInstance(String dsId);
}