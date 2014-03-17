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
package org.exoplatform.calendar.service;

import javax.jcr.query.Query;
import org.exoplatform.commons.utils.ISO8601;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class EventQuery {
  private String             nodeType           = "exo:calendarEvent";

  private String             eventType;

  private String             text               = null;

  private String[]           categoryIds        = null;

  private String[]           calendarIds        = null;

  private String[]           filterCalendarIds  = null;

  private java.util.Calendar fromDate           = null;

  private java.util.Calendar toDate             = null;

  private String             calendarPath;

  private String             priority;

  private String             state;

  private String[]           orderBy;

  private String[]           participants;

  private Boolean            excludeRepeatEvent = false;

  private String             orderType          = Utils.ASCENDING;

  private String             queryType          = Query.XPATH;

  private long               limitedItems       = 0;

  public String getNodeType() {
    return nodeType;
  }

  public void setNodeType(String nt) {
    this.nodeType = nt;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public void setText(String fullTextSearch) {
    this.text = fullTextSearch;
  }

  public String getText() {
    return text;
  }

  public String[] getCategoryId() {
    return categoryIds;
  }

  public void setCategoryId(String[] categoryIds) {
    this.categoryIds = categoryIds;
  }

  public String[] getCalendarId() {
    return calendarIds;
  }

  public void setCalendarId(String[] calendarIds) {
    this.calendarIds = calendarIds;
  }

  public void setFilterCalendarIds(String[] filterCalendarIds) {
    this.filterCalendarIds = filterCalendarIds;
  }

  public String[] getFilterCalendarIds() {
    return filterCalendarIds;
  }

  public java.util.Calendar getFromDate() {
    return fromDate;
  }

  public void setFromDate(java.util.Calendar fromDate) {
    this.fromDate = fromDate;
  }

  public java.util.Calendar getToDate() {
    return toDate;
  }

  public void setToDate(java.util.Calendar toDate) {
    this.toDate = toDate;
  }

  public String getCalendarPath() {
    return calendarPath;
  }

  public void setCalendarPath(String calendarPath) {
    this.calendarPath = calendarPath;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getState() {
    return state;
  }

  public void setState(String st) {
    this.state = st;
  }

  public String[] getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String[] order) {
    this.orderBy = order;
  }

  public String[] getParticipants() {
    return participants;
  }

  public void setParticipants(String[] par) {
    this.participants = par;
  }

  public String getOrderType() {
    return orderType;
  }

  public void setOrderType(String type) {
    this.orderType = type;
  }

  public String getQueryStatement() throws Exception {
    StringBuilder queryString = null;
    if (queryType.equals(Query.SQL)) {
      if (!Utils.isEmpty(calendarPath))
        queryString = new StringBuilder(" select * from ").append(nodeType).append(" where jcr:path like '").append(calendarPath).append("/%'");
      else
        queryString = new StringBuilder(" select * from ").append(nodeType).append(" ");
      if (!Utils.isEmpty(text)) {
        String val = escapeLikeQuery(text);
        queryString.append(" and (").append(Utils.EXO_SUMMARY).append(" like '%").append(val).append("%'");
        queryString.append(" ESCAPE '\\'");
        queryString.append(" or ").append(Utils.EXO_DESCRIPTION).append(" like '%").append(val).append("%'");
        queryString.append(" ESCAPE '\\'");
        queryString.append(" or ").append(Utils.EXO_LOCATION).append(" like '%").append(val).append("%'");
        queryString.append(" ESCAPE '\\'");
        queryString.append(" or ").append(Utils.EXO_PARTICIPANT).append(" like '%").append(val).append("%'");
        queryString.append(" ESCAPE '\\'");
        queryString.append(" or ").append(Utils.EXO_INVITATION).append(" like '%").append(val).append("%'");
        queryString.append(" ESCAPE '\\'");
        // queryString.append(" and contains (.,'"+ text +"') ") ;
        queryString.append(")");
      }
      if (!Utils.isEmpty(eventType)) {
        queryString.append(" and ").append(Utils.EXO_EVENT_TYPE).append(" = '").append(eventType).append("'");
      }
      if (!Utils.isEmpty(priority)) {
        queryString.append(" and ").append(Utils.EXO_PRIORITY).append(" = '").append(priority).append("'");
      }
      if (!Utils.isEmpty(state)) {
        queryString.append(" and ").append(Utils.EXO_EVENT_STATE).append(" = '").append(state).append("'");
      }
      if (categoryIds != null && categoryIds.length > 0) {
        for (String category : categoryIds) {
          queryString.append(" and ").append(Utils.EXO_EVENT_CATEGORYID).append(" = '").append(category).append("'");
        }
      }
      if (calendarIds != null && calendarIds.length > 0) {
        for (String calendarId : calendarIds) {
          queryString.append(" and ").append(Utils.EXO_CALENDAR_ID).append(" = '").append(calendarId).append("'");
        }
      }
      return queryString.toString();
    } else {
      if (calendarPath != null)
        queryString = new StringBuilder("/jcr:root").append(calendarPath).append("//element(*,").append(nodeType).append(")");
      else
        queryString = new StringBuilder("/jcr:root//element(*,").append(nodeType).append(")");
      boolean hasConjuntion = false;
      StringBuilder stringBuffer = new StringBuilder("[");
      // desclared full text query
      if (text != null && text.length() > 0) {
        String val = escapeContainsQuery(text);
        stringBuffer.append("(jcr:contains(@").append(Utils.EXO_SUMMARY).append(", '").append(val).append("')")
              .append(" or jcr:contains(@").append(Utils.EXO_DESCRIPTION).append(", '").append(val).append("')")
              .append(" or jcr:contains(@").append(Utils.EXO_LOCATION).append(", '").append(val).append("')")
              .append(" or jcr:contains(@").append(Utils.EXO_PARTICIPANT).append(", '").append(val).append("')")
              .append(" or jcr:contains(@").append(Utils.EXO_INVITATION).append(", '").append(val).append("'))");
        hasConjuntion = true;
      }
      // desclared event type query
      if (eventType != null && eventType.length() > 0) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        stringBuffer.append("@exo:eventType='").append(eventType).append("'");
        stringBuffer.append(")");
        hasConjuntion = true;
      }
      // desclared priority query
      if (priority != null && priority.length() > 0) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        stringBuffer.append("@exo:priority='").append(priority).append("'");
        stringBuffer.append(")");
        hasConjuntion = true;
      }
      // desclared state query
      if (state != null && state.length() > 0) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        stringBuffer.append("@exo:eventState='").append(state).append("'");
        stringBuffer.append(")");
        hasConjuntion = true;
      }
      // desclared category query
      if (categoryIds != null && categoryIds.length > 0) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        for (int i = 0; i < categoryIds.length; i++) {
          if (i == 0)
            stringBuffer.append("@exo:eventCategoryId='").append(categoryIds[i]).append("'");
          else
            stringBuffer.append(" or @exo:eventCategoryId='").append(categoryIds[i]).append("'");
        }
        stringBuffer.append(")");
        hasConjuntion = true;
      }
      // desclared calendar query
      if (calendarIds != null && calendarIds.length > 0) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        for (int i = 0; i < calendarIds.length; i++) {
          if (i == 0)
            stringBuffer.append("@exo:calendarId='").append(calendarIds[i]).append("'");
          else
            stringBuffer.append(" or @exo:calendarId='").append(calendarIds[i]).append("'");
        }
        stringBuffer.append(")");
        hasConjuntion = true;
      }
      if (filterCalendarIds != null && filterCalendarIds.length > 0) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        for (int i = 0; i < filterCalendarIds.length; i++) {
          if (i == 0)
            stringBuffer.append("@exo:calendarId !='").append(filterCalendarIds[i]).append("'");
          else
            stringBuffer.append(" and @exo:calendarId !='").append(filterCalendarIds[i]).append("'");
        }
        stringBuffer.append(")");
        hasConjuntion = true;
      }
      // desclared participants query
      if (participants != null && participants.length > 0) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        for (int i = 0; i < participants.length; i++) {
          if (i == 0)
            stringBuffer.append("@exo:participant='").append(participants[i]).append("'");
          else
            stringBuffer.append(" or @exo:participant='").append(participants[i]).append("'");
        }
        stringBuffer.append(")");
        hasConjuntion = true;
      }

      // desclared Date time
      if (fromDate != null && toDate != null) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        stringBuffer.append("(");
        /*
         * stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ; stringBuffer.append(") or (") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')")
         * ; stringBuffer.append(") or (") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ; stringBuffer.append(") or (") ;
         * stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(toDate)+"') and ") ; stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ;
         */

        // case where the event span fully the interval (starts before and ends after)
        stringBuffer.append("@exo:fromDateTime <= xs:dateTime('").append(ISO8601.format(fromDate)).append("') and ");
        stringBuffer.append("@exo:toDateTime >= xs:dateTime('").append(ISO8601.format(toDate)).append("')");
        stringBuffer.append(") or (");

        // case where the event starts in the interval
        stringBuffer.append("@exo:fromDateTime >= xs:dateTime('").append(ISO8601.format(fromDate)).append("') and ");
        stringBuffer.append("@exo:fromDateTime <= xs:dateTime('").append(ISO8601.format(toDate)).append("')");
        stringBuffer.append(") or (");

        // case where the event ends in the interval
        stringBuffer.append("@exo:toDateTime >= xs:dateTime('").append(ISO8601.format(fromDate)).append("') and ");
        stringBuffer.append("@exo:toDateTime <= xs:dateTime('").append(ISO8601.format(toDate)).append("')");

        stringBuffer.append(")");
        stringBuffer.append(")");
        hasConjuntion = true;
      } else if (fromDate != null) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        // stringBuffer.append("(") ;
        // stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"')") ;
        // stringBuffer.append(") or (") ;
        // stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
        stringBuffer.append("@exo:fromDateTime >= xs:dateTime('").append(ISO8601.format(fromDate)).append("')");
        // stringBuffer.append(")") ;
        stringBuffer.append(")");
        hasConjuntion = true;
      } else if (toDate != null) {
        if (hasConjuntion)
          stringBuffer.append(" and (");
        else
          stringBuffer.append("(");
        // stringBuffer.append("(") ;
        // stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ;
        // stringBuffer.append(") or (") ;
        // stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(toDate)+"') and ") ;
        // stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ;
        // stringBuffer.append(")") ;
        stringBuffer.append("@exo:toDateTime <= xs:dateTime('").append(ISO8601.format(toDate)).append("')");
        stringBuffer.append(")");
        hasConjuntion = true;
      }

      if (excludeRepeatEvent != null && excludeRepeatEvent) {
        stringBuffer.append("and not(@jcr:mixinTypes='exo:repeatCalendarEvent' and @exo:repeat!='").append(CalendarEvent.RP_NOREPEAT).append("' and @exo:recurrenceId='')");
      }
      stringBuffer.append("]");
      // declared order by
      if (orderBy != null && orderBy.length > 0 && orderType != null && orderType.length() > 0) {
        for (int i = 0; i < orderBy.length; i++) {
          if (i == 0)
            stringBuffer.append(" order by @").append(orderBy[i].trim()).append(" ").append(orderType);
          else
            stringBuffer.append(", order by @").append(orderBy[i].trim()).append(" ").append(orderType);
        }
        hasConjuntion = true;
      }
      if (hasConjuntion)
        queryString.append(stringBuffer.toString());
      return queryString.toString();
    }
  }

  public void setQueryType(String queryType) {
    this.queryType = queryType;
  }

  public String getQueryType() {
    return queryType;
  }

  public void setLimitedItems(int limitedItems) {
    this.limitedItems = limitedItems;
  }

  public void setLimitedItems(long limitedItems) {
    this.limitedItems = limitedItems;
  }

  public long getLimitedItems() {
    return limitedItems;
  }

  public void setExcludeRepeatEvent(Boolean excludeRepeatEvent) {
    this.excludeRepeatEvent = excludeRepeatEvent;
  }

  public Boolean getExcludeRepeatEvent() {
    return excludeRepeatEvent;
  }
  
  protected String escapeContainsQuery(String s) {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
        char ch = s.charAt(i);
        if (ch == '"' || ch == '-' || ch == '\\') {
            buffer.append('\\').append(ch);
        }  else if (ch == '\'') {
          buffer.append("''");
        } else {
            buffer.append(ch);
        }
    }
    return buffer.toString();
  }

  protected String escapeLikeQuery(String s) {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
        char ch = s.charAt(i);
        if (ch == '%' || ch == '_' || ch == '\\') {
            buffer.append('\\').append(ch);
        } else if (ch == '\'') {
            buffer.append("''");
        } else {
            buffer.append(ch);
        }
    }
    return buffer.toString();
  }
}