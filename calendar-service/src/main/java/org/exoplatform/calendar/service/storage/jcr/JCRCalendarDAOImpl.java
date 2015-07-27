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

package org.exoplatform.calendar.service.storage.jcr;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarQuery;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class JCRCalendarDAOImpl implements CalendarDAO {
  
  private Storage context;
  private JCRDataStorage            dataStorage;

  private static final Log          LOG   = ExoLogger.getExoLogger(JCRCalendarDAOImpl.class);  

  public JCRCalendarDAOImpl(CalendarService service, JCRStorage context) {
    this.context = context;
    this.dataStorage = ((CalendarServiceImpl) service).getDataStorage();
  }
  
  @Override
  public Calendar getById(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Calendar getById(String id, CalendarType calType) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Calendar save(Calendar object, boolean isNew) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Calendar remove(String id, CalendarType calType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Calendar newInstance(CalendarType type) {
    // TODO Auto-generated method stub
    return null;
  }

//  @Override
//  public Calendar getCalendarById(String calId) {
//    try {
//      return dataStorage.getCalendarById(calId);
//    } catch (Exception e) {
//      LOG.error(e);
//      return null;
//    }
//  }
//
//  @Override
//  public Calendar getCalendarById(String calId, CalendarType calType) {
//    return getCalendarById(calId);
//  }
//
  @Override
  public ListAccess findCalendarsByQuery(final CalendarQuery query) {
//    String userName = null;
//    if (query == null
//        || (query.getUserName() == null && (query.getCalType() == Calendar.TYPE_ALL || query.getCalType() == Calendar.TYPE_PRIVATE))) {
//      ConversationState state = ConversationState.getCurrent();
//      if (state != null) {
//        userName = state.getIdentity().getUserId();
//      }
//    } else {
//      userName = query.getUserName();
//    }
//
//    final String name = userName;
//    if (query == null || query.getCalType() == Calendar.TYPE_ALL) {
//      return new ListAccess<Calendar>() {
//        private CalendarCollection<Calendar> calendars;
//
//        @Override
//        public int getSize() throws Exception {
//          if (calendars == null) {
//            calendars = service.getAllCalendars(name, Calendar.TYPE_ALL, 0, -1);
//          }
//          return (int) calendars.getFullSize();
//        }
//
//        @Override
//        public Calendar[] load(int offset, int limit) throws Exception, IllegalArgumentException {
//          calendars = service.getAllCalendars(name, Calendar.TYPE_ALL, offset, limit);
//
//          return calendars.toArray(new Calendar[calendars.size()]);
//        }
//      };
//    } else {
//      final List<Calendar> calendars = new LinkedList<Calendar>();
//      try {
//        switch (query.getCalType()) {
//        case Calendar.TYPE_PRIVATE:
//          calendars.addAll(dataStorage.getUserCalendars(name, true));
//          break;
//        case Calendar.TYPE_PUBLIC:
//          String[] groups = new String[0];
//          if (query.getGroups() != null) {
//            groups = query.getGroups().toArray(new String[query.getGroups().size()]);
//          }
//          List<GroupCalendarData> data = dataStorage.getGroupCalendars(groups, true, name);
//          for (GroupCalendarData d : data) {
//            calendars.addAll(d.getCalendars());
//          }
//          break;
//        default:
//          throw new IllegalStateException("calendar type not support: " + query.getCalType());
//        }
//      } catch (Exception e) {
//        LOG.error(e);
//      }
//
//      return new ListAccess<Calendar>() {
//        @Override
//        public int getSize() throws Exception {
//          return calendars.size();
//        }
//
//        @Override
//        public Calendar[] load(int offset, int limit) throws Exception, IllegalArgumentException {
//          return Utils.subList(calendars, offset, limit).toArray(new Calendar[limit]);
//        }
//      };
//    }
    return null;
  }
//
//  @Override
//  public Calendar saveCalendar(Calendar calendar, boolean isNew) {
//    try {
//      switch (calendar.getCalType()) {
//      case Calendar.TYPE_PRIVATE:
//        dataStorage.saveUserCalendar(calendar.getCalendarOwner(), calendar, isNew);
//        break;
//      case Calendar.TYPE_PUBLIC:
//        dataStorage.savePublicCalendar(calendar, isNew, null);
//        break;
//      default:
//        throw new IllegalStateException("calendar type not supported " + calendar.getCalType());
//      }
//      return getCalendarById(calendar.getId());
//    } catch (Exception e) {
//      LOG.error(e);
//      return null;
//    }
//  }
//
//  @Override
//  public Calendar removeCalendar(String calendarId, CalendarType calType) {
//    try {
//      switch (calType) {
//      case Calendar.TYPE_PRIVATE:
//        Calendar cal = getCalendarById(calendarId);
//        if (cal != null) {
//          dataStorage.removeUserCalendar(cal.getCalendarOwner(), calendarId);          
//        }
//        break;
//      case Calendar.TYPE_PUBLIC:
//        dataStorage.removeGroupCalendar(calendarId);
//        break;
//      default:
//        throw new IllegalStateException("calendar type not supported " + calType);
//      }      
//    } catch (Exception ex) {
//      LOG.error(ex);
//    }
//    return null;
//  }
//
//  @Override
//  public Calendar newCalendarInstance(CalendarType type) {
//    return new Calendar();
//  }

}
