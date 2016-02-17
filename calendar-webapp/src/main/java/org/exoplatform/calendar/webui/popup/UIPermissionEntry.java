package org.exoplatform.calendar.webui.popup;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.webui.popup.UISharedForm.Permission;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * represents an entry for permission in the grid
 *
 * @author Created by The eXo Platform SAS
 *         <br>Anh-Tu Nguyen
 *         <br><a href="mailto:tuna@exoplatform.com">tuna@exoplatform.com</a>
 *         <br>Jan 28, 2013
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template  = "app:/templates/calendar/webui/UIPopup/UIPermissionEntry.gtmpl"
)

/**
 * represents the ui for a permission with its owner and rights
 * - id is set to UIPermissionEntry.<hashcode-of-permission>
 *
 **/
public class UIPermissionEntry extends UIContainer
{
  /* contains a permission to display */
  private Permission permission;

  public static final String CHECKBOX = "UICheckBoxInput";

  public UIPermissionEntry(Permission aPermission)
  {
    permission = aPermission;
    /* id set to UIPermissionEntry.<hashcode_of_permission> */
    setId(UISharedForm.PERMISSION_ENTRY + CalendarUtils.DOT + permission.hashCode());
    setComponentConfig(getClass(), null);

    /**
     * add a checkbox input, set id of checkbox to UICheckBoxInput.<hashcode-of-permission>
     */
    UICheckBoxInput checkBoxInput = new UICheckBoxInput(CHECKBOX + CalendarUtils.DOT + permission.hashCode(),
        null, permission.hasEditPermission());
    addChild(checkBoxInput);
  }

  public boolean isTheCheckBoxChecked()
  {
    return ((UICheckBoxInput) getChildById(CHECKBOX + CalendarUtils.DOT + permission.hashCode())).getValue();
  }

  public Permission getPermission()
  {
    return permission;
  }
}
