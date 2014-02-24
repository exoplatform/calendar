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
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.jcr.PathNotFoundException;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.popup.UICalendarForm;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIEventCategoryManager;
import org.exoplatform.calendar.webui.popup.UIExportForm;
import org.exoplatform.calendar.webui.popup.UIImportForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.calendar.webui.popup.UIRemoteCalendar;
import org.exoplatform.calendar.webui.popup.UISharedForm;
import org.exoplatform.calendar.webui.popup.UISubscribeForm;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM
 *
 * <br/>modified by: <a href="mailto:tuna@exoplatform.com">Anh-Tu NGUYEN<a/>
 */

@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template =  "app:/templates/calendar/webui/UICalendars.gtmpl",
                 events = {
                   @EventConfig(listeners = UICalendars.AddCalendarActionListener.class),
                   @EventConfig(listeners = UICalendars.AddEventCategoryActionListener.class),
                   @EventConfig(listeners = UICalendars.ExportCalendarActionListener.class),
                   @EventConfig(listeners = UICalendars.ExportCalendarsActionListener.class), 
                   @EventConfig(listeners = UICalendars.ImportCalendarActionListener.class),
                   @EventConfig(listeners = UICalendars.AddEventActionListener.class),
                   @EventConfig(listeners = UICalendars.AddTaskActionListener.class),
                   @EventConfig(listeners = UICalendars.EditCalendarActionListener.class),
                   @EventConfig(phase=Phase.DECODE, listeners = UICalendars.RemoveCalendarActionListener.class),
                   @EventConfig(listeners = UICalendars.ConfirmCloseActionListener.class),
                   @EventConfig(listeners = UICalendars.AbortCloseActionListener.class),
                   @EventConfig(phase=Phase.DECODE, listeners = UICalendars.RemoveSharedCalendarActionListener.class),
                   @EventConfig(listeners = UICalendars.ChangeColorActionListener.class),
                   @EventConfig(listeners = UICalendars.TickActionListener.class),
                   @EventConfig(listeners = UICalendars.CalendarSettingActionListener.class),
                   @EventConfig(listeners = UICalendars.RemoteCalendarActionListener.class),
                   @EventConfig(listeners = UICalendars.RefreshRemoteCalendarActionListener.class),
                   @EventConfig(listeners = UICalendars.ShareCalendarActionListener.class)
                 }
    )

public class UICalendars extends UIForm  {
  private static final Log LOG = ExoLogger.getLogger(UICalendars.class);
  public static String CALENDARID = "calendarid".intern() ;
  public static String CALTYPE = "calType".intern() ;
  public static String CALNAME = "calName".intern() ;
  public static String CALCOLOR = "calColor".intern() ;

  private boolean isShowTaskList_ = false ;

  /**
   * contains key as <type_calendar>:<calendar_id> and value as color of calendar
   * example: key 2:calendar1401dda8c0a801303011177469ff542e, value: color code
   */
  private LinkedHashMap<String, String> colorMap_ = new LinkedHashMap<String, String>() ;
  private String removed_cal_id = null;
  private String calType = CalendarUtils.SHARED_TYPE;

  public UICalendars() throws Exception {

  } 
  @Override
  public String getLabel(String key) {
    try {
      return super.getLabel(key) ;
    } catch (Exception e) {
      return key ;
    }
  }
  
  public void setShowTaskList(boolean isShowTaskList) {
    this.isShowTaskList_ = isShowTaskList;
  }

  public boolean isShowTaskList() {
    return isShowTaskList_;
  }
  public java.util.Calendar getCurrentMiniBeginDate() {
    UIMiniCalendar miniCal = getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
    java.util.Calendar temCal = miniCal.getInstanceTempCalendar() ;
    temCal.setTime(miniCal.getCurrentDate()) ;
    return miniCal.getBeginDay(temCal) ;
  }
  public java.util.Calendar getCurrentMiniEndDate() {
    UIMiniCalendar miniCal = getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
    java.util.Calendar temCal = miniCal.getInstanceTempCalendar() ;
    temCal.setTime(miniCal.getCurrentDate()) ;
    return miniCal.getEndDay(temCal) ;
  }
  public String[] getTaskStatus() {
    return CalendarEvent.TASK_STATUS ;
  }
  public List<CalendarEvent> getAllTask(java.util.Calendar formDate,java.util.Calendar toDate, String taskStatus) throws Exception {
    List<CalendarEvent> list = new ArrayList<CalendarEvent>() ;
    EventQuery eq = new EventQuery() ;
    if(!CalendarUtils.isEmpty(taskStatus)) eq.setState(taskStatus) ;
    eq.setEventType(CalendarEvent.TYPE_TASK) ;
    eq.setFromDate(formDate) ;
    eq.setToDate(toDate) ;
    list = CalendarUtils.getCalendarService().getEvents(CalendarUtils.getCurrentUser(), eq, null) ;
    return list ;
  }

  public void checkAll() {
    if (UICalendarPortlet.getSpaceId() != null) {
      try {
        CalendarService calendarService = CalendarUtils.getCalendarService() ;
        for(UIComponent component : getChildren()){
          Calendar calendar = calendarService.getGroupCalendar(component.getId());
          if(calendar != null) {
            String[] groupIds = calendar.getGroups();
            if(getUICheckBoxInput(component.getId()) != null) {
              getUICheckBoxInput(component.getId()).setChecked(isCalendarOfSpace(groupIds)) ;
            }
          }
        }
      } catch (Exception e) {
        if(LOG.isDebugEnabled()){
          LOG.debug("Can not get calendar service", e);
        }
      }
      return;
    }
    for(UIComponent cpm : getChildren()) {
      getUICheckBoxInput(cpm.getId()).setChecked(true) ; 
    }
  }

  public List<String> getCheckedCalendars() {
    List<String> list = new ArrayList<String>();
    for(UIComponent cpm : getChildren())
      if (cpm instanceof UICheckBoxInput) {
        UICheckBoxInput checkbox = (UICheckBoxInput) cpm;
        if (checkbox.isChecked()) list.add(cpm.getId());
      }    
    return list ;
  }

  public String[] getCheckedPublicCalendars() {
    List<String> lstCheck = getCheckedCalendars();
    List<String> lstReturn = new ArrayList<String>();
    String[] publicCal = getPublicCalendarIds();
    if (publicCal != null && publicCal.length > 0) {
      for (String calId : publicCal) {
        if (lstCheck.contains(calId)) {
          lstReturn.add(calId);
        }
      }
    }
    return lstReturn.toArray(new String[lstReturn.size()]);
  }

  public EventQuery getEventQuery(EventQuery eventQuery) throws Exception {
    List<String> checkedCals = getCheckedCalendars() ;  
    List<String> calendarIds = new ArrayList<String>() ; 
    for (org.exoplatform.calendar.service.Calendar cal : getAllPrivateCalendars())
      if (checkedCals.contains(cal.getId())) calendarIds.add(cal.getId());
    for (GroupCalendarData calendarData : getPublicCalendars())
      for (org.exoplatform.calendar.service.Calendar  calendar : calendarData.getCalendars())
        if (checkedCals.contains(calendar.getId())) calendarIds.add(calendar.getId());
    GroupCalendarData sharedCalendars = getSharedCalendars();
    if (sharedCalendars != null) {
      for (org.exoplatform.calendar.service.Calendar cal : sharedCalendars.getCalendars()) {
        if (checkedCals.contains(cal.getId())) {
          calendarIds.add(cal.getId());
        }
      }
    }
    if (calendarIds.size() > 0)
      eventQuery.setCalendarId(calendarIds.toArray(new String[] {}));
    else {
      eventQuery.setCalendarId(new String[] {"null"});
    }
    eventQuery.setOrderBy(new String[] {Utils.EXO_SUMMARY});
    return eventQuery;
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
    boolean filterFromSetting = false;
    List<Calendar> calendars = calendarService.getUserCalendars(username, filterFromSetting) ;
    if (calendars.size() == 0) return new ArrayList<Calendar>(0);

    if(calendars != null) {
      for (Calendar calendar : calendars) {
        colorMap_.put(buildId(Calendar.TYPE_PRIVATE ,calendar.getId()), calendar.getCalendarColor()) ;
        UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
        if (checkbox == null) {
          checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
          checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
          addUIFormInput(checkbox);
        } else {
          setCheckedCheckbox(checkbox, calendar);
        }
      }
    }
    return calendars;
  }

  private void setCheckedCheckbox(UICheckBoxInput checkbox, Calendar calendar){
    UICalendarPortlet calendarPortlet = this.getAncestorOfType(UICalendarPortlet.class);
    UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
    boolean isListView = false;
    if(UICalendarViewContainer.LIST_VIEW.equals(uiViewContainer.getCurrentViewType())){
      isListView = true;
    }
    if(isListView){
      checkbox.setChecked(checkbox.isChecked());
    }else{
      checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
    }
  }


  /**
   * check whether calendar is calendar of space
   * based on id of calendar and space id
   *
   * @param groupIds
   * @return
   */
  protected boolean isCalendarOfSpace(String[] groupIds)
  {
    String spaceGroupId = UICalendarPortlet.getGroupIdOfSpace();
    if(spaceGroupId == null) {
      return true;
    }
    if(groupIds == null) {
      return false;
    }
    for(String groupId : groupIds) {
      if(spaceGroupId.equals(groupId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @deprecated - get group calendar for user
   *
   * @return
   * @throws Exception
   */
  @Deprecated
  public List<GroupCalendarData> getPublicCalendars() throws Exception{
    String username = CalendarUtils.getCurrentUser() ;
    String[] groups = CalendarUtils.getUserGroups(username) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(groups, false, username) ;
    Map<String, String> map = new HashMap<String, String> () ;
    for(GroupCalendarData group : groupCalendars) {
      List<Calendar> calendars = group.getCalendars() ;
      for(Calendar calendar : calendars) {
        map.put(calendar.getId(), calendar.getId()) ;
        colorMap_.put(buildId(Calendar.TYPE_PUBLIC, calendar.getId()), calendar.getCalendarColor()) ;
        UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
        if (checkbox == null) {
          checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
          checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
          addUIFormInput(checkbox);
        } else {
          setCheckedCheckbox(checkbox, calendar);
        }
      }
    }
    return groupCalendars ;
  }

  /**
   * used to open setting popup
   *
   * @see <code>UICalendars.gtmpl</code> - used in template
   * @return
   */
  private UIActionBar getUIActionBar()
  {
    return getAncestorOfType(UICalendarPortlet.class).getChild(UIActionBar.class);
  }

  /**
   *
   * @see <code>UICalendars.gtmpl</code> - used in template
   * @return
   * @throws Exception
   */
  private boolean hasFilteredCalendar() throws Exception
  {
    UICalendarPortlet portlet = getAncestorOfType(UICalendarPortlet.class) ;
    CalendarSetting calendarSetting = portlet.getCalendarSetting() ;
    if ((calendarSetting.getFilterPrivateCalendars().length == 0) &&
      (calendarSetting.getFilterSharedCalendars().length == 0) &&
      (calendarSetting.getFilterPublicCalendars().length == 0)) return false;
    return true;
  }

  /**
   * get group calendar for user, without duplicated items
   * if no calendar found, return zero length list of calendars
   *
   * @see <code>UICalendars.gtmpl</code> - used in template
   * @return
   * @throws Exception
   */
  public List<Calendar> getAllPublicCalendars() throws Exception
  {
    String username = CalendarUtils.getCurrentUser() ;
    String[] groups = CalendarUtils.getUserGroups(username) ;

    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    /* return all calendars for a list of group, filter by calendar setting */
    boolean filterFromSetting = false;
    List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(groups, filterFromSetting, username) ;
    if (groupCalendars.size() == 0) return new ArrayList<Calendar>(0);

    Map<String, String> map = new HashMap<String, String> () ;
    List<Calendar> calendars = new ArrayList<Calendar>();  /* contains all calendar */

    for (GroupCalendarData group : groupCalendars) {

      calendars.addAll(group.getCalendars()) ;
      for(Calendar calendar : calendars) {
        map.put(calendar.getId(), calendar.getId()) ;
        colorMap_.put(buildId(Calendar.TYPE_PUBLIC ,calendar.getId()), calendar.getCalendarColor()) ;

        /* add a checkbox for each calendar */
        UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
        if (checkbox == null) {
          checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
          checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
          addUIFormInput(checkbox);
        } else {
          setCheckedCheckbox(checkbox, calendar);
        }
      }
    }
    return new ArrayList<Calendar>(new HashSet<Calendar>(calendars));
  }
  
  public String[] getPublicCalendarIds() {
    try{ 
    String userId = CalendarUtils.getCurrentUser();
    String[] groups = CalendarUtils.getUserGroups(userId);
    if(UICalendarPortlet.isInSpace()) groups = new String[]{UICalendarPortlet.getGroupIdOfSpace()};
    CalendarService calendarService = CalendarUtils.getCalendarService();
    Set<String> set = new HashSet<String>();
    for (GroupCalendarData group : calendarService.getGroupCalendars(groups, false, userId)) {
      for (org.exoplatform.calendar.service.Calendar calendar : group.getCalendars()) {
        set.add(calendar.getId());
        colorMap_.put(buildId(Calendar.TYPE_PUBLIC, calendar.getId()), calendar.getCalendarColor());
      }
    }
    return set.toArray(new String[] {});
    } catch (Exception e) {
      return null;
    }
}

  /**
   * return a group of shared calendars for current users
   * if no calendar found, return a group calendar with zero length list of calendars
   *
   * @see <code>UICalendars.gtmpl</code> - used in template
   * @return
   * @throws Exception
   */
  public GroupCalendarData getSharedCalendars() throws Exception
  {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    /* get shared calendar but filter from setting */
    boolean filterFromSetting = false;
    GroupCalendarData groupCalendars = calendarService.getSharedCalendars(CalendarUtils.getCurrentUser(), filterFromSetting) ;
    if (groupCalendars == null) return new GroupCalendarData("", "", new ArrayList<Calendar>(0));

    /** does not get color from map */
    CalendarSetting setting = calendarService.getCalendarSetting(CalendarUtils.getCurrentUser()) ;
    Map<String, String> map = new HashMap<String, String>() ;
    for(String key : setting.getSharedCalendarsColors()) {
        map.put(key.split(CalendarUtils.COLON)[0], key.split(CalendarUtils.COLON)[1]) ;
    }

    List<Calendar> calendars = groupCalendars.getCalendars() ;
    for(Calendar calendar : calendars) {
      colorMap_.put(buildId(Calendar.TYPE_SHARED, calendar.getId()), calendar.getCalendarColor()) ;
      UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
      if (checkbox == null) {
        checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
        checkbox.setChecked(isCalendarOfSpace(calendar.getGroups()));
        addUIFormInput(checkbox);
      } else {
        setCheckedCheckbox(checkbox, calendar);
      }
    }

    return groupCalendars ;
  }

  public static String buildId(int calTye, String calId){
    return new StringBuffer(String.valueOf(calTye)).append(CalendarUtils.COLON).append(calId).toString();
  }

  /**
   * truncate a long name into a name with .. if length of name is larger than 20 characters
   * or return a name from the starting position to the second white space position
   *
   * @param longName
   * @return
   */
  private String truncateLongName(String longName)
  {
    if (longName.length() < 17) return longName;

    int secondWhiteSpacePos = getPositionOfSecondWhiteSpaceFrom(longName);
    if ( ( -1 < secondWhiteSpacePos) && (secondWhiteSpacePos < 20 ) )
      return longName.substring(0,secondWhiteSpacePos);

    if (longName.length() > 20) return longName.substring(0, 17) + "...";
    return longName;
  }

  /**
   * get index of second white space if the string has one
   * return -1 if not
   *
   * @param name
   * @return position
   */
  private int getPositionOfSecondWhiteSpaceFrom(String name)
  {
    int firstWhiteSpacePos = name.indexOf(" ");
    if (firstWhiteSpacePos == -1)  return -1;

    int secondWhiteSpacePos = name.indexOf(" ", firstWhiteSpacePos + 1);
    if (secondWhiteSpacePos == -1) return -1;
    return secondWhiteSpacePos;
  }


  public LinkedHashMap<String, String> getColorMap() {
    return colorMap_;
  }
  public String[] getColors() {
    return UIFormColorPicker.Colors.COLORNAMES ;
  }



  private boolean canAddTaskAndEvent(UICalendars uiComponent, String calendarId, String calType) throws Exception {
    CalendarService calService = CalendarUtils.getCalendarService() ;
    Calendar calendar = null;
    String currentUser = CalendarUtils.getCurrentUser() ;

    if(calType.equals(CalendarUtils.SHARED_TYPE)) {
      calendar = calService.getSharedCalendars(currentUser, true).getCalendarById(calendarId) ;
      return CalendarUtils.canEdit(null, Utils.getEditPerUsers(calendar), currentUser) ;
    } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
      calendar = calService.getGroupCalendar(calendarId) ;
      return CalendarUtils.canEdit(uiComponent.getApplicationComponent(OrganizationService.class), calendar.getEditPermission(), currentUser) ;
    }  
    return false ;
  }
  public boolean canEdit(String[] savePerms, String[] checkPerms) throws Exception{
    return CalendarUtils.hasEditPermission(savePerms, checkPerms);
  }

  public String getCheckPermissionString() throws Exception {
    return CalendarUtils.getCheckPermissionString();
  }

  public boolean isRemoteCalendar(String calendarId) throws Exception {
    String username = CalendarUtils.getCurrentUser();
    CalendarService calService = CalendarUtils.getCalendarService();
    return calService.isRemoteCalendar(username, calendarId);    
  }

  private void updateView(UICalendars uiComponent, Event<UICalendars> event) throws Exception {
    UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
    UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
    if(uiViewContainer.getRenderedChild()  instanceof UIListContainer) {
      UIListContainer list = (UIListContainer)uiViewContainer.getRenderedChild() ;
      UIListView uiListView = list.getChild(UIListView.class) ;
      if(uiListView.isDisplaySearchResult()) {
        uiListView.setDisplaySearchResult(false) ;
        uiListView.setCategoryId(null) ;
        uiListView.refresh() ;
        uiListView.setLastViewId(null) ;
        UISearchForm uiSearchForm = uiPortlet.findFirstComponentOfType(UISearchForm.class) ;
        uiSearchForm.reset() ;
        UIActionBar uiActionBar = uiPortlet.findFirstComponentOfType(UIActionBar.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      }
    }  
  }

  static  public class AddCalendarActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 600) ;
      uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
      UICalendarForm calendarForm = uiPopupContainer.addChild(UICalendarForm.class, null, null) ;
      calendarForm.setTimeZone(uiCalendarPortlet.getCalendarSetting().getTimeZone());
      calendarForm.groupCalId_ = categoryId ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  static  public class AddEventCategoryActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiCalendars = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiCalendars.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      try {
        popupAction.activate(UIEventCategoryManager.class, 470) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (PathNotFoundException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
        return;
      }
    }
  }

  static  public class AddEventActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String currentUser = CalendarUtils.getCurrentUser() ;

      try {
        String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
        String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
        Calendar calendar = CalendarUtils.getCalendar(calType, calendarId);
        if(calendar == null) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
        } else {
          // check if calendar is remote
          if(calService.isRemoteCalendar(currentUser, calendarId)) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, AbstractApplicationMessage.WARNING)) ;
            return;
          }        

          if(!CalendarUtils.PRIVATE_TYPE.equals(calType) && !uiComponent.canAddTaskAndEvent(uiComponent, calendarId, calType)) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
            return;
          }

          List<EventCategory> eventCategories = calService.getEventCategories(CalendarUtils.getCurrentUser()) ;
          if(eventCategories.isEmpty()) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarView.msg.event-category-list-empty", null)) ;
            return ;
          }  

          String clientTime = CalendarUtils.getCurrentTime(uiComponent) ;
          //String clientTime = event.getRequestContext().getRequestParameter(CURRENTTIME) ;
          String categoryId = event.getRequestContext().getRequestParameter("categoryId") ;
          UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
          popupAction.deActivate() ;
          UIQuickAddEvent uiQuickAddEvent = popupAction.activate(UIQuickAddEvent.class, 600) ;
          uiQuickAddEvent.setEvent(true) ;  
          uiQuickAddEvent.setId("UIQuickAddEvent") ;
          uiQuickAddEvent.update(calType, null) ;
          uiQuickAddEvent.setSelectedCalendar(calendarId) ;
          uiQuickAddEvent.init(uiCalendarPortlet.getCalendarSetting(), clientTime, null) ;
          if(categoryId != null && categoryId.trim().length() >0 && !categoryId.toLowerCase().equals("null")&& !categoryId.equals("calId")) {
            uiQuickAddEvent.setSelectedCategory(categoryId) ;
          } else {
            uiQuickAddEvent.setSelectedCategory("meeting") ;
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        }
      } catch (PathNotFoundException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
      }
    }
  }

  static  public class AddTaskActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String currentUser = CalendarUtils.getCurrentUser() ;
      try {
        String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;        
        String clientTime = CalendarUtils.getCurrentTime(uiComponent) ;
        String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
        String categoryId = event.getRequestContext().getRequestParameter("categoryId") ;
        Calendar calendar = CalendarUtils.getCalendar(calType, calendarId);
        if(calendar == null) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
        } else {
          // check if calendar is remote
          if(calService.isRemoteCalendar(currentUser, calendarId)) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, AbstractApplicationMessage.WARNING)) ;
            return;
          }        

          if(!CalendarUtils.PRIVATE_TYPE.equals(calType) && !uiComponent.canAddTaskAndEvent(uiComponent, calendarId, calType)) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
            return;
          }
          List<EventCategory> eventCategories = calService.getEventCategories(CalendarUtils.getCurrentUser()) ;
          if(eventCategories.isEmpty()) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarView.msg.event-category-list-empty", null)) ;
            return ;
          }  
          UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
          popupAction.deActivate() ;
          UIQuickAddEvent uiQuickAddTask = popupAction.activate(UIQuickAddEvent.class, 600) ;
          uiQuickAddTask.setEvent(false) ;  
          uiQuickAddTask.setId("UIQuickAddTask") ;
          uiQuickAddTask.init(uiCalendarPortlet.getCalendarSetting(), clientTime, null) ;
          uiQuickAddTask.update(calType, null) ;
          uiQuickAddTask.setSelectedCalendar(calendarId) ;
          if(categoryId != null && categoryId.trim().length() >0 && !categoryId.toLowerCase().equals("null")) {
            uiQuickAddTask.setSelectedCategory(categoryId) ;
          } else {
            uiQuickAddTask.setSelectedCategory("meeting") ;
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        }
      } catch (PathNotFoundException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
      }
    }
  }

  static  public class EditCalendarActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String username = CalendarUtils.getCurrentUser() ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      CalendarService calService = CalendarUtils.getCalendarService() ;

      if(CalendarUtils.SHARED_TYPE.equalsIgnoreCase(calType)) {

        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.not-support-edit-share-calendar", null, 1)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
        return ;
      }
      try {       
        Calendar calendar = null ;
        if(CalendarUtils.PRIVATE_TYPE.equals(calType)) 
        { 
          calendar = calService.getUserCalendar(username, calendarId) ;
        } else if (CalendarUtils.PUBLIC_TYPE.equals(calType)) {
          calendar = calService.getGroupCalendar(calendarId) ;
        }
        if(calendar == null)
        {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
        } else  
        {
          // check if remote calendar
          if (calService.isRemoteCalendar(username, calendarId)) {
            UIRemoteCalendar uiRemoteCalendar = popupAction.activate(UIRemoteCalendar.class, 600);
            uiRemoteCalendar.init(calendar);
            event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
            return;
          }

          String[] checkPerms = uiComponent.getCheckPermissionString().split(CalendarUtils.COMMA);
          if((CalendarUtils.SHARED_TYPE.equals(calType) && !uiComponent.canEdit(Utils.getEditPerUsers(calendar), checkPerms)) ||
              (CalendarUtils.PUBLIC_TYPE.equals(calType) && !uiComponent.canEdit(calendar.getEditPermission(), checkPerms))) 
          {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
            return;
          }
          UIPopupContainer uiPopupContainer = uiCalendarPortlet.createUIComponent(UIPopupContainer.class, null, null) ;
          uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
          UICalendarForm uiCalendarForm = uiPopupContainer.addChild(UICalendarForm.class, null, null) ;
          uiCalendarForm.calType_ = calType ;
          uiCalendarForm.init(calendar, uiCalendarPortlet.getCalendarSetting()) ;
          popupAction.activate(uiPopupContainer, 600, 0, true) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        }
      }catch (PathNotFoundException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
      }
      UICalendarViewContainer uiViewContainer = uiCalendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarSetting setting = calService.getCalendarSetting(username) ;
      uiViewContainer.refresh() ;
      uiCalendarPortlet.setCalendarSetting(setting) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
    }
  }
  static  public class ConfirmCloseActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String username = CalendarUtils.getCurrentUser() ;
      String calendarId = uiComponent.removed_cal_id ;
      String calType = uiComponent.calType;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      Calendar calendar = null ;

      if(CalendarUtils.SHARED_TYPE.equals(calType)){
        try {
          if(calService.getSharedCalendars(username, true) != null)
            calendar = calService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            calService.removeSharedCalendar(username, calendarId) ;
          }
        } catch (PathNotFoundException e) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        }
        UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
        uiPortlet.cancelAction() ;
        UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        uiComponent.updateView(uiComponent, event);
        CalendarSetting setting = calService.getCalendarSetting(username) ;
        uiViewContainer.refresh() ;
        uiPortlet.setCalendarSetting(setting) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
      } else {

      try {
        if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
          calendar = calService.getUserCalendar(username, calendarId) ;
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            Boolean isRemote = calService.isRemoteCalendar(username, calendarId);
            calService.removeUserCalendar(username, calendarId) ;
            if (isRemote) {
              if (calService.getRemoteCalendarCount(username) == 0) {
                // remove sync job
                calService.stopSynchronizeRemoteCalendarJob(username);
              }
            }
          }
        }else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
          calendar = calService.getGroupCalendar(calendarId) ;
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            boolean canEdit = false ;
            OrganizationService oService = uiComponent.getApplicationComponent(OrganizationService.class) ;
            for(GroupCalendarData groupCal : uiComponent.getPublicCalendars()) {
              for(Calendar cal : groupCal.getCalendars()) {
                if(cal.getId().equals(calendarId)) {
                  canEdit = CalendarUtils.canEdit(oService, (groupCal.getCalendarById(calendarId)).getEditPermission(), username) ;
                  break ;
                }
              }
            }
            if(canEdit) {
              calService.removePublicCalendar(calendarId) ;
            } else {
              event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarView.msg.have-no-delete-permission", null)) ;
              return ;
            }
          }
        }
      } catch (PathNotFoundException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
      }
      UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      uiPortlet.cancelAction() ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiComponent.updateView(uiComponent, event);
      CalendarSetting setting = calService.getCalendarSetting(username) ;
      uiViewContainer.refresh() ;
      uiPortlet.setCalendarSetting(setting) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
      }
    }
  }

  static  public class AbortCloseActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      uiComponent.removed_cal_id = null;
      uiComponent.calType = CalendarUtils.SHARED_TYPE;
      return;
    }
  }
  //, confirm="UICalendars.msg.confirm-delete-calendar"

  static  public class RemoveCalendarActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      uiComponent.removed_cal_id = calendarId;
      uiComponent.calType = calType;
      ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
      String message = resourceBundle.getString("UICalendars.msg.confirm-delete-calendar");
      calendarPortlet.showConfirmWindow(uiComponent, message);
      return ;
    }
  }

  //, confirm="UICalendars.msg.confirm-delete-sharedCalendar"
  static  public class RemoveSharedCalendarActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiComponent.removed_cal_id = calendarId;
      uiComponent.calType = CalendarUtils.SHARED_TYPE;
      ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
      String message = resourceBundle.getString("UICalendars.msg.confirm-delete-sharedCalendar");
      calendarPortlet.showConfirmWindow(uiComponent, message);
      return ;
    }
  }

  static  public class ExportCalendarActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      String currentUser = CalendarUtils.getCurrentUser() ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;

      Calendar calendar = null;
      if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        calendar = calService.getUserCalendar(currentUser, selectedCalendarId) ;
      } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
        GroupCalendarData gCalendarData = calService.getSharedCalendars(currentUser, true) ;
        if(gCalendarData != null) { 
          calendar = gCalendarData.getCalendarById(selectedCalendarId) ;
        }
      } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
        try {
          calendar = calService.getGroupCalendar(selectedCalendarId) ;
        } catch (PathNotFoundException e) {
          LOG.debug("\n\n calendar has been removed !");
        }
      }  
      if(calendar == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
      } else {
        boolean canEdit = false ;
        if(calType.equals(CalendarUtils.SHARED_TYPE)) {
          canEdit = CalendarUtils.canEdit(null, Utils.getEditPerUsers(calendar), currentUser) ;
        } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
          canEdit = CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), calendar.getEditPermission(), currentUser) ;
        }
        if(!calType.equals(CalendarUtils.PRIVATE_TYPE) && !canEdit) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null)) ;
          return ;
        }
        List<Calendar> list = new ArrayList<Calendar>() ;
        list.add(calendar) ;
        UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        UIExportForm exportForm = popupAction.activate(UIExportForm.class, 500) ;
        exportForm.update(calType, list, selectedCalendarId) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }
  }
  static  public class ExportCalendarsActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      String username = CalendarUtils.getCurrentUser() ;
      List<Calendar> list = CalendarUtils.getCalendarService().getUserCalendars(username, true);
      if(list.isEmpty()){
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.calendar-require", null)) ;
      } else {
        UIExportForm exportForm = popupAction.activate(UIExportForm.class, 500) ;
        exportForm.initCheckBox(list, null) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }
  }
  static  public class ImportCalendarActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 600) ;
      uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
      UIImportForm form = uiPopupContainer.addChild(UIImportForm.class,null,null); 
      form.init(selectedCalendarId, calType) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static  public class ChangeColorActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      uiComponent.getAncestorOfType(UICalendarPortlet.class).cancelAction() ;
      String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String color = event.getRequestContext().getRequestParameter(CALCOLOR) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;

      try{
        Calendar calendar = null ;
        if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
          calendar = calService.getUserCalendar(username, calendarId) ;
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            calendar.setCalendarColor(color) ;
            calService.saveUserCalendar(username, calendar, false) ;
          }
        } else if(CalendarUtils.SHARED_TYPE.equals(calType)){
          if(calService.getSharedCalendars(username, true) != null)
            calendar = calService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            calendar.setCalendarColor(color) ;
            calService.saveSharedCalendar(username, calendar) ;
          }
        } else if(CalendarUtils.PUBLIC_TYPE.equals(calType)){
          calendar = calService.getGroupCalendar(calendarId) ;
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            if(!CalendarUtils.canEdit(uiComponent.getApplicationComponent(OrganizationService.class), calendar.getEditPermission(), username)){
              event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, AbstractApplicationMessage.WARNING)) ;
              return ;
            }
            calendar.setCalendarColor(color) ;
            calService.savePublicCalendar(calendar, false) ;
          }
        }
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Can not change the color for the calendar", e);
        }
      }
      UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarSetting setting = calService.getCalendarSetting(username) ;
      uiViewContainer.refresh() ;
      uiPortlet.setCalendarSetting(setting) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
    }
  }
  static  public class CalendarSettingActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 600) ;
      uiPopupContainer.setId(UIPopupContainer.UICALENDAR_SETTING_POPUP);
      UICalendarSettingForm uiCalendarSettingForm = uiPopupContainer.addChild(UICalendarSettingForm.class, null, null) ;
      CalendarService cservice = CalendarUtils.getCalendarService() ;
      CalendarSetting calendarSetting = uiComponent.getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ; 
      uiCalendarSettingForm.init(calendarSetting, cservice) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static public class TickActionListener extends EventListener<UICalendars> {
    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiCalendars = event.getSource() ;
      UICalendarPortlet uiPortlet = uiCalendars.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      for(UIComponent comp : uiViewContainer.getChildren()) {
        if(comp.isRendered() && comp instanceof UIListView){
          ((UIListView)comp).setCalClicked(true) ;
        }
      }
      uiViewContainer.refresh();
      UICalendarContainer uiVContainer = uiPortlet.findFirstComponentOfType(UICalendarContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVContainer.findFirstComponentOfType(UIMiniCalendar.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;      
    }
  }  

  public static class RemoteCalendarActionListener extends EventListener<UICalendars> {

    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiCalendars = event.getSource();
      UICalendarPortlet uiPortlet = uiCalendars.getAncestorOfType(UICalendarPortlet.class);
      UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
      popupAction.deActivate();
      UISubscribeForm subscribeForm = popupAction.activate(UISubscribeForm.class, 450);
      subscribeForm.init(CalendarService.ICALENDAR, "");
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  public static class RefreshRemoteCalendarActionListener extends EventListener<UICalendars> {

    @Override
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiCalendars = event.getSource();
      UICalendarPortlet uiPortlet = uiCalendars.getAncestorOfType(UICalendarPortlet.class);
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String remoteCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String username = CalendarUtils.getCurrentUser();
      Calendar calendar = calService.getUserCalendar(username, remoteCalendarId);
      try {    
        calService.refreshRemoteCalendar(username, remoteCalendarId);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;      
      }
      catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Fail to refresh remote calendar", e);
        }
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-refresh-remote-calendar", new String[] {calendar.getName()}, AbstractApplicationMessage.WARNING)) ;
      }
    }

  }

  public static class ShareCalendarActionListener extends EventListener<UICalendars>
  {
    public void execute(Event<UICalendars> event) throws Exception
    {
      UICalendars uiComponent = event.getSource() ;
      String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;

      UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;

      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 500) ;
      uiPopupContainer.setId("UIPermissionSelectPopup") ;

      UISharedForm sharedForm = uiPopupContainer.addChild(UISharedForm.class, null, null);
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;
      Calendar cal = calService.getUserCalendar(username, selectedCalendarId) ;

      if (cal.getId().equals(Utils.getDefaultCalendarId(username)) && cal.getName().equals(NewUserListener.defaultCalendarName))
      {
        String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.defaultCalendarId, NewUserListener.defaultCalendarId);
        cal.setName(newName);
      }

      sharedForm.init(null, cal, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
