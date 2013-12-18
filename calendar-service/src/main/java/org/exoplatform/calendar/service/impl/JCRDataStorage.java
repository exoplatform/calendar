/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.DataStorage;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventPageListQuery;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.RemoteCalendar;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ActivityTypeUtils;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.IdentityConstants;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 10, 2007
 */
public class JCRDataStorage implements DataStorage {

  final private static String    CALENDARS           = "calendars".intern();

  final private static String    SHARED_CALENDAR     = "sharedCalendars".intern();

  final private static String    FEED                = "eXoCalendarFeed".intern();

  final private static String    CALENDAR_EVENT      = "events".intern();

  final private static String    CALENDAR_SETTING    = "calendarSetting".intern();

  final private static String    EVENT_CATEGORIES    = "eventCategories".intern();

  private final static String    VALUE               = "value".intern();

  private final NodeHierarchyCreator   nodeHierarchyCreator_;

  private final RepositoryService      repoService_;

  private final SessionProviderService sessionProviderService_;
  
  private final ExoCache<String, List<Calendar>>      groupCalendarCache_;

  /** cache for private calendars of users - store username as key */
  private final ExoCache<String, List<Calendar>>      userCalendarCache;

  /** cache for calendar setting of users - store username as key */
  private final ExoCache<String, CalendarSetting>     userCalendarSetting;

  /** cache for calendar events in a particular group calendar - store query as key */
  private final ExoCache<String, List<CalendarEvent>> groupCalendarEventCache;

  /** cache for calendar events in a particular group calendar - store query as key */
  private final ExoCache<String, List<CalendarEvent>> groupCalendarRecurrentEventCache;

  /** cache for event categories of users - store username as key */
  private final ExoCache<String, List<EventCategory>> userEventCategories;

  /**
   * The map that contains all the locks
   */
  private final ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>(64, 0.75f, 64);


  private static final Log       log                 = ExoLogger.getLogger("cs.calendar.service");

  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService, CacheService cservice) throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repoService_ = repoService;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    sessionProviderService_ = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    groupCalendarCache_     = cservice.getCacheInstance("calendar.GroupCalendar");
    userCalendarCache       = cservice.getCacheInstance("calendar.UserCalendar");
    groupCalendarEventCache = cservice.getCacheInstance("calendar.GroupCalendarEvent");
    groupCalendarRecurrentEventCache = cservice.getCacheInstance("calendar.GroupCalendarRecurrentEvent");
    userCalendarSetting     = cservice.getCacheInstance("calendar.UserCalendarSetting");
    userEventCategories     = cservice.getCacheInstance("calendar.EventCategories");
  }

    
  /**
   * Gives the lock related to the given type and given id
   * @param type the type of the object for which we want a lock
   * @param id the unique id of the object for which we want a lock
   * @return the existing lock if a lock exist for the given id and given type
   * otherwise a new lock
   */
  private Lock getLock(String type, String id) {
    String fullId = new StringBuilder(type.length() + id.length() + 1).append(type).
        append('-').append(id).toString();
    Lock lock = locks.get(fullId);
    if (lock != null) {
      return lock;
    }
    lock = new InternalLock(fullId);
    Lock prevLock = locks.putIfAbsent(fullId, lock);
    if (prevLock != null)
    {
      lock = prevLock;
    }
    return lock;
  }
     
  /**
   * {@inheritDoc}
   */
  public Node getPublicCalendarServiceHome() throws Exception {
    SessionProvider sProvider = createSystemProvider();
    Node publicApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider); 
    try {
      return publicApp.getNode(Utils.CALENDAR_APP);
    } catch (Exception e) {
      Node calendarApp = publicApp.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED);
      publicApp.getSession().save();
      return calendarApp;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getSharedCalendarHome() throws Exception {
    // have to use system session
    Node calendarServiceHome = getPublicCalendarServiceHome();
    try {
      return calendarServiceHome.getNode(SHARED_CALENDAR);
    } catch (Exception e) {
      Node sharedCal = calendarServiceHome.addNode(SHARED_CALENDAR, Utils.NT_UNSTRUCTURED);
      calendarServiceHome.getSession().save();
      return sharedCal;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getPublicRoot() throws Exception {
    SessionProvider sProvider = createSystemProvider();
    return nodeHierarchyCreator_.getPublicApplicationNode(sProvider); 
  }

  /**
   * {@inheritDoc}
   */
  public Node getUserCalendarServiceHome(String username) throws Exception {
    // SessionProvider sProvider = createSessionProvider();
    SessionProvider sProvider = createSystemProvider();
    Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sProvider, username); 
    Node calendarRoot;
    try {
      return userApp.getNode(Utils.CALENDAR_APP);
    } catch (Exception e) {
      calendarRoot = userApp.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED);
      if (!calendarRoot.hasNode(CALENDAR_SETTING)) {
        addCalendarSetting(calendarRoot, new CalendarSetting());
      }
      userApp.getSession().save();
      return calendarRoot;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getPublicCalendarHome() throws Exception {
    Node calendarServiceHome = getPublicCalendarServiceHome();
    try {
      return calendarServiceHome.getNode(CALENDARS);
    } catch (Exception e) {
      Node cal = calendarServiceHome.addNode(CALENDARS, Utils.NT_UNSTRUCTURED);
      calendarServiceHome.getSession().save();
      return cal;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getUserCalendarHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(username);
    try {
      return calendarServiceHome.getNode(CALENDARS);
    } catch (Exception e) {
      Node calendars = calendarServiceHome.addNode(CALENDARS, Utils.NT_UNSTRUCTURED);
      calendarServiceHome.getSession().save();
      return calendars;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getRssHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(username);
    try {
      return calendarServiceHome.getNode(FEED);
    } catch (Exception e) {
      Node feed = calendarServiceHome.addNode(FEED, Utils.NT_UNSTRUCTURED);
      calendarServiceHome.getSession().save();
      return feed;
    }
  }


  /**
   * @param username
   * @return
   * @throws Exception
   */
  protected Node getEventCategoryHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarServiceHome(username);
    try {
      return calendarServiceHome.getNode(EVENT_CATEGORIES);
    } catch (Exception e) {
      Node eventCat = calendarServiceHome.addNode(EVENT_CATEGORIES, Utils.NT_UNSTRUCTURED);
      calendarServiceHome.getSession().save();
      return eventCat;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    try {
      Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
      return getCalendar(new String[] { calendarId }, username, calendarNode, true);
    } catch (PathNotFoundException e) {
      log.debug("Failed to get calendar, maybe the calendar has been removed or not created");
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    String[] defaultCalendars;
    List<Calendar> calList;

    Lock lock = getLock("UserCalendar", username);
    lock.lock();
    try {
      calList = userCalendarCache.get(username);
      if (calList == null) {
        calList  = new ArrayList<Calendar>();
        Node userCalendarHome = getUserCalendarHome(username);
        NodeIterator iter     = userCalendarHome.getNodes();
        defaultCalendars      = getCalendarSetting(getUserCalendarServiceHome(username)).getFilterPrivateCalendars();

        while (iter.hasNext()) {
          Calendar cal = getCalendar(defaultCalendars, username, iter.nextNode(), isShowAll);
          calList.add(cal);
        }

        userCalendarCache.put(username, calList);
      }
      else {

        if (!isShowAll) {
          defaultCalendars = getCalendarSetting(getUserCalendarServiceHome(username)).getFilterPrivateCalendars();
          List<Calendar> filteredCalList = new ArrayList<Calendar>();

          for (Calendar calendar : calList) {
            if (!Arrays.asList(defaultCalendars).contains(calendar.getId())) filteredCalList.add(calendar);
          }
          calList = filteredCalList;
        }
      }
    } finally {
      lock.unlock();
    }

    /** return a copy */
    List<Calendar> returnedList = new ArrayList<Calendar>();
    for (Calendar calendar : calList) returnedList.add(calendar);
    return returnedList;
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    Node calendarHome = getUserCalendarHome(username);
    Node calendarNode;
    if (isNew) {
      try {
        calendarNode = calendarHome.getNode(calendar.getId());
      } catch (Exception e) {
        calendarNode = calendarHome.addNode(calendar.getId(), Utils.EXO_CALENDAR);
        calendarNode.setProperty(Utils.EXO_ID, calendar.getId());
      }
    } else {
      calendarNode = calendarHome.getNode(calendar.getId());
    }
    setCalendarProperties(calendarNode, calendar);
    Session session = calendarHome.getSession();
    session.save();

    Lock lock = getLock("UserCalendar", username);
    lock.lock();
    try {
      userCalendarCache.put(username, null);
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    Node calendarHome = getUserCalendarHome(username);
    if (calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId);
      Calendar calendar = getCalendar(new String[] { calendarId }, username, calNode, true);
      NodeIterator iter = calNode.getNodes();
      try {
        while (iter.hasNext()) {
          Node eventNode = iter.nextNode();
          Node eventFolder = getEventFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME)
                                                     .getDate()
                                                     .getTime());
          syncRemoveEvent(eventFolder, eventNode.getName());
          removeReminder(eventNode);
        }
        calNode.remove();
        calendarHome.save();
      } catch (Exception e) {
        log.error("Exception occurred when removing calendar " + calendarId, e);
      } finally {

      }

      Lock lock = getLock("UserCalendar", username);
      lock.lock();
      try {
        userCalendarCache.put(username, null);
      } finally {
        lock.unlock();
      }

      try {
        removeFeed(username, calendarId);
      } catch (Exception e) {
        log.warn("Exception occurred when removing feeds from calendar " + calendarId, e);
      }
      return calendar;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId);
    return getCalendar(new String[] { calendarId }, null, calendarNode, true);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds,
                                                   boolean isShowAll,
                                                   String username) throws Exception {
    List<Calendar> calendars;
    Node calendarHome = getPublicCalendarHome();
    List<GroupCalendarData> groupCalendars = new ArrayList<GroupCalendarData>();
    String[] defaultCalendars = null;
    if (username != null) {
      CalendarSetting calendarSetting = getCalendarSetting(getUserCalendarServiceHome(username));
      if (calendarSetting != null)
        defaultCalendars = calendarSetting.getFilterPublicCalendars();
    }

    for (String groupId : groupIds) {
      //StringBuilder queryString = new StringBuilder("/jcr:root" + calendarHome.getPath() + "//element(*,exo:calendar)[@exo:groups='").append(groupId).append("']");
      StringBuilder queryString = new StringBuilder("/jcr:root" + calendarHome.getPath() + "/element(*,exo:calendar)[@exo:groups='").append(groupId).append("']");
      List<Calendar> lCalendars;
      String sQuery = queryString.toString();
      Lock lock = getLock("GroupCalendar", sQuery);
      lock.lock();
      try {
        lCalendars = groupCalendarCache_.get(sQuery);
        if (lCalendars == null)
        {
          lCalendars = new ArrayList<Calendar>();
          QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
          Query query = qm.createQuery(sQuery, Query.XPATH);
          QueryResult result = query.execute();
          NodeIterator it = result.getNodes();
          while (it.hasNext()) {
            Node calNode = it.nextNode();
            Calendar cal = loadCalendar(calNode);
            lCalendars.add(cal);
          }
          groupCalendarCache_.put(sQuery, lCalendars);
        }
      } finally {
        lock.unlock(); 
      }
      
      if (!lCalendars.isEmpty()) {
        calendars = new ArrayList<Calendar>();
        for (Calendar c : lCalendars) {
          Calendar cal = getCalendar(defaultCalendars, null, c, isShowAll);
          if (cal != null)
            calendars.add(cal);
        }
        groupCalendars.add(new GroupCalendarData(groupId, groupId, calendars));
      }
    }
    return groupCalendars;
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception {
    Node calendarHome = getPublicCalendarHome();
    Node calendarNode;
    if (isNew) {
      if (calendarHome.hasNode(calendar.getId()))
        throw new Exception("This calendar is already exists");
      calendarNode = calendarHome.addNode(calendar.getId(), Utils.EXO_CALENDAR);
      calendarNode.setProperty(Utils.EXO_ID, calendar.getId());
    } else {
      calendarNode = calendarHome.getNode(calendar.getId());
    }
    setCalendarProperties(calendarNode, calendar);
    calendarHome.getSession().save();
    // Clear the cache to avoid inconsistency
    groupCalendarCache_.clearCache(); 
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removeGroupCalendar(String calendarId) throws Exception {
    Node calendarHome = getPublicCalendarHome();
    if (calendarHome.hasNode(calendarId)) {
      Node calNode = calendarHome.getNode(calendarId);
      Calendar calendar = getCalendar(new String[] { calendarId }, null, calNode, true);
      NodeIterator iter = calNode.getNodes();
      while (iter.hasNext()) {
        Node eventNode = iter.nextNode();
        Node eventFolder = getEventFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME)
                                                   .getDate()
                                                   .getTime());
        removeReminder(eventNode);
        syncRemoveEvent(eventFolder, eventNode.getName());
      }
      calNode.remove();
      calendarHome.getSession().save();
      // Clear the cache to avoid inconsistency
      groupCalendarCache_.clearCache(); 
      return calendar;
    }
    return null;
  }

  public Calendar getCalendar(String[] defaultFilterCalendars, String username, Node calNode, boolean isShowAll) throws Exception {
    if (!isShowAll && defaultFilterCalendars != null && Arrays.asList(defaultFilterCalendars).contains(calNode.getName()))
      return null;
    return loadCalendar(calNode);
  }
  
  private Calendar getCalendar(String[] defaultFilterCalendars, String username, Calendar cal, boolean isShowAll) throws Exception {
    if (!isShowAll && defaultFilterCalendars != null && Arrays.asList(defaultFilterCalendars).contains(cal.getId()))
      return null;
    return cal;
  }
   
  private Calendar loadCalendar(Node calNode) throws Exception {
    Calendar calendar = new Calendar();
    StringBuilder namePattern = new StringBuilder(256);
    namePattern.append(Utils.EXO_ID).append('|').append(Utils.EXO_NAME).append('|').append(Utils.EXO_DESCRIPTION)
         .append('|').append(Utils.EXO_LOCALE).append('|')
         .append(Utils.EXO_TIMEZONE).append('|').append(Utils.EXO_SHARED_COLOR).append('|')
         .append(Utils.EXO_CALENDAR_COLOR).append('|').append(Utils.EXO_CALENDAR_OWNER).append('|')
         .append(Utils.EXO_PUBLIC_URL).append('|').append(Utils.EXO_PRIVATE_URL).append('|').append(Utils.EXO_GROUPS)
         .append('|').append(Utils.EXO_VIEW_PERMISSIONS).append('|').append(Utils.EXO_EDIT_PERMISSIONS);
    PropertyIterator it = calNode.getProperties(namePattern.toString());
    List<String> groups = null;
    String[] viewPermission = null, editPermission = null;
    while (it.hasNext()) {
      Property p = it.nextProperty();
      String name = p.getName();
      if (name.equals(Utils.EXO_ID)) {
        calendar.setId(p.getString());
      } else if (name.equals(Utils.EXO_NAME)) {
        calendar.setName(p.getString());
      } else if (name.equals(Utils.EXO_DESCRIPTION)) {
        calendar.setDescription(p.getString());
      } else if (name.equals(Utils.EXO_LOCALE)) {
        calendar.setLocale(p.getString());
      } else if (name.equals(Utils.EXO_TIMEZONE)) {
        calendar.setTimeZone(p.getString());
      } else if (name.equals(Utils.EXO_SHARED_COLOR)) {
        calendar.setCalendarColor(p.getString());
      } else if (name.equals(Utils.EXO_CALENDAR_COLOR)) {
        calendar.setCalendarColor(p.getString());
      } else if (name.equals(Utils.EXO_CALENDAR_OWNER)) {
        calendar.setCalendarOwner(p.getString());
      } else if (name.equals(Utils.EXO_PUBLIC_URL)) {
        calendar.setPublicUrl(p.getString());
      } else if (name.equals(Utils.EXO_PRIVATE_URL)) {
        calendar.setPrivateUrl(p.getString());
      } else if (name.equals(Utils.EXO_GROUPS)) {
        Value[] values = p.getValues();
        groups = new ArrayList<String>();
        for (Value v : values) {
          groups.add(v.getString());
        }
      } else if (name.equals(Utils.EXO_VIEW_PERMISSIONS)) {
        viewPermission = ValuesToStrings(p.getValues());
      } else if (name.equals(Utils.EXO_EDIT_PERMISSIONS)) {
        editPermission = ValuesToStrings(p.getValues());
      }
    }

    if (!calendar.isPublic()) {
      if (groups != null) {
        calendar.setGroups(groups.toArray(new String[groups.size()]));
      }
      if (viewPermission != null) {
        calendar.setViewPermission(viewPermission);
      }
      if (editPermission != null) {
        calendar.setEditPermission(editPermission);
      }
    }
    return calendar;
  }

  /**=== Event Category APIs ===*/

  public List<EventCategory> getEventCategories(String username) throws Exception {
    List<EventCategory> eventCategoryList;
    Lock lock = getLock("EventCategories", username);
    lock.lock();

    try {
      eventCategoryList = userEventCategories.get(username);
      if (eventCategoryList == null) {
        /** query new */
        eventCategoryList = new ArrayList<EventCategory>();
        Node eventCategoryHome = getEventCategoryHome(username);
        NodeIterator iter = eventCategoryHome.getNodes();

        while (iter.hasNext()) {
          eventCategoryList.add(getEventCategory(iter.nextNode()));
        }
      }

      userEventCategories.put(username, eventCategoryList);
    } finally {
      lock.unlock();
    }

    return eventCategoryList;
  }

  public void saveEventCategory(String username,
                                EventCategory eventCategory,
                                String[] values,
                                boolean isNew) throws Exception {
    saveEventCategory(username, eventCategory, isNew);
  }

  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(username);
    Node eventCategoryNode = null;
    if (isNew) {
      eventCategoryNode = eventCategoryHome.addNode(eventCategory.getId(), Utils.EXO_EVENT_CATEGORY);
    } else {

      eventCategoryNode = eventCategoryHome.getNode(eventCategory.getId());
      Node calendarHome =  getUserCalendarHome(username);
      QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
      NodeIterator calIter = calendarHome.getNodes();
      Query query;
      QueryResult result;
      while (calIter.hasNext()) {
        StringBuilder queryString = new StringBuilder("/jcr:root").append(calIter.nextNode().getPath())
            .append("//element(*,exo:calendarEvent)[@exo:eventCategoryId='").append(eventCategory.getId())
                                                                      .append("']");
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        result = query.execute();
        NodeIterator it = result.getNodes();
        while (it.hasNext()) {
          Node eventNode = it.nextNode();
          eventNode.setProperty(Utils.EXO_EVENT_CATEGORY_NAME, eventCategory.getName());
        }
      }

      if (getSharedCalendarHome().hasNode(username)) {
        PropertyIterator iterPro = getSharedCalendarHome().getNode(username).getReferences();
        while (iterPro.hasNext()) {
          try {
            Node calendar = iterPro.nextProperty().getParent();
            NodeIterator it = calendar.getNodes();
            while (it.hasNext()) {
              Node eventNode = it.nextNode();
              if (eventNode.hasProperty(Utils.EXO_EVENT_CATEGORYID))
                if (eventNode.getProperty(Utils.EXO_EVENT_CATEGORYID)
                             .getString()
                             .equals(eventCategory.getId()))
                  eventNode.setProperty(Utils.EXO_EVENT_CATEGORY_NAME, eventCategory.getName());
            }

          } catch (Exception e) {
            if (log.isDebugEnabled())
              log.debug(e);
          }
        }
      }
    }
    eventCategoryNode.setProperty(Utils.EXO_ID, eventCategory.getId());
    eventCategoryNode.setProperty(Utils.EXO_NAME, eventCategory.getName());
    eventCategoryHome.getSession().save();

    Lock lock = getLock("EventCategories", username);
    lock.lock();
    try {
      userEventCategories.put(username, null);
    } finally {
      lock.unlock();
    }
  }

  public void removeEventCategory(String username, String eventCategoryId) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(username);
    if (eventCategoryHome.hasNode(eventCategoryId)) {
      Node eventCategoryNode = eventCategoryHome.getNode(eventCategoryId);
      for (CalendarEvent ce : getUserEventByCategory(username, eventCategoryId)) {
        ce.setEventCategoryId(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
        ce.setEventCategoryName(NewUserListener.DEFAULT_EVENTCATEGORY_NAME_ALL);
        saveUserEvent(username, ce.getCalendarId(), ce, false);
      }
      for (CalendarEvent ce : getSharedEventByCategory(username, eventCategoryId)) {
        ce.setEventCategoryId(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
        ce.setEventCategoryName(NewUserListener.DEFAULT_EVENTCATEGORY_NAME_ALL);
        saveEventToSharedCalendar(username, ce.getCalendarId(), ce, false);
      }
      for (CalendarEvent ce : getPublicEventByCategory(username, eventCategoryId)) {
        ce.setEventCategoryId(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL);
        ce.setEventCategoryName(NewUserListener.DEFAULT_EVENTCATEGORY_NAME_ALL);
        savePublicEvent(ce.getCalendarId(), ce, false);
      }
      eventCategoryNode.remove();
      eventCategoryHome.save();
      eventCategoryHome.getSession().save();

      Lock lock = getLock("EventCategories", username);
      lock.lock();
      try {
        userEventCategories.put(username, null);
      } finally {
        lock.unlock();
      }    }
  }

  /**
   * {@inheritDoc}
   */
  public EventCategory getEventCategory(Node eventCatNode) throws Exception {
    EventCategory eventCategory = new EventCategory();
    StringBuilder namePattern = new StringBuilder(128);
    namePattern.append(Utils.EXO_ID).append('|').append(Utils.EXO_NAME).append('|').append(Utils.EXO_DESCRIPTION);
    PropertyIterator it = eventCatNode.getProperties(namePattern.toString());
    while (it.hasNext()) {
      Property p = it.nextProperty();
      String name = p.getName();
      if (name.equals(Utils.EXO_ID)) {
        eventCategory.setId(p.getString());
      } else if (name.equals(Utils.EXO_NAME)) {
        eventCategory.setName(p.getString());
      } 
    } 
    return eventCategory;
  }

  public EventCategory getEventCategory(String username, String eventcategoryId) throws Exception {
    Node eventCategoryHome = getEventCategoryHome(username);
    return getEventCategory(eventCategoryHome.getNode(eventcategoryId));
  }

  public CalendarEvent getUserEvent(String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
    return getEvent(calendarNode.getNode(eventId));
  }

  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    for (String calendarId : calendarIds) {
      Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
      NodeIterator it = calendarNode.getNodes();
      while (it.hasNext()) {
        events.add(getEvent(it.nextNode()));
      }
    }
    return events;
  }

  public List<CalendarEvent> getPublicEventByCategory(String username, String eventCategoryId) throws Exception {
    Node publicCalendarHome = getPublicCalendarHome();
    QueryManager qm = publicCalendarHome.getSession().getWorkspace().getQueryManager();
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    try {
      Query query;
      QueryResult result;
      NodeIterator calIter = publicCalendarHome.getNodes();
      while (calIter.hasNext()) {
        StringBuilder queryString = new StringBuilder("/jcr:root").append(calIter.nextNode().getPath())
            .append("//element(*,exo:calendarEvent)[@exo:eventCategoryId='").append(eventCategoryId)
                                                                      .append("']");
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        result = query.execute();
        NodeIterator it = result.getNodes();
        while (it.hasNext()) {
          events.add(getEvent(it.nextNode()));
        }
      }
    } catch (Exception e) {
      log.error("Error occurred when querying public events from event category " + eventCategoryId,
                e);
    }
    return events;
  }

  public List<CalendarEvent> getSharedEventByCategory(String username, String eventCategoryId) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    try {
      if (getSharedCalendarHome().hasNode(username)) {
        PropertyIterator iterPro = getSharedCalendarHome().getNode(username).getReferences();
        while (iterPro.hasNext()) {
          try {
            Node calendar = iterPro.nextProperty().getParent();
            NodeIterator it = calendar.getNodes();
            while (it.hasNext()) {
              Node eventNode = it.nextNode();
              if (eventNode.hasProperty(Utils.EXO_EVENT_CATEGORYID))
                if (eventNode.getProperty(Utils.EXO_EVENT_CATEGORYID)
                             .getString()
                             .equals(eventCategoryId)) {
                  events.add(getEvent(eventNode));
                }
            }

          } catch (Exception e) {
            if (log.isDebugEnabled())
              log.debug(e);
          }
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e);
    }
    return events;
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEventByCategory(String username, String eventCategoryId) throws Exception {
    Node calendarHome = getUserCalendarHome(username);
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Query query;
    QueryResult result;
    NodeIterator calIter = calendarHome.getNodes();
    while (calIter.hasNext()) {
      StringBuilder queryString = new StringBuilder("/jcr:root").append(calIter.nextNode().getPath())
          .append("//element(*,exo:calendarEvent)[@exo:eventCategoryId='").append(eventCategoryId)
                                                                    .append("']");
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      NodeIterator it = result.getNodes();
      while (it.hasNext()) {
        events.add(getEvent(it.nextNode()));
      }
    }
    return events;
  }

  private CalendarEvent getEventById(Node calendarHome, String eventId) throws Exception {
    String queryString = new StringBuilder("/jcr:root").append(calendarHome.getPath())
        .append("//element(*,exo:calendarEvent)[@exo:id='").append(eventId).append("']").toString();
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString, Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    if (it.hasNext())
      return getEvent(it.nextNode());
    else
      return null;
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getEvent(String username, String eventId) throws Exception {
    Node calendarHome = getUserCalendarHome(username);
    return getEventById(calendarHome, eventId);
  }

  private List<CalendarEvent> getEventsByType(Node calendarHome, int type, EventQuery eventQuery) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    eventQuery.setCalendarPath(calendarHome.getPath());
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType());
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    CalendarEvent calEvent;
    while (it.hasNext()) {
      calEvent = getEvent(it.nextNode());
      calEvent.setCalType(String.valueOf(type));
      events.add(calEvent);
      if (eventQuery.getLimitedItems() == it.getPosition())
        break;
    }
    return events;
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    Node calendarHome = getUserCalendarHome(username);
    return getEventsByType(calendarHome, Calendar.TYPE_PRIVATE, eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
    event.setCalendarId(calendarId); // make sur the event is attached to the
                                     // calendar
    if (event.getReminders() != null && event.getReminders().size() > 0) {
      try {
        Node reminderFolder = getReminderFolder(event.getFromDateTime());
        saveEvent(calendarNode, event, reminderFolder, isNew);
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.debug(e);
      } finally {
      }
    } else {
      saveEvent(calendarNode, event, null, isNew);
    }
  }

  /**
   * Save a occurrence event
   * 
   * @param username
   * @param calendarId
   * @param event
   * @param isNew
   * @throws Exception
   */
  public void saveOccurrenceEvent(String username,
                                  String calendarId,
                                  CalendarEvent event,
                                  boolean isNew) throws Exception {
    int calType = getTypeOfCalendar(username, calendarId);
    if (calType == Calendar.TYPE_PRIVATE) {
      Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
      event.setCalendarId(calendarId);
      if (event.getReminders() != null && event.getReminders().size() > 0) {
        Node reminderFolder = getReminderFolder(event.getFromDateTime());
        saveOccurrenceEvent(calendarNode, event, reminderFolder, isNew);
      } else {
        saveOccurrenceEvent(calendarNode, event, null, isNew);
      }
      return;
    }

    if (calType == Calendar.TYPE_SHARED) {
      Node sharedCalendarHome = getSharedCalendarHome();
      if (sharedCalendarHome.hasNode(username)) {
        Node userNode = sharedCalendarHome.getNode(username);
        PropertyIterator iter = userNode.getReferences();
        Node calendar;
        while (iter.hasNext()) {
          calendar = iter.nextProperty().getParent();
          if (calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
            if (!canEdit(calendar, username)) {
              if (log.isDebugEnabled())
                log.debug("\n Do not have edit permission. \n");
              throw new AccessDeniedException();
            }
            Node reminderFolder = getReminderFolder(event.getFromDateTime());
            saveOccurrenceEvent(calendar, event, reminderFolder, isNew);
            break;
          }
        }
      }
      return;
    }

    if (calType == Calendar.TYPE_PUBLIC) {
      Node calendarNode = getPublicCalendarHome().getNode(calendarId);
      Node reminderFolder = getReminderFolder(event.getFromDateTime());
      saveOccurrenceEvent(calendarNode, event, reminderFolder, isNew);
      return;
    }
  }

  public void saveOccurrenceEvent(Node calendarNode,
                                  CalendarEvent event,
                                  Node reminderFolder,
                                  boolean isNew) throws Exception {
    try {
      if (isNew) {
        // convert a 'virtual' occurrence to 'exception' occurrence
        Node originalNode = null;
        try {
          originalNode = calendarNode.getNode(event.getId());
        } catch (PathNotFoundException e) {
          if (log.isDebugEnabled())
            log.debug("Original recurrence node not found", e);
        }
        event.setId("Event" + IdGenerator.generate());
        event.setRepeatType(CalendarEvent.RP_NOREPEAT);

        // set reference to original node
        if (originalNode != null) {
          event.setOriginalReference(originalNode.getUUID());
        }

        event.setIsExceptionOccurrence(true);
        event.setRepeatInterval(0);
        event.setRepeatCount(0);
        event.setRepeatUntilDate(null);
        event.setRepeatByDay(null);
        event.setRepeatByMonthDay(null);

        saveEvent(calendarNode, event, reminderFolder, true);
      } else {
        event.setRepeatType(CalendarEvent.RP_NOREPEAT);
        event.setIsExceptionOccurrence(true);
        event.setRepeatByDay(null);
        event.setRepeatCount(0);
        event.setRepeatInterval(0);
        event.setRepeatUntilDate(null);
        event.setRepeatByMonthDay(null);
        saveEvent(calendarNode, event, reminderFolder, false);
      }
    } catch (Exception e) {
      log.error("Error occurred when saving occurrence event", e);
    }
  }

  public void removeOccurrenceInstance(String username, CalendarEvent occurrence) throws Exception {
    String eventId = occurrence.getId();
    String calendarId = occurrence.getCalendarId();
    int calType = Integer.parseInt(occurrence.getCalType());
    String recurId = occurrence.getRecurrenceId();
    CalendarEvent originalEvent = null;

    // get the original event
    if (calType == Calendar.TYPE_PRIVATE)
      originalEvent = getUserEvent(username, calendarId, eventId);
    else if (calType == Calendar.TYPE_PUBLIC)
      originalEvent = getGroupEvent(calendarId, eventId);
    else if (calType == Calendar.TYPE_SHARED)
      originalEvent = getSharedEvent(username, calendarId, eventId);

    // then update the exludeId property: add recurId to this list
    String[] excludeId;
    if (originalEvent.getExcludeId() == null) {
      excludeId = new String[] { recurId };
    } else {
      List<String> excId = new ArrayList<String>(Arrays.asList(originalEvent.getExcludeId()));
      excId.add(recurId);
      excludeId = excId.toArray(new String[0]);
    }
    originalEvent.setExcludeId(excludeId);

    if (calType == Calendar.TYPE_PRIVATE) {
      saveUserEvent(username, calendarId, originalEvent, false);
      return;
    }
    if (calType == Calendar.TYPE_PUBLIC) {
      savePublicEvent(calendarId, originalEvent, false);
      return;
    }
    if (calType == Calendar.TYPE_SHARED) {
      saveEventToSharedCalendar(username, calendarId, originalEvent, false);
      return;
    }
  }

  public void removeRecurrenceSeries(String username, CalendarEvent originalEvent) throws Exception {
    int calType = Integer.parseInt(originalEvent.getCalType());
    if (originalEvent.getRepeatType().equals(CalendarEvent.RP_NOREPEAT))
      return;

    List<CalendarEvent> exceptions = getExceptionEvents(username, originalEvent);
    if (exceptions != null && exceptions.size() > 0) {
      for (CalendarEvent exception : exceptions) {
        removeReference(exception);  // remove reference to original event to avoid ReferentialIntegrityException 
      }
    }

    // delete original node
    if (calType == Calendar.TYPE_PRIVATE) {
      removeUserEvent(username, originalEvent.getCalendarId(), originalEvent.getId());
      return;
    }
    if (calType == Calendar.TYPE_PUBLIC) {
      removePublicEvent(originalEvent.getCalendarId(), originalEvent.getId());
      return;
    }
    if (calType == Calendar.TYPE_SHARED) {
      removeSharedEvent(username, originalEvent.getCalendarId(), originalEvent.getId());
      return;
    }
  }
  // removes reference of an exception event to the original event 
  private void removeReference(CalendarEvent exceptionEvent) throws Exception {
    Node calendarApp = Utils.getPublicServiceHome(Utils.createSystemProvider());
    QueryManager queryManager = calendarApp.getSession().getWorkspace().getQueryManager();
    String sql = "select * from exo:repeatCalendarEvent where exo:id=" + "\'" + exceptionEvent.getId() + "\'";
    Query query = queryManager.createQuery(sql, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator nodesIt = result.getNodes();
    if(nodesIt.hasNext()) {
      Node eventNode = nodesIt.nextNode();
      eventNode.setProperty(Utils.EXO_ORIGINAL_REFERENCE, (Value)null);
      eventNode.getSession().save();
    }
  }

  public Node getCalendarEventNode(String username,
                                   String calType,
                                   String calendarId,
                                   String eventId) throws Exception {
    Node eventNode = null;
    try {
      if (String.valueOf(Calendar.TYPE_PRIVATE).equals(calType))
        eventNode = getUserCalendarHome(username).getNode(calendarId).getNode(eventId);
      else if (String.valueOf(Calendar.TYPE_PUBLIC).equals(calType))
        eventNode = getPublicCalendarHome().getNode(calendarId).getNode(eventId);
      else if (String.valueOf(Calendar.TYPE_SHARED).equals(calType)) {
        Node sharedCalendarHome = getSharedCalendarHome();
        if (sharedCalendarHome.hasNode(username)) {
          PropertyIterator iter = sharedCalendarHome.getNode(username).getReferences();
          Node calendar;
          while (iter.hasNext()) {
            calendar = iter.nextProperty().getParent();
            if (!calendarId.equals(calendar.getProperty(Utils.EXO_ID)))
              continue;
            if (calendar.hasNode(eventId)) {
              eventNode = calendar.getNode(eventId);
              break;
            }
          }
        }
      }
      return eventNode;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Exception occurs when get calendar event node", e);
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
    if (calendarNode.hasNode(eventId)) {
      Node eventNode = calendarNode.getNode(eventId);
      CalendarEvent event = getEvent(eventNode);
      // Need to use system session
      try {
        Node eventFolder = getEventFolder(event.getFromDateTime());
        syncRemoveEvent(eventFolder, event.getId());
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.debug(e);
      }
      removeReminder(eventNode);
      eventNode.remove();
      calendarNode.save();
      calendarNode.getSession().save();
      calendarNode.refresh(true);
      return event;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void removeReminder(Node eventNode) throws Exception {
    if (eventNode.hasProperty(Utils.EXO_FROM_DATE_TIME)) {
      try {
        Node reminders = getReminderFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME)
                                                    .getDate()
                                                    .getTime());
        try {
          reminders.getNode(eventNode.getName()).remove();
          reminders.save();
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Failed to remove reminder for an event", e);
          }
        }
        Node events = reminders.getParent().getNode(Utils.CALENDAR_REMINDER);
        if (events != null && events.hasNode(eventNode.getName())) {
          if (events.hasNode(eventNode.getName())) {
            events.getNode(eventNode.getName()).remove();
            if (!reminders.isNew())
              reminders.save();
            else
              reminders.getSession().save();
          }
        }
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.debug(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId);
    CalendarEvent calEvent = getEvent(calendarNode.getNode(eventId));
    calEvent.setCalType(String.valueOf(Calendar.TYPE_PUBLIC));
    return calEvent;
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getGroupEvent(String eventId) throws Exception {
    Node calendarHome = getPublicCalendarHome();
    return getEventById(calendarHome, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    for (String calendarId : calendarIds) {
      Node calendarNode = getPublicCalendarHome().getNode(calendarId);
      NodeIterator it = calendarNode.getNodes();
      while (it.hasNext()) {
        CalendarEvent event = getEvent(it.nextNode());
        event.setCalType(String.valueOf(Calendar.TYPE_PUBLIC));
        events.add(event);
      }
    }
    return events;
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    Node calendarHome = getPublicCalendarHome();
    List<CalendarEvent> events = getEventsByType(calendarHome, Calendar.TYPE_PUBLIC, eventQuery);
    return events;
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId);
    event.setCalendarId(calendarId);
    Node reminderFolder = getReminderFolder(event.getFromDateTime());
    saveEvent(calendarNode, event, reminderFolder, isNew);

    groupCalendarEventCache.clearCache();
    groupCalendarRecurrentEventCache.clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId);
    if (calendarNode.hasNode(eventId)) {
      Node eventNode = calendarNode.getNode(eventId);
      CalendarEvent event = getEvent(eventNode);
      removeReminder(eventNode);
      eventNode.remove();
      calendarNode.save();
      calendarNode.getSession().save();
      calendarNode.refresh(true);
      try {
        Node eventFolder = getEventFolder(event.getFromDateTime());
        syncRemoveEvent(eventFolder, eventId);
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.debug(e.getMessage());
      }

      groupCalendarEventCache.clearCache();
      groupCalendarRecurrentEventCache.clearCache();

      return event;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getEvent(Node eventNode) throws Exception {
    CalendarEvent event = new CalendarEvent();
    event = EventPageListQuery.getEventFromNode(event,
                                                eventNode,
                                                getReminderFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME)
                                                                  .getDate()
                                                                  .getTime()));
    StringBuilder namePattern = new StringBuilder(128);
    namePattern.append(Utils.EXO_RECURRENCE_ID).append('|').append(Utils.EXO_IS_EXCEPTION).append('|').append(Utils.EXO_REPEAT_UNTIL)
         .append('|').append(Utils.EXO_REPEAT_COUNT).append('|').append(Utils.EXO_ORIGINAL_REFERENCE).append('|')
         .append(Utils.EXO_REPEAT_INTERVAL).append('|').append(Utils.EXO_EXCLUDE_ID).append('|').append(Utils.EXO_REPEAT_BYDAY)
         .append('|').append(Utils.EXO_REPEAT_BYMONTHDAY);
    PropertyIterator it = eventNode.getProperties(namePattern.toString());
    while (it.hasNext()) {
      Property p = it.nextProperty();
      String name = p.getName();
      if (name.equals(Utils.EXO_RECURRENCE_ID)) {
        event.setRecurrenceId(p.getString());
      } else if (name.equals(Utils.EXO_IS_EXCEPTION)) {
        event.setIsExceptionOccurrence(p.getBoolean());
      } else if (name.equals(Utils.EXO_REPEAT_UNTIL)) {
        event.setRepeatUntilDate(p.getDate().getTime());
      } else if (name.equals(Utils.EXO_REPEAT_COUNT)) {
        event.setRepeatCount(p.getLong());
      } else if (name.equals(Utils.EXO_ORIGINAL_REFERENCE)) {
        event.setOriginalReference(p.getString());
      } else if (name.equals(Utils.EXO_REPEAT_INTERVAL)) {
        event.setRepeatInterval(p.getLong());
      } else if (name.equals(Utils.EXO_EXCLUDE_ID)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setExcludeId(new String[] { values[0].getString() });
        } else {
          String[] excludeIds = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            excludeIds[i] = values[i].getString();
          }
          event.setExcludeId(excludeIds);
        }
      } else if (name.equals(Utils.EXO_REPEAT_BYDAY)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setRepeatByDay(new String[] { values[0].getString() });
        } else {
          String[] byDays = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            byDays[i] = values[i].getString();
          }
          event.setRepeatByDay(byDays);
        }
      } else if (name.equals(Utils.EXO_REPEAT_BYMONTHDAY)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setRepeatByMonthDay(new long[] { values[0].getLong() });
        } else {
          long[] byMonthDays = new long[values.length];
          for (int i = 0; i < values.length; i++) {
            byMonthDays[i] = values[i].getLong();
          }
          event.setRepeatByMonthDay(byMonthDays);
        }
      }             
    }
    
    String activitiId = ActivityTypeUtils.getActivityId(eventNode) ;
    if(activitiId != null) {
      event.setActivityId(ActivityTypeUtils.getActivityId(eventNode));
    }
    return event;
  }

  /**
   * {@inheritDoc}
   */
  public void saveEvent(Node calendarNode, CalendarEvent event, Node reminderFolder, boolean isNew) throws Exception {
    Node eventNode;
    Boolean isRepeatNode = false;
    if (isNew) {
      eventNode = calendarNode.addNode(event.getId(), Utils.EXO_CALENDAR_EVENT);
      eventNode.setProperty(Utils.EXO_ID, event.getId());
      if (Utils.isRepeatEvent(event)) {
        eventNode.addMixin(Utils.EXO_REPEAT_CALENDAR_EVENT);
        eventNode.addMixin(Utils.MIX_REFERENCEABLE);
        isRepeatNode = true;
      }

      if (Utils.isExceptionOccurrence(event)) {
        eventNode.addMixin(Utils.EXO_REPEAT_CALENDAR_EVENT);
        isRepeatNode = true;
      }
    } else {
      try {
        eventNode = calendarNode.getNode(event.getId());
        if (Utils.isRepeatEvent(event)) {
          if (!eventNode.isNodeType(Utils.EXO_REPEAT_CALENDAR_EVENT))
            eventNode.addMixin(Utils.EXO_REPEAT_CALENDAR_EVENT);
          if (!eventNode.isNodeType(Utils.MIX_REFERENCEABLE))
            eventNode.addMixin(Utils.MIX_REFERENCEABLE);
          isRepeatNode = true;
        }

      } catch (PathNotFoundException e) {
        eventNode = calendarNode.addNode(event.getId(), Utils.EXO_CALENDAR_EVENT);
        eventNode.setProperty(Utils.EXO_ID, event.getId());
      }
      try {
        removeReminder(eventNode);
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.debug(e);
      } finally {
      }
    }

    eventNode.setProperty(Utils.EXO_SUMMARY, event.getSummary());
    eventNode.setProperty(Utils.EXO_CALENDAR_ID, event.getCalendarId());
    eventNode.setProperty(Utils.EXO_EVENT_CATEGORYID, event.getEventCategoryId());
    eventNode.setProperty(Utils.EXO_EVENT_CATEGORY_NAME, event.getEventCategoryName());
    eventNode.setProperty(Utils.EXO_DESCRIPTION, event.getDescription());
    eventNode.setProperty(Utils.EXO_LOCATION, event.getLocation());
    eventNode.setProperty(Utils.EXO_TASK_DELEGATOR, event.getTaskDelegator());

    GregorianCalendar startTime = Utils.getInstanceTempCalendar();
    GregorianCalendar endTime = Utils.getInstanceTempCalendar();
    if (event.getFromDateTime() !=null) {
      startTime.setTime(event.getFromDateTime());
    }
    eventNode.setProperty(Utils.EXO_FROM_DATE_TIME, startTime);
    eventNode.getSession().save();
    if (event.getToDateTime() != null) {
      endTime.setTime(event.getToDateTime());
    }
    eventNode.setProperty(Utils.EXO_TO_DATE_TIME, endTime);
    eventNode.getSession().save();
    eventNode.setProperty(Utils.EXO_EVENT_TYPE, event.getEventType());
    eventNode.setProperty(Utils.EXO_REPEAT, event.getRepeatType());
    eventNode.setProperty(Utils.EXO_PRIORITY, event.getPriority());
    eventNode.setProperty(Utils.EXO_IS_PRIVATE, event.isPrivate());
    eventNode.setProperty(Utils.EXO_EVENT_STATE, event.getEventState());
    if (event.getInvitation() == null)
      event.setInvitation(new String[] {});
    eventNode.setProperty(Utils.EXO_INVITATION, event.getInvitation());
    if (event.getParticipant() == null)
      event.setParticipant(new String[] {});
    eventNode.setProperty(Utils.EXO_PARTICIPANT, event.getParticipant());
    List<Reminder> reminders = event.getReminders();
    if (reminders != null && !reminders.isEmpty()) {
      for (Reminder rm : reminders) {
        rm.setFromDateTime(event.getFromDateTime());
        addReminder(eventNode, reminderFolder, rm);
      }
    }
    if (eventNode.hasNode(Utils.ATTACHMENT_NODE)) {
      while (eventNode.getNodes().hasNext()) {
        eventNode.getNodes().nextNode().remove();
      }
      eventNode.save();
    }
    List<Attachment> attachments = event.getAttachment();
    if (attachments != null) {
      for (Attachment att : attachments) {
        addAttachment(eventNode, att, isNew);
      }
    }

    eventNode.setProperty(Utils.EXO_MESSAGE, event.getMessage());
    eventNode.setProperty(Utils.EXO_SEND_OPTION, event.getSendOption());
    if (event.getParticipantStatus() == null)
      event.setParticipantStatus(new String[] {});
    eventNode.setProperty(Utils.EXO_PARTICIPANT_STATUS, event.getParticipantStatus());

    if (isRepeatNode) {
      if (event.getRecurrenceId() != null)
        eventNode.setProperty(Utils.EXO_RECURRENCE_ID, event.getRecurrenceId());
      else
        eventNode.setProperty(Utils.EXO_RECURRENCE_ID, Utils.EMPTY_STR);

      if (event.getIsExceptionOccurrence() != null)
        eventNode.setProperty(Utils.EXO_IS_EXCEPTION, event.getIsExceptionOccurrence());
      else
        eventNode.setProperty(Utils.EXO_IS_EXCEPTION, (Value) null);

      if (event.getExcludeId() != null && event.getExcludeId().length > 0)
        eventNode.setProperty(Utils.EXO_EXCLUDE_ID, event.getExcludeId());
      else
        eventNode.setProperty(Utils.EXO_EXCLUDE_ID, (Value[]) null);

      if (event.getRepeatUntilDate() != null) {
        GregorianCalendar dateRepeatTo = Utils.getInstanceTempCalendar();
        dateRepeatTo.setTime(event.getRepeatUntilDate());
        eventNode.setProperty(Utils.EXO_REPEAT_UNTIL, dateRepeatTo);
      } else {
        eventNode.setProperty(Utils.EXO_REPEAT_UNTIL, (Value) null);
      }

      if (event.getOriginalReference() != null)
        eventNode.setProperty(Utils.EXO_ORIGINAL_REFERENCE, event.getOriginalReference());
      else
        eventNode.setProperty(Utils.EXO_ORIGINAL_REFERENCE, (Value) null);

      eventNode.setProperty(Utils.EXO_REPEAT_COUNT, event.getRepeatCount());
      eventNode.setProperty(Utils.EXO_REPEAT_INTERVAL, event.getRepeatInterval());

      if (event.getRepeatByDay() != null)
        eventNode.setProperty(Utils.EXO_REPEAT_BYDAY, event.getRepeatByDay());
      else
        eventNode.setProperty(Utils.EXO_REPEAT_BYDAY, (Value[]) null);

      if (event.getRepeatByMonthDay() != null) {
        long[] monthDays = event.getRepeatByMonthDay();
        String[] byMonthDay = new String[monthDays.length];
        for (int i = 0; i < monthDays.length; i++)
          byMonthDay[i] = String.valueOf(monthDays[i]);
        eventNode.setProperty(Utils.EXO_REPEAT_BYMONTHDAY, byMonthDay);
      } else {
        eventNode.setProperty(Utils.EXO_REPEAT_BYMONTHDAY, (Value[]) null);
      }

      // if event is original repeating event, calculate the end-date to save
      String repeatType = event.getRepeatType();
      if (!Utils.isEmpty(repeatType) && !repeatType.equals(CalendarEvent.RP_NOREPEAT)) {
        Date repeatFinish = calculateRecurrenceFinishDate(event);
        if (repeatFinish != null) {
          GregorianCalendar finishRepeatDate = Utils.getInstanceTempCalendar();
          finishRepeatDate.setTime(repeatFinish);
          eventNode.setProperty(Utils.EXO_REPEAT_FINISH_DATE, finishRepeatDate);
        }
      }
    }
    if(event.getActivityId() != null) {
      ActivityTypeUtils.attachActivityId(eventNode, event.getActivityId());
    }
    calendarNode.getSession().save();
    
    // only need for event, because we just get free/busy time of events in schedule tab.
    if(CalendarEvent.TYPE_EVENT.equals(event.getEventType())) {
      addEvent(event);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addReminder(Node eventNode, Node reminderFolder, Reminder reminder) throws Exception {
    Node reminderNode;
    Node catNode;
    try {
      catNode = reminderFolder.getNode(eventNode.getName());
    } catch (Exception e) {
      catNode = reminderFolder.addNode(eventNode.getName(), Utils.NT_UNSTRUCTURED);
    }
    try {
      reminderNode = catNode.getNode(reminder.getId());
    } catch (Exception e) {
      reminderNode = catNode.addNode(reminder.getId(), Utils.EXO_REMINDER);
    }
    reminderNode.setProperty(Utils.EXO_EVENT_ID, eventNode.getName());
    reminderNode.setProperty(Utils.EXO_ALARM_BEFORE, reminder.getAlarmBefore());
    reminderNode.setProperty(Utils.EXO_TIME_INTERVAL, reminder.getRepeatInterval());
    reminderNode.setProperty(Utils.EXO_REMINDER_TYPE, reminder.getReminderType());
    reminderNode.setProperty(Utils.EXO_EMAIL, reminder.getEmailAddress());
    reminderNode.setProperty(Utils.EXO_IS_REPEAT, reminder.isRepeat());
    reminderNode.setProperty(Utils.EXO_IS_OVER, false);
    if (reminder.getReminderType().equals(Reminder.TYPE_POPUP)) {
      reminderNode.setProperty(Utils.EXO_OWNER, reminder.getReminderOwner());
    }
    java.util.Calendar fromTime = new GregorianCalendar();
    java.util.Calendar remindTime = new GregorianCalendar();
    java.util.Calendar toTime = new GregorianCalendar();

    if (reminder.getFromDateTime() != null) {
      fromTime.setTime(reminder.getFromDateTime());
      reminderNode.setProperty(Utils.EXO_FROM_DATE_TIME, fromTime);
      long time = reminder.getFromDateTime().getTime() - (reminder.getAlarmBefore() * 60 * 1000);
      remindTime.setTimeInMillis(time);
      reminderNode.setProperty(Utils.EXO_REMINDER_DATE, remindTime);
    }
    StringBuilder summary = new StringBuilder("Type      : ");
    summary.append(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString()).append("<br>");
    summary.append("Summary: ");
    summary.append(eventNode.getProperty(Utils.EXO_SUMMARY).getString()).append("<br>");
    summary.append("Description: ");
    if (eventNode.hasProperty(Utils.EXO_DESCRIPTION))
      summary.append(eventNode.getProperty(Utils.EXO_DESCRIPTION).getString());
    summary.append("<br>");
    summary.append("Location   : ");
    if (eventNode.hasProperty(Utils.EXO_LOCATION))
      summary.append(eventNode.getProperty(Utils.EXO_LOCATION).getString());
    summary.append("<br>");
    if (!Utils.isEmpty(reminder.getReminderOwner())) {
      try {
        remindTime.setTimeZone(TimeZone.getTimeZone(getCalendarSetting(reminder.getReminderOwner()).getTimeZone()));
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug(e);
        }
      }
    }
    fromTime.setTime(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime());
    appendDateToSummary("From       : ", fromTime, summary);

    toTime.setTime(eventNode.getProperty(Utils.EXO_TO_DATE_TIME).getDate().getTime());
    appendDateToSummary("To         : ", toTime, summary);

    reminderNode.setProperty(Utils.EXO_DESCRIPTION, summary.toString());
    reminderNode.setProperty(Utils.EXO_SUMMARY, eventNode.getProperty(Utils.EXO_SUMMARY)
                                                         .getString());
    if (!reminderFolder.isNew())
      reminderFolder.save();
    else
      reminderFolder.getSession().save();
  }

  private void appendDateToSummary(String label, java.util.Calendar cal, StringBuilder summary) {
    summary.append(label).append(cal.get(java.util.Calendar.HOUR_OF_DAY)).append(Utils.COLON);
    summary.append(cal.get(java.util.Calendar.MINUTE)).append(" - ");
    summary.append(cal.get(java.util.Calendar.DATE)).append(Utils.SLASH);
    summary.append(cal.get(java.util.Calendar.MONTH) + 1).append(Utils.SLASH);
    summary.append(cal.get(java.util.Calendar.YEAR)).append("<br>");
  }

  /**
   * creates a virtual event for a new event in a public folder of storage
   * this virtual event is search-able (by XPATH query) and used to find the free/busy time in Schedule tab 
   */
  public void addEvent(CalendarEvent event) throws Exception {
    Node eventFolder = getEventFolder(event.getFromDateTime());
    Node publicEvent;
    int fromDate;
    int toDate;
    syncRemoveEvent(eventFolder, event.getId());
    CalendarEvent ev = new CalendarEvent();
    publicEvent = eventFolder.addNode(ev.getId(), Utils.EXO_CALENDAR_PUBLIC_EVENT);
    publicEvent.setProperty(Utils.EXO_ID, ev.getId());
    publicEvent.setProperty(Utils.EXO_ROOT_EVENT_ID, event.getId());
    publicEvent.setProperty(Utils.EXO_EVENT_TYPE, event.getEventType());
    publicEvent.setProperty(Utils.EXO_CALENDAR_ID, event.getCalendarId());
    java.util.Calendar startTime = Utils.getInstanceTempCalendar();
    java.util.Calendar endTime = Utils.getInstanceTempCalendar();

    startTime.setTime(event.getFromDateTime());
    fromDate = startTime.get(java.util.Calendar.DAY_OF_YEAR);
    publicEvent.setProperty(Utils.EXO_FROM_DATE_TIME, startTime);
    publicEvent.getSession().save();
    publicEvent.setProperty(Utils.EXO_EVENT_STATE, event.getEventState());
    endTime.setTime(event.getToDateTime());
    toDate = endTime.get(java.util.Calendar.DAY_OF_YEAR);
    if (toDate > fromDate) {
      java.util.Calendar tmpTime = Utils.getInstanceTempCalendar();
      tmpTime.setTime(event.getFromDateTime());
      tmpTime.set(java.util.Calendar.HOUR_OF_DAY, 0);
      tmpTime.set(java.util.Calendar.MINUTE, 0);
      tmpTime.set(java.util.Calendar.SECOND, 0);
      tmpTime.set(java.util.Calendar.MILLISECOND, 0);
      tmpTime.setTimeInMillis(tmpTime.getTimeInMillis() + (24 * 60 * 60 * 1000) - 1000);
      publicEvent.setProperty(Utils.EXO_TO_DATE_TIME, tmpTime);
      publicEvent.getSession().save();
    } else {
      publicEvent.setProperty(Utils.EXO_TO_DATE_TIME, endTime);
      publicEvent.getSession().save();
    }
    publicEvent.setProperty(Utils.EXO_PARTICIPANT, event.getParticipant());
    try {
      if (!eventFolder.isNew())
        eventFolder.save();
      else
        eventFolder.getSession().save();
    } catch (Exception e) {
      eventFolder.getSession().refresh(true);
      eventFolder.getSession().save();
    }
    try {
      if (toDate > fromDate) {
        java.util.Calendar cal = Utils.getInstanceTempCalendar();
        cal.setTime(event.getFromDateTime());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        for (int i = fromDate + 1; i <= toDate; i++) {
          cal.roll(java.util.Calendar.DAY_OF_YEAR, true);
          Node dateFolder = getEventFolder(cal.getTime());
          ev = new CalendarEvent();
          eventFolder.getSession()
                     .getWorkspace()
                     .copy(publicEvent.getPath(), dateFolder.getPath() + Utils.SLASH + ev.getId());
          dateFolder.getSession().save();
          if (i <= toDate) {
            Node newEvent = dateFolder.getNode(ev.getId());
            newEvent.setProperty(Utils.EXO_ID, ev.getId());
            newEvent.setProperty(Utils.EXO_FROM_DATE_TIME, cal);
            newEvent.getSession().save();
            java.util.Calendar tmpCal = Utils.getInstanceTempCalendar();
            if (i == toDate)
              tmpCal.setTime(event.getToDateTime());
            else
              tmpCal.setTimeInMillis(cal.getTimeInMillis() + (24 * 60 * 60 * 1000) - 1000);
            newEvent.setProperty(Utils.EXO_TO_DATE_TIME, tmpCal);
            newEvent.getSession().save();
          }
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void syncRemoveEvent(Node eventFolder, String rootEventId) throws Exception {
    QueryManager qm = eventFolder.getSession().getWorkspace().getQueryManager();
    StringBuilder queryString = new StringBuilder("/jcr:root")
        .append(eventFolder.getParent().getParent().getParent().getPath())
        .append("//element(*,exo:calendarPublicEvent)[@exo:rootEventId='").append(rootEventId)
                                                                    .append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    while (it.hasNext()) {
      it.nextNode().remove();
    }
    eventFolder.getSession().save();
    eventFolder.refresh(true);
  }

  /**
   * {@inheritDoc}
   */
  public Node getReminderFolder(Date fromDate) throws Exception {
    Node publicApp = getPublicCalendarServiceHome();
    Node dateFolder = getDateFolder(publicApp, fromDate);
    try {
      return dateFolder.getNode(Utils.CALENDAR_REMINDER);
    } catch (PathNotFoundException pnfe) {
      try {
        dateFolder.addNode(Utils.CALENDAR_REMINDER, Utils.NT_UNSTRUCTURED);
        if (dateFolder.isNew())
          dateFolder.getSession().save();
        else
          dateFolder.save();
      } catch (Exception e) {
        dateFolder.refresh(false);
      }
      return dateFolder.getNode(Utils.CALENDAR_REMINDER);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getEventFolder(Date fromDate) throws Exception {
    Node publicApp = getPublicCalendarServiceHome();
    Node dateFolder = getDateFolder(publicApp, fromDate);
    try {
      return dateFolder.getNode(CALENDAR_EVENT);
    } catch (Exception e) {
      dateFolder.addNode(CALENDAR_EVENT, Utils.NT_UNSTRUCTURED);
      getPublicRoot().getSession().save();
      return dateFolder.getNode(CALENDAR_EVENT);

    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getDateFolder(Node publicApp, Date date) throws Exception {
    if (date instanceof DateTime) {
      date = new Date(date.getTime());
    }
    java.util.Calendar fromCalendar = Utils.getInstanceTempCalendar();
    fromCalendar.setTime(date);
    Node yearNode;
    Node monthNode;
    String year = "Y" + String.valueOf(fromCalendar.get(java.util.Calendar.YEAR));
    String month = "M" + String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1);
    String day = "D" + String.valueOf(fromCalendar.get(java.util.Calendar.DATE));
    try {
      yearNode = publicApp.getNode(year);
    } catch (Exception e) {
      yearNode = publicApp.addNode(year, Utils.NT_UNSTRUCTURED);
    }
    try {
      monthNode = yearNode.getNode(month);
    } catch (Exception e) {
      monthNode = yearNode.addNode(month, Utils.NT_UNSTRUCTURED);
    }
    try {
      return monthNode.getNode(day);
    } catch (Exception e) {
      return monthNode.addNode(day, Utils.NT_UNSTRUCTURED);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addAttachment(Node eventNode, Attachment attachment, boolean isNew) throws Exception {
    Node attachHome;
    Node attachNode;
    // fix load image on IE6 UI
    ExtendedNode extNode = (ExtendedNode) eventNode;
    if (extNode.canAddMixin("exo:privilegeable"))
      extNode.addMixin("exo:privilegeable");
    String[] arrayPers = { PermissionType.READ, PermissionType.ADD_NODE,
        PermissionType.SET_PROPERTY, PermissionType.REMOVE };
    extNode.setPermission(IdentityConstants.ANY, arrayPers);
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries();
    for (AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers);
    }
    try {
      attachHome = eventNode.getNode(Utils.ATTACHMENT_NODE);
    } catch (Exception e) {
      attachHome = eventNode.addNode(Utils.ATTACHMENT_NODE, Utils.NT_UNSTRUCTURED);
    }
    String name = attachment.getId().substring(attachment.getId().lastIndexOf(Utils.SLASH) + 1);
    try {
      attachNode = attachHome.getNode(name);
    } catch (Exception e) {
      attachNode = attachHome.addNode(name, Utils.EXO_EVEN_TATTACHMENT);
    }
    attachNode.setProperty(Utils.EXO_FILE_NAME, attachment.getName());
    Node nodeContent = null;
    try {
      nodeContent = attachNode.getNode(Utils.JCR_CONTENT);
    } catch (Exception e) {
      nodeContent = attachNode.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
    }
    nodeContent.setProperty(Utils.JCR_LASTMODIFIED, java.util.Calendar.getInstance()
                                                                      .getTimeInMillis());
    nodeContent.setProperty(Utils.JCR_MIMETYPE, attachment.getMimeType());
    nodeContent.setProperty(Utils.JCR_DATA, attachment.getInputStream());
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {
    Node calendarHome = getUserCalendarServiceHome(username);
    addCalendarSetting(calendarHome, setting);
    Session session = calendarHome.getSession();
    session.save();
    Lock lock = getLock("UserCalendarSetting", username);
    lock.lock();
    try {
      userCalendarSetting.put(username, null);
    } finally {
      lock.unlock();
    }
  }

  private void saveCalendarSetting(CalendarSetting setting, String username) throws Exception {
    Node calendarHome = getUserCalendarServiceHome(username);
    addCalendarSetting(calendarHome, setting);
    calendarHome.save();
    Lock lock = getLock("UserCalendarSetting", username);
    lock.lock();
    try {
      userCalendarSetting.put(username, null);
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addCalendarSetting(Node calendarHome, CalendarSetting setting) throws Exception {
    Node settingNode;
    try {
      settingNode = calendarHome.getNode(CALENDAR_SETTING);
    } catch (Exception e) {
      settingNode = calendarHome.addNode(CALENDAR_SETTING, Utils.EXO_CALENDAR_SETTING);
    }
    settingNode.setProperty(Utils.EXO_VIEW_TYPE, setting.getViewType());
    settingNode.setProperty(Utils.EXO_TIME_INTERVAL, setting.getTimeInterval());
    settingNode.setProperty(Utils.EXO_WEEK_START_ON, setting.getWeekStartOn());
    settingNode.setProperty(Utils.EXO_DATE_FORMAT, setting.getDateFormat());
    settingNode.setProperty(Utils.EXO_TIME_FORMAT, setting.getTimeFormat());
    settingNode.setProperty(Utils.EXO_TIMEZONE, setting.getTimeZone());
    settingNode.setProperty(Utils.EXO_IS_SHOW_WORKING_TIME, setting.isShowWorkingTime());
    if (setting.isShowWorkingTime()) {
      settingNode.setProperty(Utils.EXO_WORKING_BEGIN, setting.getWorkingTimeBegin());
      settingNode.setProperty(Utils.EXO_WORKING_END, setting.getWorkingTimeEnd());
    }
    settingNode.setProperty(Utils.EXO_BASE_URL, setting.getBaseURL());
    settingNode.setProperty(Utils.EXO_PRIVATE_CALENDARS, setting.getFilterPrivateCalendars());
    settingNode.setProperty(Utils.EXO_PUBLIC_CALENDARS, setting.getFilterPublicCalendars());
    settingNode.setProperty(Utils.EXO_SHARED_CALENDARS, setting.getFilterSharedCalendars());
    settingNode.setProperty(Utils.EXO_SHARED_CALENDAR_COLORS, setting.getSharedCalendarsColors());
    settingNode.setProperty(Utils.EXO_SEND_OPTION, setting.getSendOption());
  }

  /**
   * {@inheritDoc}
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception {
    CalendarSetting calendarSetting;
    Lock lock = getLock("UserCalendarSetting", username);
    lock.lock();
    try {
      calendarSetting = userCalendarSetting.get(username);
      if (calendarSetting == null) {
        Node calendarHome = getUserCalendarServiceHome(username);
        calendarSetting = getCalendarSetting(calendarHome);
        if (calendarSetting == null) {
          calendarSetting = new CalendarSetting();
          addCalendarSetting(calendarHome, calendarSetting);
        }
        userCalendarSetting.put(username, calendarSetting);
      }
    } finally {
      lock.unlock();
    }

    return calendarSetting;
  }

  /**
   * @param calendarHome
   * @return
   * @throws Exception
   */
  private CalendarSetting getCalendarSetting(Node calendarHome) throws Exception {
    if (calendarHome.hasNode(CALENDAR_SETTING)) {
      CalendarSetting calendarSetting = new CalendarSetting();
      Node settingNode = calendarHome.getNode(CALENDAR_SETTING);
      StringBuilder namePattern = new StringBuilder(256);
      namePattern.append(Utils.EXO_VIEW_TYPE).append('|').append(Utils.EXO_TIME_INTERVAL).append('|').append(Utils.EXO_WEEK_START_ON)
           .append('|').append(Utils.EXO_DATE_FORMAT).append('|').append(Utils.EXO_TIME_FORMAT).append('|')
           .append(Utils.EXO_SEND_OPTION).append('|').append(Utils.EXO_BASE_URL).append('|')
           .append(Utils.EXO_TIMEZONE).append('|')
           .append(Utils.EXO_IS_SHOW_WORKING_TIME).append('|').append(Utils.EXO_WORKING_BEGIN).append('|').append(Utils.EXO_WORKING_END)
           .append('|').append(Utils.EXO_PRIVATE_CALENDARS).append('|').append(Utils.EXO_PUBLIC_CALENDARS)
           .append('|').append(Utils.EXO_SHARED_CALENDARS).append('|').append(Utils.EXO_SHARED_CALENDAR_COLORS);
      PropertyIterator it = settingNode.getProperties(namePattern.toString());
      String workingTimeBegin = null, workingTimeEnd = null;
      while (it.hasNext()) {
        Property p = it.nextProperty();
        String name = p.getName();
        if (name.equals(Utils.EXO_VIEW_TYPE)) {
          calendarSetting.setViewType(p.getString());
        } else if (name.equals(Utils.EXO_TIME_INTERVAL)) {
          calendarSetting.setTimeInterval(p.getLong());
        } else if (name.equals(Utils.EXO_WEEK_START_ON)) {
          calendarSetting.setWeekStartOn(p.getString());
        } else if (name.equals(Utils.EXO_DATE_FORMAT)) {
          calendarSetting.setDateFormat(p.getString());
        } else if (name.equals(Utils.EXO_TIME_FORMAT)) {
          calendarSetting.setTimeFormat(p.getString());
        } else if (name.equals(Utils.EXO_SEND_OPTION)) {
          calendarSetting.setSendOption(p.getString());
        } else if (name.equals(Utils.EXO_BASE_URL)) {
          calendarSetting.setBaseURL(p.getString());
        } else if (name.equals(Utils.EXO_TIMEZONE)) {
          calendarSetting.setTimeZone(p.getString());
        } else if (name.equals(Utils.EXO_IS_SHOW_WORKING_TIME)) {
          calendarSetting.setShowWorkingTime(p.getBoolean());
        } else if (name.equals(Utils.EXO_WORKING_BEGIN)) {
          workingTimeBegin = p.getString();
        } else if (name.equals(Utils.EXO_WORKING_END)) {
          workingTimeEnd = p.getString();
        } else if (name.equals(Utils.EXO_PRIVATE_CALENDARS)) {
          Value[] values = p.getValues();
          String[] calendars = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            calendars[i] = values[i].getString();
          }
          calendarSetting.setFilterPrivateCalendars(calendars);
        } else if (name.equals(Utils.EXO_PUBLIC_CALENDARS)) {
          Value[] values = p.getValues();
          String[] calendars = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            calendars[i] = values[i].getString();
          }
          calendarSetting.setFilterPublicCalendars(calendars);
        } else if (name.equals(Utils.EXO_SHARED_CALENDARS)) {
          Value[] values = p.getValues();
          String[] calendars = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            calendars[i] = values[i].getString();
          }
          calendarSetting.setFilterSharedCalendars(calendars);
        } else if (name.equals(Utils.EXO_SHARED_CALENDAR_COLORS)) {
          Value[] values = p.getValues();
          String[] calendarsColors = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            calendarsColors[i] = values[i].getString();
          }
          calendarSetting.setSharedCalendarsColors(calendarsColors);
        }
      }
      if (calendarSetting.isShowWorkingTime()) {
        calendarSetting.setWorkingTimeBegin(workingTimeBegin);
        calendarSetting.setWorkingTimeEnd(workingTimeEnd);
      }

      return calendarSetting;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void storeXML(String feedXML, Node rssHome, RssData rssData) throws Exception {
    Node rss;
    String rssNodeName = rssData.getName();
    if (rssHome.hasNode(rssNodeName))
      rss = rssHome.getNode(rssNodeName);
    else
      rss = rssHome.addNode(rssNodeName, Utils.EXO_RSS_DATA);
    rss.setProperty(Utils.EXO_BASE_URL, rssData.getUrl());
    rss.setProperty(Utils.EXO_TITLE, rssData.getTitle());
    rss.setProperty(Utils.EXO_CONTENT, new ByteArrayInputStream(feedXML.getBytes()));
  }


  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public void removeFeed(String username, String calendarId) throws Exception {
    Node rssHome = getRssHome(username);
    NodeIterator iter = rssHome.getNodes();
    List<String> removedFeedNodes = new ArrayList<String>();
    while (iter.hasNext()) {
      Node feedNode = iter.nextNode();
      if (feedNode.isNodeType(Utils.EXO_RSS_DATA)) {
        FeedData feedData = new FeedData();
        feedData.setTitle(feedNode.getProperty("exo:title").getString());
        StringBuilder url = new StringBuilder(feedNode.getProperty(Utils.EXO_BASE_URL).getString());
        url.append("/").append(PortalContainer.getCurrentPortalContainerName());
        url.append("/").append(feedNode.getSession().getWorkspace().getName());
        url.append("/").append(username);
        url.append("/").append(feedNode.getName());
        feedData.setUrl(url.toString());

        URL feedUrl = new URL(feedData.getUrl());
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));

        List entries = feed.getEntries();
        List<SyndEntry> listBefore = new ArrayList<SyndEntry>();
        listBefore.addAll(entries);
        for (int i = 0; i < listBefore.size(); i++) {
          SyndEntry entry = listBefore.get(i);
          String id = entry.getLink().substring(entry.getLink().lastIndexOf("/") + 1);
          if (id.contains(calendarId)) {
            listBefore.remove(i);
            i--;
          }
        }
        if (listBefore.size() == 0) {
          removedFeedNodes.add(feedNode.getName());
        } else {
          feed.setEntries(listBefore);
          SyndFeedOutput output = new SyndFeedOutput();
          String feedXML = output.outputString(feed);
          feedXML = StringUtils.replace(feedXML, "&amp;", "&");
          feedNode.setProperty(Utils.EXO_CONTENT, new ByteArrayInputStream(feedXML.getBytes()));
          feedNode.save();
        }
      }
    }
    if (removedFeedNodes.size() > 0) {
      for (String s : removedFeedNodes) {
        if (rssHome.getNode(s) != null) {
          rssHome.getNode(s).remove();
        }
      }
      rssHome.getSession().save();
    }

    // remove calDav
    if (rssHome.hasNode(Utils.CALDAV_NODE)) {
      iter = rssHome.getNode(Utils.CALDAV_NODE).getNodes();
      while (iter.hasNext()) {
        Node rssCal = iter.nextNode();
        if (rssCal.getPath().contains(calendarId)) {
          rssCal.remove();
        }
      }
    }
    // remove RSS
    if (rssHome.hasNode(Utils.RSS_NODE)) {
      iter = rssHome.getNode(Utils.RSS_NODE).getNodes();
      while (iter.hasNext()) {
        Node rssCal = iter.nextNode();
        if (rssCal.getPath().contains(calendarId)) {
          rssCal.remove();
        }
      }
    }
    rssHome.getSession().save();
  }

  /**
   * @param username
   * @param title
   */
  public void removeFeedData(String username, String title) {
    try {
      Node rssHome = getRssHome(username);
      NodeIterator iter = rssHome.getNodes();
      while (iter.hasNext()) {
        Node feedNode = iter.nextNode();
        if (feedNode.isNodeType(Utils.EXO_RSS_DATA)) {
          if (feedNode.getProperty(Utils.EXO_TITLE).getString().equals(title)) {
            feedNode.remove();
            break;
          }
        }
      }
      rssHome.getSession().save();
    } catch (Exception ex) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to remove feed data", ex);
      }
    }

  }

  /**
   * 
   */
  public List<FeedData> getFeeds(String username) throws Exception {
    List<FeedData> feeds = new ArrayList<FeedData>();
    try {
      Node rssHome = getRssHome(username);
      NodeIterator iter = rssHome.getNodes();
      while (iter.hasNext()) {
        Node feedNode = iter.nextNode();
        if (feedNode.isNodeType(Utils.EXO_RSS_DATA)) {
          FeedData feed = new FeedData();
          feed.setTitle(feedNode.getProperty(Utils.EXO_TITLE).getString());
          StringBuilder url = new StringBuilder(feedNode.getProperty(Utils.EXO_BASE_URL).getString());

          feed.setUrl(url.toString());

          feed.setContent(feedNode.getProperty(Utils.EXO_CONTENT).getStream());

          feeds.add(feed);
        }
      }
    } catch (Exception e) {
      log.debug(e);
    }
    return feeds;
  }

  /**
   * {@inheritDoc}
   */
  public int generateRss(String username,
                         List<String> calendarIds,
                         RssData rssData,
                         CalendarImportExport importExport) throws Exception {

    Node rssHomeNode = getRssHome(username);
    Node iCalHome = null;
    try {
      iCalHome = rssHomeNode.getNode(Utils.RSS_NODE);
    } catch (Exception e) {
      iCalHome = rssHomeNode.addNode(Utils.RSS_NODE, Utils.NT_UNSTRUCTURED);
    }
    try {
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType(rssData.getVersion());
      feed.setTitle(rssData.getTitle());
      feed.setLink(rssData.getLink());
      feed.setDescription(rssData.getDescription());
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      SyndEntry entry;
      SyndContent description;
      String portalName = PortalContainer.getCurrentPortalContainerName();
      for (String calendarId : calendarIds) {
        OutputStream out = importExport.exportCalendar(username,
                                                       Arrays.asList(new String[] { calendarId }),
                                                       "0",
                                                       -1);
        if (out != null) {
          ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes());
          try {
            iCalHome.getNode(calendarId + Utils.ICS_EXT).setProperty(Utils.EXO_DATA, is);
          } catch (Exception e) {
            Node ical = iCalHome.addNode(calendarId + Utils.ICS_EXT, Utils.EXO_ICAL_DATA);
            ical.setProperty(Utils.EXO_DATA, is);
          }
          StringBuilder path = new StringBuilder(Utils.SLASH);
          path.append(iCalHome.getName())
              .append(Utils.SLASH)
              .append(iCalHome.getNode(calendarId + Utils.ICS_EXT).getName());
          String url = getEntryUrl(portalName,
                                   rssHomeNode.getSession().getWorkspace().getName(),
                                   username,
                                   path.toString(),
                                   rssData.getUrl());
          Calendar exoCal = getUserCalendar(username, calendarId);
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());
          entry.setLink(url);
          entry.setAuthor(username);
          description = new SyndContentImpl();
          description.setType(Utils.MIMETYPE_TEXTPLAIN);
          description.setValue(exoCal.getDescription());
          entry.setDescription(description);
          entries.add(entry);
          entry.getEnclosures();
        }
      }
      if (!entries.isEmpty()) {
        feed.setEntries(entries);
        feed.setEncoding("UTF-8");
        SyndFeedOutput output = new SyndFeedOutput();
        String feedXML = output.outputString(feed);
        feedXML = StringUtils.replace(feedXML, "&amp;", "&");
        storeXML(feedXML, rssHomeNode, rssData);
        rssHomeNode.getSession().save();
      } else {
        log.info("No data to make rss!");
        return -1;
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e);
      return -1;
    }
    return 1;
  }

  /**
   * {@inheritDoc}
   */
  public int generateRss(String username,
                         LinkedHashMap<String, Calendar> calendars,
                         RssData rssData,
                         CalendarImportExport importExport) throws Exception {
    Node rssHomeNode = getRssHome(username);
    try {
      SyndFeed feed = new SyndFeedImpl();
      feed.setFeedType(rssData.getVersion());
      feed.setTitle(rssData.getTitle());
      feed.setLink(rssData.getLink());
      feed.setDescription(rssData.getDescription());
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      SyndEntry entry;
      SyndContent description;

      // String portalName = PortalContainer.getCurrentPortalContainerName();
      for (String calendarMap : calendars.keySet()) {
        String calendarId = calendarMap.split(Utils.COLON)[1];
        String type = calendarMap.split(Utils.COLON)[0];
        OutputStream out = importExport.exportCalendar(username,
                                                       Arrays.asList(new String[] { calendarId }),
                                                       type,
                                                       -1);
        if (out != null) {
          Calendar exoCal = calendars.get(calendarMap);
          entry = new SyndEntryImpl();
          entry.setTitle(exoCal.getName());

          entry.setLink(rssHomeNode.getPath() + Utils.SLASH + calendarId);
          entry.setAuthor(username);
          description = new SyndContentImpl();
          description.setType(Utils.MIMETYPE_TEXTPLAIN);
          description.setValue(exoCal.getDescription());
          entry.setDescription(description);
          entries.add(entry);
          entry.getEnclosures();
        }
      }
      if (!entries.isEmpty()) {
        feed.setEntries(entries);
        feed.setEncoding("UTF-8");
        SyndFeedOutput output = new SyndFeedOutput();
        String feedXML = output.outputString(feed);
        feedXML = StringUtils.replace(feedXML, "&amp;", "&");
        storeXML(feedXML, rssHomeNode, rssData);
        rssHomeNode.getSession().save();
      } else {
        log.info("No data to make rss!");
        return -1;
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e);
      return -1;
    }
    return 1;
  }


  /**
   * {@inheritDoc}
   */
  public String getEntryUrl(String portalName,
                            String wsName,
                            String username,
                            String path,
                            String baseUrl) throws Exception {
    StringBuilder url = new StringBuilder(baseUrl);
    url.append(Utils.SLASH).append(portalName).append(Utils.SLASH).append(wsName);
    if (username != null)
      url.append(Utils.SLASH).append(username);
    url.append(path);
    return url.toString();
  }

  /**
   * {@inheritDoc}
   */
  public EventPageList searchEvent(String username,
                                   EventQuery eventQuery,
                                   String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    try {
      if (eventQuery.getCalendarId() == null) {
        events.addAll(getUserEvents(username, eventQuery));
        if (publicCalendarIds != null && publicCalendarIds.length > 0) {
          eventQuery.setCalendarId(publicCalendarIds);
          events.addAll(getPublicEvents(eventQuery));
          eventQuery.setCalendarId(null);
        }
        events.addAll(getSharedEvents(username, eventQuery));
      } else {
        String calFullId = eventQuery.getCalendarId()[0];
        if (calFullId.split(Utils.COLON).length > 0) {
          String[] calId = new String[] { calFullId.split(Utils.COLON)[1] };
          int type = Integer.parseInt(calFullId.split(Utils.COLON)[0]);
          eventQuery.setCalendarId(calId);
          switch (type) {
          case Calendar.TYPE_PRIVATE:
            events.addAll(getUserEvents(username, eventQuery));
            return new EventPageList(events, 10);
          case Calendar.TYPE_SHARED:
            events.addAll(getSharedEvents(username, eventQuery));
            return new EventPageList(events, 10);
          case Calendar.TYPE_PUBLIC:
            events.addAll(getPublicEvents(eventQuery));
            return new EventPageList(events, 10);
          default:
            break;
          }
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e);
    } finally {
      // systemSession.close() ;
    }
    return new EventPageList(events, 10);
  }


  /**
   * Runnable to use within searchHightLightEvent
   */
  private class QueryHighLightEventsTask implements Runnable {
    private Query          query;
    private CountDownLatch latch;
    public  NodeIterator   iterator;

    public QueryHighLightEventsTask(Query aQuery, CountDownLatch aLatch) {
      query = aQuery;
      latch = aLatch;
    }
    @Override
    public void run() {
      try {
        iterator = query.execute().getNodes();
        latch.countDown();
      }
      catch (RepositoryException e) {
        log.debug("RepositoryException: " + e.getLocalizedMessage());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public Map<Integer, String> searchHightLightEvent(String username,
                                                    EventQuery eventQuery,
                                                    String[] publicCalendarIds) throws Exception {
    /** collection of which day that has event */
    Map<Integer, String> mapData = new HashMap<Integer, String>();
    Query query;

    try {
      CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));
      Node calendarHome          = getUserCalendarHome(username);
      Node calendarShareNode     = getSharedCalendarHome();
      Node publicCalHome         = getPublicCalendarHome();

      java.util.Calendar fromDateCal = calSetting.createCalendar(eventQuery.getFromDate()
                                                                           .getTimeInMillis());
      java.util.Calendar toDateCal   = calSetting.createCalendar(eventQuery.getToDate()
                                                                           .getTimeInMillis());
      QueryManager qm = publicCalHome.getSession().getWorkspace().getQueryManager();

      int queryPrivateEvents = (username != null && username.length() > 0) ? 1: 0;
      CountDownLatch latch   = new CountDownLatch(queryPrivateEvents + 1);
      Thread queryPrivateEventsThread;
      Thread queryPublicEventsThread;
      QueryHighLightEventsTask queryPrivateEventsTask = null;
      QueryHighLightEventsTask queryPublicEventsTask;
      StringBuilder queryStatement;

      /** private events */
      if (queryPrivateEvents == 1) {
        queryStatement = new StringBuilder("/jcr:root").append(calendarHome.getPath())
            .append("//element(*,exo:calendarEvent)")
            .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
            .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
            .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')]")
            .append(" /(@exo:calendarId, @exo:toDateTime, @exo:fromDateTime)");

        query = qm.createQuery(queryStatement.toString(), Query.XPATH);
        queryPrivateEventsTask   = new QueryHighLightEventsTask(query, latch);
        queryPrivateEventsThread = new Thread(queryPrivateEventsTask);
        queryPrivateEventsThread.start();
      }

      /** public events */
      queryStatement = new StringBuilder("/jcr:root").append(publicCalHome.getPath())
          .append("//element(*,exo:calendarEvent)")
          .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
          .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
          .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')");

      /** query within specific calendar ids */
      if (publicCalendarIds != null && publicCalendarIds.length > 0) {
        queryStatement.append(" and (");
        for (int i = 0; i < publicCalendarIds.length; i++) {
          if (i == 0)
            queryStatement.append("@exo:calendarId = '" + publicCalendarIds[i] + "'");
          else
            queryStatement.append(" or @exo:calendarId = '" + publicCalendarIds[i] + "'");
        }
        queryStatement.append(")");
      }
      queryStatement.append("]")
          .append(" /(@exo:calendarId, @exo:toDateTime, @exo:fromDateTime)");
      query = qm.createQuery(queryStatement.toString(), Query.XPATH);
      queryPublicEventsTask   = new QueryHighLightEventsTask(query, latch);
      queryPublicEventsThread = new Thread(queryPublicEventsTask);
      queryPublicEventsThread.start();

      /** shared events  */
      if (calendarShareNode.hasNode(username)) {

        PropertyIterator propertyIterator = calendarShareNode.getNode(username).getReferences();
        while (propertyIterator.hasNext()) {
          try {

            Node calendar = propertyIterator.nextProperty().getParent();
            queryStatement = new StringBuilder("/jcr:root").append(calendar.getPath())
                .append("//element(*,exo:calendarEvent)")
                .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
                .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
                .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')]")
                .append(" /(@exo:calendarId, @exo:toDateTime, @exo:fromDateTime)");
            query = qm.createQuery(queryStatement.toString(), Query.XPATH);

            NodeIterator it = query.execute().getNodes();
            mapData = updateMap(mapData, it, fromDateCal, toDateCal, calSetting, Calendar.TYPE_SHARED);
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Fail to update map of shared event", e);
            }
          }
        }
      }

      /** get data from threads */
      latch.await();
      mapData = updateMap(mapData, queryPublicEventsTask.iterator, fromDateCal, toDateCal, calSetting, Calendar.TYPE_PUBLIC);
      if (queryPrivateEventsTask != null)
        mapData = updateMap(mapData, queryPrivateEventsTask.iterator, fromDateCal, toDateCal, calSetting, Calendar.TYPE_PRIVATE);

    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to search highlight event", e);
      }
    }

    return mapData;
  }


  public List<Map<Integer, String>> searchHightLightEventSQL(String username, EventQuery eventQuery,
                                                             String[] privateCalendars, String[] publicCalendars) throws Exception {
    Node calendarHome      = getUserCalendarHome(username);
    Node calendarShareNode = getSharedCalendarHome();
    Node publicCalHome     = getPublicCalendarHome();
    Query query;

    CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));
    java.util.Calendar fromDateCal = calSetting.createCalendar(eventQuery.getFromDate().getTimeInMillis());
    java.util.Calendar toDateCal   = calSetting.createCalendar(eventQuery.getToDate().getTimeInMillis());

    Map<Integer, String> mapData = new HashMap<Integer, String>();
    Map<Integer, String> emptyCalendarsMap = new HashMap<Integer, String>();

    /** query private calendar */
    QueryManager qm  = calendarHome.getSession().getWorkspace().getQueryManager();
    for (String calendarId : privateCalendars) {
      String calendarPath = calendarHome.getPath() + "/" + calendarId + "/%";

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT (exo:toDateTime < TIMESTAMP '" + ISO8601.format(eventQuery.getFromDate()) + "'")
          .append(" OR exo:fromDateTime > TIMESTAMP '" + ISO8601.format(eventQuery.getToDate()) + "')")
          .append(" AND NOT (jcr:mixinTypes='exo:repeatCalendarEvent' AND NOT exo:repeat='norepeat' AND exo:recurrenceId='')");

      /**
      StringBuilder queryEventsStatementXPath = new StringBuilder("/jcr:root").append(path + "/" + calendarId)
          .append("/element(*,exo:calendarEvent)")
          .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
          .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
          .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')]")
          .append(" /(@exo:calendarId, @exo:toDateTime, @exo:fromDateTime)");
      **/

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);

      try {
        NodeIterator iterator = query.execute().getNodes();
        if (!iterator.hasNext()) {
          emptyCalendarsMap.put(emptyCalendarsMap.size(), calendarId);
        }

        mapData = updateMap(mapData, iterator, fromDateCal, toDateCal, calSetting, Calendar.TYPE_PRIVATE);

      } catch (RepositoryException e) {
        log.debug("RepositoryException: " + e.getLocalizedMessage());
      } catch (Exception e) {
        log.debug("Exception: " + e.getLocalizedMessage());
      }

    }

    /** if group calendar then check event cache before query */
    qm  = publicCalHome.getSession().getWorkspace().getQueryManager();

    for (String calendarId : publicCalendars) {
      String calendarPath = publicCalHome.getPath() + "/" + calendarId + "/%";

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT (exo:toDateTime < TIMESTAMP '" + ISO8601.format(eventQuery.getFromDate()) + "'")
          .append(" OR exo:fromDateTime > TIMESTAMP '" + ISO8601.format(eventQuery.getToDate()) + "')")
          .append(" AND NOT (jcr:mixinTypes='exo:repeatCalendarEvent' AND NOT exo:repeat='norepeat' AND exo:recurrenceId='')");

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      Lock lock = getLock("GroupCalendarEvent", queryEventsStatementSQL.toString());
      lock.lock();
      List<CalendarEvent> events;

      try {
        events = groupCalendarEventCache.get(queryEventsStatementSQL.toString());
        if (events == null) {
          query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);

          try {
            NodeIterator iterator = query.execute().getNodes();
            if (!iterator.hasNext()) {
              emptyCalendarsMap.put(emptyCalendarsMap.size(), calendarId);
            }

            events = getGroupHighLightEvents(iterator);
            groupCalendarEventCache.put(queryEventsStatementSQL.toString(), events);

          } catch (RepositoryException e) {
            log.debug("RepositoryException: " + e.getLocalizedMessage());
          } catch (Exception e) {
            log.debug("Exception: " + e.getLocalizedMessage());
          }
        }

        /** events exist in cache */
        mapData = updateMap1(mapData, events.iterator(), fromDateCal, toDateCal, calSetting, Calendar.TYPE_PUBLIC);

      } finally {
        lock.unlock();
      }
    }

    /** shared calendar */
    qm  = calendarShareNode.getSession().getWorkspace().getQueryManager();

    if (calendarShareNode.hasNode(username)) {
      PropertyIterator propertyIterator = calendarShareNode.getNode(username).getReferences();
      while (propertyIterator.hasNext()) {
        try {

          Node calendar = propertyIterator.nextProperty().getParent();
          StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
              .append(" WHERE jcr:path LIKE '").append(calendar.getPath()).append("/%'")
              .append(" AND NOT jcr:path LIKE '").append(calendar.getPath()).append("/%/%'")
              .append(" AND NOT (exo:toDateTime < TIMESTAMP '" + ISO8601.format(eventQuery.getFromDate()) + "'")
              .append(" OR exo:fromDateTime > TIMESTAMP '" + ISO8601.format(eventQuery.getToDate()) + "')")
              .append(" AND NOT (jcr:mixinTypes='exo:repeatCalendarEvent' AND NOT exo:repeat='norepeat' AND exo:recurrenceId='')");

          /**
           StringBuilder queryEventsStatementXPath = new StringBuilder("/jcr:root").append(path + "/" + calendarId)
           .append("/element(*,exo:calendarEvent)")
           .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
           .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
           .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')]")
           .append(" /(@exo:calendarId, @exo:toDateTime, @exo:fromDateTime)");
           **/

          /** event type */
          String eventType = eventQuery.getEventType();
          if (eventType != null && eventType.length() > 0) {
            queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
          }

          /** event category */
          String[] categoryIds = eventQuery.getCategoryId();
          if (categoryIds != null && categoryIds.length > 0) {
            queryEventsStatementSQL.append(" AND (");
            for (int i = 0; i < categoryIds.length; i++) {
              if (i == 0)
                queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
              else
                queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
            }
            queryEventsStatementSQL.append(")");
          }

          query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);
          NodeIterator iterator = query.execute().getNodes();
          mapData = updateMap(mapData, iterator, fromDateCal, toDateCal, calSetting, Calendar.TYPE_SHARED);
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Fail to update map of shared event", e);
          }
        }
      }
    }

    List<Map<Integer, String>> result = new ArrayList<Map<Integer, String>>();
    result.add(0, mapData);
    result.add(1, emptyCalendarsMap);
    return result;
  }


  private List<CalendarEvent> getGroupHighLightEvents(NodeIterator nodeIterator) throws Exception{
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    while (nodeIterator.hasNext()) {
      events.add(getEvent(nodeIterator.nextNode()));
    }
    return events;
  }

  public Map<Integer, String> searchHighlightRecurrenceEvent(String username,
                                                             EventQuery eventQuery,
                                                             String[] publicCalendarIds,
                                                             String timezone) throws Exception {
    Map<Integer, String> mapData = new HashMap<Integer, String>();
    int fromDayOfYear = eventQuery.getFromDate().get(java.util.Calendar.DAY_OF_YEAR);
    int toDayOfYear = eventQuery.getFromDate().get(java.util.Calendar.DAY_OF_YEAR);
    int daysOfYear = eventQuery.getFromDate().getMaximum(java.util.Calendar.DAY_OF_YEAR);
    if (eventQuery.getToDate().get(java.util.Calendar.DAY_OF_YEAR) > eventQuery.getFromDate()
                                                                               .get(java.util.Calendar.DAY_OF_YEAR)) {
      toDayOfYear = toDayOfYear + daysOfYear;
    }

    // java.util.Calendar tempCalendar = Utils.getInstanceTempCalendar();
    java.util.Calendar tempCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(timezone));

    List<CalendarEvent> originalRecurEvents = getHighLightOriginalRecurrenceEvents(username,                                                                          eventQuery.getFromDate(),
                                                                          eventQuery.getToDate(),
                                                                          publicCalendarIds);
    boolean isVictory = false;
    if (originalRecurEvents != null && originalRecurEvents.size() > 0) {
      Iterator<CalendarEvent> recurEventsIter = originalRecurEvents.iterator();
      while (recurEventsIter.hasNext() && !isVictory) {
        CalendarEvent recurEvent = recurEventsIter.next();
        Map<String, CalendarEvent> tempMap = getOccurrenceEvents(recurEvent,
                                                                 eventQuery.getFromDate(),
                                                                 eventQuery.getToDate(),
                                                                 timezone);
        if (tempMap != null) {
          for (CalendarEvent event : tempMap.values().toArray(new CalendarEvent[0])) {
            if (isVictory)
              break;
            tempCalendar.setTime(event.getFromDateTime());
            int eventFromDayOfYear = tempCalendar.get(java.util.Calendar.DAY_OF_YEAR);
            if (tempCalendar.get(java.util.Calendar.YEAR) < eventQuery.getFromDate()
                                                                      .get(java.util.Calendar.YEAR)) {
              eventFromDayOfYear = 1;
            }

            tempCalendar.setTime(event.getToDateTime());
            int eventToDayOfYear = tempCalendar.get(java.util.Calendar.DAY_OF_YEAR);
            if (tempCalendar.get(java.util.Calendar.YEAR) > eventQuery.getToDate()
                                                                      .get(java.util.Calendar.YEAR)) {
              eventToDayOfYear = 366;
            }

            Integer begin = -1;
            Integer end = -1;
            if (fromDayOfYear >= eventFromDayOfYear) {
              begin = fromDayOfYear;
              if (toDayOfYear <= eventToDayOfYear) {
                end = toDayOfYear;
                isVictory = true;
              } else {
                end = eventToDayOfYear;
              }
            } else {
              begin = eventFromDayOfYear;
              if (toDayOfYear <= eventToDayOfYear) {
                end = toDayOfYear;
              } else {
                end = eventToDayOfYear;
              }
            }
            if (begin > 0 && end > 0)
              for (Integer i = begin; i <= end; i++)
                mapData.put(i, VALUE);
          }

        }
      }
    }
    return mapData;
  }


  public List<Map<Integer, String>> searchHighlightRecurrenceEventSQL(String username, EventQuery eventQuery, String timezone,
                                                                      String[] privateCalendars, String[] publicCalendars) throws Exception {
    Map<Integer, String> mapData = new HashMap<Integer, String>();
    int fromDayOfYear = eventQuery.getFromDate().get(java.util.Calendar.DAY_OF_YEAR);
    int toDayOfYear = eventQuery.getFromDate().get(java.util.Calendar.DAY_OF_YEAR);
    int daysOfYear = eventQuery.getFromDate().getMaximum(java.util.Calendar.DAY_OF_YEAR);
    if (eventQuery.getToDate().get(java.util.Calendar.DAY_OF_YEAR) > eventQuery.getFromDate()
        .get(java.util.Calendar.DAY_OF_YEAR)) {
      toDayOfYear = toDayOfYear + daysOfYear;
    }
    java.util.Calendar tempCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone(timezone));

    List<CalendarEvent> originalRecurEvents = searchHighLightOriginalRecurrenceEventsSQL(username,
        eventQuery.getFromDate(), eventQuery.getToDate(), eventQuery, privateCalendars, publicCalendars);

    CalendarEvent calEvent = originalRecurEvents.get(0);
    String[] emptyCalsId = new String[]{};
    if (calEvent.getCalendarId().equals("-1")) {
      emptyCalsId = calEvent.getSummary().split(";");
      originalRecurEvents.remove(0);
    }

    boolean isVictory = false;
    if (originalRecurEvents != null && originalRecurEvents.size() > 0) {
      Iterator<CalendarEvent> recurEventsIter = originalRecurEvents.iterator();
      while (recurEventsIter.hasNext() && !isVictory) {
        CalendarEvent recurEvent = recurEventsIter.next();
        Map<String, CalendarEvent> tempMap = getOccurrenceEvents(recurEvent,
            eventQuery.getFromDate(),
            eventQuery.getToDate(),
            timezone);
        if (tempMap != null) {
          for (CalendarEvent event : tempMap.values().toArray(new CalendarEvent[0])) {
            if (isVictory)
              break;
            tempCalendar.setTime(event.getFromDateTime());
            int eventFromDayOfYear = tempCalendar.get(java.util.Calendar.DAY_OF_YEAR);
            if (tempCalendar.get(java.util.Calendar.YEAR) < eventQuery.getFromDate()
                .get(java.util.Calendar.YEAR)) {
              eventFromDayOfYear = 1;
            }

            tempCalendar.setTime(event.getToDateTime());
            int eventToDayOfYear = tempCalendar.get(java.util.Calendar.DAY_OF_YEAR);
            if (tempCalendar.get(java.util.Calendar.YEAR) > eventQuery.getToDate()
                .get(java.util.Calendar.YEAR)) {
              eventToDayOfYear = 366;
            }

            Integer begin = -1;
            Integer end = -1;
            if (fromDayOfYear >= eventFromDayOfYear) {
              begin = fromDayOfYear;
              if (toDayOfYear <= eventToDayOfYear) {
                end = toDayOfYear;
                isVictory = true;
              } else {
                end = eventToDayOfYear;
              }
            } else {
              begin = eventFromDayOfYear;
              if (toDayOfYear <= eventToDayOfYear) {
                end = toDayOfYear;
              } else {
                end = eventToDayOfYear;
              }
            }
            if (begin > 0 && end > 0)
              for (Integer i = begin; i <= end; i++)
                mapData.put(i, VALUE);
          }

        }
      }
    }

    //return mapData;
    Map<Integer, String> emptyCalendarsMap = new HashMap<Integer, String>();
    for (String calendarId : emptyCalsId) {
      emptyCalendarsMap.put(emptyCalendarsMap.size(), calendarId);
    }

    List<Map<Integer, String>> result = new ArrayList<Map<Integer, String>>();
    result.add(0, mapData);
    result.add(1, emptyCalendarsMap);
    return result;
  }

  /**
   * put busy days of the year, which contain events, to "data" map.
   * 
   * @param data The map contains busy days of the year. Its values are
   *          optional.
   * @param it
   * @param fromDate
   * @param toDate
   * @param calendarSetting
   * @param calType
   * @return
   * @throws Exception
   */
  private Map<Integer, String> updateMap(Map<Integer, String> data, NodeIterator it,
                                         java.util.Calendar fromDate, java.util.Calendar toDate,
                                         CalendarSetting calendarSetting, int calType) throws Exception {
    String[] filterCalIds = null;
    switch (calType) {
    case Calendar.TYPE_PRIVATE:
      filterCalIds = calendarSetting.getFilterPrivateCalendars();
      break;
    case Calendar.TYPE_PUBLIC:
      filterCalIds = calendarSetting.getFilterPublicCalendars();
      break;
    case Calendar.TYPE_SHARED:
      filterCalIds = calendarSetting.getFilterSharedCalendars();
      break;
    default:
      break;
    }
    int fromDayOfYear = fromDate.get(java.util.Calendar.DAY_OF_YEAR);
    int daysOfyer = fromDate.getMaximum(java.util.Calendar.DAY_OF_YEAR);
    int toDayOfYear = toDate.get(java.util.Calendar.DAY_OF_YEAR);
    if (toDate.get(java.util.Calendar.DAY_OF_YEAR) > fromDate.get(java.util.Calendar.DAY_OF_YEAR)) {
      toDayOfYear = toDayOfYear + daysOfyer;
    }
    boolean isVictory = false;
    while (it.hasNext() && !isVictory) {
      Node eventNode = it.nextNode();
      if (filterCalIds == null
          || !Arrays.asList(filterCalIds).contains(eventNode.getProperty(Utils.EXO_CALENDAR_ID)
                                                            .getString())) {
        java.util.Calendar eventFormDate = calendarSetting.createCalendar(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME)
                                                                                   .getDate()
                                                                                   .getTime());
        java.util.Calendar eventToDate = calendarSetting.createCalendar(eventNode.getProperty(Utils.EXO_TO_DATE_TIME)
                                                                                 .getDate()
                                                                                 .getTime());
        int eventFromDayOfYear = eventFormDate.get(java.util.Calendar.DAY_OF_YEAR);
        int eventToDayOfYear = eventToDate.get(java.util.Calendar.DAY_OF_YEAR);

        if (eventFormDate.get(java.util.Calendar.YEAR) < fromDate.get(java.util.Calendar.YEAR)) {
          eventFromDayOfYear = 1;
        }
        if (eventToDate.get(java.util.Calendar.YEAR) > toDate.get(java.util.Calendar.YEAR)) {
          eventToDayOfYear = 366;
        }
        Integer begin = -1;
        Integer end = -1;
        if (fromDayOfYear >= eventFromDayOfYear) {
          begin = fromDayOfYear;
          if (toDayOfYear <= eventToDayOfYear) {
            end = toDayOfYear;
            isVictory = true;
          } else {
            end = eventToDayOfYear;
          }
        } else {
          begin = eventFromDayOfYear;
          if (toDayOfYear <= eventToDayOfYear) {
            end = toDayOfYear;
          } else {
            end = eventToDayOfYear;
          }
        }
        if (begin > 0 && end > 0)
          for (Integer i = begin; i <= end; i++)
            data.put(i, VALUE);
      }
    }
    return data;
  }


  private Map<Integer, String> updateMap1(Map<Integer, String> data, Iterator<CalendarEvent> it,
                                         java.util.Calendar fromDate, java.util.Calendar toDate,
                                         CalendarSetting calendarSetting, int calType) throws Exception {
    String[] filterCalIds = null;
    switch (calType) {
      case Calendar.TYPE_PRIVATE:
        filterCalIds = calendarSetting.getFilterPrivateCalendars();
        break;
      case Calendar.TYPE_PUBLIC:
        filterCalIds = calendarSetting.getFilterPublicCalendars();
        break;
      case Calendar.TYPE_SHARED:
        filterCalIds = calendarSetting.getFilterSharedCalendars();
        break;
      default:
        break;
    }
    int fromDayOfYear = fromDate.get(java.util.Calendar.DAY_OF_YEAR);
    int daysOfyer = fromDate.getMaximum(java.util.Calendar.DAY_OF_YEAR);
    int toDayOfYear = toDate.get(java.util.Calendar.DAY_OF_YEAR);
    if (toDate.get(java.util.Calendar.DAY_OF_YEAR) > fromDate.get(java.util.Calendar.DAY_OF_YEAR)) {
      toDayOfYear = toDayOfYear + daysOfyer;
    }
    boolean isVictory = false;
    while (it.hasNext() && !isVictory) {
      CalendarEvent event = it.next();
      if (filterCalIds == null
          || !Arrays.asList(filterCalIds).contains(event.getCalendarId())) {
        java.util.Calendar eventFormDate = calendarSetting.createCalendar(event.getFromDateTime().getTime());
        java.util.Calendar eventToDate = calendarSetting.createCalendar(event.getToDateTime().getTime());

        int eventFromDayOfYear = eventFormDate.get(java.util.Calendar.DAY_OF_YEAR);
        int eventToDayOfYear = eventToDate.get(java.util.Calendar.DAY_OF_YEAR);

        if (eventFormDate.get(java.util.Calendar.YEAR) < fromDate.get(java.util.Calendar.YEAR)) {
          eventFromDayOfYear = 1;
        }
        if (eventToDate.get(java.util.Calendar.YEAR) > toDate.get(java.util.Calendar.YEAR)) {
          eventToDayOfYear = 366;
        }
        Integer begin = -1;
        Integer end = -1;
        if (fromDayOfYear >= eventFromDayOfYear) {
          begin = fromDayOfYear;
          if (toDayOfYear <= eventToDayOfYear) {
            end = toDayOfYear;
            isVictory = true;
          } else {
            end = eventToDayOfYear;
          }
        } else {
          begin = eventFromDayOfYear;
          if (toDayOfYear <= eventToDayOfYear) {
            end = toDayOfYear;
          } else {
            end = eventToDayOfYear;
          }
        }
        if (begin > 0 && end > 0)
          for (Integer i = begin; i <= end; i++)
            data.put(i, VALUE);
      }
    }
    return data;
  }


  private List<Value> calculateSharedCalendar(String user,
                                              Node calendarNode,
                                              Value[] values,
                                              List<Value> valueList,
                                              Node sharedCalendarHome) throws Exception {
    CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(user));
    if (calSetting == null)
      calSetting = new CalendarSetting();
    Map<String, String> map = new HashMap<String, String>();
    for (String key : calSetting.getSharedCalendarsColors()) {
      map.put(key.split(":")[0], key.split(":")[1]);
    }
    if (map.get(calendarNode.getProperty(Utils.EXO_ID).getString()) == null)
      map.put(calendarNode.getProperty(Utils.EXO_ID).getString(),
              calendarNode.getProperty("exo:calendarColor").getString());
    List<String> calColors = new ArrayList<String>();
    for (String key : map.keySet()) {
      calColors.add(key + ":" + map.get(key));
    }
    calSetting.setSharedCalendarsColors(calColors.toArray(new String[calColors.size()]));
    saveCalendarSetting(calSetting, user);

    Node userNode = null;
    Session systemSession = sharedCalendarHome.getSession();
    try {
      userNode = sharedCalendarHome.getNode(user);
    } catch (PathNotFoundException e) {
      userNode = sharedCalendarHome.addNode(user, Utils.NT_UNSTRUCTURED);
      if (userNode.canAddMixin(Utils.MIX_REFERENCEABLE)) {
        userNode.addMixin(Utils.MIX_REFERENCEABLE);
      }
    }
    boolean isExist = false;
    isExist = false;
    for (int i = 0; i < values.length; i++) {
      Value value = values[i];
      String uuid = value.getString();
      Node refNode = systemSession.getNodeByUUID(uuid);
      if (refNode.getPath().equals(userNode.getPath())) {
        isExist = true;
        break;
      }
    }
    if (!isExist) {
      Value value2add = calendarNode.getSession().getValueFactory().createValue(userNode);
      valueList.add(value2add);
    }
    return valueList;
  }

  /**
   * {@inheritDoc}
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
    Value[] values = {};
    if (calendarNode.isNodeType(Utils.EXO_SHARED_MIXIN)) {
      values = calendarNode.getProperty(Utils.EXO_SHARED_ID).getValues();
    }
    List<Value> valueList = new ArrayList<Value>();
    for (int i = 0; i < values.length; i++) {
      Value value = values[i];
      valueList.add(value);
    }
    for (String user : receiverUsers) {
      valueList = calculateSharedCalendar(user, calendarNode, values, valueList, sharedCalendarHome);
    }
    if (valueList.size() > 0) {
      if (!calendarNode.isNodeType(Utils.EXO_SHARED_MIXIN)) {
        calendarNode.addMixin(Utils.EXO_SHARED_MIXIN);
      }
      calendarNode.setProperty(Utils.EXO_SHARED_ID, valueList.toArray(new Value[valueList.size()]));
      calendarNode.save();
      sharedCalendarHome.getSession().save();
      calendarNode.getSession().save();
    }
  }

  /**
   * {@inheritDoc}
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    Node shareCalendarHome = getSharedCalendarHome();
    if (shareCalendarHome.hasNode(username)) {
      Node sharedNode = shareCalendarHome.getNode(username);
      List<Calendar> calendars = new ArrayList<Calendar>();
      PropertyIterator iter = sharedNode.getReferences();
      String[] defaultFilterCalendars = null;
      if (username != null) {
        defaultFilterCalendars = getCalendarSetting(getUserCalendarServiceHome(username)).getFilterSharedCalendars();
      }
      while (iter.hasNext()) {
        try {
          Calendar cal = getCalendar(defaultFilterCalendars,
                                     null,
                                     iter.nextProperty().getParent(),
                                     isShowAll);
          if (cal != null) {
            calendars.add(cal);
          }
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Fail to create new calendar", e);
          }
        }
      }
      if (calendars.size() > 0) {
        return new GroupCalendarData("Shared", "Shared", calendars);
      }
    }
    return null;
  }

  private Node setCalendarProperties(Node calendarNode, Calendar calendar) throws Exception {
    calendarNode.setProperty(Utils.EXO_NAME, calendar.getName());
    calendarNode.setProperty(Utils.EXO_DESCRIPTION, calendar.getDescription());
    calendarNode.setProperty(Utils.EXO_VIEW_PERMISSIONS, calendar.getViewPermission());
    calendarNode.setProperty(Utils.EXO_EDIT_PERMISSIONS, calendar.getEditPermission());
    calendarNode.setProperty(Utils.EXO_GROUPS, calendar.getGroups());
    calendarNode.setProperty(Utils.EXO_LOCALE, calendar.getLocale());
    calendarNode.setProperty(Utils.EXO_TIMEZONE, calendar.getTimeZone());
    calendarNode.setProperty(Utils.EXO_CALENDAR_COLOR, calendar.getCalendarColor());
    calendarNode.setProperty(Utils.EXO_CALENDAR_OWNER, calendar.getCalendarOwner());
    try {
      calendarNode.setProperty(Utils.EXO_PUBLIC_URL, calendar.getPublicUrl());
      calendarNode.setProperty(Utils.EXO_PRIVATE_URL, calendar.getPrivateUrl());
    } catch (ConstraintViolationException e) {
      log.debug("\n\n Need to add property definition in exo:calendar node type !", e);
    }
    return calendarNode;
  }

  /**
   * {@inheritDoc}
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      String uuid = userNode.getProperty("jcr:uuid").getString();
      PropertyIterator iter = userNode.getReferences();
      Node calendarNode;
      List<Value> newValues = new ArrayList<Value>();
      while (iter.hasNext()) {
        calendarNode = iter.nextProperty().getParent();
        if (calendarNode.getProperty(Utils.EXO_ID).getString().equals(calendar.getId())) {
          Value[] values = calendarNode.getProperty(Utils.EXO_SHARED_ID).getValues();
          for (Value value : values) {
            if (!value.getString().equals(uuid)) {
              newValues.add(value);
            }
          }
          calendarNode = setCalendarProperties(calendarNode, calendar);
          CalendarSetting usCalSetting = getCalendarSetting(getUserCalendarServiceHome(username));
          Map<String, String> map = new HashMap<String, String>();
          for (String key : usCalSetting.getSharedCalendarsColors()) {
            map.put(key.split(Utils.COLON)[0], key.split(Utils.COLON)[1]);
          }
          map.put(calendar.getId(), calendar.getCalendarColor());
          List<String> calColors = new ArrayList<String>();
          for (String key : map.keySet()) {
            calColors.add(key + Utils.COLON + map.get(key));
          }
          calColors.add(calendar.getId() + Utils.COLON + calendar.getCalendarColor());
          usCalSetting.setSharedCalendarsColors(calColors.toArray(new String[calColors.size()]));
          saveCalendarSetting(usCalSetting, username);
          calendarNode.save();
          break;
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getSharedEvents(String username, EventQuery eventQuery) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    Node shareHome = getSharedCalendarHome();
    if (shareHome.hasNode(username)) {
      QueryManager qm = shareHome.getSession().getWorkspace().getQueryManager();
      PropertyIterator iter = getSharedCalendarHome().getNode(username).getReferences();
      CalendarEvent calEvent;
      while (iter.hasNext()) {
        try {
          Node calendar = iter.nextProperty().getParent();
          eventQuery.setCalendarPath(calendar.getPath());
          Query query = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType());
          NodeIterator it = query.execute().getNodes();
          while (it.hasNext()) {
            calEvent = getEvent(it.nextNode());
            calEvent.setCalType("1");
            events.add(calEvent);
            if (eventQuery.getLimitedItems() == it.getPosition())
              break;
          }
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Fail to get events from calendar", e);
          }
        }
      }
    }
    return events;
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    if (getSharedCalendarHome().hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome().getNode(username).getReferences();
      while (iter.hasNext()) {
        try {
          Node calendar = iter.nextProperty().getParent();
          if (calendarIds.contains(calendar.getProperty(Utils.EXO_ID).getString())) {
            NodeIterator it = calendar.getNodes();
            while (it.hasNext()) {
              events.add(getEvent(it.nextNode()));
            }
          }
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Fail to get events from calendar", e);
          }
        }
      }
    }
    return events;
  }

  public CalendarEvent getSharedEvent(String username, String calendarId, String eventId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    if (sharedCalendarHome.hasNode(username)) {
      PropertyIterator iter = getSharedCalendarHome().getNode(username).getReferences();
      while (iter.hasNext()) {
        try {
          Node calendar = iter.nextProperty().getParent();
          if (!calendarId.equals(calendar.getProperty(Utils.EXO_ID).getString()))
            continue;
          if (calendar.hasNode(eventId)) {
            CalendarEvent event = getEvent(calendar.getNode(eventId));
            event.setCalType(String.valueOf(Calendar.TYPE_SHARED));
            return event;
          }
        } catch (Exception e) {
          log.error("Exception when get shared event", e);
          return null;
        }
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventToSharedCalendar(String username,
                                        String calendarId,
                                        CalendarEvent event,
                                        boolean isNew) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      PropertyIterator iter = userNode.getReferences();
      Node calendar;
      while (iter.hasNext()) {
        calendar = iter.nextProperty().getParent();
        if (calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {

          if (!canEdit(calendar, username)) {
            log.debug("\n Do not have edit permission. \n");
            throw new AccessDeniedException();
          }
          Node reminderFolder = getReminderFolder(event.getFromDateTime());
          saveEvent(calendar, event, reminderFolder, isNew);
          calendar.save();
          break;
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public boolean canEdit(Node calNode, String username) throws Exception {
    OrganizationService oService = (OrganizationService) ExoContainerContext.getCurrentContainer()
                                                                            .getComponentInstanceOfType(OrganizationService.class);
    StringBuilder sb = new StringBuilder(username);
    if (oService != null) {
      Collection<Group> groups = oService.getGroupHandler().findGroupsOfUser(username);
      for (Group g : groups) {
        sb.append(Utils.COMMA).append(g.getId()).append(Utils.SLASH_COLON).append(Utils.ANY);
        sb.append(Utils.COMMA).append(g.getId()).append(Utils.SLASH_COLON).append(username);
        Collection<Membership> memberShipsType = oService.getMembershipHandler()
                                                         .findMembershipsByUserAndGroup(username,
                                                                                        g.getId());
        for (Membership mp : memberShipsType) {
          sb.append(Utils.COMMA)
            .append(g.getId())
            .append(Utils.SLASH_COLON)
            .append(Utils.ANY_OF + mp.getMembershipType());
        }
      }
    }

    Value[] editValues = calNode.getProperty(Utils.EXO_EDIT_PERMISSIONS).getValues();
    List<String> editPerms = new ArrayList<String>();
    for (Value v : editValues) {
      String value = v.getString();
      if (value.contains(Utils.SLASH)) {
        editPerms.addAll(Utils.getUsersCanEdit(value));
      } else {
        editPerms.add(value);
      }
    }
    if (editPerms != null) {
      String[] checkPerms = sb.toString().split(Utils.COMMA);
      for (String sp : editPerms) {
        for (String cp : checkPerms) {
          if (sp.equals(cp)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getEvents(String username,
                                       EventQuery eventQuery,
                                       String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    List<String> filterList = new ArrayList<String>();
    CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));
    filterList.addAll(Arrays.asList(calSetting.getFilterPrivateCalendars()));
    filterList.addAll(Arrays.asList(calSetting.getFilterPublicCalendars()));
    filterList.addAll(Arrays.asList(calSetting.getFilterSharedCalendars()));
    eventQuery.setFilterCalendarIds(filterList.toArray(new String[] {}));
    events.addAll(getUserEvents(username, eventQuery));

    try {
      events.addAll(getSharedEvents(username, eventQuery));

      if (publicCalendarIds != null && publicCalendarIds.length > 0) {
        eventQuery.setCalendarId(publicCalendarIds);
        events.addAll(getPublicEvents(eventQuery));
      }

      // add recurrence events
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to get events", e);
      }
    } finally {
      // systemSession.close() ;
    }
    return events;
  }


  /**
   * Runnable used within getAllNoRepeatEvents
   */
  private class QueryEventsTask implements Runnable {
    private Query query;
    private CountDownLatch latch;
    private String calendarType;
    public List<CalendarEvent> events;

    public QueryEventsTask(Query aQuery, String type, CountDownLatch aLatch) {
      query = aQuery;
      latch = aLatch;
      calendarType = type;
      events = new ArrayList<CalendarEvent>();
    }

    @Override
    public void run() {
      try {
        NodeIterator iterator = query.execute().getNodes();
        CalendarEvent event;
        while (iterator.hasNext()) {
          event = getEvent(iterator.nextNode());
          event.setCalType(String.valueOf(calendarType));
          events.add(event);
        }
        latch.countDown();
      } catch (RepositoryException e) {
        log.debug("RepositoryException: " + e.getLocalizedMessage());
      } catch (Exception e) {
        log.debug("Exception: " + e.getLocalizedMessage());
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getAllNoRepeatEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> allEvents = new ArrayList<CalendarEvent>();
    String[] filteredCalendars;
    Node calendarHome      = getUserCalendarHome(username);
    Node calendarShareNode = getSharedCalendarHome();
    Node publicCalHome     = getPublicCalendarHome();
    Query query;

    CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));
    QueryManager qm = publicCalHome.getSession().getWorkspace().getQueryManager();
    CountDownLatch latch = new CountDownLatch(2);

    /** query private calendar events */
    filteredCalendars = calSetting.getFilterPrivateCalendars();

    StringBuilder queryStatement = new StringBuilder("/jcr:root").append(calendarHome.getPath())
            .append("//element(*,exo:calendarEvent)")
            .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
            .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
            .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')");

    if (filteredCalendars != null && filteredCalendars.length > 0) {
        queryStatement.append(" and (");
        for (int i = 0; i < filteredCalendars.length; i++) {
            if (i == 0)
                queryStatement.append("@exo:calendarId != '" + filteredCalendars[i] + "'");
            else
              queryStatement.append(" or @exo:calendarId != '" + filteredCalendars[i] + "'");
          }
        queryStatement.append(")");
      }

    /** event type */
    String eventType = eventQuery.getEventType();
    if (eventType != null && eventType.length() > 0) {
      queryStatement.append(" and (@exo:eventType='" + eventType + "')");
    }

    /** event category */
    String[] categoryIds = eventQuery.getCategoryId();
    if (categoryIds != null && categoryIds.length > 0) {
      queryStatement.append(" and (");
      for (int i = 0; i < categoryIds.length; i++) {
        if (i == 0)
          queryStatement.append("@exo:eventCategoryId='").append(categoryIds[i]).append("'");
        else
          queryStatement.append(" or @exo:eventCategoryId='").append(categoryIds[i]).append("'");
      }
      queryStatement.append(")");
    }

    queryStatement.append("]");

    query = qm.createQuery(queryStatement.toString(), Query.XPATH);

    QueryEventsTask queryPrivateEventsTask = new QueryEventsTask(query, String.valueOf(Calendar.TYPE_PRIVATE), latch);
    Thread queryPrivateEventsThread = new Thread(queryPrivateEventsTask);
    queryPrivateEventsThread.start();

    /** query public events */
    filteredCalendars = calSetting.getFilterPublicCalendars();

    queryStatement = new StringBuilder("/jcr:root").append(publicCalHome.getPath())
            .append("//element(*,exo:calendarEvent)")
            .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
            .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
            .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')");

    if (publicCalendarIds != null && publicCalendarIds.length > 0) {
      queryStatement.append(" and (");
        for (int i = 0; i < publicCalendarIds.length; i++) {
          if (i == 0)
            queryStatement.append("@exo:calendarId = '" + publicCalendarIds[i] + "'");
          else
            queryStatement.append(" or @exo:calendarId = '" + publicCalendarIds[i] + "'");
          }
      queryStatement.append(")");
    }

    if (filteredCalendars != null && filteredCalendars.length > 0) {
      queryStatement.append(" and (");
        for (int i = 0; i < filteredCalendars.length; i++) {
          if (i == 0)
            queryStatement.append("@exo:calendarId != '" + filteredCalendars[i] + "'");
          else
            queryStatement.append(" or @exo:calendarId != '" + filteredCalendars[i] + "'");
        }
        queryStatement.append(")");
    }

    /** event type */
    eventType = eventQuery.getEventType();
    if (eventType != null && eventType.length() > 0) {
      queryStatement.append(" and (@exo:eventType='" + eventType + "')");
    }

    /** event category */
    categoryIds = eventQuery.getCategoryId();
    if (categoryIds != null && categoryIds.length > 0) {
      queryStatement.append(" and (");
      for (int i = 0; i < categoryIds.length; i++) {
        if (i == 0)
          queryStatement.append("@exo:eventCategoryId='").append(categoryIds[i]).append("'");
        else
          queryStatement.append(" or @exo:eventCategoryId='").append(categoryIds[i]).append("'");
      }
      queryStatement.append(")");
    }
    queryStatement.append("]");

    query = qm.createQuery(queryStatement.toString(), Query.XPATH);

    QueryEventsTask queryPublicEventsTask = new QueryEventsTask(query, String.valueOf(Calendar.TYPE_PUBLIC), latch);
    Thread queryPublicEventsThread = new Thread(queryPublicEventsTask);
    queryPublicEventsThread.start();

    /** query shared events */
    filteredCalendars = calSetting.getFilterSharedCalendars();

    try {
      PropertyIterator propertyIterator = calendarShareNode.getNode(username).getReferences();

      while (propertyIterator.hasNext()) {
        Node calendar = propertyIterator.nextProperty().getParent();
        queryStatement = new StringBuilder("/jcr:root").append(calendar.getPath())
            .append("//element(*,exo:calendarEvent)")
            .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
            .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
            .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')");

        if (filteredCalendars != null && filteredCalendars.length > 0) {
          queryStatement.append(" and (");
          for (int i = 0; i < filteredCalendars.length; i++) {
            if (i == 0)
              queryStatement.append("@exo:calendarId != '" + filteredCalendars[i] + "'");
            else
              queryStatement.append(" or @exo:calendarId != '" + filteredCalendars[i] + "'");
          }
          queryStatement.append(")");
        }

        /** event type */
        eventType = eventQuery.getEventType();
        if (eventType != null && eventType.length() > 0) {
          queryStatement.append(" and (@exo:eventType='" + eventType + "')");
        }

        /** event category */
        categoryIds = eventQuery.getCategoryId();
        if (categoryIds != null && categoryIds.length > 0) {
          queryStatement.append(" and (");
          for (int i = 0; i < categoryIds.length; i++) {
            if (i == 0)
              queryStatement.append("@exo:eventCategoryId='").append(categoryIds[i]).append("'");
            else
              queryStatement.append(" or @exo:eventCategoryId='").append(categoryIds[i]).append("'");
          }
          queryStatement.append(")");
        }
        queryStatement.append("]");

        query = qm.createQuery(queryStatement.toString(), Query.XPATH);
        NodeIterator iterator = query.execute().getNodes();
        CalendarEvent event;
        while (iterator.hasNext()) {
          event = getEvent(iterator.nextNode());
          event.setCalType(String.valueOf(Calendar.TYPE_SHARED));
          allEvents.add(event);
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception: " + e.getLocalizedMessage());
      }
    }
    latch.await();
    allEvents.addAll(queryPrivateEventsTask.events);
    allEvents.addAll(queryPublicEventsTask.events);
    return allEvents;
  }

  private class QueryRecurrentEventsTask implements Runnable {
    private CountDownLatch     latch;
    private Node               homeNode;
    private String             calendarType;
    private java.util.Calendar fromDate;
    private java.util.Calendar toDate;
    private String[]           filteredCalendars;
    private String[]           calendarIds;
    public List<CalendarEvent> calendarEvents;
    public QueryRecurrentEventsTask(Node calendarHome, String type,
                                    java.util.Calendar from, java.util.Calendar to,
                                    String[] _calendarIds, String[] _filteredCalendars,
                                    CountDownLatch aLatch) {
      homeNode     = calendarHome;
      calendarType = type;
      fromDate     = from;
      toDate       = to;
      latch        = aLatch;
      filteredCalendars = _filteredCalendars;
      calendarIds  = _calendarIds;
    }

    @Override
    public void run() {
      try {
        calendarEvents = getOriginalRecurrenceEvents1(homeNode, calendarType,
            fromDate, toDate, calendarIds, filteredCalendars) ;
        latch.countDown();
      } catch (Exception e) {
        // log.debug("Exception: " + e.getLocalizedMessage());
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getAllNoRepeatEventsSQL(String username, EventQuery eventQuery,
                                                     String[] privateCalendars, String[] publicCalendars,
                                                     List<String> emptyCalendars) throws Exception {
    List<CalendarEvent> allEvents = new ArrayList<CalendarEvent>();

    Node calendarHome      = getUserCalendarHome(username);
    Node calendarShareNode = getSharedCalendarHome();
    Node publicCalHome     = getPublicCalendarHome();
    Query query;

    /** query private calendar */
    QueryManager qm  = calendarHome.getSession().getWorkspace().getQueryManager();

    for (String calendarId : privateCalendars) {
      if (emptyCalendars!= null && emptyCalendars.contains(calendarId)) continue;
      String calendarPath = calendarHome.getPath() + "/" + calendarId + "/%";

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT (exo:toDateTime < TIMESTAMP '" + ISO8601.format(eventQuery.getFromDate()) + "'")
          .append(" OR exo:fromDateTime > TIMESTAMP '" + ISO8601.format(eventQuery.getToDate()) + "')")
          .append(" AND NOT (jcr:mixinTypes='exo:repeatCalendarEvent' AND NOT exo:repeat='norepeat' AND exo:recurrenceId='')");

      /**
       StringBuilder queryEventsStatementXPath = new StringBuilder("/jcr:root").append(path + "/" + calendarId)
       .append("/element(*,exo:calendarEvent)")
       .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
       .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
       .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')]")
       .append(" /(@exo:calendarId, @exo:toDateTime, @exo:fromDateTime)");
       **/

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);

      try {
        NodeIterator iterator = query.execute().getNodes();
        CalendarEvent event;
        while (iterator.hasNext()) {
          event = getEvent(iterator.nextNode());
          event.setCalType(String.valueOf(Calendar.TYPE_PRIVATE));
          allEvents.add(event);
        }
      } catch (RepositoryException e) {
        log.debug("RepositoryException: " + e.getLocalizedMessage());
      } catch (Exception e) {
        log.debug("Exception: " + e.getLocalizedMessage());
      }

    }

    /** if group calendar then check event cache before query */
    qm  = publicCalHome.getSession().getWorkspace().getQueryManager();

    for (String calendarId : publicCalendars) {
      if (emptyCalendars!= null && emptyCalendars.contains(calendarId)) continue;
      String calendarPath = publicCalHome.getPath() + "/" + calendarId + "/%";

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT (exo:toDateTime < TIMESTAMP '" + ISO8601.format(eventQuery.getFromDate()) + "'")
          .append(" OR exo:fromDateTime > TIMESTAMP '" + ISO8601.format(eventQuery.getToDate()) + "')")
          .append(" AND NOT (jcr:mixinTypes='exo:repeatCalendarEvent' AND NOT exo:repeat='norepeat' AND exo:recurrenceId='')");

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      Lock lock = getLock("GroupCalendarEvent", queryEventsStatementSQL.toString());
      lock.lock();
      List<CalendarEvent> events;

      try {

        events = groupCalendarEventCache.get(queryEventsStatementSQL.toString());
        if (events == null) {
          query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);
          events = new ArrayList<CalendarEvent>();

          try {
            NodeIterator iterator = query.execute().getNodes();
            CalendarEvent event;
            while (iterator.hasNext()) {
              event = getEvent(iterator.nextNode());
              event.setCalType(String.valueOf(Calendar.TYPE_PUBLIC));
              events.add(event);
            }

            groupCalendarEventCache.put(queryEventsStatementSQL.toString(), events);

          } catch (RepositoryException e) {
            log.debug("RepositoryException: " + e.getLocalizedMessage());
          } catch (Exception e) {
            log.debug("Exception: " + e.getLocalizedMessage());
          }
        }
      } finally {
        lock.unlock();
      }

      allEvents.addAll(events);
    }

    /** shared calendar */
    qm  = calendarShareNode.getSession().getWorkspace().getQueryManager();

    if (calendarShareNode.hasNode(username)) {
      PropertyIterator propertyIterator = calendarShareNode.getNode(username).getReferences();
      while (propertyIterator.hasNext()) {
        try {

          Node calendar = propertyIterator.nextProperty().getParent();
          StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
              .append(" WHERE jcr:path LIKE '").append(calendar.getPath()).append("/%'")
              .append(" AND NOT jcr:path LIKE '").append(calendar.getPath()).append("/%/%'")
              .append(" AND NOT (exo:toDateTime < TIMESTAMP '" + ISO8601.format(eventQuery.getFromDate()) + "'")
              .append(" OR exo:fromDateTime > TIMESTAMP '" + ISO8601.format(eventQuery.getToDate()) + "')")
              .append(" AND NOT (jcr:mixinTypes='exo:repeatCalendarEvent' AND NOT exo:repeat='norepeat' AND exo:recurrenceId='')");

          /**
           StringBuilder queryEventsStatementXPath = new StringBuilder("/jcr:root").append(path + "/" + calendarId)
           .append("/element(*,exo:calendarEvent)")
           .append(" [ not(@exo:toDateTime < xs:dateTime('" + ISO8601.format(eventQuery.getFromDate()) + "')")
           .append(" or @exo:fromDateTime > xs:dateTime('" + ISO8601.format(eventQuery.getToDate()) + "'))")
           .append(" and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='norepeat' and @exo:recurrenceId='')]")
           .append(" /(@exo:calendarId, @exo:toDateTime, @exo:fromDateTime)");
           **/

          /** event type */
          String eventType = eventQuery.getEventType();
          if (eventType != null && eventType.length() > 0) {
            queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
          }

          /** event category */
          String[] categoryIds = eventQuery.getCategoryId();
          if (categoryIds != null && categoryIds.length > 0) {
            queryEventsStatementSQL.append(" AND (");
            for (int i = 0; i < categoryIds.length; i++) {
              if (i == 0)
                queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
              else
                queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
            }
            queryEventsStatementSQL.append(")");
          }

          query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);

          try {
            NodeIterator iterator = query.execute().getNodes();
            CalendarEvent event;
            while (iterator.hasNext()) {
              event = getEvent(iterator.nextNode());
              event.setCalType(String.valueOf(Calendar.TYPE_SHARED));
              allEvents.add(event);
            }
          } catch (RepositoryException e) {
            log.debug("RepositoryException: " + e.getLocalizedMessage());
          } catch (Exception e) {
            log.debug("Exception: " + e.getLocalizedMessage());
          }
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Fail to update map of shared event", e);
          }
        }
      }
    }

    return allEvents;
  }


  /**
   * Another version of getOriginalRecurrenceEvents using Thread
   */
  public List<CalendarEvent> getHighLightOriginalRecurrenceEvents(String username,java.util.Calendar from,
                                                                  java.util.Calendar to, String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();
    CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));

    /** get from user private calendars, with filter settings  */
    Node calendarHome = getUserCalendarHome(username);
    String[] filterIds = calSetting.getFilterPrivateCalendars();
    CountDownLatch latch = new CountDownLatch(2);
    QueryRecurrentEventsTask privateEventsQueryTask = new QueryRecurrentEventsTask(calendarHome,
        String.valueOf(Calendar.TYPE_PRIVATE), from, to, null, filterIds, latch);
    Thread privateEventsQueryThread = new Thread(privateEventsQueryTask);
    privateEventsQueryThread.start();

    /** get from public calendars, with filter settings */
    Node publicCalendarHome = getPublicCalendarHome();
    filterIds = calSetting.getFilterPublicCalendars();

    QueryRecurrentEventsTask publicEventsQueryTask = new QueryRecurrentEventsTask(publicCalendarHome,
        String.valueOf(Calendar.TYPE_PUBLIC), from, to, publicCalendarIds, filterIds, latch);
    Thread publicEventsQueryThread = new Thread(publicEventsQueryTask);
    publicEventsQueryThread.start();

    /** get from shared calendars, with filter settings */
    Node sharedCalendarHome = getSharedCalendarHome();
    filterIds = calSetting.getFilterSharedCalendars();
    List<String> filterIdList = null;
    if (filterIds != null && filterIds.length > 0)
      filterIdList = Arrays.asList(filterIds);
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      PropertyIterator iter = userNode.getReferences();
      Node calendar;
      while (iter.hasNext()) {
        calendar = iter.nextProperty().getParent();
        if (filterIdList != null && filterIdList.contains(calendar.getProperty(Utils.EXO_ID).getString()))
          continue;
        recurEvents.addAll(getOriginalRecurrenceEvents1(calendar, String.valueOf(Calendar.TYPE_SHARED),
            from, to, null, null));
      }
    }
    latch.await();
    recurEvents.addAll(privateEventsQueryTask.calendarEvents);
    recurEvents.addAll(publicEventsQueryTask.calendarEvents);
    return recurEvents;
  }


  /**
   * Another version of getOriginalRecurrenceEvents
   */
  public List<CalendarEvent> getHighLightOriginalRecurrenceEventsSQL(String username,java.util.Calendar from,
                                                                     java.util.Calendar to, EventQuery eventQuery, String[] privateCalendars,
                                                                     String[] publicCalendars, List<String> emptyCalendars) throws Exception {
    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();

    /** get from user private calendars */
    recurEvents.addAll(getOriginalRecurrenceEventsSQL(getUserCalendarHome(username), String.valueOf(Calendar.TYPE_PRIVATE),
        from, to, eventQuery, privateCalendars, emptyCalendars));

    Node publicHome = getPublicCalendarHome();
    QueryManager qm = publicHome.getSession().getWorkspace().getQueryManager();

    for (String calendarId : publicCalendars) {
      if (emptyCalendars!= null && emptyCalendars.contains(calendarId)) continue;

      String calendarPath = publicHome.getPath() + "/" + calendarId;

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%/%'")
          .append(" AND jcr:mixinTypes='exo:repeatCalendarEvent'")
          .append(" AND NOT exo:repeat='norepeat'")
          .append(" AND (exo:repeatUntil IS NULL OR exo:repeatUntil >=  TIMESTAMP '" + ISO8601.format(from) +  "' )")
          .append(" AND (exo:repeatFinishDate IS NULL OR exo:repeatFinishDate >=  TIMESTAMP '" + ISO8601.format(from) +  "' )");

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      Lock lock = getLock("GroupCalendarRecurrentEvent", queryEventsStatementSQL.toString());
      lock.lock();
      List<CalendarEvent> events;

      try {
        events = groupCalendarRecurrentEventCache.get(queryEventsStatementSQL.toString());
        if (events == null) {
          events = new ArrayList<CalendarEvent>();
          Query query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);

          try {
            NodeIterator it = query.execute().getNodes();
            while (it.hasNext()) {
              Node eventNode = it.nextNode();
              CalendarEvent event = getEvent(eventNode);
              event.setCalType(String.valueOf(Calendar.TYPE_PUBLIC));
              events.add(event);
            }

            groupCalendarRecurrentEventCache.put(queryEventsStatementSQL.toString(), events);

          } catch (RepositoryException e) {
            log.debug("RepositoryException: " + e.getLocalizedMessage());
          } catch (Exception e) {
            log.debug("Exception: " + e.getLocalizedMessage());
          }
        }
      } finally {
        lock.unlock();
      }

      recurEvents.addAll(events);
    }

    Node sharedCalendarHome = getSharedCalendarHome();
    qm = sharedCalendarHome.getSession().getWorkspace().getQueryManager();

    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      PropertyIterator propertyIterator = userNode.getReferences();
      Node calendarNode;
      while (propertyIterator.hasNext()) {
        calendarNode = propertyIterator.nextProperty().getParent();

        StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
            .append(" WHERE jcr:path LIKE '").append(calendarNode.getPath()).append("/%'")
            .append(" AND NOT jcr:path LIKE '").append(calendarNode.getPath()).append("/%/%'")
            .append(" AND jcr:mixinTypes='exo:repeatCalendarEvent'")
            .append(" AND NOT exo:repeat='norepeat'")
            .append(" AND (exo:repeatUntil IS NULL OR exo:repeatUntil >=  TIMESTAMP '" + ISO8601.format(from) +  "' )")
            .append(" AND (exo:repeatFinishDate IS NULL OR exo:repeatFinishDate >=  TIMESTAMP '" + ISO8601.format(from) +  "' )");

        /** event type */
        String eventType = eventQuery.getEventType();
        if (eventType != null && eventType.length() > 0) {
          queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
        }

        /** event category */
        String[] categoryIds = eventQuery.getCategoryId();
        if (categoryIds != null && categoryIds.length > 0) {
          queryEventsStatementSQL.append(" AND (");
          for (int i = 0; i < categoryIds.length; i++) {
            if (i == 0)
              queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
            else
              queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
          }
          queryEventsStatementSQL.append(")");
        }

        Query query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);
        QueryResult result = query.execute();

        NodeIterator it = result.getNodes();
        while (it.hasNext()) {
          Node eventNode = it.nextNode();
          CalendarEvent event = getEvent(eventNode);
          event.setCalType(String.valueOf(Calendar.TYPE_SHARED));
          recurEvents.add(event);
        }

      }
    }

    return recurEvents;
  }


  public List<CalendarEvent> searchHighLightOriginalRecurrenceEventsSQL(String username,java.util.Calendar from,
                                                                        java.util.Calendar to, EventQuery eventQuery,
                                                                        String[] privateCalendars, String[] publicCalendars) throws Exception {
    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();
    StringBuffer emptyCalendars = null;

    /** query private calendars */
    Node calendarHome = getUserCalendarHome(username);
    QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();

    for (String calendarId : privateCalendars) {
      String calendarPath = calendarHome.getPath() + "/" + calendarId;

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%/%'")
          .append(" AND jcr:mixinTypes='exo:repeatCalendarEvent'")
          .append(" AND NOT exo:repeat='norepeat'")
          .append(" AND (exo:repeatUntil IS NULL OR exo:repeatUntil >=  TIMESTAMP '" + ISO8601.format(from) +  "' )")
          .append(" AND (exo:repeatFinishDate IS NULL OR exo:repeatFinishDate >=  TIMESTAMP '" + ISO8601.format(from) +  "' )");

      /**
      StringBuilder queryEventsStatementXPath = new StringBuilder("/jcr:root").append(calendarPath)
          .append("/element(*,exo:repeatCalendarEvent) [@exo:repeat!='norepeat' and @exo:recurrenceId=''")
          .append(" and (not(@exo:repeatUntil) or @exo:repeatUntil >= xs:dateTime('" + ISO8601.format(from) + "'))")
          .append(" and (not(@exo:repeatFinishDate) or @exo:repeatFinishDate >= xs:dateTime('" + ISO8601.format(from) + "'))")
          .append("]");
      **/

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      Query query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);
      QueryResult result = query.execute();

      NodeIterator it = result.getNodes();
      if (!it.hasNext()) {

        if (emptyCalendars == null) {
          emptyCalendars = new StringBuffer();
          emptyCalendars.append(calendarId);
        }
        else emptyCalendars.append(";" + calendarId);
      }
      else {
        while (it.hasNext()) {
          Node eventNode = it.nextNode();
          CalendarEvent event = getEvent(eventNode);
          event.setCalType(String.valueOf(Calendar.TYPE_PRIVATE));
          recurEvents.add(event);
        }
      }
    }

    /** query public calendar evens */
    Node publicHome = getPublicCalendarHome();
    qm = publicHome.getSession().getWorkspace().getQueryManager();

    for (String calendarId : publicCalendars) {
      String calendarPath = publicHome.getPath() + "/" + calendarId;

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%/%'")
          .append(" AND jcr:mixinTypes='exo:repeatCalendarEvent'")
          .append(" AND NOT exo:repeat='norepeat'")
          .append(" AND (exo:repeatUntil IS NULL OR exo:repeatUntil >=  TIMESTAMP '" + ISO8601.format(from) +  "' )")
          .append(" AND (exo:repeatFinishDate IS NULL OR exo:repeatFinishDate >=  TIMESTAMP '" + ISO8601.format(from) +  "' )");

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      Lock lock = getLock("GroupCalendarRecurrentEvent", queryEventsStatementSQL.toString());
      lock.lock();
      List<CalendarEvent> events;

      try {
        events = groupCalendarRecurrentEventCache.get(queryEventsStatementSQL.toString());
        if (events == null) {
          Query query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);
          events = new ArrayList<CalendarEvent>();

          try {
            NodeIterator it = query.execute().getNodes();
            if (!it.hasNext()) {
              if (emptyCalendars == null) {
                emptyCalendars = new StringBuffer();
                emptyCalendars.append(calendarId);
              }
              else emptyCalendars.append(";" + calendarId);
            }
            else {

              while (it.hasNext()) {
                Node eventNode = it.nextNode();
                CalendarEvent event = getEvent(eventNode);
                event.setCalType(String.valueOf(Calendar.TYPE_PUBLIC));
                events.add(event);
              }

              groupCalendarRecurrentEventCache.put(queryEventsStatementSQL.toString(), events);
            }

          } catch (RepositoryException e) {
            log.debug("RepositoryException: " + e.getLocalizedMessage());
          } catch (Exception e) {
            log.debug("Exception: " + e.getLocalizedMessage());
          }
        }
      } finally {
        lock.unlock();
      }

      recurEvents.addAll(events);
    }

    /** query shared calendar event */
    Node sharedCalendarHome = getSharedCalendarHome();
    qm = sharedCalendarHome.getSession().getWorkspace().getQueryManager();

    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      PropertyIterator propertyIterator = userNode.getReferences();
      Node calendarNode;
      while (propertyIterator.hasNext()) {
        calendarNode = propertyIterator.nextProperty().getParent();

        StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
            .append(" WHERE jcr:path LIKE '").append(calendarNode.getPath()).append("/%'")
            .append(" AND NOT jcr:path LIKE '").append(calendarNode.getPath()).append("/%/%'")
            .append(" AND jcr:mixinTypes='exo:repeatCalendarEvent'")
            .append(" AND NOT exo:repeat='norepeat'")
            .append(" AND (exo:repeatUntil IS NULL OR exo:repeatUntil >=  TIMESTAMP '" + ISO8601.format(from) +  "' )")
            .append(" AND (exo:repeatFinishDate IS NULL OR exo:repeatFinishDate >=  TIMESTAMP '" + ISO8601.format(from) +  "' )");

        /** event type */
        String eventType = eventQuery.getEventType();
        if (eventType != null && eventType.length() > 0) {
          queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
        }

        /** event category */
        String[] categoryIds = eventQuery.getCategoryId();
        if (categoryIds != null && categoryIds.length > 0) {
          queryEventsStatementSQL.append(" AND (");
          for (int i = 0; i < categoryIds.length; i++) {
            if (i == 0)
              queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
            else
              queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
          }
          queryEventsStatementSQL.append(")");
        }

        Query query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);
        QueryResult result = query.execute();

        NodeIterator it = result.getNodes();
        while (it.hasNext()) {
          Node eventNode = it.nextNode();
          CalendarEvent event = getEvent(eventNode);
          event.setCalType(String.valueOf(Calendar.TYPE_SHARED));
          recurEvents.add(event);
        }


      }
    }

    if (emptyCalendars != null) {
      CalendarEvent emptyCalEvent = new CalendarEvent();
      emptyCalEvent.setCalendarId("-1");
      emptyCalEvent.setSummary(emptyCalendars.toString());
      recurEvents.add(0, emptyCalEvent);
    }

    return recurEvents;
  }


  /**
   * Get all active 'original' recurrence event <br/>
   * The result list includes all 'original' recurrence event in all three
   * types: private, public and shared
   * 
   * @param username
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getOriginalRecurrenceEvents(String username,
                                                         java.util.Calendar from,
                                                         java.util.Calendar to,
                                                         String[] publicCalendarIds) throws Exception {
    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();
    CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));

    /** get from user private calendars, with filter settings  */
    Node calendarHome = getUserCalendarHome(username);
    String[] filterIds = calSetting.getFilterPrivateCalendars();
    recurEvents.addAll(getOriginalRecurrenceEvents1(calendarHome,
                                                   String.valueOf(Calendar.TYPE_PRIVATE),
                                                   from,
                                                   to,
                                                   null,
                                                   filterIds));

    /** get from shared calendars, with filter settings */
    Node sharedCalendarHome = getSharedCalendarHome();
    filterIds = calSetting.getFilterSharedCalendars();
    List<String> filterIdList = null;
    if (filterIds != null && filterIds.length > 0)
      filterIdList = Arrays.asList(filterIds);
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      PropertyIterator iter = userNode.getReferences();
      Node calendar;
      while (iter.hasNext()) {
        calendar = iter.nextProperty().getParent();
        if (filterIdList != null
            && filterIdList.contains(calendar.getProperty(Utils.EXO_ID).getString()))
          continue;
        recurEvents.addAll(getOriginalRecurrenceEvents1(calendar,
                                                       String.valueOf(Calendar.TYPE_SHARED),
                                                       from,
                                                       to,
                                                       null,
                                                       null));
      }
    }

    /** get from public calendars, with filter settings */
    Node publicCalendarHome = getPublicCalendarHome();
    filterIds = calSetting.getFilterPublicCalendars();
    recurEvents.addAll(getOriginalRecurrenceEvents1(publicCalendarHome,
                                                   String.valueOf(Calendar.TYPE_PUBLIC),
                                                   from,
                                                   to,
                                                   publicCalendarIds,
                                                   filterIds));
    return recurEvents;
  }


  /**
   * A faster version getOriginalRecurrenceEvents with improved JCR queries
   *
   * @param calendar
   * @param calType
   * @param from
   * @param to
   * @param calendarIds
   * @param filterCalendarIds
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getOriginalRecurrenceEvents1(Node calendar, String calType, java.util.Calendar from,
                                                          java.util.Calendar to, String[] calendarIds, String[] filterCalendarIds) throws Exception {
    if (calendar == null) return null;

    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();
    StringBuilder queryString = new StringBuilder("/jcr:root").append(calendar.getPath())
        .append("//element(*,exo:repeatCalendarEvent) [@exo:repeat!='norepeat' and @exo:recurrenceId=''")
        .append(" and (not(@exo:repeatUntil) or @exo:repeatUntil >= xs:dateTime('" + ISO8601.format(from) + "'))")
        .append(" and (not(@exo:repeatFinishDate) or @exo:repeatFinishDate >= xs:dateTime('" + ISO8601.format(from) + "'))");

    /** search within calendars */
    if (calendarIds != null && calendarIds.length > 0) {
      queryString.append(" and (");
      for (int i = 0; i < calendarIds.length; i++) {
        if (i == 0)
          queryString.append("@exo:calendarId = '").append(calendarIds[i]).append("'");
        else
          queryString.append(" or @exo:calendarId = '").append(calendarIds[i]).append("'");
      }
      queryString.append(")");
    }

    /** exclude calendars */
    if (filterCalendarIds != null && filterCalendarIds.length > 0) {
      queryString.append(" and (");
      for (int i = 0; i < filterCalendarIds.length; i++) {
        if (i == 0)
          queryString.append("@exo:calendarId != '").append(filterCalendarIds[i]).append("'");
        else
          queryString.append(" and @exo:calendarId != '").append(filterCalendarIds[i]).append("'");
      }
      queryString.append(")");
    }

    /**  Add event query to this
    String eventType = eventQuery.getEventType();
    if (eventType != null && eventType.length() > 0) {
      queryStatement.append(" and (@exo:eventType='" + eventType + "')");
    }

    String categoryIds = eventQuery.getCategoryId();
    if (categoryIds != null && categoryIds.length > 0) {
      queryStatement.append(" and (");
      for (int i = 0; i < categoryIds.length; i++) {
        if (i == 0)
          queryStatement.append("@exo:eventCategoryId='").append(categoryIds[i]).append("'");
        else
          queryStatement.append(" or @exo:eventCategoryId='").append(categoryIds[i]).append("'");
      }
      queryStatement.append(")");
    }
     **/

    queryString.append("]");
    QueryManager qm = calendar.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();

    NodeIterator it = result.getNodes();
    while (it.hasNext()) {
      Node eventNode = it.nextNode();
      CalendarEvent event = getEvent(eventNode);
      event.setCalType(calType);
      recurEvents.add(event);
    }

    return recurEvents;
  }


  /**
   * Another version for getOriginalRecurrenceEvents1 using SQL with different queries
   *
   * @param calendar
   * @param calType
   * @param from
   * @param to
   * @param calendarIds
   *
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getOriginalRecurrenceEventsSQL(Node calendar, String calType, java.util.Calendar from,
                                                            java.util.Calendar to, EventQuery eventQuery,
                                                            String[] calendarIds, List<String> emptyCalendars) throws Exception {
    if (calendar == null) return null;

    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();

    for (String calendarId : calendarIds) {
      /** skip the calendar if it's empty */
      if (emptyCalendars!= null && emptyCalendars.contains(calendarId)) continue;

      String calendarPath = calendar.getPath() + "/" + calendarId;

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%/%'")
          .append(" AND jcr:mixinTypes='exo:repeatCalendarEvent'")
          .append(" AND NOT exo:repeat='norepeat'")
          .append(" AND (exo:repeatUntil IS NULL OR exo:repeatUntil >=  TIMESTAMP '" + ISO8601.format(from) +  "' )")
          .append(" AND (exo:repeatFinishDate IS NULL OR exo:repeatFinishDate >=  TIMESTAMP '" + ISO8601.format(from) +  "' )");

      StringBuilder queryEventsStatementXPath = new StringBuilder("/jcr:root").append(calendarPath)
          .append("/element(*,exo:repeatCalendarEvent) [@exo:repeat!='norepeat' and @exo:recurrenceId=''")
          .append(" and (not(@exo:repeatUntil) or @exo:repeatUntil >= xs:dateTime('" + ISO8601.format(from) + "'))")
          .append(" and (not(@exo:repeatFinishDate) or @exo:repeatFinishDate >= xs:dateTime('" + ISO8601.format(from) + "'))")
          .append("]");

      /** event type */
      String eventType = eventQuery.getEventType();
      if (eventType != null && eventType.length() > 0) {
        queryEventsStatementSQL.append(" AND (exo:eventType='" + eventType + "')");
      }

      /** event category */
      String[] categoryIds = eventQuery.getCategoryId();
      if (categoryIds != null && categoryIds.length > 0) {
        queryEventsStatementSQL.append(" AND (");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            queryEventsStatementSQL.append("exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            queryEventsStatementSQL.append(" OR exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        queryEventsStatementSQL.append(")");
      }

      QueryManager qm = calendar.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);

      //Query query = qm.createQuery(queryEventsStatementXPath.toString(), Query.XPATH);
      QueryResult result = query.execute();

      NodeIterator it = result.getNodes();
      while (it.hasNext()) {
        Node eventNode = it.nextNode();
        CalendarEvent event = getEvent(eventNode);
        event.setCalType(calType);
        recurEvents.add(event);
      }
    }

    return recurEvents;
  }


  public List<CalendarEvent> searchOriginalRecurrenceEventsSQL(Node calendar, String calType, java.util.Calendar from,
                                                               java.util.Calendar to, String[] calendarIds, List<String> emptyCalendars) throws Exception {
    if (calendar == null) return null;

    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();

    for (String calendarId : calendarIds) {
      String calendarPath = calendar.getPath() + "/" + calendarId;

      StringBuilder queryEventsStatementSQL = new StringBuilder(" SELECT * FROM ").append("exo:calendarEvent")
          .append(" WHERE jcr:path LIKE '").append(calendarPath).append("/%'")
          .append(" AND NOT jcr:path LIKE '").append(calendarPath).append("/%/%'")
          .append(" AND jcr:mixinTypes='exo:repeatCalendarEvent'")
          .append(" AND NOT exo:repeat='norepeat'")
          .append(" AND (exo:repeatUntil IS NULL OR exo:repeatUntil >=  TIMESTAMP '" + ISO8601.format(from) +  "' )")
          .append(" AND (exo:repeatFinishDate IS NULL OR exo:repeatFinishDate >=  TIMESTAMP '" + ISO8601.format(from) +  "' )");

      /**
      StringBuilder queryEventsStatementXPath = new StringBuilder("/jcr:root").append(calendarPath)
          .append("/element(*,exo:repeatCalendarEvent) [@exo:repeat!='norepeat' and @exo:recurrenceId=''")
          .append(" and (not(@exo:repeatUntil) or @exo:repeatUntil >= xs:dateTime('" + ISO8601.format(from) + "'))")
          .append(" and (not(@exo:repeatFinishDate) or @exo:repeatFinishDate >= xs:dateTime('" + ISO8601.format(from) + "'))")
          .append("]");
      **/

      QueryManager qm = calendar.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryEventsStatementSQL.toString(), Query.SQL);
      QueryResult result = query.execute();

      NodeIterator it = result.getNodes();
      if (!it.hasNext()) emptyCalendars.add(calendarId);
      else {
        while (it.hasNext()) {
          Node eventNode = it.nextNode();
          CalendarEvent event = getEvent(eventNode);
          event.setCalType(calType);
          recurEvents.add(event);
        }
      }
    }

    return recurEvents;
  }


  public List<CalendarEvent> getOriginalRecurrenceEvents(Node calendar,
                                                         String calType,
                                                         java.util.Calendar from,
                                                         java.util.Calendar to,
                                                         String[] calendarIds,
                                                         String[] filterCalendarIds) throws Exception {
    if (calendar == null)
      return null;
    List<CalendarEvent> recurEvents = new ArrayList<CalendarEvent>();
    StringBuilder queryString = new StringBuilder("/jcr:root").append(calendar.getPath())
        .append("//element(*,exo:repeatCalendarEvent)[@exo:repeat!='").append(CalendarEvent.RP_NOREPEAT)
                                                                .append("' and @exo:recurrenceId=''");
    if (from != null) {
      queryString.append(" and (not(@exo:repeatUntil) or @exo:repeatUntil >= xs:dateTime('"
          + ISO8601.format(from)).append("'))")
                 .append(" and (not(@exo:repeatFinishDate) or @exo:repeatFinishDate >= xs:dateTime('"
                     + ISO8601.format(from)).append("'))");
    } else {
      queryString.append(" and (not(@exo:repeatUntil))")
                 .append(" and (not(@exo:repeatFinishDate))");
    }
    if (calendarIds != null && calendarIds.length > 0) {
      queryString.append(" and (");
      for (int i = 0; i < calendarIds.length; i++) {
        if (i == 0)
          queryString.append("@exo:calendarId = '").append(calendarIds[i]).append("'");
        else
          queryString.append(" or @exo:calendarId = '").append(calendarIds[i]).append("'");
      }
      queryString.append(")");
    }

    if (filterCalendarIds != null && filterCalendarIds.length > 0) {
      queryString.append(" and (");
      for (int i = 0; i < filterCalendarIds.length; i++) {
        if (i == 0)
          queryString.append("@exo:calendarId != '").append(filterCalendarIds[i]).append("'");
        else
          queryString.append(" and @exo:calendarId != '").append(filterCalendarIds[i]).append("'");
      }
      queryString.append(")");
    }
    queryString.append("]");

    QueryManager qm = calendar.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    while (it.hasNext()) {
      Node eventNode = it.nextNode();
      CalendarEvent event = getEvent(eventNode);
      event.setCalType(calType);
      recurEvents.add(event);
    }
    return recurEvents;
  }

  /**
   * Get all exception occurrence from original recurrence event
   * 
   * @param username
   * @param recurEvent
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getExceptionEvents(String username, CalendarEvent recurEvent) throws Exception {

    if (recurEvent == null || recurEvent.getRepeatType().equals(CalendarEvent.RP_NOREPEAT))
      return null;
    try {
      Node calendarHome = null;
      int calType = Integer.parseInt(recurEvent.getCalType());
      Node eventNode = null;
      if (calType == Calendar.TYPE_PRIVATE) {
        calendarHome = getUserCalendarHome(username);
        eventNode = calendarHome.getNode(recurEvent.getCalendarId()).getNode(recurEvent.getId());
      }
      if (calType == Calendar.TYPE_SHARED) {
        calendarHome = getSharedCalendarHome();
        if (calendarHome.hasNode(username)) {
          PropertyIterator iter = calendarHome.getNode(username).getReferences();
          while (iter.hasNext()) {
            Node calendar = iter.nextProperty().getParent();
            if (!recurEvent.getCalendarId().equals(calendar.getProperty(Utils.EXO_ID)))
              continue;
            if (calendar.hasNode(recurEvent.getId())) {
              eventNode = calendar.getNode(recurEvent.getId());
            }
          }
        }
      }
      if (calType == Calendar.TYPE_PUBLIC) {
        calendarHome = getPublicCalendarHome();
        eventNode = calendarHome.getNode(recurEvent.getCalendarId()).getNode(recurEvent.getId());
      }

      if (eventNode == null || !eventNode.isNodeType(Utils.MIX_REFERENCEABLE))
        return null;
      PropertyIterator propIter = eventNode.getReferences();
      if (propIter == null)
        return null;
      List<CalendarEvent> exceptions = new ArrayList<CalendarEvent>();
      while (propIter.hasNext()) {
        Property prop = propIter.nextProperty();
        Node exceptionNode = prop.getParent();
        CalendarEvent exception = getEvent(exceptionNode);
        exception.setCalType(String.valueOf(calType));
        exceptions.add(exception);
      }
      return exceptions;
    } catch (Exception e) {
      log.error("Exception occurred when finding all exception occurrences", e);
      return null;
    }
  }

  /**
   * Query all occurrences of recurrence event between from and to date. <br/>
   * The result contains only 'virtual' occurrence events, no exception
   * occurrences
   * 
   * @param recurEvent the original recurrence event
   * @param from
   * @param to
   * @param timezone
   * @return
   * @throws Exception
   */
  public Map<String, CalendarEvent> getOccurrenceEvents(CalendarEvent recurEvent,
                                                        java.util.Calendar from,
                                                        java.util.Calendar to,
                                                        String timezone) throws Exception {
    if (Utils.isEmpty(recurEvent.getRepeatType()))
      return null;
    if (from.after(to) || !recurEvent.getFromDateTime().before(to.getTime())) {
      return null;
    }

    // check if this recurEvent is expired
    if (recurEvent.getRepeatUntilDate() != null
        && recurEvent.getRepeatUntilDate().before(from.getTime())) {
      return null;
    }

    TimeZone userTimeZone = TimeZone.getTimeZone(timezone);
    SimpleDateFormat format = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    format.setTimeZone(userTimeZone);
    
    Map<String, CalendarEvent> occurrences = new HashMap<String, CalendarEvent>();

    int diffMinutes = (int) ((recurEvent.getToDateTime().getTime() - recurEvent.getFromDateTime()
                                                                               .getTime()) / (60 * 1000));

    List<String> excludeIds = null;
    if (recurEvent.getExcludeId() != null && recurEvent.getExcludeId().length > 0) {
      excludeIds = new ArrayList<String>(Arrays.asList(recurEvent.getExcludeId()));
    }

    Recur recur = getICalendarRecur(recurEvent);
    if (recur == null)
      return null;

    DateTime ical4jEventFrom = new DateTime(recurEvent.getFromDateTime());//the date time of the first occurrence of the series
    net.fortuna.ical4j.model.TimeZone tz = Utils.getICalTimeZone(TimeZone.getTimeZone(timezone));
    ical4jEventFrom.setTimeZone(tz);
    
    Utils.adaptRepeatRule(recur, ical4jEventFrom, userTimeZone);
    
    java.util.Calendar occurenceFrom = java.util.Calendar.getInstance();
    occurenceFrom.setTime(from.getTime());
    
    // because occurrence event can begin before the 'from' but end after
    // 'from', so it still intersects with [from, to] window
    // thus, we decrease the 'from' value (by duration of event) to get such
    // occurrences
    occurenceFrom.add(java.util.Calendar.MINUTE, -(diffMinutes - 1));
    DateTime ical4jFrom = new DateTime(occurenceFrom.getTime());
    DateTime ical4jTo = new DateTime(to.getTime());
    Period period = new Period(ical4jFrom, ical4jTo);
    period.setTimeZone(tz);
    // get list of occurrences in a period
    DateList list = recur.getDates(ical4jEventFrom,
                                   period,
                                   net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
    
    for (Object dt : list) {
      DateTime ical4jStart = (DateTime) dt;
      ical4jStart.setTimeZone(tz);

      // make occurrence
      CalendarEvent occurrence = new CalendarEvent(recurEvent);

      java.util.Calendar startTime = java.util.Calendar.getInstance(TimeZone.getDefault());
      java.util.Calendar endTime = java.util.Calendar.getInstance(TimeZone.getDefault());
      startTime.setTimeInMillis(ical4jStart.getTime());
     
      occurrence.setFromDateTime(startTime.getTime());
      
      endTime.setTime(startTime.getTime());
      endTime.add(java.util.Calendar.MINUTE, diffMinutes);
      
      occurrence.setToDateTime(endTime.getTime());
      
      // if user time zone uses DST, need to adapt the time occurrence 
      if( userTimeZone.useDaylightTime()) {
        adaptTimeToDST(occurrence, recurEvent, userTimeZone);    
      }
      
      String recurId = format.format(occurrence.getFromDateTime());
      
      // if this occurrence was listed in the exclude list, skip
      if (excludeIds != null && excludeIds.contains(recurId))
    	  continue;
      
      occurrence.setRecurrenceId(recurId);
      occurrences.put(recurId, occurrence);
    }
    return occurrences;

  }

  // here we need to edit the occurrence to have correct time 
  // following the DST condition of created date and occur date
  private void adaptTimeToDST(CalendarEvent occurrence, CalendarEvent recurEvent, TimeZone userTimeZone) {
    java.util.Calendar from = java.util.Calendar.getInstance(userTimeZone);
    java.util.Calendar to = java.util.Calendar.getInstance(userTimeZone);
    from.setTime(occurrence.getFromDateTime());
    to.setTime(occurrence.getToDateTime());

    int dstUser = userTimeZone.getDSTSavings() / 3600000;

    Boolean originalUseDst = userTimeZone.inDaylightTime(recurEvent.getFromDateTime()); 
    Boolean occurenceUseDst = userTimeZone.inDaylightTime(occurrence.getFromDateTime());

    // if original created date uses DST but occur date does not use DST
    if(originalUseDst && !occurenceUseDst) {
      from.add(java.util.Calendar.HOUR, dstUser);
      to.add(java.util.Calendar.HOUR, dstUser);
    }

    // if occur date uses DST but original created date does not use DST
    if(!originalUseDst && occurenceUseDst) {
      from.add(java.util.Calendar.HOUR, -dstUser);
      to.add(java.util.Calendar.HOUR, -dstUser);
    }

    occurrence.setFromDateTime(from.getTime());
    occurrence.setToDateTime(to.getTime());
  }
  /**
   * Calculate the end-date of recurrence event
   * 
   * @param originalEvent
   * @return
   * @throws Exception
   */
  public Date calculateRecurrenceFinishDate(CalendarEvent originalEvent) throws Exception {
    try {
      if (originalEvent.getRepeatUntilDate() != null) {
        return originalEvent.getRepeatUntilDate();
      }

      // in case of repeat forever event
      if (originalEvent.getRepeatCount() <= 0) {
        return null;
      }

      DateTime ical4jEventFrom = new DateTime(originalEvent.getFromDateTime());
      VEvent vevent = new VEvent(ical4jEventFrom, Utils.EMPTY_STR);

      Recur recur = getICalendarRecur(originalEvent);

      vevent.getProperties().add(new RRule(recur));
      java.util.Calendar calendar = new GregorianCalendar(2011, 7, 1);
      calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      calendar.set(java.util.Calendar.YEAR, calendar.getMinimum(java.util.Calendar.YEAR));
      DateTime ical4jFrom = new DateTime(calendar.getTime());
      calendar.set(java.util.Calendar.YEAR, calendar.getMaximum(java.util.Calendar.YEAR));
      DateTime ical4jTo = new DateTime(calendar.getTime());
      Period period = new Period(ical4jFrom, ical4jTo);
      PeriodList list = vevent.calculateRecurrenceSet(period);
      if (list == null || list.size() == 0)
        return null;
      Period last = (Period) list.last();
      calendar.setTimeInMillis(last.getStart().getTime());
      calendar.add(java.util.Calendar.DATE, 1);
      calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
      calendar.set(java.util.Calendar.MINUTE, 0);
      return calendar.getTime();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Exception occurred when calculating finish date of recurrence event", e);
      return null;
    }
  }

  public Recur getICalendarRecur(CalendarEvent recurEvent) throws Exception {
    String repeatType = recurEvent.getRepeatType();
    // get the repeat count property of recurrence event
    int count = (int) recurEvent.getRepeatCount();

    java.util.Calendar until = null;
    if (recurEvent.getRepeatUntilDate() != null) {
      until = Utils.getInstanceTempCalendar();
      until.setTime(recurEvent.getRepeatUntilDate());
    }

    int interval = (int) recurEvent.getRepeatInterval();
    if (interval <= 1)
      interval = 1;

    Recur recur = null;

    // daily recurrence
    if (repeatType.equals(CalendarEvent.RP_DAILY)) {
      if (until != null) {
        recur = new Recur(Recur.DAILY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.DAILY, count);
        } else
          recur = new Recur("FREQ=DAILY");
      }
      recur.setInterval(interval);
      return recur;
    }

    // weekly recurrence
    if (repeatType.equals(CalendarEvent.RP_WEEKLY)) {
      if (until != null) {
        recur = new Recur(Recur.WEEKLY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.WEEKLY, count);
        } else
          recur = new Recur("FREQ=WEEKLY");
      }
      recur.setInterval(interval);

      // byday property
      String[] repeatByDay = recurEvent.getRepeatByDay();
      if (repeatByDay == null || repeatByDay.length == 0)
        return null;
      WeekDayList weekDayList = new WeekDayList();
      for (String s : repeatByDay) {
        weekDayList.add(new WeekDay(s));
      }
      recur.getDayList().addAll(weekDayList);
      return recur;
    }

    // monthly recurrence
    if (repeatType.equals(CalendarEvent.RP_MONTHLY)) {
      if (until != null) {
        recur = new Recur(Recur.MONTHLY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.MONTHLY, count);
        } else
          recur = new Recur("FREQ=MONTHLY");
      }
      recur.setInterval(interval);

      long[] repeatByMonthDay = recurEvent.getRepeatByMonthDay();
      // case 1: byMonthDay: day 1, 15, 26 of month
      if (repeatByMonthDay != null && repeatByMonthDay.length > 0) {
        NumberList numberList = new NumberList();
        for (long monthDay : repeatByMonthDay) {
          numberList.add(new Integer((int) monthDay));
        }
        recur.getMonthDayList().addAll(numberList);
      } else {
        // case 2: byDay: 1SU: first Sunday of month, -1TU: last Tuesday of
        // month
        String[] repeatByDay = recurEvent.getRepeatByDay();
        if (repeatByDay != null && repeatByDay.length > 0) {
          WeekDayList weekDayList = new WeekDayList();
          for (String s : repeatByDay) {
            weekDayList.add(new WeekDay(s));
          }
          recur.getDayList().addAll(weekDayList);
        }
      }
      return recur;
    }

    // yearly recurrence
    if (repeatType.equals(CalendarEvent.RP_YEARLY)) {
      if (until != null) {
        recur = new Recur(Recur.YEARLY, new net.fortuna.ical4j.model.Date(until.getTime()));
      } else {
        if (count > 0) {
          recur = new Recur(Recur.YEARLY, count);
        } else
          recur = new Recur("FREQ=YEARLY");
      }
      recur.setInterval(interval);
      return recur;
    }

    return recur;
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    Date fromDate = eventQuery.getFromDate().getTime();
    Date toDate = eventQuery.getToDate().getTime();
    Map<String, String> participantMap = new HashMap<String, String>();
    participantMap.putAll(checkFreeBusy(eventQuery, fromDate));
    if (!Utils.isSameDate(fromDate, toDate)) {
      Map<String, String> remainingInfo = checkFreeBusy(eventQuery, toDate);
      if (remainingInfo.size() > 0) {
        for (String par : remainingInfo.keySet()) {
          String newValue = remainingInfo.get(par);
          if (participantMap.containsKey(par))
            newValue += Utils.COMMA + participantMap.get(par);
          participantMap.put(par, newValue);
        }
      }
    }
    return participantMap;
  }

  public Map<String, String> checkFreeBusy(EventQuery eventQuery, Date date) throws Exception {
    Node eventFolder = getEventFolder(date);
    Map<String, String> participantMap = new HashMap<String, String>();
    String calendarPath = eventQuery.getCalendarPath();
    String[] orderBy = eventQuery.getOrderBy();
    eventQuery.setCalendarPath(eventFolder.getPath());
    eventQuery.setOrderBy(new String[] { Utils.EXO_FROM_DATE_TIME });
    QueryManager qm = getSession(createSystemProvider()).getWorkspace().getQueryManager();
    String[] pars = eventQuery.getParticipants();
    Query query;
    Node event;
    String from;
    String to;
    for (String par : pars) {
      String[] participants = eventQuery.getParticipants();
      eventQuery.setParticipants(new String[] { par });
      query = qm.createQuery(eventQuery.getQueryStatement(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      StringBuilder timeValues = new StringBuilder();
      while (it.hasNext()) {
        event = it.nextNode();
        if (event.hasProperty(Utils.EXO_EVENT_STATE)
            && !CalendarEvent.ST_AVAILABLE.equals(event.getProperty(Utils.EXO_EVENT_STATE)
                                                       .getValue()
                                                       .getString())) {
          java.util.Calendar fromCal = event.getProperty(Utils.EXO_FROM_DATE_TIME).getDate();
          java.util.Calendar toCal = event.getProperty(Utils.EXO_TO_DATE_TIME).getDate();
          if (fromCal.getTimeInMillis() < eventQuery.getFromDate().getTimeInMillis())
            from = String.valueOf(eventQuery.getFromDate().getTimeInMillis());
          else
            from = String.valueOf(fromCal.getTimeInMillis());
          if (toCal.getTimeInMillis() > eventQuery.getToDate().getTimeInMillis()) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(eventQuery.getToDate().getTimeInMillis() - 1000);
            to = String.valueOf(cal.getTimeInMillis());
          } else
            to = String.valueOf(toCal.getTimeInMillis());

          if (timeValues != null && timeValues.length() > 0)
            timeValues.append(",");
          timeValues.append(from).append(",").append(to);
          participantMap.put(par, timeValues.toString());
        }
      }
      eventQuery.setParticipants(participants);
    }
    eventQuery.setCalendarPath(calendarPath);
    eventQuery.setOrderBy(orderBy);
    return participantMap;
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      PropertyIterator iter = userNode.getReferences();
      Node calendar;
      while (iter.hasNext()) {
        calendar = iter.nextProperty().getParent();
        if (calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
          if (calendar.hasNode(eventId)) {
            Node event = calendar.getNode(eventId);
            Node eventFolder = getEventFolder(event.getProperty(Utils.EXO_FROM_DATE_TIME)
                                                   .getDate()
                                                   .getTime());
            syncRemoveEvent(eventFolder, eventId);
            removeReminder(event);
            event.remove();
          }
          calendar.save();
          calendar.refresh(true);
          break;
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void moveEvent(String formCalendar,
                        String toCalendar,
                        String fromType,
                        String toType,
                        List<CalendarEvent> calEvents,
                        String username) throws Exception {
    try {
      switch (Integer.parseInt(fromType)) {
      case Calendar.TYPE_PRIVATE:
        if (getUserCalendarHome(username).hasNode(formCalendar)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            // move events in side public calendars
            if (getUserCalendarHome(username).hasNode(toCalendar)) {
              for (CalendarEvent calEvent : calEvents) {
                if (!formCalendar.equals(toCalendar)) {
                  removeUserEvent(username, formCalendar, calEvent.getId());
                  calEvent.setCalendarId(toCalendar);
                  saveUserEvent(username,
                                toCalendar,
                                calEvent,
                                getUserCalendarHome(username).getNode(toCalendar)
                                                             .hasNode(calEvent.getId()));
                } else {
                  saveUserEvent(username, toCalendar, calEvent, false);
                }
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            // move events form public to shared calendar
            if (getSharedCalendarHome().hasNode(username)) {
              for (CalendarEvent calEvent : calEvents) {
                removeUserEvent(username, formCalendar, calEvent.getId());
                calEvent.setCalendarId(toCalendar);
                saveEventToSharedCalendar(username,
                                          toCalendar,
                                          calEvent,
                                          getSharedCalendarHome().getNode(username)
                                                                 .hasNode(calEvent.getId()));
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            // move events form public to public calendar
            if (getPublicCalendarHome().hasNode(toCalendar)) {
              for (CalendarEvent calEvent : calEvents) {
                removeUserEvent(username, formCalendar, calEvent.getId());
                calEvent.setCalendarId(toCalendar);
                savePublicEvent(toCalendar,
                                calEvent,
                                getPublicCalendarHome().getNode(toCalendar)
                                                       .hasNode(calEvent.getId()));
              }
            }
            break;
          default:
            break;
          }
        }
        break;
      case Calendar.TYPE_SHARED:
        if (getSharedCalendarHome().hasNode(username)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            // move events form share to public calendar
            if (getUserCalendarHome(username).hasNode(toCalendar)) {
              for (CalendarEvent calEvent : calEvents) {
                removeSharedEvent(username, formCalendar, calEvent.getId());
                calEvent.setCalendarId(toCalendar);
                saveUserEvent(username,
                              toCalendar,
                              calEvent,
                              getUserCalendarHome(username).getNode(toCalendar)
                                                           .hasNode(calEvent.getId()));
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            // move events in side shared calendars
            if (getSharedCalendarHome().hasNode(username)) {
              for (CalendarEvent calEvent : calEvents) {
                if (!formCalendar.equals(toCalendar)) {
                  removeSharedEvent(username, formCalendar, calEvent.getId());
                  calEvent.setCalendarId(toCalendar);
                  saveEventToSharedCalendar(username,
                                            toCalendar,
                                            calEvent,
                                            getSharedCalendarHome().getNode(username)
                                                                   .hasNode(calEvent.getId()));
                } else {
                  saveEventToSharedCalendar(username, toCalendar, calEvent, false);
                }
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            // move events form share to public calendar
            if (getPublicCalendarHome().hasNode(toCalendar)) {
              for (CalendarEvent calEvent : calEvents) {
                removeSharedEvent(username, formCalendar, calEvent.getId());
                calEvent.setCalendarId(toCalendar);
                savePublicEvent(toCalendar,
                                calEvent,
                                getPublicCalendarHome().getNode(toCalendar)
                                                       .hasNode(calEvent.getId()));
              }
            }
            break;
          default:
            break;
          }
        }
        break;
      case Calendar.TYPE_PUBLIC:
        if (getPublicCalendarHome().hasNode(formCalendar)) {
          switch (Integer.parseInt(toType)) {
          case Calendar.TYPE_PRIVATE:
            // move events from public to public calendar
            if (getUserCalendarHome(username).hasNode(toCalendar)) {
              for (CalendarEvent calEvent : calEvents) {
                removePublicEvent(formCalendar, calEvent.getId());
                calEvent.setCalendarId(toCalendar);
                saveUserEvent(username,
                              toCalendar,
                              calEvent,
                              getUserCalendarHome(username).getNode(toCalendar)
                                                           .hasNode(calEvent.getId()));
              }
            }
            break;
          case Calendar.TYPE_SHARED:
            // move events from public to shared calendar
            if (getSharedCalendarHome().hasNode(username)) {
              for (CalendarEvent calEvent : calEvents) {
                removePublicEvent(formCalendar, calEvent.getId());
                calEvent.setCalendarId(toCalendar);
                saveEventToSharedCalendar(username, toCalendar, calEvent, true);
              }
            }
            break;
          case Calendar.TYPE_PUBLIC:
            // move events in side public calendars
            if (getPublicCalendarHome().hasNode(toCalendar)) {
              for (CalendarEvent calEvent : calEvents) {
                if (!formCalendar.equals(toCalendar)) {
                  removePublicEvent(formCalendar, calEvent.getId());
                  calEvent.setCalendarId(toCalendar);
                  savePublicEvent(toCalendar,
                                  calEvent,
                                  getPublicCalendarHome().getNode(toCalendar)
                                                         .hasNode(calEvent.getId()));
                } else {
                  savePublicEvent(toCalendar, calEvent, false);
                }
              }
            }
            break;
          default:
            break;
          }
        }
        break;
      default:
        break;
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to move event", e);
      }
    } finally {
      // systemSession.close() ;
    }
  }

  /**
   * This function is used to update a occurrence event. <br/>
   * A occurrence event after update will be an exception occurrence from
   * recurrence series and will be saved as an new event node refers to original
   * node
   * 
   * @param fromCalendar
   * @param toCalendar
   * @param fromCalendarType
   * @param toCalendarType
   * @param events
   * @param username
   * @throws Exception
   */
  public void updateOccurrenceEvent(String fromCalendar,
                                    String toCalendar,
                                    String fromCalendarType,
                                    String toCalendarType,
                                    List<CalendarEvent> events,
                                    String username) throws Exception {
    try {
      int fromType = Integer.parseInt(fromCalendarType);
      int toType = Integer.parseInt(toCalendarType);
      for (CalendarEvent event : events) {
        if (event.getIsExceptionOccurrence() == null || !event.getIsExceptionOccurrence()) {
          // get the original recurrence event to update excludeId property
          CalendarEvent originalEvent = null;
          if (fromType == Calendar.TYPE_PRIVATE)
            originalEvent = getUserEvent(username, fromCalendar, event.getId());
          else if (fromType == Calendar.TYPE_SHARED)
            originalEvent = getSharedEvent(username, fromCalendar, event.getId());
          else if (fromType == Calendar.TYPE_PUBLIC)
            originalEvent = getGroupEvent(fromCalendar, event.getId());

          if (originalEvent != null) {
            List<String> excludeId;
            if (originalEvent.getExcludeId() == null) {
              excludeId = new ArrayList<String>();
            } else {
              excludeId = new ArrayList<String>(Arrays.asList(originalEvent.getExcludeId()));
            }
            excludeId.add(event.getRecurrenceId());
            originalEvent.setExcludeId(excludeId.toArray(new String[0]));

            if (fromType == Calendar.TYPE_PRIVATE)
              saveUserEvent(username, fromCalendar, originalEvent, false);
            else if (fromType == Calendar.TYPE_SHARED)
              saveEventToSharedCalendar(username, fromCalendar, originalEvent, false);
            else if (fromType == Calendar.TYPE_PUBLIC)
              savePublicEvent(fromCalendar, originalEvent, false);
          }
        }

        if (fromCalendar.equals(toCalendar)) {
          if (Utils.isExceptionOccurrence(event)) {
            saveOccurrenceEvent(username, toCalendar, event, false);
          } else {
            saveOccurrenceEvent(username, toCalendar, event, true);
          }
        } else {
          if (Utils.isExceptionOccurrence(event)) {
            if (fromType == Calendar.TYPE_PRIVATE)
              removeUserEvent(username, fromCalendar, event.getId());
            else if (fromType == Calendar.TYPE_SHARED)
              removeSharedEvent(username, fromCalendar, event.getId());
            else if (fromType == Calendar.TYPE_PUBLIC)
              removePublicEvent(fromCalendar, event.getId());
          }
          event.setCalendarId(toCalendar);
          event.setRepeatType(CalendarEvent.RP_NOREPEAT);
          event.setIsExceptionOccurrence(false);

          if (toType == Calendar.TYPE_PRIVATE)
            saveUserEvent(username, toCalendar, event, true);
          else if (toType == Calendar.TYPE_SHARED)
            saveEventToSharedCalendar(username, toCalendar, event, true);
          else if (toType == Calendar.TYPE_PUBLIC)
            savePublicEvent(toCalendar, event, true);
        }
      }
    } catch (Exception e) {
      log.error("Error occurred when updating occurrence event", e);
    }
  }

  /**
   * Update recurrence series
   * 
   * @param fromCalendar
   * @param toCalendar
   * @param fromType
   * @param toType
   * @param occurrence
   * @param username
   * @throws Exception
   */
  public void updateRecurrenceSeries(String fromCalendar,
                                     String toCalendar,
                                     String fromType,
                                     String toType,
                                     CalendarEvent occurrence,
                                     String username) throws Exception {
    try {
      String eventId = occurrence.getId();
      int calType = Integer.parseInt(fromType);
      int toCalType = Integer.parseInt(toType);
      CalendarEvent originalEvent = null;

      // get the original event
      if (calType == Calendar.TYPE_PRIVATE)
        originalEvent = getUserEvent(username, fromCalendar, eventId);
      else if (calType == Calendar.TYPE_PUBLIC)
        originalEvent = getGroupEvent(fromCalendar, eventId);
      else if (calType == Calendar.TYPE_SHARED)
        originalEvent = getSharedEvent(username, fromCalendar, eventId);
      // update original event from occurrence
      java.util.Calendar fromDate = Utils.getInstanceTempCalendar();
      fromDate.setTime(originalEvent.getFromDateTime());
      java.util.Calendar newFromDate = Utils.getInstanceTempCalendar();
      newFromDate.setTime(occurrence.getFromDateTime());
      fromDate.set(java.util.Calendar.HOUR_OF_DAY, newFromDate.get(java.util.Calendar.HOUR_OF_DAY));
      fromDate.set(java.util.Calendar.MINUTE, newFromDate.get(java.util.Calendar.MINUTE));
      originalEvent.setFromDateTime(fromDate.getTime());

      // calculate time amount
      java.util.Calendar newToDate = Utils.getInstanceTempCalendar();
      newToDate.setTime(occurrence.getToDateTime());
      int diffMinutes = (int) (newToDate.getTimeInMillis() - newFromDate.getTimeInMillis())
          / (60 * 1000);

      newToDate.setTime(fromDate.getTime());
      newToDate.add(java.util.Calendar.MINUTE, diffMinutes);
      originalEvent.setToDateTime(newToDate.getTime());

      originalEvent.setSummary(occurrence.getSummary());
      originalEvent.setDescription(occurrence.getDescription());
      originalEvent.setEventCategoryId(occurrence.getEventCategoryId());
      originalEvent.setEventCategoryName(occurrence.getEventCategoryName());
      originalEvent.setMessage(occurrence.getMessage());
      originalEvent.setLocation(occurrence.getLocation());
      List<Attachment> attachments = occurrence.getAttachment();
      originalEvent.setAttachment(attachments);
      originalEvent.setInvitation(occurrence.getInvitation());
      originalEvent.setParticipant(occurrence.getParticipant());
      originalEvent.setParticipantStatus(occurrence.getParticipantStatus());
      originalEvent.setReminders(occurrence.getReminders());
      originalEvent.setSendOption(occurrence.getSendOption());
      originalEvent.setStatus(occurrence.getStatus());
      originalEvent.setPriority(occurrence.getPriority());
      originalEvent.setRepeatType(occurrence.getRepeatType());
      originalEvent.setRepeatUntilDate(occurrence.getRepeatUntilDate());
      originalEvent.setRepeatCount(occurrence.getRepeatCount());
      originalEvent.setRepeatInterval(occurrence.getRepeatInterval());
      originalEvent.setRepeatByDay(occurrence.getRepeatByDay());
      originalEvent.setRepeatByMonthDay(occurrence.getRepeatByMonthDay());

      // if move occurrence to another calendar, same date
      if (!fromCalendar.equals(toCalendar)) {
        // remove original event from old calendar
        if (calType == Calendar.TYPE_PRIVATE)
          removeUserEvent(username, fromCalendar, eventId);
        else if (calType == Calendar.TYPE_PUBLIC)
          removePublicEvent(fromCalendar, eventId);
        else if (calType == Calendar.TYPE_SHARED)
          removeSharedEvent(username, fromCalendar, eventId);

        // save new original event to new calendar
        CalendarEvent newEvent = new CalendarEvent(originalEvent);
        newEvent.setCalendarId(toCalendar);
        newEvent.setExcludeId(originalEvent.getExcludeId());

        if (toCalType == Calendar.TYPE_PRIVATE)
          saveUserEvent(username, toCalendar, newEvent, true);
        else if (toCalType == Calendar.TYPE_SHARED)
          saveEventToSharedCalendar(username, toCalendar, newEvent, true);
        else if (toCalType == Calendar.TYPE_PUBLIC)
          savePublicEvent(toCalendar, newEvent, true);
      } else {
        // save original event
        if (calType == Calendar.TYPE_PRIVATE)
          saveUserEvent(username, toCalendar, originalEvent, false);
        else if (calType == Calendar.TYPE_SHARED)
          saveEventToSharedCalendar(username, toCalendar, originalEvent, false);
        else if (calType == Calendar.TYPE_PUBLIC)
          savePublicEvent(toCalendar, originalEvent, false);
      }
    } catch (Exception e) {
      log.error("Error when update recurrence series", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId,
                                String toUserId,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) {
    try {
      Map<String, String> pars = new HashMap<String, String>();
      CalendarEvent event = getInvitationEvent(calType, calendarId, eventId, fromUserId);
      if (event != null) {
        if (event.getParticipant() != null) {
          for (String id : event.getParticipant()) {
            pars.put(id, id);
          }
        }
        for (String s : toUserId.split(",")) {
          if (Utils.DENY == answer) {

            pars.remove(s);
          }
          if (Utils.ACCEPT == answer || Utils.NOTSURE == answer) {
            pars.put(s, s);
          }
        }
        event.setParticipant(pars.values().toArray(new String[pars.values().size()]));
        if (Calendar.TYPE_PRIVATE == calType) {
          saveUserEvent(fromUserId, calendarId, event, false);
        } else if (Calendar.TYPE_SHARED == calType) {
          saveEventToSharedCalendar(fromUserId, calendarId, event, false);
        } else if (Calendar.TYPE_PUBLIC == calType) {
          savePublicEvent(calendarId, event, false);
        }
      }
    } catch (Exception e) {
      log.error(String.format("Failed to confirm an invitation from user %s to user %s with event %s in calendar %s",
                              fromUserId,
                              toUserId,
                              eventId,
                              calendarId),
                e);
    } finally {
      // session.close() ;
    }
  }

  private CalendarEvent getInvitationEvent(int calType,
                                           String calendarId,
                                           String eventId,
                                           String fromUserId) throws Exception {
    CalendarEvent event = null;
    if (Calendar.TYPE_PRIVATE == calType) {
      event = getUserEvent(fromUserId, calendarId, eventId);
    } else if (Calendar.TYPE_SHARED == calType) {
      List<String> calendarIds = new ArrayList<String>();
      calendarIds.add(calendarId);
      for (CalendarEvent calEvent : getSharedEventByCalendars(fromUserId, calendarIds)) {
        if (calEvent.getId().equals(eventId)) {
          event = calEvent;
          break;
        }
      }
    } else if (Calendar.TYPE_PUBLIC == calType) {
      event = getGroupEvent(calendarId, eventId);
    }
    return event;
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId,
                                String confirmingEmail,
                                String confirmingUser,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {
    try {
      Map<String, String> pars = new HashMap<String, String>();
      CalendarEvent event = getInvitationEvent(calType, calendarId, eventId, fromUserId);
      if (event != null) {
        if (event.getParticipantStatus() != null) {
          for (String parStatus : event.getParticipantStatus()) {
            String[] entry = parStatus.split(":");
            if (entry.length > 1)
              pars.put(entry[0], entry[1]);
            else
              pars.put(entry[0], Utils.EMPTY_STR);
          }
        }
        String status = Utils.EMPTY_STR;
        switch (answer) {
        case Utils.DENY:
          status = Utils.STATUS_NO;
          break;
        case Utils.ACCEPT:
          status = Utils.STATUS_YES;
          break;
        case Utils.NOTSURE:
          status = Utils.STATUS_PENDING;
          break;
        default:
          break;
        }

        if (pars.containsKey(confirmingUser)) {
          pars.remove(confirmingUser);
          pars.put(confirmingUser, status);
        }
        if (pars.containsKey(confirmingEmail)) {
          pars.remove(confirmingEmail);
          pars.put(confirmingEmail, status);
        }
        Map<String, String> participant = new HashMap<String, String>();
        for (Entry<String, String> par : pars.entrySet()) {
          participant.put(par.getKey() + ":" + par.getValue(), Utils.EMPTY_STR);
        }
        event.setParticipantStatus(participant.keySet().toArray(new String[participant.keySet()
                                                                                      .size()]));
        if (Calendar.TYPE_PRIVATE == calType) {
          saveUserEvent(fromUserId, calendarId, event, false);
        } else if (Calendar.TYPE_SHARED == calType) {
          saveEventToSharedCalendar(fromUserId, calendarId, event, false);
        } else if (Calendar.TYPE_PUBLIC == calType) {
          savePublicEvent(calendarId, event, false);
        }
      }

    } catch (Exception e) {
      throw new Exception(e.getClass().toString(), e.fillInStackTrace());
    } finally {
    }
  }

  /**
   * {@inheritDoc}
   */
  public int getTypeOfCalendar(String userName, String calendarId) {
    try {
      getUserCalendarHome(userName).getNode(calendarId);
      return Utils.PRIVATE_TYPE;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Failed to find calendar %s from user node of user %s",
                                calendarId,
                                userName), e);
      }
    }
    try {
      getPublicCalendarHome().getNode(calendarId);
      return Utils.PUBLIC_TYPE;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Failed to find calendar %s from public node of user %s",
                                calendarId,
                                userName), e);
      }
    }
    try {
      Node sharedCalendarHome = getSharedCalendarHome();
      if (sharedCalendarHome.hasNode(userName)) {
        Node userNode = sharedCalendarHome.getNode(userName);
        PropertyIterator iter = userNode.getReferences();
        Node calendar;
        while (iter.hasNext()) {
          calendar = iter.nextProperty().getParent();
          if (calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId))
            return Utils.SHARED_TYPE;
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Failed to find calendar %s from shared node of user %s",
                                calendarId,
                                userName), e);
      }
    }
    return Utils.INVALID_TYPE;
  }

  /**
   * {@inheritDoc}
   */
  public SessionProvider createSessionProvider() {
    SessionProvider provider = sessionProviderService_.getSessionProvider(null);
    if (provider == null) {
      log.info("No user session provider was available, trying to use a system session provider");
      provider = sessionProviderService_.getSystemSessionProvider(null);
    }
    return SessionProvider.createSystemProvider();
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unused")
  public SessionProvider createUserProvider() {
    return  sessionProviderService_.getSessionProvider(null);
  }

  /**
   * {@inheritDoc}
   */
  public SessionProvider createSystemProvider() {
    return sessionProviderService_.getSystemSessionProvider(null);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unused")
  public void closeSessionProvider(SessionProvider sessionProvider) {
    if (sessionProvider != null) {
      sessionProvider.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }

  /**
   * {@inheritDoc}
   */
  public Session getSession(SessionProvider sprovider) throws Exception {
    ManageableRepository currentRepo = repoService_.getCurrentRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(),
                                currentRepo);
  }

  private String[] ValuesToStrings(Value[] Val) throws Exception {
    if (Val.length == 1)
      return new String[] { Val[0].getString() };
    String[] Str = new String[Val.length];
    for (int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }

  public void autoShareCalendar(List<String> groupsOfUser, String receiver) throws Exception {
    Node sharedHome = getSharedCalendarHome();
    NodeIterator userNodes = sharedHome.getNodes();
    List<String> sharedCalendars = new ArrayList<String>();
    while (userNodes.hasNext()) {
      Node sharedNode = userNodes.nextNode();
      PropertyIterator iter = sharedNode.getReferences();
      while (iter.hasNext()) {
        try {
          Node calendarNode = iter.nextProperty().getParent();
          if (!sharedCalendars.contains(calendarNode.getProperty(Utils.EXO_ID).getString())) {
            sharedCalendars.add(calendarNode.getProperty(Utils.EXO_ID).getString());
            Value[] viewPers = calendarNode.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues();
            for (Value viewPer : viewPers) {
              for (String groupId : groupsOfUser) {
                if (viewPer.getString().equals(groupId)) {
                  Node sharedCalendarHome = getSharedCalendarHome();
                  Value[] values = {};
                  if (calendarNode.isNodeType(Utils.EXO_SHARED_MIXIN)) {
                    values = calendarNode.getProperty(Utils.EXO_SHARED_ID).getValues();
                  } else {
                    calendarNode.addMixin(Utils.EXO_SHARED_MIXIN);
                  }
                  List<Value> valueList = new ArrayList<Value>();
                  for (int i = 0; i < values.length; i++) {
                    Value value = values[i];
                    valueList.add(value);
                  }
                  valueList = calculateSharedCalendar(receiver,
                                                      calendarNode,
                                                      values,
                                                      valueList,
                                                      sharedCalendarHome);
                  if (valueList.size() > 0) {
                    calendarNode.setProperty(Utils.EXO_SHARED_ID,
                                             valueList.toArray(new Value[valueList.size()]));
                    calendarNode.save();
                    sharedCalendarHome.getSession().save();
                    calendarNode.getSession().save();
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Exception in method autoShareCalendar", e);
          }
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      String uuid = userNode.getProperty("jcr:uuid").getString();
      PropertyIterator iter = userNode.getReferences();
      Node calendar;
      CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));
      Map<String, String> map = new HashMap<String, String>();
      for (String key : calSetting.getSharedCalendarsColors()) {
        map.put(key.split(":")[0], key.split(":")[1]);
      }
      while (iter.hasNext()) {
        calendar = iter.nextProperty().getParent();
        if (calendar.getProperty(Utils.EXO_ID).getString().equals(calendarId)) {
          deleteSharedCalendar(username, uuid, calSetting, calendar, map);

          List<String> viewPerms = new ArrayList<String>();
          if (calendar.hasProperty(Utils.EXO_VIEW_PERMISSIONS)) {
            Value[] viewValues = calendar.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues();
            for (Value v : viewValues) {
              if (v.getString() != null && !v.getString().equals(username))
                viewPerms.add(v.getString());
            }
          }
          List<String> editPerms = new ArrayList<String>();
          if (calendar.hasProperty(Utils.EXO_EDIT_PERMISSIONS)) {
            Value[] editValues = calendar.getProperty(Utils.EXO_EDIT_PERMISSIONS).getValues();
            for (Value v : editValues) {
              if (v.getString() != null && !v.getString().equals(username))
                editPerms.add(v.getString());
            }
          }
          calendar.setProperty(Utils.EXO_VIEW_PERMISSIONS,
                               viewPerms.toArray(new String[viewPerms.size()]));
          calendar.setProperty(Utils.EXO_EDIT_PERMISSIONS,
                               editPerms.toArray(new String[editPerms.size()]));
          calendar.getSession().save();
          calendar.refresh(true);
          break;
        }
      }
      try {
        removeFeed(username, calendarId);
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to remove feed", e);
        }
      }
    }
  }

  public void removeSharedCalendarFolder(String username) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      String uuid = userNode.getProperty("jcr:uuid").getString();
      PropertyIterator propIter = userNode.getReferences();
      while (propIter.hasNext()) {
        Property prop = propIter.nextProperty();
        Node calendar = prop.getParent();
        Value[] sharedIds = prop.getValues();
        List<Value> newValues = new ArrayList<Value>();
        for (Value value : sharedIds) {
          if (!value.getString().equals(uuid)) {
            newValues.add(value);
          }
        }
        calendar.setProperty(Utils.EXO_SHARED_ID, newValues.toArray(new Value[newValues.size()]));
        newValues.clear();

        if (calendar.hasProperty(Utils.EXO_VIEW_PERMISSIONS)) {
          Value[] viewPerms = calendar.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues();
          for (Value v : viewPerms) {
            if (!username.equals(v.getString())) {
              newValues.add(v);
            }
          }
          calendar.setProperty(Utils.EXO_VIEW_PERMISSIONS,
                               newValues.toArray(new Value[newValues.size()]));
          newValues.clear();
        }

        if (calendar.hasProperty(Utils.EXO_EDIT_PERMISSIONS)) {
          Value[] editPerms = calendar.getProperty(Utils.EXO_EDIT_PERMISSIONS).getValues();
          for (Value v : editPerms) {
            if (!username.equals(v.getString())) {
              newValues.add(v);
            }
          }
          calendar.setProperty(Utils.EXO_EDIT_PERMISSIONS,
                               newValues.toArray(new Value[newValues.size()]));
        }
        calendar.save();
      }
      userNode.remove();
      sharedCalendarHome.save();
    }
  }

  public void autoRemoveShareCalendar(String groupId, String username) throws Exception {
    Node sharedCalendarHome = getSharedCalendarHome();
    if (sharedCalendarHome.hasNode(username)) {
      Node userNode = sharedCalendarHome.getNode(username);
      String uuid = userNode.getProperty("jcr:uuid").getString();
      PropertyIterator iter = userNode.getReferences();
      CalendarSetting calSetting = getCalendarSetting(getUserCalendarServiceHome(username));
      Node calendar;
      OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()
                                                                                         .getComponentInstanceOfType(OrganizationService.class);
      Map<String, String> map = new HashMap<String, String>();
      for (String key : calSetting.getSharedCalendarsColors()) {
        map.put(key.split(":")[0], key.split(":")[1]);
      }
      while (iter.hasNext()) {
        calendar = iter.nextProperty().getParent();
        List<String> viewPers = new ArrayList<String>();
        for (Value value : calendar.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues()) {
          viewPers.add(value.getString());
        }
        viewPers.remove(groupId);
        boolean deleteShared = true;
        if (viewPers.contains(username))
          deleteShared = false;
        else {
          Object[] groups = organizationService.getGroupHandler()
                                               .findGroupsOfUser(username)
                                               .toArray();
          for (Object object : groups) {
            if (viewPers.contains(((Group) object).getId())) {
              deleteShared = false;
              break;
            }
          }
        }
        if (deleteShared) {
          for (Value viewPer : calendar.getProperty(Utils.EXO_VIEW_PERMISSIONS).getValues())
            if (viewPer.getString().equals(groupId)) {
              deleteSharedCalendar(username, uuid, calSetting, calendar, map);

              calendar.getSession().save();
              calendar.refresh(true);
              break;
            }
          try {
            removeFeed(username, calendar.getProperty(Utils.EXO_ID).getString());
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Fail to remove feed", e);
            }
          }
        }
      }
    }
  }

  private void deleteSharedCalendar(String username,
                                    String userNodeUuid,
                                    CalendarSetting calSetting,
                                    Node calendarNode,
                                    Map<String, String> colorMap) throws Exception {
    colorMap.remove(calendarNode.getProperty(Utils.EXO_ID).getString());

    Value[] values = calendarNode.getProperty(Utils.EXO_SHARED_ID).getValues();
    List<Value> newValues = new ArrayList<Value>();
    for (Value value : values) {
      if (!value.getString().equals(userNodeUuid)) {
        newValues.add(value);
      }
    }
    List<String> calColors = new ArrayList<String>();
    for (String key : colorMap.keySet()) {
      calColors.add(key + Utils.COLON + colorMap.get(key));
    }
    calSetting.setSharedCalendarsColors(calColors.toArray(new String[calColors.size()]));
    saveCalendarSetting(calSetting, username);
    calendarNode.setProperty(Utils.EXO_SHARED_ID, newValues.toArray(new Value[newValues.size()]));
  }

  public void assignGroupTask(String taskId, String calendarId, String assignee) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId);
    Node eventNode = calendarNode.getNode(taskId);
    String taskDelegator = null;
    if (eventNode.hasProperty(Utils.EXO_TASK_DELEGATOR))
      taskDelegator = eventNode.getProperty(Utils.EXO_TASK_DELEGATOR).getString();
    if (assignee != null && assignee.length() > 0) {
      if (taskDelegator == null || taskDelegator.trim().length() == 0) {
        taskDelegator = assignee;
      } else {
        taskDelegator += "," + assignee;
      }
      eventNode.setProperty(Utils.EXO_TASK_DELEGATOR, taskDelegator);
      eventNode.getSession().save();
    }
  }

  public void setGroupTaskStatus(String taskId, String calendarId, String status) throws Exception {
    Node calendarNode = getPublicCalendarHome().getNode(calendarId);
    Node eventNode = calendarNode.getNode(taskId);
    if (status != null && status.length() > 0) {
      eventNode.setProperty(Utils.EXO_EVENT_STATE, status);
      eventNode.getSession().save();
    }
  }

  public boolean isRemoteCalendar(String username, String calendarId) throws Exception {
    try {
      Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
      if (calendarNode.isNodeType(Utils.EXO_REMOTE_MIXIN))
        return true;
      return false;
    } catch (Exception e) {
      log.debug(e.getMessage());
      return false;
    }
  }

  @Override
  public Calendar updateRemoteCalendarInfo(RemoteCalendar remoteCalendar) throws Exception {
    Node calendarNode = getUserCalendarHome(remoteCalendar.getUsername()).getNode(remoteCalendar.getCalendarId());
    updateRemoteCalendarInfo(calendarNode, remoteCalendar);
    return getUserCalendar(remoteCalendar.getUsername(), remoteCalendar.getCalendarId());
  }

  private void updateRemoteCalendarInfo(Node calendarNode, RemoteCalendar remoteCalendar) throws Exception {
    calendarNode.setProperty(Utils.EXO_REMOTE_TYPE, remoteCalendar.getType());
    calendarNode.setProperty(Utils.EXO_DESCRIPTION, remoteCalendar.getDescription());
    calendarNode.setProperty(Utils.EXO_REMOTE_URL, remoteCalendar.getRemoteUrl());
    calendarNode.setProperty(Utils.EXO_REMOTE_SYNC_PERIOD, remoteCalendar.getSyncPeriod());
    calendarNode.setProperty(Utils.EXO_REMOTE_USERNAME, (remoteCalendar.getRemoteUser()));
    calendarNode.setProperty(Utils.EXO_REMOTE_PASSWORD, (remoteCalendar.getRemotePassword()));
    calendarNode.setProperty(Utils.EXO_REMOTE_AFTER_DATE, (remoteCalendar.getAfterDateSave()));
    calendarNode.setProperty(Utils.EXO_REMOTE_BEFORE_DATE, (remoteCalendar.getBeforeDateSave()));
    if (remoteCalendar.getLastUpdated() != null)
      calendarNode.setProperty(Utils.EXO_REMOTE_LAST_UPDATED, remoteCalendar.getLastUpdated());
    else if (!calendarNode.hasProperty(Utils.EXO_REMOTE_LAST_UPDATED))
      calendarNode.setProperty(Utils.EXO_REMOTE_LAST_UPDATED, Utils.getInstanceTempCalendar());
    calendarNode.save();
  }

  public RemoteCalendar getRemoteCalendar(String username, String remoteCalendarId) throws Exception {
    RemoteCalendar remoteCalendar = new RemoteCalendar();
    try {
      Node calendarNode = getUserCalendarHome(username).getNode(remoteCalendarId);
      remoteCalendar.setUsername(username);
      remoteCalendar.setCalendarId(remoteCalendarId);
      StringBuilder namePattern = new StringBuilder(128);
      namePattern.append(Utils.EXO_REMOTE_TYPE).append('|').append(Utils.EXO_REMOTE_SYNC_PERIOD).append('|')
           .append(Utils.EXO_REMOTE_BEFORE_DATE).append('|').append(Utils.EXO_REMOTE_AFTER_DATE).append('|')
           .append(Utils.EXO_REMOTE_URL).append('|').append(Utils.EXO_REMOTE_USERNAME).append('|')
           .append(Utils.EXO_REMOTE_PASSWORD).append('|').append(Utils.EXO_REMOTE_LAST_UPDATED);
      PropertyIterator it = calendarNode.getProperties(namePattern.toString());
      while (it.hasNext()) {
        Property p = it.nextProperty();
        String name = p.getName();
        if (name.equals(Utils.EXO_REMOTE_TYPE)) {
          remoteCalendar.setType(p.getString());
        } else if (name.equals(Utils.EXO_REMOTE_SYNC_PERIOD)) {
          remoteCalendar.setSyncPeriod(p.getString());
        } else if (name.equals(Utils.EXO_REMOTE_BEFORE_DATE)) {
          remoteCalendar.setBeforeDateSave(p.getString());
        } else if (name.equals(Utils.EXO_REMOTE_AFTER_DATE)) {
          remoteCalendar.setAfterDateSave(p.getString());
        } else if (name.equals(Utils.EXO_REMOTE_URL)) {
          remoteCalendar.setRemoteUrl(p.getString());
        } else if (name.equals(Utils.EXO_REMOTE_USERNAME)) {
          remoteCalendar.setRemoteUser(p.getString());
        } else if (name.equals(Utils.EXO_REMOTE_PASSWORD)) {
          remoteCalendar.setRemotePassword(p.getString());
        } else if (name.equals(Utils.EXO_REMOTE_LAST_UPDATED)) {
          remoteCalendar.setLastUpdated(p.getDate());
        }
      }
    } catch (Exception e) {
      log.debug("Failed to get RemoteCalendar by user.", e);
    }
    return remoteCalendar;
  }

  public Calendar createRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception {
    Calendar eXoCalendar = new Calendar();
    eXoCalendar.setName(remoteCalendar.getCalendarName());
    eXoCalendar.setPublic(false);
    eXoCalendar.setCalendarOwner(remoteCalendar.getUsername());
    eXoCalendar.setDescription(remoteCalendar.getDescription());
    eXoCalendar.setCalendarColor(remoteCalendar.getCalendarColor());
    saveUserCalendar(remoteCalendar.getUsername(), eXoCalendar, true);

    // add mixin type exo:remoteCalendar to this calendar
    Node calendarNode = getUserCalendarHome(remoteCalendar.getUsername()).getNode(eXoCalendar.getId());
    if (!calendarNode.isNodeType(Utils.EXO_REMOTE_MIXIN)) {
      calendarNode.addMixin(Utils.EXO_REMOTE_MIXIN);
    }
    updateRemoteCalendarInfo(calendarNode, remoteCalendar);
    return eXoCalendar;
  }

  public void setRemoteEvent(String username,
                             String calendarId,
                             String eventId,
                             String href,
                             String etag) throws Exception {
    Node eventNode = getUserCalendarHome(username).getNode(calendarId).getNode(eventId);
    if (!eventNode.isNodeType(Utils.EXO_REMOTE_EVENT_MIXIN)) {
      eventNode.addMixin(Utils.EXO_REMOTE_EVENT_MIXIN);
    }
    if (href != null) {
      eventNode.setProperty(Utils.EXO_CALDAV_HREF, href);
    }
    eventNode.setProperty(Utils.EXO_CALDAV_ETAG, etag);
    eventNode.save();
  }

  @Override
  public void setRemoteCalendarLastUpdated(String username,
                                           String calendarId,
                                           java.util.Calendar timeGMT) throws Exception {
    Node calendarNode = getUserCalendarHome(username).getNode(calendarId);
    calendarNode.setProperty(Utils.EXO_REMOTE_LAST_UPDATED, timeGMT);
    calendarNode.save();
  }

  public Calendar getRemoteCalendar(String username, String remoteUrl, String remoteType) throws Exception {
    try {
      Node calendarHome = getUserCalendarHome(username);
      String queryString = new StringBuilder("/jcr:root" + calendarHome.getPath()
          + "//element(*,exo:remoteCalendar)[@exo:remoteUrl='").append(remoteUrl)
                                                               .append("' and @exo:remoteType='")
                                                               .append(remoteType)
                                                               .append("']")
                                                               .toString();
      QueryManager queryManager = calendarHome.getSession().getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.XPATH);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      if (iter.hasNext()) {
        Node calNode = iter.nextNode();
        Calendar calendar = getCalendar(null, username, calNode, true);
        return calendar;
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public int getRemoteCalendarCount(String username) throws Exception {
    try {
      Node calendarHome = getUserCalendarHome(username);
      String queryString = new StringBuilder("/jcr:root").append(calendarHome.getPath())
          .append("//element(*,exo:remoteCalendar)").toString();
      QueryManager queryManager = calendarHome.getSession().getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.XPATH);
      QueryResult results = query.execute();
      NodeIterator iter = results.getNodes();
      int count = 0;
      while (iter.hasNext()) {
        iter.nextNode();
        count++;
      }
      return count;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e);
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.calendar.service.DataStorage#setCalDavResourceHref(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void setCalDavResourceHref(String username, String calendarId, String eventId, String href) throws Exception {
    Node eventNode = getUserCalendarHome(username).getNode(calendarId).getNode(eventId);
    eventNode.setProperty(Utils.EXO_CALDAV_HREF, href);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.calendar.service.DataStorage#setCalDavResourceEtag(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void setCalDavResourceEtag(String username, String calendarId, String eventId, String etag) throws Exception {
    Node eventNode = getUserCalendarHome(username).getNode(calendarId).getNode(eventId);
    eventNode.setProperty(Utils.EXO_CALDAV_HREF, etag);
  }
  
  public CalendarEvent getEventById(String eventId) throws Exception {
    Node calendarApp = Utils.getPublicServiceHome(Utils.createSystemProvider());
    QueryManager queryManager = calendarApp.getSession().getWorkspace().getQueryManager();
    String sql = "select * from exo:calendarEvent where exo:id=" + "\'" + eventId + "\'";
    Query query = queryManager.createQuery(sql, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator nodesIt = result.getNodes();
    if(nodesIt.hasNext()) {
      return getEvent(nodesIt.nextNode());
    } else {
      return null;
    }
  }

  @Override
  public Calendar getCalendarById(String calId) throws Exception {
    Node calendarApp = Utils.getPublicServiceHome(Utils.createSystemProvider());
    QueryManager queryManager = calendarApp.getSession().getWorkspace().getQueryManager();
    String sql = "select * from exo:calendar where exo:id=" + "\'" + calId + "\'";
    Query query = queryManager.createQuery(sql, Query.SQL);
    QueryResult result = query.execute();
    NodeIterator nodesIt = result.getNodes();
    if(nodesIt.hasNext()) {
      return loadCalendar(nodesIt.nextNode());
    } else {
      return null;
    }
  }
  /**
   * This kind of locks can self unregister from the map of locks
   */
  private class InternalLock extends ReentrantLock {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -3362387346368015145L;

    /**
     * The id corresponding to the lock in the map
     */
    private final String fullId;

    /**
     * The default constructor
     * @param fullId the id corresponding to the lock in the map
     */
    public InternalLock(String fullId) {
      super();
      this.fullId = fullId;
    }

    @Override
    public void unlock() {
      if (!hasQueuedThreads()) {
        // No thread is currently waiting for this lock
        // The lock will then be removed
        locks.remove(fullId, this);
      }
      super.unlock();
    }
  }
}
