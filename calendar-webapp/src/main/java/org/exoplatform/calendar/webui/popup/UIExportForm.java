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

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

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
      @EventConfig(listeners = UIExportForm.SaveActionListener.class),      
      @EventConfig(listeners = UIExportForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIExportForm extends UIForm implements UIPopupComponent{
  final static private String NAME = "name".intern() ;
  final static private String TYPE = "type".intern() ;
  private String calType = "0" ;
  private Map<String,String> names_ = new HashMap<String, String>() ;
  public String eventId = null ;
  public UIExportForm() throws Exception {
    addUIFormInput(new UIFormStringInput(NAME, NAME, null)) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ; 
    options.add(new SelectItemOption<String>(CalendarService.ICALENDAR, CalendarService.ICALENDAR)) ;
    addUIFormInput(new UIFormSelectBox(TYPE, TYPE, options)) ;
  }
  public void setCalType(String type) {calType = type ; }
  public void update(String type, List<Calendar> calendars, String selectedCalendarId) throws Exception {
    calType = type ;
    names_.clear() ;
    Iterator iter = getChildren().iterator() ;
    while(iter.hasNext()) {
      if(iter instanceof UIFormCheckBoxInput) {
        iter.remove() ;
      }
      iter.next() ;
    }
    initCheckBox(calendars, selectedCalendarId) ;
  }
  public void initCheckBox(List<Calendar> calendars, String selectedCalendarId) {
    for(Calendar calendar : calendars) {
      UIFormCheckBoxInput checkBox = new UIFormCheckBoxInput<Boolean>(calendar.getId(), calendar.getId(), false);
      if(calendar.getId().equals(selectedCalendarId)) checkBox.setChecked(true) ; 
      else checkBox.setChecked(false) ;
      if(eventId != null) checkBox.setEnable(false) ;
      else checkBox.setEnable(true) ;
      addUIFormInput(checkBox) ;
      names_.put(calendar.getId(), calendar.getName()) ;
    }
  }
  public String getLabel(String id) throws Exception {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      ResourceBundle res = context.getApplicationResourceBundle() ;     
      String label = getId() + ".label." + id;
      try {
        return res.getString(label);      
      } catch (MissingResourceException e) {
        if( names_.get(id) != null) return  names_.get(id) ;
      }
      return id ;
  } 

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  static  public class SaveActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource() ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      List<UIComponent> children = uiForm.getChildren() ;
      List<String> calendarIds = new ArrayList<String> () ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          UIFormCheckBoxInput input =   ((UIFormCheckBoxInput)child) ;
          if(input.isChecked()) calendarIds.add(((UIFormCheckBoxInput)child).getBindingField()) ;
        }
      }
      if(calendarIds.isEmpty()) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIExportForm.msg.calendar-does-not-existing", null));
        
        return ;
      }
      String type = uiForm.getUIFormSelectBox(TYPE).getValue() ;
      String name = uiForm.getUIStringInput(NAME).getValue() ;
      CalendarImportExport importExport = calendarService.getCalendarImportExports(type) ;
      OutputStream out = null ;
      try {
        if(uiForm.eventId != null)
           out = importExport.exportEventCalendar(CalendarUtils.getCurrentUser(), calendarIds.get(0), uiForm.calType, uiForm.eventId) ;
        else out = importExport.exportCalendar(CalendarUtils.getCurrentUser(), calendarIds, uiForm.calType, -1) ;
        ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
        DownloadResource dresource = new InputStreamDownloadResource(is, "text/iCalendar") ;
        DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class) ;
        if(name != null && name.length() > 0) {
          if(name.length() > 4 && name.substring(name.length() - 4).equals(".ics") )dresource.setDownloadName(name);
          else dresource.setDownloadName(name + ".ics");
        }else {
          dresource.setDownloadName("eXoICalendar.ics");
        }
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');") ;
        calendarPortlet.cancelAction() ;      
      }catch(Exception e) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIExportForm.msg.event-does-not-existing", null));
        return ;
      }
    }
  }

  static  public class CancelActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }  
}
