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
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.newgrouplistener_ignoreall_configuration.xml")
})
public class TestNewGroupListenerWithIgnoreAll extends BaseCalendarServiceTestCase {
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

  public void testIgnoredGroup() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.createGroupInstance();
    group.setGroupName("groupA");
    group.setLabel("label for group groupA");
    groupHandler.addChild(null, group, true);

    assertNotNull(groupHandler.findGroupById("/groupA"));
    List<GroupCalendarData> calendars =  calendarService.getGroupCalendars(new String[]{"/groupA"}, true, username);
    assertEquals(0, calendars.size());

    Group plf = groupHandler.findGroupById("/platform");
    group = groupHandler.createGroupInstance();
    group.setGroupName("groupB");
    group.setLabel("label for group groupB");
    groupHandler.addChild(plf, group, true);

    assertNotNull(groupHandler.findGroupById("/groupB"));
    calendars =  calendarService.getGroupCalendars(new String[]{"/platform/groupB"}, true, username);
    assertEquals(0, calendars.size());
  }
}
