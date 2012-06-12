/*
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
 */
package org.exoplatform.cs.common.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 16, 2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIPopupActionContainer extends UIContainer implements UIPopupComponent {
  public UIPopupActionContainer() throws Exception {
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, "UIChildPopup");
    uiPopupAction.getChild(UIPopupWindow.class).setId("UIChildPopupWindow") ;
  }
  public void activate() throws Exception {
    
  }

  public void deActivate() throws Exception {
    
  }
  public UIComponent setId(String id) {
    super.setId(id) ;
    UIPopupAction uiPopupAction = getChild(UIPopupAction.class) ;
    uiPopupAction.setId("UIChildPopup" + id);
    uiPopupAction.getChild(UIPopupWindow.class).setId("UIChildPopupWindow" + id) ;
    return this ;
  }

}
