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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.UIActionBar;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarView;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.calendar.webui.UIListView;
import org.exoplatform.calendar.webui.UIPreview;
import org.exoplatform.calendar.webui.UIWeekView;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class),
      @EventConfig(listeners = UIAdvancedSearchForm.OnchangeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent{
  private static final Log log = ExoLogger.getExoLogger(UIAdvancedSearchForm.class);
  
  final static  private String TEXT = "text" ;
  final static  private String TYPE = "type" ;
  final static  private String CALENDAR = "calendar" ;
  final static  private String CATEGORY = "category" ;
  final static  private String PRIORITY = "priority" ;
  final static  private String STATE = "state" ;
  final static  private String FROMDATE = "fromDate" ;
  final static  private String TODATE = "toDate" ;

  public UIAdvancedSearchForm() throws Exception{
    addChild(new UIFormStringInput(TEXT, TEXT, "").addValidator(SpecialCharacterValidator.class)) ;
    List<SelectItemOption<String>> types = new ArrayList<SelectItemOption<String>>() ;
    types.add(new SelectItemOption<String>("", "")) ;
    types.add(new SelectItemOption<String>(CalendarEvent.TYPE_EVENT, CalendarEvent.TYPE_EVENT)) ;
    types.add(new SelectItemOption<String>(CalendarEvent.TYPE_TASK, CalendarEvent.TYPE_TASK)) ;
    UIFormSelectBox type =  new UIFormSelectBox(TYPE, TYPE, types) ;
    type.setOnChange("Onchange") ;
    addChild(type) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    String username = CalendarUtils.getCurrentUser() ;
    CalendarService cservice = CalendarUtils.getCalendarService() ;
    options.add(new SelectItemOption<String>("", "")) ;
    String[] groupIds = CalendarUtils.getUserGroups(username);
    if(UICalendarPortlet.isInSpace()){
      groupIds = new String[]{UICalendarPortlet.getGroupIdOfSpace()};
    } else {
      for(Calendar cal : cservice.getUserCalendars(username, true)) {
        options.add(new SelectItemOption<String>(cal.getName(), Calendar.TYPE_PRIVATE + CalendarUtils.COLON + cal.getId())) ;
      }
      GroupCalendarData sharedData  = cservice.getSharedCalendars(CalendarUtils.getCurrentUser(), true) ;
      if(sharedData != null) {
        for(Calendar cal : sharedData.getCalendars()) {
          String owner = "" ;
          if(cal.getCalendarOwner() != null) owner = cal.getCalendarOwner() + "- " ;
          options.add(new SelectItemOption<String>(owner + cal.getName(), Calendar.TYPE_SHARED + CalendarUtils.COLON + cal.getId())) ;
        }
      }
    }
    List<GroupCalendarData> groupCals  = cservice.getGroupCalendars(groupIds, true, username) ;
    for(GroupCalendarData groupData : groupCals) {
      String groupName = groupData.getName();
      if(groupData != null) {
        for(Calendar cal : groupData.getCalendars()) {
          options.add(new SelectItemOption<String>(CalendarUtils.getGroupCalendarName(groupName.substring(
            groupName.lastIndexOf("/") + 1),cal.getName()), Calendar.TYPE_PUBLIC + CalendarUtils.COLON + cal.getId())) ;
        }
      }
    }
    addChild(new UIFormSelectBox(CALENDAR, CALENDAR, options)) ;
    options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("", "")) ;
    for(EventCategory cat : cservice.getEventCategories(CalendarUtils.getCurrentUser())) {
      // Check if EventCategory is default event category
      boolean isDefaultEventCategory = false;
      for (int i = 0; i < NewUserListener.defaultEventCategoryIds.length; i++) {
        if (cat.getId().equals(NewUserListener.defaultEventCategoryIds[i])
            && cat.getName().equals(NewUserListener.defaultEventCategoryNames[i])) {
          isDefaultEventCategory = true;
          break;
        }
      }
      
      if (isDefaultEventCategory) {
        String newName = CalendarUtils.getResourceBundle("UICalendarView.label." + cat.getId(), cat.getId());
        options.add(new SelectItemOption<String>(newName, cat.getId())) ;
        cat.setName(newName);
      } else {
        options.add(new SelectItemOption<String>(cat.getName(), cat.getId())) ;        
      }
    }
    addChild(new UIFormSelectBox(CATEGORY, CATEGORY, options)) ;
    addChild(new UIFormSelectBox(STATE, STATE, getStatus()).setRendered(false)) ;
    addChild(new UIFormSelectBox(PRIORITY, PRIORITY, getPriority())) ;
    addChild(new UIFormDateTimePicker(FROMDATE, FROMDATE, null, false)) ;
    addChild(new UIFormDateTimePicker(TODATE, TODATE, null, false)) ;
  }
  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {

  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);
    //autofocus the input
    RequireJS requireJS = context.getJavascriptManager().getRequireJS();
    requireJS.require("PORTLET/calendar/CalendarPortlet","cal");
    requireJS.addScripts("cal.UICalendarPortlet.autoFocusFirstInput('"+getId()+"');");
  }
  
  public void setSearchValue(String searchValue) {
    getUIStringInput(TEXT).setValue(searchValue) ;
  }  
  public UIFormDateTimePicker getUIFormDateTimePicker(String id){
    return findComponentById(id) ;
  }
  public String getFromDateValue() {
    return getUIFormDateTimePicker(FROMDATE).getValue() ;
  }
  public String getToDateValue() {
    return getUIFormDateTimePicker(TODATE).getValue() ;
  }
  public Date getFromDate() {
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    if(getFromDateValue() != null) 
      try {
        return df.parse(getFromDateValue()) ;
      }  catch (ParseException e) {
        return null ;
      }
      return null ;
  }
  public Date getToDate() {
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    if(getToDateValue() != null) 
      try {
        return df.parse(getToDateValue()) ;
      }  catch (ParseException e) {
        return null ;
      }
      return null ;
  }
  private List<SelectItemOption<String>> getPriority() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("", "")) ;
    options.add(new SelectItemOption<String>("normal", "normal")) ;
    options.add(new SelectItemOption<String>("high", "high")) ;
    options.add(new SelectItemOption<String>("low", "low")) ;
    return options ;
  }

  private List<SelectItemOption<String>> getStatus() {
    List<SelectItemOption<String>> status = new ArrayList<SelectItemOption<String>>() ;
    status.add(new SelectItemOption<String>("", "")) ;
    for(String taskStatus : CalendarEvent.TASK_STATUS) {
      status.add(new SelectItemOption<String>(taskStatus, taskStatus)) ;
    }
    return status ;
  }
  public String[] getPublicCalendars() throws Exception{
    String[] groups = CalendarUtils.getUserGroups(CalendarUtils.getCurrentUser()) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    Map<String, String> map = new HashMap<String, String> () ;    
    for(GroupCalendarData group : calendarService.getGroupCalendars(groups, true, CalendarUtils.getCurrentUser())) {
      for(org.exoplatform.calendar.service.Calendar calendar : group.getCalendars()) {
        map.put(calendar.getId(), calendar.getId()) ;          
      }
    }
    return map.values().toArray(new String[map.values().size()] ) ;
  }

  public boolean isSearchTask() {
    return getUIFormSelectBox(TYPE).getValue().equals(CalendarEvent.TYPE_TASK) ; 
  }
  public String getTaskState() {
    return getUIFormSelectBox(STATE).getValue() ;
  }
  @Override
  public String[] getActions() {
    return new String[]{"Search","Cancel"} ;
  }
  public Boolean isValidate(){
    String value = getUIStringInput(TEXT).getValue();
    if(value == null) value = "" ;
    StringBuilder formData = new StringBuilder();
    formData.append(value);
    formData.append(getUIFormSelectBox(TYPE).getValue());
    formData.append(getUIFormSelectBox(CALENDAR).getValue());
    formData.append(getUIFormSelectBox(CATEGORY).getValue());
    formData.append(getUIFormSelectBox(PRIORITY).getValue());
    formData.append(getFromDateValue());
    formData.append(getToDateValue());
    return !CalendarUtils.isEmpty(formData.toString());
  }
  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    @Override
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiForm = event.getSource() ;
      
      String fromDateValue = uiForm.getFromDateValue();
      Date fromDate = uiForm.getFromDate();
      Date toDate = uiForm.getToDate();
      if(!CalendarUtils.isEmpty(fromDateValue) && fromDate == null){
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.from-date-time-invalid", null)) ;
        return ; 
      }
      if(!CalendarUtils.isEmpty(uiForm.getToDateValue()) && uiForm.getToDate() == null)  {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.to-date-time-invalid", null)) ;
        return ;
      }
      
      if(fromDate != null && toDate != null) {
        if(fromDate.after(toDate)){
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.date-time-invalid", null)) ;
          return ;
        }
      }

      String text = uiForm.getUIStringInput(UIAdvancedSearchForm.TEXT).getValue() ;
      if(!uiForm.isValidate()){
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISearchForm.msg.no-text-to-search", null)) ;
        return ;
      }
      try {
        EventQuery query = new EventQuery() ;
        if(!CalendarUtils.isEmpty(text)) query.setText(CalendarUtils.encodeJCRText(text)) ;
        query.setEventType(uiForm.getUIFormSelectBox(UIAdvancedSearchForm.TYPE).getValue()) ;
        if(uiForm.isSearchTask()) query.setState(uiForm.getTaskState()) ; 
        String calendarId = uiForm.getUIFormSelectBox(UIAdvancedSearchForm.CALENDAR).getValue() ;
        UICalendars uiCalendars = uiForm.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UICalendars.class);
        List<String> checkedCals = uiCalendars.getCheckedCalendars() ;
        if(calendarId != null && calendarId.trim().length() > 0){
          try {
            query.setCalendarId(new String[]{calendarId.split(CalendarUtils.COLON)[1].trim()}) ;            
          } catch (ArrayIndexOutOfBoundsException e) {
            if (log.isDebugEnabled()) {
              log.debug("Fail to set calendar id", e);
            }
          }
        }

        String categoryId = uiForm.getUIFormSelectBox(UIAdvancedSearchForm.CATEGORY).getValue() ;
        if(categoryId != null && categoryId.trim().length() > 0) query.setCategoryId(new String[]{categoryId}) ;
        java.util.Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
        if(fromDate != null && toDate != null) {
          cal.setTime(fromDate);
          query.setFromDate(CalendarUtils.getBeginDay(cal)) ;
          cal.setTime(toDate) ;
          query.setToDate(CalendarUtils.getEndDay(cal)) ;
        } else if (fromDate !=null) {
          cal.setTime(fromDate) ;
          query.setFromDate(CalendarUtils.getBeginDay(cal)) ;
        } else if (toDate !=null) {
          cal.setTime(toDate) ;
          query.setToDate(CalendarUtils.getEndDay(cal)) ;
        }
        String priority = uiForm.getUIFormSelectBox(UIAdvancedSearchForm.PRIORITY).getValue() ;
        if(priority != null && priority.trim().length() > 0) query.setPriority(priority) ;
        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        
        UICalendarViewContainer calendarViewContainer = 
          calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        String currentView = calendarViewContainer.getRenderedChild().getId() ;
        if(calendarViewContainer.getRenderedChild() instanceof UIWeekView) {
          if(((UIWeekView)calendarViewContainer.getRenderedChild()).isShowCustomView()) currentView = UICalendarViewContainer.WORKING_VIEW;
        }
        calendarViewContainer.initView(UICalendarViewContainer.LIST_VIEW) ;
        UIListView uiListView = calendarViewContainer.findFirstComponentOfType(UIListView.class) ;
        uiListView.setViewType(UICalendarView.TYPE_BOTH);
        uiListView.setSortedField(UIListView.EVENT_START);
        uiListView.setIsAscending(false);
        calendarPortlet.cancelAction();
        
        if (query.getCalendarId() == null) {
          List<String> calendarIds = new ArrayList<String>() ;
            for (org.exoplatform.calendar.service.Calendar calendar : uiCalendars.getAllPrivateCalendars()) {
              if (checkedCals.contains(calendar.getId())) calendarIds.add(calendar.getId());
            }
            for (org.exoplatform.calendar.service.Calendar  calendar : uiCalendars.getAllPublicCalendars()) {
              if (checkedCals.contains(calendar.getId())) calendarIds.add(calendar.getId());
            }

          GroupCalendarData shareClas = uiCalendars.getSharedCalendars();
          if (shareClas != null) {
            for (org.exoplatform.calendar.service.Calendar calendar : shareClas.getCalendars()) {
              if (checkedCals.contains(calendar.getId())) calendarIds.add(calendar.getId());
            }
          }

          if (calendarIds.size() > 0) {
            query.setCalendarId(calendarIds.toArray(new String[] {}));
          }
          else {
            query.setCalendarId(new String[] {"null"});
          }          
        }
        
        query.setOrderBy(new String[] { Utils.EXO_FROM_DATE_TIME });
        query.setOrderType(Utils.DESCENDING);
        uiListView.setEventQuery(query);
        uiListView.setDisplaySearchResult(true) ;
        List<CalendarEvent> allEvents = uiListView.getAllEvents(query);
        uiListView.update(new EventPageList(allEvents,10));
        calendarViewContainer.setRenderedChild(UICalendarViewContainer.LIST_VIEW) ;        
        if(!uiListView.isDisplaySearchResult()) uiListView.setLastViewId(currentView) ;
        uiListView.setSelectedEvent(null) ;
        uiListView.setLastUpdatedEventId(null) ;
        calendarViewContainer.findFirstComponentOfType(UIPreview.class).setEvent(null) ;
        UIActionBar uiActionBar = calendarPortlet.findFirstComponentOfType(UIActionBar.class) ;
        uiActionBar.setCurrentView(UICalendarViewContainer.LIST_VIEW) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarViewContainer) ;
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class SearchActionListener", e);
        }
        return ;
      }
    }
  }
  static  public class OnchangeActionListener extends EventListener<UIAdvancedSearchForm> {
    @Override
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiForm = event.getSource() ;
      uiForm.getUIFormSelectBox(STATE).setRendered(uiForm.isSearchTask()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    @Override
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
}
