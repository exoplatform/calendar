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

import java.util.*;

import javax.jcr.ItemExistsException;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.*;
import org.exoplatform.calendar.webui.CalendarView;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
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
        template = "app:/templates/calendar/webui/UIEventCategoryForm.gtmpl",
    events = {
      @EventConfig(listeners = UIEventCategoryForm.SaveActionListener.class),
      @EventConfig(listeners = UIEventCategoryForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEventCategoryForm extends UIForm {
  private static final Log log = ExoLogger.getExoLogger(UIEventCategoryForm.class);
  
  final private static String EVENT_CATEGORY_NAME = "eventCategoryName" ; 
  private boolean isAddNew_ = true ;
  private EventCategory eventCategory_ = null ;
  public UIEventCategoryForm() throws Exception{
    addUIFormInput(new UIFormStringInput(EVENT_CATEGORY_NAME, EVENT_CATEGORY_NAME, null)
    .addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class)) ;
  }
  protected String getCategoryName() {return getUIStringInput(EVENT_CATEGORY_NAME).getValue() ;}
  protected void setCategoryName(String value) {getUIStringInput(EVENT_CATEGORY_NAME).setValue(value) ;}

  @Override
  public void reset() {
    super.reset() ;
    setAddNew(true);
    setEventCategory(null);
  }

  protected void setAddNew(boolean isAddNew) {
    this.isAddNew_ = isAddNew;
  }
  protected boolean isAddNew() {
    return isAddNew_;
  }

  protected void setEventCategory(EventCategory eventCategory) {
    this.eventCategory_ = eventCategory;
  }
  protected EventCategory getEventCategory() {
    return eventCategory_;
  }

  static  public class SaveActionListener extends EventListener<UIEventCategoryForm> {
    @Override
    public void execute(Event<UIEventCategoryForm> event) throws Exception {
      UIEventCategoryForm uiForm = event.getSource() ;
      UIEventCategoryManager uiManager = uiForm.getAncestorOfType(UIEventCategoryManager.class);
      
      String name = uiForm.getUIStringInput(EVENT_CATEGORY_NAME).getValue() ;
      if(!CalendarUtils.isEmpty(name)) {
        name = name.trim() ;
      }
      
      name = CalendarUtils.reduceSpace(name) ;
      CalendarService calendarService = CalendarUtils.getCalendarService();
      String username = CalendarUtils.getCurrentUser() ;
      List<EventCategory> categories = calendarService.getEventCategories(username);
      EventCategory eventCat = new EventCategory() ;
      eventCat.setName(name) ;
      try {
        if(uiForm.isAddNew_) {
          for (EventCategory cat : categories) {
            if (Utils.getLocalizedName(cat).equalsIgnoreCase(eventCat.getName())) {
              throw new ItemExistsException();
            }
          }
          calendarService.saveEventCategory(username, eventCat, true) ;
        } else { 
          eventCat = uiForm.getEventCategory() ;
          for (EventCategory cat : categories) {
            if (Utils.getLocalizedName(cat).equalsIgnoreCase(name) && !eventCat.getName().equalsIgnoreCase(name)) {
              throw new ItemExistsException();
            }
          }
          eventCat.setName(name) ;
          calendarService.saveEventCategory(username, eventCat, false) ; 
        }
        Long currentPage = uiManager.getCurrentPage() ;
        uiManager.updateGrid() ;
        if(currentPage <= uiManager.getAvailablePage()) 
          uiManager.setCurrentPage(currentPage.intValue()) ;
        else {
          currentPage = uiManager.getAvailablePage() ;
          uiManager.setCurrentPage(currentPage.intValue()) ;
        }
        uiForm.reset() ;
        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        uiMiniCalendar.setCategoryId(null) ;
        UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        uiViewContainer.refresh() ;
        uiViewContainer.updateCategory() ;
        if(uiViewContainer.getRenderedChild() instanceof CalendarView) {
          ((CalendarView)uiViewContainer.getRenderedChild()).setSelectedCategory(uiManager.categoryId_) ; 
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer.getParent()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
      } catch (ItemExistsException e) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIEventCategoryForm.msg.name-exist", null));
        return ;
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to save event category", e);
        }
      }
    }
  }

  static  public class CancelActionListener extends EventListener<UIEventCategoryForm> {
    @Override
    public void execute(Event<UIEventCategoryForm> event) throws Exception {
      UIEventCategoryForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
