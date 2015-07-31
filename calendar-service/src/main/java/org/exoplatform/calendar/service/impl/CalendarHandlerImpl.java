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
import org.exoplatform.calendar.service.CalendarQuery;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.NoSuchEntityException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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
  public ListAccess<Calendar> findCalendarsByQuery(CalendarQuery query) {
    CalendarDAO dao = getCalendarDAO(query.getCalType());
    if (dao != null) {
      return dao.findCalendarsByQuery(query);
    }
    return null;
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
  
  private CalendarDAO getCalendarDAO(CalendarType type) {
    return service.getCalendarDAO(type);
  }
}
