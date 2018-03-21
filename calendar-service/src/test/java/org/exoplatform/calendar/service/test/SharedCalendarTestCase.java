/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.calendar.service.test;

import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.service.impl.CalendarSearchServiceConnector;
import org.exoplatform.calendar.service.impl.EventSearchConnector;
import org.exoplatform.calendar.service.impl.UnifiedQuery;
import org.exoplatform.commons.api.search.data.SearchResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SharedCalendarTestCase extends BaseCalendarServiceTestCase {

    private CalendarSearchServiceConnector eventSearchConnector_ ;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        eventSearchConnector_ = getService(EventSearchConnector.class);
    }


    public void testGetTypeOfCalendar() throws Exception {
      Calendar calendar = createPrivateCalendar(username, "myCalendar", "Description");
      Calendar publicCalendar = createGroupCalendar(userGroups, "publicCalendar", "publicDescription");
      Calendar sharedCalendar = createSharedCalendar("sharedCalendar", "shareDescription");
      
      assertEquals(Utils.PRIVATE_TYPE, calendarService_.getTypeOfCalendar(username, calendar.getId()));
      assertEquals(Utils.PUBLIC_TYPE, calendarService_.getTypeOfCalendar(username, publicCalendar.getId()));
      assertEquals(Utils.SHARED_TYPE, calendarService_.getTypeOfCalendar("john", sharedCalendar.getId()));
      assertEquals(Utils.INVALID_TYPE, calendarService_.getTypeOfCalendar(username, "Not exist id"));
      
      assertEquals(sharedCalendar.getId(), calendarService_.getCalendarById(sharedCalendar.getId()).getId());
    }

    public void testSharedCalendar() throws Exception {
        Calendar cal = new Calendar();
        cal.setName("myCalendar");
        cal.setPublic(true);
        cal.setViewPermission(new String[] { "*.*" });
        cal.setEditPermission(new String[] { "*.*", "john" });

        calendarService_.saveUserCalendar(username, cal, true);

        // Share calendar
        List<String> receiverUser = new ArrayList<String>();
        receiverUser.add("john");
        calendarService_.shareCalendar(username, cal.getId(), receiverUser);
        Calendar sharedCalendar = calendarService_.getSharedCalendars("john", true).getCalendarById(cal.getId());
        assertEquals("myCalendar", sharedCalendar.getName());

        sharedCalendar.setDescription("shared description");
        calendarService_.saveSharedCalendar("john", sharedCalendar);
        Calendar editedCalendar = calendarService_.getSharedCalendars("john", true).getCalendarById(cal.getId());
        assertEquals("shared description", editedCalendar.getDescription());

        CalendarEvent calendarEvent = new CalendarEvent();
        calendarEvent.setCalendarId(cal.getId());
        calendarEvent.setSummary("calendarEvent");
        calendarEvent.setEventType(CalendarEvent.TYPE_EVENT);
        java.util.Calendar current = java.util.Calendar.getInstance() ;
        current.add(java.util.Calendar.MINUTE, 10);

        calendarEvent.setFromDateTime(current.getTime());
        current.add(java.util.Calendar.MINUTE, 30);
        calendarEvent.setToDateTime(current.getTime());

        calendarService_.saveEventToSharedCalendar("john", cal.getId(), calendarEvent, true);

        List<String> calendarIds = new ArrayList<String>();
        calendarIds.add(cal.getId());
        assertEquals(1, calendarService_.getSharedEventByCalendars("john", calendarIds).size());
        assertNotNull(calendarService_.getSharedEvent("john", cal.getId(), calendarEvent.getId()));
        CalendarEvent event = calendarService_.getUserEventByCalendar(username, calendarIds).get(0);
        assertEquals("calendarEvent", event.getSummary());

        //Test search shared event
        login("john");
        EventQuery query = new UnifiedQuery();
        query.setText("calendarEvent");
        query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
        query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
        Collection<String> params = new ArrayList<String>();
        Collection<SearchResult> rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
        assertEquals(1, rs.size());

        login(username);

        rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
        assertEquals(1, rs.size());

        calendarService_.removeSharedEvent("john", cal.getId(), calendarEvent.getId());
        List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);
        assertEquals(0, events.size());

        calendarService_.removeSharedCalendar("john", cal.getId());
        assertNull(calendarService_.getSharedCalendars("john", true));
    }

    public void testRemoveSharedCalendarFolder() throws Exception {
      createSharedCalendar("sharedCalendar", "shareDescription");
      
      calendarService_.removeSharedCalendarFolder("john");
      
      GroupCalendarData groupCalendarData = calendarService_.getSharedCalendars(username, true);
      assertNull(groupCalendarData);
    }
    
    public void testShareCalendarWithEditPermission() throws Exception {
        Calendar cal = createPrivateCalendar(username, "test share", "test sharing with edit permission");
        List<String> sharedUsers = new ArrayList<String>();
        // need to update these below values if test-portal-configuration.xml is changed
        String sharedUser = "mary";
        String sharedGroup = "/platform/guests/:*.*"; // demo is a member
        String sharedMemberShip = "/organization/management/executive-board/:*.manager"; // john is a member

        String[] sharedList = new String[]{sharedUser, sharedGroup, sharedMemberShip};
        cal.setViewPermission(sharedList);
        cal.setEditPermission(sharedList);

        sharedUsers.add(sharedUser);
        sharedUsers.addAll(Utils.getUsersCanEdit(sharedGroup));
        sharedUsers.addAll(Utils.getUsersCanEdit(sharedMemberShip));

        calendarService_.saveUserCalendar(username, cal, false);
        calendarService_.shareCalendar(username, cal.getId(), sharedUsers);

        CalendarEvent eventByMary = createCalendarEventInstance("mary");
        CalendarEvent eventByJohn = createCalendarEventInstance("john");
        CalendarEvent eventByDemo = createCalendarEventInstance("demo");

        calendarService_.saveEventToSharedCalendar("john", cal.getId(), eventByJohn, true);
        calendarService_.saveEventToSharedCalendar("mary", cal.getId(), eventByMary, true);
        calendarService_.saveEventToSharedCalendar("demo", cal.getId(), eventByDemo, true);
    }
    
    private Calendar createSharedCalendar(String name, String description) {
      try {
          Calendar sharedCalendar = new Calendar();
          sharedCalendar.setName(name);
          sharedCalendar.setDescription(description);
          sharedCalendar.setPublic(true);
          sharedCalendar.setViewPermission(new String[] { "*.*" });
          sharedCalendar.setEditPermission(new String[] { "*.*", "john" });
          calendarService_.saveUserCalendar(username, sharedCalendar, true);

          List<String> receiverUser = new ArrayList<String>();
          receiverUser.add("john");
          calendarService_.shareCalendar(username, sharedCalendar.getId(), receiverUser);

          return sharedCalendar;
      } catch (Exception e) {
          fail();
          return null;
      }
  }
}
