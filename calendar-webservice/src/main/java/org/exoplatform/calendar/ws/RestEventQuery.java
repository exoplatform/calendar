/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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

package org.exoplatform.calendar.ws;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ISO8601;

public class RestEventQuery extends EventQuery {

  @Override
  public String getQueryStatement() throws Exception {
    // events from user, groups, public calendars, and events that contains
    // specific participant
    StringBuilder sql = new StringBuilder("SELECT * FROM ");
    sql.append(Utils.EXO_CALENDAR_EVENT);
    sql.append(" WHERE");

    if (getCalendarId() != null || getParticipants() != null) {
      sql.append(" AND (");
      // calendarIds: public and groups, shared calendars
      if (getCalendarId() != null) {
        for (String calId : getCalendarId()) {
          sql.append(" OR ").append(Utils.EXO_CALENDAR_ID).append(" = '").append(calId).append("'");
        }
      }
      // participant
      if (getParticipants() != null) {
        for (String participant : getParticipants()) {
          //workaround for case calendarRestApi.findEventsByCalendar
          if (getCalendarPath() != null) {
            sql.append(" AND ");
          } else {
            sql.append(" OR ");
          }
          sql.append(Utils.EXO_PARTICIPANT)
             .append(" = '")
             .append(participant)
             .append("'");
        }
      }
      sql.append(")");
    }

    // date time
    if (getFromDate() != null) {
      sql.append(" AND (")
         .append(Utils.EXO_FROM_DATE_TIME)
         .append(" >= TIMESTAMP '")
         .append(ISO8601.format(getFromDate()))
         .append("')");
    }
    if (getToDate() != null) {
      sql.append(" AND (")
         .append(Utils.EXO_TO_DATE_TIME)
         .append(" <= TIMESTAMP '")
         .append(ISO8601.format(getToDate()))
         .append("')");
    }
    // category
    String[] categoryIds = getCategoryId();
    if (categoryIds != null && categoryIds.length > 0) {
      sql.append(" AND (");
      for (int i = 0; i < categoryIds.length; i++) {
        sql.append(Utils.EXO_EVENT_CATEGORYID);
        sql.append(" = '").append(categoryIds[i]).append("'");
        if (i < categoryIds.length - 1) {
          sql.append(" OR ");
        }
      }
      sql.append(")");
    }
    // event or task
    if (!Utils.isEmpty(getEventType())) {
      sql.append(" AND ").append(Utils.EXO_EVENT_TYPE).append("='");
      sql.append(getEventType()).append("'");
    }

    int i = sql.indexOf("WHERE AND");
    if (i != -1) {
      sql.replace(i, i + 9, "WHERE");
    }
    if ((i = sql.indexOf("( OR")) != -1) {
      sql.replace(i, i + 4, "(");
    }
    
    String[] orderBy = getOrderBy();
    String orderType = " " + getOrderType();
    if (orderBy != null && orderBy.length > 0) {
      sql.append(" ORDER BY ");
      
      for (int j = 0; j < orderBy.length; j++) {
        orderBy[j] = orderBy[j] + orderType;
      }
      sql.append(StringUtils.join(orderBy, ","));
    }
    return sql.toString();
  }
}
