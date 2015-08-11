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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.ExtendedCalendarService;
import org.exoplatform.calendar.webui.UICalendarContainer;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.input.UICheckBoxInput;

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
  
  private ExtendedCalendarService xCalService = getApplicationComponent(ExtendedCalendarService.class);

  public UICalendarSettingDisplayTab(String compId) throws Exception {
    super(compId);
    setComponentConfig(getClass(), null) ;
  }
  
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  
  public Map<String, List<Calendar>> getCalendars() {    
    return getWorkingContainer().getCalendarMap();
  }
  
  public Map<String, String> getColorMap() {
    return getWorkingContainer().getColorMap();
  }
  
  private UICalendarWorkingContainer getWorkingContainer() {
    UICalendarPortlet portlet = getAncestorOfType(UICalendarPortlet.class);
    return portlet.findFirstComponentOfType(UICalendarWorkingContainer.class);
  }

  /**
   * get all private calendars for current user
   * if no calendar is found, return a zero length list of calendar
   *
   * @see <code>UICalendars.gtmpl</code> - used in template
   * @return
   * @throws Exception
   */
  public List<Calendar> getAllPrivateCalendars() throws Exception
  {
    List<Calendar> cals = getCalendars().get(Calendar.Type.PERSONAL.name());
    cals = cals != null ? cals : Collections.<Calendar>emptyList();
    initCheckbox(cals);
    return cals;
  }

  /**
   * get a reference to list of calendar in the left pane
   *
   * @see <code>UICalendarSettingDisplayTab.gtmpl</code>used in template
   * @return
   */
  private UICalendars getUICalendars()
  {
    return getAncestorOfType(UICalendarPortlet.class).getChild(UICalendarWorkingContainer.class)
        .getChild(UICalendarContainer.class).getChild(UICalendars.class);
  }


  /**
   * get group calendar for user, without duplicated items
   * if no calendar found, return zero length list of calendars
   *
   * @see <code>UICalendarSettingDisplayTab.gtmpl</code> - used in template
   * @return
   * @throws Exception
   */
  public List<Calendar> getAllPublicCalendars() throws Exception
  {
    List<Calendar> groupCalendars = getCalendars().get(Calendar.Type.GROUP.name());
    groupCalendars = groupCalendars != null ? groupCalendars : Collections.<Calendar>emptyList();
    initCheckbox(groupCalendars);    

    return groupCalendars;
  }

  /**
   * return a group of shared calendars for current users
   * if no calendar found, return a group calendar with zero length list of calendars
   *
   * @see <code>UICalendarSettingDisplayTab.gtmpl</code> - used in template
   * @return
   * @throws Exception
   */
  public List<Calendar> getSharedCalendars() throws Exception
  {
    List<Calendar> calendars = getCalendars().get(Calendar.Type.SHARED.name());
    calendars = calendars != null ? calendars : Collections.<Calendar>emptyList();
    initCheckbox(calendars);

    return calendars ;
  }

  public List<Calendar> getAllOtherCalendars() {    
    List<Calendar> cals = new LinkedList<Calendar>();
    Set<String> typeNames = new HashSet<String>();
    for (Calendar.Type t : Calendar.Type.values()) {
      typeNames.add(t.name());
    }
    
    for (String type : getCalendars().keySet()) {
      if (!typeNames.contains(type)) {
        cals.addAll(getCalendars().get(type));        
      }
    }
    initCheckbox(cals);
    return cals != null ? cals : Collections.<Calendar>emptyList();
  }

  /**
   * truncate a long name into a name with .. if length of name is larger than 35 characters
   *
   * @see <code>UICalendarSettingDisplayTab.gtmpl</code> - used in template
   * @param longName
   * @return
   */
  private String truncateLongName(String longName)
  {
    if (longName.length() > 35) return longName.substring(0, 30) + "..";
    return longName;
  }

  /**
   *
   * @param groupIds
   * @return true if the calendar is made by Social Space
   * else return false.
   */
  protected boolean isCalendarOfSpace(String[] groupIds)
  {
    UICalendarPortlet uiCalendarPortlet = getAncestorOfType(UICalendarPortlet.class);
    //String spaceGroupId = UICalendarPortlet.getGroupIdOfSpace();
    String spaceGroupId = uiCalendarPortlet.getSpaceGroupId();
    //if (spaceGroupId == null) {
    if (spaceGroupId.equals("")) {
      return true;
    }

    if (groupIds != null && groupIds.length > 0) {
      for (String groupId : groupIds) {
        if (groupId.equals(spaceGroupId)) {
          return true;
        }
      }
    }
    return false;
  }
  public void setActionField(String fieldName, List<ActionData> actions){
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}

  private void initCheckbox(List<Calendar> calendars) {
    for (Calendar calendar : calendars) {
      UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
      if (checkbox == null) {
        checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
        checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
        addUIFormInput(checkbox);
      }
    }
  }
}
