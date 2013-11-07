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
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UIFormDateTimePicker;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.cssfile.CssClassUtils;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.ext.UIFormComboBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

import org.exoplatform.calendar.webui.popup.UIEventDetailTab.FileActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
                 template = "app:/templates/calendar/webui/UIPopup/UITaskDetailTab.gtmpl"
    ) 
public class UITaskDetailTab extends UIFormInputWithActions {

  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_FROM_TIME = "fromTime".intern() ;
  final public static String FIELD_TO_TIME = "toTime".intern() ;

  final public static String FIELD_CHECKALL = "allDay".intern() ;
  final public static String FIELD_DELEGATION = "delegation".intern() ;

  final public static String FIELD_PRIORITY = "priority".intern() ; 
  final public static String FIELD_DESCRIPTION = "description".intern() ;
  final public static String FIELD_STATUS = "status".intern() ;
  final static public String FIELD_ATTACHMENTS = "attachments".intern() ;
  final static public String LABEL_ADD_ATTACHMENTS = "addfiles";
  
  protected List<Attachment> attachments_ = new ArrayList<Attachment>() ;
  private Map<String, List<ActionData>> actionField_ ;
  public UITaskDetailTab(String arg0) throws Exception {
    super(arg0);
    setComponentConfig(getClass(), null) ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null).addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormSelectBoxWithGroups(FIELD_CALENDAR, FIELD_CALENDAR, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, CalendarUtils.getCategory())) ;
    addUIFormInput(new UIFormSelectBox(FIELD_STATUS, FIELD_STATUS, getStatus())) ;

    ActionData addCategoryAction = new ActionData() ;
    addCategoryAction.setActionType(ActionData.TYPE_ICON) ;
    addCategoryAction.setCssIconClass("uiIconPlus uiIconLightGray");
    addCategoryAction.setActionName(UIEventForm.ACT_ADDCATEGORY) ;
    addCategoryAction.setActionListener(UIEventForm.ACT_ADDCATEGORY) ;
    List<ActionData> addCategoryActions = new ArrayList<ActionData>() ;
    addCategoryActions.add(addCategoryAction) ;
    setActionField(FIELD_CATEGORY, addCategoryActions) ;

    addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    setActionField(FIELD_ATTACHMENTS, getAttachmentData()) ;

    addUIFormInput(new UIFormDateTimePicker(FIELD_FROM, FIELD_FROM, new Date(), false));

    addUIFormInput(new UIFormComboBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormComboBox(FIELD_TO_TIME, FIELD_TO_TIME,  options));

    addUIFormInput(new UIFormDateTimePicker(FIELD_TO, FIELD_TO, new Date(), false));
    addUIFormInput(new UICheckBoxInput(FIELD_CHECKALL, FIELD_CHECKALL, null));
    addUIFormInput(new UIFormStringInput(FIELD_DELEGATION, FIELD_DELEGATION, null));
    addUIFormInput(new UIFormSelectBox(FIELD_PRIORITY, FIELD_PRIORITY, getPriority())) ;

    ActionData addEmailAddress = new ActionData() ;
    addEmailAddress.setActionType(ActionData.TYPE_ICON) ;
    addEmailAddress.setCssIconClass("uiIconEmail uiIconLightGray");
    addEmailAddress.setActionName(UITaskForm.ACT_ADDEMAIL) ;
    addEmailAddress.setActionListener(UITaskForm.ACT_ADDEMAIL) ;

    List<ActionData> addMailActions = new ArrayList<ActionData>() ;
    addMailActions.add(addEmailAddress) ;


    ActionData selectUser = new ActionData() ;
    selectUser.setActionType(ActionData.TYPE_ICON) ;
    selectUser.setCssIconClass("uiIconPlus uiIconLightGray");
    selectUser.setActionName(UITaskForm.ACT_SELECTUSER) ;
    selectUser.setActionListener(UITaskForm.ACT_SELECTUSER) ;

    List<ActionData> selectUsers = new ArrayList<ActionData>() ;
    selectUsers.add(selectUser) ;
    setActionField(FIELD_DELEGATION, selectUsers) ;

  }

  private List<SelectItemOption<String>> getStatus() {
    List<SelectItemOption<String>> status = new ArrayList<SelectItemOption<String>>() ;
    for(String taskStatus : CalendarEvent.TASK_STATUS) {
      status.add(new SelectItemOption<String>(taskStatus, taskStatus)) ;
    }
    return status ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }

  /**
   * used in groovy template
   *
   * @return
   * @throws Exception
   */
  public List<ActionData> getAttachmentData() throws Exception {
    List<ActionData> attachmentData = new ArrayList<ActionData>() ;
    for (Attachment attachment : attachments_) {
      FileActionData fileUpload = new FileActionData() ;
      fileUpload.setActionListener(UIEventForm.ACT_DOWNLOAD) ;
      fileUpload.setActionParameter(attachment.getId()) ;
      fileUpload.setActionType(ActionData.TYPE_LINK) ;
      fileUpload.setCssIconClass(CssClassUtils.getCSSClassByFileNameAndFileType(attachment.getName(), attachment.getMimeType(), null)) ;
      fileUpload.setActionName(attachment.getName()) ;
      fileUpload.setFileSize(CalendarUtils.convertSize(attachment.getSize()));
      fileUpload.setShowLabel(true) ;

      attachmentData.add(fileUpload) ;

      FileActionData removeAction = new FileActionData() ;
      removeAction.setActionListener(UIEventForm.ACT_REMOVE) ;
      removeAction.setActionName(UIEventForm.ACT_REMOVE);
      removeAction.setActionParameter(attachment.getId());
      removeAction.setActionType(ActionData.TYPE_ICON) ;
      removeAction.setCssIconClass("uiIconDelete uiIconLightGray");
      removeAction.setBreakLine(true) ;

      attachmentData.add(removeAction) ;
    }

    return attachmentData;
  }

  public void addToUploadFileList(Attachment attachfile) {
    attachments_.add(attachfile) ;
  }
  public void removeFromUploadFileList(Attachment attachfile) {
    attachments_.remove(attachfile);
  }  
  public void refreshUploadFileList() throws Exception {
    setActionField(FIELD_ATTACHMENTS, getAttachmentData()) ;
  }
  protected List<Attachment> getAttachments() { 
    return attachments_ ;
  }
  protected void setAttachments(List<Attachment> attachment) { 
    attachments_ = attachment ;
  }

  protected List<SelectItemOption<String>> getCalendar() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    List<Calendar> calendars = calendarService.getUserCalendars(username, true) ;
    for(Calendar c : calendars) {
      options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
    }
    return options ;
  }
  private List<SelectItemOption<String>> getPriority() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("none", "none")) ;
    options.add(new SelectItemOption<String>("normal", "normal")) ;
    options.add(new SelectItemOption<String>("high", "high")) ;
    options.add(new SelectItemOption<String>("low", "low")) ;
    return options ;
  }
  protected List<SelectItemOption<String>> getRepeater() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String s : CalendarEvent.REPEATTYPES) {
      options.add(new SelectItemOption<String>(s,s)) ;
    }
    return options ;
  }
  
  public void setActionField(String fieldName, List<ActionData> actions){
    actionField_.put(fieldName, actions) ;
  }

  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}

  public UIFormComboBox getUIFormComboBox(String id) {
    return findComponentById(id);
  }  

  public UIFormSelectBoxWithGroups getUIFormSelectBoxGroup(String id) {
    return findComponentById(id);
  }
  
  protected int getTimeShift(){
	  try {
		    return Integer.parseInt(PropertyManager.getProperty("exo.calendar.default.task.suggest")); 
		  } catch (Exception e) {
			return 2;
	  }
  }

}
