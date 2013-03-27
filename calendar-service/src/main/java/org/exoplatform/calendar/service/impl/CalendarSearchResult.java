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

import org.exoplatform.commons.api.search.data.SearchResult;

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
