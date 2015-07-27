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

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarDAO;
import org.exoplatform.calendar.service.CalendarQuery;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.MultiListAccess;
import org.exoplatform.commons.utils.ListAccess;

public class CalendarHandlerImpl implements CalendarHandler {

  private List<CalendarDAO> calendarDAOs;
  
  public CalendarHandlerImpl(List<CalendarDAO> calendarDAOs) {
    this.calendarDAOs = calendarDAOs;
  }

  @Override
  public Calendar getCalendarById(String calId) {
    return getCalendarById(calId, null);
  }
  
  @Override
  public Calendar getCalendarById(String calId, CalendarType calType) {
    for (CalendarDAO dao : getDAOByType(calType)) {
      return dao.getCalendarById(calId, calType);
    }
    return null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public ListAccess findCalendarsByQuery(CalendarQuery query) {
    MultiListAccess lists = new MultiListAccess();
    List<CalendarDAO> daos = new LinkedList<CalendarDAO>();
    
    if (query == null) {
      daos = calendarDAOs;
    } else {
      daos = getDAOByType(query.getCalType());
    }
    
    for (CalendarDAO dao : daos) {
      lists.add(dao.findCalendarsByQuery(query));
    }

    return lists;
  }
  
  @Override
  public Calendar saveCalendar(Calendar calendar, boolean isNew) {
    for (CalendarDAO dao : getDAOByType(calendar.getCalendarType())) {
      return dao.saveCalendar(calendar, isNew);
    }

    return null;
  }

  @Override
  public Calendar removeCalendar(String calendarId, CalendarType calType) {
    for (CalendarDAO dao : getDAOByType(calType)) {
      return dao.removeCalendar(calendarId, calType);
    }
    
    return null;
  }
  
  private List<CalendarDAO> getDAOByType(CalendarType type) {
    if (type == null) {
      return calendarDAOs;
    } else {
      List<CalendarDAO> daos = new LinkedList<CalendarDAO>();
      for (CalendarDAO dao : calendarDAOs) {
        if (dao.getCalendarTypes().contains(type)) {
          daos.add(dao);
        }
      }
      
      return daos;      
    }
  }
}
