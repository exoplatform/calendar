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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.webui.popup.UIPopupComponent;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.services.jcr.RepositoryService;
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
      @EventConfig(listeners = UICalendarView.DeleteActionListener.class, confirm="UICalendarView.msg.confirm-delete")
    }
)
public class UIPreview extends UICalendarView implements UIPopupComponent {
  private CalendarEvent event_ = null ;
  private boolean isShowPopup_ = false ;

  public UIPreview() throws Exception {}

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

  public void refresh() throws Exception {
    if(getAncestorOfType(UIListContainer.class) != null) {
     event_ = getAncestorOfType(UIListContainer.class).findFirstComponentOfType(UIListView.class).getSelectedEventObj() ;
    }
  }

  public void activate() throws Exception {}

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
