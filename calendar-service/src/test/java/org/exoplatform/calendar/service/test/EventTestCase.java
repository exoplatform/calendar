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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.CalendarSearchServiceConnector;
import org.exoplatform.calendar.service.impl.EventSearchConnector;
import org.exoplatform.calendar.service.impl.NewGroupListener;
import org.exoplatform.calendar.service.impl.UnifiedQuery;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;

public class EventTestCase extends BaseCalendarServiceTestCase {

  private CalendarSearchServiceConnector eventSearchConnector_;

  public void setUp() throws Exception {
    super.setUp();
    eventSearchConnector_ = getService(EventSearchConnector.class);
  }

  public void testRemoveUserEvent() throws Exception {
    CalendarEvent event = createUserEvent("TestRemoveUserEvent");
    String eventId = event.getId();
    String calendarId = event.getCalendarId();
    // before removing
    assertNotNull(calendarService_.getEventById(eventId));

    calendarService_.removeUserEvent(username, calendarId, eventId);

    // after removing
    assertNull(calendarService_.getEventById(eventId));
  }

  public void testPublicEvent() throws Exception {
    Calendar cal = createGroupCalendar(new String[] {"users"}, "CalendarName", "CalendarDescription");
        
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    CalendarEvent calEvent = createGroupEvent(cal.getId(), null, "Have a meeting", true, fromCal, toCal);

    assertNotNull(calendarService_.getGroupEvent(calEvent.getId()));
    List<String> calendarIds = new ArrayList<String>();
    calendarIds.add(cal.getId());
    assertEquals(1, calendarService_.getGroupEventByCalendar(calendarIds).size());
    assertNotNull(calendarService_.removePublicEvent(cal.getId(), calEvent.getId()));
  }

  public void testPrivateEvent() throws Exception {
    Calendar cal = createPrivateCalendar(username, "CalendarName", "CalendarDescription");
    EventCategory eventCategory = createUserEventCategory(username, "EventCategoryName2");
    CalendarEvent calEvent = createUserEvent(cal.getId(), eventCategory, "Have a meeting");

    EventQuery query = new EventQuery();
    query.setCategoryId(new String[] { eventCategory.getId() });
    assertEquals(calendarService_.getUserEvents(username, query).size(), 1);

    EventQuery eventQuery = new EventQuery();
    eventQuery.setText("Have a meeting");

    assertEquals(1, calendarService_.searchEvent(username, eventQuery, new String[] {})
                                    .getAll()
                                    .size());
    assertEquals(1, calendarService_.getEvents(username, eventQuery, new String[] {}).size());

    List<CalendarEvent> list = new ArrayList<CalendarEvent>();
    list.add(calEvent);
    Calendar movedCal = new Calendar();
    movedCal.setName("MovedCalendarName");
    movedCal.setDescription("CalendarDescription");
    movedCal.setPublic(false);
    calendarService_.saveUserCalendar(username, movedCal, true);

    calendarService_.moveEvent(cal.getId(),
                               movedCal.getId(),
                               calEvent.getCalType(),
                               calEvent.getCalType(),
                               list,
                               username);
    eventQuery = new EventQuery();
    eventQuery.setCalendarId(new String[] { movedCal.getId() });
    assertEquals(1, calendarService_.getEvents(username, eventQuery, new String[] {}).size());
  }

  public void testLastUpdatedTime() throws Exception {
    CalendarEvent event = createUserEvent("Have a meeting");    
    Date createdDate = calendarService_.getEventById(event.getId()).getLastUpdatedTime();
    assertNotNull(createdDate);
    event.setSummary("Have a new meeting");
    calendarService_.saveUserEvent(username, event.getCalendarId(), event, false);
    Date modifiedDate = calendarService_.getEventById(event.getId())
                                        .getLastUpdatedTime();
    assertNotNull(modifiedDate);
    assertTrue(modifiedDate.after(createdDate));
  }

  public void testGetPublicEvents() throws Exception {
    Calendar publicCalendar = createGroupCalendar(userGroups,
                                                  "publicCalendar",
        "publicDescription");    
    EventCategory eventCategory = createUserEventCategory(username, "GetPublicEventsCategory");
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    fromCal.add(java.util.Calendar.HOUR, 1);
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 2);
    
    CalendarEvent publicEvent = createGroupEvent(publicCalendar.getId(),
                                                 eventCategory,
                                                 "Have a meeting",
                                                 false,
                                                 fromCal,
                                                 toCal);
    
    EventQuery eventQuery = new EventQuery();
    eventQuery.setCalendarId(new String[] { publicCalendar.getId() });
    List<CalendarEvent> events = calendarService_.getPublicEvents(eventQuery);
    assertEquals(1, events.size());
    CalendarEvent resultEvent = events.get(0);
    assertEquals(publicEvent.getId(), resultEvent.getId());
    assertEquals(publicEvent.getSummary(), resultEvent.getSummary());

    login("john");
    EventQuery query = new UnifiedQuery();
    query.setText("  \" a meeting\" ");
    query.setOrderType(Utils.ORDER_TYPE_ASCENDING);
    query.setOrderBy(new String[] { Utils.ORDERBY_TITLE });
    Collection<String> params = new ArrayList<String>();
    Collection<SearchResult> rs = eventSearchConnector_.search(null,
                                                               query.getText(),
                                                               params,
                                                               0,
                                                               10,
                                                               query.getOrderBy()[0],
                                                               query.getOrderType());
    assertEquals(1, rs.size());
    checkFields(rs.iterator().next());

    login(username);
    rs = eventSearchConnector_.search(null,
                                      query.getText(),
                                      params,
                                      0,
                                      10,
                                      query.getOrderBy()[0],
                                      query.getOrderType());
    assertEquals(1, rs.size());
    checkFields(rs.iterator().next());

    // update group calendar
    publicCalendar = calendarService_.getGroupCalendar(publicCalendar.getId());
    publicCalendar.setGroups(new String[] { "/platform/guests" });

    calendarService_.savePublicCalendar(publicCalendar, false);
    rs = eventSearchConnector_.search(null,
                                      query.getText(),
                                      params,
                                      0,
                                      10,
                                      query.getOrderBy()[0],
                                      query.getOrderType());
    assertEquals(0, rs.size());
    
    login("demo");
    rs = eventSearchConnector_.search(null,
                                      query.getText(),
                                      params,
                                      0,
                                      10,
                                      query.getOrderBy()[0],
                                      query.getOrderType());
    assertEquals(1, rs.size());
    
    // update to space calendar
    organizationService_.getGroupHandler()
    .addGroupEventListener(new NewGroupListener(calendarService_,
                                                new InitParams()));
    Group parent = organizationService_.getGroupHandler().findGroupById("/spaces");
    Group g = organizationService_.getGroupHandler().createGroupInstance();
    g.setGroupName("spacetest");
    g.setLabel("Calendar Space");
    g.setDescription("simulate space creted");
    organizationService_.getGroupHandler().addChild(parent, g, true);
    g = organizationService_.getGroupHandler().findGroupById(g.getId());
    Collection<Group> gr = organizationService_.getGroupHandler().findGroupsOfUser("raul");
    assertEquals(1, gr.size());
    User u = organizationService_.getUserHandler().findUserByName("raul");
    MembershipType m = (MembershipType) organizationService_.getMembershipTypeHandler()
        .findMembershipTypes()
        .toArray()[0];
    organizationService_.getMembershipHandler().linkMembership(u, g, m, false);
    gr = organizationService_.getGroupHandler().findGroupsOfUser("raul");
    assertEquals(2, gr.size());
    
    List<GroupCalendarData> spaceCals = calendarService_.getGroupCalendars(new String[] { "/spaces/spacetest" },
                                                                           true,
        "raul");
    // success save calendar by NewGroupListener;
    assertEquals(1, spaceCals.get(0).getCalendars().size());
    Calendar spaceCal = spaceCals.get(0).getCalendars().get(0);
    // spaceCal.setId(g.getGroupName() + Utils.SPACE_ID_PREFIX);
    // calendarService_.savePublicCalendar(spaceCal, true);
    publicEvent.setCalendarId(spaceCal.getId());
    publicEvent.setSummary("space event created");
    calendarService_.savePublicEvent(spaceCal.getId(), publicEvent, true);
    query.setText("\"space event\"");
    login("raul");
    SearchContext sc = new SearchContext(loadConfiguration("conf/portal/controller.xml"),
        "classic");
    assertNotNull(sc);
    
    // Case build url for event detail in calendar
    rs = eventSearchConnector_.search(sc,
                                      query.getText(),
                                      params,
                                      0,
                                      10,
                                      query.getOrderBy()[0],
                                      query.getOrderType());
    assertEquals(1, rs.size());
    SearchResult item = rs.iterator().next();
    checkFields(item);
    assertEquals("/portal/classic/calendar/details/" + publicEvent.getId(), item.getUrl());
  }

  public void testGetEvent() throws Exception {
    Calendar calendar = createPrivateCalendar(username, "myCalendar", "Description");
    
    EventCategory eventCategory = createUserEventCategory(username, "eventCategoryName3");
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    
    // Create attachment
    String attachmentName = "Acttach file";
    String attachmentMinetype = "MimeType";
    Attachment attachment = new Attachment();
    attachment.setName(attachmentName);
    attachment.setInputStream(new InputStream() {
      @Override
      public int read() throws IOException {
        return 0;
      }
    });
    attachment.setMimeType(attachmentMinetype);
    
    // Create reminder
    String reminderType = Reminder.TYPE_BOTH;
    long reminderAlarmBefore = new Date().getTime();
    String reminderEmailAddress = "abc@gmail.com";
    Reminder reminder = new Reminder(reminderType);
    reminder.setAlarmBefore(reminderAlarmBefore);
    reminder.setEmailAddress(reminderEmailAddress);
    reminder.setRepeate(false);
    
    // Create and save event
    String eventSummay = "Have a meeting";
    CalendarEvent calendarEvent = new CalendarEvent();
    calendarEvent.setEventCategoryId(eventCategory.getId());
    calendarEvent.setEventCategoryName(eventCategory.getName());
    calendarEvent.setSummary(eventSummay);
    calendarEvent.setFromDateTime(fromCal.getTime());
    calendarEvent.setToDateTime(toCal.getTime());
    calendarEvent.setAttachment(Arrays.asList(attachment));
    calendarEvent.setReminders(Arrays.asList(reminder));
    calendarService_.saveUserEvent(username, calendar.getId(), calendarEvent, true);
    
    CalendarEvent findEvent = calendarService_.getEvent(username, calendarEvent.getId());
    assertNotNull(findEvent);
    assertEquals(eventSummay, findEvent.getSummary());
    
    // Check attachment
    List<Attachment> attachments = findEvent.getAttachment();
    assertNotNull(attachments);
    assertEquals(1, attachments.size());
    Attachment eventAttachment = attachments.get(0);
    assertEquals(attachmentName, eventAttachment.getName());
    assertEquals(attachmentMinetype, eventAttachment.getMimeType());
    
    // Check reminder
    List<Reminder> reminders = findEvent.getReminders();
    assertNotNull(reminders);
    assertEquals(1, reminders.size());
    Reminder eventReminder = reminders.get(0);
    assertEquals(reminderType, eventReminder.getReminderType());
    assertEquals(reminderAlarmBefore, eventReminder.getAlarmBefore());
    assertEquals(reminderEmailAddress, eventReminder.getEmailAddress());
    assertEquals(false, eventReminder.isRepeat());
    
    // remove reminder
    findEvent.setReminders(null);
    calendarService_.saveUserEvent(username, calendar.getId(), findEvent, false);
    
    CalendarEvent findEvent1 = calendarService_.getEvent(username, calendarEvent.getId());
    assertNotNull(findEvent1);
    List<Reminder> reminders1 = findEvent1.getReminders();
    assertEquals(0, reminders1.size());
  }

  public void testGetEventById() throws Exception {
    CalendarEvent calendarEvent = createUserEvent("Have a meeting");
    calendarService_.saveUserEvent(username, calendarEvent.getCalendarId(), calendarEvent, true);

    CalendarEvent findEvent1 = calendarService_.getEventById(calendarEvent.getId());
    assertNotNull(findEvent1);
  }

  public void testMoveEvent() throws Exception {
    Calendar calendar = createPrivateCalendar(username, "myCalendar", "Description");
    Calendar publicCalendar = createGroupCalendar(userGroups,
                                                  "publicCalendar",
        "publicDescription");
    
    EventCategory eventCategory = createUserEventCategory(username, "MoveEventCategory");
    
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    
    CalendarEvent event = createGroupEvent(publicCalendar.getId(),
                                           eventCategory,
                                           "Have a meeting",
                                           true,
                                           fromCal,
                                           toCal);
    
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    events.add(event);
    
    calendarService_.moveEvent(publicCalendar.getId(),
                               calendar.getId(),
                               String.valueOf(Calendar.TYPE_PUBLIC),
                               String.valueOf(Calendar.TYPE_PRIVATE),
                               events,
                               username);
    
    CalendarEvent userEvent = calendarService_.getEvent(username, event.getId());
    assertNotNull(userEvent);
    
    List<CalendarEvent> events1 = new ArrayList<CalendarEvent>();
    events1.add(userEvent);
    calendarService_.moveEvent(calendar.getId(),
                               publicCalendar.getId(),
                               String.valueOf(Calendar.TYPE_PRIVATE),
                               String.valueOf(Calendar.TYPE_PUBLIC),
                               events1,
                               username);
    
    CalendarEvent publicEvent = calendarService_.getGroupEvent(publicCalendar.getId(),
                                                               userEvent.getId());
    assertNotNull(publicEvent);    
  }
  
  public void testDeleteAttachment() throws Exception{
    CalendarEvent ev = createUserEvent("event with attachment");
    List<Attachment> att = new ArrayList<Attachment>();
    for (int i = 0; i < 12; i++) {
      Attachment file = new Attachment();
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("png_attachment.ics");
      file.setName("attachement-" + i + ".ics");
      file.setInputStream(input);
      file.setMimeType("text/calendar");
      att.add(file);
    }
    ev.setAttachment(att);
    //Create event with attachment
    calendarService_.saveUserEvent(username, ev.getCalendarId(), ev, false);
    
    String attId = calendarService_.getEventById(ev.getId()).getAttachment().get(0).getId();
    calendarService_.removeAttachmentById(attId);
    Attachment a = calendarService_.getAttachmentById(attId);
    assertNull(a);
    assertEquals(11, calendarService_.getEventById(ev.getId()).getAttachment().size());
    
    attId = calendarService_.getEventById(ev.getId()).getAttachment().get(0).getId();
    calendarService_.removeAttachmentById(attId);
    a = calendarService_.getAttachmentById(attId);
    assertNull(a);
    assertEquals(10, calendarService_.getEventById(ev.getId()).getAttachment().size());
  }
  
  public void testAttachment() throws Exception{
    CalendarEvent ev = createUserEvent("event with attachment");
    List<Attachment> att = new ArrayList<Attachment>();
    for (int i = 0; i < 12; i++) {
      Attachment file = new Attachment();
      InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("png_attachment.ics");
      file.setName("attachement-" + i + ".ics");
      file.setInputStream(input);
      file.setMimeType("text/calendar");
      att.add(file);
    }
    ev.setAttachment(att);
    calendarService_.saveUserEvent(username, ev.getCalendarId(), ev, false);
    String attId = calendarService_.getEventById(ev.getId()).getAttachment().get(0).getId();

    Attachment a = calendarService_.getAttachmentById(attId);
    assertNotNull(a);
  }

  private void checkFields(SearchResult item) {
    assertNotNull(item.getTitle());
    assertNotNull(item.getExcerpt());
    assertNotNull(item.getDetail());
    assertNull(item.getImageUrl());
    assertNotNull(item.getUrl());
    assertEquals(true, item.getDate() > 0);
  }

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
}
