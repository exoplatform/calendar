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

import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.UIActionBar;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIListContainer;
import org.exoplatform.calendar.webui.UIListView;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.calendar.webui.UISearchForm;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIConfirmation;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Oct 3, 2007  
 */
@ComponentConfig(
                 lifecycle = UIContainerLifecycle.class,
                 events = {
                   @EventConfig(listeners = UIEventCategoryManager.EditActionListener.class),
                   @EventConfig(listeners = UIEventCategoryManager.DeleteActionListener.class),
                   @EventConfig(listeners = UIEventCategoryManager.ConfirmCloseActionListener.class),
                   @EventConfig(listeners = UIEventCategoryManager.AbortCloseActionListener.class)
                 }
)
public class UIEventCategoryManager extends UIContainer implements UIPopupComponent {
  public static String[] BEAN_FIELD = {"name"};
  private static String[] ACTION = {"Edit", "Delete"} ;
  public String categoryId_ ;
  private String confirmedCategoryId_;
  Map<String, String> defaultEventCategoriesMap = new LinkedHashMap<String, String>();

  public UIEventCategoryManager() throws Exception {
    this.setName("UIEventCategoryManager") ;
    UIGrid categoryList = addChild(UIGrid.class, null , "UIEventCategoryList") ;
    categoryList.configure("id", BEAN_FIELD, ACTION) ;
    categoryList.getUIPageIterator().setId("EventCategoryIterator");
    addChild(UIEventCategoryForm.class, null, null) ;
    updateGrid() ;
  }

  public long getCurrentPage() {
    return getChild(UIGrid.class).getUIPageIterator().getCurrentPage() ;
  }
  public long getAvailablePage() {
    return getChild(UIGrid.class).getUIPageIterator().getAvailablePage() ;
  }
  public void setCurrentPage(int page) throws Exception {
    getChild(UIGrid.class).getUIPageIterator().setCurrentPage(page) ;
  }
  @Override
  public void activate() throws Exception {}

  @Override
  public void deActivate() throws Exception {}
  public void resetForm() {
    getChild(UIEventCategoryForm.class).reset() ;
  }
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ;
    w.write("<div id=\"UIEventCategoryManager\" class=\"uiEventCategoryManager\">");
    renderChildren();
    w.write("</div>");
  }
  public void updateGrid() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = CalendarUtils.getCurrentUser() ;
    List<EventCategory>  categories = calService.getEventCategories(username) ;
    defaultEventCategoriesMap.clear();
    for (EventCategory category : categories) {
      // Check if EventCategory is default event category
      boolean isDefaultEventCategory = false;
      for (int i = 0; i < NewUserListener.defaultEventCategoryIds.length; i++) {
        if (category.getId().equals(NewUserListener.defaultEventCategoryIds[i])
            && category.getName().equals(NewUserListener.defaultEventCategoryNames[i])) {
          isDefaultEventCategory = true;
          break;
        }
      }
      
      if (isDefaultEventCategory) {
        String newName = CalendarUtils.getResourceBundle("UICalendarView.label." + category.getId(), category.getId());
        category.setName(newName);
        defaultEventCategoriesMap.put(category.getId(), newName);
      }
    }
    UIGrid uiGrid = getChild(UIGrid.class) ; 
    LazyPageList<EventCategory> pageList = new LazyPageList<EventCategory>(new ListAccessImpl<EventCategory>(EventCategory.class, categories), 10);
    uiGrid.getUIPageIterator().setPageList(pageList) ;   
  }

  static  public class EditActionListener extends EventListener<UIEventCategoryManager> {
    @Override
    public void execute(Event<UIEventCategoryManager> event) throws Exception {
      UIEventCategoryManager uiManager = event.getSource() ;
      UIEventCategoryForm uiForm = uiManager.getChild(UIEventCategoryForm.class) ;
      uiForm.setAddNew(false) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      EventCategory category = calService.getEventCategory(username, categoryId) ;
      uiForm.setEventCategory(category) ;
      if (uiManager.defaultEventCategoriesMap.containsKey(categoryId)) 
        category.setName(uiManager.defaultEventCategoriesMap.get(categoryId));      
      uiForm.setCategoryName(category.getName());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  static  public class DeleteActionListener extends EventListener<UIEventCategoryManager> {
    @Override
    public void execute(Event<UIEventCategoryManager> event) throws Exception {
      UIEventCategoryManager uiManager = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiManager.getAncestorOfType(UICalendarPortlet.class) ;
      String eventCategoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if (eventCategoryId.equalsIgnoreCase(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIEventCategoryManager.msg.cannot-delete", null));
        return ;        
      }
      uiManager.confirmedCategoryId_ = eventCategoryId;
      //, confirm = "UIEventCategoryManager.msg.confirm-delete"
      ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
      String message = resourceBundle.getString("UIEventCategoryManager.msg.confirm-delete");
      calendarPortlet.showConfirmWindow(uiManager, message);
      return ;
      /*
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      calService.removeEventCategory(username, eventCategoryId) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; 
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      if(uiViewContainer.getRenderedChild()  instanceof UIListContainer) {
        UIListContainer list = (UIListContainer)uiViewContainer.getRenderedChild() ;
        UIListView uiListView = list.getChild(UIListView.class) ;
        if(uiListView.isDisplaySearchResult()) {
          uiListView.setDisplaySearchResult(false) ;
          uiListView.setCategoryId(null) ;
          uiListView.refresh() ;
          uiListView.setLastViewId(null) ;
          UISearchForm uiSearchForm = calendarPortlet.findFirstComponentOfType(UISearchForm.class) ;
          uiSearchForm.reset() ;
          UIActionBar uiActionBar = calendarPortlet.findFirstComponentOfType(UIActionBar.class) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
        }
      }
      uiViewContainer.updateCategory() ;
      uiViewContainer.refresh() ;
      UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
      Long currentPage  = uiManager.getCurrentPage() ;
      uiManager.updateGrid() ;
      if(currentPage <= uiManager.getAvailablePage()) uiManager.setCurrentPage(currentPage.intValue()) ;
      uiManager.resetForm() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getAncestorOfType(UIPopupAction.class)) ;
      UIEventDetailTab uiEventDetailTab = calendarPortlet.findFirstComponentOfType(UIEventDetailTab.class) ;
      UITaskDetailTab uiTaskDetailTab = calendarPortlet.findFirstComponentOfType(UITaskDetailTab.class) ;
      if(uiEventDetailTab != null) { 
        uiEventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory());
        uiEventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(uiManager.categoryId_) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventDetailTab) ;
      }
      if(uiTaskDetailTab != null) {
        uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory());
        uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setValue(uiManager.categoryId_) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskDetailTab) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
      */
    }
  }
  static  public class ConfirmCloseActionListener extends EventListener<UIEventCategoryManager> {
      @Override
      public void execute(Event<UIEventCategoryManager> event) throws Exception {

        UIEventCategoryManager uiManager = event.getSource() ;
        UICalendarPortlet calendarPortlet = uiManager.getAncestorOfType(UICalendarPortlet.class) ;
        if (uiManager.confirmedCategoryId_.equalsIgnoreCase(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL)) {
          event.getRequestContext()
                  .getUIApplication()
                  .addMessage(new ApplicationMessage("UIEventCategoryManager.msg.cannot-delete", null));
          return ;
        }
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      calService.removeEventCategory(username, uiManager.confirmedCategoryId_) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      if(uiViewContainer.getRenderedChild()  instanceof UIListContainer) {
        UIListContainer list = (UIListContainer)uiViewContainer.getRenderedChild() ;
        UIListView uiListView = list.getChild(UIListView.class) ;
        if(uiListView.isDisplaySearchResult()) {
          uiListView.setDisplaySearchResult(false) ;
          uiListView.setCategoryId(null) ;
          uiListView.refresh() ;
          uiListView.setLastViewId(null) ;
          UISearchForm uiSearchForm = calendarPortlet.findFirstComponentOfType(UISearchForm.class) ;
          uiSearchForm.reset() ;
          UIActionBar uiActionBar = calendarPortlet.findFirstComponentOfType(UIActionBar.class) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
        }
      }
      uiViewContainer.updateCategory() ;
      uiViewContainer.refresh() ;
      UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
      Long currentPage  = uiManager.getCurrentPage() ;
      uiManager.updateGrid() ;
      if(currentPage <= uiManager.getAvailablePage()) uiManager.setCurrentPage(currentPage.intValue()) ;
      uiManager.resetForm() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getAncestorOfType(UIPopupAction.class)) ;
      UIEventDetailTab uiEventDetailTab = calendarPortlet.findFirstComponentOfType(UIEventDetailTab.class) ;
      UITaskDetailTab uiTaskDetailTab = calendarPortlet.findFirstComponentOfType(UITaskDetailTab.class) ;
      if(uiEventDetailTab != null) {
        uiEventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory());
        uiEventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setValue(uiManager.categoryId_) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventDetailTab) ;
      }
      if(uiTaskDetailTab != null) {
        uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setOptions(CalendarUtils.getCategory());
        uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setValue(uiManager.categoryId_) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskDetailTab) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet) ;
      }
    }

  static  public class AbortCloseActionListener extends EventListener<UIEventCategoryManager> {
      @Override
      public void execute(Event<UIEventCategoryManager> event) throws Exception {
        UIEventCategoryManager uiManager = event.getSource() ;
        uiManager.confirmedCategoryId_ = null;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      }
    }


}
