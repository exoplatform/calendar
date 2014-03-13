/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.calendar.webui;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.webui.popup.UIEventForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIConfirmation;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * Author : Nguyen Quang Hung
 *          hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class, 
                 template = "app:/templates/calendar/webui/UICalendarPortlet.gtmpl"
    )
public class UICalendarPortlet extends UIPortletApplication {

  private static Log log = ExoLogger.getLogger(UICalendarPortlet.class);

  private String spaceGroupId;

  public UICalendarPortlet() throws Exception {
    addChild(UIConfirmation.class, null, null);
    UIActionBar uiActionBar = addChild(UIActionBar.class, null, null) ;
    uiActionBar.setCurrentView(CalendarUtils.getViewInSetting()) ;
    addChild(UICalendarWorkingContainer.class, null, null) ;
    UIPopupAction uiPopup =  addChild(UIPopupAction.class, null, null) ;
    uiPopup.setId("UICalendarPopupAction") ;
    uiPopup.getChild(UIPopupWindow.class).setId("UICalendarPopupWindow") ;
  }
  public CalendarSetting getCalendarSetting() throws Exception{
    return CalendarUtils.getCurrentUserCalendarSetting(); 
  }
  public void setCalendarSetting(CalendarSetting setting) throws Exception{
    CalendarUtils.setCurrentCalendarSetting(setting); 
  }

  /**
   * @return a calendar that contains configuration of the user, such as: Time zone, First day of week.
   * @throws Exception
   */
  public Calendar getUserCalendar() {    
    return CalendarUtils.getInstanceOfCurrentCalendar();
  }

  public String getSettingTimeZone() throws Exception {
    TimeZone tz = TimeZone.getTimeZone(getCalendarSetting().getTimeZone());
    //get time zone offset in a specified date to take day light saving into account
    long timezoneOffset = tz.getOffset(Calendar.getInstance().getTimeInMillis());
    return String.valueOf(timezoneOffset/1000/60) ;
  }

  public String getWeekStartOn() throws Exception {
    return getCalendarSetting().getWeekStartOn();
  }

  /**
   * close all popups
   *
   * @throws Exception
   */
  public void cancelAction() throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }

  public String getRemoteUser() throws Exception {
    return CalendarUtils.getCurrentUser() ;
  }
  public String getUserToken()throws Exception {
    ContinuationService continuation = CalendarUtils.getContinuationService() ;
    try {
      return continuation.getUserToken(this.getRemoteUser());
    } catch (Exception e) {
      log.debug("\n\n can not get UserToken", e);
      return "" ;
    }
  }

  protected String getCometdContextName() {
    EXoContinuationBayeux bayeux = (EXoContinuationBayeux) PortalContainer.getInstance()
        .getComponentInstanceOfType(AbstractBayeux.class);
    return (bayeux == null ? "cometd" : bayeux.getCometdContextName());
  }

  public String getRestContextName() {
    return PortalContainer.getInstance().getRestContextName();
  }

  /**
   * get space id if the request comes from one Social space, else return empty string.
   * @return 
   */
  public static String getSpaceId() {
    String spaceIdStr = "";
    PortalRequestContext pContext = Util.getPortalRequestContext();
    String requestPath = pContext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    ExoRouter.Route er = ExoRouter.route(requestPath);
    if(er == null) return spaceIdStr;
    String spacePrettyName = er.localArgs.get("spacePrettyName");
    SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
    Space space = sService.getSpaceByPrettyName(spacePrettyName);
    if (space == null) return spaceIdStr;
    spaceIdStr = space.getId();
    return spaceIdStr == null ? "" : spaceIdStr;
  }

  public static String getGroupIdOfSpace() {
    String spaceGroupId = "";
    PortalRequestContext pContext = Util.getPortalRequestContext();
    String requestPath = pContext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    ExoRouter.Route er = ExoRouter.route(requestPath);
    if (er == null) return spaceGroupId;
    String spacePrettyName = er.localArgs.get("spacePrettyName");
    SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
    Space space = sService.getSpaceByPrettyName(spacePrettyName);
    if (space == null) return spaceGroupId;
    spaceGroupId = space.getGroupId();
    return spaceGroupId == null ? "" : spaceGroupId;
  }

  public String getSpaceGroupId() {
    if (spaceGroupId != null) return spaceGroupId;

    String spaceIdStr = "";
    PortalRequestContext pContext = Util.getPortalRequestContext();
    String requestPath = pContext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    ExoRouter.Route er = ExoRouter.route(requestPath);
    spaceGroupId = spaceIdStr;
    if (er == null) return spaceIdStr;
    String spacePrettyName = er.localArgs.get("spacePrettyName");
    SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
    Space space = sService.getSpaceByPrettyName(spacePrettyName);
    spaceGroupId = spaceIdStr;
    if (space == null) return spaceIdStr;
    spaceIdStr = space.getGroupId();
    spaceGroupId = (spaceIdStr == null ? "" : spaceIdStr);
    return spaceGroupId;
  }


  public static boolean isInSpace() {
    return !getSpaceId().equals("");
  }

  public boolean isInSpaceContext() {
    return !getSpaceGroupId().equals("");
  }

  public void processInvitationURL(WebuiRequestContext context, PortalRequestContext pContext, String url) throws Exception
  {
    String isAjax = pContext.getRequestParameter("ajaxRequest");
    if(isAjax != null && Boolean.parseBoolean(isAjax)) return;
    String username = CalendarUtils.getCurrentUser();
    User user = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(username);
    String formTime = CalendarUtils.getCurrentTime(this) ;
    CalendarService calService = CalendarUtils.getCalendarService();
    if (url.contains(CalendarUtils.INVITATION_IMPORT_URL)) {
      // import to personal calendar
      url = url.substring(url.indexOf(CalendarUtils.INVITATION_IMPORT_URL) + CalendarUtils.INVITATION_IMPORT_URL.length());
      String[] params = url.split("/");
      String inviter = params[0];
      String eventId = params[1];
      int calType = Integer.parseInt(params[2]);
      CalendarEvent event = calService.getEventById(eventId);
      if (event != null) {
        // update status
        event.setCalType(String.valueOf(calType));
        calService.confirmInvitation(inviter, user.getEmail(), username, calType, event.getCalendarId(), eventId, Utils.ACCEPT);
        // pop-up event form
        UIPopupAction uiParentPopup = this.getChild(UIPopupAction.class);
        UIPopupContainer uiPopupContainer = uiParentPopup.activate(UIPopupContainer.class, 800);
        uiPopupContainer.setId(UIPopupContainer.UIEVENTPOPUP);
        UIEventForm uiEventForm =  uiPopupContainer.addChild(UIEventForm.class, null, null) ;
        uiEventForm.initForm(this.getCalendarSetting(), null, formTime);
        uiEventForm.update(CalendarUtils.PRIVATE_TYPE, CalendarUtils.getCalendarOption()) ;
        uiEventForm.importInvitationEvent(this.getCalendarSetting(), event, Utils.getDefaultCalendarId(username), formTime);
        uiEventForm.setSelectedEventState(UIEventForm.ITEM_BUSY) ;
        uiEventForm.setEmailRemindBefore(String.valueOf(5));
        uiEventForm.setEmailReminder(false) ;
        uiEventForm.setEmailRepeat(false) ;
        context.addUIComponentToUpdateByAjax(uiParentPopup);
      } else {
        context.getUIApplication().addMessage(new ApplicationMessage("UICalendarPortlet.msg.event-was-not-found", null, ApplicationMessage.ERROR));
      }
      return;
    }

    if (url.contains(CalendarUtils.INVITATION_DETAIL_URL)) {
      // open event on source calendar to view
      url = url.substring(url.indexOf(CalendarUtils.INVITATION_DETAIL_URL) + CalendarUtils.INVITATION_DETAIL_URL.length());
      String[] params = url.split("/");
      String eventId = params[1];
      int calType = Integer.parseInt(params[2]);
      CalendarEvent event = calService.getEventById(eventId);
      event.setCalType(String.valueOf(calType));
      org.exoplatform.calendar.service.Calendar calendar = calService.getCalendarById(event.getCalendarId());
      if (calendar == null)
      {
        context.getUIApplication().addMessage(
                                              new ApplicationMessage("UICalendarPortlet.msg.have-no-permission-to-view-event", null, ApplicationMessage.WARNING ));
        return ;
      }
      openEventPreviewPopup(event, context);
    }
  }


  /**
   * process event details url to show a popup to display event details
   * url like <calendar_app>/details/<event-id>
   *
   * @param webuiRequestContext
   * @param eventId
   * @throws Exception
   */
  private void processEventDetailsURL(WebuiRequestContext webuiRequestContext, String eventId, String recurId) throws Exception
  {
    CalendarService calService = CalendarUtils.getCalendarService();
    CalendarEvent event = null ;
    String username = CalendarUtils.getCurrentUser();
    if(recurId != null && !recurId.isEmpty()) {
      CalendarSetting calSetting = calService.getCalendarSetting(username);
      String timezoneId = calSetting.getTimeZone();
      TimeZone timezone = TimeZone.getTimeZone(timezoneId);
      CalendarEvent orgEvent = calService.getEventById(eventId); // the repetitive event of which we need to find the occurrence
      if(orgEvent != null) {
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT_RECUR_ID);
        sdf.setTimeZone(timezone);
        Date occurDate = sdf.parse(recurId); // get the date that the occurrence appear in the time table
        java.util.Calendar cal = java.util.Calendar.getInstance(timezone);
        cal.setTime(occurDate);
        java.util.Calendar from = Utils.getBeginDay(cal);
        java.util.Calendar to = Utils.getEndDay(cal);
        /* Here we get occurrences of the repetitive event in the occurDate 
         * so that the result must be <recurId, occurrence> (occurrence: the occurrence event that we are searching for)
         */
        Map<String, CalendarEvent> occMap = calService.getOccurrenceEvents(orgEvent, from, to, timezoneId);
        event = occMap.get(recurId);
      }
    } else {
      /* find event from username and event id */
      event = calService.getEventById(eventId) ;
    }
    if (event == null){
      webuiRequestContext.getUIApplication().addMessage(new ApplicationMessage("UICalendarPortlet.msg.have-no-permission-to-view-event", null, ApplicationMessage.WARNING ));
    } else {
      event.setCalType(String.valueOf(calService.getTypeOfCalendar(username, event.getCalendarId())));
      openEventPreviewPopup(event, webuiRequestContext);
    }
  }

  /**
   * open an popup to display event or task details
   *
   * @param event
   * @param webuiRequestContext
   * @throws Exception
   */
  private void openEventPreviewPopup(CalendarEvent event, WebuiRequestContext webuiRequestContext) throws Exception
  {
    UIPopupAction uiPopupAction = getChild(UIPopupAction.class);
    uiPopupAction.deActivate();
    UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class, 700);
    uiPopupContainer.setId("UIEventPreview");
    uiPopupAction.getChild(UIPopupWindow.class).setShowCloseButton(false);
    UIPreview uiPreview = uiPopupContainer.addChild(UIPreview.class, null, null);
    uiPreview.setEvent(event);
    uiPreview.setId("UIPreviewPopup");
    uiPreview.setShowPopup(true);
    uiPreview.setPreviewByUrl(true);
    webuiRequestContext.addUIComponentToUpdateByAjax(uiPopupAction);
  }


  /**
   * process url that links to calendar application
   * this might be an url comes from email or gadget...
   *
   * @param webuiRequestContext
   * @throws Exception
   */
  private void processExternalUrl(WebuiRequestContext webuiRequestContext) throws Exception
  {
    PortalRequestContext pContext = Util.getPortalRequestContext();
    String requestedURL = ((HttpServletRequest) pContext.getRequest()).getRequestURL().toString();
    if (requestedURL.contains(CalendarUtils.INVITATION_URL))
    {
      try {
        processInvitationURL(webuiRequestContext, pContext, requestedURL);
      }
      catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Invitation url is not valid", e);
        }
      }
    }
    else if (requestedURL.contains(CalendarUtils.DETAILS_URL))
    {
      String eventId = requestedURL.substring(requestedURL.indexOf(CalendarUtils.DETAILS_URL) + CalendarUtils.DETAILS_URL.length());
      if (!eventId.startsWith("Event")) return;
      else if(eventId.endsWith("/")) eventId = eventId.substring(0, eventId.lastIndexOf("/"));
      String occurenceId = "";
      String[] array = eventId.split("/");
      if(array.length >=2) {
        eventId = array[0];
        occurenceId = array[1];
      }
      processEventDetailsURL(webuiRequestContext, eventId, occurenceId);
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    processExternalUrl(context);
    super.processRender(app, context);
  }

  public void showConfirmWindow(UIComponent comp, String message) {
    UIConfirmation uiConfirmation = getChild(UIConfirmation.class);
    uiConfirmation.setCaller(comp);
    uiConfirmation.setMessage(message);
    createActionConfirms(uiConfirmation);
    ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiConfirmation);
  }
  public void createActionConfirms(UIConfirmation uiConfirmation) {
    ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
    String yes = resourceBundle.getString("UICalendarPortlet.confirm.yes");
    if(yes == null) yes = "UICalendarPortlet.confirm.yes";
    String no = resourceBundle.getString("UICalendarPortlet.confirm.no");
    if(no == null) no = "UICalendarPortlet.confirm.no";
    List<UIConfirmation.ActionConfirm> actionConfirms = new ArrayList<UIConfirmation.ActionConfirm>();
    actionConfirms.add(new UIConfirmation.ActionConfirm("ConfirmClose", yes));
    actionConfirms.add(new UIConfirmation.ActionConfirm("AbortClose", no));
    uiConfirmation.setActions(actionConfirms);
  }
}