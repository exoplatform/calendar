package org.exoplatform.calendar.webui;

import java.io.Writer;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormStringInput;

public class UIEmailInput extends UIFormStringInput {
  
  public UIEmailInput(String arg0, String arg1, String value) {
    super(arg0, arg1);
    this.value_ = value;
  }
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    if(input != null) value_ = ((String)input).trim();
  }
  public void processRender(WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.cs.UIEmailInput","/csResources/javascript/") ;
    Writer w = context.getWriter();
    String value = getValue();
    if(CalendarUtils.isEmpty(value)) value = "";
    String[] list =  null;
    if(!CalendarUtils.isEmpty(value) && (value.indexOf(",") != -1)) list = value.split(",");
    w.write("<div class='UIEmailInput'>");
    w.write("  <input type='hidden' name='"+ getName() +"' id='" + getId() + "' value='"+value+"'>");
    if(list != null){
      for(int i=0; i < list.length ; i++ ){ 
        w.write("  <div class='UIEmailAddressItem'>");
        w.write("    <div class='UIEmailAddressLabel'>" + list[i] + "</div><div class='UIRemoveEmailIcon' onclick='eXo.calendar.UICalendarPortlet.removeEmailReminder(this) ;'><span></span></div>");
        w.write("    <div style='clear:both;'><span></span></div>");
        w.write("  </div>");  
      }
      return ;
    }
    if (!CalendarUtils.isEmpty(value)){
      w.write("  <div class='UIEmailAddressItem'>");
      w.write("    <div class='UIEmailAddressLabel'>" + value + "</div><div class='UIRemoveEmailIcon' onclick='eXo.calendar.UICalendarPortlet.removeEmailReminder(this) ;'><span></span></div>");
      w.write("    <div style='clear:both;'><span></span></div>");
      w.write("  </div>");
    }
    w.write("</div>");
  }
}
