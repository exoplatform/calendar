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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.CalendarUpdateEventListener;
import org.exoplatform.calendar.service.DeleteShareJob;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.ImportCalendarJob;
import org.exoplatform.calendar.service.RemoteCalendar;
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.ShareCalendarJob;
import org.exoplatform.calendar.service.SynchronizeRemoteCalendarJob;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.picocontainer.Startable;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;


/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 11, 2007
 */
public class CalendarServiceImpl implements CalendarService, Startable {

  private final AtomicBoolean                 isRBLoaded_           = new AtomicBoolean(); 
  
  private ResourceBundle                      rb_;

  private ResourceBundleService               rbs_;

  private JCRDataStorage                      storage_;

  private Map<String, CalendarImportExport>   calendarImportExport_ = new LinkedHashMap<String, CalendarImportExport>();

  protected List<CalendarUpdateEventListener> listeners_            = new ArrayList<CalendarUpdateEventListener>(3);

  protected List<CalendarEventListener>       eventListeners_       = new ArrayList<CalendarEventListener>(3);

  private RemoteCalendarService               remoteCalendarService;

  private static final Log LOG = ExoLogger.getExoLogger(CalendarServiceImpl.class);

  public CalendarServiceImpl(InitParams params, NodeHierarchyCreator nodeHierarchyCreator, RepositoryService reposervice, ResourceBundleService rbs, CacheService cservice) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator, reposervice, cservice);
    calendarImportExport_.put(CalendarService.ICALENDAR, new ICalendarImportExport(storage_));
    calendarImportExport_.put(CalendarService.EXPORTEDCSV, new CsvImportExport(storage_));
    remoteCalendarService = new RemoteCalendarServiceImpl(storage_);
    rbs_ = rbs;
    ExoProperties props = params.getPropertiesParam("eventNumber.info").getProperties();
    String eventNumber = props.getProperty("eventNumber");
    Utils.EVENT_NUMBER = Integer.parseInt(eventNumber);
  }

  public JCRDataStorage getDataStorage() {
    return storage_;
  } 
  
  /**
   * {@inheritDoc}
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    return storage_.getUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    return storage_.getUserCalendars(username, isShowAll);
  }


  /**
   * {@inheritDoc}
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    storage_.saveUserCalendar(username, calendar, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    return storage_.removeUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception {
    return storage_.getGroupCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds, boolean isShowAll, String username) throws Exception {
    return storage_.getGroupCalendars(groupIds, isShowAll, username);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew) throws Exception {
    storage_.savePublicCalendar(calendar, isNew, null);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removePublicCalendar(String calendarId) throws Exception {
    return storage_.removeGroupCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<EventCategory> getEventCategories(String username) throws Exception {
    return storage_.getEventCategories(username);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception {
    EventCategory ev = getEventCategoryByName(username, eventCategory.getName());
    if (ev != null && (isNew || !ev.getId().equals(eventCategory.getId())))
      throw new ItemExistsException();
    storage_.saveEventCategory(username, eventCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventCategory(String username, EventCategory eventCategory, String[] values, boolean isNew) throws Exception {
    saveEventCategory(username, eventCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void removeEventCategory(String username, String eventCategoryId) throws Exception {
    storage_.removeEventCategory(username, eventCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    return storage_.getUserEventByCalendar(username, calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    return storage_.getUserEvents(username, eventQuery);
  }

  public CalendarEvent getEvent(String username, String eventId) throws Exception {
    return storage_.getEvent(username, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveUserEvent(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    return storage_.removeUserEvent(username, calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getGroupEvent(String eventId) throws Exception {
    return storage_.getGroupEvent(eventId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    return storage_.getGroupEvent(calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    return storage_.getGroupEventByCalendar(calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    return storage_.getPublicEvents(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    CalendarEvent oldEvent = getGroupEvent(event.getId());
    storage_.savePublicEvent(calendarId, event, isNew);
    for (CalendarEventListener cel : eventListeners_) {
      if (isNew) {
        cel.savePublicEvent(event, calendarId);
        storage_.savePublicEvent(calendarId, event, false);
      } else {
        cel.updatePublicEvent(oldEvent, event, calendarId);
        storage_.savePublicEvent(calendarId, event, false);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    CalendarEvent event = storage_.removePublicEvent(calendarId, eventId);
    for (CalendarEventListener cel : eventListeners_) {
      cel.deletePublicEvent(event, calendarId);
    }
    return event ;
  }

  /**
   * {@inheritDoc}
   */
  public CalendarImportExport getCalendarImportExports(String type) {
    return calendarImportExport_.get(type);
  }

  /**
   * {@inheritDoc}
   */
  public String[] getExportImportType() throws Exception {
    return calendarImportExport_.keySet().toArray(new String[] {});
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {
    storage_.saveCalendarSetting(username, setting);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception {
    return storage_.getCalendarSetting(username);
  }

  /**
   * {@inheritDoc}
   */
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception {
    return storage_.generateRss(username, calendars, rssData, calendarImportExport_.get(CalendarService.ICALENDAR));
  }

  /**
   * {@inheritDoc}
   */
  public List<FeedData> getFeeds(String username) throws Exception {
    return storage_.getFeeds(username);
  }

  /**
   * {@inheritDoc}
   */
  public Node getRssHome(String username) throws Exception {
    return storage_.getRssHome(username);
  }

  /**
   * {@inheritDoc}
   */
  public EventPageList searchEvent(String username, EventQuery query, String[] publicCalendarIds) throws Exception {
    return storage_.searchEvent(username, query, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception {
    return storage_.getEventCategory(username, eventCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public Map<Integer, String> searchHightLightEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    return storage_.searchHightLightEvent(username, eventQuery, publicCalendarIds);
  }

  public List<Map<Integer, String>> searchHightLightEventSQL(String username, EventQuery eventQuery,
                                                             String[] privateCalendars, String[] publicCalendars) throws Exception {
    return storage_.searchHightLightEventSQL(username, eventQuery, privateCalendars, publicCalendars);
  }

  /**
   * {@inheritDoc}
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    storage_.shareCalendar(username, calendarId, receiverUsers);
  }

  /**
   * {@inheritDoc}
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    return storage_.getSharedCalendars(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    return storage_.getEvents(username, eventQuery, publicCalendarIds);
  }

  @Override
  public List<CalendarEvent> getAllNoRepeatEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception {
    return storage_.getAllNoRepeatEvents(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getAllNoRepeatEventsSQL(String username, EventQuery eventQuery, String[] privateCalendars,
                                                     String[] publicCalendars, List<String> emptyCalendars) throws Exception {
    return storage_.getAllNoRepeatEventsSQL(username, eventQuery, privateCalendars, publicCalendars, emptyCalendars);
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    storage_.removeSharedCalendar(username, calendarId);
  }

  public void removeSharedCalendarFolder(String username) throws Exception {
    storage_.removeSharedCalendarFolder(username);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveEventToSharedCalendar(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    return storage_.checkFreeBusy(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    storage_.saveSharedCalendar(username, calendar);
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception {
    storage_.removeSharedEvent(username, calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public void moveEvent(String fromCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception {
    Map<String,  CalendarEvent> oldEventList = new HashMap<String, CalendarEvent>();
    if (fromType.equalsIgnoreCase(toType) && toType.equalsIgnoreCase(String.valueOf(Calendar.TYPE_PUBLIC)) && fromCalendar.equalsIgnoreCase(toCalendar)) {
      for (CalendarEvent event : calEvents) {
        oldEventList.put(event.getId(), getGroupEvent(event.getId()));
      }
    }
    storage_.moveEvent(fromCalendar, toCalendar, fromType, toType, calEvents, username);

    if (fromType.equalsIgnoreCase(toType) && toType.equalsIgnoreCase(String.valueOf(Calendar.TYPE_PUBLIC)) && fromCalendar.equalsIgnoreCase(toCalendar)) {
      for (CalendarEventListener cel : eventListeners_) {
        for (CalendarEvent event : calEvents) {
          if (!oldEventList.isEmpty() && oldEventList.get(event.getId()) != null) {
            cel.updatePublicEvent(oldEventList.get(event.getId()), event, toCalendar);
            storage_.savePublicEvent(toCalendar, event, false) ;
          }
        }
      }
    }
    
    if (!fromType.equalsIgnoreCase(String.valueOf(Calendar.TYPE_PUBLIC)) && toType.equalsIgnoreCase(String.valueOf(Calendar.TYPE_PUBLIC)) ) {
      for(CalendarEventListener cel : eventListeners_) {
        for (CalendarEvent event : calEvents) {
          cel.savePublicEvent(event, toCalendar);
          storage_.savePublicEvent(toCalendar, event, false) ;
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId, String toUserId, int calType, String calendarId, String eventId, int answer) {
    storage_.confirmInvitation(fromUserId, toUserId, calType, calendarId, eventId, answer);
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId, String confirmingEmail, String confirmingUser, int calType, String calendarId, String eventId, int answer) throws Exception {
    storage_.confirmInvitation(fromUserId, confirmingEmail, confirmingUser, calType, calendarId, eventId, answer);
  }

  public void start() {
    for (CalendarUpdateEventListener updateListener : listeners_) {
      updateListener.preUpdate();
    }
  }

  public void stop() {
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception {
    listeners_.add(listener);
  }

  public int getTypeOfCalendar(String userName, String calendarId) throws Exception {
    return storage_.getTypeOfCalendar(userName, calendarId);
  }

  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return storage_.generateRss(username, calendarIds, rssData, calendarImportExport_.get(CalendarService.ICALENDAR));
  }

  public ResourceBundle getResourceBundle() throws Exception {
    if (!isRBLoaded_.get()) {
      synchronized (isRBLoaded_) {
        if (!isRBLoaded_.get()) {
          try {
            rb_ = rbs_.getResourceBundle(Utils.RESOURCEBUNDLE_NAME, Locale.getDefault());
          } catch (MissingResourceException e) {
            rb_ = null;
          }
          isRBLoaded_.set(true);
        }
      }
    } 
    return rb_;
  }

  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception {   
    ResourceBundle rb = getResourceBundle(); 
    for (EventCategory ev : storage_.getEventCategories(username)) {
      if (ev.getName().equalsIgnoreCase(eventCategoryName)) {
        return ev;
      }  else if (rb != null && eventCategoryName.equalsIgnoreCase(rb.getString("UICalendarView.label." + ev.getId()))) {
        return ev;
      } 
    }
    return null;
  }

  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception {
    return storage_.getSharedEventByCalendars(username, calendarIds);
  }

  public void removeFeedData(String username, String title) {
    storage_.removeFeedData(username, title);
  }

  public void initNewUser(String userName, CalendarSetting defaultCalendarSetting_) throws Exception {
    EventCategory eventCategory = new EventCategory();
    eventCategory.setDataInit(true);
    for (int id = 0; id < NewUserListener.defaultEventCategoryIds.length; id++) {
      if (NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL.equals(id)) continue;
      eventCategory.setId(NewUserListener.defaultEventCategoryIds[id]);
      eventCategory.setName(NewUserListener.defaultEventCategoryNames[id]);
      saveEventCategory(userName, eventCategory, true);
    }

    // get the user's full name
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    User u = organizationService.getUserHandler().findUserByName(userName);
    String fullName = u.getFirstName();
    if (u.getLastName() != null && fullName != null) {
      fullName = fullName + " " + u.getLastName();
    }
    if (fullName == null) fullName = u.getUserName();
    // save default calendar
    Calendar cal = new Calendar();
    cal.setId(Utils.getDefaultCalendarId(userName));
    cal.setName(fullName); // name the default calendar after the user's full name, cf CAL-86
    cal.setDataInit(true);
    cal.setCalendarOwner(userName);
    cal.setCalendarColor(Calendar.COLORS[0]);
    if (defaultCalendarSetting_ != null) {
      if (defaultCalendarSetting_.getTimeZone() != null)
        cal.setTimeZone(defaultCalendarSetting_.getTimeZone());
    }
    saveUserCalendar(userName, cal, true);

    if (defaultCalendarSetting_ != null) {
      saveCalendarSetting(userName, defaultCalendarSetting_);
    }
    
    Object[] groupsOfUser = organizationService.getGroupHandler().findGroupsOfUser(userName).toArray();
    List<String> groups = new ArrayList<String>();
    for (Object object : groupsOfUser) {
      String groupId = ((Group) object).getId();
      groups.add(groupId);
    }
    storage_.autoShareCalendar(groups, userName);
  }

  public void addEventListenerPlugin(CalendarEventListener listener) throws Exception {
    eventListeners_.add(listener);
  }

  public void assignGroupTask(String taskId, String calendarId, String assignee) throws Exception {
    storage_.assignGroupTask(taskId, calendarId, assignee);

  }

  public void setGroupTaskStatus(String taskId, String calendarId, String status) throws Exception {
    storage_.setGroupTaskStatus(taskId, calendarId, status);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isRemoteCalendar(String username, String calendarId) throws Exception {
    return storage_.isRemoteCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws Exception {
    return remoteCalendarService.isValidRemoteUrl(url, type, remoteUser, remotePassword);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar updateRemoteCalendarInfo(RemoteCalendar remoteCalendar) throws Exception {
    return storage_.updateRemoteCalendarInfo(remoteCalendar);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception {
    return remoteCalendarService.refreshRemoteCalendar(username, remoteCalendarId);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar importRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception {
    return remoteCalendarService.importRemoteCalendar(remoteCalendar);
  }

  /**
   * {@inheritDoc}
   */
  public RemoteCalendar getRemoteCalendar(String owner, String calendarId) throws Exception {
    return storage_.getRemoteCalendar(owner, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public RemoteCalendarService getRemoteCalendarService() throws Exception {
    return remoteCalendarService;
  }

  public Calendar getRemoteCalendar(String owner, String remoteUrl, String remoteType) throws Exception {
    return storage_.getRemoteCalendar(owner, remoteUrl, remoteType);
  }

  public String getCalDavResourceHref(String username, String calendarId, String eventId) throws Exception {
    Node eventNode = storage_.getUserCalendarHome(username).getNode(calendarId).getNode(eventId);
    try {
      return eventNode.getProperty(Utils.EXO_CALDAV_HREF).getString();
    } catch (PathNotFoundException e) {
      if(LOG.isDebugEnabled()) {
        LOG.debug("Exception when getting caldav resource",e);
      }
      return null;
    }
  }

  public String getCalDavResourceEtag(String username, String calendarId, String eventId) throws Exception {
    Node eventNode = storage_.getUserCalendarHome(username).getNode(calendarId).getNode(eventId);
    return eventNode.getProperty(Utils.EXO_CALDAV_ETAG).getString();
  }

  public void setCalDavResourceHref(String username, String calendarId, String eventId, String href) throws Exception {
    storage_.setCalDavResourceHref(username, calendarId, eventId, href);
  }

  public void setCalDavResourceEtag(String username, String calendarId, String eventId, String etag) throws Exception {
    storage_.setCalDavResourceEtag(username, calendarId, eventId, etag);
  }

  // load synchronize remote calendar job with period
  public void loadSynchronizeRemoteCalendarJob(String username) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
    JobInfo info = SynchronizeRemoteCalendarJob.getJobInfo(username);
    JobDetail job = findSynchronizeRemoteCalendarJob(schedulerService, username);
    if (job == null) {
      JobDataMap jobData = new JobDataMap();
      jobData.put(SynchronizeRemoteCalendarJob.USERNAME, username);
      RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      jobData.put(repositoryService.getCurrentRepository()
                  .getConfiguration()
                  .getName(), repositoryService.getCurrentRepository()
                  .getConfiguration()
                  .getName());
      PeriodInfo periodInfo = new PeriodInfo(null, null, 0, 5 * 60 * 1000);
      schedulerService.addPeriodJob(info, periodInfo, jobData);
    }
  }

  public void stopSynchronizeRemoteCalendarJob(String username) throws Exception {
    JobInfo info = SynchronizeRemoteCalendarJob.getJobInfo(username);
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
    schedulerService.removeJob(info);
    schedulerService.removeJobListener(info.getJobName());
    schedulerService.removeTriggerListener(info.getJobName());
  }

  public JobDetail findSynchronizeRemoteCalendarJob(JobSchedulerService schedulerService, String username) throws Exception {
    // find synchronize job
    List<JobDetail> list = schedulerService.getAllJobs();
    for (JobDetail jobDetail : list) {
      if (jobDetail.getKey().getName().equals(SynchronizeRemoteCalendarJob.getRemoteCalendarName(username))) {
        return jobDetail;
      }
    }
    return null;
  }

  public int getRemoteCalendarCount(String username) throws Exception {
    return storage_.getRemoteCalendarCount(username);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getOccurrenceEvents(org.exoplatform.calendar.service.CalendarEvent, java.util.Calendar, java.util.Calendar)
   */
  public Map<String, CalendarEvent> getOccurrenceEvents(CalendarEvent recurEvent, java.util.Calendar from, java.util.Calendar to, String timezone) throws Exception {
    return storage_.getOccurrenceEvents(recurEvent, from, to, timezone);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#updateOccurrenceEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String)
   */
  public void updateOccurrenceEvent(String fromCalendar, String toCalendar, String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception {
    // keep map of old events to compare
    Map<String,  CalendarEvent> oldEventList = new HashMap<String, CalendarEvent>();
    if (fromType.equalsIgnoreCase(toType) && toType.equalsIgnoreCase(String.valueOf(Calendar.TYPE_PUBLIC)) && fromCalendar.equalsIgnoreCase(toCalendar)) {
      for (CalendarEvent event : calEvents) {
        oldEventList.put(event.getId(), getGroupEvent(event.getId()));
      }
    }
    storage_.updateOccurrenceEvent(fromCalendar, toCalendar, fromType, toType, calEvents, username);
    if (fromType.equalsIgnoreCase(toType) && toType.equalsIgnoreCase(String.valueOf(Calendar.TYPE_PUBLIC)) && fromCalendar.equalsIgnoreCase(toCalendar)) {
      for (CalendarEventListener cel : eventListeners_) {
        for (CalendarEvent event : calEvents) {
          // if event existed, update the activity
          if (oldEventList.get(event.getId()) != null) {
            cel.updatePublicEvent(oldEventList.get(event.getId()), event, toCalendar);
            storage_.savePublicEvent(toCalendar, event, false) ;
          } else {
            // publish new activity
            cel.savePublicEvent(event, toCalendar);
            storage_.savePublicEvent(toCalendar, event, false);
          }
        }
      }
    }
  }

  public List<CalendarEvent> getOriginalRecurrenceEvents(String username, java.util.Calendar from, java.util.Calendar to, String[] publicCalendarIds) throws Exception {
    return storage_.getOriginalRecurrenceEvents(username, from, to, publicCalendarIds);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getExceptionEvents(org.exoplatform.calendar.service.CalendarEvent)
   */
  public List<CalendarEvent> getExceptionEvents(String username, CalendarEvent recurEvent) throws Exception {
    return storage_.getExceptionEvents(username, recurEvent);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#removeRecurrenceSeries(java.lang.String, org.exoplatform.calendar.service.CalendarEvent)
   */
  public void removeRecurrenceSeries(String username, CalendarEvent originalEvent) throws Exception {
    storage_.removeRecurrenceSeries(username, originalEvent);
    if(originalEvent.getActivityId() != null) {
      for (CalendarEventListener cel : eventListeners_) {
        cel.deletePublicEvent(originalEvent, originalEvent.getCalendarId());
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#updateRecurrenceSeries(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String)
   */
  public void updateRecurrenceSeries(String fromCalendar, String toCalendar, String fromType, String toType, CalendarEvent occurrence, String username) throws Exception {
    CalendarEvent oldEvent = getGroupEvent(occurrence.getId());
    if (oldEvent == null) oldEvent = getEvent(username, occurrence.getId());
    storage_.updateRecurrenceSeries(fromCalendar, toCalendar, fromType, toType, occurrence, username);
    if(toType.equalsIgnoreCase(String.valueOf(Utils.PUBLIC_TYPE))) {
      for (CalendarEventListener cel : eventListeners_) {
        if(oldEvent != null) {
          if (oldEvent.getActivityId() != null)
          {
            occurrence.setActivityId(oldEvent.getActivityId());
            cel.updatePublicEvent(oldEvent, occurrence, fromCalendar);
          }
          else {
            cel.updatePublicEvent(occurrence, toCalendar);
          }
          storage_.savePublicEvent(toCalendar, occurrence, false) ;
        }
      }
    }
    
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#getSharedEvent(java.lang.String, java.lang.String, java.lang.String)
   */
  public CalendarEvent getSharedEvent(String username, String calendarId, String eventId) throws Exception {
    return storage_.getSharedEvent(username, calendarId, eventId);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.calendar.service.CalendarService#removeOccurrenceInstance(java.lang.String, org.exoplatform.calendar.service.CalendarEvent)
   */
  public void removeOccurrenceInstance(String username, CalendarEvent occurrence) throws Exception {
    storage_.removeOccurrenceInstance(username, occurrence);

    /*=== check if we remove the last instance of repetitive event ===*/
    String calendarId = occurrence.getCalendarId();
    int calType = Integer.parseInt(occurrence.getCalType());
    CalendarEvent repetitiveEvent = null;

    // get the original repetitive event
    if (calType == Calendar.TYPE_PRIVATE)
      repetitiveEvent = storage_.getUserEvent(username, calendarId, occurrence.getId());
    else if (calType == Calendar.TYPE_PUBLIC)
      repetitiveEvent = storage_.getGroupEvent(calendarId, occurrence.getId());
    else if (calType == Calendar.TYPE_SHARED)
      repetitiveEvent = storage_.getSharedEvent(username, calendarId, occurrence.getId());

    if (isEventSeriesGettingToLastItem(repetitiveEvent, username))
    {
      /* remove the original repetitive event */
      removeRecurrenceSeries(username, repetitiveEvent);
      return ;
    }

    if(occurrence.getActivityId() != null) {
      for (CalendarEventListener cel : eventListeners_) {
        cel.deletePublicEvent(occurrence, occurrence.getCalendarId());
      }  
    }
    else /* remove an instance of repetitive event */
    {
      /* reset date for occurrence to avoids posting changed date */
      occurrence.setFromDateTime(repetitiveEvent.getFromDateTime());
      occurrence.setToDateTime(repetitiveEvent.getToDateTime());

      for (CalendarEventListener cel : eventListeners_) {
        cel.updatePublicEvent(occurrence, repetitiveEvent, repetitiveEvent.getCalendarId());
      }
    }
    
  }

  /**
   * check if the series of event is getting to its last item
   *
   * @param repetitiveEvent
   * @return
   */
  private boolean isEventSeriesGettingToLastItem(CalendarEvent repetitiveEvent, String username) throws Exception
  {
    /* event repeated unlimited  */
    if ((repetitiveEvent.getRepeatCount() <= 0) && (repetitiveEvent.getRepeatUntilDate() == null)) return false;

    /* event repeated to a certain date */
    if ((repetitiveEvent.getRepeatCount() <= 0) && (repetitiveEvent.getRepeatUntilDate() != null))
    {
      CalendarSetting calendarSetting = getCalendarSetting(username);
      TimeZone timezone = TimeZone.getTimeZone(calendarSetting.getTimeZone()); // europe brussels
      java.util.Calendar fromDate = java.util.Calendar.getInstance(timezone);
      fromDate.setTime(repetitiveEvent.getFromDateTime());
      java.util.Calendar toDate   = java.util.Calendar.getInstance(timezone);
      toDate.setTime(repetitiveEvent.getRepeatUntilDate()); // correct date

      Map<String, CalendarEvent> occurrenceEvents = getOccurrenceEvents(repetitiveEvent, fromDate, toDate, calendarSetting.getTimeZone()); // not null
      return (occurrenceEvents.keySet().size() == 0);
    }

    /* event with repeat_count set*/
    int numberOfOccurrences = (int) repetitiveEvent.getRepeatCount();
    return (repetitiveEvent.getExcludeId().length == numberOfOccurrences);
  }

  public Map<Integer, String> searchHighlightRecurrenceEvent(String username, EventQuery eventQuery, String[] publicCalendarIds, String timezone) throws Exception {
    return storage_.searchHighlightRecurrenceEvent(username, eventQuery, publicCalendarIds, timezone);
  }

  public List<Map<Integer, String>> searchHighlightRecurrenceEventSQL(String username, EventQuery eventQuery, String timezone,
                                                                String[] privateCalendars, String[] publicCalendars) throws Exception {
    return storage_.searchHighlightRecurrenceEventSQL(username, eventQuery, timezone, privateCalendars, publicCalendars);
  }

  public List<CalendarEvent> getHighLightOriginalRecurrenceEvents(String username, java.util.Calendar from, java.util.Calendar to, String[] publicCalendarIds) throws Exception {
    return storage_.getHighLightOriginalRecurrenceEvents(username, from, to, publicCalendarIds);
  }

  public List<CalendarEvent> getHighLightOriginalRecurrenceEventsSQL(String username, java.util.Calendar from, java.util.Calendar to, EventQuery eventQuery,
                                                                     String[] privateCalendars, String[] publicCalendars, List<String> emptyCalendars) throws Exception {
    return storage_.getHighLightOriginalRecurrenceEventsSQL(username, from, to, eventQuery, privateCalendars, publicCalendars, emptyCalendars);
  }

  /**
   * {@inheritDoc}
   */
  public void shareCalendarByRunJob(String username, String calendarId, List<String> sharedGroups) throws Exception{
    JobSchedulerServiceImpl  schedulerService = (JobSchedulerServiceImpl)ExoContainerContext.getCurrentContainer().getComponentInstance(JobSchedulerService.class) ;
    JobInfo jobInfo = new JobInfo(sharedGroups.toString(),Utils.SHARE_CALENDAR_GROUP, ShareCalendarJob.class);
    List<String> newSharedGroups = new ArrayList<String>();
    //check if a group is being shared, remove it from the job
    for(String group : sharedGroups) {
      if(!isGroupBeingShared(group, schedulerService)) {
        newSharedGroups.add(group);
      }
    }
    
    if(newSharedGroups.size() > 0) {
      JobDetailImpl job = new JobDetailImpl();
      job.setName(jobInfo.getJobName());
      job.setGroup(jobInfo.getGroupName());
      job.setJobClass(jobInfo.getJob());
      
      job.setDescription(jobInfo.getDescription());
      job.getJobDataMap().put(Utils.SHARED_GROUPS, newSharedGroups);
      job.getJobDataMap().put(Utils.USER_NAME, username);
      job.getJobDataMap().put(Utils.CALENDAR_ID, calendarId);

      SimpleTriggerImpl trigger = new SimpleTriggerImpl();
      trigger.setName(jobInfo.getJobName());
      trigger.setGroup(jobInfo.getGroupName());
      trigger.setStartTime(new Date());

      schedulerService.addJob(job, trigger);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedCalendarByJob(String username, List<String> unsharedGroups, String calendarId) throws Exception {
    JobSchedulerServiceImpl  schedulerService_ = (JobSchedulerServiceImpl)ExoContainerContext.getCurrentContainer().getComponentInstance(JobSchedulerService.class) ;
    JobInfo jobInfo = new JobInfo(unsharedGroups.toString(), Utils.DELETE_SHARED_GROUP, DeleteShareJob.class);
    
    JobDetailImpl job = new JobDetailImpl();
    job.setName(jobInfo.getJobName());
    job.setGroup(jobInfo.getGroupName());
    job.setJobClass(jobInfo.getJob());    
    
    job.setDescription(jobInfo.getDescription());
    job.getJobDataMap().put(Utils.USER_NAME, username);
    job.getJobDataMap().put(Utils.REMOVED_USERS,unsharedGroups);
    job.getJobDataMap().put(Utils.CALENDAR_ID, calendarId);

    SimpleTriggerImpl trigger = new SimpleTriggerImpl();
    trigger.setName(jobInfo.getJobName());
    trigger.setGroup(jobInfo.getGroupName());
    trigger.setStartTime(new Date());
    
    schedulerService_.addJob(job, trigger);
  }  

  
  public boolean isGroupBeingShared(String group, JobSchedulerServiceImpl schedulerService_) throws Exception {
    List<?> list = schedulerService_.getAllExcutingJobs();

    for (Object obj : list) {
      JobExecutionContext job = (JobExecutionContext) obj;
      JobDataMap jobDataMap = job.getJobDetail().getJobDataMap();
      List<String> sharedGroups = (List<String>) jobDataMap.get(Utils.SHARED_GROUPS);
      if(sharedGroups != null) {
        if(sharedGroups.contains(group)) {
          return true;
        }
      }
    }
    return false;
  }

  public CalendarEvent getEventById(String eventId) throws Exception {
    return storage_.getEventById(eventId);
  }


  @Override
  public Calendar getCalendarById(String calId) throws Exception {
    return storage_.getCalendarById(calId);
  }  
  /**
   * {@inheritDoc}
   */
  public void autoShareCalendar(List<String> groupsOfUser, String receiver) throws Exception {
    storage_.autoShareCalendar(groupsOfUser, receiver);
  }

  /**
   * {@inheritDoc}
   */
  public void autoRemoveShareCalendar(String groupId, String username) throws Exception {
    storage_.autoRemoveShareCalendar(groupId, username);
  }

  @Override
  public void importRemoteCalendarByJob(RemoteCalendar remoteCalendar) throws Exception {
   
    JobSchedulerServiceImpl  schedulerService = (JobSchedulerServiceImpl)ExoContainerContext.getCurrentContainer().
        getComponentInstance(JobSchedulerService.class) ;

    JobDetail job = ImportCalendarJob.getImportRemoteCalendarJobDetail(remoteCalendar);

    SimpleTriggerImpl trigger = new SimpleTriggerImpl();
    trigger.setName(remoteCalendar.getCalendarName());
    trigger.setGroup(ImportCalendarJob.IMPORT_CALENDAR_JOB_GROUP_NAME);
    trigger.setStartTime(new Date());

    schedulerService.addJob(job, trigger);  
  }
}
