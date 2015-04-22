package org.exoplatform.calendar.ws;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCollection;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;
import org.exoplatform.webservice.cs.rest.AbstractResourceTest;

public abstract class TestRestApi extends AbstractResourceTest {

  protected Calendar userCalendar;
  
  protected Calendar groupCalendar;
  
  protected Calendar sharedCalendar;
  
  protected CalendarWebservice calendarWebservice;

  protected CalendarService calendarService;

  protected static final String baseURI = "";
  
  protected MultivaluedMap<String, String> headers;

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    RequestLifeCycle.begin(container);
    calendarWebservice = (CalendarWebservice) container.getComponentInstanceOfType(CalendarWebservice.class);
    calendarService = (CalendarService) container.getComponentInstanceOfType(CalendarService.class);
    binder.addResource(calendarWebservice, null);

    CalendarRestApi restApi =  (CalendarRestApi)container.getComponentInstanceOfType(CalendarRestApi.class);
    binder.addResource(restApi, null);

    userCalendar = this.createPersonalCalendar("root-calendar", "root");
    
    groupCalendar = this.createGroupCalendarWithEditPermission("group-calendar", Arrays.asList("/platform/administrators", "/platform/users"), "/platform/administrators/:*.manager");
    
    sharedCalendar = this.createSharedCalendarWithEditable("shared-calendar", "root", "john");

    headers = new MultivaluedMapImpl();
  }

  public void tearDown() throws Exception {
    String[] users = new String[] {"root", "john"};
    
    for (String username : users) {
      CalendarCollection<Calendar> cals = calendarService.getAllCalendars(username, Calendar.TYPE_ALL,0, Utils.UNLIMITED);
      for (int i = 0; i < cals.getFullSize(); i++) {
        String id = cals.get(i).getId();
        calendarService.removeUserCalendar(username, id);
        calendarService.removePublicCalendar(id);
        calendarService.removeSharedCalendar(username, id);      
      }
      for (EventCategory cat : calendarService.getEventCategories(username, 0, -1)) {
        calendarService.removeEventCategory(username, cat.getId());
      }
      for (FeedData f : calendarService.getFeeds(username)) {
        calendarService.removeFeedData(username, f.getTitle());
      }      
    }
    ConversationState.setCurrent(null);
    super.tearDown();
    RequestLifeCycle.end();
  }
  
  protected String currentUser() {
    return ConversationState.getCurrent().getIdentity().getUserId();
  }
  
  protected CalendarEvent createEvent(Calendar cal, String sum) {
    TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
    java.util.Calendar from = java.util.Calendar.getInstance(tz);
    java.util.Calendar to = java.util.Calendar.getInstance(tz);
    to.add(java.util.Calendar.WEEK_OF_MONTH, 1);

    CalendarEvent ev = new CalendarEvent();
    ev.setSummary(sum);
    ev.setCalendarId(cal.getId());
    ev.setFromDateTime(from.getTime());
    ev.setToDateTime(to.getTime());
    return ev;
  }
  
  protected CalendarEvent createEvent(Calendar cal) {
    return createEvent(cal, null);
  }

  protected CalendarEvent createRepetitiveEventForTest(Calendar cal) throws  Exception{
    TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
    java.util.Calendar fromCal = java.util.Calendar.getInstance(tz);
    fromCal.set(2013, 2, 7, 5, 30);

    java.util.Calendar toCal = java.util.Calendar.getInstance(tz);
    toCal.set(2013, 2, 7, 6, 30);

    CalendarEvent recurEvent = new CalendarEvent();
    recurEvent.setSummary("repeated past");
    recurEvent.setFromDateTime(fromCal.getTime());
    recurEvent.setToDateTime(toCal.getTime());
    recurEvent.setRepeatType(CalendarEvent.RP_DAILY);
    recurEvent.setRepeatInterval(1);
    recurEvent.setRepeatCount(6);
    recurEvent.setRepeatUntilDate(null);
    recurEvent.setRepeatByDay(null);
    recurEvent.setRepeatUntilDate(null);
    recurEvent.setCalendarId(cal.getId());

    return recurEvent;
  }

  protected EventCategory createEventCategory(String uName, String name) throws Exception {
    EventCategory eventCategory = new EventCategory();
    eventCategory.setName(name);
    calendarService.saveEventCategory(uName, eventCategory, true);
    return eventCategory;
  }

  protected Calendar createPersonalCalendar(String name, String owner, String... viewPermissions) {
    Calendar cal = new Calendar() ;
    cal.setName(name) ;
    cal.setDescription("Description") ;
    cal.setCalType(Calendar.TYPE_PRIVATE);
    cal.setCalendarOwner(owner);
    cal.setViewPermission(viewPermissions);
    calendarService.saveUserCalendar(owner, cal, true);
    return cal;
  }
  
  protected Calendar createPersonalCalendarWithEditPermission(String name, String owner, String... editPermissions) {
    Calendar cal = this.createPersonalCalendar(name, owner, editPermissions);
    cal.setEditPermission(editPermissions);
    calendarService.saveUserCalendar(owner, cal, false);
    return cal;
  }

  protected Calendar createGroupCalendar(String name, String ...groups) {
    Calendar publicCalendar = new Calendar();
    publicCalendar.setName(name);
    publicCalendar.setDescription("Description");
    publicCalendar.setGroups(groups);
    publicCalendar.setCalType(Calendar.TYPE_PUBLIC);
    calendarService.savePublicCalendar(publicCalendar, true);
    return publicCalendar;
  }
  
  protected Calendar createGroupCalendarWithEditPermission(String name, List<String> groups, String... editPermissions) {
    Calendar cal = this.createGroupCalendar(name, groups.toArray(new String[]{}));
    cal.setEditPermission(editPermissions);
    calendarService.savePublicCalendar(cal, false);
    return cal;
  }
  
  protected Calendar createSharedCalendar(String name, String owner, String... share) throws Exception {
    Calendar cal = this.createPersonalCalendar(name, owner, share);
    calendarService.shareCalendar(owner, cal.getId(), Arrays.asList(share));
    return cal;
  }
  
  protected Calendar createSharedCalendarWithEditable(String name, String owner, String... share) throws Exception {
    Calendar cal = this.createPersonalCalendarWithEditPermission(name, owner, share);
    calendarService.shareCalendar(owner, cal.getId(), Arrays.asList(share));
    return cal;
  }
}