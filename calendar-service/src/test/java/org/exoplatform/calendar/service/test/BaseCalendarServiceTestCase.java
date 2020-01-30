/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service.test;

import java.text.SimpleDateFormat;
import java.util.*;

import org.picocontainer.Startable;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.ExtendedCalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS
 * 
 * @author : Hung nguyen hung.nguyen@exoplatform.com May 7, 2008
 */

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.calendar.component.service-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.calendar.component.service-dependencies-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.calendar.component.service-local-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.calendar.component.service-organization-configuration.xml"),
})
public abstract class BaseCalendarServiceTestCase extends AbstractKernelTest {

  protected static Log          log        = ExoLogger.getLogger("cs.calendar.services.test");

  protected TimeZone            tz         = java.util.Calendar.getInstance().getTimeZone();

  protected String              timeZone   = tz.getID();

  protected String              username   = "root";

  protected String[]            userGroups = new String[] { "/platform/users",
      "/organization/management/executive-board" };

  protected SimpleDateFormat    df         = new SimpleDateFormat(Utils.DATE_TIME_FORMAT, Locale.ENGLISH);

  protected OrganizationService organizationService_;

  protected CalendarService     calendarService_;
  
  protected ExtendedCalendarService xCalService;

  @Override
  public void setUp() throws Exception {
    begin();

    Locale.setDefault(Locale.ENGLISH);

    // Init services
    organizationService_ = getService(OrganizationService.class);
    calendarService_ = getService(CalendarService.class);
    xCalService = getService(ExtendedCalendarService.class);

    // . Init JCR root node
    JCRDataStorage storage = ((CalendarServiceImpl) calendarService_).getDataStorage();
    storage.getPublicCalendarHome();
    storage.getPublicCalendarServiceHome();
    storage.getUserCalendarHome(username);
    storage.getSharedCalendarHome();

    // Login user
    login(username);

    ListAccess<User> users = organizationService_.getUserHandler().findAllUsers(UserStatus.DISABLED);
    if (users != null) {
      for (User user : users.load(0, users.getSize())) {
        organizationService_.getUserHandler().setEnabled(user.getUserName(), true, false);
      }
    }
    System.setProperty("gatein.email.domain.url", "localhost:8080");
  }

  @Override
  public void tearDown() throws Exception {
    ListAccess<User> users = organizationService_.getUserHandler().findAllUsers();
    int size = users.getSize();
    for (User u : users.load(0, size)) {
      cleanData(u);
    }
    end();
  }

  protected void cleanData(User user) throws Exception {
    // . Get all private calendar of user
    List<Calendar> cals = calendarService_.getUserCalendars(user.getUserName(), true);

    // . Load all group calendar
    List<Group> groups = new ArrayList<Group>();
    groups.addAll(organizationService_.getGroupHandler().findGroupsOfUser(user.getUserName()));
    String[] groupIds = new String[groups.size()];
    for (int i = 0; i < groups.size(); i++) {
      groupIds[i] = groups.get(i).getId();
    }
    for (GroupCalendarData g : calendarService_.getGroupCalendars(groupIds,
                                                                  true,
                                                                  user.getUserName())) {
      cals.addAll(g.getCalendars());
    }

    // . Find all shared calendar
    GroupCalendarData gData = calendarService_.getSharedCalendars(user.getUserName(), true);
    if (gData != null)
      cals.addAll(gData.getCalendars());

    // . Remove all calendar
    for (int i = 0; i < cals.size(); i++) {
      String id = cals.get(i).getId();
      calendarService_.removeUserCalendar(user.getUserName(), id);
      calendarService_.removePublicCalendar(id);
      calendarService_.removeSharedCalendar(user.getUserName(), id);
    }

    // . Remove all EventCategory
    List<EventCategory> categories = calendarService_.getEventCategories(user.getUserName());
    for (EventCategory category : categories) {
      calendarService_.removeEventCategory(user.getUserName(), category.getId());
    }
  }

  protected Calendar createPrivateCalendar(String username, String name, String description) throws Exception {
    Calendar calendar = new Calendar();
    calendar.setName(name);
    calendar.setDescription(description);
    calendar.setPublic(false);
    calendarService_.saveUserCalendar(username, calendar, true);
    return calendar;
  }

  protected Calendar createGroupCalendar(String[] groups, String calName, String description) throws Exception {
    Calendar calendar = new Calendar();
    calendar.setName(calName);
    calendar.setDescription(description);
    calendar.setPublic(true);
    calendar.setGroups(groups);
    calendarService_.savePublicCalendar(calendar, true);
    return calendar;
  }
  
  protected Calendar createSharedCalendar(String name, String description, String[] shared) {
    try {
      Calendar sharedCalendar = new Calendar();
      sharedCalendar.setName(name);
      sharedCalendar.setDescription(description);
      sharedCalendar.setPublic(true);
      sharedCalendar.setViewPermission(shared);
      sharedCalendar.setEditPermission(shared);
      calendarService_.saveUserCalendar(username, sharedCalendar, true);

      calendarService_.shareCalendar(username, sharedCalendar.getId(), Arrays.asList(shared));

      return sharedCalendar;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  protected EventCategory createUserEventCategory(String username, String name) throws Exception {
    EventCategory eventCategory = new EventCategory();
    eventCategory.setName(name);
    calendarService_.saveEventCategory(username, eventCategory, true);
    return eventCategory;
  }

  protected CalendarEvent createCalendarEventInstance(String summary) {
    CalendarEvent event = new CalendarEvent();
    event.setSummary(summary);
    java.util.Calendar from = java.util.Calendar.getInstance();
    from.add(java.util.Calendar.MINUTE, 5);
    event.setFromDateTime(from.getTime());
    from.add(java.util.Calendar.HOUR, 1);
    event.setToDateTime(from.getTime());
    return event;
  }

  @SuppressWarnings("unchecked")
  protected <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }

  protected CalendarEvent createUserEvent(String calendarId,
                                          EventCategory eventCategory,
                                          String summary) {
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance();
    toCal.add(java.util.Calendar.HOUR, 1);
    return createUserEvent(calendarId, eventCategory, summary, true, fromCal, toCal);
  }

  protected CalendarEvent createUserEvent(String calendarId,
                                          EventCategory eventCategory,
                                          String summary,
                                          boolean isPrivate,
                                          java.util.Calendar fromCal,
                                          java.util.Calendar toCal) {
    try {
      CalendarEvent calendarEvent = new CalendarEvent();
      if (eventCategory != null) {
        calendarEvent.setEventCategoryId(eventCategory.getId());
        calendarEvent.setEventCategoryName(eventCategory.getName());
      }
      calendarEvent.setSummary(summary);
      calendarEvent.setFromDateTime(fromCal.getTime());
      calendarEvent.setToDateTime(toCal.getTime());
      calendarEvent.setPrivate(isPrivate);
      calendarService_.saveUserEvent(username, calendarId, calendarEvent, true);
      return calendarEvent;
    } catch (Exception e) {
      fail("Exception while create user event", e);
      return null;
    }
  }

  protected CalendarEvent createUserEvent(String username,
                                          String calendarId,
                                          EventCategory eventCategory,
                                          String summary,
                                          boolean isPrivate,
                                          java.util.Calendar fromCal,
                                          java.util.Calendar toCal) {
    try {
      CalendarEvent calendarEvent = new CalendarEvent();
      if (eventCategory != null) {
        calendarEvent.setEventCategoryId(eventCategory.getId());
        calendarEvent.setEventCategoryName(eventCategory.getName());
      }
      calendarEvent.setSummary(summary);
      calendarEvent.setFromDateTime(fromCal.getTime());
      calendarEvent.setToDateTime(toCal.getTime());
      calendarEvent.setPrivate(isPrivate);
      calendarService_.saveUserEvent(username, calendarId, calendarEvent, true);
      return calendarEvent;
    } catch (Exception e) {
      fail("Exception while create user event", e);
      return null;
    }
  }

  protected CalendarEvent createUserEvent(String summary) throws Exception {
    Calendar calendar = createPrivateCalendar(username, "CalendarTest", "CalendarTest");
    EventCategory category = createUserEventCategory(username, "CalendarCategoryTest");
    java.util.Calendar from = java.util.Calendar.getInstance();
    java.util.Calendar to = java.util.Calendar.getInstance();
    to.add(java.util.Calendar.HOUR, 1);
    return createUserEvent(calendar.getId(), category, summary, true, from, to);
  }

  protected CalendarEvent createGroupEvent(String publicCalendarId,
                                           EventCategory eventCategory,
                                           String summary,
                                           boolean isPrivate,
                                           java.util.Calendar fromCal,
                                           java.util.Calendar toCal) {
    try {
      CalendarEvent publicEvent = new CalendarEvent();
      if (eventCategory != null) {
        publicEvent.setEventCategoryId(eventCategory.getId());
        publicEvent.setEventCategoryName(eventCategory.getName());
      }
      publicEvent.setSummary(summary);
      publicEvent.setFromDateTime(fromCal.getTime());
      publicEvent.setToDateTime(toCal.getTime());
      publicEvent.setPrivate(isPrivate);
      calendarService_.savePublicEvent(publicCalendarId, publicEvent, true);
      return publicEvent;
    } catch (Exception e) {
      fail();
      return null;
    }
  }

  protected void login(String username) {
    List<MembershipEntry> entries = new LinkedList<MembershipEntry>();

    MembershipHandler mHandler = organizationService_.getMembershipHandler();
    try {
      Collection<Membership> memberships = mHandler.findMembershipsByUser(username);
      for (Membership m : memberships) {
        entries.add(new MembershipEntry(m.getGroupId(), m.getMembershipType()));
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    ;
    login(username, entries);
  }

  private void login(String username, Collection<MembershipEntry> entries) {
    Identity identity = new Identity(username, entries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }
}
