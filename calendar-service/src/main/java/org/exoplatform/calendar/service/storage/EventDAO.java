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

package org.exoplatform.calendar.service.storage;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Invitation;
import org.exoplatform.commons.utils.ListAccess;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public interface EventDAO extends GenericDAO<CalendarEvent> {

  ListAccess<CalendarEvent> findEventsByQuery(EventQuery eventQuery) throws CalendarException;

  public ListAccess<Invitation> findInvitationsByQuery(EventQuery query);

  public Invitation getInvitationById(String invitationID) throws CalendarException;

  public void removeInvitation(String id) throws CalendarException;

  public void updateInvitation(String id, String status) throws CalendarException;

  public Invitation createInvitation(String eventId, String participant, String status) throws CalendarException;
}
