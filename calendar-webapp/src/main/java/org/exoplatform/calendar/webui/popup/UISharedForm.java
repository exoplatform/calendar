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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfigs({
  @ComponentConfig(
      lifecycle = UIFormLifecycle.class,
      template = "system:/groovy/webui/form/UIForm.gtmpl",
      events = {
        @EventConfig(listeners = UISharedForm.SaveActionListener.class),    
        @EventConfig(listeners = UISharedForm.SelectPermissionActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UISharedForm.SelectGroupActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UISharedForm.CancelActionListener.class)
      }
  ),
  @ComponentConfig(
    id = "UIPopupWindowUserSelect",
        type = UIPopupWindow.class,
        template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
        events = {
          @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
          @EventConfig(listeners = UISharedForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
          @EventConfig(listeners = UITaskForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
        }
  )
})
public class UISharedForm extends UIForm implements UIPopupComponent, UISelector{
  final public static String SPECIALCHARACTER[] = {CalendarUtils.SEMICOLON,CalendarUtils.SLASH,CalendarUtils.BACKSLASH,"'","|",">","<","\"", "?", "!", "@", "#", "$", "%","^","&","*"} ;
  final public static String SHARED_TAB = "UIInputUserSelect".intern() ;
  private String calendarId_ ;
  protected boolean isAddNew_ = true ;
  public UISharedForm() throws Exception{ 
    UISharedTab inputset = new UISharedTab(SHARED_TAB) ;
    inputset.addChild(new UIFormInputInfo(UISharedTab.FIELD_NAME, UISharedTab.FIELD_NAME, null)) ; 
    inputset.addUIFormInput(new UIFormStringInput(UISharedTab.FIELD_USER, UISharedTab.FIELD_USER, null));
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener("SelectPermission") ;
    selectUserAction.setActionName("SelectUser") ;
    selectUserAction.setCssIconClass("SelectUserIcon") ;
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    selectUserAction.setActionParameter(UISelectComponent.TYPE_USER) ;
    actions.add(selectUserAction) ;
    inputset.setActionField(UISharedTab.FIELD_USER, actions) ;
    inputset.addUIFormInput(new UIFormStringInput(UISharedTab.FIELD_GROUP, UISharedTab.FIELD_GROUP, null));
    List<ActionData> actionGroups = new ArrayList<ActionData>() ;
    ActionData selectGroupAction = new ActionData() ;
    selectGroupAction.setActionListener("SelectPermission") ;
    selectGroupAction.setActionName("SelectGroup") ;
    selectGroupAction.setCssIconClass("SelectGroupIcon") ;
    selectGroupAction.setActionType(ActionData.TYPE_ICON) ;
    selectGroupAction.setActionParameter(UISelectComponent.TYPE_GROUP) ;
    actionGroups.add(selectGroupAction) ;
    inputset.setActionField(UISharedTab.FIELD_GROUP, actionGroups) ;
    inputset.addChild(new UIFormCheckBoxInput<Boolean>(UISharedTab.FIELD_EDIT, UISharedTab.FIELD_EDIT, null)) ;
    addChild(inputset) ;
  }

  public void init(String username, Calendar cal, boolean isAddNew) {
    isAddNew_ = isAddNew ;
    calendarId_ = cal.getId() ;
    setCalendarName(cal.getName()) ;
    boolean canEdit = false ;
    if(cal.getEditPermission() != null) {
      canEdit = Arrays.asList(cal.getEditPermission()).contains(username) ;
    }
    setCanEdit(canEdit) ;
  }
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  public void setSelectedCalendarId(String id) { calendarId_ = id ;}
  public void setCalendarName(String value) {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    inputset.calendarName_ = value ;
    if(!CalendarUtils.isEmpty(value) && value.trim().length() > 30) value = value.substring(0, 30)+"..." ; 
    inputset.getUIFormInputInfo(UISharedTab.FIELD_NAME).setValue(value) ;
  }
  protected void setCanEdit(boolean canEdit) {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    inputset.getUIFormCheckBoxInput(UISharedTab.FIELD_EDIT).setChecked(canEdit) ;
  }
  protected boolean canEdit() {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    return inputset.getUIFormCheckBoxInput(UISharedTab.FIELD_EDIT).isChecked() ;
  }
  protected void setSharedUser(String value) {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    inputset.getUIStringInput(UISharedTab.FIELD_USER).setValue(value) ;
  }
  protected String getSharedUser() {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    return inputset.getUIStringInput(UISharedTab.FIELD_USER).getValue() ;
  }
  protected void setSharedGroup(String value) {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    inputset.getUIStringInput(UISharedTab.FIELD_GROUP).setValue(value) ;
  }
  protected String getSharedGroup() {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    return inputset.getUIStringInput(UISharedTab.FIELD_GROUP).getValue() ;
  }
  public String[] getActions() {
    return new String[] {"Save","Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
    UISharedTab inputset = getChildById(SHARED_TAB) ;
    UIFormStringInput fieldInput = inputset.getUIStringInput(selectField) ;
    String currentValues = fieldInput.getValue();
    if(!CalendarUtils.isEmpty(currentValues) && !currentValues.equals("null")){
      value += ","+ currentValues; 
    }
    fieldInput.setValue(CalendarUtils.cleanValue(value)) ;
  }  

  static  public class SaveActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception { 
      UISharedForm uiForm = event.getSource() ;
      String names = uiForm.getUIStringInput(UISharedTab.FIELD_USER).getValue() ;
      String groups = uiForm.getUIStringInput(UISharedTab.FIELD_GROUP).getValue() ;
      
      if(CalendarUtils.isEmpty(names) && CalendarUtils.isEmpty(groups)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISharedForm.msg.required", null)) ;
        return ;
      }
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      OrganizationService oService = CalendarUtils.getOrganizationService() ;
      String username = CalendarUtils.getCurrentUser() ;
      Calendar cal = calendarService.getUserCalendar(username, uiForm.calendarId_) ;
      Map<String, String> sharedUsers  = new HashMap<String, String>() ;
      
      if (!CalendarUtils.isEmpty(names)) {
        List<String> newUsers = new ArrayList<String>();
        List<String> receiverUsers  = new ArrayList<String>() ;
        StringBuffer sb = new StringBuffer() ;
        for(String name : Arrays.asList(names.split(CalendarUtils.COMMA))) {
          name = name.trim();
          if( oService.getUserHandler().findUserByName(name) != null) { 
            receiverUsers.add(name) ;
          }else{
            sb.append(name) ;
            sb.append(CalendarUtils.COMMA) ;
          }
        }
        if(sb.length() > 0) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISharedForm.msg.not-founduser", new Object[]{sb.toString()}, 1)) ;
          return ;
        }
        if(receiverUsers.contains(username)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISharedForm.msg.found-user", new Object[]{username}, 1)) ;
          return ;
        }
        Map<String, String> perms = new HashMap<String, String>() ;
        if(cal.getViewPermission() != null) {
          for(String v : cal.getViewPermission()) {
            perms.put(v,String.valueOf(cal.getEditPermission()!= null && Arrays.asList(cal.getEditPermission()).contains(v))) ;
          }
        }
        for(String u : receiverUsers) {
          if(perms.get(u) == null) newUsers.add(u) ; 
          perms.put(u, String.valueOf(uiForm.canEdit())) ;
        }
        for (String newUser : newUsers) {
          sharedUsers.put(newUser, newUser);
        }
        cal.setViewPermission(perms.keySet().toArray(new String[perms.keySet().size()])) ;
        List<String> tempList = new ArrayList<String>() ;
        for(String v : perms.keySet()) {
          if(Boolean.parseBoolean(perms.get(v))) tempList.add(v) ;       
        }
        cal.setEditPermission(tempList.toArray(new String[tempList.size()])) ;
      }
      
      if (!CalendarUtils.isEmpty(groups)) {
        StringBuffer sb = new StringBuffer() ;
        for(String name : Arrays.asList(groups.split(CalendarUtils.COMMA))) {
          name = name.trim();
          if( oService.getGroupHandler().findGroupById(name) != null) {
            for (User user : oService.getUserHandler().findUsersByGroup(name.trim()).getAll()) {
              String userId = user.getUserName();
              sharedUsers.put(userId, userId);
            }
          } else{
            sb.append(name) ;
            sb.append(CalendarUtils.COMMA) ;
          }
        }
        if(sb.length() > 0) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISharedForm.msg.not-foundgroup", new Object[]{sb.toString()}, 1)) ;
          return ;
        }
        sharedUsers.remove(username);
        Map<String, String> perms = new HashMap<String, String>() ;
        if(cal.getViewPermission() != null) {
          for(String v : cal.getViewPermission()) {
            perms.put(v,String.valueOf(cal.getEditPermission() != null && Arrays.asList(cal.getEditPermission()).contains(v))) ;
          }
        }
        for(String groupId : groups.split(CalendarUtils.COMMA)) { 
          perms.put(groupId, String.valueOf(uiForm.canEdit())) ;
        }
        cal.setViewPermission(perms.keySet().toArray(new String[perms.keySet().size()])) ;
        List<String> tempList = new ArrayList<String>() ;
        for(String v : perms.keySet()) {
          if(Boolean.parseBoolean(perms.get(v))) tempList.add(v) ;       
        }
        cal.setEditPermission(tempList.toArray(new String[tempList.size()])) ;
      }
      
      calendarService.saveUserCalendar(username, cal, false) ;
      calendarService.shareCalendar(username, uiForm.calendarId_, Arrays.asList(sharedUsers.keySet().toArray(new String[sharedUsers.keySet().size()]))) ;      
      UIAddEditPermission uiAddEdit = uiForm.getParent() ;
      uiAddEdit.updateGrid(cal, uiAddEdit.getCurrentPage());
      uiForm.setCanEdit(false) ;
      uiForm.setSharedUser(null) ;
      uiForm.setSharedGroup(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ;
      UICalendars uiCalendars = uiForm.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UICalendars.class);
      if (uiCalendars != null)
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars);
    }
  }
   
  static  public class SelectGroupActionListener extends EventListener<UIGroupSelector> {   
    public void execute(Event<UIGroupSelector> event) throws Exception {
      UIGroupSelector uiForm = event.getSource() ;
      String user = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.getAncestorOfType(UISharedForm.class).updateSelect(UISharedTab.FIELD_GROUP, user) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  
  static  public class SelectPermissionActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;  
      uiContainer.removeChild(UIPopupWindow.class) ;
      uiForm.removeChild(UIPopupWindow.class) ;
      if (permType.equals(UISelectComponent.TYPE_USER)) {
        UIPopupWindow uiPopupWindow = uiContainer.getChild(UIPopupWindow.class) ;
        if(uiPopupWindow == null) {
          uiPopupWindow = uiContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelect", "UIPopupWindowUserSelect") ;
        }
        UIUserSelector uiUserSelector = uiContainer.createUIComponent(UIUserSelector.class, null, null) ;
        uiUserSelector.setShowSearch(true);
        uiUserSelector.setShowSearchUser(true) ;
        uiUserSelector.setShowSearchGroup(true);
        uiPopupWindow.setUIComponent(uiUserSelector);
        uiPopupWindow.setShow(true);
        uiPopupWindow.setWindowSize(740, 400) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
      } else {
        UIPopupWindow uiPopupWindow = uiForm.addChild(UIPopupWindow.class, null, "UIPopupGroupSelector");
        UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null,null) ;
        uiPopupWindow.setUIComponent(uiGroupSelector);
        uiGroupSelector.setId("UIGroupSelector");
        uiGroupSelector.getChild(UITree.class).setId("TreeGroupSelector");
        uiGroupSelector.getChild(UIBreadcumbs.class).setId("BreadcumbsGroupSelector");
        uiForm.getChild(UIPopupWindow.class).setShow(true);
        uiPopupWindow.setWindowSize(540, 0) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
      }
    }
  }
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UISharedForm uiShareForm = uiContainer.findFirstComponentOfType(UISharedForm.class);
      UISharedTab uiSharedTab = uiShareForm.getChild(UISharedTab.class);
      UIFormStringInput uiInput = uiSharedTab.getUIStringInput(UISharedTab.FIELD_USER);
      String currentValues = uiInput.getValue();
      String values = uiUserSelector.getSelectedUsers();
      if(!CalendarUtils.isEmpty(currentValues) && !currentValues.equals("null")) values += ","+ currentValues; 
      values = CalendarUtils.cleanValue(values);
      uiInput.setValue(values);
      UIPopupWindow popupWindow = uiUserSelector.getAncestorOfType(UIPopupWindow.class); 
      popupWindow.setShow(false);
      popupWindow.setUIComponent(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }

  static  public class CancelActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }

}
