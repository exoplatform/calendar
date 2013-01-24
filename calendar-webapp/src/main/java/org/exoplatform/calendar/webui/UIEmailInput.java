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
  public void decode(Object input, WebuiRequestContext context){
    if(input != null) value_ = ((String)input).trim();
  }
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().loadScriptResource("eXo.cs.UIEmailInput") ;
	
    Writer w = context.getWriter();
    String value = getValue();
    if(CalendarUtils.isEmpty(value)) value = "";
    String[] list =  null;
    if(!CalendarUtils.isEmpty(value) && (value.indexOf(",") != -1)) list = value.split(",");
    w.write("<div class='UIEmailInput'>");
    w.write("  <input type='hidden' name='"+ getName() +"' id='" + getId() + "' value='"+value+"'>");
    if(list != null){
      for(int i=0; i < list.length ; i++ ){ 
        w.write("  <div class='clearfix'>");
        w.write("    <div class='pull-left'>" + list[i] + "</div>");
        w.write("    <i class='uiIconDelete uiIconLightGray pull-right' onclick='eXo.calendar.UICalendarPortlet.removeEmailReminder(this) ;'></i>");
        w.write("  </div>");  
      }
      return ;
    }
    if (!CalendarUtils.isEmpty(value)){
      w.write("  <div class='clearfix'>");
      w.write("    <div class='pull-left'>" + value +"</div>");
      w.write("    <i class='uiIconDelete uiIconLightGray pull-right' onclick='eXo.calendar.UICalendarPortlet.removeEmailReminder(this) ;'></i>");
      w.write("  </div>");  
    }
    w.write("</div>");
  }
}
