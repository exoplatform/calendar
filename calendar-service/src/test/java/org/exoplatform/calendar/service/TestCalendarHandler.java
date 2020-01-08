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

import static org.exoplatform.calendar.service.AssertUtil.assertContainCalendarName;
import static org.exoplatform.calendar.service.AssertUtil.assertNotContainCalendarName;

import java.util.List;

import org.exoplatform.calendar.model.CompositeID;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.service.test.BaseCalendarServiceTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class TestCalendarHandler extends BaseCalendarServiceTestCase {
  protected CalendarHandler calHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.calHandler = this.xCalService.getCalendarHandler();    
  }

  public void testCreateCalendar() {
    org.exoplatform.calendar.model.Calendar cal = new MockCalendar();
    cal.setName("newCalendar");
    cal = calHandler.saveCalendar(cal);

    assertEquals(MockStorage.ID, CompositeID.parse(cal.getCompositeId()).getDS());

    cal = calHandler.getCalendarById(cal.getCompositeId());
    assertNotNull(cal);
    assertEquals("newCalendar", cal.getName());
  }
  
  public void testCreatePersonalCalendar() {
    Calendar cal = new Calendar();
    cal.setName("testCreatePersonalCalendar");
    cal.setCalType(Calendar.Type.PERSONAL.type());
    cal.setCalendarOwner(username);

    calHandler.saveCalendar(cal);

    org.exoplatform.calendar.model.Calendar calSaved = calHandler.getCalendarById(cal.getId());
    assertNotNull(calSaved);
    assertEquals("testCreatePersonalCalendar", calSaved.getName());
  }

  public void testCreateGroupCalendar() {
    Calendar cal = new Calendar();
    cal.setName("testCreateGroupCalendar");
    cal.setCalType(Calendar.Type.GROUP.type());
    cal.setGroups(userGroups);

    calHandler.saveCalendar(cal);

    org.exoplatform.calendar.model.Calendar calSaved = calHandler.getCalendarById(cal.getId());
    assertNotNull(calSaved);
    assertEquals("testCreateGroupCalendar", calSaved.getName());
  }

  public void testRemoveGroupCalendar() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    org.exoplatform.calendar.model.Calendar cal1 = TestUtil.createGroupCalendar(calHandler, "testRemoveGroupCalendar_1", userGroups);
    org.exoplatform.calendar.model.Calendar cal2 = TestUtil.createGroupCalendar(calHandler, "testRemoveGroupCalendar_2", userGroups);

    List<org.exoplatform.calendar.model.Calendar> calendars;
    int size;

    CalendarQuery query = new CalendarQuery();
    query.setIdentity(identity);
    calendars = calHandler.findCalendars(query);
    size = calendars.size();
    assertTrue("User must have at least 2 group calendars", size >= 2);
    assertContainCalendarName(calendars, "testRemoveGroupCalendar_1");
    assertContainCalendarName(calendars, "testRemoveGroupCalendar_2");

    // Remove calendar
    calHandler.removeCalendar(cal1.getId());

    calendars = calHandler.findCalendars(query);
    size = calendars.size();
    assertTrue("User must have at least 1 personal calendars", size >= 1);
    assertNotContainCalendarName(calendars, "testRemoveGroupCalendar_1");
    assertContainCalendarName(calendars, "testRemoveGroupCalendar_2");
  }

  public void testUpdatePersonalCalendar() {
    String calId = TestUtil.createPersonalCalendar(calHandler, "testUpdatePersonalCalendar", username).getId();

    org.exoplatform.calendar.model.Calendar cal = calHandler.getCalendarById(calId);
    assertNotNull(cal);
    assertEquals("testUpdatePersonalCalendar", cal.getName());

    cal.setName("testUpdatePersonalCalendar_updated");
    cal.setDescription("testUpdatePersonalCalendar description");
    calHandler.updateCalendar(cal);

    cal = calHandler.getCalendarById(calId);
    assertNotNull(cal);
    assertEquals("testUpdatePersonalCalendar_updated", cal.getName());
    assertEquals("testUpdatePersonalCalendar description", cal.getDescription());
  }

  public void testUpdateGroupCalendar() {
    String calId = TestUtil.createGroupCalendar(calHandler, "testUpdateGroupCalendar", userGroups).getId();

    org.exoplatform.calendar.model.Calendar cal = calHandler.getCalendarById(calId);
    assertNotNull(cal);
    assertEquals("testUpdateGroupCalendar", cal.getName());

    cal.setName("testUpdateGroupCalendar_updated");
    cal.setDescription("testUpdateGroupCalendar description");
    calHandler.updateCalendar(cal);

    cal = calHandler.getCalendarById(calId);
    assertNotNull(cal);
    assertEquals("testUpdateGroupCalendar_updated", cal.getName());
    assertEquals("testUpdateGroupCalendar description", cal.getDescription());
  }
}
