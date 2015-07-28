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
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.service.storage.CalendarDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCRCalendarDAOImpl implements CalendarDAO {
  
  private Storage context;
  private JCRDataStorage            dataStorage;

  private static final Log          LOG   = ExoLogger.getExoLogger(JCRCalendarDAOImpl.class);

  private static final ListAccess<Calendar> EMPTY = new ListAccess<Calendar>() {
    @Override
    public Calendar[] load(int i, int i2) throws Exception, IllegalArgumentException {
      return new Calendar[0];
    }

    @Override
    public int getSize() throws Exception {
      return 0;
    }
  };

  public JCRCalendarDAOImpl(CalendarService service, JCRStorage context) {
    this.context = context;
    this.dataStorage = ((CalendarServiceImpl) service).getDataStorage();
  }
  
  @Override
  public Calendar getById(String id) {
    try {
      return dataStorage.getCalendarById(id);
    } catch (Exception ex) {
      LOG.error("Exception while loading calendar by ID", ex);
      return null;
    }
  }

  @Override
  public Calendar getById(String id, CalendarType calType) {
    return this.getById(id);
  }
  
  @Override
  public Calendar save(Calendar calendar, boolean isNew) {
    CalendarType type = calendar.getCalendarType();

    if (type == Calendar.Type.PERSONAL) {
      try {
        dataStorage.saveUserCalendar(calendar.getCalendarOwner(), calendar, isNew);
      } catch (Exception ex) {
        LOG.error(ex);
      }
    } else if (type == Calendar.Type.GROUP) {
      try {
        dataStorage.savePublicCalendar(calendar, isNew, null);
      } catch (Exception ex) {
        LOG.error(ex);
      }
    } else {
      throw new UnsupportedOperationException("Save calendar with type '" + type + "' is not supported");
    }

    return calendar;
  }

  @Override
  public Calendar remove(String id, CalendarType calType) {
    Calendar calendar = getById(id, calType);
    if (calendar == null) {
      return null;
    }

    if (calType == Calendar.Type.PERSONAL) {
      try {
        dataStorage.removeUserCalendar(calendar.getCalendarOwner(), id);
      } catch (Exception ex) {
        LOG.error(ex);
      }
    } else if (calType == Calendar.Type.GROUP) {
      try {
        dataStorage.removeGroupCalendar(id);
      } catch (Exception ex) {
        LOG.error(ex);
      }
    }
    return calendar;
  }

  @Override
  public Calendar newInstance(CalendarType type) {
    Calendar c = new Calendar();
    c.setCalendarType(type);
    return c;
  }

  @Override
  public ListAccess findCalendarsByQuery(final CalendarQuery query) {
    if (query == null || query.getCalType() == null) {
      return EMPTY;
    }

    CalendarType type = query.getCalType();
    if (type == Calendar.Type.PERSONAL) {
      String username = query.getUserName();
      if (username == null || username.isEmpty()) {
        return EMPTY;
      }
      try {
        List<Calendar> calendars = dataStorage.getUserCalendars(username, query.isShowAll());
        return new ListAccessImpl(Calendar.class, calendars);
      } catch (Exception ex) {
        LOG.error(ex);
        return EMPTY;
      }

    } else if (type == Calendar.Type.GROUP) {
      List<String> groups = query.getGroups();
      if (groups == null || groups.isEmpty()) {
        return EMPTY;
      }
      try {
        List<GroupCalendarData> data = dataStorage.getGroupCalendars(groups.toArray(new String[0]), query.isShowAll(), query.getUserName());
        List<Calendar> calendars = new ArrayList<Calendar>();
        for (GroupCalendarData cd : data) {
          calendars.addAll(cd.getCalendars());
        }
        return new ListAccessImpl(Calendar.class, calendars);
      } catch (Exception ex) {
        LOG.error(ex);
        return EMPTY;
      }

    } else {
      return EMPTY;
    }

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
    //return null;
  }
}
