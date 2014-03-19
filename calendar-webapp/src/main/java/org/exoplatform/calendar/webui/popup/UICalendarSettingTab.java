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
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Jan 07, 2008  
 */

@ComponentConfig(
                 template = "app:/templates/calendar/webui/UIPopup/UICalendarSettingTab.gtmpl"
    ) 
public class UICalendarSettingTab extends UIFormInputWithActions {
  final public static String VIEW_TYPE = "viewType" ;
  final public static String TIME_INTERVAL = "timeInterval" ;
  final public static String WEEK_START_ON = "weekStartOn" ;
  final public static String DATE_FORMAT = "dateFormat" ;
  final public static String TIME_FORMAT = "timeFormat" ;
  final public static String TIMEZONE = "timeZone" ;
  final public static String ISSHOWWORKINGTIME = "showWorkingTime" ;
  final public static String WORKINGTIME_BEGIN = "beginTime" ;
  final public static String WORKINGTIME_END = "endTime" ;
  final public static String BASE_URL = "baseURL" ;
  final public static String FIELD_SEND = "send" ;

  private Map<String, List<ActionData>> actionField_  = new HashMap<String, List<ActionData>>() ;

  public UICalendarSettingTab(String compId) throws Exception {
    super(compId);
    setComponentConfig(getClass(), null) ;
    List<SelectItemOption<String>> viewTypes = new ArrayList<SelectItemOption<String>>() ;
    viewTypes.add(new SelectItemOption<String>(CalendarSetting.DAY_VIEW, CalendarSetting.DAY_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>(CalendarSetting.WEEK_VIEW, CalendarSetting.WEEK_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>(CalendarSetting.MONTH_VIEW, CalendarSetting.MONTH_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>(CalendarSetting.LIST_VIEW, CalendarSetting.LIST_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>(CalendarSetting.WORKING_VIEW, CalendarSetting.WORKING_VIEW)) ;
    
    addUIFormInput(new UIFormSelectBox(VIEW_TYPE, VIEW_TYPE, viewTypes)) ;

    List<SelectItemOption<String>> weekStartOn = new ArrayList<SelectItemOption<String>>() ;
    DateFormatSymbols dfs = new DateFormatSymbols() ;  ;
    for(int id =1 ;id<  dfs.getWeekdays().length; id++) {
      weekStartOn.add(new SelectItemOption<String>(String.valueOf(id)+"-wst", String.valueOf(id)+"-wst")) ;
    }

    addUIFormInput(new UIFormSelectBox(WEEK_START_ON, WEEK_START_ON, weekStartOn)) ;

    List<SelectItemOption<String>> dateFormat = new ArrayList<SelectItemOption<String>>() ;
    dateFormat.add(new SelectItemOption<String>(CalendarUtils.DATEFORMAT1, CalendarUtils.DATEFORMAT1)) ;
    dateFormat.add(new SelectItemOption<String>(CalendarUtils.DATEFORMAT2, CalendarUtils.DATEFORMAT2)) ;
    dateFormat.add(new SelectItemOption<String>(CalendarUtils.DATEFORMAT3, CalendarUtils.DATEFORMAT3)) ;
    dateFormat.add(new SelectItemOption<String>(CalendarUtils.DATEFORMAT4, CalendarUtils.DATEFORMAT4)) ;
    addUIFormInput(new UIFormSelectBox(DATE_FORMAT, DATE_FORMAT, dateFormat)) ;

    List<SelectItemOption<String>> timeFormat = new ArrayList<SelectItemOption<String>>() ;
    timeFormat.add(new SelectItemOption<String>(CalendarUtils.TWELVE_HOURS, CalendarUtils.TWELVE_HOURS)) ;
    timeFormat.add(new SelectItemOption<String>(CalendarUtils.TWENTY_FOUR_HOURS, CalendarUtils.TWENTY_FOUR_HOURS)) ;
    addUIFormInput(new UIFormSelectBox(TIME_FORMAT, TIME_FORMAT, timeFormat)) ;
    addUIFormInput(new UIFormSelectBox(TIMEZONE, TIMEZONE, getTimeZones(null))) ;
    addUIFormInput(new UICheckBoxInput(ISSHOWWORKINGTIME, ISSHOWWORKINGTIME, false)) ;
    List<SelectItemOption<String>> startTimes = new ArrayList<SelectItemOption<String>>() ;
    List<SelectItemOption<String>> endTimes = CalendarUtils.getTimesSelectBoxOptions(CalendarUtils.TIMEFORMAT, 30) ;
    addUIFormInput(new UIFormSelectBox(WORKINGTIME_BEGIN, WORKINGTIME_BEGIN, startTimes)) ;
    addUIFormInput(new UIFormSelectBox(WORKINGTIME_END, WORKINGTIME_END, endTimes)) ;
    addUIFormInput(new UIFormRadioBoxInput(CalendarUtils.FIELD_SEND,CalendarUtils.FIELD_SEND,CalendarUtils.getSendValue(null)));
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  public void setActionField(String fieldName, List<ActionData> actions){
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
  protected String getViewType() {
    return getUIFormSelectBox(VIEW_TYPE).getValue() ;
  }
  protected void setViewType(String value) {
    getUIFormSelectBox(VIEW_TYPE).setValue(value) ;
  }

  protected String getTimeInterval() {
    return String.valueOf(CalendarSetting.DEFAULT_TIME_INTERVAL);
  }

  protected String getWeekStartOn() {
    String value = getUIFormSelectBox(WEEK_START_ON).getValue()  ;
    return value.substring(0, value.lastIndexOf("-wst")) ;
  }
  protected void setWeekStartOn(String value) {
    value = value + "-wst" ;
    getUIFormSelectBox(WEEK_START_ON).setValue(value) ;
  }
  protected String getDateFormat() {
    String value = getUIFormSelectBox(DATE_FORMAT).getValue() ;
    return CalendarUtils.FORMATPATTERNS[Integer.parseInt(value.substring(value.length() -1))] ;
  }
  protected void setDateFormat(String value) {
    if(!CalendarUtils.isEmpty(value)) {
      for(int i = 0; i < CalendarUtils.FORMATPATTERNS.length ; i++ ) {
        if(value.equalsIgnoreCase(CalendarUtils.FORMATPATTERNS[i])) 
          getUIFormSelectBox(DATE_FORMAT).setValue(CalendarUtils.DATEFORMATS[i]) ;
      }
    }
  }
  protected String getTimeFormat() {
    if(CalendarUtils.TWELVE_HOURS.equals(getUIFormSelectBox(TIME_FORMAT).getValue()))
      return CalendarUtils.TIMEFORMATPATTERNS[0] ;
    else return CalendarUtils.TIMEFORMATPATTERNS[1] ;
  }
  protected void setTimeFormat(String value) {
    if(CalendarUtils.TIMEFORMATPATTERNS[0].endsWith(value)) getUIFormSelectBox(TIME_FORMAT).setValue(CalendarUtils.TWELVE_HOURS) ;
    else getUIFormSelectBox(TIME_FORMAT).setValue(CalendarUtils.TWENTY_FOUR_HOURS) ;
  }

  protected String getTimeZone() {
    return getUIFormSelectBox(TIMEZONE).getValue() ;
  }
  protected void setTimeZone(String value) {
    getUIFormSelectBox(TIMEZONE).setValue(value) ;
  }
  protected boolean getShowWorkingTimes() {
    return getUICheckBoxInput(ISSHOWWORKINGTIME).isChecked() ;
  }
  protected void setShowWorkingTimes(boolean value) {
    getUICheckBoxInput(ISSHOWWORKINGTIME).setChecked(value) ;
  }
  protected String getWorkingBegin() throws Exception {
    return getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
  }
  protected String getWorkingBegin(Locale locale) throws Exception {
    java.util.Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(getTimeFormat()) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    dateFormat.setCalendar(cal) ;
    timeFormat.setCalendar(cal) ;
    dateTimeFormat.setCalendar(cal) ;
    String value = getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return timeFormat.format(cal.getTime()) ;
  }
  protected Date getWorkingBeginTime() throws Exception {
    java.util.Calendar cal = CalendarUtils.getBeginDay(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return  cal.getTime()  ;
  }
  protected Date getWorkingBeginTime(Locale locale) throws Exception {
    java.util.Calendar cal = CalendarUtils.getBeginDay(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return  cal.getTime()  ;
  }
  protected void setWorkingBegin(String value, String format) throws Exception {
    getUIFormSelectBox(WORKINGTIME_BEGIN).setValue(value) ;
  }
  protected String getWorkingEnd() throws Exception{
    return getUIFormSelectBox(WORKINGTIME_END).getValue() ;
  }

  protected Date getWorkingEndTime() throws Exception{
    java.util.Calendar cal = CalendarUtils.getBeginDay(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    dateFormat.setCalendar(cal) ;
    dateTimeFormat.setCalendar(cal) ;
    String value = getUIFormSelectBox(WORKINGTIME_END).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return  cal.getTime();
  }
  protected void setWorkingEnd(String value, String format) throws Exception {
    getUIFormSelectBox(WORKINGTIME_END).setValue(value) ;
  }
  public List<SelectItemOption<String>> getTimeZones(String locale) {
    return CalendarUtils.getTimeZoneSelectBoxOptions(TimeZone.getAvailableIDs()) ;
  }
  private List<SelectItemOption<String>> getLocales() {
    return CalendarUtils.getLocaleSelectBoxOptions(java.util.Calendar.getAvailableLocales()) ;
  }
  public String getSendOption() {
    return getChild(UIFormRadioBoxInput.class).getValue() ;
  }
  public void setSendOption(String value) {
    getChild(UIFormRadioBoxInput.class).setValue(value) ;
  }
}
