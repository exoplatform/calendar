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
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.jcr.Node;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.DeleteShareJob;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.ImportCalendarJob;
import org.exoplatform.calendar.service.ModifiedInputStream;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.impl.JobSchedulerServiceImpl;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Encoding;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public class ICalendarImportExport implements CalendarImportExport {
  
  private JCRDataStorage      storage_;

  private static final Log    logger       = ExoLogger.getLogger(ICalendarImportExport.class);

  public ICalendarImportExport(JCRDataStorage storage) throws Exception {
    storage_ = storage;
  }

  public net.fortuna.ical4j.model.Calendar getCalendarComponent(net.fortuna.ical4j.model.Calendar calendar, CalendarEvent exoEvent, String componentType) throws Exception {
    Uid id = new Uid(exoEvent.getId());
    long start = exoEvent.getFromDateTime().getTime();
    long end = exoEvent.getToDateTime().getTime();
    String summary = exoEvent.getSummary();
    CalendarComponent event = null;

    if (end > 0) {
      if (CalendarComponent.VEVENT.equals(componentType)) {
        event = new VEvent(new DateTime(start), new DateTime(end), summary);
      } else if (CalendarComponent.VTODO.equals(componentType)) {
        event = new VToDo(new DateTime(start), new DateTime(end), summary);
      }
    } else {
      if (CalendarComponent.VEVENT.equals(componentType)) {
        event = new VEvent(new DateTime(start), summary);
      } else if (CalendarComponent.VTODO.equals(componentType)) {
        event = new VToDo(new DateTime(start), summary);
      }
    }

    if (event == null)
      return null;

    event.getProperties().getProperty(Property.DTSTART).getParameters().add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);

    event.getProperties().add(new Description(exoEvent.getDescription()));
    event.getProperties().getProperty(Property.DESCRIPTION).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);

    event.getProperties().add(new Location(exoEvent.getLocation()));
    event.getProperties().getProperty(Property.LOCATION).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);

    if (exoEvent.getEventCategoryName() != null) {
      event.getProperties().add(new Categories(exoEvent.getEventCategoryName()));
      event.getProperties().getProperty(Property.CATEGORIES).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    }
    setPriorityCalEvent(event.getProperties(), exoEvent);

    if (CalendarComponent.VTODO.equals(componentType)) {
      if (exoEvent.getCompletedDateTime() != null) {
        long completed = exoEvent.getCompletedDateTime().getTime();
        event.getProperties().add(new Completed(new DateTime(completed)));
        event.getProperties().getProperty(Property.COMPLETED).getParameters().add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
      }
      event.getProperties().add(new Due(new DateTime(end)));
      event.getProperties().getProperty(Property.DUE).getParameters().add(net.fortuna.ical4j.model.parameter.Value.DATE_TIME);
      if (!Utils.isEmpty(exoEvent.getStatus())) {
        event.getProperties().add(new Status(exoEvent.getStatus()));
        event.getProperties().getProperty(Property.STATUS).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);
      }
    }

    if (exoEvent.getAttachment() != null && !exoEvent.getAttachment().isEmpty()) {
      for (Attachment att : exoEvent.getAttachment()) {
        byte bytes[] = new byte[att.getInputStream().available()];
        att.getInputStream().read(bytes);
        ParameterList plist = new ParameterList();
        plist.add(new XParameter(Parameter.CN, att.getName()));
        plist.add(new XParameter(Parameter.FMTTYPE, att.getMimeType()));
        plist.add(Encoding.BASE64);
        plist.add(Value.BINARY);
        Attach attach = new Attach(plist, bytes);
        event.getProperties().add(attach);
      }
    }

    if (exoEvent.getReminders() != null && !exoEvent.getReminders().isEmpty()) {
      for (Reminder r : exoEvent.getReminders()) {
        VAlarm reminder = new VAlarm(new DateTime(r.getFromDateTime()));
        Long times = new Long(1);
        if (r.isRepeat())
          times = (r.getAlarmBefore() / r.getRepeatInterval());
        reminder.getProperties().add(new Repeat(times.intValue()));
        reminder.getProperties().add(new Duration(new Dur(new Long(r.getAlarmBefore()).intValue())));
        if (Reminder.TYPE_POPUP.equals(r.getReminderType())) {
          for (String n : r.getReminderOwner().split(Utils.COMMA)) {
            Attendee a = new Attendee(n);
            reminder.getProperties().add(a);
          }
          reminder.getProperties().add(Action.DISPLAY);
        } else {
          for (String m : r.getEmailAddress().split(Utils.COMMA)) {
            Attendee a = new Attendee(m);
            reminder.getProperties().add(a);
          }
          reminder.getProperties().add(Action.EMAIL);
        }
        reminder.getProperties().add(new Summary(exoEvent.getSummary()));
        reminder.getProperties().add(new Description(r.getDescription()));
        reminder.getProperties().add(id);
        calendar.getComponents().add(reminder);
      }
    }
    if (exoEvent.isPrivate())
      event.getProperties().add(new Clazz(Clazz.PRIVATE.getValue()));
    else
      event.getProperties().add(new Clazz(Clazz.PUBLIC.getValue()));
    event.getProperties().getProperty(Property.CLASS).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    String[] attendees = exoEvent.getInvitation();
    if (attendees != null && attendees.length > 0) {
      for (int i = 0; i < attendees.length; i++) {
        if (attendees[i] != null) {
          event.getProperties().add(new Attendee(attendees[i]));
        }
      }
      event.getProperties().getProperty(Property.ATTENDEE).getParameters().add(net.fortuna.ical4j.model.parameter.Value.TEXT);
    }
    if (!Utils.isEmpty(exoEvent.getRepeatType())) {
      Recur rc = null;
      if (CalendarEvent.RP_NOREPEAT.equalsIgnoreCase(exoEvent.getRepeatType())) {
      } else if (CalendarEvent.RP_WEEKEND.equalsIgnoreCase(exoEvent.getRepeatType())) {
        rc = new Recur(Recur.WEEKLY, 1);
        rc.getDayList().add(WeekDay.SU);
        rc.getDayList().add(WeekDay.SA);
        rc.setInterval(1);
      } else if (CalendarEvent.RP_WORKINGDAYS.equalsIgnoreCase(exoEvent.getRepeatType())) {
        rc = new Recur(Recur.WEEKLY, 1);
        rc.getDayList().add(WeekDay.MO);
        rc.getDayList().add(WeekDay.TU);
        rc.getDayList().add(WeekDay.WE);
        rc.getDayList().add(WeekDay.TH);
        rc.getDayList().add(WeekDay.FR);
        rc.setInterval(1);
      } else if (CalendarEvent.RP_WEEKLY.equalsIgnoreCase(exoEvent.getRepeatType())) {
        rc = new Recur(Recur.WEEKLY, 1);
        rc.getDayList().add(WeekDay.SU);
        rc.getDayList().add(WeekDay.MO);
        rc.getDayList().add(WeekDay.TU);
        rc.getDayList().add(WeekDay.WE);
        rc.getDayList().add(WeekDay.TH);
        rc.getDayList().add(WeekDay.FR);
        rc.getDayList().add(WeekDay.SA);
        rc.setInterval(1);
      } else {
        rc = new Recur(exoEvent.getRepeatType().toUpperCase(), 1);
        rc.setInterval(1);
      }
      if (rc != null) {
        rc.setWeekStartDay(WeekDay.SU.getDay());
        RRule r = new RRule(rc);
        event.getProperties().add(r);
      }
    }
    if (!Utils.isEmpty(exoEvent.getEventState())) {
      XProperty xProperty = new XProperty(Utils.X_STATUS, exoEvent.getEventState());
      event.getProperties().add(xProperty);
    }
    event.getProperties().add(id);
    calendar.getComponents().add(event);
    return calendar;
  }

  public OutputStream exportCalendar(String username, List<String> calendarIds, String type, int limited) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    if (type.equals(PRIVATE_TYPE)) {
      if (limited > 0) {
        EventQuery eventQuery = new EventQuery();
        eventQuery.setCalendarId(calendarIds.toArray(new String[] {}));
        eventQuery.setOrderBy(new String[] { Utils.JCR_LASTMODIFIED });
        eventQuery.setLimitedItems(limited);
        events = storage_.getEvents(username, eventQuery, null);
      } else {
        events = storage_.getUserEventByCalendar(username, calendarIds);
      }
    } else if (type.equals(SHARED_TYPE)) {
      events = storage_.getSharedEventByCalendars(username, calendarIds);
    } else if (type.equals(PUBLIC_TYPE)) {
      events = storage_.getGroupEventByCalendar(calendarIds);
    }
    if (events.isEmpty())
      return null;
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties().add(Method.REQUEST);
    for (CalendarEvent exoEvent : events) {
      if (exoEvent.getEventType().equals(CalendarEvent.TYPE_EVENT)) {
        calendar = getCalendarComponent(calendar, exoEvent, CalendarComponent.VEVENT);
      } else { // task
        calendar = getCalendarComponent(calendar, exoEvent, CalendarComponent.VTODO);
      }
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, bout);
    } catch (ValidationException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Validate error", e);
      }
      return null;
    }
    return bout;
  }

  public OutputStream exportEventCalendar(String username, String calendarId, String type, String eventId) throws Exception {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    List<String> calendarIds = Arrays.asList(new String[] { calendarId });

    if (type.equals(PRIVATE_TYPE)) {
      events = storage_.getUserEventByCalendar(username, calendarIds);
    } else if (type.equals(SHARED_TYPE)) {
      events = storage_.getSharedEventByCalendars(username, calendarIds);
    } else if (type.equals(PUBLIC_TYPE)) {
      events = storage_.getGroupEventByCalendar(calendarIds);
    }
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties().add(Method.REQUEST);
    for (CalendarEvent exoEvent : events) {
      if (exoEvent.getId().equals(eventId)) {
        if (exoEvent.getEventType().equals(CalendarEvent.TYPE_EVENT)) {
          calendar = getCalendarComponent(calendar, exoEvent, CalendarComponent.VEVENT);
        } else {
          calendar = getCalendarComponent(calendar, exoEvent, CalendarComponent.VTODO);
        }
        break;
      }
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, bout);
    } catch (ValidationException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Validate error", e);
      }
      return null;
    }
    return bout;
  }


  public List<CalendarEvent> getEventObjects(InputStream icalInputStream) throws Exception {
    CalendarBuilder calendarBuilder = new CalendarBuilder();
    net.fortuna.ical4j.model.Calendar iCalendar = calendarBuilder.build(icalInputStream);
    ComponentList componentList = iCalendar.getComponents();
    List<CalendarEvent> eventList = new ArrayList<CalendarEvent>();
    VEvent event;
    for (Object obj : componentList) {
      if (obj instanceof VEvent) {
        CalendarEvent exoEvent = new CalendarEvent();
        event = (VEvent) obj;
        if (event.getProperty(Property.UID) != null) {
          exoEvent.setId(event.getProperty(Property.UID).getValue());
        }
        if (event.getProperty(Property.CATEGORIES) != null) {
          exoEvent.setEventCategoryName(event.getProperty(Property.CATEGORIES).getValue().trim());
        }
        if (event.getSummary() != null)
          exoEvent.setSummary(event.getSummary().getValue());
        if (event.getDescription() != null)
          exoEvent.setDescription(event.getDescription().getValue());
        if (event.getStatus() != null)
          exoEvent.setStatus(event.getStatus().getValue());
        exoEvent.setEventType(CalendarEvent.TYPE_EVENT);
        if (event.getStartDate() != null)
          exoEvent.setFromDateTime(event.getStartDate().getDate());
        if (event.getEndDate() != null)
          exoEvent.setToDateTime(event.getEndDate().getDate());
        if (event.getLocation() != null)
          exoEvent.setLocation(event.getLocation().getValue());
        logger.info(event.getPriority());
        setPriorityExoEvent(event.getPriority(), exoEvent);
        try {
          RRule r = (RRule) event.getProperty(Property.RRULE);
          if (r != null && r.getRecur() != null) {
            Recur rc = r.getRecur();
            rc.getFrequency();
            if (Recur.WEEKLY.equalsIgnoreCase(rc.getFrequency())) {
              if (rc.getDayList().size() == 2) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKEND);
              } else if (rc.getDayList().size() == 5) {
                exoEvent.setRepeatType(CalendarEvent.RP_WORKINGDAYS);
              }
              if (rc.getDayList().size() == 7) {
                exoEvent.setRepeatType(CalendarEvent.RP_WEEKLY);
              }
            } else {
              exoEvent.setRepeatType(rc.getFrequency().toLowerCase());
            }
          }
        } catch (Exception e) {
          if (logger.isDebugEnabled()) {
            logger.debug("Exception in method getEventObjects", e);
          }
        }
        exoEvent.setPrivate(true);
        PropertyList attendees = event.getProperties(Property.ATTENDEE);
        if (attendees.size() < 1) {
          exoEvent.setInvitation(new String[] {});
        } else {
          String[] invitation = new String[attendees.size()];
          for (int i = 0; i < attendees.size(); i++) {
            invitation[i] = ((Attendee) attendees.get(i)).getValue();
          }
          exoEvent.setInvitation(invitation);
        }
        eventList.add(exoEvent);
      }
    }
    return eventList;
  }

  public boolean isValidate(InputStream icalInputStream) {
    try {
      CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
      CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
      CalendarBuilder calendarBuilder = new CalendarBuilder();
      calendarBuilder.build(icalInputStream);
      return true;
    } catch (ParserException pe) {
      if (logger.isDebugEnabled()) 
        logger.debug("Can not parsing input stream to calendar under ICal format", pe);
    } catch (IOException ioe) {
      if (logger.isDebugEnabled()) 
        logger.debug("IO Error occurs when parsing input stream to calendar under ICal format", ioe);
    }
    return false;
  }

  public void importCalendar(String username,
                              InputStream icalInputStream,
                              String calendarId,
                              String calendarName,
                              java.util.Calendar from,
                              java.util.Calendar to,
                              boolean isNew) throws Exception {
    CalendarBuilder calendarBuilder = new CalendarBuilder();
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
    net.fortuna.ical4j.model.Calendar iCalendar;
    try {
      InputStream modStream = ModifiedInputStream.getIcsModifiedStream(icalInputStream);
      iCalendar = calendarBuilder.build(modStream);
    } catch (ParserException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("ParserException occurs when building iCalendar object", e);
      }
      throw new ParserException("Cannot parsed the input stream to iCalendar object", e.getLineNo());
    } catch (IOException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("IOException occurs when building iCalendar object", e);
      }
      throw new IOException("IOException when parsing input stream to iCalendar object");
    } finally {
      icalInputStream.close();
    }

    CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);


    if (isNew) {
      Calendar exoCalendar = new Calendar();
      exoCalendar.setName(calendarName);
      exoCalendar.setCalendarColor(org.exoplatform.calendar.service.Calendar.COLORS[0]);
      exoCalendar.setDescription(iCalendar.getProductId().getValue());
      exoCalendar.setPublic(false);
      exoCalendar.setCalendarOwner(username);
      calService.saveUserCalendar(username, exoCalendar, true);
      calendarId = exoCalendar.getId();
    }
    CalendarEvent exoEvent;
    ComponentList componentList = iCalendar.getComponents();

    Map<String, VFreeBusy> vFreeBusyData = new HashMap<String, VFreeBusy>();
    Map<String, VAlarm> vAlarmData = new HashMap<String, VAlarm>();

    Map<String, String> originalRecurrenceEvents = new HashMap<String, String>();
    Map<String, List<CalendarEvent>> exceptionOccurrences = new HashMap<String, List<CalendarEvent>>();

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    int calType = storage_.getTypeOfCalendar(username, calendarId);

    for (Object obj : componentList) {
      if (obj instanceof VEvent) {
        VEvent v = (VEvent) obj;
        if (!v.getAlarms().isEmpty()) {
          for (Object o : v.getAlarms()) {
            if (o instanceof VAlarm) {
              VAlarm va = (VAlarm) o;
              vAlarmData.put(v.getUid().getValue() + ":" + va.getProperty(Property.ACTION).getName(), va);
            }
          }
        }
      }
      if (obj instanceof VFreeBusy)
        vFreeBusyData.put(((VFreeBusy) obj).getUid().getValue(), (VFreeBusy) obj);
    }

    for (Object obj : componentList) {
      if (obj instanceof VEvent) {
        VEvent event = (VEvent) obj;
        exoEvent = new CalendarEvent();
        try {
          exoEvent = RemoteCalendarServiceImpl.generateEvent(event, exoEvent, username, calendarId);

          String sValue = Utils.EMPTY_STR;
          String eValue = Utils.EMPTY_STR;
          if (event.getStartDate() != null) {
            if (to != null && event.getStartDate().getDate().getTime() > to.getTimeInMillis()) {
              continue;
            }
            sValue = event.getStartDate().getValue();
            exoEvent.setFromDateTime(event.getStartDate().getDate());
          }
          if (event.getEndDate() != null) {
            if (from != null && event.getEndDate().getDate().getTime() < from.getTimeInMillis()) {
              continue;
            }
            eValue = event.getEndDate().getValue();
            exoEvent.setToDateTime(event.getEndDate().getDate());
          }
          if (sValue.length() == 8 && eValue.length() == 8) {
            exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() - 1));
          }
          if (vFreeBusyData.get(event.getUid().getValue()) != null) {
            exoEvent.setEventState(CalendarEvent.ST_BUSY);
          }
          exoEvent = RemoteCalendarServiceImpl.setEventAttachment(event, exoEvent,eValue,sValue);
          exoEvent.setRepeatType(CalendarEvent.RP_NOREPEAT);
          if (event.getProperty(Property.RECURRENCE_ID) != null) {
            RecurrenceId recurId = (RecurrenceId) event.getProperty(Property.RECURRENCE_ID);
            exoEvent.setRecurrenceId(format.format(new Date(recurId.getDate().getTime())));
            String originalId = originalRecurrenceEvents.get(event.getUid().getValue());
            if (originalId != null) {
              Node originalNode = storage_.getCalendarEventNode(username, String.valueOf(calType), calendarId, originalId);
              CalendarEvent original = storage_.getEvent(originalNode);
              String uuid = originalNode.getUUID();
              exoEvent.setOriginalReference(uuid);

              List<String> excludeId;
              if (original.getExcludeId() != null && original.getExcludeId().length > 0) {
                excludeId = new ArrayList<String>(Arrays.asList(original.getExcludeId()));
              } else {
                excludeId = new ArrayList<String>();
              }
              excludeId.add(exoEvent.getRecurrenceId());
              original.setExcludeId(excludeId.toArray(new String[0]));
              if (calType == Calendar.TYPE_PRIVATE)
                storage_.saveUserEvent(username, calendarId, original, false);
              else if (calType == Calendar.TYPE_PUBLIC)
                storage_.savePublicEvent(calendarId, original, false);
              else if (calType == Calendar.TYPE_SHARED)
                storage_.saveEventToSharedCalendar(username, calendarId, original, false);

            } else {
              if (exceptionOccurrences.get(event.getUid().getValue()) == null) {
                List<CalendarEvent> exceptions = new ArrayList<CalendarEvent>();
                exceptions.add(exoEvent);
                exceptionOccurrences.put(event.getUid().getValue(), exceptions);
              } else {
                exceptionOccurrences.get(event.getUid().getValue()).add(exoEvent);
              }
            }
            storage_.saveOccurrenceEvent(username, calendarId, exoEvent, true);
          } else {
            if (event.getProperty(Property.RRULE) != null && event.getProperty(Property.RECURRENCE_ID) == null) {
              exoEvent = RemoteCalendarServiceImpl.calculateEvent(event, exoEvent);

              originalRecurrenceEvents.put(event.getUid().getValue(), exoEvent.getId());

              List<String> excludeIds = new ArrayList<String>();
              PropertyList exdates = event.getProperties(Property.EXDATE);
              if (exdates != null && exdates.size() > 0) {
                for (Object exdate : exdates) {
                  for (Object date : ((ExDate) exdate).getDates()) {
                    excludeIds.add(format.format(new Date(((net.fortuna.ical4j.model.DateTime) date).getTime())));
                  }
                }
              }

              List<CalendarEvent> exceptions = exceptionOccurrences.get(event.getUid().getValue());
              if (exceptions != null && exceptions.size() > 0) {
                for (CalendarEvent exception : exceptions) {
                  excludeIds.add(exception.getRecurrenceId());
                }
              }
              exoEvent.setExcludeId(excludeIds.toArray(new String[0]));

              if (calType == Utils.PRIVATE_TYPE)
                storage_.saveUserEvent(username, calendarId, exoEvent, true);
              else if (calType == Utils.SHARED_TYPE)
                storage_.saveEventToSharedCalendar(username, calendarId, exoEvent, true);
              else if (calType == Utils.PUBLIC_TYPE)
                storage_.savePublicEvent(calendarId, exoEvent, true);

              Node originalNode = storage_.getCalendarEventNode(username, String.valueOf(calType), calendarId, exoEvent.getId());
              String uuid = originalNode.getUUID();
              if (exceptions != null && exceptions.size() > 0) {
                for (CalendarEvent exception : exceptions) {
                  exception.setOriginalReference(uuid);
                  storage_.saveOccurrenceEvent(username, calendarId, exception, false);
                }
              }
            } else {
              if (calType == Utils.PRIVATE_TYPE)
                storage_.saveUserEvent(username, calendarId, exoEvent, true);
              else if (calType == Utils.SHARED_TYPE)
                storage_.saveEventToSharedCalendar(username, calendarId, exoEvent, true);
              else if (calType == Utils.PUBLIC_TYPE)
                storage_.savePublicEvent(calendarId, exoEvent, true);
            }
          }
        } catch (Exception e) {
          if (logger.isDebugEnabled()) {
            logger.debug("Exception occurs when importing iCalendar component: " + event.getUid().getValue() + ". Skip this component.", e);
          }
          continue;
        }
      }

      else if (obj instanceof VToDo) {
        VToDo event = (VToDo) obj;
        exoEvent = new CalendarEvent();
        try {
          exoEvent = RemoteCalendarServiceImpl.setTaskAttachment(event, exoEvent,username,calendarId,vFreeBusyData);
          switch (storage_.getTypeOfCalendar(username, calendarId)) {
          case Utils.PRIVATE_TYPE:
            calService.saveUserEvent(username, calendarId, exoEvent, true);
            break;
          case Utils.SHARED_TYPE:
            calService.saveEventToSharedCalendar(username, calendarId, exoEvent, true);
            break;
          case Utils.PUBLIC_TYPE:
            calService.savePublicEvent(calendarId, exoEvent, true);
            break;
          }
        } catch (Exception e) {
          if (logger.isDebugEnabled()) {
            logger.debug("Exception occurs when importing iCalendar VTODO component: " + event.getUid().getValue() + ". Skip this component.", e);
          }
          continue;
        }
      }
    }

  }

  @Override
  public ByteArrayOutputStream exportEventCalendar(CalendarEvent exoEvent) throws Exception {
    net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
    calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
    calendar.getProperties().add(Version.VERSION_2_0);
    calendar.getProperties().add(CalScale.GREGORIAN);
    calendar.getProperties().add(Method.REQUEST);
    if (exoEvent.getEventType().equals(CalendarEvent.TYPE_EVENT)) {
      calendar = getCalendarComponent(calendar, exoEvent, CalendarComponent.VEVENT);
    } else {
      calendar = getCalendarComponent(calendar, exoEvent, CalendarComponent.VTODO);
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    CalendarOutputter output = new CalendarOutputter();
    try {
      output.output(calendar, bout);
    } catch (ValidationException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Validate error", e);
      }
      return null;
    }
    return bout;
  }
  
  public static void setPriorityCalEvent(PropertyList propertyList, CalendarEvent exoEvent) {
    if (exoEvent.getPriority() != null) {
      int priority = 0;
      for (int i = 0; i < CalendarEvent.PRIORITY.length; i++) {
        if (exoEvent.getPriority().equalsIgnoreCase(CalendarEvent.PRIORITY[i])) {
          if (i == CalendarEvent.PRI_MEDIUM) {
            priority = 5;
          } else if (i == CalendarEvent.PRI_LOW) {
            priority = 6;
          } else {
            priority = i;
          }
          propertyList.add(new Priority(priority));
          propertyList.getProperty(Property.PRIORITY).getParameters()
                      .add(net.fortuna.ical4j.model.parameter.Value.INTEGER);
          break;
        }
      }
    }
  }

  public static void setPriorityExoEvent(Priority priority, CalendarEvent exoEvent) {
    if (priority != null) {
      if (priority.getValue() != null) {
        int priorityVl = 0;
        try {
          priorityVl = Integer.parseInt(priority.getValue());
          if (1 < priorityVl && priorityVl <= 4) {
            priorityVl = CalendarEvent.PRI_HIGH;
          } else if (priorityVl == 5) {
            priorityVl = CalendarEvent.PRI_MEDIUM;
          } else if (priorityVl > 5) {
            priorityVl = CalendarEvent.PRI_LOW;
          }
          exoEvent.setPriority(CalendarEvent.PRIORITY[priorityVl]);
        } catch (NumberFormatException e) {
          logger.warn("Can not set the priority of this event");
        }
      }
    }
  }
  
  public void importCalendarByJob(String username,
                                  InputStream icalInputStream,
                                  String calendarId, String calendarName,
                                  java.util.Calendar from, java.util.Calendar to,
                                  boolean isNew) throws Exception {
    JobSchedulerServiceImpl  schedulerService = (JobSchedulerServiceImpl)ExoContainerContext.getCurrentContainer().
        getComponentInstance(JobSchedulerService.class) ;

    JobDetail job = findImportJob(schedulerService, calendarId);

    if(job == null) {
      job = ImportCalendarJob.getImportICSFileJobDetail(username, calendarId, calendarName, icalInputStream, from, to, isNew);
    }

    SimpleTriggerImpl trigger = new SimpleTriggerImpl();
    trigger.setName(calendarId);
    trigger.setGroup(ImportCalendarJob.IMPORT_CALENDAR_JOB_GROUP_NAME);
    trigger.setStartTime(new Date());

    schedulerService.addJob(job, trigger);  
  }

  public JobDetail findImportJob(JobSchedulerService schedulerService, String calendarId) throws Exception {
    List<JobDetail> listJobs = schedulerService.getAllJobs();
    for(JobDetail job : listJobs) {
      if(job.getKey().getName().equals(calendarId)) {
        return job;
      }
    }
    return null;
  }
}
