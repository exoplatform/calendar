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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
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
import org.exoplatform.calendar.webui.UIListContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
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
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.ext.UIFormComboBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.organization.account.UIUserSelector;
import sun.util.calendar.Gregorian;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Editor : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfigs ( {
  @ComponentConfig(
                   lifecycle = UIFormLifecycle.class,
                   template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
                   events = {
                     @EventConfig(listeners = UIEventForm.SaveActionListener.class),
                     @EventConfig(listeners = UIEventForm.AddCategoryActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.RemoveEmailActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.MoveNextActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.MovePreviousActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.DeleteUserActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.DownloadAttachmentActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.AddParticipantActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.AddUserActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.OnChangeActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.CancelActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIFormTabPane.SelectTabActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmOKActionListener.class, name = "ConfirmOK", phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmCancelActionListener.class, name = "ConfirmCancel", phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmUpdateOnlyInstance.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmUpdateFollowSeries.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmUpdateAllSeries.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.ConfirmUpdateCancel.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventForm.EditRepeatActionListener.class, phase = Phase.DECODE)
                   }
      )
  ,
  @ComponentConfig(
                   id = "UIPopupWindowAddUserEventForm",
                   type = UIPopupWindow.class,
                   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
                   events = {
                     @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
                     @EventConfig(listeners = UIEventForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
                     @EventConfig(listeners = UITaskForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
                   }
      )
}
    )
public class UIEventForm extends UIFormTabPane implements UIPopupComponent, UISelector{

  private static final Log LOG = ExoLogger.getExoLogger(UIEventForm.class);

  final public static String TAB_EVENTDETAIL = "eventDetail".intern() ;
  final public static String TAB_EVENTREMINDER = "eventReminder".intern() ;
  final public static String TAB_EVENTSHARE = "eventShare".intern() ;
  final public static String TAB_EVENTATTENDER = "eventAttender".intern() ;


  final public static String FIELD_MEETING = "participant".intern() ;
  final public static String FIELD_ISSENDMAIL = "isSendMail".intern() ;

  final public static String ITEM_PUBLIC = "public".intern() ;
  final public static String ITEM_PRIVATE = "private".intern() ;
  final public static String ITEM_AVAILABLE = "available".intern() ;
  final public static String ITEM_BUSY = "busy".intern() ;
  final public static String ITEM_OUTSIDE = "outside".intern() ;

  final public static String ITEM_REPEAT = "true".intern() ;
  final public static String ITEM_UNREPEAT = "false".intern() ;

  final public static String ITEM_ALWAYS = "always".intern();
  final public static String ITEM_NERVER = "never".intern();
  final public static String ITEM_ASK = "ask".intern();

  final public static String ACT_REMOVE = "RemoveAttachment".intern() ;
  final public static String ACT_DOWNLOAD = "DownloadAttachment".intern() ;
  final public static String ACT_ADDEMAIL = "AddEmailAddress".intern() ;
  final public static String ACT_ADDCATEGORY = "AddCategory".intern() ;
  final public static String ACT_EDITREPEAT = "EditRepeat".intern();
  final public static String STATUS_EMPTY = "".intern();
  final public static String STATUS_PENDING = "pending".intern();
  final public static String STATUS_YES = "yes".intern();
  final public static String STATUS_NO = "no".intern();
  public final static String RP_END_BYDATE = "endByDate";
  public final static String RP_END_AFTER = "endAfter";
  public final static String RP_END_NEVER = "neverEnd";

  public boolean isAddNew_ = true ;
  private boolean isChangedSignificantly = false;
  private CalendarEvent calendarEvent_ = null ;
  protected String calType_ = "0" ;
  protected String invitationMsg_ = "" ;
  /* participant list contains both eXo userId and email addresses */
  protected String participantList_ = "" ;
  private String errorMsg_ = null ;
  private String errorValues = null ;
  protected Map<String, String> participants_ = new LinkedHashMap<String, String>() ;
  protected Map<String, String> participantStatus_ = new LinkedHashMap<String, String>() ;
  protected LinkedList<ParticipantStatus> participantStatusList_ = new LinkedList<ParticipantStatus>();
  private String oldCalendarId_ = null ;
  private String newCalendarId_ = null ;
  private String saveEventInvitation = "";
  private String saveEventNoInvitation = "";
  private CalendarEvent repeatEvent;
  private String repeatSummary;

  public static final int LIMIT_FILE_UPLOAD = 10;

  public UIEventForm() throws Exception {
    super("UIEventForm");
    this.setId("UIEventForm");
    saveEventInvitation = "SaveEvent-Invitation" ;
    saveEventNoInvitation = "SaveEvent-NoSendInvitation" ;
    try{
      saveEventInvitation = getLabel("SaveEvent-Invitation") ;
      saveEventNoInvitation = getLabel("SaveEvent-NoSendInvitation") ;
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Fail to get label: " + saveEventInvitation, e);
        LOG.debug("Fail to get label: " + saveEventNoInvitation, e);
      }
    }
    UIEventDetailTab eventDetailTab =  new UIEventDetailTab(TAB_EVENTDETAIL) ;
    addChild(eventDetailTab) ;
    UIEventReminderTab eventReminderTab =  new UIEventReminderTab(TAB_EVENTREMINDER) ;
    addChild(eventReminderTab) ;
    UIEventShareTab eventShareTab =  new UIEventShareTab(TAB_EVENTSHARE) ;
    eventShareTab.addUIFormInput(new UIFormRadioBoxInput(UIEventShareTab.FIELD_SHARE, UIEventShareTab.FIELD_SHARE, getShareValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormRadioBoxInput(UIEventShareTab.FIELD_STATUS, UIEventShareTab.FIELD_STATUS, getStatusValue()) ) ;
    eventShareTab.addUIFormInput(new UIFormRadioBoxInput(UIEventShareTab.FIELD_SEND, UIEventShareTab.FIELD_SEND, CalendarUtils.getSendValue(null)) ) ;
    eventShareTab.addUIFormInput(new UIFormInputInfo(UIEventShareTab.FIELD_INFO, UIEventShareTab.FIELD_INFO, null) ) ;
    eventShareTab.addUIFormInput(new UIFormTextAreaInput(FIELD_MEETING, FIELD_MEETING, null)) ;

    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData addUser = new ActionData() ;
    addUser.setActionListener("AddParticipant") ;
    addUser.setActionName("AddUser") ;
    addUser.setActionParameter(TAB_EVENTSHARE);
    addUser.setActionType(ActionData.TYPE_ICON) ;
    addUser.setCssIconClass("uiIconPlus uiIconLightGray") ;
    actions.add(addUser) ;
    eventShareTab.setActionField(UIEventShareTab.FIELD_INFO, actions) ;

    addChild(eventShareTab) ;
    UIEventAttenderTab eventAttenderTab = new UIEventAttenderTab(TAB_EVENTATTENDER) ;
    addChild(eventAttenderTab) ;
    setSelectedTab(eventDetailTab.getId()) ;
  }
  @Override
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      LOG.warn("Can not find " + getId() + ".label." + id);
      return id ;
    }
  }

  public void initForm(CalendarSetting calSetting, CalendarEvent eventCalendar, String formTime) throws Exception {
    reset() ;
    UIEventDetailTab eventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    ((UIFormDateTimePicker)eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM)).setDateFormatStyle(calSetting.getDateFormat()) ;
    ((UIFormDateTimePicker)eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO)).setDateFormatStyle(calSetting.getDateFormat()) ;
    UIEventAttenderTab attenderTab = getChildById(TAB_EVENTATTENDER) ;
    List<SelectItemOption<String>> fromTimes 
    = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    List<SelectItemOption<String>> toTimes 
    = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat(), calSetting.getTimeInterval()) ;
    eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_FROM_TIME).setOptions(fromTimes) ;
    eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_TO_TIME).setOptions(toTimes) ;
    List<SelectItemOption<String>> fromOptions = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat()) ;
    List<SelectItemOption<String>> toOptions = CalendarUtils.getTimesSelectBoxOptions(calSetting.getTimeFormat(),calSetting.getTimeFormat()) ;
    attenderTab.getUIFormComboBox(UIEventAttenderTab.FIELD_FROM_TIME).setOptions(fromOptions) ;
    attenderTab.getUIFormComboBox(UIEventAttenderTab.FIELD_TO_TIME).setOptions(toOptions) ;
    if(eventCalendar != null) {
      isAddNew_ = false ;
      calendarEvent_ = eventCalendar ;
      repeatEvent = new CalendarEvent(calendarEvent_);
      setEventSumary(eventCalendar.getSummary()) ;
      setEventDescription(eventCalendar.getDescription()) ;
      setEventAllDate(CalendarUtils.isAllDayEvent(eventCalendar)) ;
      setEventFromDate(eventCalendar.getFromDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setEventCheckTime(eventCalendar.getFromDateTime()) ;
      setEventToDate(eventCalendar.getToDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setSelectedCalendarId(eventCalendar.getCalendarId()) ;

      String eventCategoryId = eventCalendar.getEventCategoryId() ;
      if(!CalendarUtils.isEmpty(eventCategoryId)) {
        UIFormSelectBox selectBox = eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY) ;
        boolean hasEventCategory = false ;
        for (SelectItemOption<String> o : selectBox.getOptions()) {
          if (o.getValue().equals(eventCategoryId)) {
            hasEventCategory = true ;
            break ;
          }
        }
        if (!hasEventCategory){
          selectBox.getOptions().add(new SelectItemOption<String>(eventCalendar.getEventCategoryName(), eventCategoryId)) ;
        }
        setSelectedCategory(eventCategoryId) ;
      }
      setEventPlace(eventCalendar.getLocation()) ;
      setEventIsRepeat(eventCalendar.getRepeatType() != null && !CalendarEvent.RP_NOREPEAT.equals(eventCalendar.getRepeatType()));
      setRepeatSummary(buildRepeatSummary(calendarEvent_));
      // if it's exception occurrence, disabled repeat checkbox, it means you can't convert a exception occurrence to a repeating event 
      if ((CalendarEvent.RP_NOREPEAT.equals(calendarEvent_.getRepeatType()) || calendarEvent_.getRepeatType() == null) && !CalendarUtils.isEmpty(calendarEvent_.getRecurrenceId())
          && calendarEvent_.getIsExceptionOccurrence()) {
        getChild(UIEventDetailTab.class).getUICheckBoxInput(UIEventDetailTab.FIELD_ISREPEAT).setDisabled(true);
      }

      setSelectedEventPriority(eventCalendar.getPriority()) ;
      if(eventCalendar.getReminders() != null)
        setEventReminders(eventCalendar.getReminders()) ;
      setAttachments(eventCalendar.getAttachment()) ;
      if(eventCalendar.isPrivate()) {
        setSelectedShareType(UIEventForm.ITEM_PRIVATE) ;
      } else {
        setSelectedShareType(UIEventForm.ITEM_PUBLIC) ;
      }
      setSendOption(eventCalendar.getSendOption());
      setMessage(eventCalendar.getMessage());
      setParticipantStatusValues(eventCalendar.getParticipantStatus());
      getChild(UIEventShareTab.class).setParticipantStatusList(participantStatusList_);

      setSelectedEventState(eventCalendar.getEventState()) ;
      setMeetingInvitation(eventCalendar.getInvitation()) ;
      StringBuffer pars = new StringBuffer() ;
      if(eventCalendar.getParticipant() != null) {
        for(String par : eventCalendar.getParticipant()) {
          if(!CalendarUtils.isEmpty(pars.toString())) pars.append(CalendarUtils.BREAK_LINE) ;
          pars.append(par) ;
        }
      }
      setParticipant(pars.toString()) ;

      if(eventCategoryId != null) {
        UIFormSelectBox uiSelectBox = eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY) ;
        if(!isAddNew_ && ! String.valueOf(Calendar.TYPE_PRIVATE).equalsIgnoreCase(calType_) ){
          SelectItemOption<String> item = new SelectItemOption<String>(eventCalendar.getEventCategoryName(), eventCalendar.getEventCategoryId()) ;
          uiSelectBox.getOptions().add(item) ;
          uiSelectBox.setValue(eventCalendar.getEventCategoryId());
          uiSelectBox.setDisabled(true) ;
          eventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).setDisabled(true) ;
          eventDetailTab.setActionField(UIEventDetailTab.FIELD_CATEGORY, null) ;
        }
      }      
      attenderTab.calendar_.setTime(eventCalendar.getFromDateTime()) ;
    } else {
      java.util.Calendar cal = getCalendar(this, formTime, calSetting);
      setEventFromDate(cal.getTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      cal.add(java.util.Calendar.MINUTE, (int)calSetting.getTimeInterval()*2) ;
      setEventCheckTime(cal.getTime()) ;
      setEventToDate(cal.getTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      StringBuffer pars = new StringBuffer(CalendarUtils.getCurrentUser()) ;
      setMeetingInvitation(new String[] { CalendarUtils.getOrganizationService().getUserHandler().findUserByName(pars.toString()).getEmail() }) ;
      setParticipant(pars.toString()) ;
      setSendOption(calSetting.getSendOption());
      getChild(UIEventShareTab.class).setParticipantStatusList(participantStatusList_);
      attenderTab.updateParticipants(pars.toString());
      setRepeatSummary(buildRepeatSummary(null));
    }
  }

  public boolean isReminderByEmail(List<Reminder> reminders){
    for(Reminder rm : reminders) {
      return (Reminder.TYPE_EMAIL.equals(rm.getReminderType()));
    }
    return false;
  }

  public static java.util.Calendar getCalendar(UIForm uiForm,String formTime,CalendarSetting calSetting) {
    java.util.Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
    try {
      cal.setTimeInMillis(Long.parseLong(formTime)) ;
    } catch (Exception e) {
      UIMiniCalendar miniCalendar = uiForm.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      cal.setTime(miniCalendar.getCurrentCalendar().getTime()) ;
    }
    Long beginMinute = (cal.get(java.util.Calendar.MINUTE)/calSetting.getTimeInterval())*calSetting.getTimeInterval() ;
    cal.set(java.util.Calendar.MINUTE, beginMinute.intValue()) ;
    return cal;
  }

  private void setEventCheckTime(Date time) {
    UIEventAttenderTab uiAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    uiAttenderTab.calendar_.setTime(time) ;
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
    UIEventDetailTab uiEventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    UIFormSelectBoxWithGroups selectBoxWithGroups = uiEventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR);
    if(options != null) {
      selectBoxWithGroups.setOptions(options) ;
    }else {
      selectBoxWithGroups.setOptions(getCalendars()) ;
    }

    String spaceId = UICalendarPortlet.getSpaceId();
    if (spaceId != null) {
      setCalendarOptionOfSpaceAsSelected(spaceId, selectBoxWithGroups.getOptions(), selectBoxWithGroups);
    }

    calType_ = calType ;
  }
  private List<SelectItem> getCalendars() throws Exception {
    return CalendarUtils.getCalendarOption() ;
  }

  protected void refreshCategory()throws Exception {
    UIFormInputWithActions eventDetailTab = getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory()) ;
  }

  private List<SelectItemOption<String>> getShareValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_PRIVATE, ITEM_PRIVATE)) ;
    options.add(new SelectItemOption<String>(ITEM_PUBLIC, ITEM_PUBLIC)) ;
    return options ;
  }
  private List<SelectItemOption<String>> getStatusValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(ITEM_BUSY, ITEM_BUSY)) ;
    options.add(new SelectItemOption<String>(ITEM_AVAILABLE, ITEM_AVAILABLE)) ;
    options.add(new SelectItemOption<String>(ITEM_OUTSIDE, ITEM_OUTSIDE)) ;
    return options ;
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
  public void updateSelect(String selectField, String value) throws Exception { } 

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

  protected boolean isEventDetailValid(CalendarSetting calendarSetting) throws Exception{
    String dateFormat = calendarSetting.getDateFormat() ;
    String timeFormat = calendarSetting.getTimeFormat() ;
    Date from = null ;
    Date to = null ;

    if(CalendarUtils.isEmpty(getCalendarId())) {
      errorMsg_ = getId() +  ".msg.event-calendar-required" ;
      return false ;
    } 
    if(CalendarUtils.isEmpty(getEventCategory())) {
      errorMsg_ = getId() +  ".msg.event-category-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getEventFormDateValue())) {
      errorMsg_ = getId() +  ".msg.event-fromdate-required" ;
      return false ;
    }
    if(CalendarUtils.isEmpty(getEventToDateValue())){
      errorMsg_ = getId() +  ".msg.event-todate-required" ;
      return false ;
    }
    try {
      from = getEventFromDate(dateFormat, timeFormat) ;
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Faile to get event from date", e);
      }
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    if(from == null){
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    try {
      to = getEventToDate(dateFormat, timeFormat) ;
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Fail to get event to date", e);
      }
      errorMsg_ = getId() +  ".msg.event-fromdate-notvalid" ;
      return false ;
    }
    if(to == null){
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

  private boolean isParticipantValid() throws Exception {
    if(isSendMail() && getMeetingInvitation() == null) {
      errorMsg_ = "UIEventForm.msg.error-particimant-email-required" ;
      return false ;
    }
    errorMsg_ = null ;
    return true ;
  }
  protected String getEventSumary() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_EVENT).getValue() ;
  }
  protected void setEventSumary(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_EVENT).setValue(value) ;
  }
  protected String getEventDescription() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormTextAreaInput(UIEventDetailTab.FIELD_DESCRIPTION).getValue() ;
  }
  protected void setEventDescription(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormTextAreaInput(UIEventDetailTab.FIELD_DESCRIPTION).setValue(value) ;
  }
  protected String getCalendarId() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    String value = eventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).getValue() ;
    newCalendarId_ = value ;
    if (!CalendarUtils.isEmpty(value) && value.split(CalendarUtils.COLON).length>0) {
      calType_ = value.split(CalendarUtils.COLON)[0] ; 
      return value.split(CalendarUtils.COLON)[1] ;      
    } 
    return value ;
  }
  public void setSelectedCalendarId(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    String selectedCal = new StringBuffer(calType_).append(CalendarUtils.COLON).append(value).toString();
    eventDetailTab.getUIFormSelectBoxGroup(UIEventDetailTab.FIELD_CALENDAR).setValue(selectedCal);
    oldCalendarId_ = selectedCal ;
  }

  protected String getEventCategory() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).getValue() ;
  }
  public void setSelectedCategory(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(value);
  }

  protected Date getEventFromDate(String dateFormat,String timeFormat) throws Exception {
    try {
      UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
      UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
      UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_FROM_TIME) ;
      return UITaskForm.getBeginDate(getEventAllDate(), dateFormat, fromField.getValue(), timeFormat, timeField.getValue());
    } catch (Exception e) {
      return null;
    }
  }
  protected Date getEventFromDate() throws Exception {
    try {
      UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
      UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
      UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_FROM_TIME) ;
      CalendarSetting calendarSetting = CalendarUtils.getCurrentUserCalendarSetting();
      return UITaskForm.getBeginDate(getEventAllDate(), calendarSetting.getDateFormat(), fromField.getValue(), calendarSetting.getTimeFormat(), timeField.getValue());
    } catch (Exception e) {
      return null;
    }
  }

  protected String getEventFormDateValue () {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    return fromField.getValue() ;
  }
  protected void setEventFromDate(Date date,String dateFormat, String timeFormat) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIEventAttenderTab eventAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    UIFormDateTimePicker fromField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM) ;
    UIFormComboBox timeField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_FROM_TIME) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    fromField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    timeField.setValue(df.format(date)) ;
    eventAttenderTab.setEventFromDate(date, timeFormat) ;
  }

  protected Date getEventToDate(String dateFormat, String timeFormat) throws Exception {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_TO_TIME) ;
    return UITaskForm.getToDate(getEventAllDate(), dateFormat, toField.getValue(), timeFormat, timeField.getValue());
  }

  protected Date getEventToDate() throws Exception {
    CalendarSetting calendarSetting = CalendarUtils.getCurrentUserCalendarSetting();
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormComboBox timeField = eventDetailTab.getUIFormComboBox(UIEventDetailTab.FIELD_TO_TIME) ;
    return UITaskForm.getToDate(getEventAllDate(), calendarSetting.getDateFormat(), toField.getValue(), calendarSetting.getTimeFormat(), timeField.getValue());
  }

  protected void setEventToDate(Date date,String dateFormat, String timeFormat) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIEventAttenderTab eventAttenderTab = getChildById(TAB_EVENTATTENDER) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    UIFormComboBox timeField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO_TIME) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    toField.setValue(df.format(date)) ;
    df = new SimpleDateFormat(timeFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    timeField.setValue(df.format(date)) ;
    eventAttenderTab.setEventToDate(date, timeFormat) ;
  }

  protected String getEventToDateValue () {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker toField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_TO) ;
    return toField.getValue() ;
  }
  protected boolean getEventAllDate() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUICheckBoxInput(UIEventDetailTab.FIELD_CHECKALL).isChecked() ;
  }
  protected void setEventAllDate(boolean isCheckAll) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUICheckBoxInput(UIEventDetailTab.FIELD_CHECKALL).setChecked(isCheckAll) ;
  }

  protected String getEventRepeat() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return  eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_REPEAT).getValue() ;
  }
  protected void setEventRepeat(String type) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_REPEAT).setValue(type) ;
  }

  protected boolean getEventIsRepeat() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return  eventDetailTab.getUICheckBoxInput(UIEventDetailTab.FIELD_ISREPEAT).isChecked();
  }

  protected void setEventIsRepeat(boolean isRepeat) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUICheckBoxInput(UIEventDetailTab.FIELD_ISREPEAT).setChecked(isRepeat);
  }

  protected String getEventRepeatUntilValue() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    UIFormDateTimePicker untilField = eventDetailTab.getChildById(UIEventDetailTab.FIELD_REPEAT_UNTIL);
    return  untilField.getValue();
  }


  protected String getEventPlace() {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_PLACE).getValue();
  }
  protected void setEventPlace(String value) {
    UIEventDetailTab eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIStringInput(UIEventDetailTab.FIELD_PLACE).setValue(value) ;
  }

  protected boolean getEmailReminder() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).isChecked() ;
  }
  public void setEmailReminder(boolean isChecked) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_EMAIL).setChecked(isChecked) ;
  }

  protected String getEmailRemindBefore() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).getValue() ;
  }
  protected boolean isEmailRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return Boolean.parseBoolean(eventReminderTab.getUICheckBoxInput(UIEventReminderTab.EMAIL_IS_REPEAT).getValue().toString()) ;
  }
  public void setEmailRepeat(Boolean value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUICheckBoxInput(UIEventReminderTab.EMAIL_IS_REPEAT).setChecked(value) ;
  }
  protected String getEmailRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).getValue() ;
  }
  protected void setEmailRepeatInterVal(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
  }
  protected Boolean isPopupRepeat() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return Boolean.parseBoolean(eventReminderTab.getUICheckBoxInput(UIEventReminderTab.POPUP_IS_REPEAT).getValue().toString()) ;
  }
  protected void setPopupRepeat(Boolean value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUICheckBoxInput(UIEventReminderTab.POPUP_IS_REPEAT).setChecked(value) ;
  }
  protected String getPopupRepeatInterVal() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
  } 

  public void setEmailRemindBefore(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.EMAIL_REMIND_BEFORE).setValue(value) ;
  }

  protected String getEmailAddress() throws Exception {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).getValue() ;
  }
  public void setEmailAddress(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIStringInput(UIEventReminderTab.FIELD_EMAIL_ADDRESS).setValue(value) ;
  }

  protected boolean getPopupReminder() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).isChecked() ;
  }
  protected void setPopupReminder(boolean isChecked) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUICheckBoxInput(UIEventReminderTab.REMIND_BY_POPUP).setChecked(isChecked) ;
  }
  protected String getPopupReminderTime() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    return eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REMIND_BEFORE).getValue() ;
  }

  protected void setPopupRemindBefore(String value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REMIND_BEFORE).setValue(value) ;
  }
  protected long getPopupReminderSnooze() {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    try {
      String time =  eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).getValue() ;
      return Long.parseLong(time) ;
    } catch (Exception e){
      if (LOG.isDebugEnabled()) {
        LOG.debug("Can't get time from POPUP_REPEAT_INTERVAL", e);
      }
    }
    return 0 ;
  } 
  protected List<Attachment>  getAttachments(String eventId, boolean isAddNew) {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    return uiEventDetailTab.getAttachments() ;
  }

  protected long getTotalAttachment() {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    long attSize = 0 ; 
    for(Attachment att : uiEventDetailTab.getAttachments()) {
      attSize = attSize + att.getSize() ;
    }
    return attSize ;
  }

  protected void setAttachments(List<Attachment> attachment) throws Exception {
    UIEventDetailTab uiEventDetailTab = getChild(UIEventDetailTab.class) ;
    uiEventDetailTab.setAttachments(attachment) ;
    uiEventDetailTab.refreshUploadFileList() ;
  }
  protected void setPopupRepeatInterval(long value) {
    UIEventReminderTab eventReminderTab =  getChildById(TAB_EVENTREMINDER) ;
    eventReminderTab.getUIFormSelectBox(UIEventReminderTab.POPUP_REPEAT_INTERVAL).setValue(String.valueOf(value)) ;
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
  protected List<Reminder>  getEventReminders(Date fromDateTime, List<Reminder> currentReminders) throws Exception {
    List<Reminder> reminders = new ArrayList<Reminder>() ;
    if(getEmailReminder()) {
      Reminder email = new Reminder() ;
      if(currentReminders != null) {
        for(Reminder rm : currentReminders) {
          if(rm.getReminderType().equals(Reminder.TYPE_EMAIL)) {
            email = rm ;
            break ;
          }
        }
      }     
      email.setReminderType(Reminder.TYPE_EMAIL) ;
      email.setReminderOwner(CalendarUtils.getCurrentUser());
      email.setAlarmBefore(Long.parseLong(getEmailRemindBefore())) ;
      StringBuffer sbAddress = new StringBuffer() ;
      for(String s : getEmailAddress().replaceAll(CalendarUtils.SEMICOLON, CalendarUtils.COMMA).split(CalendarUtils.COMMA)) {
        s = s.trim() ;
        if(sbAddress.indexOf(s) < 0) {
          if(sbAddress.length() > 0) sbAddress.append(CalendarUtils.COMMA) ;
          sbAddress.append(s) ;
        }  
      }
      email.setEmailAddress(sbAddress.toString()) ;
      email.setRepeate(isEmailRepeat()) ;
      email.setRepeatInterval(Long.parseLong(getEmailRepeatInterVal())) ;
      email.setFromDateTime(fromDateTime) ;      
      if (!CalendarUtils.isEmpty(email.getEmailAddress())) reminders.add(email) ;
    }
    if(getPopupReminder()) {
      Reminder popup = new Reminder() ;
      if(currentReminders != null) {
        for(Reminder rm : currentReminders) {
          if(rm.getReminderType().equals(Reminder.TYPE_POPUP)) {
            popup = rm ;
            break ;
          }
        }
      } 
      StringBuffer sb = new StringBuffer() ;
      boolean isExist = false ;
      if(!isExist) {
        if(sb.length() >0) sb.append(CalendarUtils.COMMA);
        sb.append(CalendarUtils.getCurrentUser());
      }
      popup.setReminderOwner(sb.toString()) ;
      popup.setReminderType(Reminder.TYPE_POPUP) ;
      popup.setAlarmBefore(Long.parseLong(getPopupReminderTime()));
      popup.setRepeate(isPopupRepeat()) ;
      popup.setRepeatInterval(Long.parseLong(getPopupRepeatInterVal())) ;
      popup.setFromDateTime(fromDateTime) ;
      reminders.add(popup) ;
    } 
    return reminders ;
  }

  protected String getEventPriority() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    return eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).getValue() ;
  }
  protected void setSelectedEventPriority(String value) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTDETAIL) ;
    eventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_PRIORITY).setValue(value) ;
  }

  protected String getEventState() {
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_STATUS).getValue() ;
  }
  public void setSelectedEventState(String value) {
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_STATUS).setValue(value) ;
  }

  protected String getShareType() {
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return  eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SHARE).getValue()  ;
  }

  protected String getSendOption(){
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    return  eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SEND).getValue()  ;
  }

  protected void setSendOption(String value){
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SEND).setValue(value)  ;
  }

  public String getMessage() {
    return invitationMsg_;
  }

  public void setMessage(String invitationMsg) {
    this.invitationMsg_ = invitationMsg;
  }

  protected void setSelectedShareType(String value) {
    UIEventShareTab eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    eventDetailTab.getUIFormRadioBoxInput(UIEventShareTab.FIELD_SHARE).setValue(value) ;
  }

  protected String[] getMeetingInvitation() {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    String invitation = eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).getValue() ;
    if(CalendarUtils.isEmpty(invitation)) return null ;
    else return invitation.replace(CalendarUtils.SEMICOLON, CalendarUtils.COMMA).split(CalendarUtils.COMMA) ;
  } 

  protected String getInvitationEmail() {
    StringBuilder buider = new StringBuilder("") ;
    for (Entry<String, String> par : participantStatus_.entrySet()) {
      if (buider.length() > 0 && par.getKey().contains("@")) buider.append(CalendarUtils.COMMA) ;
      if(par.getKey().contains("@")) buider.append(par.getKey().substring(par.getKey()
                                                                          .lastIndexOf(CalendarUtils.OPEN_PARENTHESIS) + 1).replace(CalendarUtils.CLOSE_PARENTHESIS, "")) ;
    }
    return buider.toString() ;
  } 
  protected void setMeetingInvitation(String[] values) {
    UIFormInputWithActions eventDetailTab =  getChildById(TAB_EVENTSHARE) ;
    StringBuffer sb = new StringBuffer() ;
    if(values != null) {
      for(String s : values) {
        if(sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
        sb.append(s) ;
      }
    }
    eventDetailTab.getUIFormTextAreaInput(FIELD_MEETING).setValue(sb.toString()) ;
  }

  protected String getParticipantValues() {
    StringBuilder buider = new StringBuilder("") ;
    for (String par : participants_.keySet()) {
      if (buider.length() > 0) buider.append(CalendarUtils.BREAK_LINE) ;
      buider.append(par) ;
    }
    return buider.toString() ;
  } 

  protected String  getParticipantStatusValues() {
    StringBuilder buider = new StringBuilder("") ;
    for (Entry<String, String> par : participantStatus_.entrySet()) {
      if (buider.length() > 0) buider.append(CalendarUtils.BREAK_LINE) ;
      buider.append(par.getKey()+":"+par.getValue()) ;
    }
    return buider.toString() ;
  }

  protected void  setParticipantStatusValues(String[] values) throws Exception {
    participantStatus_.clear();
    participantStatusList_.clear();
    for (String par : values) {
      String[] entry = par.split(":");
      if(entry.length> 0 && StringUtils.isNotBlank(entry[0])){
        if(entry.length>1){
          participantStatus_.put(entry[0], entry[1]);
          participantStatusList_.add(new ParticipantStatus(entry[0],entry[1]));
        } else if(entry.length == 1){
          participantStatus_.put(entry[0], STATUS_EMPTY);
          participantStatusList_.add(new ParticipantStatus(entry[0],STATUS_EMPTY));
        }
      }
    }
  }

  public void setParticipant(String values) throws Exception{
    OrganizationService orgService = CalendarUtils.getOrganizationService() ;
    StringBuffer sb = new StringBuffer() ;

    for(String s : values.split("[\\r\\n]+")) {
      User user = orgService.getUserHandler().findUserByName(s) ;
      if(user != null) {
        participants_.put(s.trim(), user.getEmail()) ;
        if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.BREAK_LINE) ;
        sb.append(s.trim()) ;
      }
    }
    ((UIEventAttenderTab)getChildById(TAB_EVENTATTENDER)).updateParticipants(getParticipantValues()) ;
  }

  public String  getParticipantStatus() {
    StringBuilder buider = new StringBuilder("") ;
    for (String par : participantStatus_.keySet()) {
      if (buider.length() > 0) buider.append(CalendarUtils.BREAK_LINE) ;
      buider.append(par) ;
    }
    return buider.toString() ;
  } 

  public void setParticipantStatus(String values) throws Exception{
    String[] array = values.split(CalendarUtils.BREAK_LINE);
    for(String s : array) {
      if(s.trim().length()>0)
        if(participantStatus_.put(s.trim(), STATUS_EMPTY) == null)
          participantStatusList_.add(new ParticipantStatus(s.trim(),STATUS_EMPTY));
    }
  }

  protected boolean isSendMail() {
    return false ;
  }

  /**
   * Fill data from invitation event to current event form 
   * @param calSetting
   * @param event
   * @param calendarId
   * @param formtime
   * @throws Exception
   */
  public void importInvitationEvent(CalendarSetting calSetting, CalendarEvent event, String calendarId, String formtime) throws Exception {
    if(event != null) {
      setEventSumary(event.getSummary()) ;
      setEventDescription(event.getDescription()) ;
      setEventAllDate(CalendarUtils.isAllDayEvent(event)) ;
      setEventFromDate(event.getFromDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setEventCheckTime(event.getFromDateTime()) ;
      setEventToDate(event.getToDateTime(),calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
      setSelectedCalendarId(calendarId) ;

      setEventPlace(event.getLocation()) ;
      setEventRepeat(event.getRepeatType()) ;
      setEventReminders(event.getReminders()) ;
      setAttachments(event.getAttachment()) ;

      setMessage(event.getMessage());
      setParticipantStatusValues(event.getParticipantStatus());
    }
  }

  private String buildMailSubject(CalendarEvent event, DateFormat df) {
    StringBuffer sbSubject = new StringBuffer("["+getLabel("invitation")+"] ") ;
    sbSubject.append(event.getSummary()) ;
    sbSubject.append(" ") ;
    sbSubject.append(df.format(event.getFromDateTime())) ;

    return sbSubject.toString();
  }

  private String buildMailBody(User invitor, CalendarEvent event, String toId, DateFormat df, String timezone) throws Exception {
    List<Attachment> atts = getAttachments(null, false);

    StringBuffer sbBody = new StringBuffer() ;
    sbBody.append("<div style=\"margin: 20px auto; padding: 8px; background: rgb(224, 236, 255) none repeat scroll 0%; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; width: 500px;\">") ;
    sbBody.append("<table style=\"margin: 0px; padding: 0px; border-collapse: collapse; border-spacing: 0px; width: 100%; line-height: 16px;\">") ;
    sbBody.append("<tbody>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap; \">"+getLabel("fromWho")+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\"> " + invitor.getUserName() +"("+invitor.getEmail()+")" + " </td>") ;
    sbBody.append("</tr>") ;

    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_MESSAGE)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + event.getMessage()+ "</td>") ;
    sbBody.append("</tr>") ;

    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_EVENT)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + event.getSummary()+ "</td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_DESCRIPTION)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + (event.getDescription() != null && event.getDescription().trim().length() > 0 ? event.getDescription() : " ") + "</td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel("when")+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\"> <div>"+getLabel(UIEventDetailTab.FIELD_FROM)+": " +df.format(event.getFromDateTime())+" " + timezone + "</div>");
    sbBody.append("<div>"+getLabel(UIEventDetailTab.FIELD_TO)+": "+df.format(event.getToDateTime())+" " + timezone + "</div></td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_PLACE)+":</td>") ;
    sbBody.append("<td style=\"padding: 4px;\">" + (event.getLocation() != null && event.getLocation().trim().length() > 0 ? event.getLocation(): " ") + "</td>") ;
    sbBody.append("</tr>") ;
    sbBody.append("<tr>") ;
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(FIELD_MEETING)+"</td>") ;
    toId = toId.replace(CalendarUtils.BREAK_LINE, CalendarUtils.COMMA);
    if (CalendarUtils.isEmpty(getInvitationEmail())) {
      sbBody.append("<td style=\"padding: 4px;\">" +toId + "</td>") ;
    } else {
      String newInvi = getInvitationEmail().replace(",", ", ") ;
      sbBody.append("<td style=\"padding: 4px;\">" +toId + ", " + newInvi + "</td>") ;
    }
    sbBody.append("</tr>");
    if(!atts.isEmpty()){
      sbBody.append("<tr>");
      sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"+getLabel(UIEventDetailTab.FIELD_ATTACHMENTS)+":</td>");
      StringBuffer sbf = new StringBuffer();
      for(Attachment att : atts) {
        if(sbf.length() > 0) sbf.append(",") ;
        sbf.append(att.getName());
      }
      sbBody.append("<td style=\"padding: 4px;\"> ("+atts.size()+") " +sbf.toString()+" </td>");
      sbBody.append("</tr>");
    }

    return sbBody.toString();
  }

  protected void sendMail(MailService svr, OrganizationService orSvr,CalendarSetting setting, String fromId,  String toId, CalendarEvent event) throws Exception {
    User invitor = orSvr.getUserHandler().findUserByName(CalendarUtils.getCurrentUser()) ;
    List<Attachment> atts = getAttachments(null, false);

    Map<String, String> eXoIdMap = new HashMap<String, String>();
    Map<String, String> eXoMailMap = new HashMap<String, String>();

    StringBuffer sbAddress = new StringBuffer() ;
    if(event.getInvitation()!= null) {
      for(String s : event.getInvitation()) {
        s = s.trim() ;
        if(sbAddress.length() > 0) sbAddress.append(",") ;
        sbAddress.append(s) ;
        eXoIdMap.put(s, null);
      }
    }

    OrganizationService orgService = CalendarUtils.getOrganizationService() ;
    StringBuffer sb = new StringBuffer() ;
    for(String s : toId.split(CalendarUtils.COMMA)) {
      User user = orgService.getUserHandler().findUserByName(s) ;
      if(user != null) {
        if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.COMMA) ;
        sb.append(user.getEmail()) ;
        eXoIdMap.put(user.getEmail(), s);
        eXoMailMap.put(s, user.getEmail());
      }
    }

    if (sbAddress.length() > 0 && sb.toString().trim().length() > 0 ) sbAddress.append(",") ;
    sbAddress.append(sb.toString().trim()) ;

    StringBuffer values = new StringBuffer(fromId) ;
    User user = orSvr.getUserHandler().findUserByName(fromId) ;

    values.append(CalendarUtils.SEMICOLON + " ") ;
    values.append(toId) ;
    values.append(CalendarUtils.SEMICOLON + " ") ;
    values.append(event.getCalType()) ;
    values.append(CalendarUtils.SEMICOLON + " ") ;
    values.append(event.getCalendarId()) ;
    values.append(CalendarUtils.SEMICOLON + " ") ;
    values.append(event.getId()) ;

    CalendarService calService = CalendarUtils.getCalendarService() ;
    org.exoplatform.services.mail.MailService mService = getApplicationComponent(org.exoplatform.services.mail.impl.MailServiceImpl.class) ;
    org.exoplatform.services.mail.Attachment attachmentCal = new org.exoplatform.services.mail.Attachment() ;

    try {
      OutputStream out = calService.getCalendarImportExports(CalendarService.ICALENDAR)
          .exportEventCalendar(fromId, event.getCalendarId(), event.getCalType(), event.getId()) ;
      ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
      attachmentCal.setInputStream(is) ;
      attachmentCal.setName("icalendar.ics");
      attachmentCal.setMimeType("text/calendar") ;
    }
    catch (Exception e) {
      attachmentCal = null;
      if (LOG.isDebugEnabled()) LOG.debug("Fail to create attachment", e);
    }

    CalendarSetting calendarSetting;
    DateFormat _df;

    String userEmail;
    for (String userId : toId.split(CalendarUtils.COMMA)) {
      userEmail = eXoMailMap.get(userId);

      calendarSetting = (calService.getCalendarSetting(userId) != null) ?
                                                                         calService.getCalendarSetting(userId) : CalendarUtils.getCurrentUserCalendarSetting();
                                                                         _df = new SimpleDateFormat(calendarSetting.getDateFormat() + " " + calendarSetting.getTimeFormat());
                                                                         _df.setTimeZone(TimeZone.getTimeZone(calendarSetting.getTimeZone()));

                                                                         if (CalendarUtils.isEmpty(userEmail)) continue;
                                                                         org.exoplatform.services.mail.Message  message = new org.exoplatform.services.mail.Message();
                                                                         message.setSubject(buildMailSubject(event, _df)) ;
                                                                         message.setBody(getBodyMail(buildMailBody(invitor, event, toId, _df, CalendarUtils.generateTimeZoneLabel(calendarSetting.getTimeZone())),
                                                                                                     eXoIdMap, userEmail, invitor, event)) ;
                                                                         message.setTo(userEmail);
                                                                         message.setMimeType(Utils.MIMETYPE_TEXTHTML) ;
                                                                         message.setFrom(user.getEmail()) ;

                                                                         if (attachmentCal != null) {
                                                                           message.addAttachment(attachmentCal) ;
                                                                         }

                                                                         if(!atts.isEmpty()){
                                                                           for(Attachment att : atts) {
                                                                             org.exoplatform.services.mail.Attachment attachment = new org.exoplatform.services.mail.Attachment() ;
                                                                             attachment.setInputStream(att.getInputStream()) ;
                                                                             attachment.setMimeType(att.getMimeType()) ;
                                                                             attachment.setName(att.getName());
                                                                             message.addAttachment(attachment) ;
                                                                           }
                                                                         }
                                                                         mService.sendMessage(message) ;
    }
  }

  private String getBodyMail(String sbBody,Map<String, String> eXoIdMap,String s,User invitor,CalendarEvent event) throws Exception {
    StringBuilder body = new StringBuilder(sbBody.toString());
    String eXoId = CalendarUtils.isEmpty(eXoIdMap.get(s)) ? "null":eXoIdMap.get(s);
    body.append("<tr>");
    body.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">");
    body.append(getLabel("likeToAttend")+" </td><td> <a href=\"" + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.ACCEPT, invitor, s, eXoId, event) + "\" >"+getLabel("yes")+"</a>" + " - " + "<a href=\"" + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.NOTSURE, invitor, s, eXoId, event) + "\" >"+getLabel("notSure")+"</a>" + " - " + "<a href=\"" + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.DENY, invitor, s, eXoId, event) + "\" >"+getLabel("no")+"</a>");
    body.append("</td></tr>");
    body.append("<tr>");
    body.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">");
    body.append(getLabel("seeMoreDetails")+" </td><td><a href=\"" + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.ACCEPT_IMPORT, invitor, s, eXoId, event) + "\" >"+getLabel("importToExoCalendar")+"</a> "+getLabel("or")+" <a href=\"" + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.JUMP_TO_CALENDAR, invitor, s, eXoId, event) + "\" >"+getLabel("jumpToExoCalendar")+"</a>");
    body.append("</td></tr>");
    body.append("</tbody>");
    body.append("</table>");
    body.append("</div>") ;
    return body.toString();
  }

  protected String getReplyInvitationLink(int answer, User invitor, String invitee, String eXoId, CalendarEvent event) throws Exception{
    String portalURL = CalendarUtils.getServerBaseUrl() + PortalContainer.getCurrentPortalContainerName();
    String restURL = portalURL + "/" + PortalContainer.getCurrentRestContextName();
    String calendarURL = CalendarUtils.getCalendarURL();

    if (answer == org.exoplatform.calendar.service.Utils.ACCEPT || answer == org.exoplatform.calendar.service.Utils.DENY ||
        answer == org.exoplatform.calendar.service.Utils.NOTSURE) {
      return (restURL + "/cs/calendar" + CalendarUtils.INVITATION_URL + event.getCalendarId() + "/" + event.getCalType() + "/" + event.getId() + "/" + invitor.getUserName() + "/" + invitee + "/" + eXoId + "/" + answer);
    }
    if (answer == org.exoplatform.calendar.service.Utils.ACCEPT_IMPORT) {
      return (calendarURL + CalendarUtils.INVITATION_IMPORT_URL + invitor.getUserName() + "/" + event.getId() + "/" + event.getCalType());
    }
    if (answer == org.exoplatform.calendar.service.Utils.JUMP_TO_CALENDAR) {
      return (calendarURL + CalendarUtils.INVITATION_DETAIL_URL + invitor.getUserName() + "/" + event.getId() + "/" + event.getCalType());
    }     
    return "";
  }

  public Attachment getAttachment(String attId) {
    UIEventDetailTab uiDetailTab = getChildById(TAB_EVENTDETAIL) ;
    for (Attachment att : uiDetailTab.getAttachments()) {
      if(att.getId().equals(attId)) {
        return att ;
      }
    }
    return null;
  }

  public List<ParticipantStatus> getParticipantStatusList() {    
    return participantStatusList_;
  }


  public static void downloadAtt(Event<?> event, UIForm uiForm, boolean isEvent) throws Exception {
    String attId = event.getRequestContext().getRequestParameter(OBJECTID) ;
    Attachment attach = null;
    if (isEvent) {
      UIEventForm uiEventForm = (UIEventForm)uiForm;
      attach = uiEventForm.getAttachment(attId) ;
    } else {
      UITaskForm uiTaskForm = (UITaskForm)uiForm;
      attach = uiTaskForm.getAttachment(attId);
    }
    if(attach != null) {
      String mimeType = attach.getMimeType().substring(attach.getMimeType().indexOf("/")+1) ;
      DownloadResource dresource = new InputStreamDownloadResource(attach.getInputStream(), mimeType);
      DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
      dresource.setDownloadName(attach.getName());
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
      if (isEvent) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTDETAIL)) ;        
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(UITaskForm.TAB_TASKDETAIL)) ;
      }
    }
  }

  private boolean isSignificantChanged(CalendarEvent newCalendarEvent, CalendarEvent oldCalendarEvent) {
    return newCalendarEvent != null && oldCalendarEvent != null && (
        (oldCalendarEvent.getSummary() != null && !oldCalendarEvent.getSummary().equalsIgnoreCase(newCalendarEvent.getSummary()) || newCalendarEvent.getSummary() != null && !newCalendarEvent.getSummary().equalsIgnoreCase(oldCalendarEvent.getSummary()))
        || (oldCalendarEvent.getDescription() != null && !oldCalendarEvent.getDescription().equalsIgnoreCase(newCalendarEvent.getDescription()) || newCalendarEvent.getDescription() != null && !newCalendarEvent.getDescription().equalsIgnoreCase(oldCalendarEvent.getDescription()))
        || (oldCalendarEvent.getLocation() != null && !oldCalendarEvent.getLocation().equalsIgnoreCase(newCalendarEvent.getLocation()) || newCalendarEvent.getLocation() != null && !newCalendarEvent.getLocation().equalsIgnoreCase(oldCalendarEvent.getLocation()))
        || (!oldCalendarEvent.getFromDateTime().equals(newCalendarEvent.getFromDateTime())) 
        || (!oldCalendarEvent.getToDateTime().equals(newCalendarEvent.getToDateTime()))
        );
  }

  private CalendarEvent sendInvitation(Event<UIEventForm> event, CalendarSetting calSetting, CalendarEvent calendarEvent) throws Exception {
    String username = RequestContext.getCurrentInstance().getRemoteUser();
    String toId = null;
    if (this.isAddNew_ || this.isChangedSignificantly) {
      StringBuilder participantsList = new StringBuilder("") ;
      for (String par : participants_.keySet()) {
        if (participantsList.length() > 0) participantsList.append(CalendarUtils.COMMA) ;
        participantsList.append(par) ;
      }
      toId = participantsList.toString();
    }
    else {
      // select new Invitation email
      Map<String, String> invitations = new LinkedHashMap<String, String>();
      for (String s : calendarEvent.getInvitation()) {
        invitations.put(s, s);
      }
      for (String parSt : calendarEvent.getParticipantStatus()) {
        String[] entry = parSt.split(":");
        // is old
        if (entry.length > 1 && entry[0].contains("@"))
          invitations.remove(entry[0]);
      }
      calendarEvent.setInvitation(invitations.keySet().toArray(new String[invitations.size()]));
      // select new User
      StringBuilder builder = new StringBuilder("");
      for (String parSt : calendarEvent.getParticipantStatus()) {
        String[] entry = parSt.split(":");
        // is new
        if ((entry.length == 1) && (!entry[0].contains("@"))) {
          if (builder.length() > 0)
            builder.append(CalendarUtils.BREAK_LINE);
          builder.append(entry[0]);
        }
      }

      if (builder.toString().trim().length() > 0 || invitations.size() > 0) {
        toId = builder.toString();
      }
    }

    try {
      if (toId != null) {
        sendMail(CalendarUtils.getMailService(), CalendarUtils.getOrganizationService(), calSetting, username, toId, calendarEvent);
        List<String> parsUpdated = new LinkedList<String>();
        for (String parSt : calendarEvent.getParticipantStatus()) {
          String[] entry = parSt.split(":");
          if (entry.length > 1)
            parsUpdated.add(entry[0] + ":" + entry[1]);
          else
            parsUpdated.add(entry[0] + ":" + STATUS_PENDING);
        }
        calendarEvent.setParticipantStatus(parsUpdated.toArray(new String[parsUpdated.size()]));
      }
      return calendarEvent;
    } catch (Exception e) {
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIEventForm.msg.error-send-email", null));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Fail to send mail ivitation to the participant", e);
      }
    }
    return null;
  }

  public void saveAndNoAsk(Event<UIEventForm> event, boolean isSend, Boolean updateSeries)throws Exception {
    UIEventForm uiForm = event.getSource() ;
    UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
    UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
    UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
    CalendarSetting calSetting = calendarPortlet.getCalendarSetting() ;
    CalendarService calService = CalendarUtils.getCalendarService() ;
    String summary = uiForm.getEventSumary().trim() ;
    summary = CalendarUtils.enCodeTitle(summary);
    String location = uiForm.getEventPlace() ;
    if(!CalendarUtils.isEmpty(location)) {
      location = location.replaceAll(CalendarUtils.GREATER_THAN, "").replaceAll(CalendarUtils.SMALLER_THAN,"") ;
    }
    else {
      location = null;
    }
    String description = uiForm.getEventDescription() ;
    if(!CalendarUtils.isEmpty(description)) {
      description = description.replaceAll(CalendarUtils.GREATER_THAN, "").replaceAll(CalendarUtils.SMALLER_THAN,"") ;
    }
    else {
      description = null;
    }
    if(!uiForm.isEventDetailValid(calSetting)) {
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.errorMsg_, null));
      uiForm.setSelectedTab(TAB_EVENTDETAIL) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      return ;
    } 
    if(!uiForm.isReminderValid()) {
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.errorMsg_, new String[] {uiForm.errorValues} ));
      uiForm.setSelectedTab(TAB_EVENTREMINDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      return ;
    } 
    if(!uiForm.isParticipantValid()) {
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.errorMsg_, new String[] { uiForm.errorValues }));
      uiForm.setSelectedTab(TAB_EVENTSHARE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      return ;
    }
    Date from = uiForm.getEventFromDate(calSetting.getDateFormat(), calSetting.getTimeFormat()) ;
    Date to = uiForm.getEventToDate(calSetting.getDateFormat(),calSetting.getTimeFormat()) ;
    if(from.after(to)) {
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.getId() + ".msg.event-date-time-logic", null, AbstractApplicationMessage.WARNING)) ;
      return ;
    }
    String username = CalendarUtils.getCurrentUser() ;
    String calendarId = uiForm.getCalendarId() ;
    if(from.equals(to)) {
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
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1));
      return ;
    }
    boolean canEdit = false ;
    if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)) {
      canEdit = CalendarUtils.canEdit(null, org.exoplatform.calendar.service.Utils.getEditPerUsers(currentCalendar), username) ;
    } else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)) {
      canEdit = CalendarUtils.canEdit(
                                      CalendarUtils.getOrganizationService(), currentCalendar.getEditPermission(), username) ;
    }
    if(!canEdit && !uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE) ) {
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null,1));
      return ;
    }
    CalendarEvent calendarEvent  = null ; 
    CalendarEvent oldCalendarEvent = null;
    String[] pars = uiForm.getParticipantValues().split(CalendarUtils.BREAK_LINE) ;
    String eventId = null ;
    if(uiForm.isAddNew_){
      calendarEvent = new CalendarEvent() ;
    } else {
      calendarEvent = uiForm.calendarEvent_ ;
      oldCalendarEvent = new CalendarEvent();
      oldCalendarEvent.setSummary(calendarEvent.getSummary());
      oldCalendarEvent.setDescription(calendarEvent.getDescription());
      oldCalendarEvent.setLocation(calendarEvent.getLocation());
      oldCalendarEvent.setFromDateTime(calendarEvent.getFromDateTime());
      oldCalendarEvent.setToDateTime(calendarEvent.getToDateTime());
    }
    calendarEvent.setFromDateTime(from) ;
    calendarEvent.setToDateTime(to);

    calendarEvent.setSendOption(uiForm.getSendOption());
    calendarEvent.setMessage(uiForm.getMessage());
    String[] parStatus = uiForm.getParticipantStatusValues().split(CalendarUtils.BREAK_LINE) ;


    calendarEvent.setParticipantStatus(parStatus);

    calendarEvent.setParticipant(pars) ;
    if(CalendarUtils.isEmpty(uiForm.getInvitationEmail())) calendarEvent.setInvitation(ArrayUtils.EMPTY_STRING_ARRAY);
    else 
      if(CalendarUtils.isValidEmailAddresses(uiForm.getInvitationEmail())) {
        String addressList = uiForm.getInvitationEmail().replaceAll(CalendarUtils.SEMICOLON,CalendarUtils.COMMA) ;
        Map<String, String> emails = new LinkedHashMap<String, String>() ;
        for(String email : addressList.split(CalendarUtils.COMMA)) {
          String address = email.trim() ;
          if (!emails.containsKey(address)) emails.put(address, address) ;
        }
        if(!emails.isEmpty()) calendarEvent.setInvitation(emails.keySet().toArray(new String[emails.size()])) ;
      } else {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIEventForm.msg.event-email-invalid"
                                                                                       , new String[] { CalendarUtils.invalidEmailAddresses(uiForm.getInvitationEmail())}));
        uiForm.setSelectedTab(TAB_EVENTSHARE) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;

        return ;
      }
    calendarEvent.setCalendarId(uiForm.getCalendarId()) ;
    calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
    calendarEvent.setSummary(summary) ;
    calendarEvent.setDescription(description) ;
    calendarEvent.setCalType(uiForm.calType_) ;
    calendarEvent.setCalendarId(calendarId) ;
    calendarEvent.setEventCategoryId(uiForm.getEventCategory()) ;     
    UIFormSelectBox selectBox = ((UIFormInputWithActions)uiForm.getChildById(TAB_EVENTDETAIL))
        .getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY) ;
    for (SelectItemOption<String> o : selectBox.getOptions()) {
      if (o.getValue().equals(selectBox.getValue())) {
        calendarEvent.setEventCategoryName(o.getLabel()) ;
        break ;
      }
    }              
    calendarEvent.setLocation(location) ;

    if (uiForm.getEventIsRepeat()) {
      if (repeatEvent != null) {
        // copy repeat properties from repeatEvent to calendarEvent
        calendarEvent.setRepeatType(repeatEvent.getRepeatType());
        calendarEvent.setRepeatInterval(repeatEvent.getRepeatInterval());
        calendarEvent.setRepeatCount(repeatEvent.getRepeatCount());
        calendarEvent.setRepeatUntilDate(repeatEvent.getRepeatUntilDate());
        calendarEvent.setRepeatByDay(repeatEvent.getRepeatByDay());
        calendarEvent.setRepeatByMonthDay(repeatEvent.getRepeatByMonthDay());
      }
    } else {
      calendarEvent.setRepeatType(CalendarEvent.RP_NOREPEAT);
      calendarEvent.setRepeatInterval(0);
      calendarEvent.setRepeatCount(0);
      calendarEvent.setRepeatUntilDate(null);
      calendarEvent.setRepeatByDay(null);
      calendarEvent.setRepeatByMonthDay(null);
    }

    calendarEvent.setPriority(uiForm.getEventPriority()) ; 
    calendarEvent.setPrivate(UIEventForm.ITEM_PRIVATE.equals(uiForm.getShareType())) ;
    calendarEvent.setEventState(uiForm.getEventState()) ;
    calendarEvent.setAttachment(uiForm.getAttachments(calendarEvent.getId(), uiForm.isAddNew_)) ;
    calendarEvent.setReminders(uiForm.getEventReminders(from, calendarEvent.getReminders())) ;
    eventId = calendarEvent.getId() ;
    CalendarView calendarView = (CalendarView)uiViewContainer.getRenderedChild() ;
    this.isChangedSignificantly = this.isSignificantChanged(calendarEvent, oldCalendarEvent);

    try {

      if(uiForm.isAddNew_){
        if(uiForm.calType_.equals(CalendarUtils.PRIVATE_TYPE)) {
          calService.saveUserEvent(username, calendarId, calendarEvent, uiForm.isAddNew_) ;
        }else if(uiForm.calType_.equals(CalendarUtils.SHARED_TYPE)){
          calService.saveEventToSharedCalendar(username , calendarId, calendarEvent, uiForm.isAddNew_) ;
        }else if(uiForm.calType_.equals(CalendarUtils.PUBLIC_TYPE)){
          calService.savePublicEvent(calendarId, calendarEvent, uiForm.isAddNew_) ;
        }
      } else  {
        String fromCal = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
        String toCal = uiForm.newCalendarId_.split(CalendarUtils.COLON)[1].trim() ;
        String fromType = uiForm.oldCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
        String toType = uiForm.newCalendarId_.split(CalendarUtils.COLON)[0].trim() ;
        List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
        listEvent.add(calendarEvent) ;

        // if the event (before change) is a virtual occurrence
        if (!uiForm.calendarEvent_.getRepeatType().equals(CalendarEvent.RP_NOREPEAT) && !CalendarUtils.isEmpty(uiForm.calendarEvent_.getRecurrenceId())) {
          // save following
          CalendarEvent originEvent = calService.getRepetitiveEvent(uiForm.calendarEvent_);
          if(updateSeries == null) {
            calService.saveFollowingSeriesEvents(originEvent, uiForm.calendarEvent_, username);
          } else if (!updateSeries) {
            calService.saveOneOccurrenceEvent(originEvent, uiForm.calendarEvent_, username);
            //calService.updateOccurrenceEvent(fromCal, toCal, fromType, toType, listEvent, username);
          } else {
            // update series:

            if (CalendarUtils.isSameDate(oldCalendarEvent.getFromDateTime(), calendarEvent.getFromDateTime())) {
              calService.saveAllSeriesEvents(calendarEvent, username);
              //calService.updateRecurrenceSeries(fromCal, toCal, fromType, toType, calendarEvent, username);
            }
            else {
              calService.saveOneOccurrenceEvent(originEvent, uiForm.calendarEvent_, username);
              //calService.updateOccurrenceEvent(fromCal, toCal, fromType, toType, listEvent, username);
            }
          }
        } 
        else {
          if (org.exoplatform.calendar.service.Utils.isExceptionOccurrence(calendarEvent_)) calService.updateOccurrenceEvent(fromCal, toCal, fromType, toType, listEvent, username);
          else calService.moveEvent(fromCal, toCal, fromType, toType, listEvent, username) ;
        }
        UITaskForm.updateListView(calendarView, calendarEvent, calService, username);
      }

      if (calendarEvent != null && isSend) {
        try {
          CalendarEvent tempCal = sendInvitation(event, calSetting, calendarEvent);
          calendarEvent = tempCal != null ? tempCal : calendarEvent;
        } catch (Exception e) {
          if (LOG.isWarnEnabled()) LOG.warn("Sending invitation failed!" , e);
        }
      }

      if(calendarView instanceof UIListContainer) {
        UIListContainer uiListContainer = (UIListContainer)calendarView ;
        if (!uiListContainer.isDisplaySearchResult()) {
          uiViewContainer.refresh() ;
        }
      } else {
        uiViewContainer.refresh() ;
      }  
      calendarView.setLastUpdatedEventId(eventId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
      UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }catch (Exception e) {
      event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIEventForm.msg.add-event-error", null));
      if (LOG.isDebugEnabled()) {
        LOG.debug("Fail to add the event", e);
      }
    }

    UIEventDetailTab uiDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
    for (Attachment att : uiDetailTab.getAttachments()) {
      UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class), att.getResourceId()) ;
    }
  }

  public void setRepeatEvent(CalendarEvent repeatEvent) throws Exception {
    this.repeatEvent = repeatEvent;
    setEventIsRepeat(true);
    setRepeatSummary(buildRepeatSummary(repeatEvent));
  }

  public void setRepeatSummary(String summary) {
    repeatSummary = summary;
  }

  public String getRepeatSummary() {
    return repeatSummary;
  }

  public CalendarEvent getRepeatEvent() {
    return repeatEvent;
  }

  /**
   * Build the repeating summary, i.e: daily every 2 days, until 02/03/2011. <br/>
   * The summary structure is defined in resource bundle, it contains some parameters and </br> 
   * will be replaced by values from repeatEvent. <br/> 
   * <p>There are 6 parameters: {count}, {until}, {interval}, {byDays}, {theDay}, {theNumber}.<br/>
   * Some labels in resource bundle to define numbers (the first, the second, ...) which were used in summary
   * @param repeatEvent the repeating event
   * @return summary string about repeating event
   * @throws Exception
   */
  public String buildRepeatSummary(CalendarEvent repeatEvent) throws Exception {
    CalendarSetting calSetting = CalendarUtils.getCurrentUserCalendarSetting(); 
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat format = new SimpleDateFormat(calSetting.getDateFormat(), locale);
    DateFormatSymbols symbols = new DateFormatSymbols(locale);
    String[] dayOfWeeks = symbols.getWeekdays();

    String summary = "";
    if (repeatEvent == null) return "";
    String repeatType = repeatEvent.getRepeatType();
    if (CalendarEvent.RP_NOREPEAT.equals(repeatType) || repeatType == null) return "";
    int interval = (int)repeatEvent.getRepeatInterval();
    int count = (int)repeatEvent.getRepeatCount();
    Date until = repeatEvent.getRepeatUntilDate();
    String endType = RP_END_NEVER;
    if (count > 0) endType = RP_END_AFTER;
    if (until != null) endType = RP_END_BYDATE;

    String pattern = "";
    if (repeatType.equals(CalendarEvent.RP_DAILY)) {
      if (interval == 1) {
        //pattern = "Daily";
        pattern = getLabel("daily");
      } else {
        //pattern = "Every {interval} days";
        pattern = getLabel("every-day");
      }
      if (endType.equals(RP_END_AFTER)) {
        //pattern = "Daily, {count} times";
        //pattern = "Every {interval} days, {count} times";
        pattern += ", ";
        pattern += getLabel("count-times");
      } 
      if (endType.equals(RP_END_BYDATE)) {
        //pattern = "Daily, until {until}";
        //pattern = "Every {interval} days, until {until}";
        pattern += ", ";
        pattern += getLabel("until");
      }

      summary = pattern.replace("{interval}", String.valueOf(interval)).replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
          .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate()));
      return summary;
    }

    if (repeatType.equals(CalendarEvent.RP_WEEKLY)) {   
      if (interval == 1) {
        //pattern = "Weekly on {byDays}";
        pattern = getLabel("weekly");
      } else {
        //pattern = "Every {interval} weeks on {byDays}";
        pattern = getLabel("every-week");
      }
      if (endType.equals(RP_END_AFTER)) {
        //pattern = "Weekly on {byDays}, {count} times";
        //pattern = "Every {interval} weeks on {byDays}, {count} times";
        pattern += ", ";
        pattern += getLabel("count-times");
      }
      if (endType.equals(RP_END_BYDATE)) {
        //pattern = "Weekly on {byDays}, until {until}";
        //pattern = "Every {interval} weeks on {byDays}, until {until}";
        pattern += ", ";
        pattern += getLabel("until");
      }

      String[] weeklyByDays = repeatEvent.getRepeatByDay();
      StringBuffer byDays = new StringBuffer();
      for (int i = 0; i < weeklyByDays.length; i++) {
        if (i == 0) {
          byDays.append(dayOfWeeks[UIRepeatEventForm.convertToDayOfWeek(weeklyByDays[0])]);
        } else {
          byDays.append(", ");
          byDays.append(dayOfWeeks[UIRepeatEventForm.convertToDayOfWeek(weeklyByDays[i])]);
        }
      }
      summary = pattern.replace("{interval}", String.valueOf(interval)).replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
          .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate())).replace("{byDays}", byDays.toString());
      return summary;

    }

    if (repeatType.equals(CalendarEvent.RP_MONTHLY)) {
      String monthlyType = UIRepeatEventForm.RP_MONTHLY_BYMONTHDAY;
      if (repeatEvent.getRepeatByDay() != null && repeatEvent.getRepeatByDay().length > 0) monthlyType = UIRepeatEventForm.RP_MONTHLY_BYDAY;

      if (interval == 1) {
        // pattern = "Monthly on" 
        pattern = getLabel("monthly");
      } else {
        // pattern = "Every {interval} months on
        pattern = getLabel("every-month");
      }

      if (monthlyType.equals(UIRepeatEventForm.RP_MONTHLY_BYDAY)) {
        // pattern = "Monthly on {theNumber} {theDay}
        // pattern = "Every {interval} months on {theNumber} {theDay}
        pattern += " ";
        pattern += getLabel("monthly-by-day");
      } else {
        // pattern = "Monthly on day {theDay}
        // pattern = "Every {interval} months on day {theDay}
        pattern += " ";
        pattern += getLabel("monthly-by-month-day");
      }

      if (endType.equals(RP_END_AFTER)) {
        pattern += ", ";
        pattern += getLabel("count-times");
      }
      if (endType.equals(RP_END_BYDATE)) {
        pattern += ", ";
        pattern += getLabel("until");
      } 

      String theNumber = ""; // the first, the second, the third, ...
      String theDay = ""; // in monthly by day, it's Monday, Tuesday, ... (day of week), in monthly by monthday, it's 1-31 (day of month)
      if (monthlyType.equals(UIRepeatEventForm.RP_MONTHLY_BYDAY)) {
        java.util.Calendar temp = CalendarUtils.getInstanceOfCurrentCalendar();
        temp.setTime(repeatEvent.getFromDateTime());
        temp.setFirstDayOfWeek(1);
        int weekOfMonth = temp.get(java.util.Calendar.WEEK_OF_MONTH);
        java.util.Calendar temp2 = CalendarUtils.getInstanceOfCurrentCalendar();
        temp2.setTime(temp.getTime());
        temp2.add(java.util.Calendar.DATE, 7);
        if (temp2.get(java.util.Calendar.MONTH) != temp.get(java.util.Calendar.MONTH)) weekOfMonth = 5;
        int dayOfWeek = temp.get(java.util.Calendar.DAY_OF_WEEK);
        String[] weekOfMonths = new String[] {getLabel("summary-the-first"), getLabel("summary-the-second"), getLabel("summary-the-third"),
            getLabel("summary-the-fourth"), getLabel("summary-the-last")};
        theNumber = weekOfMonths[weekOfMonth-1];
        theDay = dayOfWeeks[dayOfWeek];
      } else {
        java.util.Calendar temp = CalendarUtils.getInstanceOfCurrentCalendar();
        temp.setTime(repeatEvent.getFromDateTime());
        int dayOfMonth = temp.get(java.util.Calendar.DAY_OF_MONTH);
        theDay = String.valueOf(dayOfMonth);
      }
      summary = pattern.replace("{interval}", String.valueOf(interval)).replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
          .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate())).replace("{theDay}", theDay).replace("{theNumber}", theNumber);
      return summary;
    }

    if (repeatType.equals(CalendarEvent.RP_YEARLY)) {
      if (interval == 1) {
        // pattern = "Yearly on {theDay}"
        pattern = getLabel("yearly");
      } else {
        // pattern = "Every {interval} years on {theDay}" 
        pattern = getLabel("every-year");
      }

      if (endType.equals(RP_END_AFTER)) {
        // pattern = "Yearly on {theDay}, {count} times"
        // pattern = "Every {interval} years on {theDay}, {count} times" 
        pattern += ", ";
        pattern += getLabel("count-times");
      }
      if (endType.equals(RP_END_BYDATE)) {
        // pattern = "Yearly on {theDay}, until {until}"
        // pattern = "Every {interval} years on {theDay}, until {until}" 
        pattern += ", ";
        pattern += getLabel("until");
      }

      String theDay = format.format(repeatEvent.getFromDateTime()); //
      summary = pattern.replace("{interval}", String.valueOf(interval)).replace("{count}", String.valueOf(repeatEvent.getRepeatCount()))
          .replace("{until}", repeatEvent.getRepeatUntilDate()==null?"":format.format(repeatEvent.getRepeatUntilDate())).replace("{theDay}", theDay);
      return summary;
    }
    return summary;
  }

  static  public class AddCategoryActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      UIEventCategoryManager categoryMan =  uiChildPopup.activate(UIEventCategoryManager.class, 470) ;
      categoryMan.categoryId_ = uiForm.getEventCategory() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class RemoveEmailActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      uiForm.setEmailAddress(uiForm.getEmailAddress());
      Util.getPortalRequestContext().setResponseComplete(true);
    }
  }

  static  public class AddAttachmentActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventDetailTab detailTab = uiForm.getChild(UIEventDetailTab.class);
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      UIAttachFileForm uiAttachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      uiAttachFileForm.setAttSize(uiForm.getTotalAttachment()) ;
      uiAttachFileForm.setLimitNumberOfFiles(UIEventForm.LIMIT_FILE_UPLOAD - detailTab.getAttachments().size());
      uiAttachFileForm.init();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  static  public class RemoveAttachmentActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      if(uiContainer != null) uiContainer.deActivate() ;
      UIEventDetailTab uiEventDetailTab = uiForm.getChild(UIEventDetailTab.class) ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      Attachment attachfile = new Attachment();
      for (Attachment att : uiEventDetailTab.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = att;
        }
      }
      uiEventDetailTab.removeFromUploadFileList(attachfile);
      uiEventDetailTab.refreshUploadFileList() ;
      uiForm.setSelectedTab(TAB_EVENTDETAIL) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class DownloadAttachmentActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      downloadAtt(event, uiForm, true);
    }
  }

  static  public class AddParticipantActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab tabAttender = uiForm.getChildById(TAB_EVENTATTENDER) ;
      String values = uiForm.getParticipantValues() ;
      tabAttender.updateParticipants(values) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(tabAttender) ;       

      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction = uiContainer.getChild(UIPopupAction.class);
      UIPopupContainer uiInvitationContainer= uiPopupAction.createUIComponent(UIPopupContainer.class, null, "UIInvitationContainer");
      uiInvitationContainer.getChild(UIPopupAction.class).setId("UIPopupAction3") ;
      uiInvitationContainer.getChild(UIPopupAction.class).getChild(UIPopupWindow.class).setId("UIPopupWindow");
      UIInvitationForm uiInvitationForm = uiInvitationContainer.addChild(UIInvitationForm.class, null, null);
      uiInvitationForm.setInvitationMsg(uiForm.invitationMsg_) ;
      uiForm.participantList_ = "";
      uiInvitationForm.setParticipantValue(uiForm.participantList_) ;
      uiPopupAction.activate(uiInvitationContainer, 500, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;

      UIEventForm uiEventForm = uiContainer.getChild(UIEventForm.class);
      UIEventShareTab uiEventShareTab =  uiEventForm.getChild(UIEventShareTab.class);
      Long currentPage = uiEventShareTab.getCurrentPage();
      String values = uiUserSelector.getSelectedUsers();
      List<String> currentEmails = new ArrayList<String>() ;
      String [] invitors = uiEventForm.getMeetingInvitation() ;
      if (invitors != null) currentEmails.addAll(Arrays.asList(invitors)) ;
      for (String value : values.split(CalendarUtils.COMMA)) {
        String email = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(value).getEmail() ;
        if (!currentEmails.contains(email)) currentEmails.add(email) ;
        if (!uiEventForm.participants_.keySet().contains(value)) {
          uiEventForm.participants_.put(value, email) ;
        }
      }
      for(Entry<String,String> entry : uiEventForm.participants_.entrySet()){
        if(!uiEventForm.participantStatus_.keySet().contains(entry.getKey())){
          if(uiEventForm.participantStatus_.put(entry.getKey(),STATUS_EMPTY)==null)
            uiEventForm.participantStatusList_.add(uiEventForm.new ParticipantStatus(entry.getKey(),STATUS_EMPTY));
        }
      }
      uiEventShareTab.setParticipantStatusList(uiEventForm.getParticipantStatusList());
      uiEventShareTab.updateCurrentPage(currentPage.intValue());
      ((UIEventAttenderTab)uiEventForm.getChildById(TAB_EVENTATTENDER)).updateParticipants(uiEventForm.getParticipantValues()) ; 
      uiEventForm.setMeetingInvitation(currentEmails.toArray(new String[currentEmails.size()])) ;
      //close select user popup
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }
  static  public class AddUserActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiPopupContainer = uiForm.getParent();  
      uiPopupContainer.deActivate();
      UIPopupWindow uiPopupWindow = uiPopupContainer.getChildById("UIPopupWindowAddUserEventForm") ;
      if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UIPopupWindowAddUserEventForm", "UIPopupWindowAddUserEventForm") ;
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

  static  public class MoveNextActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventDetailTab uiEventDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
      UIEventAttenderTab uiEventAttenderTab =  uiForm.getChildById(TAB_EVENTATTENDER) ;
      uiEventAttenderTab.moveNextDay() ;
      if(uiEventAttenderTab.isCheckFreeTime()) {
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_FROM).setCalendar(uiEventAttenderTab.calendar_) ;
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_TO).setCalendar(uiEventAttenderTab.calendar_) ;
      }
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventDetailTab uiEventDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
      UIEventAttenderTab uiEventAttenderTab =  uiForm.getChildById(TAB_EVENTATTENDER) ;
      uiEventAttenderTab.movePreviousDay() ;
      if(uiEventAttenderTab.isCheckFreeTime()) {
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_FROM).setCalendar(uiEventAttenderTab.calendar_) ;
        uiEventDetailTab.getUIFormDateTimePicker(UIEventDetailTab.FIELD_TO).setCalendar(uiEventAttenderTab.calendar_) ;
      }
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class DeleteUserActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab tabAttender = uiForm.getChildById(TAB_EVENTATTENDER) ;
      String values = uiForm.getParticipantValues() ;
      tabAttender.updateParticipants(values) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(tabAttender) ;
      StringBuffer newPars = new StringBuffer() ;
      for(String id : tabAttender.getParticipants()){
        UICheckBoxInput input = tabAttender.getUICheckBoxInput(id) ;
        if(input != null) {
          if( input.isChecked()) {
            tabAttender.parMap_.remove(id) ;
            uiForm.participantStatus_.remove(id);
            for(Iterator<ParticipantStatus> i = uiForm.participantStatusList_.iterator(); i.hasNext();){
              ParticipantStatus participantStatus = i.next();
              if(id.equalsIgnoreCase(participantStatus.getParticipant()))
                i.remove();
            }
            uiForm.participants_.remove(id);
            uiForm.removeChildById(id) ; 

            List<String> currentEmails = new ArrayList<String>() ;
            String [] invitors = uiForm.getMeetingInvitation() ;
            if (invitors != null) {
              currentEmails.addAll(Arrays.asList(invitors)) ;
              currentEmails.remove(CalendarUtils.getOrganizationService().getUserHandler().findUserByName(id.trim()).getEmail()) ;
              uiForm.setMeetingInvitation(currentEmails.toArray(new String[currentEmails.size()])) ;
            }
          }else {
            if(!CalendarUtils.isEmpty(newPars.toString())) newPars.append(CalendarUtils.BREAK_LINE) ;
            newPars.append(id) ;
          }
        }
      }
      uiForm.getChild(UIEventShareTab.class).setParticipantStatusList(uiForm.getParticipantStatusList());
      uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTATTENDER)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(TAB_EVENTSHARE)) ;
    }
  }

  static  public class SaveActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;

      UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class);
      if(!uiForm.isReminderValid()) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage(uiForm.errorMsg_, new String[] {uiForm.errorValues}, AbstractApplicationMessage.WARNING));
        uiForm.setSelectedTab(TAB_EVENTREMINDER) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
        return ;
      }
      else {
        CalendarService calService = CalendarUtils.getCalendarService();
        if(calService.isRemoteCalendar(CalendarUtils.getCurrentUser(), uiForm.getCalendarId())) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, AbstractApplicationMessage.WARNING));
          return;
        }

        String sendOption = uiForm.getSendOption();
        List<ParticipantStatus>  lstPart = ((UIEventShareTab)uiForm.getChildById(TAB_EVENTSHARE)).getData();
        if(CalendarSetting.ACTION_ASK.equalsIgnoreCase(sendOption)){
          // Show Confirm
          UIPopupAction pAction = uiPopupContainer.getChild(UIPopupAction.class) ;
          UIConfirmForm confirmForm =  pAction.activate(UIConfirmForm.class, 480);
          if(lstPart.isEmpty()){
            confirmForm.setConfirmMessage(uiForm.saveEventNoInvitation);
          }else{
            confirmForm.setConfirmMessage(uiForm.saveEventInvitation);
          }
          confirmForm.setConfirmMessage("update-recurrence-event-confirm-msg");
          confirmForm.setDelete(false);
          confirmForm.setConfig_id(uiForm.getId()) ;

          /* String[] actions;
        if (CalendarUtils.isEmpty(uiForm.getParticipantValues()) && CalendarUtils.isEmpty(uiForm.getInvitationEmail())) {
          actions = new String[] {"ConfirmCancel"};
        } else {
          actions = new String[] {"ConfirmOK","ConfirmCancel"};
        }

        confirmForm.setActions(actions);
           */
          event.getRequestContext().addUIComponentToUpdateByAjax(pAction) ;
        }
        else {
          CalendarSetting calSetting = uiPortlet.getCalendarSetting();
          Date fromDate = uiForm.getEventFromDate(calSetting.getDateFormat(), calSetting.getTimeFormat()) ;

          // if it's a virtual recurrence
          CalendarEvent occurrence = uiForm.calendarEvent_;
          if (occurrence != null && !CalendarEvent.RP_NOREPEAT.equals(occurrence.getRepeatType())
              && !CalendarUtils.isEmpty(occurrence.getRecurrenceId()) && CalendarUtils.isSameDate(fromDate, occurrence.getFromDateTime()) ) {
            // popup confirm form
            UIConfirmForm confirmForm =  uiPopupAction.activate(UIConfirmForm.class, 600);
            confirmForm.setConfirmMessage(uiForm.getLabel("update-recurrence-event-confirm-msg"));
            confirmForm.setConfig_id(uiForm.getId()) ;

            String[] actions = new String[] {"ConfirmUpdateOnlyInstance", "ConfirmUpdateAllSeries", "ConfirmUpdateCancel"};
            confirmForm.setActions(actions);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
          } else { 
            if(CalendarSetting.ACTION_ALWAYS.equalsIgnoreCase(sendOption))
              uiForm.saveAndNoAsk(event, true, false);
            else
              uiForm.saveAndNoAsk(event, false, false);
          }            
        }
      }
    }
  }
  static  public class OnChangeActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIEventAttenderTab attendTab = uiForm.getChildById(TAB_EVENTATTENDER) ;
      UIFormInputWithActions eventShareTab = uiForm.getChildById(TAB_EVENTSHARE) ;
      String values = uiForm.getParticipantValues() ;

      boolean isCheckFreeTime = attendTab.getUICheckBoxInput(UIEventAttenderTab.FIELD_CHECK_TIME).isChecked() ;
      if(CalendarUtils.isEmpty(values)) {
        if(isCheckFreeTime) attendTab.getUICheckBoxInput(UIEventAttenderTab.FIELD_CHECK_TIME).setChecked(false) ;
        uiForm.setSelectedTab(TAB_EVENTATTENDER) ;
        attendTab.updateParticipants(values) ;

        uiForm.setParticipant(values) ;
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIEventForm.msg.participant-required", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(eventShareTab) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(attendTab) ;
      } else {
        StringBuilder sb1 = new StringBuilder() ;
        StringBuilder sb2 = new StringBuilder() ;
        for(String uName : values.split(CalendarUtils.BREAK_LINE)) {
          User user = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(uName.trim()) ;
          if(user != null) {
            if(sb1 != null && sb1.length() > 0) sb1.append(CalendarUtils.BREAK_LINE) ;
            sb1.append(uName.trim()) ;
          } else {
            if(sb2 != null && sb2.length() > 0) sb2.append(CalendarUtils.BREAK_LINE) ;
            sb2.append(uName.trim()) ;
          }
        }
        attendTab.updateParticipants(sb1.toString());
        uiForm.setParticipant(values) ;
        if(sb2.length() > 0) {
          if(isCheckFreeTime) attendTab.getUICheckBoxInput(UIEventAttenderTab.FIELD_CHECK_TIME).setChecked(false) ;
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIEventForm.msg.name-not-correct", new Object[]{sb2.toString()}));
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(eventShareTab) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(attendTab) ;
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      UIEventDetailTab uiDetailTab = uiForm.getChildById(TAB_EVENTDETAIL) ;
      for (Attachment att : uiDetailTab.getAttachments()) {
        UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class), att.getResourceId()) ;
      }
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }


  static public class ConfirmOKActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiEventForm = event.getSource();
      uiEventForm.confirmSaveEvent(event, true);      
    }
  }

  static public class  ConfirmCancelActionListener extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiEventForm = event.getSource();
      uiEventForm.confirmSaveEvent(event, false);
    }
  }

  public static class EditRepeatActionListener extends EventListener<UIEventForm> {

    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiContainer.getChild(UIPopupAction.class) ;
      UIRepeatEventForm repeatEventForm =  uiChildPopup.activate(UIRepeatEventForm.class, 480) ;
      repeatEventForm.init(uiForm.repeatEvent);
      if(uiForm.isAddNew_) {
        java.util.Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(uiForm.getEventFromDate());
        repeatEventForm.setWeeklyByDay(repeatEventForm.convertToDayOfWeek(cal.get(java.util.Calendar.DAY_OF_WEEK)));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }

  public static class ConfirmUpdateOnlyInstance extends EventListener<UIEventForm> {

    @Override
    public void execute(Event<UIEventForm> event) throws Exception {      
      UIEventForm uiForm = event.getSource();
      String sendOption = uiForm.getSendOption();
      if (CalendarSetting.ACTION_ALWAYS.equals(sendOption)) {
        uiForm.saveAndNoAsk(event, true, false);
      }
      else {
        // update only this occurrence instance
        uiForm.saveAndNoAsk(event, false, false);
      }
      UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class);
      if(uiPortlet != null) uiPortlet.cancelAction();
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  public static class ConfirmUpdateAllSeries extends EventListener<UIEventForm> {

    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource();
      String sendOption = uiForm.getSendOption();
      uiForm.saveAndNoAsk(event, CalendarSetting.ACTION_ALWAYS.equals(sendOption), true);
    }
  }

  public static class ConfirmUpdateFollowSeries extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource();
      String sendOption = uiForm.getSendOption();
      uiForm.saveAndNoAsk(event, CalendarSetting.ACTION_ALWAYS.equals(sendOption), null);
      UICalendarPortlet uiPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class);
      if(uiPortlet != null) uiPortlet.cancelAction();
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  public static class ConfirmUpdateCancel extends EventListener<UIEventForm> {
    @Override
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiEventForm = event.getSource();
      UIPopupContainer uiPopupContainer = uiEventForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class);
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  /**
   * This method was called when user saves event from event form
   * @param event save event
   * @param isSend send mail or not
   * @throws Exception
   */
  private void confirmSaveEvent(Event<UIEventForm> event, boolean isSend) throws Exception {
    UIEventForm uiEventForm = event.getSource();
    UICalendarPortlet uiPortlet = uiEventForm.getAncestorOfType(UICalendarPortlet.class) ;
    UIPopupContainer uiPopupContainer = uiEventForm.getAncestorOfType(UIPopupContainer.class);
    UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class);
    uiPopupAction.deActivate();

    CalendarSetting calSetting = uiPortlet.getCalendarSetting();
    Date fromDate = uiEventForm.getEventFromDate(calSetting.getDateFormat(), calSetting.getTimeFormat()) ;

    // if it's a virtual recurrence
    CalendarEvent occurrence = uiEventForm.calendarEvent_;
    if (occurrence != null && !CalendarEvent.RP_NOREPEAT.equals(occurrence.getRepeatType()) 
        && !CalendarUtils.isEmpty(occurrence.getRecurrenceId()) && CalendarUtils.isSameDate(fromDate, occurrence.getFromDateTime()) ) {
      // popup confirm form
      UIConfirmForm confirmForm =  uiPopupAction.activate(UIConfirmForm.class, 480);
      confirmForm.setConfirmMessage("update-recurrence-event-confirm-msg");
      confirmForm.setConfig_id(uiEventForm.getId()) ;
      confirmForm.setDelete(false);
      //String[] actions = new String[] {"ConfirmUpdateOnlyInstance", "ConfirmUpdateAllSeries", "ConfirmUpdateCancel"};
      //confirmForm.setActions(actions);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    } else { 
      uiEventForm.saveAndNoAsk(event, isSend, false);
    }
  }

  public class ParticipantStatus {
    private String participant ;
    private String name ;
    private String email ;
    private String status ;

    public ParticipantStatus(String participant, String status) throws Exception {
      this.participant = participant;
      User user = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(participant);
      if (user != null) {
        this.name = UIEventAttenderTab.getFullname(participant);
        this.email = user.getEmail();
      } else if (participant.matches(CalendarUtils.contactRegex)) {
        this.name = participant.substring(0, participant.lastIndexOf(CalendarUtils.OPEN_PARENTHESIS));
        this.email = participant.substring(participant.lastIndexOf(CalendarUtils.OPEN_PARENTHESIS) + 1)
            .replace(CalendarUtils.CLOSE_PARENTHESIS, "");
      } else {
        this.name = participant;
        this.email = participant;
      }
      this.status = status;
    }

    public String getParticipant() throws Exception {
      return participant;
    }

    public void setParticipant(String participant) {
      this.participant = participant;
    }

    public String getName() throws Exception {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() throws Exception {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }
}
