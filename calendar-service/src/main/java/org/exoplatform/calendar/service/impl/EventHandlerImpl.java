/**
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.calendar.service.impl;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.EventHandler;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventHandlerImpl implements EventHandler {

  private static Log      log = ExoLogger.getLogger(EventHandlerImpl.class);

  protected CalendarServiceImpl calSerVice;
  protected JCRDataStorage  storage;

  public EventHandlerImpl(CalendarServiceImpl service) {
    this.calSerVice = service;
    this.storage = service.getDataStorage();
  }

  @Override
  public CalendarEvent getEventById(String eventId, CalendarType calType) {
    EventDAO dao = getSupportedEventDAOs(calType);
    if (dao != null) {
      return dao.getById(eventId, calType);
    }
    return null;
  }

  @Override
  public CalendarEvent saveEvent(CalendarEvent event, boolean isNew) {
    EventDAO dao = getSupportedEventDAOs(event.getCalendarType());
    if (dao != null) {
      return dao.save(event);
    }

    return null;
  }

  @Override
  public CalendarEvent removeEvent(String eventId, CalendarType calendarType) {
    EventDAO dao = getSupportedEventDAOs(calendarType);
    if (dao != null) {
      return dao.remove(eventId, calendarType);
    }
    return null;
  }

  /**
   * if no calendarType in query, fallback solution: use JCR DAO implementation with all available JCR calendar types (PERSONAL, GROUP)
   */
  @Override
  public ListAccess<CalendarEvent> findEventsByQuery(EventQuery eventQuery) {
    CalendarType type = eventQuery.getCalendarType();
    EventDAO dao = null;

    if (type != null) {
      dao = getSupportedEventDAOs(type);
    } else {
      dao = getSupportedEventDAOs(Calendar.Type.PERSONAL);
    }

    if (dao != null) {
      return dao.findEventsByQuery(eventQuery);
    } else {
      throw new IllegalStateException("Can't find supported DAO for type: " + type);
    }
  }

  private EventDAO getSupportedEventDAOs(CalendarType type) {
    return calSerVice.getSupportedEventDAO(type);
  }
}
