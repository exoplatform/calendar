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
package org.exoplatform.calendar.service;

import org.exoplatform.calendar.storage.Storage;

/**
 * A new Calendar Service API which supports multiple data sources known as
 * Storage.
 * <p>
 * The new service API is also better organized into smaller handlers, such as
 * CalendarHandler and EventHandler.
 * 
 * @author <a href="trongtt@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public interface ExtendedCalendarService {

  /**
   * @return the CalendarHandler implementation
   */
  public CalendarHandler getCalendarHandler();

  /**
   * @return the EventHandler implementation
   */
  public EventHandler getEventHandler();

  /**
   * Return the Storage associated with given id, or NULL if there is no one
   * with such id.
   * <p>
   * If the given id is NULL, the JCRStorage will be returned by default.
   * 
   * @param compositeId
   * @return the Storage associated with given id.
   */
  public Storage lookForDS(String id);
}