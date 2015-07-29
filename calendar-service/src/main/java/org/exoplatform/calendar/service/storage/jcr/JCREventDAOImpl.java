/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.calendar.service.storage.jcr;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarType;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Invitation;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.calendar.service.storage.EventDAO;
import org.exoplatform.calendar.service.storage.Storage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class JCREventDAOImpl implements EventDAO {

  private final Storage context;
  private final JCRDataStorage dataStorage;
  private final org.exoplatform.calendar.service.EventDAO oldDAO;

  private static final Log LOG   = ExoLogger.getExoLogger(JCRCalendarDAOImpl.class);

  public JCREventDAOImpl(CalendarService calService, JCRStorage storage) {
    this.context = storage;
    this.dataStorage = ((CalendarServiceImpl) calService).getDataStorage();
    this.oldDAO = calService.getEventDAO();
  }

  @Override
  public ListAccess<CalendarEvent> findEventsByQuery(EventQuery eventQuery) throws CalendarException {
    return null;
  }

  @Override
  public ListAccess<Invitation> findInvitationsByQuery(EventQuery query) {
    return oldDAO.findInvitationsByQuery(query);
  }

  @Override
  public Invitation getInvitationById(String invitationID) throws CalendarException {
    return oldDAO.getInvitationById(invitationID);
  }

  @Override
  public void removeInvitation(String id) throws CalendarException {
    oldDAO.removeInvitation(id);
  }

  @Override
  public void updateInvitation(String id, String status) throws CalendarException {
    oldDAO.updateInvitation(id, status);
  }

  @Override
  public Invitation createInvitation(String eventId, String participant, String status) throws CalendarException {
    return oldDAO.createInvitation(eventId, participant, status);
  }

  @Override
  public CalendarEvent getById(String id) {
    return getById(id, null);
  }

  @Override
  public CalendarEvent getById(String id, CalendarType calType) {
    try {
      return dataStorage.getEventById(id);
    } catch (Exception ex) {
      LOG.error(ex);
    }
    return null;
  }

  @Override
  public CalendarEvent save(CalendarEvent event, boolean isNew) {
    try {
      CalendarType calType = event.getCalendarType();
      String calendarId = event.getCalendarId();
      Calendar cal = context.getCalendarDAO().getById(calendarId, calType);
      if (cal == null) {
        return null;
      }
      calType = cal.getCalendarType();

      if (calType == Calendar.Type.PERSONAL) {
        dataStorage.saveUserEvent(cal.getCalendarOwner(), cal.getId(), event, isNew);
      } else if (calType == Calendar.Type.GROUP) {
        dataStorage.savePublicEvent(cal.getId(), event, isNew);
      } else {
        return null;
      }

      return event;
    } catch (Exception ex) {
      LOG.error(ex);
    }
    return null;
  }

  @Override
  public CalendarEvent remove(String id, CalendarType calType) {
    try {
      CalendarEvent event = this.getById(id, calType);
      if (event == null) {
        return null;
      }
      Calendar cal = context.getCalendarDAO().getById(event.getCalendarId(), event.getCalendarType());
      CalendarType type = cal.getCalendarType();

      if (type == Calendar.Type.PERSONAL) {
        dataStorage.removeUserEvent(cal.getCalendarOwner(), cal.getId(), id);
      } else if (type == Calendar.Type.GROUP) {
        dataStorage.removePublicEvent(cal.getId(), id);
      } else {
        return null;
      }

      return event;

    } catch (Exception ex) {
      LOG.error(ex);
    }
    return null;
  }

  @Override
  public CalendarEvent newInstance(CalendarType type) {
    CalendarEvent event = new CalendarEvent();
    event.setCalendarType(type);
    return event;
  }
}
