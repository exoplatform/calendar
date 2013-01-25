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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
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


  private NodeHierarchyCreator nodeHierarchyCreator_;

  private static final Log     log                 = ExoLogger.getLogger("cs.calendar.service");

  public CalendarSearchServiceConnector(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService) {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
  }


  @Override
  public Collection<SearchResult> search(String query,
                                         Collection<String> sites,
                                         int offset,
                                         int limit,
                                         String sort,
                                         String order) {
    return searchData(null, query, sites, offset, limit, sort, order);

  }


  protected Collection<SearchResult> searchData(String dataType, String query,
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
      eventQuery.setQueryType(Query.SQL);
      eventQuery.setEventType(dataType);
      eventQuery.setText(query) ;
      eventQuery.setOrderBy(new String[]{Utils.sortFieldsMap.get(sort)});
      eventQuery.setOrderType(order);
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
        events.add(getResult(rIt.nextRow()));
      }
    }
    catch (Exception e) {
      log.info("Could not execute unified seach " + dataType , e) ; 
    }
    return events;

  }

  private SearchResult getResult(Object iter) {
    SearchResult result = new SearchResult() ;
    try {
      StringBuffer detail = new StringBuffer();
      result.setTitle(buildValue(Utils.EXO_SUMMARY, iter));
      detail.append(result.getTitle()) ; 
      result.setUrl(buildValue(Utils.EXO_ID, iter));
      result.setExcerpt(buildValue(Utils.EXO_DESCRIPTION, iter));
      detail.append(buildDetail(iter));
      if(detail.length() > 0) result.setDetail(detail.toString());
      result.setRelevancy(getScore(iter));
    }catch (Exception e) {
      log.info("Error when getting property from node " + e);
    }
    return result;
  }

  private long getScore(Object iter){
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
    return null;
  }

  private String buildDetail(Object iter) throws RepositoryException{
    SimpleDateFormat df = new SimpleDateFormat("EEEEE, MMMMMMMM d, yyyy K:mm a") ;
    StringBuffer detail = new StringBuffer();
    if(iter instanceof Row){
      Row row = (Row) iter;
      if(row.getValue(Utils.EXO_EVENT_TYPE) != null)
        if(CalendarEvent.TYPE_EVENT.equals(row.getValue(Utils.EXO_EVENT_TYPE).getString())) {
          if(row.getValue(Utils.EXO_FROM_DATE_TIME) != null)
            detail.append("-").append(df.format(row.getValue(Utils.EXO_FROM_DATE_TIME).getDate().getTime())) ;
          if(row.getValue(Utils.EXO_LOCATION) != null)
            detail.append("-").append(row.getValue(Utils.EXO_LOCATION).getString()) ;
        } else {
          if(row.getValue(Utils.EXO_TO_DATE_TIME) != null)
            detail.append("- Due for: ").append("-").append(df.format(row.getValue(Utils.EXO_TO_DATE_TIME).getDate().getTime()));
        }
    } else {
      Node eventNode = (Node) iter;
      if(eventNode.hasProperty(Utils.EXO_EVENT_TYPE)){
        if(CalendarEvent.TYPE_EVENT.equals(eventNode.getProperty(Utils.EXO_EVENT_TYPE).getString())) {
          if(eventNode.hasProperty(Utils.EXO_FROM_DATE_TIME)) {
            detail.append("-").append(df.format(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime())) ;
          }
          if(eventNode.hasProperty(Utils.EXO_LOCATION)) {
            detail.append("-").append(eventNode.getProperty(Utils.EXO_LOCATION).getString()) ;
          }
        } else {
          if(eventNode.hasProperty(Utils.EXO_TO_DATE_TIME)) {
            detail.append("- Due for: ").append("-").append(df.format(eventNode.getProperty(Utils.EXO_TO_DATE_TIME).getDate().getTime())) ;
          }
        }
      }
    }  
    return detail.toString();
  }
}
