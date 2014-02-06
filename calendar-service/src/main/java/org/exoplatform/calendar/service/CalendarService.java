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
package org.exoplatform.calendar.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.jcr.Node;
import org.exoplatform.calendar.service.impl.CalendarEventListener;
import org.exoplatform.calendar.service.impl.CsvImportExport;
import org.exoplatform.calendar.service.impl.NewMembershipListener;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.quartz.JobDetail;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */

public interface CalendarService {

  final public static String ICALENDAR   = "ICalendar(.ics)";

  final public static String EXPORTEDCSV = "ExportedCsv(.csv)";

  final public static String CALDAV      = "CalDAV";

  /**
   * Gets the given user's private calendar, identified by its ID.
   * @param username current user name(or user id)
   * @param calendarId Id of the calendar
   * @return The private Calendar of the given user identified by the given id
   * @throws Exception
   * @see Calendar
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception;

  /**
   * Gets private calendars of the given user.
   * <p> The result depends on value of <code>isShowAll</code> parameter. If <code>isShowAll</code> <br>
   * is <b>true</b>, this method returns all private calendars of this user, otherwise it returns <br>
   * only calendars selected to be displayed in Calendar setting
   * 
   * @param username current user name(or user id)
   * @param isShowAll If <code>true</code>, returns all private calendars. If <code>false</code>, returns <br/>
   * only calendars that are selected in Calendar Setting
   * @return List of Calendar objects
   * @throws Exception
   * @see Calendar
   * @see CalendarSetting
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception;


  /**
   * Saves an user's private calendar to storage
   * 
   * @param username current user name(or user id)
   * @param calendar Calendar object that will be stored
   * @param isNew Boolean value to know adding a new calendar or just updating this calendar
   * @throws Exception
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception;

  /**
   * Removes private calendar by given id, all events and tasks belong to this calendar will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @return
   * @throws Exception
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception;

  /**
   * Saves a calendar to shared calendars section
   * @param username current user name(or user id)
   * @param calendar Calendar object
   * @throws Exception
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception;

  /**
   * Gets a group/public calendar by its Id
   * @param calendarId given calendar id
   * @return Calendar object 
   * @throws Exception
   * @see Calendar
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception;

  /**
   * Gets all the group calendar data of current user
   * <p> The {@link GroupCalendarData} contains information about list of calendars with the <br>
   * group that those calendars belong to.
   * @param groupIds The id of groups that current user belongs to
   * @param isShowAll to specify getting all calendars or only calendars selected in Calendar user setting
   * @param username current user name(or user id)
   * @return List of GroupCalendarData and each GroupCalendarData contains list of calendar objects
   * @throws Exception
   * @see GroupCalendarData
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds, boolean isShowAll, String username) throws Exception;

  /**
  * Saves a calendar to public area (group calendar)
  * @param calendar Calendar to be saved
  * @param isNew If <code>true</code>, a new calendar will be saved. If <code>false</code>, an existing calendar will be updated.
  * @throws Exception
  */
  public void savePublicCalendar(Calendar calendar, boolean isNew) throws Exception;

  /**
   * Removes the group calendar form data base, every events, tasks inside this calendar will be removed too
   * @param calendarId
   * @return
   * @throws Exception
   */
  public Calendar removePublicCalendar(String calendarId) throws Exception;

  /**
   * Gets all event categories of an user
   * @param username current user name(or user id)
   * @return list of event categories
   * @throws Exception
   * @see EventCategory
   */
  public List<EventCategory> getEventCategories(String username) throws Exception;

  /**
   * Saves an event category to data base
   * <p>Every users have their own categories of events. The name of category is unique. 
   * @param username current user name(or user id)
   * @param eventCategory
   * @param isNew
   * @throws Exception
   */
  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception;

  /**
   * Removes an event category, all events and tasks belong to this category will be removed.
   * @param username current user name(or user id)
   * @param eventCategoryId The unique name of category
   * @throws Exception
   */
  public void removeEventCategory(String username, String eventCategoryId) throws Exception;

  /**
   * Gets event category by its id and its owner
   * @param username user id of the event category's owner
   * @param eventCategoryId Id of the event category
   * @return an EventCategory object
   * @throws Exception
   * @see EventCategory
   */
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception;

  /**
   * Gets event category by its name and its owner
   * @param username Id of the owner
   * @param eventCategoryName The name of the category
   * @return EventCategory object
   * @throws Exception
   * @see EventCategory
   */
  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception;

  /**
   * Gets all events and tasks from a list of private calendars
   * @param username The owner of the calendar
   * @param calendarIds List of calendar id
   * @return List of events and tasks
   * @throws Exception
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception;

  /**
   * Gets events and tasks that match the conditions in the given EventQuery
   * <p> Each property of the <code>EventQuery</code> contains a condition of the query. For example:
   * <ul>
   * <li><code>text</code>: to search events that have some fields containing this value.</li>
   * <li><code>calendarIds</code>: array of calendar IDs in which to search events.</li>
   * <li>...</li>
   * </ul>
   * After setting value for those properties, the conditions are built in a query statement by the <br/>
   * method {@link EventQuery#getQueryStatement()} 
   * 
   * @param username current user name(or user id)
   * @param eventQuery EventQuery object containing the conditions
   * @return List of <code>CalendarEvent</code> object (events and tasks)
   * @throws Exception
   * @see CalendarEvent
   * @see EventQuery
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception;

  /**
   * Gets event/task by its id and its owner
   * @param username user id of the event owner
   * @param eventId id of the event
   * @return CalendarEvent in the personal calendar of owner 
   * @throws Exception 
   */
  public CalendarEvent getEvent(String username, String eventId) throws Exception;

  /**
   * Saves events/tasks to a personal calendar
   * @param username current user name(or user id)
   * @param calendarId Id of the calendar to which the event will be saved
   * @param event <code>CalendarEvent</code> object to be saved
   * @param isNew If <code>true</code>, a new event will be saved. If <code>false</code>, an existing event will be updated.
   * @throws Exception
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Removes an event from the personal calendar
   * <p>All attachments and reminders will be removed
   * @param username current user name(or user id)
   * @param calendarId Id of the calendar from which the event will be removed.
   * @param eventId Id of the removed event.
   * @return the removed <code>CalendarEvent</code> object. Null if no event was removed.
   * @throws Exception
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * Gets a group event by its Id
   * @param eventId
   * @return <code>CalendarEvent</code> object or null if event was not found
   * @throws Exception
   */
  public CalendarEvent getGroupEvent(String eventId) throws Exception;

  /**
   * Gets event or task from a given group calendar by its id
   * @param calendarId given group calendar id
   * @param eventId given event id
   * @return <code>CalendarEvent</code> object
   * @throws Exception
   * @see CalendarEvent
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception;

  /**
   * Gets all events and tasks from a list of public calendars  
   * @param calendarIds List of Calendar IDs
   * @return List of <code>CalendarEvent</code> objects
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception;

  /**
   * Gets all public events and tasks that match the conditions in the given EventQuery
   * @param eventQuery EventQuery object
   * @return List of calendar event objects
   * @throws Exception
   * @see CalendarEvent
   * @see EventQuery
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception;

  /**
   * Saves event or task to a public calendar
   * @param calendarId Id of the public calendar to which the event will be saved
   * @param event <code>CalendarEvent</code> object to be saved.
   * @param isNew If <code>true</code>, a new event will be saved. If <code>false</code>, an existing event will be updated.
   * @throws Exception
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Removes a public event or task, all attachments and reminders of this event will be removed
   * @param calendarId given calendar id
   * @param eventId given event or task id
   * @return the removed CalendarEvent. Null if no event was removed.
   * @throws Exception
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception;

  /**
   * Saves User Calendar setting
   * @param username current user name(or user id)
   * @param setting CalendarSetting object
   * @throws Exception
   * @see CalendarSetting
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception;

  /**
   * Gets calendar settings of an user
   * @param username current user name(or user id)
   * @return CalendarSetting object
   * @throws Exception
   * @see CalendarSetting
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception;

  /**
   * Gets the object performing import/export by type of import/export.
   * <p> There are 2 classes that implements the interface CalendarImportExport:
   * <ul>
   * <li> {@link ICalendarImportExport} </li>
   * <li> {@link CsvImportExport} </li>
   * </ul>
   * @param type ICS or CSV 
   * @return a CalendarImportExport object that belongs to 1 of those 2 above classes.
   * @see CalendarImportExport
   */
  public CalendarImportExport getCalendarImportExports(String type);

  /**
   * Gets array of supported types of import/export 
   * <p> Now there are 2 types that are supported: ICS and CSV.
   * @return array of supported types
   * @throws Exception
   * @see CalendarImportExport
   * @see RemoteCalendarService
   */
  public String[] getExportImportType() throws Exception;

  /**
   * Generates RSS Feed link for list of calendars following RSS standard and stores the feed in storage
   * <p> This method is called when user selects calendars to generate RSS link in UIEditFeed form.
   * @param username current user name(or user id)
   * @param calendars the RSS feed will contain events and tasks from the calendars given in this param
   * @param rssData object containing basic informations (url, title, description) of the feed
   * @return 1 if succeed, -1 if fail
   * @throws Exception
   * @see RssData
   */
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception;

  /**
   * Generates RSS Feed data for list of calendars following RSS standard and stores the feed in storage
   * @param username current user name(or user id)
   * @param calendarIds the RSS feed will contain events and tasks from the calendars given in this param
   * @param rssData object containing basic informations (url, title, description) of the feed
   * @return 1 if succeed, -1 if fail
   * @throws Exception
   * @see RssData
   */
  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception;

  /**
   * Gets RSS Feed data created by the given user.
   * @param username current user name (or user id)
   * @return List of FeedData
   * @throws Exception
   * @see FeedData
   */
  public List<FeedData> getFeeds(String username) throws Exception;

  /**
   * Gets RSS home node where RSS data is stored in data storage.
   * @param username current user name(or user id)
   * @return Rss home node
   * @throws Exception
   */
  public Node getRssHome(String username) throws Exception;

  /**
   * Gets an <code>EventPageList</code> of events/tasks of a given list of public calendars that matches the condition <br> 
   * in the given <code>EvenQuery</code> object. 
   * @param username current user name(or user id)
   * @param eventQuery <code>EventQuery</code> object
   * @param publicCalendarIds Array of public calendar IDs in which to search events
   * @return <code>EventPageList</code> object.
   * @throws Exception
   * @see EventPageList
   */
  public EventPageList searchEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * Gets the day in month on which there are events.
   * <p> This method is used when UIMiniCalendar is loaded or updated. We need to know on which day there are events <br>
   * to add  class 'highlight' for that day in the template.
   * <p> The given <code>EventQuery</code> always has from date is the first day of the month, and end date is the last <br>
   * day of the month. The returned result is a Map with key set is the days having events, the values are all "value".
   * @param username current user name(or user id)
   * @param eventQuery <code>EventQuery</code> object
   * @param publicCalendarIds array of public calendar IDs of which to search events
   * @return a <code>Map</code> with key set is the days having events. Ex: <code>{<14,"value">, <15,"value">}</code>
   * @throws Exception
   */
  public Map<Integer, String> searchHightLightEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;


  public List<Map<Integer, String>> searchHightLightEventSQL(String username, EventQuery eventQuery,
                                                             String[] privateCalendars, String[] publicCalendars) throws Exception;


  /**
   * Shares the private calendar to other users
   * @param username current user name(or user id)
   * @param calendarId Id of the shared calendar
   * @param sharedUsers list of users with whom to share this calendar
   * @throws Exception
   */
  public void shareCalendar(String username, String calendarId, List<String> sharedUsers) throws Exception;

  /**
   * Gets all calendars that are shared with the given user
   * @param username given user name(or user id)
   * @param isShowAll If <code>true</code>, returns all shared calendars, if <code>false</code>, returns only shared calendars
   * that are selected in Calendar Setting.
   * @return <code>GroupCalendarData</code> object
   * @throws Exception
   * @see GroupCalendarData
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception;

  /**
   * Gets all the events and tasks that match the conditions in the given <code>EventQuery</code> object <br>
   * <p> The result includes events of private, public and share calendars.
   * <p> If <code>publicCalendarIds</code> is not null, the result will include also all public events from those <br>
   * public calendar.
   * @param username current user name(or user id)
   * @param eventQuery <code>EventQuery</code> object
   * @param publicCalendarIds Optional array of public calendar IDs of which to get events
   * @return
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * A faster version of getEvents used for UIWeekview
   *
   * @param username
   * @param eventQuery
   * @param publicCalendarIds
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getAllNoRepeatEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;


  /**
   * A faster version of getAllNoRepeatEvents without thread
   *
   * @param username
   * @param eventQuery
   * @param privateCalendars
   * @param publicCalendars
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getAllNoRepeatEventsSQL(String username, EventQuery eventQuery,
                                                     String[] privateCalendars, String[] publicCalendars, List<String> emptyCalendars) throws Exception;

  /**
   * Removes shared calendars of an user
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @throws Exception
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception;
  
  /**
   * Removes the shared calendar folder of an user and all references (from shared calendars) to this folder.   
   * @param username the username
   * @throws Exception
   */
  public void removeSharedCalendarFolder(String username) throws Exception;

  /**
   * Saves event to shared calendar.
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param event <code>CalendarEvent</code> object
   * @param isNew If <code>true</code>, a new event will be saved. If <code>false</code>, an existing event will be updated.
   * @throws Exception
   */
  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Gets busy time of participants in a period
   * <p> The list of participants and the period are given in an <code>EventQuery</code> object.
   * <p> The returned result is a <code>Map<String,String></code> with the key is user name of participants <br>
   * and the value is pairs of <code>{fromtime, totime}</code> in milliseconds , separated by ','.  
   * @param eventQuery <code>EventQuery</code> object
   * @return a </code>Map<String,String></code> with the key is user name and value is  
   * @throws Exception 
   * @see EventQuery
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception;

  /**
   * Removes event/task from shared calendar
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param eventId given event id
   * @throws Exception
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * Saves changes for list of events.
   * <p> This method can be used in 2 cases: <br/>
   * <ul>
   * <li>Moving events between 2 calendars</li>
   * <li>Editing/updating events. In this case, from calendar and to calendar are the same</li>
   * </ul>
   * @param fromCalendar Id of the source calendar
   * @param toCalendar  Id of the destination calendar
   * @param formType type of the source calendar (private/shared/public)
   * @param toType type of the destination calendar (private/shared/public)
   * @param calEvents List of <code>CalendarEvent</code> objects
   * @param username current user name(or user id)
   * @throws Exception
   */
  public void moveEvent(String fromCalendar, String toCalendar, String formType, String toType, List<CalendarEvent> calEvents, String username) throws Exception;

  /**
   * Confirms invitation to participate in an event
   * <p>This method is called only when user uses exo mail product. <br/>
   * <p>The answer can be: DENY, ACCEPT, NOTSURE
   * 
   * @param fromUserId Id of the invitation owner
   * @param toUserId Id of the invited user
   * @param calType type of the invitation event's calendar
   * @param calendarId Id of the invitation event's calendar
   * @param eventId Id of the invitation event
   * @param answer The answer of the invited user
   * @deprecated
   */
  public void confirmInvitation(String fromUserId, String toUserId, int calType, String calendarId, String eventId, int answer);

  /**
   * Confirms an invitation to participate in an event
   * @param fromUserId Id of the invitation owner
   * @param confirmingEmail Email of the invited participant
   * @param confirmingUser User id of the invited participant
   * @param calType Type of the invitation event's calendar
   * @param calendarId Id of the invitation event's calendar
   * @param eventId Id of the invitation event
   * @param answer The answer of the invited user. It can be: DENY/ACCEPT/NOT_SURE
   * @throws Exception
   */
  public void confirmInvitation(String fromUserId, String confirmingEmail, String confirmingUser, int calType, String calendarId, String eventId, int answer) throws Exception;

  public void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception;

  public void addEventListenerPlugin(CalendarEventListener listener) throws Exception;

  /**
   * Gets type of a calendar by user name and calendar id
   * <p> There a 3 types of calendar: <br/>
   * <ul>
   * <li>Private calendar - returned value: 0</li>
   * <li>Shared calendar - returned value: 1</li>
   * <li>Group/Public calendar - returned value: 2</li>
   * </ul>
   * @param userName
   * @param calendarId
   * @return 0 if the calendar is private, 1 if the calendar is shared, 2 if the calendar is public
   * @throws Exception
   * @see Calendar
   */
  public int getTypeOfCalendar(String userName, String calendarId) throws Exception;

  /**
   * Gets events in shared calendars
   * @param username current user name
   * @param calendarIds list of shared calendars from which to get events
   * @return list of <code>CalendarEvent</code> object
   * @throws Exception
   */
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception;

  /**
   * Get shared event by user name, calendar id and event id
   * @param username current user name
   * @param calendarId id of shared calendar
   * @param eventId id of shared event
   * @return the <code>CalendarEvent</code> object
   * @throws Exception
   */
  public CalendarEvent getSharedEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * Removes the feed data of an user by the feed's title
   * @param username user id of the feed owner
   * @param title The title of the removed feed
   */
  public void removeFeedData(String username, String title);

  /**
   * Gets the resource bundle to support localization
   * @return a <code>ResourceBundle</code> object
   * @throws Exception
   */
  public ResourceBundle getResourceBundle() throws Exception;

  /**
   * Initializes calendar data for a new created user
   * @param userName User id of the new created user
   * @param defaultCalendarSetting default calendar setting
   * @throws Exception
   */
  public void initNewUser(String userName, CalendarSetting defaultCalendarSetting) throws Exception;

  /**
   * Assigns a group task for an user
   * @param taskId the assigned task
   * @param calendarId Id of the task's calendar
   * @param assignee User id of the assignee
   * @throws Exception
   */
  public void assignGroupTask(String taskId, String calendarId, String assignee) throws Exception;

  /**
   * Sets status for a group task.
   * @param taskId Id of the task
   * @param calendarId Id of the task's calendar
   * @param status
   * @throws Exception
   * @see CalendarEvent
   */
  public void setGroupTaskStatus(String taskId, String calendarId, String status) throws Exception;

  /**
   * Checks if the calendar with given calendarId is a remote calendar
   * @param username the owner of calendar
   * @param calendarId the Id of calendar
   * @return true if this calendar is remote, otherwise false
   * @throws Exception
   */
  public boolean isRemoteCalendar(String username, String calendarId) throws Exception;

  /**
   * Checks if the remote URL is valid, in 2 cases of iCalendar URL or CalDav URL, with authentication
   * @param url the remote URL
   * @param type the type of remote calendar, iCalendar or CalDav
   * @param remoteUser the remote user name used to authenticate
   * @param remotePassword the remote password used to authenticate
   * @return true if remote URL is available in case of iCalendar or supports CalDav access in case of CalDav
   * @throws Exception
   */
  boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws Exception;

  /**
   * Imports an online .ics or through CalDav access to local calendar
   * @param remoteCalendar object content all properties for remote calendar.
   * @throws Exception
   */
  public Calendar importRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception;

  /**
   * Reloads data for a remote calendar
   * @param username owner of the calendar
   * @param remoteCalendarId Id of the remote calendar to refresh
   * @return the <code>RemoteCalendar</code> object
   * @throws Exception
   */
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception;

  /**
   * Updates a remote calendar
   * @param remoteCalendar a <code>RemoteCalendar</code> object.
   * @return this calendar after updating
   * @throws Exception
   * @see RemoteCalendar
   */
  public Calendar updateRemoteCalendarInfo(RemoteCalendar remoteCalendar) throws Exception;

  /**
   * Gets an user's remote calendar, identified by its ID
   * @param owner user name of the calendar's owner
   * @param calendarId the Id of calendar
   * @return <code>RemoteCalendar<code> object
   * @throws Exception
   * @see RemoteCalendar
   */
  public RemoteCalendar getRemoteCalendar(String owner, String calendarId) throws Exception;

  /**
   * Gets the <code>RemoteCalendarService</code> object
   * @return <code>RemoteCalendarService</code> object
   * @throws Exception
   */
  public RemoteCalendarService getRemoteCalendarService() throws Exception;

  /**
   * Gets an user's remote calendar, identified by its URL
   * @param owner user name of the calendar's owner
   * @param remoteUrl URL of the remote calendar
   * @param remoteType iCalendar or CalDav
   * @return a <code>Calendar</code> object
   * @throws Exception
   */
  public Calendar getRemoteCalendar(String owner, String remoteUrl, String remoteType) throws Exception;

  /**
   * Gets number of remote calendars of an user
   * @param username
   * @return number of remote calendars
   * @throws Exception
   */
  public int getRemoteCalendarCount(String username) throws Exception;

  /**
   * Gets the reference key to remote event of an event in a subscribed calendar.
   * <p> Each event of a CalDav subscribed calendar has a reference key to its respective remote event. This key allows us to know if <br/>
   * an event is deleted or created from remote calendar. 
   * <p> The JCR property holding this value is <code>exo:caldavHref</code> of the node type <code>exo:caldavCalendarEvent</code>.
   * 
   * @param username current user name (or user ID)
   * @param calendarId the subscribed calendar's ID
   * @param eventId Id of the local event
   * @return reference key to the remote event
   * @throws Exception
   */
  public String getCalDavResourceHref(String username, String calendarId, String eventId) throws Exception;

  /**
   * Gets the entity tag of an event in a subscribed calendar.
   * <p> Each event of a CalDav subscribed calendar has an entity tag. This value allows us to know if the details content of a remote event <br/>
   * were updated, so that we can update the respective local event properly.
   * <p> The JCR property holding this value is <code>exo:caldavEtag</code> of the node type <code>exo:caldavCalendarEvent</code>.
   * @param username current user name (or user ID)
   * @param calendarId ID of the subscribed calendar
   * @param eventId Id of the local event
   * @return entity tag for the given event
   * @throws Exception
   */
  public String getCalDavResourceEtag(String username, String calendarId, String eventId) throws Exception;

  /**
   * Initializes job for synchronizing remote calendar
   * @param username
   * @throws Exception
   */
  public void loadSynchronizeRemoteCalendarJob(String username) throws Exception;

  /**
   * Finds job of synchronizing remote calendar of an user
   * @param schedulerService
   * @param username
   * @return a <code>JobDetail</code> object
   * @throws Exception
   */
  public JobDetail findSynchronizeRemoteCalendarJob(JobSchedulerService schedulerService, String username) throws Exception;

  /**
   * Stops an user's job for synchronizing remote calendar
   * @param username
   * @throws Exception
   */
  public void stopSynchronizeRemoteCalendarJob(String username) throws Exception;

  /**
   * Gets all occurrences of a repetitive event in a period of time. <br/>
   * The result will be depended on the recurrence rule, the start date of recurrent event and the period of time to view.
   * @param recurEvent the original recurrent event
   * @param from the from time
   * @param to the to time
   * @return the map of <code>CalendarEvent</code> object, each entry will contains an occurrence event object with recurrence-id as the key
   * @throws Exception
   */
  public Map<String, CalendarEvent> getOccurrenceEvents(CalendarEvent recurEvent, java.util.Calendar from, java.util.Calendar to, String timezone) throws Exception;

  /**
   * Gets all original repetitive events of an user in period of time
   * @param username the owner of recurrent event
   * @param from from time
   * @param to to time
   * @return list of <code>CalendarEvent</code> objects
   * @throws Exception
   */
  public List<CalendarEvent> getOriginalRecurrenceEvents(String username, java.util.Calendar from, java.util.Calendar to, String[] publicCalendarIds) throws Exception;

  /**
   * Updates an occurrence of a repetitive event
   * <p> This method is called when:
   * <ul>
   * <li>User wants to update only one instance of the recurrent series. In this case, a new normal event will be created as an exception event of the series</li>
   * <li>User wants to update an exception event of a recurrent series. In this case, this event will be updated as a normal one</li>
   * </ul> 
   * @param fromCalendar ID of the source calendar
   * @param toCalendar ID of the destination calendar
   * @param fromType type of the source Calendar
   * @param toType type of the destination calendar
   * @param calEvents list of events to be updated
   * @param username current user name (or user ID)
   * @throws Exception
   */
  public void updateOccurrenceEvent(String fromCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception;

  /**
   * Gets all exception occurrences from a original recurrent event, the exception event always belong to same calendar with original recurrent event
   * @param username the owner of this recurrent event
   * @param recurEvent the original recurrent event
   * @return the list of <code>CalendarEvent</code> objects
   * @throws Exception
   */
  public List<CalendarEvent> getExceptionEvents(String username, CalendarEvent recurEvent) throws Exception;

  /**
   * Removes only an occurrence instance from recurrence series, this function will get the original event node of the occurrence
   * and then put the recurrence id of the need-to-delete occurrence to excludeId list of original node.
   * @param username owner of this occurrence event
   * @param occurrence the occurrence event object to remove
   * @throws Exception
   */
  public void removeOccurrenceInstance(String username, CalendarEvent occurrence) throws Exception;

  /**
   * Removes all occurrence from an recurrence series. It will delete the original event of recurrence series. <br/>
   * All exception occurrences of this series still exist and will be treated as a normal event
   * @param username owner of recurrent event, in case of private or shared calendar
   * @param originalEvent the original recurrent event object
   * @throws Exception
   */
  public void removeRecurrenceSeries(String username, CalendarEvent originalEvent) throws Exception;

  /**
   * Updates recurrence series from an occurrence, this function is only called if the occurrence event is not changed the from date property. <br/>
   * In other way, if the occurrence event move to another date, it will be updated as a 'exception' occurrence and not affects to series
   * @param fromCalendar the calendarId of the calendar which the recurrent event belong to before updating
   * @param toCalendar the new calendarId of the recurrent event
   * @param fromType calendarType of recurrent event before updating
   * @param toType calendarType of recurrent event after updating
   * @param occurrence the occurrence contains the new data about recurrence series
   * @param username owner of recurrent event, in case of private and shared calendar
   * @throws Exception
   */
  public void updateRecurrenceSeries(String fromCalendar, String toCalendar, String fromType, String toType, CalendarEvent occurrence, String username) throws Exception;

  /**
   * Finds all days of month or year that have event/task to highlight from all personal, shared and public calendar of an user <br/>
   * This function is much same like searchHightLightEvent() function but it only counts for recurrent event
   * @param username the username of user
   * @param eventQuery EventQuery object to limit time-range
   * @param publicCalendarIds list of public calendar
   * @param timezone timezone
   * @return 
   * @throws Exception
   */
  public Map<Integer, String> searchHighlightRecurrenceEvent(String username, EventQuery eventQuery, String[] publicCalendarIds, String timezone) throws Exception;

  public List<Map<Integer, String>> searchHighlightRecurrenceEventSQL(String username, EventQuery eventQuery, String timezone,
                                                                      String[] privateCalendars, String[] publicCalendars) throws Exception;

  public List<CalendarEvent> getHighLightOriginalRecurrenceEvents(String username, java.util.Calendar from, java.util.Calendar to, String[] publicCalendarIds) throws Exception;

  public List<CalendarEvent> getHighLightOriginalRecurrenceEventsSQL(String username, java.util.Calendar from, java.util.Calendar to, EventQuery eventQuery,
                                                                     String[] privateCalendars, String[] publicCalendars, List<String> emptyCalendars) throws Exception;


    /**
     * Shares Calendar with groups
     * <p> When a calendar is selected to share with a group, the sharing process will run as job
     * in background to avoid blocking the user. It will send notification for users about starting and finishing the job.
     *
     * @param username Id of current user
     * @param calendarId Id of shared calendar
     * @param sharedGroups List of shared groups
     * @throws Exception
     */
  public void shareCalendarByRunJob(String username, String calendarId, List<String> sharedGroups) throws Exception;

  /**
   * Un-shares a calendar with groups.
   * <p> To avoid letting users wait too long, un-sharing with groups runs as job in background.
   * There are notifications about job starting and finishing
   * 
   * @param username Id of current user
   * @param unsharedGroups List of un-shared groups
   * @param calendarId Id of shared calendar
   * @throws Exception
   * @see {@link CalendarService#shareCalendarByRunJob(String, String, List)}
   */
  public void removeSharedCalendarByJob(String username, List<String> unsharedGroups, String calendarId) throws Exception;

  /**
   * Checks if a group is belong to a list of groups that's being shared
   * @param group Id of the group to be checked
   * @param schedulerService The schedule service that manages jobs
   * @return true if this group is belong to a list of groups that is being shared, false if not.
   * @throws Exception
   * @see {@link CalendarService#shareCalendarByRunJob(String, String, List)}
   */
  public boolean isGroupBeingShared(String group, JobSchedulerServiceImpl schedulerService) throws Exception;
  
  /**
   * Gets event by its Id
   * @param eventId Id of the event
   * @return a {@link CalendarEvent} 
   * @throws Exception
   */
  public CalendarEvent getEventById(String eventId) throws Exception;
  
  /**
   * Gets a calendar by its id
   * @param calId Id of the calendar
   * @return a {@link Calendar}
   * @throws Exception
   */
  public Calendar getCalendarById(String calId) throws Exception;
  
  /**
   * Shares group calendars with an user when he is assigned a new membership
   * <p>This method is called in {@link NewMembershipListener#postSave}.
   * @param groupsOfUser List of group id
   * @param receiver User id 
   * @throws Exception
   */
  public void autoShareCalendar(List<String> groupsOfUser, String receiver) throws Exception; 
  
  /**
   * Un-shares group calendars with an user when he is removed from a membership
   * <p>This method is called in {@link NewMembershipListener#preDelete}.
   * @param groupId
   * @param username
   * @throws Exception
   */
  public void autoRemoveShareCalendar(String groupId, String username) throws Exception;   

  /**
   * Imports given remote calendar in background <br/>
   * <p> Users don't need to wait too long when importing a big calendar
   * @param remoteCalendar
   * @throws Exception
   */
  public void importRemoteCalendarByJob(RemoteCalendar remoteCalendar) throws Exception;
}


