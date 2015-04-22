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

import static org.exoplatform.calendar.ws.CalendarRestApi.CAL_BASE_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.EVENT_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.TASK_URI;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.ws.bean.CalendarResource;
import org.exoplatform.calendar.ws.bean.CategoryResource;
import org.exoplatform.calendar.ws.bean.EventResource;
import org.exoplatform.calendar.ws.bean.TaskResource;
import org.exoplatform.calendar.ws.common.Resource;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 21, 2014
 */
public class TestTaskRestApi extends AbstractTestEventRestApi {
  
  public void testGetTasks() throws Exception {
    runTestGetEvents(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }

  public void testGetTasks_Public() throws Exception {
    runTestGetEvents_Public(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }

  public void testGetTasks_Group() throws Exception {
    runTestGetEvents_Group(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }
  
  public void testGetTasks_Shared() throws Exception {
    runTestGetEventById_Shared(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }
  
  public void testGetTaskById() throws Exception {
    runTestGetEventById(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }

  public void testGetTaskById_Public() throws Exception {
    runTestGetEventById_Public(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }
  
  public void testGetTaskById_Group() throws Exception {
    runTestGetEventById_Group(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }
  
  public void testGetTaskById_Shared() throws Exception {
    runTestGetEventById_Shared(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }
  
  public void testGetTaskById_Expand() throws Exception {    
    EventCategory cat = createEventCategory("root", "testCategory");
    
    CalendarEvent uEvt = createEvent(userCalendar);
    uEvt.setEventCategoryId(cat.getId());
    uEvt.setEventType(CalendarEvent.TYPE_TASK);
    calendarService.saveUserEvent("root", userCalendar.getId(), uEvt, true);
    
    login("root");
    //
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service(HTTPMethods.GET, CAL_BASE_URI + TASK_URI + 
                                         uEvt.getId(), baseURI, headers, null, writer);
    assertEquals(HTTPStatus.OK, response.getStatus());    
    TaskResource calR0 = (TaskResource)response.getEntity();
    assertNotNull(calR0);
    assertEquals(uEvt.getId(), calR0.getId());
    String calHref = "/v1/calendar/calendars/" + uEvt.getCalendarId();
    assertEquals(calHref, calR0.getCalendar());    
    
    //expand=calendar
    response = service(HTTPMethods.GET, CAL_BASE_URI + TASK_URI + 
                                         uEvt.getId() + "?expand=calendar", baseURI, headers, null, writer);
    calR0 = (TaskResource)response.getEntity();
    assertTrue(calR0.getCalendar() instanceof CalendarResource);
    assertEquals(uEvt.getCalendarId(), ((CalendarResource)calR0.getCalendar()).getId());
    
    //expand=categories
    response = service(HTTPMethods.GET, CAL_BASE_URI + TASK_URI + 
                                         uEvt.getId() + "?expand=categories", baseURI, headers, null, writer);
    calR0 = (TaskResource)response.getEntity();
    assertTrue(calR0.getCategories() instanceof CategoryResource[]);
    assertEquals(1, calR0.getCategories().length);
  }
  
  public void testUpdateTask() throws Exception {
    runTestUpdateEvent(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }

  public void testDeleteTaskById() throws Exception {
    runTestDeleteEventById(CAL_BASE_URI + TASK_URI, CalendarEvent.TYPE_TASK);
  }
}
