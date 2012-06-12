
// Create new method for special context
DragDrop.prototype.findDropableTarget4Cal = function(dndEvent, dropableTargets, mouseEvent) {
  if(dropableTargets == null) return null ;
  var UICalendarDragDropObj = eXo.calendar.UICalendarDragDrop;
  var additionX = UICalendarDragDropObj.RowContainerDay.scrollLeft;
  var additionY = UICalendarDragDropObj.RowContainerDay.scrollTop;
  var mousexInPage = eXo.core.Browser.findMouseXInPage(mouseEvent) + additionX ;
  var mouseyInPage = eXo.core.Browser.findMouseYInPage(mouseEvent) + additionY ;
  if(eXo.core.Browser.getBrowserType() == "ie" && eXo.core.I18n.isRT())
  	mousexInPage = mousexInPage / 2;
  
  var clickObject = dndEvent.clickObject ;
  var dragObject = dndEvent.dragObject ;
  var foundTarget = null ;
  var len = dropableTargets.length ;
  for(var i = 0 ; i < len ; i++) {
    var ele =  dropableTargets[i] ;
    if(document.getElementById("UIPageDesktop")) {
			mousexInPage = eXo.core.Browser.findMouseXInPage(mouseEvent) + eXo.cs.Utils.getScrollLeft(ele) ;
  		mouseyInPage = eXo.core.Browser.findMouseYInPage(mouseEvent) + eXo.cs.Utils.getScrollTop(ele) ;
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
  this.DOMUtil = eXo.core.DOMUtil ;
  this.DragDrop = eXo.core.DragDrop ;
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
  this.RowContainerDay = eXo.core.DOMUtil.findAncestorByClass((this.tableData[0])[0], 'RowContainerDay');
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
	var calendarItems = eXo.core.DOMUtil.findDescendantsByClass(uiCalendars,"div","CalendarItem");
	this.dropableSets.pushAll(calendarItems);
} ;

UICalendarDragDrop.prototype.regDnDItem = function() {
  for (var i=0; i<this.events.length; i++) {
    this.events[i].rootNode.onmousedown = this.dndTrigger;
    for (var j = 0; j < this.events[i].cloneNodes.length; j++) {
      this.events[i].cloneNodes[j].onmousedown = this.dndTrigger;
    }    
  } ; 
} ;

UICalendarDragDrop.prototype.dndTrigger = function(e){
  e = e ? e : window.event;
	eXo.calendar.EventTooltip.disable(e);
  if (e.button == 1 || e.which == 1) {
    return eXo.calendar.UICalendarDragDrop.initDnD(eXo.calendar.UICalendarDragDrop.dropableSets, this, this, e);
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

  eXo.core.Browser.setOpacity(tmpNode, 50) ;
  var UIMonthViewNode = document.createElement('div');
  UIMonthViewNode.className = 'UICalendarPortlet UIMonthView';
  var EventMonthContentNode = document.createElement('div');
  EventMonthContentNode.className = 'EventMonthContent';
  
	eXo.core.DOMUtil.addClass(UIMonthViewNode,"DummyDNDClass");
	eXo.core.DOMUtil.addClass(EventMonthContentNode,"DummyDNDClass");
	
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
  var mouseX = eXo.core.Browser.findMouseXInPage(dndEvent.backupMouseEvent);
  var mouseY = eXo.core.Browser.findMouseYInPage(dndEvent.backupMouseEvent);
  dragObject.style.top = mouseY + 'px' ;
  dragObject.style.left = mouseX + 'px' ;
  if (eXo.core.I18n.isRT()) {
		if(eXo.core.Browser.browserType == "ie") mouseX -= eXo.cs.Utils.getScrollbarWidth();
		dragObject.style.right = (eXo.core.Browser.getBrowserWidth() - mouseX) + 'px' ;
		dragObject.style.left = '' ;
  }
} ;

UICalendarDragDrop.prototype.initCallback = function(dndEvent) {
	var dragObj = dndEvent.dragObject;
  dragObj.style.top = '-1000px';
  eXo.calendar.UICalendarDragDrop.pos = {
    "x": dragObj.offsetLeft,
    "y": dragObj.offsetTop
  } ;
} ;

UICalendarDragDrop.prototype.dragCallback = function(dndEvent) {
	eXo.calendar.EventTooltip.disable();
	if(eXo.core.Browser.getBrowserType() == "safari"){
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
  eXo.calendar.UICalendarDragDrop.synDragObjectPos(dndEvent) ;
  // Re-find target
  var foundTarget = 
      eXo.core.DragDrop.findDropableTarget4Cal(dndEvent, eXo.core.DragDrop.dropableTargets, dndEvent.backupMouseEvent) ;
  var junkMove =  eXo.core.DragDrop.isJunkMove(dragObject, foundTarget) ;
  dndEvent.update(foundTarget, junkMove) ;
  
  if (dndEvent.foundTargetObject) {
    if (this.foundTargetObjectCatch != dndEvent.foundTargetObject) {
      if(this.foundTargetObjectCatch) {
        this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
      }
      this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
      this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style.backgroundColor ;
      this.foundTargetObjectCatch.style.backgroundColor = eXo.calendar.UICalendarDragDrop.scValue ;
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
  var eventObj = eXo.core.DOMUtil.findDescendantsByClass(dndEvent.dragObject,"div","EventBoxes");
  eXo.calendar.UICalendarDragDrop.highlight(false);
  if ((eXo.calendar.UICalendarDragDrop.pos.x == dndEvent.dragObject.offsetLeft) && (eXo.calendar.UICalendarDragDrop.pos.y == dndEvent.dragObject.offsetTop)) {
    eXo.calendar.UICalendarDragDrop.pos = null ;
    return ;
  }
  // Re-find target
  var foundTarget = 
      eXo.core.DragDrop.findDropableTarget4Cal(dndEvent, eXo.core.DragDrop.dropableTargets, dndEvent.backupMouseEvent) ;
  var junkMove =  eXo.core.DragDrop.isJunkMove(dndEvent.dragObject, foundTarget) ;
  dndEvent.update(foundTarget, junkMove) ;
  
  eXo.core.DOMUtil.removeElement(dndEvent.dragObject);
  if (this.foundTargetObjectCatch) {
    this.foundTargetObjectCatch.style.backgroundColor = this.foundTargetObjectCatchStyle ;
  }
  this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
	if (this.foundTargetObjectCatch && eXo.core.DOMUtil.hasClass(this.foundTargetObjectCatch,"CalendarItem")) {
		var moveAction = eXo.core.DOMUtil.findFirstDescendantByClass(dndEvent.dragObject,"div","EventBoxes").getAttribute("moveAction");
		ajaxAsyncGetRequest(eXo.cs.Utils.createUrl(moveAction,null), false) ;
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
			
      eval(actionlink) ;
    }
  }
} ;

UICalendarDragDrop.prototype.getCheckedObject = function(clickObj){
  var eventContainer = eXo.core.DOMUtil.findAncestorByClass(clickObj,"RowContainerDay");
  var evenObj = eXo.core.DOMUtil.findDescendantsByClass(eventContainer,"div", "EventBoxes");
  var checkedObj = [];
  var i = evenObj.length ;
  var tmpNode = null ;
  var top = 0 ;
  this.selectedEvent = new Array();
	tmpNode = eXo.core.DOMUtil.findFirstDescendantByClass(clickObj, "input", "checkbox");
	tmpNode.checked = true ;
  while(i--){
    if(!this.isCheckedObject(evenObj[i])) continue ;
    tmpNode = evenObj[i].cloneNode(true) ;
    eXo.core.Browser.setOpacity(tmpNode,50) ;
    tmpNode.style.top = top + "px";		
    top += 20 ;
    this.selectedEvent.push(evenObj[i]);
    checkedObj.push(tmpNode);
  }
  this.highlight(true);
  return checkedObj ;
} ;

UICalendarDragDrop.prototype.highlight = function(isHighlight){
	var me = eXo.calendar.UICalendarDragDrop;
	if(!me.selectedEvent) return ;
	var i = me.selectedEvent.length ;
	if(isHighlight){
		while(i--){
	  	eXo.core.DOMUtil.addClass(me.selectedEvent[i],"UIHightlightEvent");
	  }
	} else{
		while(i--){
	  	eXo.core.DOMUtil.replaceClass(me.selectedEvent[i],"UIHightlightEvent","");
	  }
	  delete me.selectedEvent ;
	}
};

UICalendarDragDrop.prototype.isCheckedObject = function(eventObj){
  var checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(eventObj, "input", "checkbox");
  return checkbox.checked ;
} ;

eXo.calendar.UICalendarDragDrop = new UICalendarDragDrop();
