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
package org.exoplatform.cs.bench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 3, 2011  
 */
public class CalendarDataInjector extends DataInjector {
  private static final Log       log                = ExoLogger.getLogger(CalendarDataInjector.class);

  private static final String    EMPTY              = "".intern();

  private int                    maxCategories      = 3;

  private int                    maxEventCategories = 3;

  private int                    maxCalendars       = 3;

  private int                    maxEvents          = 4;

  private int                    maxTasks           = 4;

  private String                 baseURL            = EMPTY;

  private String                 typeOfInject       = "all";

  private boolean                randomize          = true;

  private CalendarService        calService;

  private String                 currentUser        = EMPTY;

  private CalendarSetting        setting            = new CalendarSetting();

  private List<EventCategory>    eventCategory      = new ArrayList<EventCategory>();

  private Set<String>            eventCategorys     = new HashSet<String>();

  private Set<String>            categoryIds        = new HashSet<String>();

  private Set<String>            publicCalendar     = new HashSet<String>();

  private Set<String>            privateCalendar    = new HashSet<String>();

  private List<String>           name               = new ArrayList<String>();

  private String[]               groupShare         = new String[] { EMPTY };

  private String[]               groups             = new String[] { EMPTY };

  private Random                 rand               = new Random();
  
  private static final String DEFAULT_LOCATION = "VNM";

  public CalendarDataInjector(CalendarService calService, InitParams params) {
    initParams(params);
    this.calService = calService;
  }

  private void initDatas() {
    String str = "/:*.*";
    rand = new Random();
    Identity identity = ConversationState.getCurrent().getIdentity();
    currentUser = identity.getUserId();
    Set<String> set = new HashSet<String>(identity.getGroups());
    groupShare = new String[set.size() + 1];
    int i = 0;
    for (String string : set) {
      groupShare[i] = string + str;
      i++;
    }
    groupShare[i] = currentUser;
    set.add(currentUser);
    groups = set.toArray(new String[set.size()]);
  }

  @Override
  public Log getLog() {
    return log;
  }

  private int getParam(InitParams initParams, String param, int df) {
    try {
      return Integer.parseInt(initParams.getValueParam(param).getValue());
    } catch (NumberFormatException e) {
      return df;
    }
  }

  private boolean getParam(InitParams initParams, String param) {
    return Boolean.parseBoolean(initParams.getValueParam(param).getValue());
  }

  private String getParam(InitParams initParams, String param, String df) {
    return String.valueOf(initParams.getValueParam(param).getValue());
  }

  public void initParams(InitParams initParams) {
    maxCategories = getParam(initParams, "mCt", maxCategories);
    maxEventCategories = getParam(initParams, "mEcat", maxEventCategories);
    maxCalendars = getParam(initParams, "mCal", maxCalendars);
    maxEvents = getParam(initParams, "mEv", maxEvents);
    maxTasks = getParam(initParams, "mTa", maxTasks);
    baseURL = getParam(initParams, "baseURL", baseURL);
    typeOfInject = getParam(initParams, "typeOfInject", typeOfInject);
    randomize =(boolean)getParam(initParams, "rand");
  }

  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    log.info("Start inject datas for calendar....");
    setHistoryInject();
    if ("all".equals(typeOfInject)) {
      if (currentUser.length() > 0) {
        initPrivateCalendar();
      }
      initPublicCalendar();
    } else if ("public".equals(typeOfInject)) {
      initPublicCalendar();
    } else if (currentUser.length() > 0) {
      initPrivateCalendar();
    }
  }

  private void removePrivateData() throws Exception {
    try {
      log.info(String.format("removing private datas..... \n  removing %s calendars.....", privateCalendar.size()));
      for (String calId : privateCalendar) {
        if (!isEmpty(calId)) {
          calService.removeUserCalendar(currentUser, calId);
        }
      }
      log.info(String.format("removing %s event catetories.....", eventCategorys.size()));
      for (String evCatId : eventCategorys) {
        if (!isEmpty(evCatId)) {
          calService.removeEventCategory(currentUser, evCatId);
        }
      }

    } catch (Exception e) {
      log.debug("Failed to remove private injecter datas", e);
    }
  }

  private void removePublicData() throws Exception {
    try {
      log.info(String.format("remove public datas..... \n  removing %s calendars.....", publicCalendar.size()));
      for (String calId : publicCalendar) {
        if (!isEmpty(calId)) {
          calService.removePublicCalendar(calId);
        }
      }
    } catch (Exception e) {
      log.debug("Failed to remove public injecter datas", e);
    }
  }

  @Override
  public void reject(HashMap<String, String> queryParams) throws Exception {
    setHistoryInject();
    if ("all".equals(typeOfInject)) {
      // remove public
      removePublicData();
      if (currentUser.length() > 0) {
        // remove private
        removePrivateData();
      }
    } else if ("public".equals(typeOfInject)) {
      // remove public
      removePublicData();
    } else if (currentUser.length() > 0) {
      // remove private
      removePrivateData();
    }
    log.info("Complated reject datas..");
    publicCalendar.clear();
    privateCalendar.clear();
    eventCategorys.clear();
    categoryIds.clear();
    saveHistoryInject();
  }

  private void initPublicCalendar() throws Exception {
    // save public calendar
    List<Calendar> calendars = findCalendars(true);
    log.info("Inject public datas ....");
    int index = 0, size = calendars.size(), evsCal, tasCal, evs = 0, tas = 0;
    long t, t1 = System.currentTimeMillis();
    for (Calendar calendar : calendars) {
      t = System.currentTimeMillis();
      evsCal = tasCal = 0;
      calService.savePublicCalendar(calendar, true);
      publicCalendar.add(calendar.getId());
      // save event in public calendar
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "2", CalendarEvent.TYPE_EVENT, true)) {
        calService.savePublicEvent(calendar.getId(), event, true);
        evsCal++;
      }
      // save task in public calendar
      for (CalendarEvent event : findCalendarEvent(calendar.getId(), "2", CalendarEvent.TYPE_TASK, true)) {
        calService.savePublicEvent(calendar.getId(), event, true);
        tasCal++;
      }
      log.info(String.format("Saved Calendar %s/%s with %s Events and %s Tasks in %sms",
                             (++index), size, evsCal, tasCal, (System.currentTimeMillis()) - t));
      evs += evsCal;
      tas += tasCal;
    }
    log.info(String.format("INITIALIZED: Calendars=%s / Events=%s / Tasks=%s in %sms",
                           publicCalendar.size(), evs, tas, (System.currentTimeMillis() - t1)));
    saveHistoryInject();
  }

  private void initPrivateCalendar() throws Exception {
    log.info("Inject private datas ....");
    // save setting
    try {
      setting = calService.getCalendarSetting(currentUser);
      log.info(String.format("Save calendar setting for user %s ....", currentUser));
    } catch (Exception e) {
      setting = newCalendarSetting();
      calService.saveCalendarSetting(currentUser, setting);
    }
    long t = System.currentTimeMillis(), t1 = t;

    t = System.currentTimeMillis();
    // save EventCategoy
    List<EventCategory> eventCategories = findEventCategorys();
    for (EventCategory evCat : eventCategories) {
      calService.saveEventCategory(currentUser, evCat, true);
      eventCategory.add(evCat);
      eventCategorys.add(evCat.getId());
    }
    log.info(String.format("Saved %s eventCategories in %sms", eventCategories.size(), (System.currentTimeMillis() - t)));
    // save calendar
    List<Calendar> calendars = findCalendars(false);
    List<CalendarEvent> events;
    int index = 0, size = calendars.size(), evsCal, evs = 0, tas = 0;
    for (Calendar calendar : calendars) {
      t = System.currentTimeMillis();
      calService.saveUserCalendar(currentUser, calendar, true);
      privateCalendar.add(calendar.getId());
      // save Event
      events = findCalendarEvent(calendar.getId(), "0", CalendarEvent.TYPE_EVENT, false);
      for (CalendarEvent event : events) {
        calService.saveUserEvent(currentUser, calendar.getId(), event, true);
      }
      evsCal = events.size();
      evs += evsCal;
      // save Task
      events = findCalendarEvent(calendar.getId(), "0", CalendarEvent.TYPE_TASK, false);
      tas += events.size();
      for (CalendarEvent event : events) {
        calService.saveUserEvent(currentUser, calendar.getId(), event, true);
      }
      log.info(String.format("Saved Calendar %s/%s with %s Events and %s Tasks in %sms",
                             (++index), size, evsCal, events.size(), (System.currentTimeMillis()) - t));
    }
    log.info(String.format("INITIALIZED EventCategorys=%s / Calendars=%s / Events=%s / Tasks=%s in %sms",
                           eventCategories.size(), calendars.size(), evs, tas, (System.currentTimeMillis() - t1)));
    saveHistoryInject();
  }

  private List<EventCategory> findEventCategorys() throws Exception {
    List<EventCategory> categories = new ArrayList<EventCategory>();
    int mCat = getMaxItem(maxEventCategories);
    name.clear();
    for (int i = 0; i < mCat; i++) {
      categories.add(newEventCategory());
    }
    return categories;
  }

  private List<Calendar> findCalendars(boolean isPublic) throws Exception {
    List<Calendar> calendars = new ArrayList<Calendar>();
    int mCal = getMaxItem(maxCalendars);
    name.clear();
    for (int i = 0; i < mCal; i++) {
      calendars.add((isPublic) ? newPublicCalendar() : newPrivateCalendar());
    }
    return calendars;
  }

  private List<CalendarEvent> findCalendarEvent(String calendarId, String CalType, String type, boolean isPublic) throws Exception {
    List<CalendarEvent> calendars = new ArrayList<CalendarEvent>();
    int mCe = (type.equals(CalendarEvent.TYPE_EVENT)) ? getMaxItem(maxEvents) : getMaxItem(maxTasks);
    name.clear();
    for (int i = 0; i < mCe; i++) {
      calendars.add(newCalendarEvent(calendarId, CalType, type, isPublic));
    }
    return calendars;
  }

  private int getMaxItem(int maxType) {
    return (randomize) ? (new Random(maxType + 1).nextInt(maxType) + 1) : maxType;
  }

  private CalendarSetting newCalendarSetting() {
    CalendarSetting setting = new CalendarSetting();
    setting.setViewType("1");
    setting.setBaseURL(baseURL);
    setting.setWeekStartOn(String.valueOf(java.util.Calendar.MONDAY));
    setting.setWorkingTimeBegin("08:00");
    setting.setWorkingTimeEnd("18:00");
    setting.setShowWorkingTime(false);
    setting.setTimeZone("Asia/Ho_Chi_Minh");
    return setting;
  }

  private Calendar newPrivateCalendar() {
    Calendar calendar = new Calendar();
    calendar.setCalendarOwner(currentUser);
    calendar.setDataInit(true);
    calendar.setName(calRandomWords(5));
    calendar.setDescription(randomWords(20));
    calendar.setCalendarColor(getRandomColor());
    calendar.setEditPermission(new String[] {});
    calendar.setGroups(new String[] {});
    calendar.setViewPermission(new String[] {});
    calendar.setPrivateUrl(EMPTY);
    calendar.setPublicUrl(EMPTY);
    calendar.setPublic(false);
    calendar.setLocale(DEFAULT_LOCATION);
    calendar.setTimeZone(setting.getTimeZone());
    return calendar;
  }

  private Calendar newPublicCalendar() {
    Calendar calendar = new Calendar();
    calendar.setCalendarOwner(currentUser);
    calendar.setDataInit(true);
    calendar.setName(calRandomWords(5));
    calendar.setDescription(randomWords(20));
    calendar.setCalendarColor(getRandomColor());
    calendar.setEditPermission(groupShare);
    calendar.setGroups(groups);
    calendar.setViewPermission(new String[] { "*.*" });
    calendar.setPrivateUrl(EMPTY);
    calendar.setPublicUrl(EMPTY);
    calendar.setPublic(true);
    calendar.setLocale("VNM");
    calendar.setTimeZone("Asia/Ho_Chi_Minh");
    return calendar;
  }

  private EventCategory newEventCategory() {
    EventCategory eventCategory = new EventCategory();
    eventCategory.setDataInit(true);
    eventCategory.setName(calRandomWords(5));
    return eventCategory;
  }

  private CalendarEvent newCalendarEvent(String calendarId, String CalType, String type, boolean isPublic) {
    CalendarEvent categoryEvent = new CalendarEvent();
    categoryEvent.setCalendarId(calendarId);
    categoryEvent.setCalType(CalType);
    categoryEvent.setDescription(randomWords(20));
    if (!isPublic) {
      EventCategory eventCategory = randomEventCategory();
      categoryEvent.setEventCategoryId(eventCategory.getId());
      categoryEvent.setEventCategoryName(eventCategory.getName());
    }
    categoryEvent.setEventState(randomState());
    categoryEvent.setEventType(type);
    long time = randomDateTime(rand.nextInt(365), 0);
    categoryEvent.setFromDateTime(getTime(time));
    time = randomDateTime(rand.nextInt(5), time);
    categoryEvent.setToDateTime(getTime(time));

    categoryEvent.setLocation(DEFAULT_LOCATION);
    categoryEvent.setMessage(randomWords(30));

    categoryEvent.setInvitation(new String[] { EMPTY });
    categoryEvent.setParticipant(new String[] { currentUser });
    categoryEvent.setParticipantStatus(new String[] { currentUser + ":" });
    categoryEvent.setPriority(CalendarEvent.PRIORITY[rand.nextInt(CalendarEvent.PRIORITY.length)]);
    categoryEvent.setSendOption(CalendarSetting.ACTION_NEVER);
    categoryEvent.setStatus(EMPTY);
    categoryEvent.setTaskDelegator(EMPTY);
    categoryEvent.setRepeatType(CalendarEvent.REPEATTYPES[rand.nextInt(CalendarEvent.REPEATTYPES.length)]);

    categoryEvent.setSummary(calRandomWords(5));
    categoryEvent.setPrivate(!isPublic);
    return categoryEvent;
  }

  private String randomState() {
    String[] srts = new String[] { CalendarEvent.ST_AVAILABLE, CalendarEvent.ST_BUSY, CalendarEvent.ST_OUTSIDE };
    return srts[rand.nextInt(srts.length)];
  }

  private String calRandomWords(int i) {
    String s = "qwertyuiopasdfghjkzxcvbnm";
    s = randomWords(i) + String.valueOf(s.charAt(new Random().nextInt(s.length())));
    if (name.contains(s)) {
      return calRandomWords(i + 1);
    } else {
      name.add(s);
    }
    return s;
  }

  private static int clIndex = -1;

  private static int l       = 1;

  private String getRandomColor() {
    if (clIndex <= 0) {
      l = 1;
    } else if (clIndex >= Calendar.COLORS.length - 1) {
      l = -1;
    }
    clIndex += l;
    return Calendar.COLORS[clIndex];
  }

  private long randomDateTime(long days, long oldTime) {
    long time = (rand.nextInt(107) + 7) * 600000;
    if (days > 0) {
      time = days * 86400000 + (time / 10);
    }
    if (oldTime > 0) {
      time += oldTime;
    }
    return time;
  }

  private Date getTime(long time) {
    java.util.Calendar calendar = GregorianCalendar.getInstance();
    calendar.setLenient(false);
    long gmtoffset = calendar.get(java.util.Calendar.DST_OFFSET) + calendar.get(java.util.Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset + time);
    return calendar.getTime();
  }

  private EventCategory randomEventCategory() {
    int i = eventCategory.size();
    return eventCategory.get(new Random().nextInt(i));
  }

  private void saveHistoryInject() throws Exception {
    baseURL = publicCalendar.toString();
    baseURL += ";" + privateCalendar.toString();
    baseURL += ";" + eventCategorys.toString();
    baseURL += ";" + categoryIds.toString();
    setting.setBaseURL(baseURL);
    calService.saveCalendarSetting(currentUser, setting);
  }

  private void setHistoryInject() {
    initDatas();
    try {
      String s = calService.getCalendarSetting(currentUser).getBaseURL();
      if (!isEmpty(s) && s.indexOf(";") > 0) {
        String[] strs = s.split(";");
        publicCalendar.addAll(convertStringToList(strs[0]));
        privateCalendar.addAll(convertStringToList(strs[1]));
        eventCategorys.addAll(convertStringToList(strs[2]));
        categoryIds.addAll(convertStringToList(strs[3]));
      }
    } catch (Exception e) {
      log.warn("Failed to get calendar settings", e);
    }
  }

  private List<String> convertStringToList(String s) {
    s = s.replace("[", "").replace("]", "");
    s = s.trim().replaceAll("(,\\s*)", ",").replaceAll("(\\s*,)", ",");
    String[] strs = s.split(",");
    return new ArrayList<String>(Arrays.asList(strs));
  }

  private boolean isEmpty(String s) {
    return (s == null || s.trim().length() <= 0);
  }

  @Override
  public Object execute(HashMap<String, String> arg0) throws Exception {
    return new Object();
  }
}
