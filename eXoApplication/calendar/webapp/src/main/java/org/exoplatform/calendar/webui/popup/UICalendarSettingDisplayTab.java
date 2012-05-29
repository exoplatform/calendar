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
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Jan 07, 2008  
 */

@ComponentConfig(
    template = "app:/templates/calendar/webui/UIPopup/UICalendarSettingDisplayTab.gtmpl"
) 
public class UICalendarSettingDisplayTab extends UIFormInputWithActions {
  private Map<String, List<ActionData>> actionField_  = new HashMap<String, List<ActionData>>() ;

  public UICalendarSettingDisplayTab(String compId) throws Exception {
    super(compId);
    setComponentConfig(getClass(), null) ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  public List<UIComponent> getUIFormPrivateCheckboxs() throws Exception {
    List<UIComponent> infoFields = new ArrayList<UIComponent>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService();
    String  username = CalendarUtils.getCurrentUser() ;
    for(Calendar c :((UICalendarSettingForm)getParentFrom()).getPrivateCalendars(calendarService, username)) {
      if(getUIFormCheckBoxInput(c.getId()) != null) infoFields.add(getUIFormCheckBoxInput(c.getId())) ;
    }
    return  infoFields;
  }
  public List<UIComponent> getUIFormShareCheckboxs() throws Exception {
    List<UIComponent> infoFields = new ArrayList<UIComponent>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService();
    String  username = CalendarUtils.getCurrentUser() ;
    for(Calendar c :((UICalendarSettingForm)getParentFrom()).getSharedCalendars(calendarService, username)) {
      if(getUIFormCheckBoxInput(c.getId()) != null) infoFields.add(getUIFormCheckBoxInput(c.getId())) ;
    }
    return  infoFields;
  }
  public List<Calendar> getShareCalendars() throws Exception {
    CalendarService calendarService = CalendarUtils.getCalendarService();
    String  username = CalendarUtils.getCurrentUser() ;
    return ((UICalendarSettingForm)getParentFrom()).getSharedCalendars(calendarService, username) ;
  }
  public List<UIComponent> getUIFormPublicCheckboxs() throws Exception {
    List<UIComponent> infoFields = new ArrayList<UIComponent>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService();
    String  username = CalendarUtils.getCurrentUser() ;
    for(Calendar c :((UICalendarSettingForm)getParentFrom()).getPublicCalendars(calendarService, username)) {
      if(getUIFormCheckBoxInput(c.getId()) != null) infoFields.add(getUIFormCheckBoxInput(c.getId())) ;
    }
    return  infoFields;
  }
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
}
