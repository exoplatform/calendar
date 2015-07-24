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
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.MultiListAccess;
import org.exoplatform.commons.utils.ListAccess;

public class CalendarHandlerImpl implements CalendarHandler {

  @Override
  public Calendar getCalendarById(String calId) {
    Calendar cal = null;
    
//    for (CalendarDAO dao : calendarDAOs) {
//      cal = dao.getCalendarById(calId);
//      if (cal != null) break;
//    }
    return cal;
  }
  
  @Override
  public Calendar getCalendarById(String calId, int calType) {
//    for (CalendarDAO dao : calendarDAOs) {
//      if (dao.getCalendarTypes().contains(calType)) {
//        return dao.getCalendarById(calId, calType);
//      }
//    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ListAccess findCalendarsByQuery(CalendarQuery query) {
    MultiListAccess lists = new MultiListAccess();
//    if (query == null || query.getCalType() == Calendar.TYPE_ALL) {
//      for (CalendarDAO dao : calendarDAOs) {
//        lists.add(dao.findCalendarsByQuery(query));
//      }
//    } else {
//      for (CalendarDAO dao : calendarDAOs) {
//        if (dao.getCalendarTypes().contains(query.getCalType())) {
//          return dao.findCalendarsByQuery(query);
//        }
//      }
//    }

    return lists;
  }
  
  @Override
  public Calendar saveCalendar(Calendar calendar, boolean isNew) {
//    for (CalendarDAO dao : calendarDAOs) {
//      if (dao.getCalendarTypes().contains(calendar.getCalType())) {
//        return dao.saveCalendar(calendar, isNew);
//      }
//    }
//    
    return null;
  }

  @Override
  public ListAccess<Calendar> getPublicCalendars() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void shareCalendar(String username, String calendarId, List<String> sharedUsers) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeCalendar(String calendarId, int calType) {
//    for (CalendarDAO dao : calendarDAOs) {
//      if (dao.getCalendarTypes().contains(calType)) {
//        dao.removeCalendar(calendarId, calType);
//      }
//    }
  }
  
}
