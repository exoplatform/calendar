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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.portal.config.UserACL;

public class RestSecurityServiceImpl implements RestSecurityService {

  private PermissionConfig config = new PermissionConfig();

  private UserACL userACL;

  public static String NOBODY = "Nobody";

  public RestSecurityServiceImpl(UserACL userACL) {
    this.userACL = userACL;
  }

  public boolean hasPermission(String requestPath) {
    if (requestPath != null) {      
      for (String permission : config.getPermission(requestPath)) {
        //Remove nobody permission checking after upgrading to gatein 3.7.x
        permission = NOBODY.equalsIgnoreCase(permission) ? null : permission;
        
        if (!userACL.hasPermission(permission)) {
          return false;
        }
      }
    }

    //
    return true;
  }

  public void addPermission(PermissionPlugin config) {
    Map<String, String> perConfig = config.getConfig();
    for (Entry<String, String> c : perConfig.entrySet()) {      
      addPermission(c.getKey(), c.getValue());
    }
  }
  
  public void addPermission(String path, String permission) {
    synchronized (config) {
      config.addConfig(path, permission);      
    }
  }
  
  public static class PermissionConfig {
    private Map<String, PermissionConfig> childs = new HashMap<String, PermissionConfig>();
    private String permission;
    
    public Set<String> getPermission(String path) {
      List<String> fragments = getFragments(path);
      
      Set<String> result = new HashSet<String>();
      if (this.permission != null) {
        result.add(this.permission);        
      }

      PermissionConfig current = this;
      for (String fragment : fragments) {
        current = current.childs.get(fragment);        
        if (current != null) {
          if (current.permission != null) {
            result.add(current.permission);            
          }
        } else {
          break;
        }
      }
      return result;
    }
    
    public void addConfig(String path, String permission) {
      List<String> fragments = getFragments(path);
      PermissionConfig current = this;
      
      for (String fragment : fragments) {        
        PermissionConfig child = current.childs.get(fragment);
        if (child != null) {
          current  = child; 
        } else {
          child = new PermissionConfig();
          current.childs.put(fragment, child);
          current = child;
        }
      }
      current.permission = permission;      
    }
    
    private List<String> getFragments(String path) {
      List<String> fragments = new LinkedList<String>();
      
      if (path != null) {
        String[] tmp = path.split("/");
        for (String s : tmp) {
          s = s.trim();
          if (!s.isEmpty()) {
            fragments.add(s);
          }
        }        
      }
      return fragments;      
    }
  }

  /**
   * The permission follow pattern: membershipType:groupId. For example: *:/platform/administrators <br/>  
   */
  public static class PermissionPlugin extends BaseComponentPlugin {
    private Map<String, String> config = new HashMap<String, String>();
    
    public PermissionPlugin(InitParams params) {
      if (params != null) {
        @SuppressWarnings("unchecked")
        Iterator<PropertiesParam> iter = params.getPropertiesParamIterator();
        while (iter.hasNext()) {
          PropertiesParam param = iter.next();
          Iterator<Property> props = param.getPropertyIterator();
          while (props.hasNext()) {
            Property permission = props.next();
            config.put(permission.getName().trim(), permission.getValue().trim());
          }
        }
      }
    }
    
    public Map<String, String> getConfig() {
      return config;
    }
  }
}
