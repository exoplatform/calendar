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
  
package org.exoplatform.calendar.service.handler;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarQuery;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.MultiListAccess;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.commons.utils.ListAccess;

public class CalendarHandlerImpl implements CalendarHandler {

  private CalendarServiceImpl service;

  public CalendarHandlerImpl(CalendarServiceImpl service) {
    this.service = service;
  }

  @Override
  public Calendar getCalendarById(String calId) {
    return getCalendarById(calId, null);
  }
  
  @Override
  public Calendar getCalendarById(String calId, CalendarType calType) {
    for (CalendarDAO dao : getCalendarDAO(calType)) {
      return dao.getById(calId, calType);
    }
    return null;
  }

  @Override
  public ListAccess findCalendarsByQuery(CalendarQuery query) {
    MultiListAccess lists = new MultiListAccess();    
    
    for (CalendarDAO dao : getCalendarDAO(query.getCalType())) {
      lists.add(dao.findCalendarsByQuery(query));
    }

    return lists;
  }
  
  @Override
  public Calendar saveCalendar(Calendar calendar, boolean isNew) {
    for (CalendarDAO dao : getCalendarDAO(calendar.getCalendarType())) {
      return dao.save(calendar, isNew);
    }

    return null;
  }

  @Override
  public Calendar removeCalendar(String calendarId, CalendarType calType) {
    for (CalendarDAO dao : getCalendarDAO(calType)) {
      return dao.remove(calendarId, calType);
    }
    
    return null;
  }
  
  private List<CalendarDAO> getCalendarDAO(CalendarType type) {
    return service.getCalendarDAO(type);
  }
}
