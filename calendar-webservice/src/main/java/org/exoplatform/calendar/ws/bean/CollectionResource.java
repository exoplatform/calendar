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

import java.io.Serializable;
import java.util.Collection;

public class CollectionResource<T> implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 8125671009576049265L;
  private int offset = 0;
  private int limit = 0;
  private long fullSize = -1;
  
  private Collection<T> data ;

  public CollectionResource(Collection<T> data) {
    this.data = data;
  }

  public CollectionResource(Collection<T> data, long fullSize){
    this.data = data ;
    this.fullSize = fullSize;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public void setData(Collection<T> data) {
    this.data = data;
  }

  public Collection<T> getData() {
    return data;
  }

  public long getSize() {
    return fullSize;
  }

  public void setSize(long fullSize) {
    this.fullSize = fullSize;
  }
}
