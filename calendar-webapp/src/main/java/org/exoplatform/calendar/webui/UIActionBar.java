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

import java.util.List;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/calendar/webui/UIActionBar.gtmpl", 
    events = {
        @EventConfig(listeners = UIActionBar.QuickAddEventActionListener.class),
        @EventConfig(listeners = UIActionBar.ChangeViewActionListener.class),
        @EventConfig(listeners = UIActionBar.SettingActionListener.class),
        @EventConfig(listeners = UIActionBar.TodayActionListener.class)
    }
)
public class UIActionBar extends UIContainer  {

  final static String CATEGORYID = "categoryId".intern() ;
  private boolean isShowPane_ = true ;
  private String currentView_ = null ;

  private static final Log LOG = ExoLogger.getExoLogger(UIActionBar.class);

  public UIActionBar() throws Exception {
    addChild(UISearchForm.class, null, null) ;
  }
  
  protected String[] getViewTypes() {return UICalendarViewContainer.TYPES ;} 
  protected String getCurrentView() {return currentView_ ;}
  public void setCurrentView(String viewName) {currentView_ = viewName ;}

  protected boolean isShowPane() {return isShowPane_ ;}
  protected void setShowPane(boolean isShow) {isShowPane_ = isShow ;}
  
  static public class QuickAddEventActionListener extends EventListener<UIActionBar> {
    @Override
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      if(CalendarUtils.getCalendarOption().isEmpty()) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
        return ;
      }
      List<EventCategory> eventCategories = CalendarUtils.getCalendarService().getEventCategories(CalendarUtils.getCurrentUser()) ;
      if(eventCategories.isEmpty()) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarView.msg.event-category-list-empty", null)) ;
        return ;
      }
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarWorkingContainer workContainer = uiPortlet.findFirstComponentOfType(UICalendarWorkingContainer.class) ;
      workContainer.getChild(UIPopupWindow.class).setShow(true) ;
      
    }
  }

  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    @Override
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String categoryId = event.getRequestContext().getRequestParameter("categoryId") ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      UICalendarContainer uiVContainer = uiPortlet.findFirstComponentOfType(UICalendarContainer.class) ;
      uiVContainer.findFirstComponentOfType(UICalendars.class).checkAll();
      uiViewContainer.initView(viewType);
      UIMiniCalendar miniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      miniCalendar.setCategoryId(categoryId) ; 
      if(uiViewContainer.getRenderedChild() instanceof UIListContainer) {
        UIListContainer listContainer = (UIListContainer) uiViewContainer.getRenderedChild() ;
        listContainer.setSelectedCategory(categoryId) ;
      }
      uiActionBar.setCurrentView(viewType) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiVContainer) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }  

  static public class TodayActionListener extends EventListener<UIActionBar> {
    @Override
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      UICalendarContainer uiCalendarContainer = uiPortlet.findFirstComponentOfType(UICalendarContainer.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarView renderedChild = (CalendarView)uiViewContainer.getRenderedChild() ;
      if(renderedChild instanceof UIListContainer) {
        UIListContainer listContainer = (UIListContainer)renderedChild ; 
        if(listContainer.isDisplaySearchResult()) {
          listContainer.setDisplaySearchResult(false) ;
          UIListView uiListView = listContainer.getChild(UIListView.class) ;
          uiViewContainer.initView(uiListView.getLastViewId()) ;
          uiActionBar.setCurrentView(uiListView.getLastViewId()) ;
          uiListView.setLastViewId(null) ;
        } 
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      renderedChild.setCurrentCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
      renderedChild.refresh() ;
      uiMiniCalendar.setCurrentCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarContainer) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }  
  static public class SettingActionListener extends EventListener<UIActionBar> {
    @Override
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      // 311px + borders, header and footer heights equals 450px which is the max height authorized for this popup
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, null, 600, 311) ;
      uiPopupContainer.setId(UIPopupContainer.UICALENDAR_SETTING_POPUP);
      UICalendarSettingForm uiCalendarSettingForm = uiPopupContainer.addChild(UICalendarSettingForm.class, null, null) ;
      CalendarService cservice = CalendarUtils.getCalendarService() ;
      CalendarSetting calendarSetting = calendarPortlet.getCalendarSetting() ;
      uiCalendarSettingForm.init(calendarSetting, cservice) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
