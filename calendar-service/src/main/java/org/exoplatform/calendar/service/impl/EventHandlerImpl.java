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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.exoplatform.calendar.model.Calendar;
import org.exoplatform.calendar.model.CompositeID;
import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.EventHandler;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.storage.EventDAO;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.calendar.storage.jcr.JCRStorage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.cache.future.Loader;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventHandlerImpl implements EventHandler {

  private static Log log = ExoLogger.getLogger(EventHandlerImpl.class);

  protected ExtendedCalendarServiceImpl calService;

  protected FutureExoCache<String, String, ExtendedCalendarServiceImpl> dsNameByCalId = null;

  public EventHandlerImpl(ExtendedCalendarServiceImpl service, CacheService cacheService) {
    this.calService = service;
    ExoCache<String, String> dsNameByCalIdCache = cacheService.getCacheInstance("calendar.dsNameById");
    dsNameByCalId = new FutureExoCache<String, String, ExtendedCalendarServiceImpl>(new CalDSNameLoader(), dsNameByCalIdCache);
  }

  @Override
  public Event getEventById(String eventId) {    
    CompositeID composID = CompositeID.parse(eventId);
    if (composID.getDS() != null) {
      EventDAO dao = getEventDAOImpl(composID.getDS());
      if (dao != null) {
        return dao.getById(composID.getId());
      }      
    } else {
      for (Storage storage : calService.getAllStorage()) {
        EventDAO dao = storage.getEventDAO();
        if (dao != null) {
          Event evt =  dao.getById(composID.getId());
          if (evt != null) {
            return evt;
          }
        }
      }
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
    String[] calendarIds = eventQuery.getCalendarIds();
    if (eventQuery.getDS() == null && calendarIds != null && calendarIds.length > 0) {
      Set<String> allCalIds = Arrays.stream(calendarIds).collect(Collectors.toSet());
      Map<String, List<String>> computedCalIdByDS = new HashMap<String, List<String>>();

      for (String calendarId : calendarIds) {
        String ds = dsNameByCalId.get(calService, calendarId);
        if (ds == null) {
          if(log.isDebugEnabled()) {
            log.warn("Can't find a store for cal id '{}'", calendarId);
          }
          ds = JCRStorage.JCR_STORAGE;
        }
        addCalendarIdToDSMap(calendarId, ds, allCalIds, computedCalIdByDS);
      }

      List<ListAccess<Event>> result = new LinkedList<ListAccess<Event>>();
      for (String dsName : computedCalIdByDS.keySet()) {
        List<String> calIdsListByDSName = computedCalIdByDS.get(dsName);
        eventQuery.setDS(dsName);
        eventQuery.setCalendarIds(calIdsListByDSName.toArray(new String[0]));
        EventDAO dao = calService.lookForDS(dsName).getEventDAO();
        ListAccess<Event> tmp = dao.findEventsByQuery(eventQuery);
        if (tmp != null) {
          result.add(tmp);
        }
      }
      return mergeListAccesses(result);
    }
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

    return mergeListAccesses(result);
  }

  private ListAccess<Event> mergeListAccesses(List<ListAccess<Event>> result) {
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

  private void addCalendarIdToDSMap(String calendarId,
                                    String ds,
                                    Set<String> allCalIds,
                                    Map<String, List<String>> computedCalIdByDS) {
    List<String> computedCalIdList = computedCalIdByDS.get(ds);
    if (computedCalIdList == null) {
      computedCalIdList = new ArrayList<String>();
      computedCalIdByDS.put(ds, computedCalIdList);
    } else if (computedCalIdList.contains(calendarId)) {
      allCalIds.remove(calendarId);
      return;
    }
    computedCalIdList.add(calendarId);
    allCalIds.remove(calendarId);
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

  private final class CalDSNameLoader implements Loader<String, String, ExtendedCalendarServiceImpl> {

    /**
    * Retrieves the originating datasource for a given calendarId.
    * If no DS name found, the default one will be used
    *
    * @param calService the CalendarService
    * @param calendarId the calendarId
    * @return the originating datasource for a given calendarId
    * @throws Exception any exception that would prevent the value to be loaded
     */
    @Override
    public String retrieve(ExtendedCalendarServiceImpl calService, String calendarId) throws Exception {
      CompositeID composId = CompositeID.parse(calendarId);
      String ds = composId.getDS();
      if (log.isDebugEnabled()) {
        log.warn("Calendar id '{}' hasn't store definition, search information from store", calendarId);
      }
      Calendar calendar = calService.getCalendarHandler().getCalendarById(calendarId);
      if(calendar == null) {
        return null;
      }
      ds = calendar.getDS();
      if(ds == null) {
        if (log.isDebugEnabled()) {
          log.warn("Retrieved calendar '{}' from stores hasn't a DS definition, use default one");
        }
        ds = JCRStorage.JCR_STORAGE;
      }
      return ds;
    }
  }
}
