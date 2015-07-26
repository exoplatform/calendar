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

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarQuery;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.GroupCalendarData;
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
  ListAccess<Calendar> findCalendarsByQuery(CalendarQuery query);
  
  /**
   * @param calendar
   * @param isNew
   */
  Calendar saveCalendar(Calendar calendar, boolean isNew);
  
  /**
   * Return calendars that have publicUrl enabled
   * @return
   * @throws Exception 
   */
  ListAccess<Calendar> getPublicCalendars() throws Exception;
  
  /**
   * Shares the private calendar to other users
   * @param username current user name(or user id)
   * @param calendarId Id of the shared calendar
   * @param sharedUsers list of users with whom to share this calendar
   * @throws Exception
   */
  public void shareCalendar(String username, String calendarId, List<String> sharedUsers) throws Exception;
  
  /**
   * Gets all calendars that are shared with the given user
   * @param username given user name(or user id)
   * @param isShowAll If <code>true</code>, returns all shared calendars, if <code>false</code>, returns only shared calendars
   * that are selected in Calendar Setting.
   * @return <code>GroupCalendarData</code> object
   * @throws Exception
   * @see GroupCalendarData
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception;
  
  /**
   * @param calendarId
   * @param calType
   */
  void removeCalendar(String calendarId, CalendarType calType);
}
