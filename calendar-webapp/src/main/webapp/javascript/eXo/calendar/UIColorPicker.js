/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

eXo.calendar.UIColorPicker = {

  show : function(obj) {
    document.onmousedown = _module.UIColorPicker.hide;
    var jObj = gj(obj);
    this.tableColor = jObj.next("div")[0];
    this.title = jObj.find(".displayValue").first()[0];
    this.input = jObj.parent().find(".uiColorPickerValue").first()[0];
    this.showHide();
    this.getSelectedValue();
  },
  
  setColor : function(color) {
    this.title = gj('span.displayValue')[0];
    this.input = gj('input.uiColorPickerValue')[0];
    if (gj(this.title).hasClass(color)) {
      this.hide();
      return;
    }
    var className = "displayValue " + color;
    this.title.className = className;
    this.input.value = color;
    this.hide();
  },

  clearSelectedValue : function() {
    var checkbox = gj(this.tableColor).find("i");
    checkbox.each(function(){
    	var jObj = gj(this);
    	if (jObj.hasClass("iconCheckBox")) {
    		jObj.removeClass("iconCheckBox");    		
    		return false;
    	}
    });
  },

  getSelectedValue : function() {
    var selectedValue = this.input.value;
    
    this.clearSelectedValue();
    var colorCell = gj(this.tableColor).find("a");
    colorCell.each(function() {
    	var jObj = gj(this);
    	if (jObj.hasClass(selectedValue)) {
    		cPbj = gj(jObj).find("i");
    		cPbj.addClass("iconCheckBox");
    		return false;
    	}
    });
  },

  hide : function() {
    if (_module.UIColorPicker.tableColor) {
      _module.UIColorPicker.tableColor.style.display = "none";
      _module.UIColorPicker.tableColor = null;
      _module.UIColorPicker.title = null;
      _module.UIColorPicker.input = null;
      document.onmousedown = null;
    }
  },

  showHide : function() {
    var obj = this.tableColor;
    if (obj.style.display != "block") {
      obj.style.display = "block";
    } else {
      obj.style.display = "none";
    }
  },
  //change css properties of the parent popup so that the Color picker popup overlaps the parent popup
  // see design BD-1105
  adaptPopup : function(obj) {
    var popupContent = gj(obj).parents('.popupContent');
    var tabContent = gj(obj).parents('.tab-content');
    popupContent.css('overflow','visible');
    tabContent.css('overflow','visible');
  }
};

_module.UIColorPicker = eXo.calendar.UIColorPicker;