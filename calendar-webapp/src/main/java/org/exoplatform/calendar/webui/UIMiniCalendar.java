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

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
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
  
  protected Map<Integer, String> getData() throws Exception{ 
    Map<Integer, String> busyDay = new HashMap<Integer, String>();
    List<org.exoplatform.calendar.model.Event> events = getEventInMonth();
    
    Calendar first = getBeginDateOfMonth();
    while (first.compareTo(getEndDateOfMonth()) < 0) {
      for (org.exoplatform.calendar.model.Event evt : events) {
        if (first.compareTo(CalendarUtils.getBeginDay(evt.getFromDateTime())) >= 0 && 
            first.compareTo(CalendarUtils.getEndDay(evt.getToDateTime())) < 0) {
          busyDay.put(first.get(Calendar.DAY_OF_YEAR), "value");
        }
      }
      first.add(Calendar.DATE, 1);
    }
    return busyDay;
  }
  
  @Override
  public LinkedHashMap<String, org.exoplatform.calendar.model.Event> getDataMap(){ return null ; }

  public java.util.Calendar getBeginDateOfMonthView() throws Exception {
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
  
  public void setCategoryId(String categoryId) {
    categoryId_ = categoryId ;
  }
  
  @Override
  public String getSelectedCategory() {
    return categoryId_  ;
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
