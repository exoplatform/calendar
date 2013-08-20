(function(base, cs, gj){
var _module = {};
eXo.calendar = eXo.calendar || {};

function CalendarLayout() {
    this.UI_WORKING_WORKSPACE = 'UIWorkingWorkspace';
    this.MAIN_WORKING_PANEL = 'MainWorkingPanel';
    this.UI_CALENDAR_CONTAINER = 'UICalendarContainer';
    this.UI_CALENDAR_VIEW_CONTAINER = 'uiCalendarViewContainer';
    this.TOGGLE_BUTTON_HEIGHT = 14;
    this.UI_CALENDARS_MIN_HEIGHT = 40;
    this.UI_LIST_VIEW = "uiListView";
}

CalendarLayout.prototype.init = function() {
    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
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

    /*=== reduce height of left navigation container to left navigation ===*/
    var leftNavCont       = gj(".LeftNavigationTDContainer")[0],
        leftNav           = gj(leftNavCont).children("#LeftNavigation")[0],
        leftNavContHeight = gj(leftNavCont).height(),
        leftNavHeight     = gj(leftNav).height(),
        viewPortHeight    = gj(window).height(),
        actionBarHeight   = 40,
        resizedHeight     = viewPortHeight - actionBarHeight;

    if ((leftNavHeight !== null) && (leftNavContHeight !== null)) {
        if (leftNavContHeight > leftNavHeight) {
            if (gj.browser.mozilla) resizedHeight += 12;
            gj(leftNavCont).css('height', resizedHeight + 'px');
        }
    }

    /* override table-layout: auto from plf */
    gj('.UITableColumn')[0].style.cssText = 'table-layout: fixed !important; margin: 0px auto;';
};


/**
 * This function is installed in the 'setInterval' function to be executed each 200ms to adjust the application height.  
 */
CalendarLayout.prototype.adjustApplicationHeight = function() {
  var CalendarLayout = _module.CalendarLayout;
  
};


CalendarLayout.prototype.updateUICalendarViewLayout = function(view) {
    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    var CalendarLayout = _module.CalendarLayout;
    var UICalendarPortlet = document.getElementById(_module.UICalendarPortlet.portletId);
    var UICalendarViewContainer = gj(UICalendarPortlet).find('div.' + CalendarLayout.UI_CALENDAR_VIEW_CONTAINER)[0];
    var uCVCHeight = UICalendarViewContainer.offsetHeight;
    var uiMainWorkingArea = gj(UICalendarPortlet).find('div.' + CalendarLayout.MAIN_WORKING_PANEL)[0]; 
    if (uCVCHeight != CalendarLayout.uiCalendarWorkingContainerHeight) {
        if (uiMainWorkingArea) {
            if (view != this.UI_LIST_VIEW || uCVCHeight > CalendarLayout.uiCalendarWorkingContainerHeight) {
                uiMainWorkingArea.style.height = (uiMainWorkingArea.offsetHeight + CalendarLayout.uiCalendarWorkingContainerHeight - uCVCHeight) + "px";
            } else {
                uiMainWorkingArea.style.height = "auto";
            }
        }
    }
};


CalendarLayout.prototype.loadDOMElements = function() {
  _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
  var UICalendarPortlet = document.getElementById(_module.UICalendarPortlet.portletId);
  this.UICalendarContainer = gj(UICalendarPortlet).find("div.UICalendarContainer")[0];
  var layoutMan = _module.LayoutManager;
  this.layoutcookie = base.Browser.getCookie(layoutMan.layoutId);
};

CalendarLayout.prototype.updateCalendarContainerLayout = function() {
	this.loadDOMElements();
	var arrowIcon = gj("#ShowHideAll").find('i');
	if (this.layoutcookie.indexOf("1") >= 0) {
		arrowIcon.attr('class','uiIconMiniArrowRight');
		arrowIcon.css('display','block');
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
    UICalendarViewContainer.style.marginRight = "245px" ;
  }else{
    UICalendarViewContainer.style.marginLeft = "245px" ;
  }
};

 

if (!eXo.calendar.CalendarLayout) eXo.calendar.CalendarLayout = new CalendarLayout();
if(!eXo.calendar.LayoutManager) eXo.calendar.LayoutManager = cs.CSUtils.LayoutManager("calendarlayout");

_module.CalendarLayout = eXo.calendar.CalendarLayout;
_module.LayoutManager = eXo.calendar.LayoutManager;

return _module;
})(base, cs, gj);
