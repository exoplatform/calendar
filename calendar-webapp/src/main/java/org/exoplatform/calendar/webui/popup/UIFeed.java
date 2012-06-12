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

import java.util.List;

import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIFeed.gtmpl",
    events = {
      @EventConfig(listeners = UIFeed.CloseActionListener.class)
    }
)
public class UIFeed extends UIForm implements UIPopupComponent{
  @SuppressWarnings("unchecked")
  private List feeds_ = null;
  
  public UIFeed() {}
  
  @SuppressWarnings("unchecked")
  public void setFeeds(List feeds) { feeds_ = feeds ; }
  @SuppressWarnings("unchecked")
  public List getFeeds() {
    return feeds_ ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}  
  
  static  public class SelectActionListener extends EventListener<UIFeed> {
    public void execute(Event<UIFeed> event) throws Exception {
      UIFeed uiForm = event.getSource() ;
      
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      //event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');") ;
      calendarPortlet.cancelAction() ;      
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIFeed> {
    public void execute(Event<UIFeed> event) throws Exception {
      UIFeed uiForm = event.getSource() ;
      UIPopupAction popupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }  
}
