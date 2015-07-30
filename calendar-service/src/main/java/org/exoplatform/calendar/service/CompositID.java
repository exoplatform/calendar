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
  
package org.exoplatform.calendar.service;

public class CompositID {
  private String id;
  private CalendarType type;
  public static final String SEPARATOR = "-";
  protected String formater = "%s" + SEPARATOR + "%s";
  
  public CompositID(String id, CalendarType type) {
    this.id = id;
    this.type = type;
  }
  
  public String getId() {
    return id;
  }
  public CalendarType getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format(formater, id, type.getName());
  }
  
}
