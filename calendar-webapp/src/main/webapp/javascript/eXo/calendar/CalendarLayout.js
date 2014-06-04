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
        var jTableColumn = gj('.UITableColumn');
        if (jTableColumn.length > 0) {
          jTableColumn[0].style.cssText = 'table-layout: fixed !important; margin: 0px auto;';      
        }
    },
    
    /**
     * This function is installed in the 'setInterval' function to be executed each 200ms to adjust the application height.  
     */
    adjustApplicationHeight : function() {      
    },
        
    updateUICalendarViewLayout : function(view) {
        var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
        var UICalendarPortlet = document.getElementById(UICalendarPortlet.portletId);
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
    },    
    
    loadDOMElements : function() {
      var UICalendarPortlet = eXo.calendar.UICalendarPortlet;      
      var UICalendarPortlet = document.getElementById(UICalendarPortlet.portletId);
      this.UICalendarContainer = gj(UICalendarPortlet).find("div.UICalendarContainer")[0];
      this.layoutcookie = base.Browser.getCookie(layoutMan.layoutId);
    },
    
    updateCalendarContainerLayout : function() {
      this.loadDOMElements();
      var arrowIcon = gj("#ShowHideAll").find('i');
      if (this.layoutcookie.indexOf("1") >= 0) {
        arrowIcon.attr('class','uiIconMiniArrowRight');
        arrowIcon.css('display','block');
        this.collapseCalendarContainer();
    
      } else {
        this.expandCalendarContainer();
      }
    },    
    
    collapseCalendarContainer : function() {
      this.UICalendarContainer.style.display = "none";
      var UICalendarViewContainer = gj(this.UICalendarContainer).nextAll("div")[0];
      if (base.I18n.isRT()) {
          UICalendarViewContainer.style.marginRight = "0px";
        }else{
          UICalendarViewContainer.style.marginLeft = "0px";
        }
    },
    
    expandCalendarContainer : function() {
      this.UICalendarContainer.style.display = "block";
      var UICalendarViewContainer = gj(this.UICalendarContainer).nextAll("div")[0];
      if (base.I18n.isRT()) {
        UICalendarViewContainer.style.marginRight = "245px" ;
      }else{
        UICalendarViewContainer.style.marginLeft = "245px" ;
      }
    }
  };
  
  return {
    CalendarLayout : CalendarLayout,
    LayoutManager : layoutMan
  };

})(base, LayoutManager, gj);
