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

import org.exoplatform.calendar.webui.UIEmailInput;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    template = "app:/templates/calendar/webui/UIPopup/UIEventReminderTab.gtmpl"
) 
public class UIEventReminderTab extends UIFormInputWithActions {


  final public static String REMIND_BY_EMAIL = "mailReminder".intern() ;
  final public static String EMAIL_REMIND_BEFORE = "mailReminderTime".intern() ;
  final public static String FIELD_EMAIL_ADDRESS = "mailReminderAddress".intern() ;
  final public static String EMAIL_REPEAT_INTERVAL = "emailRepeatInterval".intern() ;
  final public static String EMAIL_IS_REPEAT = "emailIsRepeat".intern() ;
  final public static String REPEAT = "repeat".intern() ;
  
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
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(REMIND_BY_EMAIL, REMIND_BY_EMAIL, false)) ;
    addUIFormInput(new UIFormSelectBox(EMAIL_REMIND_BEFORE, EMAIL_REMIND_BEFORE, emailRemindBeforeOptions));
    //addUIFormInput(new UIFormTextAreaInput(FIELD_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS, null)) ;
    addUIFormInput(new UIEmailInput(FIELD_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS, null)) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(EMAIL_IS_REPEAT, EMAIL_IS_REPEAT, false));
    addUIFormInput(new UIFormSelectBox(EMAIL_REPEAT_INTERVAL, EMAIL_REPEAT_INTERVAL, emailRemindRepeatOptions));
    ActionData addEmailAddress = new ActionData() ;
    addEmailAddress.setActionType(ActionData.TYPE_ICON) ;
    addEmailAddress.setActionName(UIEventForm.ACT_ADDEMAIL) ;
    addEmailAddress.setActionListener(UIEventForm.ACT_ADDEMAIL) ;

    List<ActionData> addMailActions = new ArrayList<ActionData>() ;
    addMailActions.add(addEmailAddress) ;
    setActionField(FIELD_EMAIL_ADDRESS, addMailActions) ;

    List<SelectItemOption<String>> popupRemindRepeatOptions = getReminderTimes(5,60) ;
    List<SelectItemOption<String>> popupRemindBeforeOptions = getReminderTimes(5,60) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(REMIND_BY_POPUP, REMIND_BY_POPUP, false)) ;
    addUIFormInput(new UIFormSelectBox(POPUP_REMIND_BEFORE, POPUP_REMIND_BEFORE, popupRemindBeforeOptions));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(POPUP_IS_REPEAT, POPUP_IS_REPEAT, false));
    addUIFormInput(new UIFormSelectBox(POPUP_REPEAT_INTERVAL, POPUP_REPEAT_INTERVAL, popupRemindRepeatOptions));

  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }

  public List<SelectItemOption<String>> getReminderTimes(int steps, int maxValue) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(int i = 1; i <= maxValue/steps ; i++) {
      options.add(new SelectItemOption<String>(String.valueOf(i*steps)+" minutes", String.valueOf(i*steps))) ;      
    }
    return options ;
  }

  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }


}
