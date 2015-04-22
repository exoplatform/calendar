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

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.ws.CalendarRestApi;
import org.exoplatform.calendar.ws.common.Resource;

public class CalendarResource extends Resource {

  private static final long serialVersionUID = -4500214043430048066L;

  private String name;
  private String description ;
  private String type;
  private String timeZone;
  private String color;
  private String owner;
  private String viewPermision;
  private String editPermission;
  private String[] groups;
  private String publicURL;
  private String privateURL;
  private String icsURL; 
  
  public CalendarResource() {
    super(null);
  }

  public CalendarResource(Calendar data, String basePath) {
    super(data.getId());
    
    StringBuilder calUri = new StringBuilder(basePath);
    calUri.append(CalendarRestApi.CALENDAR_URI).append(getId());    
    setHref(calUri.toString());
    icsURL = getHref() + CalendarRestApi.ICS_URI;
    
    name = data.getName();
    description = data.getDescription();
    type = String.valueOf(data.getCalType());
    timeZone = data.getTimeZone();
    color = data.getCalendarColor();
    owner = data.getCalendarOwner();
    
    StringBuilder sb = new StringBuilder();
    if(data.getViewPermission() != null)
    for (String s: data.getViewPermission()) {
      sb.append(s).append(Utils.SEMICOLON);
    }
    viewPermision = sb.toString();
    
    sb = new StringBuilder();
    if(data.getEditPermission() != null)
    for (String s: data.getEditPermission()) {
      sb.append(s).append(Utils.SEMICOLON);
    }
    editPermission = sb.toString();
    groups = data.getGroups();
    publicURL = data.getPublicUrl();
    privateURL = data.getPrivateUrl();    
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getViewPermision() {
    return viewPermision;
  }

  public void setViewPermision(String viewPermision) {
    this.viewPermision = viewPermision;
  }

  public String getEditPermission() {
    return editPermission;
  }

  public void setEditPermission(String editPermission) {
    this.editPermission = editPermission;
  }

  public String[] getGroups() {
    return groups;
  }

  public void setGroups(String[] groups) {
    this.groups = groups;
  }

  public String getPublicURL() {
    return publicURL;
  }

  public void setPublicURL(String publicURL) {
    this.publicURL = publicURL;
  }

  public String getPrivateURL() {
    return privateURL;
  }

  public void setPrivateURL(String privateURL) {
    this.privateURL = privateURL;
  }

  public String getIcsURL() {
    return icsURL;
  }

  public void setIcsURL(String icsURL) {
    this.icsURL = icsURL;
  }
}