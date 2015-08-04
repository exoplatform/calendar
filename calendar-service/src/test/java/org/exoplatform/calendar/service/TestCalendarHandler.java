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

import org.exoplatform.calendar.service.test.BaseCalendarServiceTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import java.util.Arrays;
import java.util.List;
import static org.exoplatform.calendar.service.AssertUtil.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-calendar-service.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.portal-configuration.xml")
})
public class TestCalendarHandler extends BaseCalendarServiceTestCase {
  protected CalendarHandler calHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.calHandler = this.calendarService_.getCalendarHandler();    
  }

  public void testCreatePersonalCalendar() {
    Calendar cal = new Calendar();
    cal.setName("testCreatePersonalCalendar");
    cal.setCalendarType(Calendar.Type.PERSONAL);
    cal.setCalendarOwner(username);

    cal = calHandler.saveCalendar(cal);

    Calendar calSaved = calHandler.getCalendarById(cal.getId(), Calendar.Type.PERSONAL);
    assertNotNull(calSaved);
    assertEquals("testCreatePersonalCalendar", calSaved.getName());
  }

  public void testCreateGroupCalendar() {
    Calendar cal = new Calendar();
    cal.setName("testCreateGroupCalendar");
    cal.setCalendarType(Calendar.Type.GROUP);
    cal.setGroups(userGroups);

    cal = calHandler.saveCalendar(cal);

    Calendar calSaved = calHandler.getCalendarById(cal.getId(), Calendar.Type.GROUP);
    assertNotNull(calSaved);
    assertEquals("testCreateGroupCalendar", calSaved.getName());
  }

  public void testGetAllPersonalCalendar() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    TestUtil.createPersonalCalendar(calHandler, "testGetAllPersonalCalendar_Personal", username);
    TestUtil.createGroupCalendar(calHandler, "testGetAllPersonalCalendar_Group", userGroups);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.PERSONAL);
    query.setUserName(username);
    query.setShowAll(true);

    List<Calendar> calendars = calHandler.findCalendarsByIdentity(identity, Calendar.Type.PERSONAL, null);
    assertNotNull(calendars);
    int size = calendars.size();
    assertTrue("User " + username + " must has at least 1 personal calendar", size >= 1);
    assertContainCalendarName(calendars, "testGetAllPersonalCalendar_Personal");
    assertNotContainCalendarName(calendars, "testGetAllPersonalCalendar_Group");
  }

  public void testGetAllGroupCalendarOfUser() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    TestUtil.createPersonalCalendar(calHandler, "testGetAllGroupCalendarOfUser_Personal", username);
    TestUtil.createGroupCalendar(calHandler, "testGetAllGroupCalendarOfUser_Group", userGroups);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.GROUP);
    query.setGroups(Arrays.asList("/platform/users"));
    query.setShowAll(true);

    List<Calendar> calendars = calHandler.findCalendarsByIdentity(identity, Calendar.Type.GROUP, null);
    assertNotNull(calendars);
    int size = calendars.size();
    assertTrue("User " + username + " must has at least 1 group calendar", size >= 1);
    assertContainCalendarName(calendars, "testGetAllGroupCalendarOfUser_Group");
    assertNotContainCalendarName(calendars, "testGetAllGroupCalendarOfUser_Personal");
  }

  public void testRemovePersonalCalendar() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    Calendar cal1 = TestUtil.createPersonalCalendar(calHandler, "testRemovePersonalCalendar_1", username);
    Calendar cal2 = TestUtil.createPersonalCalendar(calHandler, "testRemovePersonalCalendar_2", username);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.PERSONAL);
    query.setUserName(username);
    query.setShowAll(true);

    List<Calendar> calendars;
    int size;

    calendars = calHandler.findCalendarsByIdentity(identity, Calendar.Type.PERSONAL, null);
    size = calendars.size();
    assertTrue("User " + username + " must have at least 2 personal calendars", size >= 2);
    assertContainCalendarName(calendars, "testRemovePersonalCalendar_1");
    assertContainCalendarName(calendars, "testRemovePersonalCalendar_2");

    // Remove calendar
    calHandler.removeCalendar(cal1.getId(), Calendar.Type.PERSONAL);

    calendars = calHandler.findCalendarsByIdentity(identity, Calendar.Type.PERSONAL, null);
    size = calendars.size();
    assertTrue("User " + username + " must have at least 1 personal calendars", size >= 1);
    assertNotContainCalendarName(calendars, "testRemovePersonalCalendar_1");
    assertContainCalendarName(calendars, "testRemovePersonalCalendar_2");
  }

  public void testRemoveGroupCalendar() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    Calendar cal1 = TestUtil.createGroupCalendar(calHandler, "testRemoveGroupCalendar_1", userGroups);
    Calendar cal2 = TestUtil.createGroupCalendar(calHandler, "testRemoveGroupCalendar_2", userGroups);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.GROUP);
    query.setGroups(Arrays.asList("/platform/users"));
    query.setShowAll(true);

    List<Calendar> calendars;
    int size;

    calendars = calHandler.findCalendarsByIdentity(identity, Calendar.Type.GROUP, null);
    size = calendars.size();
    assertTrue("User must have at least 2 group calendars", size >= 2);
    assertContainCalendarName(calendars, "testRemoveGroupCalendar_1");
    assertContainCalendarName(calendars, "testRemoveGroupCalendar_2");

    // Remove calendar
    calHandler.removeCalendar(cal1.getId(), Calendar.Type.GROUP);

    calendars = calHandler.findCalendarsByIdentity(identity, Calendar.Type.GROUP, null);
    size = calendars.size();
    assertTrue("User must have at least 1 personal calendars", size >= 1);
    assertNotContainCalendarName(calendars, "testRemoveGroupCalendar_1");
    assertContainCalendarName(calendars, "testRemoveGroupCalendar_2");
  }

  public void testUpdatePersonalCalendar() {
    String calId = TestUtil.createPersonalCalendar(calHandler, "testUpdatePersonalCalendar", username).getId();

    Calendar cal = calHandler.getCalendarById(calId, Calendar.Type.PERSONAL);
    assertNotNull(cal);
    assertEquals("testUpdatePersonalCalendar", cal.getName());

    cal.setName("testUpdatePersonalCalendar_updated");
    cal.setDescription("testUpdatePersonalCalendar description");
    calHandler.updateCalendar(cal);

    cal = calHandler.getCalendarById(calId, Calendar.Type.PERSONAL);
    assertNotNull(cal);
    assertEquals("testUpdatePersonalCalendar_updated", cal.getName());
    assertEquals("testUpdatePersonalCalendar description", cal.getDescription());
  }

  public void testUpdateGroupCalendar() {
    String calId = TestUtil.createGroupCalendar(calHandler, "testUpdateGroupCalendar", userGroups).getId();

    Calendar cal = calHandler.getCalendarById(calId, Calendar.Type.GROUP);
    assertNotNull(cal);
    assertEquals("testUpdateGroupCalendar", cal.getName());

    cal.setName("testUpdateGroupCalendar_updated");
    cal.setDescription("testUpdateGroupCalendar description");
    calHandler.updateCalendar(cal);

    cal = calHandler.getCalendarById(calId, Calendar.Type.GROUP);
    assertNotNull(cal);
    assertEquals("testUpdateGroupCalendar_updated", cal.getName());
    assertEquals("testUpdateGroupCalendar description", cal.getDescription());
  }
}
