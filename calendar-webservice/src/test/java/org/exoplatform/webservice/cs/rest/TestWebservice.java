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
import java.util.TimeZone;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.common.http.HTTPStatus;
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

	protected CalendarWebservice calendarWebservice;
  protected CalendarService calendarService;

  protected static final String             baseURI = "";
  protected String username = "root";
  
  protected MultivaluedMap<String, String> h = new MultivaluedMapImpl();
  

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    calendarWebservice = (CalendarWebservice) container.getComponentInstanceOfType(CalendarWebservice.class);
    calendarService = (CalendarService) container.getComponentInstanceOfType(CalendarService.class);
    binder.addResource(calendarWebservice, null);
    login() ;
    h.putSingle("username", username);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    
  }

  protected Calendar createCalendar(String name) {
    Calendar cal = new Calendar() ;
    cal.setName(name) ;
    cal.setDescription("Desscription") ;
    cal.setPublic(true) ;
    cal.setCalType(Calendar.TYPE_PRIVATE);
    return cal;
  }
  protected Calendar createPublicCalendar(String name, String description) {
      Calendar publicCalendar = new Calendar();
      publicCalendar.setName(name);
      publicCalendar.setDescription(description);
      publicCalendar.setPublic(true);
      publicCalendar.setGroups(new String[]{"/platform/users","/organization/management/executive-board"});
      publicCalendar.setCalType(Calendar.TYPE_PUBLIC);
      return publicCalendar;
  }
  
  private CalendarEvent createEvent(String summary, String calendarId, Date from, Date to, String calType) {
    CalendarEvent c = new CalendarEvent() ;
    c.setSummary(summary) ;
    c.setCalendarId(calendarId);
    c.setEventType(calType) ;
    c.setFromDateTime(from) ;
    c.setToDateTime(to);
    return c ;
  }

  public void testCheckPublicRss() throws Exception {
   

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar");
     

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
    try {
    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    response = service("GET", cal.getPublicUrl(), baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    } catch (WebApplicationException wae){
        //TODO check null pointer exception
    }
   
    //deleteData(username, calCate.getId());
    
  }

  public void testUpcomingEvent() throws Exception {
    
  //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar");
     

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    CalendarEvent calE = createEvent("newEvent", cal.getId(), new Date(), new Date(), CalendarEvent.TYPE_EVENT);
    calendarService.saveUserEvent(username, cal.getId(), calE, true);
    
    
    //ArrayList<String> calIds = new ArrayList<String>() ;
    //calIds.add(cal.getId());
    //assertEquals(calendarService.getUserEventByCalendar(username, calIds).size(), 1);
    // data correct
    String eventURI = "/cs/calendar/getissues/"+ CalendarEvent.TYPE_EVENT + "/10";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK ,response.getStatus());
    
    //deleteData(username, calCate.getId());
  }

  public void testUpdateStatus() throws Exception {

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar");
     

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    CalendarEvent calE = createEvent("newTask", cal.getId(), new Date(), new Date(), CalendarEvent.TYPE_TASK);
    calendarService.saveUserEvent(username, cal.getId(), calE, true);
    
     
    
    String eventURI = "/cs/calendar/updatestatus/"+calE.getId()+"/" + calE.getEventState();
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    //deleteData(username, calCate.getId());
  }
  
  //mvn test -Dtest=TestWebservice#testGetEvent
  public void testGetEvent() throws Exception {

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar");
     

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    CalendarEvent calE = createEvent("newEvent", cal.getId(), new Date(), new Date(), CalendarEvent.TYPE_EVENT);
    calendarService.saveUserEvent(username, cal.getId(), calE, true);
    
    String eventURI = "/cs/calendar/getevent/" + calE.getId();
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    //deleteData(username, calCate.getId());
    
  }
  
  public void testGetEventById() throws Exception {

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar");
     
    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    CalendarEvent calE = createEvent("newEvent", cal.getId(), new Date(), new Date(), CalendarEvent.TYPE_EVENT);
    calendarService.saveUserEvent(username, cal.getId(), calE, true);
    
    String eventURI = "/cs/calendar/geteventbyid/" + calE.getId();
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    //deleteData(username, calCate.getId());
  }
  
  public void testGetOccurrence() throws Exception {
    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar");

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);

    calendarService.saveUserCalendar(username, cal, true);

    java.util.Calendar fromTime = java.util.Calendar.getInstance();
    java.util.Calendar toTime = java.util.Calendar.getInstance();
    toTime.add(java.util.Calendar.HOUR, 1);
    
    CalendarEvent event = createEvent("newEvent", cal.getId(), fromTime.getTime(), toTime.getTime(), CalendarEvent.TYPE_EVENT);
    event.setRepeatType(CalendarEvent.RP_DAILY);
    calendarService.saveUserEvent(username, cal.getId(), event, true);

    SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sdf.setTimeZone(TimeZone.getDefault());
    String recurId = sdf.format(fromTime.getTime());

    String eventURI = "/cs/calendar/getoccurrence/" + event.getId() + "/" + recurId;

    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);

    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
  }
  
  public void testGetCalendars() throws Exception {

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar");
     
    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    String eventURI = "/cs/calendar/getcalendars/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);

    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    //deleteData(username, calCate.getId());
  }
  
  private void login() {    
    login(username, "member:/platform/users");
  }
}
