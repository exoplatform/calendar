function UICalendars() {
  this.POPUP_CONTAINER_ID = "tmpMenuElement";
  this.calsFormElem = null;
  this.currentMenuElm = null;
  this.currentAnchorElm = null;
}

UICalendars.prototype.init = function(calendarsForm) {
  if (typeof(calendarsForm) == "string") 
    calendarsForm = _module.UICalendarPortlet.getElementById(calendarsForm);
  var UICalendarPortlet = _module.UICalendarPortlet;
  UICalendarPortlet.filterForm = calendarsForm;
  this.calsFormElem = calendarsForm;
  var CalendarGroup = gj(calendarsForm).find('input.CalendarGroup');
  var CalendarItem = gj(calendarsForm).find('li.CalendarItem'); 
  var len = CalendarGroup.length;
  var clen = CalendarItem.length;
  for (var i = 0; i < len; i++) {
      CalendarGroup[i].onclick = UICalendarPortlet.filterByGroup;
  }
  for (var j = 0; j < clen; j++) {
    var checkBox = gj(CalendarItem[j]).find('div.CalendarCheckboxBlock')[0];
    checkBox.onclick = UICalendarPortlet.filterByCalendar;
}
};

UICalendars.prototype.resetSettingButton = function(settingButton) {
  if (settingButton) 
	  gj(settingButton).removeClass("IconSetting");
};

UICalendars.prototype.showSettingButtonStably = function(settingButton) {
  if (settingButton) 
	  gj(settingButton).addClass("IconSetting");
};

UICalendars.prototype.renderMenu = function(menuElm, anchorElm) {
  var UICalendarPortlet = _module.UICalendarPortlet;
  UICalendarPortlet.swapMenu(menuElm, anchorElm);
  this.currentMenuElm = UICalendarPortlet.menuElement;
  if (!base.I18n.isRT()) {
    this.currentMenuElm.style.left = (base.Browser.findPosX(this.currentMenuElm) - this.currentMenuElm.offsetWidth  + anchorElm.offsetWidth) + 'px';
  }
};

UICalendars.prototype.mainMenuCallback = function(anchorElm, evt) {
  var d = new Date();
  var currentTime = d.getTime();
  var timezoneOffset = d.getTimezoneOffset();
  var menu = _module.UICalendars.currentMenuElm;
  var actions = gj(menu).find('div');
  actions[0].onclick = String(actions[0].onclick).replace(/&.*/, "&ct=" + currentTime + "&tz=" + timezoneOffset + "')");
};

UICalendars.prototype.calendarMenuCallback = function(anchorElm, evt) {
  var obj = cs.EventManager.getEventTargetByClass(evt,"CalendarItem") || cs.EventManager.getEventTargetByClass(evt,"GroupItem");
  var calType = obj.getAttribute("calType");
  var calName = obj.getAttribute("calName");
  var calColor = obj.getAttribute("calColor");
  var canEdit = String(obj.getAttribute("canedit")).toLowerCase();
  var UICalendars = _module.UICalendars;
  var menu = UICalendars.currentMenuElm;
  var contentContainerElm = gj(anchorElm).parents(".ContentContainer")[0];
  if (contentContainerElm) {
    menu.style.top = (cs.Browser.findPosY(menu) - contentContainerElm.scrollTop) + 'px';
  }
  try {
    var selectedCategory = (_module.UICalendarPortlet.filterSelect) ? _module.UICalendarPortlet.filterSelect : null;
    if (selectedCategory) {
    	selectedCategory = selectedCategory.options[selectedCategory.selectedIndex].value;
    } 
  } catch (e) { //Fix for IE
    var selectedCategory = null;
  }
  if(!menu || !obj.id) {
    if (menu) menu.style.display = 'none';
    cs.UIContextMenu.menuElement = null ;
    return ;
  } 
  var value = "" ;
  value = "objectId=" + obj.id;
  if (calType) {
      value += "&calType=" + calType;
  }
  if (calName) {
      value += "&calName=" + calName;
  }
  if (calColor) {
      value += "&calColor=" + calColor;
  }
  var items = gj(menu).find("a");  
  for (var i = 0; i < items.length; i++) {
      if (gj(items[i].firstChild).hasClass("SelectedColorCell")) {
          items[i].firstChild.className = items[i].firstChild.className.toString().replace(/SelectedColorCell/, "");
      }
      if (gj(items[i]).hasClass(calColor)) {
          var selectedCell = items[i].firstChild;
          gj(selectedCell).addClass("SelectedColorCell");
      }
      if (items[i].href.indexOf("ChangeColor") != -1) {
          value = value.replace(/calColor\s*=\s*\w*/, "calColor=" + items[i].className.split(" ")[0]);
      }
      items[i].href = String(items[i].href).replace(/objectId\s*=.*(?='|")/, value);
  }
  
  if (gj(obj).hasClass("CalendarItem")) {
      items[0].href = String(items[0].href).replace("')", "&categoryId=" + selectedCategory + "')");
      items[1].href = String(items[1].href).replace("')", "&categoryId=" + selectedCategory + "')");      
  }
  if (calType && (calType != "0")) {
  
      var actions = gj(menu).find("a");
      for (var j = 0; j < actions.length; j++) {
          if ((actions[j].href.indexOf("EditCalendar") >= 0) ||
          (actions[j].href.indexOf("RemoveCalendar") >= 0) ||
          (actions[j].href.indexOf("ShareCalendar") >= 0) ||
          (actions[j].href.indexOf("ChangeColorCalendar") >= 0)) {
              actions[j].style.display = "none";
          }
      }
  }
  if (canEdit && (canEdit == "true")) {
      var actions = gj(menu).find("a");
      for (var j = 0; j < actions.length; j++) {
          if (actions[j].href.indexOf("EditCalendar") >= 0 || actions[j].href.indexOf("RemoveCalendar") >= 0) {
              actions[j].style.display = "block";
          }
      }
  }
  UICalendars.resetSettingButton(UICalendars.currentAnchorElm);
  UICalendars.currentAnchorElm = anchorElm;
  if (gj(UICalendars.currentAnchorElm).hasClass("IconHoverSetting")) {
    UICalendars.showSettingButtonStably(UICalendars.currentAnchorElm);
    if (!UICalendars.modifiedOnclick) {
      UICalendars.defaultOnclickFunc = document.onclick;
      UICalendars.modifiedOnclick = true;
      document.onclick = function (evt) {
        var UICalendars = _module.UICalendars;
        UICalendars.resetSettingButton(UICalendars.currentAnchorElm);
//        UICalendars.defaultOnclickFunc(evt);
        UICalendars.defaultOnclickFunc = evt;
        document.onclick = UICalendars.defaultOnclickFunc;
        UICalendars.modifiedOnclick = false;
      };
    }
  }
};

UICalendars.prototype.showMenu = function(anchorElm, evt, menuClassName, menuCallback) {
  var _e = window.event || evt;
  _e.cancelBubble = true;
  var menuTemplateElm = gj(this.calsFormElem).find('div.' + menuClassName)[0]; 
  this.renderMenu(menuTemplateElm, anchorElm);
  // invoke callback
  if (menuCallback) menuCallback(anchorElm, evt);
};

eXo.calendar.UICalendars = new UICalendars();
_module.UICalendars = new UICalendars();