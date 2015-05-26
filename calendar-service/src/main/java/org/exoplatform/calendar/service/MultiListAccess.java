/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
  
package org.exoplatform.calendar.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;

@SuppressWarnings("rawtypes")
public class MultiListAccess implements ListAccess {

  private boolean removeDuplicated;

  private List<ListAccess> list = new LinkedList<ListAccess>();

  public MultiListAccess() {
    this(false);
  }
  
  public MultiListAccess(boolean removeDuplicated) {
    this.removeDuplicated = removeDuplicated;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object[] load(int index, int length) throws IllegalArgumentException, Exception {
    Collection data;
    if (removeDuplicated) {
      data = new LinkedHashSet();
    } else {
      data = new LinkedList();
    }

    for (ListAccess item : list) {
      Object[] loaded = item.load(index, length);

      int loadedSize = data.size();
      if (loaded != null) {
        data.addAll(Arrays.asList(loaded));
        loadedSize = data.size() - loadedSize;        
      }

      if (length == -1 || loadedSize < length) {
        index = loaded.length > 0 ? 0 : index - item.getSize();
        length = length - loadedSize;
      } else {
        break;
      }
    }

    return data.toArray();
  }

  public void add(ListAccess item) {
    list.add(item);
  }

  public void clear() {
    list.clear();
  }

  @Override
  public int getSize() throws Exception {
    if (removeDuplicated) {
      return load(0, -1).length;
    } else {
      int size = 0;
      for (ListAccess item : list) {
        size += item.getSize();
      }
      return size;      
    }
  }

  public boolean isRemoveDuplicated() {
    return removeDuplicated;
  }
}