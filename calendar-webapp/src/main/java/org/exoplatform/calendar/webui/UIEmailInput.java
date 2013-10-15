package org.exoplatform.calendar.webui;

import java.io.Writer;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormStringInput;

public class UIEmailInput extends UIFormStringInput {

  private static final Log LOG = ExoLogger.getLogger(UIEmailInput.class);

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
    w.write("<div class='uiEmailInput'>");
    w.write("  <input type='hidden' name='"+ getName() +"' id='" + getId() + "' value='"+value+"'>");
    if(list != null){
      for(int i=0; i < list.length ; i++ ){ 
        w.write("  <p>");
        w.write("   <span>" + list[i] + "</span>");
        w.write("   <a class='actionIcon' href='javascript:void(0);'>"
            + "<i  class='uiIconDelete uiIconLightGray ' onclick='eXo.calendar.UICalendarPortlet.removeEmailReminder(this) ;'></i></a>");
        w.write("  </p>");  
      }
      return ;
    }
    if (!CalendarUtils.isEmpty(value)){
      w.write("  <p>");
      w.write("    <span>" + value +"</span>");
      w.write("    <a class='actionIcon' href='javascript:void(0);'><i   class='uiIconDelete uiIconLightGray ' onclick='eXo.calendar.UICalendarPortlet.removeEmailReminder(this) ;'></i></a>");
      w.write("  </p>");  
    }
    w.write("</div>");
  }
}
