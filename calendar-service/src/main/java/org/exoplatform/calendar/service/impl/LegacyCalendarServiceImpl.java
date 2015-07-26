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
  
package org.exoplatform.calendar.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCollection;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.CalendarIterator;
import org.exoplatform.calendar.service.DeleteShareJob;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.LegacyCalendarService;
import org.exoplatform.calendar.service.ShareCalendarJob;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

public abstract class LegacyCalendarServiceImpl implements LegacyCalendarService {

  protected JCRDataStorage                      storage_;
  
  protected List<CalendarEventListener>       eventListeners_       = new ArrayList<CalendarEventListener>(3);
  
  private static final Log LOG = ExoLogger.getExoLogger(LegacyCalendarServiceImpl.class);  
  
  public LegacyCalendarServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService reposervice, CacheService cservice) throws Exception {
    this.storage_ = new JCRDataStorage(nodeHierarchyCreator, reposervice, cservice);
  }

  @Override
  public Calendar getCalendarById(String calId) throws Exception {
    return storage_.getCalendarById(calId);
  }

  @Override
  public CalendarCollection<Calendar> getAllCalendars(String username, int calType, int offset, int limit) {
    Collection<Calendar> cals = new ArrayList<Calendar>();
    int fullSize = 0;
    try {
      QueryManager queryManager = null ;
      StringBuffer sql = new StringBuffer("SELECT * FROM ");
      sql.append(Utils.EXO_CALENDAR);
      switch (calType) {
      case Calendar.TYPE_PRIVATE:
        Node userNode = storage_.getUserCalendarHome(username);
        sql.append(" WHERE ").append(Utils.JCR_PATH).append(" LIKE '").append(userNode.getPath()).append("/%'")
        .append("AND NOT jcr:path LIKE '").append(userNode.getPath()).append("/%/%'");
        queryManager = userNode.getSession().getWorkspace().getQueryManager();
        break;
      case Calendar.TYPE_SHARED:
        int counter = 0;
        Node sharedHomeNode =  storage_.getSharedCalendarHome();
        if(sharedHomeNode.hasNode(username)) {
          Node sharedNode = sharedHomeNode.getNode(username);
          PropertyIterator iter = sharedNode.getReferences();
          fullSize += iter.getSize();
          Utils.skip(iter, offset);
          while (iter.hasNext()) {
            Calendar cal = Utils.loadCalendar(iter.nextProperty().getParent()); 
            cals.add(cal);
            if (++counter == limit) {
              break;
            }
          }
        }
        break;
      case Calendar.TYPE_PUBLIC:
        OrganizationService orgService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
        Collection<Group> groups = orgService.getGroupHandler().findGroupsOfUser(username);
        if (groups == null || groups.isEmpty()) return new CalendarCollection<Calendar>(Collections.<Calendar>emptyList(), 0);
        
        Node node =  storage_.getPublicCalendarHome();
        sql.append(" WHERE ").append(Utils.JCR_PATH).append(" LIKE '").append(node.getPath()).append("/%'").append("AND NOT jcr:path LIKE '")
        .append(node.getPath()).append("/%/%'");
        sql.append(" AND (");
        for (Iterator<Group> i = groups.iterator(); i.hasNext();) {
          sql.append("CONTAINS(").append(Utils.EXO_GROUPS).append(", ").append("'").append(i.next().getId()).append("')");
          if (i.hasNext()) sql.append(" OR ");
        }
        sql.append(")");
        queryManager = node.getSession().getWorkspace().getQueryManager();
        break;

      case Calendar.TYPE_ALL:
        Node uNode = storage_.getUserCalendarHome(username);
        Node pNode =  storage_.getPublicCalendarHome();
        Node sHome =  storage_.getSharedCalendarHome();
        CalendarIterator rIt = new CalendarIterator();
        if(sHome.hasNode(username)) {
          Node sNode = sHome.getNode(username);
          PropertyIterator pIt = sNode.getReferences();
          rIt.addShareIterator(pIt);
        }
        StringBuffer pSql = new StringBuffer(sql.toString());

        pSql.append(" WHERE ").append(Utils.JCR_PATH).append(" LIKE '").append(uNode.getPath()).append("/%'").append("AND NOT jcr:path LIKE '")
        .append(uNode.getPath()).append("/%/%'"); 
        queryManager = uNode.getSession().getWorkspace().getQueryManager();
        QueryImpl jcrquery = (QueryImpl)queryManager.createQuery(pSql.toString(), Query.SQL);
        QueryResult result = jcrquery.execute();
        NodeIterator nIt1 = result.getNodes();
        rIt.addPeronalIterator(nIt1);

        orgService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
        groups = orgService.getGroupHandler().findGroupsOfUser(username);
        if (groups != null) {
          StringBuffer gSql = new StringBuffer(sql.toString());
          gSql.append(" WHERE ").append(Utils.JCR_PATH).append(" LIKE '").append(pNode.getPath()).append("/%'").append("AND NOT jcr:path LIKE '")
          .append(pNode.getPath()).append("/%/%'");
          gSql.append(" AND (");
          for (Iterator<Group> i = groups.iterator(); i.hasNext();) {
            gSql.append("CONTAINS(").append(Utils.EXO_GROUPS).append(", ").append("'").append(i.next().getId()).append("')");
            if (i.hasNext()) gSql.append(" OR ");
          }
          gSql.append(")");
          queryManager = pNode.getSession().getWorkspace().getQueryManager();
          
          QueryImpl jcrquery2 = (QueryImpl)queryManager.createQuery(gSql.toString(), Query.SQL);
          QueryResult result2 = jcrquery2.execute();
          NodeIterator nIt2 = result2.getNodes();
          rIt.addPublicIterator(nIt2);
          fullSize += rIt.getSize();
          Utils.skip(rIt, offset);
          counter = 0;
          while (rIt.hasNext()) {
            Calendar cal = null;
            Object it = rIt.next();
            if(it != null){
              if(rIt.isNode()) cal = Utils.loadCalendar(((Node)it));
              else cal = Utils.loadCalendar(((Property)it).getParent());
            }
            if(cal != null) cals.add(cal);
            if (++counter == limit) {
              break;
            }
          }
        }
        break;
      default:
        break;
      }
      if(queryManager != null && Calendar.TYPE_ALL != calType) {

        QueryImpl jcrquery = (QueryImpl)queryManager.createQuery(sql.toString(), Query.SQL);
        jcrquery.setOffset(offset);
        //jcrquery.setLimit(limit);
        QueryResult result = jcrquery.execute();
        NodeIterator rIt  = result.getNodes();
        fullSize += rIt.getSize();
        int counter = 0;
        while (rIt.hasNext()) {
          Calendar cal = Utils.loadCalendar(rIt.nextNode());
          if(cal != null) cals.add(cal);
          if (++counter == limit) {
            break;
          }
        }
      }
    } catch (Exception e) {
      if(LOG.isDebugEnabled()) LOG.debug(e.getMessage());
    }
    return new CalendarCollection<Calendar>(cals, fullSize);
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

  @Override
  public ListAccess<Calendar> getPublicCalendars() throws Exception {
    StringBuffer sql = new StringBuffer("SELECT * FROM ");
    sql.append(Utils.EXO_CALENDAR).append(" WHERE ");
    sql.append(Utils.EXO_PUBLIC_URL).append(" IS NOT NULL");
       
    QueryManager queryManager = storage_.getSystemSession().getWorkspace().getQueryManager();
    final QueryImpl jcrQuery = (QueryImpl)queryManager.createQuery(sql.toString(), Query.SQL);    
    
    return new ListAccess<Calendar>() {
      private int size = -1;
      
      @Override
      public int getSize() throws Exception {
        return size;
      }

      @Override
      public Calendar[] load(int offset, int limit) throws Exception, IllegalArgumentException {
        List<Calendar> cals = new LinkedList<Calendar>();
        
        if (limit > 0) {
          jcrQuery.setOffset(offset);
          jcrQuery.setLimit(limit);
        }
        
        QueryResultImpl result = (QueryResultImpl)jcrQuery.execute();
        NodeIterator iter = result.getNodes();
        while (iter.hasNext()) {
          cals.add(Utils.loadCalendar(iter.nextNode()));
        }
        this.size = result.getTotalSize();
        return cals.toArray(new Calendar[cals.size()]);
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) {
    try {
      storage_.saveUserCalendar(username, calendar, isNew);
    } catch (Exception e) {
      throw new CalendarException();
    }
  }

  public Calendar saveCalendar(String username, Calendar calendar, int caltype , boolean isNew){
    Calendar instance = null;
    try {
      switch (caltype) {
      case Calendar.TYPE_PRIVATE:
        storage_.saveUserCalendar(username, calendar, isNew);
        break;

      case Calendar.TYPE_PUBLIC:
        storage_.savePublicCalendar(calendar, isNew, username);
        break;
      case Calendar.TYPE_SHARED:
        storage_.saveSharedCalendar(username, calendar);
        break;
      default:
        break;
      }
      instance = storage_.getCalendarById(calendar.getId());
    } catch (Exception e) {
      if(LOG.isDebugEnabled()) LOG.debug(e.getMessage());
    }

    return instance;
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
  public void savePublicCalendar(Calendar calendar, boolean isNew) {
    try {
      storage_.savePublicCalendar(calendar, isNew, null);
    } catch (Exception e) {
      throw new CalendarException();
    }
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
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    return storage_.getUserEventByCalendar(username, calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    return storage_.getUserEvents(username, eventQuery);
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
    CalendarEvent originEvent = getRepetitiveEvent(event);
    if(originEvent != null) {
      //if event is an exception, add comment about cancelling event in the origin event's activity
      for(CalendarEventListener cel : eventListeners_) {
        cel.removeOneOccurrence(originEvent, event);
      }
    }
    //remove the event's activity
    for (CalendarEventListener cel : eventListeners_) {
      cel.deletePublicEvent(event, calendarId);
    }

    return event ;
  }
  
  @Override
  public CalendarEvent getRepetitiveEvent(CalendarEvent occurence) throws Exception {
    CalendarEvent originEvent = null;
    if(occurence.getOriginalReference() != null) {
      //if occurence is an exception that was broken from series, get the origin by UUID
      Node eventNode = storage_.getSystemSession().getNodeByUUID(occurence.getOriginalReference());
      originEvent =  storage_.getEvent(eventNode);
    } else if(occurence.getRecurrenceId() != null) {
      //if occurrence is not an exception event, the id of the origin is the id of the occurrence
      //(because the occurrence is clone from origin event
      originEvent = getEventById(occurence.getId());
    }
    if(originEvent != null) {
      originEvent.setCalType(occurence.getCalType());
    }

    return originEvent;
  }
  
  public CalendarEvent getEventById(String eventId) throws Exception {
    return storage_.getEventById(eventId);
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

  public int getTypeOfCalendar(String userName, String calendarId) throws Exception {
    return storage_.getTypeOfCalendar(userName, calendarId);
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

  /**
   * {@inheritDoc}
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    storage_.removeSharedCalendar(username, calendarId);
  }

  public void removeSharedCalendarFolder(String username) throws Exception {
    storage_.removeSharedCalendarFolder(username);
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

  /**
   * {@inheritDoc}
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    storage_.saveSharedCalendar(username, calendar);
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
    if(Utils.isUserEnabled(username))
    return storage_.getSharedCalendars(username, isShowAll);
    else return null;
  }
}
