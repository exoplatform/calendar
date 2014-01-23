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
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.calendar.webui.UIListContainer;
import org.exoplatform.calendar.webui.UIListView;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.calendar.webui.UIPreview;
import org.exoplatform.calendar.webui.popup.UIAddressForm.ContactData;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.ext.UIFormComboBox;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 */
@ComponentConfigs({
  @ComponentConfig(
          lifecycle = UIFormLifecycle.class,
          template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
          events = {
            @EventConfig(listeners = UITaskForm.SaveActionListener.class),
            @EventConfig(listeners = UITaskForm.AddCategoryActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UITaskForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UITaskForm.DownloadAttachmentActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UITaskForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UITaskForm.SelectUserActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UITaskForm.CancelActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UITaskForm.RemoveEmailActionListener.class, phase = Phase.DECODE),
            @EventConfig(listeners = UIFormTabPane.SelectTabActionListener.class, phase = Phase.DECODE)
          }
  ),
  @ComponentConfig(
      id = "UIPopupWindowUserSelectTaskForm",
            type = UIPopupWindow.class,
            template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
            events = {
              @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
              @EventConfig(listeners = UITaskForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
              @EventConfig(listeners = UITaskForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
            }
  )


})
public class UITaskForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  private static final Log log = ExoLogger.getExoLogger(UITaskForm.class);
  
  final public static String TAB_TASKDETAIL = "eventDetail".intern() ;
  final public static String TAB_TASKREMINDER = "eventReminder".intern() ;
  final public static String ITEM_PUBLIC = "public".intern() ;
  final public static String ITEM_PRIVATE = "private".intern() ;
  final public static String ITEM_AVAILABLE = "available".intern() ;
  final public static String ITEM_BUSY = "busy".intern() ;
  final public static String ITEM_REPEAT = "true".intern() ;
  final public static String ITEM_UNREPEAT = "false".intern() ;
  final public static String ACT_REMOVE = "RemoveAttachment".intern() ;
  final public static String ACT_DOWNLOAD = "DownloadAttachment".intern() ;
  final public static String ACT_ADDEMAIL = "AddEmailAddress".intern() ;
  final public static String ACT_ADDCATEGORY = "AddCategory".intern() ;
  final public static String ACT_SELECTUSER = "SelectUser".intern() ;

  public boolean isAddNew_ = true ;
  private CalendarEvent calendarEvent_ = null ;
  private String errorMsg_ = null ;
  private String errorValues = null ;
  protected String calType_ = "0" ;
  
  private String oldCalendarId_ = null ;
  private String newCalendarId_ = null ;
  private Map<String, String> delegators_ = new LinkedHashMap<String, String>() ;

  public static final int LIMIT_FILE_UPLOAD = 10;

  public UITaskForm() throws Exception {
    super("UIEventForm");
    UITaskDetailTab uiTaskDetailTab =  new UITaskDetailTab(TAB_TASKDETAIL) ;
    addChild(uiTaskDetailTab) ;
    UIEventReminderTab eventReminderTab =  new UIEventReminderTab(TAB_TASKREMINDER) ;
    addChild(eventReminderTab) ;
    setSelectedTab(uiTaskDetailTab.getId()) ;
  }
  @Override
  public String getLabel(String id) {
    String label = id ;
    try {
      label = super.getLabel(id) ;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Can not get the label " + id + " from the resource bundle", e);
      }
    }
    return label ;
  }
  @Override
  public void reset() {
    super.reset() ;
    calendarEvent_ = null;
  }

  public void setSelectedEventState(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_STATUS).setValue(value) ;
  }

  public void initForm(CalendarSetting calSetting, CalendarEvent eventCalendar, String formTime) throws Exception {
    reset() ;
    String dateFormat = calSetting.getDateFormat() ;
    String timeFormat = calSetting.getTimeFormat() ;

    UITaskDetailTab taskDetailTab = getChildById(TAB_TASKDETAIL) ;
    ((UIFormDateTimePicker)taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM)).setDateFormatStyle(dateFormat) ;
    ((UIFormDateTimePicker)taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO)).setDateFormatStyle(dateFormat) ;
    List<SelectItemOption<String>> fromTimes 
    = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(), calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    List<SelectItemOption<String>> toTimes 
    = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(), calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_FROM_TIME).setOptions(fromTimes) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_TO_TIME).setOptions(toTimes) ;
    if(eventCalendar != null) {
      oldCalendarId_ = eventCalendar.getCalType() + CalendarUtils.COLON + eventCalendar.getCalendarId();
      isAddNew_ = false ;
      calendarEvent_ = eventCalendar ;
      setEventSumary(eventCalendar.getSummary()) ;
      setEventDescription(eventCalendar.getDescription()) ;
      setEventAllDate(CalendarUtils.isAllDayEvent(eventCalendar)) ;
      setEventFromDate(eventCalendar.getFromDateTime(),dateFormat, timeFormat) ;
      setEventToDate(eventCalendar.getToDateTime(),calSetting.getDateFormat(),  calSetting.getTimeFormat()) ;
      setSelectedCalendarId(eventCalendar.getCalendarId()) ;

      String eventCategoryId = eventCalendar.getEventCategoryId() ;
      if(!CalendarUtils.isEmpty(eventCategoryId)) {
        UIFormSelectBox selectBox = taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY) ;
        boolean hasEventCategory = false ;
        for (SelectItemOption<String> o : selectBox.getOptions()) {
          if (o.getValue().equals(eventCalendar.getEventCategoryId())) {
            hasEventCategory = true ;
            break ;
          }
        }
        if (!hasEventCategory){
          selectBox.getOptions().add(new SelectItemOption<String>(eventCalendar.getEventCategoryName(), eventCalendar.getEventCategoryId())) ;
        }
        selectBox.setValue(eventCalendar.getEventCategoryId()) ;
      }
      StringBuilder delegator = new StringBuilder("") ;
      if (eventCalendar.getTaskDelegator() != null)
        for (String user : eventCalendar.getTaskDelegator().split(CalendarUtils.COMMA))
          if (CalendarUtils.getOrganizationService().getUserHandler().findUserByName(user) != null) {
            if(delegator.length() > 0) delegator.append(CalendarUtils.COMMA) ;
            delegator.append(user) ;
          }
      setEventDelegation(delegator.toString()) ;
      setSelectedEventPriority(eventCalendar.getPriority()) ;
      if(eventCalendar.getReminders() != null)
        setEventReminders(eventCalendar.getReminders()) ;
      setAttachments(eventCalendar.getAttachment()) ;
      setSelectedEventState(eventCalendar.getEventState()) ;
      if(!CalendarUtils.isEmpty(eventCategoryId)) {
        UIFormSelectBox uiSelectBox = taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY) ;
        if(!isAddNew_ && ! String.valueOf(Calendar.TYPE_PRIVATE).equalsIgnoreCase(calType_) ){
          SelectItemOption<String> item = new SelectItemOption<String>(eventCalendar.getEventCategoryName(), eventCategoryId) ;
          uiSelectBox.getOptions().add(item) ;
          uiSelectBox.setValue(eventCategoryId);
          uiSelectBox.setDisabled(true) ;
          taskDetailTab.getUIFormSelectBoxGroup(UITaskDetailTab.FIELD_CALENDAR).setDisabled(true) ;
          taskDetailTab.setActionField(UITaskDetailTab.FIELD_CATEGORY, null) ;
        }
      }
    } else {
      java.util.Calendar cal = UIEventForm.getCalendar(this, formTime, calSetting);
      setEventFromDate(cal.getTime(),dateFormat, timeFormat) ;
      cal.add(java.util.Calendar.MINUTE, (int)calSetting.getTimeInterval()*2) ;
      setEventToDate(cal.getTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setEventDelegation(CalendarUtils.getCurrentUser()) ;
    }
  }

  protected void refreshCategory()throws Exception {
    UIFormInputWithActions taskDetailTab = getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory()) ;
  }
  protected String getStatus() {
    UITaskDetailTab uiTaskDetailTab = getChildById(TAB_TASKDETAIL) ;
    return uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_STATUS).getValue() ;
  }
  protected void setStatus(String value) {
    UITaskDetailTab uiTaskDetailTab = getChildById(TAB_TASKDETAIL) ;
    uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_STATUS).setValue(value) ;
  }
  @Override
  public String[] getActions() {
    return new String[]{"Save", "Cancel"} ;
  }
  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {}

  @Override
  public void updateSelect(String selectField, String value) throws Exception {
    if(value.lastIndexOf("/") > 0) value = value.substring(value.lastIndexOf("/") + 1) ;
    delegators_.put(value, value) ;
    StringBuffer sb = new StringBuffer() ;
    for(String s : delegators_.values()) {
      if(sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
      sb.append(s) ;
    }
    getUIStringInput(selectField).setValue(sb.toString()) ;
  }

  protected boolean isTaskDetailValid(CalendarSetting calendarSetting){
    String dateFormat = calendarSetting.getDateFormat() ;
    String timeFormat = calendarSetting.getTimeFormat() ;
    Date from = null ;
    Date to = null ;

    if(CalendarUtils.isEmpty(getCalendarId())) {
      errorMsg_ = getId() +  ".msg.event-calendar-required" ;
      return false ;
    } 
    if(CalendarUtils.isEmpty(getTaskCategory())) {
      errorMsg_ = getId() +  ".msg.event-category-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getTaskFormDateValue())) {
      errorMsg_ = getId() +  ".msg.event-fromdate-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getTaskToDateValue())){
      errorMsg_ = getId() +  ".msg.event-todate-required" ;
      return false ;
    }
    try {
      from = getTaskFromDate(dateFormat, timeFormat) ;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception when get task of from date", e);
      }
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    try {
      to = getTaskToDate(dateFormat, timeFormat) ;
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception when get task of to date", e);
      }
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    if(from.after(to) || from.equals(to)){
      errorMsg_ = "UIEventForm.msg.event-date-time-logic" ;
      return false ;
    }
    errorMsg_ = null ;
    return true ;
  }

  private boolean isReminderValid() throws Exception {
    if(getEmailReminder()) {
      if(CalendarUtils.isEmpty(getEmailAddress())) {
        errorMsg_ = "UIEventForm.msg.event-email-required" ;
        errorValues = "";
        return false ;
      }
      else if(!CalendarUtils.isValidEmailAddresses(getEmailAddress())) {
        errorMsg_ = "UIEventForm.msg.event-email-invalid" ;
        errorValues = CalendarUtils.invalidEmailAddresses(getEmailAddress()) ;
        return false ;
      } 
    } 
    errorMsg_ = null ;
    return true ;
  }

  protected String getEventSumary() {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_EVENT).getValue() ;
  }
  protected void setEventSumary(String value) {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_EVENT).setValue(value) ;
  }
  protected String getEventDescription() {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormTextAreaInput(UITaskDetailTab.FIELD_DESCRIPTION).getValue() ;
  }
  protected void setEventDescription(String value) {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormTextAreaInput(UITaskDetailTab.FIELD_DESCRIPTION).setValue(value) ;
  }
  protected String getCalendarId() {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    String value = taskDetailTab.getUIFormSelectBoxGroup(UITaskDetailTab.FIELD_CALENDAR).getValue() ;
    if(oldCalendarId_ != null) newCalendarId_ = value ;
    if(value != null && value.trim().length() > 0 && value.split(CalendarUtils.COLON).length > 0) {
      calType_ = value.split(CalendarUtils.COLON)[0] ;
      return value.split(CalendarUtils.COLON)[1] ;
    }
    return null ;
  }
  public void setSelectedCalendarId(String value) {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    String selectedCal = new StringBuffer(calType_).append(CalendarUtils.COLON).append(value).toString();
    taskDetailTab.getUIFormSelectBoxGroup(UITaskDetailTab.FIELD_CALENDAR).setValue(selectedCal) ;
  }

  protected String getTaskCategory() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).getValue() ;
  }
  public void setSelectedCategory(String value) {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setValue(value) ;
  }

  protected Date getTaskFromDate(String dateFormat, String timeFormat) throws Exception {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormComboBox timeField = taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_FROM_TIME) ;
    UIFormDateTimePicker fromField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM) ;
    return getBeginDate(getEventAllDate(), dateFormat, fromField.getValue(), timeFormat, timeField.getValue());
  }
  
  public static Date getBeginDate(boolean isAllDate,String dateFormat, String fromField,String timeFormat,String timeField) throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    if(isAllDate) {
      DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
      return CalendarUtils.getBeginDay(df.parse(fromField)).getTime();
    } 
    DateFormat df = new SimpleDateFormat(dateFormat + Utils.SPACE  + timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    return df.parse(fromField + Utils.SPACE + timeField) ;
  }
  
  protected String getTaskFormDateValue () {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimePicker fromField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM) ;
    return fromField.getValue() ;
  }
  protected void setEventFromDate(Date date,String dateFormat, String timeFormat) throws Exception{
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    ((UIFormDateTimePicker)taskDetailTab.getChildById(UITaskDetailTab.FIELD_FROM))
    .setValue(df.format(date)) ;
    
    df = new SimpleDateFormat(timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_FROM_TIME)
    .setValue(df.format(date)) ;    

  }

  public static Date getToDate(boolean isAllDate,String dateFormat,String toField,String timeFormat,String timeField) throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    if(isAllDate) {
      DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
      df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
      return CalendarUtils.getEndDay(df.parse(toField)).getTime();
    } 
    DateFormat df = new SimpleDateFormat(dateFormat + Utils.SPACE + timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    return df.parse(toField + Utils.SPACE + timeField) ;
  }
  
  protected Date getTaskToDate(String dateFormat, String timeFormat) throws Exception {
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormComboBox timeField = taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_TO_TIME) ;
    UIFormDateTimePicker toField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO) ;
    return getToDate(getEventAllDate(), dateFormat, toField.getValue(), timeFormat, timeField.getValue());
  }
  protected String getTaskToDateValue () {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    UIFormDateTimePicker toField = taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO) ;
    return toField.getValue() ;
  }
  protected void setEventToDate(Date date,String dateFormat,  String timeFormat) throws Exception{
    UITaskDetailTab taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    ((UIFormDateTimePicker)taskDetailTab.getChildById(UITaskDetailTab.FIELD_TO))
    .setValue(df.format(date)) ;
    df = new SimpleDateFormat(timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    taskDetailTab.getUIFormComboBox(UITaskDetailTab.FIELD_TO_TIME)
    .setValue(df.format(date)) ; 
  }

  protected boolean getEventAllDate() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUICheckBoxInput(UITaskDetailTab.FIELD_CHECKALL).isChecked() ;
  }
  protected void setEventAllDate(boolean isCheckAll) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUICheckBoxInput(UITaskDetailTab.FIELD_CHECKALL).setChecked(isCheckAll) ;
  }
  protected String getEventDelegation() throws Exception {
    delegators_.clear() ;
    String values = getEventDelegationValue() ;
    StringBuffer sb = new StringBuffer() ;
    if(!CalendarUtils.isEmpty(values)) {
      for(String s : values.split(CalendarUtils.COMMA)) {
        s = s.trim() ;
        delegators_.put(s.trim(),s.trim()) ; 
      }
      for(String s : delegators_.values()) {
        if(!CalendarUtils.isEmpty(s)) {
          if(sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          sb.append(s) ;
        }
      }
      return sb.toString() ; 
    } else {
      return null ;
    } 
  }
  protected String[] getEventDelegationAll() {
    delegators_.clear() ;
    String values = getEventDelegationValue() ;
    if(!CalendarUtils.isEmpty(values)) {
      for(String s : values.split(CalendarUtils.COMMA)) {
        s = s.trim() ;
        delegators_.put(s.trim(),s.trim()) ; 
      }
      return delegators_.values().toArray(new String[delegators_.values().size()]) ;
    } else {
      return null ;
    } 
  }
  protected String getEventDelegationValue() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_DELEGATION).getValue();
  }
  protected void setEventDelegation(String value) {
    if(!CalendarUtils.isEmpty(value)) {
      for(String s : value.split(CalendarUtils.COMMA)) {
        s = s.trim() ;
        delegators_.put(s, s) ;
      }
    } else {
      delegators_.clear() ;
    }
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKDETAIL) ;
    taskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_DELEGATION).setValue(value) ;
  }

  protected boolean getEmailReminder() {
    UIEventReminderTab taskReminderTab =  getChildById(TAB_TASKREMINDER) ;
    return taskReminderTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).isChecked() ;
  }
  protected void setEmailReminder(boolean isChecked) {
    UIEventReminderTab taskReminderTab =  getChildById(TAB_TASKREMINDER) ;
    taskReminderTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).setChecked(isChecked) ;
  }
  protected String getEmailRemindBefore() {
    UIEventReminderTab taskReminderTab =  getChildById(TAB_TASKREMINDER) ;
    return taskReminderTab.getUIStringInput(UIEventReminderTab.EMAIL_REMIND_BEFORE).getValue() ;
  }
  protected boolean isEmailRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    return Boolean.parseBoolean(eventReminderTab.getUICheckBoxInput(UIEventReminderTab.EMAIL_IS_REPEAT).getValue().toString()) ;
  }

  public void setEmailRepeat(Boolean value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    eventReminderTab.getUICheckBoxInput(UIEventReminderTab.EMAIL_IS_REPEAT).setChecked(value) ;
  }
  
  protected String getEmailRepeatInterVal() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).getValue() ;
  }
  protected void setEmailReminderBefore(String value) {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIStringInput(UIEventReminderTab.EMAIL_REMIND_BEFORE).setValue(value) ;
  }

  protected String getEmailAddress() {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).getValue() ;
  }

  public void setEmailAddress(String value) {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).setValue(value) ;
  }

  protected boolean getPopupReminder() {
    UIEventReminderTab taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return taskDetailTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).isChecked() ;
  }
  protected void setPopupReminder(boolean isChecked) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).setChecked(isChecked) ;
  }
  protected String getPopupReminderTime() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return taskDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REMIND_BEFORE).getValue() ;
  }
  
  protected Boolean isPopupRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    return Boolean.parseBoolean(eventReminderTab.getUICheckBoxInput(UIEventReminderTab.POPUP_IS_REPEAT).getValue().toString()) ;
  }
  protected void setPopupRepeat(Boolean value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    eventReminderTab.getUICheckBoxInput(UIEventReminderTab.POPUP_IS_REPEAT).setChecked(value) ;
  }

  protected String getPopupRepeatInterVal() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKREMINDER) ;
    return eventDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
  }
  protected void setPopupReminderTime(String value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIStringInput(UIEventReminderTab.POPUP_REMIND_BEFORE).setValue(value) ;
  }
  protected long getPopupReminderSnooze() {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    try {
      String time =  taskDetailTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
      return Long.parseLong(time) ;
    } catch (Exception e){
      if (log.isDebugEnabled()) {
        log.debug("Fail to get time from POPUP_REPEAT_INTERVAL", e);
      }
    }
    return 0 ;
  }
  protected void setPopupReminderSnooze(long value) {
    UIFormInputWithActions taskDetailTab =  getChildById(TAB_TASKREMINDER) ;
    taskDetailTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
  protected List<Attachment>  getAttachments(String eventId, boolean isAddNew) {
    UITaskDetailTab taskDetailTab = getChild(UITaskDetailTab.class) ;
    return taskDetailTab.getAttachments() ;
  }
  protected void setAttachments(List<Attachment> attachment) throws Exception {
    UITaskDetailTab taskDetailTab = getChild(UITaskDetailTab.class) ;
    taskDetailTab.setAttachments(attachment) ;
    taskDetailTab.refreshUploadFileList() ;
  }
  protected void setEventReminders(List<Reminder> reminders){
    for(Reminder rm : reminders) {
      if(Reminder.TYPE_EMAIL.equals(rm.getReminderType())) {
        setEmailReminder(true) ;
        setEmailAddress(rm.getEmailAddress()) ;
        setEmailRepeat(rm.isRepeat()) ;
        setEmailRemindBefore(String.valueOf(rm.getAlarmBefore())) ;
        setEmailRepeatInterVal(rm.getRepeatInterval()) ;
      } else if(Reminder.TYPE_POPUP.equals(rm.getReminderType())) {
        setPopupReminder(true) ;  
        setPopupRepeat(rm.isRepeat()) ;
        setPopupRemindBefore(String.valueOf(rm.getAlarmBefore()));
        setPopupRepeatInterval(rm.getRepeatInterval()) ;
      }  
    }
  }

  public boolean isReminderByEmail(List<Reminder> reminders){
    for(Reminder rm : reminders) {
      return (Reminder.TYPE_EMAIL.equals(rm.getReminderType()));
    }
    return false;
  }
  
  protected List<Reminder>  getEventReminders(Date fromDateTime) throws Exception {
    List<Reminder> reminders = new ArrayList<Reminder>() ;
    if(getEmailReminder()) { 
      Reminder email = new Reminder() ;
      email.setReminderType(Reminder.TYPE_EMAIL) ;
      email.setAlarmBefore(Long.parseLong(getEmailRemindBefore())) ;
      email.setEmailAddress(getEmailAddress()) ;
      email.setRepeate(isEmailRepeat()) ;
      email.setRepeatInterval(Long.parseLong(getEmailRepeatInterVal())) ;
      email.setFromDateTime(fromDateTime) ;
      email.setReminderOwner(CalendarUtils.getCurrentUser()) ;
      reminders.add(email) ;
    }
    if(getPopupReminder()) {
      Reminder popup = new Reminder() ;
      popup.setReminderType(Reminder.TYPE_POPUP) ;
      popup.setAlarmBefore(Long.parseLong(getPopupReminderTime())) ;
      popup.setRepeate(isPopupRepeat()) ;
      popup.setRepeatInterval(Long.parseLong(getPopupRepeatInterVal())) ;
      popup.setFromDateTime(fromDateTime) ;
      StringBuffer sb = new StringBuffer() ;
      boolean isExist = false ;
      if(getEventDelegationAll() != null) {
        for(String s : getEventDelegationAll()) {
          if(s.equals(CalendarUtils.getCurrentUser())) {
            isExist = true ;
            break ;
          }
        }
        for(String s : getEventDelegationAll()) {
          if(sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          sb.append(s) ;
        }
      }
      if(!isExist) {
        if(sb.length() >0) sb.append(CalendarUtils.COMMA);
        sb.append(CalendarUtils.getCurrentUser()) ;
      }
      popup.setReminderOwner(sb.toString()) ;
      reminders.add(popup) ;
    }
    return reminders ;
  }

  protected String getEventPriority() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_PRIORITY).getValue() ;
  }
  protected void setSelectedEventPriority(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_TASKDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_PRIORITY).setValue(value) ;
  }

  private boolean setCalendarOptionOfSpaceAsSelected(String spaceId, List<? extends SelectItem> items, UIFormSelectBoxWithGroups selectBoxWithGroups) {
    if (spaceId == null || items == null) {
      return false;
    }
    for (SelectItem si : items) {
      if (si instanceof SelectOption) {
        SelectOption so = (SelectOption) si;
        // find Calendar option of space of which value end with space id.
        if (so.getValue().endsWith(spaceId)) {
          selectBoxWithGroups.setValue(so.getValue());
          return true;
        }
      } else if (si instanceof SelectOptionGroup) {
        if (setCalendarOptionOfSpaceAsSelected(spaceId, ((SelectOptionGroup) si).getOptions(), selectBoxWithGroups)) {
          return true;
        }
      }
    }
    return false;
  }
  
  public void update(String calType, List<SelectItem> options) throws Exception{
    UITaskDetailTab uiTaskDetailTab = getChildById(TAB_TASKDETAIL) ;
    UIFormSelectBoxWithGroups selectBoxWithGroups = uiTaskDetailTab.getUIFormSelectBoxGroup(UITaskDetailTab.FIELD_CALENDAR);
    if(options != null) {
      selectBoxWithGroups.setOptions(options) ;
    }else {
      selectBoxWithGroups.setOptions(getCalendars()) ;
    }

    UICalendarPortlet uiCalendarPortlet = getAncestorOfType(UICalendarPortlet.class);
    String spaceId = uiCalendarPortlet != null ? uiCalendarPortlet.getSpaceGroupId()
        : UICalendarPortlet.getGroupIdOfSpace();
    if (!spaceId.equals("")) {
      setCalendarOptionOfSpaceAsSelected(spaceId, selectBoxWithGroups.getOptions(), selectBoxWithGroups);
    }
    
    calType_ = calType ;
  }

  private List<SelectItem> getCalendars() throws Exception {
    return CalendarUtils.getCalendarOption() ;
  }
  protected long getTotalAttachment() {
    UITaskDetailTab uiTaskDetailTab = getChild(UITaskDetailTab.class) ;
    long attSize = 0 ; 
    for(Attachment att : uiTaskDetailTab.getAttachments()) {
      attSize = attSize + att.getSize() ;
    }
    return attSize ;
  }

  public Attachment getAttachment(String attId) {
    UITaskDetailTab uiDetailTab = getChildById(TAB_TASKDETAIL) ;
    for (Attachment att : uiDetailTab.getAttachments()) {
      if(att.getId().equals(attId)) {
        return att ;
      }
    }
    return null;
  }
  
  public static void showAddressForm(UIAddressForm uiAddressForm, String oldAddress) throws Exception {
    List<ContactData> contacts = uiAddressForm.getContactList() ;
    if(!CalendarUtils.isEmpty(oldAddress)) {
      for(String address : oldAddress.split(",")) {
        for(ContactData c : contacts){
          if(!CalendarUtils.isEmpty(c.getEmail())) {
            if(Arrays.asList(c.getEmail().split(";")).contains(address.trim())) {
              if (!uiAddressForm.checkedList_.contains(c.getId())) uiAddressForm.checkedList_.add(c.getId()) ;
            }
          }
        }
      }
    }
  }


  public static void updateListView(CalendarView calendarView,CalendarEvent calendarEvent,CalendarService calService,String username) throws Exception {
    if(calendarView instanceof UIListContainer) {
      UIListContainer uiListContainer = (UIListContainer)calendarView ;
      if (uiListContainer.isDisplaySearchResult() && calendarEvent.getAttachment() != null) {
        UIPreview uiPreview = uiListContainer.getChild(UIPreview.class) ;
        EventQuery eventQuery = new EventQuery() ;
        eventQuery.setCalendarId(new String[] {calendarEvent.getCalendarId()}) ;
        eventQuery.setEventType(calendarEvent.getEventType()) ;
        eventQuery.setCategoryId(new String[] {calendarEvent.getEventCategoryId()}) ;

        UIListView listView = uiListContainer.getChild(UIListView.class) ;
        List<CalendarEvent> list = calService. getEvents(username, eventQuery, listView.getPublicCalendars()) ;
        for (CalendarEvent ev : list) {
          if (ev.getId().equals(calendarEvent.getId())) {
            if (listView.getDataMap().containsKey(ev.getId())) {
              listView.getDataMap().put(ev.getId(), ev) ;
              if (uiPreview.getEvent().getId().equals(ev.getId())) {
                uiPreview.setEvent(ev) ; 
              }
            }                        
            break ;
          }
        }                    
      }
    }
  }
  
  
  static  public class DownloadAttachmentActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIEventForm.downloadAtt(event, uiForm, false);
    }
  }
  static  public class AddCategoryActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      UIEventCategoryManager uiCategoryMan = uiChildPopup.activate(UIEventCategoryManager.class, 470) ;
      uiForm.setSelectedTab(TAB_TASKDETAIL) ;
      uiCategoryMan.categoryId_ = uiForm.getTaskCategory() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static  public class AddAttachmentActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UITaskDetailTab detailTab = uiForm.getChild(UITaskDetailTab.class);
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      UIAttachFileForm uiAttachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      uiAttachFileForm.setAttSize(uiForm.getTotalAttachment()) ;
      uiAttachFileForm.setLimitNumberOfFiles(LIMIT_FILE_UPLOAD - detailTab.getAttachments().size());
      uiAttachFileForm.init();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class RemoveAttachmentActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      if(uiContainer != null) uiContainer.deActivate() ;
      UITaskDetailTab uiTaskDetailTab = uiForm.getChild(UITaskDetailTab.class) ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      Attachment attachfile = new Attachment();
      for (Attachment att : uiTaskDetailTab.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = att;
        }
      }
      uiTaskDetailTab.removeFromUploadFileList(attachfile);
      uiTaskDetailTab.refreshUploadFileList() ;
      uiForm.setSelectedTab(TAB_TASKDETAIL) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class AddCalendarActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
    }
  }

  static  public class SelectUserActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      String value = uiForm.getEventDelegation() ;
      uiForm.setEventDelegation(value) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupWindow uiPopupWindow = uiPopupContainer.getChild(UIPopupWindow.class) ;
        if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelectTaskForm", "UIPopupWindowUserSelectTaskForm") ;
        UIUserSelector uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelector.class, null, null) ;
        uiUserSelector.setShowSearch(true);
        uiUserSelector.setShowSearchUser(true) ;
        uiUserSelector.setShowSearchGroup(true);
        uiPopupWindow.setUIComponent(uiUserSelector);
        uiPopupWindow.setShow(true);
        uiPopupWindow.setWindowSize(740, 400) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;  
    }
  }
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      UITaskForm uiTaskForm = uiContainer.getChild(UITaskForm.class);
      UITaskDetailTab uiTaskDetailTab  = uiTaskForm.getChild(UITaskDetailTab.class); 
      UIFormStringInput uiInput = uiTaskDetailTab.getUIStringInput(UITaskDetailTab.FIELD_DELEGATION);
      String currentValues = uiInput.getValue();
      String values = uiUserSelector.getSelectedUsers();
      if(!CalendarUtils.isEmpty(currentValues) && !currentValues.equals("null")) values += ","+ currentValues;
      values = CalendarUtils.cleanValue(values);
      uiInput.setValue(values);
      //close popup
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUseSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUseSelector.getParent() ;
      UIPopupContainer uiContainer = uiPoupPopupWindow.getAncestorOfType(UIPopupContainer.class) ;
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }

  static  public class SaveActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarService calService = CalendarUtils.getCalendarService();
      if(uiForm.isTaskDetailValid(calendarPortlet.getCalendarSetting())) {
        String username = CalendarUtils.getCurrentUser() ;
        String calendarId = uiForm.getCalendarId() ;
        String summary = uiForm.getEventSumary().trim() ;
        summary = CalendarUtils.enCodeTitle(summary);
        String description = uiForm.getEventDescription() ;
        if(!CalendarUtils.isEmpty(description)) description = description.replaceAll(CalendarUtils.GREATER_THAN, "").replaceAll(CalendarUtils.SMALLER_THAN,"") ;
        CalendarEvent calendarEvent = null ;
        if(uiForm.isAddNew_){
          calendarEvent = new CalendarEvent() ; 
          calendarEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        } else {
          calendarEvent = uiForm.calendarEvent_ ;
        }
        calendarEvent.setEventType(CalendarEvent.TYPE_TASK) ;
        calendarEvent.setSummary(summary) ;
        calendarEvent.setDescription(description) ;
        String delegation = uiForm.getEventDelegationValue() ;
        if(!CalendarUtils.isEmpty(delegation)) {
          OrganizationService orgService = CalendarUtils.getOrganizationService() ;
          for(String s : delegation.split(CalendarUtils.COMMA)) {
            s = s.trim() ;
            if(!CalendarUtils.isEmpty(s))
              if(orgService.getUserHandler().findUserByName(s) == null) {
                event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIEventForm.msg.name-not-correct", new Object[]{s}, AbstractApplicationMessage.WARNING)) ;
                return ;
              }  
          }
        }
        calendarEvent.setTaskDelegator(uiForm.getEventDelegation()) ;
        Date from = uiForm.getTaskFromDate(calendarPortlet.getCalendarSetting().getDateFormat(), calendarPortlet.getCalendarSetting().getTimeFormat()) ;
        Date to = uiForm.getTaskToDate(calendarPortlet.getCalendarSetting().getDateFormat(), calendarPortlet.getCalendarSetting().getTimeFormat()) ;
        if(from.after(to)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.event-date-time-logic", null, AbstractApplicationMessage.WARNING)) ;
          return ;
        } else if(from.equals(to)) {
          to = CalendarUtils.getEndDay(from).getTime() ;
        } 
        if(uiForm.getEventAllDate()) {
          java.util.Calendar tempCal = CalendarUtils.getInstanceOfCurrentCalendar() ;
          tempCal.setTime(to) ;
          tempCal.add(java.util.Calendar.MILLISECOND, -1) ;
          to = tempCal.getTime() ;
        }
        Calendar currentCalendar = CalendarUtils.getCalendar(uiForm.calType_, calendarId);
        if(currentCalendar == null) {
          uiPopupAction.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1));
          return ;
        } else {
          boolean canEdit = false ;          
          if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)) {
            canEdit = CalendarUtils.canEdit(null, Utils.getEditPerUsers(currentCalendar), username) ;
          } else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)) {
            canEdit = CalendarUtils.canEdit(CalendarUtils.getOrganizationService(),currentCalendar.getEditPermission(), username) ;
          }
          if(!canEdit && !uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE) ) {
            uiPopupAction.deActivate() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1));
            return ;
          }
        }
        if(!uiForm.isReminderValid()) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.errorMsg_, new String[] {uiForm.errorValues }, AbstractApplicationMessage.WARNING));
          uiForm.setSelectedTab(TAB_TASKREMINDER) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
          
          return ;
        }
        calendarEvent.setCalType(uiForm.calType_) ;
        calendarEvent.setFromDateTime(from) ;
        calendarEvent.setToDateTime(to);
        calendarEvent.setCalendarId(calendarId) ;
        calendarEvent.setEventCategoryId(uiForm.getTaskCategory()) ;
        UIFormInputWithActions taskDetailTab =  uiForm.getChildById(TAB_TASKDETAIL) ;
        UIFormSelectBox uiSelectBox = taskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY) ;
        for (SelectItemOption<String> o : uiSelectBox.getOptions()) {
          if (o.getValue().equals(uiSelectBox.getValue())) {
            calendarEvent.setEventCategoryName(o.getLabel()) ;
            break ;
          }
        }  

        calendarEvent.setEventState(uiForm.getStatus()) ;
        calendarEvent.setPriority(uiForm.getEventPriority()) ; 
        calendarEvent.setAttachment(uiForm.getAttachments(calendarEvent.getId(), uiForm.isAddNew_)) ;
        calendarEvent.setReminders(uiForm.getEventReminders(from)) ;
        try {
          if(uiForm.isAddNew_){
            if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
              CalendarUtils.getCalendarService().saveUserEvent(username, calendarId, calendarEvent, uiForm.isAddNew_) ;
            }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
              CalendarUtils.getCalendarService().saveEventToSharedCalendar(username, calendarId, calendarEvent, uiForm.isAddNew_) ;
            }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
              CalendarUtils.getCalendarService().savePublicEvent(calendarId, calendarEvent, uiForm.isAddNew_) ;          
            }
          } else {
            String fromCal = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
            String toCal = uiForm.newCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
            String fromType = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
            String toType = uiForm.newCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
            List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
            listEvent.add(calendarEvent) ;
            calService.moveEvent(fromCal, toCal, fromType, toType, listEvent, username) ;
            CalendarView calendarView = (CalendarView)uiViewContainer.getRenderedChild() ;
            updateListView(calendarView, calendarEvent, calService, username);
          }
          CalendarView calendarView = (CalendarView)uiViewContainer.getRenderedChild() ;
          uiViewContainer.refresh() ;
          calendarView.setLastUpdatedEventId(calendarEvent.getId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
          UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;

          uiPopupAction.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        }catch (Exception e) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.add-event-error", null));
          if (log.isDebugEnabled()) {
            log.debug("Can not save the task", e);
          }
        }
      } else {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.errorMsg_, null));
        uiForm.setSelectedTab(TAB_TASKDETAIL) ;
      }
      
      UITaskDetailTab uiDetailTab = uiForm.getChildById(TAB_TASKDETAIL) ;
      for (Attachment att : uiDetailTab.getAttachments()) {
        UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class), att.getResourceId()) ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      UITaskDetailTab uiDetailTab = uiForm.getChildById(TAB_TASKDETAIL) ;
      for (Attachment att : uiDetailTab.getAttachments()) {
        UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class), att.getResourceId()) ;
      }
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class RemoveEmailActionListener extends EventListener<UITaskForm> {
    @Override
    public void execute(Event<UITaskForm> event) throws Exception {
      UITaskForm uiForm = event.getSource() ;
      uiForm.setEmailAddress(uiForm.getEmailAddress());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChild(UIEventReminderTab.class)) ;
    }
  }
  public void setEmailRemindBefore(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).setValue(value) ;
  }
  protected void setEmailRepeatInterVal(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
  protected void setPopupRemindBefore(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REMIND_BEFORE).setValue(value) ;
  }
  protected void setPopupRepeatInterval(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_TASKREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
}

