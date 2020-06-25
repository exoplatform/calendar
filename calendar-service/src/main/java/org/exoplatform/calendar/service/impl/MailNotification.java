/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.calendar.service.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.util.CalendarUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.DateUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.Constants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.ConversationState;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MailNotification {

  public static final   String FIELD_MESSAGE = "messageName" ;
  public static final   String FIELD_EVENT = "eventName";
  public static final   String FIELD_DESCRIPTION = "description";
  public static final   String FIELD_ATTACHMENTS = "attachments";
  public static final   String FIELD_FROM = "from";
  public static final   String FIELD_TO = "to";
  public static final   String FIELD_PLACE = "place";
  public static final   String FIELD_MEETING = "participant";
  public static final   String EXO_EMAIL_SMTP_FROM = System.getProperty("exo.email.smtp.from", "noreply@exoplatform.com");



  private static final  AtomicBoolean                 isRBLoaded           = new AtomicBoolean();

  private static final Log      LOG = ExoLogger.getExoLogger(MailNotification.class);

  public static void sendEmail(CalendarEvent event, String username) throws Exception {
    //TODO remove invitor
    if(StringUtils.isBlank(username)){
      throw new IllegalArgumentException("Username could not be be null or empty");
    }
    User invitor = (User) ConversationState.getCurrent().getAttribute("UserProfile");
    if (invitor == null)
      return;
    List<Attachment> atts = event.getAttachment();
    Map<String, String> eXoIdMap = new HashMap<>();   

    String toDisplayName = getParticiapntsDisplayName(event);
    String emailList = getEmailsInivtationList(event);

    for (String s : event.getParticipant()) {
      User user = CommonsUtils.getOrganizationService().getUserHandler().findUserByName(s);
      eXoIdMap.put(user.getEmail(), s);
    }

    User user = CommonsUtils.getOrganizationService().getUserHandler().findUserByName(username);
    byte[] icsFile;
    try (OutputStream out = CommonsUtils.getService(CalendarService.class).getCalendarImportExports(CalendarService.ICALENDAR)
                                           .exportEventCalendar(username,
                                                                event.getCalendarId(),
                                                                event.getCalType(),
                                                                event.getId())) {
      icsFile = out.toString().getBytes("UTF-8");
    }

    String userId;
    for (String userEmail : emailList.split(CalendarUtils.COMMA)) {
      if (CalendarUtils.isEmpty(userEmail)) continue;

      userId = eXoIdMap.get(userEmail);
      ResourceBundle res = null;

      CalendarSetting calendarSetting = CommonsUtils.getService(CalendarService.class).getCalendarSetting(userId);
      UserProfile userProfile = CommonsUtils.getOrganizationService().getUserProfileHandler().findUserProfileByName(userId);
      String lang = userProfile == null ? null : userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE);
      ResourceBundleService ressourceBundleService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ResourceBundleService.class);
      if (lang != null && !lang.isEmpty()) {
        res = ressourceBundleService.getResourceBundle(Utils.RESOURCEBUNDLE_NAME, LocaleContextInfo.getLocale(lang));
      }

      if (res == null) {
        res = getResourceBundle();
      }

      DateFormat df = new SimpleDateFormat(calendarSetting.getDateFormat() + " " + calendarSetting.getTimeFormat());
      df.setTimeZone(DateUtils.getTimeZone(calendarSetting.getTimeZone()));

      org.exoplatform.services.mail.Message message = new org.exoplatform.services.mail.Message();
      message.setSubject(buildMailSubject(event, df, res));
      message.setBody(getBodyMail(buildMailBody(invitor,
                                                event,
                                                toDisplayName,
                                                df,
                                                CalendarUtils.generateTimeZoneLabel(calendarSetting.getTimeZone()),
                                                res),
                                  eXoIdMap,
                                  userEmail,
                                  invitor,
                                  event,
                                  res));
      message.setTo(userEmail);
      message.setMimeType(Utils.MIMETYPE_TEXTHTML);
      message.setFrom(user.getDisplayName() + "<" + EXO_EMAIL_SMTP_FROM + ">");
      message.setReplyTo(user.getEmail());

      if (icsFile != null) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(icsFile)) {
          org.exoplatform.services.mail.Attachment attachmentCal = new org.exoplatform.services.mail.Attachment();
          attachmentCal.setInputStream(is);
          attachmentCal.setName("icalendar.ics");
          attachmentCal.setMimeType("text/calendar");
          message.addAttachment(attachmentCal);
        }
      }

      if (!atts.isEmpty()) {
        for (Attachment att : atts) {
          org.exoplatform.services.mail.Attachment attachment = new org.exoplatform.services.mail.Attachment();
          attachment.setInputStream(att.getInputStream());
          attachment.setMimeType(att.getMimeType());
          attachment.setName(att.getName());
          message.addAttachment(attachment);
        }
      }
      CommonsUtils.getService(MailService.class).sendMessage(message);
    }
  }

  public static String getEmailsInivtationList(CalendarEvent event) throws Exception {
    User invitor = (User) ConversationState.getCurrent().getAttribute("UserProfile");
    StringBuilder emaillist = new StringBuilder("");
    for (String s : event.getParticipant()) {
      User user = CommonsUtils.getOrganizationService().getUserHandler().findUserByName(s);
      if (user == null)  {
        continue;
      }
      if(! user.getEmail().equals(invitor.getEmail())) {
        if (emaillist.length() > 0) {
          emaillist.append(",");
        }
        emaillist.append(user.getEmail());
      }
    }
    return String.valueOf(emaillist);
  }

  public static String getParticiapntsDisplayName(CalendarEvent event) throws Exception{
    StringBuilder usersDisplayNameList = new StringBuilder("");
    for (String s : event.getParticipant()) {
      User user = CommonsUtils.getOrganizationService().getUserHandler().findUserByName(s);
      if (user == null)  {
        continue;
      }
      if (usersDisplayNameList.length() > 0) {
        usersDisplayNameList.append(",");
      }
      usersDisplayNameList.append(user.getDisplayName());
    }
    return String.valueOf(usersDisplayNameList);
  }

  private static String getBodyMail(Object sbBody,
                             Map<String, String> eXoIdMap,
                             String userEmail,
                             User invitor,
                             CalendarEvent event,
                             ResourceBundle res) throws Exception {
    StringBuilder body = new StringBuilder(sbBody.toString());
    String eXoId = CalendarUtils.isEmpty(eXoIdMap.get(userEmail)) ? "null":eXoIdMap.get(userEmail);
    body.append("<tr>");
    body.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">");
    body.append(" </td><td> <a href=\""
        + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.ACCEPT, invitor, userEmail, eXoId, event) + "\" >"
        + getLabel(res, "yes") + "</a>" + " - " + "<a href=\""
        + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.NOTSURE, invitor, userEmail, eXoId, event) + "\" >"
        + getLabel(res, "notSure") + "</a>" + " - " + "<a href=\""
        + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.DENY, invitor, userEmail, eXoId, event) + "\" >"
        + getLabel(res, "no") + "</a>");
    body.append("</td></tr>");
    body.append("<tr>");
    body.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">");
    body.append(getLabel(res, "seeMoreDetails") + " </td><td><a href=\""
        + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.ACCEPT_IMPORT, invitor, userEmail, eXoId, event) + "\" >"
        + getLabel(res, "importToExoCalendar") + "</a> " + getLabel(res, "or") + " <a href=\""
        + getReplyInvitationLink(org.exoplatform.calendar.service.Utils.JUMP_TO_CALENDAR, invitor, userEmail, eXoId, event)
        + "\" >" + getLabel(res, "jumpToExoCalendar") + "</a>");
    body.append("</td></tr>");
    body.append("</tbody>");
    body.append("</table>");
    body.append("</div>");
    return body.toString();
  }

  private static String getReplyInvitationLink(int answer,
                                        User invitor,
                                        String invitee,
                                        String eXoId,
                                        CalendarEvent event) throws Exception {
    String portalURL = CalendarUtils.getServerBaseUrl() + "/" + PortalContainer.getCurrentPortalContainerName();
    String restURL = portalURL + "/" + PortalContainer.getCurrentRestContextName();
    String calendarURL = portalURL + "/intranet/calendar";

    if (answer == org.exoplatform.calendar.service.Utils.ACCEPT || answer == org.exoplatform.calendar.service.Utils.DENY
        || answer == org.exoplatform.calendar.service.Utils.NOTSURE) {
      return (restURL + "/cs/calendar" + CalendarUtils.INVITATION_URL + event.getCalendarId() + "/" + event.getCalType() + "/"
          + event.getId() + "/" + invitor.getUserName() + "/" + invitee + "/" + eXoId + "/" + answer);
    }
    if (answer == org.exoplatform.calendar.service.Utils.ACCEPT_IMPORT) {
      return (calendarURL + CalendarUtils.INVITATION_IMPORT_URL + invitor.getUserName() + "/" + event.getId() + "/" + event.getCalType());
    }
    if (answer == org.exoplatform.calendar.service.Utils.JUMP_TO_CALENDAR) {
      return (calendarURL + CalendarUtils.INVITATION_DETAIL_URL + invitor.getUserName() + "/" + event.getId() + "/" + event.getCalType());
    }
    return "";
  }

  private static String buildMailBody(User invitor,
                               CalendarEvent event,
                               String toDisplayName,
                               DateFormat df,
                               String timezone,
                               ResourceBundle res) throws Exception {
    List<Attachment> atts = event.getAttachment();

    StringBuilder sbBody = new StringBuilder();
    sbBody.append("<div style=\"margin: 20px auto; padding: 8px; background: rgb(224, 236, 255) none repeat scroll 0%; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; width: 500px;\">");
    sbBody.append("<table style=\"margin: 0px; padding: 0px; border-collapse: collapse; border-spacing: 0px; width: 100%; line-height: 16px;\">");
    sbBody.append("<tbody>");
    sbBody.append("<tr>");
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap; \">"
        + getLabel(res, "fromWho") + ":</td>");
    sbBody.append("<td style=\"padding: 4px;\"> " + invitor.getDisplayName() + " (" + invitor.getEmail() + ")" + " </td>");
    sbBody.append("</tr>");

    sbBody.append("<tr>");
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"
        + getLabel(res, FIELD_MESSAGE) + ":</td>");
    sbBody.append("<td style=\"padding: 4px;\">" + event.getMessage() + "</td>");
    sbBody.append("</tr>");

    sbBody.append("<tr>");
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"
        + getLabel(res, FIELD_EVENT) + ":</td>");
    sbBody.append("<td style=\"padding: 4px;\">" + event.getSummary() + "</td>");
    sbBody.append("</tr>");
    sbBody.append("<tr>");
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"
        + getLabel(res, FIELD_DESCRIPTION) + ":</td>");
    sbBody.append("<td style=\"padding: 4px;\">"
        + (event.getDescription() != null && event.getDescription().trim().length() > 0 ? event.getDescription() : " ")
        + "</td>");
    sbBody.append("</tr>");
    sbBody.append("<tr>");
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"
        + getLabel(res, "when") + ":</td>");
    sbBody.append("<td style=\"padding: 4px;\"> <div>" + getLabel(res, FIELD_FROM) + ": "
        + df.format(event.getFromDateTime()) + " " + timezone + "</div>");
    sbBody.append("<div>" + getLabel(res, FIELD_TO) + ": " + df.format(event.getToDateTime()) + " " + timezone
        + "</div></td>");
    sbBody.append("</tr>");
    sbBody.append("<tr>");
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"
        + getLabel(res, FIELD_PLACE) + ":</td>");
    sbBody.append("<td style=\"padding: 4px;\">"
        + (event.getLocation() != null && event.getLocation().trim().length() > 0 ? event.getLocation() : " ") + "</td>");
    sbBody.append("</tr>");
    sbBody.append("<tr>");
    sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"
        + getLabel(res, FIELD_MEETING) + "</td>");
    sbBody.append("<td style=\"padding: 4px;\">" + toDisplayName + "</td>");
    sbBody.append("</tr>");
    if (!atts.isEmpty()) {
      sbBody.append("<tr>");
      sbBody.append("<td style=\"padding: 4px;  text-align: right; vertical-align: top; white-space:nowrap;\">"
          + getLabel(res, FIELD_ATTACHMENTS) + ":</td>");
      StringBuilder sbf = new StringBuilder();
      for (Attachment att : atts) {
        if (sbf.length() > 0)
          sbf.append(",");
        sbf.append(att.getName());
      }
      sbBody.append("<td style=\"padding: 4px;\"> (" + atts.size() + ") " + sbf.toString() + " </td>");
      sbBody.append("</tr>");
    }

    return sbBody.toString();
  }

  private static String buildMailSubject(CalendarEvent event, DateFormat df, ResourceBundle res) {
    StringBuilder sbSubject = new StringBuilder("[" + getLabel(res, "invitation") + "] ");
    sbSubject.append(event.getSummary());
    Date fromDateTime = event.getFromDateTime();
    if(fromDateTime != null) {
      sbSubject.append(" ");
      sbSubject.append(df.format(fromDateTime));
    }

    return sbSubject.toString();
  }

  public static String getLabel(ResourceBundle res, String label) {
    if(res != null) {
      String resKey = ".label." + label;
      try {
        return res.getString(resKey);
      } catch (MissingResourceException e) {
        return label;
      }
    } else {
      return label;
    }
  }
  
  public static ResourceBundle getResourceBundle() throws Exception {
    ResourceBundle resourceBundle = null;
    if (!isRBLoaded.get()) {
      synchronized (isRBLoaded) {
        if (!isRBLoaded.get()) {
          try {
            resourceBundle = CommonsUtils.getService(ResourceBundleService.class).getResourceBundle(Utils.RESOURCEBUNDLE_NAME, Locale.getDefault());
          } catch (MissingResourceException e) {
            resourceBundle = null;
          }
          isRBLoaded.set(true);
        }
      }
    } 
    return resourceBundle;
  }
}
