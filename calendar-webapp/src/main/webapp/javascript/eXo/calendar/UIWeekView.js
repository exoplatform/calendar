(function(base, cs, gj, UICalendarMan){
function UIWeekView() {	
    this.originalHeightOfEventWeekContent = null;
}

var _module = {};
eXo.calendar = eXo.calendar || {} ;

UIWeekView.prototype.mousePos = function(evt){
	return {
		"x" : cs.Browser.Browser.findMouseXInPage(evt) ,
		"y" : cs.Browser.Browser.findMouseYInPage(evt)
	} ;
} ;

UIWeekView.prototype.init = function() {
    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
	var UICalendarPortlet = _module.UICalendarPortlet ;
	var UIWeekView = _module.UIWeekView ;
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	/* get all event div */
    var allEvents = gj(uiCalendarViewContainer).find('div.eventContainerBorder');
	this.container = document.getElementById("UIWeekViewGrid") ;
	var EventWeekContent = gj(this.container).parents(".eventWeekContent")[0] ;
    if (this.originalHeightOfEventWeekContent === null) {
        this.originalHeightOfEventWeekContent = gj(EventWeekContent).height();
    }

    /* store all events in instance variable 'items' */
	this.items = new Array() ;
	_module.UICalendarPortlet.viewType = "UIWeekView" ;
	for(var i = 0 ; i < allEvents.length ; i ++) {
		if(allEvents[i].style.display != "none") this.items.push(allEvents[i]) ;
	}

  /*== REGISTER onclick for allDay header ==*/
  var $whiteTds = gj('#UIWeekViewGridAllDay').find('.whiteTd'),
      container = 'UIWeekViewGridAllDay';
  gj.each($whiteTds, function (index, whiteTd) {
    gj(whiteTd).click(function () {
      var start = parseInt(Date.parse(gj(this).attr("startTimeFull"))) ;
      var end  = parseInt(Date.parse(gj(this).attr("startTimeFull")))  + 24*60*60*1000 - 1;
      UICalendarPortlet.addQuickShowHiddenWithTime(container, 1, start, end) ;
    });
  });

  var len = UIWeekView.items.length ;

    /*=== RESIZE WIDTH AND HEIGHT WHEN NO EVENTS FOUND ===*/
    if (len <= 0) {     /* no events in view */
	    this.initAllday() ;

        this.increaseWidth(EventWeekContent);

        this.resizeHeight(EventWeekContent, this.originalHeightOfEventWeekContent);
        var originalHeight = this.originalHeightOfEventWeekContent;
        /* resize content each time the window is resized */
        gj(window).resize(function() {
            UIWeekView.resizeHeight(EventWeekContent, originalHeight);
  
            UIWeekView.resizeWidth(EventWeekContent);
        });
	  
	    return;
	}

    /*== REGISTER Tooltip and dbclick on event ==*/
	var marker = null ;
	for(var i = 0 ; i < len ; i ++){		
		var height = parseInt(this.items[i].getAttribute("endTime")) - parseInt(this.items[i].getAttribute("startTime")) ;
        var isEditable = gj(this.items[i]).attr('isEditable');
		if (isEditable && (isEditable == "true")) {  
		gj(this.items[i]).off('dblclick').on({'mousedown':UIWeekView.dragStart,
			'mouseover':eXo.calendar.EventTooltip.show,
			'mouseout':eXo.calendar.EventTooltip.hide,
			'dblclick':_module.UICalendarPortlet.ondblclickCallback});
		marker = gj(this.items[i]).find('div.resizeEventContainer')[0];
		gj(marker).on('mousedown',UIWeekView.initResize);
	    }

        if (isEditable && (isEditable == "false")) {
          gj(this.items[i]).find('.eventContainerBar').css('cursor', 'default');
          marker = gj(this.items[i]).find('div.resizeEventContainer')[0];
          gj(this.items[i]).off('dblclick').on({'mousedown':false,
            'mouseover':eXo.calendar.EventTooltip.show,
			'mouseout':eXo.calendar.EventTooltip.hide,
          	'dblclick':_module.UICalendarPortlet.ondblclickCallback});
          //gj(marker).css('cursor', 'default');
          //gj(marker).removeClass('eventContainerBorder:hover');
          gj(marker).hide();
        }	
	}
	var tr = gj(this.container).find('tr'); 
	var firstTr = null ;
	for(var i = 0 ; i < tr.length ; i ++) {
		if (tr[i].style.display != "none") {
			firstTr = tr[i] ;
			break ;
		}
	}
	this.cols = gj(firstTr).find("td") ;

    /*=== resize width ===*/
    this.increaseWidth(EventWeekContent);

    /*=== resize height ===*/
    this.resizeHeight(EventWeekContent, this.originalHeightOfEventWeekContent);

    /* resize content each time the window is resized */
    var originalHeight = this.originalHeightOfEventWeekContent;
    gj(window).resize(function() {
        UIWeekView.resizeHeight(EventWeekContent, originalHeight);

        UIWeekView.resizeWidth(EventWeekContent);
    });

	this.distributeEvent() ;
	this.setSize() ;
	this.initAllday() ;

	//UICalendarPortlet.setFocus() ;
} ;

/**
 * Increase the width to include the scrollbar
 */
UIWeekView.prototype.increaseWidth = function(contentContainer) {
  var originalWidth   = gj(contentContainer).width(),
      widthOfTitleBar = gj(contentContainer).siblings(".eventWeekBar")[0].offsetWidth;

  if (originalWidth !== widthOfTitleBar) {
      gj(contentContainer).css("width", widthOfTitleBar);
  }

  gj(contentContainer).css("width", (widthOfTitleBar + 20));
  var eventTable = gj(contentContainer).children("table.uiGrid")[0];
  gj(eventTable).css("width", widthOfTitleBar);
};

/**
 * Resize height for week view
 * @param {Object} contentContainer DOM element
 * @param {int}    originalHeight   original height of content container
 */
UIWeekView.prototype.resizeHeight = function(contentContainer, originalHeight) {
    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    _module.UICalendarPortlet.resizeHeight(contentContainer, 6, originalHeight);
};

/**
 * Resize width for week view to include the scrollbar
 * @param {Object} contentContainer DOM element
 */
UIWeekView.prototype.resizeWidth = function(contentContainer) {
  var eventWeekBar = gj(contentContainer).siblings(".eventWeekBar")[0],
      resizedWidth = gj(eventWeekBar).width(),
      eventTable   = gj(contentContainer).children("table.uiGrid")[0];
  
  gj(eventTable).css("width", resizedWidth);
  gj(contentContainer).css("width", (resizedWidth + 20));
};


UIWeekView.prototype.distributeEvent = function() {
	var UIWeekView = _module.UIWeekView,
        len = UIWeekView.cols.length ;

    for(var i = 1 ; i < len ; i ++) {
		if (gj(UIWeekView.cols[i]).children('div.eventContainerBorder').length < 0) { return ; }

		var colIndex   = parseInt(UIWeekView.cols[i].getAttribute("eventindex")),
		    eventIndex = null;

		for(var j = 0 ; j < UIWeekView.items.length ; j ++){		
			eventIndex = parseInt(UIWeekView.items[j].getAttribute("eventindex")) ;
			if (colIndex == eventIndex) UIWeekView.cols[i].appendChild(UIWeekView.items[j]) ;
		}			
	}
} ;


UIWeekView.prototype.onResize = function() {
	    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;

		_module.UIWeekView.setSize() ;
		_module.UICalendarPortlet.checkFilter();
} ;

UIWeekView.prototype.setSize = function() {
	var UIWeekView = _module.UIWeekView ;
	if(!UIWeekView.cols) return ;
	var len = UIWeekView.cols.length ;
	for(var i = 1 ; i < len ; i ++) {
		UIWeekView.showInCol(UIWeekView.cols[i]) ;
	}
} ;

UIWeekView.prototype.adjustWidth = function(el) {
    var UICalendarPortlet = _module.UICalendarPortlet ;
    var inter = UICalendarPortlet.getInterval(el) ;
    if (el.length <= 0) return ;
//  set position of events in vertical axis.
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
	for(var j = inter[i]; j < inter[i+1] ; j++) { // loop on each event element
	    
	    gj(el[j]).removeClass("shortTitle"); // reset shortened status
	    gj(el[j]).removeClass("shortDesc"); // reset shortened status
	    var evtCont = gj(el[j]).find('div.eventContainer'); // get the content part of the event area
	    var evtBar = gj(el[j]).find('div.eventContainerBar'); // get the title part of the event area
	    // if the original title and description are not yet saved, we store them in a DOM attribute of the main element
	    if (gj(el[j]).attr("titleHTML") == "" || gj(el[j]).attr("titleHTML") == null) gj(el[j]).attr("titleHTML", gj(evtBar).text());
	    if (gj(el[j]).attr("descHTML") == "" || gj(el[j]).attr("descHTML") == null) gj(el[j]).attr("descHTML", evtCont[0].innerHTML);

	    if(mark != null) {				
			width = parseFloat((totalWidth + left - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width))/len - 1) ;
	    } else {
			width = parseFloat(totalWidth/len - 1) ;
	    }
	    width = Math.round(width);
	    gj(el[j]).css('overflow','hidden');
	    el[j].style.width = width + "px" ;

	    // whether the event has a priority (high, normal, low)
	    var hasFlag = gj(gj(evtBar).find('i')[1]).hasClass("uiIconCalhighPriority") || gj(gj(evtBar).find('i')[1]).hasClass("uiIconCalnormalPriority") || gj(gj(evtBar).find('i')[1]).hasClass("uiIconCallowPriority");
	    if (hasFlag && width <= 60) {
			// if the event has a priority and its width <= 60: hide the title (start end times)
			evtBar[0].lastChild.data = "";
			gj(el[j]).addClass("shortTitle");	
	    } 
	    else if ((hasFlag && width <= 76) || (!hasFlag && width <= 60)) {
			// if the event has a priority and its width <= 76
			// or the event has no priority and its width <= 60 : display start time only
			evtBar[0].lastChild.data = gj(el[j]).attr("titleHTML").split("-")[0];
			gj(el[j]).addClass("shortTitle");	
	    }
	    if (width <= 20) {
			// replace the description by ...
			evtCont[0].innerHTML = "...";
			gj(el[j]).addClass("shortDesc");	
	    }
	    // if the content was NOT shortened
		// keep the original title and description
	    if (!gj(el[j]).hasClass("shortDesc")) evtCont[0].innerHTML = gj(el[j]).attr("descHTML");
		if (!gj(el[j]).hasClass("shortTitle")) evtBar[0].lastChild.data = gj(el[j]).attr("titleHTML");


	    if (el[j-1]&&(len > 1)) {
			setLeft(el[j],offsetLeft + (parseFloat(el[j-1].style.width) + 1)*n);
	    }
	    else {		
			setLeft(el[j], offsetLeft);
	    }
	    n++ ;
	}
    }

    function setLeft(obj, left){
	    obj.style.left = left + "px";

	    if(base.I18n.isRT()){
	      if(base.Browser.isIE6()) left -= cs.Utils.getScrollbarWidth();
	      obj.style.right = left + "px";
	    }
    }

} ;


UIWeekView.prototype.showInCol = function(obj) {
	_module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
	var items = _module.UICalendarPortlet.getElements(obj) ;
	var len = items.length ;
	if (len <= 0) return ;
	var UIWeekView = _module.UIWeekView ;
	var container = (gj.browser.mozilla != undefined) ? UIWeekView.container : items[0].offsetParent ;
	var left = parseFloat((base.Browser.findPosXInContainer(obj, container) - 1)/container.offsetWidth)*100 ;
	var width = parseFloat((obj.offsetWidth - 2)/container.offsetWidth)*100 ;
	items = _module.UICalendarPortlet.sortByAttribute(items, "startTime") ;
	UIWeekView.adjustWidth(items, obj.offsetWidth, base.Browser.findPosXInContainer(obj, container,base.I18n.isRT())) ;
};


UIWeekView.prototype.dragStart = function(evt) {
	_module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
	eXo.calendar.EventTooltip.disable(evt);
	var _e = evt ;
	_e.stopPropagation();
	//_e.cancelBubble = true ;
	if (_e.button == 2) return ;
	var UIWeekView = _module.UIWeekView ;
	UIWeekView.dragElement = this ;
	_module.UICalendarPortlet.resetZIndex(UIWeekView.dragElement) ;
	UIWeekView.objectOffsetLeft = base.Browser.findPosX(UIWeekView.dragElement) ;
	UIWeekView.offset = UIWeekView.getOffset(UIWeekView.dragElement, _e) ;
	UIWeekView.mouseY = _e.clientY ;
	UIWeekView.mouseX = _e.clientX ;
	UIWeekView.eventY = UIWeekView.dragElement.offsetTop ;
	UIWeekView.containerOffset = {
		"x" : base.Browser.findPosX(UIWeekView.container.parentNode),
		"y" : cs.Browser.Browser.findPosY(UIWeekView.container.parentNode)
	}
	UIWeekView.title = gj(UIWeekView.dragElement).find('div.eventTitle')[0].innerHTML;
	gj(document).on({'mousemove':UIWeekView.drag,'mouseup':UIWeekView.drop});

	_module.UICalendarPortlet.dropCallback = UIWeekView.dropCallback ;
	_module.UICalendarPortlet.setPosition(UIWeekView.dragElement);
} ;

UIWeekView.prototype.drag = function(evt) {
	_module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
  var UICalendarPortlet = _module.UICalendarPortlet;
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var UIWeekView = _module.UIWeekView ;
	var mouseY = base.Browser.findMouseRelativeY(UIWeekView.container,_e) - UIWeekView.container.scrollTop ;
	var posY = UIWeekView.dragElement.offsetTop ;
	var height =  UIWeekView.dragElement.offsetHeight ;
	var deltaY = null ;
	deltaY = _e.clientY - UIWeekView.mouseY ;
	var currentTop =  UIWeekView.mousePos(_e).y - UIWeekView.offset.y - UIWeekView.containerOffset.y;
	var maxTop = UIWeekView.dragElement.offsetParent.scrollHeight - height; 
	if(currentTop >= 0 && currentTop <= maxTop){
		UIWeekView.dragElement.style.top = (currentTop - currentTop%_module.UICalendarPortlet.interval) + "px" ;
		if (UIWeekView.isCol(_e)) {
			var posX = base.Browser.findPosXInContainer(UIWeekView.currentCol, UIWeekView.dragElement.offsetParent) ;
			UIWeekView.dragElement.style.left = posX + "px" ;
		}
	}
	UICalendarPortlet.updateTitle(UIWeekView.dragElement, posY);
	UIWeekView.dragElement.style.width = (UIWeekView.dragElement.parentNode.offsetWidth - 10) + "px";
} ;

UIWeekView.prototype.dropCallback = function() {
	_module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
	var me = _module.UIWeekView ;
	var UICalendarPortlet = _module.UICalendarPortlet;
	var dragElement = me.dragElement ;
	var start = parseInt(gj(dragElement).attr("startTime")) ;
	var end = parseInt(gj(dragElement).attr("endTime")) ;
	var calType = parseInt(gj(dragElement).attr("calType")) ;
	var calId = gj(dragElement).attr("calid");
	var eventId = gj(dragElement).attr("eventid");
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
	var form = gj(dragElement).parents('form')[0]; 
  form.elements[eventId + "startTime"].value = currentStart;
  form.elements[eventId + "finishTime"].value = currentEnd;
  form.elements[eventId + "currentDate"].value = currentDate;
  form.elements[eventId + "isOccur"].value = isOccur;
  form.elements[eventId + "recurId"].value = recurId;
	me.currentCol.appendChild(dragElement) ;
	_module.UICalendarPortlet.setTimeValue(dragElement,currentStart,currentEnd,me.currentCol);
	me.setSize();
	eval(actionLink);
	//eXo.webui.UIForm.submitEvent(eXo.calendar.UICalendarPortlet.portletId + '#' + 'UIWeekView', 'UpdateEvent', '&subComponentId=' + 'UIWeekView' + '&objectId=' + eventId + '&calendarId=' + calId + '&calType=' + calType + '&startTime=' + currentStart + '&finishTime=' + currentEnd + '&currentDate=' + currentDate + '&isOccur=' + isOccur + '&recurId=' + recurId);
	me.cleanUp();
} ;

UIWeekView.prototype.drop = function(evt) {
	gj(document).off("mousemove mouseup");
	var _e = window.event || evt ;
	var UIWeekView = _module.UIWeekView ;
	var isEventbox = UIWeekView.dragElement;
	if (!UIWeekView.isCol(_e) || !isEventbox) return ;
	var currentCol = UIWeekView.currentCol ;
	var sourceCol = UIWeekView.dragElement.parentNode ;
	var eventY = UIWeekView.eventY ;
	if((UIWeekView.mouseY != _e.clientY) || (UIWeekView.mouseX != _e.clientX)) _module.UICalendarPortlet.checkPermission(UIWeekView.dragElement);
	eXo.calendar.EventTooltip.enable();
	return null ;
} ;

UIWeekView.prototype.cleanUp = function(){
	var UIWeekView = _module.UIWeekView ;
	UIWeekView.title = null ;
	UIWeekView.offset = null ;
	UIWeekView.mouseY = null ;
	UIWeekView.mouseX = null ;
	UIWeekView.eventY = null ;
	UIWeekView.objectOffsetLeft = null ;
	UIWeekView.containerOffset = null ;
	UIWeekView.title = null ;
	UIWeekView.dragElement = null ;	
	// fix bug: hide tootip after dragndrop
	gj('div.tooltip-inner').remove();
	gj('div.tooltip-arrow').remove();
};

UIWeekView.prototype.getOffset = function(object, evt) {	
	return {
		"x": (_module.UIWeekView.mousePos(evt).x - base.Browser.findPosX(object)) ,
		"y": (_module.UIWeekView.mousePos(evt).y - cs.Browser.Browser.findPosY(object))
	} ;
} ;

UIWeekView.prototype.isCol = function(evt) {
	var UIWeekView = _module.UIWeekView ;
	if (!UIWeekView.dragElement) return false;
	var Browser = base.Browser ;
	var isIE = (gj.browser.msie != undefined);
	var isDesktop = (document.getElementById("UIPageDesktop"))?true:false ;
	var mouseX = cs.Browser.Browser.findMouseXInPage(evt);
	if(base.I18n.isRT() && (Browser.isIE7() || Browser.isIE6())) mouseX = mouseX - 32; // 32 =  double of scrollbar width
	var len = UIWeekView.cols.length ;
	var colX = 0 ;
	var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
	for(var i = 1 ; i < len ; i ++) {
		colX = base.Browser.findPosX(UIWeekView.cols[i]) ;
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
	_module.UIWeekView.tooltip = tooltip;
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
	var settingTimezone = _module.UICalendarPortlet.settingTimezone;
	delta = delta - timezoneOffset * 60000 + settingTimezone * 60000;
	
	var unit = 15*60*1000;
	delta = parseInt(delta/unit)*unit;
	var tooltip = _module.UIWeekView.tooltip;
	var extraLeft = gj(window).width() - cs.Browser.Browser.findMouseXInPage(evt);
	extraLeft = (extraLeft < tooltip.offsetWidth)? (tooltip.offsetWidth - extraLeft):0;
	tooltip.style.left = cs.Browser.Browser.findMouseXInPage(evt) - extraLeft + "px";
	tooltip.style.top = cs.Browser.Browser.findMouseYInPage(evt) + 20 + "px";
	tooltip.innerHTML = cs.DateTimeFormater.format((new Date(delta)),"ddd, dd/mmm hh:MM TT");
};

UIWeekView.prototype.removeTooltip = function(){
	if(_module.UIWeekView.tooltip){
		gj(_module.UIWeekView.tooltip).remove();
		delete _module.UIWeekView.tooltip;
	}
};

/**
 * resize the event in weekview
 * minimum height is 15px
 */
UIWeekView.prototype.initResize = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = evt ;
	_e.stopPropagation();
	//_e.cancelBubble = true ;
	if(_e.button == 2) return ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	// this : the marker - div tag with class resizeEventContainer
	var eventContainer = gj(this).parents('.eventContainerBorder')[0]; 
	var siblingOfMarker = gj(this).prevAll('div')[0];
	var container = gj("#UIWeekViewGrid").parents('.eventWeekContent')[0];
    gj(container).css({
        '-moz-user-select'   :'none',
        '-o-user-select'     :'none',
        '-khtml-user-select' :'none', /* you could also put this in a class */
        '-webkit-user-select':'none', /* and add the CSS class here instead */
        '-ms-user-select'    :'none',
        'user-select'        :'none'}).bind('selectstart', function(){ return false; });
	var minHeight = 15 ; // minimum height is 15 px
	var interval = _module.UICalendarPortlet.interval ;
	UIResizeEvent.start(_e, siblingOfMarker, eventContainer, container, minHeight, interval) ;
	_module.UICalendarPortlet.dropCallback = _module.UIWeekView.resizeCallback;
	_module.UICalendarPortlet.setPosition(eventContainer);
} ;

/**
 * update event after resizing
 */
UIWeekView.prototype.resizeCallback = function(evt) {
    var UICalendarPortlet = _module.UICalendarPortlet;
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
	var form = gj(eventBox).parents('form')[0];
	form.elements[eventId + "startTime"].value = start;
	form.elements[eventId + "finishTime"].value = end;
	form.elements[eventId + "currentDate"].value = currentDate;
	form.elements[eventId + "isOccur"].value = isOccur;
	form.elements[eventId + "recurId"].value = recurId;
	_module.UICalendarPortlet.setTimeValue(eventBox,start,end);	
	_module.UIWeekView.setSize();	
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
	var outerElement = gj(this).parents('.weekViewEventBoxes')[0];
	var innerElement = gj(outerElement).find('div.eventAlldayContent')[0];
	UIHorizontalResize.start(_e, outerElement, innerElement) ;
	UIHorizontalResize.dragCallback = _module.UIWeekView.rightDragResizeCallback ;
	UIHorizontalResize.callback = _module.UIWeekView.rightResizeCallback ;
	_module.UIWeekView.createTooltip();
} ;

UIWeekView.prototype.initAllDayLeftResize = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	if (_e.button == 2) return ;	
	var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var outerElement = gj(this).parents('.weekViewEventBoxes')[0];
	var innerElement = gj(outerElement).find("div.eventAlldayContent")[0];
	UIHorizontalResize.start(_e, outerElement, innerElement, true) ;
	UIHorizontalResize.dragCallback = _module.UIWeekView.leftDragResizeCallback ;
	UIHorizontalResize.callback = _module.UIWeekView.leftResizeCallback ;
	_module.UIWeekView.createTooltip();
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
	if (document.getElementById("UIPageDesktop") || !base.Browser.isIE6()) {
  	maxX -= 55;
		extraWidth = 55;
  }
	if (maxX >= totalWidth) {
		outer.style.width = (totalWidth - posX - 2 + extraWidth) + "px" ;
		inner.style.width = (totalWidth - posX - 8 + extraWidth) + "px" ;
	}
	var delta = outer.offsetWidth - eXo.calendar.UIHorizontalResize.beforeWidth ;
	_module.UIWeekView.showTooltip(outer,delta,evt);
} ;

UIWeekView.prototype.leftDragResizeCallback = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var outer = eXo.calendar.UIHorizontalResize.outerElement ;
	var left = outer.offsetLeft ;
	var extraWidth = 0;
	if (document.getElementById("UIPageDesktop") || !base.Browser.isIE6()) {
		left -= 55;
		extraWidth = 55;
  }
	if(left == 0) _module.UIWeekView.extraWidth = outer.offsetWidth - 2;
	if (left < 0 ) {		
		outer.style.left = extraWidth + "px" ;
		outer.style.width = _module.UIWeekView.extraWidth + "px" ;
	}
	var delta = eXo.calendar.UIHorizontalResize.beforeWidth - outer.offsetWidth ;
	_module.UIWeekView.showTooltip(outer,delta,evt,true);
} ;

UIWeekView.prototype.rightResizeCallback = function() {
    var UIWeekView = _module.UIWeekView ;
    var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;	
    var outer = UIHorizontalResize.outerElement ;
    var totalWidth = outer.parentNode.offsetWidth;
    var delta = outer.offsetWidth - UIHorizontalResize.beforeWidth ;
    if (delta != 0) {
	var weekdays = parseInt(document.getElementById("UIWeekViewGridAllDay").getAttribute("numberofdays"));
	var UICalendarPortlet = _module.UICalendarPortlet
	var delta = parseInt(delta * (24 * weekdays * 60 * 60 * 1000) / totalWidth);
	var start = parseInt(outer.getAttribute("startTime"));
	var end = parseInt(outer.getAttribute("endTime")) + delta;
	var calType = parseInt(outer.getAttribute("calType"));
	var isOccur = outer.getAttribute("isoccur");
	var eventId = outer.getAttribute("eventid");
	var recurId = outer.getAttribute("recurid");
	if (recurId == "null") recurId = "";
	var actionLink = outer.getAttribute("actionLink");
	var form = gj(outer).parents('form')[0]; 
	form.elements[eventId + "startTime"].value = start;
	form.elements[eventId + "finishTime"].value = end;
	form.elements[eventId + "isOccur"].value = isOccur;
	form.elements[eventId + "recurId"].value = recurId;
	eval(actionLink);
    }
    _module.UIWeekView.removeTooltip();
    eXo.calendar.EventTooltip.enable();
} ;

UIWeekView.prototype.leftResizeCallback = function() {
    var UIWeekView = _module.UIWeekView ;
    var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
    var outer = UIHorizontalResize.outerElement ;
    var totalWidth = outer.parentNode.offsetWidth;
    var delta = UIHorizontalResize.beforeWidth - outer.offsetWidth ;
    if (delta != 0) {
	var weekdays = parseInt(document.getElementById("UIWeekViewGridAllDay").getAttribute("numberofdays"));
	var UICalendarPortlet = _module.UICalendarPortlet
	var delta = Math.round(delta * (24 * weekdays * 60 * 60 * 1000) / totalWidth);
	var start = parseInt(outer.getAttribute("startTime")) + delta;
	var end = parseInt(outer.getAttribute("endTime"));
	var calType = parseInt(outer.getAttribute("calType"));
	var isOccur = outer.getAttribute("isoccur");
	var eventId = outer.getAttribute("eventid");
	var recurId = outer.getAttribute("recurid");
	if (recurId == "null") recurId = "";
	var actionLink = outer.getAttribute("actionLink");
	var form = gj(outer).parents('form')[0]; 
	form.elements[eventId + "startTime"].value = start;
	form.elements[eventId + "finishTime"].value = end;
	form.elements[eventId + "isOccur"].value = isOccur;
	form.elements[eventId + "recurId"].value = recurId;
	eval(actionLink);
    }
    if(_module.UIWeekView.extraWidth) delete _module.UIWeekView.extraWidth;
    _module.UIWeekView.removeTooltip();
    eXo.calendar.EventTooltip.enable();
} ;

// For all day event

UIWeekView.prototype.initAlldayDND = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt ;
	if (_e.button == 2) return ;
	var UIWeekView = _module.UIWeekView ;
	var DragDrop = cs.DragDrop ;
	var EventAllday = gj(this).parents('.eventAllDay')[0];
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
	var UIWeekView = _module.UIWeekView ;
	var dragObject = evt.dragObject ;
	UIWeekView.beforePercentStart = parseFloat(dragObject.style.left) ;
	UIWeekView.beforeStart = dragObject.offsetLeft ;
	dragObject.style.left = dragObject.offsetLeft + "px" ;
} ;

UIWeekView.prototype.allDayDragCallback = function(evt) {
	eXo.calendar.EventTooltip.disable(evt);
	var UIWeekView = _module.UIWeekView ;
	var dragObject = evt.dragObject ;
	dragObject.style.top = UIWeekView.elementTop + "px" ;
	var posX = parseInt(dragObject.style.left) ;
	var is55 = document.getElementById("UIPageDesktop") || !base.Browser.isIE6() ;
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
	var UIWeekView = _module.UIWeekView ;
	var totalWidth = dragObject.parentNode.offsetWidth ;
	var delta = dragObject.offsetLeft - UIWeekView.beforeStart ;
	//if (delta == 0) dragObject.style.left = UIWeekView.beforePercentStart + "%" ;
	UIWeekView.elementLeft = null ;
	UIWeekView.elementTop = null ;
	UIWeekView.beforeStart = null ;
	UIWeekView.beforePercentStart = null ;
	if (delta != 0) {
		var weekdays = parseInt(document.getElementById("UIWeekViewGridAllDay").getAttribute("numberofdays"));
		var UICalendarPortlet = _module.UICalendarPortlet
		var delta = Math.round(delta*(24*weekdays*60*60*1000)/totalWidth) ;
		var start =  parseInt(dragObject.getAttribute("startTime")) + delta ;
		var end = parseInt(dragObject.getAttribute("endTime")) + delta ;
		var calType = parseInt(dragObject.getAttribute("calType")) ;
		var isOccur = dragObject.getAttribute("isoccur");
		var eventId = dragObject.getAttribute("eventid");
		var recurId = dragObject.getAttribute("recurid");
		if (recurId == "null") recurId = "";
		var actionLink = dragObject.getAttribute("actionLink");
		var form = gj(dragObject).parents('form')[0]; 
    form.elements[eventId + "startTime"].value = start;
    form.elements[eventId + "finishTime"].value = end;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
    eval(actionLink);
	}	
	eXo.calendar.EventTooltip.enable();
} ;

UIWeekView.prototype.initAllday = function() {
    var UIWeekView = _module.UIWeekView ;
    var uiWeekView = document.getElementById("UIWeekView") ;
    var uiWeekViewGridAllDay = gj(uiWeekView).find('table.UIGrid')[0]; 
    this.eventAlldayContainer = gj(uiWeekView).find('div.eventAlldayContainer');
    var eventAllday = new Array() ;
    for(var i = 0 ; i < this.eventAlldayContainer.length ; i ++) {
	if (this.eventAlldayContainer[i].style.display != "none") eventAllday.push(this.eventAlldayContainer[i]) ;
    }
    var len = eventAllday.length ;
    if (len <= 0) return ;
    var resizeMark = null ;
    for(var i = 0 ; i < len ; i ++) {
	resizeMark = gj(eventAllday[i]).children("div") ;
	if (gj(resizeMark[0]).hasClass("leftResizeEvent"))
	    gj(resizeMark[0]).on('mousedown',UIWeekView.initAllDayLeftResize);
	if (gj(resizeMark[2]).hasClass("rightResizeEvent")) {
	    gj(resizeMark[2]).on('mousedown',UIWeekView.initAllDayRightResize);
	}
	gj(eventAllday[i]).off('dblclick').on({'mouseover':eXo.calendar.EventTooltip.show,
	    'mouseout':eXo.calendar.EventTooltip.hide,
	    'mousedown':_module.UIWeekView.initAlldayDND,
	    'dblclick':_module.UICalendarPortlet.ondblclickCallback});
    }
    var EventAlldayContainer = gj(uiWeekViewGridAllDay).find('td.eventAllDayContainer')[0]; 
    this.weekdays = gj(uiWeekViewGridAllDay).find('td.uiCellBlock');
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
	return _module.UICalendarPortlet.timeToMin(millisecond) ;
} ;

UIWeekView.prototype.sortEventsInCol = function(events) {
	var index = this.getStartEvent(events) ;
	//events = eXo.calendar.UICalendarPortlet.sortByAttribute(events, "startTime", "dsc") ;
	var len = index.length ;// alert(len) ;
	var tmp = new Array() ;
	for(var i = 0 ; i < len ; i ++) {
		tmp.pushAll(this.setGroup(events, index[i])) ;
	}
	_module.UICalendarPortlet.sortByAttribute(tmp, "startTime") ;
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
	var totalWidth = parseFloat(base.Browser.findPosXInContainer(events[0].parentNode, events[0].offsetParent)/events[0].offsetParent.offsetWidth)*100 ;
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
		events[i].style.top = base.Browser.findPosYInContainer(events[i],events[i].offsetParent) +  i*events[i].offsetHeight + "px" ;
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
		gj(events[i]).on('mousedown',this.initAlldayDND);
//		events[i].onmousedown = this.initAlldayDND;
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
	gj(document).on({'mousemove':eXo.calendar.UIHorizontalResize.execute,
		'mouseup':eXo.calendar.UIHorizontalResize.end});
//	document.onmousemove = eXo.calendar.UIHorizontalResize.execute ;
//	document.onmouseup = eXo.calendar.UIHorizontalResize.end ;
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
	gj(document).off("mousemove mouseup");
//	document.onmousemove = null ;
//	document.onmouseup = null ;
} ;

// For user selection 

UIWeekView.prototype.initSelection = function() {
  var UICalendarPortlet = _module.UICalendarPortlet;
	var UISelection = eXo.calendar.UISelection ;
	var container = document.getElementById("UIWeekViewGrid") ;
	UISelection.step = UICalendarPortlet.CELL_HEIGHT;	
	UISelection.block = document.createElement("div");
	UISelection.block.className = "userSelectionBlock" ;
	UISelection.container = container ;
	gj(container).prevAll('div')[0].appendChild(UISelection.block) ;
	gj(UISelection.container).on('mousedown',UISelection.start);
//	UISelection.container.onmousedown = UISelection.start ;
	UISelection.relativeObject = gj(UISelection.container).parents('.eventWeekContent')[0]; 
	UISelection.viewType = "UIWeekView" ;
} ;

UIWeekView.prototype.initSelectionX = function() {
	var Highlighter = _module.Highlighter ;
	var table = gj("#UIWeekViewGridAllDay")[0] ;
	var cell = gj(table).find('th'); 
	var len = cell.length ;
	var link = null ;
	for(var i = 0 ; i < len ; i ++) {
		link = gj(cell[i]).children('a')[0]
		if (link) 
			gj(link).on('mousedown',_module.UIWeekView.cancelBubble);
//			link.onmousedown = eXo.calendar.UIWeekView.cancelBubble ;
		gj(cell[i]).on('mousedown',_module.Highlighter.start);
//		cell[i].onmousedown = Highlighter.start ;
	}
} ;

UIWeekView.prototype.cancelBubble = function(evt) {
	var _e = evt || window.event ;
	_e.cancelBubble = true ;
} ;

/**
 * Callback executed when click on cell table in WeekView, that opens the QuickAddEvent popup
 */
UIWeekView.prototype.callbackSelectionX = function() {
	var UIHSelection = eXo.calendar.UIHSelection ;
	var startTime = parseInt(Date.parse(UIHSelection.firstCell.getAttribute("startTimeFull"))) ;
	var endTime = parseInt(Date.parse(UIHSelection.lastCell.getAttribute("startTimeFull"))) + 24*60*60*1000 - 1 ;
	var portlet = _module.UICalendarPortlet;
	var container = document.getElementById("UICalendarViewContainer");	
	portlet.addQuickShowHiddenWithTime(container, 1, startTime, endTime) ;
} ;
eXo.calendar.UIHorizontalResize = new UIHorizontalResize() ;
_module.UIWeekView = new UIWeekView() ;
eXo.calendar.UIWeekView = _module.UIWeekView;

return _module;
})(base, cs, gj, UICalendarMan);
