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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.model.Event;
import org.exoplatform.calendar.model.query.EventQuery;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 events = {
                   @EventConfig(listeners = UICalendarView.AddEventActionListener.class),
                   @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
                   @EventConfig(listeners = UICalendarView.ConfirmCloseActionListener.class),
                   @EventConfig(listeners = UICalendarView.AbortCloseActionListener.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteEvent.class),
                   @EventConfig(listeners = UICalendarView.CancelDeleteEvent.class),
                   @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
                   @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
                   @EventConfig(listeners = UICalendarView.ViewActionListener.class),
                   @EventConfig(listeners = UICalendarView.EditActionListener.class),
                   @EventConfig(listeners = UICalendarView.DeleteActionListener.class),
                   @EventConfig(listeners = UIListView.CloseSearchActionListener.class),
                   @EventConfig(listeners = UIListView.ViewDetailActionListener.class),
                   @EventConfig(listeners = UICalendarView.MoveNextActionListener.class),
                   @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class),
                   @EventConfig(listeners = UIListView.ShowPageActionListener.class ),
                   @EventConfig(listeners = UICalendarView.ExportEventActionListener.class),
                   @EventConfig(listeners = UIListView.OnchangeActionListener.class ),
                   @EventConfig(listeners = UIListView.SortActionListener.class ),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteOnlyInstance.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteAllSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteFollowingSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteCancel.class),
                   @EventConfig(listeners = UICalendarView.ConfirmUpdateCancel.class),
                   @EventConfig(listeners = UIListView.AdvancedSearchActionListener.class)
                 }
    )
public class UIListView extends UICalendarView {
  private static final Log log = ExoLogger.getLogger(UIListView.class);
  private LinkedHashMap<String, Event> eventMap_ = new LinkedHashMap<String, Event>() ;
  private EventPageList pageList_ = null ;
  private String selectedEvent_ = null ;
  private boolean isShowEventAndTask = true ;
  private boolean isSearchResult = false ;
  private String lastViewId_ = null ;
  private String categoryId_ = null ;
  private String keyWords_ = null ;
  private int currentPage_ = 0 ;
  private EventQuery query = null;

  public static final String EVENT_SUMMARY = Utils.EXO_SUMMARY;
  public static final String EVENT_PRIORITY = Utils.EXO_PRIORITY;
  public static final String EVENT_DESCRIPTION = Utils.EXO_DESCRIPTION;
  public static final String EVENT_START = Utils.EXO_FROM_DATE_TIME;
  public static final String EVENT_END = Utils.EXO_TO_DATE_TIME;
  private String sortedField_ = EVENT_SUMMARY;
  private boolean isAscending_ = true;

  private boolean calClicked = true;

  public UIListView() throws Exception{
    if(getEvents().length > 0 ) {
      selectedEvent_ = getEvents()[0].getId() ;
    }
  }

  @Override
  public String getTemplate() {
    if(getViewType().equals(TYPE_EVENT)) {
      return "app:/templates/calendar/webui/UIListEvent.gtmpl" ;
    } else {
      return "app:/templates/calendar/webui/UIListView.gtmpl" ;
    }
  }

  public void setSortedField(String field) { sortedField_ = field; }
  public String getSortedField() { return sortedField_; }

  public void setIsAscending(boolean b) { isAscending_ = b; }
  public boolean isAscending() { return isAscending_; }

  public void setEventQuery(EventQuery eventQuery) { 
    query = eventQuery;
  }
  public EventQuery getEventQuery() { 
    return query;
  }

  @Override
  public void refresh() throws Exception {
    UIListContainer uiListContainer = getParent() ;
    this.setCalClicked(true);
    if (uiListContainer.isDisplaySearchResult()) return;
    query = new EventQuery();
    query.setOwner(CalendarUtils.getCurrentUser());
    if (!CalendarUtils.isEmpty(categoryId_) && !categoryId_.toLowerCase().equals("null")
        && !categoryId_.equals("calId") && !categoryId_.equals(NewUserListener.DEFAULT_EVENTCATEGORY_ID_ALL)) {
      query.setCategoryIds(new String[] { categoryId_ });
    }

    Calendar fromcalendar = getBeginDay(getCurrentCalendar());
    query.setFromDate(fromcalendar.getTimeInMillis());
    Calendar tocalendar = getEndDay(getCurrentCalendar());
    if(tocalendar.get(Calendar.MILLISECOND) == 0) tocalendar.add(Calendar.MILLISECOND, -1);
    query.setToDate(tocalendar.getTimeInMillis());
    if(!getViewType().equals(TYPE_BOTH)) {
      query.setEventType(getViewType());
    }

    List<String> calendarIds = findCalendarIds();
    if (calendarIds.size() > 0)
      query.setCalendarIds(calendarIds.toArray(new String[] {}));
    else {
      query.setCalendarIds(null);
    }
    query.setOrderBy(new String[] {Utils.EXO_SUMMARY});

    List<Event> allEvents = getAllEvents(query);

    if(uiListContainer.isDisplaySearchResult())  {
      update(pageList_) ;
    } else {
      update(new EventPageList(allEvents,10)) ;
    }

    if(currentPage_ > 0 && currentPage_ <= pageList_.getAvailablePage()) {
      updateCurrentPage(currentPage_) ;
    }

    UIListContainer uiContainer = getParent() ;
    UIPreview view = uiContainer.getChild(UIPreview.class);
    Event[] events = getEvents();
    String selectedEvent = getSelectedEvent();

    if(events.length == 0) {
      setSelectedEvent(null) ;
      view.setEvent(null) ;
      setLastUpdatedEventId(null) ;
    } else if(CalendarUtils.isEmpty(selectedEvent) || !eventMap_.containsKey(selectedEvent)){
        String eventId = events[0].getId() ;
        setSelectedEvent(eventId) ;
        setLastUpdatedEventId(eventId) ;
        view.setEvent(events[0]) ;
    } else {
        view.setEvent(eventMap_.get(selectedEvent));
        setLastUpdatedEventId(selectedEvent);
    }
  }
  
  public void refreshSearch() throws Exception {
    this.setDisplaySearchResult(true);
    List<Event> allEvents = this.getAllEvents(getEventQuery());
    this.update(new EventPageList(allEvents, 10));
    this.setSelectedEvent(null);
    this.setLastUpdatedEventId(null);
    this.getParent().findFirstComponentOfType(UIPreview.class).setEvent(null);
  }

  private List<String> findCalendarIds() throws Exception {
    List<String> calendarIds = new ArrayList<String>();
    UICalendars uiCalendars = getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UICalendars.class);
    if(isInSpace()){
      List<String> spaceCals = new LinkedList<String>(Arrays.asList(getPublicCalendars()));
      spaceCals.addAll(getOtherSpaceCalendar());
      return spaceCals;
    }
    List<String> checkedCals = uiCalendars.getCheckedCalendars();
    List<org.exoplatform.calendar.service.Calendar> privateCalendar = uiCalendars.getAllPrivateCalendars();
    List<org.exoplatform.calendar.service.Calendar> publicCalendar = uiCalendars.getAllPublicCalendars();
    List<org.exoplatform.calendar.service.Calendar> shareClas = uiCalendars.getAllSharedCalendars();
    List<org.exoplatform.calendar.service.Calendar> otherClas = uiCalendars.getAllOtherCalendars();

    for (org.exoplatform.calendar.service.Calendar cal :privateCalendar) {
      if (checkedCals.contains(cal.getId())) {
        calendarIds.add(cal.getId());
      }
    }

    for (org.exoplatform.calendar.service.Calendar calendar : publicCalendar) {
      if (checkedCals.contains(calendar.getId())) {
        calendarIds.add(calendar.getId());
      }
    }


    if (shareClas != null) {
      for (org.exoplatform.calendar.service.Calendar cal : shareClas) {
        if (checkedCals.contains(cal.getId())) {
          calendarIds.add(cal.getId());
        }
      }
    }
    
    if (otherClas != null) {
      for (org.exoplatform.calendar.service.Calendar cal : otherClas) {
        if (checkedCals.contains(cal.getId())) {
          calendarIds.add(cal.getId());
        }
      }
    }
    return calendarIds;
  }

  public List<Event> getAllEvents(EventQuery eventQuery) throws Exception {
    String username = CalendarUtils.getCurrentUser();
    eventQuery.setOwner(username);

    List<Event> allEvents = new LinkedList<Event>();
    List<Event> tmp = new LinkedList<Event>();
    if (isDisplaySearchResult()) {
      List<String> publicCalendars  = null;
      if(eventQuery.getCalendarIds() != null && eventQuery.getCalendarIds().length > 0) {
        publicCalendars = new LinkedList<String>();
        List<String> calendarIds = Arrays.asList(eventQuery.getCalendarIds());
        for(String cal : getPublicCalendars()) {
            if(calendarIds.contains(cal)) {
                publicCalendars.add(cal);
            }
        }
      } else {
        publicCalendars  = Arrays.asList(getPublicCalendars());
      }
      eventQuery.setExcludeRepeatEvent(false); 
      tmp.addAll(CalendarUtils.getCalendarService().getEvents(username, convertQuery(eventQuery), publicCalendars.toArray(new String[publicCalendars.size()])));
    } else {
      ListAccess<org.exoplatform.calendar.model.Event> list = xCalService.getEventHandler().findEventsByQuery(eventQuery);
      tmp.addAll(Arrays.asList(list.load(0, -1)));
    }

    CalendarService calendarService = CalendarUtils.getCalendarService();
    CalendarSetting setting = getCalendarSetting();
    recurrenceEventsMap.clear();
    for (Event evt : tmp) {
      if (evt.getRepeatType() != null &&
          !evt.getRepeatType().equals(org.exoplatform.calendar.model.Event.RP_NOREPEAT)) {
        CalendarEvent depEvt = CalendarEvent.build(evt);
        
        Calendar fromDate = CalendarUtils.getCalendarInstanceBySetting(setting);
        if (eventQuery.getFromDate() != null) {
          fromDate.setTimeInMillis(eventQuery.getFromDate());
        } else {
          fromDate.setTime(evt.getFromDateTime());
        }
        Calendar toDate = (Calendar)fromDate.clone();
        if (eventQuery.getToDate() != null) {
          toDate.setTimeInMillis(eventQuery.getToDate());
        } else {
          toDate.add(Calendar.YEAR, 2);
        }
        
        Map<String,CalendarEvent> tempMap = calendarService.getOccurrenceEvents(depEvt, fromDate, toDate, setting.getTimeZone());
        if (tempMap != null) {
          recurrenceEventsMap.put(depEvt.getId(), tempMap);
        }        
      } else {
        allEvents.add(evt);
      }
    }
    
    for (Map<String, CalendarEvent> map : recurrenceEventsMap.values()) {
      allEvents.addAll(map.values());      
    }

    final String orderType = eventQuery.getOrderType();
    final String[] orderBys = eventQuery.getOrderBy();
    if (orderBys != null && orderBys.length > 0) {
      final String orderBy = orderBys[0];
      
      Collections.sort(allEvents, new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
          Event tmp;
          if (Utils.DESCENDING.equals(orderType)) {
            tmp = o1;
            o1 = o2;
            o2 = tmp;
          }
          
          if (UIListView.EVENT_START.equals(orderBy)) {
            return o1.getFromDateTime().compareTo(o2.getFromDateTime());
          } else if (UIListView.EVENT_END.equals(orderBy)) {
            return o1.getToDateTime().compareTo(o2.getToDateTime());
          } else if (UIListView.EVENT_SUMMARY.equals(orderBy)) {
            return o1.getSummary().compareTo(o2.getSummary());
          } else if (UIListView.EVENT_DESCRIPTION.equals(orderBy)) {
            if (o1.getDescription() == null) {
              if (o2.getDescription() == null) {
                return 0;
              } else {
                return -1;
              }
            } else {
              if (o2.getDescription() == null) {
                return 1;
              } else {
                return o1.getDescription().compareTo(o2.getDescription());                
              }
            }
          }
          return 0;
        }
      });      
    } 
    return allEvents;
  }

  private org.exoplatform.calendar.service.EventQuery convertQuery(EventQuery eventQuery) {
    org.exoplatform.calendar.service.EventQuery query = new org.exoplatform.calendar.service.EventQuery();
    query.setCalendarId(eventQuery.getCalendarIds());
    query.setCategoryId(eventQuery.getCategoryIds());
    query.setEventType(eventQuery.getEventType());
    query.setExcludeRepeatEvent(eventQuery.getExcludeRepeatEvent());
    query.setFilterCalendarIds(eventQuery.getFilterCalendarIds());
    if (eventQuery.getFromDate() != null) {
      Calendar fromDate = Calendar.getInstance();
      fromDate.setTimeInMillis(eventQuery.getFromDate());      
      query.setFromDate(fromDate);
    }
    query.setOrderBy(eventQuery.getOrderBy());
    query.setOrderType(eventQuery.getOrderType());
    query.setParticipants(eventQuery.getParticipants());
    query.setPriority(eventQuery.getPriority());
    query.setState(eventQuery.getState());
    query.setText(eventQuery.getText());
    if (eventQuery.getToDate() != null) {
      Calendar toDate = Calendar.getInstance();
      toDate.setTimeInMillis(eventQuery.getToDate());
      query.setToDate(toDate);      
    }
    return query;
  }

  public void update(EventPageList pageList) throws Exception {
    pageList_ = pageList ;
    updateCurrentPage(pageList_.getCurrentPage()) ;
  }

  public EventPageList getPageList() {
    return pageList_;
  }

  public void setPageList(EventPageList pageList) {
    this.pageList_ = pageList;
  }

  protected void updateCurrentPage(long page) throws Exception{
    getChildren().clear() ;
    update();
    UIFormSelectBox uiCategory = getUIFormSelectBox(EVENT_CATEGORIES) ;
    uiCategory.setValue(categoryId_) ;
    uiCategory.setOnChange("Onchange") ;
    eventMap_.clear();
    if(pageList_ != null) {
      for(Object evt : pageList_.getPage(page ,CalendarUtils.getCurrentUser())) {
        Event calendarEvent = (Event)evt;
        String id = calendarEvent.getId();
        if (calendarEvent.getRecurrenceId() != null && (calendarEvent.getIsExceptionOccurrence() == null || !calendarEvent.getIsExceptionOccurrence())
            && isDisplaySearchResult()) {
            id = id + "-" + calendarEvent.getRecurrenceId();
        }
        UICheckBoxInput checkbox = new UICheckBoxInput(id, id, false) ;
        addUIFormInput(checkbox);
        if(getViewType().equals(TYPE_BOTH)){
          eventMap_.put(id, calendarEvent);
        }
        else if(getViewType().equals(calendarEvent.getEventType())) {
          eventMap_.put(id, calendarEvent);
        }
      }
    }
  }

  public Event[] getEvents() throws Exception {
    if(eventMap_.size() == 0){
      return new Event[]{} ;
    }else{
      return eventMap_.values().toArray(new Event[]{}) ;
    }
  }


  protected void refreshBrowser() {
    UIListContainer uiListContainer = getParent() ;
    if (uiListContainer.isDisplaySearchResult()) return ;
    if(!this.isCalClicked()){
      try {
        // When user refreshBrowser: we should check to all Calendar for consistence with other view (MonthView, WeekView...)
        UICalendarPortlet uiPortlet = uiListContainer.getAncestorOfType(UICalendarPortlet.class);
        UICalendars uiCalendars = uiPortlet.findFirstComponentOfType(UICalendars.class);
        uiCalendars.checkAll();

        refresh();
      } catch (Exception e) {
        if(log.isDebugEnabled()){
          log.debug("Exception occurs in freshBrowserListMethod", e);
        }
      }
    }
  }
  public long getAvailablePage(){
    return pageList_.getAvailablePage() ;
  }

  public void setCurrentPage(int page) { currentPage_ = page ;}
  public long getCurrentPage() { return pageList_.getCurrentPage();}
  protected boolean isShowEvent() {return isShowEvent_ ;}

  protected boolean isShowEventAndTask() {return isShowEventAndTask ;}
  public void setShowEventAndTask(boolean show) {isShowEventAndTask = show ;}

  public boolean isDisplaySearchResult() {return isSearchResult ;}
  public void setDisplaySearchResult(boolean show) {isSearchResult = show ;}

  public void setSelectedEvent(String selectedEvent) { this.selectedEvent_ = selectedEvent ; }
  public String getSelectedEvent() { return selectedEvent_ ;}

  public boolean isCalClicked() {
    return calClicked;
  }

  public void setCalClicked(boolean clickChkCalendar) {
    this.calClicked = clickChkCalendar;
  }

  @Override
  public LinkedHashMap<String, Event> getDataMap(){
    return eventMap_ ;
  }
  static public class ViewDetailActionListener extends EventListener<UIListView> {
    @Override
    public void execute(org.exoplatform.webui.event.Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource();
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID);
      Boolean isOccur = false;
      if (!Utils.isEmpty(event.getRequestContext().getRequestParameter(ISOCCUR))) {
        isOccur = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(ISOCCUR));
      }
      String recurId = null, selectedId = eventId;
      if (isOccur) {
        recurId = event.getRequestContext().getRequestParameter(RECURID);      
        selectedId = selectedId + "-" + recurId;
      }
      
      UIListContainer uiListContainer = uiListView.getAncestorOfType(UIListContainer.class);
      UIPreview uiPreview = uiListContainer.getChild(UIPreview.class);
      CalendarEvent calendarEvent = null;
      if(uiListView.getDataMap() != null) {
        if (isOccur && !Utils.isEmpty(recurId)) {
            calendarEvent = uiListView.getRecurrenceMap().get(eventId).get(recurId);
        } else {
            calendarEvent = CalendarEvent.build(uiListView.getDataMap().get(eventId));            
        }
        if(calendarEvent != null) {
          uiListView.setLastUpdatedEventId(eventId) ;
          if(uiListView.isDisplaySearchResult()) {
            uiListView.setSelectedEvent(selectedId);
          } else {
            uiListView.setSelectedEvent(eventId);
          }
          uiPreview.setEvent(calendarEvent);
        } else {
          uiListView.setLastUpdatedEventId(eventId) ;
          uiListView.setSelectedEvent(null) ;
          uiPreview.setEvent(null);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiListContainer);
      }
    }
  }
  public List<CalendarEvent> getSelectedEvents() {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(Event ce : eventMap_.values()) {
      UICheckBoxInput checkbox = getChildById(ce.getId())  ;
      if(checkbox != null && checkbox.isChecked()) events.add(CalendarEvent.build(ce));
    }
    return events ;
  }
  public void setLastViewId(String lastViewId_) {
    this.lastViewId_ = lastViewId_;
  }

  public String getLastViewId() {
    return lastViewId_;
  }
  public void setCategoryId(String catetoryId) {
    categoryId_  = catetoryId ;
    setSelectedCategory(catetoryId) ;
  }
  @Override
  public String getSelectedCategory() {
    return categoryId_ ;
  }
  static public class CloseSearchActionListener extends EventListener<UIListView> {
    @Override
    public void execute(org.exoplatform.webui.event.Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      uiListView.setDisplaySearchResult(false) ;
      uiListView.setCategoryId(null) ;
      uiListView.refresh() ;
      UICalendarPortlet uiPortlet = uiListView.getAncestorOfType(UICalendarPortlet.class) ;
      UISearchForm uiSearchForm = uiPortlet.findFirstComponentOfType(UISearchForm.class) ;
      uiSearchForm.reset() ;
      UIActionBar uiActionBar = uiPortlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionBar.setCurrentView(uiListView.getLastViewId()) ;
      UICalendarViewContainer uiCalViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiCalViewContainer.initView(uiListView.getLastViewId(), false) ;
      uiListView.setLastViewId(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalViewContainer) ;
    }
  }
  static  public class ShowPageActionListener extends EventListener<UIListView> {
    @Override
    public void execute(org.exoplatform.webui.event.Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiListView.currentPage_ = page ;
      uiListView.updateCurrentPage(page) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView.getParent());
    }
  }
  static  public class OnchangeActionListener extends EventListener<UIListView> {
    @Override
    public void execute(org.exoplatform.webui.event.Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      String categoryId = uiListView.getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
      uiListView.setCategoryId(categoryId) ;
      uiListView.refresh() ;
      UIMiniCalendar uiMiniCalendar = uiListView.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      uiMiniCalendar.setCategoryId(categoryId) ;
      UIPreview uiPreview = uiListView.getAncestorOfType(UIListContainer.class).getChild(UIPreview.class) ;
      if(uiListView.getEvents().length >0) {
        uiPreview.setEvent(uiListView.getEvents()[0]) ;
      } else {
        uiPreview.setEvent(null) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView.getParent());
    }
  }
  public Event getSelectedEventObj() {
    return eventMap_.get(selectedEvent_);
  }

  public void setKeyWords(String keyWords) {
    this.keyWords_ = keyWords;
  }

  public String getKeyWords() {
    return keyWords_;
  }
  
  public static class SortActionListener extends EventListener<UIListView> {

    @Override
    public void execute(org.exoplatform.webui.event.Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      long currentPage      = uiListView.getCurrentPage() ;
      String fieldId        = event.getRequestContext().getRequestParameter(OBJECTID) ;
      EventQuery query      = uiListView.getEventQuery();

      List<String> calendarIds = uiListView.findCalendarIds();
      if (calendarIds.size() > 0)
        query.setCalendarIds(calendarIds.toArray(new String[] {}));
      else {
        query.setCalendarIds(new String[] {"null"});
      }
      query.setOrderBy(new String[] {fieldId});
      uiListView.setIsAscending(!uiListView.isAscending());
      uiListView.setSortedField(fieldId);
      if (uiListView.isAscending()) query.setOrderType(Utils.ASCENDING);
      else query.setOrderType(Utils.DESCENDING);

      List<Event> allEvents = uiListView.getAllEvents(query);

      /** re-sorting priority */
      if (fieldId.equals(UIListView.EVENT_PRIORITY)) {
        List<Event> sortedEvents         = new LinkedList<Event>();
        List<Event> nonePriorityEvents   = new ArrayList<Event>();
        List<Event> lowPriorityEvents    = new ArrayList<Event>();
        List<Event> normalPriorityEvents = new ArrayList<Event>();
        List<Event> highPriorityEvents   = new ArrayList<Event>();

        for (Event calendarEvent : allEvents) {
          if (calendarEvent.getPriority().equals("none"))        nonePriorityEvents.add(calendarEvent);
          else if (calendarEvent.getPriority().equals("low"))    lowPriorityEvents.add(calendarEvent);
          else if (calendarEvent.getPriority().equals("normal")) normalPriorityEvents.add(calendarEvent);
          else if (calendarEvent.getPriority().equals("high"))   highPriorityEvents.add(calendarEvent);
        }

        if (uiListView.isAscending()) {
          sortedEvents.addAll(nonePriorityEvents);
          sortedEvents.addAll(lowPriorityEvents);
          sortedEvents.addAll(normalPriorityEvents);
          sortedEvents.addAll(highPriorityEvents);
        }
        else {
          sortedEvents.addAll(highPriorityEvents);
          sortedEvents.addAll(normalPriorityEvents);
          sortedEvents.addAll(lowPriorityEvents);
          sortedEvents.addAll(nonePriorityEvents);
        }

        allEvents = sortedEvents;
      }

      uiListView.update(new EventPageList(allEvents, 10));
      uiListView.updateCurrentPage(currentPage);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView);
    }
  }

  @Override
  public String getDefaultStartTimeOfEvent() {
    return String.valueOf(calendar_.getTimeInMillis());
  }

  @Override
  public void processAction(WebuiRequestContext context) throws Exception {
    this.setCalClicked(true);
    super.processAction(context);
  }

  static  public class AdvancedSearchActionListener extends EventListener<UIListView> {
    public void execute(org.exoplatform.webui.event.Event<UIListView> event) throws Exception {
      UIListView currentView = event.getSource() ;
      UICalendarPortlet calendarPortlet = currentView.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      UIAdvancedSearchForm uiAdvancedSearchForm = popupAction.activate(UIAdvancedSearchForm.class, 600) ;
      uiAdvancedSearchForm.setSearchValue(currentView.getKeyWords()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}

