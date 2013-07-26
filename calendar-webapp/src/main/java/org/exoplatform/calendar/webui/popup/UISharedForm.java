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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM
 *
 * <br/>modified by: <a href="mailto:tuna@exoplatform.com">Anh-Tu NGUYEN<a/>
 */
@ComponentConfigs({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UISharedForm.gtmpl",
    events = {
      @EventConfig(listeners = UISharedForm.SaveActionListener.class),
      @EventConfig(listeners = UISharedForm.CancelActionListener.class),
      @EventConfig(listeners = UISharedForm.OpenSelectUserFormActionListener.class),
      @EventConfig(listeners = UISharedForm.SelectUserActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISharedForm.OpenSelectGroupFormActionListener.class),
      @EventConfig(listeners = UISharedForm.SelectGroupActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISharedForm.OpenSelectMembershipFormActionListener.class),
      @EventConfig(listeners = UISharedForm.SelectMembershipActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISharedForm.AddEntryActionListener.class),
      @EventConfig(listeners = UISharedForm.DeleteEntryActionListener.class)
    }
  ),     

  @ComponentConfig(
    id       = UISharedForm.POPUP_USER_SELECTOR,
    type     = UIPopupWindow.class,
    template = "system:/groovy/webui/core/UIPopupWindow.gtmpl",
    events = {
      @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
      @EventConfig(listeners = UISharedForm.SelectUserActionListener.class, name = "Add", phase = Phase.DECODE),
      @EventConfig(listeners = UITaskForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
    }
  )
})
public class UISharedForm extends UIForm implements UIPopupComponent
{
  private static final Log LOG = ExoLogger.getExoLogger(UISharedForm.class);

  private String calendarId_ ;

  protected boolean isAddNew_ = true ;

  public static final String PERMISSION_GRID = "UIPermissionGrid";

  public static final String PERMISSION_ENTRY = "UIPermissionEntry";

  public static final String POPUP_USER_SELECTOR = "UIPopupUserSelector";

  public static final String POPUP_GROUP_MEMBERSHIP_SELECTOR = "UIPopupGroupMembershipSelector";

  /* popup */
  public static final String OPEN_SELECT_USER_FORM = "OpenSelectUserForm";

  public static final String OPEN_SELECT_MEMBERSHIP_FORM = "OpenSelectMembershipForm";

  public static final String OPEN_SELECT_GROUP_FORM = "OpenSelectGroupForm";

  /* icons */
  public static final String ADD_ENTRY = "AddEntry";

  public static final String DELETE_ENTRY = "DeleteEntry";

  public static final String USER_ICON = "uiIconUser uiIconLightGray";

  public static final String MEMBERSHIP_ICON = "uiIconMembership uiIconLightGray";

  public static final String GROUP_ICON = "uiIconGroup uiIconLightGray";

  public static final String ADD_ICON = "ActionIcon Add";

  public static final String INPUT_PERMISSION_OWNER = "PermissionOwnerInput";

  public static String INPUT_PERMISSION_OWNER_LABEL = "Select recipient";

  /* define a button type for action data */
  public static final int    TYPE_BUTTON = 5;

  public static final String SAVE = "Save";

  public static final String CLOSE = "Cancel";

  private Map<String, Set<ActionData>> actionField;

  private String calendarName;

  /**
   * set of calendar permission being displayed
   * if a permission is removed from display, it's removed from the set
   **/
  private Set<Permission> calendarPermissions;

  public UISharedForm() throws Exception
  {
    calendarPermissions = new HashSet<Permission>();
    actionField = new HashMap<String, Set<ActionData>>();

    /* add the grid */
    UIPermissionGrid permissionGrid = new UIPermissionGrid(PERMISSION_GRID);
    addChild(permissionGrid);

    String [] actionNames = new String[]{
        OPEN_SELECT_USER_FORM,
        OPEN_SELECT_MEMBERSHIP_FORM,
        OPEN_SELECT_GROUP_FORM, ADD_ENTRY};
    String [] actionIcons = new String[] { USER_ICON, MEMBERSHIP_ICON, GROUP_ICON, ADD_ICON };
    Set<ActionData> actions = new LinkedHashSet<ActionData>(); // keep the insertion order
    ActionData action;
    for (int i = 0; i < actionNames.length; ++i)
    {
      action = new ActionData();
      action.setActionListener(actionNames[i]);
      if (i < actionNames.length - 1) {
        action.setActionType(ActionData.TYPE_ICON);
      } else {
        action.setActionType(TYPE_BUTTON);
      }
      action.setActionName(actionNames[i]);
      action.setCssIconClass(actionIcons[i]);
      actions.add(action);
    }

    /* a form containing button and input for selecting permission */
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    INPUT_PERMISSION_OWNER_LABEL = res.getString("UISharedForm.label.UIPermissionOwnerInput");
    addUIFormInput(new UIFormStringInput(INPUT_PERMISSION_OWNER, null, INPUT_PERMISSION_OWNER_LABEL));
    setActionField(INPUT_PERMISSION_OWNER, actions);

    addPopupWindow();

    setActions(new String[] { SAVE, CLOSE });
  }

  public Set<Permission> getCalendarPermissions()
  {
    return calendarPermissions;
  }

  /**
   * add a permission to be displayed
   *
   * @param aPermission
   * @throws Exception
   */
  public void addPermission(Permission aPermission) throws Exception
  {
    /* if the permission is already there, do nothing */
    if (containsPermission(aPermission.getId())) return;
    /* can not add current user to the grid */
    if (aPermission.getId().equals(CalendarUtils.getCurrentUser())) return;

    calendarPermissions.add(aPermission);
    UIPermissionGrid permissionGrid = getChildById(PERMISSION_GRID);
    permissionGrid.addEntry(aPermission);
  }

  /**
   * check whether the permission is included in the Permission Grid
   *
   * @param permissionId
   * @return
   */
  public boolean containsPermission(String permissionId)
  {
    Iterator<Permission> it = calendarPermissions.iterator();
    while (it.hasNext())
    {
      Permission aPermission = it.next();
      if (aPermission.getOwner().getId().equals(permissionId)) return true;
    }
    return false;
  }


  /**
   * remove a permission from the grid
   *
   */
  public void removePermission(String permissionId)
  {
    Iterator<Permission> it = calendarPermissions.iterator();
    while (it.hasNext())
    {
      Permission aPermission = it.next();
      if (aPermission.getId().equals(permissionId))
      {
        it.remove();
        break ;
      }
    }

    /* un-display the corresponding permission entry */
    getChild(UIPermissionGrid.class).removeEntry(permissionId);
  }


  public void setActionField(String fieldName, Set<ActionData> actions) throws Exception
  {
    actionField.put(fieldName, actions) ;
  }

  public Set<ActionData> getActionField(String fieldName)
  {
    return actionField.get(fieldName);
  }

  @Override
  public void activate() throws Exception {}

  @Override
  public void deActivate() throws Exception {}

  /**
   * init the shared form
   *
   * @param username
   * @param calendar
   * @param isAddNew
   * @throws Exception
   */
  public void init(String username, Calendar calendar, boolean isAddNew) throws Exception
  {
    isAddNew_ = isAddNew ;
    calendarId_ = calendar.getId() ;
    calendarName = calendar.getName();

    if (calendar.getViewPermission() != null)
    {
      for (String permission : calendar.getViewPermission())
      {
        Permission aPermission = new Permission(PermissionOwner.createPermissionOwnerFrom(permission));
        /* add edit permission */
        if (Arrays.asList(calendar.getEditPermission()).contains(permission)) aPermission.allowEditPermission();
        addPermission(aPermission);
      }
    }
  }



  @Override
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }


  /**
   * uses 2 popup windows, one for membership, another for group and user
   *
   * @throws Exception
   */
  private void addPopupWindow() throws Exception {
    addChild(UIPopupWindow.class, POPUP_USER_SELECTOR, POPUP_USER_SELECTOR);
    addChild(UIPopupWindow.class, null, POPUP_GROUP_MEMBERSHIP_SELECTOR);
  }

  private void closeAllPopupAction()
  {
    List<UIComponent> children = new ArrayList<UIComponent>(getChildren());
    for (UIComponent uichild : children) {
      if (uichild instanceof UIPopupWindow) {
        closePopupAction((UIPopupWindow) uichild);
      }
    }
  }

  private static void closePopupAction(UIPopupWindow uiPopupWindow)
  {
    uiPopupWindow.setUIComponent(null);
    uiPopupWindow.setShow(false);
  }

  private static void openPopupAction(UIPopupWindow uiPopup, UIComponent component, int width, int height)
  {
    uiPopup.setUIComponent(component);
    uiPopup.setShow(true);
    uiPopup.setWindowSize(width, height);
  }

  /**
   * update the value of the permission input after selecting permission from popup
   *
   * @param permissionId
   * @throws Exception
   */
  public void updatePermissionOwnerInputWith(String permissionId) throws Exception
  {
    UIFormStringInput permissionOwner = getChildById(INPUT_PERMISSION_OWNER);

    if (permissionOwner.getValue() == null) permissionOwner.setValue("");
    if (permissionOwner.getValue().contains(permissionId) || permissionId.equals(CalendarUtils.getCurrentUser())) return;

    if (permissionOwner.getValue().equals(INPUT_PERMISSION_OWNER_LABEL) || permissionOwner.getValue().isEmpty())
      permissionOwner.setValue(permissionId);
    else
      permissionOwner.setValue(permissionOwner.getValue() + CalendarUtils.COMMA + " " + permissionId);
  }

  /**
   * get list of users by groupId
   * groupId in the form of /platform/users
   *
   * @param groupId
   * @return
   * @throws Exception
   */
  private static Set<String> getUsersByGroupId(String groupId) throws Exception
  {
    OrganizationService organizationService = CalendarUtils.getOrganizationService();
    List<User> users = organizationService.getUserHandler().findUsersByGroup(groupId).getAll();
    Set<String> userIds = new HashSet<String>();
    if (users == null) return userIds;

    for (User user : users.toArray(new User[]{}))
    {
      userIds.add(user.getUserName());
    }
    return userIds;
  }

  /**
   * check edit permission of user on shared calendar
   *
   * @param calendar
   * @return
   * @throws Exception
   */
  public static Set<String> getUsersAbleToEditSharedCalendar(Calendar calendar) throws Exception
  {
    Set<String> users = new HashSet<String>();

    for (String permissionId : calendar.getEditPermission())
    {
      Permission aPermission = new Permission(PermissionOwner.createPermissionOwnerFrom(permissionId));
      PermissionOwner owner = aPermission.getOwner();
      if (owner.getOwnerType().equals(PermissionOwner.USER_OWNER))
      {
        users.add(owner.getId());
        continue;
      }

      if (owner.getOwnerType().equals(PermissionOwner.GROUP_OWNER))
      {
        users.addAll(getUsersByGroupId(owner.getGroupId()));
        continue;
      }

      if (owner.getOwnerType().equals(PermissionOwner.MEMBERSHIP_OWNER))
      {
        users.addAll(Utils.getUserByMembershipId(owner.getMembership(), owner.getGroupId()));
        continue;
      }
    }
    return users;
  }

  public static boolean canUserEditCalendar(String userId, Calendar calendar) throws Exception
  {
    return UISharedForm.getUsersAbleToEditSharedCalendar(calendar).contains(userId);
  }

  public static class SaveActionListener extends EventListener<UISharedForm>
  {
    @Override
    public void execute(Event<UISharedForm> event) throws Exception
    {
      UISharedForm sharedForm = event.getSource();
      Set<Permission> permissions = sharedForm.getCalendarPermissions();
      UIPermissionGrid permissionGrid = sharedForm.getChild(UIPermissionGrid.class);
      CalendarService calendarService = CalendarUtils.getCalendarService();
      String username = CalendarUtils.getCurrentUser() ;
      Calendar calendar = calendarService.getUserCalendar(username, sharedForm.calendarId_) ;

      List<String> viewPermissions = new ArrayList<String>();
      List<String> editPermissions = new ArrayList<String>();
      /* set of users to share to */
      Set<String> sharedUsers      = new HashSet<String>();
      Set<String> sharedGroups = new HashSet<String>();
      
      Iterator<Permission> it = permissions.iterator();
      while (it.hasNext())
      {
        Permission aPermission = it.next();
        viewPermissions.add(aPermission.getId());

        /* get the 'checked' value of checkbox */
        boolean hasEditPermission = ((UIPermissionEntry) permissionGrid.getChildById(PERMISSION_ENTRY + CalendarUtils.DOT + aPermission.hashCode()))
          .isTheCheckBoxChecked();
        if (hasEditPermission) editPermissions.add(aPermission.getId());

        PermissionOwner owner = aPermission.getOwner();
        if (owner.getOwnerType().equals(PermissionOwner.USER_OWNER))
        {
          sharedUsers.add(owner.getId());
          continue;
        }

        if (owner.getOwnerType().equals(PermissionOwner.GROUP_OWNER))
        {
          sharedGroups.add(owner.getGroupId());
          continue;
        }

        if (owner.getOwnerType().equals(PermissionOwner.MEMBERSHIP_OWNER))
        {
          sharedUsers.addAll(Utils.getUserByMembershipId(owner.getMembership(), owner.getGroupId()));
          continue;
        }
      }

      /* remove current user from the list of shared users */
      Iterator<String> userIt = sharedUsers.iterator();
      while (userIt.hasNext())
      {
        String userId = userIt.next();
        if (userId.equals(username)) {
          userIt.remove();
          break;
        }
      }

      // compare to old list of users to remove shared calendar
      Set<Permission> oldPermissions = new HashSet<Permission>();
      if (calendar.getViewPermission() != null)
      {
        for (String permission : calendar.getViewPermission())
        {
          Permission aPermission = new Permission(PermissionOwner.createPermissionOwnerFrom(permission));
          /* add edit permission */
          if (Arrays.asList(calendar.getEditPermission()).contains(permission)) aPermission.allowEditPermission();
          oldPermissions.add(aPermission);
        }
      }

      Set<String> oldSharedUsers = new HashSet<String>();
      Set<String> oldSharedGroups = new HashSet<String>();
      
      it = oldPermissions.iterator();
      while (it.hasNext())
      {
        Permission aPermission = it.next();
        PermissionOwner owner = aPermission.getOwner();
        if (owner.getOwnerType().equals(PermissionOwner.USER_OWNER))
        {
          oldSharedUsers.add(owner.getId());
          continue;
        }

        if (owner.getOwnerType().equals(PermissionOwner.GROUP_OWNER))
        {
          oldSharedGroups.add(owner.getGroupId());
          continue;
        }

        if (owner.getOwnerType().equals(PermissionOwner.MEMBERSHIP_OWNER))
        {
          oldSharedUsers.addAll(Utils.getUserByMembershipId(owner.getMembership(), owner.getGroupId()));
          continue;
        }
      }

      /* find users who are removed from share list */
      oldSharedUsers.removeAll(sharedUsers);
      /* find groups that are removed from share list */
      oldSharedGroups.removeAll(sharedGroups);
      /* remove current user from the list of shared users */
      userIt = oldSharedUsers.iterator();
      while (userIt.hasNext())
      {
        String userId = userIt.next();
        if (userId.equals(username)) {
          userIt.remove();
          break;
        }
      }

      for (String userToUnshare : oldSharedUsers.toArray(new String[]{}))
      {
        calendarService.removeSharedCalendar(userToUnshare, sharedForm.calendarId_) ;
      }
      if(oldSharedGroups.size() > 0) {
        calendarService.removeSharedCalendarByJob(username, new ArrayList<String>(oldSharedGroups), sharedForm.calendarId_);
      }
      calendar.setViewPermission(viewPermissions.toArray(new String[]{}));
      calendar.setEditPermission(editPermissions.toArray(new String[]{}));
      calendarService.saveUserCalendar(username, calendar, false) ;
      calendarService.shareCalendar(username, sharedForm.calendarId_, new ArrayList<String>(sharedUsers));
      if(sharedGroups.size() > 0) {
        calendarService.shareCalendarByRunJob(username, sharedForm.calendarId_, new ArrayList<String>(sharedGroups));
      }

      /* close all child popup and close share popup */
      sharedForm.closeAllPopupAction();

      UIPopupContainer popupContainer = sharedForm.getAncestorOfType(UIPopupContainer.class);
      popupContainer.getChild(UIPopupAction.class).setRendered(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm) ;
      UICalendarPortlet calendarPortlet = sharedForm.getAncestorOfType(UICalendarPortlet.class);
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class);
      if (uiCalendars != null)
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars);
      calendarPortlet.cancelAction();
    }
  }


  public static class OpenSelectUserFormActionListener extends EventListener<UISharedForm>
  {
    @Override
    public void execute(Event<UISharedForm> event) throws Exception
    {
      UISharedForm sharedForm = event.getSource();
      sharedForm.closeAllPopupAction();

      UIPopupWindow popupWindow = sharedForm.getChildById(POPUP_USER_SELECTOR);
      UIUserSelector userSelector = sharedForm.createUIComponent(UIUserSelector.class, null, null);
      userSelector.setShowSearch(true);
      userSelector.setShowSearchUser(true);
      userSelector.setShowSearchGroup(false);
      openPopupAction(popupWindow, userSelector, 650, 0);

      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm.getParent());
    }
  }

  public static class SelectUserActionListener extends EventListener<UIUserSelector>
  {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception
    {
      UIUserSelector userSelector = event.getSource();
      UISharedForm sharedForm = userSelector.getParent().getParent();
      String values = userSelector.getSelectedUsers();

      if (values.contains(CalendarUtils.COMMA))
      {
        String[] userIds = values.split(CalendarUtils.COMMA);

        for (String userId : userIds)
        {
          userId = userId.trim();
          sharedForm.updatePermissionOwnerInputWith(userId);
        }
      }
      else sharedForm.updatePermissionOwnerInputWith(values.trim());

      closePopupAction((UIPopupWindow)sharedForm.getChildById(POPUP_USER_SELECTOR));
      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm);
    }
  }

  public static class OpenSelectGroupFormActionListener extends EventListener<UISharedForm>
  {
    @Override
    public void execute(Event<UISharedForm> event) throws Exception
    {
      UISharedForm sharedForm = event.getSource();
      sharedForm.closeAllPopupAction();

      UIPopupWindow popupWindow = sharedForm.getChildById(POPUP_GROUP_MEMBERSHIP_SELECTOR);
      UIGroupSelector groupSelector = popupWindow.createUIComponent(UIGroupSelector.class, null, null);
      openPopupAction(popupWindow, groupSelector, 550, 0);

      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm);
    }
  }

  public static class SelectGroupActionListener extends EventListener<UIGroupSelector>
  {
    @Override
    public void execute(Event<UIGroupSelector> event) throws Exception
    {
      UIGroupSelector groupSelector = event.getSource();
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      UISharedForm sharedForm = groupSelector.getParent().getParent();
      groupId = groupId + CalendarUtils.SLASH_COLON + CalendarUtils.ANY;

      sharedForm.updatePermissionOwnerInputWith(groupId);

      closePopupAction((UIPopupWindow)sharedForm.getChildById(POPUP_GROUP_MEMBERSHIP_SELECTOR));
      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm);
    }
  }


  /**
   * open select membership popup
   */
  public static class OpenSelectMembershipFormActionListener extends EventListener<UISharedForm>
  {
    @Override
    public void execute(Event<UISharedForm> event) throws Exception
    {
      UISharedForm sharedForm = event.getSource();
      sharedForm.closeAllPopupAction();

      UIPopupWindow popupWindow = sharedForm.getChildById(POPUP_GROUP_MEMBERSHIP_SELECTOR);
      UIGroupMembershipSelector groupMembershipSelector = popupWindow.createUIComponent(UIGroupMembershipSelector.class, null, null);
      openPopupAction(popupWindow, groupMembershipSelector, 550, 0);

      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm);
    }
  }

  public static class SelectMembershipActionListener extends EventListener<UIGroupMembershipSelector>
  {
    @Override
    public void execute(Event<UIGroupMembershipSelector> event) throws Exception
    {
      UIGroupMembershipSelector groupMembershipSelector = event.getSource();
      UISharedForm sharedForm = groupMembershipSelector.getParent().getParent();

      String currentGroup = groupMembershipSelector.getCurrentGroup().getId();
      String membershipId = event.getRequestContext().getRequestParameter(OBJECTID);
      // current group: /developers - membership: member
      String permissionId = currentGroup + CalendarUtils.SLASH_COLON + CalendarUtils.STAR + CalendarUtils.DOT
          + membershipId;
      // permission id: /developers/:*.member
      sharedForm.updatePermissionOwnerInputWith(permissionId);

      closePopupAction((UIPopupWindow)sharedForm.getChildById(POPUP_GROUP_MEMBERSHIP_SELECTOR));
      UIPopupContainer popupContainer = sharedForm.getAncestorOfType(UIPopupContainer.class);
      popupContainer.getChild(UIPopupAction.class).setRendered(true);

      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm.getParent());
    }
  }


  public static class CancelActionListener extends EventListener<UISharedForm>
  {
    @Override
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm sharedForm = event.getSource() ;
      sharedForm.closeAllPopupAction();

      UIPopupContainer popupContainer = sharedForm.getAncestorOfType(UIPopupContainer.class);
      popupContainer.getChild(UIPopupAction.class).setRendered(true);

      UICalendarPortlet portlet = sharedForm.getAncestorOfType(UICalendarPortlet.class);
      portlet.cancelAction();
    }
  }


  public static class AddEntryActionListener extends EventListener<UISharedForm>
  {
    @Override
    public void execute(Event<UISharedForm> event) throws Exception
    {
      UISharedForm sharedForm = event.getSource();
      UIFormStringInput permissionOwner = sharedForm.getChildById(INPUT_PERMISSION_OWNER);
      String permissionStatement = permissionOwner.getValue();

      if (permissionStatement == null) return;
      String[] permissions = permissionStatement.split(CalendarUtils.COMMA);
      List<String> permissionsToAdd = new ArrayList<String>();
      StringBuffer permissionsNonexistent = new StringBuffer();

      for (String permissionId : permissions)
      {
        permissionId = permissionId.trim();
        if (sharedForm.containsPermission(permissionId)) continue;

        /* check if the permission is really a user, group or membership */
        OrganizationService organizationService = CalendarUtils.getOrganizationService() ;
        String groupId = null;
        int indexOfSlashColon = permissionId.indexOf(CalendarUtils.SLASH_COLON);
        if (indexOfSlashColon!= -1) groupId = permissionId.substring(0, indexOfSlashColon);

        if (groupId != null)
        {
          Group aGroup = organizationService.getGroupHandler().findGroupById(groupId);
          if (aGroup != null)
          {
            permissionsToAdd.add(permissionId);
            continue;
          }
          else permissionsNonexistent.append(permissionId + CalendarUtils.COMMA + " ");

          Membership membership = organizationService.getMembershipHandler().findMembership(permissionId);
          if (membership != null) {
            permissionsToAdd.add(permissionId);
          }
          else permissionsNonexistent.append(permissionId + CalendarUtils.COMMA + " ");
          continue;
        }

        /* user permission */
        if ((organizationService.getUserHandler().findUserByName(permissionId) != null))
        {
          permissionsToAdd.add(permissionId);
          continue;
        }
        else permissionsNonexistent.append(permissionId + CalendarUtils.COMMA + " ");
      }

      for (String permission : permissionsToAdd.toArray(new String[]{}))
      {
        Permission aPermission = new Permission(PermissionOwner.createPermissionOwnerFrom(permission));
        sharedForm.addPermission(aPermission);
      }

      if (!permissionsNonexistent.toString().isEmpty())
      {
        permissionsNonexistent.deleteCharAt(permissionsNonexistent.lastIndexOf(CalendarUtils.COMMA));
        event.getRequestContext().getUIApplication().addMessage(
          new ApplicationMessage("UISharedForm.msg.permissions-do-not-exist",
              new String[]{ permissionsNonexistent.toString() },
              ApplicationMessage.WARNING ));
      }

      /* reset input to Select Owner*/
      permissionOwner.setValue(INPUT_PERMISSION_OWNER_LABEL);
      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm);
    }
  }


  public static class DeleteEntryActionListener extends EventListener<UISharedForm>
  {
    @Override
    public void execute(Event<UISharedForm> event) throws Exception
    {
      UISharedForm sharedForm = event.getSource();
      String permissionEntryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPermissionEntry permissionEntry = ((UIPermissionGrid) sharedForm.getChildById(PERMISSION_GRID)).getChildById(permissionEntryId);
      Permission aPermission = permissionEntry.getPermission();
      
      if(aPermission.getOwner().getOwnerType().equals(PermissionOwner.GROUP_OWNER)) {
        JobSchedulerServiceImpl  schedulerService = (JobSchedulerServiceImpl)PortalContainer.getComponent(JobSchedulerService.class) ;
        CalendarService calService = (CalendarService)PortalContainer.getInstance().getComponentInstance(CalendarService.class) ;
        if(calService.isGroupBeingShared(aPermission.getOwner().getGroupId(),schedulerService)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarPortlet.label.unshare-being-shared",null)) ;
          return;
        }
      }
      sharedForm.removePermission(aPermission.getId());
      event.getRequestContext().addUIComponentToUpdateByAjax(sharedForm);
    }
  }

  /**
   * represents a permission with its owner and his status of edit permission
   *
   */
  public static class Permission
  {
    private PermissionOwner owner;

    private boolean hasEditPermission;

    public Permission(PermissionOwner owner)
    {
      this.owner = owner;
      hasEditPermission = false;
    }

    public boolean hasEditPermission() { return hasEditPermission; }

    public void allowEditPermission() { hasEditPermission = true; }

    public PermissionOwner getOwner()
    {
      return owner;
    }

    @Override
    public boolean equals(Object o)
    {
      if ( !(o instanceof Permission)) return false;
      if (! ((Permission) o).getOwner().equals(owner) ) return false;
      return true;
    }

    @Override
    public int hashCode()
    {
      return owner.hashCode();
    }

    public String getId()
    {
      return owner.getId();
    }

    /**
     * convert from an array string of permission statements to
     * a set of permission
     *
     * @param permissions
     * @return
     */
    public static Set<Permission> valueOf(String[] permissions)
    {
      Set<Permission> permissionList = new HashSet<Permission>();
      for (String aPermission : permissions)
      {
        permissionList.add(new Permission( new PermissionOwner().createPermissionOwnerFrom(aPermission) ));
      }
      return permissionList;
    }

    /**
     * add edit permissions to a group of permissions
     *
     * @return permissions new set of permissions
     */
    public static Set<Permission> allowEditToGroupOfPermissions(Set<Permission> permissions)
    {
      Iterator<Permission> it = permissions.iterator();
      while (it.hasNext())
      {
        it.next().allowEditPermission();
      }
      return permissions;
    }
  }


  public static class PermissionOwner
  {
    /**
     * id of Permission Owner is
     * - userId: like john if owner is an user
     * - groupId: like /platform/users if owner is a group
     * - membershipId: like /platform/users:*.manager
     * used to evaluate equality
     **/
    private String id;

    /**
     * owner = user --> empty string
     * owner = group --> groupId
     * owner = membership --> groupId
     **/
    private String groupId;

    private String membership;

    private String ownerType;

    public static final String USER_OWNER = "user";

    public static final String GROUP_OWNER = "group";

    public static final String MEMBERSHIP_OWNER = "membership";

    public String getId()
    {
      return id;
    }

    public void setId(String permissionId)
    {
      id = permissionId;
    }

    public String getMembership() {
      return membership;
    }

    public void setMembership(String membership) {
      this.membership = membership;
    }

    public String getGroupId() {
      return groupId;
    }

    public void setGroupId(String groupId) {
      this.groupId = groupId;
    }

    public String getOwnerType() {
      return ownerType;
    }

    public void setOwnerType(String ownerType) {
      this.ownerType = ownerType;
    }

    public PermissionOwner() {}

    /**
     * example of permission statement
     * - membership: /platform/users/:*.manager
     * - user: demo
     * - group: /organization/management/executive-board/:*.*
     */
    private static PermissionOwner createPermissionOwnerFrom(String permissionStatement)
    {
      PermissionOwner owner = new PermissionOwner();
      owner.setId(permissionStatement);

      /* user permission */
      if (permissionStatement.indexOf(CalendarUtils.SLASH_COLON) == -1)
      {
        owner.setGroupId("");
        owner.setMembership("");
        owner.setOwnerType(USER_OWNER);
        return owner;
      }

      int indexOfSlashColon = permissionStatement.indexOf(CalendarUtils.SLASH_COLON);
      owner.setGroupId(permissionStatement.substring(0, indexOfSlashColon));

      /* membership permission */
      if (permissionStatement.indexOf(CalendarUtils.ANY) == -1 )
      {
        int indexAnyOf = permissionStatement.indexOf(CalendarUtils.ANY_OF);
        owner.setMembership(permissionStatement.substring(indexAnyOf + 2, permissionStatement.length()));
        owner.setOwnerType(MEMBERSHIP_OWNER);
        return owner;
      }

      /* group permission */
      owner.setMembership(CalendarUtils.ANY);
      owner.setOwnerType(GROUP_OWNER);
      return owner;
    }

    /**
     * takes the string after the last "/" of group id
     * and replace special character by space
     *
     * @return
     */
    private String truncateGroupId()
    {
      String[] groupIdParts = groupId.split(CalendarUtils.SLASH);
      char[] newGroupId = groupIdParts[groupIdParts.length - 1].toCharArray();
      newGroupId[0] = Character.toUpperCase(newGroupId[0]); /* upper case the first character */
      return new String(newGroupId).replaceAll("[^a-zA-Z0-9]+"," "); /* replace special character by space */
    }

    /**
     * translate membership *.* to anybody
     *
     * @return
     */
    private String getMeaningfulMembership()
    {
      if (membership.equals(CalendarUtils.STAR))
        return "Anybody";
      return membership;
    }

    /**
     * returns a readable permission under form: user or membership in group
     *
     * @return
     */
    public String getMeaningfulPermissionOwnerStatement()
    {
      if (ownerType.equals(USER_OWNER))
        return id;
      else if (ownerType.equals(GROUP_OWNER))
        return "Anybody in " + truncateGroupId();
      return getMeaningfulMembership() + " in " + truncateGroupId();
    }

    /**
     * get the owner statement for the permission
     *
     * @return
     */
    @Override
    public String toString()
    {
      return id;
    }

    /**
     * compare 2 permissions owner
     * equality happens when 2 permission owner has the same type and id
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
      if ( !(o instanceof PermissionOwner) ) return false;
      PermissionOwner owner = ((PermissionOwner) o);
      return id.equals(owner.getId());
    }

    @Override
    public int hashCode()
    {
      return Math.abs(id.hashCode());
    }
  }

}
