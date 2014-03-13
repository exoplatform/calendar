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
package org.exoplatform.webservice.cs.rest;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.CalendarUpdateEventListener;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RemoteCalendar;
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.CalendarEventListener;
import org.exoplatform.calendar.service.impl.CsvImportExport;
import org.exoplatform.calendar.service.impl.ICalendarImportExport;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.quartz.JobDetail;

import javax.jcr.Node;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 3, 2010  
 */
public class MockCalendarService implements CalendarService{
  private static final Log log = ExoLogger.getExoLogger(MockCalendarService.class);

  private Calendar cal_;
  private Map<String, List<CalendarEvent>> data_;
  private Map<String, CalendarImportExport>   calendarImportExport_ = new LinkedHashMap<String, CalendarImportExport>();
  private CalendarSetting setting_ ;

  public MockCalendarService() throws Exception{
    calendarImportExport_.put(CalendarService.ICALENDAR, new ICalendarImportExport(new JCRDataStorage(null, null,(CacheService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CacheService.class))));
    calendarImportExport_.put(CalendarService.EXPORTEDCSV, new CsvImportExport(new JCRDataStorage(null, null, (CacheService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CacheService.class))));
  }

  @Override
  public void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception {

  }

  @Override
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    return null;
  }

  @Override
  public void confirmInvitation(String fromUserId, String toUserId, int calType, String calendarId, String eventId, int answer) {

  }

  @Override
  public void confirmInvitation(String fromUserId,
                                String confirmingEmail,
                                String confirmingUser,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {

  }


  @Override
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception {
    return 0;
  }

  @Override
  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return 0;
  }


  @Override
  public CalendarImportExport getCalendarImportExports(String type) {
    return calendarImportExport_.get(type);
  }

  @Override
  public CalendarSetting getCalendarSetting(String username) throws Exception {
    if(setting_ == null) setting_ = new CalendarSetting() ;
    return setting_;
  }

  @Override
  public CalendarEvent getEvent(String username, String eventId) throws Exception {
    Iterator<List<CalendarEvent>> iter = data_.values().iterator() ; 
    return iter.next().get(0) ;
  }

  @Override
  public List<EventCategory> getEventCategories(String username) throws Exception {
    return null;
  }

  @Override
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception {
    return null;
  }

  @Override
  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception {
    return null;
  }

  @Override
  public List<CalendarEvent> getEvents(String username,
                                       EventQuery eventQuery,
                                       String[] publicCalendarIds) throws Exception {
    return null;
  }

  @Override
  public List<CalendarEvent> getAllNoRepeatEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    return null;
  }

  @Override
  public List<CalendarEvent> getAllNoRepeatEventsSQL(String username, EventQuery eventQuery,
                                                     String[] privateCalendars, String[] publicCalendars, List<String> emptyCalendars) throws Exception {
    return null;
  }


  @Override
  public String[] getExportImportType() throws Exception {
    return null;
  }

  @Override
  public List<FeedData> getFeeds(String username) throws Exception {
    return null;
  }

  @Override
  public Calendar getGroupCalendar(String calendarId) throws Exception {
    return null;
  }

  @Override
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds,
                                                   boolean isShowAll,
                                                   String username) throws Exception {
    return null;
  }

  @Override
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    return null;
  }

  @Override
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    return null;
  }

  @Override
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    return null;
  }

  @Override
  public Node getRssHome(String username) throws Exception {
    return null;
  }

  @Override
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    return null;
  }

  @Override
  public int getTypeOfCalendar(String userName, String calendarId) throws Exception {
    return 0;
  }

  @Override
  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    return cal_;
  }

  @Override
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    return null;
  }

  @Override
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    return data_.get(calendarIds.get(0));
  }

  @Override
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    return null;
  }

  @Override
  public void moveEvent(String formCalendar,
                        String toCalendar,
                        String formType,
                        String toType,
                        List<CalendarEvent> calEvents,
                        String username) throws Exception {

  }


  @Override
  public void removeEventCategory(String username, String eventCategoryName) throws Exception {

  }

  @Override
  public Calendar removePublicCalendar(String calendarId) throws Exception {
    return null;
  }

  @Override
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    return null;
  }

  @Override
  public void removeSharedCalendar(String username, String calendarId) throws Exception {

  }

  @Override
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception {

  }

  @Override
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    return null;
  }

  @Override
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    return null;
  }


  @Override
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {

  }

  @Override
  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception {

  }

  @Override
  public void saveEventToSharedCalendar(String username,
                                        String calendarId,
                                        CalendarEvent event,
                                        boolean isNew) throws Exception {

  }

  @Override
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {

  }

  @Override
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {

  }

  @Override
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    cal_ = calendar;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    data_ = new HashMap<String, List<CalendarEvent>>();
    data_.put(cal_.getId(), events);


  }

  @Override
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {

    data_.get(cal_.getId()).add(event);

  }

  @Override
  public EventPageList searchEvent(String username,
                                   EventQuery eventQuery,
                                   String[] publicCalendarIds) throws Exception {
    if(data_ != null && cal_ != null && data_.get(cal_.getId()) != null)
      if(eventQuery.getEventType().equals(CalendarEvent.TYPE_EVENT)) {
        return new EventPageList(data_.get(cal_.getId()),0);
      } else if(eventQuery.getEventType().equals(CalendarEvent.TYPE_TASK)) {
        return new EventPageList(data_.get(cal_.getId()),0);
      }

    return null;
  }

  @Override
  public Map<Integer, String> searchHightLightEvent(String username,
                                                    EventQuery eventQuery,
                                                    String[] publicCalendarIds) throws Exception {
    return null;
  }

  @Override
  public List<Map<Integer, String>> searchHightLightEventSQL(String username, EventQuery eventQuery,
                                                       String[] privateCalendars, String[] publicCalendars) throws Exception {
    return null;
  }


  @Override
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {

  }

  @Override
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception {
    return null;
  }

  @Override
  public void removeFeedData(String username, String title) {

  }
  @Override
  public ResourceBundle getResourceBundle() throws Exception {
    return null;
  }

  public void initNewUser(String userName, CalendarSetting defaultCalendarSetting)
  throws Exception {

  }

  @Override
  public void addEventListenerPlugin(CalendarEventListener listener) throws Exception {
    
  }

  @Override
  public void savePublicCalendar(Calendar calendar, boolean isNew) throws Exception {
    
  }

  @Override
  public void assignGroupTask(String taskId, String calendarId, String assignee) throws Exception {
    
  }

  @Override
  public void setGroupTaskStatus(String taskId, String calendarId, String status) throws Exception {
    
  }

  @Override
  public CalendarEvent getGroupEvent(String eventId) throws Exception {
    return null;
  }

  @Override
  public boolean isRemoteCalendar(String username, String calendarId) throws Exception {
    return false;
  }

  @Override
  public Calendar importRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception {
    return null;
  }
  
  @Override
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception {
    return null;
  }

  /*@Override
  public Calendar updateRemoteCalendarInfo(String username,
                                           String calendarId,
                                           String remoteUrl,
                                           String calendarName,
                                           String description,
                                           String syncPeriod,
                                           String remoteUser,
                                           String remotePassword) throws Exception {
    return null;
  }*/
  public Calendar updateRemoteCalendarInfo(RemoteCalendar remoteCalendar) throws Exception {
    return null;
  }

  @Override
  public boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws Exception {
    return false;
  }

  @Override
  public RemoteCalendarService getRemoteCalendarService() throws Exception {
    return null;
  }

  @Override
  public Calendar getRemoteCalendar(String owner, String remoteUrl, String remoteType) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getCalDavResourceHref(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public String getCalDavResourceHref(String username, String calendarId, String eventId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getCalDavResourceEtag(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public String getCalDavResourceEtag(String username, String calendarId, String eventId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getRemoteCalendarCount(java.lang.String)
   */
  @Override
  public int getRemoteCalendarCount(String username) throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#loadSynchronizeRemoteCalendarJob(java.lang.String)
   */
  @Override
  public void loadSynchronizeRemoteCalendarJob(String username) throws Exception {
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#findSynchronizeRemoteCalendarJob(org.exoplatform.services.scheduler.JobSchedulerService, java.lang.String)
   */
  @Override
  public JobDetail findSynchronizeRemoteCalendarJob(JobSchedulerService schedulerService,
                                                    String username) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#stopSynchronizeRemoteCalendarJob(java.lang.String)
   */
  @Override
  public void stopSynchronizeRemoteCalendarJob(String username) throws Exception {
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getOccurrenceEvents(org.exoplatform.calendar.service.CalendarEvent, java.util.Calendar, java.util.Calendar)
   */
  @Override
  public Map<String,CalendarEvent> getOccurrenceEvents(CalendarEvent recurEvent,
                                                 java.util.Calendar from,
                                                 java.util.Calendar to, String timezone) throws Exception {
    Iterator<List<CalendarEvent>> iter = data_.values().iterator() ; 
    CalendarEvent event =  iter.next().get(0);
    Map<String, CalendarEvent> result = new HashMap<String, CalendarEvent>();
    SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
    sdf.setTimeZone(TimeZone.getTimeZone(timezone));
    if(event.getRepeatType() != null) {
      String recurId = sdf.format(event.getFromDateTime());
      result.put(recurId, event);
    }
    return result;
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#updateOccurrenceEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String)
   */
  @Override
  public void updateOccurrenceEvent(String fromCalendar,
                                    String toCalendar,
                                    String fromType,
                                    String toType,
                                    List<CalendarEvent> calEvents,
                                    String username) throws Exception {
    
  }

  @Override
  public List<CalendarEvent> getOriginalRecurrenceEvents(String username, java.util.Calendar from, java.util.Calendar to, String[] publicCalendarIds) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getExceptionEvents(java.lang.String, org.exoplatform.calendar.service.CalendarEvent)
   */
  @Override
  public List<CalendarEvent> getExceptionEvents(String username, CalendarEvent recurEvent) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#removeRecurrenceSeries(java.lang.String, org.exoplatform.calendar.service.CalendarEvent)
   */
  @Override
  public void removeRecurrenceSeries(String username, CalendarEvent originalEvent) throws Exception {
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#updateRecurrenceSeries(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.exoplatform.calendar.service.CalendarEvent, java.lang.String)
   */
  @Override
  public void updateRecurrenceSeries(String fromCalendar,
                                     String toCalendar,
                                     String fromType,
                                     String toType,
                                     CalendarEvent occurrence,
                                     String username) throws Exception {
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getSharedEvent(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public CalendarEvent getSharedEvent(String username, String calendarId, String eventId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#removeOccurrenceInstance(java.lang.String, org.exoplatform.calendar.service.CalendarEvent)
   */
  @Override
  public void removeOccurrenceInstance(String username, CalendarEvent occurrence) throws Exception {
    
  }

  @Override
  public Map<Integer, String> searchHighlightRecurrenceEvent(String username,
                                                             EventQuery eventQuery,
                                                             String[] publicCalendarIds,
                                                             String timezone) throws Exception {
    return null;
  }

  @Override
  public List<Map<Integer, String>> searchHighlightRecurrenceEventSQL(String username, EventQuery eventQuery, String timezone, String[] privateCalendars, String[] publicCalendars) throws Exception {
    return null;
  }


  @Override
  public List<CalendarEvent> getHighLightOriginalRecurrenceEvents(String username, java.util.Calendar from, java.util.Calendar to, String[] publicCalendarIds) throws Exception {
    return null;
  }

  @Override
  public List<CalendarEvent> getHighLightOriginalRecurrenceEventsSQL(String username, java.util.Calendar from, java.util.Calendar to, EventQuery eventQuery, String[] privateCalendars, String[] publicCalendars, List<String> emptyCalendars) throws Exception {
    return null;
  }

  @Override
  public RemoteCalendar getRemoteCalendar(String owner, String calendarId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#removeSharedCalendarFolder(java.lang.String)
   */
  @Override
  public void removeSharedCalendarFolder(String username) throws Exception {
  }
  
  @Override
  public void shareCalendarByRunJob(String username, String calendarId, List<String> sharedGroups) throws Exception {
  }

  @Override
  public boolean isGroupBeingShared(String deletedGroup, JobSchedulerServiceImpl schedulerService_) throws Exception {
    return false;
  }

  @Override
  public void removeSharedCalendarByJob(String username,
                                        List<String> unsharedGroups,
                                        String calendarId) throws Exception {
  }

  @Override
  public CalendarEvent getEventById(String eventId) throws Exception {
    Iterator<List<CalendarEvent>> iter = data_.values().iterator() ; 
    return iter.next().get(0) ;
  }

  @Override
  public Calendar getCalendarById(String calId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void autoShareCalendar(List<String> groupsOfUser, String reciever) throws Exception {
  }

  @Override
  public void autoRemoveShareCalendar(String groupId, String username) throws Exception {
  }

  @Override
  public void importRemoteCalendarByJob(RemoteCalendar remoteCalendar) throws Exception {
  }

  @Override
  public void saveOneOccurrenceEvent(CalendarEvent originEvent,
                                     CalendarEvent newEvent,
                                     String username) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveAllSeriesEvents(CalendarEvent originEvent,
                                  String username) {
    // TODO Auto-generated method stub
    
  }

  

  @Override
  public void removeOneOccurrenceEvent(CalendarEvent originEvent,
                                       CalendarEvent removedOccurence,
                                       String username) {
    // TODO Auto-generated method stub
    
  }

   

  @Override
  public Collection<CalendarEvent> getAllExcludedEvent(CalendarEvent originEvent,
                                                       Date from,
                                                       Date to,
                                                       String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<CalendarEvent> buildSeries(CalendarEvent originEvent,
                                               Date from,
                                               Date to,
                                               String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String buildRecurrenceId(Date formTime, String username) {
    // TODO Auto-generated method stub
    return null;
  }

  
  @Override
  public void saveFollowingSeriesEvents(CalendarEvent originEvent,
                                        CalendarEvent newEvent,
                                        String username) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeAllSeriesEvents(CalendarEvent originEvent, String username) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeFollowingSeriesEvents(CalendarEvent originEvent,
                                          CalendarEvent newEvent,
                                          String username) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<CalendarEvent> getExceptionEventsFromDate(String username,
                                                        CalendarEvent event,
                                                        Date fromDate) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CalendarEvent getRepetitiveEvent(CalendarEvent occurence) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
