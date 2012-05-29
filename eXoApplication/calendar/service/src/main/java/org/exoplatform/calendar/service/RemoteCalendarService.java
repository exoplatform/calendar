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
package org.exoplatform.calendar.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * Author : khiem.dohoang
 *          khiem.dohoang@exoplatform.com
 * Jan 10, 2011  
 */
public interface RemoteCalendarService {

  /**
   * Check if the remote url is valid, in 2 cases of iCalendar url or CalDav url, with authentication
   * @param url the remote url
   * @param type the type of remote calendar, iCalendar or CalDav
   * @param username the remote username used to authenticate
   * @param password the remote password used to authenticate
   * @return true if remote url is available in case of iCalendar and CalDav access support in case of CalDav
   * @throws Exception
   */
  boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws IOException, UnsupportedOperationException;

  /**
   * Connect to remote server
   * @param remoteUrl the remote url
   * @param remoteType the remote type, iCalendar or CalDav
   * @param remoteUser remote username to authenticate
   * @param remotePassword remote password to authenticate
   * @return response's input stream
   * @throws Exception
   */
  InputStream connectToRemoteServer(RemoteCalendar remoteCalendar) throws Exception;

  /**
   * Import iCalendar to local eXo Calendar
   * @param remoteCalendar object content all properties for remote calendar.
   * @return Calendar object
   * @throws Exception
   */
  Calendar importRemoteCalendar(RemoteCalendar remoteCalendar) throws Exception;

  /**
   * Reload remote calendar
   * @param username
   * @param remoteCalendarId
   * @return
   * @throws Exception
   */
  Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception;
  
  /**
   * Read calendar information(name, description,...) from url.
   * @param url url to the calendar data
   * @param type type of the calendar
   * @param remoteUser username
   * @param remotePassword password
   * @return null if cannot read the calendar information.
   * @throws Exception
   */
  RemoteCalendar getRemoteCalendar(String url, String type, String remoteUser, String remotePassword) throws Exception;

}
