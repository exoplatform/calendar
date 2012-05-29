/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.calendar.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 * tu.duy@exoplatform.com
 * Mar 9, 2011
 */

public class RemoteCalendar {
  private String   type;

  private String   username;

  private String   calendarId;

  private String   remoteUrl;

  private String   calendarName;

  private String   description;

  private String   syncPeriod;

  private String   beforeDateSave = "";

  private String   afterDateSave  = "";

  private long     beforeDate     = 0;

  private long     afterDate      = 0;

  private String   remoteUser;

  private String   remotePassword;

  private Calendar lastUpdated;

  public RemoteCalendar() {
    description = syncPeriod = remoteUser = remotePassword = "";
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCalendarId() {
    return calendarId;
  }

  public void setCalendarId(String calendarId) {
    this.calendarId = calendarId;
  }

  public String getRemoteUrl() {
    return remoteUrl;
  }

  public void setRemoteUrl(String remoteUrl) {
    this.remoteUrl = remoteUrl;
  }

  public String getCalendarName() {
    return calendarName;
  }

  public void setCalendarName(String calendarName) {
    this.calendarName = calendarName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSyncPeriod() {
    return syncPeriod;
  }

  public void setSyncPeriod(String syncPeriod) {
    this.syncPeriod = syncPeriod;
  }

  public String getBeforeDateSave() {
    return beforeDateSave;
  }

  public void setBeforeDateSave(String beforeDateSave) {
    setBeforeDate(getTimeField(beforeDateSave, true));
    this.beforeDateSave = beforeDateSave;
  }

  public String getAfterDateSave() {
    return afterDateSave;
  }

  public void setAfterDateSave(String afterDateSave) {
    setAfterDate(getTimeField(afterDateSave, false));
    this.afterDateSave = afterDateSave;
  }

  public long getBeforeDate() {
    return beforeDate;
  }

  public void setBeforeDate(long beforeDate) {
    this.beforeDate = beforeDate;
  }

  public long getAfterDate() {
    return afterDate;
  }

  public void setAfterDate(long afterDate) {
    this.afterDate = afterDate;
  }

  public String getRemoteUser() {
    return remoteUser;
  }

  public void setRemoteUser(String remoteUser) {
    this.remoteUser = remoteUser;
  }

  public String getRemotePassword() {
    return remotePassword;
  }

  public void setRemotePassword(String remotePassword) {
    this.remotePassword = remotePassword;
  }

  public Calendar getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Calendar lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public Calendar getBeforeTime() {
    return calculateTime(beforeDate, true);
  }

  public Calendar getAfterTime() {
    return calculateTime(afterDate, false);
  }

  private Calendar calculateTime(long time, boolean isBefore) {
    Calendar cal = Calendar.getInstance();
    if (time != 0) {
      cal.setTimeInMillis(cal.getTimeInMillis() + time);
    } else {
      cal.add(java.util.Calendar.YEAR, (isBefore) ? -1 : 1);
    }
    return cal;
  }

  private long getTimeField(String vls, boolean isBefore) {
    try {
      int vl = Integer.parseInt(vls.substring(0, 1));
      String unit = vls.substring(1, 2);
      java.util.Calendar calendar = GregorianCalendar.getInstance();
      java.util.Calendar calendar2 = GregorianCalendar.getInstance();
      long l = (unit.equals("d")) ? vl * 86400000 : (unit.equals("w")) ? vl * 86400000 * 7 : 0;
      if (l == 0) {
        if (unit.equals("m"))
          calendar.set(calendar.get(java.util.Calendar.YEAR), (calendar.get(java.util.Calendar.MONTH) + ((isBefore) ? ((-1) * vl) : vl)), calendar.get(java.util.Calendar.DATE));
        else if (unit.equals("y")) {
          calendar.set((calendar.get(java.util.Calendar.YEAR) + ((isBefore) ? ((-1) * vl) : vl)), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DATE));
        } else {
          return 0;
        }
      } else {
        return ((isBefore) ? ((-1) * l) : l);
      }
      return calendar.getTimeInMillis() - calendar2.getTimeInMillis();
    } catch (Exception e) {
      return 0;
    }
  }

}
