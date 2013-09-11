/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.calendar.service;

import java.util.Date;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 19, 2010  
 */
public interface EventLifeCycle {

  /**
   * Post save new event or task for group calendar
   * @param event : given event object
   * @param calendarId : given id of calendar object
   */
  public void savePublicEvent(CalendarEvent event, String calendarId);

  /**
   * Post update event or task for group calendar
   * @param event : given event object to be updated
   * @param calendarId : given id of calendar object
   */
  public void updatePublicEvent(CalendarEvent event, String calendarId);
  
  /**
   * Post update event or task for group calendar
   * @param oldEvent : old event to check the changes 
   * @param event : new event to get new changes 
   * @param calendarId: given id of calendar object
   */
  public void updatePublicEvent(CalendarEvent oldEvent, CalendarEvent event, String calendarId);

  /**
   * Post delete event or task for group calendar
   * @param event : given event object to be deleted
   * @param calendarId: given id of calendar object
   */
  public  void deletePublicEvent(CalendarEvent event, String calendarId);

  /**
   * Posts event about updating occurrences of a repetitive series that start from a selected occurrence
   * @param originEvent the origin repetitive event
   * @param stopDate last occurrence date of the repetitive event
   */
  public void updateFollowingOccurrences(CalendarEvent originEvent, Date stopDate);

  /**
   * Posts event about removing one exception event of a repetitive event
   * @param originEvent  origin repetitive event
   * @param removedEvent  removed exception event
   */
  public void removeOneOccurrence(CalendarEvent originEvent, CalendarEvent removedEvent);
}
