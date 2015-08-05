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

import org.exoplatform.calendar.model.Query;

public class EventQueryCondition implements Query {

  private String             eventType = CalendarEvent.TYPE_EVENT;

  private String owner;
  
  private String             text;

  private String[]           categoryIds;

  private String[]           calendarIds;

  private CalendarType       calendarType;

  private String[]           filterCalendarIds;

  private Long fromDate;

  private Long toDate;

  private String             priority;

  private String             state;

  private String[]           participants;

  private Boolean            excludeRepeatEvent;

  private String[]           orderBy;

  private String             orderType = Utils.ASCENDING;

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String[] getCategoryIds() {
    return categoryIds;
  }

  public void setCategoryIds(String[] categoryIds) {
    this.categoryIds = categoryIds;
  }

  public String[] getCalendarIds() {
    return calendarIds;
  }

  public void setCalendarIds(String[] calendarIds) {
    this.calendarIds = calendarIds;
  }

  public CalendarType getCalendarType() {
    return calendarType;
  }

  public void setCalendarType(CalendarType calendarType) {
    this.calendarType = calendarType;
  }

  public String[] getFilterCalendarIds() {
    return filterCalendarIds;
  }

  public void setFilterCalendarIds(String[] filterCalendarIds) {
    this.filterCalendarIds = filterCalendarIds;
  }

  public Long getFromDate() {
    return fromDate;
  }

  public void setFromDate(Long fromDate) {
    this.fromDate = fromDate;
  }

  public Long getToDate() {
    return toDate;
  }

  public void setToDate(Long toDate) {
    this.toDate = toDate;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String[] getParticipants() {
    return participants;
  }

  public void setParticipants(String[] participants) {
    this.participants = participants;
  }

  public Boolean getExcludeRepeatEvent() {
    return excludeRepeatEvent;
  }

  public void setExcludeRepeatEvent(Boolean excludeRepeatEvent) {
    this.excludeRepeatEvent = excludeRepeatEvent;
  }

  public String[] getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String[] orderBy) {
    this.orderBy = orderBy;
  }

  public String getOrderType() {
    return orderType;
  }

  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  @Override
  public String getDS() {
    // TODO Auto-generated method stub
    return null;
  }
  
}
