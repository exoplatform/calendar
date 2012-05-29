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
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.JobDetail;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */

public interface CalendarService {

  final public static String ICALENDAR   = "ICalendar(.ics)".intern();

  final public static String EXPORTEDCSV = "ExportedCsv(.csv)".intern();

  final public static String CALDAV      = "CalDAV".intern();

  /**
   * The method gets all calendar category of current user from data base
   * @param username current user name
   * @return List of CalendarCategory object
   * @throws Exception
   * @see CalendarCategory
   */
  public List<CalendarCategory> getCategories(String username) throws Exception;

  /**
   * The method gets all groups of private calendar, and each GroupCalendar contains List of Calendar object
   * @param username current user name
   * @param isShowAll The parameter to make sure that the user want to show all calendar or not, if it is <b>true</b> then 
   * it gets all calendars, if <b>false</b> it will check from calendar setting to know which calendar will be shown
   * @return List<GroupCalendarData> List of GroupCalendarData
   * @throws Exception
   * @see GroupCalendarData
   */
  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception;

  /**
   * The method gets the calendar category by given id
   * @param username current user name 
   * @param calendarCategoryId id of calendar category
   * @return CalendarCategory
   * @throws Exception
   * @see CalendarCategory
   */
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception;

  /**
   * Save details of category for a user
   * @param username current user name(or user id)
   * @param calendarCategory the object that contains category details
   * @param isNew the boolean value to point out that add new category or update
   * @throws Exception
   */
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception;

  /**
   * The method used for removing one category by id
   * @param username current user name(or user id)
   * @param calendarCategoryId given category id
   * @return
   * @throws Exception
   * @see CalendarCategory
   */
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception;

  /**
   * The method get private calendar by given calendarId, and all calendar related to this category will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @return Calendar object returned contants details of a calendar
   * @throws Exception
   * @see Calendar
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception;

  /**
   * The method queries all private calendars of current user
   * @param username current user name(or user id)
   * @param isShowAll boolean value if equals <b>true</b> will get all private calendars, equals <b>false</b> it will take only 
   * the calendars in current user's setting
   * @return List of calendar object
   * @throws Exception
   * @see Calendar
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception;

  /**
   * The method look up all private calendars by given category id
   * @param username current user name(or user id)
   * @param calendarCategoryId given calendar category id
   * @return List calendar object
   * @throws Exception
   * @see Calendar
   */
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception;

  /**
   * The method saves private calendar infomations in to data base
   * @param username current user name(or user id)
   * @param calendar object contants infomations
   * @param isNew Boolean value to know add new calendar or update infomations only
   * @throws Exception
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception;

  /**
   * Remove private calendar by given id, all events and tasks belong to this calendar will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @return
   * @throws Exception
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception;

  /**
   * The method save all infomations about shared calendar, it will be updated original calendar
   * @param username current user name(or user id)
   * @param calendar the oject contants infomations
   * @throws Exception
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception;

  /**
   * The method  gets all calendar of a group user, we called it is group calendar
   * it means the calendar for group of users and depen on the permission the user will have right to view or edit that calendar
   * @param calendarId given calendar id
   * @return Calendar object contants infomations
   * @throws Exception
   * @see Calendar
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception;

  /**
   * The method  gets all the group calendar data of current user and list of calendars belong to that group
   * with group calendar data it will classify calendar to each group
   * @param groupIds The group ids that current user belong
   * @param isShowAll Gets all calendar or use setting from calendar setting
   * @param username current user name(or user id)
   * @return List of GroupCalendarData and each GroupCalendarData contants List of calendar object too
   * @throws Exception
   * @see GroupCalendarData
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds, boolean isShowAll, String username) throws Exception;

  /**
   * @deprecated
   * @see #savePublicCalendar(Calendar calendar, boolean isNew)
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception;

  /**
  * The method save calendar to public area (group calendar)
  * @param calendar
  * @param isNew Boolean value will be checked is it add new or update infomations only
  * @param username current user name(or user id)
  * @throws Exception
  */
  public void savePublicCalendar(Calendar calendar, boolean isNew) throws Exception;

  /**
   * Remove the group calendar form data base, every events, tasks inside this calendar will be removed too
   * @param calendarId
   * @return
   * @throws Exception
   */
  public Calendar removePublicCalendar(String calendarId) throws Exception;

  /**
   * The method gets all categories of event
   * @param username current user name(or user id)
   * @return List event category object
   * @throws Exception
   * @see EventCategory
   */
  public List<EventCategory> getEventCategories(String username) throws Exception;

  /**
   * Save event category to data base, every user will have their own category to classify events, and it will use unique name in data base
   * @param username current user name(or user id)
   * @param eventCategory
   * @param values 
   * @param isNew
   * @throws Exception
   */
  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception;

  /**
   * Remove event category, all events and tasks belong to this category will be destroyed
   * @param username current user name(or user id)
   * @param eventCategoryId The unique name of category
   * @throws Exception
   */
  public void removeEventCategory(String username, String eventCategoryId) throws Exception;

  /**
   * The method gets category of event by given id
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventCategoryId given event category id
   * @return event category object contents infomations
   * @throws Exception
   * @see EventCategory
   */
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception;

  /**
   * The method gets category of event by given id
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventCategoryName given event category name
   * @return event category object contents infomations
   * @throws Exception
   * @see EventCategory
   */
  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception;

  /**
   * The method gets list events and tasks of given private calendar ids 
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param calendarIds given calendar ids
   * @return List of events and tasks
   * @throws Exception
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception;

  /**
   * The method gets all events and tasks by given conditions in event query
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventQuery given coditons
   * @return List of CalendarEvent object (events and tasks)
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception;

  /**
   * Get a personal event for a given owner
   * @param owner user id of the event owner
   * @param eventId id of event to get
   * @return CalendarEvent in the personal events of owner 
   * @throws Exception 
   */
  public CalendarEvent getEvent(String username, String eventId) throws Exception;

  /**
   * The method save infomation to an event or a task by given private calendar id to data
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param event object contants infomations
   * @param isNew boolean value, is update or add new event
   * @throws Exception
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Remove given event or task in private calendar with calendar id, all attachments and reminders will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param eventId given event id
   * @return
   * @throws Exception
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * Get a group event from eventID
   * @param eventId
   * @return CalendarEvent object or null if event was not found
   * @throws Exception
   */
  public CalendarEvent getGroupEvent(String eventId) throws Exception;

  /**
   * The method gets event or task form group calendar by given calendar id
   * @param calendarId given calendar id
   * @param eventId given event id
   * @return CalendarEvent object contains information and attachments, reminders
   * @throws Exception
   * @see CalendarEvent
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception;

  /**
   * The method gets events and tasks by given public calendar ids  
   * @param calendarIds public calendar ids
   * @return List calendar event object
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception;

  /**
   * The method gets events and tasks by given event query
   * @param eventQuery object contants given conditions 
   * @return List calendar event object
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception;

  /**
   * Save event or task by given group calendar id
   * @param calendarId given calendar id
   * @param event object contants infomation about event
   * @param isNew boolean value to check update or add new event
   * @throws Exception
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * Remove event or task, all attachments and reminders item will be removed
   * @param calendarId given calendar id
   * @param eventId given event or task id
   * @return
   * @throws Exception
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception;

  /**
   * This menthod stores individual setting of each user, with setting you can configue many things like Default view
   * date, time formating, time inteval 
   * @param username current user name(or user id)
   * @param setting Obicject containts infomations about setting
   * @throws Exception
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception;

  /**
   * This method gets infomations of current user's setting
   * @param username current user name(or user id)
   * @return
   * @throws Exception
   * @see CalendarSetting
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception;

  /**
   * The method  gets Import/Export implement class to import or export ics,csv
   * @param type type of import, export, it supports two types, ICS and CSV 
   * @return CalendarImportExport
   * @see CalendarImportExport
   */
  public CalendarImportExport getCalendarImportExports(String type);

  /**
   * The method gets types of data will be imported and exported
   * @return types of inport/export
   * @throws Exception
   */
  public String[] getExportImportType() throws Exception;

  /**
   * The menthod uses to make url to contants links to subcribe calendar folows RSS stand
   * @param username current user name(or user id)
   * @param calendars
   * @param rssData object contants infomations about the rss feed
   * @return
   * @throws Exception
   * @see RssData
   */
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception;

  /**
   * The menthod uses to make url to contants links to subcribe calendar folows RSS stand
   * @param username current user name(or user id)
   * @param calendars
   * @param rssData object contants infomations about the rss feed
   * @return
   * @throws Exception
   * @see RssData
   */
  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception;

  /**
   * It gets data form server and show the url to view contents of RSS
   * @param systemSession Sessesion to access the public data
   * @param username current user name(or user id)
   * @return List of FeedData
   * @throws Exception
   * @see FeedData
   */
  public List<FeedData> getFeeds(String username) throws Exception;

  /**
   * The method return root of rss data store area
   * @param username current user name(or user id)
   * @return
   * @throws Exception
   */
  public Node getRssHome(String username) throws Exception;

  /**
   * The method query events and tasks form given coditions, the coditions know by set value for eventquery
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventQuery object contants coditions to query
   * @param publicCalendarIds pulic calendar ids
   * @return
   * @throws Exception
   * @see EventPageList
   */
  public EventPageList searchEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * The method query all events, tasks and mark to hightlight the date have events or tasks 
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventQuery object contants coditions to query
   * @param publicCalendarIds publicCalendarIds pulic calendar ids
   * @return
   * @throws Exception
   */
  public Map<Integer, String> searchHightLightEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * The method share the private calendar to other user, it can share for one or many users
   * @param systemSession Sessesion to access the public data
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param receiverUsers List receive user username or id
   * @throws Exception
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception;

  /**
   * The method gets all shared calendars of the current user
   * @param systemSession Sessesion to access the public data
   * @param username current user name(or user id)
   * @param isShowAll boolean value to point out that it will get all calendars or use user's clendar setting
   * @return
   * @throws Exception
   * @see GroupCalendarData
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception;

  /**
   * The method selects all the events and tasks by given conditions, it includes events of private, public and share calendars
   * @param username current user name(or user id)
   * @param eventQuery given coditions
   * @param publicCalendarIds public calendar ids
   * @return
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception;

  /**
   * Removed shared calendar, but not the orloginal calendar
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @throws Exception
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception;
  
  /**
   * This method removes the shared calendar folder of this user and all references (from shared calendars) to this folder.   
   * @param username the username
   * @throws Exception
   */
  public void removeSharedCalendarFolder(String username) throws Exception;

  /**
   * Add event to shared calendar, mean add event to orloginal calendar too
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param event object contants infomations about event
   * @param isNew boolean value to check that add new or update event
   * @throws Exception
   */
  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /**
   * The method  will check the time free or busy of the user, it depents on events and tasks of this user 
   * now it only check on one day and if the events and tasks marked with busy, out side status will be checked 
   * @param eventQuery The query object it containts query statement to look up the data 
   * @return Map data with key is user name (or user id), and value is the a pair of <i>from time</i> and <i>to time</i> by miliseconds and sperate by coma(,)
   * @throws Exception 
   * @see EventQuery
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception;

  /**
   * The method genarete links to access calendar throw WEBDAV, it will require user name and password when access
   * @param username current user name(or user id)
   * @param calendars List calendar ids will look up and publicing
   * @param rssData Object contants infomations about rss feed
   * @return
   * @throws Exception
   */
  public int generateCalDav(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception;

  /**
   * The method genarete links to access calendar throw WEBDAV, it will require user name and password when access
   * @param username current user name(or user id)
   * @param calendars List calendar ids will look up and publicing
   * @param rssData Object contants infomations about rss feed
   * @return
   * @throws Exce
   */
  public int generateCalDav(String username, List<String> calendarIds, RssData rssData) throws Exception;

  /**
   * The method removes the events or tasks form shared calendar, orloginal item will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param eventId given event id
   * @throws Exception
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception;

  /**
   * The method  move and save events form private calendars share calendars public calendars each other
   * @param formCalendar the source calendar id
   * @param toCalendar  destination calendar id
   * @param formType type of source calendar
   * @param toType type of destination calendar
   * @param calEvents List of object contant infomations
   * @param username current user name(or user id)
   * @throws Exception
   */
  public void moveEvent(String formCalendar, String toCalendar, String formType, String toType, List<CalendarEvent> calEvents, String username) throws Exception;

  /**
   * The method calls when the user use exomail product only, when user receives an invitation (in the same data system), the user will 
   * congfirme that do they want to take part in or not
   * @param fromUserId id or user name of the user, who make the invitation
   * @param toUserId receiver user's id or name
   * @param calType type of calendar contants the event
   * @param calendarId given calendar id
   * @param eventId given event id
   * @param answer The answer of the receive user
   */
  public void confirmInvitation(String fromUserId, String toUserId, int calType, String calendarId, String eventId, int answer);

  public void confirmInvitation(String fromUserId, String confirmingEmail, String confirmingUser, int calType, String calendarId, String eventId, int answer) throws Exception;

  public void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception;

  public void addEventListenerPlugin(CalendarEventListener listener) throws Exception;

  /**
   * The method update exited rss data when calendar has been changed
   * @param usename 
   * @param calendarId calendar id
   * Added from 1.3
   */
  public void updateRss(String usename, String calendarId, CalendarImportExport imp) throws Exception;

  public void updateRss(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception;

  /**
   * The method update exited rss data when calendar has been changed
   * @param usename 
   * @param calendarId calendar id
   * Added from 1.3
   */
  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp) throws Exception;

  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception;

  public int getTypeOfCalendar(String userName, String calendarId) throws Exception;

  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception;

  /**
   * Get a shared event of user from user name, id of shared calendar, id of shared event
   * @param username username
   * @param calendarId id of shared calendar
   * @param eventId id of shared event
   * @return the CalendarEvent object
   * @throws Exception
   */
  public CalendarEvent getSharedEvent(String username, String calendarId, String eventId) throws Exception;

  public void removeFeedData(String username, String title);

  public ResourceBundle getResourceBundle() throws Exception;

  public void initNewUser(String userName, CalendarSetting defaultCalendarSetting) throws Exception;

  /**
   * assign a user as delegator for a group task.
   * @param taskId
   * @param calendarId
   * @param assignee
   * @throws Exception
   */
  public void assignGroupTask(String taskId, String calendarId, String assignee) throws Exception;

  /**
   * set status value of a group task.
   * @param taskId
   * @param calendarId
   * @param status
   * @throws Exception
   */
  public void setGroupTaskStatus(String taskId, String calendarId, String status) throws Exception;

  /**
   * Check if the calendar with given calendarId is a remote calendar
   * @param username the owner of calendar
   * @param calendarId the Id of calendar
   * @return true if this calendar is remote, otherwise false
   * @throws Exception
   */
  public boolean isRemoteCalendar(String username, String calendarId) throws Exception;

  /**
   * Check if the remote url is valid, in 2 cases of iCalendar url or CalDav url, with authentication
   * @param url the remote url
   * @param type the type of remote calendar, iCalendar or CalDav
   * @param username the remote username used to authenticate
   * @param password the remote password used to authenticate
   * @return true if remote url is available in case of iCalendar and CalDav access support in case of CalDav
   * @throws Exception
   */
  boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws Exception;

  /**
   * Import an online .ics or through CalDav access to local calendar
   * @param remoteCalendar object content all properties for remote calendar.
   * @param credentials
   * @throws Exception
   */
  public Calendar importRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception;

  /**
   * Reload remote calendar data
   * @param username owner of the calendar
   * @param remoteCalendar object content all properties for remote calendar.Id id of the calendar
   * @return the remote Calendar ojbect
   * @throws Exception
   */
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception;

  /**
   * @param remoteCalendar object content all properties for remote calendar.
   * @return
   * @throws Exception
   */
  public Calendar updateRemoteCalendarInfo(RemoteCalendar remoteCalendar) throws Exception;

  /**
   * @param owner the owner of this calendar
   * @param calendarId the Id of calendar
   * @return RemoteCalendar
   * @throws Exception
   */
  public RemoteCalendar getRemoteCalendar(String owner, String calendarId) throws Exception;

  /**
   * Get the RemoteCalendarService object
   * @return
   * @throws Exception
   */
  public RemoteCalendarService getRemoteCalendarService() throws Exception;

  public Calendar getRemoteCalendar(String owner, String remoteUrl, String remoteType) throws Exception;

  public int getRemoteCalendarCount(String username) throws Exception;

  public String getCalDavResourceHref(String username, String calendarId, String eventId) throws Exception;

  public String getCalDavResourceEtag(String username, String calendarId, String eventId) throws Exception;

  public void loadSynchronizeRemoteCalendarJob(String username) throws Exception;

  public JobDetail findSynchronizeRemoteCalendarJob(JobSchedulerService schedulerService, String username) throws Exception;

  public void stopSynchronizeRemoteCalendarJob(String username) throws Exception;

  /**
   * Get all virtual occurrences from an original recurrence event  in a period of time <br/>
   * The result will be depended on the recurrence rule, the start date of recurrence event and the period of time to view.
   * @param recurEvent the original recurrence event
   * @param from the from time
   * @param to the to time
   * @return the map of CalendarEvent object, each entry will contains an occurrence event object with recurrence-id as the key
   * @throws Exception
   */
  public Map<String, CalendarEvent> getOccurrenceEvents(CalendarEvent recurEvent, java.util.Calendar from, java.util.Calendar to, String timezone) throws Exception;

  /**
   * Get all original recurrence events of an user in period of time
   * @param username the owner of recurrence event
   * @param from from time
   * @param to to time
   * @return list of CalendarEvent objects
   * @throws Exception
   */
  public List<CalendarEvent> getOriginalRecurrenceEvents(String username, java.util.Calendar from, java.util.Calendar to, String[] publicCalendarIds) throws Exception;

  /**
   * Update an occurrence event, there are two cases: if this occurrence is virtual occurrence, convert it to exception, <br />
   * if this occurrence is exception, update it as a normal event
   * @param fromCalendar
   * @param toCalendar
   * @param fromType
   * @param toType
   * @param calEvents
   * @param username
   * @throws Exception
   */
  public void updateOccurrenceEvent(String fromCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception;

  /**
   * Get all exception occurrences from a original recurrence event, the exception event always belong to same calendar with original recurrence event
   * @param username the owner of this recurrence event
   * @param recurEvent the original recurrence event
   * @return the list of CalendarEvent objects
   * @throws Exception
   */
  public List<CalendarEvent> getExceptionEvents(String username, CalendarEvent recurEvent) throws Exception;

  /**
   * Remove only an occurrence instance from recurrence series, this function will get the original event node of the occurrence
   * and then put the recurrence id of the need-to-delete occurrence to excludeId list of original node.
   * @param username owner of this occurrence event
   * @param occurrence the occurrence event object to remove
   * @throws Exception
   */
  public void removeOccurrenceInstance(String username, CalendarEvent occurrence) throws Exception;

  /**
   * Remove all occurrence from an recurrence series. It will delete the original event of recurrence series. <br/>
   * All exception occurrences of this series still exist and will be treated as a normal event
   * @param username owner of recurrence event, in case of private or shared calendar
   * @param originalEvent the original recurrence event object
   * @throws Exception
   */
  public void removeRecurrenceSeries(String username, CalendarEvent originalEvent) throws Exception;

  /**
   * Update recurrence series from an occurrence, this function is only called if the occurrence event is not changed the from date property. <br/>
   * In other way, if the occurrence event move to another date, it will be updated as a 'exception' occurrence and not affects to series
   * @param fromCalendar the calendarId of the calendar which the recurrence event belong to before updating
   * @param toCalendar the new calendarId of the recurrence event
   * @param fromType calendarType of recurrence event before updating
   * @param toType calendarType of recurrence event after updating
   * @param occurrence the occurrence contains the new data about recurrence series
   * @param username owner of recurrence event, in case of private and shared calendar
   * @throws Exception
   */
  public void updateRecurrenceSeries(String fromCalendar, String toCalendar, String fromType, String toType, CalendarEvent occurrence, String username) throws Exception;

  /**
   * Find all days of month or year that have event/task to highlight from all personal, shared and public calendar of an user <br/>
   * This function is much same like searchHightLightEvent() function but it only counts for recurrence event
   * @param username the username of user
   * @param eventQuery EventQuery object to limit time-range
   * @param publicCalendarIds list of public calendar
   * @param timezone timezone
   * @return 
   * @throws Exception
   */
  public Map<Integer, String> searchHighlightRecurrenceEvent(String username, EventQuery eventQuery, String[] publicCalendarIds, String timezone) throws Exception;
}
