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

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;

@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.component.core.test.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.newgrouplistener_configuration.xml")
})
public class TestNewGroupListener extends BaseCalendarServiceTestCase {
  private CalendarService calendarService;
  private OrganizationService organizationService;
  private String username = "root";

  @Override
  public void setUp() throws Exception {
    super.setUp();

    calendarService = getService(CalendarService.class);
    organizationService = getService(OrganizationService.class);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testCreateNewGroup() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.createGroupInstance();
    group.setGroupName("groupA");
    group.setLabel("label for group A");
    groupHandler.addChild(null, group, true);

    assertNotNull(groupHandler.findGroupById("/groupA"));

    List<GroupCalendarData> calendars =  calendarService.getGroupCalendars(new String[]{"/groupA"}, true, username);
    assertEquals(1, calendars.size());

    GroupCalendarData data = calendars.get(0);
    assertEquals("/groupA", data.getName());

    List<Calendar> cs = data.getCalendars();
    assertEquals(1, cs.size());

    Calendar c = cs.get(0);
    assertEquals("label for group A", c.getName());
    assertEquals("Europe/Brussels", c.getTimeZone());
    assertEquals("BEL", c.getLocale());
    assertEquals(1, c.getViewPermission().length);
    assertEquals("*.*", c.getViewPermission()[0]);
    assertEquals(1, c.getEditPermission().length);
    assertEquals("/groupA/:*.*", c.getEditPermission()[0]);
    assertEquals(1, c.getGroups().length);
    assertEquals("/groupA", c.getGroups()[0]);
  }

  public void testCreateNewChildGroup() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group plf = groupHandler.findGroupById("/platform");

    Group group = groupHandler.createGroupInstance();
    group.setGroupName("groupB");
    group.setLabel("label for groupB");
    groupHandler.addChild(plf, group, true);

    assertNotNull(groupHandler.findGroupById("/groupB"));

    List<GroupCalendarData> calendars =  calendarService.getGroupCalendars(new String[]{"/platform/groupB"}, true, username);
    assertEquals(1, calendars.size());

    GroupCalendarData data = calendars.get(0);
    assertEquals("/platform/groupB", data.getName());

    List<Calendar> cs = data.getCalendars();
    assertEquals(1, cs.size());

    Calendar c = cs.get(0);
    assertEquals("label for groupB", c.getName());
    assertEquals("Europe/Brussels", c.getTimeZone());
    assertEquals("BEL", c.getLocale());
    assertEquals(1, c.getViewPermission().length);
    assertEquals("*.*", c.getViewPermission()[0]);
    assertEquals(1, c.getEditPermission().length);
    assertEquals("/platform/groupB/:*.*", c.getEditPermission()[0]);
    assertEquals(1, c.getGroups().length);
    assertEquals("/platform/groupB", c.getGroups()[0]);
  }

  public void testSilentCreateNewGroup() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.createGroupInstance();
    group.setGroupName("groupSilent");
    group.setLabel("label for group groupSilent");
    groupHandler.addChild(null, group, false);

    assertNotNull(groupHandler.findGroupById("/groupSilent"));

    List<GroupCalendarData> calendars =  calendarService.getGroupCalendars(new String[]{"/groupSilent"}, true, username);
    assertEquals(0, calendars.size());
  }

  public void testIgnoredGroup() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.createGroupInstance();
    group.setGroupName("ignoreGroup");
    group.setLabel("label for group ignoreGroup");
    groupHandler.addChild(null, group, true);

    assertNotNull(groupHandler.findGroupById("/ignoreGroup"));
    List<GroupCalendarData> calendars =  calendarService.getGroupCalendars(new String[]{"/ignoreGroup"}, true, username);
    assertEquals(0, calendars.size());

    Group plf = groupHandler.findGroupById("/platform");
    group = groupHandler.createGroupInstance();
    group.setGroupName("ignoreGroupChild");
    group.setLabel("label for group ignoreGroupChild");
    groupHandler.addChild(plf, group, true);

    assertNotNull(groupHandler.findGroupById("/ignoreGroupChild"));
    calendars =  calendarService.getGroupCalendars(new String[]{"/platform/ignoreGroupChild"}, true, username);
    assertEquals(0, calendars.size());
  }

  public void testIgnoreAllDescendant() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.createGroupInstance();
    group.setGroupName("ignoreAllDescendant");
    group.setLabel("label for group ignoreAllDescendant");
    groupHandler.addChild(null, group, true);

    Group parent = groupHandler.findGroupById("/ignoreAllDescendant");
    assertNotNull(parent);
    List<GroupCalendarData> calendars =  calendarService.getGroupCalendars(new String[]{"/ignoreAllDescendant"}, true, username);
    assertEquals(1, calendars.size());

    group = groupHandler.createGroupInstance();
    group.setGroupName("firstLevelChild");
    group.setLabel("label for group firstLevelChild");
    groupHandler.addChild(parent, group, true);

    Group firstLevelChild = groupHandler.findGroupById("/ignoreAllDescendant/firstLevelChild");
    assertNotNull(firstLevelChild);
    calendars =  calendarService.getGroupCalendars(new String[]{"/ignoreAllDescendant/firstLevelChild"}, true, username);
    assertEquals(0, calendars.size());

    group = groupHandler.createGroupInstance();
    group.setGroupName("secondLevelChild");
    group.setLabel("label for group secondLevelChild");
    groupHandler.addChild(firstLevelChild, group, true);

    Group secondLevelChild = groupHandler.findGroupById("/ignoreAllDescendant/firstLevelChild/secondLevelChild");
    assertNotNull(secondLevelChild);
    calendars =  calendarService.getGroupCalendars(new String[]{"/ignoreAllDescendant/firstLevelChild/secondLevelChild"}, true, username);
    assertEquals(0, calendars.size());
  }

  public void testDeleteGroup() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.createGroupInstance();
    group.setGroupName("groupWillBeDeleted");
    group.setLabel("label for group groupWillBeDeleted");
    groupHandler.addChild(null, group, true);

    Group groupWillBeDeleted = groupHandler.findGroupById("/groupWillBeDeleted");
    assertNotNull(groupWillBeDeleted);

    List<GroupCalendarData> data =  calendarService.getGroupCalendars(new String[]{"/groupWillBeDeleted"}, true, username);
    assertEquals(1, data.size());

    List<Calendar> calendars = data.get(0).getCalendars();
    assertEquals(1, calendars.size());

    Calendar calendar = calendars.get(0);
    String calendarId = calendar.getId();
    assertNotNull(calendarService.getCalendarById(calendarId));

    groupHandler.removeGroup(groupWillBeDeleted, true);

    assertNull(groupHandler.findGroupById("/groupWillBeDeleted"));
    data =  calendarService.getGroupCalendars(new String[]{"/groupWillBeDeleted"}, true, username);
    assertEquals(0, data.size());
    assertNull(calendarService.getCalendarById(calendarId));
  }
}
