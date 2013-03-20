/*
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
 */
package org.exoplatform.calendar.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class EventPageListQuery extends JCRPageList {
  private static final Log log = ExoLogger.getExoLogger(EventPageListQuery.class);

  private String       username_;

  private NodeIterator iter_      = null;

  private String       value_;

  private long         pageReturn = 0;

  private Session      session_   = null;

  public EventPageListQuery(String username, String value, long pageSize) throws Exception {

    super(pageSize);
    username_ = username;
    value_ = value;
    /*
     * value_ = "Select * from exo:calendarEvent where jcr:path like '%/Users/root/ApplicationData/CalendarApplication/calendars/%'" + " and jcr:path like '%/exo:applications/CalendarApplication/calendars/%'";
     */
    Session session = getJCRSession(username);
    if (session != null) {
      setAvailablePage(((QueryResultImpl) createXPathQuery(session, username, value_).execute()).getTotalSize());
    }
  }

  protected void populateCurrentPage(long page, String username) throws Exception {
    long pageSize = getPageSize();
    Node currentNode;
    Session session = getJCRSession(username);
    long totalPage = 0;
    QueryImpl queryImpl = createXPathQuery(session, username, value_);
    if (page > 1) {
      long position = (page - 1) * pageSize;
      if (pageReturn == page) {
        queryImpl.setOffset(position - 1);
      } else {
        queryImpl.setOffset(position);
      }
    }
    queryImpl.setLimit(pageSize);
    QueryResult result = queryImpl.execute();
    iter_ = result.getNodes();
    totalPage = ((QueryResultImpl) result).getTotalSize();
    setAvailablePage(totalPage);

    currentListPage_ = new ArrayList<CalendarEvent>();
    for (int i = 0; i < pageSize; i++) {
      if (iter_ != null && iter_.hasNext()) {
        currentNode = iter_.nextNode();
        if (currentNode.isNodeType(Utils.EXO_CALENDAR_EVENT)) {
          CalendarEvent calendarEvent = getEvent(currentNode);
          currentListPage_.add(calendarEvent);
        }
      } else {
        break;
      }
    }
    iter_ = null;
  }

  public static CalendarEvent getEventFromNode(CalendarEvent event, Node eventNode, Node reminderFolder) throws Exception {
    StringBuilder namePattern = new StringBuilder(512);
    namePattern.append(Utils.EXO_ID).append('|').append(Utils.EXO_CALENDAR_ID).append('|').append(Utils.EXO_SUMMARY)
         .append('|').append(Utils.EXO_EVENT_CATEGORYID).append('|').append(Utils.EXO_EVENT_CATEGORY_NAME).append('|')
         .append(Utils.EXO_LOCATION).append('|').append(Utils.EXO_TASK_DELEGATOR).append('|').append(Utils.EXO_REPEAT)
         .append('|').append(Utils.EXO_DESCRIPTION).append('|').append(Utils.EXO_FROM_DATE_TIME).append('|')
         .append(Utils.EXO_TO_DATE_TIME).append('|').append(Utils.EXO_EVENT_TYPE).append('|')
         .append(Utils.EXO_PRIORITY).append('|').append(Utils.EXO_IS_PRIVATE).append('|').append(Utils.EXO_EVENT_STATE).append('|')
         .append(Utils.EXO_SEND_OPTION).append('|').append(Utils.EXO_MESSAGE).append('|').append(Utils.EXO_DATE_MODIFIED)
         .append('|').append(Utils.EXO_INVITATION).append('|').append(Utils.EXO_PARTICIPANT).append('|')
         .append(Utils.EXO_PARTICIPANT_STATUS);
    PropertyIterator it = eventNode.getProperties(namePattern.toString());
    while (it.hasNext()) {
      Property p = it.nextProperty();
      String name = p.getName();
      if (name.equals(Utils.EXO_ID)) {
        event.setId(p.getString());
      } else if (name.equals(Utils.EXO_CALENDAR_ID)) {
        event.setCalendarId(p.getString());
      } else if (name.equals(Utils.EXO_SUMMARY)) {
        event.setSummary(p.getString());
      } else if (name.equals(Utils.EXO_EVENT_CATEGORYID)) {
        event.setEventCategoryId(p.getString());
      } else if (name.equals(Utils.EXO_EVENT_CATEGORY_NAME)) {
        event.setEventCategoryName(p.getString());
      } else if (name.equals(Utils.EXO_LOCATION)) {
        event.setLocation(p.getString());
      } else if (name.equals(Utils.EXO_TASK_DELEGATOR)) {
        event.setTaskDelegator(p.getString());
      } else if (name.equals(Utils.EXO_REPEAT)) {
        event.setRepeatType(p.getString());
      } else if (name.equals(Utils.EXO_DESCRIPTION)) {
        event.setDescription(p.getString());
      } else if (name.equals(Utils.EXO_FROM_DATE_TIME)) {
        event.setFromDateTime(p.getDate().getTime());
      } else if (name.equals(Utils.EXO_TO_DATE_TIME)) {
        event.setToDateTime(p.getDate().getTime());
      } else if (name.equals(Utils.EXO_EVENT_TYPE)) {
        event.setEventType(p.getString());
      } else if (name.equals(Utils.EXO_PRIORITY)) {
        event.setPriority(p.getString());
      } else if (name.equals(Utils.EXO_IS_PRIVATE)) {
        event.setPrivate(p.getBoolean());
      } else if (name.equals(Utils.EXO_EVENT_STATE)) {
        event.setEventState(p.getString());
      } else if (name.equals(Utils.EXO_SEND_OPTION)) {
        event.setSendOption(p.getString());
      } else if (name.equals(Utils.EXO_MESSAGE)) {
        event.setMessage(p.getString());
      } else if (name.equals(Utils.EXO_DATE_MODIFIED)) {
        event.setLastUpdatedTime(p.getDate().getTime());
      } else if (name.equals(Utils.EXO_INVITATION)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setInvitation(new String[] { values[0].getString() });
        } else {
          String[] invites = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            invites[i] = values[i].getString();
          }
          event.setInvitation(invites);
        }
      } else if (name.equals(Utils.EXO_PARTICIPANT)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setParticipant(new String[] { values[0].getString() });
        } else {
          String[] participant = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            participant[i] = values[i].getString();
          }
          event.setParticipant(participant);
        }
      } else if (name.equals(Utils.EXO_PARTICIPANT_STATUS)) {
        Value[] values = p.getValues();
        if (values.length == 1) {
          event.setParticipantStatus(new String[] { values[0].getString() });
        } else {
          String[] participantStatus = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            participantStatus[i] = values[i].getString();
          }
          event.setParticipantStatus(participantStatus);
        }
      }
    }
    try {
      event.setReminders(getReminders(eventNode, reminderFolder));
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("fail to set the reminders to the event", e);
      }
    }
    event.setAttachment(getAttachments(eventNode));
    return event;
  }

  private CalendarEvent getEvent(Node eventNode) throws Exception {
    CalendarEvent event = new CalendarEvent();
    String emptyStr = String.valueOf("");
    if (eventNode.getPath().contains(getPublicServiceHome())) {
      event.setCalType(emptyStr + Utils.PUBLIC_TYPE);
    } else if (eventNode.getPath().contains(getPrivateServiceHome())) {
      event.setCalType(emptyStr + Utils.PRIVATE_TYPE);
    } else
      event.setCalType(emptyStr + Utils.SHARED_TYPE);
    return getEventFromNode(event, eventNode, null);
  }

  private static List<Attachment> getAttachments(Node eventNode) throws Exception {
    List<Attachment> attachments = new ArrayList<Attachment>();
    if (eventNode.hasNode(Utils.ATTACHMENT_NODE)) {
      Node attachHome = eventNode.getNode(Utils.ATTACHMENT_NODE);
      NodeIterator iter = attachHome.getNodes();
      while (iter.hasNext()) {
        Node attchmentNode = iter.nextNode();
        if (attchmentNode.isNodeType(Utils.EXO_EVEN_TATTACHMENT)) {
          Attachment attachment = new Attachment();
          attachment.setId(attchmentNode.getPath());
          if (attchmentNode.hasProperty(Utils.EXO_FILE_NAME))
            attachment.setName(attchmentNode.getProperty(Utils.EXO_FILE_NAME).getString());
          Node contentNode = attchmentNode.getNode(Utils.JCR_CONTENT);
          if (contentNode != null) {
            if (contentNode.hasProperty(Utils.JCR_LASTMODIFIED))
              attachment.setLastModified(contentNode.getProperty(Utils.JCR_LASTMODIFIED).getDate());
            if (contentNode.hasProperty(Utils.JCR_MIMETYPE))
              attachment.setMimeType(contentNode.getProperty(Utils.JCR_MIMETYPE).getString());
            if (contentNode.hasProperty(Utils.JCR_DATA)) {
              InputStream inputStream = contentNode.getProperty(Utils.JCR_DATA).getStream();
              attachment.setSize(inputStream.available());
              attachment.setInputStream(inputStream);
            }
          }
          attachment.setWorkspace(attchmentNode.getSession().getWorkspace().getName());
          attachments.add(attachment);
        }
      }
    }
    return attachments;
  }

  public static List<Reminder> getReminders(Node eventNode, Node reminderFolder) throws Exception {
    List<Reminder> reminders = new ArrayList<Reminder>();
    Date fromDate = eventNode.getProperty(Utils.EXO_FROM_DATE_TIME).getDate().getTime();
    if (reminderFolder == null)
      return reminders;
    if (reminderFolder.hasNode(eventNode.getName())) {
      NodeIterator iter = reminderFolder.getNode(eventNode.getName()).getNodes();
      while (iter.hasNext()) {
        Node reminderNode = iter.nextNode();
        if (reminderNode.isNodeType(Utils.EXO_REMINDER)) {
          Reminder reminder = new Reminder();
          reminder.setId(reminderNode.getName());
          StringBuilder namePattern = new StringBuilder(128);
          namePattern.append(Utils.EXO_OWNER).append('|').append(Utils.EXO_EVENT_ID).append('|').append(Utils.EXO_REMINDER_TYPE)
               .append('|').append(Utils.EXO_ALARM_BEFORE).append('|').append(Utils.EXO_EMAIL).append('|')
               .append(Utils.EXO_IS_REPEAT).append('|').append(Utils.EXO_TIME_INTERVAL).append('|').append(Utils.EXO_DESCRIPTION);
          PropertyIterator it = reminderNode.getProperties(namePattern.toString());
          while (it.hasNext()) {
            Property p = it.nextProperty();
            String name = p.getName();
            if (name.equals(Utils.EXO_OWNER)) {
              reminder.setReminderOwner(p.getString());
            } else if (name.equals(Utils.EXO_EVENT_ID)) {
              reminder.setEventId(p.getString());
            } else if (name.equals(Utils.EXO_REMINDER_TYPE)) {
              reminder.setReminderType(p.getString());
            } else if (name.equals(Utils.EXO_ALARM_BEFORE)) {
              reminder.setAlarmBefore(p.getLong());
            } else if (name.equals(Utils.EXO_EMAIL)) {
              reminder.setEmailAddress(p.getString());
            } else if (name.equals(Utils.EXO_IS_REPEAT)) {
              reminder.setRepeate(p.getBoolean());
            } else if (name.equals(Utils.EXO_TIME_INTERVAL)) {
              reminder.setRepeatInterval(p.getLong());
            } else if (name.equals(Utils.EXO_DESCRIPTION)) {
              reminder.setDescription(p.getString());
            }              
          }
          reminder.setFromDateTime(fromDate);
          reminders.add(reminder);
        }
      }
    }
    return reminders;
  }

  @Override
  public List<CalendarEvent> getAll() throws Exception {
    Session session = getJCRSession(username_);
    QueryImpl queryImpl = createXPathQuery(session, username_, value_);
    // queryImpl.setLimit(pageSize);
    QueryResult result = queryImpl.execute();
    iter_ = result.getNodes();
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    while (iter_.hasNext()) {
      Node eventNode = iter_.nextNode();
      events.add(getEvent(eventNode));
    }
    return events;
  }

  private String getPublicServiceHome() throws Exception {
    SessionProvider provider = Utils.createSystemProvider();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    Node publicApp = nodeHierarchyCreator.getPublicApplicationNode(provider);
    if (publicApp != null && publicApp.hasNode(Utils.CALENDAR_APP))
      return publicApp.getNode(Utils.CALENDAR_APP).getPath();
    return null;
  }

  private String getPrivateServiceHome() throws Exception {
    SessionProvider provider = Utils.createSystemProvider();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    Node privateApp = nodeHierarchyCreator.getUserApplicationNode(provider, username_);
    if (privateApp != null && privateApp.hasNode(Utils.CALENDAR_APP))
      return privateApp.getNode(Utils.CALENDAR_APP).getPath();
    return null;
  }

  public void setSession(Session s) {
    session_ = s;
  }

  private Session getJCRSession(String username) throws Exception {
    try {
      RepositoryService repositoryService = (RepositoryService) PortalContainer.getComponent(RepositoryService.class);
      SessionProvider sessionProvider = Utils.createSystemProvider();
      String defaultWS = repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
      return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository());
    } catch (NullPointerException e) {
      return session_;
    }
  }

  private QueryImpl createXPathQuery(Session session, String username, String xpath) throws Exception {
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    return (QueryImpl) queryManager.createQuery(xpath, Query.XPATH);
  }
}
