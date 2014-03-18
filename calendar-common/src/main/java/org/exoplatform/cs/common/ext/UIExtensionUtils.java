/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.cs.common.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 16, 2010  
 */
public final class UIExtensionUtils {
  private static final Log logger = ExoLogger.getLogger(UIExtensionUtils.class);
  
  /**
   * get list of ui components by extenstion type and parent component.
   * @param extensionType - type of ui extenstion
   * @param parent - parent component
   * @param context
   * @return map of extension name and ui component.
   */
  public static Map<String, UIComponent> getComponents(String extensionType, UIContainer parent, Map<String, Object> context) {
    Map<String, UIComponent> components = new HashMap<String, UIComponent>();
    UIExtensionManager manager = (UIExtensionManager) PortalContainer.getInstance().getComponentInstance(UIExtensionManager.class);
    if (manager != null) {
      List<UIExtension> extensions = manager.getUIExtensions(extensionType);
      if (extensions != null) {
        for (UIExtension extension : extensions) {
          try {
            UIComponent component = manager.addUIExtension(extension, context, parent);
            if (component != null) components.put(extension.getName(), component);
          } catch (Exception e) {
            if (logger.isDebugEnabled()) logger.debug("failed to get ui component of uiextension: type=" + extensionType, e);
          }
        }
      }
    }
    return components;
  }
}
