/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.util.*;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 10, 2008  
 */

@ComponentConfig(
    template = "app:/templates/calendar/webui/UIPopup/UIGroupCalendarTab.gtmpl"
)

public class UIGroupCalendarTab extends UIFormInputWithActions
{
  private Map<String, List<ActionData>> actionField_ ;

   /**
    * contains id of groups being displayed
    **/
  private Set<String> groupsList;

  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }

  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
  
  public UIGroupCalendarTab(String id) throws Exception {
    super(id) ;
    setComponentConfig(getClass(), null) ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
    groupsList   = new HashSet<String>();
  }

  /**
   * clear field value, hides all input field and remove group permission from group list
   */
  public void resetTab()
  {
    UIFormStringInput groupPermission;
    Iterator<String> it = groupsList.iterator();
    while (it.hasNext())
    {
      String groupId = it.next();
      groupPermission = getChildById(groupId + UICalendarForm.PERMISSION_SUB);
      groupPermission.setValue(null);
      groupPermission.setRendered(false);
      it.remove();
    }

    UIFormStringInput addGroupInput = getChildById(UICalendarForm.ADD_GROUP_INPUT);
    addGroupInput.setValue(UICalendarForm.ADD_GROUP_INPUT_LABEL);
  }

  /**
   * add an entry in the permission list for a specific group
   * if the entry is already there, do nothing otherwise, add it and show it
   *
   * @param groupId
   */
  public void addGroupPermissionEntry(String groupId) throws Exception
  {
    if (!addGroupToDisplay(groupId)) return;

    /* check if there is a input for the group, if it's hidden then set it shown */
    if (getChildById(groupId + UICalendarForm.PERMISSION_SUB) != null)
    {
      UIFormStringInput groupPermissionInput = getChildById(groupId + UICalendarForm.PERMISSION_SUB);
      groupPermissionInput.setRendered(true);
      groupPermissionInput.setValue(CalendarUtils.getCurrentUser());
      return ;
    }

    /* by default, add name of current user to the group permission */
    addUIFormInput(new UIFormStringInput(groupId + UICalendarForm.PERMISSION_SUB, groupId + UICalendarForm.PERMISSION_SUB,
        CalendarUtils.getCurrentUser())) ;

    List<ActionData> actions = new ArrayList<ActionData>() ;
    /* add select user action */
    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener(UICalendarForm.ACTION_SELECT_PERMISSION) ;
    selectUserAction.setActionName("SelectUser") ;
    selectUserAction.setActionParameter(UISelectComponent.TYPE_USER + ":" + groupId + UICalendarForm.PERMISSION_SUB) ;
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    selectUserAction.setCssIconClass("uiIconUser uiIconLightGray") ;
    actions.add(selectUserAction) ;

    /* add select membership action */
    ActionData selectMembershipAction = new ActionData() ;
    selectMembershipAction.setActionListener(UICalendarForm.ACTION_SELECT_PERMISSION) ;
    selectMembershipAction.setActionName("SelectMemberShip") ;
    selectMembershipAction.setActionParameter(UISelectComponent.TYPE_MEMBERSHIP + ":" + groupId + UICalendarForm.PERMISSION_SUB) ;
    selectMembershipAction.setActionType(ActionData.TYPE_ICON) ;
    selectMembershipAction.setCssIconClass("uiIconMembership uiIconLightGray") ;
    actions.add(selectMembershipAction) ;

    /* add delete permission action */
    ActionData deletePermissionAction = new ActionData() ;
    // action listener DeletePermissionActionListener class
    deletePermissionAction.setActionListener(UICalendarForm.ACTION_DELETE_PERMISSION) ;
    // name of action: DeletePermission
    deletePermissionAction.setActionName(UICalendarForm.ACTION_DELETE_PERMISSION) ;
    // parameter 2:groupId + _permission like 2:/platform/web-contributors_permission
    deletePermissionAction.setActionParameter(UISelectComponent.TYPE_GROUP + ":" + groupId + UICalendarForm.PERMISSION_SUB) ;
    // uses icon to represent action on UI
    deletePermissionAction.setActionType(ActionData.TYPE_ICON) ;
    deletePermissionAction.setCssIconClass("uiIconDelete uiIconLightGray") ;
    actions.add(deletePermissionAction) ;

    // for each group, we use groupId_permission as key, that corresponds to a set of actions
    setActionField(groupId + UICalendarForm.PERMISSION_SUB, actions) ;
  }

  /**
   * hides the permission entry for the group being removed
   *
   * @param groupId
   */
  public void removeGroupPermissionEntry(String groupId)
  {
    if (!containsGroup(groupId)) return;

    removeGroup(groupId);
    UIFormStringInput groupPermissionInput = getUIStringInput(groupId + UICalendarForm.PERMISSION_SUB);
    if (groupPermissionInput != null) {
      groupPermissionInput.setValue(""); // reset value
      groupPermissionInput.setRendered(false);
    }
  }

  /**
   * check if the group belongs to user's groups
   * if it's ok then add the group to be displayed by the tab
   *
   * @param groupId
   * @return
   */
  public boolean addGroupToDisplay(String groupId) throws Exception
  {
    if (!UICalendarForm.isGroupBelongingToUserGroups(groupId)) return false;
    return groupsList.add(groupId);
  }

  public void removeGroup(String groupId)
  {
    groupsList.remove(groupId);
  }

  /**
   * return the groups that are set permissions
   * check the string input for all the displayed groups, if it's modified then the groups
   * is added to the calendar
   *
   * @return
   */
  public String[] getGroupsAddedToTheCalendar()
  {
    List<String> groupsAdded = new ArrayList<String>();
    for (String groupId : groupsList)
    {
      String groupPermission = ((UIFormStringInput) getChildById(groupId + UICalendarForm.PERMISSION_SUB)).getValue();
      if (groupPermission == null) continue;
      //LOG.info("groupId: " + groupId + " - group permission: " + groupPermission);
      groupPermission = groupPermission.trim();      /* trim space characters */
      if (groupPermission.isEmpty()) continue;
      //LOG.info("add group to added groups: " + groupId);
      groupsAdded.add(groupId);
    }
    return groupsAdded.toArray( new String[]{});
  }

  public String[] getDisplayedGroups()
  {
    return groupsList.toArray(new String[]{});
  }

  public boolean containsGroup(String groupId)
  {
    return groupsList.contains(groupId);
  }

  @Override
  public void setActionField(String fieldName, List<ActionData> actions)
  {
    actionField_.put(fieldName, actions) ;
  }

  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}


  /**
   * takes the string after the last "/" of group id
   *
   * @return
   */
  private String truncateGroupId(String groupId)
  {
    String[] groupIdParts = groupId.split(CalendarUtils.SLASH);
    char[] newGroupId = groupIdParts[groupIdParts.length - 1].toCharArray();
    newGroupId[0] = Character.toUpperCase(newGroupId[0]); /* upper case the first character */
    return new String(newGroupId).replaceAll("[^a-zA-Z0-9]+"," "); /* replace special character by space */
  }
}
