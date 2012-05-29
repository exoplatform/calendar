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

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIYearView.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UICalendarView.MoveNextActionListener.class), 
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class),
      @EventConfig(listeners = UIYearView.OnchangeActionListener.class )   
    }

)
public class UIYearView extends UICalendarView {
  private Map<Integer, String > yearData_ = new HashMap<Integer, String>() ;
  private String categoryId_ = null ;
  public UIYearView() throws Exception {
    super() ;
  }

  protected void yearNext(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  protected void yearBack(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  protected Map<Integer, String> getValueMap() { return yearData_ ; }
  
  public void refresh() throws Exception { 
    yearData_.clear() ;
    Calendar cal =  calendarSetting_.createCalendar(calendar_.getTime());
    cal.set(Calendar.DAY_OF_YEAR, 1);
    Calendar beginYear = CalendarUtils.getBeginDay(cal) ;
    cal.add(Calendar.YEAR, 1) ;
    Calendar endYear = CalendarUtils.getBeginDay(cal);
    endYear.add(Calendar.MILLISECOND, -1) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    String timezone = CalendarUtils.getCurrentUserCalendarSetting().getTimeZone();
    EventQuery eventQuery = new EventQuery() ;
    
    if (!CalendarUtils.isEmpty(categoryId_) && !categoryId_.toLowerCase().equals("null") 
        && !categoryId_.equals("calId") && !categoryId_.equals(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL)) {
      eventQuery.setCategoryId(new String[]{categoryId_}) ;
    }
    eventQuery.setFromDate(beginYear) ;
    eventQuery.setToDate(endYear) ;
    eventQuery.setExcludeRepeatEvent(true);
    yearData_ = calendarService.searchHightLightEvent(username, eventQuery, getPublicCalendars());
    yearData_.putAll(calendarService.searchHighlightRecurrenceEvent(username, eventQuery, getPublicCalendars(), timezone));
    UIFormSelectBox uiCategory = getUIFormSelectBox(EVENT_CATEGORIES) ;
    uiCategory.setValue(categoryId_) ;
    uiCategory.setOnChange("Onchange") ;
  }

  @Override
  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    return null;
  }
  public String getSelectedCategory() {
    return categoryId_ ;
  }
  public void setCategoryId(String categoryId) {
    categoryId_ = categoryId ;
    setSelectedCategory(categoryId) ;
  }

  // CS-3357
  protected String[] getDaysNameTitle() { 
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormatSymbols dfs = new DateFormatSymbols(locale) ;
    Map<Integer, String> days = new LinkedHashMap<Integer, String>();
    if (!locale.getDisplayLanguage().contains("Vietnam")) {
      for(int i = 1; i < dfs.getWeekdays().length ; i ++) {
        days.put(i, dfs.getWeekdays()[i]) ;
      }
    } else {
      days.put(1, dfs.getWeekdays()[1]);
      for(int i = 2; i < dfs.getWeekdays().length ; i ++) {
        days.put(i, dfs.getWeekdays()[i].split(" ")[1].toUpperCase()) ;
      }
    }    
    return days.values().toArray(new String[]{})  ;
  }
  
  static  public class OnchangeActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView uiYearView = event.getSource() ;
      String categoryId = uiYearView.getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
      uiYearView.setCategoryId(categoryId) ;
      UIMiniCalendar uiMiniCalendar = uiYearView.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      uiMiniCalendar.setCategoryId(categoryId) ;
      uiYearView.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiYearView);           
    }
  }

  @Override
  public String getDefaultStartTimeOfEvent() {
    return String.valueOf(calendar_.getTimeInMillis());
  }
}
