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

    Collection<CalendarEvent> list = calendarService_.getExceptionEvents(username, recurEvent);
    assertEquals(0, list.size());

    occEvent1.setSummary("broken series event");
    //breaks 1 from the series
    calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent1, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    list = calendarService_.getExceptionEvents(username, recurEvent);
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

    Utils.adaptRepeatRule(recurEvent, tz, TimeZone.getTimeZone("GMT"));
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
    userEvent.setRepeatByMonthDay(new long[]{2, 3, 4, 5, 7});
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
    TimeZone timeZone = TimeZone.getTimeZone("GMT");
    
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
    userEvent.setRepeatByMonthDay(new long[]{2, 3, 4, 5, 7});
    
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
    assertEquals(9, calendar.get(java.util.Calendar.DATE));

    fromCal.set(2013, java.util.Calendar.SEPTEMBER, 10, 22, 30);
    toCal.set(2013, java.util.Calendar.SEPTEMBER, 10, 23, 30);
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

  public void testRecurringWithTimezoneChange() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");

    // Set current user timezone is GTM+7
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = TimeZone.getTimeZone(setting.getTimeZone());

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

    //Adjust recurring info
    Utils.adaptRepeatRule(event, userTimezone, TimeZone.getTimeZone("GMT"));

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
  public void testCreateRecurringEvent1() throws Exception {
    CalendarSetting setting = calendarService_.getCalendarSetting(username);
    setting.setDateFormat("MMddyyyy");
    setting.setTimeFormat("H:m");

    // Set current user timezone is GTM+7
    setting.setTimeZone("Asia/Saigon");
    calendarService_.saveCalendarSetting(username, setting);
    setting = calendarService_.getCalendarSetting(username);
    TimeZone userTimezone = TimeZone.getTimeZone(setting.getTimeZone());

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
    TimeZone userTimezone = TimeZone.getTimeZone(setting.getTimeZone());

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
    Utils.adaptRepeatRule(event, userTimezone, TimeZone.getTimeZone("GMT"));

    calendarService_.saveUserEvent(username, calendar.getId(), event, true);

    //. Get occurrenceEvent
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
    TimeZone userTimezone = TimeZone.getTimeZone(setting.getTimeZone());

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
    Utils.adaptRepeatRule(event, userTimezone, TimeZone.getTimeZone("GMT"));

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

  private Date getDate(CalendarSetting setting, String datetime) throws Exception {
    String format = setting.getDateFormat() + " " + setting.getTimeFormat();
    DateFormat df = new SimpleDateFormat(format);
    df.setCalendar(getCalendarInstanceBySetting(setting));
    return df.parse(datetime);
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

  private java.util.Calendar getCalendarInstanceBySetting(final CalendarSetting calendarSetting) {
    java.util.Calendar calendar = java.util.Calendar.getInstance() ;
    calendar.setLenient(false);
    calendar.setTimeZone(TimeZone.getTimeZone(calendarSetting.getTimeZone()));
    calendar.setFirstDayOfWeek(Integer.parseInt(calendarSetting.getWeekStartOn()));
    calendar.setMinimalDaysInFirstWeek(4);
    return calendar;
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
