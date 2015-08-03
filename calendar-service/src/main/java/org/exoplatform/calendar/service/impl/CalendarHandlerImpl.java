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

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarHandler;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.NoSuchEntityException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.services.security.Identity;
import java.util.LinkedList;
import java.util.List;

public class CalendarHandlerImpl implements CalendarHandler {
  private static final Log log = ExoLogger.getExoLogger(CalendarHandlerImpl.class);

  private CalendarServiceImpl service;

  public CalendarHandlerImpl(CalendarServiceImpl service) {
    this.service = service;
  }

  @Override
  public Calendar getCalendarById(String calId, CalendarType calType) {
    CalendarDAO dao = getCalendarDAO(calType);
    if (dao != null) {
      Calendar cal = dao.getById(calId, calType);
      if (cal != null) {
        return cal;
      }
    }
    return null;
  }

  @Override
  public List<Calendar> findCalendarsByIdentity(Identity identity, CalendarType type, String[] excludeIds) {
    CalendarDAO dao = getCalendarDAO(type);
    if (dao != null) {
      if (excludeIds == null) {
        excludeIds = new String[0];
      }
      return dao.findCalendarsByIdentity(identity, type, excludeIds);
    }
    return null;
  }

  @Override
  public List<Calendar> findAllCalendarOfUser(Identity identity, String[] excludeIds) {
    List<Calendar> calendars = new LinkedList<Calendar>();
    for (Storage storage : service.getAllStorage()) {
      List<Calendar> cals = storage.getCalendarDAO().findCalendarsByIdentity(identity, null, excludeIds);
      if (cals != null) {
        calendars.addAll(cals);
      }
    }
    return calendars;
  }

  @Override
  public Calendar saveCalendar(Calendar calendar) {
    CalendarDAO dao = getCalendarDAO(calendar.getCalendarType());
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
    CalendarDAO dao = getCalendarDAO(calendar.getCalendarType());
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
  public Calendar removeCalendar(String calendarId, CalendarType calType) {
    CalendarDAO dao = getCalendarDAO(calType);
    if (dao != null){
      Calendar cal = dao.remove(calendarId, calType);
      if (cal != null) {
        return cal;        
      }
    }
    
    return null;
  }

  @Override
  public Calendar newCalendarInstance(CalendarType calendarType) {
    CalendarDAO dao = getCalendarDAO(calendarType);
    if (dao != null) {
      return dao.newInstance(calendarType);
    }
    return null;
  }

  private CalendarDAO getCalendarDAO(CalendarType type) {
    return service.getCalendarDAO(type);
  }
}
