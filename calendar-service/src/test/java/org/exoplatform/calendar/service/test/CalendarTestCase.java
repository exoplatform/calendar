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

import javax.jcr.PathNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCollection;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RemoteCalendar;
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.User;

public class CalendarTestCase extends BaseCalendarServiceTestCase {
  private RepositoryService repositoryService_ ;
  private JCRDataStorage  storage_;

  public void setUp() throws Exception {
    super.setUp();
    repositoryService_ = getService(RepositoryService.class);
    storage_ = ((CalendarServiceImpl)calendarService_).getDataStorage();
  }

  public void testInitServices() throws Exception{

    assertNotNull(repositoryService_) ;
    assertEquals(repositoryService_.getDefaultRepository().getConfiguration().getName(), "repository");
    assertEquals(repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName(), "portal-test");
    assertNotNull(organizationService_) ;

    assertEquals(9, organizationService_.getUserHandler().findAllUsers().getSize());

    assertNotNull(storage_);

    assertNotNull(storage_.getUserCalendarHome(username));

    assertNotNull(storage_.getPublicCalendarHome());

    assertNotNull(storage_.getPublicCalendarServiceHome());

    assertNotNull(calendarService_) ;
  }

  public void testGetCalendarById() throws Exception {
    Calendar cal = createPrivateCalendar(username, "myCalendar", "Desscription");

    Calendar calSave = calendarService_.getCalendarById(cal.getId());
    assertNotNull(calSave);
    assertEquals(Calendar.TYPE_PRIVATE, calendarService_.getTypeOfCalendar(username, calSave.getId()));
    assertEquals("myCalendar", calSave.getName());
    assertEquals("Desscription", calSave.getDescription());
  }

  public void testDefaultData() throws Exception {
    String defaultEventCategoriesConfig = "Birthday,Memo,Wedding,DayOff";

    // Create valueParam
    ValueParam defaultEventCategoriesConfigParam = new ValueParam();
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
    List<String> results = new LinkedList<String>();
    for (EventCategory eventCategory : eventCategories) {
      results.add(eventCategory.getId());
    }
    assertEquals(defaultEventCategories.size(), results.size());
    assertTrue(results.containsAll(defaultEventCategories));
  }

  public void testCalendar() throws Exception {

    // create/get calendar in private folder
    Calendar uCal = createPrivateCalendar(username, "myCalendar", "Description");
    uCal = calendarService_.getUserCalendar(username, uCal.getId());
    assertNotNull(uCal);
    assertEquals(uCal.getName(), "myCalendar");

    // create/get calendar in public folder
    Calendar gCal = createGroupCalendar(new String[] { "users", "admin" }, "groupCalendar", "groupDes");    
    gCal = calendarService_.getGroupCalendar(gCal.getId());
    assertNotNull(gCal);
    assertEquals(gCal.getName(), "groupCalendar");

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
    gCal.setName("myCalendarUpdated");
    calendarService_.savePublicCalendar(gCal, false);
    gCal = calendarService_.getGroupCalendar(gCal.getId());
    assertEquals(gCal.getName(), "myCalendarUpdated");

    // remove public calendar
    Calendar removeCal = calendarService_.removePublicCalendar(gCal.getId());
    assertEquals(removeCal.getName(), "myCalendarUpdated");

    // remove private calendar
    removeCal = calendarService_.removeUserCalendar(username, uCal.getId());
    assertEquals(removeCal.getName(), "myCalendar");

    // calendar setting
    CalendarSetting setting = new CalendarSetting();
    setting.setBaseURL("url");
    calendarService_.saveCalendarSetting(username, setting);
    assertEquals("url", calendarService_.getCalendarSetting(username).getBaseURL());
  }

  public void testEventCategory() throws Exception {
    EventCategory eventCategory = new EventCategory();
    String name = "eventCategoryName";
    eventCategory.setName(name);
    calendarService_.saveEventCategory(username, eventCategory, true);
    assertEquals(1, calendarService_.getEventCategories(username).size());
    assertNotNull(calendarService_.getEventCategory(username, eventCategory.getId()));
  }

  public void testFeed() throws Exception {
    CalendarEvent event = createUserEvent("Have a meeting");
    String calId = event.getCalendarId();

    LinkedHashMap<String, Calendar> calendars = new LinkedHashMap<String, Calendar>();
    calendars.put(Utils.PRIVATE_TYPE + Utils.COLON + calId, calendarService_.getCalendarById(calId));
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

  public void testGetNonExistCalendar() {
    try {
      Calendar calendar = calendarService_.getUserCalendar(username, "Not exist calendar");
      assertNull(calendar);
    } catch (Exception e) {
      fail();
    }
  }

  public void testSaveUserCalendar() throws Exception {
    Calendar calendar = createPrivateCalendar(username, "CalendarName", "CalendarDesscription");
    
    // Edit calendar
    String newCalendarName = "CalendarName edited";
    calendar.setName(newCalendarName);
    
    calendarService_.saveUserCalendar(username, calendar, false);
    Calendar edidedCalendar = calendarService_.getUserCalendar(username, calendar.getId()) ;
    assertEquals(newCalendarName, edidedCalendar.getName());
  }

  public void testSaveGroupCalendar() throws Exception {
    Calendar calendar = createGroupCalendar(new String[]{"/platform/users", "/organization/management/executive-board"}, "CalendarName", "CalendarDesscription");
    assertNotNull(calendarService_.getGroupCalendar(calendar.getId())) ;
    assertEquals(calendar, calendarService_.getCalendarById(calendar.getId()));
  }
  
  public void testSaveSharedCalendar() throws Exception{
    Calendar shCal = createSharedCalendar(username + "-calendar-share", "shared to john", new String[] {"john"});
    shCal.setName(shCal.getName() + "-updated");
    Calendar shReturn = calendarService_.saveCalendar(username, shCal, Calendar.TYPE_SHARED, false);
    assertEquals(shReturn, calendarService_.getCalendarById(shCal.getId()));
  }
  
  public void testGetCalendars() throws Exception{
    int offset = 0;
    int limit = 5;
    int counter = 0;
    for(int i=0; i <100; i++){
      createPrivateCalendar(username, username + "-calendar-" +i, "description-"+i);
      counter++;
    }
    CalendarCollection<Calendar> pCals = calendarService_.getAllCalendars(username, Calendar.TYPE_PRIVATE, offset, limit);

    assertEquals(limit, pCals.size());
    offset = 5;
    limit = 10;

    pCals = calendarService_.getAllCalendars(username, Calendar.TYPE_PRIVATE, offset, limit);
    assertEquals(limit, pCals.size());

    offset = 0;
    String[] groups = new String[]{"/platform/users", "/organization/management/executive-board"};
    for(int i = 0 ; i < 100; i++) {
      createGroupCalendar(groups, "group cal-"+i, "group calendar");
      counter++;
    }
    CalendarCollection<Calendar> gCals = calendarService_.getAllCalendars(username, Calendar.TYPE_PUBLIC, offset, limit);
    assertEquals(limit, gCals.size());

    List<String> shareTo = new ArrayList<String>();
    shareTo.add("john");
    for(Calendar cal : pCals){
      calendarService_.shareCalendar(username, cal.getId(), shareTo);
    }
    login("john");
    CalendarCollection<Calendar> shCals = calendarService_.getAllCalendars("john", Calendar.TYPE_SHARED, offset, limit);
    assertEquals(limit, shCals.size());
    List<Calendar> jPcals = new ArrayList<Calendar>();
    for (int i = 0; i < 50; i++) {
      jPcals.add(createPrivateCalendar("john", "john-"+i, "john calendar "+i));
    }
    shareTo = new ArrayList<String>();
    shareTo.add("root");
    for(Calendar cal : jPcals){
      calendarService_.shareCalendar("john", cal.getId(), shareTo);
      counter++;
    }
    limit = 120;
    login(username);
    CalendarCollection<Calendar> cals = calendarService_.getAllCalendars(username, Calendar.TYPE_ALL, offset, limit);
    assertEquals(limit, cals.size());

    assertEquals(counter, cals.getFullSize());
  }
  
  public void testGetAllCalendarsInGroup() throws Exception {
    int offset = 0, limit = 100;
    for (int i = 0 ; i < 2; i++) {    
      createGroupCalendar(new String[] {"/platform/administrators"}, "group cal-"+ System.currentTimeMillis(), "group calendar");
    }
    for (int i = 0 ; i < 2; i++) {    
      createGroupCalendar(new String[] {"/platform/administrators, /platform/users"}, "group cal-"+ System.currentTimeMillis(), "group calendar");
    }
    for (int i = 0 ; i < 2; i++) {    
      createGroupCalendar(new String[] {"/platform/users"}, "group cal-"+ System.currentTimeMillis(), "group calendar");
    }
    for (int i = 0 ; i < 2; i++) {    
      createGroupCalendar(new String[] {"/platform/guests"}, "group cal-"+ System.currentTimeMillis(), "group calendar");
    }
    for (int i = 0 ; i < 2; i++) {    
      createGroupCalendar(new String[] {"/organization/management/executive-board"}, "group cal-"+ System.currentTimeMillis(), "group calendar");
    }
    assertEquals(8, calendarService_.getAllCalendars("root", Calendar.TYPE_PUBLIC, offset, limit).getFullSize());
    assertEquals(8, calendarService_.getAllCalendars("john", Calendar.TYPE_PUBLIC, offset, limit).getFullSize());
    assertEquals(4, calendarService_.getAllCalendars("mary", Calendar.TYPE_PUBLIC, offset, limit).getFullSize());
    assertEquals(6, calendarService_.getAllCalendars("demo", Calendar.TYPE_PUBLIC, offset, limit).getFullSize());
  }

  public void testSaveEventCategory() throws Exception {
    String eventCategoryName = "eventCategoryName1";
    EventCategory eventCategory = createUserEventCategory(username, eventCategoryName);
    
    // Edit event category
    String newEventCategoryName = "newEventCategoryName";
    eventCategory.setName(newEventCategoryName);
    calendarService_.saveEventCategory(username, eventCategory, false);

    // Check edited event category
    EventCategory edidedEventCategory = calendarService_.getEventCategory(username, eventCategory.getId());
    assertNotNull(edidedEventCategory);
    assertEquals(newEventCategoryName, edidedEventCategory.getName());
  }

  public void testRemoveEventCategory() throws Exception {
    Calendar calendar = createPrivateCalendar(username, "myCalendar", "Description");
    Calendar groupCalendar = createGroupCalendar(userGroups, "groupCalendar", "groupDescription");
    
    EventCategory eventCategory = createUserEventCategory(username, "eventCategoryName2");
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    
    CalendarEvent userEvent = createUserEvent(calendar.getId(), eventCategory, "Have a meeting", true, fromCal, toCal);
    CalendarEvent publicEvent = createGroupEvent(groupCalendar.getId(), eventCategory, "Have a meeting", true, fromCal, toCal);
    
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
    CalendarEvent calendarEvent4 = calendarService_.getGroupEvent(groupCalendar.getId(), publicEvent.getId());
    assertNotNull(calendarEvent4);
    assertEquals(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL, calendarEvent4.getEventCategoryId());
    assertEquals(NewUserListener.DEFAULT_EVENTCATEGORY_NAME_ALL, calendarEvent4.getEventCategoryName());
  }

  public void testCheckFreeBusy() throws Exception {
    Calendar publicCalendar = createGroupCalendar(userGroups, "publicCalendar", "publicDescription");
    
    EventCategory eventCategory = createUserEventCategory(username, "CheckFreeBusyCategory");
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.DATE, 1);
    createGroupEvent(publicCalendar.getId(), eventCategory, "Have a meeting", true, fromCal, toCal);
    
    toCal.add(java.util.Calendar.DATE, 1);
    
    EventQuery eventQuery = new EventQuery();
    eventQuery.setFromDate(fromCal);
    eventQuery.setToDate(toCal);
    eventQuery.setParticipants(new String[] { "root" });
    eventQuery.setNodeType("exo:calendarPublicEvent");
    calendarService_.checkFreeBusy(eventQuery);
  }

  //mvn test -Dtest=CalendarTestCase#testGetCalendarOfDisabledUser
  /*public void testGetCalendarOfDisabledUser() throws Exception{
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

    receiverUser.add("mary");
    calendarService_.shareCalendar(username, cal.getId(), receiverUser);
    login("mary");
    assertEquals(1, calendarService_.getSharedCalendars("mary", true).getCalendars().size());
    assertEquals(1, calendarService_.getSharedEventByCalendars("mary", calendarIds).size());
    EventQuery eq = new EventQuery();
    eq.setText("calendarEvent");
    assertEquals(1, calendarService_.getEvents("john", eq, null).size());
    assertEquals(1, calendarService_.getEvents("mary", eq, null).size());

    //Disable john (calendar of root is shared
    organizationService_.getUserHandler().setEnabled("john", false, false);
    assertNull(calendarService_.getSharedCalendars("john", true));
    assertEquals(0, calendarService_.getSharedEventByCalendars("john", calendarIds).size());
    assertEquals(0, calendarService_.getEvents("john", eq, null).size());



    calendarService_.removeSharedEvent("john", cal.getId(), calendarEvent.getId());
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);
    assertEquals(0, events.size());

    calendarService_.removeSharedCalendar("john", cal.getId());
    assertNull(calendarService_.getSharedCalendars("john", true));
  }*/
}
