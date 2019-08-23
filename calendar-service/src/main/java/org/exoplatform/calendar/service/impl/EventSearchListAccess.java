/*
 * Copyright (C) 2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.calendar.service.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.LocaleUtils;
import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.utils.DateUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.*;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;

public class EventSearchListAccess extends AbstractEventListAccess<SearchResult> {

  private static Log log = ExoLogger.getLogger(EventSearchListAccess.class);

  private ThreadLocal<Map<String, Calendar>> calendarMap = new ThreadLocal<Map<String, Calendar>>();

  private SpaceService spaceService_ = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
  
  private CalendarService calendarService_  = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarServiceImpl.class);

  public EventSearchListAccess(EventDAOImpl evtDAO, EventQuery eventQuery) {
    super(evtDAO, eventQuery);
  }

  @Override
  public SearchResult[] load(int offset, int limit) throws Exception {
    return new SearchResult[0];
  }

  public List<SearchResult> load(int offset, int limit, SearchContext context, Collection<String> sites, String dataType) throws RepositoryException {
    SessionProvider provider = SessionProvider.createSystemProvider();
    OrganizationService orgService = (OrganizationService) ExoContainerContext.getCurrentContainer()
            .getComponentInstanceOfType(OrganizationService.class);
    Set<String> readOnlyCalendars = new HashSet<String>();
    List<SearchResult> events = new ArrayList<SearchResult>();
    try {
      QueryResultImpl queryResult = super.loadData(provider, offset, limit);
      if (queryResult != null) {
        RowIterator rows = queryResult.getRows();
        
        List<Row> results = new LinkedList<Row>();
        while (rows.hasNext()) {
          results.add(rows.nextRow());
        }
        try {
          getCalendarMap().clear();
          Identity currentUser = ConversationState.getCurrent().getIdentity();
          final String userId = currentUser.getUserId();

          List<String> uCals = new LinkedList<String>();
          List<Calendar> privateCalendars = calendarService_.getUserCalendars(userId, true);
          for (Calendar cal : privateCalendars) {
            getCalendarMap().put(cal.getId(), cal);
            uCals.add(cal.getId());
          }
          List<String> sCals = new LinkedList<String>();
          GroupCalendarData sharedCalendar = calendarService_.getSharedCalendars(userId, true);
          if (sharedCalendar != null) {
            List<Calendar> shareCalendars = sharedCalendar.getCalendars();
            for (Calendar cal : shareCalendars) {
              getCalendarMap().put(cal.getId(), cal);
              sCals.add(cal.getId());

              if (!Utils.hasPermission(orgService, cal.getEditPermission(), userId)) {
                readOnlyCalendars.add(cal.getId());
              }
            }
          }

          List<String> gCals = new LinkedList<String>();
          Set<String> groupIds = currentUser.getGroups();
          if (!groupIds.isEmpty()) {
            List<GroupCalendarData> groupCalendar = calendarService_.getGroupCalendars(groupIds.toArray(new String[groupIds.size()]), true, userId);
            if (groupCalendar != null) {
              List<Calendar> spaceCalendars = new ArrayList<Calendar>();
              for (GroupCalendarData gCal : groupCalendar) {
                if (gCal.getCalendars() != null) spaceCalendars.addAll(gCal.getCalendars());
              }
              for (Calendar cal : spaceCalendars) {
                getCalendarMap().put(cal.getId(), cal);
                gCals.add(cal.getId());

                if (!Utils.hasPermission(orgService, cal.getEditPermission(), userId)) {
                  readOnlyCalendars.add(cal.getId());
                }
              }
            }
          }

          List<String> calIds = new LinkedList<String>();
          calIds.addAll(uCals);
          calIds.addAll(gCals);
          calIds.addAll(sCals);

          CalendarSetting calSetting = calendarService_.getCalendarSetting(userId);

          Object[] rowsResult = results.toArray(new Row[results.size()]);
          for (Object row : rowsResult) {
            SearchResult rs = buildResult(context, sites, dataType, row, calSetting);
            if (rs != null) events.add(rs);
          }
        } catch (Exception ex) {
          log.error(ex.getMessage(), ex);
        }
      }
    } finally {
      provider.close();
    }
    return events;
  }

  private SearchResult buildResult(SearchContext sc, Collection<String> siteKeys, String dataType, Object iter, CalendarSetting calSeting) {
    try {
      String calId = null;
      if (iter instanceof Row) {
        Row row = (Row) iter;
        calId = row.getValue(Utils.EXO_CALENDAR_ID).getString();
      } else {
        Node eventNode = (Node) iter;
        if (eventNode.hasProperty(Utils.EXO_CALENDAR_ID))
          calId = eventNode.getProperty(Utils.EXO_CALENDAR_ID).getString();
      }
      if (getCalendarMap().keySet().contains(calId)) {
        Calendar cal = getCalendarMap().get(calId);

        StringBuffer detail = new StringBuffer();
        String title = buildValue(Utils.EXO_SUMMARY, iter);
        detail.append(cal.getName());
        String url = buildLink(sc, siteKeys, calId, buildValue(Utils.EXO_ID, iter));
        String excerpt = buildValue(Utils.EXO_DESCRIPTION, iter);
        String detailValue = Utils.EMPTY_STR;
        String imageUrl = buildImageUrl(iter);
        String lang = "en";
        if (sc != null) {
          lang = sc.getParamValue(SearchContext.RouterParams.LANG.create());
        }
        detail.append(buildDetail(iter, calSeting.getTimeZone(), lang));
        if (detail.length() > 0) detailValue = detail.toString();
        long relevancy = buildScore(iter);
        long date = buildDate(iter);
        TimeZone userTimezone = DateUtils.getTimeZone(calSeting.getTimeZone());
        CalendarSearchResult result = new CalendarSearchResult(url, title, excerpt, detailValue, imageUrl, date, relevancy);
        result.setDataType(dataType);
        result.setTimeZoneName(calSeting.getTimeZone());
        result.setTimeZoneOffset(userTimezone.getOffset(date));
        if (CalendarEvent.TYPE_EVENT.equals(dataType)) {
          result.setFromDateTime(buildDate(iter, Utils.EXO_FROM_DATE_TIME).getTimeInMillis());
        } else if (CalendarEvent.TYPE_TASK.equals(dataType)) {
          result.setTaskStatus(buildValue(Utils.EXO_EVENT_STATE, iter));
        }
        return result;
      }
    } catch (Exception e) {
      log.error("Error when building result object from result data ", e);
    }
    return null;
  }

  private String buildValue(String property, Object iter) throws RepositoryException {
    if (iter instanceof Row) {
      Row row = (Row) iter;
      if (row.getValue(property) != null) return row.getValue(property).getString();
    } else {
      Node eventNode = (Node) iter;
      if (eventNode.hasProperty(property)) {
        return eventNode.getProperty(property).getString();
      }
    }
    return Utils.EMPTY_STR;
  }

  private String buildLink(SearchContext sc, Collection<String> siteKeys, String calendarId, String eventId) {
    String url = Utils.NONE_NAGVIGATION;
    if (sc != null)
      try {
        SiteKey siteKey = null;
        String spaceGroupId = null;
        String siteName = sc.getSiteName() != null ? sc.getSiteName() : Utils.DEFAULT_SITENAME;

        if (calendarId.indexOf(Utils.SPACE_CALENDAR_ID_SUFFIX) > 0) {
          Calendar cal = getCalendarMap().get(calendarId);

          // In case of space calendar, calendarOwner always not null
          String ownerGroup = cal.getCalendarOwner();
          String[] groups = cal.getGroups();

          // Because user can remove space-group from calendar in portal-calendar page
          // In this case, we will redirect to portal-calendar page instead of space-calendar page when user click
          // to view event-detail (keep siteKey = null) to avoid he can not see event detail
          for (String g : groups) {
            if (g.equals(ownerGroup)) {
              spaceGroupId = ownerGroup;
              siteKey = SiteKey.group(spaceGroupId);
            }
          }
        }

        if (siteKey == null) {
          siteKey = SiteKey.portal(siteName);
        }

        String pageName = findCalendarPageNode(siteKey);
        if (Utils.isEmpty(pageName)) {
          siteKey = SiteKey.portal(siteName);
          pageName = findCalendarPageNode(siteKey);
        }

        Router router = sc.getRouter();
        ExoContainerContext context = (ExoContainerContext) ExoContainerContext.getCurrentContainer()
                .getComponentInstanceOfType(ExoContainerContext.class);
        String handler = context.getPortalContainerName();
        url = new StringBuffer(getUrl(router, handler, siteKey.getName(), spaceGroupId, pageName)).append(Utils.SLASH).append(Utils.DETAIL_PATH).append(Utils.SLASH).append(eventId).toString();
      } catch (Exception e) {
        if (log.isDebugEnabled()) log.debug("build link error !");
      }
    return url;
  }

  private String buildImageUrl(Object iter) throws RepositoryException {
    String icon = null;
    if (iter instanceof Row) {
      Row row = (Row) iter;
      if (row.getValue(Utils.EXO_EVENT_TYPE) != null)
        if (CalendarEvent.TYPE_TASK.equals(row.getValue(Utils.EXO_EVENT_TYPE).getString()))
          icon = Utils.TASK_ICON_URL;
        else icon = Utils.EVENT_ICON_URL;
    } else {
      Node eventNode = (Node) iter;
      if (eventNode.hasProperty(Utils.EXO_EVENT_TYPE)) {
        if (CalendarEvent.TYPE_TASK.equals(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString()))
          icon = Utils.TASK_ICON_URL;
        else icon = Utils.EVENT_ICON_URL;
      }
    }
    return icon;
  }

  private long buildDate(Object iter) {
    try {
      return buildDate(iter, Utils.EXO_DATE_CREATED).getTimeInMillis();
    } catch (NullPointerException e) {
      if (log.isDebugEnabled()) log.debug("Clould not build date value to long from data " + e);
      return 0;
    }
  }

  private java.util.Calendar buildDate(Object iter, String readProperty) {
    try {
      if (iter instanceof Row) {
        Row row = (Row) iter;
        return row.getValue(readProperty).getDate();
      } else {
        Node eventNode = (Node) iter;
        if (eventNode.hasProperty(readProperty)) {
          return eventNode.getProperty(readProperty).getDate();
        } else {
          return null;
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) log.debug("Could not build date value from " + readProperty + " : " + e);
      return null;
    }
  }

  public String getUrl(Router router, String handler, String siteName, String spaceGroupId, String pageName) {
    HashedMap qualifiedName = new HashedMap();
    qualifiedName.put(QualifiedName.create("gtn", "handler"), handler);
    qualifiedName.put(QualifiedName.create("gtn", "path"), pageName);
    qualifiedName.put(QualifiedName.create("gtn", "lang"), "");
    if (Utils.isEmpty(spaceGroupId)) {
      qualifiedName.put(QualifiedName.create("gtn", "sitename"), siteName);
      qualifiedName.put(QualifiedName.create("gtn", "sitetype"), SiteType.PORTAL.getName());
    } else {
      String groupId = spaceGroupId.split(Utils.SLASH)[2];
      if (spaceService_ != null) {
        Space sp = spaceService_.getSpaceByGroupId(spaceGroupId);
        if (sp != null) groupId = sp.getPrettyName();
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

  private String buildDetail(Object iter, String timeZone, String lang) throws RepositoryException {
    Locale locale = LocaleUtils.toLocale(lang);
    SimpleDateFormat df = new SimpleDateFormat(Utils.DATE_TIME_FORMAT, locale);
    df.setTimeZone(DateUtils.getTimeZone(timeZone));
    StringBuffer detail = new StringBuffer();
    if (iter instanceof Row) {
      Row row = (Row) iter;
      if (row.getValue(Utils.EXO_EVENT_TYPE) != null)
        if (CalendarEvent.TYPE_EVENT.equals(row.getValue(Utils.EXO_EVENT_TYPE).getString())) {
          if (row.getValue(Utils.EXO_FROM_DATE_TIME) != null)
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(df.format(row.getValue(Utils.EXO_FROM_DATE_TIME).getDate().getTime()));
          if (row.getValue(Utils.EXO_LOCATION) != null)
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(row.getValue(Utils.EXO_LOCATION).getString());
        } else {
          if (row.getValue(Utils.EXO_TO_DATE_TIME) != null)
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(Utils.DUE_FOR).append(df.format(row.getValue(Utils.EXO_TO_DATE_TIME).getDate().getTime()));
        }
    } else {
      Node eventNode = (Node) iter;
      if (eventNode.hasProperty(Utils.EXO_EVENT_TYPE)) {
        if (CalendarEvent.TYPE_EVENT.equals(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString())) {
          if (eventNode.hasProperty(Utils.EXO_FROM_DATE_TIME)) {
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(df.format(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime()));
          }
          if (eventNode.hasProperty(Utils.EXO_LOCATION)) {
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(eventNode.getProperty(Utils.EXO_LOCATION).getString());
          }
        } else {
          if (eventNode.hasProperty(Utils.EXO_TO_DATE_TIME)) {
            detail.append(Utils.SPACE).append(Utils.MINUS).append(Utils.SPACE).append(Utils.DUE_FOR).append(df.format(eventNode.getProperty(Utils.EXO_TO_DATE_TIME).getDate().getTime()));
          }
        }
      }
    }
    return detail.toString();
  }

  private static String findCalendarPageNode(SiteKey siteKey) {
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      NavigationService navService = (NavigationService) container.getComponentInstance(NavigationService.class);
      NavigationContext nav = navService.loadNavigation(siteKey);

      Scope scope;
      if (siteKey.getType().equals(SiteType.GROUP)) {
        scope = Scope.GRANDCHILDREN;
      } else {
        scope = Scope.CHILDREN;
      }
      NodeContext<NodeContext<?>> parentNodeCtx = navService.loadNode(NodeModel.SELF_MODEL, nav, scope, null);
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

  private long buildScore(Object iter) {
    try {
      if (iter instanceof Row) {
        Row row = (Row) iter;
        return row.getValue(Utils.JCR_SCORE).getLong();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) log.debug("No score return by query " + e);
    }
    return 0;
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