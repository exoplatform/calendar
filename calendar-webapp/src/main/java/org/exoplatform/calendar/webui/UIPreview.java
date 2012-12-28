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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.webui.popup.UIPopupComponent;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
//import org.exoplatform.services.cms.impl.ImageUtils;
//import org.exoplatform.services.cms.thumbnail.ThumbnailService;
//import org.exoplatform.services.cms.thumbnail.impl.ThumbnailRESTService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

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
        @EventConfig(listeners = UICalendarView.DeleteActionListener.class, confirm="UICalendarView.msg.confirm-delete")
    }
)
public class UIPreview extends UICalendarView implements UIPopupComponent {
  private CalendarEvent event_ = null ;
  private boolean isShowPopup_ = false ;
  private static final Log LOG = ExoLogger.getExoLogger(UIPreview.class);

  public UIPreview() throws Exception {}


  @Override
  public void processRender(WebuiRequestContext context) throws Exception
  {
    super.processRender(context);

    //generateThumbnailForAttachment();
  }

  /**
  private void generateThumbnailForAttachment() throws Exception
  {
    LOG.info("generate thumbnail for attachment");

    if (event_ == null) return;
    if (event_.getAttachment() == null) return;

    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
    ThumbnailService thumbnailService = (ThumbnailService) PortalContainer.getInstance().getComponentInstanceOfType(ThumbnailService.class);
    DownloadService downloadService = getApplicationComponent(DownloadService.class) ;


    LOG.info("loop through the attachment");
    Iterator<Attachment> it = event_.getAttachment().iterator();
    while (it.hasNext())
    {
      Attachment attachment = it.next();

      if (!attachment.getMimeType().startsWith("image")) continue;
      LOG.info("attachment has image");
      Session session = repoService.getCurrentRepository().getSystemSession(attachment.getWorkspace());
      Node attachmentNode = (Node) session.getItem(attachment.getId());

      if (thumbnailService.getThumbnailNode(attachmentNode) != null) continue;
      LOG.info("add thumbnail to node");
      BufferedImage image = ImageIO.read(attachment.getInputStream());
      thumbnailService.createThumbnailImage(attachmentNode, image, attachment.getMimeType());

      InputStream thumbnailDataStream = thumbnailService.getThumbnailImage(attachmentNode, ThumbnailService.MEDIUM_SIZE);
      if (thumbnailDataStream != null)
      {
        LOG.info("link to download: " + getLinkToDownloadFor(thumbnailDataStream,
            downloadService, attachment.getMimeType(), attachment.getName()));

      }
      else LOG.info("no data for thumbnail");
    }
  }
   **/

  /**
   * generate a download link for a input stream using download service
   *
   * @param inputStream
   * @param downloadService
   * @param mimeType
   * @param name
   * @return
   * @throws Exception
   */
  private String getLinkToDownloadFor(InputStream inputStream, DownloadService downloadService, String mimeType, String name) throws Exception
  {
    if (inputStream == null) return null;

    byte[] imageBytes = null ;

    imageBytes = new byte[inputStream.available()] ;
    inputStream.read(imageBytes) ;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
    InputStreamDownloadResource downloadResource = new InputStreamDownloadResource(byteImage, mimeType ) ;
    downloadResource.setDownloadName(name) ;
    return downloadService.getDownloadLink(downloadService.addDownloadResource(downloadResource)) ;
  }

  /**
  public String getThumbnailLink(Attachment attachment) throws Exception
  {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
    ThumbnailService thumbnailService = (ThumbnailService) PortalContainer.getInstance().getComponentInstanceOfType(ThumbnailService.class);
    DownloadService downloadService = getApplicationComponent(DownloadService.class) ;

    Session session = repoService.getCurrentRepository().getSystemSession(attachment.getWorkspace());
    Node attachmentNode = (Node) session.getItem(attachment.getId());

    InputStream thumbnailDataStream = thumbnailService.getThumbnailImage(attachmentNode, ThumbnailService.MEDIUM_SIZE);
    if (thumbnailDataStream != null)
      return getLinkToDownloadFor(thumbnailDataStream, downloadService, attachment.getMimeType(), attachment.getName());
    return null;
  }
   **/

  public String getRestThumbnailLink(Attachment attachment) throws Exception
  {
    return "/"+PortalContainer.getInstance().getRestContextName()+ "/thumbnailImage/medium/repository/collaboration/" + attachment.getDataPath();
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
}
