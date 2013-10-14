/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.calendar.webui.popup;

import java.util.LinkedHashMap;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 12, 2009  
 */
@ComponentConfigs ( {
  @ComponentConfig (
                    lifecycle = UIFormLifecycle.class,
                    template = "app:/templates/calendar/webui/UIPopup/UIInvitationForm.gtmpl",
                    events = {
                      @EventConfig(listeners = UIInvitationForm.AddUserParticipantActionListener.class, phase = Phase.DECODE ),
                      @EventConfig(listeners = UIInvitationForm.AddGroupParticipantActionListener.class, phase = Phase.DECODE ),
                      @EventConfig(listeners = UIInvitationForm.SaveActionListener.class),
                      @EventConfig(listeners = UIInvitationForm.CancelActionListener.class, phase = Phase.DECODE ),
                      @EventConfig(listeners = UIInvitationForm.SelectGroupActionListener.class, phase = Phase.DECODE)
                    }
  ),
  @ComponentConfig(
                   id = "UIPopupWindowUserSelectEventFormForParticipant",
                   type = UIPopupWindow.class,
                   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
                   events = {
                     @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup"),
                     @EventConfig(listeners = UIInvitationForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
                     @EventConfig(listeners = UIInvitationForm.CloseActionListener.class, phase = Phase.DECODE )
                   }
  )
}
)
public class UIInvitationForm extends UIForm implements UIPopupComponent {
  private static final Log LOG = ExoLogger.getExoLogger(UIInvitationForm.class);

  public final static String FIELD_PARTICIPANT = "participant".intern() ;
  public final static String FIELD_INVITATION_MSG = "invitation-msg".intern() ;
  public final static String TOOLTIP_CONTACT = "contact-picker".intern() ;
  public final static String TOOLTIP_USER = "user-picker".intern() ;
  public final static String TOOLTIP_GROUP = "group-picker".intern() ;

  public static final String NEW_LINE = "\r\n";

  protected CalendarEvent event_ ;

  protected UIForm getParentFrom() {
    return this ;
  }
  public UIInvitationForm() throws Exception {
    this.setId("UIInvitationForm");
    String defaul_msg = "default-invitation-msg" ;
    try{
      defaul_msg = getLabel("default-invitation-msg") ;
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Can not get the label: " + defaul_msg + " from resource bundle", e);
      }
    }

    addUIFormInput(new UIFormTextAreaInput(FIELD_PARTICIPANT, FIELD_PARTICIPANT, null)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_INVITATION_MSG, FIELD_INVITATION_MSG, defaul_msg)) ;


    UIPopupWindow uiPopup = addChild(UIPopupWindow.class, null, "PUIPopupGroupSelector");
    uiPopup.setWindowSize(540, 0);
    UIGroupSelector uiGroup = createUIComponent(UIGroupSelector.class, null, null);
    uiPopup.setUIComponent(uiGroup);
    uiGroup.setId("PGroupSelector");
    uiGroup.getChild(UITree.class).setId("PTreeGroupSelector");
    uiGroup.getChild(UIBreadcumbs.class).setId("PBreadcumbsGroupSelector");
  } 

  @Override
  public String[] getActions() {
    return new String[]{"Save","Cancel"} ;   
  }

  public String appendValue(String oldValue, String newValue) {
    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>() ;
    if(!CalendarUtils.isEmpty(oldValue)) {
      oldValue = oldValue.replaceAll(CalendarUtils.COMMA, CalendarUtils.BREAK_LINE);
      oldValue = oldValue.replaceAll(CalendarUtils.SEMICOLON, CalendarUtils.BREAK_LINE);      
      for(String s : oldValue.split(CalendarUtils.BREAK_LINE)){
        map.put(s+CalendarUtils.BREAK_LINE, s+CalendarUtils.BREAK_LINE);
      }       
    }
    if(newValue.indexOf(CalendarUtils.COMMA) > -1) {
      for (String s : newValue.split(CalendarUtils.COMMA)) {
        map.put(s + CalendarUtils.BREAK_LINE, s+ CalendarUtils.BREAK_LINE);
      }
    } else 
      if(newValue.indexOf(CalendarUtils.SEMICOLON) > -1) {
        for (String s : newValue.split(CalendarUtils.SEMICOLON)) {
          map.put(s + CalendarUtils.BREAK_LINE, s+ CalendarUtils.BREAK_LINE);
        }
      } else
        map.put(newValue + CalendarUtils.BREAK_LINE, newValue+ CalendarUtils.BREAK_LINE);
    StringBuffer sb = new StringBuffer("") ;
    for(String s : map.values()){
      sb.append(s) ;
    }
    return sb.toString() ;
  }
  public String getParticipantValue() {
    return getUIFormTextAreaInput(FIELD_PARTICIPANT).getValue() ;
  }
  public void setParticipantValue(String value) {
    getUIFormTextAreaInput(FIELD_PARTICIPANT).setValue(value) ;
  }
  public String getInvitationMsg() {
    return getUIFormTextAreaInput(FIELD_INVITATION_MSG).getValue() ;
  }
  public void setInvitationMsg(String value) {
    getUIFormTextAreaInput(FIELD_INVITATION_MSG).setValue(value) ; 
  }

  public String escapeGroupReferences(String s){
    if(CalendarUtils.isEmpty(s)) return new String("");
    return s.replace("$", "\\$");
  }
  
  static public class SaveActionListener extends EventListener<UIInvitationForm>{
    @Override
    public void execute(Event<UIInvitationForm> event) throws Exception{
      UIInvitationForm uiInvitationForm = event.getSource();
      UIPopupContainer uiParentPopup = (UIPopupContainer)uiInvitationForm.getParent() ;
      UIPopupContainer uiGrandParentPopup = uiParentPopup.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiGrandParentPopup.getChild(UIEventForm.class) ;
      UIEventShareTab uiEventShareTab =  uiEventForm.getChild(UIEventShareTab.class);
      Long currentPage = uiEventShareTab.getCurrentPage();
      uiEventForm.invitationMsg_ = uiInvitationForm.getUIFormTextAreaInput(FIELD_INVITATION_MSG).getValue() ;
      uiEventForm.participantList_ = uiInvitationForm.getParticipantValue() ;
      uiEventForm.participantList_ = uiInvitationForm.appendValue(uiEventForm.participantList_,"");

      // contains both invalid userId and email
      StringBuilder invalidParticipants = new StringBuilder();
      int invalidParticipantsNumber = 0;

      // filter userId and userEmails to different list
      for (String participant: uiEventForm.participantList_.split(NEW_LINE)) {
        participant = participant.trim();
        if (participant.length() == 0) continue;

        if (CalendarUtils.isAValidEmailAddress(participant) || CalendarUtils.isUserExisted(CalendarUtils.getOrganizationService(), participant)) continue;
        if (invalidParticipants.length() > 0) invalidParticipants.append(", ");
        invalidParticipants.append(participant);
        invalidParticipantsNumber ++;
      }

      if (invalidParticipantsNumber == 0) {
        if(uiEventForm.participantList_!= null){
          uiEventForm.setParticipant(uiEventForm.participantList_);
          uiEventForm.setParticipantStatus(uiEventForm.participantList_);
          uiEventShareTab.setParticipantStatusList(uiEventForm.getParticipantStatusList());
          uiEventShareTab.updateCurrentPage(currentPage.intValue());
          }
          UIPopupAction uiPopup = uiParentPopup.getAncestorOfType(UIPopupAction.class) ;
          uiPopup.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
      }
      else {
        String msg = "UIEventForm.msg.event-participant-invalid";
        if (invalidParticipantsNumber > 1) msg = "UIEventForm.msg.events-participants-invalid";

        event.getRequestContext().getUIApplication()
             .addMessage(new ApplicationMessage(msg, new String[] { invalidParticipants.toString() },
                                                AbstractApplicationMessage.WARNING));
      }      
    }
  }

  static public class SelectGroupActionListener extends EventListener<UIGroupSelector>{
    @Override
    public void execute(Event<UIGroupSelector> event) throws Exception{
      UIGroupSelector uiSelectGroupForm = event.getSource();
      UIInvitationForm uiInvitationForm = uiSelectGroupForm.<UIComponent>getParent().getParent();
      uiInvitationForm.getChild(UIPopupWindow.class).setShow(false) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIFormTextAreaInput uiInput = uiInvitationForm.getUIFormTextAreaInput(FIELD_PARTICIPANT) ;
      uiInput.setValue(uiInvitationForm.appendValue(uiInput.getValue(), groupId)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiInvitationForm) ;
    }
  }
  static public class CancelActionListener extends EventListener<UIInvitationForm>{
    @Override
    public void execute(Event<UIInvitationForm> event) throws Exception{
      UIInvitationForm uiInvitationForm = event.getSource();
      UIPopupAction uiPopupAction = uiInvitationForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  static public class AddUserParticipantActionListener extends EventListener<UIInvitationForm>{
    @Override
    public void execute(Event<UIInvitationForm> event) throws Exception{
      UIInvitationForm uiInvitationForm = event.getSource();
      UIPopupContainer uiPopupContainer = uiInvitationForm.getParent();  
      uiPopupContainer.deActivate();
      UIPopupWindow uiPopupWindow = uiPopupContainer.getChild(UIPopupWindow.class) ;
      if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelectEventFormForParticipant", "UIPopupWindowUserSelectEventFormForParticipant") ;
      UIUserSelector uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelector.class, null, null) ;
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true) ;
      uiUserSelector.setShowSearchGroup(true);
      uiPopupWindow.setUIComponent(uiUserSelector);
      uiPopupWindow.setShow(true);
      uiPopupWindow.setRendered(true);
      uiPopupWindow.setWindowSize(740, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
      JavascriptManager jsManager = ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance())
          .getJavascriptManager();
      RequireJS requireJS = jsManager.getRequireJS();
      requireJS.require("SHARED/jquery","gj");
      requireJS.addScripts("gj('#uiInvitationUser').tooltip('show');");
    }
  }

  static public class AddGroupParticipantActionListener extends EventListener<UIInvitationForm>{
    @Override
    public void execute(Event<UIInvitationForm> event) throws Exception{
      UIInvitationForm uiInvitationForm = event.getSource();  
      UIPopupWindow uiPopupWindow = uiInvitationForm.getChild(UIPopupWindow.class) ;
      if(uiPopupWindow != null) {
        uiPopupWindow.setUIComponent(null) ;
        uiPopupWindow.setRendered(false);
        uiPopupWindow.setShow(false) ;
      }
      uiPopupWindow.setShow(true);    
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow) ;      
    }
  }


  static  public class AddActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      UIInvitationForm uiInvitationForm = uiContainer.getChild(UIInvitationForm.class) ;
      String values = uiUserSelector.getSelectedUsers();
      String value = uiInvitationForm.appendValue(uiInvitationForm.getParticipantValue(), values) ;
      uiInvitationForm.getUIFormTextAreaInput(UIInvitationForm.FIELD_PARTICIPANT).setValue(value) ;
//      event.getRequestContext().addUIComponentToUpdateByAjax(uiInvitationForm) ;
      //close select user popup
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }

  static  public class CloseActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      UIPopupContainer uiContainer = uiPoupPopupWindow.getAncestorOfType(UIPopupContainer.class) ;
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }

  @Override
  public void activate() throws Exception {

  }
  @Override
  public void deActivate() throws Exception {

  }
}
