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
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.UIActionBar;
import org.exoplatform.calendar.webui.UICalendarContainer;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarView;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarSettingForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarSettingForm.ShowAllTimeZoneActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UICalendarSettingForm.CancelActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UICalendarSettingForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIFormTabPane.SelectTabActionListener.class, phase = Phase.DECODE)
    }
)
public class UICalendarSettingForm extends UIFormTabPane implements UIPopupComponent
{
  final private static String SETTING_CALENDAR_TAB = "setting" ;
  final private static String DEFAULT_CALENDAR_TAB = "defaultCalendarTab" ;
  final private static String FEED_TAB = "feedTab";

  private Map<String, String> names_ = new HashMap<String, String>() ;
  public String[] sharedCalendarColors_  = null ;
  private CalendarSetting calendarSetting_ = null;

  public UICalendarSettingForm() throws Exception{
    super("UICalendarSettingForm") ;
    UICalendarSettingTab setting = new UICalendarSettingTab(SETTING_CALENDAR_TAB) ;//.setRendered(true) ;
    addUIFormInput(setting) ;
    setSelectedTab(setting.getId()) ;
    UICalendarSettingDisplayTab defaultCalendarsTab  = new UICalendarSettingDisplayTab(DEFAULT_CALENDAR_TAB) ;    
    addUIFormInput(defaultCalendarsTab) ;
    UICalendarSettingFeedTab uiFeedTab = new UICalendarSettingFeedTab(FEED_TAB);
    addUIFormInput(uiFeedTab);
  }

  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {}

  public void init(CalendarSetting calendarSetting, CalendarService cservice) throws Exception{
    names_.clear() ;
    if(calendarSetting != null) {
      calendarSetting_ = calendarSetting;
      sharedCalendarColors_ = calendarSetting.getSharedCalendarsColors() ;
      UICalendarSettingTab settingTab = getChildById(SETTING_CALENDAR_TAB) ;
      settingTab.setViewType(calendarSetting.getViewType()) ;
      settingTab.setWeekStartOn(calendarSetting.getWeekStartOn()) ;
      settingTab.setDateFormat(calendarSetting.getDateFormat()) ;
      settingTab.setTimeFormat(calendarSetting.getTimeFormat()) ;
      settingTab.getUIFormSelectBox(UICalendarSettingTab.WORKINGTIME_BEGIN).setOptions(CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(), 30)) ;
      settingTab.getUIFormSelectBox(UICalendarSettingTab.WORKINGTIME_END).setOptions(CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(), 30)) ;
      settingTab.setTimeZone(calendarSetting.getTimeZone()) ;
      settingTab.setShowWorkingTimes(calendarSetting.isShowWorkingTime()) ;
      if(calendarSetting.isShowWorkingTime()) {
        settingTab.setWorkingBegin(calendarSetting.getWorkingTimeBegin(), CalendarUtils.DATEFORMAT + " " + calendarSetting.getTimeFormat()) ;
        settingTab.setWorkingEnd(calendarSetting.getWorkingTimeEnd(), CalendarUtils.DATEFORMAT + " " + calendarSetting.getTimeFormat()) ;
      }
      settingTab.setSendOption(calendarSetting.getSendOption()) ;
      if(calendarSetting.getBaseURL() == null) calendarSetting.setBaseURL(CalendarUtils.getServerBaseUrl() + "calendar/iCalRss") ;
    }

    initDisplayedCalendarTab(calendarSetting, cservice);
  }

  private void initDisplayedCalendarTab(CalendarSetting calendarSetting, CalendarService cservice) throws Exception
  {
    String username = CalendarUtils.getCurrentUser() ;
    UICalendarSettingDisplayTab defaultCalendarsTab = getChildById(DEFAULT_CALENDAR_TAB) ;
    List<String> filteredCalendars = new ArrayList<String>() ;
    if(calendarSetting != null && calendarSetting.getFilterPrivateCalendars() != null) {
      filteredCalendars.addAll(Arrays.asList(calendarSetting.getFilterPrivateCalendars())) ;
    }
    if(calendarSetting != null && calendarSetting.getFilterPublicCalendars() != null) {
      filteredCalendars.addAll(Arrays.asList(calendarSetting.getFilterPublicCalendars())) ;
    }
    if(calendarSetting != null && calendarSetting.getFilterSharedCalendars() != null) {
      filteredCalendars.addAll(Arrays.asList(calendarSetting.getFilterSharedCalendars())) ;
    }

    List<Calendar> privateCals = defaultCalendarsTab.getAllPrivateCalendars();
    if (privateCals != null && !privateCals.isEmpty()) {
      for (Calendar calendar : privateCals) {
        names_.put(calendar.getId(), calendar.getName()) ;
        UICheckBoxInput checkBox = defaultCalendarsTab.getChildById(calendar.getId()) ;
        if(checkBox == null) {
          checkBox = new UICheckBoxInput(calendar.getId(), calendar.getId(), true) ;
          defaultCalendarsTab.addUIFormInput(checkBox) ;
        }
        checkBox.setChecked(true) ;
      }
    }


    List<Calendar> sharedCals = defaultCalendarsTab.getSharedCalendars().getCalendars();
    if(sharedCals != null && !sharedCals.isEmpty()) {
      for(Calendar calendar : sharedCals) {
        names_.put(calendar.getId(), calendar.getName()) ;
        UICheckBoxInput checkBox = defaultCalendarsTab.getChildById(calendar.getId()) ;
        if(checkBox == null) {
          checkBox = new UICheckBoxInput(calendar.getId(), calendar.getId(), true) ;
          defaultCalendarsTab.addUIFormInput(checkBox) ;
        }
        checkBox.setChecked(true) ;
      }
    }

    List<Calendar> publicCals = defaultCalendarsTab.getAllPublicCalendars();
    if (publicCals != null && !publicCals.isEmpty()) {
      for(Calendar calendar : publicCals) {

        String groupName = cservice.getGroupCalendars(calendar.getGroups(), false, username).get(0).getName();
        names_.put(calendar.getId(), CalendarUtils.getGroupCalendarName(
        groupName.substring(groupName.lastIndexOf("/") + 1), calendar.getName())) ;

        UICheckBoxInput checkBox = defaultCalendarsTab.getChildById(calendar.getId()) ;

        if(checkBox == null) {
          checkBox = new UICheckBoxInput(calendar.getId(), calendar.getId(), true) ;
          defaultCalendarsTab.addUIFormInput(checkBox) ;
        }
        checkBox.setChecked(true) ;
      }
    }

    for (String calId : filteredCalendars) {
      UICheckBoxInput input = defaultCalendarsTab.getChildById(calId) ;
      if (input != null) input.setChecked(false) ;
    }

  }


  @Override
  public String getLabel(ResourceBundle res, String id) {
    if(names_.get(id) != null) return names_.get(id) ;
    String label = getId() + ".label." + id;    
    return res.getString(label);
  }

  @Override
  public String getLabel(String id) {
    String label;
    try {
      label = super.getLabel(id);
    } catch (Exception e) {
      label = id;
    }
    return label;
  }

  /**
   * return list of unchecked calendars id
   *
   * @param calendars
   * @return
   */
  protected List<String> getUnCheckedList(List<Calendar> calendars) {
    List<String> list = new ArrayList<String>() ;
    for (Calendar cal : calendars) {
      UICheckBoxInput input = ((UIFormInputWithActions)getChildById(DEFAULT_CALENDAR_TAB)).getChildById(cal.getId()) ;
      if(input != null && !input.isChecked()) list.add(input.getId()) ;
    }
    return list;
  }


  @Override
  public String[] getActions(){
    return new String[]{"Save", "Cancel"} ;
  }


  static  public class SaveActionListener extends EventListener<UICalendarSettingForm> {
    @Override
    public void execute(Event<UICalendarSettingForm> event) throws Exception {
      UICalendarSettingForm uiForm = event.getSource() ;
      CalendarSetting calendarSetting = uiForm.calendarSetting_;
      UICalendarSettingTab settingTab = uiForm.getChildById(UICalendarSettingForm.SETTING_CALENDAR_TAB) ;
      calendarSetting.setSharedCalendarsColors(uiForm.sharedCalendarColors_) ;
      calendarSetting.setViewType(settingTab.getViewType()) ;
      calendarSetting.setWeekStartOn(settingTab.getWeekStartOn()) ;
      calendarSetting.setDateFormat(settingTab.getDateFormat()) ;
      calendarSetting.setTimeFormat(settingTab.getTimeFormat()) ;
      calendarSetting.setTimeZone(settingTab.getTimeZone()) ;
      calendarSetting.setBaseURL(CalendarUtils.getServerBaseUrl() + "calendar/iCalRss") ;
      calendarSetting.setSendOption(settingTab.getSendOption()) ;

      calendarSetting.setShowWorkingTime(settingTab.getShowWorkingTimes()) ;
      if(settingTab.getShowWorkingTimes()) {
        if(settingTab.getWorkingBegin().equals(settingTab.getWorkingEnd()) || settingTab.getWorkingBeginTime().after(settingTab.getWorkingEndTime())) {
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UICalendarSettingForm.msg.working-time-logic",
                                                  null,
                                                  AbstractApplicationMessage.WARNING));
          return ;
        }
        calendarSetting.setWorkingTimeBegin(settingTab.getWorkingBegin()) ;
        calendarSetting.setWorkingTimeEnd(settingTab.getWorkingEnd()) ;
      }

      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      List<String> defaultFilterCalendars = new ArrayList<String>() ;
      List<String> unCheckList = new ArrayList<String>() ;

      /* set calendar setting filter for private calendar */
      UICalendarSettingDisplayTab displayTab = uiForm.getChild(UICalendarSettingDisplayTab.class);
      defaultFilterCalendars = uiForm.getUnCheckedList( displayTab.getAllPrivateCalendars() ) ;
      calendarSetting.setFilterPrivateCalendars(defaultFilterCalendars.toArray(new String[] {})) ;
      if(!defaultFilterCalendars.isEmpty()){
        unCheckList.addAll(defaultFilterCalendars) ;
        defaultFilterCalendars.clear() ;
      }

      /* set calendar setting filter for shared calendar */
      defaultFilterCalendars = uiForm.getUnCheckedList(displayTab.getAllPublicCalendars()) ;
      calendarSetting.setFilterPublicCalendars(defaultFilterCalendars.toArray(new String[] {})) ;
      if(!defaultFilterCalendars.isEmpty()){
        unCheckList.addAll(defaultFilterCalendars) ;
        defaultFilterCalendars.clear() ;
      }

      /* set calendar setting filter for public calendar */
      defaultFilterCalendars = uiForm.getUnCheckedList(displayTab.getSharedCalendars().getCalendars());
      calendarSetting.setFilterSharedCalendars(defaultFilterCalendars.toArray(new String[] {})) ;
      if(!defaultFilterCalendars.isEmpty()){
        unCheckList.addAll(defaultFilterCalendars) ;
        defaultFilterCalendars.clear() ;
      }
      uiCalendars.checkAll() ;

      calendarService.saveCalendarSetting(CalendarUtils.getCurrentUser(), calendarSetting) ;
      
      calendarPortlet.setCalendarSetting(calendarSetting) ;
      String viewType = UICalendarViewContainer.TYPES[Integer.parseInt(calendarSetting.getViewType())] ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiViewContainer.initView(viewType) ;
      uiViewContainer.applySeting() ;
      uiViewContainer.refresh() ;
      
      UICalendarContainer uiCalendarContainer = calendarPortlet.findFirstComponentOfType(UICalendarContainer.class);
      uiCalendarContainer.applySeting();
      calendarPortlet.findFirstComponentOfType(UICalendarView.class).setCalendarSetting(calendarSetting);
      
      calendarPortlet.findFirstComponentOfType(UIActionBar.class).setCurrentView(viewType) ;
      calendarPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
    }
  }

  static  public class ShowAllTimeZoneActionListener extends EventListener<UICalendarSettingForm> {
    @Override
    public void execute(Event<UICalendarSettingForm> event) throws Exception {
      UICalendarSettingForm uiForm = event.getSource() ;
      UICalendarSettingTab calendarSettingTab = uiForm.getChildById(UICalendarSettingForm.SETTING_CALENDAR_TAB) ;
      uiForm.getUIFormSelectBox(UICalendarSettingTab.TIMEZONE).setOptions(calendarSettingTab.getTimeZones(null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarSettingForm> {
    @Override
    public void execute(Event<UICalendarSettingForm> event) throws Exception {
      UICalendarSettingForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
  
  static  public class AddActionListener extends EventListener<UICalendarSettingForm> {
    @Override
    public void execute(Event<UICalendarSettingForm> event) throws Exception {
      UICalendarSettingForm uiform = event.getSource() ;   
      UIPopupContainer popupContainer = uiform.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      UIEditFeed uiEditFeed = popupAction.activate(UIEditFeed.class, 500) ;
      uiEditFeed.setNew(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;      
    }
  }
  
}
