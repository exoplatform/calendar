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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.ext.UIFormComboBox;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    template = "app:/templates/calendar/webui/UIPopup/UIEventDetailTab.gtmpl"
) 
public class UIEventDetailTab extends UIFormInputWithActions {

  final public static String FIELD_MESSAGE = "messageName".intern() ;
  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_FROM_TIME = "fromTime".intern() ;
  final public static String FIELD_TO_TIME = "toTime".intern() ;

  final public static String FIELD_CHECKALL = "allDay".intern() ;
  final public static String FIELD_REPEAT = "repeat".intern() ;
  final public static String FIELD_ISREPEAT = "isRepeat".intern();
  final public static String FIELD_REPEAT_UNTIL = "repeatUntil".intern();
  final public static String FIELD_PLACE = "place".intern() ;
  final public static String FIELD_PRIORITY = "priority".intern() ; 
  final public static String FIELD_DESCRIPTION = "description".intern() ;
  final static public String FIELD_ATTACHMENTS = "attachments".intern() ;

  protected List<Attachment> attachments_ = new ArrayList<Attachment>() ;
  private Map<String, List<ActionData>> actionField_ ;
  
  public UIEventDetailTab(String id) throws Exception {
    super(id);
    setComponentConfig(getClass(), null) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null).addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormSelectBoxWithGroups(FIELD_CALENDAR, FIELD_CALENDAR, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, CalendarUtils.getCategory())) ;
    ActionData addCategoryAction = new ActionData() ;
    addCategoryAction.setActionType(ActionData.TYPE_ICON) ;
    addCategoryAction.setActionName(UIEventForm.ACT_ADDCATEGORY) ;
    addCategoryAction.setActionListener(UIEventForm.ACT_ADDCATEGORY) ;
    List<ActionData> addCategoryActions = new ArrayList<ActionData>() ;
    addCategoryActions.add(addCategoryAction) ;
    setActionField(FIELD_CATEGORY, addCategoryActions) ;
    addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
    addUIFormInput(new UIFormComboBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormComboBox(FIELD_TO_TIME, FIELD_TO_TIME,  options));
    addUIFormInput(new UIFormDateTimePicker(FIELD_FROM, FIELD_FROM, new Date(), false));
    addUIFormInput(new UIFormDateTimePicker(FIELD_TO, FIELD_TO, new Date(), false));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_CHECKALL, FIELD_CHECKALL, null));
    addUIFormInput(new UIFormStringInput(FIELD_PLACE, FIELD_PLACE, null));
    //addUIFormInput(new UIFormSelectBox(FIELD_REPEAT, FIELD_REPEAT, getRepeater())) ;
    
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ISREPEAT, FIELD_ISREPEAT, false));
    
    ActionData editRepeatAction = new ActionData() ;
    editRepeatAction.setActionType(ActionData.TYPE_ICON) ;
    editRepeatAction.setActionName(UIEventForm.ACT_EDITREPEAT) ;
    editRepeatAction.setActionListener(UIEventForm.ACT_EDITREPEAT) ;
    List<ActionData> editRepeatActions = new ArrayList<ActionData>() ;
    editRepeatActions.add(editRepeatAction) ;
    setActionField(FIELD_ISREPEAT, editRepeatActions) ;
    
    //addUIFormInput(new UIFormDateTimePicker(FIELD_REPEAT_UNTIL, FIELD_REPEAT_UNTIL, null, false));
    
    addUIFormInput(new UIFormSelectBox(FIELD_PRIORITY, FIELD_PRIORITY, getPriority())) ;
    ActionData addEmailAddress = new ActionData() ;
    addEmailAddress.setActionType(ActionData.TYPE_ICON) ;
    addEmailAddress.setActionName(UIEventForm.ACT_ADDEMAIL) ;
    addEmailAddress.setActionListener(UIEventForm.ACT_ADDEMAIL) ;
    List<ActionData> addMailActions = new ArrayList<ActionData>() ;
    addMailActions.add(addEmailAddress) ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  
  public List<ActionData> getUploadFileList() throws Exception { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(Attachment attachdata : attachments_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener(UIEventForm.ACT_DOWNLOAD) ;
      fileUpload.setActionParameter(attachdata.getId()) ;
      fileUpload.setActionType(ActionData.TYPE_LINK) ;
      fileUpload.setCssIconClass("AttachmentIcon ZipFileIcon") ;
      fileUpload.setActionName(attachdata.getName() + "-(" + CalendarUtils.convertSize(attachdata.getSize()) + ")" ) ;
      fileUpload.setShowLabel(true) ;
      uploadedFiles.add(fileUpload) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener(UIEventForm.ACT_REMOVE) ;
      removeAction.setActionName(UIEventForm.ACT_REMOVE);
      removeAction.setActionParameter(attachdata.getId());
      removeAction.setActionType(ActionData.TYPE_ICON) ;
      removeAction.setCssIconClass("RemoveFile");
      removeAction.setBreakLine(true) ;
      uploadedFiles.add(removeAction) ;
    }
    return uploadedFiles ;
  }

  public void addToUploadFileList(Attachment attachfile) {
    attachments_.add(attachfile) ;
  }
  public void removeFromUploadFileList(Attachment attachfile) {
    attachments_.remove(attachfile);
  }  
  public void refreshUploadFileList() throws Exception {
    setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
  }
  protected List<Attachment> getAttachments() { 
    return attachments_ ;
  }
  protected void setAttachments(List<Attachment> attachment) { 
    attachments_ = attachment ;
  }


  private List<SelectItemOption<String>> getPriority() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("none", "none")) ;
    options.add(new SelectItemOption<String>("normal", "normal")) ;
    options.add(new SelectItemOption<String>("high", "high")) ;
    options.add(new SelectItemOption<String>("low", "low")) ;
    return options ;
  }

  /*private List<SelectItemOption<String>> getReminder() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String rmdType : Reminder.REMINDER_TYPES) {
      options.add(new SelectItemOption<String>(rmdType, rmdType)) ;
    }
    return options ;
  }*/
  public void setActionField(String fieldName, List<ActionData> actions){
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
  public UIFormComboBox getUIFormComboBox (String id) {
    return findComponentById(id);
  }
  public UIFormDateTimePicker getUIFormDateTimePicker (String id) {
    return findComponentById(id) ;
  }
  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id) ;
  }
}
