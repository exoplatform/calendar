/*
 * Copyright (C) 2014 eXo Platform SAS.
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

package org.exoplatform.calendar.service.impl;

import javax.jcr.NodeIterator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarException;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Invitation;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class InvitationListAccess implements ListAccess<Invitation> {

  private static Log   log = ExoLogger.getLogger(InvitationListAccess.class);

  private EventDAOImpl evtDAO;

  private EventQuery   query;

  private Integer      size;

  public InvitationListAccess(EventDAOImpl evtDAO, EventQuery query) {
    this.evtDAO = evtDAO;
    this.query = query;
  }

  @Override
  public int getSize() throws Exception {
    if (size == null) {
      log.debug("Querying to get Size");
      load(0, 0);
    }
    return size;
  }

  @Override
  public Invitation[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    try {
      QueryImpl jcrQuery = evtDAO.createJCRQuery(query.getQueryStatement(), query.getQueryType());
      if (limit > 0) {
        jcrQuery.setOffset(offset);
        jcrQuery.setLimit(limit);
      }

      QueryResultImpl queryResult = (QueryResultImpl)jcrQuery.execute();
      NodeIterator events = queryResult.getNodes();
      this.size = queryResult.getTotalSize();

      List<Invitation> invitations = new LinkedList<Invitation>();

      String[] calIds = query.getCalendarId() == null ? new String[0] : query.getCalendarId();
      Arrays.sort(calIds);        
      String[] pars = query.getParticipants() == null ? new String[0] : query.getParticipants();
      Arrays.sort(pars);
      
      while (events.hasNext()) {
        CalendarEvent event = evtDAO.storage.getEventById(events.nextNode().getProperty(Utils.EXO_ID).getString());
        if (Arrays.binarySearch(calIds, event.getCalendarId()) >= 0) {
          invitations.addAll(Arrays.asList(event.getInvitations()));
        } else {
          for (Invitation ivt : event.getInvitations()) {
            if (Arrays.binarySearch(pars, ivt.getParticipant()) >= 0) {
              invitations.add(ivt);
            }
          }          
        }
      }
      
      return invitations.toArray(new Invitation[invitations.size()]);
    } catch (Exception ex) {
      throw new CalendarException(null, ex);
    }
  }

}
