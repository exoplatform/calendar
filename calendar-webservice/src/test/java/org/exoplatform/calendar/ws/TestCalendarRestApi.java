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
import static org.exoplatform.calendar.ws.CalendarRestApi.ICS_URI;

import java.util.Map;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.ws.bean.CalendarResource;
import org.exoplatform.calendar.ws.bean.CollectionResource;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

public class TestCalendarRestApi extends TestRestApi {

  @SuppressWarnings("unchecked")
  public void testGetSubResources() throws Exception {
    login("root");
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, "http://localhost:3333" + CAL_BASE_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());    
    Map<String, String[]> subResources = (Map<String, String[]>)response.getEntity();
    String[] resources = subResources.get("subResourcesHref");
    assertEquals(33, resources.length);
    System.out.println(resources[0]);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void testGetCalendars() throws Exception {
    login("john");
            
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    String queryParams ="?offset=0&limit=20";
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + queryParams, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource<CalendarResource> calR = (CollectionResource<CalendarResource>)response.getEntity();
    assertEquals(2, calR.getData().size());
    assertEquals(2, calR.getSize());
    
    //url should be absolute, we'll improve this in unit test later
    CalendarResource cal = calR.getData().iterator().next();
    String ics = "/v1/calendar/calendars/" + cal.getId() + "/ics";
    assertEquals(ics, cal.getIcsURL());
    String href =   "/v1/calendar/calendars/" + cal.getId();
    assertEquals(href, cal.getHref());
    
    login("root");
    //
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + queryParams, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CollectionResource<CalendarResource>)response.getEntity();
    assertEquals(3, calR.getData().size());
    assertEquals(3, calR.getSize());

    for(int i = 0; i < 10; i ++) {
      createPersonalCalendar("root" + " myCalendar2" + i, "root");
    }

    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + queryParams, baseURI, headers, null, writer);
    calR = (CollectionResource)response.getEntity();
    assertEquals(10, calR.getData().size());
    assertEquals(13, calR.getSize());
    String header = "[</v1/calendar/calendars/?offset=10&limit=10>;rel=\"next\"," + 
                              "</v1/calendar/calendars/?offset=0&limit=10>;rel=\"first\",</v1/calendar/calendars/?offset=10&limit=3>;rel=\"last\"]";    
    assertEquals(header, response.getHttpHeaders().get(HEADER_LINK).toString());
  }

  public void testCreateCalendar() throws Exception {
    login("root");
    
    //
    Calendar cal = new Calendar() ;
    cal.setName("myCal") ;
    cal.setCalendarOwner("root");
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(new CalendarResource(cal, CAL_BASE_URI + "/"));
    byte[] data = json.toString().getBytes("UTF-8");
    
    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + data.length);

    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    String location = "[/v1/calendar/calendars/" + cal.getId() + "]";
    assertEquals(location, response.getHttpHeaders().get(CalendarRestApi.HEADER_LOCATION).toString());

    //demo is not owner of root calendar    
    login("demo");
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    
    //demo can create group calendar
    cal = new Calendar() ;
    cal.setName("myCal") ;
    cal.setGroups(new String[] {"/platform/users"});
    json = generatorImpl.createJsonObject(new CalendarResource(cal, CAL_BASE_URI + "/"));
    data = json.toString().getBytes("UTF-8");
    //
    login("demo", "/platform/users:member");
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    
    //demo can't create cal for group that he's not in
    cal.setGroups(new String[] {"/platform/admin"});
    json = generatorImpl.createJsonObject(new CalendarResource(cal, CAL_BASE_URI + "/"));
    data = json.toString().getBytes("UTF-8");
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
  }
  
  public void testGetCalendarById() throws Exception {
    login("mary");
    //mary can't read root calendar
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    login("root");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId(), baseURI, headers, null, writer);    
    assertEquals(HTTPStatus.OK, response.getStatus());
    CalendarResource calR = (CalendarResource)response.getEntity();
    assertEquals(userCalendar.getId(), calR.getId());
    
    login("mary", "/platform/users:member");
    //mary can read group calendar
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId(), baseURI, headers, null, writer);    
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CalendarResource)response.getEntity();
    assertEquals(groupCalendar.getId(), calR.getId());
    
    //mary can't read group calendar that she not belongs to
    Calendar adminCal = createGroupCalendar("newGroups", "/platform/administrators");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + adminCal.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    login("john");
    //john can read shared calendar
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId(), baseURI, headers, null, writer);    
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CalendarResource)response.getEntity();
    assertEquals(sharedCalendar.getId(), calR.getId());
    
    //not found
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + "notExists", baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
  }

  public void testUpdateCalendarById() throws Exception {
    Calendar cal = new Calendar() ;
    cal.setName("myCal") ;
    cal.setCalendarOwner("root");
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(new CalendarResource(cal, CAL_BASE_URI + "/"));
    byte[] data = json.toString().getBytes("UTF-8");

    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + data.length);
    
    login("john");
    //john can't update root calendar
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.PUT, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId(), baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    //john can't update shared calendar
    response = service(HTTPMethods.PUT, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId(), baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    
    login("root");
    response = service(HTTPMethods.PUT, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId(), baseURI, headers, data, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    
    //john doesn't has edit permission
    login("john");
    response = service(HTTPMethods.PUT, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId(), baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    
    login("root");
    response = service(HTTPMethods.PUT, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId(), baseURI, headers, data, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
  }

  public void testDeleteCalendarById() throws Exception {
    login("john");
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.DELETE, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());

    response = service(HTTPMethods.DELETE, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());

    login("root");
    response = service(HTTPMethods.DELETE, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertNull(calendarService.getCalendarById(userCalendar.getId()));

    response = service(HTTPMethods.DELETE, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertNull(calendarService.getCalendarById(groupCalendar.getId()));
  }
  
  public void testDeleteSharedCalendarById() throws Exception {
    assertEquals(1, calendarService.getSharedCalendars("john", true).getCalendars().size());

    login("john");
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.DELETE, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    //calendar is not shared to john anymore, but it's not deleted
    assertNull(calendarService.getSharedCalendars("john", true));
    assertNotNull(calendarService.getCalendarById(sharedCalendar.getId()));
  }
  
  public void testExportCalendarToIcs() throws Exception {
    CalendarEvent uEvt = createEvent(sharedCalendar);
    calendarService.saveUserEvent("root", sharedCalendar.getId(), uEvt, true);

    login("john");
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId() + ICS_URI, baseURI, headers, null, writer);
    //john can read shared calendar ics 
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertEquals(CalendarRestApi.TEXT_ICS_TYPE, response.getContentType());
    
    login("root");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId() + ICS_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertEquals(CalendarRestApi.TEXT_ICS_TYPE, response.getContentType());
    
    //sharedCalendar is not shared to mary
    login("mary");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId() + ICS_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    sharedCalendar.setPublicUrl("/test/url.ics");
    calendarService.saveUserCalendar("root", sharedCalendar, false);
    //may can read public calendar ics
    login("mary");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId() + ICS_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertEquals(CalendarRestApi.TEXT_ICS_TYPE, response.getContentType());
    
    CalendarEvent gEvt = createEvent(groupCalendar);
    calendarService.savePublicEvent(groupCalendar.getId(), gEvt, true);
    //
    login("john", "/platform/administrators:member");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId() + ICS_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertEquals(CalendarRestApi.TEXT_ICS_TYPE, response.getContentType());
    
    login("test");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId() + ICS_URI, baseURI, headers, null, writer);
    //test is not in group of groupCalendar
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
  }
}