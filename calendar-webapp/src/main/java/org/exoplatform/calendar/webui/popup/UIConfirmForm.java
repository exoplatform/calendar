/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 20, 2009  
 */
@ComponentConfig (
                  lifecycle  = UIFormLifecycle.class,
                  template =  "app:/templates/calendar/webui/UIPopup/UIConfirmForm.gtmpl"
)

public class UIConfirmForm extends UIForm implements UIPopupComponent{

  public static String CONFIRM_TRUE = "true".intern();
  public static String CONFIRM_FALSE = "false".intern();
  private String config_id = "";
  private String confirmMessage;

  public UIConfirmForm() {}

  public void setConfirmMessage(String confirmMessage) {
    this.confirmMessage = confirmMessage;
  }

  @Override
  public String event(String name) throws Exception {
    StringBuilder b = new StringBuilder() ;
    b.append("javascript:eXo.webui.UIForm.submitForm('").append(getConfig_id()).append("','");
    b.append(name).append("',true)");
    return b.toString() ;
  } 

  public void setConfig_id(String config_id) {
    this.config_id = config_id;
  }

  public String getConfig_id() {
    return config_id;
  }

  
  
  @Override
  public void activate() throws Exception {
    
  }


  @Override
  public void deActivate() throws Exception {
    
  }

}
