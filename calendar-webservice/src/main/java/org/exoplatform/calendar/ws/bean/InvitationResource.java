/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.calendar.ws.bean;

import static org.exoplatform.calendar.ws.CalendarRestApi.EVENT_URI;
import static org.exoplatform.calendar.ws.CalendarRestApi.INVITATION_URI;

import java.io.Serializable;

import org.exoplatform.calendar.service.Invitation;
import org.exoplatform.calendar.ws.common.Resource;

public class InvitationResource extends Resource {
  private static final long serialVersionUID = -5546515171185717545L;

  private Serializable            event;

  private String            participant;

  private String            status;

  public InvitationResource() {
    super(null);
  }
  
  public InvitationResource(Invitation data, String basePath) {
    super(data.getId());
    setHref(new StringBuffer(basePath).append(INVITATION_URI).append(data.getId()).toString());
    event = new StringBuffer(basePath).append(EVENT_URI).append(data.getEventId()).toString();
    this.participant = data.getParticipant();
    this.status = data.getStatus();
  }

  public Serializable getEvent() {
    return event;
  }
  
  public void setEvt(Serializable event) {
    this.event = event;
  }

  public String getParticipant() {
    return participant;
  }

  public String getStatus() {
    return status;
  }

}
