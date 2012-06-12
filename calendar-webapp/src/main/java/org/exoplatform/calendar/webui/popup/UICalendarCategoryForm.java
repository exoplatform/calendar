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

import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarCategoryForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarCategoryForm.ResetActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UICalendarCategoryForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UICalendarCategoryForm extends UIForm {
  private static final Log log = ExoLogger.getExoLogger(UICalendarCategoryForm.class);
  
  final static String CATEGORY_NAME = "categoryName" ;
  final static String DESCRIPTION = "description" ;
  private boolean isAddNew = true ;
  private String categoryId = null ;
  public UICalendarCategoryForm() throws Exception {
    addUIFormInput(new UIFormStringInput(CATEGORY_NAME, null).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
  }

  public void reset() {
    super.reset() ;
    isAddNew = true ;
    categoryId = null ;
  }
  public void init(String categoryId) throws Exception{
    setAddNew(false) ;
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = CalendarUtils.getCurrentUser() ;
    CalendarCategory category = calService.getCalendarCategory(username, categoryId) ;
    if (category.getId().equals(NewUserListener.defaultCalendarCategoryId) && category.getName().equals(NewUserListener.defaultCalendarCategoryName)) {
      String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.defaultCalendarCategoryId, NewUserListener.defaultCalendarCategoryId);
      category.setName(newName);
    }
    setCategoryId(category.getId()) ;
    setCategoryName(category.getName()) ;
    setCategoryDescription(category.getDescription()) ;
  }
  protected void setAddNew(boolean isAddNew) {
    this.isAddNew = isAddNew;
  }

  protected boolean isAddNew() {
    return isAddNew;
  }

  protected void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  protected String getCategoryId() {
    return categoryId;
  }

  protected String getCategoryName() {return getUIStringInput(CATEGORY_NAME).getValue() ;}
  protected void setCategoryName(String value) { getUIStringInput(CATEGORY_NAME).setValue(value) ;}

  protected String getCategoryDescription() {return getUIFormTextAreaInput(DESCRIPTION).getValue() ;}
  protected void setCategoryDescription(String value) { getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;}

  static  public class SaveActionListener extends EventListener<UICalendarCategoryForm> {
    public void execute(Event<UICalendarCategoryForm> event) throws Exception {
      UICalendarCategoryForm uiForm = event.getSource() ;
      UICalendarCategoryManager uiManager = uiForm.getAncestorOfType(UICalendarCategoryManager.class) ;
      String categoryName = uiForm.getCategoryName() ;
      // CS-3009
      categoryName = CalendarUtils.reduceSpace(categoryName) ;
      /*if(!CalendarUtils.isNameValid(categoryName, CalendarUtils.SPECIALCHARACTER)){
        uiApp.addMessage(new ApplicationMessage("UICalendarCategoryForm.msg.name-invalid", null, ApplicationMessage.WARNING) ) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }*/
      String description = uiForm.getCategoryDescription() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      try {
        CalendarService calendarService = CalendarUtils.getCalendarService() ;
        String username = CalendarUtils.getCurrentUser() ;
        boolean existed = false ;
                       
        List<CalendarCategory> gData = calendarService.getCategories(username) ;
        if(uiForm.isAddNew())  {
          for(CalendarCategory cal : gData) {
            if (cal.getName().trim().equalsIgnoreCase(categoryName.trim())) {
              existed = true ;
              break ;
            }
          }
        } else {
          for(CalendarCategory cal : gData) {
            if (!cal.getId().equals(uiForm.getCategoryId()) &&  cal.getName().trim().equalsIgnoreCase(categoryName.trim())) {
              existed = true ;
              break ;
            }
          }
        }
        if(existed) {
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UICalendarCategoryForm.msg.group-existed", null));
          return ;
        }
        CalendarCategory category = new CalendarCategory() ;
        if(!uiForm.isAddNew()) category.setId(uiForm.getCategoryId()) ; 
        category.setName(categoryName.trim()) ;
        category.setDescription(description) ;
        calendarService.saveCalendarCategory(username, category, uiForm.isAddNew()) ;
        UICalendarForm uiCalendarForm = calendarPortlet.findFirstComponentOfType(UICalendarForm.class) ;
        if(uiCalendarForm != null) {
          uiCalendarForm.reloadCategory() ;
          //cs-1905
          uiCalendarForm.setSelectedGroup(category.getId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarForm.getChildById(UICalendarForm.INPUT_CALENDAR)) ;
        }
        uiManager.updateGrid() ;
        uiForm.reset() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManager.getAncestorOfType(UIPopupAction.class)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarPortlet.findFirstComponentOfType(UICalendars.class)) ;
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("fail to save calendar category", e);
        }
      }
    }
  }
  static  public class ResetActionListener extends EventListener<UICalendarCategoryForm> {
    public void execute(Event<UICalendarCategoryForm> event) throws Exception {
      UICalendarCategoryForm uiForm = event.getSource() ;
      uiForm.reset() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarCategoryForm> {
    public void execute(Event<UICalendarCategoryForm> event) throws Exception {
      UICalendarCategoryForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

}
