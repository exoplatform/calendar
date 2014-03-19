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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
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

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@gmail.com
 * Dec 11, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/calendar/webui/UIPopup/UISelectUserForm.gtmpl",
    events = {
      @EventConfig(listeners = UISelectUserForm.ReplaceActionListener.class), 
      @EventConfig(listeners = UISelectUserForm.AddActionListener.class, phase = Phase.DECODE), 
      @EventConfig(listeners = UISelectUserForm.SearchActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.ChangeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.ShowPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UISelectUserForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)

public class UISelectUserForm extends UIForm implements UIPopupComponent { 
  final public static String FIELD_KEYWORD = "keyWord".intern() ;
  final public static String FIELD_FILTER = "filter".intern() ;
  final public static String FIELD_GROUP = "group".intern() ;
  public static String USER_NAME = "userName";
  public static String LAST_NAME = "lastName";
  public static String FIRST_NAME = "firstName";
  public static String EMAIL = "email";

  protected Map<String, User> userData_ = new HashMap<String, User>() ;
  private boolean isShowSearch_ = false ;
  protected String tabId_ = null ;
  protected String groupId_ = null ;
  protected Collection<String> pars_ ;
  public UIPageIterator uiIterator_ ;

  @SuppressWarnings("unchecked")
  public List<User> getData() throws Exception {
    for(Object obj : uiIterator_.getCurrentPageData()){
      User user = (User)obj ;
      if(getUICheckBoxInput(user.getUserName()) == null)
        addUIFormInput(new UICheckBoxInput(user.getUserName(),user.getUserName(), false)) ;
    }
    for(String s : pars_) {
      if(getUICheckBoxInput(s) != null) getUICheckBoxInput(s).setChecked(true) ;
    }
    return  new ArrayList<User>(uiIterator_.getCurrentPageData());
  }
  public UISelectUserForm() throws Exception {  
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_FILTER, FIELD_FILTER, getFilters())) ;
    UIFormSelectBox uiSelectBox = new UIFormSelectBox(FIELD_GROUP, FIELD_GROUP, getGroups()) ;
    addUIFormInput(uiSelectBox) ;
    uiSelectBox.setOnChange("Change") ;
    isShowSearch_ = true ;
    uiIterator_ = new UIPageIterator() ;
    uiIterator_.setId("UISelectUserPage") ;
  }
  public UIPageIterator  getUIPageIterator() {  return uiIterator_ ; }
  public long getAvailablePage(){ return uiIterator_.getAvailablePage() ;}
  public long getCurrentPage() { return uiIterator_.getCurrentPage();}

  public void init(Collection<String> pars) throws Exception{
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    LazyPageList<User> pageList = new LazyPageList<User>(new ListAccessImpl<User>(User.class, service.getUserHandler().getUserPageList(0).getAll()), 10);
    uiIterator_.setPageList(pageList) ;
    pars_ = pars ;
  }
  private List<SelectItemOption<String>> getGroups() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    OrganizationService orgService = CalendarUtils.getOrganizationService() ;
    options.add(new SelectItemOption<String>("all", "all")) ;
    for( Object g : orgService.getGroupHandler().getAllGroups()) { 
      Group  cg = (Group)g ;
      options.add(new SelectItemOption<String>(cg.getGroupName(), cg.getId())) ;
    }
    return options;
  }
  private List<SelectItemOption<String>> getFilters() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(USER_NAME, USER_NAME)) ;
    options.add(new SelectItemOption<String>(LAST_NAME, LAST_NAME)) ;
    options.add(new SelectItemOption<String>(FIRST_NAME, FIRST_NAME)) ;
    options.add(new SelectItemOption<String>(EMAIL, EMAIL)) ;
    return options;
  }

  @Override
  public String[] getActions() { return new String[]{"Add", "Replace", "Close"}; }
  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {} 
  @Override
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  public void setShowSearch(boolean isShowSearch) {
    this.isShowSearch_ = isShowSearch;
  }
  public boolean isShowSearch() {
    return isShowSearch_;
  }
  public String getSelectedGroup() {
    if("all".equals(getUIFormSelectBox(FIELD_GROUP).getValue())) return null ;
    return getUIFormSelectBox(FIELD_GROUP).getValue() ;
  }
  public void setSelectedGroup(String selectedGroup) {
    getUIFormSelectBox(FIELD_GROUP).setValue(selectedGroup) ;
    groupId_ = selectedGroup ;
  }
  static  public class AddActionListener extends EventListener<UISelectUserForm> {
    @Override
    public void execute(Event<UISelectUserForm> event) throws Exception { 
      UISelectUserForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      if(uiEventForm != null) {
        StringBuilder sb = new StringBuilder() ;
        int count = 0;
        for(Object o : uiForm.uiIterator_.getCurrentPageData()) {
          User u = (User)o ;
          UICheckBoxInput input = uiForm.getUICheckBoxInput(u.getUserName()) ;
          if(input != null && input.isChecked()) {
            count++ ;
            if(!uiForm.pars_.contains(u.getUserName())) {
              if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.COMMA) ;
              sb.append(u.getUserName()) ;
            }
          }
        }
        if(count == 0) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISelectUserForm.msg.user-required",
                                                                                         null));
          return ;
        } 
        for(String s : uiForm.pars_) {
          if(!CalendarUtils.isEmpty(sb.toString())) sb.append(CalendarUtils.COMMA) ;
          sb.append(s) ;
        }
        uiEventForm.setSelectedTab(uiForm.tabId_) ;
        uiEventForm.setParticipant(sb.toString()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm.getParent()) ;
      } 
    }  
  } 

  protected void updateCurrentPage(int page) throws Exception{
    uiIterator_.setCurrentPage(page) ;
  }
  public void setKeyword(String value) {
    getUIStringInput(FIELD_KEYWORD).setValue(value) ;
  }
  static  public class ReplaceActionListener extends EventListener<UISelectUserForm> {
    @Override
    public void execute(Event<UISelectUserForm> event) throws Exception { 
      UISelectUserForm uiForm = event.getSource();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIEventForm uiEventForm = uiContainer.findFirstComponentOfType(UIEventForm.class) ;
      if(uiEventForm != null) {
        StringBuilder sb = new StringBuilder() ;
        for(Object o : uiForm.uiIterator_.getCurrentPageData()) {
          User u = (User)o ;
          UICheckBoxInput input = uiForm.getUICheckBoxInput(u.getUserName()) ;
          if(input != null && input.isChecked()) {
            if(sb != null && sb.length() > 0) sb.append(CalendarUtils.COMMA) ;
            sb.append(u.getUserName()) ;
          }
        }
        if(CalendarUtils.isEmpty(sb.toString())) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISelectUserForm.msg.user-required",
                                                                                         null));
          return ;
        }
        uiEventForm.setSelectedTab(uiForm.tabId_) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm.getParent()) ;
      } 
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }  
  } 
  @SuppressWarnings("unchecked")
  static  public class SearchActionListener extends EventListener<UISelectUserForm> {
    @Override
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource() ;
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class) ;
      String keyword = uiForm.getUIStringInput(FIELD_KEYWORD).getValue();
      String filter = uiForm.getUIFormSelectBox(FIELD_FILTER).getValue() ;
      if(CalendarUtils.isEmpty(keyword)) {
        uiForm.init(uiForm.pars_) ;
      }  else {
        keyword = new StringBuilder().append("*").append(keyword).append("*").toString() ;
        Query q = new Query() ;
        if(USER_NAME.equals(filter)) {
          q.setUserName(keyword) ;
        } 
        if(LAST_NAME.equals(filter)) {
          q.setLastName(keyword) ;
        }
        if(FIRST_NAME.equals(filter)) {
          q.setFirstName(keyword) ;
        }
        if(EMAIL.equals(filter)) {
          q.setEmail(keyword) ;
        }
        List results = new CopyOnWriteArrayList() ;
        results.addAll(service.getUserHandler().findUsers(q).getAll()) ;
        
        MembershipHandler memberShipHandler = service.getMembershipHandler();
        String groupId = uiForm.getSelectedGroup();
        if(groupId != null && groupId.trim().length() != 0) {
          for(Object user : results) {
            if(memberShipHandler.findMembershipsByUserAndGroup(((User)user).getUserName(), groupId).size() == 0) {
              results.remove(user);
            }
          }
        }
        LazyPageList<User> pageList = new LazyPageList<User>(new ListAccessImpl<User>(User.class, results) , 10);
        uiForm.uiIterator_.setPageList(pageList);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class ChangeActionListener extends EventListener<UISelectUserForm> {
    @Override
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiForm = event.getSource() ;
      uiForm.setSelectedGroup(uiForm.getSelectedGroup()) ;
      uiForm.setKeyword(null) ;
      OrganizationService service = CalendarUtils.getOrganizationService() ;
      if(!CalendarUtils.isEmpty(uiForm.getSelectedGroup())) {
        uiForm.uiIterator_.setPageList(service.getUserHandler().findUsersByGroup(uiForm.getSelectedGroup()));
      } else {
        uiForm.uiIterator_.setPageList(service.getUserHandler().getUserPageList(0));
      }
      for(String s : uiForm.pars_) {
        if(uiForm.getUICheckBoxInput(s) != null) uiForm.getUICheckBoxInput(s).setChecked(true) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  static  public class CloseActionListener extends EventListener<UISelectUserForm> {
    @Override
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiAddressForm = event.getSource();  
      UIPopupContainer uiContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction chilPopup =  uiContainer.getChild(UIPopupAction.class) ;
      chilPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(chilPopup) ;
    }
  }
  static  public class ShowPageActionListener extends EventListener<UISelectUserForm> {
    @Override
    public void execute(Event<UISelectUserForm> event) throws Exception {
      UISelectUserForm uiSelectUserForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiSelectUserForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectUserForm);           
    }
  }
}
