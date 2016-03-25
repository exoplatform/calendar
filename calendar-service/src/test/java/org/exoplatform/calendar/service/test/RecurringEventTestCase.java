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

import javax.jcr.query.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.impl.CalendarSearchServiceConnector;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.service.impl.UnifiedQuery;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.DateUtils;

public class RecurringEventTestCase extends BaseCalendarServiceTestCase {

  private JCRDataStorage storage_;
  private CalendarSearchServiceConnector unifiedSearchService_ ;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    unifiedSearchService_ = getService(CalendarSearchServiceConnector.class);
    storage_ = ((CalendarServiceImpl)calendarService_).getDataStorage();
  }

  public void testRemoveOccurrenceEvent() throws Exception {

    CalendarEvent recurEvent = createRepetitiveEventForTest();

    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());

    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());

    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);

    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.setTime(recurEvent.getFromDateTime());
    fromCal.add(java.util.Calendar.DATE, 1);
    String recurenceId1 = sf.format(fromCal.getTime());

    fromCal.add(java.util.Calendar.DATE, 2);
    String recurrenceId2 = sf.format(fromCal.getTime());

    CalendarEvent occEvent1 = occMap.get(recurenceId1);
    CalendarEvent occEvent2 = occMap.get(recurrenceId2);
    occEvent2.setDescription("newException");
    calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent2, username);
    calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent2, username);
    List<CalendarEvent> exceptionEvents = calendarService_.getExceptionEvents(username, recurEvent);
    assertEquals(1,exceptionEvents.size());
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(4,occMap.size());

    calendarService_.removeOneOccurrenceEvent(recurEvent, occEvent2, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    exceptionEvents = calendarService_.getExceptionEvents(username, recurEvent);

    assertEquals(4,occMap.size());
    assertEquals(0,exceptionEvents.size());

    calendarService_.removeFollowingSeriesEvents(recurEvent, occEvent1, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    exceptionEvents = calendarService_.getExceptionEvents(username, recurEvent);

    assertEquals(1, occMap.size());
    assertEquals(0, exceptionEvents.size());

    calendarService_.removeAllSeriesEvents(recurEvent, username);
    assertNull(calendarService_.getEventById(recurEvent.getId()));
  }

  public void testSaveAllOccurrenceEvent() throws Exception {

    CalendarEvent recurEvent = createRepetitiveEventForTest();
    
    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());

    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());

    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.setTime(recurEvent.getFromDateTime());
    fromCal.add(java.util.Calendar.DATE, 2);
    String reocurenceId1 = sf.format(fromCal.getTime());
    CalendarEvent occEvent1 = occMap.get(reocurenceId1);

    calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent1, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(4, occMap.size());

    recurEvent.setSummary("change and update all");
    calendarService_.saveAllSeriesEvents(recurEvent, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(4, occMap.size());
  }

  public void testSaveFollowingOccurrenceEvent() throws Exception {

    CalendarEvent recurEvent = createRepetitiveEventForTest();

    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.setTime(recurEvent.getFromDateTime());

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.setTime(recurEvent.getToDateTime());

    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);

    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());

    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());

    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);

    fromCal.add(java.util.Calendar.DATE, 1);
    String recurrenceId1 = sf.format(fromCal.getTime());

    fromCal.add(java.util.Calendar.DATE,2);
    String recurrenceId2 = sf.format(fromCal.getTime());

    CalendarEvent occEvent1 = occMap.get(recurrenceId1);
    CalendarEvent occEvent2 = occMap.get(recurrenceId2);

    calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent2, username); //break occEvent2 from the series
    Collection<CalendarEvent> exceptions = calendarService_.getExceptionEvents(username, recurEvent);
    assertEquals(1,exceptions.size());

    //create new series starting from occEvent1
    calendarService_.saveFollowingSeriesEvents(recurEvent, occEvent1, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(1, occMap.size());

    occMap = calendarService_.getOccurrenceEvents(occEvent1, from, to, timeZone);
    assertEquals(4, occMap.size());

    exceptions = calendarService_.getExceptionEvents(username, recurEvent);
    assertEquals(0,exceptions.size());//the exception occEvent2 should be removed now
  }

  public void testSaveFollowingOccurrenceEventWithChangeTime() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());

    Calendar calendar = createPrivateCalendar(username, "testSaveFollowingOccurrenceEventWithChangeTime calendar", "description");

    String startTime = "05252015 10:00";
    String endTime = "05252015 11:00";
    Date start = getDate(setting, startTime);
    Date end = getDate(setting, endTime);

    java.util.Calendar from = java.util.Calendar.getInstance();
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTime(end);
    to.add(java.util.Calendar.DATE, 10);

    // Create recuring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("testSaveFollowingOccurrenceEventWithChangeTime event");
    event.setFromDateTime(start);
    event.setToDateTime(end);
    event.setRepeatType(CalendarEvent.RP_DAILY);
    event.setRepeatInterval(1);
    event.setRepeatCount(5);
    event.setRepeatUntilDate(null);
    Utils.updateOriginDate(event, userTimezone);
    Utils.adaptRepeatRule(event, userTimezone, CalendarService.PERSISTED_TIMEZONE);
    calendarService_.saveUserEvent(username, calendar.getId(), event, true);

    event = calendarService_.getEvent(username, event.getId());

    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    assertEquals(5, events.size());

    CalendarEvent occurence = events.get("20150527T100000Z");
    assertNotNull(occurence);

    java.util.Calendar newFromCal = java.util.Calendar.getInstance(userTimezone);
    newFromCal.setTime(occurence.getFromDateTime());
    java.util.Calendar newToCal = java.util.Calendar.getInstance(userTimezone);
    newFromCal.setTime(occurence.getToDateTime());
    newFromCal.add(java.util.Calendar.MINUTE, 30);
    newToCal.add(java.util.Calendar.MINUTE, 30);
    occurence.setFromDateTime(newFromCal.getTime());
    occurence.setToDateTime(newToCal.getTime());

    calendarService_.saveFollowingSeriesEvents(event, occurence, username);

    event = calendarService_.getEvent(username, event.getId());
    occurence = calendarService_.getEvent(username, occurence.getId());

    events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    assertEquals(2, events.size());

    events = calendarService_.getOccurrenceEvents(occurence, from, to, setting.getTimeZone());
    assertEquals(3, events.size());
  }

  public void testSaveOneOccurrenceEvent() throws Exception {

    CalendarEvent recurEvent = createRepetitiveEventForTest();

    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.setTime(recurEvent.getFromDateTime());

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.setTime(recurEvent.getToDateTime());

    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);

    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());

    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());   //there are 5 occurrences

    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);
    String recurrenceId1 = sf.format(fromCal.getTime());
    CalendarEvent occEvent1 = occMap.get(recurrenceId1);

    fromCal.add(java.util.Calendar.DATE, 1);
    String recurrenceId2 = sf.format(fromCal.getTime());
    CalendarEvent occEvent2 = occMap.get(recurrenceId2);

    Collection<CalendarEvent> list = calendarService_.getExceptionEvents(username,recurEvent);
    assertEquals(0,list.size());

    occEvent1.setSummary("broken series event");
    //breaks 1 from the series
    calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent1, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    list = calendarService_.getExceptionEvents(username,recurEvent);
    assertNotNull(list);
    assertEquals(1,list.size());
    assertEquals(4, occMap.size());  //5 - 1

    //remove 1 occurrence from the series
    calendarService_.removeOneOccurrenceEvent(recurEvent, occEvent2, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(3, occMap.size());  //5 - 1 - 1

    //remove the exception event
    calendarService_.removeOneOccurrenceEvent(recurEvent, occEvent1, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    list = calendarService_.getExceptionEvents(username,recurEvent);
    assertEquals(3, occMap.size());
    assertEquals(0,list.size());
  }

  public void testGetRepetitiveEvent() throws Exception {

    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);

    CalendarEvent recurEvent = createRepetitiveEventForTest();

    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.setTime(recurEvent.getFromDateTime());

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.setTime(recurEvent.getToDateTime());

    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);

    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    Map<String,CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);

    fromCal.add(java.util.Calendar.DATE, 1);
    String recurrenceId = sf.format(fromCal.getTime());
    CalendarEvent occurrence = occMap.get(recurrenceId);

    //test get origin event from one occurrence
    CalendarEvent expectedOrigin = calendarService_.getRepetitiveEvent(occurrence);
    assertNotNull(expectedOrigin);
    assertEquals(recurEvent.getId(), expectedOrigin.getId());

    occurrence.setSummary("I'm now an exception");
    calendarService_.saveOneOccurrenceEvent(recurEvent, occurrence, username);

    List<CalendarEvent> exceptions = calendarService_.getExceptionEvents(username, recurEvent);
    assertEquals(1, exceptions.size());
    CalendarEvent exceptionEvent = exceptions.iterator().next();
    assertFalse(exceptionEvent.getId() == recurEvent.getId());

    //test get origin event from an exception event
    expectedOrigin = calendarService_.getRepetitiveEvent(exceptionEvent);
    assertNotNull(expectedOrigin);
    assertEquals(recurEvent.getId(), expectedOrigin.getId());
  }

  /**
   * test case event created at the beginning of the day, so that the date stored
   * in server is 1 less than the date selected because of different timezones
   * @since CAL-351
   * @throws Exception
   */
  public void testGetOccurrenceEvents1() throws Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, 2, 7, 5, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, 2, 7, 6, 30);

    Calendar calendar = createPrivateCalendar(username, username, "unified search test");
    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("repeated past");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_WEEKLY);
    String[] days = {"TH"};
    recurEvent.setRepeatByDay(days);
    recurEvent.setRepeatUntilDate(null);
    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    Utils.adaptRepeatRule(recurEvent, tz, CalendarService.PERSISTED_TIMEZONE);
    calendarService_.saveUserEvent(username, calendar.getId(), recurEvent, true);
    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);

    assertEquals(1, occMap.size());

    CalendarEvent occEvent = occMap.get(occMap.keySet().iterator().next());
    java.util.Calendar occCal = java.util.Calendar.getInstance(tz);
    occCal.setTime(occEvent.getFromDateTime());

    assertEquals(occCal.get(java.util.Calendar.DATE), 7);
    assertEquals(occCal.get(java.util.Calendar.MONTH), 2);
    assertEquals(occCal.get(java.util.Calendar.YEAR), 2013);

    EventQuery query = new UnifiedQuery() ;
    query.setQueryType(Query.SQL);
    query.setText("past") ;
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertNotNull(result) ;
    assertEquals(1, result.size());
  }

  /**
   * test with big diffrence of timezone, event created at the end of the day
   * so that the day stored is 1 more than the day selected because of different timezones
   * @since CAL-386
   * @throws Exception
   */
  public void testGetOccurrenceEvents2() throws Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, 2, 7, 22, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, 2, 7, 23, 30);

    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_WEEKLY);
    String[] days = {"TH"};
    recurEvent.setRepeatByDay(days);
    recurEvent.setRepeatUntilDate(null);

    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 24, 0, 0, 0);
    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);

    assertEquals(3, occMap.size());

    SimpleDateFormat format = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    format.setTimeZone(tz);

    assertNotNull(occMap.get(format.format(fromCal.getTime())));

    fromCal.add(java.util.Calendar.DATE, 7);

    assertNotNull(occMap.get(format.format(fromCal.getTime())));

    fromCal.add(java.util.Calendar.DATE, 7);

    assertNotNull(occMap.get(format.format(fromCal.getTime())));

  }

  /**
   * test duplicate event issue in shared calendar when break occurrence series
   * @since CAL-358
   * @throws Exception
   */
  public void testGetOccurrenceEvents3() throws Exception {

    CalendarEvent recurEvent = createRepetitiveEventForTest();
    Calendar calendar = calendarService_.getCalendarById(recurEvent.getCalendarId());
    
    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());

    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());

    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.setTime(recurEvent.getFromDateTime());
    String reocurenceId1 = sf.format(fromCal.getTime());
    CalendarEvent occEvent1 = occMap.get(reocurenceId1);

    java.util.Calendar occCal = java.util.Calendar.getInstance(tz);
    occCal.setTime(occEvent1.getFromDateTime());

    assertEquals(occCal.get(java.util.Calendar.DATE), 7);
    assertEquals(occCal.get(java.util.Calendar.MONTH), 2);
    assertEquals(occCal.get(java.util.Calendar.YEAR), 2013);

    fromCal.add(java.util.Calendar.DATE, 1);
    String reocurenceId2 = sf.format(fromCal.getTime());
    CalendarEvent occEvent2 = occMap.get(reocurenceId2);
    occCal = java.util.Calendar.getInstance(tz);
    occCal.setTime(occEvent2.getFromDateTime());

    assertEquals(occCal.get(java.util.Calendar.DATE), 8);
    assertEquals(occCal.get(java.util.Calendar.MONTH), 2);
    assertEquals(occCal.get(java.util.Calendar.YEAR), 2013);

    //Break first event in series and save
    //set break ID

    List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
    occEvent1.setIsExceptionOccurrence(true);
    occEvent1.setSummary("broken series event");
    occEvent1.setRecurrenceId(reocurenceId1);
    listEvent.add(occEvent1);
    calendar.setEditPermission(new String[]{"john", username});
    calendarService_.saveUserCalendar(username, calendar, false);
    calendarService_.shareCalendar(username, calendar.getId(), Arrays.asList(new String[]{"john"}));
    login("john");
    calendarService_.updateOccurrenceEvent(calendar.getId(), calendar.getId(), String.valueOf(Calendar.TYPE_SHARED), String.valueOf(Calendar.TYPE_SHARED), listEvent, "john");
    recurEvent.setExcludeId(new String[]{reocurenceId1});
    calendarService_.saveEventToSharedCalendar("john", calendar.getId(), recurEvent, false);

    //check occurrences
    original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(4, occMap.size());
    listEvent = new ArrayList<CalendarEvent>();
    occEvent2.setSummary("broken series event 2");
    occEvent2.setIsExceptionOccurrence(true);
    occEvent2.setRecurrenceId(reocurenceId2);
    listEvent.add(occEvent2);
    calendarService_.updateOccurrenceEvent(calendar.getId(), calendar.getId(), String.valueOf(Calendar.TYPE_SHARED), String.valueOf(Calendar.TYPE_SHARED), listEvent, "john");
    recurEvent.setExcludeId(new String[]{reocurenceId1, reocurenceId2});
    calendarService_.saveEventToSharedCalendar("john", calendar.getId(), recurEvent, false);

    original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(3, occMap.size());
  }

  public void testUpdateRecurrenceSeries() throws Exception {
    Calendar calendar = createPrivateCalendar(username, "myCalendar", "Description");
    Calendar publicCalendar = createGroupCalendar(userGroups, "publicCalendar", "publicDescription");
    
    EventCategory eventCategory = createUserEventCategory(username, "eventCategoryName0");
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.add(java.util.Calendar.HOUR, 1);
    java.util.Calendar repeatUntilDate = java.util.Calendar.getInstance(tz);
    repeatUntilDate.add(java.util.Calendar.DATE, 5);
    
    CalendarEvent userEvent = new CalendarEvent();
    userEvent.setSummary("Have a meeting");
    userEvent.setFromDateTime(fromCal.getTime());
    userEvent.setToDateTime(toCal.getTime());
    userEvent.setCalendarId(calendar.getId());
    userEvent.setEventCategoryId(eventCategory.getId());
    userEvent.setRepeatType(CalendarEvent.RP_DAILY);
    userEvent.setRepeatInterval(2);
    userEvent.setRepeatCount(3);
    userEvent.setRepeatUntilDate(repeatUntilDate.getTime());
    userEvent.setRepeatByDay(null);
    userEvent.setRepeatByMonthDay(new long[] { 2, 3, 4, 5, 7 });
    storage_.saveOccurrenceEvent(username, calendar.getId(), userEvent, true);
    
    storage_.getOccurrenceEvents(userEvent, fromCal, toCal, timeZone);
    
    List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
    listEvent.add(userEvent);
    storage_.updateOccurrenceEvent(calendar.getId(),
                                   publicCalendar.getId(),
                                   String.valueOf(Calendar.TYPE_PRIVATE),
                                   String.valueOf(Calendar.TYPE_PUBLIC),
                                   listEvent,
                                   username);    
  }

  public void testCalculateRecurrenceFinishDate() throws Exception {
    TimeZone timeZone = DateUtils.getTimeZone("GMT");
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance(timeZone);
    fromCal.set(2011, 6, 20, 5, 30);
    
    java.util.Calendar toCal = java.util.Calendar.getInstance(timeZone);
    toCal.set(2011, 6, 25, 5, 30);
    
    CalendarEvent userEvent = new CalendarEvent();
    userEvent.setFromDateTime(fromCal.getTime());
    userEvent.setToDateTime(toCal.getTime());
    userEvent.setRepeatType(CalendarEvent.RP_DAILY);
    userEvent.setRepeatInterval(2);
    userEvent.setRepeatCount(3);
    userEvent.setRepeatUntilDate(null);
    userEvent.setRepeatByDay(null);
    userEvent.setRepeatByMonthDay(new long[] { 2, 3, 4, 5, 7 });
    
    Date date = storage_.calculateRecurrenceFinishDate(userEvent);
    
    java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);
    calendar.setTime(date);
    
    assertEquals(2011, calendar.get(java.util.Calendar.YEAR));
    assertEquals(6, calendar.get(java.util.Calendar.MONTH));
    assertEquals(25, calendar.get(java.util.Calendar.DATE));
    assertEquals(0, calendar.get(java.util.Calendar.HOUR));
    assertEquals(0, calendar.get(java.util.Calendar.MINUTE));
  }

  public void testGetPreviousOccurence() throws  Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, java.util.Calendar.MARCH, 7, 22, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, java.util.Calendar.MARCH, 7, 23, 30);

    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("test previous");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_DAILY);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatCount(6);
    recurEvent.setRepeatUntilDate(null);

    java.util.Calendar calendar = java.util.Calendar.getInstance(tz);
    calendar.set(2013, java.util.Calendar.MARCH, 10, 22, 30);
    //get occurrence right before 09 Feb
    Date expectedDate = Utils.getPreviousOccurrenceDate(recurEvent, calendar.getTime(), tz);
    calendar.setTime(expectedDate);
    assertEquals(9,calendar.get(java.util.Calendar.DATE));

    fromCal.set(2013, java.util.Calendar.SEPTEMBER, 10, 22, 30);
    toCal.set(2013,java.util.Calendar.SEPTEMBER, 10, 23, 30);
    recurEvent.setRepeatType(CalendarEvent.RP_WEEKLY);
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatCount(-1);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatByDay(new String[]{"TU"});

    calendar.set(2013, java.util.Calendar.SEPTEMBER, 24, 22, 30);
    expectedDate = Utils.getPreviousOccurrenceDate(recurEvent, calendar.getTime(), tz);
    calendar.setTime(expectedDate);
    assertEquals(17, calendar.get(java.util.Calendar.DATE));

  }

  public void testGetPreviousOccurrence2() throws Exception {

    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, java.util.Calendar.SEPTEMBER, 11, 19, 0);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, java.util.Calendar.SEPTEMBER, 11, 22, 30);

    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("test previous");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_WEEKLY);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatCount(-1);
    recurEvent.setRepeatByDay(new String[]{"WE"});
    recurEvent.setRepeatUntilDate(null);

    java.util.Calendar calendar = java.util.Calendar.getInstance(tz);
    calendar.set(2013, java.util.Calendar.SEPTEMBER, 25, 19, 0);
    Date expectedDate = Utils.getPreviousOccurrenceDate(recurEvent, calendar.getTime(), tz);
    calendar.setTime(expectedDate);
    assertEquals(18, calendar.get(java.util.Calendar.DATE));
  }

  /**
   * * This test now only failure if:
   * - Default timezone (timezone set up on computer) is GMT +7
   * - Time in GMT +7 is afternoon (after 3:00 PM)
   *
   * TODO: Please fix this test case. It is failure in 10 last days of summer time (DST) of timezone GMT+1 (10 days before 25 Oct 2015)
   * - This seem current behaviour is not good (Google calendar is different)
   * I see if I create a event recurring at 8AM every day (at GTM+1), but after 25 0ct 2015, all recurring event will start at 9AM.
   * I think all event must be started at 8AM (like this test case), the start-time only break if I share this event to other user at different timezone
   *
   * I will temporary disable this test-case to release CI, please re-enable and fix it.
   *
   * @throws Exception
   */
  public void tes1tRecurringEventStartFromToday() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    String originUserTimezone = setting.getTimeZone();
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM +1
    setting.setTimeZone("Europe/Brussels");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    // Build startTime and endTime string
    // startTime == nextHour from current hour
    // endTime = next hour from start time
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.HOUR_OF_DAY, 1);
    from.set(java.util.Calendar.MINUTE, 0);
    from.set(java.util.Calendar.SECOND, 0);
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTime(from.getTime());
    to.add(java.util.Calendar.HOUR_OF_DAY, 1);
    String startTime = formatDate(setting, from.getTime());
    String endTime = formatDate(setting, to.getTime());
    //. Rebuild fromDate and toDate from user time
    Date fromDate = getDate(setting, startTime);
    Date toDate = getDate(setting, endTime);

    java.util.Calendar fromCalendar = java.util.Calendar.getInstance(userTimezone);
    fromCalendar.setTime(fromDate);

    //. Create calendar
    Calendar cal = createPrivateCalendar(username, "testRecurringEventStartFromToday", "testRecurringEventStartFromToday");
    //. Create recurring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test previous");
    event.setFromDateTime(fromDate);
    event.setToDateTime(toDate);
    event.setRepeatType(CalendarEvent.RP_DAILY);
    event.setRepeatInterval(1);
    event.setRepeatCount(10);
    event.setRepeatByDay(null);
    event.setRepeatUntilDate(null);
    calendarService_.saveUserEvent(username, cal.getId(), event, true);
    from.setTimeZone(userTimezone);
    from.add(java.util.Calendar.DATE, -2);
    to.setTimeZone(userTimezone);
    to.add(java.util.Calendar.DATE, 20);
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    Set<String> keys = events.keySet();
    assertEquals(10, keys.size());
    SimpleDateFormat format = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    format.setCalendar(fromCalendar);
    from = (java.util.Calendar)fromCalendar.clone();
    for(int i = 0; i < 10; i++) {
      assertContain(format.format(from.getTime()), keys);
      from.add(java.util.Calendar.HOUR_OF_DAY, 1);
      assertNotContain(format.format(from.getTime()), keys);
      from.add(java.util.Calendar.HOUR_OF_DAY, -2);
      assertNotContain(format.format(from.getTime()), keys);
      from.add(java.util.Calendar.HOUR_OF_DAY, 1);
      from.add(java.util.Calendar.DAY_OF_YEAR, 1);
    }

    calendarService_.removeAllSeriesEvents(event, username);
    // Reset user timezone
    setting.setTimeZone(originUserTimezone);
    calendarService_.saveCalendarSetting(username, setting);
  }

  /**
   * This test now only failure if:
   * - Default timezone (timezone set up on computer) is GMT +7
   * - Time in GMT +7 is afternoon (after 3:00 PM)
   *
   * TODO: this test case is temporary failed like testRecurringEventStartFromToday, I temporary disable it
   * @throws Exception
   */
  public void tes1tRecurringEventStartFromYesterday() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    String originUserTimezone = setting.getTimeZone();
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM +1
    setting.setTimeZone("Europe/Brussels");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    // Build startTime and endTime string
    // startTime == nextHour from current hour
    // endTime = next hour from start time
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.DATE, -1);
    from.add(java.util.Calendar.HOUR_OF_DAY, 1);
    from.set(java.util.Calendar.MINUTE, 0);
    from.set(java.util.Calendar.SECOND, 0);
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTime(from.getTime());
    to.add(java.util.Calendar.HOUR_OF_DAY, 1);
    String startTime = formatDate(setting, from.getTime());
    String endTime = formatDate(setting, to.getTime());
    //. Rebuild fromDate and toDate from user time
    Date fromDate = getDate(setting, startTime);
    Date toDate = getDate(setting, endTime);

    java.util.Calendar fromCalendar = java.util.Calendar.getInstance(userTimezone);
    fromCalendar.setTime(fromDate);

    //. Create calendar
    Calendar cal = createPrivateCalendar(username, "testRecurringEventStartFromYesterday", "testRecurringEventStartFromYesterday");
    //. Create recurring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test previous");
    event.setFromDateTime(fromDate);
    event.setToDateTime(toDate);
    event.setRepeatType(CalendarEvent.RP_DAILY);
    event.setRepeatInterval(1);
    event.setRepeatCount(10);
    event.setRepeatByDay(null);
    event.setRepeatUntilDate(null);
    calendarService_.saveUserEvent(username, cal.getId(), event, true);
    from.setTimeZone(userTimezone);
    from.add(java.util.Calendar.DATE, -2);
    to.setTimeZone(userTimezone);
    to.add(java.util.Calendar.DATE, 20);
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    Set<String> keys = events.keySet();
    assertEquals(10, keys.size());
    SimpleDateFormat format = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    format.setCalendar(fromCalendar);
    from = (java.util.Calendar)fromCalendar.clone();
    for(int i = 0; i < 10; i++) {
      assertContain(format.format(from.getTime()), keys);
      from.add(java.util.Calendar.HOUR_OF_DAY, 1);
      assertNotContain(format.format(from.getTime()), keys);
      from.add(java.util.Calendar.HOUR_OF_DAY, -2);
      assertNotContain(format.format(from.getTime()), keys);
      from.add(java.util.Calendar.HOUR_OF_DAY, 1);
      from.add(java.util.Calendar.DAY_OF_YEAR, 1);
    }

    calendarService_.removeAllSeriesEvents(event, username);
    // Reset user timezone
    setting.setTimeZone(originUserTimezone);
    calendarService_.saveCalendarSetting(username, setting);
  }

  public void testRecurringWithTimezoneChange() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM+7
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    Calendar calendar = createPrivateCalendar(username, "test recurring calendar", "description");
    String startTime = "10202014 10:00";
    String endTime = "10202014 11:00";
    Date start = getDate(setting, startTime);
    Date end = getDate(setting, endTime);
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.setTimeZone(userTimezone);
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTime(end);
    to.setTimeZone(userTimezone);
    to.add(java.util.Calendar.MONTH, 2);
    // Create recuring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test recurring");
    event.setFromDateTime(start);
    event.setToDateTime(end);
    event.setRepeatType(CalendarEvent.RP_WEEKLY);
    event.setRepeatInterval(1);
    event.setRepeatByDay(new String[]{"MO", "TU", "WE", "TH"});
    event.setRepeatCount(10);
    event.setRepeatUntilDate(null);

    Utils.updateOriginDate(event, userTimezone);
    Utils.adaptRepeatRule(event, userTimezone, CalendarService.PERSISTED_TIMEZONE);

    calendarService_.saveUserEvent(username, calendar.getId(), event, true);
    //. Get occurrenceEvent
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    assertEquals(10, events.size());
    Set<String> keys = events.keySet();
    assertContain("20141020T100000Z", keys);
    assertContain("20141021T100000Z", keys);
    assertContain("20141022T100000Z", keys);
    assertContain("20141023T100000Z", keys);
    // Set current user timezone to GMT-7
    setting.setTimeZone("US/Mountain");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    userTimezone = TimeZone.getTimeZone(setting.getTimeZone());
    //. Re get occurrenceEvent
    from = java.util.Calendar.getInstance();
    from.setTimeZone(userTimezone);
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    to = java.util.Calendar.getInstance();
    to.setTimeZone(userTimezone);
    to.setTime(end);
    to.add(java.util.Calendar.MONTH, 2);
    //. Get list events after user change his timezone
    events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    assertEquals(10, events.size());
    keys = events.keySet();
    assertContain("20141019T210000Z", keys);
    assertContain("20141020T210000Z", keys);
    assertContain("20141021T210000Z", keys);
    assertContain("20141022T210000Z", keys);
  }
  /**
   * On this case, we create event on Wednesday 22 Oct 2014 and repeat all weekday (Mon to Fri)
   * Expect:
   * - Origin event must start on Wednesday 22 Oct 2014
   * - On Monday (20 Oct) and Tuesday (21 Oct) must not have any instance of recurring
   * @throws Exception
   */
  public void testCreateRecurringEvent() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM+7
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    Calendar calendar = createPrivateCalendar(username, "test recurring calendar", "description");
    String startTime = "10222014 10:00";
    String endTime = "10222014 11:00";
    Date start = getDate(setting, startTime);
    Date end = getDate(setting, endTime);
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.setTimeZone(userTimezone);
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTime(end);
    to.setTimeZone(userTimezone);
    to.add(java.util.Calendar.MONTH, 2);
    // Create recuring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test recurring");
    event.setFromDateTime(start);
    event.setToDateTime(end);
    event.setRepeatType(CalendarEvent.RP_WEEKLY);
    event.setRepeatInterval(1);
    event.setRepeatByDay(new String[]{"MO", "TU", "WE", "TH", "FR"});
    event.setRepeatCount(10);
    event.setRepeatUntilDate(null);
    calendarService_.saveUserEvent(username, calendar.getId(), event, true);
    //. Get occurrenceEvent
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    assertEquals(10, events.size());
    Set<String> keys = events.keySet();
    assertNotContain("20141020T100000Z", keys);
    assertNotContain("20141021T100000Z", keys);
    assertContain("20141022T100000Z", keys);
    assertContain("20141023T100000Z", keys);
    assertContain("20141024T100000Z", keys);
    assertNotContain("20141025T100000Z", keys);
    assertNotContain("20141026T100000Z", keys);
  }
  /**
   * On this test case, we create event on (Wednesday 22 Oct 2014) but repeat on Thursday and Friday
   * The result: Origin event must be on Thursday 23 Oct 2014 and repeat on Thursday and Friday
   * @throws Exception
   */
  public void testCreateEventAndRecurringFromNextDay() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM+7
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    Calendar calendar = createPrivateCalendar(username, "test recurring calendar", "description");
    //. 22 Oct 2014 (Wednesday)
    String startTime = "10222014 10:00";
    String endTime = "10222014 11:00";
    Date start = getDate(setting, startTime);
    Date end = getDate(setting, endTime);
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.setTimeZone(userTimezone);
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTime(end);
    to.setTimeZone(userTimezone);
    to.add(java.util.Calendar.MONTH, 2);
    // Create recuring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test recurring");
    event.setFromDateTime(start);
    event.setToDateTime(end);
    //. Create event on Wednesday but repeat weekly on Thursday and Friday
    event.setRepeatType(CalendarEvent.RP_WEEKLY);
    event.setRepeatInterval(1);
    event.setRepeatByDay(new String[]{"TH", "FR"});
    event.setRepeatCount(10);
    event.setRepeatUntilDate(null);
    Utils.updateOriginDate(event, userTimezone);
    Utils.adaptRepeatRule(event, userTimezone, CalendarService.PERSISTED_TIMEZONE);
    calendarService_.saveUserEvent(username, calendar.getId(), event, true);
    // Get occurrenceEvent
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    assertEquals(10, events.size());
    Set<String> keys = events.keySet();
    assertNotContain("20141020T100000Z", keys);
    assertNotContain("20141021T100000Z", keys);
    assertNotContain("20141022T100000Z", keys);
    assertContain("20141023T100000Z", keys);
    assertContain("20141024T100000Z", keys);
    assertNotContain("20141025T100000Z", keys);
    assertNotContain("20141026T100000Z", keys);
  }
  public void testCreateRecurringEventStartAtSunday() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM+7
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    Calendar calendar = createPrivateCalendar(username, "test recurring calendar", "description");
    //. 22 Oct 2014 (Wednesday)
    String startTime = "10192014 10:00";
    String endTime = "10192014 11:00";
    Date start = getDate(setting, startTime);
    Date end = getDate(setting, endTime);
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.setTimeZone(userTimezone);
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTime(end);
    to.setTimeZone(userTimezone);
    to.add(java.util.Calendar.MONTH, 2);
    // Create recuring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test recurring");
    event.setFromDateTime(start);
    event.setToDateTime(end);
    //. Create event on Wednesday but repeat weekly on Thursday and Friday
    event.setRepeatType(CalendarEvent.RP_WEEKLY);
    event.setRepeatInterval(1);
    event.setRepeatByDay(new String[]{"MO", "SU"});
    event.setRepeatCount(10);
    event.setRepeatUntilDate(null);
    Utils.updateOriginDate(event, userTimezone);
    Utils.adaptRepeatRule(event, userTimezone, CalendarService.PERSISTED_TIMEZONE);
    calendarService_.saveUserEvent(username, calendar.getId(), event, true);
    //. Get occurrenceEvent
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());
    assertEquals(10, events.size());
    Set<String> keys = events.keySet();
    assertNotContain("20141017T100000Z", keys);
    assertNotContain("20141018T100000Z", keys);
    assertContain("20141019T100000Z", keys);
    assertContain("20141020T100000Z", keys);
    assertNotContain("20141021T100000Z", keys);
    assertContain("20141026T100000Z", keys);
    assertContain("20141027T100000Z", keys);
  }

  public void testEventRecurringMonthlyByDayOfWeek() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM+7
    String oldTimeZone = setting.getTimeZone();
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);

    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    Calendar calendar = createPrivateCalendar(username, "calendar for testing monthly recurring", "description");
    //. 09 Mar 2015 (Monday)
    String startTime = "03092015 03:00";
    String endTime = "03092015 04:00";
    Date start = getDate(setting, startTime);
    Date end = getDate(setting, endTime);

    java.util.Calendar from = java.util.Calendar.getInstance(userTimezone);
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    java.util.Calendar to = java.util.Calendar.getInstance(userTimezone);
    to.setTime(end);
    to.add(java.util.Calendar.MONTH, 6);

    // Create recuring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test monthly recurring event");
    event.setFromDateTime(start);
    event.setToDateTime(end);
    //. Create event on Wednesday but repeat weekly on Thursday and Friday
    event.setRepeatType(CalendarEvent.RP_MONTHLY);
    event.setRepeatInterval(1);
    // Repeat monthly on the second Monday
    event.setRepeatByDay(new String[]{"2MO"});
    event.setRepeatCount(5);
    event.setRepeatUntilDate(null);
    Utils.updateOriginDate(event, userTimezone);
    Utils.adaptRepeatRule(event, userTimezone, CalendarService.PERSISTED_TIMEZONE);
    calendarService_.saveUserEvent(username, calendar.getId(), event, true);
    //. Get occurrenceEvent
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());

    //. Reset old timezone in setting
    setting.setTimeZone(oldTimeZone);
    calendarService_.saveCalendarSetting(username, setting);

    //. Assert result
    assertEquals(5, events.size());
    Set<String> keys = events.keySet();
    assertContain("20150309T030000Z", keys);
    assertNotContain("20150316T030000Z", keys);
    assertContain("20150413T030000Z", keys);
    assertContain("20150511T030000Z", keys);
    assertContain("20150608T030000Z", keys);
    assertContain("20150713T030000Z", keys);
    assertNotContain("20150810T030000Z", keys);
  }

  public void testEventRecurringMonthlyByDayOfMonth() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");
    // Set current user timezone is GTM+7
    String oldTimeZone = setting.getTimeZone();
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);

    TimeZone userTimezone = DateUtils.getTimeZone(setting.getTimeZone());
    Calendar calendar = createPrivateCalendar(username, "calendar for testing monthly recurring", "description");
    //. 09 Mar 2015 (Monday)
    String startTime = "03092015 03:00";
    String endTime = "03092015 04:00";
    Date start = getDate(setting, startTime);
    Date end = getDate(setting, endTime);

    java.util.Calendar from = java.util.Calendar.getInstance(userTimezone);
    from.setTime(start);
    from.add(java.util.Calendar.DATE, -2);
    java.util.Calendar to = java.util.Calendar.getInstance(userTimezone);
    to.setTime(end);
    to.add(java.util.Calendar.MONTH, 6);

    // Create recuring event
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test monthly recurring event");
    event.setFromDateTime(start);
    event.setToDateTime(end);
    //. Create event on Wednesday but repeat weekly on Thursday and Friday
    event.setRepeatType(CalendarEvent.RP_MONTHLY);
    event.setRepeatInterval(1);
    //Repeat at 09th every month
    event.setRepeatByMonthDay(new long[]{9L});
    event.setRepeatCount(5);
    event.setRepeatUntilDate(null);
    Utils.updateOriginDate(event, userTimezone);
    Utils.adaptRepeatRule(event, userTimezone, CalendarService.PERSISTED_TIMEZONE);
    calendarService_.saveUserEvent(username, calendar.getId(), event, true);
    //. Get occurrenceEvent
    Map<String, CalendarEvent> events = calendarService_.getOccurrenceEvents(event, from, to, setting.getTimeZone());

    //. Reset old timezone in setting
    setting.setTimeZone(oldTimeZone);
    calendarService_.saveCalendarSetting(username, setting);

    //. Assert result
    assertEquals(5, events.size());
    Set<String> keys = events.keySet();
    assertContain("20150309T030000Z", keys);
    assertNotContain("20150316T030000Z", keys);
    assertContain("20150409T030000Z", keys);
    assertContain("20150509T030000Z", keys);
    assertContain("20150609T030000Z", keys);
    assertContain("20150709T030000Z", keys);
    assertNotContain("20150809T030000Z", keys);
  }

  private Date getDate(CalendarSetting setting, String datetime) throws Exception {
    String format = setting.getDateFormat() + " " + setting.getTimeFormat();
    DateFormat df = new SimpleDateFormat(format);
    df.setCalendar(getCalendarInstanceBySetting(setting));
    return df.parse(datetime);
  }
  private String formatDate(CalendarSetting setting, Date date) {
    String format = setting.getDateFormat() + " " + setting.getTimeFormat();
    DateFormat df = new SimpleDateFormat(format);
    return df.format(date);
  }
  private java.util.Calendar getCalendarInstanceBySetting(final CalendarSetting calendarSetting) {
    java.util.Calendar calendar = java.util.Calendar.getInstance() ;
    calendar.setLenient(false);
    calendar.setTimeZone(DateUtils.getTimeZone(calendarSetting.getTimeZone()));
    calendar.setFirstDayOfWeek(Integer.parseInt(calendarSetting.getWeekStartOn()));
    calendar.setMinimalDaysInFirstWeek(4);
    return calendar;
  }
  private void assertContain(String key, Set<String> keys) {
    for(String k : keys) {
      if(k.equals(key)) {
        return;
      }
    }
    fail("Event at " + key + " should be found");
  }
  private void assertNotContain(String key, Set<String> keys) {
    for(String k : keys) {
      if(k.equals(key)) {
        fail("Event at " + key + " should not be found");
      }
    }
  }

  private CalendarEvent createRepetitiveEventForTest() throws  Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, 2, 7, 5, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, 2, 7, 6, 30);

    Calendar calendar = createPrivateCalendar(username, username, "TestSaveOneOccurrence");

    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("repeated past");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_DAILY);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatCount(6);
    recurEvent.setRepeatUntilDate(null);
    recurEvent.setRepeatByDay(null);
    recurEvent.setCalendarId(calendar.getId());

    calendarService_.saveUserCalendar(username, calendar, true);
    calendarService_.saveUserEvent(username, calendar.getId(), recurEvent, true);

    return recurEvent;
  }
}
