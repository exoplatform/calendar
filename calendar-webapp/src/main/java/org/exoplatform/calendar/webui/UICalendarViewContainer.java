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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/calendar/webui/UICalendarViewContainer.gtmpl"
)

public class UICalendarViewContainer extends UIContainer  {

  final public static String DAY_VIEW = "UIDayView".intern() ;
  final public static String WEEK_VIEW = "UIWeekView".intern() ;
  final public static String MONTH_VIEW = "UIMonthView".intern() ;
  final public static String LIST_VIEW = "UIListContainer".intern() ;
  final public static String WORKING_VIEW = "UIWorkingView".intern() ;

  final public static String[] TYPES = {DAY_VIEW, WEEK_VIEW, MONTH_VIEW, LIST_VIEW, WORKING_VIEW} ;

  private String currentViewType_;

  private static final Log LOG = ExoLogger.getExoLogger(UICalendarViewContainer.class);

  public UICalendarViewContainer() throws Exception {
    initView(null) ;
  }


  public void initView(String viewType) throws Exception {
    CalendarSetting calendarSetting = new CalendarSetting();
    try {
      calendarSetting = getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ;
    }catch (Exception e) {
      CalendarService cservice = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;
      calendarSetting =  cservice.getCalendarSetting(username) ;
    }

    if (viewType == null) viewType = TYPES[Integer.parseInt(calendarSetting.getViewType())] ;
    currentViewType_ = viewType;

    if (DAY_VIEW.equals(viewType)) {
      UIDayView uiView = getChild(UIDayView.class) ;
      if(uiView == null) uiView =  addChild(UIDayView.class, null, null) ;
      if(getRenderedChild() != null) uiView.setCurrentCalendar(((CalendarView)getRenderedChild()).getCurrentCalendar()) ;
      setRenderedChild(viewType) ;
    }
    else if (WEEK_VIEW.equals(viewType)) {
      UIWeekView uiView = getChild(UIWeekView.class) ;
      if(uiView == null) uiView =  addChild(UIWeekView.class, null, null) ;
      uiView.isShowCustomView_ = false ;
      if(getRenderedChild() != null) uiView.setCurrentCalendar(((CalendarView)getRenderedChild()).getCurrentCalendar()) ;
      setRenderedChild(viewType) ;
    }
    else if (MONTH_VIEW.equals(viewType)) {
      UIMonthView uiView = getChild(UIMonthView.class) ;
      if(uiView == null) uiView =  addChild(UIMonthView.class, null, null) ;
      if(getRenderedChild() != null) uiView.setCurrentCalendar(((CalendarView)getRenderedChild()).getCurrentCalendar()) ;
      setRenderedChild(viewType) ;
    }
    else if (LIST_VIEW.equals(viewType)) {
      UIListContainer uiView = getChild(UIListContainer.class) ;
      boolean reloadListView = false;
      if(uiView == null) {
        uiView =  addChild(UIListContainer.class, null, null) ;
        reloadListView = true;
      }
      UIListView uiListView = uiView.getChild(UIListView.class) ;
      uiListView.setShowEventAndTask(false) ;
      uiListView.setCategoryId(null) ;
      uiListView.isShowEvent_ = true ;
      if(getRenderedChild() != null) uiView.setCurrentCalendar(((CalendarView)getRenderedChild()).getCurrentCalendar()) ;
      setRenderedChild(viewType) ;
      if (reloadListView) uiListView.refresh();
    } else if (WORKING_VIEW.equals(viewType)) {
      UIWeekView uiView = getChild(UIWeekView.class) ;
      if(uiView == null) uiView =  addChild(UIWeekView.class, null, null) ;
      uiView.isShowCustomView_ = true ;
      if(getRenderedChild() != null) uiView.setCurrentCalendar(((CalendarView)getRenderedChild()).getCurrentCalendar()) ;
      setRenderedChild(WEEK_VIEW) ;
    }
  }


  /**
   * @return the currentViewType_
   */
   public String getCurrentViewType() {
     return currentViewType_;
   }
   /**
    * @param currentViewType the currentViewType_ to set
    */
   public void setCurrentViewType_(String currentViewType) {
     currentViewType_ = currentViewType;
   }

  public void refresh() throws Exception {
    for (UIComponent comp : getChildren()) {
      if (comp.isRendered() && comp instanceof CalendarView){
        ((CalendarView)comp).refresh();
      }
    }
   }


   protected boolean isShowPane() {
     return getAncestorOfType(UICalendarWorkingContainer.class).getChild(UICalendarContainer.class).isRendered() ;
   }
   public UIComponent getRenderedChild() {
     for(UIComponent comp : getChildren()) {
       if(comp.isRendered()) return comp ;
     }
     return null ;
   }
   public void updateCategory() throws Exception{
     for(UIComponent comp : getChildren()) {
       if(comp instanceof CalendarView) {
         ((CalendarView)comp).update() ;
       }
     }
   }
   protected boolean isInSpace() {
     //return UICalendarPortlet.isInSpace() ;
     return getAncestorOfType(UICalendarPortlet.class).isInSpaceContext();
   }
   public void applySeting() throws Exception {
     for(UIComponent comp : getChildren()) {
       if((comp instanceof CalendarView)) ((CalendarView)comp).applySeting() ;  
     }
   }
}
