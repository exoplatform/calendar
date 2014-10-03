package org.exoplatform.cs.bench.test;

import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.bench.CalendarDataInjector;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

import java.util.*;

/**
 * Created by tuyennt on 10/2/14.
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.calendar.test.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/calendar-injector-configuration.xml")
})
public class CalendarDataInjectorTestCase extends AbstractKernelTest {
  private OrganizationService orgService;
  private CalendarService calService;
  private CalendarDataInjector injector;

  private String username = "root";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    PortalContainer container = getContainer();
    orgService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
    calService = (CalendarService)container.getComponentInstanceOfType(CalendarService.class);
    injector = (CalendarDataInjector)getContainer().getComponentInstanceOfType(CalendarDataInjector.class);

    assertNotNull("OrganizationService must not null", orgService);
    assertNotNull("CalendarService must not null", calService);
    assertNotNull("Injector must not null", injector);

    login(username);
  }

  public void testInjectAndRejectData() throws Exception {
    //Load user group
    Collection<Group> groups = orgService.getGroupHandler().findGroupsOfUser(username);
    String[] userGroups = new String[groups.size()];
    int i = 0;
    for(Group g : groups) {
      userGroups[i++] = g.getId();
    }

    //Before inject, user has not any calendar
    List<Calendar> calendars = calService.getUserCalendars(username, true);
    assertEquals(0, calendars.size());
    List<GroupCalendarData> datas = calService.getGroupCalendars(userGroups, true, username);
    assertEquals(0, datas.size());
    List<EventCategory> categories = calService.getEventCategories(username);
    assertEquals(0, categories.size());

    //Inject data
    // it will create 9 private calendars and 9 group calendars (in each group)
    // Each calendar, it will create 2 event and 2 task
    injector.inject(new HashMap<String, String>());

    calendars = calService.getUserCalendars(username, true);
    assertEquals(9, calendars.size());
    assertInjectedCalendar(calendars, true);

    datas = calService.getGroupCalendars(userGroups, true, username);
    assertEquals(userGroups.length, datas.size());
    for(GroupCalendarData data : datas) {
      List<Calendar> cs = data.getCalendars();
      assertEquals(9, cs.size());
      assertInjectedCalendar(cs, false);
    }

    categories = calService.getEventCategories(username);
    assertEquals(9, categories.size());

    //. Reject data - remove all injected data
    injector.reject(new HashMap<String, String>());

    calendars = calService.getUserCalendars(username, true);
    assertEquals(0, calendars.size());
    datas = calService.getGroupCalendars(userGroups, true, username);
    assertEquals(0, datas.size());
    categories = calService.getEventCategories(username);
    assertEquals(0, categories.size());
  }

  private void assertInjectedCalendar(List<Calendar> calendars, boolean isPrivate) throws Exception {
    for(Calendar c : calendars) {
      assertTrue(c.getName().startsWith("Lorem"));
      List<CalendarEvent> events;
      if(isPrivate) {
        events = calService.getUserEventByCalendar(username, Arrays.asList(c.getId()));
      } else {
        events = calService.getGroupEventByCalendar(Arrays.asList(c.getId()));
      }
      assertEquals(4, events.size());
      for(CalendarEvent e : events) {
        assertTrue(e.getSummary().startsWith("Lorem"));
      }
    }
  }

  private void login(String userId) {
    List<MembershipEntry> entries = new LinkedList<MembershipEntry>();

    MembershipHandler mHandler = orgService.getMembershipHandler();
    try {
      Collection<Membership> memberships = mHandler.findMembershipsByUser(userId);
      for (Membership m : memberships) {
        entries.add(new MembershipEntry(m.getGroupId(), m.getMembershipType()));
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    };
    login(userId, entries);
  }

  private void login(String userId, Collection<MembershipEntry> entries) {
    Identity identity = new Identity(userId, entries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }
}
