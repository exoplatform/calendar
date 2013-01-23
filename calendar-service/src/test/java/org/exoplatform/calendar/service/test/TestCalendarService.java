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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import javax.jcr.PathNotFoundException;
import javax.jcr.query.Query;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
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
import org.exoplatform.calendar.service.impl.CalendarSearchServiceConnector;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.service.impl.UnifiedQuery;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */

public class TestCalendarService extends BaseCalendarServiceTestCase {
  public static final String COMA     = ",".intern();


  private OrganizationService organizationService_;
 
  private RepositoryService repositoryService_ ;
  private JCRDataStorage  storage_;
  public CalendarService calendarService_;
  private CalendarSearchServiceConnector unifiedSearchService_ ;
  private static String   username = "root";

  public void setUp() throws Exception {
    super.setUp();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) getService(NodeHierarchyCreator.class);
    repositoryService_ = getService(RepositoryService.class);
    storage_ = new JCRDataStorage(nodeHierarchyCreator,repositoryService_);
    organizationService_ = (OrganizationService) getService(OrganizationService.class);
    calendarService_ = getService(CalendarService.class);
    unifiedSearchService_ = getService(CalendarSearchServiceConnector.class);
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
 
  public void testUnifiedSeach() throws Exception{
    EventQuery query = new UnifiedQuery() ;
    EventQuery equery = new EventQuery() ;
    query.setQueryType(Query.SQL);
    assertNotNull(query.getQueryStatement());
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = unifiedSearchService_.search(query.getQueryStatement(), params, 0, 10, "" , query.getOrderType());
    assertNotNull(result) ;
    assertEquals(0, result.size());
    String keyword = "hello \" i am  a \" new guy" ;
    List<String> formated = UnifiedQuery.parse(keyword) ;
    assertEquals(4, formated.size());
    keyword = keyword + " \" why don't \"we talk \" ";
    formated = UnifiedQuery.parse(keyword) ;
    assertEquals(7, formated.size());
    
    keyword = " key \"word to\" search";
    //log.info("keyword:  " + keyword + "\n")  ;
    query.setText(keyword) ;
    //log.info("======== " + query.getQueryStatement()) ;
    //equery.setText(keyword);
    //equery.setQueryType(Query.SQL) ;
    //log.info("======== " + equery.getQueryStatement()) ;
    
    //create/get calendar in private folder
    Calendar cal = new Calendar();
    cal.setName("myCalendar");
    cal.setDescription("Desscription");
    //cal.setCategoryId();
    cal.setPublic(true);
    calendarService_.saveUserCalendar(username, cal, true);
    
    EventCategory eventCategory = new EventCategory();
    String name = "eventCategoryName";
    eventCategory.setName(name);
    eventCategory.setDescription("description");
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
    List<String> ids = new ArrayList<String>();
    ids.add(cal.getId());
    List<CalendarEvent> data = calendarService_.getUserEventByCalendar(username, ids) ;
    //Success to add event 
    assertEquals(1,data.size()) ;
    
    keyword = "Have" ;
    query.setText(keyword);
    result = unifiedSearchService_.search(query.getQueryStatement(), params, 0, 10, "" , query.getOrderType());
    //Success to search 
    //assertEquals(1, result.size()) ;
    
    
    calendarService_.removeUserEvent(username, ids.get(0), calEvent.getId());
    calendarService_.removeEventCategory(username, eventCategory.getId());
    assertNotNull(calendarService_.removeUserCalendar(username, ids.get(0)));
    
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
    params.put(NewUserListener.CALENDAR_CATEGORY, defaultCalendarCategoryIdParam);
    params.put(NewUserListener.CALENDAR_NAME, defaultCalendarIdParam);
    params.put(NewUserListener.EVENT_CATEGORIES, defaultEventCategoriesConfigParam);
    NewUserListener newUserListener = new NewUserListener(calendarService_, params);
    organizationService_.addListenerPlugin(newUserListener);

    // Create new user
    String newUserName = "testUser";
    User newUser = organizationService_.getUserHandler().createUserInstance(newUserName);
    organizationService_.getUserHandler().createUser(newUser, true);

    // Create event category list from config
    String[] configValues = defaultEventCategoriesConfig.split(COMA);
    List<String> defaultEventCategories = new ArrayList<String>();
    defaultEventCategories.add(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
    for (int i = 0; i < configValues.length; i++) {
      defaultEventCategories.add(configValues[i].trim());
    }

    // Test default calendar category
    List<GroupCalendarData> categories = calendarService_.getCalendarCategories(newUserName, true);
    assertEquals(1, categories.size());
    assertEquals(defaultCalendarCategoryId, categories.get(0).getId());

    // Test default calendar
    List<Calendar> calendars = calendarService_.getUserCalendars(newUserName, true);
    assertEquals(1, calendars.size());
    assertEquals(newUserName + "-" + defaultCalendarId, calendars.get(0).getId());

    
    // Test default event categories
    List<EventCategory> eventCategories = calendarService_.getEventCategories(newUserName);
    assertEquals(defaultEventCategories.size(), eventCategories.size());
    for (EventCategory eventCategory : eventCategories) {
      assertTrue(defaultEventCategories.contains(eventCategory.getId()));
    }
    for (EventCategory eventCategory : eventCategories) {
      calendarService_.removeEventCategory(newUserName,eventCategory.getId());
    }
    eventCategories = calendarService_.getEventCategories(newUserName);
    assertEquals(eventCategories.size(), 0);
    
    calendarService_.removeUserCalendar(newUserName, defaultCalendarId);
    organizationService_.getUserHandler().removeUser(newUserName, true);
    
  }

 
  public void testCalendar() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("categoryName");
    calCategory.setDescription("Description");
    calendarService_.saveCalendarCategory("root", calCategory, true);

    // create/get calendar in private folder
    Calendar cal = new Calendar();
    cal.setName("myCalendar");
    cal.setDescription("Desscription");
    cal.setCategoryId(calCategory.getId());
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

    // get calendar in private folder by categoryID
    List<Calendar> calendars = calendarService_.getUserCalendarsByCategory(username, calCategory.getId());
    assertNotNull(calendars);
    assertEquals(calendars.size(), 1);

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

    // remove private calendar category
    assertNotNull(calendarService_.removeCalendarCategory(username, calCategory.getId()));

    // calendar setting
    CalendarSetting setting = new CalendarSetting();
    setting.setBaseURL("url");
    setting.setLocation("location");
    calendarService_.saveCalendarSetting(username, setting);
    assertEquals("url", calendarService_.getCalendarSetting(username).getBaseURL());
    
    calendars = calendarService_.getUserCalendars(username, true);
    
    assertEquals(calendars.size(), 0);
  }

  
  public void testSharedCalendar() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("categoryName");
    calendarService_.saveCalendarCategory("root", calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("myCalendar");
    cal.setCategoryId(calCategory.getId());
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
    calendarEvent.setFromDateTime(new Date());
    calendarEvent.setToDateTime(new Date());
    calendarService_.saveEventToSharedCalendar("john", cal.getId(), calendarEvent, true);

    List<String> calendarIds = new ArrayList<String>();
    calendarIds.add(cal.getId());
    assertEquals(1, calendarService_.getSharedEventByCalendars("john", calendarIds).size());

    CalendarEvent event = calendarService_.getUserEventByCalendar(username, calendarIds).get(0);
    assertEquals("calendarEvent", event.getSummary());

    calendarService_.removeSharedEvent("john", cal.getId(), calendarEvent.getId());
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);
    assertEquals(0, events.size());

    calendarService_.removeSharedCalendar("john", cal.getId());
    assertNull(calendarService_.getSharedCalendars("john", true));
    calendarService_.removeCalendarCategory(username, calCategory.getId());
    
  }

  
  public void testCalendarCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("categoryName");
    calCategory.setDescription("Description");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setCategoryId(calCategory.getId());
    cal.setPublic(false);
    calendarService_.saveUserCalendar(username, cal, true);

    assertEquals(1, calendarService_.getCategories(username).size());
    List<GroupCalendarData> categories = calendarService_.getCalendarCategories(username, true);
    assertEquals(1, categories.size());

    List<Calendar> calendars = categories.get(0).getCalendars();
    assertEquals(1, calendars.size());
    Calendar calendar = calendars.get(0);
    assertEquals("CalendarName", calendar.getName());

    // get calendar category
    calCategory = calendarService_.getCalendarCategory(username, calCategory.getId());
    assertEquals(calCategory.getName(), "categoryName");

    // update calendar category
    calCategory.setName("categoryNameUpdated");
    calendarService_.saveCalendarCategory(username, calCategory, false);

    // remove calendar category
    CalendarCategory removeCate = calendarService_.removeCalendarCategory(username, calCategory.getId());
    assertEquals(removeCate.getName(), "categoryNameUpdated");
  }

 
  public void testEventCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("categoryName");
    calCategory.setDescription("Description");
    // calCategory.setCalendars(new String [] {""}) ;
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("myCalendar");
    cal.setDescription("Desscription");
    cal.setCategoryId(calCategory.getId());
    cal.setPublic(true);
    // create/get calendar in private folder
    calendarService_.saveUserCalendar(username, cal, true);
    Calendar myCal = calendarService_.getUserCalendar(username, cal.getId());
    assertNotNull(myCal);
    assertEquals(myCal.getName(), "myCalendar");

    EventCategory eventCategory = new EventCategory();
    String name = "eventCategoryName";
    eventCategory.setName(name);
    eventCategory.setDescription("description");
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

    // remove Event category
    calendarService_.removeEventCategory(username, eventCategory.getId());

    assertNotNull(calendarService_.removeUserCalendar(username, newCalendarIds.get(0)));
    assertNotNull(calendarService_.removeCalendarCategory(username, calCategory.getId()));
  }

  
  public void testPublicEvent() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calCategory.setDescription("CaldendarCategoryDescription");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setDescription("CalendarDesscription");
    cal.setCategoryId(calCategory.getId());
    cal.setPublic(true);
    calendarService_.savePublicCalendar(cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName1");
    eventCategory.setDescription("EventCategoryDescription");
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
    calendarService_.removeCalendarCategory(username, calCategory.getId());
  }

  
  public void testPrivateEvent() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calCategory.setDescription("CaldendarCategoryDescription");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setDescription("CalendarDesscription");
    cal.setCategoryId(calCategory.getId());
    cal.setPublic(false);
    calendarService_.saveUserCalendar(username, cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName2");
    eventCategory.setDescription("EventCategoryDescription");
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
    movedCal.setDescription("CalendarDesscription");
    movedCal.setCategoryId(calCategory.getId());
    movedCal.setPublic(false);
    calendarService_.saveUserCalendar(username, movedCal, true);

    calendarService_.moveEvent(cal.getId(), movedCal.getId(), calEvent.getCalType(), calEvent.getCalType(), list, username);
    eventQuery = new EventQuery();
    eventQuery.setCalendarId(new String[] { movedCal.getId() });
    assertEquals(1, calendarService_.getEvents(username, eventQuery, new String[] {}).size());

    calendarService_.removeEventCategory(username, eventCategory.getId());
    calendarService_.removeUserCalendar(username, cal.getId());
    calendarService_.removeCalendarCategory(username, calCategory.getId());
  }
 
  public void testLastUpdatedTime() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calCategory.setDescription("CaldendarCategoryDescription");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setDescription("CalendarDesscription");
    cal.setCategoryId(calCategory.getId());
    cal.setPublic(true);
    calendarService_.savePublicCalendar(cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("LastUpdatedTimeEventCategoryName");
    eventCategory.setDescription("EventCategoryDescription");
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
    calendarService_.removeUserCalendar(username, cal.getId());
    calendarService_.removeCalendarCategory(username, calCategory.getId());
  }

  public void testFeed() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName");
    cal.setCategoryId(calCategory.getId());
    cal.setPublic(false);
    calendarService_.saveUserCalendar(username, cal, true);

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName3");
    eventCategory.setDescription("EventCategoryDescription");
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
    calendarService_.removeCalendarCategory(username, calCategory.getId());
  }

  
  public void testRemoteCalendar() throws Exception {
    String remoteUrl = "http://www.google.com/calendar/ical/exomailtest@gmail.com/private-462ee65e38f964b0aa64a37b427ed673/basic.ics";

    // test Remote ICS
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calendarService_.saveCalendarCategory(username, calCategory, true);
    RemoteCalendarService remoteCalendarService = calendarService_.getRemoteCalendarService();
    RemoteCalendar remoteCal = new RemoteCalendar();
    remoteCal.setType(CalendarService.ICALENDAR);
    remoteCal.setUsername(username);
    remoteCal.setRemoteUrl(remoteUrl);
    remoteCal.setCalendarId(calCategory.getId());
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
    cal.setCategoryId(calCategory.getId());
    calendarService_.saveUserCalendar(username, cal, true);
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, Arrays.asList(cal.getId()));
    assertTrue(events.size() > 0);

    boolean isRemoteCalendar = calendarService_.isRemoteCalendar(username, cal.getId());
    assertTrue(isRemoteCalendar);

    RemoteCalendar remoteCalendar = calendarService_.getRemoteCalendar(username, cal.getId());
    assertEquals(remoteUrl, remoteCalendar.getRemoteUrl());

    Calendar calendar1 = calendarService_.getRemoteCalendar(username, remoteUrl, CalendarService.ICALENDAR);
    assertEquals(cal.getId(), calendar1.getId());
    assertEquals(cal.getCategoryId(), calendar1.getCategoryId());

    int remoteCalendarCount = calendarService_.getRemoteCalendarCount(username);
    assertEquals(1, remoteCalendarCount);

    String newRemoteUrl = "https://www.google.com/calendar/dav/exomailtest@gmail.com/events/";
    remoteCalendar.setRemoteUrl(newRemoteUrl);
    calendarService_.updateRemoteCalendarInfo(remoteCalendar);

    remoteCalendar = calendarService_.getRemoteCalendar(username, cal.getId());
    assertEquals(newRemoteUrl, remoteCalendar.getRemoteUrl());

    calendarService_.removeUserCalendar(username, cal.getId());

    //TODO this need to find MOCK server to test RemoteCaldav
    remoteCal.setType(CalendarService.CALDAV);
    remoteCal.setRemoteUser("exomailtest@gmail.com");
    remoteCal.setRemotePassword("");
    remoteCal.setRemoteUrl("https://www.google.com/calendar/dav/exomailtest@gmail.com/events/");
    try {
      cal = remoteCalendarService.importRemoteCalendar(remoteCal);
    } catch (Exception e) {
      log.info("Exception occurs when connect to remote calendar. Skip this test.");
      return;
    }

    List<CalendarEvent> events1 = calendarService_.getUserEventByCalendar(username, Arrays.asList(cal.getId()));
    assertTrue(events1.size() > 0);
    calendarService_.removeUserCalendar(username, cal.getId());
    calendarService_.removeCalendarCategory(username, calCategory.getId());
  }

 
  public void testGetUserCalendar() {
    try {
      Calendar calendar = calendarService_.getUserCalendar(username, "Not exist calendar");
      assertNull(calendar);
    } catch (Exception e) {
      fail();
    }
  }


  public void testSaveEventCategory() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());

      String eventCategoryName = "eventCategoryName1";
      EventCategory eventCategory = createEventCategory(eventCategoryName, "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);

      // Create user event
      CalendarEvent userEvent = createEvent(calendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      // Create public event
      CalendarEvent publicEvent = createPublicEvent(publicCalendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      // Edit event category
      String newEventCategoryName = "newEventCategoryName";
      String newDescription = "newDescription";
      eventCategory.setName(newEventCategoryName);
      eventCategory.setDescription(newDescription);
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
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testRemoveEventCategory() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());

      EventCategory eventCategory = createEventCategory("eventCategoryName2", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);

      CalendarEvent userEvent = createEvent(calendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);
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
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testGetEvent() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());

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

      // Remove reminder
      findEvent.setReminders(null);
      calendarService_.saveUserEvent(username, calendar.getId(), findEvent, false);

      CalendarEvent findEvent1 = calendarService_.getEvent(username, calendarEvent.getId());
      assertNotNull(findEvent1);
      List<Reminder> reminders1 = findEvent1.getReminders();
      assertEquals(0, reminders1.size());

      calendarService_.removeUserEvent(username, calendar.getId(), calendarEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testSaveUserEvent() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());

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
      
      log.info("=====BEFORE SAVING=====");
      log.info("START TIME: " + fromCal.getTime().toString());
      log.info("ENDTIME: " + toCal.getTime().toString());
      
      calendarService_.saveUserEvent(username, calendar.getId(), calendarEvent, true);
      
      CalendarEvent findEvent = calendarService_.getEvent(username, calendarEvent.getId());
      
      log.info("=====AFTER SAVING=====");
      log.info("START TIME: " + findEvent.getFromDateTime().toString());
      log.info("ENDTIME: " + findEvent.getToDateTime().toString());
      
      assertEquals(false, findEvent.getFromDateTime().equals(findEvent.getToDateTime()));
      // restore data storage
      calendarService_.removeUserEvent(username, calendar.getId(), calendarEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
    

  }
  public void testRemoveSharedCalendarFolder() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar cal = createSharedCalendar("sharedCalendar", "shareDescription", calendarCategory.getId());

      calendarService_.removeSharedCalendarFolder("john");

      GroupCalendarData groupCalendarData = calendarService_.getSharedCalendars(username, true);
      assertNull(groupCalendarData);
      calendarService_.removeUserCalendar(username, cal.getId());
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testGetTypeOfCalendar() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());
      Calendar sharedCalendar = createSharedCalendar("sharedCalendar", "shareDescription", calendarCategory.getId());

      assertEquals(Utils.PRIVATE_TYPE, calendarService_.getTypeOfCalendar(username, calendar.getId()));
      assertEquals(Utils.PUBLIC_TYPE, calendarService_.getTypeOfCalendar(username, publicCalendar.getId()));
      assertEquals(Utils.SHARED_TYPE, calendarService_.getTypeOfCalendar("john", sharedCalendar.getId()));
      assertEquals(Utils.INVALID_TYPE, calendarService_.getTypeOfCalendar(username, "Not exist id"));

      calendarService_.removeUserCalendar(username, calendar.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
      calendarService_.removeSharedCalendar(username, sharedCalendar.getId());
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testMoveEvent() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());

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
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testCheckFreeBusy() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());

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
      calendarService_.checkFreeBusy(eventQuery);

      calendarService_.removePublicEvent(publicCalendar.getId(), publicEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception e) {
      fail();
    }
  }

  public void testUpdateRecurrenceSeries() {
    try {
      TimeZone timezone = TimeZone.getTimeZone("GMT+7:00");

      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());

      EventCategory eventCategory = createEventCategory("eventCategoryName0", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance(timezone);
      java.util.Calendar toCal = java.util.Calendar.getInstance(timezone);
      toCal.add(java.util.Calendar.HOUR, 1);
      java.util.Calendar repeatUntilDate = java.util.Calendar.getInstance(timezone);
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

      storage_.getOccurrenceEvents(userEvent, fromCal, toCal, timezone.toString());

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
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
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

  public void testGetPublicEvents() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());

      EventCategory eventCategory = createEventCategory("GetPublicEventsCategory", "description");

      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);

      CalendarEvent publicEvent = createPublicEvent(publicCalendar.getId(), eventCategory, "Have a meeting", fromCal, toCal);

      EventQuery eventQuery = new EventQuery();
      eventQuery.setCalendarId(new String[] {publicCalendar.getId()});
      List<CalendarEvent> events = calendarService_.getPublicEvents(eventQuery);
      assertEquals(1, events.size());
      CalendarEvent resultEvent = events.get(0);
      assertEquals(publicEvent.getId(), resultEvent.getId());
      assertEquals(publicEvent.getSummary(), resultEvent.getSummary());

      calendarService_.removePublicEvent(publicCalendar.getId(), publicEvent.getId());
      calendarService_.removeEventCategory(username, eventCategory.getId());
      calendarService_.removePublicCalendar(publicCalendar.getId());
      calendarService_.removeCalendarCategory(username, calendarCategory.getId());
    } catch (Exception ex) {
      fail();
    }
  }

  private CalendarCategory createCalendarCategory(String name, String description) {
    try {
      // Create and save calendar category
      CalendarCategory calendarCategory = new CalendarCategory();
      calendarCategory.setName(name);
      calendarCategory.setDescription(description);
      calendarService_.saveCalendarCategory(username, calendarCategory, true);
      return calendarCategory;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private Calendar createSharedCalendar(String name, String description, String calendarCategoryId) {
    try {
      Calendar sharedCalendar = new Calendar();
      sharedCalendar.setName(name);
      sharedCalendar.setDescription(description);
      sharedCalendar.setCategoryId(calendarCategoryId);
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

  private Calendar createCalendar(String name, String desscription, String calendarCategoryId) {
    try {
      // Create and save calendar
      Calendar calendar = new Calendar();
      calendar.setName(name);
      calendar.setDescription(desscription);
      calendar.setCategoryId(calendarCategoryId);
      calendar.setPublic(false);
      calendarService_.saveUserCalendar(username, calendar, true);
      return calendar;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private Calendar createPublicCalendar(String name, String desscription, String calendarCategoryId) {
    try {
      Calendar publicCalendar = new Calendar();
      publicCalendar.setName(name);
      publicCalendar.setDescription(desscription);
      publicCalendar.setCategoryId(calendarCategoryId);
      publicCalendar.setPublic(true);
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
      eventCategory.setDescription("description");
      calendarService_.saveEventCategory(username, eventCategory, true);
      return eventCategory;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  private CalendarEvent createEvent(String calendarId,
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

 
}
