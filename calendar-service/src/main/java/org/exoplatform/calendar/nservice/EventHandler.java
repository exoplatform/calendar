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
package org.exoplatform.calendar.nservice;

import org.exoplatform.calendar.model.Event;
import org.exoplatform.commons.utils.ListAccess;

public interface EventHandler {
  public Event getEventById(String eventId);

  public Event saveEvent(Event event);

  public Event removeEvent(String eventId);

  ListAccess<Event> findEventsByQuery(org.exoplatform.calendar.model.query.EventQuery eventQuery);

  public Event newEventInstance(String dsId);
}