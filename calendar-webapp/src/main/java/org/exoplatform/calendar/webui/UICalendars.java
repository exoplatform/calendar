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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jcr.PathNotFoundException;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.model.CompositeID;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarHandler;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.ExtendedCalendarService;
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
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
 * <br>modified by: <a href="mailto:tuna@exoplatform.com">Anh-Tu NGUYEN</a>
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

  private String removed_cal_id = null;
  private String calType = CalendarUtils.SHARED_TYPE;
  
  private ExtendedCalendarService xCalService = getApplicationComponent(ExtendedCalendarService.class);

  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    init();
    super.processRender(arg0);
  }

  public void init() throws Exception {
    String invisibleCalendars = "";
    SettingService settingService = getApplicationComponent(SettingService.class);
    SettingValue<?> value = settingService.get(Context.USER, Scope.APPLICATION.id(UICalendarPortlet.CALENDAR_APP_SETTING_SCOPE), UICalendarPortlet.CALENDAR_INVISIBLE_SETTING_KEY);
    if (value != null) {
      invisibleCalendars = (String) value.getValue();
    }

    Map<String, List<Calendar>> tmp = getCalendars();
    for (List<Calendar> cals : tmp.values()) {
      for (Calendar calendar :cals) {
        initCheckBox(calendar, invisibleCalendars.contains(calendar.getId()));
      }
    }
  }

  private void initCheckBox(Calendar calendar, boolean invisible) {
    UICheckBoxInput checkbox = getUICheckBoxInput(calendar.getId());
    if (checkbox == null) {
      checkbox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
      addUIFormInput(checkbox);
    }

    if (invisible) {
      checkbox.setChecked(false);
    } else {
      checkbox.setChecked(isCalendarOfSpace(calendar));
    }
  }

  public Map<String, List<Calendar>> getCalendars() {
    UICalendarWorkingContainer container = getAncestorOfType(UICalendarWorkingContainer.class);
    return container.getCalendarMap();
  }

  @Override
  public String getLabel(String key) {
    try {
      return super.getLabel(key) ;
    } catch (Exception e) {
      return key ;
    }
  }

  public void checkAll() {
    //if (UICalendarPortlet.getSpaceGroupId() != null) {
    if (!getAncestorOfType(UICalendarPortlet.class).getSpaceGroupId().equals("")) {
      for(UIComponent component : getChildren()){
        for (Calendar cal : getAllPublicCalendars()) {
          if (cal.getId().equals(component.getId())) {
            if(getUICheckBoxInput(component.getId()) != null) {
              getUICheckBoxInput(component.getId()).setChecked(isCalendarOfSpace(cal)) ;
            }
          }
        }
      }
    } else {
      for(UIComponent cpm : getChildren()) {
        getUICheckBoxInput(cpm.getId()).setChecked(true) ;
      }      
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

  public EventQuery getEventQuery(EventQuery eventQuery) throws Exception {
    List<String> checkedCals = getCheckedCalendars() ;
    List<String> calendarIds = new ArrayList<String>() ;
    for (org.exoplatform.calendar.service.Calendar cal : getAllPrivateCalendars()) {
      if (checkedCals.contains(cal.getId())) calendarIds.add(cal.getId());
    }
    for (org.exoplatform.calendar.service.Calendar  calendar : getAllPublicCalendars()) {
      if (checkedCals.contains(calendar.getId())) calendarIds.add(calendar.getId());      
    }
    for (org.exoplatform.calendar.service.Calendar cal : getAllSharedCalendars()) {
      if (checkedCals.contains(cal.getId())) {
        calendarIds.add(cal.getId());
      }
    }
    for (org.exoplatform.calendar.service.Calendar cal : getAllOtherCalendars()) {
      if (checkedCals.contains(cal.getId())) {
        calendarIds.add(cal.getId());
      }
    }
    if (calendarIds.size() > 0)
      eventQuery.setCalendarIds(calendarIds.toArray(new String[] {}));
    else {
      eventQuery.setCalendarIds(new String[] {"null"});
    }
    eventQuery.setOrderBy(new String[] {Utils.EXO_SUMMARY});
    return eventQuery;
  }

  private boolean hasNoCalendarShown() throws Exception
  {
    int privateCalendars = getAllPrivateCalendars().size();
    int sharedCalendars  = getAllSharedCalendars().size();
    int publicCalendars  = getAllPublicCalendars().size();
    int othersCalendars  = getAllOtherCalendars().size();
    if ( (privateCalendars == 0) && (sharedCalendars == 0) && (publicCalendars == 0) && othersCalendars == 0)
      return true;
    return false;
  }

  private boolean hasNoSpaceCalendarShown() {
    try {
      UICalendarPortlet calendarPortlet = getAncestorOfType(UICalendarPortlet.class);
      if(!calendarPortlet.isInSpaceContext()) {
        return false;
      }

      List<Calendar> publicCalendars = getAllPublicCalendars();
      if(publicCalendars.size() == 0) {
        return true;
      }
      for(Calendar c : publicCalendars) {
        // Check if this public calendar belong to current space
        if(isCalendarOfSpace(c)) {
          return false;
        }
      }
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * get all private calendars for current user
   * if no calendar is found, return a zero length list of calendar
   *
   * @see <code>UICalendars.gtmpl</code> - used in template
   * @return
   */
  public List<Calendar> getAllPrivateCalendars() {
    return filterHidden(getCalendars().get(Calendar.Type.PERSONAL.name()));    
  }
  
  private List<Calendar> filterHidden(List<Calendar> calendars) {
    List<Calendar> result = new LinkedList<Calendar>();
    
    if (calendars != null && !calendars.isEmpty()) {
      CalendarService calService = getApplicationComponent(CalendarService.class);
      Set<String> filterCals = new HashSet<String>();
      try {
        CalendarSetting settings = calService.getCalendarSetting(CalendarUtils.getCurrentUser());
        filterCals.addAll(settings.getFilterCalendars());
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
      
      for (Calendar cal : calendars) {
        if (!filterCals.contains(cal.getId())) {
          result.add(cal);
        }
      }      
    }
    return result;
  }

  public List<Calendar> getAllSharedCalendars() {
    return filterHidden(getCalendars().get(Calendar.Type.SHARED.name()));
  }
  
  public List<Calendar> getAllOtherCalendars() {    
    List<Calendar> cals = new LinkedList<Calendar>();
    Set<String> typeNames = new HashSet<String>();
    for (Calendar.Type t : Calendar.Type.values()) {
      if (!Calendar.Type.UNDEFINED.equals(t)) {
        typeNames.add(t.name());        
      }
    }
    

    for (String type : getCalendars().keySet()) {
      if (!typeNames.contains(type)) {
        cals.addAll(getCalendars().get(type));        
      }
    }
    return filterHidden(cals);
  }

  protected boolean isCalendarOfSpace(Calendar calendar)
  {    
    UICalendarPortlet calendarPortlet = getAncestorOfType(UICalendarPortlet.class);
    UICalendarView calView = calendarPortlet.findFirstComponentOfType(UICalendarView.class);
    Set<String> otherCals = new HashSet<String>();
    try {
      otherCals.addAll(calView.getOtherSpaceCalendar());
    } catch (Exception e) {
      LOG.error(e);
    }
    //
    if (otherCals.contains(calendar.getId())) {
      return true;
    } else {
      String[] groupIds = calendar.getGroups();
      String spaceGroupId = calendarPortlet != null ? calendarPortlet.getSpaceGroupId()
                                                    : UICalendarPortlet.getGroupIdOfSpace();
      if (spaceGroupId.equals("")) {
        return true;
      }
      if (groupIds == null) {
        return false;
      }
      
      for (String groupId : groupIds) {
        if (spaceGroupId.equals(groupId)) {
          return true;
        }
      }      
    }
    return false;
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
   */
  public List<Calendar> getAllPublicCalendars() {
    return filterHidden(getCalendars().get(Calendar.Type.GROUP.name()));
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


  public Map<String, String> getColorMap() {
    UICalendarWorkingContainer container = getAncestorOfType(UICalendarWorkingContainer.class);
    return container.getColorMap();
  }
  public String[] getColors() {
    return UIFormColorPicker.Colors.COLORNAMES ;
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
        Calendar calendar = Calendar.build(uiComponent.xCalService.getCalendarHandler().getCalendarById(calendarId));
        if(calendar == null) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
        } else {
          // check if calendar is remote
          if(calendar.isRemote()) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, AbstractApplicationMessage.WARNING)) ;
            return;
          }

          if(!Utils.isCalendarEditable(currentUser, calendar)) {
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
          CompositeID compositeID = CompositeID.parse(calendarId);
          uiQuickAddEvent.setSelectedCalendar(compositeID.getId()) ;
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
        Calendar calendar = Calendar.build(uiComponent.xCalService.getCalendarHandler().getCalendarById(calendarId));
        if(calendar == null) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
        } else {
          // check if calendar is remote
          if(calendar.isRemote()) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.cant-add-event-on-remote-calendar", null, AbstractApplicationMessage.WARNING)) ;
            return;
          }

          if(!Utils.isCalendarEditable(currentUser, calendar)) {
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
          CompositeID compositeID = CompositeID.parse(calendarId);
          uiQuickAddTask.setSelectedCalendar(compositeID.getId()) ;
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
      Calendar calendar = Calendar.build(uiComponent.xCalService.getCalendarHandler().getCalendarById(calendarId));
      if (calendar == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
      } else {
        if (calendar.isShared(username)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.not-support-edit-share-calendar", null, 1)) ;
          Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
        } else if (calendar.isRemote()) {
          UIRemoteCalendar uiRemoteCalendar = popupAction.activate(UIRemoteCalendar.class, 600);
          uiRemoteCalendar.init(calendar);
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        } else if (!Utils.isCalendarEditable(username, calendar)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
          Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
        } else {
          UIPopupContainer uiPopupContainer = uiCalendarPortlet.createUIComponent(UIPopupContainer.class, null, null) ;
          uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
          UICalendarForm uiCalendarForm = uiPopupContainer.addChild(UICalendarForm.class, null, null) ;
          uiCalendarForm.calType_ = calType ;
          uiCalendarForm.init(calendar, uiCalendarPortlet.getCalendarSetting()) ;
          popupAction.activate(uiPopupContainer, 600, 0, true) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        }
      }
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

      if(CalendarUtils.SHARED_TYPE.equals(calType)) {
        calendarId = CompositeID.parse(calendarId).getId();
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
      } else {
        CalendarHandler handler = uiComponent.xCalService.getCalendarHandler();
        Calendar cal = Calendar.build(handler.getCalendarById(calendarId));
        if (cal == null) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        } else {
          if (Utils.isCalendarEditable(username, cal, false)) {
            handler.removeCalendar(calendarId);
            if (cal.isRemote() && calService.getRemoteCalendarCount(username) == 0) {
              // remove sync job
              calService.stopSynchronizeRemoteCalendarJob(username);
            }              
          } else {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarView.msg.have-no-delete-permission", null)) ;
            return ;
          }
        }        
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
      String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      Calendar calendar =  Calendar.build(uiComponent.xCalService.getCalendarHandler().getCalendarById(selectedCalendarId));
      if (calendar == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
      } else if (!Utils.isCalendarEditable(currentUser, calendar)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null)) ;
        Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(true);
      } else {
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
      List<Calendar> list = uiComponent.getAllPrivateCalendars();
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
      String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (selectedCalendarId != null) {
        CompositeID compositeID = CompositeID.parse(selectedCalendarId);
        selectedCalendarId = compositeID.getId();
      }
      
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
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;

      try{
        org.exoplatform.calendar.model.Calendar cal = uiComponent.xCalService.getCalendarHandler().getCalendarById(calendarId); 
        Calendar calendar = Calendar.build(cal);
        if (calendar == null) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        } else if (!Utils.isCalendarEditable(username, calendar, false)) { // Color could be changed even if it's a remote calendar
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, AbstractApplicationMessage.WARNING)) ;          
        } else {
          if (calendar.isShared(username)) {
            calendar.setCalendarColor(color) ;
            calService.saveSharedCalendar(username, calendar);
          } else {
            cal.setCalendarColor(color);
            uiComponent.xCalService.getCalendarHandler().updateCalendar(cal);
          }
          
          UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
          UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
          CalendarSetting setting = calService.getCalendarSetting(username) ;
          uiViewContainer.refresh() ;
          uiPortlet.setCalendarSetting(setting) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        }        
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Can not change the color for the calendar", e);
        }
      }      
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
        if(comp.isRendered() && comp instanceof UIListContainer){
          UIListContainer container = (UIListContainer)comp;
          container.getChild(UIListView.class).setCalClicked(true);
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
      Calendar calendar = Calendar.build(uiCalendars.xCalService.getCalendarHandler().getCalendarById(remoteCalendarId));
      try {
        calService.refreshRemoteCalendar(username, calendar.getId());
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
      String username = CalendarUtils.getCurrentUser() ;
      Calendar cal = Calendar.build(uiComponent.xCalService.getCalendarHandler().getCalendarById(selectedCalendarId));

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
