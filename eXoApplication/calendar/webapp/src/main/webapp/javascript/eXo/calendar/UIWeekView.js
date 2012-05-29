function UIWeekView() {
	
}

UIWeekView.prototype.mousePos = function(evt){
	return {
		"x" : eXo.core.Browser.findMouseXInPage(evt) ,
		"y" : eXo.core.Browser.findMouseYInPage(evt)
	} ;
} ;

UIWeekView.prototype.init = function() {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var DOMUtil = eXo.core.DOMUtil ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	var allEvents = DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", "EventContainerBorder") ;
	this.container = document.getElementById("UIWeekViewGrid") ;
	var EventWeekContent = DOMUtil.findAncestorByClass(this.container,"EventWeekContent") ;
	this.items = new Array() ;
	eXo.calendar.UICalendarPortlet.viewType = "UIWeekView" ;
	for(var i = 0 ; i < allEvents.length ; i ++) {
		if(allEvents[i].style.display != "none") this.items.push(allEvents[i]) ;
	}
	var len = UIWeekView.items.length ;
  //UICalendarPortlet.setFocus() ;
	if (len <= 0) {
		this.initAllday() ;
		return;
	}	
	var marker = null ;
	for(var i = 0 ; i < len ; i ++){		
		var height = parseInt(this.items[i].getAttribute("endTime")) - parseInt(this.items[i].getAttribute("startTime")) ;
		eXo.core.EventManager.addEvent(this.items[i],"mousedown",UIWeekView.dragStart);
		eXo.core.EventManager.addEvent(this.items[i],"dblclick",eXo.calendar.UICalendarPortlet.ondblclickCallback);
		eXo.core.EventManager.addEvent(this.items[i],"mouseover",eXo.calendar.EventTooltip.show);
		eXo.core.EventManager.addEvent(this.items[i],"mouseout",eXo.calendar.EventTooltip.hide);
		marker = DOMUtil.findFirstDescendantByClass(this.items[i], "div", "ResizeEventContainer") ;
		eXo.core.EventManager.addEvent(marker,"mousedown",UIWeekView.initResize);
	}
	var tr = DOMUtil.findDescendantsByTagName(this.container, "tr") ;
	var firstTr = null ;
	for(var i = 0 ; i < tr.length ; i ++) {
		if (tr[i].style.display != "none") {
			firstTr = tr[i] ;
			break ;
		}
	}
	this.cols = DOMUtil.findDescendantsByTagName(firstTr, "td") ;
	this.distributeEvent() ;
	this.setSize() ;
	this.initAllday() ;
	//UICalendarPortlet.setFocus() ;
} ;

UIWeekView.prototype.distributeEvent = function() {
	var UIWeekView = eXo.calendar.UIWeekView ;
	var len = UIWeekView.cols.length ;
	for(var i = 1 ; i < len ; i ++) {
		if (!eXo.core.DOMUtil.findChildrenByClass(UIWeekView.cols[i], "div", "EventContainerBorder")) return ;
		var colIndex = parseInt(UIWeekView.cols[i].getAttribute("eventindex")) ;
		var eventIndex = null ;
		for(var j = 0 ; j < UIWeekView.items.length ; j ++){		
			eventIndex = parseInt(UIWeekView.items[j].getAttribute("eventindex")) ;
			if (colIndex == eventIndex) UIWeekView.cols[i].appendChild(UIWeekView.items[j]) ;
		}			
	}
} ;

UIWeekView.prototype.onResize = function() {
		eXo.calendar.UIWeekView.setSize() ;
		eXo.calendar.UICalendarPortlet.checkFilter();
} ;

UIWeekView.prototype.setSize = function() {
	var UIWeekView = eXo.calendar.UIWeekView ;
	if(!UIWeekView.cols) return ;
	var len = UIWeekView.cols.length ;
	for(var i = 1 ; i < len ; i ++) {
		UIWeekView.showInCol(UIWeekView.cols[i]) ;
	}	
} ;

UIWeekView.prototype.adjustWidth = function(el) {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var inter = UICalendarPortlet.getInterval(el) ;
	if (el.length <= 0) return ;
  // set position of events in vertical axis.
  for (var i = 0; i < el.length; i++) {
    UICalendarPortlet.setSize(el[i]) ;
  }

	var width = "" ;
	for(var i = 0 ; i < inter.length ; i ++) {
		var totalWidth = (arguments.length > 1) ? arguments[1] : parseFloat(100) ;
    totalWidth -= 10 ;
		var offsetLeft = parseFloat(0) ;
		var left = parseFloat(0) ;
		if(arguments.length > 2) {
			offsetLeft = parseFloat(arguments[2]) ;
			left = arguments[2] ;
		} 
		var len = (inter[i+1] - inter[i]) ;
		if(isNaN(len)) continue ;
		var mark = null ;
		if (i > 0){
			for(var l = 0 ; l < inter[i] ; l ++) {
				if((el[inter[i]].offsetTop > el[l].offsetTop) && (el[inter[i]].offsetTop < (el[l].offsetTop + el[l].offsetHeight))) {
					mark = l ;					
				}
			}			
			if (mark != null) {
				offsetLeft = parseFloat(el[mark].style.left) + parseFloat(el[mark].style.width) ;
			}
		}
		var n = 0 ;
		for(var j = inter[i]; j < inter[i+1] ; j++) {
			if(mark != null) {				
				width = parseFloat((totalWidth + left - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width))/len) ;
			} else {
				width = parseFloat(totalWidth/len) ;
			}
			el[j].style.width = width + "px" ;
			if (el[j-1]&&(len > 1)) {
				setLeft(el[j],offsetLeft + parseFloat(el[j-1].style.width)*n);
			}
			else {		
				setLeft(el[j],offsetLeft);
			}
			n++ ;
		}
	}
	function setLeft(obj,left){
		obj.style.left = left + "px";
		if(eXo.core.I18n.isRT()){
		if(eXo.core.Browser.isIE6()) left -= eXo.cs.Utils.getScrollbarWidth();
			obj.style.right = left + "px";	
		}
	}
} ;

UIWeekView.prototype.showInCol = function(obj) {
	var items = eXo.calendar.UICalendarPortlet.getElements(obj) ;
	var len = items.length ;
	if (len <= 0) return ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var container = (eXo.core.Browser.isFF()) ? UIWeekView.container : items[0].offsetParent ;
	var left = parseFloat((eXo.core.Browser.findPosXInContainer(obj, container) - 1)/container.offsetWidth)*100 ;
	var width = parseFloat((obj.offsetWidth - 2)/container.offsetWidth)*100 ;
	items = eXo.calendar.UICalendarPortlet.sortByAttribute(items, "startTime") ;
	UIWeekView.adjustWidth(items, obj.offsetWidth, eXo.core.Browser.findPosXInContainer(obj, container,eXo.core.I18n.isRT())) ;
} ;

UIWeekView.prototype.dragStart = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	if (_e.button == 2) return ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	UIWeekView.dragElement = this ;
	eXo.calendar.UICalendarPortlet.resetZIndex(UIWeekView.dragElement) ;
	UIWeekView.objectOffsetLeft = eXo.core.Browser.findPosX(UIWeekView.dragElement) ;
	UIWeekView.offset = UIWeekView.getOffset(UIWeekView.dragElement, _e) ;
	UIWeekView.mouseY = _e.clientY ;
	UIWeekView.mouseX = _e.clientX ;
	UIWeekView.eventY = UIWeekView.dragElement.offsetTop ;
	UIWeekView.containerOffset = {
		"x" : eXo.core.Browser.findPosX(UIWeekView.container.parentNode),
		"y" : eXo.core.Browser.findPosY(UIWeekView.container.parentNode)
	}
	UIWeekView.title = eXo.core.DOMUtil.findDescendantsByTagName(UIWeekView.dragElement,"p")[0].innerHTML ;
	document.onmousemove = UIWeekView.drag ;
	document.onmouseup = UIWeekView.drop ;
	eXo.calendar.UICalendarPortlet.dropCallback = UIWeekView.dropCallback ;
	eXo.calendar.UICalendarPortlet.setPosition(UIWeekView.dragElement);
} ;

UIWeekView.prototype.drag = function(evt) {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var mouseY = eXo.core.Browser.findMouseRelativeY(UIWeekView.container,_e) - UIWeekView.container.scrollTop ;
	var posY = UIWeekView.dragElement.offsetTop ;
	var height =  UIWeekView.dragElement.offsetHeight ;
	var deltaY = null ;
	deltaY = _e.clientY - UIWeekView.mouseY ;
	var currentTop =  UIWeekView.mousePos(_e).y - UIWeekView.offset.y - UIWeekView.containerOffset.y;
	var maxTop = UIWeekView.dragElement.offsetParent.scrollHeight - height; 
	if(currentTop >= 0 && currentTop <= maxTop){
		UIWeekView.dragElement.style.top = (currentTop - currentTop%eXo.calendar.UICalendarPortlet.interval) + "px" ;
		if (UIWeekView.isCol(_e)) {
			var posX = eXo.core.Browser.findPosXInContainer(UIWeekView.currentCol, UIWeekView.dragElement.offsetParent) ;
			UIWeekView.dragElement.style.left = posX + "px" ;
		}
	}
	UICalendarPortlet.updateTitle(UIWeekView.dragElement, posY);
	UIWeekView.dragElement.style.width = (UIWeekView.dragElement.parentNode.offsetWidth - 10) + "px";
} ;

UIWeekView.prototype.dropCallback = function() {
	var me = eXo.calendar.UIWeekView ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
	var dragElement = me.dragElement ;
	var start = parseInt(dragElement.getAttribute("startTime")) ;
	var end = parseInt(dragElement.getAttribute("endTime")) ;
	var calType = parseInt(dragElement.getAttribute("calType")) ;
	var calId = dragElement.getAttribute("calid");
	var eventId = dragElement.getAttribute("eventid");
	var workingStart = 0 ;
	if (end == 0) end = 1440 ;
	var delta = end - start  ;
	var currentStart = UICalendarPortlet.pixelsToMins(dragElement.offsetTop)  + workingStart ;
	var currentEnd = currentStart + delta ;
	var currentDate = me.currentCol.getAttribute("startTime").toString() ;
	var isOccur = dragElement.getAttribute("isoccur");
    var recurId = dragElement.getAttribute("recurid");
    if (recurId == "null") recurId = "";
	var actionLink = dragElement.getAttribute("actionLink");
	var form = eXo.core.DOMUtil.findAncestorByTagName(dragElement,"form");
  form.elements[eventId + "startTime"].value = currentStart;
  form.elements[eventId + "finishTime"].value = currentEnd;
  form.elements[eventId + "currentDate"].value = currentDate;
  form.elements[eventId + "isOccur"].value = isOccur;
  form.elements[eventId + "recurId"].value = recurId;
	me.currentCol.appendChild(dragElement) ;
	eXo.calendar.UICalendarPortlet.setTimeValue(dragElement,currentStart,currentEnd,me.currentCol);
	me.setSize();
	eval(actionLink);
	//eXo.webui.UIForm.submitEvent(eXo.calendar.UICalendarPortlet.portletId + '#' + 'UIWeekView', 'UpdateEvent', '&subComponentId=' + 'UIWeekView' + '&objectId=' + eventId + '&calendarId=' + calId + '&calType=' + calType + '&startTime=' + currentStart + '&finishTime=' + currentEnd + '&currentDate=' + currentDate + '&isOccur=' + isOccur + '&recurId=' + recurId);
	me.cleanUp();
} ;

UIWeekView.prototype.drop = function(evt) {
	document.onmousemove = null ;
	var _e = window.event || evt ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var isEventbox = eXo.core.EventManager.getEventTargetByClass(evt,"WeekViewEventBoxes");
	if (!UIWeekView.isCol(_e) || !isEventbox) return ;
	var currentCol = UIWeekView.currentCol ;
	var sourceCol = UIWeekView.dragElement.parentNode ;
	var eventY = UIWeekView.eventY ;
	if((UIWeekView.mouseY != _e.clientY) || (UIWeekView.mouseX != _e.clientX)) eXo.calendar.UICalendarPortlet.checkPermission(UIWeekView.dragElement);
	eXo.calendar.EventTooltip.enable();
	return null ;
} ;

UIWeekView.prototype.cleanUp = function(){
	var UIWeekView = eXo.calendar.UIWeekView ;
	UIWeekView.title = null ;
	UIWeekView.offset = null ;
	UIWeekView.mouseY = null ;
	UIWeekView.mouseX = null ;
	UIWeekView.eventY = null ;
	UIWeekView.objectOffsetLeft = null ;
	UIWeekView.containerOffset = null ;
	UIWeekView.title = null ;
	UIWeekView.dragElement = null ;	
};

UIWeekView.prototype.getOffset = function(object, evt) {	
	return {
		"x": (eXo.calendar.UIWeekView.mousePos(evt).x - eXo.core.Browser.findPosX(object)) ,
		"y": (eXo.calendar.UIWeekView.mousePos(evt).y - eXo.core.Browser.findPosY(object))
	} ;
} ;

UIWeekView.prototype.isCol = function(evt) {
	var UIWeekView = eXo.calendar.UIWeekView ;
	if (!UIWeekView.dragElement) return false;
	var Browser = eXo.core.Browser ;
	var isIE = (Browser.browserType == "ie")?true:false ;
	var isDesktop = (document.getElementById("UIPageDesktop"))?true:false ;
	var mouseX = Browser.findMouseXInPage(evt);
	if(eXo.core.I18n.isRT() && (Browser.isIE7() || Browser.isIE6())) mouseX = mouseX - 32; // 32 =  double of scrollbar width
	var len = UIWeekView.cols.length ;
	var colX = 0 ;
	var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
	for(var i = 1 ; i < len ; i ++) {
		colX = Browser.findPosX(UIWeekView.cols[i]) ;
		if(uiControlWorkspace && isIE && (!isDesktop || Browser.isIE7())) colX -= uiControlWorkspace.offsetWidth ;
		if ((mouseX > colX) && (mouseX < colX + UIWeekView.cols[i].offsetWidth)){
			return UIWeekView.currentCol = UIWeekView.cols[i] ;
		}
	}
	
	return false ;
} ;

// for resize

UIWeekView.prototype.createTooltip = function(){
	var tooltip = document.createElement("div");
	tooltip.className = "UIEventTooltip";
	eXo.calendar.UIWeekView.tooltip = tooltip;
	var app = document.getElementById("UIPortalApplication");
	app.appendChild(tooltip);
};

UIWeekView.prototype.showTooltip = function(outer,delta,evt,dir){	
	var totalWidth = outer.parentNode.offsetWidth;
	var weekdays = parseInt(document.getElementById("UIWeekViewGridAllDay").getAttribute("numberofdays"));
	delta = parseInt(delta * (24 * weekdays * 60 * 60 * 1000) / totalWidth);
	if(dir) delta = parseInt(outer.getAttribute("startTime")) + delta;
	else delta = parseInt(outer.getAttribute("endTime")) + delta;
	
	// for timezone setting
	var timezoneOffset = -(new Date()).getTimezoneOffset();
	var settingTimezone = eXo.calendar.UICalendarPortlet.settingTimezone;
	delta = delta - timezoneOffset * 60000 + settingTimezone * 60000;
	
	var unit = 15*60*1000;
	delta = parseInt(delta/unit)*unit;
	var tooltip = eXo.calendar.UIWeekView.tooltip;
	var extraLeft = eXo.core.Browser.getBrowserWidth() - eXo.core.Browser.findMouseXInPage(evt);
	extraLeft = (extraLeft < tooltip.offsetWidth)? (tooltip.offsetWidth - extraLeft):0;
	tooltip.style.left = eXo.core.Browser.findMouseXInPage(evt) - extraLeft + "px";
	tooltip.style.top = eXo.core.Browser.findMouseYInPage(evt) + 20 + "px";
	tooltip.innerHTML = eXo.cs.DateTimeFormater.format((new Date(delta)),"ddd, dd/mmm hh:MM TT");
};

UIWeekView.prototype.removeTooltip = function(){
	if(eXo.calendar.UIWeekView.tooltip){
		eXo.core.DOMUtil.removeElement(eXo.calendar.UIWeekView.tooltip);
		delete eXo.calendar.UIWeekView.tooltip;
	}
};

UIWeekView.prototype.initResize = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	if(_e.button == 2) return ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var outerElement = eXo.core.DOMUtil.findAncestorByClass(this,'EventContainerBorder') ;
	var innerElement = eXo.core.DOMUtil.findPreviousElementByTagName(this, "div") ;
	var container = eXo.core.DOMUtil.findAncestorByClass(document.getElementById("UIWeekViewGrid"), "EventWeekContent") ;
	var minHeight = 15 ;
	var interval = eXo.calendar.UICalendarPortlet.interval ;
	UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval) ;
	eXo.calendar.UICalendarPortlet.dropCallback = eXo.calendar.UIWeekView.resizeCallback;
	eXo.calendar.UICalendarPortlet.setPosition(outerElement);
} ;

UIWeekView.prototype.resizeCallback = function(evt) {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var eventBox = UIResizeEvent.outerElement ;
	var start =  parseInt(eventBox.getAttribute("startTime")) ;
	var end =  start + UICalendarPortlet.pixelsToMins(eventBox.offsetHeight);
	var calType = parseInt(eventBox.getAttribute("calType")) ;
	var isOccur = eventBox.getAttribute("isoccur");
	var eventId = eventBox.getAttribute("eventid");
  var recurId = eventBox.getAttribute("recurid");
  if (recurId == "null") recurId = "";
    
	if (eventBox.offsetHeight != UIResizeEvent.beforeHeight) {
		var actionLink = eventBox.getAttribute("actionLink");
		var currentDate = eventBox.parentNode.getAttribute("startTime").toString() ;
		var form = eXo.core.DOMUtil.findAncestorByTagName(eventBox,"form");
    form.elements[eventId + "startTime"].value = start;
    form.elements[eventId + "finishTime"].value = end;
    form.elements[eventId + "currentDate"].value = currentDate;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
		eXo.calendar.UICalendarPortlet.setTimeValue(eventBox,start,end);	
		eXo.calendar.UIWeekView.setSize();	
		 eval(actionLink);
	}
	eXo.calendar.EventTooltip.enable();
} ;

UIWeekView.prototype.initAllDayRightResize = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	if (_e.button == 2) return ;
	var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var outerElement = eXo.core.DOMUtil.findAncestorByClass(this,'WeekViewEventBoxes') ;
	var innerElement = eXo.core.DOMUtil.findFirstDescendantByClass(outerElement, "div", "EventAlldayContent") ;
	UIHorizontalResize.start(_e, outerElement, innerElement) ;
	UIHorizontalResize.dragCallback = eXo.calendar.UIWeekView.rightDragResizeCallback ;
	UIHorizontalResize.callback = eXo.calendar.UIWeekView.rightResizeCallback ;
	eXo.calendar.UIWeekView.createTooltip();
} ;

UIWeekView.prototype.initAllDayLeftResize = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	if (_e.button == 2) return ;	
	var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var outerElement = eXo.core.DOMUtil.findAncestorByClass(this,'WeekViewEventBoxes') ;
	var innerElement = eXo.core.DOMUtil.findFirstDescendantByClass(outerElement, "div", "EventAlldayContent") ;
	UIHorizontalResize.start(_e, outerElement, innerElement, true) ;
	UIHorizontalResize.dragCallback = eXo.calendar.UIWeekView.leftDragResizeCallback ;
	UIHorizontalResize.callback = eXo.calendar.UIWeekView.leftResizeCallback ;
	eXo.calendar.UIWeekView.createTooltip();
} ;

UIWeekView.prototype.rightDragResizeCallback = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var outer = eXo.calendar.UIHorizontalResize.outerElement ;
	var inner = eXo.calendar.UIHorizontalResize.innerElement ;
	var totalWidth = outer.parentNode.offsetWidth;
	var posX = outer.offsetLeft ;
	var width = outer.offsetWidth;
	var maxX = posX + width ;
	var extraWidth = 0;
	if (document.getElementById("UIPageDesktop") || !eXo.core.Browser.isIE6()) {
  	maxX -= 55;
		extraWidth = 55;
  }
	if (maxX >= totalWidth) {
		outer.style.width = (totalWidth - posX - 2 + extraWidth) + "px" ;
		inner.style.width = (totalWidth - posX - 8 + extraWidth) + "px" ;
	}
	var delta = outer.offsetWidth - eXo.calendar.UIHorizontalResize.beforeWidth ;
	eXo.calendar.UIWeekView.showTooltip(outer,delta,evt);
} ;

UIWeekView.prototype.leftDragResizeCallback = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var outer = eXo.calendar.UIHorizontalResize.outerElement ;
	var left = outer.offsetLeft ;
	var extraWidth = 0;
	if (document.getElementById("UIPageDesktop") || !eXo.core.Browser.isIE6()) {
		left -= 55;
		extraWidth = 55;
  }
	if(left == 0) eXo.calendar.UIWeekView.extraWidth = outer.offsetWidth - 2;
	if (left < 0 ) {		
		outer.style.left = extraWidth + "px" ;
		outer.style.width = eXo.calendar.UIWeekView.extraWidth + "px" ;
	}
	var delta = eXo.calendar.UIHorizontalResize.beforeWidth - outer.offsetWidth ;
	eXo.calendar.UIWeekView.showTooltip(outer,delta,evt,true);
} ;

UIWeekView.prototype.rightResizeCallback = function() {
	var UIWeekView = eXo.calendar.UIWeekView ;
	var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;	
	var outer = UIHorizontalResize.outerElement ;
	var totalWidth = outer.parentNode.offsetWidth;
	var delta = outer.offsetWidth - UIHorizontalResize.beforeWidth ;
	if (delta != 0) {
  	var weekdays = parseInt(document.getElementById("UIWeekViewGridAllDay").getAttribute("numberofdays"));
  	var UICalendarPortlet = eXo.calendar.UICalendarPortlet
  	var delta = parseInt(delta * (24 * weekdays * 60 * 60 * 1000) / totalWidth);
  	var start = parseInt(outer.getAttribute("startTime"));
  	var end = parseInt(outer.getAttribute("endTime")) + delta;
  	var calType = parseInt(outer.getAttribute("calType"));
  	var isOccur = outer.getAttribute("isoccur");
  	 var eventId = outer.getAttribute("eventid");
  	var recurId = outer.getAttribute("recurid");
  	if (recurId == "null") recurId = "";
  	var actionLink = outer.getAttribute("actionLink");
  	var form = eXo.core.DOMUtil.findAncestorByTagName(outer,"form");
    form.elements[eventId + "startTime"].value = start;
    form.elements[eventId + "finishTime"].value = end;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
    eval(actionLink);
  }
	eXo.calendar.UIWeekView.removeTooltip();
	eXo.calendar.EventTooltip.enable();
} ;

UIWeekView.prototype.leftResizeCallback = function() {
	var UIWeekView = eXo.calendar.UIWeekView ;
	var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var outer = UIHorizontalResize.outerElement ;
	var totalWidth = outer.parentNode.offsetWidth;
	var delta = UIHorizontalResize.beforeWidth - outer.offsetWidth ;
	if (delta != 0) {
  	var weekdays = parseInt(document.getElementById("UIWeekViewGridAllDay").getAttribute("numberofdays"));
  	var UICalendarPortlet = eXo.calendar.UICalendarPortlet
  	var delta = Math.round(delta * (24 * weekdays * 60 * 60 * 1000) / totalWidth);
  	var start = parseInt(outer.getAttribute("startTime")) + delta;
  	var end = parseInt(outer.getAttribute("endTime"));
  	var calType = parseInt(outer.getAttribute("calType"));
  	var isOccur = outer.getAttribute("isoccur");
  	var eventId = outer.getAttribute("eventid");
  	var recurId = outer.getAttribute("recurid");
  	if (recurId == "null") recurId = "";
  	var actionLink = outer.getAttribute("actionLink");
    var form = eXo.core.DOMUtil.findAncestorByTagName(outer,"form");
    form.elements[eventId + "startTime"].value = start;
    form.elements[eventId + "finishTime"].value = end;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
    eval(actionLink);
  }
	if(eXo.calendar.UIWeekView.extraWidth) delete eXo.calendar.UIWeekView.extraWidth;
	eXo.calendar.UIWeekView.removeTooltip();
	eXo.calendar.EventTooltip.enable();
} ;

// For all day event

UIWeekView.prototype.initAlldayDND = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	if (_e.button == 2) return ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var DragDrop = eXo.core.DragDrop ;
	var EventAllday = eXo.core.DOMUtil.findAncestorByClass(this, "EventAllday") ;
	dragObject = this ;
	UIWeekView.totalWidth = EventAllday.offsetWidth ;
	UIWeekView.elementTop = dragObject.offsetTop ;
	UIWeekView.elementLeft = dragObject.offsetLeft ;
	DragDrop.initCallback = UIWeekView.allDayInitCallback ;
	DragDrop.dragCallback = UIWeekView.allDayDragCallback ;
	DragDrop.dropCallback = UIWeekView.allDayDropCallback ;
	DragDrop.init(null, dragObject, dragObject, _e) ;	
} ;

UIWeekView.prototype.allDayInitCallback = function(evt) {
	var UIWeekView = eXo.calendar.UIWeekView ;
	var dragObject = evt.dragObject ;
	UIWeekView.beforePercentStart = parseFloat(dragObject.style.left) ;
	UIWeekView.beforeStart = dragObject.offsetLeft ;
	dragObject.style.left = dragObject.offsetLeft + "px" ;
} ;

UIWeekView.prototype.allDayDragCallback = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var UIWeekView = eXo.calendar.UIWeekView ;
	var dragObject = evt.dragObject ;
	dragObject.style.top = UIWeekView.elementTop + "px" ;
	var posX = parseInt(dragObject.style.left) ;
	var is55 = document.getElementById("UIPageDesktop") || !eXo.core.Browser.isIE6() ;
	var min = 0 ;
	var max = UIWeekView.totalWidth - dragObject.offsetWidth ;	
	if(is55) 
		min += 55 ;
	else
		max -= 55 ;
		
	if (posX <= min) {
		dragObject.style.left = min + "px" ;
	}
	if (posX >= max) {		
		dragObject.style.left = max + "px" ;
	}
} ;

UIWeekView.prototype.allDayDropCallback = function(evt) {
	var dragObject = evt.dragObject ;	
	var UIWeekView = eXo.calendar.UIWeekView ;
	var totalWidth = dragObject.parentNode.offsetWidth ;
	var delta = dragObject.offsetLeft - UIWeekView.beforeStart ;
	//if (delta == 0) dragObject.style.left = UIWeekView.beforePercentStart + "%" ;
	UIWeekView.elementLeft = null ;
	UIWeekView.elementTop = null ;
	UIWeekView.beforeStart = null ;
	UIWeekView.beforePercentStart = null ;
	if (delta != 0) {
		var weekdays = parseInt(document.getElementById("UIWeekViewGridAllDay").getAttribute("numberofdays"));
		var UICalendarPortlet = eXo.calendar.UICalendarPortlet
		var delta = Math.round(delta*(24*weekdays*60*60*1000)/totalWidth) ;
		var start =  parseInt(dragObject.getAttribute("startTime")) + delta ;
		var end = parseInt(dragObject.getAttribute("endTime")) + delta ;
		var calType = parseInt(dragObject.getAttribute("calType")) ;
		var isOccur = dragObject.getAttribute("isoccur");
		var eventId = dragObject.getAttribute("eventid");
		var recurId = dragObject.getAttribute("recurid");
		if (recurId == "null") recurId = "";
		var actionLink = dragObject.getAttribute("actionLink");
		var form = eXo.core.DOMUtil.findAncestorByTagName(dragObject,"form");
    form.elements[eventId + "startTime"].value = start;
    form.elements[eventId + "finishTime"].value = end;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
    eval(actionLink);
	}	
	eXo.calendar.EventTooltip.enable();
} ;

UIWeekView.prototype.initAllday = function() {
	var EventManager = eXo.core.EventManager;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var uiWeekView = document.getElementById("UIWeekView") ;
	var uiWeekViewGridAllDay = eXo.core.DOMUtil.findFirstDescendantByClass(uiWeekView,"table","UIGrid") ;
	this.eventAlldayContainer = eXo.core.DOMUtil.findDescendantsByClass(uiWeekView, "div", "EventAlldayContainer") ;
	var eventAllday = new Array() ;
	for(var i = 0 ; i < this.eventAlldayContainer.length ; i ++) {
		if (this.eventAlldayContainer[i].style.display != "none") eventAllday.push(this.eventAlldayContainer[i]) ;
	}
	var len = eventAllday.length ;
	if (len <= 0) return ;
	var resizeMark = null ;
	for(var i = 0 ; i < len ; i ++) {
		resizeMark = eXo.core.DOMUtil.getChildrenByTagName(eventAllday[i], "div") ;
		if (eXo.core.DOMUtil.hasClass(resizeMark[0], "ResizableSign")) EventManager.addEvent(resizeMark[0],"mousedown",UIWeekView.initAllDayLeftResize) ;
		if (eXo.core.DOMUtil.hasClass(resizeMark[2], "ResizableSign")) EventManager.addEvent(resizeMark[2],"mousedown",UIWeekView.initAllDayRightResize) ; 
    EventManager.addEvent(eventAllday[i],"mouseover",eXo.calendar.EventTooltip.show) ;
		EventManager.addEvent(eventAllday[i],"mouseout",eXo.calendar.EventTooltip.hide) ;
		EventManager.addEvent(eventAllday[i],"mousedown",eXo.calendar.UIWeekView.initAlldayDND) ;
    EventManager.addEvent(eventAllday[i],"dblclick",eXo.calendar.UICalendarPortlet.ondblclickCallback) ;
	}
	var EventAlldayContainer = eXo.core.DOMUtil.findFirstDescendantByClass(uiWeekViewGridAllDay,"td","EventAllday") ;
	this.weekdays = eXo.core.DOMUtil.findDescendantsByClass(uiWeekViewGridAllDay, "td","UICellBlock") ;
	this.startWeek = 	UIWeekView.weekdays[1] ;
	this.endWeek = 	UIWeekView.weekdays[UIWeekView.weekdays.length-1] ;
} ;

UIWeekView.prototype.sortByWidth = function(obj) {
	var len = obj.length ;
	var tmp = null ;
	var attribute1 = null ;
	var attribute2 = null ;
	for(var i = 0 ; i < len ; i ++){
		attribute1 = obj[i].offsetWidth ;
		for(var j = i + 1 ; j < len ; j ++){
			attribute2 = obj[j].offsetWidth ;
			if(attribute2 > attribute1) {
				tmp = obj[i] ;
				obj[i] = obj[j] ;
				obj[j] = tmp ;
			}
		}
	}
	return obj ;
} ;

UIWeekView.prototype.getMinutes = function(millisecond) {
	return eXo.calendar.UICalendarPortlet.timeToMin(millisecond) ;
} ;

UIWeekView.prototype.sortEventsInCol = function(events) {
	var index = this.getStartEvent(events) ;
	//events = eXo.calendar.UICalendarPortlet.sortByAttribute(events, "startTime", "dsc") ;
	var len = index.length ;// alert(len) ;
	var tmp = new Array() ;
	for(var i = 0 ; i < len ; i ++) {
		tmp.pushAll(this.setGroup(events, index[i])) ;
	}
	eXo.calendar.UICalendarPortlet.sortByAttribute(tmp, "startTime") ;
	return tmp ;
} ;

UIWeekView.prototype.setPosition = function(events) {
	events = this.setWidth(events) ;
	events = this.setLeft(events) ;
	events = this.sortEventsInCol(events) ;
	this.setTop(events) ;
} ;

UIWeekView.prototype.setLeft = function(events) {
	var len = events.length ;
	if (len <= 0) return ;
	var start = 0 ;
	var left = 0 ;
	var startWeek = parseInt(this.startWeek.getAttribute("startTime")) ;
	var totalWidth = parseFloat(eXo.core.Browser.findPosXInContainer(events[0].parentNode, events[0].offsetParent)/events[0].offsetParent.offsetWidth)*100 ;
	for(var i = 0 ; i < len ; i ++) {
		start = parseInt(events[i].getAttribute("startTime")) ;
		if (start < startWeek) start = startWeek ;
		diff = start - startWeek ;
		left = parseFloat((diff/(24*7*60*60*1000))*(100*events[0].parentNode.offsetWidth)/(events[0].offsetParent.offsetWidth)) ;
		events[i].style.left = left + totalWidth + "%" ;
	}
	return events ;
} ;

UIWeekView.prototype.arrayUnique = function(arr) {
	var tmp = new Array() ;
	arr.sort() ;
	for(var i = 0 ; i < arr.length ; i ++) {
		if(arr[i] !== arr[i+1]) {
			tmp[tmp.length] = arr[i] ;
		}
	}
	return tmp ;
} ;

UIWeekView.prototype.getStartEvent = function(events) {
	var start = new Array() ;
	var len = events.length ;
	for(var i = 0 ; i < len ; i ++) {
		start.push(parseInt(events[i].offsetLeft)) ;
	}
	return this.arrayUnique(start) ;
} ;

UIWeekView.prototype.setGroup = function(events, value) {
	var len = events.length ;
	var tmp = new Array() ;
	for(var i = 0 ; i < len ; i ++) {
		if (events[i].offsetLeft == value) {
			tmp.push(events[i]) ;
		}
	}
	return this.sortByWidth(tmp) ;
} ;
UIWeekView.prototype.setTop = function (events) {
	var len = events.length ;
	for(var i = 0 ; i < len ; i ++) {		
		events[i].style.top = "0px" ;
		events[i].style.top = eXo.core.Browser.findPosYInContainer(events[i],events[i].offsetParent) +  i*events[i].offsetHeight + "px" ;
	}
	this.resort(events) ;
	return events ;
} ;

UIWeekView.prototype.resort = function (events) {
	var len = events.length ;
	for(var i = 0 ; i < len ; i ++) {
		var beforeLeft = events[i].offsetLeft + events[i].offsetWidth - 1 ;
		for(var j = i + 1 ; j < len ; j ++) {
			var afterLeft = events[j].offsetLeft ;
			if (afterLeft > beforeLeft) {
				events[j].style.top = events[i].style.top ;
				break ;
			}
		}
	}	
} ;

UIWeekView.prototype.setIndex = function (events) {

} ;

UIWeekView.prototype.setWidth = function(events) {
	var len = events.length ;
	var start = 0 ;
	var end = 0 ;
	var diff = 0 ;
	var uiWeekViewGridAllDay = document.getElementById("UIWeekViewGridAllDay") ;
	var startWeek = this.startWeek ;
	var endWeek = this.endWeek ;
	startWeek = parseInt(startWeek.getAttribute("startTime")) ;
	endWeek = parseInt(endWeek.getAttribute("startTime")) ;
	var totalWidth = parseFloat(events[0].parentNode.offsetWidth/events[0].offsetParent.offsetWidth) ;
	for(var i = 0 ; i < len ; i ++) {
		start = parseInt(events[i].getAttribute("startTime")) ;
		end = parseInt(events[i].getAttribute("endTime")) ;
		if (start < startWeek) start = startWeek ;
		if (end > (endWeek + 24*60*60*1000)) end = endWeek + 24*60*60*1000 ;
		diff = end - start ;
		events[i].style.width = parseFloat(diff/(24*7*60*60*1000))*100*totalWidth - 0.2 + "%" ;
		eXo.core.EventManager.addEvent(events[i],"mousedown",this.initAlldayDND) ;
	}
	return events ;
} ;
// Resize horizontal

function UIHorizontalResize() {
	
}

UIHorizontalResize.prototype.start = function(evt, outer, inner) {
	var _e = window.event || evt ;
	this.outerElement = outer ;
	this.innerElement = inner ;
	if(arguments.length > 3) {
		this.outerElement.style.left = this.outerElement.offsetLeft + "px" ;
		this.isLeft = true ;
		this.beforeLeft = this.outerElement.offsetLeft ;
	} else {
		this.isLeft = false ;
	}
	this.mouseX = _e.clientX ;
	this.outerBeforeWidth = this.outerElement.offsetWidth - 2 ;
	this.innerBeforeWidth = this.innerElement.offsetWidth - 2 ;
	this.beforeWidth = this.outerElement.offsetWidth ;
	document.onmousemove = eXo.calendar.UIHorizontalResize.execute ;
	document.onmouseup = eXo.calendar.UIHorizontalResize.end ;
} ;

UIHorizontalResize.prototype.execute = function(evt) {
	var _e = window.event || evt ;
	var	UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var delta = _e.clientX - UIHorizontalResize.mouseX ;
	if(UIHorizontalResize.isLeft == true) {
		UIHorizontalResize.outerElement.style.left = UIHorizontalResize.beforeLeft + delta + "px" ;
		if (parseInt(UIHorizontalResize.outerElement.style.left) > 0){
			UIHorizontalResize.outerElement.style.width = UIHorizontalResize.outerBeforeWidth - delta + "px" ;
			UIHorizontalResize.innerElement.style.width = UIHorizontalResize.innerBeforeWidth - delta + "px" ;			
		}
	} else {
		UIHorizontalResize.outerElement.style.width = UIHorizontalResize.outerBeforeWidth + delta + "px" ;
		UIHorizontalResize.innerElement.style.width = UIHorizontalResize.innerBeforeWidth + delta + "px" ;		
	}
	if(typeof(UIHorizontalResize.dragCallback) == "function") {
		UIHorizontalResize.dragCallback(_e) ;
	}
} ;

UIHorizontalResize.prototype.end = function(evt) {
	var	UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	if (typeof(UIHorizontalResize.callback) == "function") UIHorizontalResize.callback() ;
	delete UIHorizontalResize.outerElement ;
	delete UIHorizontalResize.innerElement ;
	delete UIHorizontalResize.outerBeforeWidth ;
	delete UIHorizontalResize.innerBeforeWidth ;
	delete UIHorizontalResize.beforeWidth ;
	delete UIHorizontalResize.callback ;
	delete UIHorizontalResize.mouseX ;
	delete UIHorizontalResize.isLeft ;
	delete UIHorizontalResize.beforeLeft ;
	document.onmousemove = null ;
	document.onmouseup = null ;
} ;

// For user selection 

UIWeekView.prototype.initSelection = function() {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
	var UISelection = eXo.calendar.UISelection ;
	var container = document.getElementById("UIWeekViewGrid") ;
	UISelection.step = UICalendarPortlet.CELL_HEIGHT;	
	UISelection.block = document.createElement("div");
	UISelection.block.className = "UserSelectionBlock" ;
	UISelection.container = container ;
	eXo.core.DOMUtil.findPreviousElementByTagName(container, "div").appendChild(UISelection.block) ;
	UISelection.container.onmousedown = UISelection.start ;
	UISelection.relativeObject = eXo.core.DOMUtil.findAncestorByClass(UISelection.container, "EventWeekContent") ;
	UISelection.viewType = "UIWeekView" ;
} ;

UIWeekView.prototype.initSelectionX = function() {
	var Highlighter = eXo.calendar.Highlighter ;
	var table = document.getElementById("UIWeekViewGridAllDay") ;
	var cell = eXo.core.DOMUtil.findDescendantsByTagName(table, "th");	
	var len = cell.length ;
	var link = null ;
	for(var i = 0 ; i < len ; i ++) {
		link = eXo.core.DOMUtil.getChildrenByTagName(cell[i],"a")[0] ;		
		if (link) link.onmousedown = eXo.calendar.UIWeekView.cancelBubble ;
		cell[i].onmousedown = Highlighter.start ;
	}
} ;

UIWeekView.prototype.cancelBubble = function(evt) {
	var _e = evt || window.event ;
	_e.cancelBubble = true ;
} ;

UIWeekView.prototype.callbackSelectionX = function() {
	var UIHSelection = eXo.calendar.UIHSelection ;
	var startTime = parseInt(Date.parse(UIHSelection.firstCell.getAttribute("startTimeFull"))) ;
	var endTime = parseInt(Date.parse(UIHSelection.lastCell.getAttribute("startTimeFull"))) + 24*60*60*1000 - 1 ;
	var porlet = eXo.calendar.UICalendarPortlet;
	var container = document.getElementById("UICalendarViewContainer");	
	porlet.addQuickShowHiddenWithTime(container, 1, startTime, endTime) ;
	//eXo.webui.UIForm.submitEvent("UIWeekView" ,'QuickAdd','&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime) ;
} ;
eXo.calendar.UIHorizontalResize = new UIHorizontalResize() ;
eXo.calendar.UIWeekView = new UIWeekView() ;

