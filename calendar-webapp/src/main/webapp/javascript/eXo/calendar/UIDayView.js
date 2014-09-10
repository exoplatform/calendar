(function(gj, EventTooltip, UIResizeEvent, base, CSUtils) {
  var uiDayView = null;

  function UIDayView() {}
  
  /**
   * Initialize some properties in Day view
   */
  UIDayView.prototype.init = function() {
    try {
      var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
      var uiDayViewGrid = UICalendarPortlet.getElementById("UIDayViewGrid");
      if (!uiDayViewGrid)
        return false;
      this.viewer = gj(uiDayViewGrid).find('div.eventBoardContainer')[0];
    }
    catch (e) {
      window.status = " !!! Error : " + e.message;
      return false;
    }
    return true;
  };
  
  /**
   * Sort event elements in time table - day view
   */
  UIDayView.prototype.showEvent = function() {
    this.init();
    var EventDayContainer = gj(this.viewer).parents(".eventDayContainer")[0];
    if (this.originalHeightOfEventDayContent === null) {
      this.originalHeightOfEventDayContent = gj(EventDayContainer).height();
    }
    
    if (!EventDayContainer) return ;
    this.editAlldayEvent(EventDayContainer);
    if (!this.init())
      return;
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    UICalendarPortlet.viewType = "UIDayView";
    var el = CSUtils.getElements(this.viewer);
    el = CSUtils.sortByAttribute(el, "startTime");
    if (el.length <= 0) {
      this.resizeHeightForDayView(EventDayContainer, this.originalHeightOfEventDayContent);
      return;
    }

    var marker = null;
    for (var i = 0; i < el.length; i++) {
      UICalendarPortlet.setSize(el[i]);
      var isEditable = gj(el[i]).attr('isEditable');
      
      if (isEditable && (isEditable == "true")) {
        gj(el[i]).off('dblclick').on({'mouseover': eXo.calendar.EventTooltip.show,
          'mouseout':eXo.calendar.EventTooltip.hide,
          'mousedown': this.initDND,
          'dblclick': UICalendarPortlet.ondblclickCallback});
        marker = gj(el[i]).children("div.resizeEventContainer")[0];
        gj(marker).off().on('mousedown', UIResizeEvent.init);
      }
      
      if (isEditable && (isEditable == "false")) {
        gj(el[i]).off('dblclick').on({'mouseover':eXo.calendar.EventTooltip.show,
          'mouseout':eXo.calendar.EventTooltip.hide,
          'mousedown': false,
          'dblclick': UICalendarPortlet.ondblclickCallback});
        gj(el[i]).find('.eventContainerBar').css('cursor', 'default');
        marker = gj(el[i]).find('div.resizeEventContainer')[0];
        gj(marker).hide();
      }
    }
    
    this.items = el;
    this.adjustWidth(this.items);
    for(var i = 0; i < el.length; i++) {
      gj(el[i]).css('display','block');
    }
    
    this.resizeHeightForDayView(EventDayContainer, this.originalHeightOfEventDayContent);
    
    this.items = null;
    this.viewer = null;
  };
  
  /**
   * resize height for day view to stop at bottom of the page
   * @param {Object} contentContainer DOM element
   * @param {int}    originalHeight   original height of content container
   */
  UIDayView.prototype.resizeHeightForDayView = function(contentContainer, originalHeight) {
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    UICalendarPortlet.resizeHeight(contentContainer, 6, originalHeight);

    gj(window).resize(function() {
      UICalendarPortlet.resizeHeight(contentContainer, 6, originalHeight);
    });
  };
  
  UIDayView.prototype.editAlldayEvent = function(cont) {
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    cont = gj(cont).prevAll("div")[0];
    var events = gj(cont).find("div.eventContainerBorder");
    var i = events.length;
    if(!events || (i <= 0)) return;
    while(i--) {
      gj(events[i]).off('dblclick').off('mouseover').off('mouseout').on('dblclick', UICalendarPortlet.ondblclickCallback)
      .on('mouseover',eXo.calendar.EventTooltip.show).on('mouseout',eXo.calendar.EventTooltip.hide);
    }
  }
  
  /**
   * Initializes drag and drop actions
   * @param {Object} evt Mouse event
   */
  UIDayView.prototype.initDND = function(evt) {
    eXo.calendar.EventTooltip.disable(evt);
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    evt.stopPropagation();
    if(evt.button == 2) return;
    uiDayView.dragObject = this;
    CSUtils.resetZIndex(uiDayView.dragObject);
    uiDayView.dragContainer = gj(uiDayView.dragObject).parents(".eventDayContainer")[0];
    CSUtils.resetZIndex(uiDayView.dragObject);
    uiDayView.eventY = evt.clientY;
    uiDayView.eventTop = uiDayView.dragObject.offsetTop;
    gj(uiDayView.dragContainer).on({'mousemove':uiDayView.dragStart,
      'mouseup':uiDayView.dragEnd});
    uiDayView.title = gj(uiDayView.dragObject).find("span")[0].innerHTML;
    UICalendarPortlet.dropCallback = uiDayView.dayviewDropCallback;
    UICalendarPortlet.setPosition(uiDayView.dragObject);
    return false; // prevent default drag event of browser.
  };
  
  /**
   * Processes when dragging object
   * @param {Object} evt Mouse event
   */
  UIDayView.prototype.dragStart = function(evt) {
    evt.preventDefault();
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    var mouseY = base.Browser.findMouseRelativeY(uiDayView.dragContainer, evt) + uiDayView.dragContainer.scrollTop;
    var posY = uiDayView.dragObject.offsetTop;
    UICalendarPortlet.updateTitle(uiDayView.dragObject, posY);
    var delta = parseInt((mouseY - posY) - (mouseY - posY) % UICalendarPortlet.interval);
    uiDayView.dragObject.style.top = posY + delta  + "px";
  };
  
  /**
   * End calendar event dragging, this method clean up some unused properties and execute callback function
   */
  UIDayView.prototype.dragEnd = function() {
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    gj(this).off('mousemove');
    var dragObject = uiDayView.dragObject;
    var eventTop = uiDayView.eventTop;
    if (dragObject.offsetTop != eventTop) {
      UICalendarPortlet.checkPermission(dragObject);
    }
    eXo.calendar.EventTooltip.enable();
  }
  
  UIDayView.prototype.dayviewDropCallback = function() {
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    var dragObject = uiDayView.dragObject;
    
    if (!dragObject) { return; }
    
    var calType = dragObject.getAttribute("caltype");
    var start = parseInt(dragObject.getAttribute("starttime"));
    var end = parseInt(dragObject.getAttribute("endTime"));
    var isOccur = dragObject.getAttribute("isoccur");
    var eventId = dragObject.getAttribute("eventid");
    var recurId = dragObject.getAttribute("recurid");
    if (recurId == "null") recurId = "";
    var title = gj(dragObject).find("p")[0];
    var titleName = uiDayView.title;
    if (end == 0)
      end = 1440;
    var delta = end - start;
    var currentStart = UICalendarPortlet.pixelsToMins(dragObject.offsetTop);
    var currentEnd = currentStart + delta;
    var eventDayContainer = gj(dragObject).parents(".eventDayContainer")[0];
    gj(eventDayContainer).off("mousemove mouseup");
    uiDayView.dragObject = null;
    uiDayView.eventTop = null;
    uiDayView.eventY = null;
    uiDayView.dragContainer = null;
    uiDayView.title = null;
    var actionLink = dragObject.getAttribute("actionLink");
    var form = gj(dragObject).parents("form")[0];
    form.elements[eventId + "startTime"].value = currentStart;
    form.elements[eventId + "finishTime"].value = currentEnd;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
    UICalendarPortlet.setTimeValue(dragObject,currentStart,currentEnd);
    uiDayView.showEvent();
    gj.globalEval(actionLink);
  }

  /**
   * Callback method when browser resizes
   */
  UIDayView.prototype.browserResizeCallback = function() {
    if (!uiDayView.items)
      return;
    uiDayView.adjustWidth(uiDayView.items);
  }
  
  /**
   * Sets dimension for event elements
   * @param {Object} el An array of calendar events
   * @param {Object} totalWidth Width of calendar event container
   */
  UIDayView.prototype.adjustWidth = function(el, totalWidth) {
    var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    var inter = UICalendarPortlet.getInterval(el);
    if (el.length <= 0)
      return;
    var width = "";
    for (var i = 0; i < inter.length; i++) {
      var totalWidth = (arguments.length > 1) ? arguments[1] : parseFloat(100);
      totalWidth -= 2 ;
      var offsetLeft = parseFloat(0);
      var left = parseFloat(0);
      if (arguments.length > 2) {
        offsetLeft = parseFloat(arguments[2]);
        left = arguments[2];
      }
      var len = (inter[i + 1] - inter[i]);
      if (isNaN(len))
        continue;
      var mark = null;
      if (i > 0) {
        for (var l = 0; l < inter[i]; l++) {
          if ((el[inter[i]].offsetTop > el[l].offsetTop) && (el[inter[i]].offsetTop < (el[l].offsetTop + el[l].offsetHeight))) {
            mark = l;
          }
        }
        if (mark != null) {
          offsetLeft = parseFloat(el[mark].style.left) + parseFloat(el[mark].style.width);
        }
      }
      var n = 0;
      for (var j = inter[i]; j < inter[i + 1]; j++) {
        
        if (mark != null) {
          width = parseFloat((totalWidth + left - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width)) / len - 1);
        }
        else {
          width = parseFloat(totalWidth / len - 1);
        }
        gj(el[j]).css('overflow','hidden');
        
        UICalendarPortlet.setWidth(el[j], width);
        
        if (el[j - 1] && (len > 1))
          setLeft(el[j],offsetLeft + (parseFloat(el[j - 1].style.width) + 1) * n);
        else {
          setLeft(el[j],offsetLeft);
        }
        n++;
      }
    }
    function setLeft(obj,left) {
      obj.style.left = left + "%";
      if(base.I18n.isRT()) {
        obj.style.right = left + "%";
      }
    }
  }
  
  uiDayView = new UIDayView();
  return uiDayView;
})($, EventTooltip, UIResizeEvent, base, CSUtils);