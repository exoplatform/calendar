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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 22, 2013  
 */
public class CalendarSearchServiceConnector extends SearchServiceConnector {

  final private static String  CALENDARS           = "calendars".intern();

  final private static String  SHARED_CALENDAR     = "sharedCalendars".intern();

  final private static String  CALENDAR_CATEGORIES = "categories".intern();

  final private static String  FEED                = "eXoCalendarFeed".intern();

  final private static String  CALENDAR_EVENT      = "events".intern();

  final private static String  CALENDAR_SETTING    = "calendarSetting".intern();

  final private static String  EVENT_CATEGORIES    = "eventCategories".intern();

  private final static String  VALUE               = "value".intern();

  private NodeHierarchyCreator nodeHierarchyCreator_;

  private RepositoryService    repoService_;

  private SessionProviderService sessionProviderService_;

  private static final Log     log                 = ExoLogger.getLogger("cs.calendar.service");

  public CalendarSearchServiceConnector(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService) {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repoService_ = repoService;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    sessionProviderService_ = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
  }

  @Override
  public Collection<String> getSortFields() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<SearchResult> search(String query,
                                         Collection<String> sites,
                                         int offset,
                                         int limit,
                                         String sort,
                                         String order) {
    List<SearchResult> events = new ArrayList<SearchResult>();
    try {
      String userId = ConversationState.getCurrent().getIdentity().getUserId() ;
      Node calendarHome = nodeHierarchyCreator_.getUserApplicationNode(SessionProvider.createSystemProvider(), userId);
      EventQuery eventQuery = new UnifiedQuery(); 
      //eventQuery.setCalendarPath(calendarHome.getPath());
      eventQuery.setText(query) ;
      QueryManager qm = calendarHome.getSession().getWorkspace().getQueryManager();
      Query jcrquery = qm.createQuery(eventQuery.getQueryStatement(), eventQuery.getQueryType());
      QueryResult result = jcrquery.execute();
      NodeIterator it = result.getNodes();
      while (it.hasNext()) {
        events.add(getResult(it.nextNode()));
      }
    }
    catch (Exception e) {
      log.info("Could not execute unified seach " , e) ; 
    }
    return events;

  }

  private SearchResult getResult(Node data) {
    SearchResult result = new SearchResult() ;

    return result;

  }

}
