/*
 * Copyright (C) 2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.calendar.webui;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.calendar.util.CalendarUtils;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = { @EventConfig(listeners = UICalendarPortletEditMode.SaveActionListener.class) })
@Serialized
public class UICalendarPortletEditMode extends UIForm {
    private static final String SAVE_ACTION = "Save";

    public UICalendarPortletEditMode() {
        String limit = String.valueOf(CalendarUtils.getLimitUploadSize());
        addUIFormInput(new UIFormStringInput(CalendarUtils.UPLOAD_LIMIT, CalendarUtils.UPLOAD_LIMIT, limit));
        setActions(new String[] {SAVE_ACTION});
    }

    public static class SaveActionListener extends EventListener<UICalendarPortletEditMode> {
        @Override
        public void execute(Event<UICalendarPortletEditMode> event) throws Exception {
            UIPortalApplication portalApp = Util.getUIPortalApplication();
            UICalendarPortletEditMode uiForm = event.getSource();
            UIFormStringInput limitInput = uiForm.getUIStringInput(CalendarUtils.UPLOAD_LIMIT); 
            String value = limitInput.getValue();
            
            int limit = 0;
            try {
              limit = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
              limitInput.setValue(String.valueOf(CalendarUtils.getLimitUploadSize()));

              ApplicationMessage msg = new ApplicationMessage("UICalendarPortletEditMode.msg.invalid_upload_limit", null, ApplicationMessage.WARNING);
              portalApp.addMessage(msg);
              return;
            }
            
            PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
            PortletPreferences pref = pcontext.getRequest().getPreferences();
            pref.setValue(CalendarUtils.UPLOAD_LIMIT, String.valueOf(limit));
            pref.store();
                        
            if (portalApp.getModeState() == UIPortalApplication.NORMAL_MODE)
                pcontext.setApplicationMode(PortletMode.VIEW);
        }
    }
}
