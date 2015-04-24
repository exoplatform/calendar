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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.GenericContainerRequest;
import org.exoplatform.services.rest.RequestFilter;

public class RestSecurityFilter implements RequestFilter {

  private RestSecurityServiceImpl security;
  
  public RestSecurityFilter(RestSecurityServiceImpl security) {
    this.security = security;
  }
  
  @Override
  public void doFilter(GenericContainerRequest request) {
      String requestUri = request.getRequestUri().getPath();
      String path = requestUri.replaceFirst(request.getBaseUri().getPath(), "");
      if (!security.hasPermission(path)) {
        throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(
            "You do not have access rights to this resource, please contact your administrator. ").type(
            MediaType.TEXT_PLAIN).build());
      }
  }  
}
