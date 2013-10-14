/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.URLValidator;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 4, 2011  
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "system:/groovy/webui/form/UIForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UISubscribeForm.NextActionListener.class),
                   @EventConfig(listeners = UISubscribeForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
             )
public class UISubscribeForm extends UIForm implements UIPopupComponent {
  
  private final static String URL = "url".intern();
  private final static String TYPE = "type".intern();
  
  public UISubscribeForm() throws Exception {
    List<SelectItemOption<String>> types = new ArrayList<SelectItemOption<String>>();
    types.add(new SelectItemOption<String>(CalendarService.ICALENDAR, CalendarService.ICALENDAR));
    types.add(new SelectItemOption<String>(CalendarService.CALDAV, CalendarService.CALDAV));
    addUIFormInput(new UIFormRadioBoxInput(TYPE, TYPE, types));
    addUIFormInput(new UIFormStringInput(URL, URL, null).addValidator(MandatoryValidator.class).addValidator(URLValidator.class));
  }

  @Override
  public void activate() throws Exception {

  }

  @Override
  public void deActivate() throws Exception {

  }
  
  public void init(String type, String remoteUrl) {
    setType(type);
    setUrl(remoteUrl);
  }
  
  protected void setType(String type) {
    this.getChild(UIFormRadioBoxInput.class).setValue(type);
  }
  
  protected String getType() {
    return this.getChild(UIFormRadioBoxInput.class).getValue();
  }
  
  protected void setUrl(String url) {
    this.getUIStringInput(URL).setValue(url);
  }
  
  protected String getUrl() {
    return this.getUIStringInput(URL).getValue();
  }
  
  @Override
  public String getLabel(String id) throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;     
    String label = getId() + ".label." + id;
    try {
      return res.getString(label);      
    } catch (MissingResourceException e) {
      return id ;
    }
  } 
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);
    //autofocus the input
    RequireJS requireJS = context.getJavascriptManager().getRequireJS();
    requireJS.require("PORTLET/calendar/CalendarPortlet","cal");
    requireJS.addScripts("cal.UICalendarPortlet.autoFocusFirstInput('" + getId() + "');");
    requireJS.addScripts("cal.UICalendarPortlet.resizeSubscribeForm('" + getId() + "');");
  }
  public static class CancelActionListener extends EventListener<UISubscribeForm> {
    @Override
    public void execute(Event<UISubscribeForm> event) throws Exception {
        UISubscribeForm uiform = event.getSource();
      UICalendarPortlet calendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction();
    }

  }

  public static class NextActionListener extends EventListener<UISubscribeForm> {
    @Override
    public void execute(Event<UISubscribeForm> event) throws Exception {
      UISubscribeForm uiform = event.getSource();
      UICalendarPortlet calendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class);
      CalendarService calService = CalendarUtils.getCalendarService();
      String username = CalendarUtils.getCurrentUser();
      
      String url = uiform.getUIStringInput(URL).getValue();
      String type = uiform.getChild(UIFormRadioBoxInput.class).getValue();
      
      
      if (CalendarUtils.isEmpty(type)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISubscribeForm.msg.remote-type-is-not-null", null, AbstractApplicationMessage.WARNING));
        return;
      }
      
      // check duplicate remote calendar
      if (calService.getRemoteCalendar(username, url, type) != null) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISubscribeForm.msg.this-remote-calendar-already-exists",
                                                null,
                                                AbstractApplicationMessage.WARNING));
        return;
      }
      
      UIPopupAction uiPopupAction = calendarPortlet.getChild(UIPopupAction.class);
      uiPopupAction.deActivate();
      UIRemoteCalendar uiRemoteCalendar = uiPopupAction.activate(UIRemoteCalendar.class, 600);
      uiRemoteCalendar.init(url, type);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);      
    }
  }

}
