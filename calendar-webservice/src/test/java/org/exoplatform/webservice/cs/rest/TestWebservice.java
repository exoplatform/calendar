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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.RuntimeDelegate;

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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;

/**
 * Created by The eXo Platform SARL Author : Volodymyr Krasnikov
 * volodymyr.krasnikov@exoplatform.com.ua
 */

public class TestWebservice extends AbstractResourceTest {

  CalendarWebservice calendarWebservice;
  CalendarService calendarService;
  private Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();

  static final String             baseURI = "";
  String username = "root";
  
  MultivaluedMap<String, String> h = new MultivaluedMapImpl();
  

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    calendarWebservice = (CalendarWebservice) container.getComponentInstanceOfType(CalendarWebservice.class);
    calendarService = (MockCalendarService) container.getComponentInstanceOfType(MockCalendarService.class);
    binder.addResource(calendarWebservice, null);
    login() ;
    h.putSingle("username", username);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    
  }


  private CalendarCategory createCalendarCategory(String name) {

    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName(name) ;
    calCategory.setDescription("Description") ;
    //assertNotNull(calendarService);
    return calCategory ;
  }

  private Calendar createCalendar(String name, String categoryId) {
    Calendar cal = new Calendar() ;
    cal.setName(name) ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(categoryId) ;
    cal.setPublic(true) ;
    return cal;
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
  
  private void deleteData(String username, String calId) throws Exception {
    calendarService.removeCalendarCategory(username, calId); 
  }
  
  public void testCheckPublicRss() throws Exception {
   

    //Create calendar
    CalendarCategory calCate = createCalendarCategory("categoryName");
    calendarService.saveCalendarCategory(username, calCate, true) ;

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar",calCate.getId());
     

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
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

    response = service("GET", cal.getPublicUrl(), baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());

   
    deleteData(username, calCate.getId());
    
  }

  public void testUpcomingEvent() throws Exception {
    
    //Create calendar
    CalendarCategory calCate = createCalendarCategory("categoryName");
    calendarService.saveCalendarCategory(username, calCate, true) ;

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar",calCate.getId());
     

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    CalendarEvent calE = createEvent("newEvent", cal.getId(), new Date(), new Date(), CalendarEvent.TYPE_EVENT);
    calendarService.saveUserEvent(extURI, cal.getId(), calE, true);
    
    
    //ArrayList<String> calIds = new ArrayList<String>() ;
    //calIds.add(cal.getId());
    //assertEquals(calendarService.getUserEventByCalendar(username, calIds).size(), 1);
    // data correct
    String eventURI = "/cs/calendar/getissues/"+ CalendarEvent.TYPE_EVENT + "/10";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK ,response.getStatus());
    
    deleteData(username, calCate.getId());
  }

  public void testUpdateStatus() throws Exception {

    //Create calendar
    CalendarCategory calCate = createCalendarCategory("categoryName");
    calendarService.saveCalendarCategory(username, calCate, true) ;

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar",calCate.getId());
     

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    CalendarEvent calE = createEvent("newTask", cal.getId(), new Date(), new Date(), CalendarEvent.TYPE_TASK);
    calendarService.saveUserEvent(extURI, cal.getId(), calE, true);
    
     
    
    String eventURI = "/cs/calendar/updatestatus/"+calE.getId()+"/" + calE.getEventState();
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    deleteData(username, calCate.getId());
  }
  
  public void testGetEvent() throws Exception {

    //Create calendar
    CalendarCategory calCate = createCalendarCategory("categoryName");
    calendarService.saveCalendarCategory(username, calCate, true) ;

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar",calCate.getId());
     

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    CalendarEvent calE = createEvent("newEvent", cal.getId(), new Date(), new Date(), CalendarEvent.TYPE_EVENT);
    calendarService.saveUserEvent(extURI, cal.getId(), calE, true);
    
    String eventURI = "/cs/calendar/getevent/" + calE.getId();
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);
    
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    deleteData(username, calCate.getId());
    
  }
  public void testGetCalendars() throws Exception {
    
    //Create calendar
    CalendarCategory calCate = createCalendarCategory("categoryName");
    calendarService.saveCalendarCategory(username, calCate, true) ;

    //create/get calendar in private folder
    Calendar cal = createCalendar("myCalendar",calCate.getId());
     

    String extURI = "/cs/calendar/subscribe/" + username + "/" + cal.getId() + "/0";

    cal.setPublicUrl(extURI);
    
    calendarService.saveUserCalendar(username, cal, true);
    
    String eventURI = "/cs/calendar/getcalendars/";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", eventURI, baseURI, h, null, writer);

    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    deleteData(username, calCate.getId());

  }
  
  private void login() {
    
    setMembershipEntry("/platform/users", "member", true);
    Identity identity = new Identity(username, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  private void setMembershipEntry(String group, String membershipType, boolean isNew) {
    MembershipEntry membershipEntry = new MembershipEntry(group, membershipType);
    if (isNew) {
      membershipEntries.clear();
    }
    membershipEntries.add(membershipEntry);
  }


}
