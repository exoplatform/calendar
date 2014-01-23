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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.ext.UIFormComboBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIQuickAddEvent.gtmpl",
    events = {
      @EventConfig(listeners = UIQuickAddEvent.SaveActionListener.class),
      @EventConfig(listeners = UIQuickAddEvent.MoreDetailActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIQuickAddEvent.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIQuickAddEvent extends UIForm implements UIPopupComponent{
  private static final Log log = ExoLogger.getExoLogger(UIQuickAddEvent.class);

  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_FROM_TIME = "fromTime".intern() ;
  final public static String FIELD_TO_TIME = "toTime".intern() ;
  final public static String FIELD_ALLDAY = "allDay".intern() ;
  final public static String FIELD_DESCRIPTION = "description".intern() ;
  final public static String UIQUICKADDTASK = "UIQuickAddTask".intern() ;

  private String calType_ = "0".intern() ;
  private boolean isEvent_ = true ;
  public UIQuickAddEvent() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null));
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormDateTimePicker(FIELD_FROM, FIELD_FROM, new Date(), false));
    addUIFormInput(new UIFormDateTimePicker(FIELD_TO, FIELD_TO, new Date(), false));
    addUIFormInput(new UIFormComboBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormComboBox(FIELD_TO_TIME, FIELD_TO_TIME, options));
    addUIFormInput(new UICheckBoxInput(FIELD_ALLDAY, FIELD_ALLDAY, false));
    addUIFormInput(new UIFormSelectBoxWithGroups(FIELD_CALENDAR, FIELD_CALENDAR, CalendarUtils.getCalendarOption())) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, CalendarUtils.getCategory())) ;
  }

  public UIFormComboBox getUIFormCombobox(String name) {
    return  findComponentById(name) ;
  }

  public void init(CalendarSetting  calendarSetting, String startTime, String endTime) throws Exception {
    List<SelectItemOption<String>> fromOptions 
    = CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(),calendarSetting.getTimeFormat(), calendarSetting.getTimeInterval()) ;
    List<SelectItemOption<String>> toOptions 
    = CalendarUtils.getTimesSelectBoxOptions(calendarSetting.getTimeFormat(),calendarSetting.getTimeFormat(),  calendarSetting.getTimeInterval()) ;
    UIFormDateTimePicker fromField = getChildById(FIELD_FROM) ;
    fromField.setDateFormatStyle(calendarSetting.getDateFormat()) ;
    UIFormDateTimePicker toField = getChildById(FIELD_TO) ;
    toField.setDateFormatStyle(calendarSetting.getDateFormat()) ;
    getUIFormCombobox(FIELD_FROM_TIME).setOptions(fromOptions) ;
    getUIFormCombobox(FIELD_TO_TIME).setOptions(toOptions) ;
    java.util.Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
    /*UIMiniCalendar miniCalendar = getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
    cal.setTime(miniCalendar.getCurrentCalendar().getTime());*/
    if(startTime != null) {
      cal.setTimeInMillis(Long.parseLong(startTime)) ;
    } 
    Long begingMinute = (cal.get(java.util.Calendar.MINUTE)/calendarSetting.getTimeInterval())*calendarSetting.getTimeInterval() ;
    cal.set(java.util.Calendar.MINUTE, begingMinute.intValue()) ;
    setEventFromDate(cal.getTime(),calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()) ;
    if(endTime != null ) cal.setTimeInMillis(Long.parseLong(endTime)) ; 
    else {
      cal.add(java.util.Calendar.MINUTE, (int)calendarSetting.getTimeInterval()*2) ;
    }
    setEventToDate(cal.getTime(),calendarSetting.getDateFormat(), calendarSetting.getTimeFormat()) ;
  }
  private void setEventFromDate(Date value, String dateFormat, String timeFormat) {
    UIFormDateTimePicker fromField = getChildById(FIELD_FROM) ;
    UIFormComboBox timeFile = getChildById(FIELD_FROM_TIME) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat ,locale) ;
    fromField.setValue(df.format(value));
    DateFormat tf = new SimpleDateFormat(timeFormat, locale) ;
    timeFile.setValue(tf.format(value)) ;
  }
  private Date getEventFromDate(String dateFormat, String timeFormat) throws Exception {
    try {
      UIFormDateTimePicker fromField = getChildById(FIELD_FROM) ;
      UIFormComboBox timeFile = getChildById(FIELD_FROM_TIME) ;
      
      WebuiRequestContext context = RequestContext.getCurrentInstance() ;
      Locale locale = context.getParentAppRequestContext().getLocale() ;
      
      if(getIsAllDay()) {
        DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
        df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
        return CalendarUtils.getBeginDay(df.parse(fromField.getValue())).getTime();
      } 
      DateFormat df = new SimpleDateFormat(dateFormat + Utils.SPACE  + timeFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
      return df.parse(fromField.getValue() + Utils.SPACE + timeFile.getValue()) ;
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to get event from date", e);
      }
      return null ;
    }
  }

  private void setEventToDate(Date value,String dateFormat,  String timeFormat) {
    UIFormDateTimePicker toField =  getChildById(FIELD_TO) ;
    UIFormComboBox timeField =  getChildById(FIELD_TO_TIME) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    toField.setValue(df.format(value)) ;
    DateFormat tf = new SimpleDateFormat(timeFormat, locale) ;
    timeField.setValue(tf.format(value)) ;
  }
  private Date getEventToDate(String dateFormat, String timeFormat) throws Exception {
    try {
      UIFormDateTimePicker toField = getChildById(FIELD_TO) ;
      UIFormComboBox timeFile = getChildById(FIELD_TO_TIME) ;
      WebuiRequestContext context = RequestContext.getCurrentInstance() ;
      Locale locale = context.getParentAppRequestContext().getLocale() ;
      
      if(getIsAllDay()) {
        DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
        df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
        return CalendarUtils.getEndDay(df.parse(toField.getValue())).getTime();
      } 
      DateFormat df = new SimpleDateFormat(dateFormat + Utils.SPACE + timeFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
      return df.parse(toField.getValue() + Utils.SPACE + timeFile.getValue()) ;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to get event to date", e);
      }
      return null ;
    }
  }

  public List<SelectItem> getCalendars() throws Exception {
    return CalendarUtils.getCalendarOption() ;
  }

  @Override
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  private String getEventSummary() {
    return getUIStringInput(FIELD_EVENT).getValue() ;
  }
  private String getEventDescription() {return getUIFormTextAreaInput(FIELD_DESCRIPTION).getValue() ;}


  private boolean getIsAllDay() {
    return getUICheckBoxInput(FIELD_ALLDAY).isChecked() ;
  }
  public void setIsAllday(boolean isChecked) {
    getUICheckBoxInput(FIELD_ALLDAY).setChecked(isChecked) ;
  }
  private String getEventCalendar() {
    String values = getUIFormSelectBoxGroup(FIELD_CALENDAR).getValue() ;
    if(values != null && values.trim().length() > 0 && values.split(CalendarUtils.COLON).length > 0) {
      calType_ = values.split(CalendarUtils.COLON)[0] ;
      return values.split(CalendarUtils.COLON)[1] ;
    }
    return null ;

  }
  public void setSelectedCalendar(String value) {
    value = calType_ + CalendarUtils.COLON + value ;
    getUIFormSelectBoxGroup(FIELD_CALENDAR).setValue(value) ;
  }
  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id) ;
  }
  public void setSelectedCategory(String value) {getUIFormSelectBox(FIELD_CATEGORY).setValue(value) ;}
  
  private String getEventCategory() {return getUIFormSelectBox(FIELD_CATEGORY).getValue() ;}

  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {}
  public void setEvent(boolean isEvent) { isEvent_ = isEvent ; }
  public boolean isEvent() { return isEvent_ ; }

  public void update(String calType, List<SelectItem> options) throws Exception{
    if(options != null) {
      getUIFormSelectBoxGroup(FIELD_CALENDAR).setOptions(options) ;
    }else {
      getUIFormSelectBoxGroup(FIELD_CALENDAR).setOptions(getCalendars()) ;
    } 
    calType_ = calType ;
  }
  
  public boolean canEdit(String[] savePerms) throws Exception{
    return CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), savePerms, CalendarUtils.getCurrentUser()) ;
  }
  
  static  public class SaveActionListener extends EventListener<UIQuickAddEvent> {
    @Override
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      UIQuickAddEvent uiForm = event.getSource() ;
      UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      
      String summary = uiForm.getEventSummary();
      if (summary == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId()
            + ".msg.summary-field-required", null, AbstractApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }
      summary = summary.trim();
      summary = CalendarUtils.enCodeTitle(summary);

      String description = uiForm.getEventDescription() ;
      if(!CalendarUtils.isEmpty(description)) description = description.replaceAll(CalendarUtils.GREATER_THAN, "").replaceAll(CalendarUtils.SMALLER_THAN,"") ;
      if(CalendarUtils.isEmpty(uiForm.getEventCalendar())) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.calendar-field-required", null, AbstractApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return ;
      }
      if(CalendarUtils.isEmpty(uiForm.getEventCategory())) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.category-field-required", null, AbstractApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return ;
      }
      Date from = uiForm.getEventFromDate(uiPortlet.getCalendarSetting().getDateFormat() ,uiPortlet.getCalendarSetting().getTimeFormat()) ;
      Date to = uiForm.getEventToDate(uiPortlet.getCalendarSetting().getDateFormat(), uiPortlet.getCalendarSetting().getTimeFormat()) ;
      if(from == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.fromDate-format", null, AbstractApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return ;
      }
      if(to == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.toDate-format", null, AbstractApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return ;
      }
      if(from.after(to) || from.equals(to)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.logic-required", null, AbstractApplicationMessage.WARNING)) ;
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return ;
      }
      
      CalendarService calService =  CalendarUtils.getCalendarService() ;
      if(calService.isRemoteCalendar(CalendarUtils.getCurrentUser(), uiForm.getEventCalendar())) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, AbstractApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      }
      
      if(uiForm.getIsAllDay()) {
        java.util.Calendar tempCal = CalendarUtils.getInstanceOfCurrentCalendar() ;
        tempCal.setTime(to) ;
        tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
        to = tempCal.getTime() ;
      }
      try {
        CalendarEvent calEvent = new CalendarEvent() ;
        calEvent.setSummary(summary) ;
        calEvent.setDescription(description) ;
        calEvent.setCalendarId(uiForm.getEventCalendar());
        String username = CalendarUtils.getCurrentUser() ;
        if(uiForm.isEvent_){ 
          calEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
          calEvent.setEventState(UIEventForm.ITEM_BUSY) ;
          calEvent.setParticipant(new String[]{username}) ;
          calEvent.setParticipantStatus(new String[] {username + ":"});
          calEvent.setSendOption(uiPortlet.getCalendarSetting().getSendOption());
        } else {
          calEvent.setEventType(CalendarEvent.TYPE_TASK) ;
          calEvent.setEventState(CalendarEvent.NEEDS_ACTION) ;
          calEvent.setTaskDelegator(event.getRequestContext().getRemoteUser());
        }
        uiForm.autoAddReminder(calEvent, from, username);
        calEvent.setEventCategoryId(uiForm.getEventCategory());
        UIFormSelectBox selectBox = (UIFormSelectBox)uiForm.getChildById(FIELD_CATEGORY) ;
        for (SelectItemOption<String> o : selectBox.getOptions()) {
          if (o.getValue().equals(selectBox.getValue())) {
            calEvent.setEventCategoryName(o.getLabel()) ;
            break ;
          }
        }
        
        calEvent.setFromDateTime(from);
        calEvent.setToDateTime(to) ;
        calEvent.setCalType(uiForm.calType_) ;
        
        Calendar calendar = null ;
        if(CalendarUtils.PRIVATE_TYPE.equals(uiForm.calType_)) { 
          calendar = calService.getUserCalendar(username, uiForm.getEventCalendar()) ;
        } else if (CalendarUtils.PUBLIC_TYPE.equals(uiForm.calType_)) {
          calendar = calService.getGroupCalendar(uiForm.getEventCalendar()) ;
        } else if (CalendarUtils.SHARED_TYPE.equals(uiForm.calType_)){
          calendar = calService.getSharedCalendars(username, true).getCalendarById(uiForm.getEventCalendar()) ;
        }
        if (calendar == null) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          uiForm.reset() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UICalendarPortlet.class)) ;
          return ;
        } else {
          if(CalendarUtils.SHARED_TYPE.equals(uiForm.calType_) && !CalendarUtils.canEdit(null, Utils.getEditPerUsers(calendar), username)) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null,1));
            uiForm.reset() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UICalendarPortlet.class)) ;
            return ;
          }
          if(CalendarUtils.PUBLIC_TYPE.equals(uiForm.calType_) && !uiForm.canEdit(calendar.getEditPermission())) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
            uiForm.reset() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UICalendarPortlet.class)) ;
            return ;
          }
        }

        if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
          calService.saveUserEvent(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
          calService.saveEventToSharedCalendar(username, calEvent.getCalendarId(), calEvent, true) ;
        }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
          calService.savePublicEvent(calEvent.getCalendarId(), calEvent, true) ;          
        }

        UICalendarViewContainer uiContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class);
        UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        CalendarView calendarView = (CalendarView)uiContainer.getRenderedChild() ;

        calendarView.setLastUpdatedEventId(calEvent.getId()) ; 
        uiContainer.refresh() ;
        uiForm.reset() ;
        
        UIPopupWindow popupWindow = uiForm.getAncestorOfType(UIPopupWindow.class);
        if (popupWindow != null) {
          popupWindow.setShow(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow) ;
        }
        
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to quick add event to the calendar", e);
        }
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-unsuccessfully", null)) ;
      }
    }
  }
  static  public class MoreDetailActionListener extends EventListener<UIQuickAddEvent> {
    @Override
    public void execute(Event<UIQuickAddEvent> event) throws Exception {
      UIQuickAddEvent uiForm = event.getSource() ;
      UICalendarPortlet porlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      CalendarSetting calendarSetting = porlet.getCalendarSetting() ;
      String dateFormat = calendarSetting.getDateFormat() ;
      String timeFormat = calendarSetting.getTimeFormat() ;
      UIPopupAction uiPopupAction = porlet.getChild(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class, 750) ;
      if(uiForm.isEvent()) {
        uiPopupContainer.setId(UIPopupContainer.UIEVENTPOPUP) ;
        UIEventForm uiEventForm = uiPopupContainer.addChild(UIEventForm.class, null, null) ;
        String calId = uiForm.getEventCalendar();
        uiEventForm.update(uiForm.calType_, uiForm.getUIFormSelectBoxGroup(FIELD_CALENDAR).getOptions()) ;
        uiEventForm.initForm(calendarSetting, null, null) ;
        uiEventForm.setEventSumary(uiForm.getEventSummary()) ;
        uiEventForm.setEventDescription(uiForm.getEventDescription()) ;
        uiEventForm.setEventFromDate(uiForm.getEventFromDate(dateFormat, timeFormat),dateFormat, timeFormat) ;
        Date to = uiForm.getEventToDate(dateFormat, timeFormat) ;
        if(uiForm.getIsAllDay()) {
          java.util.Calendar tempCal = CalendarUtils.getInstanceOfCurrentCalendar() ;
          tempCal.setTime(to) ;
          tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
          to = tempCal.getTime() ;
        }
        uiEventForm.setEventToDate(to,dateFormat, timeFormat) ;
        uiEventForm.setEventAllDate(uiForm.getIsAllDay()) ;
        uiEventForm.setSelectedCategory(uiForm.getEventCategory()) ;
        String username = CalendarUtils.getCurrentUser() ;
        uiEventForm.setSelectedEventState(UIEventForm.ITEM_BUSY) ;
        uiEventForm.setParticipant(username) ;
        uiEventForm.setParticipantStatus(username) ;
        uiEventForm.getChild(UIEventShareTab.class).setParticipantStatusList(uiEventForm.getParticipantStatusList());
        uiEventForm.setEmailAddress(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail()) ;
        uiEventForm.setEmailRemindBefore(String.valueOf(5));
        uiEventForm.setEmailReminder(true) ;
        uiEventForm.setEmailRepeat(false) ;
        uiEventForm.calType_ = uiForm.calType_ ;
        if (calId != null) uiEventForm.setSelectedCalendarId(calId);
      } else {
        uiPopupContainer.setId(UIPopupContainer.UITASKPOPUP) ;
        UITaskForm uiTaskForm = uiPopupContainer.addChild(UITaskForm.class, null, null) ;
        String calId = uiForm.getEventCalendar();
        uiTaskForm.update(uiForm.calType_, uiForm.getUIFormSelectBoxGroup(FIELD_CALENDAR).getOptions()) ;
        uiTaskForm.initForm(calendarSetting, null, null) ;
        String username = CalendarUtils.getCurrentUser() ;
        uiTaskForm.setEmailAddress(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail()) ;
        Date to = uiForm.getEventToDate(dateFormat, timeFormat) ;
        if(uiForm.getIsAllDay()) {
          java.util.Calendar tempCal = CalendarUtils.getInstanceOfCurrentCalendar() ;
          tempCal.setTime(to) ;
          tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
          to = tempCal.getTime() ;
        }
        uiTaskForm.setEventSumary(uiForm.getEventSummary()) ;
        uiTaskForm.setEventDescription(uiForm.getEventDescription()) ;
        uiTaskForm.setEventFromDate(uiForm.getEventFromDate(dateFormat, timeFormat),dateFormat, timeFormat) ;
        uiTaskForm.setEventToDate(to, dateFormat, timeFormat) ;
        uiTaskForm.setEventAllDate(uiForm.getIsAllDay()) ;
        uiTaskForm.setSelectedCategory(uiForm.getEventCategory()) ;
        uiTaskForm.setEmailAddress(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail()) ;
        uiTaskForm.setEmailRemindBefore(String.valueOf(5));
        uiTaskForm.setEmailReminder(true) ;
        uiTaskForm.setEmailRepeat(false) ;
        uiTaskForm.calType_ = uiForm.calType_ ;
        if (calId != null) uiTaskForm.setSelectedCalendarId(calId);
      }

      UIPopupWindow popupWindow = uiForm.getAncestorOfType(UIPopupWindow.class);
      if (popupWindow != null) {
        popupWindow.setShow(false);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIQuickAddEvent> {
    @Override
    public void execute(Event<UIQuickAddEvent> event) throws Exception {      
      UIQuickAddEvent uiQuickAddEvent = event.getSource() ;
      uiQuickAddEvent.reset() ;
      UIPopupWindow popupWindow = uiQuickAddEvent.getAncestorOfType(UIPopupWindow.class);
      if (popupWindow != null) {
        popupWindow.setShow(false);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow) ;
      }
    }
  }
  public void autoAddReminder(CalendarEvent calEvent, Date from, String username) throws Exception{
    String emailAddress = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username).getEmail() ;
    if(CalendarUtils.isEmailValid(emailAddress)) {
      List<Reminder> reminders = new ArrayList<Reminder>() ;
      Reminder email = new Reminder(Reminder.TYPE_EMAIL) ;
      email.setReminderType(Reminder.TYPE_EMAIL) ;
      email.setAlarmBefore(5) ;
      email.setEmailAddress(emailAddress) ;
      email.setRepeate(Boolean.FALSE) ;
      email.setRepeatInterval(0) ;
      email.setFromDateTime(from) ;      
      reminders.add(email) ;
      calEvent.setReminders(reminders) ;
    }
    calEvent.setRepeatType(CalendarEvent.RP_NOREPEAT) ;
    
  }

}
