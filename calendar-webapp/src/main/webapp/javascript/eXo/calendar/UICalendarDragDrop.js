(function(base, gj, common, CSUtils) {
var _module = {};
eXo.calendar = eXo.calendar || {};
var DragDrop = common.DragDrop;

// Create new method for special context
DragDrop.findDropableTarget4Cal = function(dragObject, dropableTargets, mouseEvent) {
  if(dropableTargets == null) return null ;
  var mousexInPage = mouseEvent.pageX;
  var mouseyInPage = mouseEvent.pageY;
  if(gj.browser.msie != undefined && base.I18n.isRT())
  	mousexInPage = mousexInPage / 2;

  var foundTarget = null ;
  var len = dropableTargets.length ;
  for(var i = 0 ; i < len ; i++) {
    var ele =  dropableTargets[i] ;
    if(document.getElementById("UIPageDesktop")) {
		mousexInPage = mouseEvent.pageX + CSUtils.getScrollLeft(ele) ;
  		mouseyInPage = mouseEvent.pageY + CSUtils.getScrollTop(ele) ;
	}
    if(dragObject != ele && this.isIn(mousexInPage, mouseyInPage, ele)) {
      if(foundTarget == null) {
        foundTarget = ele ;
      } else {
        if(this.isAncestor(foundTarget, ele)) {
          foundTarget = ele ;
        }
      } 
    }
  }
  
  return foundTarget ;
};

DragDrop.isIn = function(x, y, component) {
  var componentLeft = base.Browser.findPosX(component);
  var componentRight = componentLeft + component.offsetWidth ;
  var componentTop = gj(component).offset().top;
  var componentBottom = componentTop + component.offsetHeight ;
  var isOver = false ;

  if((componentLeft < x) && (x < componentRight)) {
    if((componentTop < y) && (y < componentBottom)) {
      isOver = true;
    }
  }
  return isOver ;
};

var UICalendarDragDrop = {
  scKey : 'background',
  scValue : '#c0c0c0',
  dropableSets : [],
  listView : false,
  onMouseMoveCount : 0, //Trick to slow onMouseMove event on Safari

  /**
   * 
   * @param {Array} tableData
   * @param {Array} events
   */
  init : function(tableData, events) {
    this.tableData = tableData;
    this.events = events;
    this.RowContainerDay = gj((this.tableData[0])[0]).parents('.rowContainerDay')[0]; 
    this.getAllDropableSets() ;
    this.regDnDItem() ;
  },

  getAllDropableSets : function() {
    this.dropableSets = new Array();
    for (var i=0; i<this.tableData.length; i++) {
      var row = this.tableData[i];
      for (var j=0; j<row.length; j++) {
        this.dropableSets.push(row[j]);
      }
    }
    // For moving events between calendars.
    var uiCalendars = document.getElementById("UICalendars");
    var calendarItems = gj(uiCalendars).find('div.calendarItem'); 
    this.dropableSets.pushAll(calendarItems);
  },

  regDnDItem : function() {
    for (var i=0; i<this.events.length; i++) {
      gj(this.events[i].rootNode).off('mousedown.dndTrigger').on('mousedown.dndTrigger',this.dndTrigger);
      for (var j = 0; j < this.events[i].cloneNodes.length; j++) {
        gj(this.events[i].cloneNodes[j]).off('mousedown.dndTrigger').on('mousedown.dndTrigger',this.dndTrigger);
      }    
    };
  },

  dndTrigger : function(e) {
    eXo.calendar.EventTooltip.disable(e);
    if (e.button == 1 || e.which == 1) {
      return UICalendarDragDrop.initDnD(UICalendarDragDrop.dropableSets, this, e);
    }
    return true;
  },

  /**
   * 
   * @param {Array} dropableObjs
   * @param {Element} clickObj
   * @param {Element} dragObj
   * @param {Event} e
   */
  initDnD : function(dropableObjs, clickObj, e) {
    var clickBlock = clickObj;  

    var UIMonthViewNode = document.createElement('div');
    UIMonthViewNode.className = 'UICalendarPortlet uiMonthView';
    var EventMonthContentNode = document.createElement('div');
    EventMonthContentNode.className = 'EventMonthContent';
    
    gj(UIMonthViewNode).addClass("DummyDNDClass");
    gj(EventMonthContentNode).addClass("DummyDNDClass");
    
    var tmpNode = this.getCheckedObject(clickBlock);
    var len = tmpNode.length;
    while(len--) {
      tmpNode[len].style.width = dropableObjs[0].offsetWidth + 'px';
      EventMonthContentNode.appendChild(tmpNode[len]);
    }
    UIMonthViewNode.appendChild(EventMonthContentNode);
    document.body.insertBefore(UIMonthViewNode,document.body.firstChild);
    
    DragDrop.init(clickBlock, UIMonthViewNode);
    UIMonthViewNode.onDragStart = this.initCallback;
    UIMonthViewNode.onDrag = this.dragCallback;
    UIMonthViewNode.onDragEnd = this.dropCallback;
    DragDrop.start.call(clickBlock, e);
    return false;
  },

  synDragObjectPos : function(dragObject, e) {
    var mouseX = e.pageX;
    var mouseY = e.pageY;
    dragObject.style.top = mouseY + 'px' ;
    dragObject.style.left = mouseX + 'px' ;
    if (base.I18n.isRT()) {
      if(gj.browser.msie != undefined) mouseX -= CSUtils.getScrollbarWidth();
      dragObject.style.right = (gj(window).width() - mouseX) + 'px' ;
      dragObject.style.left = '' ;
    }
  },

  initCallback : function(left, top, lastMouseX, lastMouseY, e) {
    var dragObj = this;
    dragObj.style.top = '-1000px';
    UICalendarDragDrop.pos = {
      "x": dragObj.offsetLeft,
      "y": dragObj.offsetTop
    } ;
  },

  dragCallback : function(nx, ny, ex, ey, e) {
    eXo.calendar.EventTooltip.disable(e);
    if(base.Browser.webkit !=0){
      if(!this.onMouseMoveCount) this.onMouseMoveCount = 0;
      this.onMouseMoveCount++;
      if(this.onMouseMoveCount < 7) return;
      else this.onMouseMoveCount = 0;
    }
    var dragObject = this;
    if (!dragObject.style.display ||
        dragObject.style.display == 'none') {
      dragObject.style.display = 'block';
    }
    dragObject.style.zIndex = 2000 ; // fix for IE 

    UICalendarDragDrop.synDragObjectPos(dragObject, e);
    // Re-find target
    var foundTarget = 
       DragDrop.findDropableTarget4Cal(dragObject, UICalendarDragDrop.dropableSets, e);
    
    if (foundTarget) {
      if (this.foundTargetObjectCatch != foundTarget) {
        if(this.foundTargetObjectCatch) {
          this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
        }
        this.foundTargetObjectCatch = foundTarget;
        this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style.backgroundColor ;
        this.foundTargetObjectCatch.style.backgroundColor = UICalendarDragDrop.scValue ;
      }
    } else {
      if (this.foundTargetObjectCatch) {
        this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
      }
      this.foundTargetObjectCatch = null;
    }
  },

  dropCallback : function(left, top, clientX, clientY, e) {
    eXo.calendar.EventTooltip.enable(e);
    var eventObj = gj(this).find('div.eventBoxes');
    UICalendarDragDrop.highlight(false);
    if ((UICalendarDragDrop.pos.x == this.offsetLeft) && (UICalendarDragDrop.pos.y == this.offsetTop)) {
      UICalendarDragDrop.pos = null ;
      return ;
    }
    // Re-find target
    var foundTarget = 
        DragDrop.findDropableTarget4Cal(this, UICalendarDragDrop.dropableSets, e);

    gj(this).remove();
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
    }
    this.foundTargetObjectCatch = foundTarget;
    if (this.foundTargetObjectCatch && gj(this.foundTargetObjectCatch).hasClass("calendarItem")) {
      var moveAction = gj(this).find('div.eventBoxes')[0].getAttribute("moveAction"); 
      ajaxAsyncGetRequest(CSUtils.createUrl(moveAction,null), false) ;
      return ;
    }
    var clickObject = e.target;
    if (this.foundTargetObjectCatch) {
      if ((this.foundTargetObjectCatch.getAttribute('startTime') == clickObject.getAttribute('startTime')) && (eventObj.length == 1)) {
        return;
      }
      if (actionlink = this.foundTargetObjectCatch.getAttribute("actionLink")) {
        var currentDate = this.foundTargetObjectCatch.getAttribute("startTime") ;
        var eventId = clickObject.getAttribute("eventId") ;
        var calId = clickObject.getAttribute("calId") ;
        var calType = clickObject.getAttribute("calType") ;
        
        actionlink = actionlink.replace(/objectId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"objectId=" + currentDate) ;
        actionlink = actionlink.replace(/eventId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"eventId=" + eventId) ;
        actionlink = actionlink.replace(/calendarId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calendarId=" + calId) ;
        actionlink = actionlink.replace(/calType\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calType=" + calType) ;
        actionlink = actionlink.replace("javascript:","") ;

        gj.globalEval(actionlink) ;
      }
    }
  },

  getCheckedObject : function(clickObj) {
      var eventContainer = gj(clickObj).parents('.rowContainerDay')[0];
      var evenObj = gj(eventContainer).find('div.eventBoxes');
      var checkedObj = [];
      var i = evenObj.length ;
      var tmpNode = null ;
      var top = 0 ;
      this.selectedEvent = new Array();

      /** Check the checkbox */
      tmpNode = gj(clickObj).find('input.checkbox')[0];
      tmpNode.checked = true ;

      while(i--) {
          if(!this.isCheckedObject(evenObj[i])) continue ;
          tmpNode = evenObj[i].cloneNode(true) ;
          gj(tmpNode).css({opacity:0.5});
          tmpNode.style.top = top + "px";
          top += 20 ;
          this.selectedEvent.push(evenObj[i]);
          checkedObj.push(tmpNode);
      }
      this.highlight(true);
      return checkedObj ;
  },

  highlight : function(isHighlight) {
    var me = UICalendarDragDrop;
    if(!me.selectedEvent) return ;
    var i = me.selectedEvent.length ;
    if(isHighlight){
      while(i--){
        gj(me.selectedEvent[i]).addClass("UIHightlightEvent");
      }
    } else{
      while(i--){
        gj(me.selectedEvent[i]).toggleClass("UIHightlightEvent");
      }
      delete me.selectedEvent ;
    }
  },

  isCheckedObject : function(eventObj) {
    var checkbox = gj(eventObj).find('input.checkbox')[0]; 
    return checkbox.checked ;
  }
};

return UICalendarDragDrop;
})(base, gj, common, CSUtils);