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

import static org.exoplatform.calendar.ws.CalendarRestApi.ATTACHMENT_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.CALENDAR_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.CAL_BASE_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.EVENT_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.HEADER_LINK;
import static org.exoplatform.calendar.ws.CalendarRestApi.OCCURRENCE_URI;

import javax.ws.rs.core.MultivaluedMap;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.ws.bean.AttachmentResource;
import org.exoplatform.calendar.ws.bean.CalendarResource;
import org.exoplatform.calendar.ws.bean.CategoryResource;
import org.exoplatform.calendar.ws.bean.CollectionResource;
import org.exoplatform.calendar.ws.bean.EventResource;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

import com.googlecode.mp4parser.boxes.ultraviolet.AssetInformationBox;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 21, 2014
 */
public class TestEventRestApi extends AbstractTestEventRestApi {
   
  public void testGetEvents() throws Exception {
    runTestGetEvents(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
  
  public void testGetEvents_Public() throws Exception {
    runTestGetEvents_Public(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
  
  public void testGetEvents_Group() throws Exception {
    runTestGetEvents_Group(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
  
  public void testGetEvents_Shared() throws Exception {
    runTestGetEventById_Shared(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
  
  public void testGetEventById() throws Exception {
    runTestGetEventById(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }

  public void testGetEventById_Public() throws Exception {
    runTestGetEventById_Public(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
  
  public void testGetEventById_Group() throws Exception {
    runTestGetEventById_Group(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
  
  public void testGetEventById_Shared() throws Exception {
    runTestGetEventById_Shared(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
    
  public void testGetEventById_Expand() throws Exception {    
    EventCategory cat = createEventCategory("root", "testCategory");
    
    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setEventCategoryId(cat.getId());
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    
    login("root");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + 
                                         uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());    
    EventResource calR0 = (EventResource)response.getEntity();
    assertNotNull(calR0);
    assertEquals(uEvt.getId(), calR0.getId());
    String calHref = "/v1/calendar/calendars/" + uEvt.getCalendarId();
    assertEquals(calHref, calR0.getCalendar());
    
    //expand=calendar
    response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + 
                                         uEvt.getId() + "?expand=calendar", baseURI, headers, null, writer);
    calR0 = (EventResource)response.getEntity();
    assertTrue(calR0.getCalendar() instanceof CalendarResource);
    assertEquals(uEvt.getCalendarId(), ((CalendarResource)calR0.getCalendar()).getId());
    
    //expand=categories
    response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + 
                                         uEvt.getId() + "?expand=categories", baseURI, headers, null, writer);
    calR0 = (EventResource)response.getEntity();
    assertTrue(calR0.getCategories() instanceof CategoryResource[]);
    assertEquals(1, calR0.getCategories().length);
  }
  
  public void testUpdateEvent() throws Exception {
    runTestUpdateEvent(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }

  public void testDeleteEventById() throws Exception {
    runTestDeleteEventById(CAL_BASE_URI + EVENT_URI, CalendarEvent.TYPE_EVENT);
  }
  
  public void testGetAttachmentByEvent() throws Exception {
    CalendarEvent uEvt = createEvent(userCalendar);    
    List<Attachment> att = new ArrayList<Attachment>();
    for (int i = 0; i < 12; i++) {
      Attachment file = new Attachment();
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("image.png");
      file.setName("image-" + i + ".png");
      file.setInputStream(input);
      file.setMimeType("image/png");
      att.add(file);
    }
    uEvt.setAttachment(att);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    
    login("root");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + "notExists" + ATTACHMENT_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());

    //expand=attachments
    response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + uEvt.getId() + "?expand=attachments", baseURI, headers, null, writer);
    EventResource eventRs = (EventResource)response.getEntity();
    assertTrue(eventRs.getAttachments() instanceof AttachmentResource[]);
    assertEquals(12, eventRs.getAttachments().length);
    
    //expand=attachments(offset:1,limit:1)
    response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + uEvt.getId() + 
                       "?expand=attachments(offset:1,limit:1)", baseURI, headers, null, writer);
    eventRs = (EventResource)response.getEntity();
    assertEquals(1, eventRs.getAttachments().length);
    AttachmentResource attRs = (AttachmentResource)eventRs.getAttachments()[0];
    assertEquals("image-1.png", attRs.getName());
    

    response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + uEvt.getId() + ATTACHMENT_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());    
    CollectionResource<?> calR = (CollectionResource<?>)response.getEntity();
    List<?> evs = (ArrayList<?>)calR.getData();
    assertEquals(10, evs.size());
    assertEquals(12, calR.getSize());
    assertNotNull(response.getHttpHeaders().get(HEADER_LINK));
    
    login("john");
    response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + uEvt.getId() + ATTACHMENT_URI, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
  }
  
  public void testCreateAttachmentForEvent() throws Exception {
    CalendarEvent uEvt = createEvent(userCalendar);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter w = new PrintWriter(out);
    w.write("--abcdef\r\n" + "Content-Disposition: form-data; name=\"xml-file\"; filename=\"foo.xml\"\r\n"
        + "Content-Type: text/xml\r\n" + "\r\n" + XML_DATA + "\r\n" + "--abcdef\r\n"
        + "Content-Disposition: form-data; name=\"json-file\"; filename=\"foo.json\"\r\n"
        + "Content-Type: application/json\r\n" + "\r\n" + JSON_DATA + "\r\n" + "--abcdef\r\n"
        + "Content-Disposition: form-data; name=\"field\"\r\n" + "\r\n" + "to be or not to be" + "\r\n"
        + "--abcdef--\r\n");
    w.flush();
    headers.putSingle("content-type", "multipart/form-data; boundary=abcdef");

    byte[] data = out.toByteArray();

    login("john");
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.POST, CAL_BASE_URI + EVENT_URI + uEvt.getId() + 
                                         ATTACHMENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    
    login("root");
    response = service(HTTPMethods.POST, CAL_BASE_URI + EVENT_URI + uEvt.getId() + 
                       ATTACHMENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    String location = "[/v1/calendar/" + uEvt.getId() + "/attachments/]";
    assertEquals(location, response.getHttpHeaders().get("Location").toString());
  }

  @SuppressWarnings("rawtypes")
  public void testGetEventsByCalendar() throws Exception {
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         "notExists" + EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());

    CalendarEvent uEvt = createEvent(userCalendar);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);

    login("john");
    //john can't read private calendar
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource calR = (CollectionResource)response.getEntity();  
    assertEquals(0, calR.getData().size());
    assertEquals(-1, calR.getSize());
    assertNull(response.getHttpHeaders().get(HEADER_LINK));

    login("root");
    String queryParams = "?returnSize=true";
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       EVENT_URI + queryParams , baseURI, headers, null, writer);
    calR = (CollectionResource)response.getEntity();
    assertEquals(1, calR.getData().size());
    assertEquals(1, calR.getSize());
    assertNotNull(response.getHttpHeaders().get(HEADER_LINK));
    
    uEvt.addParticipant("john", "");
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, false);
    
    login("john");
    //john can read private event because he's participant
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() +
                       EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CollectionResource)response.getEntity();
    assertEquals(1, calR.getData().size());
  }

  public void testGetEventsByCalendar_Public() throws Exception {
    CalendarEvent uEvt = createEvent(userCalendar);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         userCalendar.getId() + EVENT_URI , baseURI, headers, null, writer);
    //john can't read private calendar
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource<?> calR = (CollectionResource<?>)response.getEntity();
    assertEquals(0, calR.getData().size());
    
    userCalendar.setPublicUrl("test/uri.ics");
    calendarService.saveUserCalendar("root", userCalendar, false);
    //john can read public calendar
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CollectionResource<?>)response.getEntity();
    assertEquals(1, calR.getData().size());
  }

  public void testGetEventsByCalendar_Group() throws Exception {
    Calendar adminCal = createGroupCalendar("adminCal", "/platform/administrators");
    CalendarEvent gEvt = createEvent(adminCal);
    calendarService.savePublicEvent(adminCal.getId(), gEvt, true);
    
    login("john", "/platform/administrators:member");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         adminCal.getId() + EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource<?> calR = (CollectionResource<?>)response.getEntity();
    assertEquals(1, calR.getData().size());
    
    login("mary");
    //mary is not in admin group
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                       adminCal.getId() + EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CollectionResource<?>)response.getEntity();
    assertEquals(0, calR.getData().size());
  }
  
  public void testGetEventsByCalendar_Shared() throws Exception {
    CalendarEvent sEvt = createEvent(sharedCalendar);
    calendarService.saveUserEvent("root", sharedCalendar.getId(), sEvt, true);

    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         sharedCalendar.getId() + EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource<?> calR = (CollectionResource<?>)response.getEntity();
    assertEquals(1, calR.getData().size());
    
    login("mary");
    //sharedCalendar is not shared to mary
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         sharedCalendar.getId() + EVENT_URI , baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    calR = (CollectionResource<?>)response.getEntity();
    assertEquals(0, calR.getData().size());
  }
  
  @SuppressWarnings("unchecked")
  public void testGetEventsByCalendar_Fields_JSONP() throws Exception {
    List<CalendarEvent> evt = new LinkedList<CalendarEvent>();
    for (int i = 0; i < 10; i++) {
      CalendarEvent uEvt = createEvent(userCalendar);
      uEvt.setSummary(String.valueOf(i));
      calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
      evt.add(uEvt);
    }

    login("root");
    String queryParams = "?returnSize=true&fields=id&offset=5&limit=1";
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         userCalendar.getId() + EVENT_URI + queryParams, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource<Map<String, ?>> calR = (CollectionResource<Map<String, ?>>)response.getEntity();
    assertEquals(1, calR.getData().size());
    assertEquals(10, calR.getSize());
    Collection<Map<String, ?>> data = calR.getData();
    Map<String, ?> event = data.iterator().next();
    //event at offset 5, only return field id
    assertNotNull(event.get("id"));
    assertNull(event.get("calendarId"));
    
    queryParams += "&jsonp=callback";
    //
    response = service(HTTPMethods.GET, CAL_BASE_URI + CALENDAR_URI + 
                                         userCalendar.getId() + EVENT_URI + queryParams, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    String entity = (String)response.getEntity();
    assertTrue(entity.matches("callback\\(\\{.+\"id\":\"" + event.get("id") + "\".+\\}\\);"));
  }

  public void testCreateEventForCalendar() throws Exception {
    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setSummary("test");
    
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(new EventResource(uEvt, ""));
    byte[] data = json.toString().getBytes("UTF-8");

    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + data.length);
    
    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + "nonExists"
                                         + EVENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());

    //john doens't has edit permission on root calendar
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       EVENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());

    login("root");
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + userCalendar.getId() + 
                       EVENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.CREATED, response.getStatus()); 
    assertNotNull(response.getHttpHeaders().get(CalendarRestApi.HEADER_LOCATION).toString());
  }
  
  public void testCreateEventForCalendar_Group() throws Exception {
    CalendarEvent gEvt = createEvent(groupCalendar);
    
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(new EventResource(gEvt, ""));
    byte[] data = json.toString().getBytes("UTF-8");

    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + data.length);
    
    login("john");
    //john doesn't has permission on group
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId()
                                         + EVENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.UNAUTHORIZED, response.getStatus());
    
    login("root");
    response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + groupCalendar.getId() + 
                       EVENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.CREATED, response.getStatus()); 
    assertNotNull(response.getHttpHeaders().get(CalendarRestApi.HEADER_LOCATION));
  }
  
  public void testCreateEventForCalendar_Shared() throws Exception {
    CalendarEvent sEvt = createEvent(sharedCalendar);
    
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = generatorImpl.createJsonObject(new EventResource(sEvt, ""));
    byte[] data = json.toString().getBytes("UTF-8");

    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + data.length);
    
    login("john");
    //john has permission on shared calendar
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.POST, CAL_BASE_URI + CALENDAR_URI + sharedCalendar.getId()
                                         + EVENT_URI, baseURI, headers, data, writer);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    assertNotNull(response.getHttpHeaders().get(CalendarRestApi.HEADER_LOCATION).toString());
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void testGetOccurrencesFromEvent() throws Exception {

    TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
    String timeZone = tz.getID();
    
    CalendarEvent ev = createRepetitiveEventForTest(userCalendar);
    calendarService.saveUserEvent("root", ev.getCalendarId(), ev, false);
    
    SimpleDateFormat sf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sf.setTimeZone(tz);

    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.setTime(ev.getFromDateTime());

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.setTime(ev.getToDateTime());

    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);

    from.set(2013, 2, 1, 0, 0, 0);
    to.set(2013, 2, 12, 0, 0, 0);

    String queryParams ="?start="+ISO8601.format(from)+"&end="+ISO8601.format(to)+"&offset=0&limit=4";

    login("john");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + ev.getId() + 
                                         OCCURRENCE_URI + queryParams, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    login("root");
    //
    response = service(HTTPMethods.GET, CAL_BASE_URI + EVENT_URI + ev.getId() + 
                       OCCURRENCE_URI + queryParams, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
    CollectionResource calR = (CollectionResource)response.getEntity();
    List<EventResource> evs = (ArrayList<EventResource>)calR.getData();
    assertEquals(4, evs.size());
    Map<String,CalendarEvent> occMap = calendarService.getOccurrenceEvents(ev, from, to, timeZone);
    assertEquals(occMap.values().size(), calR.getSize());
    assertNotNull(response.getHttpHeaders().get(HEADER_LINK));
  }
}
