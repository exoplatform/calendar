/**
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.calendar.service.impl;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.QueryManager;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.model.DateTime;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventDAO;
import org.exoplatform.calendar.service.EventPageListQuery;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Invitation;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ActivityTypeUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EventDAOImpl implements EventDAO {

  private static Log      log = ExoLogger.getLogger(EventDAOImpl.class);

  private CalendarService calService;

  private JCRDataStorage  storage;
  
  private String pubCalendarQuery;

  public EventDAOImpl(CalendarService calService, JCRDataStorage storage_) {
    this.calService = calService;
    this.storage = storage_;
    
    //Remove this after we have CalendarDAO that support to find all public calendars
    StringBuilder stm = new StringBuilder("SELECT ").append(Utils.EXO_ID).append(" FROM ");
    stm.append(Utils.EXO_CALENDAR).append(" WHERE ").append(Utils.EXO_PUBLIC_URL);
    stm.append(" IS NOT NULL");
    pubCalendarQuery = stm.toString();
  }

  @Override
  public ListAccess<CalendarEvent> findEventsByQuery(EventQuery eventQuery) throws CalendarException {
    return new EventList(new EventNodeListAccess(this, eventQuery));
  }
  
  @Override
  public ListAccess<Invitation> findInvitationsByQuery(EventQuery query) {
    return new InvitationListAccess(this, query);
  }

  @Override
  public Invitation getInvitationById(String invitationID) throws CalendarException {
    String[] tmp = Invitation.parse(invitationID);
    String eventId = tmp[0];
    String participant = tmp[1];

    CalendarEvent event = null;
    try {
      event = calService.getEventById(eventId);
    } catch (Exception e) {
      log.error("error during get event: {}, exeption: {}", eventId, e.getMessage());
      throw new CalendarException(null, e);
    }

    if (event != null) {
      Invitation[] invitations = event.getInvitations();
      for (Invitation invite : invitations) {
        if (invite.getParticipant().equals(participant)) {
          return invite;
        }
      }
    } else {
      log.debug("Can't find invitation due to event not found: {}", invitationID);
    }
    return null;
  }

  @Override
  public void removeInvitation(String invitationId) throws CalendarException {
    String[] tmp = Invitation.parse(invitationId);
    String eventId = tmp[0];
    String participant = tmp[1];

    CalendarEvent event = null;
    try {
      event = calService.getEventById(eventId);
    } catch (Exception e) {
      log.error("Can't remove Invitation, there is error during get event {}", eventId);
      throw new CalendarException(null, e);
    }
    
    if (event != null) {
      event.removeParticipant(participant);
      
      try {
        this.saveEvent(participant, event);
      } catch (Exception e) {
        log.error("Can't remove invitation, there is error during saving event {}", e);
        throw new CalendarException(null, e);
      }
    }
  }

  @Override
  public void updateInvitation(String id, String status) {
    String[] tmp = Invitation.parse(id);
    String eventId = tmp[0];
    String participant = tmp[1];

    CalendarEvent event = null;
    try {
      event = calService.getEventById(eventId);
    } catch (Exception e) {
      log.error("Can't update invitation due to can't find event {}", eventId);
      throw new CalendarException(null, e);
    }
    
    if (event != null) {
      Map<String, String> statusMap = event.getStatusMap();
      String currStatus = statusMap.get(participant);

      if (currStatus != null && !currStatus.equals(status)) {
        statusMap.put(participant, status);
        event.setStatusMap(statusMap);

        try {
          saveEvent(participant, event);
        } catch (Exception e) {
          log.error("Can't update invitation. There is error event {}", eventId);
          throw new CalendarException(null, e);
        }
      }
    }    
  }

  @Override
  public Invitation createInvitation(String eventId, String participant, String status) throws CalendarException {
    CalendarEvent event;
    try {
      event = calService.getEventById(eventId);
    } catch (Exception e) {
      log.error("Can't create invitation. There is error duing getting event: {}", eventId);
      throw new CalendarException(null, e);
    }

    if (event != null && event.getStatusMap().get(participant) == null) {
      event.addParticipant(participant, status);
      
      try {
        saveEvent(participant, event);
        return new Invitation(eventId, participant, status);
      } catch (Exception e) {
        log.error("Can't create invitation. There is error during saving event: {}",  eventId);
        throw new CalendarException(null, e);
      }
    }
    return null;
  }
  
  private void saveEvent(String username, CalendarEvent event) throws Exception {
    String calendarId = event.getCalendarId();
    Calendar cal = calService.getCalendarById(calendarId);
    String owner = cal.getCalendarOwner();
    int type = calService.getTypeOfCalendar(owner, calendarId);

    switch (type) {
    case Calendar.TYPE_PRIVATE:
      calService.saveUserEvent(owner, calendarId, event, false);
      break;
    case Calendar.TYPE_PUBLIC:
      calService.savePublicEvent(calendarId, event, false);
      break;
    case Calendar.TYPE_SHARED:
      calService.saveEventToSharedCalendar(username, calendarId, event, false);
    }
  }
  
  public QueryImpl createJCRQuery(String queryStm, String queryType) throws RepositoryException {
    QueryManager qm = getSession().getWorkspace().getQueryManager();
    return (QueryImpl)qm.createQuery(queryStm, queryType);
  }

  public CalendarEvent getEventFromNode(Node eventNode) throws RepositoryException {
    CalendarEvent event = new CalendarEvent();
    try {
      event = EventPageListQuery.getEventFromNode(event,
                                                  eventNode,
                                                  getReminderFolder(eventNode.getProperty(Utils.EXO_FROM_DATE_TIME)
                                                                             .getDate()
                                                                             .getTime()));
    } catch (Exception e) {
      log.error("Error during mapping node to CalendarEvent", e);
      return null;
    }
    StringBuilder namePattern = new StringBuilder(128);
    namePattern.append(Utils.EXO_RECURRENCE_ID)
               .append('|')
               .append(Utils.EXO_IS_EXCEPTION)
               .append('|')
               .append(Utils.EXO_REPEAT_UNTIL)
               .append('|')
               .append(Utils.EXO_REPEAT_COUNT)
               .append('|')
               .append(Utils.EXO_ORIGINAL_REFERENCE)
               .append('|')
               .append(Utils.EXO_REPEAT_INTERVAL)
               .append('|')
               .append(Utils.EXO_EXCLUDE_ID)
               .append('|')
               .append(Utils.EXO_REPEAT_BYDAY)
               .append('|')
               .append(Utils.EXO_REPEAT_BYMONTHDAY);
    PropertyIterator it = eventNode.getProperties(namePattern.toString());
    while (it.hasNext()) {
      Property p = it.nextProperty();
      String name = p.getName();
      if (name.equals(Utils.EXO_RECURRENCE_ID)) {
        event.setRecurrenceId(p.getString());
      } else if (name.equals(Utils.EXO_IS_EXCEPTION)) {
        event.setIsExceptionOccurrence(p.getBoolean());
      } else if (name.equals(Utils.EXO_REPEAT_UNTIL)) {
        event.setRepeatUntilDate(p.getDate().getTime());
      } else if (name.equals(Utils.EXO_REPEAT_COUNT)) {
        event.setRepeatCount(p.getLong());
      } else if (name.equals(Utils.EXO_ORIGINAL_REFERENCE)) {
        event.setOriginalReference(p.getString());
      } else if (name.equals(Utils.EXO_REPEAT_INTERVAL)) {
        event.setRepeatInterval(p.getLong());
      } else if (name.equals(Utils.EXO_EXCLUDE_ID)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setExcludeId(new String[] { values[0].getString() });
        } else {
          String[] excludeIds = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            excludeIds[i] = values[i].getString();
          }
          event.setExcludeId(excludeIds);
        }
      } else if (name.equals(Utils.EXO_REPEAT_BYDAY)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setRepeatByDay(new String[] { values[0].getString() });
        } else {
          String[] byDays = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            byDays[i] = values[i].getString();
          }
          event.setRepeatByDay(byDays);
        }
      } else if (name.equals(Utils.EXO_REPEAT_BYMONTHDAY)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setRepeatByMonthDay(new long[] { values[0].getLong() });
        } else {
          long[] byMonthDays = new long[values.length];
          for (int i = 0; i < values.length; i++) {
            byMonthDays[i] = values[i].getLong();
          }
          event.setRepeatByMonthDay(byMonthDays);
        }
      }
    }

    String activitiId = ActivityTypeUtils.getActivityId(eventNode);
    if (activitiId != null) {
      event.setActivityId(ActivityTypeUtils.getActivityId(eventNode));
    }
    return event;
  }
  
  public class EventList implements ListAccess<CalendarEvent> {

    private EventNodeListAccess list;
    
    public EventList(EventNodeListAccess list) {
      this.list = list;
    }

    @Override
    public int getSize() throws Exception {
      return list.getSize();
    }

    @Override
    public CalendarEvent[] load(int offset, int limit) throws Exception, IllegalArgumentException {
      Node[] results = list.load(offset, limit);
      List<CalendarEvent> events = new LinkedList<CalendarEvent>();
      for (Node node : results) {
        events.add(getEventFromNode(node));
      }
      
      return events.toArray(new CalendarEvent[events.size()]);
    }
    
  }

  private Node getReminderFolder(Date fromDate) throws Exception {
    Node publicApp = storage.getPublicCalendarServiceHome();
    Node dateFolder = getDateFolder(publicApp, fromDate);
    try {
      return dateFolder.getNode(Utils.CALENDAR_REMINDER);
    } catch (PathNotFoundException pnfe) {
      try {
        dateFolder.addNode(Utils.CALENDAR_REMINDER, Utils.NT_UNSTRUCTURED);
        if (dateFolder.isNew())
          dateFolder.getSession().save();
        else
          dateFolder.save();
      } catch (Exception e) {
        dateFolder.refresh(false);
      }
      return dateFolder.getNode(Utils.CALENDAR_REMINDER);
    }
  }

  private Node getDateFolder(Node publicApp, Date date) throws Exception {
    if (date instanceof DateTime) {
      date = new Date(date.getTime());
    }
    java.util.Calendar fromCalendar = Utils.getInstanceTempCalendar();
    fromCalendar.setTime(date);
    Node yearNode;
    Node monthNode;
    String year = "Y" + String.valueOf(fromCalendar.get(java.util.Calendar.YEAR));
    String month = "M" + String.valueOf(fromCalendar.get(java.util.Calendar.MONTH) + 1);
    String day = "D" + String.valueOf(fromCalendar.get(java.util.Calendar.DATE));
    try {
      yearNode = publicApp.getNode(year);
    } catch (PathNotFoundException e) {
      yearNode = publicApp.addNode(year, Utils.NT_UNSTRUCTURED);
    }
    try {
      monthNode = yearNode.getNode(month);
    } catch (PathNotFoundException e) {
      monthNode = yearNode.addNode(month, Utils.NT_UNSTRUCTURED);
    }
    try {
      return monthNode.getNode(day);
    } catch (PathNotFoundException e) {
      return monthNode.addNode(day, Utils.NT_UNSTRUCTURED);
    }
  }

  private Session getSession() {
    Session session = null;
    try {
      session = storage.getSession(storage.createSessionProvider());
    } catch (Exception ex) {
    }
    return session;
  }
}
