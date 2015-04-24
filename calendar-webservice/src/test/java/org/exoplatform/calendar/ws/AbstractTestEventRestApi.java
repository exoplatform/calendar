/*
 * Copyright (C) 2003-2014 eXo Platform SAS. 
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
package org.exoplatform.calendar.ws;

import static org.exoplatform.calendar.ws.CalendarRestApi.CALENDAR_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.CAL_BASE_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.HEADER_LINK;

import java.util.Collection;
import java.util.List;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.ws.bean.CollectionResource;
import org.exoplatform.calendar.ws.bean.EventResource;
import org.exoplatform.calendar.ws.bean.TaskResource;
import org.exoplatform.calendar.ws.common.Resource;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

public abstract class AbstractTestEventRestApi extends TestRestApi {
  
  protected String XML_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<root><data>hello world xml</data></root>";

  protected String JSON_DATA = "{\"data\":\"hello world json\"}";
  
  public void runTestGetEvents_Shared(String uri, String eventType) throws Exception {
    CalendarEvent sEvt = createEvent(sharedCalendar);
    sEvt.setEventType(eventType);
    calendarService.saveUserEvent("root", sharedCalendar.getId(), sEvt, true);
    
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, uri, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource<?> calR = (CollectionResource<?>)response.getEntity();
    Collection<?> evs = calR.getData();
    assertEquals(1, evs.size());
    
    login("mary");
    //sharedCalendar is not shared to mary
    writer = new ByteArrayContainerResponseWriter();
    response = service(HTTPMethods.GET, uri, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CollectionResource<?>)response.getEntity();
    evs = calR.getData();
    assertEquals(0, evs.size());
  }
  
  public void runTestGetEventById(String uri, String eventType) throws Exception {
    login("root");
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, uri + "notExists", baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
       
    CalendarEvent uEvt = createEvent(userCalendar);    
    uEvt.setEventType(eventType);    
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    //
    response = service(HTTPMethods.GET, uri + uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    Resource calR0 = (Resource)response.getEntity();
    assertNotNull(calR0);
    assertEquals(uEvt.getId(), calR0.getId());
    
    //cache control    
    assertEquals("[private, no-transform, 604800, 604800]", response.getHttpHeaders().get("cache-control").toString());
    assertTrue(response.getHttpHeaders().get("last-modified").size() > 0);
    
    login("john");
    //
    response = service(HTTPMethods.GET, uri + uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    uEvt.addParticipant("john", "");
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, false);
    //john can read event in private calendar if he's participant
    response = service(HTTPMethods.GET, uri + uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    //jsonp
    response = service(HTTPMethods.GET, uri + uEvt.getId() + "?fields=id&jsonp=callback", baseURI, headers, null, writer);
    String data = (String) response.getEntity();
    StringBuilder sb = new StringBuilder("callback(").append("{\"id\":\"" + uEvt.getId() + "\"}").append(");");
    assertEquals(sb.toString(), data);
  }
  
  public void runTestGetEventById_Public(String uri, String eventType) throws Exception {
    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setEventType(eventType);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, uri + uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    userCalendar.setPublicUrl("test/url.ics");
    calendarService.saveUserCalendar("root", userCalendar, false);
    //
    response = service(HTTPMethods.GET, uri + uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    Resource calR0 = (Resource)response.getEntity();
    assertNotNull(calR0);
    assertEquals(uEvt.getId(), calR0.getId());
  }
  
  public void runTestGetEventById_Group(String uri, String eventType) throws Exception {
    CalendarEvent gEvt = createEvent(groupCalendar);
    gEvt.setEventType(eventType);
    calendarService.savePublicEvent(groupCalendar.getId(), gEvt, true);
    
    login("john", "/platform/administrators:member");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, uri + gEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
  }
  
  public void runTestGetEventById_Shared(String uri, String eventType) throws Exception {
    CalendarEvent sEvt = createEvent(sharedCalendar);
    sEvt.setEventType(eventType);
    calendarService.saveUserEvent("root", sharedCalendar.getId(), sEvt, true);
    
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, uri + sEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());

    login("mary");
    //
    response = service(HTTPMethods.GET, uri + sEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
  }
  
  public void runTestUpdateEvent(String uri, String eventType) throws Exception {
    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setEventType(eventType);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    assertNull(uEvt.getEventCategoryId());

    EventCategory cat = createEventCategory("root", "testCat");
    
    Resource resource = null;
    if (CalendarEvent.TYPE_EVENT.equals(eventType)) {
      resource = new EventResource(uEvt, "");      
      ((EventResource)resource).setCategoryId(cat.getId());
    } else {
      resource = new TaskResource(uEvt, ""); 
      ((TaskResource)resource).setCategoryId(cat.getId());
    }
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(resource);

    byte[] data = json.toString().getBytes("UTF-8");

    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + data.length);

    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.PUT, uri + uEvt.getId(), baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    
    login("root");
    //
    response = service(HTTPMethods.PUT, uri + uEvt.getId(), baseURI, headers, data, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    uEvt = calendarService.getEventById(uEvt.getId());
    assertEquals("testCat", uEvt.getEventCategoryName());
  }
  
  public void runTestDeleteEventById(String uri, String eventType) throws Exception {
    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setEventType(eventType);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);

    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.DELETE, uri + uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    
    login("root");
    //
    response = service(HTTPMethods.DELETE, uri + uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertNull(calendarService.getEventById(uEvt.getId()));
  }  

  public void runTestCreateEventForCalendar(String uri, String eventType) throws Exception {
    EventCategory cat = createEventCategory("root", "testCat");
    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setEventType(eventType);
    uEvt.setSummary("test");
    uEvt.setEventCategoryId(cat.getId());
        
    Resource resource = null;
    if (CalendarEvent.TYPE_TASK.equals(eventType)) {
      resource = new TaskResource(uEvt, "");
    } else {
      resource = new EventResource(uEvt, "");
    }
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(resource);
    
    byte[] data = json.toString().getBytes("UTF-8");

    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + data.length);
    
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + "nonExists"
                                         + uri, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());

    //john doens't has edit permission on root calendar
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       uri, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());

    login("root");    
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       uri, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    List<Object> location = response.getHttpHeaders().get(CalendarRestApi.HEADER_LOCATION);
    assertNotNull(location);
    
    String evtHref = location.get(0).toString();
    CalendarEvent evt  = calendarService.getEventById(evtHref.substring(evtHref.lastIndexOf("/") + 1));
    assertEquals("testCat", evt.getEventCategoryName());
  }
  
  public void runTestGetEventsByCalendar(String uri, String eventType) throws Exception {
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         "notExists" + uri , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());

    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setEventType(eventType);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);

    login("john");
    //john can't read private calendar
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       uri , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource calR = (CollectionResource)response.getEntity();  
    assertEquals(0, calR.getData().size());
    assertEquals(-1, calR.getSize());
    assertNull(response.getHttpHeaders().get(HEADER_LINK));

    login("root");
    String queryParams = "?returnSize=true";
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       uri + queryParams , baseURI, headers, null, writer);
    calR = (CollectionResource)response.getEntity();
    assertEquals(1, calR.getData().size());
    assertEquals(1, calR.getSize());
    assertNotNull(response.getHttpHeaders().get(HEADER_LINK));
    
    if (CalendarEvent.TYPE_EVENT.equals(eventType)) {
      uEvt.addParticipant("john", "");
    } else {
      uEvt.setTaskDelegator("john");
    }
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, false);
    
    login("john");
    //john can read private event because he's event participant or task delegator
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() +
                       uri , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CollectionResource)response.getEntity();
    assertEquals(1, calR.getData().size());
  }
}
