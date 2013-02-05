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
package org.exoplatform.calendar.webui;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Dec 3, 2007  
 */
public class UIFormComboBox extends UIFormInputBase<String>  {
   
  /**
   * The size of the list (number of select options)
   */
  protected int size_ = 1 ;

  /**
   * The list of options
   */
  private List<SelectItemOption<String>> options_ ;

  /**
   * The javascript expression executed when an onChange event fires
   */
  private String onchange_;
  
  /**
   * The javascript expression executed when an client onChange event fires
   */
  public static final String ON_CHANGE = "onchange".intern();
  
  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_BLUR = "onblur".intern();
  
  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_FOCUS = "onfocus".intern();
  
  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_KEYUP = "onkeyup".intern();
  
  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_KEYDOWN = "onkeydown".intern();
  
  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_CLICK = "onclick".intern();
  
  private Map<String, String> jsActions_ = new HashMap<String, String>() ;
  
  public UIFormComboBox(String name, String bindingExpression, String value) {
    super(name, bindingExpression, String.class);
    this.value_ = value ;
  }

  public UIFormComboBox(String name, String bindingExpression, List<SelectItemOption<String>> options) {
    super(name, bindingExpression, null);
    setOptions(options);
  }

  public void setJsActions(Map<String, String> jsActions) {
    if(jsActions != null) jsActions_ = jsActions;
  }

  public Map<String, String> getJsActions() {
    return jsActions_;
  }
  public void addJsActions(String action, String javaScript) {
    jsActions_.put(action, javaScript) ;
  }
  public UIFormComboBox(String name, String bindingExpression, List<SelectItemOption<String>> options, Map<String, String> jsActions) {
    super(name, bindingExpression, null);
    setOptions(options);
    setJsActions(jsActions) ;
  }
  
  public UIFormComboBox(String name, String value) {
    this(name, null, value);
  }
  final public UIFormComboBox setOptions(List<SelectItemOption<String>> options) { 
    options_ = options ; 
    if(options_ == null || options_.size() < 1) return this;
    value_ = options_.get(0).getValue();
    return this ;
  }
  public void decode(Object input, WebuiRequestContext context){
    value_ = (String) input;
    if(value_ != null && value_.length() == 0) value_ = null ;
  }
  public void setOnChange(String onchange){ onchange_ = onchange; } 
  
  protected String renderOnChangeEvent(UIForm uiForm) throws Exception {
    return uiForm.event(onchange_, (String)null);
  }
  public UIForm getUIform() {
    return getAncestorOfType(UIForm.class) ; 
  }
  
  private String renderJsActions() {
    StringBuffer sb = new StringBuffer() ;
    for(String k : jsActions_.keySet()){
      if(sb != null && sb.length() > 0 ) sb.append(" ") ;
      if(jsActions_.get(k) != null) {
        sb.append(k).append("=\"").append(jsActions_.get(k)).append("\"") ;
      }  
    }
    return sb.toString() ;
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    String parentId = ((UIComponent) this.getParent()).getId();
    
    RequireJS requirejs = context.getJavascriptManager().getRequireJS();
    requirejs.require("SHARED/webui-ext", "wx");    
    requirejs.addScripts("wx.UICombobox.init('" + parentId + "','" + getId() + "');");       
    
    Writer w = context.getWriter();
    String options = "[";
    StringBuffer text = new StringBuffer(
        "<div class='UIComboboxComponent'><div class='UIComboboxList'><div class='UIComboboxContainer'><div class='UIComboboxItemContainer'>");
    for (SelectItemOption item : options_) {      
      options += "'" + item.getValue() + "',";
      //text.append("<div onclick='eXo.calendar.UICombobox.getValue(this);' value='").append(item.getValue()).append("' class='UIComboboxItem'>");
      text.append("<div value='").append(item.getValue()).append("' class='UIComboboxItem'>");
      text.append("<div class='UIComboboxIcon'>");
      text.append("<div class='UIComboboxLabel'>").append(item.getLabel()).append("</div>");
      text.append("</div>");
      text.append("</div>");            
    }    
    
    text.append("</div></div></div>");
    options = options.substring(0, options.length() - 1) + "]";
    
    text.append("<input class='UIComboboxInput' options=\"").append(options)
        //.append("\" onkeyup='eXo.calendar.UICombobox.complete(this,event);' name='").append(getName())
    	.append("\" name='").append(getName())
        .append("' type='text'").append(" id='").append(getId()).append("' ").append(renderJsActions());
        
    if (value_ != null && value_.trim().length() > 0) {
      text.append(" value='").append(encodeValue(value_).toString()).append("'");
    }
    text.append(" /></div>");
    
    //Set action events
    requirejs.require("SHARED/jquery", "gj");
    //Set onclick function for all combobox items      
    requirejs.addScripts("gj('div.UIComboboxItem').click(function(){wx.UICombobox.getValue(this);});");
    //set onkeyup function for this comboBox
    requirejs.addScripts("gj('input#"+getId()+"').keyup(function(){wx.UICombobox.complete(this,event);});");
    
    w.write(text.toString());
  }

  private StringBuilder encodeValue(String value){
    char [] chars = {'\'', '"'};
    String [] refs = {"&#39;", "&#34;"};
    StringBuilder builder = new StringBuilder(value);
    int idx ;
    for(int i = 0; i < chars.length; i++){
      idx = indexOf(builder, chars[i], 0);
      while(idx > -1){
        builder = builder.replace(idx, idx+1, refs[i]);
        idx = indexOf(builder, chars[i], idx);
      }
    }    
    return builder;
  }

  private int indexOf(StringBuilder builder, char c, int from){
    int i = from;
    while(i < builder.length()){
      if(builder.charAt(i) == c) return i;
      i++;
    }
    return -1;
  }
}
