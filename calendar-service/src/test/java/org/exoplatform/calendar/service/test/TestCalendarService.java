/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 */
package org.exoplatform.calendar.service.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jcr.PathNotFoundException;
import javax.jcr.query.Query;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.RemoteCalendar;
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.CalendarSearchResult;
import org.exoplatform.calendar.service.impl.CalendarSearchServiceConnector;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.EventSearchConnector;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.service.impl.NewGroupListener;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.service.impl.TaskSearchConnector;
import org.exoplatform.calendar.service.impl.UnifiedQuery;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */



public class TestCalendarService extends BaseCalendarServiceTestCase {

  public static final String COMA     = ",".intern();
  private TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
  private String timeZone = tz.getID();
  

  private OrganizationService organizationService_;

  private RepositoryService repositoryService_ ;
  private JCRDataStorage  storage_;
  public CalendarService calendarService_;
  private CalendarSearchServiceConnector unifiedSearchService_ ;
  private CalendarSearchServiceConnector taskSearchConnector_ ;
  private CalendarSearchServiceConnector eventSearchConnector_ ;
  private static String   username = "root";
  private SimpleDateFormat df = new SimpleDateFormat(Utils.DATE_TIME_FORMAT) ;
  public Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();

  public void setUp() throws Exception {
    super.setUp();
    repositoryService_ = getService(RepositoryService.class);
    calendarService_ = getService(CalendarService.class);
    organizationService_ = (OrganizationService) getService(OrganizationService.class);
    unifiedSearchService_ = getService(CalendarSearchServiceConnector.class);
    taskSearchConnector_ = getService(TaskSearchConnector.class);
    eventSearchConnector_ = getService(EventSearchConnector.class);
    storage_ = ((CalendarServiceImpl)calendarService_).getDataStorage();
  }

  private void loginUser(String userId) {
    Identity identity = new Identity(userId, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  public void testInitServices() throws Exception{

    assertNotNull(repositoryService_) ;
    assertEquals(repositoryService_.getDefaultRepository().getConfiguration().getName(), "repository");
    assertEquals(repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName(), "portal-test");
    assertNotNull(organizationService_) ;

    assertEquals(organizationService_.getUserHandler().findAllUsers().getSize(), 8);

    assertNotNull(storage_);

    assertNotNull(storage_.getUserCalendarHome(username));

    assertNotNull(storage_.getPublicCalendarHome());

    assertNotNull(storage_.getPublicCalendarServiceHome());

    assertNotNull(calendarService_) ;

    assertNotNull(unifiedSearchService_);

  }
  
//mvn test -Dtest=TestCalendarService#testRemoveOccurrenceEvent
 public void testRemoveOccurrenceEvent() throws Exception{

   java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
   fromCal.set(2013, 2, 7, 5, 30);

   java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
   toCal.set(2013, 2, 7, 6, 30);

   Calendar calendar = createCalendar(username, "TestRepetitiveEvent");
   CalendarEvent recurEvent = new CalendarEvent();
   recurEvent.setSummary("TestRemoveOccurence");
   recurEvent.setFromDateTime(fromCal.getTime());
   recurEvent.setToDateTime(toCal.getTime());
   recurEvent.setRepeatType(CalendarEvent.RP_DAILY);
   recurEvent.setRepeatInterval(1);
   recurEvent.setRepeatCount(6);
   recurEvent.setRepeatUntilDate(null);
   recurEvent.setRepeatByDay(null);
   recurEvent.setRepeatUntilDate(null);
   java.util.Calendar from = java.util.Calendar.getInstance(tz);
   java.util.Calendar to = java.util.Calendar.getInstance(tz);
   from.set(2013, 2, 1, 0, 0, 0);
   to.set(2013, 2, 12, 0, 0, 0);

   calendarService_.saveUserCalendar(username, calendar, true);
   recurEvent.setCalendarId(calendar.getId());
   calendarService_.saveUserEvent(username, calendar.getId(), recurEvent, true);
   List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
   assertEquals(1, original.size());

   Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
   assertEquals(5, occMap.size());

   SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
   sf.setTimeZone(tz);

   fromCal.add(java.util.Calendar.DATE, 1);
   String recurenceId1 = sf.format(fromCal.getTime());

   fromCal.add(java.util.Calendar.DATE, 2);
   String recurrenceId2 = sf.format(fromCal.getTime());

   CalendarEvent occEvent1 = occMap.get(recurenceId1);
   CalendarEvent occEvent2 = occMap.get(recurrenceId2);
   occEvent2.setDescription("newException");
   calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent2, username);
   calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent2, username); List<CalendarEvent> exceptionEvents = calendarService_.getExceptionEvents(username, recurEvent);
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
   calendarService_.removeUserCalendar(username, calendar.getId());
 }
 
  
//mvn test -Dtest=TestCalendarService#testSaveAllOccurrenceEvent
 public void tetSaveAllOccurrenceEvent() throws Exception{
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, 2, 7, 5, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, 2, 7, 6, 30);

    Calendar calendar = createCalendar(username, "unified search test");
    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("repeated past");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_DAILY);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatCount(6);
    recurEvent.setRepeatUntilDate(null);
    recurEvent.setRepeatByDay(null);
    recurEvent.setRepeatUntilDate(null);
    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    calendarService_.saveUserCalendar(username, calendar, true);
    recurEvent.setCalendarId(calendar.getId());
    calendarService_.saveUserEvent(username, calendar.getId(), recurEvent, true);
    List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());

    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());
    
    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);
    fromCal.add(java.util.Calendar.DATE, 2);
    String reocurenceId1 = sf.format(fromCal.getTime());
    CalendarEvent occEvent1 = occMap.get(reocurenceId1);
    
    calendarService_.saveOneOccurrenceEvent(recurEvent, occEvent1, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(4, occMap.size());
    
    recurEvent.setSummary("change and update all");
    calendarService_.saveAllSeriesEvents(recurEvent, username);
    occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());

    calendarService_.removeRecurrenceSeries(username, recurEvent);
    calendarService_.removeUserCalendar(username, calendar.getId());
 }
 

  //mvn test -Dtest=TestCalendarService#testSaveFollowingOccurrenceEvent
  public void testSaveFollowingOccurrenceEvent() throws Exception{

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

    calendarService_.removeRecurrenceSeries(username, recurEvent);
    calendarService_.removeRecurrenceSeries(username, occEvent1);
    calendarService_.removeUserCalendar(username, recurEvent.getCalendarId());
  }


  //mvn test -Dtest=TestCalendarService#testSaveOneOccurrenceEvent
  public void tetSaveOneOccurrenceEvent() throws Exception{

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

    calendarService_.removeUserEvent(username, recurEvent.getCalendarId(), recurEvent.getId());
    calendarService_.removeUserCalendar(username, recurEvent.getCalendarId());
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

    calendarService_.removeRecurrenceSeries(username, recurEvent);
    calendarService_.removeUserCalendar(username, recurEvent.getCalendarId());
  }
  public void testRemoveUserEvent() throws Exception {
    CalendarEvent event = createUserEvent("TestRemoveUserEvent");
    String eventId = event.getId();
    String calendarId = event.getCalendarId();
    String eventCategoryId = event.getEventCategoryId();
    //before removing
    assertNotNull(calendarService_.getEventById(eventId));

    calendarService_.removeUserEvent(username,calendarId,eventId);

    //after removing
    assertNull(calendarService_.getEventById(eventId));

    calendarService_.removeUserCalendar(username,calendarId);
    calendarService_.removeEventCategory(username, eventCategoryId);
  }


  private CalendarEvent cloneException(CalendarEvent originEvent){

    CalendarEvent cloneException = new CalendarEvent(originEvent) ;
    cloneException.setId("Event" + IdGenerator.generate());
    cloneException.setRepeatType(CalendarEvent.RP_NOREPEAT);
    cloneException.setIsExceptionOccurrence(true);
    cloneException.setRepeatInterval(0);
    cloneException.setRepeatCount(0);
    cloneException.setRepeatUntilDate(null);
    cloneException.setRepeatByDay(null);
    cloneException.setRepeatByMonthDay(null);

    return cloneException ;
  }

  //mvn test -Dtest=TestCalendarService#testGetCalendarById
  public void testGetCalendarById() throws Exception{
    Calendar cal = new Calendar();
    cal.setName("myCalendar");
    cal.setDescription("Desscription");
    //cal.setCategoryId();
    cal.setPublic(true);
    calendarService_.saveUserCalendar(username, cal, true);
    EventCategory eventCategory = new EventCategory();
    String name = "eventCategoryName";
    eventCategory.setName(name);

    Calendar calSave = calendarService_.getCalendarById(cal.getId());
    assertNotNull(calSave);
    assertEquals(Calendar.TYPE_PRIVATE, calendarService_.getTypeOfCalendar(username, calSave.getId()));

    calendarService_.saveEventCategory(username, eventCategory, true);
    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventType(CalendarEvent.TYPE_EVENT);
    calEvent.setEventCategoryId(eventCategory.getId());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 1);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    toCal.add(java.util.Calendar.MINUTE, 1);
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, true);
    CalendarEvent eventSave = calendarService_.getEventById(calEvent.getId());
    assertNotNull(eventSave);

    calendarService_.removeEventCategory(username, eventCategory.getId());
    assertNotNull(calendarService_.removeUserCalendar(username, calSave.getId()));

  }

  //mvn test -Dtest=TestCalendarService#testGroupData
  public void testGroupData() throws Exception{
    Collection<Group> rootGroups = organizationService_.getGroupHandler().findGroupsOfUser(username);
    assertTrue(rootGroups.size() > 0);
    Collection<Group> johnGroups = organizationService_.getGroupHandler().findGroupsOfUser("john");
    assertTrue(johnGroups.size() > 0);

  }

  //mvn test -Dtest=TestCalendarService#testUnifiedSeach
  public void testUnifiedSeach() throws Exception {
    String user2 = "john" ;
    //Simple case
    String keyword = "hello \"how are\" you " ;
    EventQuery query = new UnifiedQuery() ;
    query.setQueryType(Query.SQL);
    query.setText(keyword) ;
    assertNotNull(query.getText());
    assertNotNull(query.getQueryStatement());
    loginUser(username) ;
    CalendarSetting calSetting = calendarService_.getCalendarSetting(username) ;
    TimeZone serverZone = TimeZone.getDefault();
    assertNotNull(calSetting);
    assertEquals(serverZone.getID(), calSetting.getTimeZone());
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertNotNull(result) ;
    assertEquals(0, result.size());

    //Complex case
    keyword = "hello \" i am  a \" new guy" ;
    List<String> formated = UnifiedQuery.parse(keyword) ;
    assertEquals(4, formated.size());
    keyword = keyword + " \" why don't \"we talk \" ";
    formated = UnifiedQuery.parse(keyword) ;
    assertEquals(7, formated.size());

    //create/get calendar in private folder
    Calendar cal = new Calendar();
    cal.setTimeZone(calSetting.getTimeZone());
    cal.setName("myCalendar");
    cal.setDescription("Desscription");
    //cal.setCategoryId();
    cal.setPublic(true);
    calendarService_.saveUserCalendar(username, cal, true);
    EventCategory eventCategory = new EventCategory();
    String name = "eventCategoryName";
    eventCategory.setName(name);
    calendarService_.saveEventCategory(username, eventCategory, true);

    //=Test search generic type=//
    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventType(CalendarEvent.TYPE_EVENT);
    //calEvent.setCalType(CalendarEvent.TYPE_EVENT);
    calEvent.setEventCategoryId(eventCategory.getId());
    //Search summary 
    calEvent.setSummary("do you getting Have some busy day?");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 5);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    toCal.add(java.util.Calendar.MINUTE, 1);
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    //calEvent.setEventState(CalendarEvent.CANCELLED);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, true);
    List<String> ids = new ArrayList<String>();
    ids.add(cal.getId());
    List<CalendarEvent> data = calendarService_.getUserEventByCalendar(username, ids) ;
    //Success to add event 
    assertEquals(1,data.size()) ;

    //=Keyword to search=//
    keyword = "Have \"you getting\" busy" ;
    query.setText(keyword);
    result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    //Success to search 
    assertEquals(1, result.size()) ;
    for(SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    //Search summary and description 
    calEvent.setDescription("we have meeting with CEO");
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    keyword = "do \"you getting\" CEO" ;
    query.setText(keyword);
    result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());

    //Success to search 
    assertEquals(1, result.size()) ;
    for(SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    //Search summary , description and location
    calEvent.setLocation("in Hanoi");
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    keyword = "hanoi CEO" ;
    query.setText(keyword);
    query.setOrderBy(new String[]{Utils.ORDERBY_DATE});
    result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());

    //Success to search 
    assertEquals(1, result.size()) ;
    for(SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    //== test event search ==//
    calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setOrderBy(new String[]{Utils.ORDERBY_RELEVANCY});
    result = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size()) ;
    for(SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    //== test task search ==//
    calEvent.setEventType(CalendarEvent.TYPE_TASK);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size()) ;
    for(SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    //Test task status and icon 
    calEvent.setEventType(CalendarEvent.TYPE_TASK) ;
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size()) ;
    for(SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, (CalendarSearchResult)item);
    }
    String status = CalendarEvent.COMPLETED + Utils.COLON + CalendarEvent.CANCELLED ;

    // Does not search completed task 
    calEvent.setEventState(CalendarEvent.COMPLETED);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setState(status);
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(0,result.size());

    // search all need action
    calEvent.setEventState(CalendarEvent.NEEDS_ACTION);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setState(status);
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1,result.size());
    CalendarSearchResult calItem = (CalendarSearchResult)result.toArray()[0] ;
    assertEquals(calEvent.getEventState(), calItem.getTaskStatus());

    // search all inprocess
    calEvent.setEventState(CalendarEvent.IN_PROCESS);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setState(status);
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1,result.size());
    calItem = (CalendarSearchResult)result.toArray()[0] ;
    assertEquals(calEvent.getEventState(), calItem.getTaskStatus());

    // Does not search cancelled task
    calEvent.setEventState(CalendarEvent.CANCELLED);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setState(status);
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(0,result.size());


    //Specia case//
    calEvent.setSummary("today is friday, we will have a weekend");
    calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
    CalendarEvent calEvent2 = new CalendarEvent();
    calEvent2.setCalType(CalendarEvent.TYPE_EVENT);
    calEvent2.setFromDateTime(calEvent.getFromDateTime());
    calEvent2.setToDateTime(calEvent.getToDateTime());
    calEvent2.setSummary("Summary CEO come we will have some dayoff");
    calEvent2.setDescription("friday");
    calEvent2.setLocation("");
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent2, true);
    keyword = "\"we will have\" friday \"" ;
    query.setText(keyword);
    query.setOrderType(Utils.ORDER_TYPE_DESCENDING);
    result = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(2, result.size()) ;
    SearchResult item = (SearchResult)result.toArray()[0] ;
    checkFields(item);
    SearchResult item2 = (SearchResult) result.toArray()[1] ;
    checkFields(item2);
    assertEquals(false, item2.getRelevancy() > item.getRelevancy()) ;
    query.setOrderBy(new String[]{Utils.ORDERBY_DATE});
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    result = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(2, result.size()) ;
    CalendarSearchResult calSerResult = (CalendarSearchResult)result.toArray()[0] ;
    checkFields(calSerResult);
    checkFieldsValueWithType(cal.getName(), calEvent, calSerResult);
    CalendarSearchResult calSerResult2 = (CalendarSearchResult)result.toArray()[1] ;
    checkFields(calSerResult2);
    checkFieldsValueWithType(cal.getName(), calEvent2, calSerResult2);
    assertEquals(false, item.getDate() < item2.getDate()) ;

    //Test query filter by permission 
    loginUser(user2) ;
    Calendar johnCalendar = cal ;
    calSetting = calendarService_.getCalendarSetting(user2);
    johnCalendar.setTimeZone(calSetting.getTimeZone());
    String calendarId = "john_cal_id";
    johnCalendar.setId(calendarId) ;
    johnCalendar.setName("john calendar") ;
    calendarService_.saveUserCalendar(user2, johnCalendar, true);
    assertEquals(calendarId,calendarService_.getUserCalendar(user2, johnCalendar.getId()).getId());
    EventCategory johnEventCategory = new EventCategory();
    johnEventCategory.setName("john category");
    calendarService_.saveEventCategory(user2, eventCategory, true);
    CalendarEvent johnEvent = calEvent2 ;
    johnEvent.setId(new CalendarEvent().getId()) ;
    johnEvent.setCalendarId(johnCalendar.getId()) ;
    johnEvent.setEventType(CalendarEvent.TYPE_EVENT);
    calendarService_.saveUserEvent(user2, johnCalendar.getId(), johnEvent, true);
    assertEquals(1,calendarService_.getUserEventByCalendar(user2, Arrays.asList(new String[]{calendarId})).size());
    result = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size()) ;
    loginUser(username);
    result = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(2, result.size()) ;

    //Test case search only up coming events only
    loginUser(user2) ;
    CalendarEvent inPassEvent = johnEvent ;
    inPassEvent.setId(new CalendarEvent().getId());
    java.util.Calendar current = java.util.Calendar.getInstance() ;
    current.add(java.util.Calendar.MINUTE, -1);
    inPassEvent.setFromDateTime(current.getTime());
    calendarService_.saveUserEvent(user2, calendarId, inPassEvent, true) ;
    assertEquals(2,calendarService_.getUserEventByCalendar(user2, Arrays.asList(new String[]{calendarId})).size());
    result = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size()) ;
    current = java.util.Calendar.getInstance();
    current.add(java.util.Calendar.MINUTE, 1);
    inPassEvent.setFromDateTime(current.getTime()) ;
    calendarService_.saveUserEvent(user2, calendarId, inPassEvent, false) ;
    assertEquals(2,calendarService_.getUserEventByCalendar(user2, Arrays.asList(new String[]{calendarId})).size());
    result = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(2, result.size());

    //Search task due for and no need check from time
    current = java.util.Calendar.getInstance();
    current.add(java.util.Calendar.MINUTE, -1);
    inPassEvent.setFromDateTime(current.getTime()) ;
    current.add(java.util.Calendar.MINUTE, 2);
    inPassEvent.setToDateTime(current.getTime()) ;
    inPassEvent.setEventType(CalendarEvent.TYPE_TASK) ;
    calendarService_.saveUserEvent(user2, calendarId, inPassEvent, false) ;
    assertEquals(2,calendarService_.getUserEventByCalendar(user2, Arrays.asList(new String[]{calendarId})).size());
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size());

    //Not search task in cancelled 
    CalendarEvent cancelledTask = johnEvent ;
    cancelledTask.setId(new CalendarEvent().getId());
    current = java.util.Calendar.getInstance() ;
    current.add(java.util.Calendar.MINUTE, -1);
    cancelledTask.setFromDateTime(current.getTime());
    cancelledTask.setEventType(CalendarEvent.TYPE_TASK);
    cancelledTask.setEventState(CalendarEvent.CANCELLED) ;
    calendarService_.saveUserEvent(user2, calendarId, cancelledTask, true) ;
    assertEquals(3,calendarService_.getUserEventByCalendar(user2, Arrays.asList(new String[]{calendarId})).size());
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size());

    //Search task not completed or not cancelled in part
    current = java.util.Calendar.getInstance() ;
    current.add(java.util.Calendar.MINUTE, -10);
    cancelledTask.setFromDateTime(current.getTime());
    cancelledTask.setEventState(CalendarEvent.NEEDS_ACTION) ;
    calendarService_.saveUserEvent(user2, calendarId, cancelledTask, false) ;
    assertEquals(3,calendarService_.getUserEventByCalendar(user2, Arrays.asList(new String[]{calendarId})).size());
    result = taskSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(2, result.size());

    String siteName = "classic";
    //test search context to build url 
    SearchContext sc = new SearchContext(loadConfiguration("conf/portal/controller.xml"), siteName);
    assertNotNull(sc);
    Router rt = sc.getRouter();
    assertNotNull(rt);
    ExoContainerContext context = (ExoContainerContext)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ExoContainerContext.class);
    String portalName = context.getPortalContainerName();

    //router page expected return /portal/intranet/calendar
    String spaceGroupId = null;
    String url = unifiedSearchService_.getUrl(rt, portalName, siteName, spaceGroupId, Utils.PAGE_NAGVIGATION);
    assertEquals("/"+portalName+"/"+siteName+"/" + Utils.PAGE_NAGVIGATION, url);
    spaceGroupId = "/spaces/space1";
    //router space expected return /portal/g/:spaces:space1/space1/calendar
    url = unifiedSearchService_.getUrl(rt, portalName, siteName, spaceGroupId, Utils.PAGE_NAGVIGATION);
    assertEquals("/"+portalName+"/g/"+ spaceGroupId.replaceAll(Utils.SLASH, ":")+"/space1/" + Utils.PAGE_NAGVIGATION, url);
    result = eventSearchConnector_.search(sc, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size());
    url = unifiedSearchService_.getUrl(rt, portalName, siteName, null, Utils.PAGE_NAGVIGATION);
    for( SearchResult sr : result){
      checkFields(sr);
      assertEquals(url + Utils.SLASH + Utils.DETAIL_PATH + Utils.SLASH + sr.getUrl().split(Utils.SLASH)[sr.getUrl().split(Utils.SLASH).length-1], sr.getUrl());
    }

    //site name null
    sc = new SearchContext(loadConfiguration("conf/portal/controller.xml"), null);
    //siteName = Utils.DEFAULT_SITENAME;
    spaceGroupId = null;
    url = unifiedSearchService_.getUrl(rt, portalName, siteName, spaceGroupId, Utils.PAGE_NAGVIGATION);
    assertEquals("/"+portalName+"/"+siteName+"/" + Utils.PAGE_NAGVIGATION, url);
    spaceGroupId = "/spaces/space1";
    //router space expected return /portal/g/:spaces:space1/space1/calendar
    url = unifiedSearchService_.getUrl(rt, portalName, siteName, spaceGroupId, Utils.PAGE_NAGVIGATION);
    assertEquals("/"+portalName+"/g/"+ spaceGroupId.replaceAll(Utils.SLASH, ":")+"/space1/" + Utils.PAGE_NAGVIGATION, url);
    result = eventSearchConnector_.search(sc, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, result.size());
    url = unifiedSearchService_.getUrl(rt, portalName, siteName, null, Utils.PAGE_NAGVIGATION);
    for( SearchResult sr : result){
      checkFields(sr);
      assertEquals(url + Utils.SLASH + Utils.DETAIL_PATH + Utils.SLASH + sr.getUrl().split(Utils.SLASH)[sr.getUrl().split(Utils.SLASH).length-1], sr.getUrl());
    }

    // Clean up data 
    calendarService_.removeUserEvent(username, ids.get(0), calEvent.getId());
    calendarService_.removeUserEvent(username, ids.get(0), calEvent2.getId());
    calendarService_.removeUserEvent(user2, johnCalendar.getId(), johnEvent.getId());
    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removeEventCategory(user2, johnEventCategory.getId());

    assertNotNull(calendarService_.removeUserCalendar(username, ids.get(0)));
    assertNotNull(calendarService_.removeUserCalendar(user2, johnCalendar.getId()));
  }

  //mvn test -Dtest=TestCalendarService#testUnifiedSeachEx
  public void testUnifiedSeachEx() throws Exception{
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 5);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.MINUTE, 65);

    //Simple case
    String keyword = "you are search able" ;
    EventQuery query = new UnifiedQuery() ;
    query.setText(keyword) ;
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<String> params = new ArrayList<String>();

    String john = "john" ;
    loginUser(username) ;
    Calendar cal = new Calendar() ;
    cal.setName("root calendar");
    calendarService_.saveUserCalendar(username, cal, true);

    Calendar johnCalendar = new Calendar();
    johnCalendar.setName("john calendar");
    calendarService_.saveUserCalendar(john, johnCalendar, true);

    EventCategory eventCategory = new EventCategory() ;
    eventCategory.setName("Meeting");
    calendarService_.saveEventCategory(username, eventCategory, true);

    EventCategory johnEventCategory = new EventCategory();
    johnEventCategory.setName("Call to boss");
    calendarService_.saveEventCategory(john, johnEventCategory, true);

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calEvent.setSummary("how do you do root");
    calEvent.setDescription("you are a search able event");
    calEvent.setCalendarId(cal.getId());
    calEvent.setEventCategoryId(eventCategory.getId());
    calEvent.setPrivate(false);
    calendarService_.saveUserEvent(username, cal.getId(),calEvent, true);

    CalendarEvent calEvent2 = new CalendarEvent() ;
    calEvent2.setFromDateTime(fromCal.getTime());
    calEvent2.setToDateTime(toCal.getTime());
    calEvent2.setSummary("are you here?");
    calEvent2.setDescription("I am not search able event with john");
    calEvent2.setCalendarId(cal.getId());
    calEvent2.setEventCategoryId(eventCategory.getId());
    calEvent2.setPrivate(true);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent2, true);

    CalendarEvent task = new CalendarEvent() ;
    task.setEventType(CalendarEvent.TYPE_TASK);
    task.setFromDateTime(fromCal.getTime());
    task.setToDateTime(toCal.getTime());
    task.setSummary("are you here?");
    task.setDescription("I am search able task with john");
    task.setCalendarId(cal.getId());
    task.setEventCategoryId(eventCategory.getId());
    calendarService_.saveUserEvent(username, cal.getId(), task, true);

    cal.setViewPermission(new String[]{john});
    cal.setEditPermission(null);
    calendarService_.saveUserCalendar(username, cal, false);

    calendarService_.shareCalendar(username, cal.getId(), Arrays.asList(new String[]{john}));

    CalendarEvent johnEvent = new CalendarEvent() ;
    String[] ids = new String[]{cal.getId()};
    Collection<SearchResult> result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertNotNull(result) ;
    assertEquals(3, result.size());

    loginUser(john);
    result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(2, result.size());

    cal.setEditPermission(new String[]{john}) ;
    calendarService_.saveUserCalendar(username, cal, false);

    result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(3, result.size());


    // Clean up data 
    calendarService_.removeUserEvent(username, ids[0], calEvent.getId());
    calendarService_.removeUserEvent(username, ids[0], calEvent2.getId());
    calendarService_.removeUserEvent(john, johnCalendar.getId(), johnEvent.getId());
    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removeEventCategory(john, johnEventCategory.getId());
    calendarService_.removeSharedCalendar(john, cal.getId());
    assertNotNull(calendarService_.removeUserCalendar(username, ids[0]));
    assertNotNull(calendarService_.removeUserCalendar(john, johnCalendar.getId()));

  }

  public void testUnifiedSeachAllWord() throws Exception {
    loginUser(username) ;
    Calendar cal = new Calendar() ;
    cal.setName("root calendar");
    calendarService_.saveUserCalendar(username, cal, true);

    EventCategory eventCategory = new EventCategory() ;
    eventCategory.setName("Meeting");
    calendarService_.saveEventCategory(username, eventCategory, true);

    CalendarEvent calEvent = new CalendarEvent();
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 1);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    toCal.add(java.util.Calendar.MINUTE, 1);
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());

    calEvent.setSummary("abcde fghik");
    calEvent.setDescription("I am a search able event");
    calEvent.setCalendarId(cal.getId());
    calEvent.setEventCategoryId(eventCategory.getId());
    calendarService_.saveUserEvent(username, cal.getId(),calEvent, true);

    CalendarEvent calEvent2 = new CalendarEvent() ;
    calEvent2.setSummary("abcde");
    calEvent2.setCalendarId(cal.getId());
    calEvent2.setEventCategoryId(eventCategory.getId());
    calEvent2.setFromDateTime(fromCal.getTime());
    calEvent2.setToDateTime(toCal.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), calEvent2, true);

    CalendarEvent task = new CalendarEvent() ;
    task.setEventType(CalendarEvent.TYPE_TASK);
    task.setSummary("fghik");
    task.setCalendarId(cal.getId());
    task.setEventCategoryId(eventCategory.getId());
    calendarService_.saveUserEvent(username, cal.getId(), task, true);

  //Simple case
    String keyword = "abcde fghik" ;
    EventQuery query = new UnifiedQuery() ;
    query.setText(keyword) ;
    Collection<String> params = new ArrayList<String>();
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<SearchResult> result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertNotNull(result) ;
    assertEquals(result.size(), 1);
    assertEquals(keyword, result.iterator().next().getTitle());

    // Clean up data 
    String[] ids = new String[]{cal.getId()};
    calendarService_.removeUserEvent(username, ids[0], calEvent.getId());
    calendarService_.removeUserEvent(username, ids[0], calEvent2.getId());
    calendarService_.removeEventCategory(username, eventCategory.getId());
    assertNotNull(calendarService_.removeUserCalendar(username, ids[0]));
  }
  
  private Router loadConfiguration(String path) throws IOException{
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    try {
      ControllerDescriptor routerDesc = new DescriptorBuilder().build(in);
      return new Router(routerDesc);
    } catch (RouterConfigException e) {
      log.info(e.getMessage());
    }finally {
      in.close();
    }
    return null;
  }

  private void checkFields(SearchResult item) {
    assertNotNull(item.getTitle()) ;
    assertNotNull(item.getExcerpt()) ;
    assertNotNull(item.getDetail()) ;
    assertNull(item.getImageUrl());
    assertNotNull(item.getUrl());
    assertEquals(true, item.getDate() > 0);
  }
  private void checkFields(CalendarSearchResult item) {
    checkFields((SearchResult)(item));
    assertEquals(item.getDataType(), CalendarEvent.TYPE_EVENT);
    if(CalendarEvent.TYPE_EVENT.equals(item.getDataType())){
      assertNull(item.getImageUrl());
      assertNull(item.getTaskStatus());
    } else if (CalendarEvent.TYPE_TASK.equals(item.getDataType())){
      assertEquals(Utils.TASK_ICON_URL, item.getImageUrl());
      assertNotNull(item.getTaskStatus());
    }
  }


  private void checkFieldsValueWithType(String calName, CalendarEvent calEvent, SearchResult item){
    assertEquals(calEvent.getSummary(), item.getTitle());
    if(CalendarEvent.TYPE_EVENT.equals(calEvent.getEventType())){
      if(calEvent.getLocation() != null) assertEquals(calName + Utils.SPACE+ Utils.MINUS + Utils.SPACE+ df.format(calEvent.getFromDateTime())+ Utils.SPACE+Utils.MINUS+ Utils.SPACE+calEvent.getLocation(), item.getDetail()) ;
    } else {
      assertEquals(calName + Utils.SPACE +Utils.MINUS+ Utils.SPACE + Utils.DUE_FOR + df.format(calEvent.getToDateTime()), item.getDetail()) ;
    }
    SimpleDateFormat tempFm = new SimpleDateFormat("MM/dd/yyyy hh");
    assertEquals(tempFm.format(new Date()), tempFm.format(new Date(item.getDate())));
    assertEquals(true, item.getRelevancy() > 0);
    //case could not init url
    assertEquals(Utils.NONE_NAGVIGATION, item.getUrl());
    StringBuffer sb = new StringBuffer(Utils.EMPTY_STR) ;
    if(calEvent.getDescription() != null) sb.append(calEvent.getDescription());
    assertEquals(sb.toString(), item.getExcerpt());
  }

  private void checkFieldsValueWithType(String calName, CalendarEvent calEvent, CalendarSearchResult item){
    checkFieldsValueWithType(calName,  calEvent,  (SearchResult)item);
    if(CalendarEvent.TYPE_EVENT.equals(calEvent.getEventType())){
      assertEquals(item.getFromDateTime(), calEvent.getFromDateTime().getTime());
      assertNull(item.getImageUrl());
      assertEquals(Utils.EVENT_ICON_URL, item.getImageUrl());
    } else if(CalendarEvent.TYPE_TASK.equals(calEvent.getEventType())){
      assertEquals(0,item.getFromDateTime());
      assertNotNull(item.getImageUrl());
      assertEquals(Utils.TASK_ICON_URL, item.getImageUrl());
    }
    assertNotNull(item.getTimeZoneName());
  }

  public void testDefaultData() throws Exception {
    String defaultEventCategoriesConfig = "Birthday,Memo,Wedding,DayOff";
    String defaultCalendarId = "NewCalendarId";
    String defaultCalendarCategoryId = "NewCalendarCategoryId";

    // Create valueParam
    ValueParam defaultCalendarIdParam = new ValueParam();
    ValueParam defaultCalendarCategoryIdParam = new ValueParam();
    ValueParam defaultEventCategoriesConfigParam = new ValueParam();
    defaultCalendarIdParam.setValue(defaultCalendarId);
    defaultCalendarCategoryIdParam.setValue(defaultCalendarCategoryId);
    defaultEventCategoriesConfigParam.setValue(defaultEventCategoriesConfig);

    // Init config
    InitParams params = new InitParams();
    params.put(NewUserListener.EVENT_CATEGORIES, defaultEventCategoriesConfigParam);
    NewUserListener newUserListener = new NewUserListener(calendarService_, params);
    organizationService_.addListenerPlugin(newUserListener);

    // Create new user
    String newUserName = "testUser";
    User newUser = organizationService_.getUserHandler().createUserInstance(newUserName);
    organizationService_.getUserHandler().createUser(newUser, true);

    // Create event category list from config
    String[] configValues = defaultEventCategoriesConfig.split(Utils.COMMA);
    List<String> defaultEventCategories = new ArrayList<String>();
    defaultEventCategories.add(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
    for (int i = 0; i < configValues.length; i++) {
      defaultEventCategories.add(configValues[i].trim());
    }



    // Test default calendar
    List<Calendar> calendars = calendarService_.getUserCalendars(newUserName, true);
    assertEquals(calendars.size(),1);
    assertEquals(calendars.get(0).getName(),newUserName);

    // Test default event categories
    List<EventCategory> eventCategories = calendarService_.getEventCategories(newUserName);
    for (EventCategory eventCategory : eventCategories) {
      calendarService_.removeEventCategory(newUserName,eventCategory.getId());
    }
    eventCategories = calendarService_.getEventCategories(newUserName);
    assertEquals(eventCategories.size(), 0);

    calendarService_.removeUserCalendar(newUserName, defaultCalendarId);
    organizationService_.getUserHandler().removeUser(newUserName, true);

  }

  public void testCalendar() throws Exception {

    // create/get calendar in private folder
    Calendar cal = new Calendar();
    cal.setName("myCalendar");
    cal.setDescription("Desscription");
    cal.setPublic(true);
    calendarService_.saveUserCalendar(username, cal, true);
    Calendar myCal = calendarService_.getUserCalendar(username, cal.getId());
    assertNotNull(myCal);
    assertEquals(myCal.getName(), "myCalendar");

    // create/get calendar in public folder
    cal.setPublic(false);
    cal.setGroups(new String[] { "users", "admin" });
    cal.setViewPermission(new String[] { "member:/users", "member:/admin" });
    cal.setEditPermission(new String[] { "admin" });
    calendarService_.savePublicCalendar(cal, true);
    Calendar publicCal = calendarService_.getGroupCalendar(cal.getId());
    assertNotNull(publicCal);
    assertEquals(publicCal.getName(), "myCalendar");

    // get calendar in public folder by groupId
    List<GroupCalendarData> groupCalendarList = calendarService_.getGroupCalendars(new String[] { "users" }, true, username);
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1);

    groupCalendarList = calendarService_.getGroupCalendars(new String[] { "admin" }, true, username);
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1);

    groupCalendarList = calendarService_.getGroupCalendars(new String[] { "admin1" }, true, username);
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 0);

    // update public calendar
    cal.setPublic(false);
    cal.setName("myCalendarUpdated");
    calendarService_.savePublicCalendar(cal, false);
    myCal = calendarService_.getGroupCalendar(cal.getId());
    assertEquals(myCal.getName(), "myCalendarUpdated");

    // remove public calendar
    Calendar removeCal = calendarService_.removePublicCalendar(cal.getId());
    assertEquals(removeCal.getName(), "myCalendarUpdated");

    // remove private calendar
    removeCal = calendarService_.removeUserCalendar(username, cal.getId());
    assertEquals(removeCal.getName(), "myCalendar");

    // calendar setting
    CalendarSetting setting = new CalendarSetting();
    setting.setBaseURL("url");
    calendarService_.saveCalendarSetting(username, setting);
    assertEquals("url", calendarService_.getCalendarSetting(username).getBaseURL());
  }

  //mvn test -Dtest=TestCalendarService#testSharedCalendar
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
    loginUser("john");
    EventQuery query = new UnifiedQuery();
    query.setText("calendarEvent");
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, rs.size());

    loginUser(username);

    rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(1, rs.size());

    calendarService_.removeSharedEvent("john", cal.getId(), calendarEvent.getId());
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);
    assertEquals(0, events.size());

    calendarService_.removeSharedCalendar("john", cal.getId());
    assertNull(calendarService_.getSharedCalendars("john", true));
  }


  public void testEventCategory() throws Exception {
    Calendar cal = new Calendar();
    cal.setName("myCalendar");
    cal.setDescription("Desscription");
    cal.setPublic(true);
    // create/get calendar in private folder
    calendarService_.saveUserCalendar(username, cal, true);
    Calendar myCal = calendarService_.getUserCalendar(username, cal.getId());
    assertNotNull(myCal);
    assertEquals(myCal.getName(), "myCalendar");

    EventCategory eventCategory = new EventCategory();
    String name = "eventCategoryName";
    eventCategory.setName(name);
    calendarService_.saveEventCategory(username, eventCategory, true);
    assertEquals(1, calendarService_.getEventCategories(username).size());
    assertNotNull(calendarService_.getEventCategory(username, eventCategory.getId()));

    // import, export calendar
    CalendarEvent calendarEvent = new CalendarEvent();
    calendarEvent.setCalendarId(cal.getId());
    calendarEvent.setSummary("sum");
    calendarEvent.setEventType(CalendarEvent.TYPE_EVENT);
    calendarEvent.setFromDateTime(new Date());
    calendarEvent.setToDateTime(new Date());
    calendarService_.saveUserEvent(username, cal.getId(), calendarEvent, true);

    List<String> calendarIds = new ArrayList<String>();
    calendarIds.add(cal.getId());
    OutputStream out = calendarService_.getCalendarImportExports(CalendarService.ICALENDAR).exportCalendar(username,
            calendarIds,
            "0",
            -1);
    ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes());

    assertNotNull(calendarService_.removeUserEvent(username, cal.getId(), calendarEvent.getId()));
    assertEquals(0, calendarService_.getUserEventByCalendar(username, calendarIds).size());
    assertNotNull(calendarService_.removeUserCalendar(username, cal.getId()));

    calendarService_.getCalendarImportExports(CalendarService.ICALENDAR).importCalendar(username,
            is,
            null,
            "importedCalendar",
            null,
            null,
            true);
    List<Calendar> cals = calendarService_.getUserCalendars(username, true);
    List<String> newCalendarIds = new ArrayList<String>();
    for (Calendar calendar : cals)
      newCalendarIds.add(calendar.getId());
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, newCalendarIds);
    assertEquals(events.get(0).getSummary(), "sum");
    calendarService_.removeUserEvent(username, events.get(0).getCalendarId(), events.get(0).getId()) ;
    // remove Event category
    calendarService_.removeEventCategory(username, eventCategory.getId());

    assertNotNull(calendarService_.removeUserCalendar(username, newCalendarIds.get(0)));
  }

  public void testPublicEvent() throws Exception {
    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setDescription("CalendarDesscription");
    cal.setPublic(true);
    calendarService_.savePublicCalendar(cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName1");
    calendarService_.saveEventCategory(username, eventCategory, true);

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getId());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.savePublicEvent(cal.getId(), calEvent, true);

    assertNotNull(calendarService_.getGroupEvent(calEvent.getId()));
    List<String> calendarIds = new ArrayList<String>();
    calendarIds.add(cal.getId());
    assertEquals(1, calendarService_.getGroupEventByCalendar(calendarIds).size());
    assertNotNull(calendarService_.removePublicEvent(cal.getId(), calEvent.getId()));

    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removeUserCalendar(username, cal.getId());
  }

  public void testPrivateEvent() throws Exception {
    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setDescription("CalendarDescription");
    cal.setPublic(false);
    calendarService_.saveUserCalendar(username, cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName2");
    calendarService_.saveEventCategory(username, eventCategory, true);

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getId());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, true);

    EventQuery query = new EventQuery();
    query.setCategoryId(new String[] { eventCategory.getId() });
    assertEquals(calendarService_.getUserEvents(username, query).size(), 1);

    EventQuery eventQuery = new EventQuery();
    eventQuery.setText("Have a meeting");

    assertEquals(1, calendarService_.searchEvent(username, eventQuery, new String[] {}).getAll().size());
    assertEquals(1, calendarService_.getEvents(username, eventQuery, new String[] {}).size());

    List<CalendarEvent> list = new ArrayList<CalendarEvent>();
    list.add(calEvent);
    Calendar movedCal = new Calendar();
    movedCal.setName("MovedCalendarName");
    movedCal.setDescription("CalendarDescription");
    movedCal.setPublic(false);
    calendarService_.saveUserCalendar(username, movedCal, true);

    calendarService_.moveEvent(cal.getId(), movedCal.getId(), calEvent.getCalType(), calEvent.getCalType(), list, username);
    eventQuery = new EventQuery();
    eventQuery.setCalendarId(new String[] { movedCal.getId() });
    assertEquals(1, calendarService_.getEvents(username, eventQuery, new String[] {}).size());

    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removeUserCalendar(username, cal.getId());
  }

  public void testLastUpdatedTime() throws Exception {
    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setDescription("CalendarDesscription");
    cal.setPublic(true);
    calendarService_.savePublicCalendar(cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("LastUpdatedTimeEventCategoryName");
    calendarService_.saveEventCategory(username, eventCategory, true);

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getId());
    calEvent.setCalendarId(cal.getId());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.savePublicEvent(cal.getId(), calEvent, true);

    CalendarEvent event = calendarService_.getGroupEvent(cal.getId(), calEvent.getId());
    Date createdDate = event.getLastUpdatedTime();
    assertNotNull(createdDate);
    event.setSummary("Have a new meeting");
    calendarService_.savePublicEvent(cal.getId(), event, false);
    Date modifiedDate = calendarService_.getGroupEvent(cal.getId(), event.getId()).getLastUpdatedTime();
    assertNotNull(modifiedDate);
    assertTrue(modifiedDate.after(createdDate));

    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removePublicCalendar(cal.getId());
  }

  public void testFeed() throws Exception {
    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setPublic(false);
    calendarService_.saveUserCalendar(username, cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName3");
    calendarService_.saveEventCategory(username, eventCategory, true);

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getId());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, true);

    LinkedHashMap<String, Calendar> calendars = new LinkedHashMap<String, Calendar>();
    calendars.put(Utils.PRIVATE_TYPE + Utils.COLON + cal.getId(), cal);
    RssData rssData = new RssData();

    String name = "RSS";
    rssData.setName(name + Utils.RSS_EXT);
    String url = "http://localhost:8080/csdemo/rest-csdemo/cs/calendar/feed/" + username + Utils.SLASH + name + Utils.SLASH
            + IdGenerator.generate() + Utils.RSS_EXT;
    rssData.setUrl(url);
    rssData.setTitle(name);
    rssData.setDescription("Description");
    rssData.setLink(url);
    rssData.setVersion("rss_2.0");

    calendarService_.generateRss(username, calendars, rssData);
    assertEquals(1, calendarService_.getFeeds(username).size());
    calendarService_.removeFeedData(username, name);
    assertEquals(0, calendarService_.getFeeds(username).size());

    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removeUserCalendar(username, cal.getId());
  }

  public void testRemoteCalendar() throws Exception {
    String remoteUrl = "http://www.google.com/calendar/ical/exomailtest@gmail.com/private-462ee65e38f964b0aa64a37b427ed673/basic.ics";

    // test Remote ICS
    RemoteCalendarService remoteCalendarService = calendarService_.getRemoteCalendarService();
    RemoteCalendar remoteCal = new RemoteCalendar();
    remoteCal.setType(CalendarService.ICALENDAR);
    remoteCal.setUsername(username);
    remoteCal.setRemoteUrl(remoteUrl);
    remoteCal.setCalendarName("CalendarName");
    remoteCal.setDescription("Description");
    remoteCal.setSyncPeriod("Auto");
    remoteCal.setBeforeDate(0);
    remoteCal.setAfterDate(0);
    Calendar cal;
    try {
      cal = remoteCalendarService.importRemoteCalendar(remoteCal);
    } catch (IOException e) {
      log.info("Exception occurs when connect to remote calendar. Skip this test.");
      return;
    }
    //cal.setCategoryId(calCategory.getId());
    calendarService_.saveUserCalendar(username, cal, true);

    Calendar newCal = calendarService_.getCalendarById(cal.getId());
    assertNotNull(newCal);
    assertEquals(newCal.getId(), cal.getId());
    //List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, Arrays.asList(cal.getId()));
    //assertTrue(events.size() > 0);

    boolean isRemoteCalendar = calendarService_.isRemoteCalendar(username, cal.getId());
    assertTrue(isRemoteCalendar);

    RemoteCalendar remoteCalendar = calendarService_.getRemoteCalendar(username, cal.getId());
    assertEquals(remoteUrl, remoteCalendar.getRemoteUrl());

    Calendar calendar1 = calendarService_.getRemoteCalendar(username, remoteUrl, CalendarService.ICALENDAR);
    assertEquals(cal.getId(), calendar1.getId());

    int remoteCalendarCount = calendarService_.getRemoteCalendarCount(username);
    assertEquals(1, remoteCalendarCount);

    String newRemoteUrl = "https://www.google.com/calendar/dav/exomailtest@gmail.com/events/";
    remoteCalendar.setRemoteUrl(newRemoteUrl);
    calendarService_.updateRemoteCalendarInfo(remoteCalendar);

    remoteCalendar = calendarService_.getRemoteCalendar(username, cal.getId());
    assertEquals(newRemoteUrl, remoteCalendar.getRemoteUrl());

    calendarService_.removeUserCalendar(username, cal.getId());

    // test RemoteCaldav
    remoteCal.setType(CalendarService.CALDAV);
    remoteCal.setRemoteUser("exomailtest@gmail.com");
    remoteCal.setRemotePassword("tuanpham");
    remoteCal.setRemoteUrl("https://www.google.com/calendar/dav/exomailtest@gmail.com/events/");
    try {
      cal = remoteCalendarService.importRemoteCalendar(remoteCal);
    } catch (Exception e) {
      if(log.isDebugEnabled())
      log.info("Exception occurs when connect to remote calendar. Skip this test.");
      return;
    }

    List<CalendarEvent> events1 = calendarService_.getUserEventByCalendar(username, Arrays.asList(cal.getId()));
    assertTrue(events1.size() > 0);
    calendarService_.removeUserCalendar(username, cal.getId());
  }

  public void testGetUserCalendar() {
    try {
      Calendar calendar = calendarService_.getUserCalendar(username, "Not exist calendar");
      assertNull(calendar);
    } catch (Exception e) {
      fail();
    }
  }

  public void testSaveUserCalendar() {
    try {
      Calendar calendar = createCalendar("CalendarName", "CalendarDesscription");

      // Edit calendar
      String newCalendarName = "CalendarName edited";
      calendar.setName(newCalendarName);

      calendarService_.saveUserCalendar(username, calendar, false);
      Calendar edidedCalendar = calendarService_.getUserCalendar(username, calendar.getId()) ;
      assertEquals(newCalendarName, edidedCalendar.getName());

      calendarService_.removeUserCalendar(username, calendar.getId());
    } catch (Exception e) {
      //fail();
    }
  }

  public void testSavePublicCalendar() {
    Calendar calendar = createPublicCalendar("CalendarName", "CalendarDesscription");

    try {
      assertNotNull(calendarService_.getGroupCalendar(calendar.getId())) ;
      assertEquals(calendar, calendarService_.getCalendarById(calendar.getId()));
      calendarService_.removePublicCalendar(calendar.getId()) ;
    } catch (Exception e) {
      fail();
    }

  }


  public void testSaveEventCategory() {
    try {
      Calendar calendar = createCalendar("myCalendar", "Description");
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription");

      String eventCategoryName = "eventCategoryName1";
      EventCategory eventCategory = createEventCategory(eventCategoryName, "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);

      // Create user event
      CalendarEvent userEvent = createUserEvent(calendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      // Create public event
      CalendarEvent publicEvent = createPublicEvent(publicCalendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      // Edit event category
      String newEventCategoryName = "newEventCategoryName";
      eventCategory.setName(newEventCategoryName);
      calendarService_.saveEventCategory(username, eventCategory, false);

      // Check edited event category
      EventCategory edidedEventCategory = calendarService_.getEventCategory(username, eventCategory.getId());
      assertNotNull(edidedEventCategory);
      assertEquals(newEventCategoryName, edidedEventCategory.getName());

      calendarService_.removeUserEvent(username, calendar.getId(), userEvent.getId());
      calendarService_.removePublicEvent(publicCalendar.getId(), publicEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testRemoveEventCategory() {
    try {
      Calendar calendar = createCalendar("myCalendar", "Description");
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription");

      EventCategory eventCategory = createEventCategory("eventCategoryName2", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);

      CalendarEvent userEvent = createUserEvent(calendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);
      CalendarEvent publicEvent = createPublicEvent(publicCalendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      // Remove event category
      calendarService_.removeEventCategory(username, eventCategory.getId());

      // Check removed event category
      try {
        calendarService_.getEventCategory(username, eventCategory.getId());

        // If not throw exception then fail
        fail();
      } catch (PathNotFoundException ex) {
      }

      // Check user event
      CalendarEvent calendarEvent3 = calendarService_.getEvent(username, userEvent.getId());
      assertNotNull(calendarEvent3);
      assertEquals(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL, calendarEvent3.getEventCategoryId());
      assertEquals(NewUserListener.DEFAULT_EVENTCATEGORY_NAME_ALL, calendarEvent3.getEventCategoryName());

      // Check public event
      CalendarEvent calendarEvent4 = calendarService_.getGroupEvent(publicCalendar.getId(), publicEvent.getId());
      assertNotNull(calendarEvent4);
      assertEquals(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL, calendarEvent4.getEventCategoryId());
      assertEquals(NewUserListener.DEFAULT_EVENTCATEGORY_NAME_ALL, calendarEvent4.getEventCategoryName());


      calendarService_.removeUserEvent(username, calendar.getId(), userEvent.getId());
      calendarService_.removePublicEvent(publicCalendar.getId(), publicEvent.getId());
      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testGetEvent() {
    try {
      Calendar calendar = createCalendar("myCalendar", "Description");

      EventCategory eventCategory = createEventCategory("eventCategoryName3", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);

      // Create attachment
      String attachmentName = "Acttach file";
      String attachmentMinetype = "MimeType";
      Attachment attachment = new Attachment() ;
      attachment.setName(attachmentName) ;
      attachment.setInputStream(new InputStream() {
        @Override
        public int read() throws IOException {
          return 0;
        }
      }) ;
      attachment.setMimeType(attachmentMinetype) ;

      // Create reminder
      String reminderType = Reminder.TYPE_BOTH;
      long reminderAlarmBefore = new Date().getTime();
      String reminderEmailAddress = "abc@gmail.com";
      Reminder reminder = new Reminder(reminderType);
      reminder.setAlarmBefore(reminderAlarmBefore);
      reminder.setEmailAddress(reminderEmailAddress);
      reminder.setRepeate(false);

      // Create and save event
      String eventSummay = "Have a meeting";
      CalendarEvent calendarEvent = new CalendarEvent();
      calendarEvent.setEventCategoryId(eventCategory.getId());
      calendarEvent.setEventCategoryName(eventCategory.getName());
      calendarEvent.setSummary(eventSummay);
      calendarEvent.setFromDateTime(fromCal.getTime());
      calendarEvent.setToDateTime(toCal.getTime());
      calendarEvent.setAttachment(Arrays.asList(attachment));
      calendarEvent.setReminders(Arrays.asList(reminder));
      calendarService_.saveUserEvent(username, calendar.getId(), calendarEvent, true);

      CalendarEvent findEvent = calendarService_.getEvent(username, calendarEvent.getId());
      assertNotNull(findEvent);
      assertEquals(eventSummay, findEvent.getSummary());

      // Check attachment
      List<Attachment> attachments = findEvent.getAttachment();
      assertNotNull(attachments);
      assertEquals(1, attachments.size());
      Attachment eventAttachment = attachments.get(0);
      assertEquals(attachmentName, eventAttachment.getName());
      assertEquals(attachmentMinetype, eventAttachment.getMimeType());

      // Check reminder
      List<Reminder> reminders = findEvent.getReminders();
      assertNotNull(reminders);
      assertEquals(1, reminders.size());
      Reminder eventReminder = reminders.get(0);
      assertEquals(reminderType, eventReminder.getReminderType());
      assertEquals(reminderAlarmBefore, eventReminder.getAlarmBefore());
      assertEquals(reminderEmailAddress, eventReminder.getEmailAddress());
      assertEquals(false, eventReminder.isRepeat());

      // Xóa reminder
      findEvent.setReminders(null);
      calendarService_.saveUserEvent(username, calendar.getId(), findEvent, false);

      CalendarEvent findEvent1 = calendarService_.getEvent(username, calendarEvent.getId());
      assertNotNull(findEvent1);
      List<Reminder> reminders1 = findEvent1.getReminders();
      assertEquals(0, reminders1.size());

      calendarService_.removeUserEvent(username, calendar.getId(), calendarEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removeUserCalendar(username, calendar.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testGetEventById() throws Exception {
    Calendar calendar = createCalendar("myCalendar", "Description");

    EventCategory eventCategory = createEventCategory("eventCategoryName3", "description");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    // Create and save event
    String eventSummay = "Have a meeting";
    CalendarEvent calendarEvent = new CalendarEvent();
    calendarEvent.setEventCategoryId(eventCategory.getId());
    calendarEvent.setEventCategoryName(eventCategory.getName());
    calendarEvent.setSummary(eventSummay);
    calendarEvent.setFromDateTime(fromCal.getTime());
    calendarEvent.setToDateTime(toCal.getTime());
    calendarService_.saveUserEvent(username, calendar.getId(), calendarEvent, true);

    CalendarEvent findEvent1 = calendarService_.getEventById(calendarEvent.getId());
    assertNotNull(findEvent1);

    calendarService_.removeUserEvent(username, calendar.getId(), calendarEvent.getId());
    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removeUserCalendar(username, calendar.getId());
  }
  public void testRemoveSharedCalendarFolder() {
    try {
      createSharedCalendar("sharedCalendar", "shareDescription");

      calendarService_.removeSharedCalendarFolder("john");

      GroupCalendarData groupCalendarData = calendarService_.getSharedCalendars(username, true);
      assertNull(groupCalendarData);
    } catch (Exception e) {
      fail();
    }
  }

  public void testGetTypeOfCalendar() {
    try {
      Calendar calendar = createCalendar("myCalendar", "Description");
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription");
      Calendar sharedCalendar = createSharedCalendar("sharedCalendar", "shareDescription");

      assertEquals(Utils.PRIVATE_TYPE, calendarService_.getTypeOfCalendar(username, calendar.getId()));
      assertEquals(Utils.PUBLIC_TYPE, calendarService_.getTypeOfCalendar(username, publicCalendar.getId()));
      assertEquals(Utils.SHARED_TYPE, calendarService_.getTypeOfCalendar("john", sharedCalendar.getId()));
      assertEquals(Utils.INVALID_TYPE, calendarService_.getTypeOfCalendar(username, "Not exist id"));

      assertEquals(sharedCalendar, calendarService_.getCalendarById(sharedCalendar.getId()));

      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
      calendarService_.removeSharedCalendar(username, sharedCalendar.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testMoveEvent() {
    try {
      Calendar calendar = createCalendar("myCalendar", "Description");
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription");

      EventCategory eventCategory = createEventCategory("MoveEventCategory", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);

      CalendarEvent event = createPublicEvent(publicCalendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      List<CalendarEvent> events = new ArrayList<CalendarEvent>();
      events.add(event);

      calendarService_.moveEvent(publicCalendar.getId(),
              calendar.getId(),
              String.valueOf(Calendar.TYPE_PUBLIC),
              String.valueOf(Calendar.TYPE_PRIVATE),
              events,
              username);

      CalendarEvent userEvent = calendarService_.getEvent(username, event.getId());
      assertNotNull(userEvent);

      List<CalendarEvent> events1 = new ArrayList<CalendarEvent>();
      events1.add(userEvent);
      calendarService_.moveEvent(calendar.getId(),
              publicCalendar.getId(),
              String.valueOf(Calendar.TYPE_PRIVATE),
              String.valueOf(Calendar.TYPE_PUBLIC),
              events1,
              username);


      CalendarEvent publicEvent = calendarService_.getGroupEvent(publicCalendar.getId(), userEvent.getId());
      assertNotNull(publicEvent);

      calendarService_.removeUserEvent(username, calendar.getId(), userEvent.getId());
      calendarService_.removePublicEvent(publicCalendar.getId(), publicEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testCheckFreeBusy() {
    try {
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription");

      EventCategory eventCategory = createEventCategory("CheckFreeBusyCategory", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.DATE, 1);
      CalendarEvent publicEvent = createPublicEvent(publicCalendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      toCal.add(java.util.Calendar.DATE, 1);

      EventQuery eventQuery = new EventQuery();
      eventQuery.setFromDate(fromCal);
      eventQuery.setToDate(toCal);
      eventQuery.setParticipants(new String[] { "root" });
      eventQuery.setNodeType("exo:calendarPublicEvent");
      Map<String, String> parsMap = calendarService_.checkFreeBusy(eventQuery);

      calendarService_.removePublicEvent(publicCalendar.getId(), publicEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testCreateSessionProvider() {
    try {
      SessionProvider sessionProvider = storage_.createSessionProvider();
      assertNotNull(sessionProvider);
    } catch (Exception e) {
      fail();
    }
  }

  public void testUpdateRecurrenceSeries() {
    try {

      Calendar calendar = createCalendar("myCalendar", "Description");
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription");

      EventCategory eventCategory = createEventCategory("eventCategoryName0", "description");

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

      calendarService_.removeUserEvent(username, calendar.getId(), userEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testCalculateRecurrenceFinishDate() {
    try {
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
      userEvent.setRepeatByMonthDay(new long[] { 2, 3, 4, 5, 7 });

      Date date = storage_.calculateRecurrenceFinishDate(userEvent);

      java.util.Calendar calendar = java.util.Calendar.getInstance(timeZone);
      calendar.setTime(date);

      assertEquals(2011, calendar.get(java.util.Calendar.YEAR));
      assertEquals(6, calendar.get(java.util.Calendar.MONTH));
      assertEquals(25, calendar.get(java.util.Calendar.DATE));
      assertEquals(0, calendar.get(java.util.Calendar.HOUR));
      assertEquals(0, calendar.get(java.util.Calendar.MINUTE));
    } catch (Exception e) {
      fail();
    }
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

    Calendar calendar = createCalendar(username, "unified search test");
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

    calendarService_.saveUserCalendar(username, calendar, true);
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
    calendarService_.removeUserEvent(username, calendar.getId(), recurEvent.getId());
    calendarService_.removeUserCalendar(username, calendar.getId());
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
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, 2, 7, 5, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, 2, 7, 6, 30);

    Calendar calendar = createCalendar(username, "unified search test");
    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("repeated past");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_DAILY);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatCount(6);
    recurEvent.setRepeatUntilDate(null);
    recurEvent.setRepeatByDay(null);
    //recurEvent.setRepeatByMonthDay(new long[] { 2, 3, 4, 5, 7 });
    //recurEvent.setRepeatInterval(2);
    recurEvent.setRepeatUntilDate(null);
    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    calendarService_.saveUserCalendar(username, calendar, true);
    calendarService_.saveUserEvent(username, calendar.getId(), recurEvent, true);
    List<CalendarEvent> original = calendarService_.getOriginalRecurrenceEvents(username, from, to, null);
    assertEquals(1, original.size());

    Map<String, CalendarEvent> occMap = calendarService_.getOccurrenceEvents(recurEvent, from, to, timeZone);
    assertEquals(5, occMap.size());

    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);
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
    loginUser("john");
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

    calendarService_.removeSharedEvent("john", calendar.getId(), recurEvent.getId());
    calendarService_.removeSharedCalendar("john", calendar.getId());
    calendarService_.removeUserEvent(username, calendar.getId(), recurEvent.getId());
    calendarService_.removeUserCalendar(username, calendar.getId());

  }

  //mvn test -Dtest=TestCalendarService#testGetPublicEvents
  public void testGetPublicEvents() {
    try {
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription");

      EventCategory eventCategory = createEventCategory("GetPublicEventsCategory", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      fromCal.add(java.util.Calendar.HOUR, 1);
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 2);

      CalendarEvent publicEvent = createPublicEvent(publicCalendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);
      publicEvent.setPrivate(false);
      calendarService_.savePublicEvent(publicCalendar.getId(), publicEvent, false);
      publicEvent.setEventType(CalendarEvent.TYPE_EVENT);
      EventQuery eventQuery = new EventQuery();
      eventQuery.setCalendarId(new String[] {publicCalendar.getId()});
      List<CalendarEvent> events = calendarService_.getPublicEvents(eventQuery);
      assertEquals(1, events.size());
      CalendarEvent resultEvent = events.get(0);
      assertEquals(publicEvent.getId(), resultEvent.getId());
      assertEquals(publicEvent.getSummary(), resultEvent.getSummary());

      //Test search space event
      loginUser("john");
      EventQuery query = new UnifiedQuery();
      query.setText("  \" a meeting\" ");
      query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
      query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
      Collection<String> params = new ArrayList<String>();
      Collection<SearchResult> rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(1, rs.size());
      checkFields(rs.iterator().next());

      loginUser(username);

      rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(1, rs.size());
      checkFields(rs.iterator().next());

      //update group calendar 
      publicCalendar = calendarService_.getGroupCalendar(publicCalendar.getId());
      publicCalendar.setGroups(new String[]{"/platform/guests"});

      calendarService_.savePublicCalendar(publicCalendar, false);
      rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(0, rs.size());

      loginUser("demo");
      rs = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(1, rs.size());

      //update to space calendar 
      organizationService_.getGroupHandler().addGroupEventListener(new NewGroupListener(calendarService_, new InitParams()));
      Group parent = organizationService_.getGroupHandler().findGroupById("/spaces");
      Group g = organizationService_.getGroupHandler().createGroupInstance();
      g.setGroupName("spacetest");
      g.setLabel("Calendar Space");
      g.setDescription("simulate space creted");
      organizationService_.getGroupHandler().addChild(parent, g, true);
      g = organizationService_.getGroupHandler().findGroupById(g.getId());
      Collection<Group> gr = organizationService_.getGroupHandler().findGroupsOfUser("raul");
      assertEquals(1,gr.size());
      User u = organizationService_.getUserHandler().findUserByName("raul");
      MembershipType m = (MembershipType)organizationService_.getMembershipTypeHandler().findMembershipTypes().toArray()[0];
      organizationService_.getMembershipHandler().linkMembership(u, g, m, false);
      gr = organizationService_.getGroupHandler().findGroupsOfUser("raul");
      assertEquals(2,gr.size());

      List<GroupCalendarData> spaceCals = calendarService_.getGroupCalendars(new String[]{"/spaces/spacetest"}, true, "raul");
      //success save calendar by NewGroupListener;
      assertEquals(1, spaceCals.get(0).getCalendars().size());
      Calendar spaceCal = spaceCals.get(0).getCalendars().get(0) ;
      //spaceCal.setId(g.getGroupName() + Utils.SPACE_ID_PREFIX);
      //calendarService_.savePublicCalendar(spaceCal, true);
      publicEvent.setCalendarId(spaceCal.getId());
      publicEvent.setSummary("space event created");
      calendarService_.savePublicEvent(spaceCal.getId(), publicEvent, true);
      query.setText("\"space event\"");
      loginUser("raul");
      SearchContext sc = new SearchContext(loadConfiguration("conf/portal/controller.xml"), "");
      assertNotNull(sc);

      //Case build url for event detail in calendar
      rs = eventSearchConnector_.search(sc, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(1, rs.size());
      SearchResult item = rs.iterator().next();
      checkFields(item);
      assertEquals("/portal/classic/calendar/details/" + publicEvent.getId(), item.getUrl());

      //Case event is private but user have edit permission on group calendar
      publicEvent.setPrivate(true);
      calendarService_.savePublicEvent(spaceCal.getId(), publicEvent, false);
      rs = eventSearchConnector_.search(sc, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(0, rs.size());

      spaceCal.setEditPermission(new String[]{"raul"});
      calendarService_.savePublicCalendar(spaceCal, false);
      rs = eventSearchConnector_.search(sc, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(1, rs.size());

      spaceCal.setEditPermission(new String[]{"/spaces/spacetest/:*.*"});
      calendarService_.savePublicCalendar(spaceCal, false);
      rs = eventSearchConnector_.search(sc, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(1, rs.size());

      spaceCal.setEditPermission(new String[]{"/spaces/spacetest/:*.manager"});
      calendarService_.savePublicCalendar(spaceCal, false);
      rs = eventSearchConnector_.search(sc, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(0, rs.size());

      calendarService_.removePublicEvent(publicCalendar.getId(), publicEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
    } catch (Exception ex) {
      fail();
    }
  }

  public void testShareCalendarWithEditPermission() throws Exception {
    Calendar cal = createCalendar("test share", "test sharing with edit permission");
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

    CalendarEvent eventByMary = newEvent("mary");
    CalendarEvent eventByJohn = newEvent("john");
    CalendarEvent eventByDemo = newEvent("demo");

    try{
      calendarService_.saveEventToSharedCalendar("john", cal.getId(), eventByJohn, true);
      calendarService_.saveEventToSharedCalendar("mary", cal.getId(), eventByMary, true);
      calendarService_.saveEventToSharedCalendar("demo", cal.getId(), eventByDemo, true);
    } catch(Exception e) {
      fail();
    }
  }

  //mvn test -Dtest=TestCalendarService#testImportExportIcs
  public void testImportExportIcs() throws Exception {
    CalendarImportExport calIE = calendarService_.getCalendarImportExports(CalendarService.ICALENDAR);
    String calendarId = "IcsCalendar";
    Calendar cal = new Calendar();
    cal.setId(calendarId);
    cal.setName(calendarId);
    cal.setPublic(true);
    calendarService_.saveUserCalendar(username, cal, true);

    // event with high priority
    InputStream icalInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ObmCalendar_isolated.ics");
    // event with medium priority
    InputStream icalInputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("ObmCalendar_isolated_p2.ics");
    // event with low priority
    InputStream icalInputStream3 = Thread.currentThread().getContextClassLoader().getResourceAsStream("ObmCalendar_isolated_p3.ics");

    // test non-standard ics file, cf CAL-514, CAL-524
    InputStream icalInputStream4 = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("nonstandard.ics");
    InputStream icalInputStream5 = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("png_attachment.ics");

    calIE.importCalendar(username, icalInputStream, calendarId, calendarId, null, null, false);
    calIE.importCalendar(username, icalInputStream2, calendarId, calendarId, null, null, false);
    calIE.importCalendar(username, icalInputStream3, calendarId, calendarId, null, null, false);
    calIE.importCalendar(username, icalInputStream4, calendarId, calendarId, null, null, false);
    calIE.importCalendar(username, icalInputStream5, calendarId, calendarId, null, null, false);

    List<String> calendarIds = new ArrayList<String>();
    calendarIds.add(calendarId);
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);

    assertEquals(5, events.size());

    CalendarEvent event = events.get(0) ;
    assertEquals(CalendarEvent.PRIORITY[CalendarEvent.PRI_HIGH], event.getPriority());

    CalendarEvent event2 = events.get(1) ;
    assertEquals(CalendarEvent.PRIORITY[CalendarEvent.PRI_MEDIUM],  event2.getPriority());

    CalendarEvent event3 = events.get(2) ;
    assertEquals(CalendarEvent.PRIORITY[CalendarEvent.PRI_LOW],  event3.getPriority());

    assertNotNull(event.getFromDateTime());
    assertNotNull(event.getToDateTime());
    assertNotNull(event.getSummary());

    //export single event by id
    OutputStream icalOutputStream  =  calIE.exportEventCalendar(username, calendarId, CalendarImportExport.PRIVATE_TYPE, event.getId());
    assertNotNull(icalOutputStream);

    //export events in set of calendar
    icalOutputStream.close();
    icalOutputStream  =  calIE.exportCalendar(username, calendarIds, CalendarImportExport.PRIVATE_TYPE, 3);
    assertNotNull(icalOutputStream);

    icalOutputStream.close();
    icalInputStream.close();

    calendarService_.removeUserCalendar(username, calendarId);
  }
  //mvn test -Dtest=TestCalendarService#testImportCSVFile
  public void testImportCSVFile() throws Exception{
    CalendarImportExport calIE = calendarService_.getCalendarImportExports(CalendarService.EXPORTEDCSV);
    String calendarId = "CSVCalendar";
    Calendar cal = new Calendar();
    cal.setId(calendarId);
    cal.setName(calendarId);
    cal.setPublic(true);
    calendarService_.saveUserCalendar(username, cal, true);

    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sunbird_calendar.csv");
    calIE.importCalendar(username, in, calendarId, calendarId, null, null, false);
    List<String> calendarIds = new ArrayList<String>();
    calendarIds.add(calendarId);
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);
    assertEquals(3, events.size());

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
  /*
   * Utils for test
   */


  private CalendarEvent newEvent(String summary) {
    CalendarEvent event = new CalendarEvent();
    event.setSummary(summary);
    java.util.Calendar from = java.util.Calendar.getInstance();
    event.setFromDateTime(from.getTime());
    from.add(java.util.Calendar.HOUR, 1);
    event.setToDateTime(from.getTime());
    return event;
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

  private Calendar createCalendar(String name, String description) {
    try {
      // Create and save calendar
      Calendar calendar = new Calendar();
      calendar.setName(name);
      calendar.setDescription(description);
      calendar.setPublic(false);
      calendarService_.saveUserCalendar(username, calendar, true);
      return calendar;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private Calendar createPublicCalendar(String name, String description) {
    try {
      Calendar publicCalendar = new Calendar();
      publicCalendar.setName(name);
      publicCalendar.setDescription(description);
      publicCalendar.setPublic(true);
      publicCalendar.setGroups(new String[]{"/platform/users","/organization/management/executive-board"});
      calendarService_.savePublicCalendar(publicCalendar, true);
      return publicCalendar;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private EventCategory createEventCategory(String name, String description) {
    try {
      EventCategory eventCategory = new EventCategory();
      eventCategory.setName(name);
      calendarService_.saveEventCategory(username, eventCategory, true);
      return eventCategory;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private CalendarEvent createUserEvent(String calendarId,
                                        EventCategory eventCategory,
                                        String summary,
                                        java.util.Calendar fromCal,
                                        java.util.Calendar toCal) {
    try {
      CalendarEvent calendarEvent = new CalendarEvent();
      calendarEvent.setEventCategoryId(eventCategory.getId());
      calendarEvent.setEventCategoryName(eventCategory.getName());
      calendarEvent.setSummary(summary);
      calendarEvent.setFromDateTime(fromCal.getTime());
      calendarEvent.setToDateTime(toCal.getTime());
      calendarService_.saveUserEvent(username, calendarId, calendarEvent, true);
      return calendarEvent;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private CalendarEvent createUserEvent(String summary) throws Exception{
    CalendarEvent event = newEvent(summary);
    Calendar calendar = createCalendar("CalendarTest","CalendarTest");
    EventCategory category = createEventCategory("CalendarCategoryTest","CalendarCategoryTest");
    java.util.Calendar from = java.util.Calendar.getInstance();
    java.util.Calendar to   = java.util.Calendar.getInstance();
    to.add(java.util.Calendar.HOUR, 1);
    return createUserEvent(calendar.getId(),category,summary,from, to);
  }

  private CalendarEvent createPublicEvent(String publicCalendarId,
                                          EventCategory eventCategory,
                                          String summary,
                                          java.util.Calendar fromCal,
                                          java.util.Calendar toCal) {
    try {
      CalendarEvent publicEvent = new CalendarEvent();
      publicEvent.setEventCategoryId(eventCategory.getId());
      publicEvent.setEventCategoryName(eventCategory.getName());
      publicEvent.setSummary("Have a meeting");
      publicEvent.setFromDateTime(fromCal.getTime());
      publicEvent.setToDateTime(toCal.getTime());
      calendarService_.savePublicEvent(publicCalendarId, publicEvent, true);
      return publicEvent;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private CalendarEvent createRepetitiveEventForTest() throws  Exception{
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, 2, 7, 5, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, 2, 7, 6, 30);

    Calendar calendar = createCalendar(username, "TestSaveOneOccurrence");

    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("repeated past");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_DAILY);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatCount(6);
    recurEvent.setRepeatUntilDate(null);
    recurEvent.setRepeatByDay(null);
    recurEvent.setRepeatUntilDate(null);
    recurEvent.setCalendarId(calendar.getId());

    calendarService_.saveUserCalendar(username, calendar, true);
    calendarService_.saveUserEvent(username, calendar.getId(), recurEvent, true);

    return recurEvent;
  }
}
