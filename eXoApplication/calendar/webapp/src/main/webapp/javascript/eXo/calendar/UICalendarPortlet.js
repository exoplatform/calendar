
/** 
 * Class to cover common actions of Calendar portlet
 * @author <a href="mailto:dung14000@gmail.com">Hoang Manh Dung</a>
 * @constructor
 */

eXo.require("eXo.webui.UICalendar");
eXo.require('eXo.cs.CSUtils','/csResources/javascript/');
eXo.require('eXo.cs.UIContextMenu','/csResources/javascript/');
eXo.require('eXo.core.JSON');

function UICalendarPortlet(){
	this.clickone = 0 ;
	this.portletId = "calendars";
	this.currentDate = 0;
	this.CELL_HEIGHT = 20;
	this.MINUTE_PER_CELL = 30;
	this.PIXELS_PER_MINUTE = this.CELL_HEIGHT / this.MINUTE_PER_CELL; 
	this.MINUTES_PER_PIXEL = this.MINUTE_PER_CELL / this.CELL_HEIGHT;
}

/**
 * compute minutes from pixels in height of a event.
 */
UICalendarPortlet.prototype.pixelsToMins = function(pixels) {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  return Math.ceil(pixels * UICalendarPortlet.MINUTES_PER_PIXEL);
};

/**
 * compute pixels in height of a event from minutes.
 */
UICalendarPortlet.prototype.minsToPixels = function(minutes) {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  return Math.ceil(minutes * UICalendarPortlet.PIXELS_PER_MINUTE);
};



/**
 * Set stylesheet for a DOM element
 * @param {Object} object DOM Element
 * @param {Object} styles Object contains style name and style value
 */
UICalendarPortlet.prototype.setStyle = function(object, styles){
    for (var value in styles) {
        object.style[value] = styles[value];
    }
} ;

UICalendarPortlet.prototype.attachSwapClass = function(compId,className,hoverClass){
    var component = document.getElementById(compId);
    var items = eXo.core.DOMUtil.findDescendantsByClass(component,"div",className);
    var i = items.length;
    while(i--){
    	eXo.core.EventManager.addEvent(items[i],"mouseover",function(){
    		eXo.cs.Utils.swapClass(this,hoverClass);
    	});
    	eXo.core.EventManager.addEvent(items[i],"mouseout",function(){
    		eXo.cs.Utils.swapClass(this,hoverClass);
    	});
    };
} ;


UICalendarPortlet.prototype.makeRequest = function(url,callback){
	var request =  eXo.core.Browser.createHttpRequest() ;
	request.open('GET', url, false) ;
	request.setRequestHeader("Cache-Control", "max-age=86400") ;
	request.send(null) ;
	if(callback) callback(request.responseText) ;
};

UICalendarPortlet.prototype.notify = function(eventObj){
	var Reminder = eXo.calendar.Reminder ;
	var uiCalendarWorkingContainer = eXo.core.DOMUtil.findAncestorById(eventObj,"UICalendarWorkingContainer");
	var msg = "<div style='padding:3px;color:red;'>" + uiCalendarWorkingContainer.getAttribute("msg") + "</div>";
	var html = Reminder.generateHTML(msg) ;
	var popup = eXo.core.DOMUtil.findFirstDescendantByClass(Reminder.createMessage(html, msg), "div","UIPopupNotification") ;
	eXo.webui.Box.config(popup,popup.offsetHeight, 5, Reminder.openCallback, Reminder.closeBox) ;
	window.focus() ;
	return ;
};

UICalendarPortlet.prototype.getOrginalPosition = function(eventObj){
	if(eventObj.getAttribute("orginalSize")){
		return eventObj.getAttribute("orginalSize");
	}
};

UICalendarPortlet.prototype.setPosition = function(eventObj){
	var DOMUtil = eXo.core.DOMUtil ;
	var me = eXo.calendar.UICalendarPortlet ;
	me.activeEventObject = eventObj ;
	var cTop = DOMUtil.getStyle(eventObj,"top");
	var cLeft = DOMUtil.getStyle(eventObj,"left");
	var cWdith = DOMUtil.getStyle(eventObj,"width");
	var cHeight = DOMUtil.getStyle(eventObj,"height");
	var cTitle = DOMUtil.findFirstDescendantByClass(eventObj,"div","EventTitle").innerHTML ; 
	var cInnerHeight = DOMUtil.getStyle(DOMUtil.findFirstDescendantByClass(eventObj,"div","EventContainer"),"height") ;
	me.restoreTitle = cTitle ;
	me.restoreContainerHeight = cInnerHeight ;
	me.restoreSize = {
		"top": cTop,
		"left": cLeft,
		"width": cWdith,
		"height": cHeight
	};
};

UICalendarPortlet.prototype.restorePosition = function(eventObj){
	this.setStyle(eventObj,this.restoreSize);
	var eventTitle = eXo.core.DOMUtil.findFirstDescendantByClass(eventObj,"div","EventTitle");
	var eventContainer = eXo.core.DOMUtil.findFirstDescendantByClass(eventObj,"div","EventContainer");
	if(this.restoreTitle && eventTitle) eventTitle.innerHTML = this.restoreTitle ;
	if(this.restoreContainerHeight && eventContainer) eventContainer.style.height = this.restoreContainerHeight ;
	this.restoreSize = null ;
	this.activeEventObject = null ;
	this.dropCallback = null ;
	this.restoreTitle = null ;
};

UICalendarPortlet.prototype.postCheck = function(response){
	var me = eXo.calendar.UICalendarPortlet ;
	eval("var data = " + response);
	var isEdit = data.permission;
	if(!isEdit){
		me.notify(me.activeEventObject);		
		me.restorePosition(me.activeEventObject);
	}else{
		if(me.dropCallback) me.dropCallback();
		delete me.activeEventObject ;
		delete me.restoreSize;
	}
};

UICalendarPortlet.prototype.checkPermission = function(eventObj){
	var calId = eventObj.getAttribute("calid");
	var calType = eventObj.getAttribute("calType");
	var baseURL  = (eXo.calendar.restContext)?eXo.env.portal.context+ '/' + eXo.calendar.restContext +'/cs/calendar/checkPermission/':'portal/rest/cs/calendar/checkPermission/';
	var url = baseURL + eXo.cs.CSCometd.exoId +"/"+ calId +"/"+ calType +"/";
	this.makeRequest(url,this.postCheck);
};

/**
 * 
 * @param {Object} calendarForm : calendar form DOM node
 * @return {Object} a checked calendar id
 */
UICalendarPortlet.prototype.getCheckedCalendar = function(calendarForm){
	var checkedCalendars = new Array();
	var chks = calendarForm.elements;
	for(var i = 0 , l = chks.length; i < l; i++){
		if(chks[i].checked && eXo.core.DOMUtil.hasClass(chks[i],"checkbox")) checkedCalendars.push(chks[i]); 
	}
	if(!checkedCalendars || checkedCalendars.length == 0) return null;
	return checkedCalendars[0].name;
};

/**
 * Show Quick add event and task form 
 * @param {obj, type} has action object, type of form : event 1 | task 2
 */
UICalendarPortlet.prototype.addQuickShowHidden = function(obj, type){
  var startTime = (this.currentDate) ? new Date(this.currentDate).getTime() : new Date().getTime();
  this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 15*60*1000) ;
} ;

/**
 * Show Quick add event and task form 
 * @param {obj, type} has action object, type of form : event 1 | task 2 | calendarId selected calendar
 */
UICalendarPortlet.prototype.addQuickShowHiddenWithId = function(obj, type, id){
	var startTime = new Date().getTime() ;
		var calType = (id.match(/calType=\s*\d\s*\&/ig)).toString().match(/\d/ig);
    var id = calType + ":" + (id.match(/calendar[a-zA-Z0-9]+\&/ig)).toString().replace("&","");
    this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 15*60*1000, id) ;
} ;


/**
 * Show Quick add event and task form with selected time
 * @param {obj, type, from, to} has action object, type of form : event 1 | task 2, from in milliseconds, to in milliseconds
 */
UICalendarPortlet.prototype.addQuickShowHiddenWithTime = function(obj, type, fromMilli, toMilli, id){
	var CalendarWorkingWorkspace =  eXo.calendar.UICalendarPortlet.getElementById("UICalendarWorkingContainer");
    var id = (id)?id:this.getCheckedCalendar(this.filterForm);
    var UIQuckAddEventPopupWindow = eXo.core.DOMUtil.findDescendantById(CalendarWorkingWorkspace,"UIQuckAddEventPopupWindow");
    var UIQuckAddTaskPopupWindow = eXo.core.DOMUtil.findDescendantById(CalendarWorkingWorkspace,"UIQuckAddTaskPopupWindow");
    var selectedCategory = (eXo.calendar.UICalendarPortlet.filterSelect) ? eXo.calendar.UICalendarPortlet.filterSelect : null;
	// There is at least 1 event category to show event form
	if((selectedCategory != null) && (selectedCategory.options.length < 1)) {
    	var divEventCategory = eXo.core.DOMUtil.findAncestorByClass(eXo.calendar.UICalendarPortlet.filterSelect, "EventCategory") ;
    	alert(divEventCategory.getAttribute("msg")) ;
    	return;
    }
		
		var tmpMenuElement = document.getElementById("tmpMenuElement");
		if (tmpMenuElement) eXo.webui.UIPopup.hide(tmpMenuElement) ;
    
		var formater = eXo.cs.DateTimeFormater ;
    var data = {
    		from:parseInt(fromMilli),
    		fromTime:parseInt(fromMilli),
    		to:parseInt(toMilli),
    		toTime:parseInt(toMilli),
    		isAllday:false,
    		calendar:id,
    		category:(selectedCategory)? selectedCategory.value : null 
    };
    if(type == 1) {
    	var uiform = eXo.core.DOMUtil.findDescendantById(UIQuckAddEventPopupWindow, "UIQuickAddEvent") ;
    	uiform.reset() ;
    	this.fillData(uiform, data) ;
    	eXo.webui.UIPopupWindow.show(UIQuckAddEventPopupWindow);
    	eXo.webui.UIPopup.hide(UIQuckAddTaskPopupWindow) ;
    } else if(type == 2) {
    	var uiform = eXo.core.DOMUtil.findDescendantById(UIQuckAddTaskPopupWindow, "UIQuickAddTask") ;
    	uiform.reset() ;
    	this.fillData(uiform, data) ;
    	eXo.webui.UIPopupWindow.show(UIQuckAddTaskPopupWindow);
    	eXo.webui.UIPopup.hide(UIQuckAddEventPopupWindow);
    }
} ;
/**
 * fill data to quick event/task form
 * @param {uiform, data} uifrom obj or id, data is array of value for each element of form
 */
UICalendarPortlet.prototype.fillData = function(uiform, data) {
	uiform = (typeof uiform == "string") ? eXo.calendar.UICalendarPortlet.getElementById(uiform):uiform;
	var fromField = uiform.elements["from"] ;
	var fromFieldTime = uiform.elements["fromTime"] ;
	var toField = uiform.elements["to"] ;
	var toFieldTime = uiform.elements["toTime"] ;
	var isAllday = uiform.elements["allDay"] ;
	var calendar = uiform.elements["calendar"]; 
	var category = uiform.elements["category"] ;
	var eventName = uiform.elements["eventName"];
	var description = uiform.elements["description"];
	
	var formater = eXo.cs.DateTimeFormater ;
	var timeType = "HH:MM" ;
	var dateType = fromField.getAttribute("format").replace("MM","mm") ;
	if(this.timeFormat == "hh:mm a")  timeType = formater.masks.shortTime ;
	eventName.value = "";
	description.value = "";
	fromField.value = formater.format(data.from, dateType);
	fromFieldTime.style.visibility= "visible";
	fromFieldTime.value = formater.format(data.fromTime, timeType);	
	eXo.core.DOMUtil.findNextElementByTagName(fromFieldTime,"input").value = formater.format(data.fromTime, timeType);
	toField.value = formater.format(data.to, dateType);
	toFieldTime.style.visibility = "visible";
	toFieldTime.value = formater.format(data.toTime, timeType);
	eXo.core.DOMUtil.findNextElementByTagName(toFieldTime,"input").value = formater.format(data.toTime, timeType);
	isAllday.checked = data.isAllday ;
	if(data.calendar)
		for(i=0; i < calendar.options.length;  i++) {
			var value = calendar.options[i].value ;
			calendar.options[i].selected = (value.match(data.calendar) != null);		   
		}
	else
		for(i=0; i < calendar.options.length;  i++) {			
			calendar.options[i].selected = true;		 
			break;  
		}
	if(data.category != 'all')
		for(i=0; i < category.options.length;  i++) {
			var value = category.options[i].value ;
			category.options[i].selected = (value.match(data.category) != null);		 
		}
	else
		for(i=0; i < category.options.length;  i++) {
			category.options[i].selected = true;
			break;		 
		}
}
/**
 * Convert time from milliseconds to minutes
 * @param {Int} Milliseconds Milliseconds
 */
UICalendarPortlet.prototype.timeToMin = function(milliseconds){
    if (typeof(milliseconds) == "string") milliseconds = parseInt(milliseconds);
    var d = new Date(milliseconds);
    var hour = d.getHours();
    var min = d.getMinutes();
    var min = hour * 60 + min;
    return min;
};

/** 
 * Convert time from minutes to string
 * @param {Int} min  Minutes
 * @param {String} timeFormat  Format string of time
 * @return minutes
 */
UICalendarPortlet.prototype.minToTime = function(min, timeFormat){
    var minutes = min % 60;
    var hour = (min - minutes) / 60;
    if (hour < 10) 
        hour = "0" + hour;
    if (minutes < 10) 
        minutes = "0" + minutes;
    if (eXo.calendar.UICalendarPortlet.timeFormat != "hh:mm a") 
        return hour + ":" + minutes;
    var time = hour + ":" + minutes;
    if (!timeFormat) 
        return time;
    if (hour < 12) 
        time += " " + timeFormat.am;
    else 
        if (hour == 12) 
            time += " " + timeFormat.pm;
        else {
            hour -= 12;
            if (hour < 10) 
                hour = "0" + hour;
            time = hour + ":" + minutes;
            time += " " + timeFormat.pm;
        }
    return time;
};

/**
 * Gets begining day
 * @param {Object} millis Milliseconds
 * @return date object Date object
 */
UICalendarPortlet.prototype.getBeginDay = function(millis){
    var d = new Date(parseInt(millis));
    var date = d.getDate();
    var month = d.getMonth() + 1;
    var year = d.getFullYear();
    var strDate = month + "/" + date + "/" + year + " 00:00:00 AM";
    return Date.parse(strDate);
};

/**
 * Gets difference of two days
 * @param {Object} start Beginning date in milliseconds
 * @param {Object} end Ending date in milliseconds
 * @return Difference of two days
 */
UICalendarPortlet.prototype.dateDiff = function(start, end){
    var start = this.getBeginDay(start);
    var end = this.getBeginDay(end);
    var msDiff = end - start;
    var dateDiff = msDiff / (24 * 60 * 60 * 1000);
    return dateDiff;
};

/**
 * Apply time setting for Calendar portet
 * @param {Object} time Timi in milliseconds
 * @param {Object} settingTimeZone Timezone offset of user setting
 * @param {Object} severTimeZone Timezone offset of server
 */
UICalendarPortlet.prototype.toSettingTime = function(time, settingTimeZone, severTimeZone){
    var GMT = time - (3600000 * serverTimeZone);
    var settingTime = GMT + (3600000 * settingTimeZone);
    return settingTime;
};

/**
 * Gets full year from date object
 * @param {Object} date Date object
 * @return Full year
 */
UICalendarPortlet.prototype.getYear = function(date){
    x = date.getYear();
    var y = x % 100;
    y += (y < 38) ? 2000 : 1900;
    return y;
};

/**
 * Gets day from time in milliseconds
 * @param {Object} milliseconds Time in milliseconds
 * @return Day of week
 */
UICalendarPortlet.prototype.getDay = function(milliseconds){
    var d = new Date(milliseconds);
    var day = d.getDay();
    return day;
};

/**
 * Checks time is beginning of date or not
 * @param {Object} milliseconds Time in milliseconds
 * @return Boolean value
 */
UICalendarPortlet.prototype.isBeginDate = function(milliseconds){
    var d = new Date(milliseconds);
    var hour = d.getHours();
    var min = d.getMinutes();
    if ((hour == 0) && (hour == min)) 
        return true;
    return false;
};

/**
 * Checks time is beginning of week or not
 * @param {Object} milliseconds Time in milliseconds
 * @return Boolean value
 */
UICalendarPortlet.prototype.isBeginWeek = function(milliseconds){
    var d = new Date(milliseconds);
    var day = d.getDay();
    var hour = d.getHours();
    var min = d.getMinutes();
    if ((day == 0) && (hour == 0) && (min == 0)) 
        return true;
    return false;
};

/**
 * Gets number of week in current year
 * @param {Object} now Time in milliseconds
 * @return number of week
 */
UICalendarPortlet.prototype.getWeekNumber = function(now){
    var today = new Date(now);
    var Year = this.getYear(today);
    var Month = today.getMonth();
    var Day = today.getDate();
    var now = Date.UTC(Year, Month, Day + 1, 0, 0, 0);
    var Firstday = new Date();
    Firstday.setYear(Year);
    Firstday.setMonth(0);
    Firstday.setDate(1);
    var then = Date.UTC(Year, 0, 1, 0, 0, 0);
    var Compensation = Firstday.getDay();
    if (Compensation > 3) 
        Compensation -= 4;
    else 
        Compensation += 3;
    var NumberOfWeek = Math.round((((now - then) / 86400000) + Compensation) / 7);
    return NumberOfWeek;
};

UICalendarPortlet.prototype.setTimeValue = function(event, start,end,currentCol){
	event.setAttribute("startTime",start);
	event.setAttribute("endTime",end);
	if(currentCol) event.setAttribute("eventindex",currentCol.getAttribute("eventindex"));
};

/**
 * Gets working days of week from user setting then overrides weekdays property of UICalendarPorlet object
 * @param {Object} weekdays
 */
UICalendarPortlet.prototype.getWorkingdays = function(weekdays){
    this.weekdays = weekdays;
}

/* common method */
/**
 * Apply common setting for portlet
 * @param param1 Time interval in minutes
 * @param param2 User working time in minutes
 * @param param3 User time format
 * @param param4 Portlet id
 */
UICalendarPortlet.prototype.setting = function(){
    // paras 1: time interval, paras 2: working time, paras 3: time format type, paras 4: portletid
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    this.interval = ((arguments.length > 0) && (isNaN(parseInt(arguments[0])) == false)) ? parseInt(arguments[0]) : parseInt(15);
    this.interval = this.minsToPixels(this.interval);
    var workingStart = ((arguments.length > 1) && (isNaN(parseInt(arguments[1])) == false) && (arguments[1] != "null")) ? arguments[1] : "";
    workingStart = Date.parse("1/1/2007 " + workingStart);
    this.workingStart = UICalendarPortlet.timeToMin(workingStart);
    this.timeFormat = (arguments.length > 2) ? (new String(arguments[2])).trim() : null;
    this.portletName = arguments[3];
};

/**
 * Scroll vertical scrollbar to position of active calendar event
 * @param {Object} obj DOM element
 * @param {Object} container DOM element contains all calendar events
 */
UICalendarPortlet.prototype.setFocus = function(){
  if(eXo.calendar.UICalendarPortlet.getElementById("UIWeekView")){
    var obj = eXo.calendar.UICalendarPortlet.getElementById("UIWeekViewGrid") ;
    var container = eXo.core.DOMUtil.findAncestorByClass(obj,"EventWeekContent") ;
  }
  else if(eXo.calendar.UICalendarPortlet.getElementById("UIDayView")){
    var obj = eXo.calendar.UICalendarPortlet.getElementById("UIDayView") ;
		obj = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "EventBoardContainer");
    var container = eXo.core.DOMUtil.findAncestorByClass(obj, "EventDayContainer");
  } else return ;
  var events = eXo.core.DOMUtil.findDescendantsByClass(obj,"div", "EventContainerBorder");
	events = this.getBlockElements(events);
  var len = events.length;
	var scrollTop =  this.timeToMin((new Date()).getTime());
	if(this.workingStart){
		if(len == 0) scrollTop = this.workingStart ;
		else {
			scrollTop = (this.hasEventThrough(scrollTop,events))? scrollTop : this.workingStart ;
		}
	}	
    var lastUpdatedId = obj.getAttribute("lastUpdatedId");
    if (lastUpdatedId && (lastUpdatedId != "null")) {
        for (var i = 0; i < len; i++) {
            if (events[i].getAttribute("eventId") == lastUpdatedId) {
                scrollTop = events[i].offsetTop;
                break;
            }
        }
    }
    container.scrollTop = scrollTop;
};
/**
 * 
 * @param {Object} min minutes
 * @param {Object} events array of calendar events
 */
UICalendarPortlet.prototype.hasEventThrough = function(min,events){
	var start = 0 ;
	var end = 0 ;
	var i = events.length
	while(i--){
		start = parseInt(events[i].getAttribute("startTime")) ;
		end = parseInt(events[i].getAttribute("endTime")) ;
		if((start <= min) && (end >= min)){
			return true ;
		}
	}
	return false;
};

/**
 * Hide a DOM elemnt automatically after interval time
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.autoHide = function(evt){
    var _e = window.event || evt;
    var eventType = _e.type;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    if (eventType == 'mouseout') {
        UICalendarPortlet.timeout = setTimeout("eXo.calendar.UICalendarPortlet.menuElement.style.display='none'", 5000);
    }
    else {
        if (UICalendarPortlet.timeout) 
            clearTimeout(UICalendarPortlet.timeout);
    }
};

/**
 * Show/hide a DOM element
 * @param {Object} obj DOM element
 */
UICalendarPortlet.prototype.showHide = function(obj){
    if (obj.style.display != "block") {
        eXo.core.DOMUtil.cleanUpHiddenElements();
        obj.style.display = "block";
        eXo.core.EventManager.addEvent(obj,"mouseover",eXo.calendar.UICalendarPortlet.autoHide);
        eXo.core.EventManager.addEvent(obj,"mouseout",eXo.calendar.UICalendarPortlet.autoHide);
        eXo.core.DOMUtil.listHideElements(obj);
    }
    else {
        obj.style.display = "none";
    }
};


UICalendarPortlet.prototype.switchLayoutCallback = function(layout,status){
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  var layoutMan = eXo.calendar.LayoutManager;
  var layoutcookie = eXo.core.Browser.getCookie(layoutMan.layoutId);
  UICalendarPortlet.checkLayoutCallback(layoutcookie);
  if(eXo.core.Browser.isFF() && UICalendarPortlet.getElementById("UIWeekView") && (layout == 1)) eXo.calendar.UIWeekView.onResize();
  if(eXo.core.Browser.isFF() && UICalendarPortlet.getElementById("UIMonthView") && (layout == 1)) eXo.calendar.UICalendarMan.initMonth();
};

UICalendarPortlet.prototype.checkLayoutCallback = function(layoutcookie){
  var CalendarLayout = eXo.calendar.CalendarLayout;
  CalendarLayout.updateCalendarContainerLayout();
  CalendarLayout.updateMiniCalendarLayout();
  CalendarLayout.updateUICalendarsLayout();
};

UICalendarPortlet.prototype.resetSpaceDefaultLayout = function(){
  eXo.calendar.UICalendarPortlet.switchLayout(1);
};

UICalendarPortlet.prototype.resetLayoutCallback = function(){
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  if(UICalendarPortlet.isSpace != "null") {
    UICalendarPortlet.resetSpaceDefaultLayout();
    return;
  }
  var layoutMan = eXo.calendar.LayoutManager;
  var layoutcookie = eXo.core.Browser.getCookie(layoutMan.layoutId);
  UICalendarPortlet.checkLayoutCallback(layoutcookie);
  if(eXo.core.Browser.isFF() && eXo.calendar.UICalendarPortlet.getElementById("UIWeekView")) eXo.calendar.UIWeekView.onResize();
  if(eXo.core.Browser.isFF() && eXo.calendar.UICalendarPortlet.getElementById("UIMonthView")) eXo.calendar.UICalendarMan.initMonth();  
};

/**
 * Check layout configuration when page load to render a right layout
 */
UICalendarPortlet.prototype.checkLayout = function(){
	if(eXo.calendar.UICalendarPortlet.isSpace != "null") eXo.core.Browser.setCookie(eXo.calendar.LayoutManager.layoutId,"1",1);
	eXo.calendar.LayoutManager.layouts = [] ;
	eXo.calendar.LayoutManager.switchCallback = eXo.calendar.UICalendarPortlet.switchLayoutCallback;
	eXo.calendar.LayoutManager.resetCallback = eXo.calendar.UICalendarPortlet.resetLayoutCallback;
	eXo.calendar.LayoutManager.check();
};

/** 
 * Switch among types of layout
 * @param {Int} layout Layout value in order number
 */
UICalendarPortlet.prototype.switchLayout = function(layout){
	var layoutMan = eXo.calendar.LayoutManager ;
	if(layout == 0){
		layoutMan.reset(); 
		return ;
	}
	layoutMan.switchLayout(layout);
};
/* for event */
/**
 * Initialize some properties in Day view
 */
UICalendarPortlet.prototype.init = function(){
    try {
        var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
        var uiDayViewGrid = eXo.calendar.UICalendarPortlet.getElementById("UIDayViewGrid");
        if (!uiDayViewGrid) 
            return false;
        UICalendarPortlet.viewer = eXo.core.DOMUtil.findFirstDescendantByClass(uiDayViewGrid, "div", "EventBoardContainer");
        UICalendarPortlet.step = 60;
    } 
    catch (e) {
        window.status = " !!! Error : " + e.message;
        return false;
    }
    return true;
};

/**
 * Get all event element
 * @param {Object} viewer DOM element contains all calendar events
 * @return All event from container
 */
UICalendarPortlet.prototype.getElements = function(viewer){
    var className = (arguments.length > 1) ? arguments[1] : "EventContainerBorder";
    var elements = eXo.core.DOMUtil.findDescendantsByClass(viewer, "div", className);
    var len = elements.length;
    var elems = new Array();
    for (var i = 0; i < len; i++) {
        if (elements[i].style.display != "none") {
            elements[i].style.left = "0%";
            elements[i].style.zIndex = 1;
            elems.push(elements[i]);
        }
    }
    return elems;
};

/**
 * Checks a DOM element is visible or hidden
 * @param {Object} obj DOM element
 * @return Boolean value
 */
UICalendarPortlet.prototype.isShow = function(obj){
    if (obj.style.display != "none") 
        return true;
    return false;
};

/**
 * Gets all visible event element
 * @param {Object} elements All calendar event
 * @return An array of DOM element
 */
UICalendarPortlet.prototype.getBlockElements = function(elements){
    var el = new Array();
    var len = elements.length;
    for (var i = 0; i < len; i++) {
        if (this.isShow(elements[i])) 
            el.push(elements[i]);
    }
    return el;
};

/**
 * Sets size for a DOM element that includes height and top properties
 * @param {Object} obj Calendar event element
 */
UICalendarPortlet.prototype.setSize = function(obj){
	var domUtil = eXo.core.DOMUtil;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var start = parseInt(obj.getAttribute("startTime"));
  var topY = UICalendarPortlet.minsToPixels(start);
    var end = parseInt(obj.getAttribute("endTime"));
    var eventContainer = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "EventContainer");
    if (end == 0) 
        end = 1440;
    end = (end != 0) ? end : 1440;
    height = UICalendarPortlet.minsToPixels(Math.abs(start - end));
    if (height <= UICalendarPortlet.CELL_HEIGHT) {
      height = UICalendarPortlet.CELL_HEIGHT;
    } 
    var styles = {
        "top": topY + "px",
        "height": (height - 2) + "px"
    };
  UICalendarPortlet.setStyle(obj, styles);
	var busyIcon = domUtil.getChildrenByTagName(obj,"div")[0] ;
	if(!busyIcon ||  (busyIcon.offsetHeight <= 5)) busyIcon = domUtil.findFirstDescendantByClass(obj,"div","EventContainerBar") ;
	var extraHeight = busyIcon.offsetHeight + domUtil.findFirstDescendantByClass(obj,"div","ResizeEventContainer").offsetHeight;
    height -= (extraHeight + 5);
    if (height <= 0) 
      eventContainer.style.display = "none";
    else {
      eventContainer.style.height = height + "px";
      eventContainer.style.display = "block";
    }
};

/**
 * Sets width for a DOM element in percent unit
 * @param {Object} element A DOM element
 * @param {Object} width Width of element
 */
UICalendarPortlet.prototype.setWidth = function(element, width){
    element.style.width = width + "%";
};

/**
 * Gets starttime and endtime attribute of a calendar event element
 * @param {Object} el A calendar event element
 * @return A array includes two elements that are start and end time
 */
UICalendarPortlet.prototype.getSize = function(el){
    var start = parseInt(el.getAttribute("startTime"));
    var end = parseInt(el.getAttribute("endTime"));
		var delta = end - start ;
		if(delta < 30) end = start + 30 ;
    return [start, end];
};

/**
 * Gets interval time from a array of event elements
 * @param {Object} el Array of calendar events
 * @return An array of intervals
 */
UICalendarPortlet.prototype.getInterval = function(el){
    var bottom = new Array();
    var interval = new Array();
    var size = null;
    if (!el || (el.length <= 0)) 
        return;
    for (var i = 0; i < el.length; i++) {
        size = this.getSize(el[i]);
        bottom.push(size[1]);
        if (bottom[i - 1] && (size[0] > bottom[i - 1])) {
            interval.push(i);
        }
    }
    
    interval.unshift(0);
    interval.push(el.length);
    return interval;
};

/**
 * Sets dimension for event elements
 * @param {Object} el An array of calendar events
 * @param {Object} totalWidth Width of calendar event container
 */
UICalendarPortlet.prototype.adjustWidth = function(el, totalWidth){
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
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
                width = parseFloat((totalWidth + left - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width)) / len);
            }
            else {
                width = parseFloat(totalWidth / len);
            }
            UICalendarPortlet.setWidth(el[j], width);
            if (el[j - 1] && (len > 1)) 
//                el[j].style.left = offsetLeft + parseFloat(el[j - 1].style.width) * n + "%";
//                el[j].style.right = offsetLeft + parseFloat(el[j - 1].style.width) * n + "%";
                  setLeft(el[j],offsetLeft + parseFloat(el[j - 1].style.width) * n);
            else {
//                el[j].style.left = offsetLeft + "%";
//                el[j].style.right = offsetLeft + "%";
                  setLeft(el[j],offsetLeft);
            }
            n++;
        }
    }
    function setLeft(obj,left){
		obj.style.left = left + "%";
		if(eXo.core.I18n.isRT()){
			obj.style.right = left + "%";	
		}
	}
};
/**
 * Sort event elemnents in time table
 */
UICalendarPortlet.prototype.showEvent = function(){
    this.init();
    var EventDayContainer = eXo.core.DOMUtil.findAncestorByClass(this.viewer, "EventDayContainer");
	if (!EventDayContainer) return ;
//    this.setFocus(this.viewer, EventDayContainer);
    this.editAlldayEvent(EventDayContainer);
    if (!this.init()) 
        return;
    this.viewType = "UIDayView";
    var el = this.getElements(this.viewer);
    el = this.sortByAttribute(el, "startTime");
    if (el.length <= 0) 
        return;
    var marker = null;
    for (var i = 0; i < el.length; i++) {
        this.setSize(el[i]);
        eXo.core.EventManager.addEvent(el[i],"mouseover",eXo.calendar.EventTooltip.show);
				eXo.core.EventManager.addEvent(el[i],"mouseout",eXo.calendar.EventTooltip.hide);
				eXo.core.EventManager.addEvent(el[i],"mousedown",eXo.calendar.UICalendarPortlet.initDND);
        eXo.core.EventManager.addEvent(el[i],"dblclick",eXo.calendar.UICalendarPortlet.ondblclickCallback);
        marker = eXo.core.DOMUtil.findFirstChildByClass(el[i], "div", "ResizeEventContainer");
        eXo.core.EventManager.addEvent(marker,"mousedown",eXo.calendar.UIResizeEvent.init);
    }
    this.items = el;
    this.adjustWidth(this.items);
    this.items = null;
    this.viewer = null;
};

UICalendarPortlet.prototype.editAlldayEvent = function(cont){
	cont = eXo.core.DOMUtil.findPreviousElementByTagName(cont,"div");
	var events = eXo.core.DOMUtil.findDescendantsByClass(cont,"div","EventContainerBorder");
	var i = events.length ;
	if(!events || (i <= 0)) return ;
	while(i--){
		events[i].ondblclick = this.ondblclickCallback;
	}
}


/**
 * Deal with incorrect event sorting when portlet loads in the first times
 */
UICalendarPortlet.prototype.onLoad = function(){	
    window.setTimeout("eXo.calendar.UICalendarPortlet.checkFilter() ;", 2000);
};

/**
 * Callback method when browser resizes
 */
UICalendarPortlet.prototype.browserResizeCallback = function(){
    if (!eXo.calendar.UICalendarPortlet.items) 
        return;
    eXo.calendar.UICalendarPortlet.adjustWidth(eXo.calendar.UICalendarPortlet.items);
}

/**
 * Callback method when double click on a calendar event
 */
UICalendarPortlet.prototype.ondblclickCallback = function(){
    var eventId = this.getAttribute("eventId");
    var calendarId = this.getAttribute("calid");
    var calendarType = this.getAttribute("caltype");
	var isOccur = this.getAttribute("isOccur");
	var recurId = this.getAttribute("recurId");
	if (recurId == "null") recurId = "";
    eXo.webui.UIForm.submitEvent(eXo.calendar.UICalendarPortlet.portletId + '#' + eXo.calendar.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + eXo.calendar.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType + '&isOccur=' + isOccur + '&recurId=' + recurId);
}

/**
 * Sorts calendar event by their attribute
 * @param {Object} obj An array of calendar events
 * @param {Object} attribute A attribute to sort
 * @return An sorted array of calendar event
 */
UICalendarPortlet.prototype.sortByAttribute = function(obj, attribute){
    var len = obj.length;
    var tmp = null;
    var attribute1 = null;
    var attribute2 = null;
    for (var i = 0; i < len; i++) {
        for (var j = i + 1; j < len; j++) {
            attribute1 = parseInt(obj[i].getAttribute(attribute));
            attribute2 = parseInt(obj[j].getAttribute(attribute));
            if (attribute2 < attribute1) {
                tmp = obj[i];
                obj[i] = obj[j];
                obj[j] = tmp;
            }
            if (attribute2 == attribute1) {
                var end1 = parseInt(obj[i].getAttribute("endTime"));
                var end2 = parseInt(obj[j].getAttribute("endTime"));
                if (end2 > end1) {
                    tmp = obj[i];
                    obj[i] = obj[j];
                    obj[j] = tmp;
                }
            }
        }
    }
    return obj;
};
/* for resizing event box */
/**
 * Class to control calendar event resizing
 * @constructor
 */
function UIResizeEvent(){

}

/**
 * Initilize some propertis of UIResizeEvent
 * @param {Object} evt Mouse event
 */
UIResizeEvent.prototype.init = function(evt){
  var _e = window.event || evt;
  _e.cancelBubble = true;
  var UIResizeEvent = eXo.calendar.UIResizeEvent;
  var outerElement = eXo.core.DOMUtil.findAncestorByClass(this, 'EventBoxes');
  var innerElement = eXo.core.DOMUtil.findPreviousElementByTagName(this, "div");
  var container = eXo.core.DOMUtil.findAncestorByClass(outerElement, 'EventDayContainer');
  var minHeight = 15;
  var interval = eXo.calendar.UICalendarPortlet.interval;
  UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval);
    //UIResizeEvent.callback = UIResizeEvent.resizeCallback;
	eXo.calendar.UICalendarPortlet.dropCallback = UIResizeEvent.resizeCallback;
	eXo.calendar.UICalendarPortlet.setPosition(outerElement);
	eXo.calendar.EventTooltip.disable(evt);
};

UIResizeEvent.prototype.getOriginalHeight = function(obj){
	var domUtil = eXo.core.DOMUtil;
	var paddingTop = domUtil.getStyle(obj,"paddingTop",true);
	var paddingBottom = domUtil.getStyle(obj,"paddingBottom",true);
	var originalHeight = obj.offsetHeight - (paddingTop + paddingBottom);
	return originalHeight;
}

/**
 * Sets up calendar event resizing when mouse down on it
 * @param {Object} evt Mouse event
 * @param {Object} innerElement DOM element before maker element
 * @param {Object} outerElement DOM element after maker element
 * @param {Object} container DOM element contains all events
 * @param {Object} minHeight Minimum height to resize
 * @param {Object} interval Resizing step( default is 30 minutes)
 */
UIResizeEvent.prototype.start = function(evt, innerElement, outerElement, container, minHeight, interval){
    var _e = window.event || evt;
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    this.innerElement = innerElement;
    this.outerElement = outerElement;
    this.container = container;
    eXo.calendar.UICalendarPortlet.resetZIndex(this.outerElement);
    this.minHeight = (minHeight) ? parseInt(minHeight) : 15;
    this.interval = (interval != "undefined") ? parseInt(interval) : 15;
    document.onmousemove = UIResizeEvent.execute;
    document.onmouseup = UIResizeEvent.end;
    this.beforeHeight = this.getOriginalHeight(this.outerElement);
    this.innerElementHeight = this.getOriginalHeight(this.innerElement);
    this.posY = _e.clientY;
    this.uppermost = outerElement.offsetTop + minHeight - container.scrollTop;
    if (document.getElementById("UIPageDesktop")) {
        var uiWindow = eXo.core.DOMUtil.findAncestorByClass(container, "UIResizableBlock");
        this.uppermost -= uiWindow.scrollTop;
    }
};

/**
 * Executes calendar event resizing
 * @param {Object} evt Mouse event
 */
UIResizeEvent.prototype.execute = function(evt){
		eXo.calendar.EventTooltip.disable(evt);
    var _e = window.event || evt;
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    var mouseY = eXo.core.Browser.findMouseRelativeY(UIResizeEvent.container, _e);
    var mDelta = _e.clientY - UIResizeEvent.posY;
    if (mouseY <= UIResizeEvent.uppermost) {
        return;
    }
    else {
    	var maxDelta = 1440 - UIResizeEvent.outerElement.offsetTop - UIResizeEvent.beforeHeight;
    	if(mDelta > maxDelta) {
    		return;
    	}
        if (mDelta % UIResizeEvent.interval == 0) {
            UIResizeEvent.outerElement.style.height = UIResizeEvent.beforeHeight - 2 + mDelta + "px";
            UIResizeEvent.innerElement.style.height = UIResizeEvent.innerElementHeight + mDelta + "px";
            
        }
    }
		var min = (eXo.core.Browser.isIE6())?(UIResizeEvent.outerElement.offsetTop - 1) : UIResizeEvent.outerElement.offsetTop;
		eXo.calendar.UICalendarPortlet.updateTitle(UIResizeEvent.outerElement, UIResizeEvent.outerElement.offsetTop, 1);
};

/**
 * End calendar event resizing, this method clean up some unused properties and execute callback function
 * @param {Object} evt Mouse event
 */
UIResizeEvent.prototype.end = function(evt){
	document.onmousemove = null;
  document.onmouseup = null;
  var _e = window.event || evt;
  var UIResizeEvent = eXo.calendar.UIResizeEvent;
	eXo.calendar.UICalendarPortlet.checkPermission(UIResizeEvent.outerElement) ;
	eXo.calendar.EventTooltip.enable();
};

/**
 * Resizing callback method
 * @param {Object} evt Mouse object
 */
UIResizeEvent.prototype.resizeCallback = function(evt){
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    var eventBox = UIResizeEvent.outerElement;
    var start = parseInt(eventBox.getAttribute("startTime"));
    var calType = eventBox.getAttribute("calType");
    var isOccur = eventBox.getAttribute("isoccur");
    var eventId = eventBox.getAttribute("eventid");
    var recurId = eventBox.getAttribute("recurid");
    if (recurId == "null") recurId = "";
    var end = start + UICalendarPortlet.pixelsToMins(eventBox.offsetHeight);
    if (eventBox.offsetHeight != UIResizeEvent.beforeHeight) {
		var actionLink = eventBox.getAttribute("actionLink");
		var form = eXo.core.DOMUtil.findAncestorByTagName(eventBox,"form");
	  form.elements[eventId + "startTime"].value = start;
	  form.elements[eventId + "finishTime"].value = end;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
		UICalendarPortlet.setTimeValue(eventBox,start,end);
		UICalendarPortlet.showEvent();
		eval(actionLink);
    }
	UIResizeEvent.innerElement = null;
    UIResizeEvent.outerElement = null;
    UIResizeEvent.posY = null;
    UIResizeEvent.minHeight = null;
    UIResizeEvent.interval = null;
    UIResizeEvent.innerElementHeight = null;
    UIResizeEvent.outerElementHeight = null;
    UIResizeEvent.container = null;
    UIResizeEvent.innerElementHeight = null;
    UIResizeEvent.beforeHeight = null;
    UIResizeEvent.posY = null;
    UIResizeEvent.uppermost = null;
};

/* for drag and drop */
/**
 * Resets z-Index of DOM element when drag and drop calendar event
 * @param {Object} obj DOM element
 */
UICalendarPortlet.prototype.resetZIndex = function(obj){
    try {
        var maxZIndex = parseInt(obj.style.zIndex);
        var items = eXo.core.DOMUtil.getChildrenByTagName(obj.parentNode, "div");
        var len = items.length;
        for (var i = 0; i < len; i++) {
            if (!items[i].style.zIndex) 
                items[i].style.zIndex = 1;
            if (parseInt(items[i].style.zIndex) > maxZIndex) {
                maxZIndex = parseInt(items[i].style.zIndex);
            }
        }
        obj.style.zIndex = maxZIndex + 1;
    } 
    catch (e) {
        //alert(e.message) ;
    }
};
/**
 * Initializes drag and drop actions
 * @param {Object} evt Mouse event
 */

UICalendarPortlet.prototype.initDND = function(evt){
	eXo.calendar.EventTooltip.disable(evt);
	var _e = window.event || evt;
  eXo.core.EventManager.cancelBubble(evt);
	if(eXo.core.EventManager.getMouseButton(evt) == 2) return ;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    UICalendarPortlet.dragObject = this;
    UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject);
    UICalendarPortlet.dragContainer = eXo.core.DOMUtil.findAncestorByClass(UICalendarPortlet.dragObject, "EventDayContainer");
    UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject);
    UICalendarPortlet.eventY = _e.clientY;
    UICalendarPortlet.eventTop = UICalendarPortlet.dragObject.offsetTop;
    UICalendarPortlet.dragContainer.onmousemove = UICalendarPortlet.dragStart;
    UICalendarPortlet.dragContainer.onmouseup = UICalendarPortlet.dragEnd;
    UICalendarPortlet.title = eXo.core.DOMUtil.findDescendantsByTagName(UICalendarPortlet.dragObject, "p")[0].innerHTML;
	UICalendarPortlet.dropCallback = UICalendarPortlet.dayviewDropCallback;
	UICalendarPortlet.setPosition(UICalendarPortlet.dragObject);
};
/**
 * Processes when dragging object
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.dragStart = function(evt){
    var _e = window.event || evt;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var delta = null;
    var mouseY = eXo.core.Browser.findMouseRelativeY(UICalendarPortlet.dragContainer, _e) + UICalendarPortlet.dragContainer.scrollTop;
    var posY = UICalendarPortlet.dragObject.offsetTop;
    var height = UICalendarPortlet.dragObject.offsetHeight;
    if (mouseY <= posY) {
        UICalendarPortlet.dragObject.style.top = parseInt(UICalendarPortlet.dragObject.style.top) - UICalendarPortlet.interval + "px";
    }
    else {
        if (mouseY >= (posY + height)) {
            UICalendarPortlet.dragObject.style.top = parseInt(UICalendarPortlet.dragObject.style.top) + UICalendarPortlet.interval + "px";
        }
        else {
            delta = _e.clientY - UICalendarPortlet.eventY;
            if (delta % UICalendarPortlet.interval == 0) {
                var top = UICalendarPortlet.eventTop + delta;
                UICalendarPortlet.dragObject.style.top = top + "px";
            }
        }
		}
    UICalendarPortlet.updateTitle(UICalendarPortlet.dragObject, posY);
};
/**
 * Updates title of event when dragging calendar event
 * @param {Object} events DOM elemnt contains a calendar event
 * @param {Object} posY Position of the event
 */
UICalendarPortlet.prototype.updateTitle = function(events, posY, type){
  var min = this.pixelsToMins(posY);
    var timeFormat = events.getAttribute("timeFormat");
    var title = eXo.core.DOMUtil.findDescendantsByTagName(events, "p")[0];
		var delta = parseInt(events.getAttribute("endTime")) - parseInt(events.getAttribute("startTime")) ;
    timeFormat = (timeFormat) ? eval(timeFormat) : {
        am: "AM",
        pm: "PM"
    };
		if (type == 1) {
			title.innerHTML = this.minToTime(min, timeFormat) + " - " + this.minToTime(min + this.pixelsToMins(events.offsetHeight), timeFormat);
			return ;
		}	
    title.innerHTML = this.minToTime(min, timeFormat) + " - " + this.minToTime(min + delta, timeFormat);
}

/**
 * End calendar event dragging, this method clean up some unused properties and execute callback function
 */

UICalendarPortlet.prototype.dragEnd = function(){
	this.onmousemove = null;
	var me = eXo.calendar.UICalendarPortlet;
	var dragObject = me.dragObject;
	var eventTop = me.eventTop ;
	if (dragObject.offsetTop != eventTop) {
		me.checkPermission(dragObject);
	}
	eXo.calendar.EventTooltip.enable();
};

UICalendarPortlet.prototype.dayviewDropCallback = function(){
    //this.onmousemove = null;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var dragObject = UICalendarPortlet.dragObject;
    var calType = dragObject.getAttribute("calType");
    var start = parseInt(dragObject.getAttribute("startTime"));
    var end = parseInt(dragObject.getAttribute("endTime"));
    var isOccur = dragObject.getAttribute("isoccur");
    var eventId = dragObject.getAttribute("eventid");
    var recurId = dragObject.getAttribute("recurid");
    if (recurId == "null") recurId = "";
    var title = eXo.core.DOMUtil.findDescendantsByTagName(dragObject, "p")[0];
    var titleName = UICalendarPortlet.title;
    if (end == 0) 
        end = 1440;
    var delta = end - start;
    var currentStart = UICalendarPortlet.pixelsToMins(dragObject.offsetTop);
    var currentEnd = currentStart + delta;
    var eventDayContainer = eXo.core.DOMUtil.findAncestorByClass(dragObject, "EventDayContainer");
    //var eventTop = UICalendarPortlet.eventTop;
    eventDayContainer.onmousemove = null;
    eventDayContainer.onmouseup = null;
    UICalendarPortlet.dragObject = null;
    UICalendarPortlet.eventTop = null;
    UICalendarPortlet.eventY = null;
    UICalendarPortlet.dragContainer = null;
    UICalendarPortlet.title = null;
    //if (dragObject.offsetTop != eventTop) {
    var actionLink = dragObject.getAttribute("actionLink");    
    var form = eXo.core.DOMUtil.findAncestorByTagName(dragObject,"form");
    form.elements[eventId + "startTime"].value = currentStart;
    form.elements[eventId + "finishTime"].value = currentEnd;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
		eXo.calendar.UICalendarPortlet.setTimeValue(dragObject,currentStart,currentEnd);
		eXo.calendar.UICalendarPortlet.showEvent();
		eval(actionLink);
    //}
    //title.innerHTML = titleName;
};

/* for showing context menu */
/**
 * Sets up context menu for Calendar portlet
 * @param {Object} compid Portlet id
 */
UICalendarPortlet.prototype.showContextMenu = function(compid){
    var UIContextMenu = eXo.webui.UIContextMenu;
		this.portletNode = eXo.core.DOMUtil.findAncestorByClass(document.getElementById(compid),"PORTLET-FRAGMENT");
    this.portletName = compid;
    UIContextMenu.portletName = this.portletName;
    var config = {
        'preventDefault': false,
        'preventForms': false
    };
    UIContextMenu.init(config);
    UIContextMenu.attach("CalendarContentNomal", "UIMonthViewRightMenu");
    UIContextMenu.attach("EventOnDayContent", "UIMonthViewEventRightMenu");
    UIContextMenu.attach("TimeRule", "UIDayViewRightMenu");
    UIContextMenu.attach("EventBoxes", "UIDayViewEventRightMenu");
    UIContextMenu.attach(["Weekday","Weekend","Today", "EventAlldayContainer"], "UIWeekViewRightMenu");
    UIContextMenu.attach("UIListViewRow", "UIListViewEventRightMenu");
    if(document.getElementById("UIPageDesktop")) this.firstRun = false ;
    this.fixIE();
};

/**
 * Fixs relative positioning problems in IE
 */
UICalendarPortlet.prototype.fixIE = function(){
    var isDesktop = document.getElementById("UIPageDesktop");
    if ((eXo.core.Browser.browserType == "ie") && isDesktop) {
        var portlet = this.portletNode;
        var uiResizeBlock = eXo.core.DOMUtil.findAncestorByClass(portlet, "UIResizableBlock");
        var relative = eXo.core.DOMUtil.findFirstDescendantByClass(uiResizeBlock, "div", "FixIE");
        if (!relative) 
            return;
        relative.className = "UIResizableBlock";
        var style = {
            position: "relative",
            height: uiResizeBlock.offsetHeight + 'px',
            width: "100%",
            overflow: "auto"
        };
        this.setStyle(relative, style);
    }
};

/**
 * Callback method when right click in list view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.listViewCallack = function(evt){
    var _e = window.event || evt;
    var src = _e.srcElement || _e.target;
    if (!eXo.core.DOMUtil.hasClass(src, "UIListViewRow")) 
        src = eXo.core.DOMUtil.findAncestorByClass(src, "UIListViewRow");
    var eventId = src.getAttribute("eventid");
    var calendarId = src.getAttribute("calid");
    var calType = src.getAttribute("calType");
    var isOccur = src.getAttribute("isOccur");
    var recurId = src.getAttribute("recurId");
    map = {
        "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
        "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
        "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType,
        "isOccur\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "isOccur=" + isOccur,
        "recurId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "recurId=" + recurId
    };
    eXo.webui.UIContextMenu.changeAction(eXo.webui.UIContextMenu.menuElement, map);
};

/**
 * Callback method when right click in day view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.dayViewCallback = function(evt){
    var _e = window.event || evt;
    var src = _e.srcElement || _e.target;
   
     
    var map = null;
    if (src.nodeName == "TD") {
        src = eXo.core.DOMUtil.findAncestorByTagName(src, "tr");
        var startTime = parseInt(Date.parse(src.getAttribute('startFull')));
    	/*var endTime = parseInt(Date.parse(DOMUtil.findAncestorByTagName(src, "td").getAttribute('startFull')))  + 24*60*60*1000 - 1;
    	
        var startTime = parseInt(src.getAttribute("startTime"));*/
        var endTime = startTime + 15*60*1000 ;
        var items = eXo.core.DOMUtil.findDescendantsByTagName(eXo.webui.UIContextMenu.menuElement, "a");
        for(var i = 0; i < items.length; i++){
        	var aTag = items[i];
        	if(eXo.core.DOMUtil.hasClass(aTag, "QuickAddEvent")) {
        		aTag.href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");" 
        	} else if(eXo.core.DOMUtil.hasClass(aTag, "QuickAddTask")) {
        		aTag.href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");"
        	}
        }
        /*map = {
            "startTime\s*=\s*.*(?=&|'|\")": "startTime=" + startTime
        };*/
    }
    else {
        src = (eXo.core.DOMUtil.hasClass(src, "EventBoxes")) ? src : eXo.core.DOMUtil.findAncestorByClass(src, "EventBoxes");
        var eventId = src.getAttribute("eventid");
        var calendarId = src.getAttribute("calid");
        var calType = src.getAttribute("calType");
        var isOccur = src.getAttribute("isOccur");
        var recurId = src.getAttribute("recurId");
        if (recurId == "null") recurId = "";
        map = {
            "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
            "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
            "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType
        };
        if (isOccur) {
          map = {
              "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
              "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
              "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType,
              "isOccur\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "isOccur=" + isOccur,
              "recurId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "recurId=" + recurId
          };
        }
        
    }
    eXo.webui.UIContextMenu.changeAction(eXo.webui.UIContextMenu.menuElement, map);
};

/**
 * Callback method when right click in week view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.weekViewCallback = function(evt){
    var src = eXo.core.EventManager.getEventTarget(evt);
    var DOMUtil = eXo.core.DOMUtil;
    var UIContextMenu = eXo.webui.UIContextMenu;
    var map = null;
    var obj = eXo.core.EventManager.getEventTargetByClass(evt,"WeekViewEventBoxes");
    var items = DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement, "a");
    if (obj) {
				var eventId = obj.getAttribute("eventid");
        var calendarId = obj.getAttribute("calid");
        var calType = obj.getAttribute("calType");
        var isOccur = obj.getAttribute("isOccur");
        var recurId = obj.getAttribute("recurId");
        if (recurId == "null") recurId = "";
        map = {
            "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
            "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId
        };
        if (calType) {
            map = {
                "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
                "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
                "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType
            };
            if (isOccur) {
              map = {
                  "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
                  "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
                  "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType,
                  "isOccur\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "isOccur=" + isOccur,
                  "recurId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "recurId=" + recurId
              };
            }
        }
        
		if(!DOMUtil.hasClass(obj,"EventAlldayContainer")){
			var container = DOMUtil.findAncestorByClass(src,"EventWeekContent");
			var mouseY = (eXo.core.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
			obj =parseInt(DOMUtil.findAncestorByTagName(src, "td").getAttribute("startTime")) + mouseY;
		} else obj = null;
        for (var i = 0; i < items.length; i++) {
            if (DOMUtil.hasClass(items[i].parentNode,"EventActionMenu")) {
                items[i].parentNode.style.display = "block";
                items[i].href = UIContextMenu.replaceall(String(items[i].href), map);
            }
            else {
                //TODO Menu on allday events
            	//items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/, "startTime=" + obj);
                /*var fTime = parseInt(obj);
                var tTime = fTime + 15*60*1000 ;*/
        		if(DOMUtil.hasClass(items[i],"QuickAddEvent")){
        			items[i].style.display="none" ;
        			/*items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 1,"+fTime+","+tTime+");"
        			 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 1);" ;
        		     } */
            		 
            	} else if (DOMUtil.hasClass(items[i],"QuickAddTask")) {
            		items[i].style.display="none" ;
            		/*items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 2, "+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 2);" ;
        		     } */
            	}
            }
        }
    } else {
		var container = DOMUtil.findAncestorByClass(src,"EventWeekContent");
		var mouseY = (eXo.core.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
        obj = eXo.core.EventManager.getEventTargetByTagName(evt,"td");
				map = Date.parse(obj.getAttribute("startFull"));
        for (var i = 0; i < items.length; i++) {
            if (items[i].style.display == "block") {
                items[i].style.display = "none";
            }
            else {
            	items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/, "startTime=" + map);
                var fTime = parseInt(map);
                var tTime = fTime + 15*60*1000 ;
                
            	if(DOMUtil.hasClass(items[i],"QuickAddEvent")){
            		items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 1,"+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 1);" ;
        		     } 
            	} else if (DOMUtil.hasClass(items[i],"QuickAddTask")) {
            		items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 2, "+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 2);" ;
        		     } 
            	}
            }
            
        }
    }
};

/**
 * Callback method when right click in month view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.monthViewCallback = function(evt){
    var _e = window.event || evt;
    var src = _e.srcElement || _e.target;
    var UIContextMenu = eXo.webui.UIContextMenu;
    var DOMUtil = eXo.core.DOMUtil;
    var objectValue = "";
    var links = eXo.core.DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement, "a");
    if (!DOMUtil.findAncestorByClass(src, "EventBoxes")) {
        if (objectValue = DOMUtil.findAncestorByTagName(src, "td").getAttribute("startTime")) {
        	//TODO CS-2800
        	var startTime = parseInt(Date.parse(DOMUtil.findAncestorByTagName(src, "td").getAttribute('startTimeFull')));
        	var endTime = parseInt(Date.parse(DOMUtil.findAncestorByTagName(src, "td").getAttribute('startTimeFull')))  + 24*60*60*1000 - 1;
        	for(var i = 0; i < links.length; i++){
            	if(DOMUtil.hasClass(links[i], "QuickAddEvent")) {
            		links[i].href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");" 
            	} else if(DOMUtil.hasClass(links[i], "QuickAddTask")) {
            		links[i].href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");"
            	}
            }
        	/*var map = {
                "startTime\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "startTime=" + objectValue
            };
            UIContextMenu.changeAction(UIContextMenu.menuElement, map);*/
        }
    }
    else 
        if (objvalue = DOMUtil.findAncestorByClass(src, "DayContentContainer")) {
            var eventId = objvalue.getAttribute("eventId");
            var calendarId = objvalue.getAttribute("calId");
            var calType = objvalue.getAttribute("calType");
            var isOccur = objvalue.getAttribute("isOccur");
            var recurId = objvalue.getAttribute("recurId");
            if (recurId == "null") recurId = "";
            var map = {
                "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
                "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
                "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType,
                "isOccur\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "isOccur=" + isOccur,
                "recurId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "recurId=" + recurId
            };
            UIContextMenu.changeAction(UIContextMenu.menuElement, map);
        }
        else {
            return;
        }
};
/* BOF filter */

/**
 * Gets all calendar events of a calendar by its id
 * @param {Object} events All calendar events
 * @param {Object} calid Calendar id
 * @return All events of certain calendar
 */
UICalendarPortlet.prototype.getEventsByCalendar = function(events, calid){
    var calendarid = null;
    var len = events.length;
    var event = new Array();
    for (var i = 0; i < len; i++) {
        calendarid = events[i].getAttribute("calid");
        if (calendarid == calid) 
            event.push(events[i]);
    }
    return event;
};

/**
 * Gets all of events for filtering
 * @param {Object} events All calendar events
 * @return An array of events for filtering
 */
UICalendarPortlet.prototype.getEventsForFilter = function(events){
    var form = this.filterForm;
    var checkbox = eXo.core.DOMUtil.findDescendantsByClass(form, "input", "checkbox");
    var el = new Array();
    var len = checkbox.length;
    var calid = null;
    for (var i = 0; i < len; i++) {
        if (checkbox[i].checked) {
            calid = checkbox[i].name;
            el.pushAll(this.getEventsByCalendar(events, calid));
        }
    }
    return el;
};

/**
 * Filters calendar event by calendar group
 */
UICalendarPortlet.prototype.filterByGroup = function(){
    var DOMUtil = eXo.core.DOMUtil;
    var uiVtab = DOMUtil.findAncestorByClass(this, "UIVTab");
    var checkboxes = DOMUtil.findDescendantsByClass(uiVtab, "input", "checkbox");
    var checked = this.checked;
    var len = checkboxes.length;
    for (var i = 0; i < len; i++) {
        eXo.calendar.UICalendarPortlet.runFilterByCalendar(checkboxes[i].name, checked);
        if (checkboxes[i].checked == checked) 
            continue;
        checkboxes[i].checked = checked;
    }
	eXo.calendar.UICalendarPortlet.runFilterByCategory();
	eXo.calendar.UICalendarPortlet.resortEvents();
};

/**
 * Filters calendar event by calendar
 * @param {Object} calid Calendar id
 * @param {Boolean} checked Status of calendar(activated or disactivated)
 */
UICalendarPortlet.prototype.runFilterByCalendar = function(calid, checked){
    var uiCalendarViewContainer = eXo.calendar.UICalendarPortlet.getElementById("UICalendarViewContainer");
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    if (!uiCalendarViewContainer) 
        return;
    var className = "EventBoxes";
    if (eXo.calendar.UICalendarPortlet.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);
    if (!events) 
        return;
    var len = events.length;
    for (var i = 0; i < len; i++) {
        if (events[i].getAttribute("calId") == calid) {
            if (checked) {
                events[i].style.display = "block";
            }
            else {
                events[i].style.display = "none";
            }
        }
    } 
};

/**
 * Resort event after doing something
 */
UICalendarPortlet.prototype.resortEvents = function(){
	
    try { //TODO: review order javascript file 
        if (eXo.calendar.UICalendarPortlet.getElementById("UIMonthView")) 
            eXo.calendar.UICalendarMan.initMonth();
        if (eXo.calendar.UICalendarPortlet.getElementById("UIDayViewGrid")) 
            eXo.calendar.UICalendarPortlet.showEvent();
        if (eXo.calendar.UICalendarPortlet.getElementById("UIWeekViewGrid")) {
            eXo.calendar.UICalendarMan.initWeek();
            eXo.calendar.UIWeekView.init();
        }
    } 
    catch (e) {
    };
	
};
/**
 * Filters calendar event by calendar
 */
UICalendarPortlet.prototype.filterByCalendar = function(){
	var calid = this.getAttribute("calId");
    var show = "block";
    var hide = "none";
    var stylEvent = "none";
    
    var checkBox = eXo.core.DOMUtil.findFirstDescendantByClass(this, "input", "checkbox");
    var checked = checkBox.checked;
    var imgChk = eXo.core.DOMUtil.findFirstDescendantByClass(this, "span", "checkbox");
    
    var uiCalendarViewContainer = eXo.calendar.UICalendarPortlet.getElementById("UICalendarViewContainer");
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    if (!uiCalendarViewContainer) {
			return;    
    }
    var className = "EventBoxes";
    if (eXo.calendar.UICalendarPortlet.getElementById("UIWeekViewGrid")){
			className = "WeekViewEventBoxes";  
    }
    var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);

    if(checked){
    	stylEvent = hide;
    	checkBox.checked = false;
    	imgChk.className = "IconUnCheckBox checkbox";
    }else{
    	checkBox.checked = true;
    	stylEvent = show;
    	imgChk.className = "IconCheckBox checkbox";
    }
    
    if ((!events || events.length == 0)&& eXo.calendar.UICalendarPortlet.getElementById("UIListView")) {
        eXo.webui.UIForm.submitForm('UICalendars','Tick', true)		
    }
    if (!events) return;
    var len = events.length;
    
    for (var i = 0; i < len; i++) {
        if (events[i].getAttribute("calId") == calid) {
            events[i].style.display = stylEvent;
            var chkEvent = eXo.core.DOMUtil.findFirstDescendantByClass(events[i], "input", "checkbox");
            if (chkEvent) {
              chkEvent.checked = false;
              chkEvent.setAttribute('value', false);
            }
        }
    }
    
    //UICalendarPortlet.runFilterByCategory();
    eXo.calendar.UICalendarPortlet.resortEvents();
    
};

/**
 * Filters events by event category
 */
UICalendarPortlet.prototype.filterByCategory = function(){
    var uiCalendarViewContainer = eXo.calendar.UICalendarPortlet.getElementById("UICalendarViewContainer");
    if (!uiCalendarViewContainer) 
        return;
    var category = this.options[this.selectedIndex].value;
    eXo.calendar.UICalendarPortlet.selectedCategory = category;
    var className = "EventBoxes";
    if (eXo.calendar.UICalendarPortlet.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var allEvents = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);
    var events = eXo.calendar.UICalendarPortlet.getEventsForFilter(allEvents);
    if (!events) 
        return;
    var len = events.length;
    for (var i = 0; i < len; i++) {
        if (category == events[i].getAttribute("eventCat")) {
            events[i].style.display = "block";
        }
        else 
            if (category == "" || category == "all" || category == "defaultEventCategoryIdAll") {
                events[i].style.display = "block";
            }
            else 
                events[i].style.display = "none";
    }
    eXo.calendar.UICalendarPortlet.resortEvents();
};

/**
 * Filters event by event category
 * @param {Object} selectobj Select element
 */
UICalendarPortlet.prototype.runFilterByCategory = function(){
    var uiCalendarViewContainer = eXo.calendar.UICalendarPortlet.getElementById("UICalendarViewContainer");
		selectobj = eXo.core.DOMUtil.findFirstDescendantByClass(uiCalendarViewContainer,"select","selectbox");
    if (!selectobj) return;
    var category = null ;
		if (selectobj.selectedIndex >= 0 ) category = selectobj.options[selectobj.selectedIndex].value;
    var className = "EventBoxes";
    if (eXo.calendar.UICalendarPortlet.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var allEvents = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);
    var events = eXo.calendar.UICalendarPortlet.getEventsForFilter(allEvents);
    
     //CS-3152
    if ((!events || events.length == 0)&& eXo.calendar.UICalendarPortlet.getElementById("UIListView")) {
        eXo.webui.UIForm.submitForm('UICalendars','Tick', true)		
    }
    
    if (!events) 
        return;
    var len = events.length;
    for (var i = 0; i < len; i++) {
        if (category == events[i].getAttribute("eventCat")) {
            events[i].style.display = "block";
        }
        else 
            if (category == "" || category == "all" || category == "defaultEventCategoryIdAll") {
                events[i].style.display = "block";
            }
            else 
                events[i].style.display = "none";
    }
};

UICalendarPortlet.prototype.runAction = function(obj){
	var actionLink = obj.getAttribute("actionLink");
	var categoryId = this.filterSelect.options[this.filterSelect.selectedIndex].value;
	actionLink = actionLink.replace("')","&categoryId="+categoryId+"')");
	eval(actionLink);
};


/**
 * Gets select element that contains event category and sets up filtering action by event category
 * @param {Object} form Form id contains event category select element
 */
UICalendarPortlet.prototype.getFilterSelect = function(form){
    if (typeof(form) == "string") 
        form = eXo.calendar.UICalendarPortlet.getElementById(form);
    var eventCategory = eXo.core.DOMUtil.findFirstDescendantByClass(form, "div", "EventCategory");
		if (!eventCategory) return ;
    var select = eXo.core.DOMUtil.findDescendantsByTagName(eventCategory, "select")[0];
    var onchange = select.getAttribute("onchange");
    if (!onchange) 
        select.onchange = eXo.calendar.UICalendarPortlet.filterByCategory;
    this.filterSelect = select;
};

/**
 * Sets selected event category
 * @param {Object} form Form id contains event category select element
 */
UICalendarPortlet.prototype.setSelected = function(form){
    try {
      this.getFilterSelect(form);
      this.selectedCategory = this.filterSelect.options[this.filterSelect.selectedIndex].value;
    	this.listViewDblClick(form);
		} 
    catch (e) {
			this.listViewDblClick(form);
		}
};

UICalendarPortlet.prototype.listViewDblClick = function(form){
	form = (typeof(form) == "string")? eXo.calendar.UICalendarPortlet.getElementById(form):form ;
	if(!form) return ;
	var tr = eXo.core.DOMUtil.findDescendantsByClass(form,"tr","UIListViewRow");
	var i = tr.length ;
	eXo.calendar.UICalendarPortlet.viewType = "UIListView";
	var chk = null ;
	while(i--){
		eXo.core.EventManager.addEvent(tr[i],"dblclick",this.listViewDblClickCallback);
	}
};

UICalendarPortlet.prototype.doClick = function(){
	if(eXo.calendar.UICalendarPortlet.dblDone){
		delete eXo.calendar.UICalendarPortlet.dblDone;
		window.clearTimeout(eXo.calendar.UICalendarPortlet.clickone);
		return ;
	}
	eval(eXo.calendar.UICalendarPortlet.listViewAction);
};

UICalendarPortlet.prototype.listViewClickCallback = function(obj){
	this.listViewAction = obj.getAttribute("actionLink");
	this.clickone = setTimeout(this.doClick,200);
	return false ;
};

UICalendarPortlet.prototype.ondblclickCallbackInListView = function(obj){
	var eventId = obj.getAttribute("eventid");
	var calendarId = obj.getAttribute("calid");
	var calendarType = obj.getAttribute("caltype");
	eXo.webui.UIForm.submitEvent(eXo.calendar.UICalendarPortlet.portletId+'#' + eXo.calendar.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + eXo.calendar.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType);
};

UICalendarPortlet.prototype.listViewDblClickCallback = function(){
	eXo.calendar.UICalendarPortlet.dblDone = true;
	eXo.calendar.UICalendarPortlet.ondblclickCallbackInListView(this);
};
/**
 * Filter event when page load
 */
UICalendarPortlet.prototype.checkFilter = function() {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  for ( var i = 0; i < UICalendarPortlet.filterSelect.options.length; i++) {
    if (UICalendarPortlet.filterSelect.options[i].value == UICalendarPortlet.selectedCategory) {
      UICalendarPortlet.filterSelect.options[i].selected = true;
    }
  }
  UICalendarPortlet.checkCalendarFilter();
  eXo.calendar.UICalendarPortlet.resortEvents();
  UICalendarPortlet.setFocus();
  if (eXo.calendar.UICalendarPortlet.firstLoadTimeout)
    delete eXo.calendar.UICalendarPortlet.firstLoadTimeout;
};

/**
 * Filter event by calendar when page load
 */
UICalendarPortlet.prototype.checkCalendarFilter = function(){
    if (!this.filterForm) 
        return;
    var checkbox = eXo.core.DOMUtil.findDescendantsByClass(this.filterForm, "input", "checkbox");
    var len = checkbox.length;
    for (var i = 0; i < len; i++) {
        this.runFilterByCalendar(checkbox[i].name, checkbox[i].checked);
    }
    this.runFilterByCategory();
};

/**
 * Filter event by event category when page load
 */
UICalendarPortlet.prototype.checkCategoryFilter = function(){
    if (this.filterSelect) 
        eXo.calendar.UICalendarPortlet.runFilterByCategory();
};

/* EOF filter */
/**
 * Change among task and event view in list view
 * @param {Object} obj DOM element
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.switchListView = function(obj, evt){
    var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory");
    if (eXo.core.Browser.isIE6()) {
        var size = {
            top: obj.offsetHeight,
            left: "-" + obj.offsetWidth
        };
        this.setStyle(menu, size);
    }
    else {
        var size = {
            marginLeft: "-18px"
        };
        this.setStyle(menu, size);
    }
    eXo.webui.UIPopupSelectCategory.show(obj, evt);
};

/**
 * Shows view menu
 * @param {Object} obj DOM element
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.showView = function(obj, evt){
//    var _e = window.event || evt;
//    _e.cancelBubble = true;
		eXo.core.EventManager.cancelBubble(evt);
    var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu");
    var actions = eXo.core.DOMUtil.findDescendantsByClass(oldmenu, "a", "ItemLabel");
    if (!this.selectedCategory) 
        this.selectedCategory = null;
    for (var i = 0; i < actions.length; i++) {
        if (actions[i].href.indexOf("categoryId") < 0) 
            continue;
        actions[i].href = String(actions[i].href).replace(/categoryId.*&/, "categoryId=" + this.selectedCategory + "&");
    }
    eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj);
};

/**
 * Swap menu in IE
 * @param {Object} menu Menu DOM element
 * @param {Object} clickobj Click DOM element
 */
UICalendarPortlet.prototype.swapIeMenu = function(menu, clickobj){
    var DOMUtil = eXo.core.DOMUtil;
    var Browser = eXo.core.Browser;
    var x = Browser.findPosXInContainer(clickobj, menu.offsetParent) - eXo.cs.Utils.getScrollLeft(clickobj);
    var y = Browser.findPosYInContainer(clickobj, menu.offsetParent) - eXo.cs.Utils.getScrollTop(clickobj) + clickobj.offsetHeight;
    var browserHeight = document.documentElement.clientHeight;
    var uiRightClickPopupMenu = (!DOMUtil.hasClass(menu, "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(menu, "div", "UIRightClickPopupMenu") : menu;
    this.showHide(menu);
    if ((y + uiRightClickPopupMenu.offsetHeight) > browserHeight) {
        y = browserHeight - uiRightClickPopupMenu.offsetHeight;
    }
    
    DOMUtil.addClass(menu, "UICalendarPortlet UIEmpty");
    menu.style.zIndex = 2000;
    menu.style.left = x + "px";
    menu.style.top = y + "px";
};

/**
 * Swap menu
 * @param {Object} oldmenu Menu DOM element
 * @param {Object} clickobj clickobj Click DOM element
 */
UICalendarPortlet.prototype.swapMenu = function(oldmenu, clickobj){
    var DOMUtil = eXo.core.DOMUtil;
    var Browser = eXo.core.Browser;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var uiDesktop = document.getElementById("UIPageDesktop");
    if (document.getElementById("tmpMenuElement")) 
        DOMUtil.removeElement(document.getElementById("tmpMenuElement"));
    var tmpMenuElement = oldmenu.cloneNode(true);
    tmpMenuElement.setAttribute("id", "tmpMenuElement");
    tmpMenuElement.style.zIndex = 1 ;
    this.menuElement = tmpMenuElement;
 	if(Browser.isIE6()) this.menuElement.style.width = "140px";
    document.body.appendChild(this.menuElement);
    if (uiDesktop) {
        this.swapIeMenu(this.menuElement, clickobj);
        return;
    }
    
	DOMUtil.addClass(this.menuElement, "UICalendarPortlet UIEmpty");
    var menuX = Browser.findPosX(clickobj) ;
    var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight;
    if (arguments.length > 2) {
        menuY -= arguments[2].scrollTop;
    }
		if (eXo.core.I18n.isRT()) {
      menuX -= (eXo.cs.Utils.getElementWidth(this.menuElement) - clickobj.offsetWidth);// - uiWorkSpaceWidth;      
    }
    this.menuElement.style.top = menuY + "px";
    this.menuElement.style.left =  menuX + "px";
 		if (eXo.core.I18n.isRT() && Browser.isIE6()) {
      menuX = Browser.findPosXInContainer(clickobj,this.menuElement.offsetParent,true);
      //menuX += uiWorkSpaceWidth/2 ;
      this.menuElement.style.right = menuX + "px";
      this.menuElement.style.left =  "";
    }
    this.showHide(this.menuElement);
    var uiRightClick = (DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu") : UICalendarPortlet.menuElement;
    var mnuBottom = UICalendarPortlet.menuElement.offsetTop + uiRightClick.offsetHeight - window.document.documentElement.scrollTop;
    if (window.document.documentElement.clientHeight < mnuBottom) {
        menuY += (window.document.documentElement.clientHeight - mnuBottom);
        UICalendarPortlet.menuElement.style.top = menuY + "px";
    }
    
};

UICalendarPortlet.prototype.isAllday = function(form,selecedCalendarID){
  try {
      if (typeof(form) == "string") 
          form = eXo.calendar.UICalendarPortlet.getElementById(form);
      if (form.tagName.toLowerCase() != "form") {
          form = eXo.core.DOMUtil.findDescendantsByTagName(form, "form");
      }
      for (var i = 0; i < form.elements.length; i++) {
          if (form.elements[i].getAttribute("name") == "allDay") {
              eXo.calendar.UICalendarPortlet.allDayStatus = form.elements[i];
              eXo.calendar.UICalendarPortlet.showHideTime(form.elements[i]);
              break;
          }
      }
     /**
       * Preselect calendar when add event/task
       */
      var calendarid = (selecedCalendarID)?selecedCalendarID:this.getCheckedCalendar(this.filterForm);
      if(calendarid){
              var calendar = form.elements["calendar"];
              for(i=0; i < calendar.options.length;  i++) {
                      var value = calendar.options[i].value ;
                      calendar.options[i].selected = (value.match(calendarid) != null);                   
              }
      }
  } 
  catch (e) {
  
  }
};

/**
 * Show/hide time field in Add event form
 * @param {Object} chk Checkbox element
 */
UICalendarPortlet.prototype.showHideTime = function(chk){
    var DOMUtil = eXo.core.DOMUtil;
    if (chk.tagName.toLowerCase() != "input") {
        chk = DOMUtil.findFirstDescendantByClass(chk, "input", "checkbox");
    }
    var selectboxes = DOMUtil.findDescendantsByTagName(chk.form, "input");
    var fields = new Array();
    var len = selectboxes.length;
    for (var i = 0; i < len; i++) {
        if (selectboxes[i].className == "UIComboboxInput") {
            fields.push(selectboxes[i]);
        }
    }
    eXo.calendar.UICalendarPortlet.showHideField(chk, fields);
};

/**
 * Show/hide field in form
 * @param {Object} chk Checkbox element
 * @param {Object} fields Input field in form
 */
UICalendarPortlet.prototype.showHideField = function(chk, fields){
    var display = "";
    if (typeof(chk) == "string")
        chk = eXo.calendar.UICalendarPortlet.getElementById(chk);  
    display = (chk.checked) ? "hidden" : "visible";
    var len = fields.length;
    for (var i = 0; i < len; i++) {
        fields[i].style.visibility = display;
        i
    }
};

UICalendarPortlet.prototype.showHideRepeat = function(chk){
    var DOMUtil = eXo.core.DOMUtil;
    var checkbox = DOMUtil.findFirstDescendantByClass(chk, "input", "checkbox");
    var fieldCom = DOMUtil.findAncestorByClass(chk, "FieldComponent");
    var repeatField = DOMUtil.findFirstDescendantByClass(fieldCom, "div", "RepeatInterval");
		if (checkbox.checked) {
	    repeatField.style.visibility = "visible";
		} else {
	    repeatField.style.visibility = "hidden";
		}
};

UICalendarPortlet.prototype.autoShowRepeatEvent = function(){
		var DOMUtil = eXo.core.DOMUtil;
		var divEmailObject = document.getElementById("IsEmailRepeatEventReminderTab");
    var checkboxEmail = DOMUtil.findFirstDescendantByClass(divEmailObject, "input", "checkbox");
    var fieldComEmail = DOMUtil.findAncestorByClass(divEmailObject, "FieldComponent");
    var repeatFieldEmail = DOMUtil.findFirstDescendantByClass(fieldComEmail, "div", "RepeatInterval");
		if (checkboxEmail.checked) {
	    repeatFieldEmail.style.visibility = "visible";
		} else {
	    repeatFieldEmail.style.visibility = "hidden";
		}
    
    var divObjectPopup = document.getElementById("IsPopupRepeatEventReminderTab");
    var checkboxPopup = DOMUtil.findFirstDescendantByClass(divObjectPopup, "input", "checkbox");
    var fieldComPopup = DOMUtil.findAncestorByClass(divObjectPopup, "FieldComponent");
    var repeatFieldPopup = DOMUtil.findFirstDescendantByClass(fieldComPopup, "div", "RepeatInterval");
		if (checkboxPopup.checked) {
	    repeatFieldPopup.style.visibility = "visible";
		} else {
	    repeatFieldPopup.style.visibility = "hidden";
		}  
};

/**
 * Sets up dragging selection for calendar view
 */
UICalendarPortlet.prototype.initSelection = function(){
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var UISelection = eXo.calendar.UISelection;
    var container = eXo.core.DOMUtil.findFirstDescendantByClass(eXo.calendar.UICalendarPortlet.getElementById("UIDayViewGrid"), "div", "EventBoard");
    UISelection.step = UICalendarPortlet.CELL_HEIGHT;
    UISelection.container = container;
    UISelection.block = document.createElement("div");
    UISelection.block.className = "UserSelectionBlock";
    UISelection.container.appendChild(UISelection.block);
    UISelection.container.onmousedown = UISelection.start;
    UISelection.relativeObject = eXo.core.DOMUtil.findAncestorByClass(UISelection.container, "EventDayContainer");
    UISelection.viewType = "UIDayView";
};

/* for selection creation */
/**
 * Class control dragging selection
 * @author <a href="mailto:dung14000@gmail.com">Hoang Manh Dung</a>
 * @constructor
 */
function UISelection(){

};

/**
 * Sets up dragging selection when mouse down on calendar event
 * @param {Object} evt Mouse event
 */
UISelection.prototype.start = function(evt){
    try {
        var UISelection = eXo.calendar.UISelection;
        var src = eXo.core.EventManager.getEventTarget(evt);
        if ((src == UISelection.block) || (eXo.core.EventManager.getMouseButton(evt) == 2) || (eXo.core.DOMUtil.hasClass(src,"TdTime"))) {
						return;
        }
        
        UISelection.startTime = parseInt(Date.parse(src.getAttribute("startFull")));//src.getAttribute("startTime");
        UISelection.startX = eXo.core.Browser.findPosXInContainer(src, UISelection.container) - eXo.calendar.UICalendarPortlet.portletNode.parentNode.scrollTop;
        UISelection.block.style.display = "block";
        UISelection.startY = eXo.core.Browser.findPosYInContainer(src, UISelection.container);
        UISelection.block.style.width = src.offsetWidth + "px";
        UISelection.block.style.left = UISelection.startX + "px";
        UISelection.block.style.top = UISelection.startY + "px";
        UISelection.block.style.height = UISelection.step + "px";
        UISelection.block.style.zIndex = 1; 
        eXo.calendar.UICalendarPortlet.resetZIndex(UISelection.block);
        document.onmousemove = UISelection.execute;
        document.onmouseup = UISelection.clear;
    } 
    catch (e) {
        window.status = e.message ;
    }
};

/**
 * Executes dragging selection
 * @param {Object} evt Mouse event
 */
UISelection.prototype.execute = function(evt){
    var UISelection = eXo.calendar.UISelection;
    var _e = window.event || evt;
    var delta = null;
		var containerHeight = UISelection.container.offsetHeight;
    var scrollTop = eXo.cs.Utils.getScrollTop(UISelection.block);
    var mouseY = eXo.core.Browser.findMouseRelativeY(UISelection.container, _e) + UISelection.relativeObject.scrollTop;
    if (document.getElementById("UIPageDesktop")) 
        mouseY = eXo.core.Browser.findMouseRelativeY(UISelection.container, _e) + scrollTop;
    var posY = UISelection.block.offsetTop;
    var height = UISelection.block.offsetHeight;
    delta = posY + height - mouseY;
    if (UISelection.startY < mouseY) {
        UISelection.block.style.top = UISelection.startY + "px";
        if (delta >= UISelection.step) {
            UISelection.block.style.height = height - UISelection.step + "px";
        }
        if ((mouseY >= (posY + height)) && ((posY + height)< containerHeight) ) {
            UISelection.block.style.height = height + UISelection.step + "px";
        }
    }
    else {
        delta = mouseY - posY;
        UISelection.block.style.bottom = UISelection.startY - UISelection.step + "px";
        if ((mouseY <= posY) && (posY > 0)) {
            UISelection.block.style.top = posY - UISelection.step + "px";
            UISelection.block.style.height = height + UISelection.step + "px";
        }
        if (delta >= UISelection.step) {
            UISelection.block.style.top = posY + UISelection.step + "px";
            UISelection.block.style.height = height - UISelection.step + "px";
        }
    }
    
};

/**
 * Ends dragging selection, this method clean up some unused properties and execute callback function
 */
UISelection.prototype.clear = function(){
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var UISelection = eXo.calendar.UISelection;
    var endTime = UICalendarPortlet.pixelsToMins(UISelection.block.offsetHeight) * 60 * 1000 + parseInt(UISelection.startTime);
    var startTime = UISelection.startTime;
		var bottom = UISelection.block.offsetHeight + UISelection.block.offsetTop;

    if (UISelection.block.offsetTop < UISelection.startY) {
        startTime = parseInt(UISelection.startTime) - UISelection.block.offsetHeight * 60 * 1000 + UISelection.step * 60 * 1000;
        endTime = parseInt(UISelection.startTime) + UISelection.step * 60 * 1000;
    }
		if(bottom >= UISelection.container.offsetHeight) endTime -= 1;
	var container = UICalendarPortlet.getElementById("UICalendarViewContainer");	
	UICalendarPortlet.addQuickShowHiddenWithTime(container, 1, startTime, endTime) ;
    //eXo.webui.UIForm.submitEvent(UISelection.viewType, 'QuickAdd', '&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime);
    eXo.core.DOMUtil.listHideElements(UISelection.block);
		UISelection.startTime = null;
		UISelection.startY = null;
		UISelection.startX = null;
    document.onmousemove = null;
    document.onmouseup = null;
};

// check free/busy time
/**
 * Checks free/busy in day of an user
 * @param {Object} chk Checkbox element
 */
UICalendarPortlet.prototype.checkAllInBusy = function(chk){
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var isChecked = chk.checked;
    var timeField = eXo.core.DOMUtil.findFirstDescendantByClass(chk.form, "div", "TimeField");
    if (isChecked) {
        timeField.style.display = "none";
    }
    else {
        timeField.style.display = "block";
    }
    if (UICalendarPortlet.allDayStatus) {
        UICalendarPortlet.allDayStatus.checked = isChecked;
        UICalendarPortlet.showHideTime(UICalendarPortlet.allDayStatus);
    }
};

/**
 * Sets up checking free/busy
 * @param {Object} container DOM element contains event data
 */
UICalendarPortlet.prototype.initCheck = function(container, userSettingTimezone){
    var DOMUtil = eXo.core.DOMUtil;
    if (typeof(container) == "string") 
        container = document.getElementById(container);
    var dateAll = DOMUtil.findDescendantsByClass(container, "input", "checkbox")[1];
    var table = DOMUtil.findFirstDescendantByClass(container, "table", "UIGrid");
    var tr = DOMUtil.findDescendantsByTagName(table, "tr");
    var firstTr = tr[1];
    this.busyCell = DOMUtil.findDescendantsByTagName(firstTr, "td").slice(1);
    var len = tr.length;
    for (var i = 2; i < len; i++) {
        this.showBusyTime(tr[i], userSettingTimezone);
    }
    if (eXo.calendar.UICalendarPortlet.allDayStatus) 
        dateAll.checked = eXo.calendar.UICalendarPortlet.allDayStatus.checked;
    eXo.calendar.UICalendarPortlet.checkAllInBusy(dateAll);
    dateAll.onclick = function(){
        eXo.calendar.UICalendarPortlet.checkAllInBusy(this);
    }
    eXo.calendar.UICalendarPortlet.initSelectionX(firstTr);
};

/**
 * Localizes time
 * @param {Object} millis Time in minutes
 * @param {Object} timezoneOffset Timezone offset of current user
 * @return Time in minutes
 */
UICalendarPortlet.prototype.localTimeToMin = function(millis, timezoneOffset){
    if (typeof(millis) == "string") 
        millis = parseInt(millis);
    millis += timezoneOffset * 60 * 1000;
    var d = new Date(millis);
    var hour = d.getHours();
    var min = d.getMinutes();
    var min = hour * 60 + min;
    return min;
};

/**
 * Parses time from string
 * @param {Object} string String
 * @param {Object} timezoneOffset Timezone offset of user
 * @return Object contains two properties that are from and to
 */
UICalendarPortlet.prototype.parseTime = function(string, timezoneOffset){
    var stringTime = string.split(",");
    var len = stringTime.length;
    var time = new Array();
    var tmp = null;
    for (var i = 0; i < len; i += 2) {
        tmp = {
            "from": this.localTimeToMin(stringTime[i], timezoneOffset),
            "to": this.localTimeToMin(stringTime[i + 1], timezoneOffset)
        };
        time.push(tmp);
    }
    return time;
};

/**
 * Shows free/busy on UI
 * @param {Object} tr Tr tag contains event data
 * @param {Object} serverTimezone Server timezone
 */
UICalendarPortlet.prototype.showBusyTime = function(tr, userSettingTimezoneOffset){
    var stringTime = tr.getAttribute("busytime");
    var browserTimezone = (new Date).getTimezoneOffset();
    var extraTime = browserTimezone - userSettingTimezoneOffset;
    if (!stringTime) 
        return;
    var time = this.parseTime(stringTime, extraTime);
    var len = time.length;
    var from = null;
    var to = null;
    for (var i = 0; i < len; i++) {
        from = parseInt(time[i].from);
        to = parseInt(time[i].to);
        this.setBusyTime(from, to, tr)
    }
};

/**
 * Show free/busy time in a tr tag
 * @param {Object} from Time in minutes
 * @param {Object} to Time in minutes
 * @param {Object} tr Tr tag contains event data
 */
UICalendarPortlet.prototype.setBusyTime = function(from, to, tr){
    var cell = eXo.core.DOMUtil.findDescendantsByTagName(tr, "td").slice(1);
    var start = this.ceil(from, 15) / 15;
    var end = this.ceil(to, 15) / 15;
    for (var i = start; i < end; i++) {
        cell[i].className = "BusyDotTime";
        this.busyCell[i].className = "BusyTime";
    }
};

/**
 * Ceiling round number
 * @param {Object} number Original number
 * @param {Object} dividend Divided end
 * @return rounded number
 */
UICalendarPortlet.prototype.ceil = function(number, dividend){
    var mod = number % dividend;
    if (mod != 0) 
        number += dividend - mod;
    return number;
};

/**
 * Sets up dragging selection for free/busy time table
 * @param {Object} tr Tr tag contains event data
 */
UICalendarPortlet.prototype.initSelectionX = function(tr){
    cell = eXo.core.DOMUtil.findDescendantsByTagName(tr, "td", "UICellBlock").slice(1);
    var len = cell.length;
    for (var i = 0; i < len; i++) {
        cell[i].onmousedown = eXo.calendar.UIHSelection.start;//eXo.calendar.Highlighter.start ;
    }
};

/**
 * Gets AM/PM from input value
 * @param {Object} input Input contains time
 * @return Object contains two properties that are AM and PM
 */
UICalendarPortlet.prototype.getTimeFormat = function(input){
    //var list = eXo.core.DOMUtil.findPreviousElementByTagName(input, "div");
    //var a = eXo.core.DOMUtil.findDescendantsByTagName(list, "a");
    var am = input.getAttribute("value").match(/[A-Z]+/);
    if (!am) 
        return null;
    var pm = a[a.length - 1].getAttribute("value").match(/[A-Z]+/);
    return {
        "am": am,
        "pm": pm
    };
};

/**
 * Callback method when dragging selection end
 */
UICalendarPortlet.prototype.callbackSelectionX = function(){
    var Highlighter = eXo.calendar.UIHSelection;
    var DOMUtil = eXo.core.DOMUtil;
    var len = Math.abs(Highlighter.firstCell.cellIndex - Highlighter.lastCell.cellIndex - 1);
    var start = (Highlighter.firstCell.cellIndex - 1) * 15;
    var end = start + len * 15;
    var timeTable = DOMUtil.findAncestorByTagName(Highlighter.firstCell, "table");
    var dateValue = timeTable.getAttribute("datevalue");
    var uiTabContentContainer = DOMUtil.findAncestorByClass(Highlighter.firstCell, "UITabContentContainer");
    var UIComboboxInputs = DOMUtil.findDescendantsByClass(uiTabContentContainer, "input", "UIComboboxInput");
    len = UIComboboxInputs.length;
    var name = null;
    var timeFormat = this.getTimeFormat(this.synTime(UIComboboxInputs[0]));
    start = this.minToTime(start, timeFormat);
    end = this.minToTime(end, timeFormat);
    if (dateValue) {
        var DateContainer = DOMUtil.findAncestorByTagName(uiTabContentContainer, "form");
        DateContainer.from.value = dateValue;
        DateContainer.to.value = dateValue;
    }
    for (var i = 0; i < len; i++) {
        name = this.synTime(UIComboboxInputs[i]).name.toLowerCase();
        if (name.indexOf("from") >= 0) {
            UIComboboxInputs[i].value = start;
						this.synTime(UIComboboxInputs[i],start);
				}
        else {
            UIComboboxInputs[i].value = end;
						this.synTime(UIComboboxInputs[i],end);					
				}

    }
    var cells = eXo.core.DOMUtil.getChildrenByTagName(Highlighter.firstCell.parentNode, "td");
    Highlighter.setAttr(Highlighter.firstCell.cellIndex, Highlighter.lastCell.cellIndex, cells);
};

UICalendarPortlet.prototype.synTime = function(o,v){
	var ro = eXo.core.DOMUtil.findPreviousElementByTagName(o,"input");
	if(!v) return ro;
	ro.value = v;
}

/**
 * Sets some properties of UICalendarPortlet object again when user changes setting
 * @param {Object} cpid Component id
 */
UICalendarPortlet.prototype.initSettingTab = function(cpid){
    var cp = eXo.calendar.UICalendarPortlet.getElementById(cpid);
    var ck = eXo.core.DOMUtil.findFirstDescendantByClass(cp, "input", "checkbox");
    var div = eXo.core.DOMUtil.findAncestorByTagName(ck, "div");
    eXo.calendar.UICalendarPortlet.workingSetting = eXo.core.DOMUtil.findNextElementByTagName(div, "div");
    ck.onclick = eXo.calendar.UICalendarPortlet.showHideWorkingSetting;
    eXo.calendar.UICalendarPortlet.checkWorkingSetting(ck);
}

/**
 * Check status of working time checkbox
 * @param {Object} ck Working time checkbox
 */
UICalendarPortlet.prototype.checkWorkingSetting = function(ck){
    var isCheck = ck.checked;
    if (isCheck) {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "visible";
    }
    else {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "hidden";
    }
}

/**
 * Show/hide working time setting
 */
UICalendarPortlet.prototype.showHideWorkingSetting = function(){
    var isCheck = this.checked;
    if (isCheck) {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "visible";
    }
    else {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "hidden";
    }
};

UICalendarPortlet.prototype.showImagePreview = function(obj){
	var DOMUtil = eXo.core.DOMUtil ;
	var img = DOMUtil.findPreviousElementByTagName(obj.parentNode,"img");	
	var viewLabel = obj.getAttribute("viewLabel");
	var closeLabel = obj.getAttribute("closeLabel");
	if(img.style.display == "none"){
		img.style.display = "block";
		obj.innerHTML = closeLabel ;
		if(DOMUtil.hasClass(obj,"ViewAttachmentIcon")) DOMUtil.replaceClass(obj,"ViewAttachmentIcon"," CloseAttachmentIcon") ;
	}else {
		img.style.display = "none";
		obj.innerHTML = viewLabel ;
		if(DOMUtil.hasClass(obj,"CloseAttachmentIcon")) DOMUtil.replaceClass(obj,"CloseAttachmentIcon"," ViewAttachmentIcon") ;
	}
};

UICalendarPortlet.prototype.showHideSetting = function(obj){
	var checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"input","checkbox");
	var uiFormGrid = eXo.core.DOMUtil.findFirstDescendantByClass(obj.parentNode.parentNode,"table","UIFormGrid");
	if(checkbox.checked) {
		checkbox.checked = true;
		uiFormGrid.style.display = "";
	}
	else{
		checkbox.checked = false;
		uiFormGrid.style.display = "none";
	}	
};

UICalendarPortlet.prototype.autoShowHideSetting = function(){
	var DOMUtil = eXo.core.DOMUtil;
	var eventReminder = document.getElementById("eventReminder");
	var checkboxEmail = DOMUtil.findFirstDescendantByClass(eventReminder, "input", "checkbox");
	var uiFormGrid = DOMUtil.findFirstDescendantByClass(eventReminder,"table","UIFormGrid");
	if(checkboxEmail.checked) {
		uiFormGrid.style.display = "";
	}
	else{
		uiFormGrid.style.display = "none";
	}
	var popupReminder = DOMUtil.findFirstDescendantByClass(eventReminder, "div", "ReminderByPopup");
	var checkboxPopup = DOMUtil.findFirstDescendantByClass(popupReminder, "input", "checkbox");
	var uiFormGridPopup = DOMUtil.findFirstDescendantByClass(popupReminder,"table","UIFormGrid");
	if(checkboxPopup.checked) {
		uiFormGridPopup.style.display = "";
	}
	else{
		uiFormGridPopup.style.display = "none";
	}
};

UICalendarPortlet.prototype.removeEmailReminder = function(obj){
	var uiEmailAddressItem = obj.parentNode;
	var uiEmailAddressLabel = eXo.core.DOMUtil.findPreviousElementByTagName(obj,"div");
	var uiEmailInput = eXo.core.DOMUtil.findAncestorByClass(obj,"UIEmailInput");
	uiEmailInput = eXo.core.DOMUtil.getChildrenByTagName(uiEmailInput,"input")[0];
	uiEmailAddressLabel = uiEmailAddressLabel.innerHTML.toString().trim();
	uiEmailInput.value = this.removeItem(uiEmailInput.value,uiEmailAddressLabel);
	eXo.core.DOMUtil.removeElement(uiEmailAddressItem);
	if(eXo.calendar.UICalendarPortlet.getElementById("UIEventForm")) {
		eXo.webui.UIForm.submitForm('UIEventForm','RemoveEmail', true);		
	} else if(eXo.calendar.UICalendarPortlet.getElementById("UITaskForm")) { 
		eXo.webui.UIForm.submitForm('UITaskForm','RemoveEmail', true);	
	}
}

UICalendarPortlet.prototype.removeItem = function(str,removeValue){
	if(str.indexOf(",") <= 0) return "";
	var list = str.split(",");
	list.remove(removeValue);
	var tmp = "";
	for(var i = 0 ; i < list.length; i++){
		tmp += ","+list[i];
	}
	return tmp.substr(1,tmp.length);
};

UICalendarPortlet.prototype.getElementById = function(id){
	return eXo.core.DOMUtil.findDescendantById(this.portletNode,id);
}
eXo.calendar.UICalendarPortlet = eXo.calendar.UICalendarPortlet || new UICalendarPortlet();
eXo.calendar.UIResizeEvent = new UIResizeEvent();
eXo.calendar.UISelection = new UISelection();

UICalendarPortlet.prototype.fixFirstLoad = function(){
    if (this.firstRun){
			if (this.delay) {
	        window.clearTimeout(this.delay);
					delete this.delay ;
	    }
        return;
		}
    if (document.getElementById("UIPageDesktop")) {
        if (eXo.calendar.UICalendarPortlet.getElementById("UIWeekView")) {
            eXo.calendar.UICalendarMan.initWeek();
            eXo.calendar.UIWeekView.setSize();
            eXo.calendar.UICalendarPortlet.setFocus();
            this.firstRun = true;
        }
    }
    
};

UICalendarPortlet.prototype.fixForMaximize = function(){
	var obj = eXo.calendar.UICalendarPortlet.portletNode ;
	var uiWindow = eXo.core.DOMUtil.findAncestorByClass(obj, "UIWindow");
	if(uiWindow.style.display == "none") return ;
  if ((eXo.core.Browser.browserType != "ie")) {
      if (eXo.calendar.UICalendarPortlet.getElementById("UIWeekView")) {
          eXo.calendar.UICalendarMan.initWeek();
          eXo.calendar.UIWeekView.setSize();
      }
      if (eXo.calendar.UICalendarPortlet.getElementById("UIMonthView")) {
          eXo.calendar.UICalendarMan.initMonth();
      }
  }
};

/**
 * 
 * Scroll Manager for Action bar
 */
function CalendarScrollManager(){
};

CalendarScrollManager.prototype.load = function(){ 
	var uiNav = eXo.calendar.CalendarScrollManager ;
  var container = eXo.calendar.UICalendarPortlet.getElementById("UIActionBar") ;
  if(container) {
    var mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "CalendarActionBar") ;
	  var randomId = eXo.core.DOMUtil.generateId("CalendarScrollbar");
  	mainContainer.setAttribute("id",randomId);
    uiNav.scrollMgr = eXo.portal.UIPortalControl.newScrollManager(randomId) ;
    uiNav.scrollMgr.initFunction = uiNav.initScroll ;
    uiNav.scrollMgr.mainContainer = mainContainer ;
    uiNav.scrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ScrollButtons") ;
    uiNav.scrollMgr.loadItems("ActionBarButton", true) ;
    var button = eXo.core.DOMUtil.findDescendantsByTagName(uiNav.scrollMgr.arrowsContainer, "div");
    if(button.length >= 2) {    
      uiNav.scrollMgr.initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
      uiNav.scrollMgr.initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
    }
		
    uiNav.scrollManagerLoaded = true;
    uiNav.initScroll() ;
  }
} ;

CalendarScrollManager.prototype.initScroll = function() {
  var uiNav = eXo.calendar.CalendarScrollManager ;
  if(!uiNav.scrollManagerLoaded) uiNav.load() ;
  var elements = uiNav.scrollMgr.elements ;
  uiNav.scrollMgr.init() ;
  uiNav.scrollMgr.csCheckAvailableSpace() ;
  uiNav.scrollMgr.renderElements() ;
} ;

ScrollManager.prototype.loadItems = function(elementClass, clean) {
	if (clean) this.cleanElements();
	this.elements.clear();
	var items = eXo.core.DOMUtil.findDescendantsByClass(this.mainContainer, "div", elementClass);
	for(var i = 0; i < items.length; i++){
		this.elements.push(items[i]);
	}
};

ScrollManager.prototype.csCheckAvailableSpace = function(maxSpace) { // in pixels
	if (!maxSpace) maxSpace = this.getElementSpace(this.mainContainer) - this.getElementSpace(this.arrowsContainer);
	var elementsSpace = 0;
	var margin = 0;
	var length =  this.elements.length;
	for (var i = 0; i < length; i++) {
		elementsSpace += this.getElementSpace(this.elements[i]);
		//dynamic margin;
		if (i+1 < length) margin = this.getElementSpace(this.elements[i+1]) / 3;
		else margin = this.margin;
		if (elementsSpace + margin < maxSpace) { // If the tab fits in the available space
			this.elements[i].isVisible = true;
			this.lastVisibleIndex = i;
		} else { // If the available space is full
			this.elements[i].isVisible = false;
		}
	}
};

eXo.calendar.EventTooltip = {
  UTC_0: "UTC:0",
	isDnD: false,
	timer: 1000,
	getContainer: function(evt){
		var self = eXo.calendar.EventTooltip;
		if(self._container) delete self._container;
		if(!self._container){
			var eventNode = eXo.core.EventManager.getEventTarget(evt);
			eventNode = eXo.core.DOMUtil.findAncestorByClass(eventNode,"UICalendarPortlet");
			self._container = eXo.core.DOMUtil.findFirstDescendantByClass(eventNode,"div","UICalendarEventTooltip");
			eXo.core.EventManager.addEvent(self._container,"mouseover",function(evt){
				self.cleanupTimer(evt);
			});
			eXo.core.EventManager.addEvent(self._container,"mouseout",function(evt){
				self.hide(evt);
			});
			eXo.core.EventManager.addEvent(self._container,"click",function(evt){
				self.hideElement();
				self.editEvent(self.currentEvent);
			});
		}
	},
	editEvent: function(eventNode){				
    var eventId = eventNode.getAttribute("eventId");
    var calendarId = eventNode.getAttribute("calid");
    var calendarType = eventNode.getAttribute("caltype");
    eXo.webui.UIForm.submitEvent(eXo.calendar.UICalendarPortlet.portletId + '#' + eXo.calendar.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + eXo.calendar.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType);
	},
	show: function(evt){
		var self = eXo.calendar.EventTooltip;
		self.currentEvent = this;
		self.cleanupTimer(evt);
		if(eXo.calendar.EventTooltip.isDnD == true) return;		
		self.getContainer(evt);
		self.overTimer = setTimeout(function(){
			var url = eXo.env.portal.context + "/" + eXo.calendar.restContext;
			url += "/cs/calendar/getevent/" + self.currentEvent.getAttribute("eventid");
			self.makeRequest("GET",url);
		},self.timer);
	},
	hide: function(evt){
		var self = eXo.calendar.EventTooltip;
		self.cleanupTimer(evt);
		self.outTimer = setTimeout(function(){
			self.hideElement();					
		},self.timer);
		eXo.calendar.EventTooltip.isDnD == false;
	},
	hideElement: function(){
		if(this._container) this._container.style.display = "none";
	},
	disable: function(evt){
		this.hideElement();
		if(evt && eXo.core.EventManager.getMouseButton(evt) != 2) this.isDnD = true;
	},
	enable: function(){
		this.isDnD = false;
	},
	cleanupTimer:function(evt){
		//eXo.core.EventManager.cancelBubble(evt);
		if(this.outTimer) clearTimeout(this.outTimer);
		if(this.overTimer) clearTimeout(this.overTimer);
	},
	makeRequest: function(method, url, queryString){
		var request = new AjaxRequest(method, url, queryString) ;
	  request.onSuccess = this.render ;
	  request.onLoading = function(){
			eXo.calendar.EventTooltip._container.innerHTML = "Loading...";
		} ;
	  eXo.portal.CurrentRequest = request ;
	  request.process() ;
	},
	parseData: function(req){
		var data = eXo.core.JSON.parse(req.responseText);
		var time = this.getRealTime(data);
		return {
			title: data.summary,
			description: data.description,
			time:time,
			location: data.location,
			status: data.eventState,
			priority: data.priority
		}
	},
	isAllday:function(eventObject){
		var startDate = eventObject.startDateTime;
		var endDate = eventObject.endDateTime;
		var delta = endDate - startDate;
		if((startDate == endDate) && (delta == (24*60 - 1)*60*1000)) return 1;
		if((startDate != endDate) && (delta >= 24*60*60.1000) ) return 2;
		return 0;
	},
	getRealTime: function(data){
		var time = "";
		var type = this.isAllday(data);
		
		var timeFormat = null;
		if (eXo.calendar.UICalendarPortlet.timeFormat.indexOf("HH") > -1) {
			timeFormat = String(eXo.calendar.UICalendarPortlet.timeFormat).toUpperCase();
		} else {
			var formater = eXo.cs.DateTimeFormater ;
			timeFormat = formater.masks.shortTime ;
		}
		var currentDate = new Date();
		var d = new Date(parseInt(data.startDateTime) + parseInt(data.startTimeOffset));
		var d1 = new Date(parseInt(data.endDateTime) + parseInt(data.endTimeOffset));
		var dateFormat = data.dateFormat.toLowerCase();
		var df = eXo.cs.DateTimeFormater;
		if(type == 1){
			time = df.format(d, dateFormat, this.UTC_0);
		}
		else if(type == 2){
			time = df.format(d, dateFormat, this.UTC_0) + " " + df.format(d, timeFormat, this.UTC_0) + " - ";
			time += df.format(d1, dateFormat, this.UTC_0) + " " + df.format(d1, timeFormat, this.UTC_0);
		}
		else{
			time = df.format(d, dateFormat, this.UTC_0) + " " + df.format(d, timeFormat, this.UTC_0);
			time += " - " + df.format(d1, timeFormat, this.UTC_0);
		}
		return time;
	},
	convertTimezone: function(datetime){
		var time = parseInt(datetime.time);
		var eventTimezone = parseInt(datetime.timezoneOffset);
		var settingTimezone = parseInt(eXo.calendar.UICalendarPortlet.settingTimezone);
		time += (eventTimezone + settingTimezone)*60*1000;
		return time;
	},
	render: function(req){		
		var self = eXo.calendar.EventTooltip;
		var data = self.parseData(req);
		if(!data) return ;
		var html                   = '<div class="Time">' + data.time + '</div>';
		html                      += '<div class="Title">' + data.title + '</div>';
		if(data.description) html += '<div class="Description">' + data.description + '</div>';
		if(data.location)    html += '<div class="Location">' + data.location + '</div>';
		if(data.priority)    html += '<div class="'+ data.priority.toLowerCase() +'PriorityIcon"><span></span></div>';
		if(data.status != "null") html += '<div class="Status">' + data.status.replace("-"," ") + '</div>';
		self._container.style.display = "block";
		//var topArrow = self.currentEvent.offsetHeight/2 - 7; 
		self._container.innerHTML = '<div class="BgTLEvent"><div class="BgTREvent"><div class="BgTCEvent"><span></span></div></div></div><div class="BgMLEvent"><div class="BgMREvent"><div class="BgMCEvent">' + html + '</div></div></div><div class="BgBLEvent"><div class="BgBREvent"><div class="BgBCEvent"><span></span></div></div></div><div class="Clear"><span></span></div>';	
		self._container.style.zIndex = 1000;
		self.positioning();
	},
	positioning: function(){
		var offsetTooltip = this._container.offsetParent;
		var offsetEvent = this.currentEvent.offsetParent;
		if(eXo.calendar.UICalendarPortlet.viewType == "UIDayView") offsetEvent = eXo.core.DOMUtil.findAncestorByClass(offsetEvent,"EventDayContainer");
		var extraY = (this.currentEvent.offsetHeight - this._container.offsetHeight)/2
		var extraX = 0;
		var x = eXo.core.Browser.findPosXInContainer(this.currentEvent,offsetTooltip) + this.currentEvent.offsetWidth;
		var y = eXo.core.Browser.findPosYInContainer(this.currentEvent,offsetTooltip) - offsetEvent.scrollTop + extraY;		
		this._container.style.top = y + "px";
		this._container.style.left = x + "px";
		var relativeX = eXo.core.Browser.findPosX(this._container) + this._container.offsetWidth;
		if(relativeX > document.documentElement.offsetWidth) {
			extraX = document.documentElement.offsetWidth - relativeX;
			x += extraX;
			this._container.style.left = x + "px";
		}
	}	
}


eXo.calendar.CalendarScrollManager = new CalendarScrollManager();

if(eXo.desktop.UIDesktop){
UIDesktop.prototype._ShowHideWindow = eXo.desktop.UIDesktop.showHideWindow;
UIWindow.prototype._endResizeWindowEvt = eXo.desktop.UIWindow.endResizeWindowEvt;
UIWindow.prototype._maximizeWindowEvt = eXo.desktop.UIWindow.maximizeWindowEvt;

UIDesktop.prototype.showHideWindow = function(uiWindow, clickedElement, mode){
	//Fix for CS-3474: IE8: Webos : can not open portlet by one click
	var DOMUtil = eXo.core.DOMUtil ;
	if(typeof(uiWindow) == "string") this.cs3474 = document.getElementById(uiWindow) ;
	else this.cs3474 = uiWindow ;
	this.cs3474.maxIndex = 0;
	eXo.desktop.UIDesktop._ShowHideWindow(this.cs3474, clickedElement, mode);
	//End fix for CS-3474
    if (eXo.desktop.UIDesktop.object.style.display != "block") {
        if(uiWindow.indexOf("calendar") >=0) eXo.calendar.UICalendarPortlet.delay = window.setTimeout("eXo.calendar.UICalendarPortlet.fixFirstLoad() ;", 2000);
    }
};




UIWindow.prototype.endResizeWindowEvt = function(evt){
    // Re initializes the scroll tabs managers on the page
		eXo.desktop.UIWindow._endResizeWindowEvt(evt);
    eXo.calendar.UICalendarPortlet.fixForMaximize();
};

UIWindow.prototype.maximizeWindowEvt = function(evt){
  var DOMUtil = eXo.core.DOMUtil ;
	var portletWindow = DOMUtil.findAncestorByClass(this, "UIResizeObject") ;
	
	var uiWindow = eXo.desktop.UIWindow ;
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
  var desktopWidth = uiPageDesktop.offsetWidth  ;
  var desktopHeight = uiPageDesktop.offsetHeight  ;
  var uiResizableBlock = DOMUtil.findDescendantsByClass(portletWindow, "div", "UIResizableBlock") ;
  if(portletWindow.maximized) {
    portletWindow.maximized = false ;
    portletWindow.style.top = uiWindow.posY + "px" ;
    if(eXo.core.I18n.isLT()) portletWindow.style.left = uiWindow.posX + "px" ;
    else portletWindow.style.right = uiWindow.posX + "px" ;
    portletWindow.style.width = uiWindow.originalWidth + "px" ;
		for(var i = 0; i < uiResizableBlock.length; i++) {
  	 if (uiResizableBlock[i].originalHeight) {
      uiResizableBlock[i].style.height = uiResizableBlock[i].originalHeight + "px" ;
  	 } else	{
  	 		uiResizableBlock[i].style.height = 400 + "px" ;
  	 }
    }
    this.className = "ControlIcon MaximizedIcon" ;
    
  } else {
    uiWindow.backupObjectProperties(portletWindow, uiResizableBlock) ;
    portletWindow.style.top = "0px" ;
    if(eXo.core.I18n.isLT()) portletWindow.style.left = "0px" ;
    else portletWindow.style.right = "0px" ;
    portletWindow.style.width = "100%" ;
		portletWindow.style.height = "auto" ;
    var delta = eXo.core.Browser.getBrowserHeight() - portletWindow.clientHeight ;
    for(var i = 0; i < uiResizableBlock.length; i++) {
			uiResizableBlock[i].style.height =  (parseInt(uiResizableBlock[i].clientHeight) + delta) + "px" ;
    }
    portletWindow.style.height = portletWindow.clientHeight + "px" ;
    portletWindow.maximized = true ;
    this.className = "ControlIcon RestoreIcon" ;
  }
	eXo.desktop.UIWindow.saveWindowProperties(portletWindow) ;
  // Re initializes the scroll tabs managers on the page
	eXo.portal.UIPortalControl.initAllManagers() ;
    eXo.calendar.UICalendarPortlet.fixForMaximize();
};
}

/*
 * Override Comobobox
 * TODO : remove this method when portal fix it
 */
UICombobox.prototype.init = function() {
	var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace");
	var uiCombobox = eXo.webui.UICombobox ;
	var comboList = eXo.core.DOMUtil.findDescendantsByClass(uiWorkingWorkspace,"input","UIComboboxInput");
	var i = comboList.length ;
	while(i--){
		comboList[i].value = eXo.core.DOMUtil.findPreviousElementByTagName(comboList[i],"input").value;
	  var onfocus = comboList[i].getAttribute("onfocus") ;
	  var onclick = comboList[i].getAttribute("onclick") ;
	  var onblur = comboList[i].getAttribute("onblur") ;
	  if(!onfocus) comboList[i].onfocus = uiCombobox.show ;
	  if(!onclick) comboList[i].onclick = uiCombobox.show ;
	  if(!onblur)  comboList[i].onblur = uiCombobox.correct ;
	}
};


//fix for onblur event on calendar
//For validating

UICombobox.prototype.correct = function() {	
	var UICombobox = eXo.webui.UICombobox ; 
	var value = this.value ;
	this.value = UICombobox.setValue(value) ;
	var hiddenField = eXo.core.DOMUtil.findPreviousElementByTagName(this,"input");
	hiddenField.value = this.value;
	UICombobox.hide();
} ;

UICombobox.prototype.setValue = function(value) {
	var value = String(value).trim().toLowerCase();
	var UICombobox = eXo.webui.UICombobox;
	var time = UICombobox.digitToTime(value);
	var hour = Number(time.hour);
	var min = Number(time.minutes);
	var timeFormat = UICombobox.getTimeFormat();
	var formatTime = "";
	if (timeFormat.am) {
		var am = String(timeFormat.am).toLowerCase();
		var pm = String(timeFormat.pm).toLowerCase();
		if (!time) {
			return UICombobox.defaultValue;
		}
		if (hour > 24) {
			hour = "0";
			formatTime = " AM";
		} else if (hour == 12 || hour == 24) {
			hour = "0"
			formatTime = " PM";
		} else if (hour > 12 && hour < 24) {
			hour = hour - 12;
			formatTime = " PM";
		} else {
			hour = time.hour;
			formatTime = " AM";
		}
	} else {
		if (!time) {
			return "12:00";
		}
		if (hour > 23)
			hour = "23";
		else
			hour = time.hour;
	}
	var strHour = hour < 10 ? "0" + Number(hour) : "" + hour;
	var strMinute = min < 10 ? "0" + Number(min) : "" + min;
	return strHour + ":" + strMinute + formatTime;
};

UICombobox.prototype.getTimeFormat= function() {
	var items = eXo.webui.UICombobox.items ;
	if (items.length <= 0) return {am:"AM", pm:"PM"} ;
	var first = eXo.core.DOMUtil.findFirstDescendantByClass(items[0], "div", "UIComboboxLabel").innerHTML ;
	var last =  eXo.core.DOMUtil.findFirstDescendantByClass(items[items.length - 1], "div", "UIComboboxLabel").innerHTML ;
	var am = first.match(/[A-Z]+/) ;
	var pm = last.match(/[A-Z]+/) ;
	return {am:am, pm:pm} ;
} ;

UICombobox.prototype.digitToTime = function(stringNo) {
	stringNo = new String(eXo.webui.UICombobox.getDigit(stringNo));
	var len = stringNo.length;
	var hour = 0;
	var minute = 0;
	if (len <= 0) {
		return false;
	}
	hour = Number(stringNo.substring(0, 2));
	minute = Number(stringNo.substring(2, 4));
	if (minute == 60) {
		hour = hour + 1;
		minute = 0;
		if (hour == 24) {
			hour = 0;
		}
	} else if (minute > 60) {
		minute = 0;
	}
	return { "hour" : hour, "minutes" : minute };
};

UICombobox.prototype.getDigit = function(stringNo) {
	var parsedNo = "";
	var index = stringNo.indexOf(':');
	for ( var n = 0; n < stringNo.length; n++) {
		var i = stringNo.substring(n, n + 1);
		if (i == "1" || i == "2" || i == "3" || i == "4" || i == "5" || i == "6" || i == "7" || i == "8" || i == "9" || i == "0")
			parsedNo += i;
	}
	if (parsedNo.length == 1) {
		parsedNo = "0" + parsedNo + "00"
	} else if (parsedNo.length == 2) {
		if (index == 1) {
			parsedNo = "0" + parsedNo + "0";
		} else {
			parsedNo = parsedNo + 00;
		}
	} else if (parsedNo.length == 3) {
		if (index == 1) {
			parsedNo = "0" + parsedNo;
		} else {
			parsedNo = parsedNo + "0";
		}
	} else if (parsedNo.length >= 4) {
		parsedNo = parsedNo.substring(0, 4);
	}
	return parsedNo;
};

UICalendarPortlet.prototype.useAuthenticationForRemoteCalendar = function(id) {
  var USE_AUTHENTICATION = "useAuthentication";
  var DIV_USERNAME_ID = "id-username";
  var DIV_PASSWORD_ID = "id-password"; 
  var TXT_USERNAME_ID = "username";
  var TXT_PASSWORD_ID = "password";
  var labelClass = "InputFieldLabel";
  
  var divRemoteCalendar = eXo.calendar.UICalendarPortlet.getElementById(id);
  var divUseAuthentication = eXo.core.DOMUtil.findDescendantById(divRemoteCalendar, USE_AUTHENTICATION);
  var chkUseAuthentication = eXo.core.DOMUtil.findFirstDescendantByClass(divUseAuthentication, "input", "checkbox");
  var divUsername = eXo.core.DOMUtil.findDescendantById(divRemoteCalendar, DIV_USERNAME_ID);
  var lblUsername = eXo.core.DOMUtil.findFirstDescendantByClass(divUsername, "span", labelClass);
  var txtUsername = eXo.core.DOMUtil.findDescendantById(divUsername, TXT_USERNAME_ID);
  
  var divPassword = eXo.core.DOMUtil.findDescendantById(divRemoteCalendar, DIV_PASSWORD_ID);
  var lblPassword = eXo.core.DOMUtil.findFirstDescendantByClass(divPassword, "span", labelClass);
  var txtPassword = eXo.core.DOMUtil.findDescendantById(divPassword, TXT_PASSWORD_ID);
  chkUseAuthentication.onclick = function(){
    if(this.checked){
      txtUsername.removeAttribute('disabled');
      txtPassword.removeAttribute('disabled');
      lblUsername.style.color = 'black';
      lblPassword.style.color = 'black';                   
    } else {
      txtUsername.disabled = true;
      txtPassword.disabled = true;
      lblUsername.style.color = 'gray';
      lblPassword.style.color = 'gray';
    }   
  };  
} ;

UICalendarPortlet.prototype.editRepeat = function(id) {
  var eventForm = eXo.calendar.UICalendarPortlet.getElementById(id);
  var portletFragment = eXo.core.DOMUtil.findAncestorByClass(eventForm,"PORTLET-FRAGMENT");    
  var repeatContainer = eXo.core.DOMUtil.findDescendantById(eventForm, "repeatContainer");
  var repeatCheck = eXo.core.DOMUtil.getChildrenByTagName(repeatContainer, "input")[0];
  var summary = eXo.core.DOMUtil.findFirstDescendantByClass(repeatContainer, "span", "repeatSummary");
  var editButton = eXo.core.DOMUtil.getChildrenByTagName(repeatContainer, "a")[0];
  
  if (repeatCheck.checked) {
    editButton.style.display = "";
  } else {
    editButton.style.display="none";
  }
  
  repeatCheck.onclick = function() {
    if (repeatCheck.checked) {
      repeatCheck.checked = false;
      eXo.webui.UIForm.submitForm(portletFragment.parentNode.id + '#UIEventForm','EditRepeat', true); 
    } else {
      summary.innerHTML = "";
      editButton.style.display = "none";
    }
  };
}

UICalendarPortlet.prototype.changeRepeatType = function(id) {
  var weeklyByDayClass = "weeklyByDay";
  var monthlyTypeClass = "monthlyType";
  var RP_END_AFTER = "endAfter";
  var RP_END_NEVER = "neverEnd";
  var RP_END_BYDATE = "endByDate";
  
  var repeatingEventForm = eXo.calendar.UICalendarPortlet.getElementById(id);
  var weeklyByDay = eXo.core.DOMUtil.findFirstDescendantByClass(repeatingEventForm, "tr", weeklyByDayClass);
  var monthlyType = eXo.core.DOMUtil.findFirstDescendantByClass(repeatingEventForm, "tr", monthlyTypeClass);
  var repeatTypeSelectBox = eXo.core.DOMUtil.findFirstDescendantByClass(repeatingEventForm, "select", "selectbox");
  var repeatType = repeatTypeSelectBox.options[repeatTypeSelectBox.selectedIndex].value;
  var endNever = eXo.core.DOMUtil.findDescendantById(repeatingEventForm, "endNever");
  var endAfter = eXo.core.DOMUtil.findDescendantById(repeatingEventForm, "endAfter");
  var endByDate = eXo.core.DOMUtil.findDescendantById(repeatingEventForm, "endByDate");
  var hiddenEndType = eXo.core.DOMUtil.findDescendantById(repeatingEventForm, "endRepeat");
  var endByDateContainer = eXo.core.DOMUtil.findDescendantById(repeatingEventForm, "endByDateContainer");
  var endDateContainer = eXo.core.DOMUtil.findDescendantById(endByDateContainer,  "endDate");
  var endDate = eXo.core.DOMUtil.getChildrenByTagName(endDateContainer, "input")[0];
  var count = eXo.core.DOMUtil.findDescendantById(repeatingEventForm, "endAfterNumber");
  
  endDate.disabled = true;
  count.disabled = true;
  
  if (endAfter.checked) {
    count.disabled = false;
  }
  
  if (endByDate.checked) {
    endDate.disabled = false;
  }
  
  repeatTypeSelectBox.onchange = function() {
    var type = repeatTypeSelectBox.options[repeatTypeSelectBox.selectedIndex].value;
    if (type == "weekly") {
      monthlyType.style.display = 'none';
      weeklyByDay.style.display = '';
    } else { 
      if (type == "monthly") {
        monthlyType.style.display = '';
        weeklyByDay.style.display = 'none';
      } else {
        monthlyType.style.display = 'none';
        weeklyByDay.style.display = 'none';
      }
    }
  };
  
  endNever.onclick = function() {
    hiddenEndType.value = RP_END_NEVER;
    count.disabled = true;
    endDate.disabled = true;
  }
  
  endAfter.onclick = function() {
    hiddenEndType.value = RP_END_AFTER;
    count.disabled = false;
    if (count.value == null || count.value == "") count.value = 5;
    endDate.disabled = true;
  }

  endByDate.onclick = function() {
    hiddenEndType.value = RP_END_BYDATE;
    count.disabled = true;
    endDate.disabled = false;
    if (endDate.value == null || endDate.value == "") endDate.value = "";
  }

};

