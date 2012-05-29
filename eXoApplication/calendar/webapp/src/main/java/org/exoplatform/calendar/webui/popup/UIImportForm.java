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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.ext.UIFormColorPicker;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/calendar/webui/UIPopup/UIImportForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UIImportForm.SaveActionListener.class),  
                   @EventConfig(listeners = UIImportForm.ImportActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIImportForm.AddActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIImportForm.CancelActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIImportForm.SelectPermissionActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIImportForm.OnChangeActionListener.class, phase = Phase.DECODE)
                 }
)
public class UIImportForm extends UIForm implements UIPopupComponent, UISelector{
  private static final Log log = ExoLogger.getLogger(UIImportForm.class);
  
  final public static String DISPLAY_NAME = "displayName" ;
  final public static String DESCRIPTION = "description" ;
  final public static String CATEGORY = "category" ;
  final public static String PERMISSION = "permission" ;
  final public static String SELECT_COLOR = "selectColor" ;
  final public static String TIMEZONE = "timeZone" ;
  final public static String LOCALE = "locale" ;
  final public static String PERMISSION_SUB = "_permission".intern() ;
  public Map<String, Map<String, String>> perms_ = new HashMap<String, Map<String, String>>() ;

  final static public String TYPE = "type".intern() ;
  final static public String FIELD_UPLOAD = "upload".intern() ;
  final static public String FIELD_TO_CALENDAR = "impotTo".intern() ;
  final static public String ONCHANGE = "OnChange".intern() ;
  final static public int UPDATE_EXIST = 1 ;
  final static public int ADD_NEW = 0 ;
  protected int flag_ = -1 ;

  public UIImportForm() throws Exception {
    this.setMultiPart(true) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    for(String type : calendarService.getExportImportType()) {
      options.add(new SelectItemOption<String>(type, type)) ;
    }
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
    addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD, true));
    UIFormSelectBoxWithGroups privateCal = new UIFormSelectBoxWithGroups(FIELD_TO_CALENDAR, FIELD_TO_CALENDAR, CalendarUtils.getCalendarOption()) ;
    addUIFormInput(privateCal);
    addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class));
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null));
    UIFormSelectBoxWithGroups calCategory = new UIFormSelectBoxWithGroups(CATEGORY, CATEGORY, CalendarUtils.getCalendarCategoryOption());
    calCategory.setOnChange("OnChange");
    addUIFormInput(calCategory);
    addUIFormInput(new UIFormStringInput(PERMISSION, PERMISSION, null));
    CalendarSetting setting = CalendarUtils.getCurrentUserCalendarSetting();
    UIFormStringInput locale = new UIFormStringInput(LOCALE, LOCALE, CalendarUtils.getLocationDisplayString(setting.getLocation())) ;
    locale.setLabel(setting.getLocation());
    locale.setEditable(false);
    addUIFormInput(locale);    
    UIFormStringInput timeZones = new UIFormStringInput(TIMEZONE, TIMEZONE, CalendarUtils.generateTimeZoneLabel(setting.getTimeZone())) ;
    timeZones.setEditable(false);
    timeZones.setLabel(setting.getTimeZone());
    addUIFormInput(timeZones);
    addUIFormInput(new UIFormColorPicker(SELECT_COLOR, SELECT_COLOR));
  }

  public void init(String calId, String calType) {
    if(!CalendarUtils.isEmpty(calId)) {
      UIFormSelectBoxWithGroups selectBox = getUIFormSelectBoxGroup(FIELD_TO_CALENDAR) ;
      if(selectBox.getOptions()!= null && !selectBox.getOptions().isEmpty()) {
        switchMode(UPDATE_EXIST);
        selectBox.setValue(calType +CalendarUtils.COLON + calId) ;
      } else {
        switchMode(ADD_NEW);
      }
    } else {
      switchMode(ADD_NEW);
    } 
  }

  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  
  @SuppressWarnings("unchecked")
  private List getSelectedGroups(String groupId) throws Exception {
    List groups = new ArrayList() ;
    Group g = (Group)getApplicationComponent(OrganizationService.class).getGroupHandler().findGroupById(groupId) ;
    groups.add(g);
    return groups;
  }
  
  public String[] getActions(){
    return new String[]{"Save", "Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public List<SelectItemOption<String>> getPrivateCalendars() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    try {
      for(Calendar c : CalendarUtils.getCalendarService().getUserCalendars(CalendarUtils.getCurrentUser(), true)){
        if (c.getId().equals(Utils.getDefaultCalendarId(CalendarUtils.getCurrentUser())) && c.getName().equals(NewUserListener.defaultCalendarName)) {
          String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);
          c.setName(newName);
        }
        options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
      }
    } catch (Exception e) {
      log.debug("Failed to get private calendars.", e);
    }
    return options ;
  }
  public boolean isNew() {
    return flag_ == ADD_NEW ;
  }
  protected String getSelectedTypeGroup() {
    UIFormSelectBoxWithGroups calCategory =  getUIFormSelectBoxGroup(CATEGORY) ;
    String value = calCategory.getValue() ;
    if (!CalendarUtils.isEmpty(value) && value.split(CalendarUtils.COLON).length>0) {
      return value.split(CalendarUtils.COLON)[0] ;      
    } 
    return value ;
  }
  protected String getSelectedIdGroup() {
    UIFormSelectBoxWithGroups calCategory =  getUIFormSelectBoxGroup(CATEGORY) ;
    String value = calCategory.getValue() ;
    if (!CalendarUtils.isEmpty(value) && value.split(CalendarUtils.COLON).length>0) {
      return value.split(CalendarUtils.COLON)[1] ;      
    } 
    return value ;
  }
  protected String getDescription() {
    return getUIFormTextAreaInput(DESCRIPTION).getValue() ;
  }

  protected String getSelectedColor() {
    return getChild(UIFormColorPicker.class).getValue() ;
  }

  protected String getTimeZone() {
    return getUIStringInput(TIMEZONE).getLabel();
  }

  protected String getLocale() {
    return getUIStringInput(LOCALE).getLabel();
  }
  public void switchMode(int flag) {
    flag_ = flag ;
    if(flag == UPDATE_EXIST) {
      getUIFormSelectBoxGroup(FIELD_TO_CALENDAR).setRendered(true);
      getUIStringInput(DISPLAY_NAME).setRendered(false);
      getUIFormTextAreaInput(DESCRIPTION).setRendered(false);
      getUIFormSelectBoxGroup(CATEGORY).setRendered(false);
      getUIStringInput(PERMISSION).setRendered(false);
      getUIStringInput(TIMEZONE).setRendered(false);
      getUIStringInput(LOCALE).setRendered(false);
      getChild(UIFormColorPicker.class).setRendered(false);
    } else if(flag == ADD_NEW) {
      getUIFormSelectBoxGroup(FIELD_TO_CALENDAR).setRendered(false);
      getUIStringInput(DISPLAY_NAME).setRendered(true);
      getUIFormTextAreaInput(DESCRIPTION).setRendered(true);
      getUIFormSelectBoxGroup(CATEGORY).setRendered(true);
      String groupType = getSelectedTypeGroup();
      if(!CalendarUtils.isEmpty(groupType)&& groupType.equals(CalendarUtils.PUBLIC_TYPE))
        getUIStringInput(PERMISSION).setRendered(true);
      else
        getUIStringInput(PERMISSION).setRendered(false);
      getUIStringInput(TIMEZONE).setRendered(true);
      getUIStringInput(LOCALE).setRendered(true);
      getChild(UIFormColorPicker.class).setRendered(true);
    } else {
      log.warn("Wrong flag(" +flag+ ") only UPDATE_EXIST(1) or ADD_NEW(0) accept ");
    }
  }

  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id) ;
  }
  
  protected String getCalendarId() {
    UIFormSelectBoxWithGroups calendars =  getUIFormSelectBoxGroup(FIELD_TO_CALENDAR) ;
    String value = calendars.getValue() ;
    if (!CalendarUtils.isEmpty(value) && value.split(CalendarUtils.COLON).length>0) {
      return value.split(CalendarUtils.COLON)[1] ;      
    } 
    return value ;
  }
  
  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField);
    StringBuilder sb = new StringBuilder() ;
    Map<String, String> temp = new HashMap<String, String>() ;
    String key = value.substring(0, value.lastIndexOf(CalendarUtils.COLON_SLASH)-1);
    String tempS = value.substring(value.lastIndexOf(CalendarUtils.COLON_SLASH) + 2) ;
    if(perms_.get(selectField) == null) {
      temp.put(key, tempS) ;
    } else {
      temp = perms_.get(selectField) ;
      if(temp.get(key) != null && !tempS.equals(temp.get(key))) tempS = temp.get(key) + CalendarUtils.COMMA +  tempS ;
      temp.put(key, tempS) ;
    }
    perms_.put(selectField, temp) ;
    Map<String, String> tempMap = new HashMap<String, String>() ;
    for(String s : temp.values()) {
      for(String t : s.split(CalendarUtils.COMMA)) {
        tempMap.put(t, t) ;
      }
    }
    for(String s : tempMap.values()) {
      if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
      sb.append(s) ;
    }
    fieldInput.setValue(sb.toString());
  }
  
  static  public class SaveActionListener extends EventListener<UIImportForm> {
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void execute(Event<UIImportForm> event) throws Exception {
      String username = CalendarUtils.getCurrentUser() ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      UIImportForm uiForm = event.getSource() ;
      UIFormUploadInput input = uiForm.getUIInput(FIELD_UPLOAD) ;
      String importFormat = uiForm.getUIFormSelectBox(UIImportForm.TYPE).getValue() ;
      String calendarName = uiForm.getUIStringInput(UIImportForm.DISPLAY_NAME).getValue() ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      
      UploadResource resource = uploadService.getUploadResource(input.getUploadId()) ;
      if(resource == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.file-name-error", null));
        return ;
      }
      try {
        if(calendarService.getCalendarImportExports(importFormat).isValidate(input.getUploadDataAsStream())) {
          if(uiForm.isNew()) {
            if(CalendarUtils.isEmpty(calendarName)) {
              calendarName = resource.getFileName() ;
            } 
            List<Calendar> pCals = calendarService.getUserCalendars(username, true) ;
            for(Calendar cal : pCals) {
              if (cal.getId().equals(Utils.getDefaultCalendarId(username)) && cal.getName().equals(NewUserListener.defaultCalendarName)) {
                String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);
                cal.setName(newName);
              }
              if(cal.getName().trim().equalsIgnoreCase(calendarName)) {
                event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{calendarName}, ApplicationMessage.WARNING)) ;
                return ;
              }
            }
            Calendar calendar = new Calendar() ;
            calendar.setName(calendarName) ;
            calendar.setDescription(uiForm.getDescription()) ;
            calendar.setLocale(uiForm.getLocale()) ;
            calendar.setTimeZone(uiForm.getTimeZone()) ;
            calendar.setCalendarColor(uiForm.getSelectedColor()) ;
            calendar.setCalendarOwner(username) ;
            if(uiForm.getSelectedTypeGroup().equals(CalendarUtils.PRIVATE_TYPE)){
              calendar.setPublic(false) ;
              calendar.setCategoryId(uiForm.getSelectedIdGroup()) ;
              calendarService.saveUserCalendar(username, calendar, true) ;
              calendarService.getCalendarImportExports(importFormat).importCalendar(username, input.getUploadDataAsStream(), calendar.getId(), null, null, null, false) ;
            }
            else {
              calendar.setPublic(true) ;              
              List<String> selected = new ArrayList<String>() ;
              selected.add(uiForm.getSelectedIdGroup());
              
//            CS-3607
              List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(selected.toArray(new String[] {}), false, username) ;
              for (GroupCalendarData groupCalendarData : groupCalendars) {
                for (Calendar calendar2 : groupCalendarData.getCalendars()) {
                  if(calendar2.getName().equalsIgnoreCase(calendarName.trim())) {
                    event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{calendarName}, ApplicationMessage.WARNING)) ;
                    return ;
                  }
                }
              }
              calendar.setGroups(selected.toArray((new String[]{})));              
              OrganizationService orgService = CalendarUtils.getOrganizationService() ;
              String groupKey = uiForm.getSelectedIdGroup() + CalendarUtils.SLASH_COLON ;
              String typedPerms = uiForm.getUIStringInput(PERMISSION).getValue();
              List<String> listPermission = UICalendarForm.getPermissions(new ArrayList<String>(),
                                                                          typedPerms,
                                                                          orgService,
                                                                          uiForm.getSelectedIdGroup(),
                                                                          groupKey,
                                                                          event);
              
              if (listPermission == null) return;
              Collection<Membership> mbsh = CalendarUtils.getOrganizationService().getMembershipHandler().findMembershipsByUser(username) ;
              if(!listPermission.contains(groupKey + CalendarUtils.getCurrentUser()) 
                  && !CalendarUtils.isMemberShipType(mbsh, typedPerms))
              { 
                listPermission.add(groupKey + CalendarUtils.getCurrentUser()) ;
              }
              calendar.setEditPermission(listPermission.toArray(new String[listPermission.size()])) ;
              
              calendarService.savePublicCalendar(calendar, true, username) ;
              calendarService.getCalendarImportExports(importFormat).importCalendar(username, input.getUploadDataAsStream(), calendar.getId(), null, null, null, false) ;
            }
          } else {
            //String calendarId = uiForm.getUIFormSelectBoxGroup(FIELD_TO_CALENDAR).getValue() ;
            String calendarId = uiForm.getCalendarId();
            
            // if this is remote calendar
            if(calendarService.isRemoteCalendar(CalendarUtils.getCurrentUser(), calendarId)) {
              event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, ApplicationMessage.WARNING));
              return;
            }
            
            calendarService.getCalendarImportExports(importFormat).importCalendar(username, input.getUploadDataAsStream(), calendarId, null, null, null, false) ;
          }
          UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
          UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
          UICalendarViewContainer uiCalendarViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
          uiCalendarViewContainer.refresh() ;
//          calendarPortlet.setCalendarSetting(null) ;
          /*
           * remove cached calendar setting to load new one after that.
           */
          CalendarUtils.removeCurrentCalendarSetting();
          
          uploadService.removeUpload(input.getUploadId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ;
          calendarPortlet.cancelAction() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
        } else {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.file-type-error", null));
          } 
      } catch(Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("File format to import calendar is not valid", e);
        }
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.file-type-error", null));
        }
    }
  }
  static  public class ImportActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      if(uiForm.getPrivateCalendars().isEmpty()) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar",null, ApplicationMessage.WARNING));
      } else {
        uiForm.switchMode(UPDATE_EXIST) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      }
    }
  }  
  static  public class AddActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      uiForm.switchMode(ADD_NEW) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  } 
  static  public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UIFormUploadInput input = uiForm.getUIInput(FIELD_UPLOAD) ;
      uploadService.removeUpload(input.getUploadId()) ;
      calendarPortlet.cancelAction() ;
    }
  }
  
  static  public class SelectPermissionActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setType(value) ;
      String groupId = uiForm.getSelectedIdGroup();
      uiGroupSelector.setSelectedGroups(uiForm.getSelectedGroups(groupId));
      uiGroupSelector.changeGroup(groupId) ;
      uiGroupSelector.setComponent(uiForm, new String[] {PERMISSION});
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(uiGroupSelector, 500, 0, true) ;
      uiGroupSelector.setFilter(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static  public class OnChangeActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiImportForm = event.getSource();
      uiImportForm.log.info("Goes here on change");
      String groupType = uiImportForm.getSelectedTypeGroup();
      if(!CalendarUtils.isEmpty(groupType)&& groupType.equals(CalendarUtils.PUBLIC_TYPE))
        uiImportForm.getUIStringInput(PERMISSION).setRendered(true);
      else
        uiImportForm.getUIStringInput(PERMISSION).setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiImportForm);
    }
  }
}
