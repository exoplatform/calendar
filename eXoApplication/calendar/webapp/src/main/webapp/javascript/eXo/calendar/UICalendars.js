function UICalendars() {
  this.POPUP_CONTAINER_ID = "tmpMenuElement";
  this.calsFormElem = null;
  this.currentMenuElm = null;
  this.currentAnchorElm = null;
}

UICalendars.prototype.init = function(calendarsForm) {
  if (typeof(calendarsForm) == "string") 
    calendarsForm = eXo.calendar.UICalendarPortlet.getElementById(calendarsForm);
  var DOMUtil = eXo.core.DOMUtil;
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  UICalendarPortlet.filterForm = calendarsForm;
  this.calsFormElem = calendarsForm;
  var CalendarGroup = DOMUtil.findDescendantsByClass(calendarsForm, "input", "CalendarGroup");
  var CalendarItem = DOMUtil.findDescendantsByClass(calendarsForm, "li", "CalendarItem");
  var len = CalendarGroup.length;
  var clen = CalendarItem.length;
  for (var i = 0; i < len; i++) {
      CalendarGroup[i].onclick = UICalendarPortlet.filterByGroup;
  }
  for (var j = 0; j < clen; j++) {
    var checkBox = DOMUtil.findFirstDescendantByClass(CalendarItem[j], "div", "CalendarCheckboxBlock");
    checkBox.onclick = UICalendarPortlet.filterByCalendar;
}
};

UICalendars.prototype.resetSettingButton = function(settingButton) {
  if (settingButton) eXo.core.DOMUtil.removeClass(settingButton, "IconSetting");
};

UICalendars.prototype.showSettingButtonStably = function(settingButton) {
  if (settingButton) eXo.core.DOMUtil.addClass(settingButton, "IconSetting");
};

UICalendars.prototype.renderMenu = function(menuElm, anchorElm) {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  UICalendarPortlet.swapMenu(menuElm, anchorElm);
  this.currentMenuElm = UICalendarPortlet.menuElement;
  if (!eXo.core.I18n.isRT()) {
    this.currentMenuElm.style.left = (eXo.core.Browser.findPosX(this.currentMenuElm) - this.currentMenuElm.offsetWidth  + anchorElm.offsetWidth) + 'px';
  }
};

UICalendars.prototype.mainMenuCallback = function(anchorElm, evt) {
  var d = new Date();
  var currentTime = d.getTime();
  var timezoneOffset = d.getTimezoneOffset();
  var menu = eXo.calendar.UICalendars.currentMenuElm;
  var actions = eXo.core.DOMUtil.findDescendantsByTagName(menu, "div");
  actions[0].onclick = String(actions[0].onclick).replace(/&.*/, "&ct=" + currentTime + "&tz=" + timezoneOffset + "')");
};

UICalendars.prototype.calendarMenuCallback = function(anchorElm, evt) {
  var DOMUtil = eXo.core.DOMUtil ;
  var obj = eXo.core.EventManager.getEventTargetByClass(evt,"CalendarItem") || eXo.core.EventManager.getEventTargetByClass(evt,"GroupItem");
  var calType = obj.getAttribute("calType");
  var calName = obj.getAttribute("calName");
  var calColor = obj.getAttribute("calColor");
  var canEdit = String(obj.getAttribute("canedit")).toLowerCase();
  var UICalendars = eXo.calendar.UICalendars;
  var menu = UICalendars.currentMenuElm;
  var contentContainerElm = DOMUtil.findAncestorByClass(anchorElm, "ContentContainer");
  if (contentContainerElm) {
    menu.style.top = (eXo.core.Browser.findPosY(menu) - contentContainerElm.scrollTop) + 'px';
  }
  try {
    var selectedCategory = (eXo.calendar.UICalendarPortlet.filterSelect) ? eXo.calendar.UICalendarPortlet.filterSelect : null;
    if (selectedCategory) {
    	selectedCategory = selectedCategory.options[selectedCategory.selectedIndex].value;
    } 
  } catch (e) { //Fix for IE
    var selectedCategory = null;
  }
  if(!menu || !obj.id) {
    if (menu) menu.style.display = 'none';
    eXo.webui.UIContextMenu.menuElement = null ;
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
  var items = DOMUtil.findDescendantsByTagName(menu, "a");  
  for (var i = 0; i < items.length; i++) {
      if (DOMUtil.hasClass(items[i].firstChild, "SelectedColorCell")) {
          items[i].firstChild.className = items[i].firstChild.className.toString().replace(/SelectedColorCell/, "");
      }
      if (DOMUtil.hasClass(items[i], calColor)) {
          var selectedCell = items[i].firstChild;
          DOMUtil.addClass(selectedCell, "SelectedColorCell");
      }
      if (items[i].href.indexOf("ChangeColor") != -1) {
          value = value.replace(/calColor\s*=\s*\w*/, "calColor=" + items[i].className.split(" ")[0]);
      }
      items[i].href = String(items[i].href).replace(/objectId\s*=.*(?='|")/, value);
  }
  
  if (DOMUtil.hasClass(obj, "CalendarItem")) {
      items[0].href = String(items[0].href).replace("')", "&categoryId=" + selectedCategory + "')");
      items[1].href = String(items[1].href).replace("')", "&categoryId=" + selectedCategory + "')");      
  }
  if (calType && (calType != "0")) {
  
      var actions = DOMUtil.findDescendantsByTagName(menu, "a");
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
      var actions = DOMUtil.findDescendantsByTagName(menu, "a");
      for (var j = 0; j < actions.length; j++) {
          if (actions[j].href.indexOf("EditCalendar") >= 0 || actions[j].href.indexOf("RemoveCalendar") >= 0) {
              actions[j].style.display = "block";
          }
      }
  }
  UICalendars.resetSettingButton(UICalendars.currentAnchorElm);
  UICalendars.currentAnchorElm = anchorElm;
  if (DOMUtil.hasClass(UICalendars.currentAnchorElm, "IconHoverSetting")) {
    UICalendars.showSettingButtonStably(UICalendars.currentAnchorElm);
    if (!UICalendars.modifiedOnclick) {
      UICalendars.defaultOnclickFunc = document.onclick;
      UICalendars.modifiedOnclick = true;
      document.onclick = function (evt) {
        var UICalendars = eXo.calendar.UICalendars;
        UICalendars.resetSettingButton(UICalendars.currentAnchorElm);
        UICalendars.defaultOnclickFunc(evt);
        document.onclick = UICalendars.defaultOnclickFunc;
        UICalendars.modifiedOnclick = false;
      };
    }
  }
};

UICalendars.prototype.showMenu = function(anchorElm, evt, menuClassName, menuCallback) {
  var _e = window.event || evt;
  _e.cancelBubble = true;
  var DOMUtil = eXo.core.DOMUtil;
  var menuTemplateElm = DOMUtil.findFirstDescendantByClass(this.calsFormElem, "div", menuClassName);
  this.renderMenu(menuTemplateElm, anchorElm);
  // invoke callback
  if (menuCallback) menuCallback(anchorElm, evt);
};

eXo.calendar.UICalendars = new UICalendars();