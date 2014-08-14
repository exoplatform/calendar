/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.calendar.webui.validator;

import java.io.IOException;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.webui.popup.UIInvitationForm;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.CompoundApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;

public class EventParticipantValidator extends UserConfigurableValidator {
    public EventParticipantValidator() {
        super(UserConfigurableValidator.EMAIL, UserConfigurableValidator.DEFAULT_LOCALIZATION_KEY);
    }

    @Override
    public void validate(UIFormInput uiInput) throws Exception {
        StringBuilder invalidParticipals = new StringBuilder();
        int noInvalidParticipals = 0;

        String label = getLabelFor(uiInput);
        OrganizationService orgService = CalendarUtils.getOrganizationService();
        String values = (String)uiInput.getValue();

        for(String p : values.split(UIInvitationForm.NEW_LINE)) {
            p = p.trim();
            if(p.isEmpty()) continue;

            if(p.indexOf('@') != -1) {
                //. Validate email: delegate to UserConfigurableValidator
                CompoundApplicationMessage msg = new CompoundApplicationMessage();
                validate(p, label, msg, uiInput);

                if (!msg.isEmpty()) {
                    this.appendInvalidParticipals(p, invalidParticipals);
                    noInvalidParticipals++;
                }
            } else {
                //. Validate username
                if(!CalendarUtils.isUserExisted(orgService, p)) {
                    this.appendInvalidParticipals(p, invalidParticipals);
                    noInvalidParticipals++;
                }
            }
        }

        if(noInvalidParticipals > 0) {
            String msg = noInvalidParticipals == 1 ? "UIEventForm.msg.event-participant-invalid" : "UIEventForm.msg.events-participants-invalid";
            CompoundApplicationMessage messages = new CompoundApplicationMessage();
            messages.addMessage(msg, new String[]{invalidParticipals.toString()});

            throw new MessageException(messages);
        }
    }

    private void appendInvalidParticipals(String participal, StringBuilder buffer) throws IOException {
        if(buffer.length() > 0) {
            buffer.append(", ");
        }
        buffer.append(participal);
    }
}
