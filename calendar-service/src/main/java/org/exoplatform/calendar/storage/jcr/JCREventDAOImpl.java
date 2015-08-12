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

package org.exoplatform.calendar.storage.jcr;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.query.Query;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class JCREventDAOImpl implements EventDAO {

  private final Storage context;
  private final JCRDataStorage dataStorage;

  private static final Log LOG   = ExoLogger.getExoLogger(JCRCalendarDAOImpl.class);

  public JCREventDAOImpl(CalendarService calService, JCRStorage storage) {
    this.context = storage;
    this.dataStorage = ((CalendarServiceImpl) calService).getDataStorage();
  }

  @Override
  public Event getById(String id) {
    try {
      return dataStorage.getEventById(id);
    } catch (Exception ex) {
      LOG.error(ex);
    }
    return null;
  }

  @Override
  public Event save(Event event) {
    return persist(event, true);
  }

  public Event update(Event event) {
    return persist(event, false);
  }

  private Event persist(Event event, boolean isNew) {
    try {
      String calendarId = event.getCalendarId();
      Calendar cal = context.getCalendarDAO().getById(calendarId);
      if (cal == null) {
        return null;
      }
      int calType = cal.getCalType();
      CalendarEvent calEvent = CalendarEvent.build(event);
      if (calType == Calendar.Type.PERSONAL.type()) {
        dataStorage.saveUserEvent(cal.getCalendarOwner(), cal.getId(), calEvent, isNew);
      } else if (calType == Calendar.Type.GROUP.type()) {
        dataStorage.savePublicEvent(cal.getId(), calEvent, isNew);
      } else {
        return null;
      }
      
      return event;
    } catch (Exception ex) {
      LOG.error(ex);
    }
    return null;
  }

  @Override
  public Event remove(String id) {
    try {
      Event event = this.getById(id);
      if (event == null) {
        return null;
      }
      Calendar cal = context.getCalendarDAO().getById(event.getCalendarId());
      int type = cal.getCalType();

      if (type == Calendar.Type.PERSONAL.type()) {
        dataStorage.removeUserEvent(cal.getCalendarOwner(), cal.getId(), id);
      } else if (type == Calendar.Type.GROUP.type()) {
        dataStorage.removePublicEvent(cal.getId(), id);
      } else {
        return null;
      }

      return event;

    } catch (Exception ex) {
      LOG.error(ex);
    }
    return null;
  }

  @Override
  public Event newInstance() {
    Event event = new Event();
    return event;
  }
  
  @Override
  public ListAccess<Event> findEventsByQuery(EventQuery query) {
    final List<CalendarEvent> events = new LinkedList<CalendarEvent>();
    org.exoplatform.calendar.service.EventQuery eventQuery = buildEvenQuery(query);

    int type = Calendar.Type.UNDEFINED.type();
    if (query instanceof JCREventQuery) {
      type = ((JCREventQuery)query).getCalType();
    }
    try {
      if (Calendar.Type.UNDEFINED.type() == type || Calendar.Type.PERSONAL.type() == type) {
        events.addAll(dataStorage.getUserEvents(query.getOwner(), eventQuery));        
      }
      
      if (Calendar.Type.UNDEFINED.type() == type || Calendar.Type.GROUP.type() == type) {
        events.addAll(dataStorage.getPublicEvents(eventQuery));
      }
    } catch (Exception ex) {
      LOG.error("Can't query for event", ex);
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

  private org.exoplatform.calendar.service.EventQuery buildEvenQuery(EventQuery query) {
    org.exoplatform.calendar.service.EventQuery eventQuery = new org.exoplatform.calendar.service.EventQuery();    
    eventQuery.setCalendarId(query.getCalendarIds());
    eventQuery.setCategoryId(query.getCategoryIds());
    eventQuery.setEventType(query.getEventType());
    eventQuery.setExcludeRepeatEvent(query.getExcludeRepeatEvent());
    eventQuery.setFilterCalendarIds(query.getFilterCalendarIds());
    if (query.getFromDate() != null) {
      java.util.Calendar from = java.util.Calendar.getInstance();
      from.setTimeInMillis(query.getFromDate());
      eventQuery.setFromDate(from);      
    }
    eventQuery.setOrderBy(query.getOrderBy());
    eventQuery.setOrderType(query.getOrderType());
    eventQuery.setParticipants(query.getParticipants());
    eventQuery.setPriority(query.getPriority());
    eventQuery.setQueryType(Query.XPATH);
    eventQuery.setState(query.getState());
    eventQuery.setText(query.getText());
    if (query.getToDate() != null) {
      java.util.Calendar to = java.util.Calendar.getInstance();
      to.setTimeInMillis(query.getToDate());      
      eventQuery.setToDate(to);
    }
    return eventQuery;
  }
}
