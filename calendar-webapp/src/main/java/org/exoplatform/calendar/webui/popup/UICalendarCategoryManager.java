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
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
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
      @EventConfig(listeners = UICalendarCategoryManager.EditActionListener.class),
      @EventConfig(listeners = UICalendarCategoryManager.DeleteActionListener.class, confirm = "UICalendarCategoryManager.msg.confirm-delete")
    }
)
public class UICalendarCategoryManager extends UIContainer implements UIPopupComponent {
  public static String[] BEAN_FIELD = {"name"};
  private static String[] ACTION = {"Edit", "Delete"} ;
  public UICalendarCategoryManager() throws Exception {
    this.setName("UICalendarCategoryManager") ;
    UIGrid categoryList = addChild(UIGrid.class, null , "UICategoryList") ;
    categoryList.configure("id", BEAN_FIELD, ACTION) ;
    categoryList.getUIPageIterator().setId("CategoryIterator");
    addChild(UICalendarCategoryForm.class, null, null) ;
    updateGrid() ;
  }

  public void activate() throws Exception {}
  
  public void deActivate() throws Exception {}
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ;
    w.write("<div id=\"UICalendarCategoryManager\" class=\"UICalendarCategoryManager\">");
    renderChildren();
    w.write("</div>");
  }
  public void updateGrid() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = CalendarUtils.getCurrentUser() ;
    List<CalendarCategory> categories = calService.getCategories(username) ;
    for (CalendarCategory calendarCategory : categories)
      if (calendarCategory.getId().equals(NewUserListener.defaultCalendarCategoryId) && calendarCategory.getName().equals(NewUserListener.defaultCalendarCategoryName)) {
        String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.defaultCalendarCategoryId, NewUserListener.defaultCalendarCategoryId);
        calendarCategory.setName(newName);
      }
    UIGrid uiGrid = getChild(UIGrid.class) ; 
    //ObjectPageList objPageList = new ObjectPageList(categories, 20) ;
    LazyPageList<CalendarCategory> pageList = new LazyPageList<CalendarCategory>(new ListAccessImpl<CalendarCategory>(CalendarCategory.class, categories), 20) ;
    uiGrid.getUIPageIterator().setPageList(pageList) ;   
  }
  public void resetForm() {
    getChild(UICalendarCategoryForm.class).reset() ;
  }
  static  public class EditActionListener extends EventListener<UICalendarCategoryManager> {
    public void execute(Event<UICalendarCategoryManager> event) throws Exception {
      UICalendarCategoryManager uiManager = event.getSource() ;
      UICalendarCategoryForm uiForm = uiManager.getChild(UICalendarCategoryForm.class) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.init(categoryId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  static  public class DeleteActionListener extends EventListener<UICalendarCategoryManager> {
    public void execute(Event<UICalendarCategoryManager> event) throws Exception {
      UICalendarCategoryManager uiManager = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiManager.getAncestorOfType(UICalendarPortlet.class) ;
      String calendarCategoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarForm uiCalendarForm = calendarPortlet.findFirstComponentOfType(UICalendarForm.class) ;
      if(uiCalendarForm != null && !uiCalendarForm.isAddNew() && uiCalendarForm.calendar_ != null 
          && uiCalendarForm.calendar_.getCategoryId().contains(calendarCategoryId)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UICalendarCategoryManager.msg.can-not-delete-calendar-in-use", null));
        return ;        
      }
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      calService.removeCalendarCategory(username, calendarCategoryId) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
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
      uiViewContainer.refresh() ;
      UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      uiMiniCalendar.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
      uiManager.updateGrid() ;
      uiManager.resetForm() ;
      
      if(uiCalendarForm != null) {
        uiCalendarForm.reloadCategory() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarForm.getChildById(UICalendarForm.INPUT_CALENDAR)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  
}
