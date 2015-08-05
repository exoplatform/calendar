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
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.CompositeID;
import org.exoplatform.calendar.service.EventHandler;
import org.exoplatform.calendar.service.EventQueryCondition;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventHandlerImpl implements EventHandler {

  private static Log      log = ExoLogger.getLogger(EventHandlerImpl.class);

  protected CalendarServiceImpl calService;
  protected JCRDataStorage  storage;

  public EventHandlerImpl(CalendarServiceImpl service) {
    this.calService = service;
    this.storage = service.getDataStorage();
  }

  @Override
  public CalendarEvent getEventById(String eventId) {
    CompositeID composID = CompositeID.parse(eventId);
    EventDAO dao = getEventDAOImpl(composID.getDS());
    if (dao != null) {
      return dao.getById(composID.getId());
    }
    return null;
  }

  @Override
  public CalendarEvent saveEvent(CalendarEvent event) {
    EventDAO dao = getEventDAOImpl(event.getDS());
    if (dao != null) {
      return dao.save(event);
    }

    return null;
  }

  @Override
  public CalendarEvent removeEvent(String eventId) {
    CompositeID composId = CompositeID.parse(eventId);
    EventDAO dao = getEventDAOImpl(composId.getDS());
    if (dao != null) {
      return dao.remove(composId.getId());
    }
    return null;
  }

  /**
   * if no calendarType in query, fallback solution: use JCR DAO implementation with all available JCR calendar types (PERSONAL, GROUP)
   */
  @Override
  public ListAccess<CalendarEvent> findEventsByQuery(EventQueryCondition eventQuery) {
    EventDAO dao = getEventDAOImpl(eventQuery.getDS());
    
    if (dao != null) {
      return dao.findEventsByQuery(eventQuery);
    }

    return null;
  }

  @Override
  public CalendarEvent newEventInstance(String dsId) {
    EventDAO dao = getEventDAOImpl(dsId);
    if (dao != null) {
      return dao.newInstance();
    }
    return null;
  }

  private EventDAO getEventDAOImpl(String id) {
    return calService.lookForDS(id).getEventDAO();
  }
}
