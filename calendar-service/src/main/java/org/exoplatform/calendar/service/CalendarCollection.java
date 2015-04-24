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
package org.exoplatform.calendar.service;

import java.util.ArrayList;
import java.util.Collection;

public class CalendarCollection<T> extends ArrayList<T> { 
  private static final long serialVersionUID = -2525910004085477043L;
  private long fullSize;
  private long size;

  public long getFullSize() {
    return fullSize;
  }

  public void setFullSize(long fullSize) {
    this.fullSize = fullSize;
  }
  
  public CalendarCollection(Collection<T> list, long fullSize){
    addAll(list);
    this.size = list.size();
    this.fullSize = fullSize;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

}
