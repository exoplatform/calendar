/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.calendar.webui.popup;

import java.io.Writer;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 28, 2013  
 */
public class UICheckBoxInput extends org.exoplatform.webui.form.input.UICheckBoxInput{
  public UICheckBoxInput(String fieldId, String fieldName, boolean b) {
    super(fieldId, fieldName, b);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    String label = getLabel();
    if(label == null) {
      label = getId() + ".label";
      try {
        ResourceBundle res = context.getApplicationResourceBundle();
        label = res.getString(label);
      } catch (MissingResourceException e) {
        label = null;
      }
    }
    Writer w = context.getWriter();
    if (label != null) {
      w.write("<label class=\"uiCheckbox\">");
    } else {
      w.write("<span class=\"uiCheckbox\">");
    }
    w.append("<input id=\"").append(getId()).append("\" type=\"checkbox\" class=\"checkbox\" name=\"");
    w.write(name);
    w.write("\"");
    if (isChecked())
      w.write(" checked");
    if (isDisabled())
      w.write(" disabled");

    renderHTMLAttributes(w);

    w.write("/><span>");
    if (label != null) {
      w.write(label);
      w.write("</span></label>");
    } else {
      w.write("</span></span>");
    }
    if (this.isMandatory())
      w.write(" *");
  }

}
