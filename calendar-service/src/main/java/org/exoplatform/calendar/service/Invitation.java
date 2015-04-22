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
package org.exoplatform.calendar.service;

public class Invitation {

  private String eventId;
  
  private String  participant;
  
  private String status;

  public Invitation(String eventId, String participant, String status) {
    this.eventId = eventId;
    this.participant = participant;
    this.status = status;    
  }

  public String getId() {
    return String.format("%s:%s", eventId, participant);
  }

  public String getEventId() {
    return eventId;
  }

  public String getParticipant() {
    return participant;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
    result = prime * result + ((participant == null) ? 0 : participant.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Invitation other = (Invitation) obj;
    if (eventId == null) {
      if (other.eventId != null)
        return false;
    } else if (!eventId.equals(other.eventId))
      return false;
    if (participant == null) {
      if (other.participant != null)
        return false;
    } else if (!participant.equals(other.participant))
      return false;
    if (status == null) {
      if (other.status != null)
        return false;
    } else if (!status.equals(other.status))
      return false;
    return true;
  }

  public static String[] parse(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id should not be null");
    }
    
    String[] tmp = id.split(":");
    if (tmp.length != 2) {
      throw new IllegalArgumentException("id should be compose of eventId:participantId");
    }
    return tmp;
  }  
}