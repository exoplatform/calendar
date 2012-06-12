function CalendarLayout() {
  this.UI_WORKING_WORKSPACE = 'UIWorkingWorkspace';
  this.MAIN_WORKING_PANEL = 'MainWorkingPanel';
  this.UI_CALENDAR_CONTAINER = 'UICalendarContainer';
  this.UI_CALENDAR_VIEW_CONTAINER = 'UICalendarViewContainer';
  this.TOGGLE_BUTTON_HEIGHT = 14;
  this.UI_CALENDARS_MIN_HEIGHT = 40;
}

CalendarLayout.prototype.init = function() {
  var DOMUtil = eXo.core.DOMUtil;
  this.loadDOMElements();
  var uiWorkingWorkspace = document.getElementById(this.UI_WORKING_WORKSPACE);
  var UICalendarPortlet = document.getElementById(eXo.calendar.UICalendarPortlet.portletId);
  var UICalendarWorkingContainer = DOMUtil.findFirstDescendantByClass(UICalendarPortlet, 'div', 'UICalendarWorkingContainer');
  this.uiCalendarWorkingContainerHeight = UICalendarWorkingContainer.offsetHeight;
  if (uiWorkingWorkspace) {
    var browserHeight = eXo.core.Browser.getBrowserHeight();
    var workingWorkspaceHeight = uiWorkingWorkspace.offsetHeight;
    if (workingWorkspaceHeight < browserHeight) {
      this.uiCalendarWorkingContainerHeight += (browserHeight - workingWorkspaceHeight);
      UICalendarWorkingContainer.style.height = this.uiCalendarWorkingContainerHeight + 'px';
    }
  }
};
/**
 * This function is installed in the 'setInterval' function to be executed each 200ms to adjust the application height.  
 */
CalendarLayout.prototype.adjustApplicationHeight = function() {
  var CalendarLayout = eXo.calendar.CalendarLayout;
  
};

CalendarLayout.prototype.updateUICalendarViewLayout = function() {
  var CalendarLayout = eXo.calendar.CalendarLayout;
  var UICalendarPortlet = document.getElementById(eXo.calendar.UICalendarPortlet.portletId);
  var UICalendarViewContainer = eXo.core.DOMUtil.findFirstDescendantByClass(UICalendarPortlet, 'div', CalendarLayout.UI_CALENDAR_VIEW_CONTAINER);
  var uCVCHeight = UICalendarViewContainer.offsetHeight;
  if (uCVCHeight != CalendarLayout.uiCalendarWorkingContainerHeight) {
    var uiMainWorkingArea = eXo.core.DOMUtil.findFirstDescendantByClass(UICalendarPortlet, 'div', CalendarLayout.MAIN_WORKING_PANEL);
    if (uiMainWorkingArea) {
      uiMainWorkingArea.style.height = (uiMainWorkingArea.offsetHeight + CalendarLayout.uiCalendarWorkingContainerHeight - uCVCHeight)+ "px";
    }
  }
};

CalendarLayout.prototype.updateHeightParams = function() {
  if (!this.calendarsListHeight && this.UICalendarsListContentContainer.style.display != 'none' && this.UIMiniCalendarContainer.style.display != 'none') {
    this.calendarsListHeight = this.UICalendarsListContentContainer.offsetHeight;
    this.miniCalendarContainerHeight = this.UIMiniCalendarContainer.offsetHeight;
  }
};

CalendarLayout.prototype.loadDOMElements = function() {
  var DOMUtil = eXo.core.DOMUtil;
  var UICalendarPortlet = document.getElementById(eXo.calendar.UICalendarPortlet.portletId);
  this.UICalendarContainer = DOMUtil.findFirstDescendantByClass(UICalendarPortlet, "div", "UICalendarContainer");
  this.UIMiniCalendar = DOMUtil.findFirstDescendantByClass(this.UICalendarContainer, "div", "UIMiniCalendar");
  this.UICalendarsList = DOMUtil.findFirstDescendantByClass(this.UICalendarContainer, "div", "UICalendars");
  this.UIMiniCalendarContainer = DOMUtil.findFirstDescendantByClass(this.UIMiniCalendar, "div", "MiniCalendarContainer");
  this.UICalendarsListContentContainer = DOMUtil.findFirstDescendantByClass(this.UICalendarsList, "div", "ContentContainer");
  this.UIMiniCalendarToggleButton = DOMUtil.findFirstDescendantByClass(this.UIMiniCalendar, "div", "UIMiniCalendarToggleButton");
  this.UICalendarsToggleButton = DOMUtil.findFirstDescendantByClass(this.UICalendarsList, "div", "UICalendarsToggleButton");
  var layoutMan = eXo.calendar.LayoutManager;
  this.layoutcookie = eXo.core.Browser.getCookie(layoutMan.layoutId);
  this.updateHeightParams();
};

CalendarLayout.prototype.updateCalendarContainerLayout = function() {
  this.loadDOMElements();
  if (this.layoutcookie.indexOf("1") >= 0) {
    this.collapseCalendarContainer();
  } else {
    this.expandCalendarContainer();
  }
};

CalendarLayout.prototype.updateMiniCalendarLayout = function() {
  this.loadDOMElements();
  if (this.layoutcookie.indexOf("2") >= 0) {
    this.expandMiniCalendar();
  } else {
    this.collapseMiniCalendar();
  }
};

CalendarLayout.prototype.updateUICalendarsLayout = function() {
  this.loadDOMElements();
  if (this.layoutcookie.indexOf("3") >= 0) {
    this.collapseUICalendars();
  } else {
    this.expandUICalendars();
  }
};

CalendarLayout.prototype.collapseCalendarContainer = function() {
  this.UICalendarContainer.style.display = "none";
  var UICalendarViewContainer = eXo.core.DOMUtil.findNextElementByTagName(this.UICalendarContainer, "div");
  if (eXo.core.I18n.isRT()) {
      UICalendarViewContainer.style.marginRight = "0px";
    }else{
      UICalendarViewContainer.style.marginLeft = "0px";
    }
};

CalendarLayout.prototype.expandCalendarContainer = function() {
  this.UICalendarContainer.style.display = "block";
  var UICalendarViewContainer = eXo.core.DOMUtil.findNextElementByTagName(this.UICalendarContainer, "div");
  if (eXo.core.I18n.isRT()) {
    UICalendarViewContainer.style.marginRight = "236px" ;
  }else{
    UICalendarViewContainer.style.marginLeft = "236px" ;
  }
};

CalendarLayout.prototype.toggleMiniCalendar = function() {
  this.init();
  if (this.UIMiniCalendarContainer.style.display == "none" || this.UIMiniCalendarContainer.style.display == undefined)
    this.expandMiniCalendar();
  else this.collapseMiniCalendar();
};

CalendarLayout.prototype.collapseMiniCalendar = function() {
  this.UIMiniCalendarContainer.style.display = "none";
  this.UICalendarsList.style.top = this.TOGGLE_BUTTON_HEIGHT + "px";
  var downCssClass = this.UIMiniCalendarToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UIMiniCalendarToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UIMiniCalendarToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(upCssClass, downCssClass);
  this.UIMiniCalendarToggleButton.className = buttonCssClassStr;
};

CalendarLayout.prototype.expandMiniCalendar = function() {
  this.UIMiniCalendarContainer.style.display = "block";
  this.UICalendarsList.style.top = this.UIMiniCalendar.offsetHeight + "px";
  var downCssClass = this.UIMiniCalendarToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UIMiniCalendarToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UIMiniCalendarToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(downCssClass, upCssClass);
  this.UIMiniCalendarToggleButton.className = buttonCssClassStr;
};

CalendarLayout.prototype.collapseUICalendars = function() {
  this.UICalendarsListContentContainer.style.display = "none";
  this.UICalendarsList.style.height = this.UI_CALENDARS_MIN_HEIGHT + "px";
  var downCssClass = this.UICalendarsToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UICalendarsToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UICalendarsToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(upCssClass, downCssClass);
  this.UICalendarsToggleButton.className = buttonCssClassStr;
};

CalendarLayout.prototype.expandUICalendars = function() {
  this.UICalendarsListContentContainer.style.display = "block";
  this.UICalendarsList.style.height = "auto";
  var downCssClass = this.UICalendarsToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UICalendarsToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UICalendarsToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(downCssClass, upCssClass);
  this.UICalendarsToggleButton.className = buttonCssClassStr;
};


if (!eXo.calendar.CalendarLayout) eXo.calendar.CalendarLayout = new CalendarLayout();