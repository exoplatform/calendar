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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.query.Query;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ISO8601;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 23, 2013  
 */
public class UnifiedQuery extends EventQuery {

  public String getQueryStatement() throws Exception {
    StringBuffer queryString = new StringBuffer("");

    if (getQueryType().equals(Query.SQL)) {
      String text = getText() ;
      if (!Utils.isEmpty(text)) {
        queryString = new StringBuffer("SELECT ");
        queryString.append(repeat("%s", Arrays.asList(Utils.SELECT_FIELDS), ","));
        queryString.append(" FROM " + getNodeType() + " WHERE ");
        if(!Utils.isEmpty(getCalendarPath())) {
          queryString.append("jcr:path LIKE '").append(getCalendarPath()).append("/%' AND ");
        }
        if (!Utils.isEmpty(getEventType())) {
          queryString.append(Utils.EXO_EVENT_TYPE).append(" = '").append(getEventType()).append("' AND (");
        }
        Collection<String> inputs = parse(text) ;
        int inputCount = 0 ;
        for(String keyword : inputs){ 
          if(inputCount > 0) queryString.append(" AND ");
          queryString.append("(");
          int filterCount = 0 ;
          for(String filter : Utils.SEARCH_FIELDS) {
            if(filterCount > 0) queryString.append(" OR ");
            queryString.append("CONTAINS(").append(filter).append(",'").append(escapeContainsQuery(keyword)).append("')");
            filterCount ++ ;
          }
          queryString.append(")");
          inputCount ++ ;
        }
        if (!Utils.isEmpty(getEventType())) {
          queryString.append(") ");
        }
        if (getFromDate() != null && CalendarEvent.TYPE_EVENT.equals(getEventType())) {
          queryString.append(" AND ").append(queryStringForFromTime(getFromDate()));
        }
        if (!Utils.isEmpty(getState())) {
          if(getState().indexOf(Utils.COLON) >0)
            for(String state : getState().split(Utils.COLON))
          queryString.append(" AND ").append(Utils.EXO_EVENT_STATE).append(" <> '").append(state).append("'");
        }
        if(getOrderBy() != null && getOrderBy().length > 0) {
          queryString.append(" ORDER BY ").append(getOrderBy()[0]);
          if(!Utils.isEmpty(getOrderType())) queryString.append(Utils.SPACE).append(getOrderType().toUpperCase());
        }
      }
    }
    return queryString.toString();
  }

  public static List<String> parse(String input) {
    List<String> terms = new LinkedList<String>();
    Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(input);
    while (matcher.find()) {
      String founds = matcher.group(1);
      terms.add(founds);
    }
    String remain = matcher.replaceAll("").replaceAll("\"\"", "").trim(); //remove all remaining double quotes
    if(!remain.isEmpty()) terms.addAll(Arrays.asList(remain.split("\\s+")));
    return terms;
  }

  private static String repeat(String format, Collection<String> strArr, String delimiter){
    StringBuilder sb=new StringBuilder();
    String delim = "";
    for(String str:strArr) {
      sb.append(delim).append(String.format(format, str));
      delim = delimiter;
    }
    return sb.toString();
  }
  
  /*
   * builds query string to check if event is not end after a specified date
   */
  private String queryStringForFromTime(java.util.Calendar cal) {
    StringBuilder sb = new StringBuilder("(");
    sb.append(Utils.EXO_FROM_DATE_TIME).append(" >= TIMESTAMP '").append(ISO8601.format(cal)).append("'"); // from date is after cal
    sb.append(" OR ").append("(");
    sb.append(Utils.EXO_REPEAT).append(" <> '").append(CalendarEvent.RP_NOREPEAT).append("'"); // if event is repeated
    sb.append(" AND ").append("(");
    sb.append(Utils.EXO_REPEAT_FINISH_DATE).append(" >= TIMESTAMP '").append(ISO8601.format(cal)).append("'"); // having the repeat finish date after cal
    sb.append(" OR ");
    sb.append(Utils.EXO_REPEAT_FINISH_DATE).append(" IS ").append("NULL"); // having no property exo:repeatFinishDate (never end event)
    sb.append(")))");
    return sb.toString();
  }
}
