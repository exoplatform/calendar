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
package org.exoplatform.calendar.service;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class EventCategory extends AbstractBean {
  private static final Log log = ExoLogger.getLogger(EventCategory.class);

  private String  name;

  private boolean isDataInit = false;

  public EventCategory() {
    setId("eventCategory" + IdGenerator.generate());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getLocalizedName() {
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    
    for (int i = 0; i < NewUserListener.defaultEventCategoryIds.length; i++) {
      if (getId().equals(NewUserListener.defaultEventCategoryIds[i])
          && getName().equals(NewUserListener.defaultEventCategoryNames[i])) {
        try {
          if (res != null) {
            return res.getString("UICalendarView.label." + getId());            
          }
        } catch (MissingResourceException e) {
            log.debug("Can not find resource bundle for key: UICalendarView.label." + getId());
        }
      }
    }
    return getName();
  }

  public void setDataInit(boolean isDataInit) {
    this.isDataInit = isDataInit;
  }

  public boolean isDataInit() {
    return isDataInit;
  }
}
