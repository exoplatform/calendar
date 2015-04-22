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

import static org.exoplatform.calendar.ws.CalendarRestApi.CALENDAR_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.CATEGORY_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.TASK_URI;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.ws.CalendarRestApi;
import org.exoplatform.calendar.ws.common.Resource;
import org.exoplatform.commons.utils.ISO8601;

public class TaskResource extends Resource {
  private static final long serialVersionUID = -5290204215375549320L;

  private String name;
  private String note;
  private String from;
  private String to;
  private Serializable calendar;
  private Serializable[] categories;
  private String[] delegation;
  private String priority;
  private Reminder[] reminder;
  private Serializable[] attachments;
  private String status;  

  public TaskResource() {
    super(null);
  }
  
  public TaskResource(CalendarEvent data, String basePath) throws Exception {
   super(data.getId());
   setHref(new StringBuilder(basePath).append(TASK_URI).append(data.getId()).toString());
   name = data.getSummary();
   note = data.getDescription();
   
   Calendar fromCal = Utils.getInstanceTempCalendar();
   fromCal.setTime(data.getFromDateTime());
   from = ISO8601.format(fromCal);
   
   Calendar toCal = Utils.getInstanceTempCalendar();
   toCal.setTime(data.getFromDateTime());
   to = ISO8601.format(toCal);
   
   calendar = new StringBuilder(basePath).append(CALENDAR_URI).append(data.getCalendarId()).toString();
   categories = new String[]{new StringBuilder(basePath).append(CATEGORY_URI).append(data.getEventCategoryId()).toString()};
   if(data.getTaskDelegator() != null) delegation = data.getTaskDelegator().split(Utils.COLON);   
   this.priority = data.getPriority(); 
   if(data.getReminders() != null) reminder = data.getReminders().toArray(new Reminder[]{});
   if(data.getAttachment() != null) {
     List<String> atts = new LinkedList<String>();
     
     for (Attachment att : data.getAttachment()) {
       atts.add(new StringBuilder(basePath).append(CalendarRestApi.ATTACHMENT_URI)
                .append(URLEncoder.encode(att.getDataPath(), "ISO-8859-1"))
                .toString());
     }
     attachments = atts.toArray(new String[atts.size()]);
   }
   status = data.getStatus();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public Serializable getCalendar() {
    return calendar;
  }

  public TaskResource setCal(Serializable calendar) {
    this.calendar = calendar;
    return this;
  }

  public Serializable[] getCategories() {
    return categories;
  }

  public void setCats(Serializable[] categories) {
    this.categories = categories;
  }

  public String[] getDelegation() {
    return delegation;
  }

  public void setDelegation(String[] delegation) {
    this.delegation = delegation;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public Reminder[] getReminder() {
    return reminder;
  }

  public void setReminder(Reminder[] reminder) {
    this.reminder = reminder;
  }

  public Serializable[] getAttachments() {
    return attachments;
  }

  public void setAtts(Serializable[] attachments) {
    this.attachments = attachments;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}