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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.ext.UIFormComboBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */
@ComponentConfig(template = "app:/templates/calendar/webui/UIPopup/UIEventAttenderTab.gtmpl")
public class UIEventAttenderTab extends UIFormInputWithActions {
  final public static String FIELD_FROM_TIME = "timeFrom".intern() ;
  final public static String FIELD_TO_TIME = "timeTo".intern();
  final public static String FIELD_CHECK_TIME = "checkTime".intern();

  final public static String FIELD_DATEALL = "dateAll".intern();
  final public static String FIELD_CURRENTATTENDER = "currentAttender".intern() ;
  protected Map<String, String> parMap_ = new HashMap<String, String>() ;
  public Calendar calendar_ ;

  private static final Log LOG = ExoLogger.getExoLogger(UIEventAttenderTab.class);

  public UIEventAttenderTab(String arg0) {
    super(arg0);
    setComponentConfig(getClass(), null) ;
    calendar_ = CalendarUtils.getInstanceOfCurrentCalendar() ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormComboBox(UIEventAttenderTab.FIELD_FROM_TIME, UIEventAttenderTab.FIELD_FROM_TIME, options)) ;
    addUIFormInput(new UIFormComboBox(UIEventAttenderTab.FIELD_TO_TIME, UIEventAttenderTab.FIELD_TO_TIME, options)) ;

    addUIFormInput(new UICheckBoxInput(FIELD_DATEALL, FIELD_DATEALL, false)) ;
    UICheckBoxInput checkFreeInput = new UICheckBoxInput(FIELD_CHECK_TIME, FIELD_CHECK_TIME, false) ;
    checkFreeInput.setOnChange("OnChange") ;
    checkFreeInput.setChecked(true);
    addUIFormInput(checkFreeInput) ;
  }
  protected UIFormComboBox getUIFormComboBox(String id) {
    return findComponentById(id) ;
  }

  protected void updateParticipants(String values) throws Exception{
    OrganizationService orgService = getApplicationComponent(OrganizationService.class) ;
    parMap_.clear() ;
    Map<String, String> tmpMap = new HashMap<String, String>() ;
    List<String> newPars = new ArrayList<String>() ;
    if(!CalendarUtils.isEmpty(values)) {
      for(String par : values.split(CalendarUtils.BREAK_LINE)) {
        if(orgService.getUserHandler().findUserByName(par) != null)  {
          String vl = tmpMap.get(par) ;
          parMap_.put(par.trim(), vl) ;
          if(vl == null) newPars.add(par.trim()) ;
        }
      }
    }
    if(newPars.size() > 0) {
      EventQuery eventQuery = new EventQuery() ;
      eventQuery.setFromDate(CalendarUtils.getBeginDay(calendar_)) ;
      eventQuery.setToDate(CalendarUtils.getEndDay(calendar_)) ;
      eventQuery.setParticipants(newPars.toArray(new String[]{})) ;
      eventQuery.setNodeType("exo:calendarPublicEvent") ;
      Map<String, String> parsMap = 
          CalendarUtils.getCalendarService().checkFreeBusy(eventQuery) ;
      parMap_.putAll(parsMap) ;
    }


  }

  public boolean isCheckFreeTime() {
    return getUICheckBoxInput(FIELD_CHECK_TIME).isChecked() ;
  }
  protected Map<String, String> getMap() throws Exception{ 
    for(String id : parMap_.keySet()) {
      if(getCalUICheckBoxInput(id) == null) {
        org.exoplatform.calendar.webui.popup.UICheckBoxInput input = new org.exoplatform.calendar.webui.popup.UICheckBoxInput(id, id, false);
        input.setLabel(getFullname(id));
        addUIFormInput(input) ;
      }
    }
    return parMap_ ;
  }

  protected org.exoplatform.calendar.webui.popup.UICheckBoxInput getCalUICheckBoxInput(String name) {
    return (org.exoplatform.calendar.webui.popup.UICheckBoxInput) findComponentById(name);
  }

  public static String getFullname(String username) throws Exception {
    User u = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username);
    String fullName = u.getDisplayName();
    if(fullName == null) fullName = u.getFirstName();
    if (u.getLastName() != null && fullName != null) {
      fullName = fullName + " " + u.getLastName();
    }
    if (fullName == null) fullName = u.getUserName();
    return fullName;
  }

  private DateFormat getSimpleFormatDate() throws Exception {
    CalendarSetting calSetting = CalendarUtils.getCurrentUserCalendarSetting() ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    return new SimpleDateFormat(calSetting.getDateFormat(),context.getParentAppRequestContext().getLocale());
  }

  protected String[] getParticipants() { return parMap_.keySet().toArray(new String[]{}) ; } 

  protected String getDateValue() throws Exception  {
    DateFormat dateFormat = getSimpleFormatDate();
    dateFormat.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    return dateFormat.format(calendar_.getTime()) ;
  }
  protected void moveNextDay() throws Exception{
    calendar_.add(Calendar.DATE, 1) ;
    StringBuilder values = new StringBuilder(); 
    for(String par : parMap_.keySet()) {
      if(values != null && values.length() > 0) values.append(CalendarUtils.BREAK_LINE) ;
      values.append(par) ;      
    }
    parMap_.clear() ;
    updateParticipants(values.toString()) ;
  }
  protected void movePreviousDay() throws Exception{
    calendar_.add(Calendar.DATE, -1) ;
    StringBuilder values = new StringBuilder(); 
    for(String par : parMap_.keySet()) {
      if(values != null && values.length() > 0) values.append(CalendarUtils.BREAK_LINE) ;
      values.append(par) ;      
    }
    parMap_.clear() ;
    updateParticipants(values.toString()) ;
  }

  protected UIForm getParentFrom() {
    return getAncestorOfType(UIForm.class) ;
  }
  protected String getFormName() { 
    UIForm uiForm = getAncestorOfType(UIForm.class);
    return uiForm.getId() ; 
  }

  protected String getFromFieldValue() {
    return getUIFormComboBox(FIELD_FROM_TIME).getValue() ;
  }
  protected void setEventFromDate(Date date, String timeFormat) {
    UIFormComboBox timeField = getChildById(FIELD_FROM_TIME) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    timeField.setValue(df.format(date)) ;
    calendar_.setTime(date) ;
  }
  protected boolean getEventAllDate() {
    return false;
  }
  protected void setEventToDate(Date date, String timeFormat) {
    UIFormComboBox timeField = getChildById(FIELD_TO_TIME) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    timeField.setValue(df.format(date)) ;
  }  

  protected boolean isAllDateFieldChecked() {
    return getUICheckBoxInput(FIELD_DATEALL).isChecked() ;
  }
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }

  public String getUserTimeZone() throws Exception {
    String timeZone = CalendarUtils.getCalendarService().getCalendarSetting(CalendarUtils.getCurrentUser()).getTimeZone() ;
    TimeZone tz = TimeZone.getTimeZone(timeZone) ;
    int rawOffset = tz.getRawOffset()  ;
    // check if the time zone uses daylight saving time and the date selected is in daylight saving period
    if(tz.useDaylightTime() && !tz.inDaylightTime(calendar_.getTime())) {
      return String.valueOf(0 - rawOffset /60000);
    } else {
      return String.valueOf(0 - (rawOffset /60000 + tz.getDSTSavings()/60000)) ;  
    }
  }

  public String getServerTimeZone() {
    return CalendarUtils.getTimeZone(TimeZone.getDefault().getID()) ;
  }
}
