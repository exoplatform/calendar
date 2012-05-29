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

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

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
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;


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
  
  private static Log log = ExoLogger.getLogger("org.exoplatform.calendar.webui.UICalendarPortlet");
  
  private static String SPACE_ID_KEY = "UICalendarPortlet_Space_Id";
  
  
  public UICalendarPortlet() throws Exception {
    UIActionBar uiActionBar = addChild(UIActionBar.class, null, null) ;
    uiActionBar.setCurrentView(UICalendarViewContainer.TYPES[Integer.parseInt(getCalendarSetting().getViewType())]) ;
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
    return String.valueOf(TimeZone.getTimeZone(getCalendarSetting().getTimeZone()).getRawOffset()/1000/60) ;
  }
  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
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
   * get space id if the request comes from one Social space, else return null.
   * @return 
   */
  public static String getSpaceId() {
    PortletRequestContext pContext = WebuiRequestContext.getCurrentInstance();
    String spaceIdStr = (String) pContext.getAttribute(SPACE_ID_KEY);
    if (spaceIdStr == null) {
      try {
        PortletRequest portletRequest = pContext.getRequest();
        PortletPreferences pref = portletRequest.getPreferences();
        if (pref.getValue("SPACE_URL", null) != null) {
          String url = pref.getValue("SPACE_URL", null);
          SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
          Space space = sService.getSpaceByUrl(url);
          spaceIdStr = space.getPrettyName();
          pContext.setAttribute(SPACE_ID_KEY, spaceIdStr);
        }
      } catch (Exception e) {
        if (log.isDebugEnabled())
          log.debug("Getting space id in the UICalendar portlet failed.", e);
      }
    }
    return spaceIdStr;
  }
  
  public static boolean isInSpace() {
    return getSpaceId() != null;
  }

  public void processInvitationURL(WebuiRequestContext context) throws Exception {
    PortalRequestContext pContext = Util.getPortalRequestContext();
    String url = ((HttpServletRequest)pContext.getRequest()).getRequestURL().toString();
    if (url.contains(CalendarUtils.INVITATION_URL)) {
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
        CalendarEvent event = null;
        if (calType == org.exoplatform.calendar.service.Calendar.TYPE_PUBLIC) {
          event = calService.getGroupEvent(eventId);
        }
        else {
          event = calService.getEvent(inviter, eventId) ;
        }
        if (event != null) {
          // update status
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
        String inviter = params[0];
        String eventId = params[1];
        int calType = Integer.parseInt(params[2]);
        
        org.exoplatform.calendar.service.Calendar calendar = null;
        CalendarEvent event = null;
        
        if (calType == org.exoplatform.calendar.service.Calendar.TYPE_PRIVATE || calType == org.exoplatform.calendar.service.Calendar.TYPE_SHARED) {
          event = calService.getEvent(inviter, eventId) ;
          String calendarId = event.getCalendarId();
          calendar = calService.getUserCalendar(inviter, calendarId);
        }
        else {
          if (calType == org.exoplatform.calendar.service.Calendar.TYPE_PUBLIC) {
            event = calService.getGroupEvent(eventId);
            String calendarId = event.getCalendarId();
            calendar = calService.getGroupCalendar(calendarId);
          }
        }
        
        Boolean canView = false;
        if (calendar != null) {
          // check if current user can view the calendar event
          List<org.exoplatform.calendar.service.Calendar> calendars = CalendarUtils.getAllOfCalendars(username);
          if (calendars == null || calendars.isEmpty()) return;
          for (org.exoplatform.calendar.service.Calendar cal : calendars) {
            if (calendar.getId().equals(cal.getId())) {
              canView = true;
              break;
            }  
          }
          if (canView) {
            UIPopupAction uiParentPopup = this.getChild(UIPopupAction.class);
            UIPopupContainer uiPopupContainer = uiParentPopup.activate(UIPopupContainer.class, 700);
            uiPopupContainer.setId(UIPopupContainer.UIEVENTPOPUP);
            uiPopupContainer.setId("UIEventPreview");
            UIPreview uiPreview = uiPopupContainer.addChild(UIPreview.class, null, null) ;
            uiPreview.setEvent(event) ;
            uiPreview.setId("UIPreviewPopup") ;
            uiPreview.setShowPopup(true) ;
            context.addUIComponentToUpdateByAjax(uiParentPopup);
          }
          else {
            this.addMessage(new ApplicationMessage("UICalendarPortlet.msg.have-no-permission-to-view-event", null, ApplicationMessage.WARNING ));
            context.addUIComponentToUpdateByAjax(this.getUIPopupMessages());
          }
        }
      }     
    }
  }
  
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    try {
      processInvitationURL(context);
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Invitation url is not valid", e);
      }
    }
    super.processRender(app, context);
  }
}