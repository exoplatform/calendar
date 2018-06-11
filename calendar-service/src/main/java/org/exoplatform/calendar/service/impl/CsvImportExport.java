/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.calendar.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.ItemExistsException;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.util.Constants;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Apr 1, 2008  
 */
public class CsvImportExport implements CalendarImportExport {
  public static final String                    CSV_PATTERN         = "\"([^\"]+?)\",?|([^,]+),?|,";

  private static Pattern                        csvRE = Pattern.compile(CSV_PATTERN);

  public static String                          EV_SUMMARY          = "Subject".intern();

  public static String                          EV_STARTDATE        = "Start Date".intern();

  public static String                          EV_STARTTIME        = "Start Time".intern();

  public static String                          EV_ENDDATE          = "End Date".intern();

  public static String                          EV_ENDTIME          = "End Time".intern();

  public static String                          EV_ALLDAY           = "All day event".intern();

  public static String                          EV_HASREMINDER      = "Reminder on/off".intern();

  public static String                          EV_REMINDERDATE     = "Reminder Date".intern();

  public static String                          EV_REMINDERTIME     = "Reminder Time".intern();

  public static String                          EV_MEETINGORGANIZER = "Meeting Organizer".intern();

  public static String                          EV_ATTENDEES        = "Required Attendees".intern();

  public static String                          EV_INVITATION       = "Optional Attendees".intern();

  public static String                          EV_ATTACTMENT       = "Meeting Resources".intern();

  public static String                          EV_BILLINGINFO      = "Billing Information".intern();

  public static String                          EV_CATEGORIES       = "Categories".intern();

  public static String                          EV_DESCRIPTION      = "Description".intern();

  public static String                          EV_LOCATION         = "Location".intern();

  public static String                          EV_MILEAGE          = "Mileage".intern();

  public static String                          EV_PRIORITY         = "Priority".intern();

  public static String                          EV_PRIVATE          = "Private".intern();

  public static String                          EV_SENSITIVITY      = "Sensitivity".intern();

  public static String                          EV_STATUS           = "Show time as".intern();

  private static final String                   PRIVATE_TYPE        = "0".intern();

  private JCRDataStorage                        storage_;

  private static final Log                      logger              = ExoLogger.getLogger(CsvImportExport.class);
  
  private List<String> headers;
  
  /** Construct a regex-based CSV parser. */

  public CsvImportExport(JCRDataStorage dataStore) {
    storage_ = dataStore;
  }

  /** Process one file. Delegates to parse() a line at a time */
  public List<CalendarEvent> process(BufferedReader in) throws Exception {
    String line;
    // For each line...
    int lineCount = 0;
    List<CalendarEvent> eventList = new ArrayList<CalendarEvent>();

    while ((line = in.readLine()) != null) {
      String tempLine = line;
      if (!line.endsWith("\""))
        line = tempLine + in.readLine();
      if(lineCount == 0) { // the first line, parse the line to have the headers
        headers = parse(line);
      } else {
          CalendarEvent eventObj = getEventFromLine(line);
          if(eventObj != null) {
            eventList.add(eventObj);
          }  
      }
      lineCount++;
    }
    return eventList;
  }

  // get event from a line
  private CalendarEvent getEventFromLine(String line) {
    CalendarEvent eventObj = new CalendarEvent();
    
    eventObj.setEventType(CalendarEvent.TYPE_EVENT);
    eventObj.setCalType(PRIVATE_TYPE);
    
    List<String> values = parse(line);
    Date fromDate = getFromDate(values);
    Date toDate = getToDate(values);
    
    if(fromDate != null && toDate != null) {
      eventObj.setFromDateTime(getFromDate(values));
      eventObj.setToDateTime(getToDate(values));
      
      for(int i = 0; i < values.size(); i++) {
        String key = headers.get(i);
        if(EV_SUMMARY.equals(key)) {
          eventObj.setSummary(values.get(i));
        } else if(EV_LOCATION.equals(key)) {
          eventObj.setLocation(values.get(i));
        } else if(EV_DESCRIPTION.equals(key)) {
          eventObj.setDescription(values.get(i));
        } else if(EV_STATUS.equals(key)) {
          int st = Integer.valueOf(values.get(i));
          if (st == 0 || st == 1) {
            eventObj.setStatus(CalendarEvent.ST_AVAILABLE);
          } else if (st == 2) {
            eventObj.setStatus(CalendarEvent.ST_BUSY);
          } else if (st == 3) {
            eventObj.setStatus(CalendarEvent.ST_OUTSIDE);
          }
        } else if(EV_PRIORITY.equals(key)) {
          for (int j = 0; j < CalendarEvent.PRIORITY.length; j++) {
            if (CalendarEvent.PRIORITY[i].equalsIgnoreCase(values.get(i).toLowerCase())) {
              eventObj.setPriority(String.valueOf(j));
              break;
            }
          }
        } else if(EV_CATEGORIES.equals(key)) {
          eventObj.setEventCategoryName(values.get(i));
        } else if(EV_ATTENDEES.equals(key)) {
          if(values.get(i) != null) {
            eventObj.setParticipant(values.get(i).split(";"));
          }
        } else if(EV_INVITATION.equals(key)) {
          if(values.get(i) != null) {
            eventObj.setInvitation(values.get(i).split(";"));
          }
        }
      }
      return eventObj;
    } else {
      return null;
    }
  }
  /** Parse one line.
   * @return List of Strings, minus their double quotes
   */
  public List<String> parse(String line) {
    List<String> list = new ArrayList<String>();
    Matcher m = csvRE.matcher(line);
    // For each field
    while (m.find()) {
      String match = m.group();
      if (match == null )
        break;
      if(match.equals(",")) {
        match = null;
      } else {
        if (match.endsWith(",")) { 
          match = match.substring(0, match.length() - 1);
        }
        if (match.startsWith("\"")) {
          match = match.substring(1);
        }
        if(match.endsWith("\"")) {
          match = match.substring(0, match.length() - 1);
        }
      }
      list.add(match);
    }
    return list;
  }

  public OutputStream exportCalendar(String username, List<String> calendarIds, String type) throws Exception {
    return null;
  }

  public List<CalendarEvent> getEventObjects(InputStream inputStream) throws Exception {
    return null;
  }

  public void importCalendar(String username, InputStream inputStream, String calendarId, String calendarName, java.util.Calendar from, java.util.Calendar to, boolean isNew) throws Exception {
    List<CalendarEvent> data = process(new BufferedReader(new InputStreamReader(inputStream)));
    if (data == null || data.size() < 1)
      return;

    CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);

    if (isNew) {
      org.exoplatform.calendar.service.Calendar exoCalendar = new org.exoplatform.calendar.service.Calendar();
      exoCalendar.setName(calendarName);
      exoCalendar.setCalendarColor(Constants.COLORS[0]);
      exoCalendar.setDescription(Utils.EMPTY_STR);
      exoCalendar.setPublic(true);
      exoCalendar.setCalendarOwner(username);
      storage_.saveUserCalendar(username, exoCalendar, true);
      calendarId = exoCalendar.getId();
    }

    for (CalendarEvent exoEvent : data) {
      if (!Utils.isEmpty(exoEvent.getEventCategoryName())) {
        EventCategory evCate = new EventCategory();
        evCate.setName(exoEvent.getEventCategoryName());
        try {
          calService.saveEventCategory(username, evCate, true);
        } catch (ItemExistsException e) {
          evCate = calService.getEventCategoryByName(username, evCate.getName());
        } catch (Exception e) {
          if (logger.isDebugEnabled()) {
            logger.debug("Exception occurs when saving new event category '" + evCate.getName() + "' for event: " + exoEvent.getId(), e);
          }
        }
        exoEvent.setEventCategoryId(evCate.getId());
        exoEvent.setEventCategoryName(evCate.getName());
      }
      exoEvent.setCalendarId(calendarId);
      storage_.saveUserEvent(username, calendarId, exoEvent, true);
    }
  }

  public boolean isValidate(InputStream icalInputStream) throws Exception {
    try {
      List<CalendarEvent> eventObjs = process(new BufferedReader(new InputStreamReader(icalInputStream)));
      return eventObjs.size() > 0;
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("The inputStream is not valid", e);
      }
      return false;
    }
  }

  public OutputStream exportCalendar(String username, List<String> calendarIds, String type, int number) throws Exception {
    return null;
  }

  public OutputStream exportEventCalendar(String username, String calendarId, String type, String eventId) throws Exception {
    return null;
    // not implemented yet, export for CSV file

  }

  @Override
  public ByteArrayOutputStream exportEventCalendar(CalendarEvent event) throws Exception {
    return null;
  }
  
  /*
   * gets from date from values returned by method parse() above
   * if the header does not have info about start time -> the event will be processed as an all day event
   */
  private Date getFromDate(List<String> values) {
    DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss a", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance();
    Date date = null;
    try {
      String dateStr = getValue(values,EV_STARTDATE);
      String allDay = getValue(values, EV_ALLDAY);
      if("True".equals(allDay)) {
        date = df.parse(dateStr + "0:00:00 AM"); 
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
      }
      date = df.parse(getValue(values,EV_STARTDATE) + " " + getValue(values, EV_STARTTIME));
      return date;
    } catch (ParseException e) {
      if(logger.isDebugEnabled()) {
        logger.debug("can't parse the date",e);
      }
      return null;
    }
  }
  /*
   * gets to date of event from values returned by method parse() above
   */
  private Date getToDate(List<String> values) {
    DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss a", Locale.ENGLISH);
    Calendar cal = Calendar.getInstance();
    Date date;
    try {
      String dateStr = getValue(values, EV_ENDDATE);
      String allDay = getValue(values, EV_ALLDAY);
      
      if(dateStr == null) {
        return getFromDate(values);
      }
      if("True".equals(allDay)) {
        date = df.parse(dateStr + "23:59:59 PM");
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
      }
      date = df.parse(getValue(values,EV_ENDDATE) + " " + getValue(values,EV_ENDTIME));
      return date;
    } catch (ParseException e) {
      if(logger.isDebugEnabled()) {
        logger.debug("can't parse the date",e);
      }
      return null;
    }
  }
  /*
   * gets value of a field from list of values
   * the index of the value in the list is equals index of the field in the header
   */
  private String getValue(List<String> values, String field) {
    int i = headers.indexOf(field);
    if(i > -1) {
      return values.get(i);
    } 
    return null;
  }
}
