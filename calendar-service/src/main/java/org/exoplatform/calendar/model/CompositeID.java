/**
 * Copyright (C) 2015 eXo Platform SAS.
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
  
package org.exoplatform.calendar.model;

public class CompositeID {
  private String id;
  private String ds;
  
  public CompositeID(String id) {
    this(id, null);
  }
  
  public CompositeID(String id, String ds) {
    this.id = id;
    this.ds = ds;
  }

  public String getId() {
    return id;
  }

  public String getDS() {
    return ds;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CompositeID) {
      CompositeID comId = (CompositeID)obj;
      if (id.equals(comId.getId())) {
        if (ds == null) {
          if (comId.getDS() == null) {
            return true;
          }
        } else {
          return ds.equals(comId.getDS());
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return ds + "::" + id;
  }

  public static CompositeID parse(String compositeId) {
    if (compositeId == null) {
      throw new IllegalArgumentException();
    }
    String[] split = compositeId.split("::");

    if (split.length > 1) {
      return new CompositeID(split[1], split[0]);
    } else {
      return new CompositeID(split[0]);
    }
  }
}
