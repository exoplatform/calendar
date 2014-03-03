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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.RemoteCalendar;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UIFormColorPicker;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;


/**
 * Created by The eXo Platform SAS
 * Author : khiem.dohoang
 *          khiem.dohoang@exoplatform.com
 * Jan 5, 2011  
 */

@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/calendar/webui/UIPopup/UIRemoteCalendar.gtmpl",
                 events = {
                   @EventConfig(listeners = UIRemoteCalendar.BackActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIRemoteCalendar.SaveActionListener.class),
                   @EventConfig(listeners = UIRemoteCalendar.CancelActionListener.class, phase = Phase.DECODE)
                 }
             )
public class UIRemoteCalendar extends UIForm implements UIPopupComponent {
  private static final Log logger = ExoLogger.getLogger(UIRemoteCalendar.class);
  private static final String URL = "url".intern();
  private static final String NAME = "name".intern();
  private static final String DESCRIPTION = "description".intern();
  private static final String USE_AUTHENTICATION = "useAuthentication";
  private static final String USERNAME = "username".intern();
  private static final String PASSWORD = "password".intern();
  private static final String COLOR = "color".intern();
  private static final String AUTO_REFRESH = "autoRefresh".intern();
  private static final String FIELD_BEFORE_DATE_SELECTBOX = "beforeDate".intern();
  private static final String FIELD_AFTER_DATE_SELECTBOX = "afterDate".intern();
  protected static final String LAST_UPDATED = "lastUpdated".intern();
  
  private static Locale locale_ = null;
  private String remoteType;
  private boolean isAddNew_ = true; 
  private String calendarId_ = null;
  private String lastUpdated_ = null;
  private static RemoteCalendar remoteCalendar = new RemoteCalendar();
  
  public UIRemoteCalendar() throws Exception {
    UIFormStringInput remoteUrl = new UIFormStringInput(URL, URL, null);
    remoteUrl.addValidator(MandatoryValidator.class);
    addUIFormInput(remoteUrl);
    addUIFormInput(new UIFormStringInput(NAME, NAME, null).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class));
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null));
    addUIFormInput(new UICheckBoxInput(USE_AUTHENTICATION, USE_AUTHENTICATION, null));

    UIFormSelectBox beforeDate = new UIFormSelectBox(FIELD_BEFORE_DATE_SELECTBOX, FIELD_BEFORE_DATE_SELECTBOX, getOptionsSelectBox());
    beforeDate.setDefaultValue("0t");
    addUIFormInput(beforeDate);
    List<SelectItemOption<String>> ls = getOptionsSelectBox();
    ls.set(0, new SelectItemOption<String>(getLabel("Forever"), "0t"));
    UIFormSelectBox afterDate = new UIFormSelectBox(FIELD_AFTER_DATE_SELECTBOX, FIELD_AFTER_DATE_SELECTBOX, ls);
    afterDate.setDefaultValue("0t");
    addUIFormInput(afterDate);

    addUIFormInput(new UIFormStringInput(USERNAME, USERNAME, null));
    UIFormStringInput password = new UIFormStringInput(PASSWORD, PASSWORD, null);
    password.setType(UIFormStringInput.PASSWORD_TYPE);
    addUIFormInput(password);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    for (String s : Utils.SYNC_PERIOD) {
      options.add(new SelectItemOption<String>(s, s));
    }
    addUIFormInput(new UIFormSelectBox(AUTO_REFRESH, AUTO_REFRESH, options));  
    addUIFormInput(new UIFormColorPicker(COLOR, COLOR)); 
  }
  
  protected void setLocale() throws Exception {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    Locale locale = portalContext.getLocale();
    if (locale_ == null || !locale.getLanguage().equals(locale_.getLanguage())) {
      locale_ = locale;
      List<SelectItemOption<String>> ls = getOptionsSelectBox();
      UIFormSelectBox beforeDate = getUIFormSelectBox(FIELD_BEFORE_DATE_SELECTBOX);
      beforeDate.setOptions(ls);
      UIFormSelectBox afterDate = getUIFormSelectBox(FIELD_AFTER_DATE_SELECTBOX);
      afterDate.setOptions(ls);
    }
  }
  
  /*
   * previous : None, 1 week, 2 weeks, 1 month, 3 months, 6 months, 1 year
   * next : Forever, 1 week, 2 weeks, 1 month, 3 months, 6 months, 1 year
   */
  private List<SelectItemOption<String>> getOptionsSelectBox() throws Exception {
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(getLabel("None"), "0t"));
    ls.add(new SelectItemOption<String>("1 " + getLabel("Week"), "1w"));
    ls.add(new SelectItemOption<String>("2 " + getLabel("Weeks"), "3w"));
    ls.add(new SelectItemOption<String>("1 " + getLabel("Month"), "1m"));
    ls.add(new SelectItemOption<String>("2 " + getLabel("Months"), "2m"));
    ls.add(new SelectItemOption<String>("3 " + getLabel("Months"), "3m"));
    ls.add(new SelectItemOption<String>("6 " + getLabel("Months"), "6m"));
    ls.add(new SelectItemOption<String>("1 " + getLabel("Year"), "1y"));
    return ls;
  }
  
  public void init(String url, String remoteType) {
    isAddNew_ = true;
    this.remoteType = remoteType;
    setUrl(url);
    this.getUIStringInput(URL).setReadOnly(true);
    setSyncPeriod(Utils.SYNC_AUTO);
    setSelectColor(Calendar.COLORS[0]);
    setUseAuthentication(true);
    
    CalendarService calendarService = this.getApplicationComponent(CalendarService.class);
    try {
      RemoteCalendar rCalendar = calendarService.getRemoteCalendarService().getRemoteCalendar(url, remoteType, null, null);
      if (rCalendar != null) {
        setCalendarName(rCalendar.getCalendarName());
        setDescription(rCalendar.getDescription());
        remoteCalendar = rCalendar;
      }
    } catch (Exception e) {
      if (logger.isDebugEnabled()) logger.debug(String.format("Loading the remote calendar information from %s failed", url), e);
    }
    
  }
  
  public void init(Calendar calendar) throws Exception {
    if (calendar != null) {
      isAddNew_ = false;
      calendarId_ = calendar.getId();
    } else return;
    
    String username = CalendarUtils.getCurrentUser();
    CalendarService calService = CalendarUtils.getCalendarService();
    CalendarSetting calSettings = calService.getCalendarSetting(username);
    remoteCalendar = calService.getRemoteCalendar(username, calendarId_);
    this.remoteType = remoteCalendar.getType();
    setUrl(remoteCalendar.getRemoteUrl());
    this.getUIStringInput(URL).setReadOnly(false);
    setCalendarName(calService.getUserCalendar(username, calendarId_).getName());
    setDescription(calendar.getDescription());
    setSelectColor(calendar.getCalendarColor());
    setSyncPeriod(remoteCalendar.getSyncPeriod());
    setUseAuthentication(remoteCalendar.getUsername() != null);
    setRemoteUser(remoteCalendar.getRemoteUser());
    setRemotePassword(remoteCalendar.getRemotePassword());
    getUIFormSelectBox(FIELD_BEFORE_DATE_SELECTBOX).setValue(remoteCalendar.getBeforeDateSave()) ;
    getUIFormSelectBox(FIELD_AFTER_DATE_SELECTBOX).setValue(remoteCalendar.getAfterDateSave()) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(calSettings.getDateFormat() + " " + calSettings.getTimeFormat(), locale) ;
    df.setTimeZone(TimeZone.getTimeZone(calSettings.getTimeZone()));
    setLastUpdated(df.format(remoteCalendar.getLastUpdated().getTime()));
  }
  
  @Override
  public void activate() throws Exception {

  }

  @Override
  public void deActivate() throws Exception {

  }
  
  protected String getUrl() {
    return this.getUIStringInput(URL).getValue();
  }
  
  protected void setUrl(String url) {
    this.getUIStringInput(URL).setValue(url);
  }
  
  protected void setCalendarName(String name) {
    this.getUIStringInput(NAME).setValue(name);
  }
  
  protected String getCalendarName() {
    return this.getUIStringInput(NAME).getValue();
  }
  
  protected void setDescription(String description) {
    this.getUIFormTextAreaInput(DESCRIPTION).setValue(description);
  }
  
  protected String getDescription() {
    String s = this.getUIFormTextAreaInput(DESCRIPTION).getValue() ;
    return (Utils.isEmpty(s))?"":s;
  }
  
  protected String getSyncPeriod() {
    String s = this.getUIFormSelectBox(AUTO_REFRESH).getValue() ;
    return (Utils.isEmpty(s))?"":s;
  }
  
  protected void setSyncPeriod(String value) {
    this.getUIFormSelectBox(AUTO_REFRESH).setValue(value);
  }
  
  protected String getSelectColor() {
    return this.getChild(UIFormColorPicker.class).getValue();
  }
  
  protected void setSelectColor(String value) {
    this.getChild(UIFormColorPicker.class).setValue(value);
  }
  
  protected void setRemoteUser(String remoteUser) {
    this.getUIStringInput(USERNAME).setValue(remoteUser);
  }
  
  protected String getRemoteUser() {
    return this.getUIStringInput(USERNAME).getValue();
  }
  
  protected void setRemotePassword(String password) {
    this.getUIStringInput(PASSWORD).setValue(password);
  }
  
  protected String getRemotePassword() {
    return this.getUIStringInput(PASSWORD).getValue();
  }
  
  protected void setUseAuthentication(Boolean checked) {
    this.getUICheckBoxInput(USE_AUTHENTICATION).setChecked(checked);
  }
  
  protected Boolean getUseAuthentication() {
    return this.getUICheckBoxInput(USE_AUTHENTICATION).isChecked();
  }
  
  protected void setLastUpdated(String lastUpdated) {
    this.lastUpdated_ = lastUpdated;
  }
  
  protected String getLastUpdated() {
    return lastUpdated_;
  }
  
  public static class SaveActionListener extends EventListener<UIRemoteCalendar> {
    @Override
    public void execute(Event<UIRemoteCalendar> event) throws Exception {
      UIRemoteCalendar uiform = event.getSource();
      UICalendarPortlet calendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class);
      CalendarService calService = CalendarUtils.getCalendarService();
      remoteCalendar.setType(uiform.remoteType);
      remoteCalendar.setUsername(CalendarUtils.getCurrentUser());
      remoteCalendar.setRemoteUrl(uiform.getUrl());
      remoteCalendar.setCalendarName(uiform.getCalendarName());
      remoteCalendar.setDescription(uiform.getDescription());
      remoteCalendar.setSyncPeriod(uiform.getSyncPeriod());
      remoteCalendar.setBeforeDateSave(uiform.getUIFormSelectBox(FIELD_BEFORE_DATE_SELECTBOX).getValue());
      remoteCalendar.setAfterDateSave(uiform.getUIFormSelectBox(FIELD_AFTER_DATE_SELECTBOX).getValue());
      remoteCalendar.setCalendarColor(uiform.getSelectColor());
      remoteCalendar.setDescription(uiform.getDescription());
      Calendar eXoCalendar = null;
      try {       
        if (!uiform.getUseAuthentication()) {
          // check valid url
          if(!calService.isValidRemoteUrl(remoteCalendar.getRemoteUrl(), remoteCalendar.getType(), "", "")) {
            // pop-up error message: invalid ics url
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRemoteCalendar.msg.url-is-invalid", null, AbstractApplicationMessage.WARNING));
            return;
          }
        } else {
          remoteCalendar.setRemoteUser(uiform.getRemoteUser());
          remoteCalendar.setRemotePassword(uiform.getRemotePassword());
          if(CalendarUtils.isEmpty(remoteCalendar.getRemoteUser())) {
            // pop-up error message: require remote username
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRemoteCalendar.msg.remote-user-name-required", null, AbstractApplicationMessage.WARNING));
            return;
          }
          
          //check valid url
          if(!calService.isValidRemoteUrl(remoteCalendar.getRemoteUrl(), remoteCalendar.getType(), remoteCalendar.getRemoteUser(), remoteCalendar.getRemotePassword())) {
            // pop-up error message: invalid caldav url
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRemoteCalendar.msg.url-is-invalid-or-wrong-authentication", null, AbstractApplicationMessage.WARNING));
            return;
          }          
        }
      }
      catch (UnsupportedOperationException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRemoteCalendar.msg.remote-server-doesnt-support-caldav-access", null, AbstractApplicationMessage.WARNING));
        
        return;
      }
      catch (IOException e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRemoteCalendar.msg.cannot-connect-to-remote-server", null, AbstractApplicationMessage.WARNING));
        return;
      }
      catch (Exception e) {
        logger.warn("Exception occurs when connecting to remote server", e);
        return;
      }
      
      try {
        if (uiform.isAddNew_) {
          // access to remote calendar
          if(CalendarService.ICALENDAR.equals(remoteCalendar.getType())) {
            calService.importRemoteCalendarByJob(remoteCalendar);
          } else {
            eXoCalendar = calService.importRemoteCalendar(remoteCalendar);
          }
        } else {
          remoteCalendar.setCalendarId(uiform.calendarId_) ;
          // update remote calendar info
          eXoCalendar = calService.updateRemoteCalendarInfo(remoteCalendar);
          // refresh calendar
          eXoCalendar = calService.refreshRemoteCalendar(remoteCalendar.getUsername(), uiform.calendarId_);
        }
      }
      catch (Exception e) {
        logger.warn("Exception occurs when importing remote calendar", e);
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRemoteCalendar.msg.cant-import-remote-calendar", null, AbstractApplicationMessage.ERROR));
        return;
      }
      
      calendarPortlet.cancelAction() ;
      if(!CalendarService.ICALENDAR.equals(remoteCalendar.getType())) {
        UICalendarWorkingContainer uiWorkingContainer = calendarPortlet.getChild(UICalendarWorkingContainer.class) ;
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRemoteCalendar.msg-import-succesfully", null, AbstractApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;  
      }
    }
  }
  
  public static class BackActionListener extends EventListener<UIRemoteCalendar> {

    @Override
    public void execute(Event<UIRemoteCalendar> event) throws Exception {
      // back to UISubscribeForm
      UIRemoteCalendar uiform = event.getSource();
      UICalendarPortlet uiCalendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class);
      UIPopupAction uiPopupAction = uiCalendarPortlet.getChild(UIPopupAction.class);
      uiPopupAction.deActivate();
      UISubscribeForm uiSubscribe = uiPopupAction.activate(UISubscribeForm.class, 600);
      uiSubscribe.init(uiform.remoteType, uiform.getUrl());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
  
  public static class CancelActionListener extends EventListener<UIRemoteCalendar> {

    @Override
    public void execute(Event<UIRemoteCalendar> event) throws Exception {
      UIRemoteCalendar uiform = event.getSource();
      UICalendarPortlet calendarPortlet = uiform.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction();
    }
  }

}
