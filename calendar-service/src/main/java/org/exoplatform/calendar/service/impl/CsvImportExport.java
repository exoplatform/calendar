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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.Utils;
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

  private static Pattern                        csvRE;

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

  private static LinkedHashMap<String, Integer> dataMap             = new LinkedHashMap<String, Integer>();

  private static String[]                       keys                = new String[] { EV_SUMMARY, EV_STARTDATE, EV_STARTTIME, EV_ENDDATE, EV_ENDTIME, EV_ALLDAY, EV_HASREMINDER, EV_REMINDERDATE, EV_REMINDERTIME, EV_MEETINGORGANIZER, EV_ATTENDEES, EV_INVITATION, EV_ATTACTMENT, EV_BILLINGINFO, EV_CATEGORIES, EV_DESCRIPTION, EV_LOCATION, EV_MILEAGE, EV_PRIORITY, EV_PRIVATE, EV_SENSITIVITY, EV_STATUS };

  private static final String                   PRIVATE_TYPE        = "0".intern();

  private JCRDataStorage                        storage_;

  private static final Log                      logger              = ExoLogger.getLogger(CsvImportExport.class);

  /** Construct a regex-based CSV parser. */

  public CsvImportExport(JCRDataStorage dataStore) {
    csvRE = Pattern.compile(CSV_PATTERN);
    storage_ = dataStore;
    int count = 0;
    for (String k : keys) {
      dataMap.put(k, count);
      count++;
    }
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
      if (lineCount > 0) {
        List<String> l = parse(line);
        if (!Utils.isEmpty(l.get(dataMap.get(EV_SUMMARY)))) {
          boolean isValid = true;
          CalendarEvent eventObj = new CalendarEvent();
          eventObj.setEventType(CalendarEvent.TYPE_EVENT);
          eventObj.setCalType(PRIVATE_TYPE);
          DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH);
          // Event Summnary
          if (!Utils.isEmpty(l.get(dataMap.get(EV_SUMMARY))))
            eventObj.setSummary(l.get(dataMap.get(EV_SUMMARY)));
          // Event fromdate
          if (!Utils.isEmpty(l.get(dataMap.get(EV_STARTDATE)))) {
            if (!Utils.isEmpty(l.get(dataMap.get(EV_STARTTIME)))) {
              Calendar cal = GregorianCalendar.getInstance();
              try {
                cal.setTime(df.parse(l.get(dataMap.get(EV_STARTDATE)) + " " + l.get(dataMap.get(EV_STARTTIME))));
              } catch (Exception e) {
                isValid = false;
                throw e;
              }
              if (!Utils.isEmpty(l.get(dataMap.get(EV_ALLDAY))) && isValid) {
                if (Boolean.parseBoolean(l.get(dataMap.get(EV_ALLDAY)))) {
                  cal.set(Calendar.HOUR_OF_DAY, 0);
                  cal.set(Calendar.MINUTE, 0);
                  cal.set(Calendar.MILLISECOND, 0);
                }
              }
              if (isValid)
                eventObj.setFromDateTime(cal.getTime());
            }
          }
          // Event todate
          if (!Utils.isEmpty(l.get(dataMap.get(EV_ENDDATE)))) {
            if (!Utils.isEmpty(l.get(dataMap.get(EV_ENDTIME)))) {
              Calendar cal = GregorianCalendar.getInstance();
              try {
                cal.setTime(df.parse(l.get(dataMap.get(EV_ENDDATE)) + " " + l.get(dataMap.get(EV_ENDTIME))));
              } catch (Exception e) {
                isValid = false;
                // throw new IOException() ;
                throw e;
              }
              if (!Utils.isEmpty(l.get(dataMap.get(EV_ALLDAY))) && isValid) {
                if (Boolean.parseBoolean(l.get(dataMap.get(EV_ALLDAY)))) {
                  cal.set(Calendar.HOUR_OF_DAY, 23);
                  cal.set(Calendar.MINUTE, 59);
                  cal.set(Calendar.MILLISECOND, 999);
                }
              }
              if (isValid)
                eventObj.setToDateTime(cal.getTime());
            }
            // Event oner 9
            // Event Participants 10
            if (isValid) {
              if (!Utils.isEmpty(l.get(dataMap.get(EV_ATTENDEES)))) {
                eventObj.setParticipant(l.get(dataMap.get(EV_ATTENDEES)).split(";"));
              }
              // Event Invitation 11
              if (!Utils.isEmpty(l.get(dataMap.get(EV_INVITATION)))) {
                eventObj.setInvitation(l.get(dataMap.get(EV_INVITATION)).split(";"));
              }
              // Event categories 14
              if (!Utils.isEmpty(l.get(dataMap.get(EV_CATEGORIES)))) {
                eventObj.setEventCategoryName(l.get(dataMap.get(EV_CATEGORIES)));
                // eventObj.setEventCategoryId(l.get(dataMap.get(EV_CATEGORIES)).toLowerCase()) ;
              } else {
                eventObj.setEventCategoryName("csvimported");
                // eventObj.setEventCategoryId("csvimported") ;
              }
              // Event Place
              if (!Utils.isEmpty(l.get(dataMap.get(EV_LOCATION)))) {
                eventObj.setLocation(l.get(dataMap.get(EV_LOCATION)));
              }
              if (!Utils.isEmpty(l.get(dataMap.get(EV_DESCRIPTION)))) {
                eventObj.setDescription(l.get(dataMap.get(EV_DESCRIPTION)));
              }
              if (!Utils.isEmpty(l.get(dataMap.get(EV_STATUS)))) {
                String eventState = l.get(dataMap.get(EV_STATUS));
                // fix for csv export form outlook
                int value = Integer.valueOf(eventState);
                if (value == 0 || value == 1) {
                  eventState = CalendarEvent.ST_AVAILABLE;
                }
                if (value == 2) {
                  eventState = CalendarEvent.ST_BUSY;
                }
                if (value == 3) {
                  eventState = CalendarEvent.ST_OUTSIDE;
                }
                eventObj.setEventState(eventState);
              }
              if (!Utils.isEmpty(l.get(dataMap.get(EV_PRIORITY)))) {
                for (int i = 0; i < CalendarEvent.PRIORITY.length; i++) {
                  if (CalendarEvent.PRIORITY[i].equalsIgnoreCase(l.get(dataMap.get(EV_PRIORITY)).toLowerCase())) {
                    eventObj.setPriority(String.valueOf(i));
                    break;
                  }
                }
              }
            }
          }
          if (isValid)
            eventList.add(eventObj);
          else
            break;
        }
      }
      lineCount++;
    }
    return eventList;
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
      if (match == null)
        break;
      if (match.endsWith(",")) { // trim trailing ,
        match = match.substring(0, match.length() - 1);
      }
      if (match.startsWith("\"")) { // assume also ends with
        match = match.substring(1, match.length() - 1);
      }
      if (match.length() == 0)
        match = null;
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
      NodeIterator iter = storage_.getCalendarCategoryHome(username).getNodes();
      Node cat = null;
      String categoryId;
      boolean isExists = false;
      while (iter.hasNext()) {
        cat = iter.nextNode();
        if (cat.getProperty(Utils.EXO_NAME).getString().equals("Imported")) {
          isExists = true;
          break;
        }
      }
      if (!isExists) {
        CalendarCategory calendarCate = new CalendarCategory();
        calendarCate.setDescription("Imported icalendar category");
        calendarCate.setName("Imported");
        categoryId = calendarCate.getId();
        storage_.saveCalendarCategory(username, calendarCate, true);
      } else {
        categoryId = cat.getProperty(Utils.EXO_ID).getString();
      }
      org.exoplatform.calendar.service.Calendar exoCalendar = new org.exoplatform.calendar.service.Calendar();
      exoCalendar.setName(calendarName);
      exoCalendar.setCalendarColor(org.exoplatform.calendar.service.Calendar.COLORS[new Random().nextInt(org.exoplatform.calendar.service.Calendar.COLORS.length - 1)]);
      exoCalendar.setDescription(Utils.EMPTY_STR);
      exoCalendar.setCategoryId(categoryId);
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
      process(new BufferedReader(new InputStreamReader(icalInputStream)));
      return true;
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
}
