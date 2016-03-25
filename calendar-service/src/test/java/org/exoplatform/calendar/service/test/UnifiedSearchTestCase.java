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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.impl.CalendarSearchResult;
import org.exoplatform.calendar.service.impl.CalendarSearchServiceConnector;
import org.exoplatform.calendar.service.impl.EventSearchConnector;
import org.exoplatform.calendar.service.impl.TaskSearchConnector;
import org.exoplatform.calendar.service.impl.UnifiedQuery;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.DateUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;

public class UnifiedSearchTestCase extends BaseCalendarServiceTestCase {

  private CalendarSearchServiceConnector unifiedSearchService_;

  private CalendarSearchServiceConnector eventSearchConnector_;

  private CalendarSearchServiceConnector taskSearchConnector_;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    unifiedSearchService_ = getService(CalendarSearchServiceConnector.class);
    taskSearchConnector_ = getService(TaskSearchConnector.class);
    eventSearchConnector_ = getService(EventSearchConnector.class);
  }

  public void testParseKeyword() {
    String keyword = "hello \" i am  a \" new guy";
    List<String> formated = UnifiedQuery.parse(keyword);
    assertEquals(4, formated.size());
    keyword = keyword + " \" why don't \"we talk \" ";
    formated = UnifiedQuery.parse(keyword);
    assertEquals(8, formated.size());
  }

  public void testUnifiedSeach() throws Exception {
    // Simple case
    login(username);
    String keyword = "hello \"how are\" you ";
    EventQuery query = new UnifiedQuery();
    query.setQueryType(Query.SQL);
    query.setText(keyword);
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_TITLE });
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = unifiedSearchService_.search(null,
                                                                   query.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   query.getOrderBy()[0],
                                                                   query.getOrderType());
    assertNotNull(result);
    assertEquals(0, result.size());

    // create/get calendar in private folder
    Calendar cal = createPrivateCalendar(username, "myCalendar", "Description");

    // =Test search generic type=//
    // Search summary
    CalendarEvent calEvent = createUserEvent(username,
                                             cal.getId(),
                                             "do you getting Have some busy day?",
                                             "", true);
    List<String> ids = new ArrayList<String>();
    ids.add(cal.getId());
    List<CalendarEvent> data = calendarService_.getUserEventByCalendar(username, ids);
    // Success to add event
    assertEquals(1, data.size());

    // =Keyword to search=//
    keyword = "Have \"you getting\" busy";
    query.setText(keyword);
    result = unifiedSearchService_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    // Success to search
    assertEquals(1, result.size());
    for (SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    // Search summary and description
    calEvent.setDescription("we have meeting with CEO");
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    keyword = "do \"you getting\" CEO";
    query.setText(keyword);
    result = unifiedSearchService_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    // Success to search
    assertEquals(1, result.size());
    for (SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    // Search summary , description and location
    calEvent.setLocation("in Hanoi");
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    keyword = "hanoi CEO";
    query.setText(keyword);
    query.setOrderBy(new String[] { Utils.ORDERBY_DATE });
    result = unifiedSearchService_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    // Success to search
    assertEquals(1, result.size());
    for (SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    // == test event search ==//
    calEvent.setEventType(CalendarEvent.TYPE_EVENT);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setOrderBy(new String[] { Utils.ORDERBY_RELEVANCY });
    result = eventSearchConnector_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    assertEquals(1, result.size());
    for (SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    // == test task search ==//
    calEvent.setEventType(CalendarEvent.TYPE_TASK);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    result = taskSearchConnector_.search(null,
                                         query.getText(),
                                         params,
                                         0,
                                         10,
                                         query.getOrderBy()[0],
                                         query.getOrderType());
    assertEquals(1, result.size());
    for (SearchResult item : result) {
      checkFieldsValueWithType(cal.getName(), calEvent, item);
    }

    // Does not search completed task
    calEvent.setEventState(CalendarEvent.COMPLETED);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    String status = CalendarEvent.COMPLETED + Utils.COLON + CalendarEvent.CANCELLED;
    query.setState(status);
    result = taskSearchConnector_.search(null,
                                         query.getText(),
                                         params,
                                         0,
                                         10,
                                         query.getOrderBy()[0],
                                         query.getOrderType());
    assertEquals(0, result.size());

    // search all need action
    calEvent.setEventState(CalendarEvent.NEEDS_ACTION);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setState(status);
    result = taskSearchConnector_.search(null,
                                         query.getText(),
                                         params,
                                         0,
                                         10,
                                         query.getOrderBy()[0],
                                         query.getOrderType());
    assertEquals(1, result.size());
    CalendarSearchResult calItem = (CalendarSearchResult) result.toArray()[0];
    assertEquals(calEvent.getEventState(), calItem.getTaskStatus());

    // search all inprocess
    calEvent.setEventState(CalendarEvent.IN_PROCESS);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setState(status);
    result = taskSearchConnector_.search(null,
                                         query.getText(),
                                         params,
                                         0,
                                         10,
                                         query.getOrderBy()[0],
                                         query.getOrderType());
    assertEquals(1, result.size());
    calItem = (CalendarSearchResult) result.toArray()[0];
    assertEquals(calEvent.getEventState(), calItem.getTaskStatus());

    // Does not search cancelled task
    calEvent.setEventState(CalendarEvent.CANCELLED);
    calendarService_.saveUserEvent(username, cal.getId(), calEvent, false);
    query.setState(status);
    result = taskSearchConnector_.search(null,
                                         query.getText(),
                                         params,
                                         0,
                                         10,
                                         query.getOrderBy()[0],
                                         query.getOrderType());
    assertEquals(0, result.size());
  }

  public void testSearchResultURL() throws Exception {
    Calendar cal = createPrivateCalendar(username, "myCalendar", "Description");
    createUserEvent(username, cal.getId(), "do you getting", "", true);

    String siteName = "classic";
    // test search context to build url
    SearchContext sc = new SearchContext(loadConfiguration("conf/portal/controller.xml"), siteName);
    assertNotNull(sc);
    Router rt = sc.getRouter();
    assertNotNull(rt);
    ExoContainerContext context = (ExoContainerContext) ExoContainerContext.getCurrentContainer()
                                                                           .getComponentInstanceOfType(ExoContainerContext.class);
    String portalName = context.getPortalContainerName();

    // router page expected return /portal/intranet/calendar
    String spaceGroupId = null;
    String url = unifiedSearchService_.getUrl(rt,
                                              portalName,
                                              siteName,
                                              spaceGroupId,
                                              Utils.PAGE_NAGVIGATION);
    assertEquals("/" + portalName + "/" + siteName + "/" + Utils.PAGE_NAGVIGATION, url);
    spaceGroupId = "/spaces/space1";
    // router space expected return /portal/g/:spaces:space1/space1/calendar
    url = unifiedSearchService_.getUrl(rt,
                                       portalName,
                                       siteName,
                                       spaceGroupId,
                                       Utils.PAGE_NAGVIGATION);
    assertEquals("/" + portalName + "/g/" + spaceGroupId.replaceAll(Utils.SLASH, ":") + "/space1/"
        + Utils.PAGE_NAGVIGATION, url);

    EventQuery query = new UnifiedQuery();
    query.setQueryType(Query.SQL);
    query.setText("do you getting");
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_TITLE });
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = eventSearchConnector_.search(sc,
                                                                   query.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   query.getOrderBy()[0],
                                                                   query.getOrderType());
    assertEquals(1, result.size());

    url = unifiedSearchService_.getUrl(rt, portalName, siteName, null, Utils.PAGE_NAGVIGATION);
    for (SearchResult sr : result) {
      checkFields(sr);
      assertEquals(url + Utils.SLASH + Utils.DETAIL_PATH + Utils.SLASH
                       + sr.getUrl().split(Utils.SLASH)[sr.getUrl().split(Utils.SLASH).length - 1],
                   sr.getUrl());
    }

    // site name null
    sc = new SearchContext(loadConfiguration("conf/portal/controller.xml"), null);
    siteName = Utils.DEFAULT_SITENAME;
    spaceGroupId = null;
    url = unifiedSearchService_.getUrl(rt,
                                       portalName,
                                       siteName,
                                       spaceGroupId,
                                       Utils.PAGE_NAGVIGATION);
    assertEquals("/" + portalName + "/" + siteName + "/" + Utils.PAGE_NAGVIGATION, url);
    spaceGroupId = "/spaces/space1";
    // router space expected return /portal/g/:spaces:space1/space1/calendar
    url = unifiedSearchService_.getUrl(rt,
                                       portalName,
                                       siteName,
                                       spaceGroupId,
                                       Utils.PAGE_NAGVIGATION);
    assertEquals("/" + portalName + "/g/" + spaceGroupId.replaceAll(Utils.SLASH, ":") + "/space1/"
        + Utils.PAGE_NAGVIGATION, url);
    result = eventSearchConnector_.search(sc,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    assertEquals(1, result.size());
    url = unifiedSearchService_.getUrl(rt, portalName, siteName, null, "");
    for (SearchResult sr : result) {
      checkFields(sr);
      assertEquals(url + Utils.SLASH + Utils.DETAIL_PATH + Utils.SLASH
                       + sr.getUrl().split(Utils.SLASH)[sr.getUrl().split(Utils.SLASH).length - 1],
                   sr.getUrl());
    }
  }

  public void testSearchWithDate() throws Exception {
    Calendar cal = createPrivateCalendar(username, "root calendar", "");
    CalendarEvent inPassEvent = createUserEvent(username, cal.getId(), "Summary CEO", "Hanoi", true);
    java.util.Calendar current = java.util.Calendar.getInstance();
    current.add(java.util.Calendar.HOUR_OF_DAY, -1);
    inPassEvent.setFromDateTime(current.getTime());
    current.add(java.util.Calendar.MINUTE, 30);
    inPassEvent.setToDateTime(current.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), inPassEvent, false);

    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username,
                                                                         Arrays.asList(new String[] { cal.getId() }));
    assertEquals(1, events.size());

    // Test case search only up coming events only
    EventQuery query = new UnifiedQuery();
    query.setQueryType(Query.SQL);
    query.setText("Hanoi CEO");
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_TITLE });
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = eventSearchConnector_.search(null,
                                                                   query.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   query.getOrderBy()[0],
                                                                   query.getOrderType());
    assertEquals(0, result.size());

    current = java.util.Calendar.getInstance();
    current.add(java.util.Calendar.MINUTE, 1);
    inPassEvent.setFromDateTime(current.getTime());
    current.add(java.util.Calendar.MINUTE, 30);
    inPassEvent.setToDateTime(current.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), inPassEvent, false);
    result = eventSearchConnector_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    assertEquals(1, result.size());

    // Search task due for and no need check from time
    current = java.util.Calendar.getInstance();
    current.add(java.util.Calendar.MINUTE, -1);
    inPassEvent.setFromDateTime(current.getTime());
    inPassEvent.setEventType(CalendarEvent.TYPE_TASK);
    calendarService_.saveUserEvent(username, cal.getId(), inPassEvent, false);
    result = taskSearchConnector_.search(null,
                                         query.getText(),
                                         params,
                                         0,
                                         10,
                                         query.getOrderBy()[0],
                                         query.getOrderType());
    assertEquals(1, result.size());

    // Search task not completed or not cancelled in pass
    CalendarEvent task2 = inPassEvent;
    task2.setId(new CalendarEvent().getId());
    task2.setEventState(CalendarEvent.NEEDS_ACTION);
    calendarService_.saveUserEvent(username, cal.getId(), task2, true);
    assertEquals(2,
                 calendarService_.getUserEventByCalendar(username,
                                                         Arrays.asList(new String[] { cal.getId() }))
                                 .size());
    result = taskSearchConnector_.search(null,
                                         query.getText(),
                                         params,
                                         0,
                                         10,
                                         query.getOrderBy()[0],
                                         query.getOrderType());
    assertEquals(2, result.size());
  }

  public void testSearchPermission() throws Exception {
    String john = "john";
    // Test query filter by permission
    Calendar johnCalendar = createPrivateCalendar(john, "johnCalendar", "");
    createUserEvent(john,
                    johnCalendar.getId(),
                    "Summary CEO come we will have some dayoff",
                    "Hanoi",
                    true);

    EventQuery query = new UnifiedQuery();
    query.setQueryType(Query.SQL);
    query.setText("Hanoi CEO");
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_TITLE });

    login(john);
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = eventSearchConnector_.search(null,
                                                                   query.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   query.getOrderBy()[0],
                                                                   query.getOrderType());
    assertEquals(1, result.size());

    login(username);
    result = eventSearchConnector_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    assertEquals(0, result.size());
  }

  public void testSearchOrder() throws Exception {
    Calendar cal = createPrivateCalendar(username, "myCalendar", "Description");
    CalendarEvent calEvent = createUserEvent(username,
                                             cal.getId(),
                                             "today is friday, we will have a weekend",
                                             "",
                                             true);
    CalendarEvent calEvent2 = createUserEvent(username,
                                              cal.getId(),
                                              "Summary CEO come we will have some dayoff",
                                              "friday", true);

    String keyword = "\"we will have\" friday";
    EventQuery query = new UnifiedQuery();
    query.setText(keyword);
    query.setOrderType(Utils.ORDER_TYPE_DESCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_RELEVANCY });
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> result = eventSearchConnector_.search(null,
                                                                   query.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   query.getOrderBy()[0],
                                                                   query.getOrderType());
    assertEquals(2, result.size());
    SearchResult item = (SearchResult) result.toArray()[0];
    checkFields(item);
    SearchResult item2 = (SearchResult) result.toArray()[1];
    checkFields(item2);
    assertEquals(false, item2.getRelevancy() > item.getRelevancy());

    query.setOrderBy(new String[] { Utils.ORDERBY_DATE });
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    result = eventSearchConnector_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    assertEquals(2, result.size());
    CalendarSearchResult calSerResult = (CalendarSearchResult) result.toArray()[0];
    checkFields(calSerResult);
    checkFieldsValueWithType(cal.getName(), calEvent, calSerResult);
    CalendarSearchResult calSerResult2 = (CalendarSearchResult) result.toArray()[1];
    checkFields(calSerResult2);
    checkFieldsValueWithType(cal.getName(), calEvent2, calSerResult2);
    assertEquals(true, item.getDate() < item2.getDate());
  }

  public void testUnifiedSeachEx() throws Exception {
    login(username);
    String calId = createPrivateCalendar(username, "root calendar", "").getId();
    createUserEvent(username, calId, "how do you do root", "you are a search able event", false);
    createUserEvent(username, calId, "are you here?", "I am not search able event with john", false);
    createUserEvent(username, calId, "are you here?", "I am search able task with john", false);

    String john = "john";
    calendarService_.shareCalendar(username, calId, Arrays.asList(new String[] { john }));

    // Simple case
    String keyword = "you are search able";
    EventQuery query = new UnifiedQuery();
    query.setText(keyword);
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_TITLE });
    Collection<String> params = new ArrayList<String>();

    Collection<SearchResult> result = unifiedSearchService_.search(null,
                                                                   query.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   query.getOrderBy()[0],
                                                                   query.getOrderType());
    assertNotNull(result);
    assertEquals(3, result.size());

    login(john);
    result = unifiedSearchService_.search(null,
                                          query.getText(),
                                          params,
                                          0,
                                          10,
                                          query.getOrderBy()[0],
                                          query.getOrderType());
    assertEquals(3, result.size());
  }

  public void testUnifiedSeachAllWord() throws Exception {
    String keyword = "abcde fghik";
    Calendar cal = createPrivateCalendar(username, "root calendar", "");

    createUserEvent(cal.getId(), null, keyword);
    createUserEvent(cal.getId(), null, "abcde");
    createUserEvent(cal.getId(), null, "fghik");

    // Simple case
    EventQuery query = new UnifiedQuery();
    query.setText(keyword);
    Collection<String> params = new ArrayList<String>();
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_TITLE });
    Collection<SearchResult> result = unifiedSearchService_.search(null,
                                                                   query.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   query.getOrderBy()[0],
                                                                   query.getOrderType());
    assertNotNull(result);
    assertEquals(result.size(), 1);
    assertEquals(keyword, result.iterator().next().getTitle());
  }

  public void testSearchSpecialCharacter() throws Exception {
    String keyword = "Have a%'\\_-\". meeting";
    createUserEvent(keyword);

    EventQuery query = new EventQuery();
    query.setText(keyword);

    query.setQueryType(Query.XPATH);
    assertEquals(1, calendarService_.searchEvent(username, query, new String[] {}).getAll().size());
    assertEquals(1, calendarService_.getEvents(username, query, new String[] {}).size());

    query.setQueryType(Query.SQL);
    assertEquals(1, calendarService_.searchEvent(username, query, new String[] {}).getAll().size());
    assertEquals(1, calendarService_.getEvents(username, query, new String[] {}).size());

    EventQuery uQuery = new UnifiedQuery();
    uQuery.setText(keyword);
    Collection<String> params = new ArrayList<String>();
    uQuery.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    uQuery.setOrderBy(new String[] { Utils.ORDERBY_TITLE });
    Collection<SearchResult> result = unifiedSearchService_.search(null,
                                                                   uQuery.getText(),
                                                                   params,
                                                                   0,
                                                                   10,
                                                                   uQuery.getOrderBy()[0],
                                                                   uQuery.getOrderType());
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  public void testMultiThreadSearch() throws Exception {
    String keyword = "Have a meeting";

    List<Calendar> calendars = new LinkedList<Calendar>();
    for (int i = 0; i < 100; i++) {
      Calendar cal = new Calendar();
      cal.setName("CalendarName" + i);
      calendarService_.saveUserCalendar(username, cal, true);
      calendars.add(cal);

      CalendarEvent calEvent = createCalendarEventInstance(keyword);
      calendarService_.saveUserEvent(username, cal.getId(), calEvent, true);
    }

    final EventQuery uQuery = new UnifiedQuery();
    uQuery.setText(keyword);
    final Collection<String> params = new ArrayList<String>();
    uQuery.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    uQuery.setOrderBy(new String[] { Utils.ORDERBY_TITLE });

    final AtomicBoolean fail = new AtomicBoolean(false);
    final CountDownLatch wait = new CountDownLatch(1);
    final PortalContainer container = getContainer();
    Runnable runner = new Runnable() {
      @Override
      public void run() {
        ExoContainerContext.setCurrentContainer(container);
        try {
          wait.await();
        } catch (InterruptedException e) {
        }
        begin();
        login(username);
        Collection<SearchResult> result = unifiedSearchService_.search(null,
                                                                       uQuery.getText(),
                                                                       params,
                                                                       0,
                                                                       -1,
                                                                       uQuery.getOrderBy()[0],
                                                                       uQuery.getOrderType());
        if (result == null || result.size() < 100) {
          fail.set(true);
        }
        end();
        ExoContainerContext.setCurrentContainer(null);
      }
    };

    List<Thread> threads = new LinkedList<Thread>();
    for (int i = 0; i < 20; i++) {
      Thread t = new Thread(runner);
      t.start();
      threads.add(t);
    }
    wait.countDown();

    // wait for all threads complete
    for (Thread t : threads) {
      t.join();
    }

    assertFalse(fail.get());
  }
  
  public void testUnifiedSeachDetail() throws Exception {
    login(username);
    Calendar cal = new Calendar();
    cal.setName("root calendar");
    cal.setTimeZone(TimeZone.getAvailableIDs(0)[0]); //GMT+0
    calendarService_.saveUserCalendar(username, cal, true);
    CalendarSetting calSetting = calendarService_.getCalendarSetting(username);

    String keyword = "test";
    CalendarEvent calEvent = createUserEvent(username, cal.getId(), keyword, "", true);
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.setTime(calEvent.getFromDateTime());

    //Simple case
    EventQuery query = new UnifiedQuery();
    query.setText(keyword);
    Collection<String> params = new ArrayList<String>();
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<SearchResult> result = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertNotNull(result);
    assertEquals(result.size(), 1);
    SearchResult r = result.iterator().next();
    assertEquals(keyword, r.getTitle());
    
    SimpleDateFormat df = new SimpleDateFormat(Utils.DATE_TIME_FORMAT);
    df.setTimeZone(DateUtils.getTimeZone(calSetting.getTimeZone()));
    String detail = "root calendar - " + df.format(fromCal.getTime());
    assertEquals(detail, r.getDetail());
  }

  public void testUnifiedSearchInReadOnlySharedCalendar() throws Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 5);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.MINUTE, 65);

    String keyword = "search this event" ;
    String publicEventTitle = "UnifiedSearchWithPublicEventInReadonlySharedCalendar";
    String privateEventTitle = "UnifiedSearchWithPrivateEventInReadonlySharedCalendar";

    //. Create shared calendar
    login(username);

    // Create calendar and share with John
    Calendar cal = new Calendar() ;
    cal.setName("root shared calendar as readonly");
    calendarService_.saveUserCalendar(username, cal, true);
    calendarService_.shareCalendar(username, cal.getId(), Arrays.asList("john"));

    //. Create public event
    CalendarEvent pubEvent = new CalendarEvent();
    pubEvent.setFromDateTime(fromCal.getTime());
    pubEvent.setToDateTime(toCal.getTime());
    pubEvent.setSummary(publicEventTitle);
    pubEvent.setDescription("you can search this event");
    pubEvent.setCalendarId(cal.getId());
    pubEvent.setPrivate(false);
    calendarService_.saveUserEvent(username, cal.getId(),pubEvent, true);

    // Create private event
    CalendarEvent priEvent = new CalendarEvent();
    priEvent.setFromDateTime(fromCal.getTime());
    priEvent.setToDateTime(toCal.getTime());
    priEvent.setSummary(privateEventTitle);
    priEvent.setDescription("you can not search this event");
    priEvent.setCalendarId(cal.getId());
    priEvent.setPrivate(true);
    calendarService_.saveUserEvent(username, cal.getId(), priEvent, true);

    login("john");
    Collection<SearchResult> result = unifiedSearchService_.search(null, keyword, Arrays.<String>asList(), 0, 10, Utils.ORDERBY_TITLE , Utils.ORDER_TYPE_ASCENDING);
    assertNotFoundInSearchResults(privateEventTitle, result);
    assertFoundInSearchResults(publicEventTitle, result);
  }

  public void testUnifiedSearchInEditableSharedCalendar() throws Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 5);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.MINUTE, 65);

    String keyword = "search this event";
    String publicEventTitle = "UnifiedSearchWithPublicEventInEditableSharedCalendar";
    String privateEventTitle = "UnifiedSearchWithPrivateEventInEditableSharedCalendar";

    //. Create shared calendar
    login(username);

    // Create calendar and share with John
    Calendar cal = new Calendar() ;
    cal.setName("root shared calendar as editable");
    calendarService_.saveUserCalendar(username, cal, true);
    calendarService_.shareCalendar(username, cal.getId(), Arrays.asList("john"));
    // User John have permission on this calendar
    cal.setEditPermission(new String[]{"john"});
    calendarService_.saveUserCalendar(username, cal, false);


    //. Create public event
    CalendarEvent pubEvent = new CalendarEvent();
    pubEvent.setFromDateTime(fromCal.getTime());
    pubEvent.setToDateTime(toCal.getTime());
    pubEvent.setSummary(publicEventTitle);
    pubEvent.setDescription("you can search this event");
    pubEvent.setCalendarId(cal.getId());
    pubEvent.setPrivate(false);
    calendarService_.saveUserEvent(username, cal.getId(),pubEvent, true);

    // Create private event
    CalendarEvent priEvent = new CalendarEvent();
    priEvent.setFromDateTime(fromCal.getTime());
    priEvent.setToDateTime(toCal.getTime());
    priEvent.setSummary(privateEventTitle);
    priEvent.setDescription("you can still search this event");
    priEvent.setCalendarId(cal.getId());
    priEvent.setPrivate(true);
    calendarService_.saveUserEvent(username, cal.getId(), priEvent, true);

    login("john");
    Collection<SearchResult> results = unifiedSearchService_.search(null, keyword, Arrays.<String>asList(), 0, 10, Utils.ORDERBY_TITLE , Utils.ORDER_TYPE_ASCENDING);
    assertFoundInSearchResults(privateEventTitle, results);
    assertFoundInSearchResults(publicEventTitle, results);
  }

  public void testUnifiedSearchInReadOnlyGroupCalendar() throws Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 5);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.MINUTE, 65);

    String keyword = "search this event";
    String publicEventTitle = "UnifiedSearchWithPublicEventInReadonlyGroupCalendar";
    String privateEventTitle = "UnifiedSearchWithPrivateEventInReadonlyGroupCalendar";

    //. Create shared calendar
    login(username);

    // Create group calendar
    Calendar cal = new Calendar();
    cal.setName("Group calendar");
    cal.setDescription("user group calendar");
    cal.setPublic(true);
    cal.setGroups(new String[]{"/platform/users"});
    calendarService_.savePublicCalendar(cal, true);


    //. Create public event
    CalendarEvent pubEvent = new CalendarEvent();
    pubEvent.setFromDateTime(fromCal.getTime());
    pubEvent.setToDateTime(toCal.getTime());
    pubEvent.setSummary(publicEventTitle);
    pubEvent.setDescription("you can search this event");
    pubEvent.setCalendarId(cal.getId());
    pubEvent.setPrivate(false);
    calendarService_.savePublicEvent(cal.getId(), pubEvent, true);

    // Create private event
    CalendarEvent priEvent = new CalendarEvent();
    priEvent.setFromDateTime(fromCal.getTime());
    priEvent.setToDateTime(toCal.getTime());
    priEvent.setSummary(privateEventTitle);
    priEvent.setDescription("you can not search this event");
    priEvent.setCalendarId(cal.getId());
    priEvent.setPrivate(true);
    calendarService_.savePublicEvent(cal.getId(), priEvent, true);

    login("john");
    Collection<SearchResult> result = unifiedSearchService_.search(null, keyword, Arrays.<String>asList(), 0, 10, Utils.ORDERBY_TITLE , Utils.ORDER_TYPE_ASCENDING);
    assertNotFoundInSearchResults(privateEventTitle, result);
    assertFoundInSearchResults(publicEventTitle, result);
  }

  public void testUnifiedSearchInEditableGroupCalendar() throws Exception {
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.MINUTE, 5);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.MINUTE, 65);

    String keyword = "search this event";
    String publicEventTitle = "UnifiedSearchWithPublicEventInEditableGroupCalendar";
    String privateEventTitle = "UnifiedSearchWithPrivateEventInEditableGroupCalendar";

    //. Create shared calendar
    login(username);

    // Create group calendar
    Calendar cal = new Calendar();
    cal.setName("Group calendar");
    cal.setDescription("user group calendar");
    cal.setPublic(true);
    cal.setGroups(new String[]{"/platform/users"});
    calendarService_.savePublicCalendar(cal, true);
    cal.setEditPermission(new String[]{"john"});
    calendarService_.savePublicCalendar(cal, false);


    //. Create public event
    CalendarEvent pubEvent = new CalendarEvent();
    pubEvent.setFromDateTime(fromCal.getTime());
    pubEvent.setToDateTime(toCal.getTime());
    pubEvent.setSummary(publicEventTitle);
    pubEvent.setDescription("you can search this event");
    pubEvent.setCalendarId(cal.getId());
    pubEvent.setPrivate(false);
    calendarService_.savePublicEvent(cal.getId(), pubEvent, true);

    // Create private event
    CalendarEvent priEvent = new CalendarEvent();
    priEvent.setFromDateTime(fromCal.getTime());
    priEvent.setToDateTime(toCal.getTime());
    priEvent.setSummary(privateEventTitle);
    priEvent.setDescription("you can still search this event");
    priEvent.setCalendarId(cal.getId());
    priEvent.setPrivate(true);
    calendarService_.savePublicEvent(cal.getId(), priEvent, true);

    login("john");
    Collection<SearchResult> result = unifiedSearchService_.search(null, keyword, Arrays.<String>asList(), 0, 10, Utils.ORDERBY_TITLE , Utils.ORDER_TYPE_ASCENDING);
    assertFoundInSearchResults(privateEventTitle, result);
    assertFoundInSearchResults(publicEventTitle, result);
  }

  public void assertNotFoundInSearchResults(String eventTitle, Collection<SearchResult> results) {
    for(SearchResult result : results) {
      if(result.getTitle().equals(eventTitle)) {
        fail("The event with title " + eventTitle + " should not found in search");
      }
    }
  }
  public void assertFoundInSearchResults(String eventTitle, Collection<SearchResult> results) {
    for (SearchResult result : results) {
      if (result.getTitle().equals(eventTitle)) {
        return;
      }
    }
    fail("The event with title " + eventTitle + " should be returned in search");
  }

  //mvn test -Dtest=TestCalendarService#testUnifiedSearchWithSpecialCharacter
  public void testUnifiedSearchWithSpecialCharacter() throws Exception {
    final String specialCharacter = "!.,:;\"'()\"-@#$%^~*<>?/}{[]-=|\\";

    login(username);

    // Create calendar
    Calendar calendar = new Calendar();
    calendar.setName("testUnifiedSeachWitchSpecialCharacter");
    calendar.setDescription("testUnifiedSeachWitchSpecialCharacter");
    calendar.setPublic(false);
    calendarService_.saveUserCalendar(username, calendar, true);

    // Create category
    EventCategory category = new EventCategory();
    category.setName("testUnifiedSeachWitchSpecialCharacter");
    calendarService_.saveEventCategory(username, category, true);

    // Create event with special character
    CalendarEvent event = this.createUserEvent(username, calendar.getId(), "" + specialCharacter, "Event with " + specialCharacter, false);
    //calendarService_.saveUserEvent(username, calendar.getId(), event, true);

    EventQuery query = new UnifiedQuery();
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<String> params = new ArrayList<String>();

    // Search with with all special key
    String keyword = specialCharacter + "~0.5" ;
    query.setText(keyword);
    Collection<SearchResult> results = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(0, results.size());

    // Search with 1 special character
    for(int i = 0; i < specialCharacter.length(); i++) {
      keyword = specialCharacter.charAt(i) + "~0.5";
      query.setText(keyword) ;
      results = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      // I can not assert here because some special character like * is accepted and it will return all existing event
      //assertEquals(0, results.size());
    }

    //
    for(int i = 0; i < specialCharacter.length(); i++) {
      keyword = "gatein" + specialCharacter.charAt(i) + "~0.5";
      query.setText(keyword) ;
      results = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(0, results.size());
    }

    //
    for(int i = 0; i < specialCharacter.length(); i++) {
      keyword = "gatein3" + specialCharacter.charAt(i) + "5~0.5";
      query.setText(keyword) ;
      results = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(0, results.size());
    }

    //
    for(int i = 0; i < specialCharacter.length(); i++) {
      keyword = "3" + specialCharacter.charAt(i) + "5~0.5";
      query.setText(keyword) ;
      results = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
      assertEquals(0, results.size());
    }
  }

  //mvn test -Dtest=TestCalendarService#testUnifiedSearchWithEmptyKeyword
  public void testUnifiedSearchWithEmptyKeyword() throws Exception {
    //. Create calendar and event
    Calendar calendar = createPrivateCalendar(username, "testUnifiedSeachWithEmptyKeyword", "testUnifiedSeachWithEmptyKeyword");
    createUserEvent(username, calendar.getId(), "testUnifiedSeachWithEmptyKeyword", "testUnifiedSeachWithEmptyKeyword", false);

    EventQuery query = new UnifiedQuery();
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<String> params = new ArrayList<String>();

    // keyword is empty string
    String keyword = "";
    query.setText(keyword);
    Collection<SearchResult> results = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(0, results.size());

    // keyword contains only space character
    keyword = "  ";
    query.setText(keyword);
    results = unifiedSearchService_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertEquals(0, results.size());
  }

  public void testSearchEventEndInFuture() throws Exception {
    Calendar calendar = createPrivateCalendar(username, "testSearchEventEndInFuture", "");
    //. Create event start from past and end in future
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.HOUR_OF_DAY, -1);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR_OF_DAY, 2);

    CalendarEvent event = createUserEvent(calendar.getId(), null, "testSearchEventEndInFuture", true, fromCal, toCal);

    EventQuery query = new UnifiedQuery();
    query.setText("testSearchEventEndInFuture~0.5");
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[]{Utils.ORDERBY_TITLE});
    Collection<String> params = new ArrayList<String>();

    Collection<SearchResult> results = eventSearchConnector_.search(null, query.getText(), params, 0, 10, query.getOrderBy()[0] , query.getOrderType());
    assertFoundInSearchResults(event.getSummary(), results);
  }

  private CalendarEvent createUserEvent(String username, String calId, String summary, String desc, boolean isPrivate) throws Exception {
    CalendarEvent calEvent = createCalendarEventInstance(summary);
    calEvent.setDescription(desc);
    calEvent.setPrivate(isPrivate);
    calendarService_.saveUserEvent(username, calId, calEvent, true);
    return calEvent;
  }

  // TODO: @nttuyen consider move it to common
  private Router loadConfiguration(String path) throws IOException {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    try {
      ControllerDescriptor routerDesc = new DescriptorBuilder().build(in);
      return new Router(routerDesc);
    } catch (RouterConfigException e) {
      log.info(e.getMessage());
    } finally {
      in.close();
    }
    return null;
  }

  private void checkFieldsValueWithType(String calName,
                                        CalendarEvent calEvent,
                                        CalendarSearchResult item) {
    checkFieldsValueWithType(calName, calEvent, (SearchResult) item);
    if (CalendarEvent.TYPE_EVENT.equals(calEvent.getEventType())) {
      assertEquals(item.getFromDateTime(), calEvent.getFromDateTime().getTime());
      assertNull(item.getImageUrl());
      assertEquals(Utils.EVENT_ICON_URL, item.getImageUrl());
    } else if (CalendarEvent.TYPE_TASK.equals(calEvent.getEventType())) {
      assertEquals(0, item.getFromDateTime());
      assertNotNull(item.getImageUrl());
      assertEquals(Utils.TASK_ICON_URL, item.getImageUrl());
    }
    assertNotNull(item.getTimeZoneName());
  }

  private void checkFieldsValueWithType(String calName, CalendarEvent calEvent, SearchResult item) {
    assertEquals(calEvent.getSummary(), item.getTitle());
    if (CalendarEvent.TYPE_EVENT.equals(calEvent.getEventType())) {
      if (calEvent.getLocation() != null)
        assertEquals(calName + Utils.SPACE + Utils.MINUS + Utils.SPACE
                         + df.format(calEvent.getFromDateTime()) + Utils.SPACE + Utils.MINUS
                         + Utils.SPACE + calEvent.getLocation(),
                     item.getDetail());
    } else {
      assertEquals(calName + Utils.SPACE + Utils.MINUS + Utils.SPACE + Utils.DUE_FOR
                       + df.format(calEvent.getToDateTime()),
                   item.getDetail());
    }
    SimpleDateFormat tempFm = new SimpleDateFormat("MM/dd/yyyy hh");
    assertEquals(tempFm.format(new Date()), tempFm.format(new Date(item.getDate())));
    assertEquals(true, item.getRelevancy() > 0);
    // case could not init url
    assertEquals(Utils.NONE_NAGVIGATION, item.getUrl());
    StringBuffer sb = new StringBuffer(Utils.EMPTY_STR);
    if (calEvent.getDescription() != null)
      sb.append(calEvent.getDescription());
    assertEquals(sb.toString(), item.getExcerpt());
  }

  private void checkFields(SearchResult item) {
    assertNotNull(item.getTitle());
    assertNotNull(item.getExcerpt());
    assertNotNull(item.getDetail());
    assertNull(item.getImageUrl());
    assertNotNull(item.getUrl());
    assertEquals(true, item.getDate() > 0);
  }

  private void checkFields(CalendarSearchResult item) {
    checkFields((SearchResult) (item));
    assertEquals(item.getDataType(), CalendarEvent.TYPE_EVENT);
    if (CalendarEvent.TYPE_EVENT.equals(item.getDataType())) {
      assertNull(item.getImageUrl());
      assertNull(item.getTaskStatus());
    } else if (CalendarEvent.TYPE_TASK.equals(item.getDataType())) {
      assertEquals(Utils.TASK_ICON_URL, item.getImageUrl());
      assertNotNull(item.getTaskStatus());
    }
  }
}
