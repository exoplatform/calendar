/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.calendar.service;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Session;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 19, 2010  
 */

public interface DataStorage {

  final public static String USERS_PATH = "usersPath".intern();

  /**
   * Get the public Calendar application storage
   * @return the node that is on top of Calendar application public storage
   * @throws Exception
   */
  public Node getPublicCalendarServiceHome() throws Exception;

  /**
   * Get the shared Calendar application storage
   * @return the node that is on top of Calendar application shared storage
   * @throws Exception
   */
  public Node getSharedCalendarHome() throws Exception;

  /**
   * 
   * @return
   * @throws Exception
   */
  public Node getPublicRoot() throws Exception;

  /**
   * Get the Calendar application user data storage root
   * @param username
   * @return Node object represents the node that is on top of user data storage
   * @throws Exception
   */
  public Node getUserCalendarServiceHome(String username) throws Exception;

  /**
   * Get the home node of all public calendars under public Calendar storage
   * @return Node object represents the home node of all public calendars
   * @throws Exception
   */
  public Node getPublicCalendarHome() throws Exception;

  /**
   * Get the home node of user calendars under user calendar data storage
   * @param username
   * @return Node object represents home node of user calendars
   * @throws Exception
   */
  public Node getUserCalendarHome(String username) throws Exception;

  /**
   * Get the home node of user feeds in Calendar application
   * @param username
   * @return Node object represents the home node of user feeds
   * @throws Exception
   */
  public Node getRssHome(String username) throws Exception;

  /**
   * Get a calendar under user calendar data storage
   * @param username
   * @param calendarId
   * @return the Calendar object represents user calendar
   * @throws Exception
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception;

  /**
   * Get all calendars of a user
   * @param username
   * @param isShowAll
   * @return the list of all calendars of user
   * @throws Exception
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception;

  /**
   * Save a user calendar
   * @param username
   * @param calendar
   * @param isNew
   * @throws Exception
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception;

  /**
   * Remove a user calendar
   * @param username
   * @param calendarId
   * @return the Calendar which is removed
   * @throws Exception
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception;

  /**
   * Get a group calendar
   * @param calendarId
   * @return the group calendar
   * @throws Exception
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception;

  /**
   * 
   * @param groupIds
   * @param isShowAll
   * @param username
   * @return the list of GroupCalendarData objects
   * @throws Exception
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds, boolean isShowAll, String username) throws Exception;

  /**
   * Save a public calendar
   * @param calendar
   * @param isNew
   * @param username
   * @throws Exception
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception;

  /**
   * Remove a group calendar
   * @param calendarId
   * @return
   * @throws Exception
   */
  public Calendar removeGroupCalendar(String calendarId) throws Exception;

  /**
   * 
   * @param defaultFilterCalendars
   * @param username
   * @param calNode
   * @param isShowAll
   * @return Calendar object
   * @throws Exception
   */
  public Calendar getCalendar(String[] defaultFilterCalendars, String username, Node calNode, boolean isShowAll) throws Exception;

  // Event Category APIs

  /**
   * Get all event categories of a user
   * @param username
   * @return the list of EventCategory objects
   * @throws Exception
   */
  public List<EventCategory> getEventCategories(String username) throws Exception;

  /**
   * Save a user event category to JCR
   * @param username
   * @param eventCategory
   * @param isNew
   * @throws Exception
   */
  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception;

  /**
   * Remove a user event category
   * @param username
   * @param eventCategoryName
   * @throws Exception
   */
  public void removeEventCategory(String username, String eventCategoryName) throws Exception;

  /**
   * Get a event category from node
   * @param eventCatNode
   * @return the EventCategory object
   * @throws Exception
   */
  public EventCategory getEventCategory(Node eventCatNode) throws Exception;

  /**
   * Get event category from name
   * @param username
   * @param eventCategoryName
   * @return the EventCategory object
   * @throws Exception
   */
  public EventCategory getEventCategory(String username, String eventCategoryName) throws Exception;

  /**
   * Get a user event from calendarId and eventId
   * @param username
   * @param calendarId
   * @param eventId
   * @return the CalendarEvent
   * @throws Exception
   */
  public CalendarEvent getUserEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * Get all user events from a calendar
   * @param username
   * @param calendarIds
   * @return the list of CalendarEvent object
   * @throws Exception
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception;

  /**
   * Get all user public events from a category
   * @param username
   * @param eventCategoryId
   * @return the list of CalendarEvent 
   * @throws Exception
   */
  public List<CalendarEvent> getPublicEventByCategory(String username, String eventCategoryId) throws Exception;

  /**
   * Get all user shared events from a category
   * @param username
   * @param eventCategoryId
   * @return the list of CalendarEvent
   * @throws Exception
   */
  public List<CalendarEvent> getSharedEventByCategory(String username, String eventCategoryId) throws Exception;

  /**
   * Get all user events from a category
   * @param username
   * @param eventCategoryId
   * @return the list of CalendarEvent
   * @throws Exception
   */
  public List<CalendarEvent> getUserEventByCategory(String username, String eventCategoryId) throws Exception;

  /**
   * Get a user event
   * @param username
   * @param eventId
   * @return the CalendarEvent
   * @throws Exception
   */
  public CalendarEvent getEvent(String username, String eventId) throws Exception;

  /**
   * Get all user event with a event query
   * @param username
   * @param eventQuery
   * @return the list of CalendarEvent
   * @throws Exception
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception;

  /**
   * Save a user event
   * @param username
   * @param calendarId
   * @param event
   * @param isNew
   * @throws Exception
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Remove a user event
   * @param username
   * @param calendarId
   * @param eventId
   * @return the CalendarEvent which is removed
   * @throws Exception
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * Remove reminder from event
   * @param eventNode
   * @throws Exception
   */
  public void removeReminder(Node eventNode) throws Exception;

  /**
   * Get a group event with given eventId
   * @param eventId 
   * @return CalendarEvent object, or null if event was not found
   * @throws Exception
   */
  public CalendarEvent getGroupEvent(String eventId) throws Exception;

  /**
   * Get a group event
   * @param calendarId
   * @param eventId
   * @return the CalendarEvent
   * @throws Exception
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception;

  /**
   * Get all group events from list of calendars
   * @param calendarIds
   * @return the list of CalendarEvent objects
   * @throws Exception
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception;

  /**
   * Get all public events from a event query
   * @param eventQuery
   * @return the list of CalendarEvent objects
   * @throws Exception
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception;

  /**
   * Save a public event
   * @param calendarId
   * @param event
   * @param isNew
   * @throws Exception
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Remove a public event
   * @param calendarId
   * @param eventId
   * @return CalendarEvent object
   * @throws Exception
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception;

  /**
   * Get a event from event node
   * @param eventNode
   * @return the CalendarEvent object
   * @throws Exception
   */
  public CalendarEvent getEvent(Node eventNode) throws Exception;

  /**
   * Save a event
   * @param calendarNode
   * @param event
   * @param reminderFolder
   * @param isNew
   * @throws Exception
   */
  public void saveEvent(Node calendarNode, CalendarEvent event, Node reminderFolder, boolean isNew) throws Exception;

  /**
   * Add a reminder to event
   * @param eventNode
   * @param reminderFolder
   * @param reminder
   * @throws Exception
   */
  public void addReminder(Node eventNode, Node reminderFolder, Reminder reminder) throws Exception;

  /**
   * Add a event
   * @param event
   * @throws Exception
   */
  public void addEvent(CalendarEvent event) throws Exception;

  /**
   * @param eventFolder
   * @param rootEventId
   * @throws Exception
   */
  public void syncRemoveEvent(Node eventFolder, String rootEventId) throws Exception;

  /**
   * @param fromDate
   * @return
   * @throws Exception
   */
  public Node getReminderFolder(Date fromDate) throws Exception;

  /**
   * @param fromDate
   * @return
   * @throws Exception
   */
  public Node getEventFolder(Date fromDate) throws Exception;

  /**
   * @param publicApp
   * @param date
   * @return Node object
   * @throws Exception
   */
  public Node getDateFolder(Node publicApp, Date date) throws Exception;

  /** 
   * Add an attachment to event
   * @param eventNode the event node
   * @param attachment the attachment to add
   * @param isNew is it a new attachment?
   * @throws Exception
   */
  public void addAttachment(Node eventNode, Attachment attachment, boolean isNew) throws Exception;

  /**
   * Save calendar setting of a user to JCR
   * @param username the username
   * @param setting the setting to save
   * @throws Exception
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception;

  /**
   * @param calendarHome
   * @param setting
   * @throws Exception
   */
  public void addCalendarSetting(Node calendarHome, CalendarSetting setting) throws Exception;

  /**
   * Get calendar settings of a user
   * @param username
   * @return the CalendarSetting objects
   * @throws Exception
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception;

  /**
   * @param feedXML
   * @param rssHome
   * @param rssData
   * @throws Exception
   */
  public void storeXML(String feedXML, Node rssHome, RssData rssData) throws Exception;

  /**
   * @param username
   * @param calendarId
   * @throws Exception
   */
  public void removeFeed(String username, String calendarId) throws Exception;

  /**
   * @param username
   * @return
   * @throws Exception
   */
  public List<FeedData> getFeeds(String username) throws Exception;

  /**
   * @param username
   * @param calendarIds
   * @param rssData
   * @param importExport
   * @return
   * @throws Exception
   */
  public int generateRss(String username, List<String> calendarIds, RssData rssData, CalendarImportExport importExport) throws Exception;

  /**
   * @param username
   * @param calendars
   * @param rssData
   * @param importExport
   * @return
   * @throws Exception
   */
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData, CalendarImportExport importExport) throws Exception;

  /**
   * @param portalName
   * @param wsName
   * @param username
   * @param path
   * @param baseUrl
   * @return
   * @throws Exception
   */
  public String getEntryUrl(String portalName, String wsName, String username, String path, String baseUrl) throws Exception;

  /**
   * @param username
   * @param eventQuery
   * @param publicCalendarIds
   * @return
   * @throws Exception
   */
  public EventPageList searchEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * @param username
   * @param eventQuery
   * @param publicCalendarIds
   * @return
   * @throws Exception
   */
  public Map<Integer, String> searchHightLightEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * Shared a calendar to list of users
   * @param username
   * @param calendarId
   * @param receiverUsers
   * @throws Exception
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception;

  /**
   * Get all shared calendars of a user
   * @param username
   * @param isShowAll
   * @return the GroupCalendarData object contains list of all shared calendars belong to user
   * @throws Exception
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception;

  /**
   * Save a shared calendar to JCR
   * @param username
   * @param calendar
   * @throws Exception
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception;

  /**
   * Get all shared events with a EventQuery
   * @param username
   * @param eventQuery
   * @return the list of CalendarEvent objects
   * @throws Exception
   */
  public List<CalendarEvent> getSharedEvents(String username, EventQuery eventQuery) throws Exception;

  /**
   * Get all shared events from list of calendars
   * @param username
   * @param calendarIds
   * @return the list of CalendarEvent objects
   * @throws Exception
   */
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception;

  /**
   * Remove a shared calendar
   * @param username the username
   * @param calendarId the calendar id
   * @throws Exception
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception;

  /**
   * Save a event to a shared calendar
   * @param username
   * @param calendarId
   * @param event
   * @param isNew
   * @throws Exception
   */
  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Get the permission to edit of user on the calendar
   * @param calNode
   * @param username
   * @return
   * @throws Exception
   */
  public boolean canEdit(Node calNode, String username) throws Exception;

  /**
   * @param username
   * @param eventQuery
   * @param publicCalendarIds
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * @param eventQuery
   * @return
   * @throws Exception
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception;

  /**
   * @param username
   * @param calendarId
   * @param eventId
   * @throws Exception
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * Move events between calendars
   * @param formCalendar
   * @param toCalendar
   * @param fromType
   * @param toType
   * @param calEvents
   * @param username
   * @throws Exception
   */
  public void moveEvent(String formCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception;

  /**
   * @param fromUserId
   * @param toUserId
   * @param calType
   * @param calendarId
   * @param eventId
   * @param answer
   */
  public void confirmInvitation(String fromUserId, String toUserId, int calType, String calendarId, String eventId, int answer);

  /**
   * @param fromUserId
   * @param confirmingEmail
   * @param confirmingUser
   * @param calType
   * @param calendarId
   * @param eventId
   * @param answer
   * @throws Exception
   */
  public void confirmInvitation(String fromUserId, String confirmingEmail, String confirmingUser, int calType, String calendarId, String eventId, int answer) throws Exception;

  /**
   * @param userName
   * @param calendarId
   * @return
   */
  public int getTypeOfCalendar(String userName, String calendarId);

  /**
   * Create a session provider for current context. The method first try to get a normal session provider, 
   * then attempts to create a system provider if the first one was not available.
   * @return a SessionProvider initialized by current SessionProviderService
   * see SessionProviderService#getSessionProvider(null)
   */
  public SessionProvider createSessionProvider();

  public SessionProvider createUserProvider();

  public SessionProvider createSystemProvider();

  /**
   * Safely closes JCR session provider. Call this method in finally to clean any provider initialized by createSessionProvider()
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  public void closeSessionProvider(SessionProvider sessionProvider);

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception;

  public Session getSession(SessionProvider sprovider) throws Exception;

  public void autoShareCalendar(List<String> groupsOfUser, String reciever) throws Exception;

  public void autoRemoveShareCalendar(String groupId, String username) throws Exception;

  /**
   * Check if a calendar is a remote calendar by checking mixin type of calendar node
   * @param username owner of this calendar
   * @param calendarId id of this calendar
   * @return true if calendar node has mixin type exo:remoteCalendar, false if otherwise
   * @throws Exception
   */
  public boolean isRemoteCalendar(String username, String calendarId) throws Exception;

  /**
   * Update information about remote calendar
   * @param remoteCalendar object content all properties for remote calendar.
   * @return
   * @throws Exception
   */
  public Calendar updateRemoteCalendarInfo(RemoteCalendar remoteCalendar) throws Exception;

  /**
   * Create a new eXo calendar with mixin type 'exo:remoteCalendar' to store data from remote calendar, this method also creates a 'Remote' category
   * @param remoteCalendar object content all properties for remote calendar.
   * @return Calendar object
   * @throws Exception
   */
  public Calendar createRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception;

  public void setRemoteEvent(String username, String calendarId, String eventId, String href, String etag) throws Exception;

  /**
   * @param username
   * @param calendarId
   * @param timeGMT
   * @throws Exception
   */
  public void setRemoteCalendarLastUpdated(String username, String calendarId, java.util.Calendar timeGMT) throws Exception;

  public Calendar getRemoteCalendar(String username, String remoteUrl, String remoteType) throws Exception;

  public int getRemoteCalendarCount(String username) throws Exception;

  public void setCalDavResourceHref(String username, String calendarId, String eventId, String href) throws Exception;

  public void setCalDavResourceEtag(String username, String calendarId, String eventId, String etag) throws Exception;
  
  public Calendar getCalendarById(String calId) throws Exception;
  
  public Collection<CalendarEvent> getAllExcludedEvent(CalendarEvent originEvent,java.util.Calendar from, java.util.Calendar to, String userId);
  
  public Collection<CalendarEvent> buildSeriesByTime(CalendarEvent originEvent,java.util.Calendar from, java.util.Calendar to, String userId);
  
}
