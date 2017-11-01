(function(base, LayoutManager, gj) {
  var layoutMan = new LayoutManager("calendarlayout");
  
  var CalendarLayout = {
      UI_WORKING_WORKSPACE : 'UIWorkingWorkspace',
      MAIN_WORKING_PANEL : 'MainWorkingPanel',
      UI_CALENDAR_CONTAINER : 'UICalendarContainer',
      UI_CALENDAR_VIEW_CONTAINER : 'uiCalendarViewContainer',
      TOGGLE_BUTTON_HEIGHT : 14,
      UI_CALENDARS_MIN_HEIGHT : 40,
      UI_LIST_VIEW : "uiListView",
      
      init : function() {
        var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
        this.loadDOMElements();
        var uiWorkingWorkspace = document.getElementById(this.UI_WORKING_WORKSPACE);
        var UICalendarPortlet = document.getElementById(UICalendarPortlet.portletId);
        var UICalendarWorkingContainer = gj(UICalendarPortlet).find('div.UICalendarWorkingContainer')[0];
        this.uiCalendarWorkingContainerHeight = UICalendarWorkingContainer.offsetHeight;
    },
        
    loadDOMElements : function() {
      var UICalendarPortlet = eXo.calendar.UICalendarPortlet;      
      var UICalendarPortlet = document.getElementById(UICalendarPortlet.portletId);
      this.UICalendarContainer = gj(UICalendarPortlet).find("div.UICalendarContainer")[0];
      this.layoutcookie = base.Browser.getCookie(layoutMan.layoutId);
    },
    
    updateCalendarContainerLayout : function() {
      var workingContainer = document.getElementById('UICalendarWorkingContainer');
      workingContainer.classList.toggle('collapsed');
    }
  };
  
  return {
    CalendarLayout : CalendarLayout,
    LayoutManager : layoutMan
  };

})(base, LayoutManager, gj);
