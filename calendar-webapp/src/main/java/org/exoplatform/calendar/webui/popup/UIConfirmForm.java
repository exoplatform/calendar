/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 */
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 20, 2009  
 */
@ComponentConfig (
                  lifecycle  = UIFormLifecycle.class,
                  template =  "app:/templates/calendar/webui/UIPopup/UIConfirmForm.gtmpl"
    )

public class UIConfirmForm extends UIForm implements UIPopupComponent{

  public static String CONFIRM_TRUE = "true";
  public static String CONFIRM_FALSE = "false";
  private String config_id = "";
  private String confirmMessage;
  private boolean isDelete;
  private boolean isDeleteMultiple;
  final public static String SAVE_CONFIRM = "confirm";
  final private static String SAVE_ALL = "save_all";
  final private static String SAVE_ONE = "save_one";
  final private static String SAVE_FOLLOW = "save_follow";
  final private static String CANCEL = "Cancel";
  final private static String SAVE = "Save";
  final private static String DELETE = "Delete";
  final private static Collection<String> DELETE_ACTIONS = Arrays.asList("ConfirmDeleteOnlyInstance",
                                                                         "ConfirmDeleteFollowingSeries",
                                                                         "ConfirmDeleteAllSeries") ;
  final private static Collection<String> UPDATE_ACTIONS = Arrays.asList("ConfirmUpdateOnlyInstance", 
                                                                         "ConfirmUpdateFollowSeries", 
                                                                          "ConfirmUpdateAllSeries");
  private Collection<CalendarEvent> events ;
  public UIConfirmForm() {
    UIFormRadioBoxInput input = new UIFormRadioBoxInput(SAVE_CONFIRM, SAVE_CONFIRM, getValue());
    input.setValue(SAVE_ONE);
    input.setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN);
    addUIFormInput(input);
  }

  @Override
  public String getId() {
    if(isDeleteMultiple())  {
      if(isMutipleTask()) return super.getId() + "Tasks";
      else
      if(isMutipleEvent()) return super.getId() + "Events";
      else return super.getId();
    } else {
      if(isDelete) return super.getId() + "Delete" ;
      else return super.getId() + "Update";
    }
  }

  private List<SelectItemOption<String>> getValue() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(SAVE_ONE, SAVE_ONE));
    options.add(new SelectItemOption<String>(SAVE_FOLLOW, SAVE_FOLLOW));
    options.add(new SelectItemOption<String>(SAVE_ALL, SAVE_ALL));
    return options ;
  }

  public boolean isSaveOne(){
    try {
      return getUIInput(SAVE_CONFIRM).getValue() == SAVE_ONE;
    } catch (Exception e) {
      return false;      
    }
  }
  public boolean isSaveAll(){
    try {
      return getUIInput(SAVE_CONFIRM).getValue() == SAVE_ALL;
    } catch (Exception e) {
      return false;
    }
  }
  public boolean isSaveFollow(){
    try {
      return getUIInput(SAVE_CONFIRM).getValue() == SAVE_FOLLOW;
    } catch (Exception e) {
      return false;
    }
  }

  public void setConfirmMessage(String confirmMessage) {
    this.confirmMessage = confirmMessage;
  }

  public String getConfirmMessage() {
    try {
      if(isMutipleEvent()) return getLabel("confirm-delete-events");
      else if(isMutipleTask()) return getLabel("confirm-delete-tasks");
      else if(isDeleteMultiple() || confirmMessage == null) return  getLabel("confirm-delete-multiple");
      else
      return getLabel(confirmMessage);
    } catch (Exception e) {
       return confirmMessage;
    }
  }
  boolean isMutipleTask(){
    boolean isAllTask = (this.events != null);
    if(this.events != null)
    for(CalendarEvent ce : this.events){
      if(ce != null && CalendarEvent.TYPE_EVENT.equals(ce.getEventType())) {
        isAllTask = false;
        break;
      }
    }
    return isAllTask;
  }
  boolean isMutipleEvent(){
    boolean isAllEvent = (this.events != null);
    if(this.events != null)
    for(CalendarEvent ce : this.events){
      if(ce != null && CalendarEvent.TYPE_TASK.equals(ce.getEventType())) {
        isAllEvent = false;
        break;
      }
    }
    return isAllEvent;
  }
  @Override
  public String event(String name) throws Exception {
    StringBuilder b = new StringBuilder() ;
    b.append("javascript:eXo.webui.UIForm.submitForm('").append(getConfig_id()).append("','");
    if(isDelete()) {
      if(DELETE.equals(name)) {
        if(isSaveOne()) name = "ConfirmDeleteOnlyInstance";
        else if(isSaveAll()) name = "ConfirmDeleteAllSeries";
        else if(isSaveFollow()) name = "ConfirmDeleteFollowingSeries";
      } else if(CANCEL.equals(name)) name = "ConfirmDeleteCancel";
    } else {
      if(SAVE.equals(name)) {
        if(isSaveOne()) name = "ConfirmUpdateOnlyInstance";
        else if(isSaveAll()) name = "ConfirmUpdateAllSeries";
        else if(isSaveFollow()) name = "ConfirmUpdateFollowSeries";
      } else if(CANCEL.equals(name)) name = "ConfirmUpdateCancel";
    } 
    b.append(name).append("',true)");
    return b.toString() ;
  } 

  public void setConfig_id(String config_id) {
    this.config_id = config_id;
  }

  public String getConfig_id() {
    return config_id;
  }


  Collection<String> getAllActions(){
    Stack<String> s = new Stack<String>() ;
    if(isDelete()) {
      for(String name : DELETE_ACTIONS){
        StringBuilder b = new StringBuilder() ;
        b.append("javascript:eXo.webui.UIForm.submitForm('").append(getConfig_id()).append("','");
        b.append(name).append("',true);");
        s.add(b.toString()) ;
      }
    } else {
      for(String name : UPDATE_ACTIONS) {
        StringBuilder b = new StringBuilder() ;
        b.append("javascript:eXo.webui.UIForm.submitForm('").append(getConfig_id()).append("','");
        b.append(name).append("',true);");
        s.add(b.toString()) ;
      }
    } 
    return s;
  }


  @Override
  public void activate() throws Exception {

  }


  @Override
  public void deActivate() throws Exception {

  }

  @Override
  public String[] getActions() {
    if(isDelete() || isDeleteMultiple) return new String[]{"Delete", "Cancel"} ;
    else return new String[]{"Save", "Cancel"};
  }

  public boolean isDelete() {
    return isDelete;
  }

  public void setDelete(boolean isDelete) {
    this.isDelete = isDelete;
  }

  public boolean isDeleteMultiple() {
    return (events != null && events.size() >0);
  }

  public void setDeleteMultiple(boolean deleteMultiple) {
    isDeleteMultiple = deleteMultiple;
  }

  public Collection<CalendarEvent> getEvents() {
    return events;
  }

  public void setEvents(Collection<CalendarEvent> events) {
    this.events = events;
  }

  public void setEvent(CalendarEvent ev) {
    if(this.events == null) this.events = new ArrayList<CalendarEvent>();
    this.events.add(ev);
  }
}
