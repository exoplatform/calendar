package org.exoplatform.calendar.webui.popup;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.calendar.webui.popup.UISharedForm.Permission;

import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * a grid that contains a list of permission entry
 *
 * @author Created by The eXo Platform SEA
 *         <br/>Anh-Tu Nguyen
 *         <br/><a href="mailto:tuna@exoplatform.com">tuna@exoplatform.com<a/>
 *         <br/>Jan 25, 2013
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template  = "app:/templates/calendar/webui/UIPopup/UIPermissionGrid.gtmpl"
)
public class UIPermissionGrid extends UIContainer
{
  public UIPermissionGrid(String componentId)
  {
    setId(componentId);
    setComponentConfig(getClass(), null);
  }

  /**
   * add entry to grid to display
   *
   * @param aPermission
   * @throws Exception
   */
  public void addEntry(Permission aPermission) throws Exception
  {
    UIPermissionEntry permissionEntry = getChildById(UISharedForm.PERMISSION_ENTRY + CalendarUtils.DOT + aPermission.hashCode());
    if (permissionEntry == null)
    {
      permissionEntry = new UIPermissionEntry(aPermission);
      addChild(permissionEntry);
    }
    else /* probably the entry is disabled */
      permissionEntry.setRendered(true);
  }

  /**
   * un-display a permission entry
   */
  public void removeEntry(String permissionId)
  {
    UIPermissionEntry permissionEntry = getChildById(UISharedForm.PERMISSION_ENTRY + CalendarUtils.DOT + Math.abs(permissionId.hashCode()));
    if (permissionEntry != null)
    {
      permissionEntry.setRendered(false);
    }
  }
}
