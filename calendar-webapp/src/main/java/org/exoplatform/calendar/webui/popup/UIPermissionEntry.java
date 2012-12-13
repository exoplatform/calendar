package org.exoplatform.calendar.webui.popup;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.calendar.webui.popup.UICalendarPermissionTab.Permission;
import org.exoplatform.calendar.webui.popup.UICalendarPermissionTab.PermissionType;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.form.input.UICheckBoxInput;

import java.util.Iterator;

/**
 * represents an entry for permission in the grid
 *
 * @author Created by The eXo Platform SAS
 *         <br/>Anh-Tu Nguyen
 *         <br/><a href="mailto:tuna@exoplatform.com">tuna@exoplatform.com<a/>
 *         <br/>Dec 10, 2012
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template  = "app:/templates/calendar/webui/UIPopup/UIPermissionEntry.gtmpl"
)
public class UIPermissionEntry extends UIContainer
{
  private static final Log LOG = ExoLogger.getExoLogger(UIPermissionEntry.class);

  /* contains a permission to display */
  private Permission permission;

  public static final String CHECKBOX = "UICheckBoxInput";

  public static final String PERMISSION_ENTRY = "UIPermissionEntry";

  public UIPermissionEntry(Permission aPermission)
  {
    permission = aPermission;
    setId(PERMISSION_ENTRY);
    setComponentConfig(getClass(), null);

    /* add a checkbox input for each permission type */
    UICheckBoxInput checkBoxInput;
    Iterator<PermissionType> it = permission.getPermissionTypeList().values().iterator();
    while (it.hasNext())
    {
      PermissionType type = it.next();
      /* set id of checkbox to UICheckBoxInput.view_calendar or edit_calendar  */
      checkBoxInput = new UICheckBoxInput(CHECKBOX + CalendarUtils.DOT + type.getType(), "", type.isAllowed());
      addChild(checkBoxInput);
    }
  }

  public Permission getPermission()
  {
    return permission;
  }
}
