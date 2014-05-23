(function(CSUtils, gj, base){
function UICalendars() {
    this.POPUP_CONTAINER_ID = "tmpMenuElement";
    this.calsFormElem     = null;
    this.currentMenuElm   = null;
    this.currentAnchorElm = null;
    this.originalHeight   = null;
}

var _module = {} ;
eXo.calendar = eXo.calendar || {}

UICalendars.prototype.init = function(calendarsForm) {
    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;

    if (typeof(calendarsForm) == "string") {
      calendarsForm = _module.UICalendarPortlet.getElementById(calendarsForm);
    }

    var UICalendarPortlet = _module.UICalendarPortlet,
        contentContainer  = gj(calendarsForm).find(".contentContainer")[0];

    if ((!this.originalHeight)
    || (this.originalHeight && (this.originalHeight !== gj(contentContainer).height()))) {
        this.originalHeight = gj(contentContainer).height();
    }

    _module.UICalendarPortlet.filterForm = calendarsForm;
    this.calsFormElem = calendarsForm;
    var CalendarGroup = gj(calendarsForm).find('input.CalendarGroup');
    var CalendarItem = gj(calendarsForm).find('li.calendarItem');
    var len = CalendarGroup.length;
    var clen = CalendarItem.length;

    for (var i = 0; i < len; i++) {
        CalendarGroup[i].onclick = UICalendarPortlet.filterByGroup;
    }
    for (var j = 0; j < clen; j++) {
        var checkBox = gj(CalendarItem[j]).find('div.calendarCheckboxBlock')[0];
        checkBox.onclick = UICalendarPortlet.filterByCalendar;
    }

    /*=== resize height ===*/
    UICalendarPortlet.resizeHeight(contentContainer, 20, this.originalHeight);

    /* resize content each time the window is resized */
    var originalHeight = this.originalHeight;
    gj(window).resize(function() {
        UICalendarPortlet.resizeHeight(contentContainer, 20, originalHeight);
    });
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
_module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
  var UICalendarPortlet = _module.UICalendarPortlet;
  UICalendarPortlet.swapMenu(menuElm, anchorElm);
  this.currentMenuElm = UICalendarPortlet.menuElement;
  if (!base.I18n.isRT()) {
    this.currentMenuElm.style.left = (base.Browser.findPosX(anchorElm) + anchorElm.offsetWidth + 10) + 'px'; // 10px for the arrow
  }
};

UICalendars.prototype.calendarMenuCallback = function(anchorElm, evt) {
  _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
  var obj = CSUtils.EventManager.getEventTargetByClass(evt,"calendarItem") || CSUtils.EventManager.getEventTargetByClass(evt,"GroupItem");
  var calType = obj.getAttribute("calType");
  var calName = obj.getAttribute("calName");
  var calColor = obj.getAttribute("calColor");
  var canEdit  = obj.getAttribute("canEdit");
  var UICalendars = _module.UICalendars;
  
  /* constant for calendar type */
  var PRIVATE_CALENDAR = "0";
  var SHARED_CALENDAR  = "1";
  var PUBLIC_CALENDAR  = "2";

  var menu = UICalendars.currentMenuElm;

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
    UIContextMenu.menuElement = null ;
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
      if (gj(items[i].firstChild).hasClass("iconCheckBox")) {
          items[i].firstChild.className = items[i].firstChild.className.toString().replace(/iconCheckBox/, "");
      }
      if (gj(items[i]).hasClass(calColor)) {
          var selectedCell = items[i].firstChild;
          gj(selectedCell).addClass("iconCheckBox");
      }
      if (items[i].href.indexOf("ChangeColor") != -1) {
          value = value.replace(/calColor\s*=\s*\w*/, "calColor=" + items[i].className.split(" ")[0]);
      }
      items[i].href = String(items[i].href).replace(/objectId\s*=.*(?='|")/, value);
  }
  
  if (gj(obj).hasClass("calendarItem")) {
      items[0].href = String(items[0].href).replace("')", "&categoryId=" + selectedCategory + "')");
      items[1].href = String(items[1].href).replace("')", "&categoryId=" + selectedCategory + "')");      
  }

  /*
   * disable action for shared and public calendar
   */
  if (calType && (calType != PRIVATE_CALENDAR)) {
      var actions = gj(menu).find("a");
      for (var j = 0; j < actions.length; j++) {
          if ((actions[j].id.indexOf("AddEvent") >= 0) ||
            (actions[j].id.indexOf("AddTask") >= 0) ||
            (actions[j].href.indexOf("EditCalendar") >= 0) ||
            (actions[j].href.indexOf("RemoveCalendar") >= 0) ||
            (actions[j].href.indexOf("ShareCalendar") >= 0) ||
            (actions[j].href.indexOf("ImportCalendar") >= 0) ||
            (actions[j].href.indexOf("ExportCalendar") >= 0) ||
            (actions[j].href.indexOf("ChangeColor") >= 0))
          {
              actions[j].style.display = "none";
          }
      }
  }

  /*
   * remove 'RemoveSharedCalendar' on menu of public and private calendar
   * display only on shared calendar 
   */ 
  if (calType && (calType != SHARED_CALENDAR)) {
      var actions = gj(menu).find("a");
      for (var j = 0; j < actions.length; j++) {
          if ((actions[j].href.indexOf("RemoveSharedCalendar") >= 0)) 
          {
              actions[j].style.display = "none";
          }
      }
  }

  if (canEdit && (canEdit == "true")) {
      var actions = gj(menu).find("a");
      for (var j = 0; j < actions.length; j++) {
          if ((actions[j].id.indexOf("AddEvent") >= 0) ||
            (actions[j].id.indexOf("AddTask") >= 0) ||
            (actions[j].href.indexOf("EditCalendar") >= 0) || 
            (actions[j].href.indexOf("RemoveCalendar") >= 0) ||
            (actions[j].href.indexOf("ImportCalendar") >= 0) ||
            (actions[j].href.indexOf("ExportCalendar") >= 0) ||
            (actions[j].href.indexOf("ChangeColor") >= 0))
          {
              actions[j].style.display = "block";
          }
      }
  }

  /*
   * disable line split and table color 
   */
  if (canEdit && (canEdit == "false")) {
    gj(menu).find(".lineSplit").hide();
    gj(menu).find(".calendarTableColor").hide();
  }

  /*
   * disable remove and edit action on shared calendar menu
   */
  if (calType && (calType == SHARED_CALENDAR)) {
      var actions = gj(menu).find("a");
      for (var j = 0; j < actions.length; j++) {
          if ((actions[j].href.indexOf("RemoveCalendar") >= 0) ||
            (actions[j].href.indexOf("EditCalendar") >= 0) ||
            (actions[j].href.indexOf("ChangeColor") >= 0)) 
          {
              actions[j].style.display = "none";
          }
      }
      gj(menu).find(".lineSplit").hide();
      gj(menu).find(".calendarTableColor").hide();
  }

  var contentContainerElm = gj(anchorElm).parents(".contentContainer")[0];
  if (contentContainerElm) {
    /* position the menu at one third of its height */
    menu.style.top = (gj(anchorElm).offset().top - Math.round(menu.offsetHeight/3)) + 'px';
  }

  /* reposition menu to a distance of 3 px to bottom of viewport if it exceeds the viewport */
  var viewportYofMenu = menu.getBoundingClientRect().top + menu.offsetHeight;
  var deltaToRepositionMenu = viewportYofMenu - gj(window).height();
  if (deltaToRepositionMenu > 0) {
    menu.style.top = (gj(menu).position().top - deltaToRepositionMenu - 3) + 'px';
  }

  var arrowIcon = gj(menu).find(".arrowLeft")[0];
  var positionYofAnchor = gj(anchorElm).offset().top;
  var positionYofMenu   = gj(menu).position().top;
  var positionOfArrowRelativeToMenu = positionYofAnchor - positionYofMenu;
  /* add 7px to center arrow */
  arrowIcon.style.top   = positionOfArrowRelativeToMenu + 7 + 'px';

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
  CSUtils.EventManager.cancelBubble(evt);
  var menuTemplateElm = gj(this.calsFormElem).find('div.' + menuClassName)[0]; 
  this.renderMenu(menuTemplateElm, anchorElm);
  // invoke callback
  if (menuCallback) menuCallback(anchorElm, evt);
};

_module.UICalendars = new UICalendars();
eXo.calendar.UICalendars = _module.UICalendars;

return _module;

})(CSUtils, gj, base);
