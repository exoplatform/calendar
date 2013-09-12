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
import java.util.List;
import java.util.Map;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.webui.UIEmailInput;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfigs( {
  @ComponentConfig (
                    template = "app:/templates/calendar/webui/UIPopup/UIEventReminderTab.gtmpl",
                    events = {
                        @EventConfig(listeners = UIEventReminderTab.AddEmailAddressActionListener.class, phase = Phase.DECODE)
                    }

  ),
  @ComponentConfig(
                   id = "UIPopupWindowUserSelectEventFormForReminder",
                   type = UIPopupWindow.class,
                   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
                   events = {
                     @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup"),
                     @EventConfig(listeners = UIEventReminderTab.AddActionListener.class, name = "Add", phase = Phase.DECODE),
                     @EventConfig(listeners = UIEventReminderTab.CloseActionListener.class, phase = Phase.DECODE )
                   }
  )
}
)  
public class UIEventReminderTab extends UIFormInputWithActions {


  final public static String REMIND_BY_EMAIL = "mailReminder".intern() ;
  final public static String EMAIL_REMIND_BEFORE = "mailReminderTime".intern() ;
  final public static String FIELD_EMAIL_ADDRESS = "mailReminderAddress".intern() ;
  final public static String EMAIL_REPEAT_INTERVAL = "emailRepeatInterval".intern() ;
  final public static String EMAIL_IS_REPEAT = "emailIsRepeat".intern() ;
  final public static String REPEAT = "repeat".intern() ;
  final public static String MINUTES = "minutes".intern() ;
  
  final public static String REMIND_BY_POPUP = "popupReminder".intern() ;
  final public static String POPUP_REMIND_BEFORE = "popupReminderTime".intern() ;
  final public static String POPUP_REPEAT_INTERVAL = "popupRepeatInterval".intern() ;
  final public static String POPUP_IS_REPEAT = "popupIsRepeat".intern() ; 

  private Map<String, List<ActionData>> actionField_ ;
  public UIEventReminderTab(String arg0) throws Exception {
    super(arg0);
    setComponentConfig(getClass(), null) ;
    actionField_ = new HashMap<String, List<ActionData>>() ;

    List<SelectItemOption<String>> emailRemindRepeatOptions = getReminderTimes(5,60) ;
    List<SelectItemOption<String>> emailRemindBeforeOptions = getReminderTimes(5,60) ;
    addUIFormInput(new UICheckBoxInput(REMIND_BY_EMAIL, REMIND_BY_EMAIL, false)) ;
    addUIFormInput(new UIFormSelectBox(EMAIL_REMIND_BEFORE, EMAIL_REMIND_BEFORE, emailRemindBeforeOptions));
    addUIFormInput(new UIEmailInput(FIELD_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS, null)) ;
    addUIFormInput(new UICheckBoxInput(EMAIL_IS_REPEAT, EMAIL_IS_REPEAT, false));
    addUIFormInput(new UIFormSelectBox(EMAIL_REPEAT_INTERVAL, EMAIL_REPEAT_INTERVAL, emailRemindRepeatOptions));
    ActionData addEmailAddress = new ActionData() ;
    addEmailAddress.setActionType(ActionData.TYPE_ICON);
    addEmailAddress.setActionName(UIEventForm.ACT_ADDEMAIL) ;
    addEmailAddress.setActionListener(UIEventForm.ACT_ADDEMAIL) ;

    List<ActionData> addMailActions = new ArrayList<ActionData>() ;
    addMailActions.add(addEmailAddress) ;
    setActionField(FIELD_EMAIL_ADDRESS, addMailActions) ;

    List<SelectItemOption<String>> popupRemindRepeatOptions = getReminderTimes(5,60) ;
    List<SelectItemOption<String>> popupRemindBeforeOptions = getReminderTimes(5,60) ;
    addUIFormInput(new UICheckBoxInput(REMIND_BY_POPUP, REMIND_BY_POPUP, false)) ;
    addUIFormInput(new UIFormSelectBox(POPUP_REMIND_BEFORE, POPUP_REMIND_BEFORE, popupRemindBeforeOptions));
    addUIFormInput(new UICheckBoxInput(POPUP_IS_REPEAT, POPUP_IS_REPEAT, false));
    addUIFormInput(new UIFormSelectBox(POPUP_REPEAT_INTERVAL, POPUP_REPEAT_INTERVAL, popupRemindRepeatOptions));

  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }

  public List<SelectItemOption<String>> getReminderTimes(int steps, int maxValue) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(int i = 1; i <= maxValue/steps ; i++) {
      options.add(new SelectItemOption<String>(String.valueOf(i*steps)+" "+CalendarUtils.getResourceBundle("UIEventForm.label.minutes",MINUTES), String.valueOf(i*steps))) ;      
    }
    return options ;
  }

  public void setActionField(String fieldName, List<ActionData> actions){
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
  
  
  static  public class AddEmailAddressActionListener extends EventListener<UIEventReminderTab> {
    @Override
    public void execute(Event<UIEventReminderTab> event) throws Exception {
      UIEventReminderTab uiEventReminderTab = event.getSource() ;
      UIPopupContainer uiPopupContainer = uiEventReminderTab.getParent().getParent();  
      uiPopupContainer.deActivate();
      UIPopupWindow uiPopupWindow = uiPopupContainer.getChild(UIPopupWindow.class) ;
      if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelectEventFormForReminder", "UIPopupWindowUserSelectEventFormForReminder") ;
      UIUserSelector uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelector.class, null, null) ;
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true) ;
      uiUserSelector.setShowSearchGroup(true);
      uiPopupWindow.setUIComponent(uiUserSelector);
      uiPopupWindow.setShow(true);
      uiPopupWindow.setRendered(true);
      uiPopupWindow.setWindowSize(740, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
    }
  }
  
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      UIForm uiForm = uiContainer.getChild(UIEventForm.class) == null ? uiContainer.getChild(UITaskForm.class) : uiContainer.getChild(UIEventForm.class);
      UIEventReminderTab uiEventReminderTab = uiForm.getChild(UIEventReminderTab.class);
      UIEmailInput uiEmailInput = uiEventReminderTab.getChildById(FIELD_EMAIL_ADDRESS);
      String values = uiUserSelector.getSelectedUsers();
      List<String> newEmails = new ArrayList<String>() ;
      for (String value : values.split(CalendarUtils.COMMA)) {
        String email = CalendarUtils.getOrganizationService().getUserHandler().findUserByName(value).getEmail() ;
        if (!newEmails.contains(email)) {
          newEmails.add(email) ;
        }
      }
      uiEmailInput.setValue(appendMail(uiEmailInput,newEmails));
      uiPoupPopupWindow.setUIComponent(null) ;
      //close select user popup
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

  private static String appendMail(UIEmailInput uiEmailInput, List<String> newMails) {
    StringBuilder sb = new StringBuilder();
    String existedMails = uiEmailInput.getValue();
    if(existedMails.equals("")) {
      java.util.Iterator<String> it = newMails.iterator();
      while(it.hasNext()) {
        sb.append(it.next());
        if(!it.hasNext()) {
          break;
        }
        sb.append(",");
      }
      return sb.toString();
    } else {
      sb.append(existedMails);
      for(String mail : newMails) {
        if(!existedMails.contains(mail)) {
          sb.append("," + mail);
        }
      }
      return sb.toString();
    }
  }
}
