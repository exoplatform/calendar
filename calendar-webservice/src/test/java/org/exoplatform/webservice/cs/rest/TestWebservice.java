/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.webservice.cs.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.AssertionFailedError;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;

/**
 * Created by The eXo Platform SARL Author : Volodymyr Krasnikov
 * volodymyr.krasnikov@exoplatform.com.ua
 */

public class TestWebservice extends AbstractResourceTest {

  CalendarWebservice calendarWebservice;
  CalendarService calendarService;

  static final String             baseURI = "";

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    calendarWebservice = (CalendarWebservice) container.getComponentInstanceOfType(CalendarWebservice.class);
    calendarService = (MockCalendarService) container.getComponentInstanceOfType(MockCalendarService.class);
    
    registry(calendarWebservice);
     
    //registry(calendarService);
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }


  private CalendarCategory createCalendarCategory() {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    return calCategory;
  }


  private Calendar createCalendar(CalendarCategory calCategory)  {
    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    return cal ;

  }

  private CalendarEvent createEvent(Calendar cal) {
    CalendarEvent event = new CalendarEvent();
    event.setSummary("test");
    event.setDescription("event description");
    event.setCalendarId(cal.getId());
    event.setEventCategoryId("eventCategoryId");
    event.setEventCategoryName("EventCategirtName");
    event.setFromDateTime(new Date());
    event.setToDateTime(new Date());
    return event ;
  }


  private CalendarEvent createTask(Calendar cal) {
    CalendarEvent event = new CalendarEvent();
    event.setEventType(CalendarEvent.TYPE_TASK);
    event.setSummary("test");
    event.setDescription("event description");
    event.setCalendarId(cal.getId());
    event.setEventCategoryId("eventCategoryId");
    event.setEventCategoryName("EventCategirtName");
    event.setFromDateTime(new Date());
    event.setToDateTime(new Date());
    return event ;
  }


  public void testCheckPublicRss() throws Exception {

    start();
    UserHandler hUser = orgService.getUserHandler();

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();

    String username = "root";

    h.putSingle("username", username);

    //Create calendar

    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //assertNotNull(calendarService);
    calendarService.saveCalendarCategory(username, calCategory, true) ;

    //create/get calendar in private folder
    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    calendarService.saveUserCalendar(username, cal, true);

    CalendarEvent event = new CalendarEvent();
    event.setSummary("test");
    event.setDescription("event description");
    event.setCalendarId(cal.getId());
    event.setEventCategoryId("eventCategoryId");
    event.setEventCategoryName("EventCategirtName");
    event.setFromDateTime(new Date());
    event.setToDateTime(new Date());
    calendarService.saveUserEvent(username, cal.getId(), event, true);

    event.setLocation(extURI.replaceFirst("rss", "subscribe") +"/"+ event.getId());

    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);

    response = service("GET", cal.getPublicUrl(), baseURI, h, null, writer);

    assertNotNull(response);
    //assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    //assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());
    //assertEquals(HTTPStatus.INTERNAL_ERROR, response.getStatus());
    assertEquals(HTTPStatus.OK, response.getStatus());

    response = service("GET", event.getLocation(), baseURI, h, null, writer);
    //assertEquals(HTTPStatus.OK, response.getStatus());

    //assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, response.getContentType());
    calendarService.removeCalendarCategory(username, calCategory.getId()); 
    stop();
  }

  public void testUpcomingEvent() throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "root";
    h.putSingle("username", username);
    // data correct
    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
    String eventURI = "/cs/calendar/getissues/" + username + "/20100624/" + CalendarEvent.TYPE_EVENT;
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);

    response = service("GET", eventURI, baseURI, h, null, writer);
  }
  
  public void testUpdateStatus() throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "root";
    h.putSingle("username", username);
    
    String eventURI = "/cs/calendar/updatestatus/taskid";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    
    assertNotNull(response);
    assertNotSame(Response.Status.NOT_FOUND, response.getStatus());
  }
  public void testGetEvent() throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "root";
    h.putSingle("username", username);
    
    String eventURI = "/cs/calendar/getevent/eventid";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    
    assertNotNull(response);
    assertNotSame(Response.Status.NOT_FOUND, response.getStatus());
  }
  public void testGetCalendars() throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "root";
    h.putSingle("username", username);
    
    String eventURI = "/cs/calendar/getcalendars/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    
    assertNotNull(response);
    assertNotSame(Response.Status.NOT_FOUND, response.getStatus());

  }
  
    
  
}
