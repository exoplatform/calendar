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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIExportForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportForm.SaveActionListener.class),      
      @EventConfig(listeners = UIExportForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIExportForm extends UIForm implements UIPopupComponent{
  private static final Log LOG = ExoLogger.getExoLogger(UIExportForm.class);

  final static private String NAME = "name";

  private static final String EXPORT_TYPE = "ICalendar(.ics)";

  private String calType = "0" ;
  private Map<String,String> names_ = new HashMap<String, String>() ;

  private Map<String,String> longNames_ = new HashMap<String, String>() ;

  public String eventId = null ;
  public UIExportForm() throws Exception {
    addUIFormInput(new UIFormStringInput(NAME, NAME, null)) ;
  }

  public void setCalType(String type) {calType = type ; }

  public void update(String type, List<Calendar> calendars, String selectedCalendarId) throws Exception
  {
    calType = type ;
    names_.clear() ;
    Iterator<UIComponent> childIter = getChildren().iterator() ;
    while(childIter.hasNext()) {
      UIComponent comp = childIter.next() ;
      if (comp instanceof UICheckBoxInput ) {
        removeChildById(comp.getId()) ;
      }
    }
    initCheckBox(calendars, selectedCalendarId) ;

    // default value set to name of calendar
    ((UIFormStringInput) getChildById(NAME)).setValue(calendars.get(0).getName());
  }

  public void initCheckBox(List<Calendar> calendars, String selectedCalendarId)
  {
    for (Calendar calendar : calendars) {
      UICheckBoxInput checkBox = new UICheckBoxInput(calendar.getId(), calendar.getId(), false);
      if(calendar.getId().equals(selectedCalendarId)) checkBox.setChecked(true) ;
      else checkBox.setChecked(false) ;
      if(eventId != null) checkBox.setDisabled(true) ;
      else checkBox.setDisabled(false) ;
      addUIFormInput(checkBox) ;
      names_.put(calendar.getId(), calendar.getName()) ;
      longNames_.put(calendar.getId(), calendar.getName()) ;
    }
  }

  private String getCalendarName(String calendarId)
  {
    return longNames_.get(calendarId);
  }

  @Override
  public String getLabel(String id) throws Exception {
      WebuiRequestContext context = RequestContext.getCurrentInstance() ;
      ResourceBundle res = context.getApplicationResourceBundle() ;     
      String label = getId() + ".label." + id;
      try {
        return res.getString(label);      
      } catch (MissingResourceException e) {
        if( names_.get(id) != null) return  names_.get(id) ;
      }
      return id ;
  } 

  @Override
  public void activate() throws Exception {}
  @Override
  public void deActivate() throws Exception {}

  static  public class SaveActionListener extends EventListener<UIExportForm> {
    @Override
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource() ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      List<UIComponent> children = uiForm.getChildren() ;
      List<String> calendarIds = new ArrayList<String> () ;
      for(UIComponent child : children) {
        if(child instanceof UICheckBoxInput) {
          UICheckBoxInput input =   ((UICheckBoxInput)child) ;
          if(input.isChecked()) calendarIds.add(((UICheckBoxInput)child).getBindingField()) ;
        }
      }
      if(calendarIds.isEmpty()) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIExportForm.msg.calendar-does-not-existing", null));
        
        return ;
      }

      String name = uiForm.getUIStringInput(NAME).getValue() ;
      CalendarImportExport importExport = calendarService.getCalendarImportExports(EXPORT_TYPE) ;
      OutputStream out = null ;
      try {
        // export a single event
        if(uiForm.eventId != null) {
          out = importExport.exportEventCalendar(CalendarUtils.getCurrentUser(), calendarIds.get(0), uiForm.calType, uiForm.eventId) ;
        }
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
      } catch(Exception e) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIExportForm.msg.event-does-not-existing", null));
        return ;
      }
    }
  }

  static  public class CancelActionListener extends EventListener<UIExportForm> {
    @Override
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }  
}
