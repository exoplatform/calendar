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

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AbstractBean implements Serializable {

  private String id;

  private String ds;

  private long lastModified;

  public AbstractBean(String compositeId) {
    if (compositeId == null) {
      throw new IllegalArgumentException();
    }
    String[] split = compositeId.split("::");

    if (split.length > 1) {
      ds = split[0];
      id = split[1];
    } else {
      id = split[0];
    }
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDS(String dsName) {
    this.ds = dsName;
  }

  public String getDS() {
    return ds;
  }

  public String getCompositeId() {
    if (ds != null && !ds.isEmpty()) {
      return ds + "::" + id;
    }
    return id;
  }

  public long getLastModified() {    
    return lastModified;
  }

  public void setLastModified(long lastModified) {
    lastModified = (lastModified / 1000) * 1000;
    this.lastModified = lastModified;
  }
}
