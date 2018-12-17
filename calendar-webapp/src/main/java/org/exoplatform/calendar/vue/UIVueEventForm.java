/**
 * Copyright (C) 2018 eXo Platform SAS.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.calendar.vue;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

@ComponentConfig(
        template = "app:/templates/calendar/vue/UIVueEventForm.gtmpl"
)
public class UIVueEventForm extends UIContainer {

    private static final Log LOG = ExoLogger.getExoLogger(UIVueEventForm.class);

    public UIVueEventForm() {
        this.setId("UIVueEventForm");
    }
}
