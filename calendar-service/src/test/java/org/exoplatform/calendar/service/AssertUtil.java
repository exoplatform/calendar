/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.calendar.service;

import static org.junit.Assert.*;
import java.util.Collection;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class AssertUtil {

  public static void assertNotContainCalendarName(Collection<org.exoplatform.calendar.model.Calendar> calendars, String name) {
    for (org.exoplatform.calendar.model.Calendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        fail("List must not contain the calendar '" + name + "'");
      }
    }
  }

  public static void assertContainCalendarName(Collection<org.exoplatform.calendar.model.Calendar> calendars, String name) {
    for (org.exoplatform.calendar.model.Calendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        return;
      }
    }
    fail("List must contain the calendar '" + name + "'");
  }

  public static void assertContainEventId(String eventId, Collection<CalendarEvent> events) {
    for(CalendarEvent e : events) {
      if(e.getId().equals(eventId)) {
        return;
      }
    }
    fail("Event id [" + eventId + "] should be found in search result");
  }

  public static void assertNotContainEventId(String eventId, Collection<CalendarEvent> events) {
    for(CalendarEvent e : events) {
      if(e.getId().equals(eventId)) {
        fail("Event id [" + eventId + "] should be found in search result");
      }
    }
  }
}
