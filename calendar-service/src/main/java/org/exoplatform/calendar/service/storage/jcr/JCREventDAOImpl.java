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

  private Storage context;
  private JCRDataStorage dataStorage;

  private static final Log LOG   = ExoLogger.getExoLogger(JCRCalendarDAOImpl.class);

  public JCREventDAOImpl(CalendarService calService, JCRStorage storage) {
    this.context = context;
    this.dataStorage = ((CalendarServiceImpl) calService).getDataStorage();
  }

  @Override
  public ListAccess<CalendarEvent> findEventsByQuery(EventQuery eventQuery) throws CalendarException {
    return null;
  }

  @Override
  public ListAccess<Invitation> findInvitationsByQuery(EventQuery query) {
    return null;
  }

  @Override
  public Invitation getInvitationById(String invitationID) throws CalendarException {
    return null;
  }

  @Override
  public void removeInvitation(String id) throws CalendarException {

  }

  @Override
  public void updateInvitation(String id, String status) throws CalendarException {

  }

  @Override
  public Invitation createInvitation(String eventId, String participant, String status) throws CalendarException {
    return null;
  }

  @Override
  public CalendarEvent getById(String id) {
    return null;
  }

  @Override
  public CalendarEvent getById(String id, CalendarType calType) {
    return null;
  }

  @Override
  public CalendarEvent save(CalendarEvent object, boolean isNew) {
    return null;
  }

  @Override
  public CalendarEvent remove(String id, CalendarType calType) {
    return null;
  }

  @Override
  public CalendarEvent newInstance(CalendarType type) {
    return null;
  }
}
