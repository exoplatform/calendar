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
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;

import java.util.Date;
import java.util.List;

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


  public UICalendarWorkingContainer() throws Exception {
    addChild(UICalendarContainer.class, null, null).setRendered(true) ;
    addChild(UICalendarViewContainer.class, null, null).setRendered(true) ;
  }  

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
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
}
