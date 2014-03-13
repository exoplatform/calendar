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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.PathNotFoundException;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.popup.UIConfirmForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
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
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/calendar/webui/UIDayView.gtmpl", 
                 events = {
                   @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
                   @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
                   @EventConfig(listeners = UICalendarView.ConfirmCloseActionListener.class),
                   @EventConfig(listeners = UICalendarView.AbortCloseActionListener.class),
                   @EventConfig(listeners = UICalendarView.ViewActionListener.class),
                   @EventConfig(listeners = UICalendarView.EditActionListener.class), 
                   @EventConfig(listeners = UICalendarView.DeleteActionListener.class),
                   @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
                   @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
                   @EventConfig(listeners = UICalendarView.QuickAddActionListener.class), 
                   @EventConfig(listeners = UICalendarView.MoveNextActionListener.class), 
                   @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class), 
                   @EventConfig(listeners = UICalendarView.ExportEventActionListener.class),
                   @EventConfig(listeners = UIDayView.UpdateEventActionListener.class),
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
public class UIDayView extends UICalendarView {
  private static final Log log = ExoLogger.getExoLogger(UIDayView.class);

  private List<CalendarEvent> eventData_ = new ArrayList<CalendarEvent>();
  private List<CalendarEvent> allDayEvent_ = new ArrayList<CalendarEvent>();

  public UIDayView() throws Exception{
    super() ;
  }
  @Override
  public void refresh() throws Exception {
    eventData_.clear() ;
    allDayEvent_.clear() ;
    Calendar begin = getBeginDay(getCurrentCalendar()) ;  
    Calendar end = getEndDay(getCurrentCalendar()) ; 
    end.add(Calendar.MILLISECOND, -1) ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(begin) ;
    eventQuery.setToDate(end) ;
    eventQuery.setExcludeRepeatEvent(true);

    String[] publicCalendars  = getPublicCalendars();
    String[] privateCalendars = getPrivateCalendars().toArray(new String[]{});

    if(isInSpace()) { 
      eventQuery.setCalendarId(publicCalendars);
      events = calendarService.getPublicEvents(eventQuery);
    }
    else {
      events =  calendarService.getAllNoRepeatEventsSQL(username, eventQuery,
          privateCalendars, publicCalendars, emptyEventCalendars);
    }

    String timezone = CalendarUtils.getCurrentUserCalendarSetting().getTimeZone();

    List<CalendarEvent> originalRecurEvents = calendarService.getHighLightOriginalRecurrenceEventsSQL(username,
        eventQuery.getFromDate(), eventQuery.getToDate(), eventQuery, privateCalendars, publicCalendars, emptyRecurrentEventCalendars);

    if (originalRecurEvents != null && originalRecurEvents.size() > 0) {
      Iterator<CalendarEvent> recurEventsIter = originalRecurEvents.iterator();
      while (recurEventsIter.hasNext()) {
        CalendarEvent recurEvent = recurEventsIter.next();
        Map<String,CalendarEvent> tempMap = calendarService.getOccurrenceEvents(recurEvent, eventQuery.getFromDate(), eventQuery.getToDate(), timezone);
        if (tempMap != null) {
          recurrenceEventsMap.put(recurEvent.getId(), tempMap);
          events.addAll(tempMap.values());
        }
      }
    }

    Iterator<CalendarEvent> iter = events.iterator() ;
    while (iter.hasNext()) {
      CalendarEvent ce = iter.next() ;
      long eventAmount = ce.getToDateTime().getTime() - ce.getFromDateTime().getTime() ;
      if (isSameDate(ce.getFromDateTime(), getCurrentDate())
          && isSameDate(ce.getToDateTime(), getCurrentDate())
          && eventAmount < CalendarUtils.MILISECONS_OF_DAY) {
        eventData_.add(ce);
        iter.remove() ;
      } 
    }
    for(CalendarEvent ce : events){
      allDayEvent_.add(ce);
    } 
  }
  protected List<CalendarEvent> getEventData() { return eventData_; }
  protected List<CalendarEvent> getAllDayEvents() { return allDayEvent_; } ;

  @Override
  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    LinkedHashMap<String, CalendarEvent> dataMap = new LinkedHashMap<String, CalendarEvent>() ;
    for (CalendarEvent ce : eventData_) {
      dataMap.put(ce.getId(), ce);
    }
    for (CalendarEvent ce : allDayEvent_) {
      dataMap.put(ce.getId(), ce);
    }
    return dataMap ;
  }

  static  public class UpdateEventActionListener extends EventListener<UIDayView> {
    @Override
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource();
      UICalendarPortlet uiCalendarPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class);

      calendarview.refresh();
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID);
      String calendarId = event.getRequestContext().getRequestParameter(eventId + CALENDARID);
      String startTime = event.getRequestContext().getRequestParameter(eventId + START_TIME);
      String endTime = event.getRequestContext().getRequestParameter(eventId + FINISH_TIME);
      Boolean isOccur = false;
      if (!Utils.isEmpty(event.getRequestContext().getRequestParameter(eventId + ISOCCUR))) {
        isOccur = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(eventId + ISOCCUR));
      }
      String recurId = null;
      if (isOccur)
        recurId = event.getRequestContext().getRequestParameter(eventId + RECURID);

      String username = CalendarUtils.getCurrentUser() ;
      CalendarEvent ce = null;
      if (isOccur && !Utils.isEmpty(recurId)) {
        ce = calendarview.getRecurrenceMap().get(eventId).get(recurId);
      } else {
        ce = calendarview.getDataMap().get(eventId) ;
      }
      if(ce != null) {
        CalendarService calService = CalendarUtils.getCalendarService() ;
        try {
          org.exoplatform.calendar.service.Calendar calendar = null ;
          if(ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
            calendar = calService.getUserCalendar(username, calendarId) ;
          } else if(ce.getCalType().equals(CalendarUtils.SHARED_TYPE)){
            if(calService.getSharedCalendars(username, true) != null)
              calendar = calService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          } else if(ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)) {
            calendar = calService.getGroupCalendar(calendarId) ;
          }
          boolean isMove = false;
          if(calendar == null) {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          } else {
            if((ce.getCalType().equals(CalendarUtils.SHARED_TYPE) && !CalendarUtils.canEdit(calendarview.getApplicationComponent(

                                                                                                                                 OrganizationService.class), Utils.getEditPerUsers(calendar), CalendarUtils.getCurrentUser())) ||
                                                                                                                                 (ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE) && !CalendarUtils.canEdit(calendarview.getApplicationComponent(
                                                                                                                                                                                                                                                   OrganizationService.class), calendar.getEditPermission(), CalendarUtils.getCurrentUser()))) 
            {

              event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit-event", null, 1)) ;
              calendarview.refresh() ;
              event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
              return ;
            }
            int hoursBg = (Integer.parseInt(startTime)/60) ;
            int minutesBg = (Integer.parseInt(startTime)%60) ;
            int hoursEnd = (Integer.parseInt(endTime)/60) ;
            int minutesEnd = (Integer.parseInt(endTime)%60) ;
            Calendar cal = calendarview.getInstanceTempCalendar()  ; 
            cal.setTime(calendarview.getCurrentDate()) ;

            try {
              //cal.setTimeInMillis(Long.parseLong(currentDate)) ;
              if(hoursBg < cal.getMinimum(Calendar.HOUR_OF_DAY)) {
               hoursBg = 0 ;
               minutesBg = 0 ;
              }
              cal.set(Calendar.HOUR_OF_DAY, hoursBg) ;
              cal.set(Calendar.MINUTE, minutesBg) ;
              cal.set(Calendar.SECOND, 0) ;
              isMove = (ce.getFromDateTime().getHours() != cal.get(Calendar.HOUR) && ce.getFromDateTime().getMinutes() != cal.get(Calendar.MINUTE)) ;
              ce.setFromDateTime(cal.getTime());
              if(hoursEnd >= 24) {
                hoursEnd = 23 ;
                minutesEnd = 59 ;
              }
              cal.set(Calendar.HOUR_OF_DAY, hoursEnd) ;
              cal.set(Calendar.MINUTE, minutesEnd) ; 
              ce.setToDateTime(cal.getTime()) ; 
            } catch (Exception e) {
              if (log.isDebugEnabled()) {
                log.debug("Exception when calculate calendar time", e);
              }
              return ;
            }
            if(ce.getToDateTime().before(ce.getFromDateTime())) {
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
                calendarview.setCurrentOccurrence(ce);
                event.getRequestContext().addUIComponentToUpdateByAjax(pAction);
              } else {
                calService = CalendarUtils.getCalendarService() ;
                CalendarEvent originEvent = calService.getRepetitiveEvent(ce);
                calService.saveOneOccurrenceEvent(originEvent, ce, username);
              }
              //return;
              //List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
              //listEvent.add(ce);
              //calService.updateOccurrenceEvent(calendarId, calendarId, ce.getCalType(), ce.getCalType(), listEvent, username);
            } else {
              if (ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
                CalendarUtils.getCalendarService().saveUserEvent(username, calendarId, ce, false) ;
              } else if (ce.getCalType().equals(CalendarUtils.SHARED_TYPE)) {
                CalendarUtils.getCalendarService().saveEventToSharedCalendar(username, calendarId, ce, false) ;
              } else if (ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)) {
                CalendarUtils.getCalendarService().savePublicEvent(calendarId, ce, false) ;          
              }
            }
            calendarview.setLastUpdatedEventId(eventId) ;
            calendarview.refresh() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
          }

        } catch (PathNotFoundException e) {
          if (log.isDebugEnabled()) {
            log.debug("The calendar is not found", e);
          }

          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        }
        UICalendarViewContainer uiViewContainer = uiCalendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        CalendarSetting setting = calService.getCalendarSetting(username) ;
        uiViewContainer.refresh() ;
        uiCalendarPortlet.setCalendarSetting(setting) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
      } else  {
        UICalendarWorkingContainer uiWorkingContainer = calendarview.getAncestorOfType(UICalendarWorkingContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;

        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UICalendarView.msg.event-not-found", null)) ;
      }
    }
  }



  @Override
  public String getDefaultStartTimeOfEvent() {
    return String.valueOf(calendar_.getTimeInMillis());
  }
}
