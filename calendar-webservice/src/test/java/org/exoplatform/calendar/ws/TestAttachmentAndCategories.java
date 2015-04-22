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
import static org.exoplatform.calendar.ws.CalendarRestApi.CAL_BASE_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.CATEGORY_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.HEADER_LINK;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.ws.bean.CategoryResource;
import org.exoplatform.calendar.ws.bean.CollectionResource;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 21, 2014
 * 
 *  Rest end point: "/attachments/{id}"
 *  
 *  Verb         Roles      
 *  ---------------------------------------------------------------------------------
 *  GET          Returns the attachment if:
 *                    - the calendar of the event is public
 *                    - the authenticated user is the owner of the calendar of the event
 *                    - the authenticated user belongs to the group of the calendar of the event
 *                    - the authenticated user is a participant of the event
 *                    - the calendar of the event has been shared with the authenticated user or with a group of the authenticated user
 *                    
 *  DELETE    Deletes the attachment if:
 *                    - the authenticated user is the owner of the calendar of the event
 *                    - for group calendars, the authenticated user has edit rights on the calendar
 *                    - the calendar of the event has been shared with the authenticated user, with modification rights
 *                    - the calendar of the event has been shared with a group of the authenticated user, with modification rights
 *  
 *  
 *  Rest end point: "/categories"
 *  
 *  Verb         Roles      
 *  ---------------------------------------------------------------------------------
 *  GET          Returns the categories if an user is authenticated (the common categories + the personal categories)
 */
public class TestAttachmentAndCategories extends TestRestApi {

  public void testGetAttachmentById() throws Exception {
    CalendarEvent ev = createEvent(groupCalendar);
    List<Attachment> att = new ArrayList<Attachment>();

    for (int i = 0; i < 2; i++) {
      Attachment file = new Attachment();
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("image.png");
      file.setName("image-" + i + ".png");
      file.setInputStream(input);
      file.setMimeType("image/png");
      att.add(file);
    }
    ev.setAttachment(att);

    String atId = ev.getAttachment().get(0).getId();
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    atId = URLEncoder.encode(atId.toString(), "ISO-8859-1");
    
    login("root", "/platform/administrators:member");
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + ATTACHMENT_URI + atId, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    calendarService.savePublicEvent(groupCalendar.getId(), ev, true);
    atId = calendarService.getEventById(ev.getId()).getAttachment().get(0).getId();
    atId = URLEncoder.encode(atId.toString(), "ISO-8859-1");
    response = service(HTTPMethods.GET, CAL_BASE_URI + ATTACHMENT_URI + atId, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());

    login("john", "/platform/users:member");
    response = service(HTTPMethods.GET, CAL_BASE_URI + ATTACHMENT_URI + atId, baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());
  }

  public void testDeleteAttachmentById() throws Exception {
    login("root");
    CalendarEvent ev = createEvent(userCalendar);
    List<Attachment> att = new ArrayList<Attachment>();

    for (int i = 0; i < 2; i++) {
      Attachment file = new Attachment();
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("image.png");
      file.setName("image-" + i + ".png");
      file.setInputStream(input);
      file.setMimeType("image/png");
      att.add(file);
    }
    ev.setAttachment(att);
    calendarService.saveUserEvent("root", userCalendar.getId(), ev, true);

    String atId = calendarService.getEventById(ev.getId()).getAttachment().get(0).getId();
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    atId = URLEncoder.encode(atId.toString(), "ISO-8859-1");
    ContainerResponse response = service(HTTPMethods.DELETE, CAL_BASE_URI + ATTACHMENT_URI + atId, baseURI, headers, null, writer);
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertEquals(1, calendarService.getEventById(ev.getId()).getAttachment().size());
  }

  @SuppressWarnings("rawtypes")
  public void testGetCategories() throws Exception {
    login("root");
    EventCategory category = this.createEventCategory("root", "event-category");

    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CATEGORY_URI, baseURI, headers, null, writer);
    
    CollectionResource resource = (CollectionResource)response.getEntity();
    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertEquals(1, resource.getData().size());
    assertNotNull(response.getHttpHeaders().get(HEADER_LINK));

    //jsonp
    response = service(HTTPMethods.GET, CAL_BASE_URI + CATEGORY_URI +"?jsonp=callback", baseURI, headers, null, writer);
    String data = (String) response.getEntity();
    StringBuilder sb = new StringBuilder("callback(");
    sb.append(new JsonGeneratorImpl().createJsonObject(resource)).append(");");
    assertEquals(sb.toString(), data);
  }

  public void testGetCategoryById() throws Exception {
    login("root");
    this.createEventCategory("root", "event-category");
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + CATEGORY_URI + "notfound", baseURI, headers, null, writer);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    
    EventCategory category = createEventCategory("root", "myCategory");
    response = service(HTTPMethods.GET, CAL_BASE_URI + CATEGORY_URI + category.getId(), baseURI, headers, null, writer);
    CategoryResource resource = (CategoryResource)response.getEntity();

    assertNotNull(response);
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertNotNull(resource);
    assertEquals(category.getName(), resource.getName());
  }
}
