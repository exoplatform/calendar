
/** 
 * Class to cover common actions of Calendar portlet
 * @author <a href="mailto:dung14000@gmail.com">Hoang Manh Dung</a>
 * @constructor
 */
eXo.calendar = eXo.calendar || {};

function UICalendarPortlet(){
	this.clickone = 0 ;
	this.portletId = "calendars";
	this.currentDate = 0;
	this.CELL_HEIGHT = 20;
	if(base.Browser.webkit != 0) this.CELL_HEIGHT = 21;
	this.MINUTE_PER_CELL = 30;
	this.PIXELS_PER_MINUTE = this.CELL_HEIGHT / this.MINUTE_PER_CELL; 
	this.MINUTES_PER_PIXEL = this.MINUTE_PER_CELL / this.CELL_HEIGHT;
}

UICalendarPortlet.prototype.onLoad = function(param){
	eXo.calendar = eXo.calendar || {};
	
	_module.restContext = param.restContext;
	_module.settingTimezone = param.settingTimezone;
}

/**
 * compute minutes from pixels in height of a event.
 */
UICalendarPortlet.prototype.pixelsToMins = function(pixels) {
  var UICalendarPortlet = _module.UICalendarPortlet;
  return Math.round(pixels/UICalendarPortlet.CELL_HEIGHT) * UICalendarPortlet.MINUTE_PER_CELL;
};

/**
 * compute pixels in height of a event from minutes.
 */
UICalendarPortlet.prototype.minsToPixels = function(minutes) {
  var UICalendarPortlet = _module.UICalendarPortlet;
  return Math.round(minutes / UICalendarPortlet.MINUTE_PER_CELL) * UICalendarPortlet.CELL_HEIGHT;
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
    var items = gj(component).find('div.' + className);
    var i = items.length;
    while(i--){
    	gj(items[i]).on({'mouseover':function(){
    		cs.Utils.swapClass(this,hoverClass);
    	},
    	'mouseout':function(){
    		cs.Utils.swapClass(this,hoverClass);
    	}});
    };
} ;


UICalendarPortlet.prototype.makeRequest = function(url,callback) {
	gj.ajax({
	    type: "get",
	    url: url,
	    cache: false,
	    success: function(data, status, jqXHR) {
	      if (callback) {
	        callback(jqXHR);
	      }
	    }
	  });
};

UICalendarPortlet.prototype.notify = function(eventObj){
	var Reminder = _module.Reminder ;
	var uiCalendarWorkingContainer = gj(eventObj).parents("#UICalendarWorkingContainer")[0];
	var msg = "<div style='padding:3px;color:red;'>" + uiCalendarWorkingContainer.getAttribute("msg") + "</div>";
	var html = Reminder.generateHTML(msg) ;
	var popup = gj(Reminder.createMessage(html, msg)).find('div.UIPopupNotification')[0];
	eXo.calendar.Box.config(popup,popup.offsetHeight, 5, Reminder.openCallback, Reminder.closeBox) ;
	window.focus() ;
	return ;
};

UICalendarPortlet.prototype.getOrginalPosition = function(eventObj){
	if(eventObj.getAttribute("orginalSize")){
		return eventObj.getAttribute("orginalSize");
	}
};

UICalendarPortlet.prototype.setPosition = function(eventObj){
	var me = _module.UICalendarPortlet ;
	me.activeEventObject = eventObj ;
	var cTop = gj(eventObj).top;
	var cLeft = gj(eventObj).offset().left;
	var cWdith = gj(eventObj).width();
	var cHeight =gj(eventObj).height();
	var cTitle = gj(eventObj).find('div.EventTitle')[0].innerHTML; 
	var cInnerHeight = gj(eventObj).find('div.EventContainer').height() ;
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
	var eventTitle = gj(eventObj).find('div.EventTitle')[0]; 
	var eventContainer = gj(eventObj).find('div.EventContainer')[0];
	if(this.restoreTitle && eventTitle) eventTitle.innerHTML = this.restoreTitle ;
	if(this.restoreContainerHeight && eventContainer) eventContainer.style.height = this.restoreContainerHeight ;
	this.restoreSize = null ;
	this.activeEventObject = null ;
	this.dropCallback = null ;
	this.restoreTitle = null ;
};

UICalendarPortlet.prototype.postCheck = function(response){
	var me = _module.UICalendarPortlet ;
	gj.globalEval("var data = " + response.responseText);
	var isEdit = data.permission;
	if(!isEdit){
		me.notify(me.activeEventObject);		
		me.restorePosition(me.activeEventObject);
	} else{
		if(me.dropCallback) me.dropCallback();
		delete me.activeEventObject ;
		delete me.restoreSize;
	}
};

UICalendarPortlet.prototype.checkPermission = function(eventObj){
	var calId = eventObj.getAttribute("calid");
	var calType = eventObj.getAttribute("calType");
	var baseURL  = (_module.restContext)?eXo.env.portal.context+ '/' + _module.restContext +'/cs/calendar/checkPermission/':'portal/rest/cs/calendar/checkPermission/';
	var url = baseURL + cs.CSCometd.exoId +"/"+ calId +"/"+ calType +"/";
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
		if(chks[i].checked && gj(chks[i]).hasClass("checkbox")) checkedCalendars.push(chks[i]); 
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
	var CalendarWorkingWorkspace =  _module.UICalendarPortlet.getElementById("UICalendarWorkingContainer");
    var id = (id)?id:this.getCheckedCalendar(this.filterForm);
    var UIQuckAddEventPopupWindow = gj(CalendarWorkingWorkspace).find("#UIQuckAddEventPopupWindow")[0];
    var UIQuckAddTaskPopupWindow = gj(CalendarWorkingWorkspace).find("#UIQuckAddTaskPopupWindow")[0];
    var selectedCategory = (_module.UICalendarPortlet.filterSelect) ? _module.UICalendarPortlet.filterSelect : null;
	// There is at least 1 event category to show event form
	if((selectedCategory != null) && (selectedCategory.options.length < 1)) {
    	var divEventCategory = gj(_module.UICalendarPortlet.filterSelect).parents(".EventCategory")[0] ;
    	alert(divEventCategory.getAttribute("msg")) ;
    	return;
    }
		
		var tmpMenuElement = document.getElementById("tmpMenuElement");
		if (tmpMenuElement) base.UIPopup.hide(tmpMenuElement) ;
    
		var formater = cs.DateTimeFormater ;
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
    	var uiform = gj(UIQuckAddEventPopupWindow).find("#UIQuickAddEvent")[0] ;
    	uiform.reset() ;
    	this.fillData(uiform, data) ;
    	webui.UIPopupWindow.show("UIQuckAddEventPopupWindow");
    	base.UIPopup.hide("UIQuckAddTaskPopupWindow") ;
    } else if(type == 2) {
    	var uiform = gj(UIQuckAddTaskPopupWindow).find("#UIQuickAddTask")[0] ;
    	uiform.reset() ;
    	this.fillData(uiform, data) ;
    	webui.UIPopupWindow.show("UIQuckAddTaskPopupWindow");
    	base.UIPopup.hide("UIQuckAddEventPopupWindow");
    }
} ;
/**
 * fill data to quick event/task form
 * @param {uiform, data} uifrom obj or id, data is array of value for each element of form
 */
UICalendarPortlet.prototype.fillData = function(uiform, data) {
	uiform = (typeof uiform == "string") ? _module.UICalendarPortlet.getElementById(uiform):uiform;
	var fromField = uiform.elements["from"] ;
	var fromFieldTime = uiform.elements["fromTime"] ;
	var toField = uiform.elements["to"] ;
	var toFieldTime = uiform.elements["toTime"] ;
	var isAllday = uiform.elements["allDay"] ;
	var calendar = uiform.elements["calendar"]; 
	var category = uiform.elements["category"] ;
	var eventName = uiform.elements["eventName"];
	var description = uiform.elements["description"];
	
	var formater = cs.DateTimeFormater ;
	var timeType = "HH:MM" ;
	var dateType = fromField.getAttribute("format").replace("MM","mm") ;
	if(this.timeFormat == "hh:mm a")  timeType = formater.masks.shortTime ;
	eventName.value = "";
	description.value = "";
	fromField.value = formater.format(data.from, dateType);
	fromFieldTime.style.visibility= "visible";
	fromFieldTime.value = formater.format(data.fromTime, timeType);	
	gj(fromFieldTime).nextAll("input")[0].value = formater.format(data.fromTime, timeType);
	toField.value = formater.format(data.to, dateType);
	toFieldTime.style.visibility = "visible";
	toFieldTime.value = formater.format(data.toTime, timeType);
	gj(toFieldTime).nextAll("input")[0].value = formater.format(data.toTime, timeType);
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
    if (_module.UICalendarPortlet.timeFormat != "hh:mm a") 
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
    var UICalendarPortlet = _module.UICalendarPortlet;
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
  if(_module.UICalendarPortlet.getElementById("UIWeekView")){
    var obj = _module.UICalendarPortlet.getElementById("UIWeekViewGrid") ;
    var container = gj(obj).parents(".EventWeekContent")[0] ;
  }
  else if(_module.UICalendarPortlet.getElementById("UIDayView")){
    var obj = _module.UICalendarPortlet.getElementById("UIDayView") ;
		obj = gj(obj).find("div.EventBoardContainer")[0];
    var container = gj(obj).parents(".EventDayContainer")[0];
  } else return ;
  var events = gj(obj).find("div.EventContainerBorder");
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
    var UICalendarPortlet = _module.UICalendarPortlet;
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
        cs.DOMUtil.cleanUpHiddenElements();
        obj.style.display = "block";
        gj(obj).on({'mouseover':_module.UICalendarPortlet.autoHide,
        	'mouseout':_module.UICalendarPortlet.autoHide});
//        obj.onmouseover = _module.UICalendarPortlet.autoHide;
//        obj.onmouseout = _module.UICalendarPortlet.autoHide;
        cs.DOMUtil.listHideElements(obj);
    }
    else {
        obj.style.display = "none";
    }
};


UICalendarPortlet.prototype.switchLayoutCallback = function(layout,status){
  var UICalendarPortlet = _module.UICalendarPortlet;
  var layoutMan = _module.LayoutManager;
  var layoutcookie = base.Browser.getCookie(layoutMan.layoutId);
  UICalendarPortlet.checkLayoutCallback(layoutcookie);
  if(gj.browser.mozilla != undefined && UICalendarPortlet.getElementById("UIWeekView") && (layout == 1)) 
	  _module.UIWeekView.onResize();
  if(gj.browser.mozilla != undefined && UICalendarPortlet.getElementById("UIMonthView") && (layout == 1)) 
	  _module.UICalendarMan.initMonth();
};

UICalendarPortlet.prototype.checkLayoutCallback = function(layoutcookie){
  var CalendarLayout = _module.CalendarLayout;
  CalendarLayout.updateCalendarContainerLayout();
  CalendarLayout.updateMiniCalendarLayout();
  CalendarLayout.updateUICalendarsLayout();
};

UICalendarPortlet.prototype.resetSpaceDefaultLayout = function(){
  _module.UICalendarPortlet.switchLayout(1);
};

UICalendarPortlet.prototype.resetLayoutCallback = function(){
  var UICalendarPortlet = _module.UICalendarPortlet;
  if(UICalendarPortlet.isSpace != "null") {
    UICalendarPortlet.resetSpaceDefaultLayout();
    return;
  }
  var layoutMan = _module.LayoutManager;
  var layoutcookie = base.Browser.getCookie(layoutMan.layoutId);
  UICalendarPortlet.checkLayoutCallback(layoutcookie);
  if(gj.browser.mozilla != undefined && _module.UICalendarPortlet.getElementById("UIWeekView")) _module.UIWeekView.onResize();
  if(gj.browser.mozilla != undefined && _module.UICalendarPortlet.getElementById("UIMonthView")) _module.UICalendarMan.initMonth();  
};

/**
 * Check layout configuration when page load to render a right layout
 */
UICalendarPortlet.prototype.checkLayout = function(){
	if(_module.UICalendarPortlet.isSpace != "null") base.Browser.setCookie(_module.LayoutManager.layoutId,"1",1);
	_module.LayoutManager.layouts = [] ;
	_module.LayoutManager.switchCallback = _module.UICalendarPortlet.switchLayoutCallback;
	_module.LayoutManager.resetCallback = _module.UICalendarPortlet.resetLayoutCallback;
	_module.LayoutManager.check();
};

/** 
 * Switch among types of layout
 * @param {Int} layout Layout value in order number
 */
UICalendarPortlet.prototype.switchLayout = function(layout){
	var layoutMan = _module.LayoutManager ;
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
        var UICalendarPortlet = _module.UICalendarPortlet;
        var uiDayViewGrid = _module.UICalendarPortlet.getElementById("UIDayViewGrid");
        if (!uiDayViewGrid) 
            return false;
        UICalendarPortlet.viewer = gj(uiDayViewGrid).find('div.EventBoardContainer')[0];
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
    var elements = gj(viewer).find("div." + className);
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
	var UICalendarPortlet = _module.UICalendarPortlet;
    var start = parseInt(obj.getAttribute("startTime"));
  	var topY = UICalendarPortlet.minsToPixels(start);
    var end = parseInt(obj.getAttribute("endTime"));
    var eventContainer = gj(obj).find('div.EventContainer')[0];
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
	var busyIcon = gj(obj).children("div")[0] ;
	if(!busyIcon ||  (busyIcon.offsetHeight <= 5)) 
		busyIcon = gj(obj).find("div.EventContainerBar")[0] ;
	var extraHeight = busyIcon.offsetHeight + gj(obj).find("div.ResizeEventContainer")[0].offsetHeight;
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
    var UICalendarPortlet = _module.UICalendarPortlet;
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
                  setLeft(el[j],offsetLeft + parseFloat(el[j - 1].style.width) * n);
            else {
                  setLeft(el[j],offsetLeft);
            }
            n++;
        }
    }
    function setLeft(obj,left){
		obj.style.left = left + "%";
		if(base.I18n.isRT()){
			obj.style.right = left + "%";	
		}
	}
};
/**
 * Sort event elemnents in time table
 */
UICalendarPortlet.prototype.showEvent = function(){
    this.init();
    var EventDayContainer = gj(this.viewer).parents(".EventDayContainer")[0];
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
        gj(el[i]).on({'mouseover':eXo.calendar.EventTooltip.show,
        	'mouseout':eXo.calendar.EventTooltip.hide,
        	'mousedown':_module.UICalendarPortlet.initDND,
        	'dblclick':_module.UICalendarPortlet.ondblclickCallback});
//        el[i].onmouseover = eXo.calendar.EventTooltip.show;
//        el[i].onmouseout = eXo.calendar.EventTooltip.hide;
//        el[i].onmousedown = _module.UICalendarPortlet.initDND;
//	    el[i].ondblclick = _module.UICalendarPortlet.ondblclickCallback;        
        marker = gj(el[i]).children("div.ResizeEventContainer")[0];
        gj(marker).on('mousedown',eXo.calendar.UIResizeEvent.init);
//        marker.onmousedown = eXo.calendar.UIResizeEvent.init;
    }
    this.items = el;
    this.adjustWidth(this.items);
    this.items = null;
    this.viewer = null;
};

UICalendarPortlet.prototype.editAlldayEvent = function(cont){
	cont = gj(cont).prevAll("div")[0];
	var events = gj(cont).find("div.EventContainerBorder");
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
    if (!_module.UICalendarPortlet.items) 
        return;
    _module.UICalendarPortlet.adjustWidth(_module.UICalendarPortlet.items);
}

/**
 * Callback method when double click on a calendar event
 */
UICalendarPortlet.prototype.ondblclickCallback = function(evt){
	evt.stopPropagation();
    var eventId = this.getAttribute("eventId");
    var calendarId = this.getAttribute("calid");
    var calendarType = this.getAttribute("caltype");
	var isOccur = this.getAttribute("isOccur");
	var recurId = this.getAttribute("recurId");
	if (recurId == "null") recurId = "";
    webui.UIForm.submitEvent(_module.UICalendarPortlet.portletId + '#' + _module.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + _module.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType + '&isOccur=' + isOccur + '&recurId=' + recurId);
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
  _e.stopPropagation();
  var UIResizeEvent = eXo.calendar.UIResizeEvent;
  var outerElement = gj(this).parents('.EventBoxes')[0];
  var innerElement = gj(this).prevAll("div")[0];
  var container = gj(outerElement).parents('.EventDayContainer')[0];
  var minHeight = 15;
  var interval = _module.UICalendarPortlet.interval;
  UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval);
    //UIResizeEvent.callback = UIResizeEvent.resizeCallback;
	_module.UICalendarPortlet.dropCallback = UIResizeEvent.resizeCallback;
	_module.UICalendarPortlet.setPosition(outerElement);
	eXo.calendar.EventTooltip.disable(evt);
};

UIResizeEvent.prototype.getOriginalHeight = function(obj){
	var paddingTop = Number(gj(obj).css('paddingTop').match(/\d+/)); 
	var paddingBottom = Number(gj(obj).css('paddingBottom').match(/\d+/)); 
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
    _module.UICalendarPortlet.resetZIndex(this.outerElement);
    this.minHeight = (minHeight) ? parseInt(minHeight) : 15;
    this.interval = (interval != "undefined") ? parseInt(interval) : 15;
    gj(document).on({'mousemove':UIResizeEvent.execute,
    	'mouseup':UIResizeEvent.end});
//    document.onmousemove = UIResizeEvent.execute;
//    document.onmouseup = UIResizeEvent.end;
    this.beforeHeight = this.getOriginalHeight(this.outerElement);
    this.innerElementHeight = this.getOriginalHeight(this.innerElement);
    this.posY = _e.clientY;
    this.uppermost = outerElement.offsetTop + minHeight - container.scrollTop;
    if (document.getElementById("UIPageDesktop")) {
        var uiWindow = gj(container).parents(".UIResizableBlock")[0];
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
    var mouseY = base.Browser.findMouseRelativeY(UIResizeEvent.container, _e);
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
		var min = (base.Browser.isIE6())?(UIResizeEvent.outerElement.offsetTop - 1) : UIResizeEvent.outerElement.offsetTop;
		_module.UICalendarPortlet.updateTitle(UIResizeEvent.outerElement, UIResizeEvent.outerElement.offsetTop, 1);
};

/**
 * End calendar event resizing, this method clean up some unused properties and execute callback function
 * @param {Object} evt Mouse event
 */
UIResizeEvent.prototype.end = function(evt){
	gj(document).off("mousemove mouseup");
//	document.onmousemove = null;
//  document.onmouseup = null;
  var _e = window.event || evt;
  var UIResizeEvent = eXo.calendar.UIResizeEvent;
	_module.UICalendarPortlet.checkPermission(UIResizeEvent.outerElement) ;
	eXo.calendar.EventTooltip.enable();
};

/**
 * Resizing callback method
 * @param {Object} evt Mouse object
 */
UIResizeEvent.prototype.resizeCallback = function(evt){
  var UICalendarPortlet = _module.UICalendarPortlet;
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
		var form = gj(eventBox).parents("form")[0];
	  form.elements[eventId + "startTime"].value = start;
	  form.elements[eventId + "finishTime"].value = end;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
		UICalendarPortlet.setTimeValue(eventBox,start,end);
		UICalendarPortlet.showEvent();
		gj.globalEval(actionLink);
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
        var items = gj(obj.parentNode).children("div");
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
  cs.EventManager.cancelBubble(evt);
	if(cs.EventManager.getMouseButton(evt) == 2) return ;
    var UICalendarPortlet = _module.UICalendarPortlet;
    UICalendarPortlet.dragObject = this;
    UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject);
    UICalendarPortlet.dragContainer = gj(UICalendarPortlet.dragObject).parents(".EventDayContainer")[0];
    UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject);
    UICalendarPortlet.eventY = _e.clientY;
    UICalendarPortlet.eventTop = UICalendarPortlet.dragObject.offsetTop;
    gj(UICalendarPortlet.dragContainer).on({'mousemove':UICalendarPortlet.dragStart,
    	'mouseup':UICalendarPortlet.dragEnd});
//    UICalendarPortlet.dragContainer.onmousemove = UICalendarPortlet.dragStart;
//    UICalendarPortlet.dragContainer.onmouseup = UICalendarPortlet.dragEnd;
    UICalendarPortlet.title = gj(UICalendarPortlet.dragObject).find("p")[0].innerHTML;
	UICalendarPortlet.dropCallback = UICalendarPortlet.dayviewDropCallback;
	UICalendarPortlet.setPosition(UICalendarPortlet.dragObject);
};
/**
 * Processes when dragging object
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.dragStart = function(evt){
    var _e = window.event || evt;
    var UICalendarPortlet = _module.UICalendarPortlet;
    var delta = null;
    var mouseY = base.Browser.findMouseRelativeY(UICalendarPortlet.dragContainer, _e) + UICalendarPortlet.dragContainer.scrollTop;
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
    var title = gj(events).find("p")[0];
		var delta = parseInt(events.getAttribute("endTime")) - parseInt(events.getAttribute("startTime")) ;
    timeFormat = (timeFormat) ? gj.globalEval(timeFormat) : {
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
	gj(this).off('mousemove');
//	this.onmousemove = null;
	var me = _module.UICalendarPortlet;
	var dragObject = me.dragObject;
	var eventTop = me.eventTop ;
	if (dragObject.offsetTop != eventTop) {
		me.checkPermission(dragObject);
	}
	eXo.calendar.EventTooltip.enable();
};

UICalendarPortlet.prototype.dayviewDropCallback = function(){
    //this.onmousemove = null;
    var UICalendarPortlet = _module.UICalendarPortlet;
    var dragObject = UICalendarPortlet.dragObject;
    var calType = dragObject.getAttribute("calType");
    var start = parseInt(dragObject.getAttribute("startTime"));
    var end = parseInt(dragObject.getAttribute("endTime"));
    var isOccur = dragObject.getAttribute("isoccur");
    var eventId = dragObject.getAttribute("eventid");
    var recurId = dragObject.getAttribute("recurid");
    if (recurId == "null") recurId = "";
    var title = gj(dragObject).find("p")[0];
    var titleName = UICalendarPortlet.title;
    if (end == 0) 
        end = 1440;
    var delta = end - start;
    var currentStart = UICalendarPortlet.pixelsToMins(dragObject.offsetTop);
    var currentEnd = currentStart + delta;
    var eventDayContainer = gj(dragObject).parents(".EventDayContainer")[0];
    //var eventTop = UICalendarPortlet.eventTop;
    gj(eventDayContainer).off("mousemove mouseup");
//    eventDayContainer.onmousemove = null;
//    eventDayContainer.onmouseup = null;
    UICalendarPortlet.dragObject = null;
    UICalendarPortlet.eventTop = null;
    UICalendarPortlet.eventY = null;
    UICalendarPortlet.dragContainer = null;
    UICalendarPortlet.title = null;
    //if (dragObject.offsetTop != eventTop) {
    var actionLink = dragObject.getAttribute("actionLink");    
    var form = gj(dragObject).parents("form")[0];
    form.elements[eventId + "startTime"].value = currentStart;
    form.elements[eventId + "finishTime"].value = currentEnd;
    form.elements[eventId + "isOccur"].value = isOccur;
    form.elements[eventId + "recurId"].value = recurId;
		_module.UICalendarPortlet.setTimeValue(dragObject,currentStart,currentEnd);
		_module.UICalendarPortlet.showEvent();
		gj.globalEval(actionLink);
    //}
    //title.innerHTML = titleName;
};

/* for showing context menu */
/**
 * Sets up context menu for Calendar portlet
 * @param {Object} compid Portlet id
 */
UICalendarPortlet.prototype.showContextMenu = function(compid){
    var UIContextMenu = cs.UIContextMenu;
		this.portletNode = gj(document.getElementById(compid)).parents(".PORTLET-FRAGMENT")[0];
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
    if ((gj.browser.msie != undefined) && isDesktop) {
        var portlet = this.portletNode;
        var uiResizeBlock = gj(portlet).parents(".UIResizableBlock")[0];
        var relative = gj(uiResizeBlock).find("div.FixIE")[0];
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
    if (!gj(src).hasClass("UIListViewRow")) 
        src = gj(src).parents(".UIListViewRow")[0];
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
    cs.UIContextMenu.changeAction(cs.UIContextMenu.menuElement, map);
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
        src = gj(src).parents("tr")[0];
        var startTime = parseInt(Date.parse(src.getAttribute('startFull')));
    	
        var startTime = parseInt(src.getAttribute("startTime"));
        var endTime = startTime + 15*60*1000 ;
        var items = gj(cs.UIContextMenu.menuElement).find("a");
        for(var i = 0; i < items.length; i++){
        	var aTag = items[i];
        	if(gj(aTag).hasClass("QuickAddEvent")) {
        		aTag.href="javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");" 
        	} else if(gj(aTag).hasClass("QuickAddTask")) {
        		aTag.href="javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");"
        	}
        }
        /*map = {
            "startTime\s*=\s*.*(?=&|'|\")": "startTime=" + startTime
        };*/
    }
    else {
        src = (gj(src).hasClass("EventBoxes")) ? src : gj(src).parents(".EventBoxes")[0];
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
    cs.UIContextMenu.changeAction(cs.UIContextMenu.menuElement, map);
};

/**
 * Callback method when right click in week view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.weekViewCallback = function(evt){
    var src = cs.EventManager.getEventTarget(evt);
    var UIContextMenu = cs.UIContextMenu;
    var map = null;
    var obj = cs.EventManager.getEventTargetByClass(evt,"WeekViewEventBoxes");
    var items = gj(UIContextMenu.menuElement).find("a");
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
        
		if(!gj(obj).hasClass("EventAlldayContainer")){
			var container = gj(src).parents(".EventWeekContent")[0];
			var mouseY = (base.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
			obj =parseInt(gj(src).parents("td")[0].getAttribute("startTime")) + mouseY;
		} else obj = null;
        for (var i = 0; i < items.length; i++) {
            if (gj(items[i].parentNode).hasClass("EventActionMenu")) {
                items[i].parentNode.style.display = "block";
                items[i].href = UIContextMenu.replaceall(String(items[i].href), map);
            }
            else {
                //TODO Menu on allday events
            	//items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/, "startTime=" + obj);
                /*var fTime = parseInt(obj);
                var tTime = fTime + 15*60*1000 ;*/
        		if(gj(items[i]).hasClass("QuickAddEvent")){
        			items[i].style.display="none" ;
        			/*items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this, 1,"+fTime+","+tTime+");"
        			 if(isNaN(fTime)) {
        				 items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHidden(this, 1);" ;
        		     } */
            		 
            	} else if (gj(items[i]).hasClass("QuickAddTask")) {
            		items[i].style.display="none" ;
            		/*items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this, 2, "+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHidden(this, 2);" ;
        		     } */
            	}
            }
        }
    } else {
		var container = gj(src).parents(".EventWeekContent")[0];
		var mouseY = (base.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
        obj = cs.EventManager.getEventTargetByTagName(evt,"td");
				map = Date.parse(obj.getAttribute("startFull"));
        for (var i = 0; i < items.length; i++) {
            if (items[i].style.display == "block") {
                items[i].style.display = "none";
            }
            else {
            	items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/, "startTime=" + map);
                var fTime = parseInt(map);
                var tTime = fTime + 15*60*1000 ;
                
            	if(gj(items[i]).hasClass("QuickAddEvent")){
            		items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this, 1,"+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHidden(this, 1);" ;
        		     } 
            	} else if (gj(items[i]).hasClass("QuickAddTask")) {
            		items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this, 2, "+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:_module.UICalendarPortlet.addQuickShowHidden(this, 2);" ;
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
    var UIContextMenu = cs.UIContextMenu;
    var objectValue = "";
    var links = gj(UIContextMenu.menuElement).find("a");
    if (!gj(src).parents(".EventBoxes")[0]) {
        if (objectValue = gj(src).parents("td")[0].getAttribute("startTime")) {
        	//TODO CS-2800
        	var startTime = parseInt(Date.parse(gj(src).parents("td")[0].getAttribute('startTimeFull')));
        	var endTime = parseInt(Date.parse(gj(src).parents("td")[0].getAttribute('startTimeFull')))  + 24*60*60*1000 - 1;
        	for(var i = 0; i < links.length; i++){
            	if(gj(links[i]).hasClass("QuickAddEvent")) {
            		links[i].href="javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");" 
            	} else if(gj(links[i]).hasClass("QuickAddTask")) {
            		links[i].href="javascript:_module.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");"
            	}
            }
        	/*var map = {
                "startTime\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "startTime=" + objectValue
            };
            UIContextMenu.changeAction(UIContextMenu.menuElement, map);*/
        }
    }
    else 
        if (objvalue = gj(src).parents(".DayContentContainer")[0]) {
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
    var checkbox = gj(form).find("input.checkbox");
    var el = new Array();
    var len = checkbox.length;
    var calid = null;
    for (var i = 0; i < len; i++) {
        if (checkbox[i].checked) {
            calid = checkbox[i].name;
            el.concat(this.getEventsByCalendar(events, calid));
        }
    }
    return el;
};

/**
 * Filters calendar event by calendar group
 */
UICalendarPortlet.prototype.filterByGroup = function(){
    var uiVtab = gj(this).parents(".UIVTab")[0];
    var checkboxes = gj(uiVtab).find("input.checkbox");
    var checked = this.checked;
    var len = checkboxes.length;
    for (var i = 0; i < len; i++) {
        _module.UICalendarPortlet.runFilterByCalendar(checkboxes[i].name, checked);
        if (checkboxes[i].checked == checked) 
            continue;
        checkboxes[i].checked = checked;
    }
	_module.UICalendarPortlet.runFilterByCategory();
	_module.UICalendarPortlet.resortEvents();
};

/**
 * Filters calendar event by calendar
 * @param {Object} calid Calendar id
 * @param {Boolean} checked Status of calendar(activated or disactivated)
 */
UICalendarPortlet.prototype.runFilterByCalendar = function(calid, checked){
    var uiCalendarViewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer");
    var UICalendarPortlet = _module.UICalendarPortlet;
    if (!uiCalendarViewContainer) 
        return;
    var className = "EventBoxes";
    if (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var events = gj(uiCalendarViewContainer).find("div." + className);
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
        if (_module.UICalendarPortlet.getElementById("UIMonthView")) 
            _module.UICalendarMan.initMonth();
        if (_module.UICalendarPortlet.getElementById("UIDayViewGrid")) 
            _module.UICalendarPortlet.showEvent();
        if (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) {
            _module.UICalendarMan.initWeek();
            _module.UIWeekView.init();
        }
    } 
    catch (e) {
    	//TODO
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
    
    var checkBox = gj(this).find('input.checkbox')[0];
    var checked = checkBox.checked;
    var imgChk = gj(this).find('span.checkbox')[0]; 
    
    var uiCalendarViewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer");
    var UICalendarPortlet = _module.UICalendarPortlet;
    if (!uiCalendarViewContainer) {
			return;    
    }
    var className = "EventBoxes";
    if (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")){
			className = "WeekViewEventBoxes";  
    }
    var events = gj(uiCalendarViewContainer).find('div.' + className);

    if(checked){
    	stylEvent = hide;
    	checkBox.checked = false;
    	imgChk.className = "IconUnCheckBox checkbox";
    }else{
    	checkBox.checked = true;
    	stylEvent = show;
    	imgChk.className = "IconCheckBox checkbox";
    }
    
    if ((!events || events.length == 0)&& _module.UICalendarPortlet.getElementById("UIListView")) {
        webui.UIForm.submitForm('UICalendars','Tick', true)		
    }
    if (!events) return;
    var len = events.length;
    
    for (var i = 0; i < len; i++) {
        if (events[i].getAttribute("calId") == calid) {
            events[i].style.display = stylEvent;
            var chkEvent = gj(events[i]).find('input.checkbox')[0]; 
            if (chkEvent) {
              chkEvent.checked = false;
              chkEvent.setAttribute('value', false);
            }
        }
    }
    
    //UICalendarPortlet.runFilterByCategory();
    _module.UICalendarPortlet.resortEvents();
    
};

/**
 * Filters events by event category
 */
UICalendarPortlet.prototype.filterByCategory = function(){
    var uiCalendarViewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer");
    if (!uiCalendarViewContainer) 
        return;
    var category = this.options[this.selectedIndex].value;
    _module.UICalendarPortlet.selectedCategory = category;
    var className = "EventBoxes";
    if (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var allEvents = gj(uiCalendarViewContainer).find("div." + className);
    var events = _module.UICalendarPortlet.getEventsForFilter(allEvents);
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
    _module.UICalendarPortlet.resortEvents();
};

/**
 * Filters event by event category
 * @param {Object} selectobj Select element
 */
UICalendarPortlet.prototype.runFilterByCategory = function(){
    var uiCalendarViewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer");
		selectobj = gj(uiCalendarViewContainer).find('select.selectbox')[0]; 
    if (!selectobj) return;
    var category = null ;
		if (selectobj.selectedIndex >= 0 ) category = selectobj.options[selectobj.selectedIndex].value;
    var className = "EventBoxes";
    if (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var allEvents = gj(uiCalendarViewContainer).find("div." + className);
    var events = _module.UICalendarPortlet.getEventsForFilter(allEvents);
    
     //CS-3152
    if ((!events || events.length == 0)&& _module.UICalendarPortlet.getElementById("UIListView")) {
        webui.UIForm.submitForm('UICalendars','Tick', true)		
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
	gj.globalEval(actionLink);
};


/**
 * Gets select element that contains event category and sets up filtering action by event category
 * @param {Object} form Form id contains event category select element
 */
UICalendarPortlet.prototype.getFilterSelect = function(form){
    if (typeof(form) == "string") 
        form = _module.UICalendarPortlet.getElementById(form);
    var eventCategory = gj(form).find("div.EventCategory")[0];
		if (!eventCategory) return ;
    var select = gj(eventCategory).find("select")[0];
    var onchange = select.getAttribute("onchange");
    if (!onchange)
    	
        gj(select).on('change', _module.UICalendarPortlet.filterByCategory);
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
	form = (typeof(form) == "string")? _module.UICalendarPortlet.getElementById(form):form ;
	if(!form) return ;
	var tr = gj(form).find("tr.UIListViewRow");
	var i = tr.length ;
	_module.UICalendarPortlet.viewType = "UIListView";
	var chk = null ;
	while(i--){
		tr[i].ondblclick = this.listViewDblClickCallback;		
//gj(tr[i]).on('dblclick',this.listViewDblClickCallback);
	}
};

UICalendarPortlet.prototype.doClick = function(){
	if(_module.UICalendarPortlet.dblDone){
		delete _module.UICalendarPortlet.dblDone;
		window.clearTimeout(_module.UICalendarPortlet.clickone);
		return ;
	}
	gj.globalEval(_module.UICalendarPortlet.listViewAction);
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
	webui.UIForm.submitEvent(_module.UICalendarPortlet.portletId+'#' + _module.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + _module.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType);
};

UICalendarPortlet.prototype.listViewDblClickCallback = function(){
	_module.UICalendarPortlet.dblDone = true;
	_module.UICalendarPortlet.ondblclickCallbackInListView(this);
};
/**
 * Filter event when page load
 */
UICalendarPortlet.prototype.checkFilter = function() {
  var UICalendarPortlet = _module.UICalendarPortlet;
  for ( var i = 0; i < UICalendarPortlet.filterSelect.options.length; i++) {
    if (UICalendarPortlet.filterSelect.options[i].value == UICalendarPortlet.selectedCategory) {
      UICalendarPortlet.filterSelect.options[i].selected = true;
    }
  }
  UICalendarPortlet.checkCalendarFilter();
  _module.UICalendarPortlet.resortEvents();
  UICalendarPortlet.setFocus();
  if (_module.UICalendarPortlet.firstLoadTimeout)
    delete _module.UICalendarPortlet.firstLoadTimeout;
};

/**
 * Filter event by calendar when page load
 */
UICalendarPortlet.prototype.checkCalendarFilter = function(){
    if (!this.filterForm) 
        return;
    var checkbox = gj(this.filterForm).find("input.checkbox");
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
        _module.UICalendarPortlet.runFilterByCategory();
};

/* EOF filter */
/**
 * Change among task and event view in list view
 * @param {Object} obj DOM element
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.switchListView = function(obj, evt){
    var menu = gj(obj).find('div.UIPopupCategory')[0];
    if (base.Browser.isIE6()) {
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
    base.UIPopupSelectCategory.show(obj, evt);
};

/**
 * Shows view menu
 * @param {Object} obj DOM element
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.showView = function(obj, evt){
//    var _e = window.event || evt;
//    _e.cancelBubble = true;
		cs.EventManager.cancelBubble(evt);
    var oldmenu = gj(obj).find('div.UIRightClickPopupMenu')[0];
    var actions = gj(oldmenu).find("a.ItemLabel");
    if (!this.selectedCategory) 
        this.selectedCategory = null;
    for (var i = 0; i < actions.length; i++) {
        if (actions[i].href.indexOf("categoryId") < 0) 
            continue;
        actions[i].href = String(actions[i].href).replace(/categoryId.*&/, "categoryId=" + this.selectedCategory + "&");
    }
    _module.UICalendarPortlet.swapMenu(oldmenu, obj);
};

/**
 * Swap menu in IE
 * @param {Object} menu Menu DOM element
 * @param {Object} clickobj Click DOM element
 */
UICalendarPortlet.prototype.swapIeMenu = function(menu, clickobj){
    var Browser = base.Browser;
    var x = Browser.findPosXInContainer(clickobj, menu.offsetParent) - cs.Utils.getScrollLeft(clickobj);
    var y = Browser.findPosYInContainer(clickobj, menu.offsetParent) - cs.Utils.getScrollTop(clickobj) + clickobj.offsetHeight;
    var browserHeight = document.documentElement.clientHeight;
    var uiRightClickPopupMenu = (!gj(menu).hasClass("UIRightClickPopupMenu")) ? gj(menu).find('div.UIRightClickPopupMenu')[0] : menu;
    this.showHide(menu);
    if ((y + uiRightClickPopupMenu.offsetHeight) > browserHeight) {
        y = browserHeight - uiRightClickPopupMenu.offsetHeight;
    }
    
    gj(menu).addClass("UICalendarPortlet UIEmpty");
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
    var Browser = base.Browser;
    var UICalendarPortlet = _module.UICalendarPortlet;
    var uiDesktop = document.getElementById("UIPageDesktop");
    if (document.getElementById("tmpMenuElement")) 
        gj("#tmpMenuElement").remove();  
/*
    var tmpMenuElement = oldmenu.cloneNode(true); 
    tmpMenuElement.setAttribute("id", "tmpMenuElement");
    tmpMenuElement.style.zIndex = 1 ;
    this.menuElement = tmpMenuElement;
 	if(Browser.isIE6()) this.menuElement.style.width = "140px";
    document.body.appendChild(this.menuElement);
*/
    var tmpMenuElement = gj(oldmenu).clone(true,true);
    tmpMenuElement.attr("id","tmpMenuElement");
    var style = tmpMenuElement.attr("style") + "zIndex = 1;";
    if(Browser.isIE6())
    	style = style + "width = 140px;";
    tmpMenuElement.attr("style",style) ;
    gj('body').append(tmpMenuElement);
    this.menuElement = document.getElementById("tmpMenuElement");

    if (uiDesktop) {
        this.swapIeMenu(this.menuElement, clickobj);
        return;
    }
    
	gj(this.menuElement).addClass("UICalendarPortlet UIEmpty");
    var menuX = base.Browser.findPosX(clickobj) ;
    var menuY = cs.Browser.findPosY(clickobj) + clickobj.offsetHeight;
    if (arguments.length > 2) {
        menuY -= arguments[2].scrollTop;
    }
		if (base.I18n.isRT()) {
      menuX -= (cs.Utils.getElementWidth(this.menuElement) - clickobj.offsetWidth);// - uiWorkSpaceWidth;      
    }
    this.menuElement.style.top = menuY + "px";
    this.menuElement.style.left =  menuX + "px";
 		if (base.I18n.isRT() && Browser.isIE6()) {
      menuX = Browser.findPosXInContainer(clickobj,this.menuElement.offsetParent,true);
      //menuX += uiWorkSpaceWidth/2 ;
      this.menuElement.style.right = menuX + "px";
      this.menuElement.style.left =  "";
    }
    this.showHide(this.menuElement);
    var uiRightClick = (gj(UICalendarPortlet.menuElement).find('div.UIRightClickPopupMenu')[0]) ? gj(UICalendarPortlet.menuElement).find('div.UIRightClickPopupMenu')[0] : UICalendarPortlet.menuElement;
    var mnuBottom = UICalendarPortlet.menuElement.offsetTop + uiRightClick.offsetHeight - window.document.documentElement.scrollTop;
    if (window.document.documentElement.clientHeight < mnuBottom) {
        menuY += (window.document.documentElement.clientHeight - mnuBottom);
        UICalendarPortlet.menuElement.style.top = menuY + "px";
    }
    
};

UICalendarPortlet.prototype.isAllday = function(form,selecedCalendarID){
  try {
      if (typeof(form) == "string") 
          form = _module.UICalendarPortlet.getElementById(form);
      if (form.tagName.toLowerCase() != "form") {
          form = gj(form).find("form")[0];
      }
      for (var i = 0; i < form.elements.length; i++) {
          if (form.elements[i].getAttribute("name") == "allDay") {
              _module.UICalendarPortlet.allDayStatus = form.elements[i];
              _module.UICalendarPortlet.showHideTime(form.elements[i]);
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
    if (chk.tagName.toLowerCase() != "input") {
        chk = gj(chk).find('input.checkbox')[0];
    }
    var selectboxes = gj(chk.form).find("input");
    var fields = new Array();
    var len = selectboxes.length;
    for (var i = 0; i < len; i++) {
        if (selectboxes[i].className == "UIComboboxInput") {
            fields.push(selectboxes[i]);
        }
    }
    _module.UICalendarPortlet.showHideField(chk, fields);
};

/**
 * Show/hide field in form
 * @param {Object} chk Checkbox element
 * @param {Object} fields Input field in form
 */
UICalendarPortlet.prototype.showHideField = function(chk, fields){
    var display = "";
    if (typeof(chk) == "string")
        chk = _module.UICalendarPortlet.getElementById(chk);  
    display = (chk.checked) ? "hidden" : "visible";
    var len = fields.length;
    for (var i = 0; i < len; i++) {
        fields[i].style.visibility = display;
        i
    }
};

UICalendarPortlet.prototype.showHideRepeat = function(chk){
    var checkbox = gj(chk).find('input.checkbox')[0];
    var fieldCom =gj(chk).parents(".FieldComponent")[0];
    var repeatField = gj(fieldCom).find('div.RepeatInterval')[0];
		if (checkbox.checked) {
	    repeatField.style.visibility = "visible";
		} else {
	    repeatField.style.visibility = "hidden";
		}
};

UICalendarPortlet.prototype.autoShowRepeatEvent = function(){
	var divEmailObject = document.getElementById("IsEmailRepeatEventReminderTab");
    var checkboxEmail = gj(divEmailObject).find('input.checkbox')[0]; 
    var fieldComEmail = gj(divEmailObject).parents(".FieldComponent")[0];
    var repeatFieldEmail = gj(fieldComEmail).find('div.RepeatInterval')[0]; 
		if (checkboxEmail.checked) {
	    repeatFieldEmail.style.visibility = "visible";
		} else {
	    repeatFieldEmail.style.visibility = "hidden";
		}
    
    var divObjectPopup = document.getElementById("IsPopupRepeatEventReminderTab");
    var checkboxPopup = gj(divObjectPopup).find('input.checkbox')[0];
    var fieldComPopup = gj(divObjectPopup).parents(".FieldComponent")[0];
    var repeatFieldPopup = gj(fieldComPopup).find('div.RepeatInterval')[0];
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
  var UICalendarPortlet = _module.UICalendarPortlet;
    var UISelection = eXo.calendar.UISelection;
    var container = gj(_module.UICalendarPortlet.getElementById("UIDayViewGrid")).find('div.EventBoard')[0];
    UISelection.step = UICalendarPortlet.CELL_HEIGHT;
    UISelection.container = container;
    UISelection.block = document.createElement("div");
    UISelection.block.className = "UserSelectionBlock";
    UISelection.container.appendChild(UISelection.block);
    gj(UISelection.container).on('mousedown',UISelection.start);
//    UISelection.container.onmousedown = UISelection.start;
    UISelection.relativeObject = gj(UISelection.container).parents(".EventDayContainer")[0];
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
		if(base.Browser.webkit != 0) UISelection.step = 21;
        var src = cs.EventManager.getEventTarget(evt);

        if ((src == UISelection.block) || (cs.EventManager.getMouseButton(evt) == 2) || (gj(src).hasClass("TdTime"))) {
			return;
        }
        
        UISelection.startTime = parseInt(Date.parse(src.getAttribute("startFull")));//src.getAttribute("startTime");
        UISelection.startX = base.Browser.findPosXInContainer(src, UISelection.container) - _module.UICalendarPortlet.portletNode.parentNode.scrollTop;
        UISelection.block.style.display = "block";
        UISelection.startY = base.Browser.findPosYInContainer(src, UISelection.container);
        UISelection.block.style.width = src.offsetWidth + "px";
        UISelection.block.style.left = UISelection.startX + "px";
        UISelection.block.style.top = UISelection.startY + "px";
        UISelection.block.style.height = UISelection.step + "px";
        UISelection.block.style.zIndex = 1;
        //_module.UICalendarPortlet.resetZIndex(UISelection.block);
        gj(document).on({'mousemove':UISelection.execute,
        	'mouseup':UISelection.clear});
//        document.onmousemove = UISelection.execute;
//        document.onmouseup = UISelection.clear;
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
    var scrollTop = cs.Utils.getScrollTop(UISelection.block);
    var mouseY = base.Browser.findMouseRelativeY(UISelection.container, _e);// + UISelection.relativeObject.scrollTop;
    if (document.getElementById("UIPageDesktop")) 
        mouseY = base.Browser.findMouseRelativeY(UISelection.container, _e) + scrollTop;
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
	var UICalendarPortlet = _module.UICalendarPortlet;
	var UISelection = eXo.calendar.UISelection;
	var endTime = UICalendarPortlet.pixelsToMins(UISelection.block.offsetHeight) * 60 * 1000 + parseInt(UISelection.startTime);
	var startTime = UISelection.startTime;
	var bottom = UISelection.block.offsetHeight + UISelection.block.offsetTop;

	if (UISelection.block.offsetTop < UISelection.startY) {
		startTime = parseInt(UISelection.startTime) - UICalendarPortlet.pixelsToMins(UISelection.block.offsetHeight) * 60 * 1000 + UICalendarPortlet.MINUTE_PER_CELL * 60 * 1000;
		endTime = parseInt(UISelection.startTime) + UICalendarPortlet.MINUTE_PER_CELL * 60 * 1000;
	}
	if(bottom >= UISelection.container.offsetHeight) endTime -= 1;
	var container = UICalendarPortlet.getElementById("UICalendarViewContainer");
	UICalendarPortlet.addQuickShowHiddenWithTime(container, 1, startTime, endTime) ;
	//eXo.webui.UIForm.submitEvent(UISelection.viewType, 'QuickAdd', '&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime);
	cs.DOMUtil.listHideElements(UISelection.block);
	UISelection.startTime = null;
	UISelection.startY = null;
	UISelection.startX = null;
	gj(document).off("mousemove mouseup");
//	document.onmousemove = null;
//	document.onmouseup = null;
};

// check free/busy time
/**
 * Checks free/busy in day of an user
 * @param {Object} chk Checkbox element
 */
UICalendarPortlet.prototype.checkAllInBusy = function(chk){
    var UICalendarPortlet = _module.UICalendarPortlet;
    var isChecked = chk.checked;
    var timeField = gj(chk.form).find('div.TimeField')[0];
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
    if (typeof(container) == "string") 
        container = document.getElementById(container);
    var dateAll = gj(container).find("input.checkbox")[1];
    var table = gj(container).find('table.UIGrid')[0];
    var tr = gj(table).find("tr");
    var firstTr = tr[1];
    this.busyCell = gj(firstTr).find("td").slice(1);
    var len = tr.length;
    for (var i = 2; i < len; i++) {
        this.showBusyTime(tr[i], userSettingTimezone);
    }
    if (_module.UICalendarPortlet.allDayStatus) 
        dateAll.checked = _module.UICalendarPortlet.allDayStatus.checked;
    _module.UICalendarPortlet.checkAllInBusy(dateAll);
    gj(dateAll).on('click', function(){
        _module.UICalendarPortlet.checkAllInBusy(this);
    });
    _module.UICalendarPortlet.initSelectionX(firstTr);
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
    var cell = gj(tr).find("td").slice(1);
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
    cell = gj(tr).find("td.UICellBlock").slice(1);
    var len = cell.length;
    for (var i = 0; i < len; i++) {
    	gj(cell[i]).on('mousedown',eXo.calendar.UIHSelection.start);
//        cell[i].onmousedown = eXo.calendar.UIHSelection.start;//eXo.calendar.Highlighter.start ;
    }
};

/**
 * Gets AM/PM from input value
 * @param {Object} input Input contains time
 * @return Object contains two properties that are AM and PM
 */
UICalendarPortlet.prototype.getTimeFormat = function(input){
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
    var len = Math.abs(Highlighter.firstCell.cellIndex - Highlighter.lastCell.cellIndex - 1);
    var start = (Highlighter.firstCell.cellIndex - 1) * 15;
    var end = start + len * 15;
    var timeTable = gj(Highlighter.firstCell).parents("table")[0];
    var dateValue = timeTable.getAttribute("datevalue");
    var uiTabContentContainer = gj(Highlighter.firstCell).parents(".UITabContentContainer")[0];
    var UIComboboxInputs = gj(uiTabContentContainer).find("input.UIComboboxInput");
    len = UIComboboxInputs.length;
    var name = null;
    var timeFormat = this.getTimeFormat(this.synTime(UIComboboxInputs[0]));
    start = this.minToTime(start, timeFormat);
    end = this.minToTime(end, timeFormat);
    if (dateValue) {
        var DateContainer = gj(uiTabContentContainer).parents("form")[0];
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
    var cells = gj(Highlighter.firstCell.parentNode).children("td");
    Highlighter.setAttr(Highlighter.firstCell.cellIndex, Highlighter.lastCell.cellIndex, cells);
};

UICalendarPortlet.prototype.synTime = function(o,v){
	var ro = gj(o).prevAll("input")[0];
	if(!v) return ro;
	ro.value = v;
}

/**
 * Sets some properties of UICalendarPortlet object again when user changes setting
 * @param {Object} cpid Component id
 */
UICalendarPortlet.prototype.initSettingTab = function(cpid){
    var cp = _module.UICalendarPortlet.getElementById(cpid);
    var ck = gj(cp).find('input.checkbox')[0];
    var div = gj(ck).parents("div")[0];
    _module.UICalendarPortlet.workingSetting = gj(div).nextAll("div")[0];
    gj(ck).on('click',_module.UICalendarPortlet.showHideWorkingSetting);
//    ck.onclick = _module.UICalendarPortlet.showHideWorkingSetting;
    _module.UICalendarPortlet.checkWorkingSetting(ck);
}

/**
 * Check status of working time checkbox
 * @param {Object} ck Working time checkbox
 */
UICalendarPortlet.prototype.checkWorkingSetting = function(ck){
    var isCheck = ck.checked;
    if (isCheck) {
        _module.UICalendarPortlet.workingSetting.style.visibility = "visible";
    }
    else {
        _module.UICalendarPortlet.workingSetting.style.visibility = "hidden";
    }
}

/**
 * Show/hide working time setting
 */
UICalendarPortlet.prototype.showHideWorkingSetting = function(){
    var isCheck = this.checked;
    if (isCheck) {
        _module.UICalendarPortlet.workingSetting.style.visibility = "visible";
    }
    else {
        _module.UICalendarPortlet.workingSetting.style.visibility = "hidden";
    }
};

UICalendarPortlet.prototype.showImagePreview = function(obj){
	var img = gj(obj.parentNode).prevAll("img")[0];	
	var viewLabel = obj.getAttribute("viewLabel");
	var closeLabel = obj.getAttribute("closeLabel");
	if(img.style.display == "none"){
		img.style.display = "block";
		obj.innerHTML = closeLabel ;
		if(gj(obj).hasClass("ViewAttachmentIcon")) {
			gj(obj).removeClass("ViewAttachmentIcon");
			gj(obj).addClass("CloseAttachmentIcon");
		}
	}else {
		img.style.display = "none";
		obj.innerHTML = viewLabel ;
		if(gj(obj).hasClass("CloseAttachmentIcon")) {
			gj(obj).removeClass("CloseAttachmentIcon");
			gj(obj).addClass("ViewAttachmentIcon");
		}
	}
};

UICalendarPortlet.prototype.showHideSetting = function(obj){
	var checkbox = gj(obj).find('input.checkbox')[0]; 
	var uiFormGrid = gj(obj.parentNode.parentNode).find('table.UIFormGrid')[0];
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
	var eventReminder = document.getElementById("eventReminder");
	var checkboxEmail = gj(eventReminder).find('input.checkbox')[0];
	var uiFormGrid = gj(eventReminder).find('table.UIFormGrid') [0];
	if(checkboxEmail.checked) {
		uiFormGrid.style.display = "";
	}
	else{
		uiFormGrid.style.display = "none";
	}
	var popupReminder = gj(eventReminder).find('div.ReminderByPopup') [0];
	var checkboxPopup = gj(popupReminder).find('input.checkbox')[0];
	var uiFormGridPopup = gj(popupReminder).find('table.UIFormGrid')[0];
	if(checkboxPopup.checked) {
		uiFormGridPopup.style.display = "";
	}
	else{
		uiFormGridPopup.style.display = "none";
	}
};

UICalendarPortlet.prototype.removeEmailReminder = function(obj){
	var uiEmailAddressItem = obj.parentNode;
	var uiEmailAddressLabel = gj(obj).prevAll("div")[0];
	var uiEmailInput = gj(obj).parents(".UIEmailInput")[0];
	uiEmailInput = gj(uiEmailInput).children("input")[0];
	uiEmailAddressLabel = uiEmailAddressLabel.innerHTML.toString().trim();
	uiEmailInput.value = this.removeItem(uiEmailInput.value,uiEmailAddressLabel);
	gj(uiEmailAddressItem).remove();
	if(_module.UICalendarPortlet.getElementById("UIEventForm")) {
		webui.UIForm.submitForm('UIEventForm','RemoveEmail', true);		
	} else if(_module.UICalendarPortlet.getElementById("UITaskForm")) { 
		webui.UIForm.submitForm('UITaskForm','RemoveEmail', true);	
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
	return gj(this.portletNode).find('#' + id)[0];
}
_module.UICalendarPortlet = _module.UICalendarPortlet || new UICalendarPortlet();
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
        if (_module.UICalendarPortlet.getElementById("UIWeekView")) {
            _module.UICalendarMan.initWeek();
            _module.UIWeekView.setSize();
            _module.UICalendarPortlet.setFocus();
            this.firstRun = true;
        }
    }
    
};

UICalendarPortlet.prototype.fixForMaximize = function(){
  var obj = _module.UICalendarPortlet.portletNode ;
  var uiWindow = gj(obj).parents(".UIWindow")[0];
  if(uiWindow.style.display == "none") return ;
  if ((base.Browser.browserType != "ie")) {
      if (_module.UICalendarPortlet.getElementById("UIWeekView")) {
          _module.UICalendarMan.initWeek();
          _module.UIWeekView.setSize();
      }
      if (_module.UICalendarPortlet.getElementById("UIMonthView")) {
          _module.UICalendarMan.initMonth();
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
  var uiNav = _module.CalendarScrollManager ;
  var container = _module.UICalendarPortlet.getElementById("UIActionBar") ;
  if(container) {
    var mainContainer = gj(container).find('div.CalendarActionBar')[0];
    var randomId = cs.DOMUtil.generateId("CalendarScrollbar");
    mainContainer.setAttribute("id",randomId);
//    uiNav.scrollMgr = new ScrollManager(randomId) ;
  uiNav.scrollMgr = new gtnav.ScrollManager(randomId) ;

    uiNav.scrollMgr.initFunction = uiNav.initScroll ;
    uiNav.scrollMgr.mainContainer = mainContainer ;
    uiNav.scrollMgr.arrowsContainer = gj(container).find('div.ScrollButtons')[0];
    uiNav.scrollMgr.loadItems("ActionBarButton", true) ;
    var button = gj(uiNav.scrollMgr.arrowsContainer).find('div');
    if(button.length >= 2) {    
      uiNav.scrollMgr.initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
      uiNav.scrollMgr.initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
      }
		
    uiNav.scrollManagerLoaded = true;
    uiNav.initScroll() ;
  }
} ;

CalendarScrollManager.prototype.initScroll = function() {
  var uiNav = _module.CalendarScrollManager ;
  if(!uiNav.scrollManagerLoaded) uiNav.load() ;
  var elements = uiNav.scrollMgr.elements ;
  uiNav.scrollMgr.init() ;
  uiNav.scrollMgr.csCheckAvailableSpace() ;
  uiNav.scrollMgr.renderElements() ;
} ;

gtnav.ScrollManager.prototype.loadItems = function(elementClass, clean) {
	if (clean) this.cleanElements();
	this.elements.clear();
	var items = gj(this.mainContainer).find('div.' + elementClass); 
	for(var i = 0; i < items.length; i++){
		this.elements.push(items[i]);
	}
};

gtnav.ScrollManager.prototype.csCheckAvailableSpace = function(maxSpace) { // in pixels
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
			var eventNode = cs.EventManager.getEventTarget(evt);
			eventNode = gj(eventNode).parents('.UICalendarPortlet')[0]; 
			self._container = gj(eventNode).find('div.UICalendarEventTooltip')[0];
			gj(self._container).on({'mouseover':function(evt){
				self.cleanupTimer(evt);
				},
				'mouseout':function(evt){
					self.hide(evt);
				},
				'click':function(evt){
					self.hideElement();
					self.editEvent(self.currentEvent);
				}});
		}
	},
	editEvent: function(eventNode){				
    var eventId = eventNode.getAttribute("eventId");
    var calendarId = eventNode.getAttribute("calid");
    var calendarType = eventNode.getAttribute("caltype");
    webui.UIForm.submitEvent(_module.UICalendarPortlet.portletId + '#' + _module.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + _module.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType);
	},
	show: function(evt){
		var self = eXo.calendar.EventTooltip;
		self.currentEvent = this;
		self.cleanupTimer(evt);
		if(eXo.calendar.EventTooltip.isDnD == true) return;		
		self.getContainer(evt);
		self.overTimer = setTimeout(function(){
			var url = eXo.env.portal.context + "/" + _module.restContext;
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
		if(evt && cs.EventManager.getMouseButton(evt) != 2) this.isDnD = true;
	},
	enable: function(){
		this.isDnD = false;
	},
	cleanupTimer:function(evt){
		if(this.outTimer) clearTimeout(this.outTimer);
		if(this.overTimer) clearTimeout(this.overTimer);
	},
	makeRequest: function(method, url, queryString){
		var request = new eXo.portal.AjaxRequest(method, url, queryString) ;
	  request.onSuccess = this.render ;
	  request.onLoading = function(){
			eXo.calendar.EventTooltip._container.innerHTML = "Loading...";
		} ;
	  eXo.portal.CurrentRequest = request ;
	  request.process() ;				
	},
	parseData: function(req){
		var data = gj.parseJSON(req.responseText);
		var time = this.getRealTime(data);
		return {
			title: data.summary,
			description: data.description,
			time:time,
			location: data.location
		}
	},
	// return 1 if event is all day, 2 if event happens in 1 day, 3 for other cases. 
	isAllday:function(eventObject){
	    var startDate = new Date(parseInt(eventObject.startDateTime) + parseInt(eventObject.startTimeOffset));
	    var endDate = new Date(parseInt(eventObject.endDateTime) + parseInt(eventObject.endTimeOffset));
	    var delta = eventObject.endDateTime - eventObject.startDateTime;
	    if(startDate.getUTCDate() == endDate.getUTCDate() && startDate.getUTCMonth() == endDate.getUTCMonth()) {
		return delta == (24*60 - 1)*60*1000 ? 1 : 2;
	    } else {
		return 3;
	    }
	},
	// returns the string for event time
	// all day: Thu, December 05, 00:00 - 23:59
	// 1 day: Thu, December 05, 09:00AM - 10:00AM
	// > 2 days: Thu, December 05, 09:00AM - Fri, December 06, 10:00PM
	getRealTime: function(data){
	    var time = "";
	    var type = this.isAllday(data);
	    var startDate = new Date(parseInt(data.startDateTime) + parseInt(data.startTimeOffset));
	    var endDate = new Date(parseInt(data.endDateTime) + parseInt(data.endTimeOffset));

	    var startDayName = gj(".ShortDayName").get(startDate.getUTCDay()).getAttribute("name");
	    var startMonthName = gj(".LocalizedMonthName").get(startDate.getUTCMonth()).getAttribute("name");

	    var endDayName = gj(".ShortDayName").get(endDate.getUTCDay()).getAttribute("name");
	    var endMonthName = gj(".LocalizedMonthName").get(endDate.getUTCMonth()).getAttribute("name");

	    var startDateInMonth = startDate.getUTCDate() < 10 ? '0' + startDate.getUTCDate() : startDate.getUTCDate();
	    var endDateInMonth = endDate.getUTCDate() < 10 ? '0' + endDate.getUTCDate() : endDate.getUTCDate();

	    if(type == 1 ) {
		return startDayName + ', ' + startMonthName + ' ' + startDateInMonth;
	    }
	    if(type == 2) {
		time = startDayName + ', ' + startMonthName + ' ' + startDateInMonth;
		time += ', ' + this.getFormattedHour(startDate) + ' - ' + this.getFormattedHour(endDate);
		return time;
	    }
	    else {
		time = startDayName + ', ' + startMonthName + ' ' + startDateInMonth + ', ' + this.getFormattedHour(startDate);
		time += ' - ' + endDayName + ', ' + endMonthName + ' ' + endDateInMonth + ', ' + this.getFormattedHour(endDate);
		return time;
	    }
	},
	// get time string with AM or PM localized if needed, for ex: 08:00 AM 
	getFormattedHour: function(date) {
	    var hours = date.getUTCHours();
	    var mins = date.getUTCMinutes();
	    mins = mins < 10 ? '0' + mins : mins;
	    // if timeFormat = "HH:mm" -> no AM or PM
	    if(_module.UICalendarPortlet.timeFormat.length == 5) {
		hours = hours < 10 ? '0' + hours : hours;
		return hours + ':' + mins;
	    } else {
		var amOrPm = hours >= 12 ? gj("#PMString")[0].getAttribute("name") : gj("#AMString")[0].getAttribute("name");
		hours = hours - 12;
		if(hours > 0) {
			hours = hours < 10 ? '0' + hours : hours;
			return hours + ':' + mins + ' ' + amOrPm;
 
		} else {
		    hours = hours + 12;
		    hours = hours < 10 ? '0' + hours : hours;
		    return hours + ':' + mins + ' ' + amOrPm;
		}
	    }
	},
	convertTimezone: function(datetime){
		var time = parseInt(datetime.time);
		var eventTimezone = parseInt(datetime.timezoneOffset);
		var settingTimezone = parseInt(_module.UICalendarPortlet.settingTimezone);
		time += (eventTimezone + settingTimezone)*60*1000;
		return time;
	},
	render: function(req){		
		var self = eXo.calendar.EventTooltip;
		var data = self.parseData(req);
		if(!data) return ;
		var html = '<div class="Title">' + data.title + '</div>';
		html += '<div class="Time">' + data.time + '</div>';
		if(data.location)    html += '<div class="Location">' + data.location + '</div>';
		if(data.description) html += '<div class="Description">' + data.description + '</div>';
		self._container.style.display = "block";
		self._container.innerHTML = '<div class="BgTLEvent"><div class="BgTREvent"><div class="BgTCEvent"><span></span></div></div></div><div class="BgMLEvent"><div class="BgMREvent"><div class="BgMCEvent">' + html + '</div></div></div><div class="BgBLEvent"><div class="BgBREvent"><div class="BgBCEvent"><span></span></div></div></div><div class="Clear"><span></span></div>';	
		self._container.style.zIndex = 1000;
		self.positioning();
	},
	positioning: function(){
		var offsetTooltip = this._container.offsetParent;
		var offsetEvent = this.currentEvent.offsetParent;
		if(_module.UICalendarPortlet.viewType == "UIDayView") 
			offsetEvent = gj(offsetEvent).parents(".EventDayContainer")[0];
		var extraX = (this.currentEvent.offsetWidth - this._container.offsetWidth)/2
		var extraY = 0;
		var y = base.Browser.findPosYInContainer(this.currentEvent,offsetTooltip) - this._container.offsetHeight;
		var x = base.Browser.findPosXInContainer(this.currentEvent,offsetTooltip) + extraX;		
		this._container.style.top = y + "px";
		this._container.style.left = x + "px";
		var relativeX = base.Browser.findPosX(this._container) + this._container.offsetWidth;
		if(relativeX > document.documentElement.offsetWidth) {
			extraX = document.documentElement.offsetWidth - relativeX;
			x += extraX;
			this._container.style.left = x + "px";
		}
	}	
}


//eXo.calendar.CalendarScrollManager = new CalendarScrollManager();
_module.CalendarScrollManager = new CalendarScrollManager();
eXo.calendar.CalendarScrollManager = _module.CalendarScrollManager;

if (!eXo.desktop) 
  eXo.desktop = {};

if(eXo.desktop.UIDesktop){
UIDesktop.prototype._ShowHideWindow = eXo.desktop.UIDesktop.showHideWindow;
UIWindow.prototype._endResizeWindowEvt = eXo.desktop.UIWindow.endResizeWindowEvt;
UIWindow.prototype._maximizeWindowEvt = eXo.desktop.UIWindow.maximizeWindowEvt;

UIDesktop.prototype.showHideWindow = function(uiWindow, clickedElement, mode){
	//Fix for CS-3474: IE8: Webos : can not open portlet by one click
	if(typeof(uiWindow) == "string") this.cs3474 = document.getElementById(uiWindow) ;
	else this.cs3474 = uiWindow ;
	this.cs3474.maxIndex = 0;
	eXo.desktop.UIDesktop._ShowHideWindow(this.cs3474, clickedElement, mode);
	//End fix for CS-3474
    if (eXo.desktop.UIDesktop.object.style.display != "block") {
        if(uiWindow.indexOf("calendar") >=0) _module.UICalendarPortlet.delay = window.setTimeout("eXo.calendar.UICalendarPortlet.fixFirstLoad() ;", 2000);
    }
};




UIWindow.prototype.endResizeWindowEvt = function(evt){
    // Re initializes the scroll tabs managers on the page
		eXo.desktop.UIWindow._endResizeWindowEvt(evt);
    _module.UICalendarPortlet.fixForMaximize();
};

UIWindow.prototype.maximizeWindowEvt = function(evt){
	var portletWindow = gj(this).parents(".UIResizeObject")[0] ;
	
	var uiWindow = eXo.desktop.UIWindow ;
	var uiPageDesktop = gj("#UIPageDesktop")[0] ;
  var desktopWidth = uiPageDesktop.offsetWidth  ;
  var desktopHeight = uiPageDesktop.offsetHeight  ;
  var uiResizableBlock = gj(portletWindow).find('div.UIResizableBlock');
  if(portletWindow.maximized) {
    portletWindow.maximized = false ;
    portletWindow.style.top = uiWindow.posY + "px" ;
    if(base.I18n.isLT()) portletWindow.style.left = uiWindow.posX + "px" ;
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
    if(base.I18n.isLT()) portletWindow.style.left = "0px" ;
    else portletWindow.style.right = "0px" ;
    portletWindow.style.width = "100%" ;
		portletWindow.style.height = "auto" ;
    var delta = gj(window).height() - portletWindow.clientHeight ;
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
    _module.UICalendarPortlet.fixForMaximize();
};
}

/*
 * Override Comobobox
 * TODO : remove this method when portal fix it
 * REQUIREJS: UICombobox in webui-ext module : wx.UICombobox; dont use global variable eXo.webui
 */
//eXo.webui.UICombobox.init = function() {
wx.UICombobox.init = function() {
	var uiWorkingWorkspace = gj("#UIWorkingWorkspace")[0];
	var uiCombobox = wx.UICombobox ;
	var comboList = gj(uiWorkingWorkspace).find('input.UIComboboxInput');
	var i = comboList.length ;
	while(i--){
		comboList[i].value = gj(comboList[i]).prevAll('input')[0].value;
	  var onfocus = comboList[i].getAttribute("onfocus") ;
	  var onclick = comboList[i].getAttribute("onclick") ;
	  var onblur = comboList[i].getAttribute("onblur") ;
	  if(!onfocus) gj(comboList[i]).on('focus', uiCombobox.show) ;
	  if(!onclick) gj(comboList[i]).on('click', uiCombobox.show) ;
	  if(!onblur)  gj(comboList[i]).on('blur',uiCombobox.correct) ;
	}
};


//fix for onblur event on calendar
//For validating

wx.UICombobox.correct = function() {	
	var UICombobox = wx.UICombobox ; 
	var value = this.value ;
	this.value = UICombobox.setValue(value) ;
	var hiddenField = gj(this).prevAll('input')[0];
	hiddenField.value = this.value;
	UICombobox.hide();
} ;

wx.UICombobox.setValue = function(value) {
	var value = String(value).trim().toLowerCase();
	var UICombobox = wx.UICombobox;
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

wx.UICombobox.getTimeFormat= function() {
	var items = wx.UICombobox.items ;
	if (items.length <= 0) return {am:"AM", pm:"PM"} ;
	var first = gj(items[0]).find('div.UIComboboxLabel')[0].innerHTML; 
	var last =  gj(items[items.length - 1]).find('div.UIComboboxLabel')[0].innerHTML;
	var am = first.match(/[A-Z]+/) ;
	var pm = last.match(/[A-Z]+/) ;
	return {am:am, pm:pm} ;
} ;

wx.UICombobox.digitToTime = function(stringNo) {
	stringNo = new String(wx.UICombobox.getDigit(stringNo));
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

wx.UICombobox.getDigit = function(stringNo) {
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
  
  var divRemoteCalendar = _module.UICalendarPortlet.getElementById(id);
  var divUseAuthentication = gj(divRemoteCalendar).find('#' + USE_AUTHENTICATION)[0];
  var chkUseAuthentication = gj(divUseAuthentication).find('input.checkbox')[0];
  var divUsername = gj(divRemoteCalendar).find('#' + DIV_USERNAME_ID)[0];
  var lblUsername = gj(divUsername).find('span.' + labelClass)[0];
  var txtUsername = gj(divUsername).find('#' + TXT_USERNAME_ID)[0];
  
  var divPassword = gj(divRemoteCalendar).find('#' + DIV_PASSWORD_ID)[0];
  var lblPassword = gj(divPassword).find('span.' + labelClass)[0];
  var txtPassword = gj(divPassword).find('#' + TXT_PASSWORD_ID)[0];
  gj(chkUseAuthentication).on('click', function(){
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
  });  
} ;

UICalendarPortlet.prototype.editRepeat = function(id) {
  var eventForm = _module.UICalendarPortlet.getElementById(id);
  var portletFragment = gj(eventForm).parents(".PORTLET-FRAGMENT")[0];    
  var repeatContainer = gj(eventForm).find('#repeatContainer')[0];
  var repeatCheck = gj(repeatContainer).children("input")[0];
  var summary = gj(repeatContainer).find('span.repeatSummary')[0];
  var editButton = gj(repeatContainer).children("a")[0];
  
  if (repeatCheck.checked) {
    editButton.style.display = "";
  } else {
    editButton.style.display="none";
  }
  
  gj(repeatCheck).on('click', function() {
    if (repeatCheck.checked) {
      repeatCheck.checked = false;
      webui.UIForm.submitForm(portletFragment.parentNode.id + '#UIEventForm','EditRepeat', true); 
    } else {
      summary.innerHTML = "";
      editButton.style.display = "none";
    }
  });
}

UICalendarPortlet.prototype.changeRepeatType = function(id) {
  var weeklyByDayClass = "weeklyByDay";
  var monthlyTypeClass = "monthlyType";
  var RP_END_AFTER = "endAfter";
  var RP_END_NEVER = "neverEnd";
  var RP_END_BYDATE = "endByDate";
  
  var repeatingEventForm = _module.UICalendarPortlet.getElementById(id);
  var weeklyByDay = gj(repeatingEventForm).find('tr.' + weeklyByDayClass)[0];
  var monthlyType = gj(repeatingEventForm).find('tr.' + monthlyTypeClass)[0];
  var repeatTypeSelectBox = gj(repeatingEventForm).find('select.selectbox')[0];
  var repeatType = repeatTypeSelectBox.options[repeatTypeSelectBox.selectedIndex].value;
  var endNever = gj(repeatingEventForm).find('#endNever')[0]; 
  var endAfter = gj(repeatingEventForm).find('#endAfter')[0];
  var endByDate = gj(repeatingEventForm).find('#endByDate')[0];
  var hiddenEndType = gj(repeatingEventForm).find('#endRepeat')[0];
  var endByDateContainer = gj(repeatingEventForm).find('#endByDateContainer')[0];
  var endDateContainer = gj(endByDateContainer).find("#endDate")[0];
  var endDate = gj(endDateContainer).children("input")[0];
  var count = gj(repeatingEventForm).find("#endAfterNumber")[0];
  
  endDate.disabled = true;
  count.disabled = true;
  
  if (endAfter.checked) {
    count.disabled = false;
  }
  
  if (endByDate.checked) {
    endDate.disabled = false;
  }
  
  gj(repeatTypeSelectBox).on('change', function() {
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
  });
  
  gj(endNever).on('click', function() {
    hiddenEndType.value = RP_END_NEVER;
    count.disabled = true;
    endDate.disabled = true;
  });
  
  gj(endAfter).on('click', function() {
    hiddenEndType.value = RP_END_AFTER;
    count.disabled = false;
    if (count.value == null || count.value == "") count.value = 5;
    endDate.disabled = true;
  });

  gj(endByDate).on('click', function() {
    hiddenEndType.value = RP_END_BYDATE;
    count.disabled = true;
    endDate.disabled = false;
    if (endDate.value == null || endDate.value == "") endDate.value = "";
  });

};

_module.UICalendarPortlet = new UICalendarPortlet();
eXo.calendar.UICalendarPortlet = _module.UICalendarPortlet;
