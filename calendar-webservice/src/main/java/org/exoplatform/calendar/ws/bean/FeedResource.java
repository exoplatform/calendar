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

package org.exoplatform.calendar.ws.bean;

import static org.exoplatform.calendar.ws.CalendarRestApi.CALENDAR_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.CAL_BASE_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.FEED_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.RSS_URI;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.ws.common.Resource;

public class FeedResource extends Resource {
  private static final long serialVersionUID = 7911451293360539750L;

  private String            name;

  private String                    rss;

  private Collection<Serializable>                  calendars;

  /**
   * This field is introduced for user can update calendars in Feed
   * It's because calendars field must readonly,
   * it's a workaround for the restriction of rest framework (it can not unmarshall from JSON string into generic field)
   */
  private String[] calendarIds = null;

  public FeedResource() {
    super(null);
  }
  
  public FeedResource(FeedData data, String[] calendarids, String basePath) {
    super(data.getFeed());

    setHref(new StringBuilder(basePath).append(FEED_URI).append(data.getTitle()).toString());
    name = data.getTitle();
    rss = new StringBuilder(CAL_BASE_URI).append(FEED_URI)
                                        .append(data.getTitle())
                                        .append(RSS_URI)
                                        .toString();
    calendars = new LinkedList<Serializable>();
    for (String id : calendarids) {
      calendars.add(new StringBuilder(basePath).append(CALENDAR_URI).append(id).toString());      
    }
    this.calendarIds = calendarids;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRss() {
    return rss;
  }

  public void setRss(String rss) {
    this.rss = rss;
  }

  public Collection<Serializable> getCalendars() {
    return calendars;
  }

  /**
   * Because rest framework can not unmarshall from JSON if object contain generic field
   * So, we must make #calendars field is readonly with rest-framework by rename setter method to #setCals
   * And we introduce field #calendarIds, that enable user can update calendars in Feed
   * @param calendars
   * @return this
   */
  public FeedResource setCals(Collection<Serializable> calendars) {
    this.calendars = calendars;
    return this;
  }

  public String[] getCalendarIds() {
    return calendarIds;
  }

  public void setCalendarIds(String[] calendarIds) {
    this.calendarIds = calendarIds;
  }
}
