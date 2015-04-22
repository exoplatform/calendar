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

package org.exoplatform.calendar.ws;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.services.rest.resource.ResourceContainer;

public class SubResourceHrefBuilder {
  private List<String> subResources = new LinkedList<String>();
  
  public SubResourceHrefBuilder(ResourceContainer restService) {
    subResources = getResourcesInfo(restService);
  }

  public String[] buildResourceMap(UriInfo uriInfo) {
    List<String> resources = new LinkedList<String>();
        
    StringBuilder uriBuilder = new StringBuilder();
    URI uri = uriInfo.getRequestUri();
    uriBuilder.append(uri.getScheme()).append("://");
    uriBuilder.append(uri.getHost()).append(":").append(uri.getPort());
    String base = uriBuilder.toString();
    
    for (String rs : subResources) {
      resources.add(base + rs);
    }
    return resources.toArray(new String[resources.size()]);
  }
  
  private List<String> getResourcesInfo(ResourceContainer restService) {
    List<String> subResources = new LinkedList<String>();
    
    Path path = restService.getClass().getAnnotation(Path.class);
    if (path == null) {
      throw new IllegalStateException("base path for " + restService + " is not found");
    }
    String basePath = path.value();
    
    for (Method method : restService.getClass().getMethods()) {
      Path mPath = method.getAnnotation(Path.class);
      
      if (mPath != null) {
        String methodPath = mPath.value();       
        
        subResources.add(basePath + methodPath);
      }
    }
    
    return subResources;
  }
}