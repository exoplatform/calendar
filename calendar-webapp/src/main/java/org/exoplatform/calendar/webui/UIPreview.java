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

import java.util.LinkedHashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupComponent;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template =  "app:/templates/calendar/webui/UIDefaultPreview.gtmpl",
                 events = {
                   @EventConfig(listeners = UIPreview.DownloadActionListener.class),
                   @EventConfig(listeners = UICalendarView.EditActionListener.class),
                   @EventConfig(listeners = UICalendarView.DeleteActionListener.class),
                   @EventConfig(listeners = UIPreview.CloseWindowActionListener.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteOnlyInstance.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteAllSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteFollowingSeries.class),
                   @EventConfig(listeners = UICalendarView.ConfirmDeleteCancel.class)
                 }
    )
public class UIPreview extends UICalendarView implements UIPopupComponent
{
  public static final int DEFAULT_THUMBNAIL_DIMENSION = 50;
  public static final int DEFAULT_PREVIEW_DIMENSION   = 170;
  private CalendarEvent event_ = null;
  private boolean isShowPopup_ = false;
  private boolean isPreviewByUrl = false;
  
  public UIPreview() throws Exception {
  }

  /**
   * return a thumbnail link for image attachment using the thumbnail REST web service from ECMS
   *
   * @param attachment attachment object that contains the image
   * @param oneFixedDimension resize the image after this dimension, keeping the image ratio
   * @return
   * @throws Exception
   */
  public String getRestThumbnailLinkFor(Attachment attachment, int oneFixedDimension) throws Exception
  {
    int[] imageDimension = getScaledImageDimensionFor(attachment, oneFixedDimension);

    return "/"+PortalContainer.getInstance().getRestContextName()+ "/thumbnailImage/custom/" + imageDimension[0] + "x" + imageDimension[1]
        + "/repository/collaboration/" + attachment.getDataPath();
  }

  private int getImageAttachmentWidth(Attachment attachment) throws Exception
  {
    return ImageIO.read(attachment.getInputStream()).getWidth();
  }

  private int getImageAttachmentHeight(Attachment attachment) throws Exception
  {
    return ImageIO.read(attachment.getInputStream()).getHeight();
  }

  protected static final String CLOSE_POPUP = "CloseWindow";

  /**
   * scale the image and return dimensions of image given one fixed dimension
   *
   * @param imageAttachment
   * @param fixedDimension
   * @return an array of new dimensions {width, height}
   * @throws Exception
   */
  private int[] getScaledImageDimensionFor(Attachment imageAttachment, int fixedDimension) throws Exception
  {
    int width = getImageAttachmentWidth(imageAttachment);
    int height = getImageAttachmentHeight(imageAttachment);
    int biggerDimension = width > height ? width : height;
    int smallerDimension = biggerDimension == width ? height : width;
    double scalingRatio = (double) biggerDimension / fixedDimension;
    int newScaledDimension =  (int) Math.round(smallerDimension / scalingRatio);
    if (width > height) return new int[] { fixedDimension, newScaledDimension };
    else if (width == height) return new int[] { fixedDimension, fixedDimension};
    else return new int[] { newScaledDimension, fixedDimension};
  }

  @Override
  public String getTemplate(){
    if(event_ == null) return "app:/templates/calendar/webui/UIDefaultPreview.gtmpl" ;
    if(event_.getEventType().equals(CalendarEvent.TYPE_EVENT))
      return "app:/templates/calendar/webui/UIEventPreview.gtmpl" ;
    if(event_.getEventType().equals(CalendarEvent.TYPE_TASK))
      return "app:/templates/calendar/webui/UITaskPreview.gtmpl" ;
    return "app:/templates/calendar/webui/UIDefaultPreview.gtmpl" ;
  }

  public CalendarEvent getEvent(){ return event_ ; }
  public void setEvent(CalendarEvent event) { event_ = event ; }

  @Override
  public void refresh() throws Exception {
    if(getAncestorOfType(UIListContainer.class) != null) {
      event_ = getAncestorOfType(UIListContainer.class).findFirstComponentOfType(UIListView.class).getSelectedEventObj() ;
    }
  }

  @Override
  public void activate() throws Exception {}

  @Override
  public void deActivate() throws Exception {}

  public void setShowPopup(boolean isShow) {
    this.isShowPopup_ = isShow;
  }

  public boolean isShowPopup() {
    return isShowPopup_;
  }

  public Attachment getAttachment(String attId) {
    if(getEvent() != null) for(Attachment a : getEvent().getAttachment()) {
      if(a.getId().equals(attId)) return a ;
    }
    return null ;
  }
  public String getDownloadLink(Attachment attach) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    return CalendarUtils.getDataSource(attach, dservice) ;
  }

  public String getImageSource(Attachment attach) throws Exception {
    try {
      return "/"+PortalContainer.getInstance().getRestContextName() + "/private/jcr/" + getRepository()+"/" + attach.getWorkspace()+attach.getDataPath() ;
    } catch (Exception e) {
      return getDownloadLink(attach) ;
    }
  }

  @Override
  LinkedHashMap<String, CalendarEvent> getDataMap() {
    LinkedHashMap<String, CalendarEvent> dataMap = new LinkedHashMap<String, CalendarEvent>() ;
    if(event_ != null) dataMap.put(event_.getId(), event_) ;
    return dataMap ;
  }

  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  static  public class DownloadActionListener extends EventListener<UIPreview> {
    @Override
    public void execute(Event<UIPreview> event) throws Exception {
      UIPreview uiPreview = event.getSource() ;
      String attId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Attachment attach = uiPreview.getAttachment(attId) ;
      if(attach != null) {
        String mimeType = attach.getMimeType().substring(attach.getMimeType().indexOf("/")+1) ;
        DownloadResource dresource = new InputStreamDownloadResource(attach.getInputStream(), mimeType);
        DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
        dresource.setDownloadName(attach.getName());
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPreview) ;
      }
    }
  }
  @Override
  public String getDefaultStartTimeOfEvent() {
    return String.valueOf(calendar_.getTimeInMillis());
  }

  /**
   * get the url to the calendar portlet, used for template to return to the
   * calendar portlet or the calendar page in the space
   *
   * @return
   */
  public static String getCalendarPortletUrl()
  {
    PortalRequestContext pContext = Util.getPortalRequestContext();
    String requestedURL = ((HttpServletRequest) pContext.getRequest()).getRequestURL().toString();
    if (requestedURL.indexOf(CalendarUtils.DETAILS_URL) != -1)
      return requestedURL.substring(0, requestedURL.indexOf(CalendarUtils.DETAILS_URL));
    if (requestedURL.indexOf(CalendarUtils.INVITATION_DETAIL_URL) != -1)
      return requestedURL.substring(0, requestedURL.indexOf(CalendarUtils.INVITATION_DETAIL_URL));
    return "";
  }

  public boolean isPreviewByUrl() {
    return isPreviewByUrl;
  }

  public void setPreviewByUrl(boolean isPreviewByUrl) {
    this.isPreviewByUrl = isPreviewByUrl;
  }
  public static class CloseWindowActionListener extends EventListener<UIPreview>
  {
    public void execute(Event<UIPreview> event) throws Exception
    {
      PortalRequestContext pContext = Util.getPortalRequestContext();
      String requestedURL = ((HttpServletRequest) pContext.getRequest()).getRequestURL().toString();
      UIPreview uiPreview = event.getSource() ;
      UIPopupAction uiPopupAction = uiPreview.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      WebuiRequestContext requestContext = event.getRequestContext();
      /* we do not want to allow opening of popup */
      requestContext.addUIComponentToUpdateByAjax(uiPopupAction);
      if (requestedURL.indexOf(CalendarUtils.DETAILS_URL) != -1 || requestedURL.indexOf(CalendarUtils.DETAIL_URL) != -1) {
        uiPreview.setPreviewByUrl(false);
        event.getRequestContext().sendRedirect(getCalendarPortletUrl());
      }
    }
  }

}
