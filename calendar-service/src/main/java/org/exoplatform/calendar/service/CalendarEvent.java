/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 11, 2007
 * 
 * @deprecated This is only used in old Calendar Service API methods.
 */
@Deprecated
public class CalendarEvent extends Event {
  
  private String               calType         = "0";

  public static final CalendarEvent NULL_OBJECT = new CalendarEvent();


  public CalendarEvent() {
    super();
  }

  public CalendarEvent(CalendarEvent evt) {
    super(evt);
    this.calType = evt.calType;
  }

  public void setCalType(String calType) {
    this.calType = calType;
  }

  public String getCalType() {
    return calType;
  }

  /**
   * use getLastModified instead
   */
  @Deprecated
  public Date getLastUpdatedTime() {
    return new Date(getLastModified());    
  }

  /**
   * use setLastModified instead
   */
  @Deprecated
  public void setLastUpdatedTime(Date lastUpdatedTime) {
    long last = 0;
    if (lastUpdatedTime != null) {
      last = lastUpdatedTime.getTime();      
    }
    setLastModified(last);
  }

  /**
   * @deprecated
   * @see #setExceptionIds(Collection)
   * @param excludeId the excludeId to set
   */
  public void setExcludeId(String[] excludeId) {
    if (excludeId != null) {
      setExclusions(Arrays.asList(excludeId));
    } else {
      setExclusions(null);
    }
  }

  /**
   * @deprecated
   * @see #getExceptionIds()
   * @return the excludeId
   */
  public String[] getExcludeId() {
    Collection<String> collection = getExclusions();
    if (collection != null) {
      return collection.toArray(new String[collection.size()]);
    }
    return null;
  }

  /**
   * This method will set the collection of excluded event's id to the collection
   * @param ids a collection of id with string type
   */
  public void setExceptionIds(Collection<String> ids){
    setExclusions(ids);
  }

  /**
   * This method will return all id excluded event id
   * @return collection of excluded event's id
   */
  public Collection<String> getExceptionIds(){
    return getExclusions();
  }
  
  /**
   * This method will add more excluded id to existed collection 
   * @param id a single id want to add to exited excluded collection
   */
  public void addExceptionId(String id){
    addExclusion(id);
  }

  public void removeExceptionId(String id){
    removeExclusion(id);
  }

  public static CalendarEvent build(Event evt) {
    CalendarEvent calEvent = new CalendarEvent();

    calEvent.setId(evt.getId());
    calEvent.setCalendarId(evt.getCalendarId());
    calEvent.setSummary(evt.getSummary());
    calEvent.setEventCategoryId(evt.getEventCategoryId());
    calEvent.setEventCategoryName(evt.getEventCategoryName());
    calEvent.setLocation(evt.getLocation());
    calEvent.setTaskDelegator(evt.getTaskDelegator());
    calEvent.setRepeatType(evt.getRepeatType());
    calEvent.setDescription(evt.getDescription());
    calEvent.setFromDateTime(evt.getFromDateTime());
    calEvent.setToDateTime(evt.getToDateTime());
    calEvent.setEventType(evt.getEventType());
    calEvent.setPriority(evt.getPriority());
    calEvent.setPrivate(evt.isPrivate());
    calEvent.setEventState(evt.getEventState());
    calEvent.setSendOption(evt.getSendOption());
    calEvent.setMessage(evt.getMessage());
    calEvent.setLastModified(evt.getLastModified());
    calEvent.setInvitation(evt.getInvitation());
    calEvent.setParticipant(evt.getParticipant());
    calEvent.setParticipantStatus(evt.getParticipantStatus());
    calEvent.setReminders(evt.getReminders());
    calEvent.setAttachment(evt.getAttachment());

    calEvent.setRecurrenceId(evt.getRecurrenceId());
    calEvent.setIsExceptionOccurrence(evt.getIsExceptionOccurrence());
    calEvent.setRepeatUntilDate(evt.getRepeatUntilDate());
    calEvent.setRepeatCount(evt.getRepeatCount());
    calEvent.setOriginalReference(evt.getOriginalReference());
    calEvent.setRepeatInterval(evt.getRepeatInterval());
    calEvent.setExceptionIds(evt.getExclusions());
    calEvent.setRepeatByDay(evt.getRepeatByDay());
    calEvent.setRepeatByMonthDay(evt.getRepeatByMonthDay());
    calEvent.setActivityId(evt.getActivityId());
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (container != null) {
      String username = ConversationState.getCurrent().getIdentity().getUserId();
      CalendarService service = container.getComponentInstanceOfType(CalendarService.class);
      try {
        calEvent.setCalType(String.valueOf(service.getTypeOfCalendar(username, calEvent.getCalendarId())));
      } catch (Exception e) {        
      }      
    }
    return calEvent;
  }
}