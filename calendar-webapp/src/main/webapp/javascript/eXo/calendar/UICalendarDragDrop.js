(function(base, cs, gj){
var _module = {};
eXo.calendar = eXo.calendar || {};
// Create new method for special context
cs.DragDrop.findDropableTarget4Cal = function(dndEvent, dropableTargets, mouseEvent) {
  if(dropableTargets == null) return null ;
  var UICalendarDragDropObj = _module.UICalendarDragDrop;
  var additionX = UICalendarDragDropObj.RowContainerDay.scrollLeft;
  var additionY = UICalendarDragDropObj.RowContainerDay.scrollTop;
  var mousexInPage = cs.Browser.Browser.findMouseXInPage(mouseEvent) + additionX ;
  var mouseyInPage = cs.Browser.Browser.findMouseYInPage(mouseEvent) + additionY ;
  if(gj.browser.msie != undefined && base.I18n.isRT())
  	mousexInPage = mousexInPage / 2;
  
  var clickObject = dndEvent.clickObject ;
  var dragObject = dndEvent.dragObject ;
  var foundTarget = null ;
  var len = dropableTargets.length ;
  for(var i = 0 ; i < len ; i++) {
    var ele =  dropableTargets[i] ;
    if(document.getElementById("UIPageDesktop")) {
		mousexInPage = cs.Browser.Browser.findMouseXInPage(mouseEvent) + cs.Utils.getScrollLeft(ele) ;
  		mouseyInPage = cs.Browser.Browser.findMouseYInPage(mouseEvent) + cs.Utils.getScrollTop(ele) ;
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
} ;

/**
 * @author uocnb
 * @constructor
 */
function UICalendarDragDrop() {
  this.scKey = 'background' ;
  this.scValue = '#c0c0c0' ;
  this.DragDrop = cs.DragDrop ;
  this.dropableSets = [] ;
  this.listView = false ;
  this.onMouseMoveCount = 0; //Trick to slow onMouseMove event on Safari

} ;

/**
 * 
 * @param {Array} tableData
 * @param {Array} events
 */
UICalendarDragDrop.prototype.init = function(tableData, events) {
  this.tableData = tableData;
  this.events = events;
  this.RowContainerDay = gj((this.tableData[0])[0]).parents('.rowContainerDay')[0]; 
  this.getAllDropableSets() ;
  this.regDnDItem() ;
} ;

UICalendarDragDrop.prototype.getAllDropableSets = function() {
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
} ;

UICalendarDragDrop.prototype.regDnDItem = function() {
  for (var i=0; i<this.events.length; i++) {
	  gj(this.events[i].rootNode).on('mousedown',this.dndTrigger);
	  //    this.events[i].rootNode.onmousedown = this.dndTrigger;
    for (var j = 0; j < this.events[i].cloneNodes.length; j++) {
    	gj(this.events[i].cloneNodes[j]).on('mousedown',this.dndTrigger);
//      this.events[i].cloneNodes[j].onmousedown = this.dndTrigger;
    }    
  } ; 
} ;

UICalendarDragDrop.prototype.dndTrigger = function(e){
	e = e ? e : window.event;
	eXo.calendar.EventTooltip.disable(e);
	if (e.button == 1 || e.which == 1) {
		return _module.UICalendarDragDrop.initDnD(_module.UICalendarDragDrop.dropableSets, this, this, e);
	}
	return true ;
} ;

/**
 * 
 * @param {Array} dropableObjs
 * @param {Element} clickObj
 * @param {Element} dragObj
 * @param {Event} e
 */
UICalendarDragDrop.prototype.initDnD = function(dropableObjs, clickObj, dragObj, e) {
  var clickBlock = (clickObj && clickObj.tagName) ? clickObj : document.getElementById(clickObj) ;
  var dragBlock = (dragObj && dragObj.tagName) ? dragObj : document.getElementById(dragObj) ;
  
  var blockWidth = clickBlock.offsetWidth ;
  var blockHeight = clickBlock.offsetHeight ;
  var tmpNode = clickBlock.cloneNode(true);

  tmpNode.style.background = "rgb(237,237,237)";
  tmpNode.style.width = dropableObjs[0].offsetWidth + 'px';

  gj(tmpNode).css({opacity:0.5});
  var UIMonthViewNode = document.createElement('div');
  UIMonthViewNode.className = 'UICalendarPortlet uiMonthView';
  var EventMonthContentNode = document.createElement('div');
  EventMonthContentNode.className = 'EventMonthContent';
  
	gj(UIMonthViewNode).addClass("DummyDNDClass");
	gj(EventMonthContentNode).addClass("DummyDNDClass");
	
  tmpNode = this.getCheckedObject(clickBlock) ;
  
  var len = tmpNode.length ;
  while(len--){
		tmpNode[len].style.width = dropableObjs[0].offsetWidth + 'px';
    EventMonthContentNode.appendChild(tmpNode[len]);    
  }
  UIMonthViewNode.appendChild(EventMonthContentNode);
	document.body.insertBefore(UIMonthViewNode,document.body.firstChild);
  this.DragDrop.initCallback = this.initCallback ;
  this.DragDrop.dragCallback = this.dragCallback ;
  this.DragDrop.dropCallback = this.dropCallback ;
  this.DragDrop.init(dropableObjs, clickBlock, UIMonthViewNode, e) ;
  return false ;
} ;

UICalendarDragDrop.prototype.synDragObjectPos = function(dndEvent) {
  if (!dndEvent.backupMouseEvent) {
    dndEvent.backupMouseEvent = window.event ;
    if (!dndEvent.backupMouseEvent) {
      return ;
    }
  }
  var dragObject = dndEvent.dragObject ;
  var mouseX = cs.Browser.Browser.findMouseXInPage(dndEvent.backupMouseEvent);
  var mouseY = cs.Browser.Browser.findMouseYInPage(dndEvent.backupMouseEvent);
  dragObject.style.top = mouseY + 'px' ;
  dragObject.style.left = mouseX + 'px' ;
  if (base.I18n.isRT()) {
		if(gj.browser.msie != undefined) mouseX -= cs.Utils.getScrollbarWidth();
		dragObject.style.right = (gj(window).width() - mouseX) + 'px' ;
		dragObject.style.left = '' ;
  }
} ;

UICalendarDragDrop.prototype.initCallback = function(dndEvent) {
	var dragObj = dndEvent.dragObject;
	dragObj.style.top = '-1000px';
	_module.UICalendarDragDrop.pos = {
		"x": dragObj.offsetLeft,
		"y": dragObj.offsetTop
	} ;
} ;

UICalendarDragDrop.prototype.dragCallback = function(dndEvent) {
	eXo.calendar.EventTooltip.disable();
	if(base.Browser.webkit !=0){
		if(!this.onMouseMoveCount) this.onMouseMoveCount = 0;
		this.onMouseMoveCount++;
		if(this.onMouseMoveCount < 7) return;
		else this.onMouseMoveCount = 0;
	}
  var dragObject = dndEvent.dragObject ;
  if (!dragObject.style.display ||
      dragObject.style.display == 'none') {
    dragObject.style.display = 'block' ;
  }
	dragObject.style.zIndex = 2000 ; // fix for IE 

  _module.UICalendarDragDrop.synDragObjectPos(dndEvent) ;
  // Re-find target
  var foundTarget = 
     cs.DragDrop.findDropableTarget4Cal(dndEvent, cs.DragDrop.dropableTargets, dndEvent.backupMouseEvent) ;
  var junkMove =  cs.DragDrop.isJunkMove(dragObject, foundTarget) ;
  dndEvent.update(foundTarget, junkMove) ;
  
  if (dndEvent.foundTargetObject) {
    if (this.foundTargetObjectCatch != dndEvent.foundTargetObject) {
      if(this.foundTargetObjectCatch) {
        this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
      }
      this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
      this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style.backgroundColor ;
      this.foundTargetObjectCatch.style.backgroundColor = _module.UICalendarDragDrop.scValue ;
    }
  } else {
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
    }
    this.foundTargetObjectCatch = null ;
  }
} ;

UICalendarDragDrop.prototype.dropCallback = function(dndEvent) {
	eXo.calendar.EventTooltip.enable();
  var eventObj = gj(dndEvent.dragObject).find('div.eventBoxes');
  _module.UICalendarDragDrop.highlight(false);
  if ((_module.UICalendarDragDrop.pos.x == dndEvent.dragObject.offsetLeft) && (_module.UICalendarDragDrop.pos.y == dndEvent.dragObject.offsetTop)) {
    _module.UICalendarDragDrop.pos = null ;
    return ;
  }
  // Re-find target
  var foundTarget = 
      cs.DragDrop.findDropableTarget4Cal(dndEvent, cs.DragDrop.dropableTargets, dndEvent.backupMouseEvent) ;
  var junkMove =  cs.DragDrop.isJunkMove(dndEvent.dragObject, foundTarget) ;
  dndEvent.update(foundTarget, junkMove) ;
  
  gj(dndEvent.dragObject).remove();
  if (this.foundTargetObjectCatch) {
    this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
  }
  this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
	if (this.foundTargetObjectCatch && gj(this.foundTargetObjectCatch).hasClass("calendarItem")) {
		var moveAction = gj(dndEvent.dragObject).find('div.eventBoxes')[0].getAttribute("moveAction"); 
		ajaxAsyncGetRequest(cs.Utils.createUrl(moveAction,null), false) ;
		return ;
	}
  if (this.foundTargetObjectCatch) {
    if ((this.foundTargetObjectCatch.getAttribute('startTime') == dndEvent.clickObject.getAttribute('startTime')) && (eventObj.length == 1)) {
      return;
    }
    if (actionlink = this.foundTargetObjectCatch.getAttribute("actionLink")) {
      var clickObject = dndEvent.clickObject;
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
} ;

UICalendarDragDrop.prototype.getCheckedObject = function(clickObj){
    var eventContainer = gj(clickObj).parents('.rowContainerDay')[0];
    var evenObj = gj(eventContainer).find('div.eventBoxes');
    var checkedObj = [];
    var i = evenObj.length ;
    var tmpNode = null ;
    var top = 0 ;
    this.selectedEvent = new Array();

    /** Check the checkbox */
    //tmpNode = gj(clickObj).find('input.checkbox')[0];
    //tmpNode.checked = true ;

    while(i--){
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
};

UICalendarDragDrop.prototype.highlight = function(isHighlight){
	var me = _module.UICalendarDragDrop;
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
};

UICalendarDragDrop.prototype.isCheckedObject = function(eventObj){
  var checkbox = gj(eventObj).find('input.checkbox')[0]; 
  return checkbox.checked ;
} ;

_module.UICalendarDragDrop = new UICalendarDragDrop();

eXo.calendar.UICalendarDragDrop = _module.UICalendarDragDrop;

return _module.UICalendarDragDrop;
})(base, cs, gj);
