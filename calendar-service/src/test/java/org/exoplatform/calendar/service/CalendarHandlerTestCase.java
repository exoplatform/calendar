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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.component.core.test.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.jcr-storage-configuration.xml")
})
public class CalendarHandlerTestCase extends BaseCalendarServiceTestCase {
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

    cal = calHandler.saveCalendar(cal, true);

    Calendar calSaved = calHandler.getCalendarById(cal.getId(), Calendar.Type.PERSONAL);
    assertNotNull(calSaved);
    assertEquals("testCreatePersonalCalendar", calSaved.getName());
  }

  public void testCreateGroupCalendar() {
    Calendar cal = new Calendar();
    cal.setName("testCreateGroupCalendar");
    cal.setCalendarType(Calendar.Type.GROUP);
    cal.setGroups(userGroups);

    cal = calHandler.saveCalendar(cal, true);

    Calendar calSaved = calHandler.getCalendarById(cal.getId(), Calendar.Type.GROUP);
    assertNotNull(calSaved);
    assertEquals("testCreateGroupCalendar", calSaved.getName());
  }

  public void testGetAllPersonalCalendar() throws Exception {
    createPersonalCalendar("testGetAllPersonalCalendar_Personal", username);
    createGroupCalendar("testGetAllPersonalCalendar_Group", userGroups);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.PERSONAL);
    query.setUserName(username);
    query.setShowAll(true);

    ListAccess<Calendar> calendars = calHandler.findCalendarsByQuery(query);
    assertNotNull(calendars);
    int size = calendars.getSize();
    assertTrue("User " + username + " must has at least 1 personal calendar", size >= 1);
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testGetAllPersonalCalendar_Personal");
    assertNotContainCalendarName(Arrays.asList(calendars.load(0, size)), "testGetAllPersonalCalendar_Group");
  }

  public void testGetAllGroupCalendarOfUser() throws Exception {
    createPersonalCalendar("testGetAllGroupCalendarOfUser_Personal", username);
    createGroupCalendar("testGetAllGroupCalendarOfUser_Group", userGroups);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.GROUP);
    query.setGroups(Arrays.asList("/platform/users"));
    query.setShowAll(true);

    ListAccess<Calendar> calendars = calHandler.findCalendarsByQuery(query);
    assertNotNull(calendars);
    int size = calendars.getSize();
    assertTrue("User " + username + " must has at least 1 group calendar", size >= 1);
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testGetAllGroupCalendarOfUser_Group");
    assertNotContainCalendarName(Arrays.asList(calendars.load(0, size)), "testGetAllGroupCalendarOfUser_Personal");
  }

  public void testRemovePersonalCalendar() throws Exception {
    Calendar cal1 = createPersonalCalendar("testRemovePersonalCalendar_1", username);
    Calendar cal2 = createPersonalCalendar("testRemovePersonalCalendar_2", username);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.PERSONAL);
    query.setUserName(username);
    query.setShowAll(true);

    ListAccess<Calendar> calendars;
    int size;

    calendars = calHandler.findCalendarsByQuery(query);
    size = calendars.getSize();
    assertTrue("User " + username + " must have at least 2 personal calendars", size >= 2);
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemovePersonalCalendar_1");
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemovePersonalCalendar_2");

    // Remove calendar
    calHandler.removeCalendar(cal1.getId(), Calendar.Type.PERSONAL);

    calendars = calHandler.findCalendarsByQuery(query);
    size = calendars.getSize();
    assertTrue("User " + username + " must have at least 1 personal calendars", size >= 1);
    assertNotContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemovePersonalCalendar_1");
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemovePersonalCalendar_2");
  }

  public void testRemoveGroupCalendar() throws Exception {
    Calendar cal1 = createGroupCalendar("testRemoveGroupCalendar_1", userGroups);
    Calendar cal2 = createGroupCalendar("testRemoveGroupCalendar_2", userGroups);

    CalendarQuery query = new CalendarQuery();
    query.setCalType(Calendar.Type.GROUP);
    query.setGroups(Arrays.asList("/platform/users"));
    query.setShowAll(true);

    ListAccess<Calendar> calendars;
    int size;

    calendars = calHandler.findCalendarsByQuery(query);
    size = calendars.getSize();
    assertTrue("User must have at least 2 group calendars", size >= 2);
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemoveGroupCalendar_1");
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemoveGroupCalendar_2");

    // Remove calendar
    calHandler.removeCalendar(cal1.getId(), Calendar.Type.GROUP);

    calendars = calHandler.findCalendarsByQuery(query);
    size = calendars.getSize();
    assertTrue("User must have at least 1 personal calendars", size >= 1);
    assertNotContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemoveGroupCalendar_1");
    assertContainCalendarName(Arrays.asList(calendars.load(0, size)), "testRemoveGroupCalendar_2");
  }

  public void testUpdatePersonalCalendar() {
    String calId = createPersonalCalendar("testUpdatePersonalCalendar", username).getId();

    Calendar cal = calHandler.getCalendarById(calId, Calendar.Type.PERSONAL);
    assertNotNull(cal);
    assertEquals("testUpdatePersonalCalendar", cal.getName());

    cal.setName("testUpdatePersonalCalendar_updated");
    cal.setDescription("testUpdatePersonalCalendar description");
    calHandler.saveCalendar(cal, false);

    cal = calHandler.getCalendarById(calId, Calendar.Type.PERSONAL);
    assertNotNull(cal);
    assertEquals("testUpdatePersonalCalendar_updated", cal.getName());
    assertEquals("testUpdatePersonalCalendar description", cal.getDescription());
  }

  public void testUpdateGroupCalendar() {
    String calId = createGroupCalendar("testUpdateGroupCalendar", userGroups).getId();

    Calendar cal = calHandler.getCalendarById(calId, Calendar.Type.GROUP);
    assertNotNull(cal);
    assertEquals("testUpdateGroupCalendar", cal.getName());

    cal.setName("testUpdateGroupCalendar_updated");
    cal.setDescription("testUpdateGroupCalendar description");
    calHandler.saveCalendar(cal, false);

    cal = calHandler.getCalendarById(calId, Calendar.Type.GROUP);
    assertNotNull(cal);
    assertEquals("testUpdateGroupCalendar_updated", cal.getName());
    assertEquals("testUpdateGroupCalendar description", cal.getDescription());
  }

  private void assertNotContainCalendarName(Collection<Calendar> calendars, String name) {
    for (Calendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        fail("List must not contain the calendar '" + name + "'");
      }
    }
  }

  private void assertContainCalendarName(Collection<Calendar> calendars, String name) {
    for (Calendar calendar : calendars) {
      if (calendar.getName().equals(name)) {
        return;
      }
    }
    fail("List must contain the calendar '" + name + "'");
  }

  private Calendar createGroupCalendar(String calName, String[] groups) {
    Calendar cal = new Calendar();
    cal.setName(calName);
    cal.setCalendarType(Calendar.Type.GROUP);
    cal.setGroups(groups);

    cal = calHandler.saveCalendar(cal, true);

    return cal;
  }

  private Calendar createPersonalCalendar(String calName, String owner) {
    Calendar cal = new Calendar();
    cal.setName(calName);
    cal.setCalendarType(Calendar.Type.PERSONAL);
    cal.setCalendarOwner(owner);

    cal = calHandler.saveCalendar(cal, true);

    return cal;
  }
}
