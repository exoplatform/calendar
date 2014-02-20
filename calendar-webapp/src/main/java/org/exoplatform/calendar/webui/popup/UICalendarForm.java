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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UIFormColorPicker;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
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
                 template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
                 events = {
                   @EventConfig(listeners = UICalendarForm.SaveActionListener.class),
                   @EventConfig(listeners = UICalendarForm.SelectPermissionActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.ResetActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.CancelActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UIFormTabPane.SelectTabActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.OpenActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.ShowPublicURLActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.ActiveActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.DeactiveActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.DeletePermissionActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.OpenSelectGroupFormActionListener.class, phase=Phase.DECODE),
                   @EventConfig(listeners = UICalendarForm.AddGroupActionListener.class, phase = Phase.DECODE)
                 }
    )
public class UICalendarForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  private static final Log LOG = ExoLogger.getExoLogger(UICalendarForm.class);

  final public static String DISPLAY_NAME = "displayName" ;
  final public static String DESCRIPTION = "description" ;
  final public static String CATEGORY = "category" ;
  final public static String SHARED_GROUPS = "sharedGroups" ;
  final public static String SELECT_COLOR = "selectColor" ;

  /* group tab */
  final public static String SELECT_GROUPS = "selectGroups" ;

  final public static String EDIT_PERMISSION = "editPermission" ;

  public static final String ACTIONS = "permissionActions";

  public static final String ADD_GROUP_INPUT = "AddGroupInput";

  public static final String ADD_GROUP_INPUT_LABEL = "";

  public static final String OPEN_SELECT_GROUP_FORM = "OpenSelectGroupForm";

  public static final String ADD_GROUP = "AddGroup";

  final public static String INPUT_CALENDAR = "calendarDetail";
  final public static String INPUT_SHARE = "public";
  final public static String TIMEZONE = "timeZone";
  final public static String LOCALE = "locale";
  final public static String PERMISSION_SUB = "_permission";
  final public static String PUBLIC_URL = "public-url";
  final public static String PRIVATE_URL = "private-url";
  final public static String PUBLIC_URL_MSG = "public-url-msg-active";
  final public static String PUBLIC_URL_MSG_D = "public-url-msg-deactive";
  final public static String ACTION_SELECT_PERMISSION = "SelectPermission";

  public static final String ACTION_DELETE_PERMISSION = "DeletePermission";

  final public static String ACT_ADD_CATEGORY = "AddCategory";

  final public static String ACT_OPEN = "Open";
  final public static String ACT_SUBSCRIBE = "Subscribe";
  final public static String ACT_ACTIVE = "Active";
  final public static String ACT_DEACTIVE = "Deactive";

  public final static int TYPE_BUTTON = 5;

  public Map<String, String> permission_ = new HashMap<String, String>() ;
  public Map<String, Map<String, String>> perms_ = new HashMap<String, Map<String, String>>() ;
  public Calendar calendar_ = null ;
  public String calType_ =  CalendarUtils.PRIVATE_TYPE ;
  private boolean isAddNew_ = true ;
  public String groupCalId_ = null ;

  /* contains group id of user groups */
  private static Set<String> userGroups;

  public UICalendarForm() throws Exception
  {
    super("UICalendarForm");

    /* init user groups */
    userGroups = getCurrentUserGroups();

    UICalendarDetailTab calendarDetail = new UICalendarDetailTab(INPUT_CALENDAR) ;
    calendarDetail.addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class)) ;
    calendarDetail.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
    CalendarSetting setting = CalendarUtils.getCurrentUserCalendarSetting();

    UIFormStringInput timeZone = new UIFormStringInput(TIMEZONE, TIMEZONE, CalendarUtils.generateTimeZoneLabel(setting.getTimeZone()));
    timeZone.setLabel(setting.getTimeZone());
    timeZone.setDisabled(true);
    calendarDetail.addUIFormInput(timeZone);

    UIFormColorPicker colorPicker = new UIFormColorPicker(SELECT_COLOR, SELECT_COLOR);
    colorPicker.setNumberItemsPerLine(6);
    calendarDetail.addUIFormInput(colorPicker);

    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData addCategory = new ActionData() ;
    addCategory.setActionListener(ACT_ADD_CATEGORY) ;
    addCategory.setActionType(ActionData.TYPE_ICON) ;
    addCategory.setActionName(ACT_ADD_CATEGORY) ;
    actions.add(addCategory) ;
    calendarDetail.setActionField(CATEGORY, actions) ;
    setSelectedTab(calendarDetail.getId()) ;
    addChild(calendarDetail) ;

    initGroupTab();
  }

  /**
   * get all user groups
   *
   * @return
   * @throws Exception
   */
  private Set<String> getCurrentUserGroups() throws Exception
  {
    Object[] groups = getPublicGroups();
    Set<String> userGroups = new HashSet<String>();
    for (Object aGroup : groups)
    {
      userGroups.add(((Group) aGroup).getId());
    }
    return userGroups;
  }

  public static boolean isGroupBelongingToUserGroups(String groupId)
  {
    return userGroups.contains(groupId);
  }

  /**
   * init calendar group tab
   **/
  private void initGroupTab() throws Exception
  {
    UIGroupCalendarTab groupTab = new UIGroupCalendarTab(INPUT_SHARE) ;
    groupTab.addUIFormInput(new UIFormInputInfo(SELECT_GROUPS, SELECT_GROUPS, null)) ;
    groupTab.addUIFormInput(new UIFormStringInput(EDIT_PERMISSION, null, null)) ;

    /* add input field for adding group */
    groupTab.addUIFormInput(new UIFormStringInput(ADD_GROUP_INPUT, null, ADD_GROUP_INPUT_LABEL));

    /* add icon to open popup to select group */
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData openGroupPopupAction = new ActionData() ;
    openGroupPopupAction.setActionListener(OPEN_SELECT_GROUP_FORM) ;
    openGroupPopupAction.setActionName(OPEN_SELECT_GROUP_FORM) ;
    openGroupPopupAction.setActionType(ActionData.TYPE_ICON) ;
    openGroupPopupAction.setCssIconClass("uiIconGroup uiIconLightGray") ;
    actions.add(openGroupPopupAction);

    groupTab.setActionField(OPEN_SELECT_GROUP_FORM, actions);

    /* add button to add group */
    actions = new ArrayList<ActionData>() ;
    ActionData addGroupAction = new ActionData() ;
    addGroupAction.setActionListener(ADD_GROUP) ;
    addGroupAction.setActionName(ADD_GROUP) ;
    addGroupAction.setActionType(TYPE_BUTTON) ;
    addGroupAction.setCssIconClass("btn");

    actions.add(addGroupAction);
    groupTab.setActionField(ADD_GROUP, actions);
    addChild(groupTab);
  }

  public String getCalType() { return calType_ ; }
  @Override
  public String[] getActions(){
    return new String[]{"Save", "Reset", "Cancel"} ;
  }

  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {}

  /**
   * clear all fields and set to initial values
   *
   * @throws Exception
   */
  public void resetField() throws Exception
  {
    permission_.clear() ;
    perms_.clear() ;
    UIGroupCalendarTab groupTab = getChildById(INPUT_SHARE) ;
    groupTab.resetTab();

    if(isAddNew_) {
      calendar_ = null ;
      calType_ = CalendarUtils.PRIVATE_TYPE ;
      setDisplayName(null) ;
      setDescription(null) ;
      setTimeZone(null) ;
      setSelectedColor(null) ;
    } else {
      init(calendar_, null) ;
    }
  }

  public boolean isAddNew() { return isAddNew_ ; }

  public void init(Calendar calendar, CalendarSetting setting) throws Exception {
    isAddNew_ = false ;
    calendar_ = calendar ;

    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR);
    if (setting != null) {
      UIFormStringInput info = calendarDetail.getUIStringInput(TIMEZONE);
      info.setValue(CalendarUtils.generateTimeZoneLabel(setting.getTimeZone()));
      info.setLabel(setting.getTimeZone());
    }

    setDisplayName(calendar.getName()) ;
    setDescription(calendar.getDescription()) ;
    UIGroupCalendarTab groupTab = getChildById(INPUT_SHARE) ;

    /* group calendar */
    if(CalendarUtils.PUBLIC_TYPE.equals(calType_))
    {
      groupTab.setRendered(true) ;
      /* for each group of calendar, add an entry for group permission */
      for (String groupId : calendar.getGroups())
      {
        groupTab.addGroupPermissionEntry(groupId);
      }
      groupTab.setGroupsListInitial();

      calendarDetail.removeChildById(CATEGORY) ;
      calendarDetail.setActionField(CATEGORY, null) ;
      for(String groupId : calendar.getGroups()) {
        UIFormStringInput selectPermissionInput = groupTab.getChildById(groupId + PERMISSION_SUB) ;

        if (selectPermissionInput!= null) {
          StringBuffer sb = new StringBuffer() ;
          List<String> checkList = new ArrayList<String>() ;
          if(calendar.getEditPermission() != null) {
            for(String s : calendar.getEditPermission()) {
              if(s.lastIndexOf(CalendarUtils.SLASH_COLON) > -1) {     ///developers/:demo
                String id = s.split(CalendarUtils.SLASH_COLON)[0].trim() ;
                String perm = s.split(CalendarUtils.SLASH_COLON)[1].trim() ;
                if (groupId.equals(id)) {
                  if(!checkList.contains(s.split(CalendarUtils.SLASH_COLON)[1])) {
                    checkList.add(perm) ;
                    if(sb.length() > 0) sb.append(CalendarUtils.COMMA + Utils.SPACE) ;
                    sb.append(perm) ;
                  }
                }
              }
            }
          }
          selectPermissionInput.setValue(sb.toString()) ;
        }
      }
    }
    /* private calendar */
    else if(CalendarUtils.PRIVATE_TYPE.equals(calType_))
    {
      groupTab.setRendered(false) ;
    }
    setTimeZone(setting.getTimeZone()) ;
    setSelectedColor(calendar.getCalendarColor()) ;
    if(calendar.getPrivateUrl() == null || calendar.getPrivateUrl().isEmpty()) {
      calendar_.setPrivateUrl(CalendarUtils.buildSubscribeUrl(calendar.getId(), calType_, true));
    }
    UIFormInputInfo privateUrl = new UIFormInputInfo(PRIVATE_URL, PRIVATE_URL, null);
    ActionData privateAction = new ActionData();
    privateAction.setActionListener(ACT_OPEN);
    privateAction.setActionParameter(calendar_.getPrivateUrl());
    privateAction.setActionName(ACT_OPEN);
    privateAction.setActionType(ActionData.TYPE_ICON);
    privateAction.setCssIconClass("uiIconCalICal uiIconLightGray");
    calendarDetail.addUIFormInput(privateUrl);
    calendarDetail.setActionField(PRIVATE_URL, Arrays.asList(privateAction));
  }

  protected String getDisplayName() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIStringInput(DISPLAY_NAME).getValue() ;
  }
  protected void setDisplayName(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIStringInput(DISPLAY_NAME).setValue(value) ;
  }

  protected String getDescription() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIFormTextAreaInput(DESCRIPTION).getValue() ;
  }
  protected void setDescription(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;
  }
  protected String getSelectedGroup() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    if(calendarDetail.getUIFormSelectBox(CATEGORY) != null) return calendarDetail.getUIFormSelectBox(CATEGORY).getValue() ;
    else return null ;
  }
  protected String getSelectedColor() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getChild(UIFormColorPicker.class).getValue() ;
  }
  protected void setSelectedColor(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getChild(UIFormColorPicker.class).setValue(value) ;
  }
  protected String getLocale() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIStringInput(LOCALE).getLabel();
  }
  public void setLocale(String value) {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    calendarDetail.getUIStringInput(LOCALE).setValue(CalendarUtils.getLocationDisplayString(value)) ;
    calendarDetail.getUIStringInput(LOCALE).setLabel(value);
  }
  protected String getTimeZone() {
    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    return calendarDetail.getUIStringInput(TIMEZONE).getLabel();
  }

  public void setTimeZone(String value) {
    if (value == null) return;

    UIFormInputWithActions calendarDetail = getChildById(INPUT_CALENDAR) ;
    UIFormStringInput timeZone = calendarDetail.getUIStringInput(TIMEZONE);
    timeZone.setValue(CalendarUtils.generateTimeZoneLabel(value));
    timeZone.setLabel(value) ;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void updateSelect(String selectField, String value) throws Exception {
    UIGroupCalendarTab shareTab = getChildById(INPUT_SHARE) ;

    if (selectField.equals(ADD_GROUP_INPUT)) {
      updateSelectGroup(selectField, value);
      return;
    }

    UIFormStringInput fieldInput = shareTab.getUIStringInput(selectField) ;
    StringBuilder sb = new StringBuilder() ;
    Map<String, String> temp = new HashMap<String, String>() ;
    String key = value.substring(0, selectField.lastIndexOf(PERMISSION_SUB));
    String tempS = value.substring(value.lastIndexOf(CalendarUtils.COLON_SLASH) + 2) ;
    if(perms_.get(selectField) == null) {
      temp.put(key, tempS) ;
    } else {
      temp = perms_.get(selectField) ;
      if(temp.get(key) != null && !tempS.equals(temp.get(key))) 
        tempS = new StringBuilder().append(temp.get(key)).append(CalendarUtils.COMMA).append(tempS).toString() ;
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
      if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA + " ") ;
      sb.append(s) ;
    }
    fieldInput.setValue(sb.toString()) ;
    setSelectedTab(shareTab.getId()) ;
  }


  /**
   * update "add group input" field once finishing selecting group from popup
   *
   * @param selectField
   * @param groupId
   */
  private void updateSelectGroup(String selectField, String groupId)
  {
    UIFormStringInput addGroupInput = getChild(UIGroupCalendarTab.class).getChildById(ADD_GROUP_INPUT);

    if (addGroupInput.getValue() == null) addGroupInput.setValue("");
    if (addGroupInput.getValue().contains(groupId)) return ;
    /* empty the input at the first moment inserting a groupId */
    if (addGroupInput.getValue().equals(ADD_GROUP_INPUT_LABEL)) {
      addGroupInput.setValue(groupId);
      return;
    }

    addGroupInput.setValue(
                           addGroupInput.getValue() + CalendarUtils.COMMA + " " + groupId
        );
  }

  /**
   * check if the calendar is a public or private calendar
   *
   * @return
   * @throws Exception
   */
  protected boolean isPublic() throws Exception
  {
    UIGroupCalendarTab groupTab = getChildById(INPUT_SHARE) ;
    return (groupTab.getDisplayedGroups().length > 0);
  }

  /**
   * find public groups of user
   **/
  private Object[] getPublicGroups() throws Exception {
    OrganizationService organization = getApplicationComponent(OrganizationService.class) ;
    String currentUser = CalendarUtils.getCurrentUser() ;
    return organization.getGroupHandler().findGroupsOfUser(currentUser).toArray() ;
  }

  @SuppressWarnings("unchecked")
  private List getSelectedGroups(String groupId) throws Exception {
    UIGroupCalendarTab groupTab = getChildById(INPUT_SHARE) ;
    List groups = new ArrayList() ;
    Group g = getApplicationComponent(OrganizationService.class).getGroupHandler().findGroupById(groupId) ;
    UICheckBoxInput input =  groupTab.getUICheckBoxInput(groupId) ;
    if(input != null && input.isChecked()) {
      groups.add(g) ;
    }
    return groups  ;
  }

  protected List<SelectItemOption<String>> getTimeZones() {
    return CalendarUtils.getTimeZoneSelectBoxOptions(TimeZone.getAvailableIDs()) ;
  }

  @Override
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      LOG.info("can not find label for " + getId() + ".label." + id);
      return id ;
    }
  }

  protected List<SelectItemOption<String>> getLocales() {
    return CalendarUtils.getLocaleSelectBoxOptions(java.util.Calendar.getAvailableLocales()) ;
  }

  /**
   * open popup in order to select user or group membership
   */
  public static class SelectPermissionActionListener extends EventListener<UICalendarForm>
  {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception
    {
      UICalendarForm uiForm = event.getSource() ;
      uiForm.setSelectedTab(INPUT_SHARE) ;

      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String permType = value.split(CalendarUtils.COLON)[0] ;
      String stringInputId = value.split(CalendarUtils.COLON)[1] ;
      UIGroupCalendarTab shareTab = uiForm.getChildById(INPUT_SHARE) ;
      String currentUsers = shareTab.getUIStringInput(stringInputId).getValue() ;
      if(uiForm.perms_.get(stringInputId) != null) uiForm.perms_.get(stringInputId).clear() ;
      if(!CalendarUtils.isEmpty(currentUsers)) {
        for (String user : currentUsers.split(CalendarUtils.COMMA)) {
          user = user.trim() ;
          String fullKey = permType + CalendarUtils.COLON_SLASH + stringInputId +  CalendarUtils.COLON_SLASH + user ;
          uiForm.updateSelect(stringInputId, fullKey) ;
        }
      }

      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setType(permType) ;
      String groupId = value.split(CalendarUtils.COLON)[1].split(PERMISSION_SUB)[0] ;
      uiGroupSelector.setSelectedGroups(uiForm.getSelectedGroups(groupId)) ;
      uiGroupSelector.changeGroup(groupId) ;
      uiGroupSelector.setComponent(uiForm, new String[] {value.split(CalendarUtils.COLON)[1]});

      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(uiGroupSelector, 500, 0, true) ;
      uiGroupSelector.setFilter(false) ;

      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  /**
   * open popup to select group, only groups that user belong to is displayed
   */
  public static class OpenSelectGroupFormActionListener extends EventListener<UICalendarForm>
  {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception
    {
      UICalendarForm uiCalendarForm = event.getSource();
      uiCalendarForm.setSelectedTab(INPUT_SHARE);
      UIGroupCalendarTab groupTab = uiCalendarForm.getChild(UIGroupCalendarTab.class);
      UIGroupSelector uiGroupSelector = uiCalendarForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setType("2");

      /* get all groups of user */
      List groups = new ArrayList(Arrays.asList( uiCalendarForm.getPublicGroups() ));
      /* remove the group from user group list if it's already displayed in the permission tab */
      Iterator it = groups.iterator();
      while (it.hasNext())
      {
        String groupId = ((Group) it.next()).getId();
        if (groupTab.containsGroup(groupId)) it.remove();
      }

      uiGroupSelector.setSelectedGroups(groups);
      /* set component to passes chosen group to */
      uiGroupSelector.setComponent(uiCalendarForm, new String[] { uiCalendarForm.ADD_GROUP_INPUT });

      UIPopupAction uiPopupAction = uiCalendarForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class);
      uiPopupAction.activate(uiGroupSelector, 500, 0, true);
      uiGroupSelector.setFilter(false);

      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarForm.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }


  /**
   * add a new entry for a group in the permission list
   */
  public static class AddGroupActionListener extends EventListener<UICalendarForm>
  {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception
    {
      UICalendarForm uiCalendarForm = event.getSource();
      uiCalendarForm.setSelectedTab(INPUT_SHARE);
      UIGroupCalendarTab groupTab = uiCalendarForm.getChild(UIGroupCalendarTab.class);
      UIFormStringInput addGroupInput = groupTab.getChildById(ADD_GROUP_INPUT);
      String[] groups = addGroupInput.getValue().split(CalendarUtils.COMMA);

      for (String groupId : groups)
      {
        groupTab.addGroupPermissionEntry(groupId.trim());
      }
      addGroupInput.setValue(ADD_GROUP_INPUT_LABEL);
      UIPopupAction uiPopupAction = uiCalendarForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarForm.getParent());
    }
  }

  public static class DeletePermissionActionListener extends EventListener<UICalendarForm>
  {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception
    {
      UICalendarForm uiCalendarForm = event.getSource();
      uiCalendarForm.setSelectedTab(INPUT_SHARE);
      UIGroupCalendarTab groupTab = uiCalendarForm.getChild(UIGroupCalendarTab.class);

      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String groupId = value.split(CalendarUtils.COLON)[1].split(PERMISSION_SUB)[0];
      groupTab.removeGroupPermissionEntry(groupId);

      // update the ui
      UIPopupAction uiPopupAction = uiCalendarForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarForm.getParent());
    }
  }

  /**
   * reset the form to initial state
   */
  public static class ResetActionListener extends EventListener<UICalendarForm>
  {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception
    {
      UICalendarForm uiCalendarForm = event.getSource() ;
      uiCalendarForm.resetField() ;
      if (uiCalendarForm.isAddNew_) {
        UICalendarPortlet uiCalendarPortlet = uiCalendarForm.getAncestorOfType(UICalendarPortlet.class) ;
        uiCalendarForm.setTimeZone(uiCalendarPortlet.getCalendarSetting().getTimeZone()) ;
      }

      UIPopupAction uiPopupAction = uiCalendarForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarForm.getParent()) ;
    }
  }

  public static class SaveActionListener extends EventListener<UICalendarForm>
  {
    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void execute(Event<UICalendarForm> event) throws Exception {
      try {
        UICalendarForm uiForm = event.getSource() ;
        StringBuffer notFoundUser = new StringBuffer("");
        String displayName = uiForm.getUIStringInput(DISPLAY_NAME).getValue() ;
        displayName = CalendarUtils.reduceSpace(displayName) ;
        displayName = displayName.trim() ;
        CalendarService calendarService = CalendarUtils.getCalendarService() ;
        String username = CalendarUtils.getCurrentUser() ;
        if (uiForm.isPublic()) uiForm.calType_ = CalendarUtils.PUBLIC_TYPE ;
        Calendar calendar = new Calendar() ;
        if (!uiForm.isAddNew_) calendar = uiForm.calendar_ ;
        calendar.setName(displayName) ;
        calendar.setDescription(uiForm.getDescription()) ;
        calendar.setCalendarColor(uiForm.getSelectedColor()) ;
        calendar.setCalendarOwner(username) ;
        calendar.setPrivateUrl(CalendarUtils.buildSubscribeUrl(calendar.getId() , uiForm.calType_, true));
        calendar.setTimeZone(uiForm.getTimeZone());
        if(CalendarUtils.PRIVATE_TYPE.equals(uiForm.calType_))
        {
          List<Calendar> pCals = calendarService.getUserCalendars(username, true) ;
          for(Calendar cal : pCals) {
            if(uiForm.isAddNew_) {
              if(cal.getName().trim().equalsIgnoreCase(displayName.trim())) {
                event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{displayName}, AbstractApplicationMessage.WARNING)) ;
                return ;
              }
            } else {
              if(cal.getName().trim().equalsIgnoreCase(displayName.trim()) && !cal.getId().equals(calendar.getId())) {
                event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.name-exist", new Object[]{displayName}, AbstractApplicationMessage.WARNING)) ;
                return ;
              }
            }
          }
          calendarService.saveUserCalendar(username, calendar, uiForm.isAddNew_) ;
        } else if(CalendarUtils.SHARED_TYPE.equals(uiForm.calType_))
        {
          calendarService.saveSharedCalendar(username, calendar) ;
        }
        /* saving group calendar */
        else if (CalendarUtils.PUBLIC_TYPE.equals(uiForm.calType_))
        {
          UIGroupCalendarTab groupTab = uiForm.getChildById(INPUT_SHARE);

          calendar.setPublic(uiForm.isPublic()) ;
          List<String> listPermission = new ArrayList<String>() ;
          Set<String> groupsCalendarSet;
          OrganizationService orgService = CalendarUtils.getOrganizationService();

          /* if not add new one, update calendar */
          if (!uiForm.isAddNew())
          {
            /* build a set of groups selected */

            Set<String> groupsSelectedSet = new HashSet<String>(Arrays.asList(groupTab.getGroupsAddedToTheCalendar()));
            groupsCalendarSet = new HashSet<String>(Arrays.asList(calendar.getGroups()));

            /* combine 2 set to get all groups of calendar */
            groupsCalendarSet.addAll(groupsSelectedSet);
            List<String> deleteGroups = new ArrayList<String>(Arrays.asList(groupTab.getDeletedGroup()));
            /* filter deleted group */
            Iterator<String> it = groupsCalendarSet.iterator();
            while (it.hasNext())
            {
              String groupId = it.next();
              if (deleteGroups.contains(groupId)) it.remove();
            }

            /* looping through all calendar groups */
            for (String groupId : groupsCalendarSet.toArray(new String[]{}))
            {
              /* if the group is displayed in group tab then take group permission from ui */
              if (groupsSelectedSet.contains(groupId))
              {
                String groupKey = groupId + CalendarUtils.SLASH_COLON ;
                UIFormInputWithActions sharedTab = uiForm.getChildById(UICalendarForm.INPUT_SHARE) ;
                String typedPerms = sharedTab.getUIStringInput(groupId + PERMISSION_SUB).getValue();
                listPermission = getPermissions(listPermission, typedPerms, orgService, groupId, groupKey, event, notFoundUser);
              }
              /* else take the permission from current edit permission of calendar */
              else
              {
                /* loop through all calendar group permissions if one matches then add it into new list of edit permission */
                for (String editPermission : calendar.getEditPermission())
                {
                  if (editPermission.startsWith(groupId)) listPermission.add(editPermission);
                }
              }
            }
          }
          else {
            groupsCalendarSet = new HashSet<String>(Arrays.asList(groupTab.getGroupsAddedToTheCalendar()));

            for (String groupId : groupsCalendarSet.toArray(new String[]{}))
            {
              String groupKey = groupId + CalendarUtils.SLASH_COLON ;
              UIFormInputWithActions sharedTab = uiForm.getChildById(UICalendarForm.INPUT_SHARE) ;
              String typedPerms = sharedTab.getUIStringInput(groupId + PERMISSION_SUB).getValue();
              listPermission = getPermissions(listPermission, typedPerms, orgService, groupId, groupKey, event, notFoundUser);
            }
          }
          calendar.setGroups(groupsCalendarSet.toArray(new String[]{}));
          if(listPermission.size() >0){
            calendar.setEditPermission(listPermission.toArray(new String[listPermission.size()])) ;
            calendarService.savePublicCalendar(calendar, uiForm.isAddNew_) ;
          } else {
            UIFormInputWithActions sharedTab = uiForm.getChildById(UICalendarForm.INPUT_SHARE) ;
            if(!CalendarUtils.isEmpty(notFoundUser.toString())) {
              for(String groupId : groupsCalendarSet) {
                String groupName = orgService.getGroupHandler().findGroupById(groupId).getLabel();
                if(groupName == null) orgService.getGroupHandler().findGroupById(groupId).getGroupName();
                String typedPerms = sharedTab.getUIStringInput(groupId + PERMISSION_SUB).getValue();
                if(!typedPerms.isEmpty())
                event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.users-not-on-group", new Object[]{typedPerms.trim(), groupName}, AbstractApplicationMessage.WARNING)) ;
              }
              return ;
            }
          }

        }

        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        CalendarUtils.removeCurrentCalendarSetting();
        calendarPortlet.cancelAction() ;
        UICalendarWorkingContainer uiWorkingContainer = calendarPortlet.getChild(UICalendarWorkingContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;

        if (!CalendarUtils.isEmpty(notFoundUser.toString())) {
          JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
          RequireJS requireJS = jsManager.getRequireJS();
          requireJS.require("PORTLET/calendar/CalendarPortlet","cal");
          requireJS.addScripts("cal.UICalendarPortlet.showEditCalNotif('" + calendar.getName() + "','"+notFoundUser.substring(0, notFoundUser.lastIndexOf(","))+"');");
        }
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Fail to save the calendar", e);
        }
      }
    }
  }

  public static List<String> getPermissions(List<String> listPermission,
                                            String groupPermissions,
                                            OrganizationService orgService,
                                            String groupIdSelected,
                                            String groupKey,
                                            Event<?> event,
                                            StringBuffer notFoundUser) throws Exception
                                            {
    if (CalendarUtils.isEmpty(groupPermissions)) return new ArrayList<String>(0);

    for (String s : groupPermissions.split(CalendarUtils.COMMA))
    {
      s = s.trim() ;
      if (CalendarUtils.isEmpty(s)) continue;
      /* find all users from group */
      List<User> users = orgService.getUserHandler().findUsersByGroup(groupIdSelected).getAll() ;
      boolean isExisted = false ;

      /* check if user exists in the group */
      for (User u : users)
      {
        if (u.getUserName().equals(s)) {
          isExisted = true ;
          break ;
        }
      }

      if (isExisted) {
        /* user exists, add key to edit permission */
        listPermission.add(groupKey + s) ;
        continue;
      }
      else {
        if (!s.equals(CalendarUtils.ANY))
          notFoundUser.append(s + ", ");
      }

      /* users equals to anyone */
      if (s.equals(CalendarUtils.ANY))
      {
        listPermission.add(groupKey + s) ;
        continue;
      }

      /* membership type */
      if (s.indexOf(CalendarUtils.ANY_OF) > -1)
      {
        String membership = s.substring(s.lastIndexOf(CalendarUtils.DOT)+ 1, s.length()) ;
        if (orgService.getMembershipTypeHandler().findMembershipType(membership) != null) {
          listPermission.add(groupKey + s) ;
        } else {
          event.getRequestContext().getUIApplication()
          .addMessage(new ApplicationMessage("UICalendarForm.msg.name-not-on-group",
                                             new Object[]{s, groupKey}, AbstractApplicationMessage.WARNING));
        }
      }

    }

    return listPermission;
                                            }

  static  public class CancelActionListener extends EventListener<UICalendarForm> {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }

  static public class OpenActionListener extends EventListener<UICalendarForm> {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource();
      if(uiForm.isAddNew_) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.need-save-calendar-first", null, AbstractApplicationMessage.WARNING)) ;
      } else {
        String url = event.getRequestContext().getRequestParameter(OBJECTID);
        if(url ==null || url.isEmpty()) return;
        UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
        UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class);

        UIFeed uiFeed = uiChildPopup.activate(UIFeed.class, 600) ;
        List<FeedData> feeds = new ArrayList<FeedData>() ;
        FeedData feedData = new FeedData();
        feedData.setTitle(uiForm.getDisplayName());
        feedData.setUrl(url);
        feeds.add(feedData);
        uiFeed.setFeeds(feeds);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      }
    }
  }

  static public class ShowPublicURLActionListener extends EventListener<UICalendarForm> {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource();
      if(uiForm.isAddNew_) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.need-save-calendar-first", null, AbstractApplicationMessage.WARNING)) ;
      } else {
        String url = event.getRequestContext().getRequestParameter(OBJECTID);
        if(url ==null || url.isEmpty()) return;
        UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
        UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class);
        UIFeed uiFeed = uiChildPopup.activate(UIFeed.class, 600) ;
        List<FeedData> feeds = new ArrayList<FeedData>() ;
        FeedData feedData = new FeedData();
        feedData.setTitle(uiForm.getDisplayName());
        feedData.setUrl(url);
        feeds.add(feedData);
        uiFeed.setFeeds(feeds);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      }
    }
  }

  static public class ActiveActionListener extends EventListener<UICalendarForm> {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource();
      if(uiForm.isAddNew_) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarForm.msg.need-save-calendar-first", null, AbstractApplicationMessage.WARNING)) ;
      } else {
        uiForm.calendar_.setPublicUrl(CalendarUtils.buildSubscribeUrl(uiForm.calendar_.getId(), uiForm.calType_ , false));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      }
    }
  }

  static public class DeactiveActionListener extends EventListener<UICalendarForm> {
    @Override
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource();
      uiForm.calendar_.setPublicUrl(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }

}
