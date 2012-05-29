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
import java.util.Calendar;

import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    //lifecycle = UIContainerLifecycle.class,
    template =  "app:/templates/calendar/webui/UIListContainer.gtmpl"
)
public class UIListContainer extends UIContainer implements CalendarView {
  public UIListContainer() throws Exception {
    addChild(UIListView.class, null, null) ;
    addChild(UIPreview.class, null, null) ;    
  }

  public void refresh() throws Exception {
    UIListView list = getChild(UIListView.class) ;
    list.refresh() ;
    UIPreview view = getChild(UIPreview.class) ;
    view.refresh() ;
  }
  public void update() throws Exception {
    UIListView list = getChild(UIListView.class) ;
    list.update() ;
  }

  public void setCurrentCalendar(Calendar value) {
    UIListView list = getChild(UIListView.class) ;
    list.setCurrentCalendar(value) ;
  }

  public void applySeting() throws Exception {
    getChild(UIListView.class).applySeting() ;
    getChild(UIPreview.class).applySeting() ;
  }

  public String getLastUpdatedEventId() {
    return getChild(UIListView.class).getLastUpdatedEventId();
  }

  public void setLastUpdatedEventId(String eventId) {
    getChild(UIListView.class).setLastUpdatedEventId(eventId) ;
  }

  public boolean isDisplaySearchResult() {return getChild(UIListView.class).isDisplaySearchResult() ;}
  public void setDisplaySearchResult(boolean show) {getChild(UIListView.class).setDisplaySearchResult(show) ;}

  public Calendar getCurrentCalendar() {
    return  getChild(UIListView.class).getCurrentCalendar() ;
  }

  public void setCalendarSetting(CalendarSetting calendarSetting) {
    getChild(UIListView.class).setCalendarSetting(calendarSetting) ;
    getChild(UIPreview.class).setCalendarSetting(calendarSetting) ;
  }
  public void setSelectedCategory(String categoryId) throws Exception {
    getChild(UIListView.class).setCategoryId(categoryId);
    getChild(UIListView.class).refresh() ;
  }
}
