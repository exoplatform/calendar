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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIEventPreview.gtmpl",
    events = {
      @EventConfig(listeners = UIEventPrintPreview.SaveActionListener.class),
      @EventConfig(listeners = UIEventPrintPreview.CancelActionListener.class)
    }
)
public class UIEventPrintPreview extends UIComponent {
  
  public UIEventPrintPreview() {
  }
  
  static  public class SaveActionListener extends EventListener<UIEventPrintPreview> {
    public void execute(Event<UIEventPrintPreview> event) throws Exception {
    }
  }
  static  public class CancelActionListener extends EventListener<UIEventPrintPreview> {
    public void execute(Event<UIEventPrintPreview> event) throws Exception {
    }
  }
}
