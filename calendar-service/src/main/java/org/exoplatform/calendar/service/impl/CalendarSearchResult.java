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

import java.util.Calendar;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.api.search.data.SearchResult;

import com.sun.org.apache.bcel.internal.generic.RETURN;

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
  private Calendar fromDateTime;
  private String dataType;

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
  public Calendar getFromDateTime() {
    return fromDateTime;
  }

  public void setFromDateTime(Calendar fromDateTime) {
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
  
  /**
   * @return icon base on task status if data is task 
   */
  public String getImageUrl(){
    if(CalendarEvent.TYPE_EVENT.equals(dataType)) return super.getImageUrl();
    else return Utils.TASK_ICON + super.getImageUrl();
  }

}
