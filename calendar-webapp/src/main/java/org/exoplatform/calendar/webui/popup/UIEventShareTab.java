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
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.popup.UIEventForm.ParticipantStatus;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
                 template = "app:/templates/calendar/webui/UIPopup/UIEventShareTab.gtmpl",
                 events = {
                     @EventConfig(listeners = UIEventShareTab.ShowPageActionListener.class, phase = Phase.PROCESS),
                     @EventConfig(listeners = UIEventShareTab.DeleteActionListener.class, phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventShareTab.ConfirmCloseActionListener.class),
                     @EventConfig(listeners = UIEventShareTab.AbortCloseActionListener.class)
                 }
) 
public class UIEventShareTab extends UIFormInputWithActions {

  public static String[] BEAN_FIELD = {"name","email","status"};
  private static String[] ACTION = { "Delete"} ;
  final public static String FIELD_SHARE = "shareEvent".intern() ;
  final public static String FIELD_STATUS = "status".intern() ;
  final public static String FIELD_SEND = "send".intern();
  final public static String FIELD_INFO =  "info".intern() ;
  final public static String FIELD_ANSWER = "answer".intern() ;
  private Map<String, List<ActionData>> actionField_ = new HashMap<String, List<ActionData>>() ;

  private static final Log LOG = ExoLogger.getExoLogger(UIEventShareTab.class);
  private String parStatus ;


  public UIEventShareTab(String id) throws Exception {
    super(id);
    setComponentConfig(getClass(), null) ;
    UIGrid categoryList = addChild(UIGrid.class, null , "UIParticipantList") ;
    categoryList.configure("participant", BEAN_FIELD, ACTION) ;
    categoryList.getUIPageIterator().setId("ParticipantListIterator");
    setParticipantStatusList(new LinkedList<ParticipantStatus>()) ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  
  public UIFormRadioBoxInput getUIFormRadioBoxInput (String id) {
    return findComponentById(id);
  }
  public Map<String, String> getParticipantStatus() {
    return ((UIEventForm) getParent()).participantStatus_ ;
  }
  public void setActionField(String fieldName, List<ActionData> actions){
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  
  @SuppressWarnings("unchecked")
  public List<ParticipantStatus> getData() throws Exception {
    return new LinkedList<ParticipantStatus>(getChild(UIGrid.class).getUIPageIterator().getCurrentPageData());
  }
  
  public UIPageIterator  getUIPageIterator() {  return getChild(UIGrid.class).getUIPageIterator() ; }
  
  public long getAvailablePage(){ return getChild(UIGrid.class).getUIPageIterator().getAvailablePage() ;}
  
  public long getCurrentPage() { return getChild(UIGrid.class).getUIPageIterator().getCurrentPage();}
  
  public void setParticipantStatusList(List<ParticipantStatus> participantStatusList) throws Exception {
    List<ParticipantStatus> newParStatus = new ArrayList<ParticipantStatus>() ;
    for (ParticipantStatus participantStatus : participantStatusList) {
      if (!CalendarUtils.isEmpty(participantStatus.getParticipant())) { newParStatus.add(participantStatus) ; }
    }
    //ObjectPageList objPageList = new ObjectPageList(newParStatus, 10) ;
    LazyPageList<ParticipantStatus> pageList = new LazyPageList<ParticipantStatus>(new ListAccessImpl<ParticipantStatus>(ParticipantStatus.class, newParStatus), 10);
    getChild(UIGrid.class).getUIPageIterator().setPageList(pageList) ;
  }
  protected void updateCurrentPage(int page) throws Exception{
    getChild(UIGrid.class).getUIPageIterator().setCurrentPage(page) ;
  }
  static  public class ShowPageActionListener extends EventListener<UIEventShareTab> {
    @Override
    public void execute(Event<UIEventShareTab> event) throws Exception {
      UIEventShareTab uiEventShareTab = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiEventShareTab.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEventShareTab);           
    }
  }

  //, confirm = "UIEventForm.msg.confirm-delete"
  static  public class DeleteActionListener extends EventListener<UIEventShareTab> {
    @Override
    public void execute(Event<UIEventShareTab> event) throws Exception {
      UIEventShareTab uiEventShareTab = event.getSource() ;
      UIEventForm uiEventForm = uiEventShareTab.getParent() ;
      UICalendarPortlet calendarPortlet = uiEventShareTab.getAncestorOfType(UICalendarPortlet.class);
      String parStatus = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiEventShareTab.parStatus = parStatus;
      ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
      String message = resourceBundle.getString("UIEventForm.msg.confirm-delete");
      calendarPortlet.showConfirmWindow(uiEventShareTab, message);
      return ;
     }
    }

  static  public class ConfirmCloseActionListener extends EventListener<UIEventShareTab> {
    @Override
    public void execute(Event<UIEventShareTab> event) throws Exception {
      UIEventShareTab uiEventShareTab = event.getSource() ;
      UIEventForm uiEventForm = uiEventShareTab.getParent() ;
      String parStatus = uiEventShareTab.parStatus;
      UIEventAttenderTab tabAttender = uiEventForm.getChildById(UIEventForm.TAB_EVENTATTENDER) ;
      //UIEventShareTab uiEventShareTab = uiEventForm.getChildById(UIEventForm.TAB_EVENTSHARE) ;
      Long currentPage  = uiEventShareTab.getCurrentPage() ;
      if(uiEventForm.participants_.containsKey(parStatus)){
        uiEventForm.participants_.remove(parStatus);
        tabAttender.parMap_.remove(parStatus) ;
      }
      uiEventForm.participantStatus_.remove(parStatus);
      for(Iterator<ParticipantStatus> i = uiEventForm.participantStatusList_.iterator(); i.hasNext();){
        ParticipantStatus participantStatus = i.next();
        if(parStatus.equalsIgnoreCase(participantStatus.getParticipant()))
          i.remove();
      }
      uiEventShareTab.setParticipantStatusList(uiEventForm.getParticipantStatusList());
      if (currentPage <= uiEventShareTab.getAvailablePage())
        uiEventShareTab.updateCurrentPage(currentPage.intValue());
      else
        uiEventShareTab.updateCurrentPage((int)uiEventShareTab.getAvailablePage());
      uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTSHARE) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(tabAttender) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiEventShareTab) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  static  public class AbortCloseActionListener extends EventListener<UIEventShareTab> {
    @Override
    public void execute(Event<UIEventShareTab> event) throws Exception {
      UIEventShareTab uiEventShareTab = event.getSource() ;
      uiEventShareTab.parStatus = null;
      return;
    }
  }
  
}
