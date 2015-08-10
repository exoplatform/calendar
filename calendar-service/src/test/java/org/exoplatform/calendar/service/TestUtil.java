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

import java.util.Date;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.nservice.CalendarHandler;
import org.exoplatform.calendar.nservice.EventHandler;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TestUtil {
  public static Calendar createPersonalCalendar(CalendarHandler calHandler, String name, String username) {
    Calendar cal = new Calendar();
    cal.setName(name);
    cal.setCalType(Calendar.Type.PERSONAL.type());
    cal.setCalendarOwner(username);

    cal = calHandler.saveCalendar(cal);
    return cal;
  }

  public static Calendar createGroupCalendar(CalendarHandler calHandler, String calName, String[] groups) {
    Calendar cal = new Calendar();
    cal.setName(calName);
    cal.setCalType(Calendar.Type.GROUP.type());
    cal.setGroups(groups);

    cal = calHandler.saveCalendar(cal);

    return cal;
  }

  public static Event createEvent(EventHandler handler, String summary, Date from, Date to, boolean isPrivate, Calendar calendar, EventCategory category) {
    Event event = handler.newEventInstance(calendar.getDS());
    event.setSummary(summary);
    event.setFromDateTime(from);
    event.setToDateTime(to);
    event.setCalendarId(calendar.getId());
    event.setPrivate(isPrivate);
    if (category != null) {
      event.setEventCategoryId(category.getId());
      event.setEventCategoryName(category.getName());
    }

    return handler.saveEvent(event);
  }
}
