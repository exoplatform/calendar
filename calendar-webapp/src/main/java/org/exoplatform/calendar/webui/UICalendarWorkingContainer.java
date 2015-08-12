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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.model.query.CalendarQuery;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.ExtendedCalendarService;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
                 template =  "app:/templates/calendar/webui/UICalendarWorkingContainer.gtmpl"
)
public class UICalendarWorkingContainer extends UIContainer  {
  
  private Map<String, List<Calendar>> calendars = new HashMap<String, List<Calendar>>();
  
  /**
   * contains key as <type_calendar>:<calendar_id> and value as color of calendar
   * example: key 2:calendar1401dda8c0a801303011177469ff542e, value: color code
   */

  private LinkedHashMap<String, String> colorMap = new LinkedHashMap<String, String>();
  
  private CalendarService calService = getApplicationComponent(CalendarService.class);
  
  private ExtendedCalendarService xCalService = getApplicationComponent(ExtendedCalendarService.class);
  
  public UICalendarWorkingContainer() throws Exception {
    addChild(UICalendarContainer.class, null, null).setRendered(true) ;
    addChild(UICalendarViewContainer.class, null, null).setRendered(true) ;
  }
  
  public void init() throws Exception {
    colorMap.clear();
    calendars.clear();

    Identity identity = ConversationState.getCurrent().getIdentity();
    CalendarQuery query = new CalendarQuery();
    query.setIdentity(identity);
    List<Calendar> tmp = xCalService.getCalendarHandler().findCalendars(query);
    for (Calendar cal : tmp) {
      int t = calService.getTypeOfCalendar(identity.getUserId(), cal.getId());
      String typeName;      
      Calendar.Type type = Calendar.Type.getType(t);
      if (Calendar.Type.UNDEFINED.equals(type)) {
        typeName = String.valueOf(t);
      } else {
        typeName = type.name();
      }
      if (cal.isShared(identity.getUserId())) {
        typeName = Calendar.Type.SHARED.name();
      }
      List<Calendar> cals = calendars.get(typeName); 
      if (cals == null) {
        cals = new LinkedList<Calendar>();
        calendars.put(typeName, cals);
      }
      cals.add(cal);
      colorMap.put(cal.getId(), cal.getCalendarColor());
    }

    CalendarSetting setting = CalendarUtils.getCalendarService().getCalendarSetting(CalendarUtils.getCurrentUser()) ;
    for (String key : setting.getSharedCalendarsColors()) {
      colorMap.put(key.split(CalendarUtils.COLON)[0], key.split(CalendarUtils.COLON)[1]);
    }
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    init();
    active() ;
    super.processRender(context);
  }

  public void active() throws Exception {
    UIPopupWindow uiWindowE = getChildById("UIQuickAddEventPopupWindow") ;
    if(uiWindowE == null) uiWindowE = addChild(UIPopupWindow.class, null, "UIQuickAddEventPopupWindow") ;
    UIQuickAddEvent quickAddForm = (UIQuickAddEvent)uiWindowE.getUIComponent();
    if(quickAddForm == null) quickAddForm = createUIComponent(UIQuickAddEvent.class, null, null) ; 
    List<SelectItem> calendarOption = CalendarUtils.getCalendarOption();
    ((UIFormSelectBoxWithGroups)quickAddForm.getChildById(UIQuickAddEvent.FIELD_CALENDAR)).setOptions(calendarOption) ;
    quickAddForm.getUIFormSelectBox(UIQuickAddEvent.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory()) ;
    quickAddForm.setEvent(true) ;
    quickAddForm.setId("UIQuickAddEvent") ;
    quickAddForm.init(CalendarUtils.getCalendarService().getCalendarSetting(CalendarUtils.getCurrentUser()), String.valueOf(new Date().getTime()), String.valueOf(new Date().getTime())) ;
    uiWindowE.setUIComponent(quickAddForm) ;
    uiWindowE.setWindowSize(540, 0);

    UIPopupWindow uiWindowT =  getChildById("UIQuickAddTaskPopupWindow") ;
    if(uiWindowT == null) uiWindowT = addChild(UIPopupWindow.class, null, "UIQuickAddTaskPopupWindow") ;
    UIQuickAddEvent quickAddTask = (UIQuickAddEvent)uiWindowT.getUIComponent();
    if(quickAddTask == null) quickAddTask = createUIComponent(UIQuickAddEvent.class, null, null) ; 
    quickAddTask.setEvent(false) ;
    quickAddTask.setId("UIQuickAddTask") ;
    quickAddTask.init(CalendarUtils.getCalendarService().getCalendarSetting(CalendarUtils.getCurrentUser()),String.valueOf(new Date().getTime()), String.valueOf(new Date().getTime())) ;
    ((UIFormSelectBoxWithGroups)quickAddTask.getChildById(UIQuickAddEvent.FIELD_CALENDAR)).setOptions(calendarOption) ;
    quickAddTask.getUIFormSelectBox(UIQuickAddEvent.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory()) ;
    uiWindowT.setUIComponent(quickAddTask) ;
    uiWindowT.setWindowSize(540, 0);
  }

  public Map<String, List<Calendar>> getCalendarMap() {
    return calendars;
  }
  
  public Map<String, String> getColorMap() {
    return colorMap;
  }
}
