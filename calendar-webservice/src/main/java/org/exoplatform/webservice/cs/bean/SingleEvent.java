/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.webservice.cs.bean;

/**
 * Created by The eXo Platform SAS
 * Author : Hoang Manh Dung
 *          dung.hoang@exoplatform.com
 * Jan 4, 2011  
 */
public class SingleEvent {

  private String summary         = "";

  private String description     = "";

  private String location        = "";

  private String eventState      = "";

  private String priority        = "";

  private long   startDateTime   = -1;

  private long   endDateTime     = -1;

  private long   startTimeOffset = 0;

  private long   endTimeOffset   = 0;

  private String dateFormat      = "";

  private boolean isVirtual = true;
  private boolean isEvent = true ;
  private boolean isOccurrence = false;


  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary != null ? summary : "";
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description != null ? description : "";
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location != null ? location : "";
  }

  public String getEventState() {
    return eventState;
  }

  public void setEventState(String eventState) {
    this.eventState = eventState != null ? eventState : "";
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority != null ? priority : "";
  }

  public long getStartDateTime() {
    return startDateTime;
  }

  public void setStartDateTime(long startDateTime) {
    this.startDateTime = startDateTime;
  }

  public long getEndDateTime() {
    return endDateTime;
  }

  public void setEndDateTime(long endDateTime) {
    this.endDateTime = endDateTime;
  }

  public long getStartTimeOffset() {
    return startTimeOffset;
  }

  public void setStartTimeOffset(long startTimeOffset) {
    this.startTimeOffset = startTimeOffset;
  }

  public long getEndTimeOffset() {
    return endTimeOffset;
  }

  public void setEndTimeOffset(long endTimeOffset) {
    this.endTimeOffset = endTimeOffset;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public boolean isVirtual() {
    return isVirtual;
  }

  public void setVirtual(boolean virtual) {
    isVirtual = virtual;
  }

  public boolean isEvent() {
    return isEvent;
  }

  public void setEvent(boolean event) {
    isEvent = event;
  }

  public boolean isOccurrence() {
    return isOccurrence;
  }

  public void setOccurrence(boolean occurrence) {
    isOccurrence = occurrence;
  }
}
