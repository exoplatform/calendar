/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service.impl;

import javax.jcr.query.Query;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 22, 2013  
 */
public class CalendarSearchServiceConnector extends SearchServiceConnector {

  private CalendarService calendarService_;
  private SpaceService spaceService_;

  private static final Log     log                 = ExoLogger.getLogger(CalendarSearchServiceConnector.class);
  private ThreadLocal<Map<String, Calendar>> calendarMap = new ThreadLocal<Map<String, Calendar>>();

  public CalendarSearchServiceConnector(InitParams initParams) {
    super(initParams);
    calendarService_  = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarServiceImpl.class);
    spaceService_ = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
  }

  @Override
  public Collection<SearchResult> search(SearchContext context,
                                         String query,
                                         Collection<String> sites,
                                         int offset,
                                         int limit,
                                         String sort,
                                         String order) {
    return searchData(context, null, query, sites, offset, limit, sort, order);

  }


  protected Collection<SearchResult> searchData(SearchContext context, String dataType, String query,
                                                Collection<String> sites,
                                                int offset,
                                                int limit,
                                                String sort,
                                                String order) {
    List<SearchResult> events = new ArrayList<SearchResult>();
    if(Utils.isEmpty(query)) {
      return events;
    }

    OrganizationService orgService = (OrganizationService)ExoContainerContext.getCurrentContainer()
              .getComponentInstanceOfType(OrganizationService.class);    
    Set<String> readOnlyCalendars = new HashSet<String>();
    
    try {
      getCalendarMap().clear();
      Identity currentUser = ConversationState.getCurrent().getIdentity(); 
      final String userId = currentUser.getUserId();
      
      List<String> uCals = new LinkedList<String>();
      List<Calendar> privateCalendars = calendarService_.getUserCalendars(userId, true);
      for(Calendar cal : privateCalendars) {
        getCalendarMap().put(cal.getId(), cal);
        uCals.add(cal.getId());
      }

      List<String> sCals = new LinkedList<String>();
      GroupCalendarData sharedCalendar = calendarService_.getSharedCalendars(userId, true) ;
      if(sharedCalendar != null) {
        List<Calendar> shareCalendars = sharedCalendar.getCalendars();
        for(Calendar cal : shareCalendars){
          getCalendarMap().put(cal.getId(), cal);          
          sCals.add(cal.getId());
          
          if(!Utils.hasPermission(orgService, cal.getEditPermission(), userId)) {
            readOnlyCalendars.add(cal.getId());
          }
        }
      }

      List<String> gCals = new LinkedList<String>();
      Set<String> groupIds = currentUser.getGroups();
      if(!groupIds.isEmpty()) {
        List<GroupCalendarData> groupCalendar = calendarService_.getGroupCalendars(groupIds.toArray(new String[groupIds.size()]), true, userId) ;
        if(groupCalendar != null) {
          List<Calendar> spaceCalendars = new ArrayList<Calendar>();
          for(GroupCalendarData gCal : groupCalendar) {
            if(gCal.getCalendars() != null) spaceCalendars.addAll(gCal.getCalendars());
          }
          for(Calendar cal : spaceCalendars) {
            getCalendarMap().put(cal.getId(), cal);
            gCals.add(cal.getId());
            
            if(!Utils.hasPermission(orgService, cal.getEditPermission(), userId)) {
              readOnlyCalendars.add(cal.getId());
            }
          }
        }
      }
      
      List<String> calIds = new LinkedList<String>();
      calIds.addAll(uCals);
      calIds.addAll(gCals);
      calIds.addAll(sCals);
      
      EventQuery uEventQuery = createQuery(dataType, query, sort, order, calIds.toArray(new String[calIds.size()]), readOnlyCalendars);
      EventSearchListAccess listAccess = new EventSearchListAccess((EventDAOImpl) calendarService_.getEventDAO(), uEventQuery);

      events = listAccess.load(offset, limit, context, sites, dataType);
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) log.debug("Could not execute unified seach " + dataType , e) ; 
    }
    return events;
  }

  private EventQuery createQuery(String dataType, String queryText, String sort, String order, String[] calendarIds, Set<String> excludePrivateEventInCalendars) throws Exception {
    UnifiedQuery eventQuery = new UnifiedQuery();
    eventQuery.setExcludePrivateEventInCalendars(excludePrivateEventInCalendars);
    java.util.Calendar today = java.util.Calendar.getInstance();
    eventQuery.setFromDate(today) ;
    eventQuery.setQueryType(Query.SQL);
    eventQuery.setEventType(dataType);
    eventQuery.setText(queryText) ;
    String sortBy =  Utils.SORT_FIELD_MAP.get(sort);
    if(Utils.ORDERBY_DATE.equals(sortBy)) {
      if(CalendarEvent.TYPE_EVENT.equals(dataType))
        sortBy = Utils.EXO_FROM_DATE_TIME ;
      else sortBy = Utils.EXO_TO_DATE_TIME ;
    }
    eventQuery.setOrderBy(new String[]{sortBy});
    eventQuery.setOrderType(order);
    if(CalendarEvent.TYPE_TASK.equals(dataType))
      eventQuery.setState(CalendarEvent.COMPLETED + Utils.COLON + CalendarEvent.CANCELLED);
    eventQuery.setCalendarId(calendarIds);
    
    return eventQuery;
  }
  public String getUrl(Router router, String handler, String siteName, String spaceGroupId, String pageName) {
    HashedMap qualifiedName = new HashedMap();
    qualifiedName.put(QualifiedName.create("gtn", "handler"), handler);
    qualifiedName.put(QualifiedName.create("gtn", "path"), pageName);
    qualifiedName.put(QualifiedName.create("gtn", "lang"), "");
    if(Utils.isEmpty(spaceGroupId)) {
      qualifiedName.put(QualifiedName.create("gtn", "sitename"), siteName);
      qualifiedName.put(QualifiedName.create("gtn", "sitetype"), SiteType.PORTAL.getName());
    } else {
      String groupId = spaceGroupId.split(Utils.SLASH)[2];
      if(spaceService_ != null) {
        Space sp = spaceService_.getSpaceByGroupId(spaceGroupId);
        if(sp != null) groupId = sp.getPrettyName();
      }
      qualifiedName.put(QualifiedName.create("gtn", "sitename"), spaceGroupId.replaceAll("/", ":"));
      qualifiedName.put(QualifiedName.create("gtn", "sitetype"), SiteType.GROUP.getName());
      qualifiedName.put(QualifiedName.create("gtn", "path"), groupId + "/" + pageName);
    }
    try {
      return "/" + handler + URLDecoder.decode(router.render(qualifiedName), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }
  
  private Map<String, Calendar> getCalendarMap() {
    Map<String, Calendar> map = calendarMap.get();
    if (map == null) {
      map = new HashMap<String, Calendar>();      
      calendarMap.set(map);
    }    
    return map;
  }
}
