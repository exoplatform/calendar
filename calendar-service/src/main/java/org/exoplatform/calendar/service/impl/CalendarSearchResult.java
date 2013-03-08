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
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;
import org.mortbay.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 2, 2013  
 */

/**
 * Dedicated result object query by Event or Task search plugin
 * we extended more information from SearchResult object 
 * @author tuanp
 *
 */
public class CalendarSearchResult extends SearchResult {
  private long fromDateTime;
  private String dataType;
  private String zoneName;
  private String taskStatus;
  public CalendarSearchResult(String url,
                              String title,
                              String excerpt,
                              String detailValue,
                              String imageUrl, long date,
                              long relevancy) {
    super(url, title, excerpt, detailValue, imageUrl, date, relevancy);
  }

  /**
   * 
   * @return from date time value of event type only
   */
  public long getFromDateTime() {
    return fromDateTime;
  }

  public void setFromDateTime(long fromDateTime) {
    this.fromDateTime = fromDateTime;
  }

  /**
   * 
   * @return data type : event || task 
   */
  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public String getImageUrl(){
    return super.getImageUrl();
  }
  public void setTimeZoneName(String name) {
    zoneName = name;
  }

  public String getTimeZoneName() {
    return zoneName;
  }

  public static String buildLink(SearchContext sc, Collection<String> siteKeys, String calendarId, String eventId) {
    String url = Utils.NONE_NAGVIGATION;
    if(sc != null)
      try {
        Router router = sc.getRouter();
        ExoContainerContext context = (ExoContainerContext)ExoContainerContext.getCurrentContainer()
            .getComponentInstanceOfType(ExoContainerContext.class);
        String handler = context.getPortalContainerName();
        SiteKey siteKey = null ;
        String spaceGroupId = null;
        if (calendarId.indexOf(Utils.SPACE_ID_PREFIX) > 0) {
          spaceGroupId = String.format("/%s/%s", Utils.SPACES_GROUP, calendarId.replaceFirst(Utils.SPACE_ID_PREFIX, ""));// /spaces/space1
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
        Log.info("Could not build the link of event/task " + e.getMessage());
        //e.printStackTrace();
      }
    return url;
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

  private static String getSiteName(SiteKey siteKey) {
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
    return Utils.EMPTY_STR;
  }

  public static String getUrl(Router router, String handler, String siteName, String spaceGroupId, String pageName) {
    try {
      HashedMap qualifiedName = new HashedMap();
      qualifiedName.put(QualifiedName.create("gtn", "handler"), handler);
      qualifiedName.put(QualifiedName.create("gtn", "path"), pageName);
      qualifiedName.put(QualifiedName.create("gtn", "lang"), "");
      if(Utils.isEmpty(spaceGroupId)) {
        qualifiedName.put(QualifiedName.create("gtn", "sitename"), siteName);
        qualifiedName.put(QualifiedName.create("gtn", "sitetype"), SiteType.PORTAL.getName());
      } else {
        String groupId = spaceGroupId.split("/")[2];
        qualifiedName.put(QualifiedName.create("gtn", "sitename"), spaceGroupId.replaceAll("/", ":"));
        qualifiedName.put(QualifiedName.create("gtn", "sitetype"), SiteType.GROUP.getName());
        qualifiedName.put(QualifiedName.create("gtn", "path"), groupId + "/" + pageName);
      }
      return "/" + handler + URLDecoder.decode(router.render(qualifiedName), "UTF-8");
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * @return value base on task status if data is task 
   *  needs-action || in-process
   */
  public String getTaskStatus() {
    return taskStatus;
  }

  public void setTaskStatus(String taskStatus) {
    this.taskStatus = taskStatus;
  }
}
