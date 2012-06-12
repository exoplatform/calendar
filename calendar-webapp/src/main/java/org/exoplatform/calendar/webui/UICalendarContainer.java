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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UICalendarContainer extends UIContainer  {
  public UICalendarContainer() throws Exception {
    //addChild(UISearchForm.class, null, null) ;
    addChild(UIMiniCalendar.class, null, null) ;
    addChild(UICalendars.class, null, null) ;    
  }
  
  @Override
  public void renderChildren(WebuiRequestContext context) throws Exception {
    super.renderChildren(context);
    // load javascript block for updating the component's visibility. 
    context.getJavascriptManager().addJavascript("eXo.calendar.CalendarLayout.updateCalendarContainerLayout();");
  }



  public void applySeting() throws Exception {
    for(UIComponent comp : getChildren()) {
      if((comp instanceof CalendarView)) ((CalendarView)comp).applySeting() ;  
    }
  }
  
}
