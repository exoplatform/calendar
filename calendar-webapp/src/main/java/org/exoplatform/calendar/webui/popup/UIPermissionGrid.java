package org.exoplatform.calendar.webui.popup;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.exoplatform.calendar.webui.popup.UICalendarPermissionTab.Permission;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * a grid that contains a list of permission entry
 *
 * @author Created by The eXo Platform SAS
 *         <br/>Anh-Tu Nguyen
 *         <br/><a href="mailto:tuna@exoplatform.com">tuna@exoplatform.com<a/>
 *         <br/>Dec 07, 2012
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template  = "app:/templates/calendar/webui/UIPopup/UIPermissionGrid.gtmpl"
)
public class UIPermissionGrid extends UIContainer
{
  private static final Log LOG = ExoLogger.getExoLogger(UIPermissionGrid.class);

  public UIPermissionGrid(String componentId)
  {
    setId(componentId);
    setComponentConfig(getClass(), null);
  }

  public void addEntry(Permission aPermission) throws Exception
  {
    LOG.info("add entry");
    UIPermissionEntry permissionEntry = new UIPermissionEntry(aPermission);
    addChild(permissionEntry);
  }

}
