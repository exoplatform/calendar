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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 16, 2007  
 */
public class CalendarSetting {
  // view types
  public static String     DAY_VIEW              = "0";

  public static String     WEEK_VIEW             = "1";

  public static String     MONTH_VIEW            = "2";

  public static String     YEAR_VIEW             = "3";

  public static String     LIST_VIEW             = "4";

  public static String     SCHEDULE_VIEW         = "5";

  public static String     WORKING_VIEW          = "6";

  /**
   * default value for one moving of event, task on UI. used when drag and drop. 
   */
  public final static long DEFAULT_TIME_INTERVAL = 30;

  // time weekStartOn types
  public static String     SUNDAY                = "1";

  public static String     MONDAY                = "2";

  public static String     TUESDAY               = "3";

  public static String     WENDNESDAY            = "4";

  public static String     THURSDAY              = "5";

  public static String     FRIDAY                = "6";

  public static String     SATURDAY              = "7";

  public static String     ACTION_ALWAYS         = "always";

  public static String     ACTION_NEVER          = "never";

  public static String     ACTION_ASK            = "ask";

  public static String     ACTION_BYSETTING      = "setting";

  private String           viewType;

  private long             timeInterval;

  private String           weekStartOn;

  private String           dateFormat;

  private String           timeFormat;

  private String           location;

  private String           timeZone;

  private String           baseURL;

  private boolean          isShowWorkingTime     = true;

  private String           workingTimeBegin;

  private String           workingTimeEnd;

  private String[]         sharedCalendarsColors;

  private String[]         filterPrivateCalendars;

  private String[]         filterPublicCalendars;

  private String[]         filterSharedCalendars;

  private String           sendOption;

  public CalendarSetting() {
    viewType = WORKING_VIEW;
    timeInterval = DEFAULT_TIME_INTERVAL;
    weekStartOn = String.valueOf(Calendar.SUNDAY);
    dateFormat = "MM/dd/yyyy";
    timeFormat = "hh:mm a";
    isShowWorkingTime = true;
    timeZone = TimeZone.getDefault().getID();
    location = Locale.getDefault().getISO3Country();
    filterPrivateCalendars = new String[] {};
    filterPublicCalendars = new String[] {};
    filterSharedCalendars = new String[] {};
    sharedCalendarsColors = new String[] {};
    sendOption = ACTION_ASK;
  }

  public void setViewType(String viewType) {
    this.viewType = viewType;
  }

  public String getViewType() {
    return viewType;
  }

  public void setTimeInterval(long timeInterval) {
    this.timeInterval = timeInterval;
  }

  public long getTimeInterval() {
    return timeInterval;
  }

  public void setWeekStartOn(String weekStartOn) {
    this.weekStartOn = weekStartOn;
  }

  public String getWeekStartOn() {
    return weekStartOn;
  }

  public void setDateFormat(String dFormat) {
    dateFormat = dFormat;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public void setTimeFormat(String timeFormat) {
    this.timeFormat = timeFormat;
  }

  public String getTimeFormat() {
    return timeFormat;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getLocation() {
    return location;
  }

  public void setBaseURL(String url) {
    this.baseURL = url;
  }

  public String getBaseURL() {
    return baseURL;
  }

  public void setFilterPrivateCalendars(String[] defaultCalendars) {
    this.filterPrivateCalendars = defaultCalendars;
  }

  public String[] getFilterPrivateCalendars() {
    return filterPrivateCalendars;
  }

  public void setFilterPublicCalendars(String[] defaultCalendars) {
    this.filterPublicCalendars = defaultCalendars;
  }

  public String[] getFilterPublicCalendars() {
    return filterPublicCalendars;
  }

  public void setShowWorkingTime(boolean isShowWorkingTime) {
    this.isShowWorkingTime = isShowWorkingTime;
  }

  public boolean isShowWorkingTime() {
    return isShowWorkingTime;
  }

  public void setWorkingTimeBegin(String workingTimeBegin) {
    this.workingTimeBegin = workingTimeBegin;
  }

  public String getWorkingTimeBegin() {
    return workingTimeBegin;
  }

  public void setWorkingTimeEnd(String workingTimeEnd) {
    this.workingTimeEnd = workingTimeEnd;
  }

  public String getWorkingTimeEnd() {
    return workingTimeEnd;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setSharedCalendarsColors(String[] sharedCalendarColor) {
    sharedCalendarsColors = sharedCalendarColor;
  }

  public String[] getSharedCalendarsColors() {
    return sharedCalendarsColors;
  }

  public void setFilterSharedCalendars(String[] sharedCalendars) {
    filterSharedCalendars = sharedCalendars;
  }

  public String[] getFilterSharedCalendars() {
    return filterSharedCalendars;
  }

  public void setSendOption(String option) {
    sendOption = option;
  }

  public String getSendOption() {
    return sendOption;
  }
  
  /**
   * Create Calendar object which has the user preference (timezone, firstdayofweek, ...)
   * @param time time in long  
   * @return calendar object
   */
  public Calendar createCalendar(long time) {
    Calendar c = GregorianCalendar.getInstance(TimeZone.getTimeZone(timeZone));
    c.setFirstDayOfWeek(Integer.parseInt(weekStartOn));
    c.setTimeInMillis(time);
    // fix CS-4725
    c.setMinimalDaysInFirstWeek(4);
    return c;
  }
  
  /**
   * Create Calendar object which has the user preference (timezone, firstdayofweek, ...)
   * @param time  
   * @return calendar object
   */
  public Calendar createCalendar(Date time) {
    return time != null ? createCalendar(time.getTime()) : null;
  }
}
