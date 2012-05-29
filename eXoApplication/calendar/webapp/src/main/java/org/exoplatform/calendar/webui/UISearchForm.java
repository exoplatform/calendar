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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UISearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UISearchForm.SearchActionListener.class),
      @EventConfig(listeners = UISearchForm.AdvancedSearchActionListener.class)
    }
)
public class UISearchForm extends UIForm {
  private static final Log log = ExoLogger.getExoLogger(UISearchForm.class);
  
  final static  private String FIELD_SEARCHVALUE = "value" ;

  public UISearchForm() throws Exception {
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null).addValidator(SpecialCharacterValidator.class)) ;
  }
  public String getSearchValue() {
    return getUIStringInput(FIELD_SEARCHVALUE).getValue() ;
  }
  public String[] getPublicCalendars() throws Exception{
      String[] groups = CalendarUtils.getUserGroups(CalendarUtils.getCurrentUser()) ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      Map<String, String> map = new HashMap<String, String> () ;    
      for(GroupCalendarData group : calendarService.getGroupCalendars(groups, true, CalendarUtils.getCurrentUser())) {
        for(org.exoplatform.calendar.service.Calendar calendar : group.getCalendars()) {
          map.put(calendar.getId(), calendar.getId()) ;          
        }
      }
      return map.values().toArray(new String[map.values().size()] ) ;
  }
  static  public class SearchActionListener extends EventListener<UISearchForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource() ;
      String text = uiForm.getSearchValue() ;
     if(CalendarUtils.isEmpty(text))   {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISearchForm.msg.no-text-to-search", null));
        return ;
      }
     
      try {
        EventQuery eventQuery = new EventQuery() ;
        eventQuery.setText(CalendarUtils.encodeJCRText(text)) ;
        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UICalendarViewContainer calendarViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        String currentView = calendarViewContainer.getRenderedChild().getId() ;
        if(calendarViewContainer.getRenderedChild() instanceof UIWeekView) {
          if(((UIWeekView)calendarViewContainer.getRenderedChild()).isShowCustomView()) {
            currentView = UICalendarViewContainer.WORKING_VIEW;
          }
        }
        calendarViewContainer.initView(UICalendarViewContainer.LIST_VIEW) ;
        UIListView uiListView = calendarViewContainer.findFirstComponentOfType(UIListView.class) ;
        if(!uiListView.isDisplaySearchResult()) {
          uiListView.setLastViewId(currentView) ;
        }

        UICalendars uiCalendars = uiForm.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UICalendars.class);
        eventQuery = uiCalendars.getEventQuery(eventQuery);
        uiListView.setViewType(UIListView.TYPE_BOTH);
        uiListView.setSortedField(UIListView.EVENT_START);
        uiListView.setIsAscending(false);
        eventQuery.setOrderBy(new String[] { Utils.EXO_FROM_DATE_TIME });
        eventQuery.setOrderType(Utils.DESCENDING);
        uiListView.setEventQuery(eventQuery);
        uiListView.setDisplaySearchResult(true);
        List<CalendarEvent> allEvents = uiListView.getAllEvents(eventQuery);
        uiListView.update(new EventPageList(allEvents, 10));
        uiListView.setSelectedEvent(null);
        uiListView.setLastUpdatedEventId(null);
        calendarViewContainer.findFirstComponentOfType(UIPreview.class).setEvent(null) ;
        UIActionBar uiActionBar = calendarPortlet.findFirstComponentOfType(UIActionBar.class) ;
        uiActionBar.setCurrentView(UICalendarViewContainer.LIST_VIEW) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarViewContainer) ;
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to search the calendar", e);
        }
        return;
      }
    }
  }
  static  public class AdvancedSearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      UIAdvancedSearchForm uiAdvancedSearchForm = popupAction.activate(UIAdvancedSearchForm.class, 600) ;
      uiAdvancedSearchForm.setSearchValue(uiForm.getSearchValue()) ;
      uiForm.reset() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
