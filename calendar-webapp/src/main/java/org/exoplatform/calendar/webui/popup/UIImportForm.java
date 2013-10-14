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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.impl.ICalendarImportExport;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIFormColorPicker;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
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
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UIUploadInput;
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
                   @EventConfig(listeners = UIImportForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
    )
public class UIImportForm extends UIForm implements UIPopupComponent, UISelector{
  private static final Log log = ExoLogger.getLogger(UIImportForm.class);

  final public static String DISPLAY_NAME = "displayName";
  final public static String DESCRIPTION = "description";
  final public static String SELECT_COLOR = "selectColor";
  final public static String TIMEZONE = "timeZone";
  final public static String PERMISSION_SUB = "_permission" ;
  public Map<String, Map<String, String>> perms_ = new HashMap<String, Map<String, String>>();

  final static public String TYPE = "type" ;
  final static public String FIELD_UPLOAD = "upload";
  final static public String FIELD_TO_CALENDAR = "impotTo";
  final static public String ONCHANGE = "OnChange";
  final static public int UPDATE_EXIST = 1;
  final static public int ADD_NEW = 0;
  protected int flag_ = -1;

  public UIImportForm() throws Exception {
    this.setMultiPart(true);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    CalendarService calendarService = CalendarUtils.getCalendarService();
    for(String type : calendarService.getExportImportType()) {
      options.add(new SelectItemOption<String>(type, type));
    }
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options));
    addUIFormInput(new UIUploadInput(FIELD_UPLOAD, FIELD_UPLOAD, 1, CalendarUtils.getLimitUploadSize()));
    UIFormSelectBoxWithGroups privateCal = new UIFormSelectBoxWithGroups(FIELD_TO_CALENDAR, FIELD_TO_CALENDAR, CalendarUtils.getCalendarOption());
    addUIFormInput(privateCal);
    addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class));
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null));
    CalendarSetting setting = CalendarUtils.getCurrentUserCalendarSetting();
    UIFormStringInput timeZones = new UIFormStringInput(TIMEZONE, TIMEZONE, CalendarUtils.generateTimeZoneLabel(setting.getTimeZone()));
    timeZones.setReadOnly(true);
    timeZones.setLabel(setting.getTimeZone());
    addUIFormInput(timeZones);

    UIFormColorPicker uiFormColorPicker = new UIFormColorPicker(SELECT_COLOR, SELECT_COLOR);
    uiFormColorPicker.setNumberItemsPerLine(6);
    addUIFormInput(uiFormColorPicker);
  }

  public void init(String calId, String calType) {
    if(!CalendarUtils.isEmpty(calId)) {
      UIFormSelectBoxWithGroups selectBox = getUIFormSelectBoxGroup(FIELD_TO_CALENDAR);
      if(selectBox.getOptions()!= null && !selectBox.getOptions().isEmpty()) {
        switchMode(UPDATE_EXIST);
        selectBox.setValue(calType +CalendarUtils.COLON + calId);
      } else {
        switchMode(ADD_NEW);
      }
    } else {
      switchMode(ADD_NEW);
    } 
  }

  @Override
  public String getLabel(String id) {
    try {
      return super.getLabel(id);
    } catch (Exception e) {
      return id;
    }
  }

  @SuppressWarnings("unchecked")
  private List getSelectedGroups(String groupId) throws Exception {
    List groups = new ArrayList();
    Group g = getApplicationComponent(OrganizationService.class).getGroupHandler().findGroupById(groupId);
    groups.add(g);
    return groups;
  }

  @Override
  public String[] getActions(){
    return new String[]{"Save", "Cancel"};
  }
  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {}
  public List<SelectItemOption<String>> getPrivateCalendars() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    try {
      for(Calendar c : CalendarUtils.getCalendarService().getUserCalendars(CalendarUtils.getCurrentUser(), true)){
        options.add(new SelectItemOption<String>(c.getName(), c.getId()));
      }
    } catch (Exception e) {
      log.debug("Failed to get private calendars.", e);
    }
    return options;
  }
  public boolean isNew() {
    return flag_ == ADD_NEW;
  }
  protected String getDescription() {
    return getUIFormTextAreaInput(DESCRIPTION).getValue();
  }

  protected String getSelectedColor() {
    return getChild(UIFormColorPicker.class).getValue();
  }

  protected String getTimeZone() {
    return getUIStringInput(TIMEZONE).getLabel();
  }

  public void switchMode(int flag) {
    flag_ = flag;
    if(flag == UPDATE_EXIST) {
      getUIFormSelectBoxGroup(FIELD_TO_CALENDAR).setRendered(true);
      getUIStringInput(DISPLAY_NAME).setRendered(false);
      getUIFormTextAreaInput(DESCRIPTION).setRendered(false);
      getChild(UIFormColorPicker.class).setRendered(false);
      getUIStringInput(TIMEZONE).setRendered(false);
    } else if(flag == ADD_NEW) {
      getUIFormSelectBoxGroup(FIELD_TO_CALENDAR).setRendered(false);
      getUIStringInput(DISPLAY_NAME).setRendered(true);
      getUIFormTextAreaInput(DESCRIPTION).setRendered(true);
      getChild(UIFormColorPicker.class).setRendered(true);
      getUIStringInput(TIMEZONE).setRendered(true);
    } else {
      log.warn("Wrong flag(" +flag+ ") only UPDATE_EXIST(1) or ADD_NEW(0) accept ");
    }
  }

  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id);
  }

  protected String getCalendarId() {
    UIFormSelectBoxWithGroups calendars =  getUIFormSelectBoxGroup(FIELD_TO_CALENDAR);
    String value = calendars.getValue();
    if (!CalendarUtils.isEmpty(value) && value.split(CalendarUtils.COLON).length>0) {
      return value.split(CalendarUtils.COLON)[1];      
    } 
    return value;
  }

  @Override
  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField);
    StringBuilder sb = new StringBuilder();
    Map<String, String> temp = new HashMap<String, String>();
    String key = value.substring(0, value.lastIndexOf(CalendarUtils.COLON_SLASH)-1);
    String tempS = value.substring(value.lastIndexOf(CalendarUtils.COLON_SLASH) + 2);
    if(perms_.get(selectField) == null) {
      temp.put(key, tempS);
    } else {
      temp = perms_.get(selectField);
      if(temp.get(key) != null && !tempS.equals(temp.get(key))) tempS = temp.get(key) + CalendarUtils.COMMA +  tempS;
      temp.put(key, tempS);
    }
    perms_.put(selectField, temp);
    Map<String, String> tempMap = new HashMap<String, String>();
    for(String s : temp.values()) {
      for(String t : s.split(CalendarUtils.COMMA)) {
        tempMap.put(t, t);
      }
    }
    for(String s : tempMap.values()) {
      if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA);
      sb.append(s);
    }
    fieldInput.setValue(sb.toString());
  }

  static  public class SaveActionListener extends EventListener<UIImportForm> {
    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void execute(Event<UIImportForm> event) throws Exception {
      String username = CalendarUtils.getCurrentUser();
      CalendarService calendarService = CalendarUtils.getCalendarService();
      UIImportForm uiForm = event.getSource();
      UIUploadInput input = uiForm.getUIInput(FIELD_UPLOAD);
      String importFormat = uiForm.getUIFormSelectBox(UIImportForm.TYPE).getValue();
      String calendarName = uiForm.getUIStringInput(UIImportForm.DISPLAY_NAME).getValue();
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class);

      UploadResource[] resource = input.getUploadResources();
      if(resource == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIImportForm.msg.file-name-error", null));
        return;
      }
      try {
        if(calendarService.getCalendarImportExports(importFormat).isValidate(input.getUploadDataAsStream(resource[0].getUploadId()))) {
          if(uiForm.isNew()) {
            if(CalendarUtils.isEmpty(calendarName)) {
              calendarName = resource[0].getFileName();
            } 
            List<Calendar> pCals = calendarService.getUserCalendars(username, true);
            for(Calendar cal : pCals) {
              if(cal.getName().trim().equalsIgnoreCase(calendarName)) {
                event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{calendarName}, AbstractApplicationMessage.WARNING));
                return;
              }
            }
            Calendar calendar = new Calendar();
            calendar.setName(calendarName);
            calendar.setDescription(uiForm.getDescription());
            calendar.setTimeZone(uiForm.getTimeZone());
            calendar.setCalendarColor(uiForm.getSelectedColor());
            calendar.setCalendarOwner(username);
            calendar.setPublic(false);
            calendarService.saveUserCalendar(username, calendar, true);
            
            if(CalendarService.ICALENDAR.equals(importFormat)) {
              //import ics by job
              ICalendarImportExport iCalImEx = (ICalendarImportExport) calendarService.getCalendarImportExports(importFormat);
              iCalImEx.importCalendarByJob(username, input.getUploadDataAsStream(resource[0].getUploadId()), calendar.getId(), calendar.getName(), null, null, false);
            } else {
              calendarService.getCalendarImportExports(importFormat).importCalendar(username, input.getUploadDataAsStream(resource[0].getUploadId()), calendar.getId(), null, null, null, false);
            }

          } else {
            String calendarId = uiForm.getCalendarId();
            if(calendarService.isRemoteCalendar(CalendarUtils.getCurrentUser(), calendarId)) {
              event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, AbstractApplicationMessage.WARNING));
              return;
            }
            calendarService.getCalendarImportExports(importFormat).importCalendar(username, input.getUploadDataAsStream(resource[0].getUploadId()), calendarId, null, null, null, false);
          }
          CalendarUtils.removeCurrentCalendarSetting();
          uploadService.removeUpload(resource[0].getUploadId());
          UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class);
          calendarPortlet.cancelAction();
          if(!CalendarService.ICALENDAR.equals(importFormat)) {
            UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class);
            UICalendarViewContainer uiCalendarViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class);
            uiCalendarViewContainer.refresh();
            event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars);
            event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet);  
          }
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
    @Override
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource();
      if(uiForm.getPrivateCalendars().isEmpty()) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar",null, AbstractApplicationMessage.WARNING));
      } else {
        uiForm.switchMode(UPDATE_EXIST);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      }
    }
  }  
  static  public class AddActionListener extends EventListener<UIImportForm> {
    @Override
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource();
      uiForm.switchMode(ADD_NEW);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  } 
  static  public class CancelActionListener extends EventListener<UIImportForm> {
    @Override
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource();
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class);
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class);
      UIUploadInput input = uiForm.getUIInput(FIELD_UPLOAD);
      UploadResource[] resource = input.getUploadResources() ;
      if(resource.length >0)
      uploadService.removeUploadResource(resource[0].getUploadId());
      calendarPortlet.cancelAction();
    }
  }

}
