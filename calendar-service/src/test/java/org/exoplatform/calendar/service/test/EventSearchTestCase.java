package org.exoplatform.calendar.service.test;

import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.impl.NewUserListener;

import java.util.*;

/**
 *
 * Created by tuyennt on 11/13/14.
 */
public class EventSearchTestCase extends BaseCalendarServiceTestCase {

  public void testSearchUserEventWithCategory() throws Exception {
    EventCategory category1 = createUserEventCategory(username, "event category 1");
    EventCategory category2 = createUserEventCategory(username, "event category 2");

    // Private calendar
    Calendar calendar = createPrivateCalendar(username, "testSearchEventWithCategory", "testSearchEventWithCategory");


    java.util.Calendar from = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 30);
    java.util.Calendar to = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 60);

    // Create event
    CalendarEvent event = createUserEvent(calendar.getId(), category1, "My testSearchEventWithCategory event", false, from, to);

    EventQuery query = new EventQuery();
    query.setText("testSearchEventWithCategory");

    // Do search
    query.setCategoryId(new String[] {category2.getId()});
    List<CalendarEvent> list = calendarService_.searchEvent(username, query, new String[0]).getAll();
    assertNotContain(event.getId(), list);
    list = calendarService_.getEvents(username, query, new String[0]);
    assertNotContain(event.getId(), list);

    query.setCategoryId(new String[] {category1.getId()});
    list = calendarService_.searchEvent(username, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(username, query, new String[0]);
    assertContain(event.getId(), list);

    query.setCategoryId(new String[] {category2.getId(), category1.getId()});
    list = calendarService_.searchEvent(username, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(username, query, new String[0]);
    assertContain(event.getId(), list);

    //. With all category
    query.setCategoryId(new String[] {CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.searchEvent(username, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(username, query, new String[0]);
    assertContain(event.getId(), list);

    query.setCategoryId(new String[] {category2.getId(), CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.searchEvent(username, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(username, query, new String[0]);
    assertContain(event.getId(), list);
  }

  public void testSearchSharedEventWithCategory() throws Exception {
    final String user = "john";
    // Create and share calendar with john
    Calendar calendar = createPrivateCalendar(username, "testSearchSharedEventWithCategory", "testSearchSharedEventWithCategory");
    calendarService_.shareCalendar(username, calendar.getId(), Arrays.asList(user));
    calendar.setViewPermission(new String[]{user});
    calendar.setEditPermission(new String[]{user});
    calendarService_.saveUserCalendar(username, calendar, false);

    login(user);

    EventCategory category1 = createUserEventCategory(user, "event category 1");
    EventCategory category2 = createUserEventCategory(user, "event category 2");

    // Create calendar
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 30);
    java.util.Calendar to = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 60);

    // Create event
    CalendarEvent event = createUserEvent(calendar.getId(), category1, "My testSearchSharedEventWithCategory event", false, from, to);

    EventQuery query = new EventQuery();
    query.setText("testSearchSharedEventWithCategory");

    // Do search
    query.setCategoryId(new String[] {category2.getId()});
    List<CalendarEvent> list = calendarService_.searchEvent(user, query, new String[0]).getAll();
    assertNotContain(event.getId(), list);
    list = calendarService_.getEvents(user, query, new String[0]);
    assertNotContain(event.getId(), list);

    query.setCategoryId(new String[]{category1.getId()});
    list = calendarService_.searchEvent(user, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(user, query, new String[0]);
    assertContain(event.getId(), list);

    query.setCategoryId(new String[] {category2.getId(), category1.getId()});
    list = calendarService_.searchEvent(user, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(user, query, new String[0]);
    assertContain(event.getId(), list);

    //. With all category
    query.setCategoryId(new String[] {CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.searchEvent(user, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(user, query, new String[0]);
    assertContain(event.getId(), list);

    query.setCategoryId(new String[] {category2.getId(), CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.searchEvent(user, query, new String[0]).getAll();
    assertContain(event.getId(), list);
    list = calendarService_.getEvents(user, query, new String[0]);
    assertContain(event.getId(), list);
  }

  public void testSearchPublicEventWithCategory() throws Exception {
    //. Create a group calendar
    final String group = "/platform/user";
    Calendar calendar = createGroupCalendar(new String[]{group}, "testSearchPublicEventWithCategory", "testSearchPublicEventWithCategory");

    EventCategory category1 = createUserEventCategory(username, "event category 1");
    EventCategory category2 = createUserEventCategory(username, "event category 2");

    java.util.Calendar from = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 30);
    java.util.Calendar to = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 60);

    // Create event
    CalendarEvent event = createGroupEvent(calendar.getId(), category1, "My testSearchPublicEventWithCategory event", false, from, to);

    EventQuery query = new EventQuery();
    query.setText("testSearchPublicEventWithCategory");

    // Do search
    query.setCategoryId(new String[] {category2.getId()});
    List<CalendarEvent> list = calendarService_.searchEvent(username, query, new String[]{calendar.getId()}).getAll();
    assertNotContain(event.getId(), list);
    list = calendarService_.getEvents(username, query, new String[]{calendar.getId()});
    assertNotContain(event.getId(), list);

    query.setCalendarId(null);
    query.setCategoryId(new String[]{category1.getId()});
    list = calendarService_.searchEvent(username, query, new String[]{calendar.getId()}).getAll();
    assertContain(event.getId(), list);
    query.setCalendarId(null);
    list = calendarService_.getEvents(username, query, new String[]{calendar.getId()});
    assertContain(event.getId(), list);

    query.setCalendarId(null);
    query.setCategoryId(new String[] {category2.getId(), category1.getId()});
    list = calendarService_.searchEvent(username, query, new String[]{calendar.getId()}).getAll();
    assertContain(event.getId(), list);
    query.setCalendarId(null);
    list = calendarService_.getEvents(username, query, new String[]{calendar.getId()});
    assertContain(event.getId(), list);

    //. With all category
    query.setCalendarId(null);
    query.setCategoryId(new String[] {CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.searchEvent(username, query, new String[]{calendar.getId()}).getAll();
    assertContain(event.getId(), list);
    query.setCalendarId(null);
    list = calendarService_.getEvents(username, query, new String[]{calendar.getId()});
    assertContain(event.getId(), list);

    query.setCalendarId(null);
    query.setCategoryId(new String[] {category2.getId(), CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.searchEvent(username, query, new String[]{calendar.getId()}).getAll();
    assertContain(event.getId(), list);
    query.setCalendarId(null);
    list = calendarService_.getEvents(username, query, new String[]{calendar.getId()});
    assertContain(event.getId(), list);
  }

  public void testGetAllNoRepeatEventWithCategory() throws Exception {
    final String user = "john";
    // Create and share calendar with john
    Calendar sharedCalendar = createPrivateCalendar(username, "testAllNoRepeatEventWithCategory", "testAllNoRepeatEventWithCategory");
    calendarService_.shareCalendar(username, sharedCalendar.getId(), Arrays.asList(user));
    sharedCalendar.setViewPermission(new String[]{user});
    sharedCalendar.setEditPermission(new String[]{user});
    calendarService_.saveUserCalendar(username, sharedCalendar, false);

    login(user);
    EventCategory category1 = createUserEventCategory(user, "event category 1");
    EventCategory category2 = createUserEventCategory(user, "event category 2");

    // Create calendar
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 30);
    java.util.Calendar to = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 60);

    // Create calendar
    Calendar calendar = createPrivateCalendar(user, "testAllNoRepeatEventWithCategory", "testAllNoRepeatEventWithCategory");
    Calendar groupCalendar = createGroupCalendar(new String[]{"/platform/user"}, "group calendar", "group calendar");

    // Create event
    CalendarEvent event = createUserEvent(user, calendar.getId(), category1, "My testAllNoRepeatEventWithCategory event", false, from, to);
    CalendarEvent sharedEvent = createUserEvent(sharedCalendar.getId(), category1, "My testAllNoRepeatEventWithCategory shared event", false, from, to);
    CalendarEvent groupEvent = createGroupEvent(groupCalendar.getId(), category1, "My testAllNoRepeatEventWithCategory group event", false, from, to);

    EventQuery query = new EventQuery();
    query.setText("testAllNoRepeatEventWithCategory");
    from.add(java.util.Calendar.DATE, -2);
    to.add(java.util.Calendar.DATE, 2);
    query.setFromDate(from);
    query.setToDate(to);

    // Do search
    query.setCalendarId(null);
    query.setCategoryId(new String[] {category2.getId()});
    List<CalendarEvent> list = calendarService_.getAllNoRepeatEventsSQL(user, query, new String[]{calendar.getId()}, new String[]{groupCalendar.getId()}, Collections.<String>emptyList());
    assertNotContain(event.getId(), list);
    assertNotContain(sharedEvent.getId(), list);
    assertNotContain(groupEvent.getId(), list);

    // Do search
    query.setCalendarId(null);
    query.setCategoryId(new String[] {category1.getId()});
    list = calendarService_.getAllNoRepeatEventsSQL(user, query, new String[]{calendar.getId()}, new String[]{groupCalendar.getId()}, Collections.<String>emptyList());
    assertContain(event.getId(), list);
    assertContain(sharedEvent.getId(), list);
    assertContain(groupEvent.getId(), list);

    query.setCalendarId(null);
    query.setCategoryId(new String[] {category2.getId(), category1.getId()});
    list = calendarService_.getAllNoRepeatEventsSQL(user, query, new String[]{calendar.getId()}, new String[]{groupCalendar.getId()}, Collections.<String>emptyList());
    assertContain(event.getId(), list);
    assertContain(sharedEvent.getId(), list);
    assertContain(groupEvent.getId(), list);

    query.setCalendarId(null);
    query.setCategoryId(new String[] {CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.getAllNoRepeatEventsSQL(user, query, new String[]{calendar.getId()}, new String[]{groupCalendar.getId()}, Collections.<String>emptyList());
    assertContain(event.getId(), list);
    assertContain(sharedEvent.getId(), list);
    assertContain(groupEvent.getId(), list);

    query.setCalendarId(null);
    query.setCategoryId(new String[] {category2.getId(), CalendarService.DEFAULT_EVENTCATEGORY_ID_ALL});
    list = calendarService_.getAllNoRepeatEventsSQL(user, query, new String[]{calendar.getId()}, new String[]{groupCalendar.getId()}, Collections.<String>emptyList());
    assertContain(event.getId(), list);
    assertContain(sharedEvent.getId(), list);
    assertContain(groupEvent.getId(), list);
  }

  private void assertContain(String eventId, Collection<CalendarEvent> events) {
    for(CalendarEvent e : events) {
      if(e.getId().equals(eventId)) {
        return;
      }
    }
    fail("Event id [" + eventId + "] should be found in search result");
  }

  private void assertNotContain(String eventId, Collection<CalendarEvent> events) {
    for(CalendarEvent e : events) {
      if(e.getId().equals(eventId)) {
        fail("Event id [" + eventId + "] should be found in search result");
      }
    }

  }
}
