function CalendarLayout() {
  this.UI_WORKING_WORKSPACE = 'UIWorkingWorkspace';
  this.MAIN_WORKING_PANEL = 'MainWorkingPanel';
  this.UI_CALENDAR_CONTAINER = 'UICalendarContainer';
  this.UI_CALENDAR_VIEW_CONTAINER = 'UICalendarViewContainer';
  this.TOGGLE_BUTTON_HEIGHT = 14;
  this.UI_CALENDARS_MIN_HEIGHT = 40;
}

CalendarLayout.prototype.init = function() {
  this.loadDOMElements();
  var uiWorkingWorkspace = document.getElementById(this.UI_WORKING_WORKSPACE);
  var UICalendarPortlet = document.getElementById(_module.UICalendarPortlet.portletId);
  var UICalendarWorkingContainer = gj(UICalendarPortlet).find('div.UICalendarWorkingContainer')[0];
  this.uiCalendarWorkingContainerHeight = UICalendarWorkingContainer.offsetHeight;
  if (uiWorkingWorkspace) {
    var browserHeight = gj(window).height();
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
  var CalendarLayout = _module.CalendarLayout;
  
};

CalendarLayout.prototype.updateUICalendarViewLayout = function() {
  var CalendarLayout = _module.CalendarLayout;
  var UICalendarPortlet = document.getElementById(_module.UICalendarPortlet.portletId);
  var UICalendarViewContainer = gj(UICalendarPortlet).find('div.' + CalendarLayout.UI_CALENDAR_VIEW_CONTAINER)[0];
  var uCVCHeight = UICalendarViewContainer.offsetHeight;
  if (uCVCHeight != CalendarLayout.uiCalendarWorkingContainerHeight) {
    var uiMainWorkingArea = gj(UICalendarPortlet).find('div.' + CalendarLayout.MAIN_WORKING_PANEL)[0]; 
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
  var UICalendarPortlet = document.getElementById(_module.UICalendarPortlet.portletId);
  this.UICalendarContainer = gj(UICalendarPortlet).find("div.UICalendarContainer")[0];
  this.UIMiniCalendar = gj(this.UICalendarContainer).find("div.UIMiniCalendar")[0];
  this.UICalendarsList = gj(this.UICalendarContainer).find("div.UICalendars")[0];
  this.UIMiniCalendarContainer = gj(this.UIMiniCalendar).find("div.MiniCalendarContainer")[0];
  this.UICalendarsListContentContainer = gj(this.UICalendarsList).find("div.ContentContainer")[0];
  this.UIMiniCalendarToggleButton = gj(this.UIMiniCalendar).find("div.UIMiniCalendarToggleButton")[0];
  this.UICalendarsToggleButton = gj(this.UICalendarsList).find("div.UICalendarsToggleButton")[0];
  var layoutMan = _module.LayoutManager;
  this.layoutcookie = base.Browser.getCookie(layoutMan.layoutId);
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


CalendarLayout.prototype.collapseCalendarContainer = function() {
  this.UICalendarContainer.style.display = "none";
  var UICalendarViewContainer = gj(this.UICalendarContainer).nextAll("div")[0];
  if (base.I18n.isRT()) {
      UICalendarViewContainer.style.marginRight = "0px";
    }else{
      UICalendarViewContainer.style.marginLeft = "0px";
    }
};

CalendarLayout.prototype.expandCalendarContainer = function() {
  this.UICalendarContainer.style.display = "block";
  var UICalendarViewContainer = gj(this.UICalendarContainer).nextAll("div")[0];
  if (base.I18n.isRT()) {
    UICalendarViewContainer.style.marginRight = "236px" ;
  }else{
    UICalendarViewContainer.style.marginLeft = "236px" ;
  }
};

 

if (!eXo.calendar.CalendarLayout) eXo.calendar.CalendarLayout = new CalendarLayout();
if(!eXo.calendar.LayoutManager) eXo.calendar.LayoutManager = cs.LayoutManager("calendarlayout");

_module.CalendarLayout = eXo.calendar.CalendarLayout;
_module.LayoutManager = eXo.calendar.LayoutManager;