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

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortalContext;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
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


  private NodeHierarchyCreator nodeHierarchyCreator_;
  private CalendarService calendarService_;
  private OrganizationService organizationService_;
  private SpaceService spaceService_;

  private static final Log     log                 = ExoLogger.getLogger("cs.calendar.unified.search.service");
  private Map<String, Calendar> calendarMap = new HashMap<String,  Calendar>();



  public CalendarSearchServiceConnector(InitParams initParams) {
    super(initParams);
    nodeHierarchyCreator_  = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    calendarService_  = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarServiceImpl.class);
    organizationService_ = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
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
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
      calendarMap.clear();
      String userId = ConversationState.getCurrent().getIdentity().getUserId() ;
      Node calendarHome = nodeHierarchyCreator_.getUserApplicationNode(provider, userId);
      List<Calendar> privateCalendars = calendarService_.getUserCalendars(userId, true);
      for(Calendar cal : privateCalendars){
        calendarMap.put(cal.getId(), cal);
      }

      GroupCalendarData sharedCalendar = calendarService_.getSharedCalendars(userId, true) ;
      if(sharedCalendar != null) {
        List<Calendar> shareCalendars = sharedCalendar.getCalendars();
        for(Calendar cal : shareCalendars){
          calendarMap.put(cal.getId(), cal);
        }
      }
      Collection<Group> group = organizationService_.getGroupHandler().findGroupsOfUser(userId);
      if(!group.isEmpty()) {
        String[] groupIds = new String[group.size()];
        int i = 0 ;
        for(Group g : group) {
          groupIds[i] = g.getId() ;
          i++;
        }
        List<GroupCalendarData> groupCalendar = calendarService_.getGroupCalendars(groupIds, true, userId) ;
        if(groupCalendar != null) {
          List<Calendar> spaceCalendars = new ArrayList<Calendar>();
          for(GroupCalendarData gCal : groupCalendar){
            if(gCal.getCalendars() != null) spaceCalendars.addAll(gCal.getCalendars());
          }
          for(Calendar cal : spaceCalendars){
            calendarMap.put(cal.getId(), cal);
          }
        }
      }

      EventQuery eventQuery = new UnifiedQuery(); 
      java.util.Calendar today = java.util.Calendar.getInstance();
      eventQuery.setFromDate(today) ;
      eventQuery.setQueryType(Query.SQL);
      eventQuery.setEventType(dataType);
      eventQuery.setText(query) ;
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
      //log.info("\n -------" + eventQuery.getQueryStatement() + "\n") ;
      QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
      QueryImpl jcrquery = (QueryImpl)qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType());
      jcrquery.setOffset(offset);
      jcrquery.setLimit(limit);
      QueryResult result = jcrquery.execute();
      /*
      NodeIterator it = result.getNodes();
      while (it.hasNext()) {
        events.add(getResult(it.nextNode()));
      }
       */
      RowIterator rIt = result.getRows();
      while (rIt.hasNext()) {
        SearchResult rs = buildResult(context, sites, dataType, rIt.nextRow());
        if(rs != null) events.add(rs);
      }
    }
    catch (Exception e) {
      log.info("Could not execute unified seach " + dataType , e) ; 
    } finally {
      provider.close();
    }
    return events;
  }

  private SearchResult buildResult(SearchContext sc, Collection<String> siteKeys, String dataType, Object iter) {
    try {
      String calId = null;
      if(iter instanceof Row){
        Row row = (Row) iter;
        calId = row.getValue(Utils.EXO_CALENDAR_ID).getString() ;
      } else {
        Node eventNode = (Node) iter;
        if(eventNode.hasProperty(Utils.EXO_CALENDAR_ID))
          calId = eventNode.getProperty(Utils.EXO_CALENDAR_ID).getString() ;
      }
      if(calendarMap.keySet().contains(calId)) {
        Calendar cal = calendarMap.get(calId);
        String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
        int calType = calendarService_.getTypeOfCalendar(currentUser, calId);
        if(isSearchable(calType, cal, currentUser, iter)){
          StringBuffer detail = new StringBuffer();
          String title = buildValue(Utils.EXO_SUMMARY, iter);
          detail.append(buildCalName(Utils.EXO_CALENDAR_ID, iter)) ; 
          String url = buildLink(sc,siteKeys, calId, buildValue(Utils.EXO_ID, iter));
          String excerpt = buildValue(Utils.EXO_DESCRIPTION, iter);
          String detailValue = Utils.EMPTY_STR;
          String imageUrl = buildImageUrl(iter);
          detail.append(buildDetail(iter));
          if(detail.length() > 0) detailValue = detail.toString();
          long relevancy = buildScore(iter);
          long date = buildDate(iter) ;
          CalendarSearchResult result = new CalendarSearchResult(url, title, excerpt, detailValue, imageUrl, date, relevancy);
          result.setDataType(dataType);
          result.setTimeZoneName(cal.getTimeZone());
          if(CalendarEvent.TYPE_EVENT.equals(dataType)){
            result.setFromDateTime(buildDate(iter, Utils.EXO_FROM_DATE_TIME).getTimeInMillis());
          } else if (CalendarEvent.TYPE_TASK.equals(dataType)) {
            result.setTaskStatus(buildValue(Utils.EXO_EVENT_STATE, iter));
          }
          return result;
        }
      }
    }catch (Exception e) {
      log.info("Error when building result object from result data " + e);
    }
    return null;
  }

  private String buildExcerpt(Object iter) throws RepositoryException{
    StringBuffer origin = new StringBuffer(Utils.EMPTY_STR);
    try {
      if(iter instanceof Row){
        Row row = (Row) iter;
        int counter = 0 ;
        for(String field : Utils.SEARCH_FIELDS)
          if(row.getValue(field) != null) {
            if(counter > 0) origin.append(Utils.SPACE);
            origin.append(row.getValue(field).getString());
            counter++;
          }
      } else {
        Node eventNode = (Node) iter;
        int counter = 0 ;
        for(String field : Utils.SEARCH_FIELDS)
          if(eventNode.hasProperty(field)){
            if(counter > 0) origin.append(Utils.SPACE);
            origin.append(eventNode.getProperty(field).getString());
            counter++;
          }
      }
    } catch (Exception e) {
      log.info("Error when building customer exerpt property from data " + e);
    }
    return origin.toString();
  }

  private String buildImageUrl(Object iter) throws RepositoryException{
    String icon = null;
    if(iter instanceof Row){
      Row row = (Row) iter;
      if(row.getValue(Utils.EXO_EVENT_TYPE) != null)
        if(CalendarEvent.TYPE_TASK.equals(row.getValue(Utils.EXO_EVENT_TYPE).getString())) 
          icon = Utils.TASK_ICON_URL;
        else icon = Utils.EVENT_ICON_URL; 
    } else {
      Node eventNode = (Node) iter;
      if(eventNode.hasProperty(Utils.EXO_EVENT_TYPE)){
        if(CalendarEvent.TYPE_TASK.equals(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString())) 
          icon = Utils.TASK_ICON_URL;
        else icon = Utils.EVENT_ICON_URL;
      }
    }
    return icon;
  }

  private long buildDate(Object iter) {
    try {
      return buildDate(iter, Utils.EXO_DATE_CREATED).getTimeInMillis();
    } catch (Exception e) {
      log.info("Clould not build date value to long from data " + e);
      return 0;
    }
  }

  private java.util.Calendar buildDate(Object iter, String readProperty){
    try {
      if(iter instanceof Row){
        Row row = (Row) iter;
        return row.getValue(readProperty).getDate();
      } else {
        Node eventNode = (Node) iter;
        if(eventNode.hasProperty(readProperty)){
          return eventNode.getProperty(readProperty).getDate();
        } else {
          return null ;
        }
      }
    } catch (Exception e) {
      log.info("Could not build date value from " + readProperty + " : " + e);
      return null;
    }
  }

  private String buildCalName(String property, Object iter) throws RepositoryException{
    if(iter instanceof Row){
      Row row = (Row) iter;
      if(row.getValue(property) != null && calendarMap.get(row.getValue(property).getString()) != null) 
        return calendarMap.get(row.getValue(property).getString()).getName() ;
    } else {
      Node eventNode = (Node) iter;
      if(eventNode.hasProperty(property) && calendarMap.get(eventNode.getProperty(property).getString()) != null){
        return calendarMap.get(eventNode.getProperty(property).getString()).getName();
      }
    }
    return Utils.EMPTY_STR;
  }


  private long buildScore(Object iter){
    try {
      if(iter instanceof Row){
        Row row = (Row) iter;
        return row.getValue(Utils.JCR_SCORE).getLong() ;
      }
    } catch (Exception e) {
      log.info("No score return by query " + e);
    }
    return 0;
  }

  private String buildValue(String property, Object iter) throws RepositoryException{
    if(iter instanceof Row){
      Row row = (Row) iter;
      if(row.getValue(property) != null) return row.getValue(property).getString() ;
    } else {
      Node eventNode = (Node) iter;
      if(eventNode.hasProperty(property)){
        return eventNode.getProperty(property).getString();
      }
    } 
    return Utils.EMPTY_STR;
  }

  private String buildDetail(Object iter) throws RepositoryException{
    SimpleDateFormat df = new SimpleDateFormat(Utils.DATE_TIME_FORMAT) ;
    StringBuffer detail = new StringBuffer();
    if(iter instanceof Row){
      Row row = (Row) iter;
      if(row.getValue(Utils.EXO_EVENT_TYPE) != null)
        if(CalendarEvent.TYPE_EVENT.equals(row.getValue(Utils.EXO_EVENT_TYPE).getString())) {
          if(row.getValue(Utils.EXO_FROM_DATE_TIME) != null)
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(df.format(row.getValue(Utils.EXO_FROM_DATE_TIME).getDate().getTime())) ;
          if(row.getValue(Utils.EXO_LOCATION) != null)
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(row.getValue(Utils.EXO_LOCATION).getString()) ;
        } else {
          if(row.getValue(Utils.EXO_TO_DATE_TIME) != null)
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(Utils.DUE_FOR).append(df.format(row.getValue(Utils.EXO_TO_DATE_TIME).getDate().getTime()));
        }
    } else {
      Node eventNode = (Node) iter;
      if(eventNode.hasProperty(Utils.EXO_EVENT_TYPE)){
        if(CalendarEvent.TYPE_EVENT.equals(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString())) {
          if(eventNode.hasProperty(Utils.EXO_FROM_DATE_TIME)) {
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(df.format(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime())) ;
          }
          if(eventNode.hasProperty(Utils.EXO_LOCATION)) {
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(eventNode.getProperty(Utils.EXO_LOCATION).getString()) ;
          }
        } else {
          if(eventNode.hasProperty(Utils.EXO_TO_DATE_TIME)) {
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(Utils.DUE_FOR).append(df.format(eventNode.getProperty(Utils.EXO_TO_DATE_TIME).getDate().getTime())) ;
          }
        }
      }
    }  
    return detail.toString();
  }

  private boolean isSearchable(int calType, Calendar cal, String uId,  Object iter) throws Exception{
    Row row = (Row) iter;
    boolean isTask = CalendarEvent.TYPE_TASK.equals(row.getValue(Utils.EXO_EVENT_TYPE).getString());
    boolean isPrivateCalendar = (Calendar.TYPE_PRIVATE == calType);
    boolean isPrivateEvent = row.getValue(Utils.EXO_IS_PRIVATE).getBoolean();
    boolean isViewPublic = (!isPrivateCalendar && !isPrivateEvent);
    boolean isEditable = (!isTask && !isPrivateCalendar && isPrivateEvent && (cal.getEditPermission() != null) && Utils.canEdit(organizationService_, cal.getEditPermission(), uId)); 
    return (isTask || isPrivateCalendar || isViewPublic || isEditable); 
  }

  private String buildLink(SearchContext sc, Collection<String> siteKeys, String calendarId, String eventId) {
    String url = Utils.NONE_NAGVIGATION;
    if(sc != null)
      try {
        Router router = sc.getRouter();
        ExoContainerContext context = (ExoContainerContext)ExoContainerContext.getCurrentContainer()
            .getComponentInstanceOfType(ExoContainerContext.class);
        String handler = context.getPortalContainerName();
        SiteKey siteKey = null ;
        String spaceGroupId = null;
        if (calendarId.indexOf(Utils.SPACE_CALENDAR_ID_SUFFIX) > 0) {
          spaceGroupId = calendarMap.get(calendarId).getCalendarOwner();
          siteKey = SiteKey.group(spaceGroupId);
        } else {
          UserPortalConfig prc = getUserPortalConfig();
          siteKey = SiteKey.portal(prc.getPortalConfig().getName());
        }
        if(siteKey != null) {
          if(!Utils.isEmpty(siteKey.getName())) {
            String pageName = getSiteName(siteKey);
            if(Utils.isEmpty(pageName)) {
              siteKey = SiteKey.portal(sc.getSiteName() != null ? sc.getSiteName():Utils.DEFAULT_SITENAME);
              pageName = getSiteName(siteKey);
            }
            url = new StringBuffer(getUrl(router, handler, siteKey.getName(), spaceGroupId, pageName)).append(Utils.SLASH).append(Utils.DETAIL_PATH).append(Utils.SLASH).append(eventId).toString();
          }
        }
      } catch (Exception e) {
        log.info("build link error !");
      }
    return url;
  }

  private static String getSiteName(SiteKey siteKey) {
    try{
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      NavigationService navService = (NavigationService) container.getComponentInstance(NavigationService.class);
      NavigationContext nav = navService.loadNavigation(siteKey);
      NodeContext<NodeContext<?>> parentNodeCtx = navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      if (parentNodeCtx.getSize() >= 1) {
        Collection<NodeContext<?>> children = parentNodeCtx.getNodes();
        if (siteKey.getType() == SiteType.GROUP) {
          children = parentNodeCtx.get(0).getNodes();
        }
        Iterator<NodeContext<?>> it = children.iterator();
        NodeContext<?> child = null;
        while (it.hasNext()) {
          child = it.next();
          if (Utils.PAGE_NAGVIGATION.equals(child.getName()) || child.getName().indexOf(Utils.PORTLET_NAME) >= 0) {
            return child.getName();
          }
        }
      }
    } catch (NullPointerException e) {
      return Utils.EMPTY_STR;
    }
    return Utils.EMPTY_STR;
  }
  private static UserPortalConfig getUserPortalConfig() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserPortalConfigService userPortalConfigSer = (UserPortalConfigService)
        container.getComponentInstanceOfType(UserPortalConfigService.class);
    UserPortalContext NULL_CONTEXT = new UserPortalContext() {
      public ResourceBundle getBundle(UserNavigation navigation) {
        return null;
      }
      public Locale getUserLocale() {
        return Locale.ENGLISH;
      }
    };
    String remoteId = ConversationState.getCurrent().getIdentity().getUserId() ;
    UserPortalConfig userPortalCfg = userPortalConfigSer.
        getUserPortalConfig(userPortalConfigSer.getDefaultPortal(), remoteId, NULL_CONTEXT);
    return userPortalCfg;
  }

  public String getUrl(Router router, String handler, String siteName, String spaceGroupId, String pageName) {
    try {
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
      return "/" + handler + URLDecoder.decode(router.render(qualifiedName), "UTF-8");
    } catch (Exception e) {
      return null;
    }
  }
}
