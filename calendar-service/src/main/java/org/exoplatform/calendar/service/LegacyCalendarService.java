/**
 * Copyright (C) 2015 eXo Platform SAS.
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

import java.util.List;

import org.exoplatform.calendar.service.impl.NewMembershipListener;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;

public interface LegacyCalendarService {

  /**
   * Gets a calendar by its id
   * @param calId Id of the calendar
   * @return a {@link Calendar}
   */
  public Calendar getCalendarById(String calId) throws Exception;
  
  /**
   * Return all calendar of given user
   * @param username current user id
   * @param calType type of calendar to filter 
   * @param offset offset to continue to return data
   * @param limit limit item will be returned
   * @param fullSize reference full size of data list 
   * @return collection of Calendar object
   */
  public CalendarCollection<Calendar> getAllCalendars(String username, int calType, int offset, int limit);
  
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
   * Return calendars that have publicUrl enabled
   * @return
   * @throws Exception 
   */
  ListAccess<Calendar> getPublicCalendars() throws Exception;
  
  /**
   * Saves an user's private calendar to storage
   * 
   * @param username current user name(or user id)
   * @param calendar Calendar object that will be stored
   * @param isNew Boolean value to know adding a new calendar or just updating this calendar
   * @throws CalendarException if there is any error
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew);

  /**
   * Saves an calendar to storage with given type
   * 
   * @param username current user name(or user id)
   * @param calendar Calendar object that will be stored
   * @param caltype the type of calendar
   * @param isNew Boolean value to know adding a new calendar or just updating this calendar
   * @return instance calendar object
   */
  public Calendar saveCalendar(String username, Calendar calendar, int caltype , boolean isNew);
  
  /**
   * Saves a calendar to shared calendars section
   * @param username current user name(or user id)
   * @param calendar Calendar object
   * @throws Exception
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception;
  
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
   * Removes private calendar by given id, all events and tasks belong to this calendar will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @return
   * @throws Exception
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception;

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
  public void savePublicCalendar(Calendar calendar, boolean isNew);   

  /**
   * Removes the group calendar form data base, every events, tasks inside this calendar will be removed too
   * @param calendarId
   * @return
   * @throws Exception
   */
  public Calendar removePublicCalendar(String calendarId) throws Exception;

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
   * Checks if a group is belong to a list of groups that's being shared
   * @param group Id of the group to be checked
   * @param schedulerService The schedule service that manages jobs
   * @return true if this group is belong to a list of groups that is being shared, false if not.
   * @throws Exception
   * @see {@link CalendarService#shareCalendarByRunJob(String, String, List)}
   */
  public boolean isGroupBeingShared(String group, JobSchedulerServiceImpl schedulerService) throws Exception;
  
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
}


