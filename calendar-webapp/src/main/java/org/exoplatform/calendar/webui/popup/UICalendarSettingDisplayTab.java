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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.UICalendarContainer;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.webui.application.WebuiRequestContext;
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

  /**
   * contains key as <type_calendar>:<calendar_id> and value as color of calendar
   * example: key 2:calendar1401dda8c0a801303011177469ff542e, value: color code
   */
  private LinkedHashMap<String, String> colorMap_ = new LinkedHashMap<String, String>() ;

  public UICalendarSettingDisplayTab(String compId) throws Exception {
    super(compId);
    setComponentConfig(getClass(), null) ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }

  public LinkedHashMap<String, String> getColorMap() {
    return colorMap_;
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
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    boolean showAllCalendar = true;
    List<Calendar> calendars = calendarService.getUserCalendars(username, showAllCalendar) ;
    if (calendars.size() == 0) return new ArrayList<Calendar>(0);

    if(calendars != null) {
      for (Calendar calendar : calendars) {
        colorMap_.put(Calendar.TYPE_PRIVATE + CalendarUtils.COLON + calendar.getId(), calendar.getCalendarColor()) ;
        UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
        if (checkbox == null) {
          checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
          checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
          addUIFormInput(checkbox);
        }
      }
    }
    return calendars;
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
    String username = CalendarUtils.getCurrentUser() ;
    String[] groups = CalendarUtils.getUserGroups(username) ;

    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    /* return all calendars for a list of group, filter by calendar setting */
    boolean showAllCalendar = true;
    List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(groups, showAllCalendar, username) ;
    if (groupCalendars.size() == 0) return new ArrayList<Calendar>(0);

    Map<String, String> map = new HashMap<String, String> () ;
    List<Calendar> calendars = new ArrayList<Calendar>();  /* contains all calendar */

    for (GroupCalendarData group : groupCalendars) {

      calendars.addAll(group.getCalendars()) ;
      for(Calendar calendar : calendars) {
        map.put(calendar.getId(), calendar.getId()) ;
        colorMap_.put(Calendar.TYPE_PUBLIC + CalendarUtils.COLON + calendar.getId(), calendar.getCalendarColor()) ;

        /* add a checkbox for each calendar */
        UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
        if (checkbox == null) {
          checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
          checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
          addUIFormInput(checkbox);
        }
      }
    }

    return new ArrayList<Calendar>(new HashSet<Calendar>(calendars));
  }


  /**
   * return a group of shared calendars for current users
   * if no calendar found, return a group calendar with zero length list of calendars
   *
   * @see <code>UICalendarSettingDisplayTab.gtmpl</code> - used in template
   * @return
   * @throws Exception
   */
  public GroupCalendarData getSharedCalendars() throws Exception
  {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    /* get shared calendar but filter from setting */
    boolean showAllCalendar = true;
    GroupCalendarData groupCalendars = calendarService.getSharedCalendars(CalendarUtils.getCurrentUser(), showAllCalendar) ;
    if (groupCalendars == null) return new GroupCalendarData("", "", new ArrayList<Calendar>(0));

    CalendarSetting setting = calendarService.getCalendarSetting(CalendarUtils.getCurrentUser()) ;
    Map<String, String> map = new HashMap<String, String>() ;
    for(String key : setting.getSharedCalendarsColors()) {
      map.put(key.split(CalendarUtils.COLON)[0], key.split(CalendarUtils.COLON)[1]) ;
    }

    List<Calendar> calendars = groupCalendars.getCalendars() ;
    for (Calendar calendar : calendars) {
      String color = map.get(calendar.getId()) ;
      if(color == null) color = calendar.getCalendarColor() ;
      colorMap_.put(Calendar.TYPE_SHARED + CalendarUtils.COLON + calendar.getId(), color) ;
      UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
      if (checkbox == null) {
        checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
        checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
        addUIFormInput(checkbox);
      }
    }

    return groupCalendars ;
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
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
}
