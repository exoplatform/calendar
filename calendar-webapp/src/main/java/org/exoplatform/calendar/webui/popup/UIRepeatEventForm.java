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

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
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
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormHiddenInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 2, 2011  
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/calendar/webui/UIPopup/UIRepeatEventForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UIRepeatEventForm.SaveActionListener.class),
                   @EventConfig(listeners = UIRepeatEventForm.CancelActionListener.class)
                 }
)


public class UIRepeatEventForm extends UIForm implements UIPopupComponent {
  private static final Log log = ExoLogger.getExoLogger(UIRepeatEventForm.class);

  public final static String FIELD_REPEAT_TYPE = "repeatType";
  public final static String FIELD_INTERVAL = "interval";
  public final static String FIELD_WEEKLY_BYDAY = "weeklyByDay";
  public final static String FIELD_MONTHLY_TYPE = "monthlyType";
  public final static String FIELD_END_REPEAT = "endRepeat";
  public final static String FIELD_END_BYDATE = "endDate";
  public final static String FIELD_END_AFTER = "endAfterNumber";
  public final static String OCCURRENCES = "occurrences";
  
  public final static String RP_END_BYDATE = "endByDate";
  public final static String RP_END_AFTER = "endAfter";
  public final static String RP_END_NEVER = "neverEnd";
  
  final public static String RP_MONTHLY_BYDAY = "monthlyByDay";
  final public static String RP_MONTHLY_BYMONTHDAY = "monthlyByMonthDay";
  
  private Date startDate;
    
  public UIRepeatEventForm() throws Exception {
   addUIFormInput(new UIFormSelectBox(FIELD_REPEAT_TYPE, FIELD_REPEAT_TYPE, getRepeatTypeOptions()));
   addUIFormInput(new UIFormSelectBox(FIELD_INTERVAL, FIELD_INTERVAL, getIntervalOptions()));
   
   List<SelectItemOption<String>> monthlyTypes = new ArrayList<SelectItemOption<String>>();
   monthlyTypes.add(new SelectItemOption<String>(RP_MONTHLY_BYMONTHDAY, RP_MONTHLY_BYMONTHDAY));
   monthlyTypes.add(new SelectItemOption<String>(RP_MONTHLY_BYDAY, RP_MONTHLY_BYDAY));
   UIFormRadioBoxInput monthlyType = new UIFormRadioBoxInput(FIELD_MONTHLY_TYPE, FIELD_MONTHLY_TYPE, monthlyTypes);
   addUIFormInput(monthlyType);
   
   WebuiRequestContext context = RequestContext.getCurrentInstance() ;
   Locale locale = context.getParentAppRequestContext().getLocale() ;
   DateFormatSymbols symbols = new DateFormatSymbols(locale);
   String[] dayNames = symbols.getWeekdays();
   UICheckBoxInput checkbox;
   // weekly by day
   for (int i = 0; i < 7; i++) {
     checkbox = new UICheckBoxInput(CalendarEvent.RP_WEEKLY_BYDAY[i], CalendarEvent.RP_WEEKLY_BYDAY[i], false);
     int dayOfWeek = convertToDayOfWeek(CalendarEvent.RP_WEEKLY_BYDAY[i]);
     checkbox.setLabel(dayNames[dayOfWeek].substring(0, 2).toUpperCase());
     addUIFormInput(checkbox);
   }
      
   addUIFormInput(new UIFormStringInput(FIELD_END_AFTER, FIELD_END_AFTER, null));
   addUIFormInput(new UIFormDateTimePicker(FIELD_END_BYDATE, FIELD_END_BYDATE, new Date(), false));
   
   // hidden field to save the end option
   addUIFormInput(new UIFormHiddenInput(FIELD_END_REPEAT, FIELD_END_REPEAT));
   
  }
  
  public void init(CalendarEvent event) throws Exception {
    try {
      if (event == null) {
        setRepeatType(CalendarEvent.RP_DAILY);
        setInterval("1");
        setEndType(RP_END_NEVER);
        
        UIPopupContainer uiContainer = this.getAncestorOfType(UIPopupContainer.class) ;
        UIEventForm uiEventForm = uiContainer.getChild(UIEventForm.class);
        CalendarSetting calSetting = CalendarUtils.getCurrentUserCalendarSetting();
        
        startDate = uiEventForm.getEventFromDate(calSetting.getDateFormat(), calSetting.getTimeFormat());
        if (startDate == null) startDate = CalendarUtils.getInstanceOfCurrentCalendar().getTime();
        Date endDate =  uiEventForm.getEventToDate(calSetting.getDateFormat(), calSetting.getTimeFormat());

        java.util.Calendar start = CalendarUtils.getInstanceOfCurrentCalendar();

        int dayOfWeek = start.get(java.util.Calendar.DAY_OF_WEEK);
        setWeeklyByDay(convertToDayOfWeek(dayOfWeek));

        setMonthlyType(RP_MONTHLY_BYMONTHDAY);
        setEndAfter("5");

        start.setTime(endDate);
        //java.util.Calendar until = (java.util.Calendar)endDate.clone();
        start.add(java.util.Calendar.DATE, 5);
        
        setEndDate(start.getTime(), calSetting.getDateFormat());
        return;
      }
      UIPopupContainer uiContainer = this.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiContainer.getChild(UIEventForm.class);
      CalendarSetting calSetting = CalendarUtils.getCurrentUserCalendarSetting();
      startDate = uiEventForm.getEventFromDate(calSetting.getDateFormat(), calSetting.getTimeFormat());
      if (startDate == null) startDate = event.getFromDateTime();
      String repeatType = event.getRepeatType();
      if (repeatType != null && !repeatType.equals(CalendarEvent.RP_NOREPEAT)) {
        setRepeatType(repeatType);
      } else {
        setRepeatType(CalendarEvent.RP_DAILY);
      }
      
      long interval = event.getRepeatInterval();
      if (interval > 0) setInterval(String.valueOf(interval));
      else setInterval("1");
      
      if (CalendarEvent.RP_WEEKLY.equals(repeatType)) {
        String[] weeklyByDay = event.getRepeatByDay();
        if (weeklyByDay != null && weeklyByDay.length > 0) {
          for (String s : weeklyByDay) {
            setWeeklyByDay(s);
          }
        } else {
          java.util.Calendar start = CalendarUtils.getInstanceOfCurrentCalendar();
          start.setTime(this.startDate);
          int dayOfWeek = start.get(java.util.Calendar.DAY_OF_WEEK);
          setWeeklyByDay(convertToDayOfWeek(dayOfWeek));
        }
      } else {
        // for default data
        java.util.Calendar start = CalendarUtils.getInstanceOfCurrentCalendar();
        start.setTime(this.startDate);
        int dayOfWeek = start.get(java.util.Calendar.DAY_OF_WEEK);
        setWeeklyByDay(convertToDayOfWeek(dayOfWeek));
      }
      
      if (CalendarEvent.RP_MONTHLY.equals(repeatType)) {
        String[] monthlyByDay = event.getRepeatByDay();
        if (monthlyByDay != null && monthlyByDay.length > 0) {
          setMonthlyType(RP_MONTHLY_BYDAY);
        } else {
          setMonthlyType(RP_MONTHLY_BYMONTHDAY);
        }
      } else {
        setMonthlyType(RP_MONTHLY_BYMONTHDAY);
      }
      
      String endType = RP_END_NEVER;
      if (event.getRepeatCount() > 0) endType = RP_END_AFTER;
      else {
        if (event.getRepeatUntilDate() != null) endType = RP_END_BYDATE;
      }
      
      setEndType(endType);
      
      if (endType.equals(RP_END_AFTER)) {
        int count = (int) event.getRepeatCount();
        setEndAfter(String.valueOf(count));
      }
      if (endType.equals(RP_END_BYDATE)) {
        Date endDate = event.getRepeatUntilDate();
        setEndDate(endDate, calSetting.getDateFormat());
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception occur when init the UIRepeatEventForm", e);
      }
    }
  }
  
  public static class CancelActionListener extends EventListener<UIRepeatEventForm> {

    @Override
    public void execute(Event<UIRepeatEventForm> event) throws Exception {
        UIRepeatEventForm uiForm = event.getSource();  
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }
    
  }
  
  public static class SaveActionListener extends EventListener<UIRepeatEventForm> {

    @Override
    public void execute(Event<UIRepeatEventForm> event) throws Exception {
        UIRepeatEventForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UICalendarPortlet uiPortlet = uiContainer.getAncestorOfType(UICalendarPortlet.class);
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      UIEventForm uiEventForm = uiContainer.getChild(UIEventForm.class);
      CalendarSetting calSetting = CalendarUtils.getCurrentUserCalendarSetting();
      
      CalendarEvent repeatEvent = new CalendarEvent();
      repeatEvent.setFromDateTime(uiForm.startDate);
      repeatEvent.setRepeatType(uiForm.getRepeatType());
      repeatEvent.setRepeatInterval(Long.parseLong(uiForm.getInterval()));
      
      int count = 0;
      Date until = null;
      if (uiForm.getEndType().equals(UIRepeatEventForm.RP_END_AFTER)) {
        try {
          if (uiForm.getEndAfter() != null) count = Integer.parseInt(uiForm.getEndAfter());
          else count = 5;
        } catch (NumberFormatException e) {
          // pop-up error? too much pop-up window!
          count = 5;
        }
      } else {
        if (uiForm.getEndType().equals(UIRepeatEventForm.RP_END_BYDATE)) {

          try {
            if (uiForm.getEndDate() != null) {

              until = uiForm.getEndDate(calSetting.getDateFormat());
              if(until.before(uiEventForm.getEventToDate())){
                event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIRepeatEventForm.msg.until-time-logic-error", null, AbstractApplicationMessage.WARNING));
                return ;
              }
            }
            else { 
              java.util.Calendar temp = CalendarUtils.getInstanceOfCurrentCalendar();
              temp.setTime(uiForm.startDate);
              temp.add(java.util.Calendar.DATE, 5);
              until = temp.getTime();
            }
          } catch (ParseException e) {
            // pop up error?
            java.util.Calendar temp = CalendarUtils.getInstanceOfCurrentCalendar();
            temp.setTime(uiForm.startDate);
            temp.add(java.util.Calendar.DATE, 5);
            until = temp.getTime();
          }
        }
      }
      repeatEvent.setRepeatCount(count);
      repeatEvent.setRepeatUntilDate(until);
      
      if (repeatEvent.getRepeatType().equals(CalendarEvent.RP_WEEKLY)) {
        List<String> byDays = uiForm.getWeeklyByDay();
        if (byDays != null) repeatEvent.setRepeatByDay(byDays.toArray(new String[0]));
        else { 
          java.util.Calendar start = CalendarUtils.getInstanceOfCurrentCalendar();
          start.setTime(uiForm.startDate);
          int dayOfWeek = start.get(java.util.Calendar.DAY_OF_WEEK);
          String[] byDay = new String[] {UIRepeatEventForm.convertToDayOfWeek(dayOfWeek)};
          repeatEvent.setRepeatByDay(byDay);
        }
      }
      if (repeatEvent.getRepeatType().equals(CalendarEvent.RP_MONTHLY)) {
        String monthlyType = uiForm.getMonthlyType();
        if (monthlyType.equals(UIRepeatEventForm.RP_MONTHLY_BYDAY)) {
          // calculate day of week, week of month
          java.util.Calendar start = CalendarUtils.getInstanceOfCurrentCalendar();
          start.setTime(uiForm.startDate);
          int dayOfWeek = start.get(java.util.Calendar.DAY_OF_WEEK);
          int weekOfMonth = start.get(java.util.Calendar.WEEK_OF_MONTH);
          java.util.Calendar temp = CalendarUtils.getInstanceOfCurrentCalendar();
          temp.setTime(start.getTime());
          temp.add(java.util.Calendar.DATE, 7);
          if (temp.get(java.util.Calendar.MONTH) != start.get(java.util.Calendar.MONTH)) weekOfMonth = -1;
          String byDay = String.valueOf(weekOfMonth) + convertToDayOfWeek(dayOfWeek);
          String[] byDays = new String[1];
          byDays[0] = byDay;
          repeatEvent.setRepeatByDay(byDays);
        }
        if (monthlyType.equals(UIRepeatEventForm.RP_MONTHLY_BYMONTHDAY)) {
          // calculate month of day, save to repeatEvent object
          java.util.Calendar start = CalendarUtils.getInstanceOfCurrentCalendar();
          start.setTime(uiForm.startDate);
          int dayOfMonth = start.get(java.util.Calendar.DAY_OF_MONTH);
          long[] daysOfMonth = new long[1];
          daysOfMonth[0] = dayOfMonth;
          repeatEvent.setRepeatByMonthDay(daysOfMonth);
        }
      }
      
      uiEventForm.setRepeatEvent(repeatEvent);
      
      chilPopup.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
      
    }
    
  }
  
  public List<SelectItemOption<String>> getRepeatTypeOptions() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    for (int i = 1; i < CalendarEvent.REPEATTYPES.length; i++) {
      options.add(new SelectItemOption<String>(CalendarEvent.REPEATTYPES[i], CalendarEvent.REPEATTYPES[i]));
    }
    return options;
  }
  
  public List<SelectItemOption<String>> getIntervalOptions() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    for (int i = 1; i<= 30; i++) {
      options.add(new SelectItemOption<String>(String.valueOf(i), String.valueOf(i)));
    }
    return options;
  }
  
  protected void setRepeatType(String value) {
    this.getUIFormSelectBox(FIELD_REPEAT_TYPE).setValue(value);
  }
  
  protected String getRepeatType() {
    return this.getUIFormSelectBox(FIELD_REPEAT_TYPE).getValue();
  }
  
  protected void setInterval(String value) {
    this.getUIFormSelectBox(FIELD_INTERVAL).setValue(value);
  }
  
  protected String getInterval() {
    return this.getUIFormSelectBox(FIELD_INTERVAL).getValue();
  }
  
  // value: MO, TU, FR, ..
  protected void setWeeklyByDay(String value) {
    UICheckBoxInput checkbox = this.getUICheckBoxInput(value);
    checkbox.setChecked(true);
  }
  
  // list of MO, TU, FR
  protected List<String> getWeeklyByDay() {
    List<String> byDays = new ArrayList<String>();
    for (int i = 0; i < 7; i++) {
      UICheckBoxInput checkbox = this.getUICheckBoxInput(CalendarEvent.RP_WEEKLY_BYDAY[i]);
      if (checkbox.isChecked()) byDays.add(CalendarEvent.RP_WEEKLY_BYDAY[i]);
    }
    return byDays;
  }
  
  protected void setMonthlyType(String value) {
    UIFormRadioBoxInput monthlyType = this.findComponentById(FIELD_MONTHLY_TYPE);
    monthlyType.setValue(value);
  }
  
  protected String getMonthlyType() {
    UIFormRadioBoxInput monthlyType = this.findComponentById(FIELD_MONTHLY_TYPE);
    return monthlyType.getValue();
  }
  
  protected String getEndType() {
    return this.getChild(UIFormHiddenInput.class).getValue();
  }
  
  protected void setEndType(String value) {
    this.getChild(UIFormHiddenInput.class).setValue(value);
  }
  
  protected void setEndAfter(String value) {
    this.getUIStringInput(FIELD_END_AFTER).setValue(value);
  }
  
  protected String getEndAfter() {
    return this.getUIStringInput(FIELD_END_AFTER).getValue();
  }
  
  protected String getEndDate() {
    UIFormDateTimePicker endDate = this.getChildById(FIELD_END_BYDATE);
    return  endDate.getValue();
  }
  
  protected Date getEndDate(String dateFormat) throws Exception {
    UIFormDateTimePicker endDate = this.getChildById(FIELD_END_BYDATE) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    return df.parse(endDate.getValue()) ;
  }
  
  protected void setEndDate(Date date,String dateFormat) {
    UIFormDateTimePicker endDate = this.getChildById(FIELD_END_BYDATE) ;
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(dateFormat, locale) ;
    df.setCalendar(CalendarUtils.getInstanceOfCurrentCalendar()) ;
    endDate.setValue(df.format(date)) ;
  }
  
  public static int convertToDayOfWeek(String day) {
    int dayOfWeek = (day.equals("MO")?2:
                    (day.equals("TU")?3:
                    (day.equals("WE")?4:
                    (day.equals("TH")?5:
                    (day.equals("FR")?6:
                    (day.equals("SA")?7:
                    (day.equals("SU")?1:0)
                    ))))));
    return dayOfWeek;
  }
  
  public static String convertToDayOfWeek(int day) {
    String dayOfWeek = (day==1?"SU":
                       (day==2?"MO":
                       (day==3?"TU":
                       (day==4?"WE":
                       (day==5?"TH":
                       (day==6?"FR":
                       (day==7?"SA":null)
                       ))))));
    return dayOfWeek;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.calendar.webui.popup.UIPopupComponent#activate()
   */
  @Override
  public void activate() throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.webui.popup.UIPopupComponent#deActivate()
   */
  @Override
  public void deActivate() throws Exception {

  }
  
}
