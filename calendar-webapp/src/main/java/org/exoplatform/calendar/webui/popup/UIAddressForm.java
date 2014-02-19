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
import java.util.Collection;
import java.util.List;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template =  "app:/templates/calendar/webui/UIPopup/UIAddressForm.gtmpl",
                 events = {
                   @EventConfig(listeners = UIAddressForm.AddActionListener.class), 
                   @EventConfig(listeners = UIAddressForm.ReplaceActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIAddressForm.ShowPageActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIAddressForm.ChangeGroupActionListener.class, phase = Phase.DECODE),
                   @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE)
                 }
)

public class UIAddressForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;
  private String recipientsType = "";
  protected String selectedAddressId_ = "" ;
  private UIPageIterator uiPageIterator_ ;
  protected String[] actions_ = new String[]{"Add", "Replace", "Cancel"}; 
  
  public List<String> checkedList_ = new ArrayList<String>();
  public void setRecipientsType(String type)  {
    recipientsType=type;
  }
  public String getRecipientType(){
    return recipientsType;
  }

  public UIAddressForm() throws Exception {  
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null)) ;
    UIFormSelectBox fieldGroup = new UIFormSelectBox(FIELD_GROUP, FIELD_GROUP, getGroups()) ;
    fieldGroup.setOnChange("ChangeGroup") ;
    addUIFormInput(fieldGroup) ;
    uiPageIterator_ = new UIPageIterator() ;
    uiPageIterator_.setId("UICalendarAddressPage") ;
  }
  private List<SelectItemOption<String>> getGroups() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    OrganizationService oService = getApplicationComponent(OrganizationService.class);
    options.add(new SelectItemOption<String>("all", Utils.EMPTY_STR)) ;
   Collection<Group> groups = oService.getGroupHandler().findGroupsOfUser(CalendarUtils.getCurrentUser());
    for( Group g : groups) {
      ListAccess<User> users = oService.getUserHandler().findUsersByGroupId(g.getId()) ;
      for(User u : users.load(0, users.getSize())) {
        options.add(new SelectItemOption<String>(u.getUserName(), u.getUserName())) ;
      } 
    }
    return options;
  }

  @Override
  public String[] getActions() { return actions_ ; }

  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {
    actions_ = new String[]{"Add", "Replace", "Cancel"}; 
  } 
  @SuppressWarnings("unchecked")
  public List<ContactData> getContacts() throws Exception {
    for(String id : checkedList_) {
      UICheckBoxInput uiInput = getUICheckBoxInput(id) ;
      if(uiInput != null) uiInput.setChecked(true) ;
    }
    return new ArrayList<ContactData>(uiPageIterator_.getCurrentPageData());
  }
  
  @SuppressWarnings({ "unchecked", "deprecation" })
  public List<ContactData> getContactList() {
    try {
      return uiPageIterator_.getPageList().getAll() ;
    } catch (Exception e) {
      return new ArrayList<ContactData>() ;
    }
  }
  public void setContactList(List<ContactData> contactList) throws Exception {
    getUIFormSelectBox(FIELD_GROUP).setOptions(getGroups()) ;
    LazyPageList<ContactData> pageList = new LazyPageList<ContactData>(new ListAccessImpl<ContactData>(ContactData.class, contactList), 10);
    uiPageIterator_.setPageList(pageList) ;
    for (ContactData contact : contactList) {
      UICheckBoxInput uiCheckbox = getUICheckBoxInput(contact.getId()) ;
      if(uiCheckbox == null) {
        uiCheckbox = new UICheckBoxInput(contact.getId(), contact.getId(), false) ;
        addUIFormInput(uiCheckbox);
      } 
    }
  }
  @SuppressWarnings("unchecked")
  public List<ContactData> getCheckedContact() throws Exception {
    List<ContactData> contactList = new ArrayList<ContactData>();  
    for (ContactData contact : new ArrayList<ContactData>(uiPageIterator_.getCurrentPageData())) {
      UICheckBoxInput uiCheckbox = getChildById(contact.getId());
      if (uiCheckbox!=null && uiCheckbox.isChecked()) {
        contactList.add(contact);
      }
    }
    return contactList;
  }
  public UIPageIterator  getUIPageIterator() {  return uiPageIterator_ ; }
  public long getAvailablePage(){ return uiPageIterator_.getAvailablePage() ;}
  public long getCurrentPage() { return uiPageIterator_.getCurrentPage();}
  protected void updateCurrentPage(int page) throws Exception{
    uiPageIterator_.setCurrentPage(page) ;
  }

  static public class AddActionListener extends EventListener<UIAddressForm> {
    @Override
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource() ;
      if(uiForm.getCheckedContact().size() <= 0) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        return ;
      }
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UITaskForm uiTaskForm = uiContainer.findFirstComponentOfType(UITaskForm.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      UIInvitationForm uiInvitationForm =uiContainer.findFirstComponentOfType(UIInvitationForm.class) ;
      StringBuffer sb = new StringBuffer() ;
      if(uiTaskForm != null) {
        if(uiTaskForm.getEmailAddress() != null && uiTaskForm.getEmailAddress().trim().length() > 0) {
          sb.append(uiTaskForm.getEmailAddress()) ;
        }
      } else if (uiEventForm != null) {
        if(uiEventForm.getEmailAddress() != null && uiEventForm.getEmailAddress().trim().length() > 0) {
          sb.append(uiEventForm.getEmailAddress()) ;
        }
      }
      if(uiTaskForm != null) {
        uiTaskForm.setSelectedTab(UITaskForm.TAB_TASKREMINDER) ;
        uiTaskForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskForm) ;
      }else if(uiEventForm != null) {
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
      } else if(uiInvitationForm != null) {
        String value =  uiInvitationForm.appendValue(uiInvitationForm.getParticipantValue(),  sb.toString()) ;
        uiInvitationForm.getUIFormTextAreaInput(UIInvitationForm.FIELD_PARTICIPANT).setValue(value) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiInvitationForm) ;
     }
    }
  }

  static  public class ReplaceActionListener extends EventListener<UIAddressForm> {
    @Override
    public void execute(Event<UIAddressForm> event) throws Exception { 
      UIAddressForm uiForm = event.getSource();
      if(uiForm.getCheckedContact().size() <= 0) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        return ;
      }
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UITaskForm uiTaskForm = uiContainer.findFirstComponentOfType(UITaskForm.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      UIInvitationForm uiInvitationForm = uiContainer.findFirstComponentOfType(UIInvitationForm.class) ;
      StringBuilder sb = new StringBuilder() ;
      for(ContactData c : uiForm.getCheckedContact()) {
        if(!CalendarUtils.isEmpty(c.getEmail())) {
          if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
          for (String email : c.getEmail().replace(";", ",").split(","))
            if (sb.indexOf(email.trim()) == -1) sb.append(email.trim()) ;
        }
      }
      if(uiTaskForm != null) {
        uiTaskForm.setSelectedTab(UITaskForm.TAB_TASKREMINDER) ;
        uiTaskForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskForm) ;
      } 
      if(uiEventForm != null) {
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
      } 
      if(uiInvitationForm != null) {
         String value =  uiInvitationForm.appendValue(uiInvitationForm.getParticipantValue(),  sb.toString()) ;
         uiInvitationForm.getUIFormTextAreaInput(UIInvitationForm.FIELD_PARTICIPANT).setValue(value) ;
      }

      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }  
  } 
  
  static  public class ChangeGroupActionListener extends EventListener<UIAddressForm> {
    @Override
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource();  
      String category = uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = new StringBuilder().append(category).append(CalendarUtils.getCurrentUser()).toString() ;
      uiForm.selectedAddressId_ = category ;
      uiForm.getUIStringInput(UIAddressForm.FIELD_KEYWORD).setValue(null) ;
      uiForm.getUIFormSelectBox(UIAddressForm.FIELD_GROUP).setValue(uiForm.selectedAddressId_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIAddressForm> {
    @Override
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();  
      UIPopupContainer uiContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }
  }

  static  public class ShowPageActionListener extends EventListener<UIAddressForm> {
    @Override
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiAddressForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm.getAncestorOfType(UIPopupAction.class));           
    }
  }
  public class ContactData {
    private String id ;
    private String fullName ;
    private String email ;

    public ContactData(String id,String fullName,String email){
      this.id = id ;
      this.fullName = fullName;
      this.email = email ;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getFullName() {
      return fullName;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getEmail() {
      return email;
    }

  }
}
