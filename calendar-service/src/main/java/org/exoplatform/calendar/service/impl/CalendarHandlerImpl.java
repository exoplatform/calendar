/**
 * Copyright (C) 2015 eXo Platform SAS.
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

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarHandler;
import org.exoplatform.calendar.service.CompositeID;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.NoSuchEntityException;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;

public class CalendarHandlerImpl implements CalendarHandler {
  private static final Log log = ExoLogger.getExoLogger(CalendarHandlerImpl.class);

  private CalendarServiceImpl service;

  public CalendarHandlerImpl(CalendarServiceImpl service) {
    this.service = service;
  }

  @Override
  public Calendar getCalendarById(String compositeId) {
    CompositeID composId = CompositeID.parse(compositeId);
    Storage storage = service.lookForDS(composId.getDS());
    CalendarDAO dao = storage.getCalendarDAO();
    if (dao != null) {
      Calendar cal = dao.getById(composId.getId());
      if (cal != null) {
        return cal;
      }
    }
    return null;
  }

  @Override
  public List<Calendar> findAllCalendarOfUser(Identity identity, String[] excludeIds) {
    List<Calendar> calendars = new LinkedList<Calendar>();
    for (Storage storage : service.getAllStorage()) {
      List<Calendar> cals = storage.getCalendarDAO().findCalendarsByIdentity(identity, excludeIds);
      if (cals != null) {
        calendars.addAll(cals);
      }
    }
    return calendars;
  }

  @Override
  public Calendar saveCalendar(Calendar calendar) {
    Storage storage = service.lookForDS(calendar.getDS());
    CalendarDAO dao = storage.getCalendarDAO();
    if (dao != null) {
      Calendar cal = dao.save(calendar); 
      if (cal != null) {
        return cal;
      }
    }

    return null;
  }
  
  @Override
  public Calendar updateCalendar(Calendar calendar) {
    Storage storage = service.lookForDS(calendar.getDS());
    CalendarDAO dao = storage.getCalendarDAO();
    if (dao != null) {
      try {
        return dao.update(calendar);
      } catch (NoSuchEntityException e) {
        log.error("Can't update calendar", e);
      }
    }

    return null;
  }

  @Override
  public Calendar removeCalendar(String compositeId) {
    CompositeID composId = CompositeID.parse(compositeId);
    Storage storage = service.lookForDS(composId.getDS());
    CalendarDAO dao = storage.getCalendarDAO();
    if (dao != null){
      Calendar cal = dao.remove(compositeId);
      if (cal != null) {
        return cal;
      }
    }
    
    return null;
  }

  @Override
  public Calendar newCalendarInstance(String dsId) {
    CalendarDAO dao = service.lookForDS(dsId).getCalendarDAO();
    if (dao != null) {
      return dao.newInstance();
    }
    return null;
  }
}
