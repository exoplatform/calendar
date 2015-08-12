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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.model.CompositeID;
import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.EventHandler;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventHandlerImpl implements EventHandler {

  private static Log log = ExoLogger.getLogger(EventHandlerImpl.class);

  protected ExtendedCalendarServiceImpl calService;

  public EventHandlerImpl(ExtendedCalendarServiceImpl service) {
    this.calService = service;
  }

  @Override
  public Event getEventById(String eventId) {
    CompositeID composID = CompositeID.parse(eventId);
    EventDAO dao = getEventDAOImpl(composID.getDS());
    if (dao != null) {
      return dao.getById(composID.getId());
    }
    return null;
  }

  @Override
  public Event saveEvent(Event event) {
    EventDAO dao = getEventDAOImpl(event.getDS());
    if (dao != null) {
      return dao.save(event);
    }

    return null;
  }

  @Override
  public Event removeEvent(String eventId) {
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
  public ListAccess<Event> findEventsByQuery(EventQuery eventQuery) {
    List<EventDAO> daos = new LinkedList<EventDAO>();
    if (eventQuery.getDS() == null) {
      for (Storage storage : calService.getAllStorage()) {
        daos.add(storage.getEventDAO());
      }
    } else {
      daos.add(getEventDAOImpl(eventQuery.getDS()));
    }

    List<ListAccess<Event>> result = new LinkedList<ListAccess<Event>>();
    for (EventDAO dao : daos) {
      ListAccess<Event> tmp = dao.findEventsByQuery(eventQuery);
      if (tmp != null) {
        result.add(tmp);
      }
    }

    if (result.size() == 0) {
      return null;      
    } else if (result.size() == 1) {
      return result.get(0);
    } else {
      final List<Event> events = new LinkedList<Event>();
      for (ListAccess<Event> list : result) {
        try {
          events.addAll(Arrays.asList(list.load(0, -1)));
        } catch (Exception e) {
          throw new CalendarException(null, e.getMessage(), e);
        }
      }
      
      return new ListAccess<Event>() {
        @Override
        public int getSize() throws Exception {
          return events.size();
        }

        @Override
        public Event[] load(int offset, int limit) throws Exception, IllegalArgumentException {
          return Utils.subArray(events.toArray(new Event[getSize()]), offset, limit);
        }        
      };
    }
  }

  @Override
  public Event newEventInstance(String dsId) {
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
