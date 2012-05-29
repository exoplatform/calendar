/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 3, 2008  
 */
public class CsNodeTypeMapping {
  private String                  nodeTypeName;

  private List<CsPropertyMapping> addedProperties   = new ArrayList<CsPropertyMapping>();

  private List<CsPropertyMapping> removedProperties = new ArrayList<CsPropertyMapping>();

  private List<CsPropertyMapping> updatedProperties = new ArrayList<CsPropertyMapping>();

  public void setAddedProperties(List<CsPropertyMapping> addedProperties) {
    this.addedProperties = addedProperties;
  }

  public List<CsPropertyMapping> getAddedProperties() {
    return addedProperties;
  }

  public void setRemovedProperties(List<CsPropertyMapping> removeProperties) {
    this.removedProperties = removeProperties;
  }

  public List<CsPropertyMapping> getRemovedProperties() {
    return removedProperties;
  }

  public void setUpdatedProperties(List<CsPropertyMapping> updatedProperties) {
    this.updatedProperties = updatedProperties;
  }

  public List<CsPropertyMapping> getUpdatedProperties() {
    return updatedProperties;
  }

  public void setNodeTypeName(String nodeTypeName) {
    this.nodeTypeName = nodeTypeName;
  }

  public String getNodeTypeName() {
    return nodeTypeName;
  }

}
