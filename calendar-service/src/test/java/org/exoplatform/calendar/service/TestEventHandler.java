/*
 * Copyright (C) 2015 eXo Platform SAS.
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

package org.exoplatform.calendar.service;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.service.test.BaseCalendarServiceTestCase;
import org.exoplatform.calendar.storage.jcr.JCREventQuery;
import org.exoplatform.commons.utils.ListAccess;

public class TestEventHandler extends BaseCalendarServiceTestCase {
  protected EventHandler evtHandler;

  private Calendar       userCal;

  private Calendar       grpCal;

  private CalendarEvent  userEvent;

  private CalendarEvent  grpEvent;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.evtHandler = this.xCalService.getEventHandler();

    userCal = createPrivateCalendar(username, "CalendarName", "CalendarDescription");
    EventCategory eventCategory = createUserEventCategory(username, "EventCategoryName2");
    userEvent = createUserEvent(userCal.getId(), eventCategory, "Have a meeting");

    grpCal = createGroupCalendar(userGroups, "user calendar", "user cal des");
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.setTimeInMillis(userEvent.getFromDateTime().getTime() + 60 * 60 * 1000); // plus
                                                                                  // 1
                                                                                  // hour
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.setTimeInMillis(from.getTimeInMillis() + 60 * 60 * 1000); // plus 1 hour
    grpEvent = createGroupEvent(grpCal.getId(), eventCategory, "group evt des", false, from, to);

  }

  public void testFindEventByDate() throws Exception {
    JCREventQuery condition = new JCREventQuery();
    condition.setCalType(Calendar.Type.PERSONAL.type());
    condition.setOwner(username);
    condition.setFromDate(userEvent.getFromDateTime().getTime());
    condition.setToDate(userEvent.getToDateTime().getTime() + 60 * 60 * 1000);

    ListAccess<Event> events = evtHandler.findEventsByQuery(condition);
    assertEquals(1, events.getSize());
    assertEquals(userEvent.getId(), events.load(0, 1)[0].getId());

    condition.setFromDate(condition.getFromDate() + 60 * 60 * 1000 + 100);
    events = evtHandler.findEventsByQuery(condition);
    assertEquals(0, events.getSize());

    condition.setCalType(Calendar.Type.GROUP.type());
    events = evtHandler.findEventsByQuery(condition);
    assertEquals(1, events.getSize());
  }

  public void testFindEventByCalendarID() throws Exception {
    JCREventQuery condition = new JCREventQuery();
    condition.setCalType(Calendar.Type.PERSONAL.type());
    condition.setOwner(username);
    condition.setCalendarIds(new String[] { userCal.getId() });

    ListAccess<Event> events = evtHandler.findEventsByQuery(condition);
    assertEquals(1, events.getSize());
    assertEquals(userEvent.getId(), events.load(0, 1)[0].getId());

    condition.setCalType(Calendar.Type.GROUP.type());
    condition.setCalendarIds(new String[] { grpCal.getId() });
    events = evtHandler.findEventsByQuery(condition);
    assertEquals(1, events.getSize());
    assertEquals(grpEvent.getId(), events.load(0, 1)[0].getId());
  }

  public void testFindNonRepeatEvent() throws Exception {
    CalendarEvent repeatEvent = createUserEvent(userCal.getId(), null, "this is repeated event");
    repeatEvent.setRepeatType(CalendarEvent.RP_DAILY);
    calendarService_.saveUserEvent(username, userCal.getId(), repeatEvent, false);

    JCREventQuery condition = new JCREventQuery();
    condition.setCalType(Calendar.Type.PERSONAL.type());
    condition.setOwner(username);

    ListAccess<Event> events = evtHandler.findEventsByQuery(condition);
    assertEquals(2, events.getSize());

    condition.setExcludeRepeatEvent(true);
    events = evtHandler.findEventsByQuery(condition);
    assertEquals(1, events.getSize());
  }
  
  public void testFindEventWithFilter() throws Exception {
    Calendar cal = createPrivateCalendar(username, "cal", "des");
    createUserEvent(cal.getId(), null, "Have a meeting");
    
    JCREventQuery condition = new JCREventQuery();
    condition.setCalType(Calendar.Type.PERSONAL.type());
    condition.setOwner(username);

    ListAccess<Event> events = evtHandler.findEventsByQuery(condition);
    assertEquals(2, events.getSize());
    
    condition.setFilterCalendarIds(new String[] {cal.getId()});
    events = evtHandler.findEventsByQuery(condition);
    assertEquals(1, events.getSize());
  }
}
