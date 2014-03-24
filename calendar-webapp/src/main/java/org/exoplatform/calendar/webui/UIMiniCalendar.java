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
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/calendar/webui/UIMiniCalendar.gtmpl",
    events = {
      @EventConfig(listeners = UIMiniCalendar.MoveNextActionListener.class), 
      @EventConfig(listeners = UIMiniCalendar.MovePreviousActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class)
    }

)
public class UIMiniCalendar extends UICalendarView  {
  private Map<Integer, String> dataMap = new HashMap<Integer, String>() ;
  private String categoryId_ = null ;

  private static final Log LOG = ExoLogger.getExoLogger(UIMiniCalendar.class);

  public UIMiniCalendar() throws Exception { }

  protected int getWeeksOfTheMonth(int year, int month, int day) {
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DATE, day);
    return cal.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH) ;
  }
  
  protected Map<Integer, String> getData(){ return dataMap ; }
  @Override
  public LinkedHashMap<String, CalendarEvent> getDataMap(){ return null ; }

  public java.util.Calendar getBeginDateOfMonthView() throws Exception{
    java.util.Calendar temCal = getBeginDateOfMonth() ;
    temCal.setFirstDayOfWeek(Integer.parseInt(calendarSetting_.getWeekStartOn())) ;
    int amount = temCal.getFirstDayOfWeek() - temCal.get(java.util.Calendar.DAY_OF_WEEK) ;
    temCal.add(java.util.Calendar.DATE, amount) ;
    while ((temCal.get(Calendar.DATE) < 20) && (temCal.get(Calendar.DATE) != 1)) {
      temCal.add(Calendar.DATE, -7); 
    }
    return CalendarUtils.getBeginDay(temCal) ;
  }
  public java.util.Calendar getEndDateOfMonthView() throws Exception{
    java.util.Calendar temCal = getBeginDateOfMonthView() ;
    temCal.setFirstDayOfWeek(Integer.parseInt(calendarSetting_.getWeekStartOn())) ;
    temCal.add(java.util.Calendar.DATE, getWeeksOfTheMonth(getCurrentYear(), getCurrentMonth(), 1)*7) ;
    return getBeginDay(temCal) ;
  }
  public java.util.Calendar getBeginDateOfMonth() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    temCal.set(java.util.Calendar.DATE, 1) ;
    return CalendarUtils.getBeginDay(temCal) ;  
  }
  public java.util.Calendar getEndDateOfMonth() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    temCal.set(java.util.Calendar.DATE, getDaysInMonth()) ;
    return CalendarUtils.getEndDay(temCal) ;  
  }
  public void setCategoryId(String categoryId) {
    categoryId_ = categoryId ;
  }
  @Override
  public String getSelectedCategory() {
    return categoryId_  ;
  }


  @Override
  public void refresh() throws Exception {
    dataMap.clear() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(getBeginDateOfMonth()) ;
    Calendar cal = getEndDateOfMonth() ;
    cal.add(java.util.Calendar.MILLISECOND, -1) ;
    eventQuery.setToDate(cal) ;
    eventQuery.setExcludeRepeatEvent(true);
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String timezone = CalendarUtils.getCurrentUserCalendarSetting().getTimeZone();

    String currentUser        = CalendarUtils.getCurrentUser();
    String[] publicCalendars  = getPublicCalendars();
    String[] privateCalendars = getPrivateCalendars().toArray(new String[]{});

    List<Map<Integer, String>> map = calendarService.searchHightLightEventSQL(currentUser, eventQuery,
        privateCalendars, publicCalendars);

    dataMap = map.get(0);
    emptyEventCalendars = new ArrayList<String>(map.get(1).values());

    map = calendarService.searchHighlightRecurrenceEventSQL(currentUser, eventQuery, timezone,
        privateCalendars, publicCalendars);

    dataMap.putAll(map.get(0));
    emptyRecurrentEventCalendars = new ArrayList<String>(map.get(1).values());

    /** Propagate empty calendars to other views */
    UICalendarPortlet calendarPortlet = getAncestorOfType(UICalendarPortlet.class);
    UICalendarViewContainer viewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class);

    String viewType = viewContainer.getCurrentViewType();
    if (UICalendarViewContainer.DAY_VIEW.equals(viewType)) {
      UIDayView dayView = viewContainer.getChild(UIDayView.class);
      if (dayView == null) dayView =  viewContainer.addChild(UIDayView.class, null, null) ;
      dayView.setEmptyEventCalendars(emptyEventCalendars);
      dayView.setEmptyRecurrentEventCalendars(emptyRecurrentEventCalendars);
    } else if (UICalendarViewContainer.WEEK_VIEW.equals(viewType) || UICalendarViewContainer.WORKING_VIEW.equals(viewType)) {
      UIWeekView weekView = viewContainer.getChild(UIWeekView.class);
      if (weekView == null) weekView = viewContainer.addChild(UIWeekView.class, null, null);
      weekView.setEmptyEventCalendars(emptyEventCalendars);
      weekView.setEmptyRecurrentEventCalendars(emptyRecurrentEventCalendars);
    } else if (UICalendarViewContainer.MONTH_VIEW.equals(viewType)) {
      UIMonthView monthView = viewContainer.getChild(UIMonthView.class);
      if (monthView == null) monthView = viewContainer.addChild(UIMonthView.class, null, null);
      monthView.setEmptyEventCalendars(emptyEventCalendars);
      monthView.setEmptyRecurrentEventCalendars(emptyRecurrentEventCalendars);
    } else if (UICalendarViewContainer.LIST_VIEW.equals(viewType)) {
      UIListContainer uiView = viewContainer.getChild(UIListContainer.class) ;
      if (uiView == null) uiView =  viewContainer.addChild(UIListContainer.class, null, null) ;
      UIListView listView = uiView.getChild(UIListView.class) ;
      listView.setEmptyEventCalendars(emptyEventCalendars);
      listView.setEmptyRecurrentEventCalendars(emptyRecurrentEventCalendars);
    }

  }


  static  public class MoveNextActionListener extends EventListener<UIMiniCalendar> {
    @Override
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar miniCal = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(TYPE_MONTH == Integer.parseInt(type)) {
        miniCal.calendar_.add(java.util.Calendar.MONTH, 1) ;
      } else {
        miniCal.calendar_.add(Calendar.YEAR, 1) ;
      }
      miniCal.setCurrentDay(1);
      event.getRequestContext().addUIComponentToUpdateByAjax(miniCal.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIMiniCalendar> {
    @Override
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar miniCal = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(TYPE_MONTH == Integer.parseInt(type)) {
        miniCal.calendar_.add(java.util.Calendar.MONTH, -1) ;
      } else {
        miniCal.calendar_.add(Calendar.YEAR, -1) ;
      }
      miniCal.setCurrentDay(1);
      event.getRequestContext().addUIComponentToUpdateByAjax(miniCal.getParent()) ;
    }
  }

  @Override
  public String getDefaultStartTimeOfEvent() {
    return String.valueOf(calendar_.getTimeInMillis());
  }
}
