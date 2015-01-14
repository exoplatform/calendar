/**
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.calendar.webui;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.exoplatform.calendar.webui.UIFormColorPicker.Colors.Color;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Feb 29, 2008
 */
public class UIFormColorPicker extends UIFormInputBase<String>
{

  /**
   * The size of the list (number of select options)
   */
  private int items_ = 10;

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

  private Map<String, String> jsActions_ = new HashMap<String, String>();

  private Color[] colors_ = null;

  public UIFormColorPicker(String name, String bindingExpression, String value)
  {
    super(name, bindingExpression, String.class);
    this.value_ = value;
    setColors(Colors.COLORS);
  }

  public UIFormColorPicker(String name, String bindingExpression, Color[] colors)
  {
    super(name, bindingExpression, null);
    setColors(colors);
  }

  public void setJsActions(Map<String, String> jsActions)
  {
    if (jsActions != null)
      jsActions_ = jsActions;
  }

  public Map<String, String> getJsActions()
  {
    return jsActions_;
  }

  public void addJsActions(String action, String javaScript)
  {
    jsActions_.put(action, javaScript);
  }

  public UIFormColorPicker(String name, String bindingExpression, Color[] colors, Map<String, String> jsActions)
  {
    super(name, bindingExpression, null);
    setColors(colors);
    setJsActions(jsActions);
  }

  public UIFormColorPicker(String name, String value)
  {
    this(name, null, value);
  }

  @SuppressWarnings("unused")
  public void decode(Object input, WebuiRequestContext context)
  {
    value_ = (String)input;
    if (value_ != null && value_.trim().length() == 0)
      value_ = null;
  }

  public void setOnChange(String onchange)
  {
    onchange_ = onchange;
  }

  protected String renderOnChangeEvent(UIForm uiForm) throws Exception
  {
    return uiForm.event(onchange_, (String)null);
  }

  protected UIForm getUIform()
  {
    return getAncestorOfType(UIForm.class);
  }

  private String renderJsActions()
  {
    StringBuffer sb = new StringBuffer("");
    for (String k : jsActions_.keySet())
    {
      if (sb != null && sb.length() > 0)
        sb.append(" ");
      if (jsActions_.get(k) != null)
      {
        sb.append(k).append("=\"").append(jsActions_.get(k)).append("\"");
      }
    }
    return sb.toString();
  }

  private Color[] getColors()
  {
    return colors_;
  }

  private void setColors(Color[] colors)
  {
    colors_ = colors;
    value_ = colors_[0].getName();
  }

  private int items()
  {
    return items_;
  }

  private int size()
  {
    return colors_.length;
  }

  public void setNumberItemsPerLine(int numberItems)
  {
    items_ = numberItems;
  }

  public void processRender(WebuiRequestContext context) throws Exception
  {
    JavascriptManager jsManager = context.getJavascriptManager();
    RequireJS requireJS = jsManager.getRequireJS();
    requireJS.require("SHARED/jquery","gj");
    requireJS.require("SHARED/bts_dropdown","btsdropdown");
    requireJS.require("SHARED/UIColorPicker","UIColorPicker");

    requireJS.addScripts("gj(document).ready(function() { gj('div.uiColorPickerInput').click(function(){ UIColorPicker.adaptPopup(this); }); });");
    requireJS.addScripts("gj('a.colorCell').click(function(){ UIColorPicker.setColor(this); });");

    String value = getValue();
    if (value != null)
    {
      value = HTMLEntityEncoder.getInstance().encode(value);
    }
    Writer w = context.getWriter();
    w.write("<div class='uiFormColorPicker dropdown'>");
    w.write("<div class=\"uiColorPickerInput dropdown-toggle\" data-toggle=\"dropdown\">");
    w.write("<span class=\" displayValue " + value + "\"><span><b class=\"caret\"></b></span></span>");
    w.write("</div>");
    w.write("<ul class='calendarTableColor dropdown-menu' role=\"menu\" selectedColor=\"" + value + " \">");

    int i = 0 ;
    int items = 6 ;
    int size = getColors().length ;
    int rows = size/items ;
    int count = 0 ;
    while(i < rows)  {
      w.write("<li class=\"clearfix\">") ;  
      int j = 0 ;
      while(j < items && count < size){
        Color color = getColors()[count] ;
        w.write("<a href=\"javascript:void(0);");
        w.write("\" class=\"");
        w.write(color.getName());
        w.write(" colorCell \" onmousedown=\"event.cancelBubble=true\"><i class=\"");
        if(color.getName().equals(value)){w.write("iconCheckBox");}
        w.write("\"></i></a>");
        count++;
        j++;
      }
      w.write("</li>");  
      i++ ;
    }
    w.write("</ul>");
    w.write("<input class='uiColorPickerValue' name='" + getId() + "' type='hidden'" + " id='" + getId() + "' "
        + renderJsActions());
    if (value != null && value.trim().length() > 0)
    {
      w.write(" value='" + value + "'");
    }
    w.write(" />");
    w.write("</div>");
  }

  @Override
  public UIFormInput setValue(String arg0)
  {
    if (arg0 == null)
      arg0 = colors_[0].getName();
    return super.setValue(arg0);
  }


  public static class Colors
  {
    /* 1st line */
    public static final String H_ASPARAGUS = "#909958";

    public static final String N_ASPARAGUS = "asparagus";

    public static final Color O_ASPARAGUS = new Color(H_ASPARAGUS, N_ASPARAGUS);

    public static final String H_MUNSELL_BLUE = "#319AB3";

    public static final String N_MUNSELL_BLUE =  "munsell_blue";

    public static final Color O_MUNSELL_BLUE = new Color(H_MUNSELL_BLUE, N_MUNSELL_BLUE);

    public static final String H_NAVY_BLUE = "#4273C8";

    public static final String N_NAVY_BLUE = "navy_blue";

    public static final Color O_NAVY_BLUE = new Color(H_NAVY_BLUE, N_NAVY_BLUE);

    public static final String H_PURPLE = "#774EA9";

    public static final String N_PURPLE = "purple";

    public static final Color O_PURPLE = new Color(H_PURPLE, N_PURPLE);

    public static final String H_RED = "#FF5933";

    public static final String N_RED = "red";

    public static final Color O_RED = new Color(H_RED, N_RED);

    public static final String H_BROWN = "#BB8E62";

    public static final String N_BROWN = "brown";

    public static final Color O_BROWN = new Color(H_BROWN, N_BROWN);

    /* 2nd line */
    public static final String H_LAUREL_GREEN = "#BED67E";

    public static final String N_LAUREL_GREEN = "laurel_green";

    public static final Color O_LAUREL_GREEN = new Color(H_LAUREL_GREEN, N_LAUREL_GREEN);

    public static final String H_SKY_BLUE = "#4DBED9";

    public static final String N_SKY_BLUE = "sky_blue";

    public static final Color O_SKY_BLUE = new Color(H_SKY_BLUE, N_SKY_BLUE);

    public static final String H_BLUE_GRAY = "#8EB0EA";

    public static final String N_BLUE_GRAY = "blue_gray";

    public static final Color O_BLUE_GRAY = new Color(H_BLUE_GRAY, N_BLUE_GRAY);

    public static final String H_LIGHT_PURPLE = "#BC99E7";

    public static final String N_LIGHT_PURPLE = "light_purple";

    public static final Color O_LIGHT_PURPLE = new Color(H_LIGHT_PURPLE, N_LIGHT_PURPLE);

    public static final String H_HOT_PINK = "#F97575";

    public static final String N_HOT_PINK = "hot_pink";

    public static final Color O_HOT_PINK = new Color(H_HOT_PINK, N_HOT_PINK);

    public static final String H_LIGHT_BROWN = "#C5B294";

    public static final String N_LIGHT_BROWN = "light_brown";

    public static final Color O_LIGHT_BROWN = new Color(H_LIGHT_BROWN, N_LIGHT_BROWN);

    /* 3rd line */
    public static final String H_MOSS_GREEN = "#98CC81";

    public static final String N_MOSS_GREEN = "moss_green";

    public static final Color O_MOSS_GREEN = new Color(H_MOSS_GREEN, N_MOSS_GREEN);

    public static final String H_POWDER_BLUE = "#9EE4F5";

    public static final String N_POWDER_BLUE = "powder_blue";

    public static final Color O_POWDER_BLUE = new Color(H_POWDER_BLUE, N_POWDER_BLUE);

    public static final String H_LIGHT_BLUE = "#B3CFFF";

    public static final String N_LIGHT_BLUE = "light_blue";

    public static final Color O_LIGHT_BLUE = new Color(H_LIGHT_BLUE, N_LIGHT_BLUE);

    public static final String H_PINK = "#FFC8F0";

    public static final String N_PINK = "pink";

    public static final Color O_PINK = new Color(H_PINK, N_PINK);

    public static final String H_ORANGE = "#FDB519";

    public static final String N_ORANGE = "orange";

    public static final Color O_ORANGE = new Color(H_ORANGE, N_ORANGE);

    public static final String H_GRAY = "#A39594";

    public static final String N_GRAY = "gray";

    public static final Color O_GRAY = new Color(H_GRAY, N_GRAY);

    /* 4th line */
    public static final String H_GREEN = "#89D4B3";

    public static final String N_GREEN = "green";

    public static final Color O_GREEN = new Color(H_GREEN, N_GREEN);

    public static final String H_BABY_BLUE = "#B2E2FF";

    public static final String N_BABY_BLUE = "baby_blue";

    public static final Color O_BABY_BLUE = new Color(H_BABY_BLUE, N_BABY_BLUE);

    public static final String H_LIGHT_GRAY = "#CDCDCD";

    public static final String N_LIGHT_GRAY = "light_gray";

    public static final Color O_LIGHT_GRAY = new Color(H_LIGHT_GRAY, N_LIGHT_GRAY);

    public static final String H_BEIGE = "#FFE1BE";

    public static final String N_BEIGE = "beige";

    public static final Color O_BEIGE = new Color(H_BEIGE, N_BEIGE);

    public static final String H_YELLOW = "#FFE347";

    public static final String N_YELLOW = "yellow";

    public static final Color O_YELLOW = new Color(H_YELLOW, N_YELLOW);

    public static final String H_PLUM_PURPLE = "#CEA6AC";

    public static final String N_PLUM_PURPLE = "plum";

    public static final Color O_PLUM_PURPLE = new Color(H_PLUM_PURPLE, N_PLUM_PURPLE);

    public static final Color[] COLORS =
      {O_ASPARAGUS, O_MUNSELL_BLUE, O_NAVY_BLUE, O_PURPLE, O_RED, O_BROWN,
      O_LAUREL_GREEN, O_SKY_BLUE, O_BLUE_GRAY, O_LIGHT_PURPLE, O_HOT_PINK, O_LIGHT_BROWN,
      O_MOSS_GREEN, O_POWDER_BLUE, O_LIGHT_BLUE, O_PINK, O_ORANGE, O_GRAY,
      O_GREEN, O_BABY_BLUE, O_LIGHT_GRAY, O_BEIGE, O_YELLOW, O_PLUM_PURPLE};

    public static final String[] COLORNAMES =
      {N_ASPARAGUS, N_MUNSELL_BLUE, N_NAVY_BLUE, N_PURPLE, N_RED, N_BROWN,
      N_LAUREL_GREEN, N_SKY_BLUE, N_BLUE_GRAY, N_LIGHT_PURPLE, N_HOT_PINK, N_LIGHT_BROWN,
      N_MOSS_GREEN, N_POWDER_BLUE, N_LIGHT_BLUE, N_PINK, N_ORANGE, N_GRAY,
      N_GREEN, N_BABY_BLUE, N_LIGHT_GRAY, N_BEIGE, N_YELLOW, N_PLUM_PURPLE};

    public static final String[] CODES =
      {H_ASPARAGUS, H_MUNSELL_BLUE, H_NAVY_BLUE, H_PURPLE, H_RED, H_BROWN,
      H_LAUREL_GREEN, H_SKY_BLUE, H_BLUE_GRAY, H_LIGHT_PURPLE, H_HOT_PINK, H_LIGHT_BROWN,
      H_MOSS_GREEN, H_POWDER_BLUE, H_LIGHT_BLUE, H_PINK, H_ORANGE, H_GRAY,
      H_GREEN, H_BABY_BLUE, H_LIGHT_GRAY, H_BEIGE, H_YELLOW, H_PLUM_PURPLE};

    static public class Color
    {

      int R = 0;

      int G = 0;

      int B = 0;

      String code_;

      String name_;

      public Color(String code)
      {
        setCode(code);
      }

      public Color(int r, int g, int b)
      {
        R = r;
        G = g;
        B = b;
      }

      public Color(String code, String name)
      {
        setCode(code);
        setName(name);
      }

      public String getCode()
      {
        return code_;
      }

      public void setName(String name)
      {
        name_ = name;
      }

      public String getName()
      {
        return name_;
      }

      public void setCode(String code)
      {
        code_ = code;
      }
    }

  }

}
