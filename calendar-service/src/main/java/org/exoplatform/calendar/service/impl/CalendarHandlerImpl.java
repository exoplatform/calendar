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

import org.exoplatform.calendar.model.CompositeID;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.service.CalendarHandler;
import org.exoplatform.calendar.storage.CalendarDAO;
import org.exoplatform.calendar.storage.Storage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class CalendarHandlerImpl implements CalendarHandler {
  private static final Log log = ExoLogger.getExoLogger(CalendarHandlerImpl.class);

  private ExtendedCalendarServiceImpl service;

  public CalendarHandlerImpl(ExtendedCalendarServiceImpl service) {
    this.service = service;
  }

  @Override
  public org.exoplatform.calendar.model.Calendar getCalendarById(String compositeId) {
    CompositeID composId = CompositeID.parse(compositeId);
    if (composId.getDS() != null) {
      Storage storage = service.lookForDS(composId.getDS());
      CalendarDAO dao = storage.getCalendarDAO();
      if (dao != null) {
        org.exoplatform.calendar.model.Calendar cal = dao.getById(composId.getId());
        if (cal != null) {
          return cal;
        }
      }      
    } else {
      for (Storage storage : service.getAllStorage()) {
        CalendarDAO dao = storage.getCalendarDAO();
        if (dao != null) {
          org.exoplatform.calendar.model.Calendar cal = dao.getById(composId.getId());
          if (cal != null) {
            return cal;
          }
        }
      }
    }
    return null;
  }

  @Override
  public List<org.exoplatform.calendar.model.Calendar> findCalendars(CalendarQuery query) {
    List<org.exoplatform.calendar.model.Calendar> calendars = new LinkedList<org.exoplatform.calendar.model.Calendar>();
    for (Storage storage : service.getAllStorage()) {
      List<org.exoplatform.calendar.model.Calendar> cals = storage.getCalendarDAO().findCalendars(query);
      if (cals != null) {
        calendars.addAll(cals);
      }
    }
    return calendars;
  }

  @Override
  public org.exoplatform.calendar.model.Calendar saveCalendar(org.exoplatform.calendar.model.Calendar calendar) {
    Storage storage = service.lookForDS(calendar.getDS());
    CalendarDAO dao = storage.getCalendarDAO();
    if (dao != null) {
      org.exoplatform.calendar.model.Calendar cal = dao.save(calendar); 
      if (cal != null) {
        return cal;
      }
    }

    return null;
  }
  
  @Override
  public org.exoplatform.calendar.model.Calendar updateCalendar(org.exoplatform.calendar.model.Calendar calendar) {
    Storage storage = service.lookForDS(calendar.getDS());
    CalendarDAO dao = storage.getCalendarDAO();
    if (dao != null) {
      return dao.update(calendar);
    }

    return null;
  }

  @Override
  public org.exoplatform.calendar.model.Calendar removeCalendar(String compositeId) {
    CompositeID composId = CompositeID.parse(compositeId);
    Storage storage = service.lookForDS(composId.getDS());
    CalendarDAO dao = storage.getCalendarDAO();
    if (dao != null){
      org.exoplatform.calendar.model.Calendar cal = dao.remove(composId.getId());
      if (cal != null) {
        return cal;
      }
    }
    
    return null;
  }

  @Override
  public org.exoplatform.calendar.model.Calendar newCalendarInstance(String dsId) {
    CalendarDAO dao = service.lookForDS(dsId).getCalendarDAO();
    if (dao != null) {
      return dao.newInstance();
    }
    return null;
  }
}
