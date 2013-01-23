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

import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;

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
      if (!Utils.isEmpty(getCalendarPath()))
        queryString = new StringBuffer(" select * from " + getNodeType() + " where jcr:path like '" + getCalendarPath() + "/%'");
      else
        queryString = new StringBuffer(" select * from " + getNodeType() + " ");

      String text = getText() ;
      if (!Utils.isEmpty(text)) {
        Collection<String> inputs = parse(text) ;
        queryString.append(" and (");
        String[] filters = {Utils.EXO_SUMMARY,Utils.EXO_DESCRIPTION, Utils.EXO_LOCATION} ;
        int filterCount = 0 ;
        for(String filter : filters) {
          if(filterCount != 0) queryString.append(" or (");
          else queryString.append(" (");
          for (String words : inputs) {
            words.replaceAll("-", Utils.EMPTY_STR);
            String[] wordSet = words.split(Utils.SPACE);
            if(wordSet.length > 1) {
              queryString.append(" (");
              int count = 0 ;
              for(String word : wordSet) {
                if(word.trim().length() > 0){ 
                  if(count != 0) queryString.append(" and (");
                  else queryString.append(" (");
                  queryString.append(filter + " like '%" + word.trim() + "%'");
                  queryString.append(")") ;
                  count ++;
                }
              }
              queryString.append(" ) ");
            } else {
              queryString.append(" or (");
              queryString.append(filter + " like '%" + words + "%'");
              queryString.append(" ) ");
            }  
            // queryString.append(" or " + Utils.EXO_DESCRIPTION + " like '%" + text + "%'");
            //queryString.append(" or " + Utils.EXO_LOCATION + " like '%" + text + "%'");
            //queryString.append(" or " + Utils.EXO_PARTICIPANT + " like '%" + text + "%'");
            //queryString.append(" or " + Utils.EXO_INVITATION + " like '%" + text + "%'");
            // queryString.append(" and contains (.,'"+ text +"') ") ;
            //queryString.append(")");
          }

        }
        filterCount ++ ;
        queryString.append(" ) ") ;
      }
      if (!Utils.isEmpty(getEventType())) {
        queryString.append(" and " + Utils.EXO_EVENT_TYPE + " = '" + getEventType() + "'");
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
    String remain = matcher.replaceAll("").replaceAll("\"", "").trim(); //remove all remaining double quotes
    if(!remain.isEmpty()) terms.addAll(Arrays.asList(remain.split("\\s+")));
    return terms;
  }
}
