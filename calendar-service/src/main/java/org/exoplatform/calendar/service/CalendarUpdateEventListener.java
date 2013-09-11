/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import org.exoplatform.container.component.BaseComponentPlugin;

import java.util.Date;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 1, 2008  
 */
public class CalendarUpdateEventListener extends BaseComponentPlugin implements EventLifeCycle {

  public void preUpdate() {
  }

  public void postUpdate() {
  }

  @Override
  public void savePublicEvent(CalendarEvent event, String calendarId) {

  }

  @Override
  public void updatePublicEvent(CalendarEvent event, String calendarId) {

  }

  @Override
  public void deletePublicEvent(CalendarEvent event, String calendarId) {
    
  }

  @Override
  public void updateFollowingOccurrences(CalendarEvent originEvent, Date stopDate) {
  }

  @Override
  public void removeOneOccurrence(CalendarEvent originEvent, CalendarEvent removedEvent) {
  }

  @Override
  public void updatePublicEvent(CalendarEvent oldEvent, CalendarEvent event, String calendarId) {
    
  }

}
