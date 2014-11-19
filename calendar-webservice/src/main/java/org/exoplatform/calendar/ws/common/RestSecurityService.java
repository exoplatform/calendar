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
  
package org.exoplatform.calendar.ws.common;

public interface RestSecurityService {

  public static final String NOBODY = "Nobody";

  /**
   * Return TRUE if no permission configured for this request uri, 
   * OR user has permission configured with that uri <br/>
   * For example: if no permission is configured, any user can access any rest resource <br/>
   * But if the rest uri: /rest/calendar is configured with *:/platform/admins --> only admin group can access that resource <br/>
   * The permissions are inherited, if there are config:  /rest --> *:/admin, and /rest/calendar --> *:/users. So an user must
   * have admin permission to access to /rest/calendar
   * Super user has permission to access all resources. No matter how permission is configured
   */ 
  public boolean hasPermission(String requestPath);
}
