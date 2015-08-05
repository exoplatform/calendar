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

import org.exoplatform.calendar.model.Event;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 11, 2007
 * 
 * @deprecated This is only used in old Calendar Service API methods.
 */
@Deprecated
public class CalendarEvent extends Event {
  
  public CalendarEvent() {
    super();
  }

  public CalendarEvent(CalendarEvent evt) {
    super(evt);
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
    calEvent.setExcludeId(evt.getExcludeId());
    calEvent.setRepeatByDay(evt.getRepeatByDay());
    calEvent.setRepeatByMonthDay(evt.getRepeatByMonthDay());
    calEvent.setActivityId(evt.getActivityId());
    return calEvent;
  }
}
