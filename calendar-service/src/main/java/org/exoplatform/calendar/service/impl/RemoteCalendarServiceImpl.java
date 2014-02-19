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
package org.exoplatform.calendar.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.RemoteCalendar;
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * exo@exoplatform.com
 * Jan 10, 2011
 */
public class RemoteCalendarServiceImpl implements RemoteCalendarService {

  private static final Namespace CALDAV_NAMESPACE             = Namespace.getNamespace("C", "urn:ietf:params:xml:ns:caldav");

  private static final String    CALDAV_XML_CALENDAR_MULTIGET = "calendar-multiget";

  private static final String    CALDAV_XML_CALENDAR_QUERY    = "calendar-query";

  private static final String    CALDAV_XML_CALENDAR_DATA     = "calendar-data";

  private static final String    CALDAV_XML_FILTER            = "filter";

  private static final String    CALDAV_XML_COMP_FILTER       = "comp-filter";

  private static final String    CALDAV_XML_TIME_RANGE        = "time-range";

  private static final String    CALDAV_XML_START             = "start";

  private static final String    CALDAV_XML_END               = "end";

  private static final String    CALDAV_XML_COMP_FILTER_NAME     = "name";

  public static final String     ICAL_PROPS_CALENDAR_NAME        = "X-WR-CALNAME";
  
  public static final String    ICAL_PROPS_CALENDAR_DESCRIPTION = "X-WR-CALDESC";
  
  private static final Log       logger                       = ExoLogger.getLogger(RemoteCalendarServiceImpl.class);

  private JCRDataStorage         storage_;

  public RemoteCalendarServiceImpl(JCRDataStorage storage) {
    this.storage_ = storage;
  }
  
  @Override
  public InputStream connectToRemoteServer(RemoteCalendar remoteCalendar) throws Exception {
    HttpClient client = getRemoteClient(remoteCalendar);
    GetMethod get = new GetMethod(remoteCalendar.getRemoteUrl());
    try {
      client.executeMethod(get);
      InputStream icalInputStream = get.getResponseBodyAsStream();
      return icalInputStream;
    } catch (IOException e) {
      if (logger.isDebugEnabled()) 
        logger.debug(String.format("Connect to %s failed!", remoteCalendar.getRemoteUrl()), e);
      throw e;
    }
  }

  @Override
  public boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws IOException, UnsupportedOperationException {
    try {
      HttpClient client = new HttpClient();
      HostConfiguration hostConfig = new HostConfiguration();
      String host = new URL(url).getHost();
      if (StringUtils.isEmpty(host))
        host = url;
      hostConfig.setHost(host);
      client.setHostConfiguration(hostConfig);
      Credentials credentials = null;
      client.setHostConfiguration(hostConfig);
      if (!StringUtils.isEmpty(remoteUser)) {
        credentials = new UsernamePasswordCredentials(remoteUser, remotePassword);
        client.getState().setCredentials(new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
      }

      if (CalendarService.ICALENDAR.equals(type)) {
        GetMethod get = new GetMethod(url);
        client.executeMethod(get);
        int statusCode = get.getStatusCode();
        get.releaseConnection();
        return (statusCode == HttpURLConnection.HTTP_OK);
      } else {
        if (CalendarService.CALDAV.equals(type)) {
          OptionsMethod options = new OptionsMethod(url);
          client.executeMethod(options);
          Header header = options.getResponseHeader("DAV");
          options.releaseConnection();
          if (header == null) {
            if (logger.isDebugEnabled()) {
              logger.debug("Cannot connect to remoter server or not support WebDav access");
            }
            return false;
          }
          Boolean support = header.toString().contains("calendar-access");
          options.releaseConnection();
          if (!support) {
            if (logger.isDebugEnabled()) {
              logger.debug("Remote server does not support CalDav access");
            }
            throw new UnsupportedOperationException("Remote server does not support CalDav access");
          }
          return support;
        }
        return false;
      }
    } catch (MalformedURLException e) {
      if (logger.isDebugEnabled())
        logger.debug(e.getMessage(), e);
      throw new RuntimeException("URL is invalid. Maybe no legal protocol or URl could not be parsed");
    } catch (IOException e) {
      if (logger.isDebugEnabled())
        logger.debug(e.getMessage(), e);
      throw new RuntimeException("Error occurs when connecting to remote server");
    }
  }

  @Override
  public Calendar importRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception {
    Calendar eXoCalendar = storage_.createRemoteCalendar(remoteCalendar);
    CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);

    if (CalendarService.ICALENDAR.equals(remoteCalendar.getType())) {
      remoteCalendar.setCalendarId(eXoCalendar.getId());
      remoteCalendar.setLastUpdated(Utils.getGreenwichMeanTime());
      InputStream icalInputStream = connectToRemoteServer(remoteCalendar);
      calService.getCalendarImportExports(CalendarService.ICALENDAR).importCalendar(remoteCalendar.getUsername(), icalInputStream, remoteCalendar.getCalendarId(), null, remoteCalendar.getBeforeTime(), remoteCalendar.getAfterTime(), false);
      calService.saveUserCalendar(remoteCalendar.getUsername(), eXoCalendar, false);
      return eXoCalendar;
    } else {
      if (CalendarService.CALDAV.equals(remoteCalendar.getType())) {
        MultiStatus multiStatus = connectToCalDavServer(remoteCalendar);
        String href;
        for (int i = 0; i < multiStatus.getResponses().length; i++) {
          MultiStatusResponse multiRes = multiStatus.getResponses()[i];
          href = multiRes.getHref();
          DavPropertySet propSet = multiRes.getProperties(DavServletResponse.SC_OK);
          net.fortuna.ical4j.model.Calendar iCalEvent = getCalDavResource(remoteCalendar, href);
          DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
          try {
            importCaldavEvent(remoteCalendar.getUsername(), eXoCalendar.getId(), null, iCalEvent, href, etag.getValue().toString(), true);
            storage_.setRemoteCalendarLastUpdated(remoteCalendar.getUsername(), eXoCalendar.getId(), Utils.getGreenwichMeanTime());
          } catch (Exception e) {
            if (logger.isDebugEnabled()) {
              logger.debug("Exception occurs when import calendar component " + href + ". Skip this component.", e);
            }
            continue;
          }
        }
        calService.saveUserCalendar(remoteCalendar.getUsername(), eXoCalendar, false);
        return eXoCalendar;
      }
      return null;
    }
  }

  @Override
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception {
    if (!storage_.isRemoteCalendar(username, remoteCalendarId)) {
      if (logger.isDebugEnabled()) {
        logger.debug("This calendar is not remote calendar.");
      }
      return null;
    }
    RemoteCalendar remoteCalendar = storage_.getRemoteCalendar(username, remoteCalendarId);
    if (CalendarService.ICALENDAR.equals(remoteCalendar.getType())) {
      // remove all components in local calendar
      List<String> calendarIds = new ArrayList<String>();
      calendarIds.add(remoteCalendarId);
      EventQuery eventQuery = new EventQuery();
      eventQuery.setCalendarId(new String[] { remoteCalendarId });
      eventQuery.setFromDate(remoteCalendar.getBeforeTime());
      eventQuery.setToDate(remoteCalendar.getAfterTime());
      List<CalendarEvent> events = storage_.getUserEvents(username, eventQuery);
      if (events != null && events.size() > 0) {
        for (CalendarEvent event : events) {
          if (Utils.isExceptionOccurrence(event))
            continue;
          else if (Utils.isRepeatEvent(event)) {
            storage_.removeRecurrenceSeries(username, event);
          } else
            storage_.removeUserEvent(username, remoteCalendarId, event.getId());
        }
      }

      Calendar eXoCalendar = storage_.getUserCalendar(username, remoteCalendarId);
      InputStream icalInputStream = connectToRemoteServer(remoteCalendar);
      CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
      calService.getCalendarImportExports(CalendarService.ICALENDAR).importCalendar(username, icalInputStream, remoteCalendarId, null, remoteCalendar.getBeforeTime(), remoteCalendar.getAfterTime(), false);
      storage_.setRemoteCalendarLastUpdated(username, eXoCalendar.getId(), Utils.getGreenwichMeanTime());
      return eXoCalendar;
    }

    if (CalendarService.CALDAV.equals(remoteCalendar.getType())) {
      Calendar eXoCalendar = synchronizeWithCalDavServer(remoteCalendar);
      storage_.setRemoteCalendarLastUpdated(username, eXoCalendar.getId(), Utils.getGreenwichMeanTime());
      return eXoCalendar;
    }

    return null;
  }

  /**
   * First time connect to CalDav server to get data
   * 
   * @param remoteCalendar
   * @return
   * @throws Exception
   */
  public MultiStatus connectToCalDavServer(RemoteCalendar remoteCalendar) throws Exception {
    HttpClient client = getRemoteClient(remoteCalendar);
    return doCalendarQuery(client, remoteCalendar.getRemoteUrl(), remoteCalendar.getBeforeTime(), remoteCalendar.getAfterTime());
  }

  public net.fortuna.ical4j.model.Calendar getCalDavResource(RemoteCalendar remoteCalendar, String href) throws Exception {
    HttpClient client = getRemoteClient(remoteCalendar);
    CalendarBuilder builder = new CalendarBuilder();
    // Enable relaxed-unfolding to allow ical4j parses "folding" line follows iCalendar specification
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);

    try {
      MultiStatus multiStatus = doCalendarMultiGet(client, remoteCalendar.getRemoteUrl(), new String[] { href });
      if (multiStatus == null)
        return null;
      MultiStatusResponse multiRes = multiStatus.getResponses()[0];
      DavPropertySet propSet = multiRes.getProperties(DavServletResponse.SC_OK);
      DavProperty calendarData = propSet.get(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
      return builder.build(new StringReader(calendarData.getValue().toString()));
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Can't get resource from CalDav server", e);
      }
      return null;
    }
  }

  /**
   * Get a map of pairs (href,etag) from caldav server
   * This calendar query doesn't include calendar-data element to get data faster
   * 
   * @param client
   * @param uri
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public Map<String, String> getEntityTags(HttpClient client, String uri, java.util.Calendar from, java.util.Calendar to) throws Exception {
    Map<String, String> etags = new HashMap<String, String>();
    ReportMethod report = makeCalDavQueryReport(uri, from, to);
    if (report == null)
      return null;

    try {
      client.executeMethod(report);
      MultiStatus multiStatus = report.getResponseBodyAsMultiStatus();

      String href;
      for (int i = 0; i < multiStatus.getResponses().length; i++) {
        MultiStatusResponse multiRes = multiStatus.getResponses()[i];
        href = multiRes.getHref();
        DavPropertySet propSet = multiRes.getProperties(DavServletResponse.SC_OK);
        DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
        etags.put(href, etag.getValue().toString());
      }

      return etags;
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug("Exception occurs when querying entity tags from CalDav server", e);
      return null;
    } finally {
      if (report != null) {
        report.releaseConnection();
      }
    }
  }

  /**
   * Do reload data from CalDav server for remote calendar with a time-range condition.
   * This function first gets entity tag map from server, then compare with data from local
   * to determines which events/task (or other components) need to be update, create or delete
   * 
   * @param remoteCalendar
   * @return Calendar
   * @throws Exception
   */

  public Calendar synchronizeWithCalDavServer(RemoteCalendar remoteCalendar) throws Exception {
    String username = remoteCalendar.getUsername();
    String remoteCalendarId = remoteCalendar.getCalendarId();

    if (!storage_.isRemoteCalendar(username, remoteCalendarId)) {
      return null;
    }
    if (!CalendarService.CALDAV.equals(remoteCalendar.getType())) {
      throw new UnsupportedOperationException("Not support");
    }

    CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if (calService == null) {
      calService = (CalendarService) ExoContainerContext.getContainerByName(PortalContainer.getCurrentPortalContainerName()).getComponentInstanceOfType(CalendarService.class);
    }
    CalendarBuilder calendarBuilder = new CalendarBuilder();
    // Enable relaxed-unfolding to allow ical4j parses "folding" line follows iCalendar spec
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);

    HttpClient client = getRemoteClient(remoteCalendar);

    java.util.Calendar from = remoteCalendar.getBeforeTime();
    java.util.Calendar to = remoteCalendar.getAfterTime();
    Map<String, String> entityTags = getEntityTags(client, remoteCalendar.getRemoteUrl(), from, to);

    // get List of event from local calendar in specific time-range
    EventQuery eventQuery = new EventQuery();
    eventQuery.setCalendarId(new String[] { remoteCalendarId });
    eventQuery.setFromDate(from);
    eventQuery.setToDate(to);

    List<CalendarEvent> eXoEvents = calService.getUserEvents(username, eventQuery);
    Iterator<CalendarEvent> it = eXoEvents.iterator();
    // events map contains set of (href, eventId) pairs in the local calendar
    Map<String, String> events = new HashMap<String, String>();
    while (it.hasNext()) {
      CalendarEvent event = it.next();
      if (Utils.isExceptionOccurrence(event))
        continue;
      String calDavResourceHref = calService.getCalDavResourceHref(username, remoteCalendarId, event.getId());
      if(calDavResourceHref != null) {
        events.put(calDavResourceHref, event.getId());
      }
    }

    // list of href of new events on the server
    List<String> created = new ArrayList<String>();

    // map of out-of-date event/task, the key is the href of event/task, the value is the id of event on local calendar
    Map<String, String> updated = new HashMap<String, String>();

    // list of event id need to delete
    List<String> deleted = new ArrayList<String>();

    // for each event on entity tags list, find this event in local calendar by href then use etag value to get:
    Iterator<Entry<String, String>> iter = entityTags.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> pairs = iter.next();
      String href = pairs.getKey();
      String etag = pairs.getValue();
      // new events
      if (!events.containsKey(href)) {
        created.add(href);
      } else {
        // check need-to-update events
        String eventId = events.get(href);
        String calendarId = calService.getEvent(username, eventId).getCalendarId();
        String localEtag = calService.getCalDavResourceEtag(username, calendarId, eventId);
        if (!localEtag.equals(etag)) {
          updated.put(href, eventId);
        }
      }
    }

    // for each event on local calendar, find this event in responses list to get list of need-to-delete event
    iter = events.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> pairs = iter.next();
      String href = pairs.getKey();
      if (!entityTags.containsKey(href)) {
        deleted.add(pairs.getValue());
      }
    }

    // from three lists, do update on local calendar
    // do a multi-get report request to server to get list of new events
    MultiStatus multiStatus = doCalendarMultiGet(client, remoteCalendar.getRemoteUrl(), created.toArray(new String[0]));
    String href;
    if (multiStatus != null) {
      for (int i = 0; i < multiStatus.getResponses().length; i++) {
        MultiStatusResponse multiRes = multiStatus.getResponses()[i];
        href = multiRes.getHref();
        DavPropertySet propSet = multiRes.getProperties(DavServletResponse.SC_OK);
        DavProperty calendarData = propSet.get(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
        DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
        try {
          net.fortuna.ical4j.model.Calendar iCalEvent = calendarBuilder.build(new StringReader(calendarData.getValue().toString()));
          // add new event
          importCaldavEvent(username, remoteCalendarId, null, iCalEvent, href, etag.getValue().toString(), true);
        } catch (Exception e) {
          if (logger.isDebugEnabled()) {
            logger.debug("Exception occurs when import calendar component " + href + ". Skip this component.");
          }
          continue;
        }
      }
    }

    multiStatus = doCalendarMultiGet(client, remoteCalendar.getRemoteUrl(), updated.keySet().toArray(new String[0]));
    if (multiStatus != null) {
      for (int i = 0; i < multiStatus.getResponses().length; i++) {
        MultiStatusResponse multiRes = multiStatus.getResponses()[i];
        href = multiRes.getHref();
        DavPropertySet propSet = multiRes.getProperties(DavServletResponse.SC_OK);
        DavProperty calendarData = propSet.get(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
        DavProperty etag = propSet.get(DavPropertyName.GETETAG.getName(), DavConstants.NAMESPACE);
        String eventId = updated.get(href);
        try {
          net.fortuna.ical4j.model.Calendar iCalEvent = calendarBuilder.build(new StringReader(calendarData.getValue().toString()));
          // update event
          importCaldavEvent(username, remoteCalendarId, eventId, iCalEvent, null, etag.getValue().toString(), false);
        } catch (Exception e) {
          if (logger.isDebugEnabled()) {
            logger.debug("Exception occurs when import calendar component " + href + ". Skip this component.");
          }
          continue;
        }
      }
    }

    // delete no-longer exists events
    Iterator<String> iterator = deleted.iterator();
    while (iterator.hasNext()) {
      String eventId = iterator.next();
      CalendarEvent event = storage_.getUserEvent(username, remoteCalendarId, eventId);
      event.setCalType(String.valueOf(Calendar.TYPE_PRIVATE));
      if (Utils.isRepeatEvent(event)) {
        storage_.removeRecurrenceSeries(username, event);
      } else {
        calService.removeUserEvent(username, remoteCalendarId, eventId);
      }
    }
    return calService.getUserCalendar(remoteCalendar.getUsername(), remoteCalendar.getCalendarId());
  }

  public MultiStatus doCalendarMultiGet(HttpClient client, String uri, String[] hrefs) throws Exception {

    if (hrefs.length == 0)
      return null;

    ReportMethod report = null;

    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();

      // root element
      Element calendarMultiGet = DomUtil.createElement(doc, CALDAV_XML_CALENDAR_MULTIGET, CALDAV_NAMESPACE);
      calendarMultiGet.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(), DavConstants.NAMESPACE.getURI());

      ReportInfo reportInfo = new ReportInfo(calendarMultiGet, DavConstants.DEPTH_0);
      DavPropertyNameSet propNameSet = reportInfo.getPropertyNameSet();
      propNameSet.add(DavPropertyName.GETETAG);
      DavPropertyName calendarData = DavPropertyName.create(CALDAV_XML_CALENDAR_DATA, CALDAV_NAMESPACE);
      propNameSet.add(calendarData);

      Element href;
      for (int i = 0; i < hrefs.length; i++) {
        href = DomUtil.createElement(doc, DavConstants.XML_HREF, DavConstants.NAMESPACE, hrefs[i]);
        reportInfo.setContentElement(href);
      }

      report = new ReportMethod(uri, reportInfo);
      client.executeMethod(report);
      MultiStatus multiStatus = report.getResponseBodyAsMultiStatus();
      return multiStatus;
    } finally {
      if (report != null)
        report.releaseConnection();
    }
  }

  /**
   * Send a calendar-query REPORT request to CalDav server
   * @param client
   * @param uri
   * @param from
   * @param to
   * @return
   * @throws Exception
   */
  public MultiStatus doCalendarQuery(HttpClient client, String uri, java.util.Calendar from, java.util.Calendar to) throws Exception {
    ReportMethod report = makeCalDavQueryReport(uri, from, to);
    if (report == null)
      return null;
    try {
      client.executeMethod(report);
      MultiStatus multiStatus = report.getResponseBodyAsMultiStatus();
      return multiStatus;
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug("Exception occurs when querying calendar events from CalDav server", e);
      return null;
    } finally {
      if (report != null) {
        report.releaseConnection();
      }
    }
  }

  public void importCaldavEvent(String username, String calendarId, String eventId, net.fortuna.ical4j.model.Calendar iCalendar, String href, String etag, Boolean isNew) throws Exception {
    CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if (calService == null) {
      calService = (CalendarService) PortalContainer.getInstance().getComponentInstanceOfType(CalendarService.class);
    }

    Map<String, VFreeBusy> vFreeBusyData = new HashMap<String, VFreeBusy>();
    Map<String, VAlarm> vAlarmData = new HashMap<String, VAlarm>();

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    CalendarEvent original = null;
    List<CalendarEvent> exceptions = new ArrayList<CalendarEvent>();

    // import calendar components
    ComponentList componentList = iCalendar.getComponents();
    CalendarEvent exoEvent;

    if (!isNew) {
      exoEvent = storage_.getEvent(username, eventId);
      exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE));

      if (Utils.isRepeatEvent(exoEvent)) {
        List<CalendarEvent> oldExceptions = storage_.getExceptionEvents(username, exoEvent);
        if (oldExceptions != null && oldExceptions.size() > 0) {
          for (CalendarEvent exception : oldExceptions) {
            storage_.removeUserEvent(username, calendarId, exception.getId());
          }
        }
      }
    }

    for (Object obj : componentList) {
      if (obj instanceof VEvent) {
        VEvent event = (VEvent) obj;
        if (!event.getAlarms().isEmpty()) {
          for (Object o : event.getAlarms()) {
            if (o instanceof VAlarm) {
              VAlarm va = (VAlarm) o;
              vAlarmData.put(event.getUid().getValue() + ":" + va.getProperty(Property.ACTION).getName(), va);
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
        if (isNew)
          exoEvent = new CalendarEvent();
        else
          exoEvent = storage_.getUserEvent(username, calendarId, eventId);
        exoEvent = generateEvent(event, exoEvent, username, calendarId);

        String sValue = Utils.EMPTY_STR;
        String eValue = Utils.EMPTY_STR;
        if (event.getStartDate() != null) {
          sValue = event.getStartDate().getValue();
          exoEvent.setFromDateTime(event.getStartDate().getDate());
        }
        if (event.getEndDate() != null) {
          eValue = event.getEndDate().getValue();
          exoEvent.setToDateTime(event.getEndDate().getDate());
        }
        exoEvent = setEventAttachment(event, exoEvent, eValue, sValue);
        if (event.getProperty(Property.RECURRENCE_ID) != null) {
          RecurrenceId recurId = (RecurrenceId) event.getProperty(Property.RECURRENCE_ID);
          exoEvent.setRecurrenceId(format.format(new Date(recurId.getDate().getTime())));
          if (original != null) {
            Node originalNode = storage_.getUserCalendarHome(username).getNode(calendarId).getNode(original.getId());
            String uuid = originalNode.getUUID();
            exoEvent.setId(originalNode.getName());
            exoEvent.setOriginalReference(uuid);
            List<String> excludeId;
            if (original.getExcludeId() != null && original.getExcludeId().length > 0) {
              excludeId = new ArrayList<String>(Arrays.asList(original.getExcludeId()));
            } else {
              excludeId = new ArrayList<String>();
            }
            excludeId.add(exoEvent.getRecurrenceId());
            original.setExcludeId(excludeId.toArray(new String[0]));
            storage_.saveUserEvent(username, calendarId, original, false);
          } else {
            exceptions.add(exoEvent);
          }
          storage_.saveOccurrenceEvent(username, calendarId, exoEvent, true);
        } else {
          if (event.getProperty(Property.RRULE) != null && event.getProperty(Property.RECURRENCE_ID) == null) {
            exoEvent = calculateEvent(event, exoEvent);
            original = exoEvent;

            List<String> excludeIds = new ArrayList<String>();
            PropertyList exdates = event.getProperties(Property.EXDATE);
            if (exdates != null && exdates.size() > 0) {
              for (Object exdate : exdates) {
                for (Object date : ((ExDate) exdate).getDates()) {
                  excludeIds.add(format.format(new Date(((net.fortuna.ical4j.model.DateTime) date).getTime())));
                }
              }
            }

            if (exceptions != null && exceptions.size() > 0) {
              for (CalendarEvent exception : exceptions) {
                excludeIds.add(exception.getRecurrenceId());
              }
            }
            exoEvent.setExcludeId(excludeIds.toArray(new String[0]));
            storage_.saveUserEvent(username, calendarId, exoEvent, isNew);

            String uuid = storage_.getUserCalendarHome(username).getNode(calendarId).getNode(exoEvent.getId()).getUUID();
            if (exceptions != null && exceptions.size() > 0) {
              for (CalendarEvent exception : exceptions) {
                exception.setOriginalReference(uuid);
                storage_.saveOccurrenceEvent(username, calendarId, exception, false);
              }
            }
          } else {
            storage_.saveUserEvent(username, calendarId, exoEvent, isNew);
          }
        }
        storage_.setRemoteEvent(username, calendarId, exoEvent.getId(), href, etag);
      }

      else if (obj instanceof VToDo) {
        VToDo event = (VToDo) obj;
        exoEvent = new CalendarEvent();   
        if (event.getProperty(Utils.X_STATUS) != null) {
          exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue());
        }
        exoEvent = setTaskAttachment(event, exoEvent,username,calendarId,vFreeBusyData);
        if(exoEvent != null) {
          storage_.saveUserEvent(username, calendarId, exoEvent, isNew);
          storage_.setRemoteEvent(username, calendarId, exoEvent.getId(), href, etag);
        }
      }
    }
  }

  public static CalendarEvent generateEvent(VEvent event, CalendarEvent exoEvent, String username, String calendarId) throws Exception {
    CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if (event.getProperty(Property.CATEGORIES) != null) {
      EventCategory evCate = new EventCategory();
      evCate.setName(event.getProperty(Property.CATEGORIES).getValue().trim());
      try {
        calService.saveEventCategory(username, evCate, true);
      } catch (ItemExistsException e) {
        evCate = calService.getEventCategoryByName(username, evCate.getName());
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Exception occurs when saving new event category '" + evCate.getName() + "' for iCalendar component: " + event.getUid(), e);
        }
      }
      exoEvent.setEventCategoryId(evCate.getId());
      exoEvent.setEventCategoryName(evCate.getName());
    }
    exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE));
    exoEvent.setCalendarId(calendarId);
    if (event.getSummary() != null)
      exoEvent.setSummary(event.getSummary().getValue());
    if (event.getDescription() != null)
      exoEvent.setDescription(event.getDescription().getValue());
    if (event.getStatus() != null)
      exoEvent.setStatus(event.getStatus().getValue());
    exoEvent.setEventType(CalendarEvent.TYPE_EVENT);
    return exoEvent;
  }

  public static CalendarEvent setEventAttachment(VEvent event, CalendarEvent exoEvent,String eValue, String sValue) throws Exception {
    if (sValue.length() == 8 && eValue.length() == 8) {
      exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() - 1));
    }
    if (sValue.length() > 8 && eValue.length() > 8) {
      if ("0000".equals(sValue.substring(9, 13)) && "0000".equals(eValue.substring(9, 13))) {
        exoEvent.setToDateTime(new Date(event.getEndDate().getDate().getTime() - 1));
      }
    }
    if (event.getLocation() != null)
      exoEvent.setLocation(event.getLocation().getValue());
    ICalendarImportExport.setPriorityExoEvent(event.getPriority(), exoEvent);
    
    if (event.getProperty(Utils.X_STATUS) != null) {
      exoEvent.setEventState(event.getProperty(Utils.X_STATUS).getValue());
    }
    if (event.getClassification() != null)
      exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(event.getClassification().getValue()));
    PropertyList attendees = event.getProperties(Property.ATTENDEE);
    if (!attendees.isEmpty()) {
      String[] invitation = new String[attendees.size()];
      for (int i = 0; i < attendees.size(); i++) {
        invitation[i] = ((Attendee) attendees.get(i)).getValue();
      }
      exoEvent.setInvitation(invitation);
    }
    try {
      PropertyList dataList = event.getProperties(Property.ATTACH);
      List<Attachment> attachments = calculateAtt(dataList);
      if (!attachments.isEmpty())
        exoEvent.setAttachment(attachments);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Exception occurs when importing attachments for iCalendar component: " + event.getUid(), e);
      }
    }
    return exoEvent;
  }

  public static CalendarEvent setTaskAttachment(VToDo task,CalendarEvent exoEvent,String username,String calendarId,Map<String,VFreeBusy> vFreeBusyData) throws Exception {
    CalendarService calService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    exoEvent = new CalendarEvent();
    
    // there is no due date, no way to know start/end of the task
    // this is a bit weird, because in eXo calendar we display task in time table, so that we need start/end time 
    if(task.getDue() == null) {
      return null;
    }
    
    java.util.Calendar tmpCal = java.util.Calendar.getInstance();
    tmpCal.setTime(task.getDue().getDate());
    
    exoEvent.setToDateTime(tmpCal.getTime());
    
    if (task.getStartDate() != null) {
      exoEvent.setFromDateTime(task.getStartDate().getDate());
    } else {
      // there is no start time, set start time of event to begin of due date, end time to end of due date
      // so that the task will be displayed at the header of the view of the time table. It can be improved later by having
      // other zone to display task
      exoEvent.setFromDateTime(Utils.getBeginDay(tmpCal).getTime());
      exoEvent.setToDateTime(Utils.getEndDay(tmpCal).getTime());
    }  
    
    if (task.getProperty(Property.CATEGORIES) != null) {
      EventCategory evCate = new EventCategory();
      evCate.setName(task.getProperty(Property.CATEGORIES).getValue().trim());
      try {
        calService.saveEventCategory(username, evCate, true);
      } catch (ItemExistsException e) {
        evCate = calService.getEventCategoryByName(username, evCate.getName());
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Exception occurs when saving new event category '" + evCate.getName() + "' for CalDav event: " + task.getUid(), e);
        }
      }
      exoEvent.setEventCategoryId(evCate.getId());
      exoEvent.setEventCategoryName(evCate.getName());
    }
    exoEvent.setCalType(String.valueOf(Calendar.TYPE_PRIVATE));
    exoEvent.setCalendarId(calendarId);
    if (task.getSummary() != null)
      exoEvent.setSummary(task.getSummary().getValue());
    if (task.getDescription() != null)
      exoEvent.setDescription(task.getDescription().getValue());
    if (task.getProperty(Utils.X_STATUS) != null) {
      exoEvent.setEventState(task.getProperty(Utils.X_STATUS).getValue());
    }
    if (task.getStatus() != null)
      exoEvent.setStatus(task.getStatus().getValue());
    exoEvent.setEventType(CalendarEvent.TYPE_TASK);
    
    if (task.getLocation() != null)
      exoEvent.setLocation(task.getLocation().getValue());
    ICalendarImportExport.setPriorityExoEvent(task.getPriority(), exoEvent);
    if (vFreeBusyData.get(task.getUid().getValue()) != null) {
      exoEvent.setStatus(CalendarEvent.ST_BUSY);
    }
    if (task.getClassification() != null)
      exoEvent.setPrivate(Clazz.PRIVATE.getValue().equals(task.getClassification().getValue()));
    PropertyList attendees = task.getProperties(Property.ATTENDEE);
    if (!attendees.isEmpty()) {
      String[] invitation = new String[attendees.size()];
      for (int i = 0; i < attendees.size(); i++) {
        invitation[i] = ((Attendee) attendees.get(i)).getValue();
      }
      exoEvent.setInvitation(invitation);
    }
    try {
      PropertyList dataList = task.getProperties(Property.ATTACH);
      List<Attachment> attachments = calculateAtt(dataList);
      if (!attachments.isEmpty())
        exoEvent.setAttachment(attachments);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Exception occurs when importing attachments for iCalendar component: " + task.getUid(), e);
      }
    }
    return exoEvent;
  }
  
  private static List<Attachment> calculateAtt(PropertyList dataList) throws Exception {
    List<Attachment> attachments = new ArrayList<Attachment>();
    for (Object o : dataList) {
      Attach a = (Attach) o;
      Attachment att = new Attachment();
      att.setName(a.getParameter(Parameter.CN).getValue());
      att.setMimeType(a.getParameter(Parameter.FMTTYPE).getValue());
      InputStream in = new ByteArrayInputStream(a.getBinary());
      att.setSize(in.available());
      att.setInputStream(in);
      attachments.add(att);
    }
    return attachments;
  }

  public static CalendarEvent calculateEvent(VEvent event, CalendarEvent exoEvent) throws Exception {
    RRule rrule = (RRule) event.getProperty(Property.RRULE);
    Recur recur = rrule.getRecur();
    String repeatType = recur.getFrequency();
    int interval = recur.getInterval();
    if (interval < 1)
      interval = 1;
    int count = recur.getCount();
    net.fortuna.ical4j.model.Date until = recur.getUntil();

    exoEvent.setRepeatInterval(interval);
    if (count > 0) {
      exoEvent.setRepeatCount(count);
      exoEvent.setRepeatUntilDate(null);
    } else {
      if (until != null) {
        Date repeatUntil = new Date(until.getTime());
        exoEvent.setRepeatUntilDate(repeatUntil);
        exoEvent.setRepeatCount(0);
      } else {
        exoEvent.setRepeatCount(0);
        exoEvent.setRepeatUntilDate(null);
      }
    }

    if (Recur.DAILY.equals(repeatType))
      exoEvent.setRepeatType(CalendarEvent.RP_DAILY);
    else if (Recur.YEARLY.equals(repeatType))
      exoEvent.setRepeatType(CalendarEvent.RP_YEARLY);
    else {
      if (Recur.WEEKLY.equals(repeatType)) {
        exoEvent.setRepeatType(CalendarEvent.RP_WEEKLY);
        WeekDayList weekDays = recur.getDayList();
        if (weekDays != null && weekDays.size() > 0) {
          String[] byDays = new String[weekDays.size()];
          for (int i = 0; i < byDays.length; i++) {
            WeekDay weekDay = (WeekDay) weekDays.get(i);
            String day = weekDay.getDay();
            int offset = weekDay.getOffset();
            if (offset != 0)
              byDays[i] = String.valueOf(offset) + day;
            else
              byDays[i] = day;
          }
          exoEvent.setRepeatByDay(byDays);
        } else {
          exoEvent.setRepeatByDay(null);
        }
      } else {
        if (Recur.MONTHLY.equals(repeatType)) {
          exoEvent.setRepeatType(CalendarEvent.RP_MONTHLY);
          WeekDayList weekDays = recur.getDayList();
          if (weekDays != null && weekDays.size() > 0) {
            String[] byDays = new String[weekDays.size()];
            WeekDay weekDay;
            for (int i = 0; i < byDays.length; i++) {
              weekDay = (WeekDay) weekDays.get(i);
              String day = weekDay.getDay();
              int offset = weekDay.getOffset();
              if (offset != 0)
                byDays[i] = String.valueOf(offset) + day;
              else
                byDays[i] = day;
            }
            exoEvent.setRepeatByDay(byDays);
            exoEvent.setRepeatByMonthDay(null);
          } else {
            NumberList monthdays = recur.getMonthDayList();
            if (monthdays != null && monthdays.size() > 0) {
              long[] byMonthDays = new long[monthdays.size()];
              for (int i = 0; i < byMonthDays.length; i++) {
                int monthday = (int) (Integer) monthdays.get(i);
                byMonthDays[i] = monthday;
              }
              exoEvent.setRepeatByDay(null);
              exoEvent.setRepeatByMonthDay(byMonthDays);
            }
          }
        }
      }
    }

    return exoEvent;
  }

  /**
   * Get the HttpClient object to prepare for the connection with remote server
   * @param remoteCalendar holds information about remote server
   * @return HttpClient object
   * @throws Exception
   */
  public HttpClient getRemoteClient(RemoteCalendar remoteCalendar) throws Exception {
    HostConfiguration hostConfig = new HostConfiguration();
    String host = new URL(remoteCalendar.getRemoteUrl()).getHost();
    if (Utils.isEmpty(host))
      host = remoteCalendar.getRemoteUrl();
    hostConfig.setHost(host);
    HttpClient client = new HttpClient();
    client.setHostConfiguration(hostConfig);
    client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
    client.getHttpConnectionManager().getParams().setSoTimeout(10000);
    // basic authentication
    if (!Utils.isEmpty(remoteCalendar.getRemoteUser())) {
      Credentials credentials = new UsernamePasswordCredentials(remoteCalendar.getRemoteUser(), remoteCalendar.getRemotePassword());
      client.getState().setCredentials(new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
    }
    return client;
  }

  /**
   * Make the new REPORT method object to query calendar component on CalDav server
   * @param uri the URI to the calendar collection on server 
   * @param from start date of the time range to filter calendar components
   * @param to end date of the time range to filter calendar components
   * @return ReportMethod object
   * @throws Exception
   */
  public ReportMethod makeCalDavQueryReport(String uri, java.util.Calendar from, java.util.Calendar to) throws Exception {
    ReportMethod report = null;
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();

      // root element
      Element calendarQuery = DomUtil.createElement(doc, CALDAV_XML_CALENDAR_QUERY, CALDAV_NAMESPACE);
      calendarQuery.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + DavConstants.NAMESPACE.getPrefix(), DavConstants.NAMESPACE.getURI());

      ReportInfo reportInfo = new ReportInfo(calendarQuery, DavConstants.DEPTH_0);
      DavPropertyNameSet propNameSet = reportInfo.getPropertyNameSet();
      propNameSet.add(DavPropertyName.GETETAG);

      // filter element
      Element filter = DomUtil.createElement(doc, CALDAV_XML_FILTER, CALDAV_NAMESPACE);

      Element calendarComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      calendarComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.Calendar.VCALENDAR);

      Element eventComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      eventComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.component.VEvent.VEVENT);

      Element todoComp = DomUtil.createElement(doc, CALDAV_XML_COMP_FILTER, CALDAV_NAMESPACE);
      todoComp.setAttribute(CALDAV_XML_COMP_FILTER_NAME, net.fortuna.ical4j.model.component.VEvent.VTODO);

      Element timeRange = DomUtil.createElement(doc, CALDAV_XML_TIME_RANGE, CALDAV_NAMESPACE);
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
      timeRange.setAttribute(CALDAV_XML_START, format.format(from.getTime()));
      timeRange.setAttribute(CALDAV_XML_END, format.format(to.getTime()));

      eventComp.appendChild(timeRange);
      todoComp.appendChild(timeRange);
      calendarComp.appendChild(eventComp);
      calendarComp.appendChild(todoComp);
      filter.appendChild(calendarComp);

      reportInfo.setContentElement(filter);
      report = new ReportMethod(uri, reportInfo);
      return report;
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug("Cannot build report method for CalDav query", e);
      return null;
    }
  }

  @Override
  public RemoteCalendar getRemoteCalendar(String url,
                                          String type,
                                          String remoteUser,
                                          String remotePassword) throws Exception {
    RemoteCalendar remoteCalendar = null;
    if (CalendarService.ICALENDAR.equals(type)) {
      remoteCalendar = new RemoteCalendar();
      remoteCalendar.setRemoteUrl(url);
      remoteCalendar.setType(type);
      InputStream inputStream = connectToRemoteServer(remoteCalendar);
      try {
        CalendarBuilder calendarBuilder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar iCalendar = calendarBuilder.build(inputStream);
        Property property = null;
        remoteCalendar.setCalendarName((property = iCalendar.getProperty(ICAL_PROPS_CALENDAR_NAME)) != null ? property.getValue() : "");
        remoteCalendar.setDescription((property = iCalendar.getProperty(ICAL_PROPS_CALENDAR_DESCRIPTION)) != null ? property.getValue() : "");
      } finally {
        if (inputStream != null)
          inputStream.close();
      }
    }
    return remoteCalendar;
  }
}
