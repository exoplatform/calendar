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
package org.exoplatform.webservice.cs.bean;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * June 24, 2010  
 */
public class EventData {
  private List<CalendarEvent> info ;
  private String userTimezoneOffset;
  private Boolean isEdit;
  private List<Calendar> calendars;
  
  public List<CalendarEvent> getInfo() {
    return info;
  }  
  public void setInfo(List<CalendarEvent> info) {
    this.info = info;
  } 
  public void setUserTimezoneOffset(String timezoneOffset) {
    this.userTimezoneOffset = timezoneOffset;
  }
  public String getUserTimezoneOffset() {
    return this.userTimezoneOffset;
  }  
  public void setPermission(Boolean isEdit) {
    this.isEdit = isEdit;
  }
  public Boolean getPermission() {
    return this.isEdit;
  }
  public List<Calendar> getCalendars() {
    return calendars;
  }
  
  public void setCalendars(List<Calendar> calendars) {
    this.calendars = calendars;
  }  
}
