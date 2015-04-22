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

import java.io.Serializable;
import java.util.Date;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jul 16, 2007  
 */
public class Reminder implements Serializable {

  private static final long serialVersionUID = -2265815245058343089L;

  final public static String   REPEAT         = "1".intern();

  final public static String   UNREPEAT       = "0".intern();

  final public static String   TYPE_EMAIL     = "email".intern();

  final public static String   TYPE_POPUP     = "popup".intern();

  final public static String   TYPE_BOTH      = "both".intern();

  final public static String[] REMINDER_TYPES = { TYPE_EMAIL, TYPE_POPUP };

  private String               id;

  private String               eventId;

  private String               reminderOwner;

  private String               reminderType   = TYPE_EMAIL;

  private long                 alarmBefore    = 0;

  private String               emailAddress;

  private Date                 fromDateTime;
  
  private boolean              isRepeat       = false;

  private long                 repeatInterval = 0;

  private String               summary;

  private String               description;
  
  public Reminder() {
    id = "Reminder" + IdGenerator.generate();
  }

  public Reminder(String type) {
    id = "Reminder" + IdGenerator.generate();
    reminderType = type;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEventId() {
    return this.eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public long getRepeatInterval() {
    return repeatInterval;
  }

  public void setRepeatInterval(long interval) {
    repeatInterval = interval;
  }

  public String getReminderType() {
    return reminderType;
  }

  public void setReminderType(String reminderType) {
    this.reminderType = reminderType;
  }

  public long getAlarmBefore() {
    return alarmBefore;
  }

  public void setAlarmBefore(long alarmBefore) {
    this.alarmBefore = alarmBefore;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public Date getFromDateTime() {
    return fromDateTime;
  }

  public void setFromDateTime(Date d) {
    fromDateTime = d;
  }

  public boolean isRepeat() {
    return isRepeat;
  }

  public void setRepeate(boolean b) {
    isRepeat = b;
  }

  public void setSummary(String sm) {
    this.summary = sm;
  }

  public String getSummary() {
    return summary;
  }

  public void setReminderOwner(String owner) {
    this.reminderOwner = owner;
  }

  public String getReminderOwner() {
    return reminderOwner;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
