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
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.PathNotFoundException;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.popup.UIConfirmForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
                 lifecycle =UIFormLifecycle.class,
                 template = "app:/templates/calendar/webui/UIWeekView.gtmpl",
                 events = {
                   @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
                   @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
                   @EventConfig(listeners = UICalendarView.ConfirmCloseActionListener.class),
                   @EventConfig(listeners = UICalendarView.AbortCloseActionListener.class),
                   @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
                   @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
                   @EventConfig(listeners = UICalendarView.QuickAddActionListener.class), 
                   @EventConfig(listeners = UICalendarView.ViewActionListener.class),
                   @EventConfig(listeners = UICalendarView.EditActionListener.class), 
                   @EventConfig(listeners = UICalendarView.DeleteActionListener.class),
                   @EventConfig(listeners = UICalendarView.MoveNextActionListener.class), 
                   @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class),
                   @EventConfig(listeners = UIWeekView.UpdateEventActionListener.class),
                   @EventConfig(listeners = UICalendarView.ExportEventActionListener.class),
                   @EventConfig(listeners = UIWeekView.UpdateAllDayEventActionListener.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteOnlyInstance.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteFollowingSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteAllSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteCancel.class),
                   @EventConfig(listeners = UICalendarView.ConfirmUpdateOnlyInstance.class),
                   @EventConfig(listeners = UICalendarView.ConfirmUpdateFollowSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmUpdateAllSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmUpdateCancel.class)
                 }
    )
public class UIWeekView extends UICalendarView {
  private static final Log log = ExoLogger.getExoLogger(UIWeekView.class);

  public static final String    CURRENT_DATE     = "currentDate"; 

  protected Map<String, List<CalendarEvent>> eventData_ = new HashMap<String, List<CalendarEvent>>() ;
  protected List<CalendarEvent> allDayEvent = new ArrayList<CalendarEvent>();
  protected LinkedHashMap<String, CalendarEvent> dataMap_ = new LinkedHashMap<String,  CalendarEvent>() ;
  protected  List<CalendarEvent> daysData_  = new ArrayList<CalendarEvent>() ;
  protected boolean isShowCustomView_ = false ;
  protected Date beginDate_ ;
  protected Date endDate_ ; 

  public UIWeekView() throws Exception {
    super() ;
  }

  @Override
  public void refresh() throws Exception {
    eventData_.clear() ;
    allDayEvent.clear();
    int i = 0 ;
    Calendar c = getBeginDateOfWeek() ;
    int maxDay = 7 ;
    if(isShowCustomView_) maxDay = 5 ;
    while(i++ <maxDay) {
      List<CalendarEvent> list = new ArrayList<CalendarEvent>();
      String key = keyGen(c.get(Calendar.DATE), c.get(Calendar.MONTH), c.get(Calendar.YEAR)) ;
      eventData_.put(key, list) ;
      c.add(Calendar.DATE, 1) ;
    }
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(getBeginDateOfWeek()) ;
    Calendar endDateOfWeek = getEndDateOfWeek();
    Date toDate = endDateOfWeek.getTime();
    toDate.setTime(toDate.getTime()-1);
    endDateOfWeek.setTime(toDate);
    eventQuery.setToDate(endDateOfWeek) ; 
    eventQuery.setExcludeRepeatEvent(true);

    /** get all norepeat events */
    List<CalendarEvent> allEvents;
    String[] publicCalendars = getPublicCalendars();
    if (isInSpace()) {
      eventQuery.setCalendarId(publicCalendars);
      allEvents = calendarService.getPublicEvents(eventQuery);
    } else
      allEvents = calendarService.getEvents(username, eventQuery, getPublicCalendars());

    /** get exception occurrences, exclude original recurrence events */
    List<CalendarEvent> originalRecurEvents = calendarService.getHighLightOriginalRecurrenceEvents(username,
        eventQuery.getFromDate(), eventQuery.getToDate(), publicCalendars);
    String timezone = CalendarUtils.getCurrentUserCalendarSetting().getTimeZone();
    if (originalRecurEvents != null && originalRecurEvents.size() > 0) {
      Iterator<CalendarEvent> recurEventsIter = originalRecurEvents.iterator();
      while (recurEventsIter.hasNext()) {
        CalendarEvent recurEvent = recurEventsIter.next();
        Map<String,CalendarEvent> tempMap = calendarService.getOccurrenceEvents(recurEvent, eventQuery.getFromDate(), eventQuery.getToDate(), timezone);
        if (tempMap != null) {
          recurrenceEventsMap.put(recurEvent.getId(), tempMap);
          allEvents.addAll(tempMap.values());
        }
      }
    }

    Iterator<CalendarEvent> iter = allEvents.iterator() ;
    while(iter.hasNext()) {
      CalendarEvent event = iter.next() ;
      Date beginEvent = event.getFromDateTime() ;
      Date endEvent = event.getToDateTime() ;
      long eventAmount = endEvent.getTime() - beginEvent.getTime() ;
      i = 0 ;
      c = getBeginDateOfWeek();
      while(i++ < maxDay) {
        String key = keyGen(c.get(Calendar.DATE), c.get(Calendar.MONTH), c.get(Calendar.YEAR)) ;
        if(isSameDate(c.getTime(), beginEvent) && (isSameDate(c.getTime(), endEvent)) && eventAmount < CalendarUtils.MILISECONS_OF_DAY){
          eventData_.get(key).add(event) ;
          iter.remove() ;
        }  
        c.add(Calendar.DATE, 1) ;
      }
    }
    for( CalendarEvent ce : allEvents) {
      allDayEvent.add(ce);
    } 
  }


  public java.util.Calendar getBeginDateOfWeek() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    if(isShowCustomView_) temCal.setFirstDayOfWeek(Calendar.SUNDAY) ;    
    if(temCal.getFirstDayOfWeek() > temCal.get(Calendar.DAY_OF_WEEK)) {
      temCal.add(java.util.Calendar.WEEK_OF_YEAR, -1) ;
    }
    int amout = temCal.getFirstDayOfWeek() - temCal.get(Calendar.DAY_OF_WEEK);
    if(isShowCustomView_) amout = amout + 1 ;
    temCal.add(Calendar.DATE, amout) ;
    return getBeginDay(temCal) ;
  }

  public java.util.Calendar getEndDateOfWeek() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    if(isShowCustomView_) temCal.setFirstDayOfWeek(Calendar.SUNDAY) ; 
    temCal.setTime(getBeginDateOfWeek().getTime()) ;
    int amout = 6 ;
    if(isShowCustomView_) amout = amout - 2 ;
    temCal.add(Calendar.DATE, amout) ;
    return getEndDay(temCal) ;
  }

  protected Map<String, List<CalendarEvent>> getEventData() {return eventData_ ;}

  @Override
  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    LinkedHashMap<String, CalendarEvent> dataMap = new LinkedHashMap<String,  CalendarEvent>() ;
    for (CalendarEvent ce : allDayEvent) {
      dataMap.put(ce.getId(), ce);
    }
    for(String key : eventData_.keySet()) {
      for(CalendarEvent ce : eventData_.get(key)) {
        dataMap.put(ce.getId(), ce);
      }
    }
    return dataMap ;
  }
  public boolean isShowCustomView() {return isShowCustomView_ ;}

  static  public class UpdateEventActionListener extends EventListener<UIWeekView> {
    @Override
    public void execute(Event<UIWeekView> event) throws Exception {

      UIWeekView calendarview = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class);
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID);
      String calendarId = event.getRequestContext().getRequestParameter(eventId + CALENDARID);
      String calType = event.getRequestContext().getRequestParameter(eventId + CALTYPE);
      String startTime = event.getRequestContext().getRequestParameter(eventId + START_TIME);
      String finishTime = event.getRequestContext().getRequestParameter(eventId + FINISH_TIME);
      String currentDate = event.getRequestContext().getRequestParameter(eventId + CURRENT_DATE);

      Boolean isOccur = false;
      if (!Utils.isEmpty(event.getRequestContext().getRequestParameter(eventId+ ISOCCUR))) {
        isOccur = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(eventId + ISOCCUR));
      }
      String recurId = null;
      if (isOccur)
        recurId = event.getRequestContext().getRequestParameter(eventId+ RECURID);

      String username = CalendarUtils.getCurrentUser() ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;

      CalendarEvent eventCalendar = calendarview.getDataMap().get(eventId) ;
      if (isOccur && !Utils.isEmpty(recurId)) {
        eventCalendar = calendarview.getRecurrenceMap().get(eventId).get(recurId);
      }

      if(eventCalendar != null) {
        CalendarService calService = CalendarUtils.getCalendarService() ;
        boolean isMove = false;
        try {
          org.exoplatform.calendar.service.Calendar calendar = null ;
          if(eventCalendar.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
            calendar = calService.getUserCalendar(username, calendarId) ;
          } else if(eventCalendar.getCalType().equals(CalendarUtils.SHARED_TYPE)){
            if(calService.getSharedCalendars(username, true) != null)
              calendar = 
              calService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          } else if(eventCalendar.getCalType().equals(CalendarUtils.PUBLIC_TYPE)) {
            calendar = calService.getGroupCalendar(calendarId) ;
          }
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            Calendar cal = calendarview.getInstanceTempCalendar() ;
            int hoursBg = (Integer.parseInt(startTime)/60) ;
            int minutesBg = (Integer.parseInt(startTime)%60) ;
            int hoursEnd = (Integer.parseInt(finishTime)/60) ;
            int minutesEnd = (Integer.parseInt(finishTime)%60) ;
            try {
              cal.setTimeInMillis(Long.parseLong(currentDate)) ;
              if(hoursBg < cal.getMinimum(Calendar.HOUR_OF_DAY)) {
                hoursBg = 0 ;
                minutesBg = 0 ;
              }
              cal.set(Calendar.HOUR_OF_DAY, hoursBg) ;
              cal.set(Calendar.MINUTE, minutesBg) ;
              isMove = (eventCalendar.getFromDateTime().getTime() != cal.getTimeInMillis()) ;
              eventCalendar.setFromDateTime(cal.getTime()) ;
              if(hoursEnd >= 24) {
                hoursEnd = 23 ;
                minutesEnd = 59 ;
              }
              cal.set(Calendar.HOUR_OF_DAY, hoursEnd) ;
              cal.set(Calendar.MINUTE, minutesEnd) ;
              eventCalendar.setToDateTime(cal.getTime()) ;
            } catch (Exception e) {
              if (log.isDebugEnabled()) {
                log.debug("Fail when calculate the time for calendar", e);
              }
              return ;
            }
            if(eventCalendar.getToDateTime().before(eventCalendar.getFromDateTime())) {
              return ;
            }
            // if it's a 'virtual' occurrence
            if (isOccur && !Utils.isEmpty(recurId)) {
              if(!isMove) {
                UIPopupAction pAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
                UIConfirmForm confirmForm =  pAction.activate(UIConfirmForm.class, 480);
                confirmForm.setConfirmMessage("update-recurrence-event-confirm-msg");
                confirmForm.setDelete(false);
                confirmForm.setConfig_id(calendarview.getId()) ;
                calendarview.setCurrentOccurrence(eventCalendar);
                event.getRequestContext().addUIComponentToUpdateByAjax(pAction);
              } else {
                calService = CalendarUtils.getCalendarService() ;
                CalendarEvent originEvent = calService.getRepetitiveEvent(eventCalendar);
                calService.saveOneOccurrenceEvent(originEvent, eventCalendar, username);
              }
             // return;
              //List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
              //listEvent.add(eventCalendar);
              //calendarService.updateOccurrenceEvent(calendarId, calendarId, calType, calType, listEvent, username);
            } else {
              if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
                calendarService.saveUserEvent(username, calendarId, eventCalendar, false) ;  
              } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
                calendarService.saveEventToSharedCalendar(username, calendarId, eventCalendar, false) ;
              } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
                calendarService.savePublicEvent(calendarId, eventCalendar, false) ;          
              }
            }
            calendarview.setLastUpdatedEventId(eventId) ;
            calendarview.refresh() ;
            UIMiniCalendar uiMiniCalendar = uiCalendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar);
            if(isOccur) event.getRequestContext().addUIComponentToUpdateByAjax(calendarview) ;
            JavascriptManager jsManager = event.getRequestContext().getJavascriptManager();
            RequireJS requireJS = jsManager.getRequireJS();
            requireJS.require("PORTLET/calendar/CalendarPortlet","cal");
            requireJS.addScripts("cal.UIWeekView.setSize();cal.UIWeekView.cleanUp();");

          }
        } catch (PathNotFoundException e) {
          if (log.isDebugEnabled()) {
            log.debug("The calendar is not found", e);
          }
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        } catch (Exception ex){
          if (log.isDebugEnabled()) {
            log.debug("The calendar is not found", ex);
          }
        }
      }

    }
  }

  static  public class UpdateAllDayEventActionListener extends EventListener<UIWeekView> {
    @Override
    public void execute(Event<UIWeekView> event) throws Exception {
      UIWeekView calendarview = event.getSource() ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID);
      String calendarId = event.getRequestContext().getRequestParameter(eventId + CALENDARID);
      String calType = event.getRequestContext().getRequestParameter(eventId + CALTYPE);
      String startTime = event.getRequestContext().getRequestParameter(eventId + START_TIME);
      String finishTime = event.getRequestContext().getRequestParameter(eventId + FINISH_TIME);
      Boolean isOccur = false;
      if (!Utils.isEmpty(event.getRequestContext().getRequestParameter(eventId + ISOCCUR))) {
        isOccur = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(eventId + ISOCCUR));
      }
      String recurId = null;
      if (isOccur)
        recurId = event.getRequestContext().getRequestParameter(eventId + RECURID);
      try {
        String username = CalendarUtils.getCurrentUser() ;
        CalendarEvent eventCalendar = null;
        if (isOccur && !Utils.isEmpty(recurId)) {
          eventCalendar = calendarview.getRecurrenceMap().get(eventId).get(recurId);
        } else {
          eventCalendar = calendarview.getDataMap().get(eventId) ;
        }
        if(eventCalendar != null) {
          CalendarService calendarService = CalendarUtils.getCalendarService() ;
          Calendar calBegin = calendarview.getInstanceTempCalendar() ;
          Calendar calEnd = calendarview.getInstanceTempCalendar() ;
          long unit = 15*60*1000 ;
          calBegin.setTimeInMillis((Long.parseLong(startTime)/unit)*unit) ;
          eventCalendar.setFromDateTime(calBegin.getTime()) ;
          calEnd.setTimeInMillis((Long.parseLong(finishTime)/unit)*unit) ;
          eventCalendar.setToDateTime(calEnd.getTime()) ;
          if(eventCalendar.getToDateTime().before(eventCalendar.getFromDateTime())) {
            return ;
          }
          org.exoplatform.calendar.service.Calendar calendar = null ;
          if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
            calendar = calendarService.getUserCalendar(username, calendarId) ;
          } else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
            if(calendarService.getSharedCalendars(username, true) != null)
              calendar = 
              calendarService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          } else if(CalendarUtils.PUBLIC_TYPE.equals(calType)) {
            calendar = calendarService.getGroupCalendar(calendarId) ;
          }
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            if((CalendarUtils.SHARED_TYPE.equals(calType) && !CalendarUtils.canEdit(calendarview.getApplicationComponent(OrganizationService.class), Utils.getEditPerUsers(calendar), username)) ||
                (CalendarUtils.PUBLIC_TYPE.equals(calType) && !CalendarUtils.canEdit(calendarview.getApplicationComponent(OrganizationService.class), calendar.getEditPermission(), username))) 
            {
              event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit-event", null, 1)) ;
              calendarview.refresh() ;
              event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
              return ;
            }
            // if it's a 'virtual' occurrence
            if (isOccur && !Utils.isEmpty(recurId)) {
              List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
              listEvent.add(eventCalendar);
              calendarService.updateOccurrenceEvent(calendarId, calendarId, calType, calType, listEvent, username);
            } else {
              if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
                calendarService.saveUserEvent(username, calendarId, eventCalendar, false) ;
              } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
                calendarService.saveEventToSharedCalendar(username, calendarId, eventCalendar, false) ;
              } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
                calendarService.savePublicEvent(calendarId, eventCalendar, false) ;          
              }
            }
            calendarview.setLastUpdatedEventId(eventId) ;
            calendarview.refresh() ;
            UIMiniCalendar uiMiniCalendar = calendarview.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
          }
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to save the event to the calendar", e);
        }
        return ;
      }
    }
  }

  @Override
  public String getDefaultStartTimeOfEvent() {
    if (isCurrentWeek(calendar_.get(Calendar.WEEK_OF_YEAR), calendar_.get(Calendar.MONTH), calendar_.get(Calendar.YEAR))) {
      // if selected week is current week, the start time is present
      return String.valueOf(System.currentTimeMillis());
    } else {
      // else the start time is last date of week.
      Calendar c = Calendar.getInstance();
      c.setTime(calendar_.getTime());
      int firstDayOfWeek = calendar_.getFirstDayOfWeek();
      c.setFirstDayOfWeek(firstDayOfWeek);
      do {
        c.add(Calendar.DATE, 1);
      } while (c.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek);
      c.add(Calendar.DATE, -1);
      return String.valueOf(c.getTimeInMillis());
    }
  }
}
