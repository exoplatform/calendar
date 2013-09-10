(function(cs, gj, Highlighter){
var _module = {};
eXo.calendar = eXo.calendar || {};
eXo.calendar.UIHSelection = Highlighter ;
function QuickSortObject(){
  this.processArray = false;
  this.desc = false;
  this.compareFunction = false;
  this.compareArgs = false;
}

/**
 *
 * @param {Array} array
 * @param {Boolean} desc
 * @param {Function} compareFunction
 * @param {Object | Array} compareArgs
 */
QuickSortObject.prototype.doSort = function(array, desc, compareFunction, compareArgs){
  this.processArray = array;
  this.desc = desc;
  this.compareFunction = compareFunction;
  this.compareArgs = compareArgs;
  this.qSortRecursive(0, this.processArray.length);
};

/**
 *
 * @param {Integer} x
 * @param {Integer} y
 */
QuickSortObject.prototype.swap = function(x, y){
  if (this.processArray) {
    var tmp = this.processArray[x];
    this.processArray[x] = this.processArray[y];
    this.processArray[y] = tmp;
  }
};

/**
 *
 * @param {Integer} begin
 * @param {Integer} end
 * @param {Integer} pivotIndex
 */
QuickSortObject.prototype.qSortRecursive = function(begin, end){
  if (!this.processArray || begin >= end - 1) 
    return;
  var pivotIndex = begin + Math.floor(Math.random() * (end - begin - 1));
  var partionIndex = this.partitionProcess(begin, end, pivotIndex);
  this.qSortRecursive(begin, partionIndex);
  this.qSortRecursive(partionIndex + 1, end);
};

/**
 *
 * @param {Integer} begin
 * @param {Integer} end
 * @param {Integer} pivotIndex
 */
QuickSortObject.prototype.partitionProcess = function(begin, end, pivotIndex){
  var pivotValue = this.processArray[pivotIndex];
  this.swap(pivotIndex, end - 1);
  var scanIndex = begin;
  for (var i = begin; i < end - 1; i++) {
    if (typeof(this.compareFunction) == 'function') {
      if (!this.desc && this.compareFunction(this.processArray[i], pivotValue, this.compareArgs) <= 0) {
        this.swap(i, scanIndex);
        scanIndex++;
        continue;
      }
      else 
        if (this.desc && this.compareFunction(this.processArray[i], pivotValue, this.compareArgs) > 0) {
          this.swap(i, scanIndex);
          scanIndex++;
          continue;
        }
    }
    else {
      if (!this.desc && this.processArray[i] <= pivotValue) {
        this.swap(i, scanIndex);
        scanIndex++;
        continue;
      }
      else 
        if (this.desc && this.processArray[i] > pivotValue) {
          this.swap(i, scanIndex);
          scanIndex++;
          continue;
        }
    }
  }
  this.swap(end - 1, scanIndex);
  return scanIndex;
};

_module.QuickSortObject = new QuickSortObject();
eXo.core.QuickSortObject = _module.QuickSortObject;


function EventObject(){
  this.LABEL_MAX_LEN = 10;
  this.calId = false;
  this.calType = false;
  this.endTime = false;
  this.eventCat = false;
  this.eventId = false;
  this.eventIndex = false;
  this.startIndex = false;
  this.startTime = false;
  this.weekStartTimeIndex = new Array();
  this.cloneNodes = new Array();
  this.rootNode = false;
  this.name = false;
  if (arguments.length > 0) {
    this.init(arguments[0]);
  }
}

EventObject.prototype.init = function(rootNode){
  if (!rootNode) {
    return;
  }
  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  this.rootNode.style['cursor'] = 'pointer';
  this.startIndex = this.rootNode.getAttribute('startindex');
  this.calType = this.rootNode.getAttribute('caltype');
  this.eventId = this.rootNode.getAttribute('eventid');
  this.eventIndex = this.rootNode.getAttribute('eventindex');
  this.calId = this.rootNode.getAttribute('calid');
  this.eventCat = this.rootNode.getAttribute('eventcat');
  this.startTime = this.normalizeDate(this.rootNode.getAttribute('starttimefull'));//Date.parse(this.rootNode.getAttribute('starttimefull'));
  this.endTime = Date.parse(this.rootNode.getAttribute('endtimefull'));

  if (this.rootNode.innerText) {
    this.name = gj.trim(this.rootNode.innerText + '');
  } else {
    this.name = gj.trim(this.rootNode.textContent + '');
  }
};

EventObject.prototype.normalizeDate = function(dateStr){
	var d = new Date(dateStr);
	if(document.getElementById("UIWeekView")) return Date.parse(dateStr);
	return (new Date(d.getFullYear(),d.getMonth(),d.getDate(),0,0,0,0)).getTime();
};

EventObject.prototype.updateIndicator = function(nodeObj, hasBefore, hasAfter) {
  var labelStr = this.name;
  if (hasBefore) {
    labelStr = '>> ' + labelStr;
  }
  if (hasAfter) {
    labelStr += ' >>';
  }
  var labelNode = gj(nodeObj).find('div.EventLabel')[0]; 
  if (labelNode) {
    labelNode.innerHTML = labelStr;
  }
};

EventObject.prototype.getLabel = function() {
  if (this.name.length > this.LABEL_MAX_LEN) {
    return this.name.substring(0, this.LABEL_MAX_LEN) + '...';
  } else {
    return this.name;
  }
};

/**
 *
 * @param {EventObject} event1
 * @param {EventObject} event2
 *
 * @return {Integer} 0 if equals
 *                   > 0 if event1 > event2
 *                   < 0 if event1 < event2
 */
EventObject.prototype.compare = function(event1, event2){
  if ((event1.startTime == event2.startTime && event1.endTime < event2.endTime) ||
      event1.startTime > event2.startTime) {
    return 1;
  } else if (event1.startTime == event2.startTime && event1.endTime == event2.endTime) {
		if(event1.name < event2.name) return 1;
		return 0 ;
  } else {
      return -1;
  }
};

function DayMan(){
  this.previousDay = false;
  this.nextDay = false;
  this.MAX_EVENT_VISIBLE = (document.getElementById("UIWeekView"))?100 : 3;
  this.totalEventVisible = 0;
  this.visibleGroup = new Array();
  this.invisibleGroup = new Array();
  this.linkGroup = new Array();
  this.events = new Array();
}

/**
 * 
 * @param {EventObject} eventObj
 */
DayMan.prototype.isVisibleEventExist = function(eventObj) {
  for (var i=0; i<this.visibleGroup.length; i++) {
    if (this.visibleGroup[i] == eventObj) {
      return i;
    }
  }
  return -1;
};

/**
 * 
 * @param {EventObject} eventObj
 */
DayMan.prototype.isInvisibleEventExist = function(eventObj) {
  for (var i=0; i<this.invisibleGroup.length; i++) {
    if (this.invisibleGroup[i] == eventObj) {
      return i;
    }
  }
  return -1;
};

DayMan.prototype.synchronizeGroups = function(){
  if (this.events.length <= 0) {
    return;
  }

  this.totalEventVisible = this.MAX_EVENT_VISIBLE;
  
  for (var i=0; i<this.events.length; i++) {
    if (this.MAX_EVENT_VISIBLE < 0) {
      this.visibleGroup.push(this.events[i]);
    } else if (this.previousDay && 
        this.previousDay.isInvisibleEventExist(this.events[i]) >= 0) {
      this.invisibleGroup.push(this.events[i]);
    } else if(this.visibleGroup.length < this.totalEventVisible) {
      this.visibleGroup.push(this.events[i]);
    } else {
      this.invisibleGroup.push(this.events[i]);
    }
  }
  this.reIndex();
};

DayMan.prototype.reIndex = function() {
  var tmp = new Array();
  var cnt = 0;
  master : for (var i=0; i<this.visibleGroup.length; i++) {
    var eventTmp = this.visibleGroup[i];
    var eventIndex = i;
    // check cross event conflic
    if (this.previousDay && 
        this.invisibleGroup.length > 0 &&
        this.previousDay.visibleGroup[(this.MAX_EVENT_VISIBLE)] == eventTmp) {
      this.invisibleGroup.push(eventTmp);
      this.invisibleGroup = this.invisibleGroup.reverse();
      this.visibleGroup.push(this.invisibleGroup.pop());
      this.invisibleGroup = this.invisibleGroup.reverse();
      continue;
    } 
    
    // check cross event
    if (this.previousDay) {
      eventIndex = this.previousDay.isVisibleEventExist(eventTmp);
      if (eventIndex >= 0) {
        tmp[eventIndex] = eventTmp;
        continue;
      }
    }
    for (var j=0; j<tmp.length; j++) {
      if (!tmp[j]) {
        tmp[j] = eventTmp;
        continue master;
      }
    }
    tmp[i] = eventTmp;
  }
	this.visibleGroup = tmp;
};

function WeekMan(){
  this.startWeek = false;
  this.endWeek = false;
  this.weekIndex = false;
  this.events = new Array();
  this.days = new Array();
  this.isEventsSorted = false;
  this.MAX_EVENT_VISIBLE = 3;
}

WeekMan.prototype.resetEventWeekIndex = function() {
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    if (eventObj.startTime > parseInt(this.startWeek)) {
      eventObj.weekStartTimeIndex[this.weekIndex] = eventObj.startTime;
    } else {
      eventObj.weekStartTimeIndex[this.weekIndex] = this.startWeek;
    }
  }
};

WeekMan.prototype.createDays = function() {
  // Create 7 days
  var len = (eXo.calendar.UICalendarPortlet.weekdays && document.getElementById("UIWeekView"))?eXo.calendar.UICalendarPortlet.weekdays: 7 ;
  for (var i=0; i<len; i++) {
    this.days[i] = new DayMan();
    // link days
    if (i > 0) {
      this.days[i].previousDay = this.days[i-1];
    }
  }
  
  for (var i=0; i<this.days.length-1; i++) {
    if (this.MAX_EVENT_VISIBLE) {
      this.days[i].MAX_EVENT_VISIBLE = this.MAX_EVENT_VISIBLE;
    }
    this.days[i].nextDay = this.days[i+1];    
  }
};

WeekMan.prototype.putEvents2Days = function(){
  if (this.events.length <= 0) {
    return;
  }
  if (!this.isEventsSorted) {
    this.sortEvents();
  }
  
  this.createDays();
  // Put events to days
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    var startWeekTime = eventObj.weekStartTimeIndex[this.weekIndex];
    var endWeekTime = eventObj.endTime > this.endWeek ? this.endWeek : eventObj.endTime;
		var deltaStartWeek = (new Date(parseInt(this.startWeek))).getDay()*1000*60*60*24 ;
    var startDay = (new Date(parseInt(startWeekTime) - deltaStartWeek)).getDay() ;
    var endDay = (new Date(parseInt(endWeekTime) - deltaStartWeek)).getDay() ;
    // fix date
    var delta = (new Date(eventObj.endTime)) - (new Date(eventObj.startTime));
    delta /= (1000 * 60 * 60 * 24);
    if (delta == 1 &&
    		startDay == endDay) {
      endDay = startDay;
    }
    for (var j=startDay; j<=endDay; j++) {
      try{
      	this.days[j].events.push(eventObj);
      }catch(e){
      	//TODO check this when in UIWorkingView
      }
    }
  }
  for (var i=0; i<this.days.length; i++) {
    this.days[i].synchronizeGroups();
  }
};

WeekMan.prototype.sortEvents = function(checkDepend){
  if (this.events.length > 1) {
    if (checkDepend) {
      eXo.core.QuickSortObject.doSort(this.events, false, this.compareEventByWeek, this.weekIndex);
    } else {
      eXo.core.QuickSortObject.doSort(this.events, false, this.events[0].compare);
    }
    this.isEventsSorted = true;
  }
};

/**
 *
 * @param {EventObject} event1
 * @param {EventObject} event2
 *
 * @return {Integer} 0 if equals
 *                   > 0 if event1 > event2
 *                   < 0 if event1 < event2
 */
WeekMan.prototype.compareEventByWeek = function(event1, event2, weekIndex){
  var weekObj = _module.UICalendarMan.EventMan.weeks[weekIndex];
  var e1StartWeekTime = event1.weekStartTimeIndex[weekIndex];
  var e2StartWeekTime = event2.weekStartTimeIndex[weekIndex];
  var e1EndWeekTime = event1.endTime > weekObj.endWeek ? weekObj.endWeek : event1.endTime;
  var e2EndWeekTime = event2.endTime > weekObj.endWeek ? weekObj.endWeek : event2.endTime;
  if ((e1StartWeekTime == e2StartWeekTime && e1EndWeekTime < e2EndWeekTime) ||
      e1StartWeekTime > e2StartWeekTime) {
    return 1;
  } else if (e1StartWeekTime == e2StartWeekTime && e1EndWeekTime == e2EndWeekTime) {
      return 0;
  } else {
      return -1;
  }
};

function EventMan(){
  this.originalHeightOfEventMonthContent = null;
}

/**
 *
 * @param {Object} rootNode
 */
EventMan.prototype.initMonth = function(rootNode){
    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
    var UICalendarPortlet = _module.UICalendarPortlet,
        rowContainerDay   = gj(rootNode).find(".rowContainerDay")[0],
        EventMan          = _module.UICalendarMan.EventMan ;

    this.cleanUp();
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    this.rootNode = rootNode;
    this.events = new Array();
    this.weeks = new Array();
    var DOMUtil = cs.DOMUtil;
    // Parse all event node to event object
    var allEvents = gj(rootNode).find('div.dayContentContainer');

    // Create and init all event
    for (var i = 0; i < allEvents.length; i++) {
        if (allEvents[i].style.display == 'none') {
            continue;
        }
        var eventObj = new EventObject();
        eventObj.init(allEvents[i]);
        this.events.push(eventObj);
    }
    gj(allEvents).on('mouseover',eXo.calendar.EventTooltip.show).on('mouseout',eXo.calendar.EventTooltip.hide);
    gj(allEvents).on('dblclick',eXo.calendar.UICalendarPortlet.ondblclickCallback);

    this.UIMonthViewGrid = document.getElementById('UIMonthViewGrid');

    /* reset the scroll to put events in correct position */
    rowContainerDay.scrollTop = 0;

    this.groupByWeek();
    this.sortByWeek();

    /*=== resize width ===*/
    this.increaseWidth(rowContainerDay);

    /*=== resize height to stop at bottom of the page - for month view ===*/
    if (this.originalHeightOfEventMonthContent === null) {
        this.originalHeightOfEventMonthContent = gj(rowContainerDay).height();
    }

    UICalendarPortlet.resizeHeight(rowContainerDay, 6, this.originalHeightOfEventMonthContent);

    /* resize content each time the window resizes */
    var originalHeight = this.originalHeightOfEventMonthContent;
    gj(window).resize(function() {
        UICalendarPortlet.resizeHeight(rowContainerDay, 6, originalHeight);

        EventMan.resizeWidth(rowContainerDay);
    });

};

/**
 * Increase the width to include the scrollbar
 */
EventMan.prototype.increaseWidth = function(contentContainer) {
    var originalWidth       = gj(contentContainer).width(),
        eventMonthContainer = gj(contentContainer).parents(".eventMonthContainer")[0],
        widthOfTitleBar     = gj(eventMonthContainer).siblings(".dayTitleBar")[0].offsetWidth;

    if (widthOfTitleBar !== originalWidth) {
      gj(contentContainer).css("width", widthOfTitleBar);
    }

    gj(contentContainer).css("width", (widthOfTitleBar + 20));
    if (this.UIMonthViewGrid) {
        gj(this.UIMonthViewGrid).css("width", widthOfTitleBar);
    }
    else {
        this.UIMonthViewGrid = document.getElementById('UIMonthViewGrid');
    }
};

/**
 * Resize width for month view to include the scrollbar
 * @param {Object} contentContainer DOM element
 */
EventMan.prototype.resizeWidth = function(contentContainer) {
  var eventMonthContainer = gj(contentContainer).parents(".eventMonthContainer")[0],
      dayTitleBar         = gj(eventMonthContainer).siblings(".dayTitleBar")[0],
      resizedWidth        = gj(dayTitleBar).width(),
      eventTable          = gj(contentContainer).children("table#UIMonthViewGrid")[0];
  
  gj(eventTable).css("width", resizedWidth);
  gj(contentContainer).css("width", (resizedWidth + 20));
};


EventMan.prototype.cleanUp = function() {
  if (!this.events ||
      !this.rootNode ||
      !this.rootNode.nextSibling) {
    return;
  }
  var rowContainerDay = gj(this.rootNode).find('div.rowContainerDay')[0]; 
  
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    if (!eventObj) {
      continue;
    }
    for (var j=0; j<eventObj.cloneNodes.length; j++) {
      try {
        gj(eventObj.cloneNodes[j]).remove();
      } catch (e) {}
    }
    eventObj.rootNode.setAttribute('used', 'false');
    if (eventObj.rootNode.getAttribute('moremaster') == 'true') {
      eventObj.rootNode.setAttribute('moremaster', 'false');
      var eventNode = eventObj.rootNode.cloneNode(true);
      // Restore checkbox
      var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
      if (checkBoxTmp) {
        checkBoxTmp.style.display = '';
      }
      var bodyNode = gj(eventObj).parents('body')[0];
      if (bodyNode) {
      	try {
          rowContainerDay.appendChild(eventNode);
        } catch (e) {}
      }
    }
    this.events[i] = null;
  }
  var moreNodes = gj(this.rootNode).find('div.MoreEvent'); 
  var rowContainerDay = gj(this.rootNode).find('div.rowContainerDay'); 

  for (var i=0; i<moreNodes.length; i++) {
    var eventNodes = gj(moreNodes[i]).find('div.dayContentContainer');
    try {
      gj(moreNodes[i]).remove();
    } catch (e) {}
  }
};

/**
 * 
 * @param {Element} rootNode
 */
EventMan.prototype.initWeek = function(rootNode) {
  this.events = new Array();
  this.weeks = new Array();

  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  // Parse all event node to event object
  var allEvents = gj(rootNode).find('div.eventContainer'); 
  // Create and init all event
  for (var i=0; i < allEvents.length; i++) {
    if (allEvents[i].style.display == 'none') {
      continue;
    }
    var eventObj = new EventObject();
    eventObj.init(allEvents[i]);
    gj(allEvents[i]).on({'mouseover':eXo.calendar.EventTooltip.show, 'mouseout':eXo.calendar.EventTooltip.hide});
    this.events.push(eventObj);
  }
  var table = gj(this.rootNode).prevAll('table')[0]; 
  this.dayNodes = gj(table).find('td.uiCellBlock');
  this.week = new WeekMan();
  this.week.weekIndex = 0;
  this.week.startWeek = Date.parse(this.dayNodes[0].getAttribute('starttimefull'));
  var len = (eXo.calendar.UICalendarPortlet.weekdays && document.getElementById("UIWeekView"))?eXo.calendar.UICalendarPortlet.weekdays: 7 ;
  this.week.endWeek = this.week.startWeek + (1000 * 60 * 60 * 24 * len) - 1000;
  this.week.events = this.events;
  this.week.resetEventWeekIndex();
  // Set unlimited event visible for all days
  this.week.MAX_EVENT_VISIBLE = -1;
  this.week.putEvents2Days();
};

EventMan.prototype.groupByWeek = function(){
  var weekNodes = gj(this.UIMonthViewGrid).find('tr');
  var startWeek = 0;
  var endWeek = 0;
  var startCell = null;
  var len = (eXo.calendar.UICalendarPortlet.weekdays && document.getElementById("UIWeekView"))?eXo.calendar.UICalendarPortlet.weekdays: 7 ;
  for (var i = 0; i < weekNodes.length; i++) {
    var currentWeek = new WeekMan();
    currentWeek.weekIndex = i;
    for (var j = 0; j < this.events.length; j++) {
      var eventObj = this.events[j];
      startCell = gj(weekNodes[i]).find('td.uiCellBlock')[0]; 
//      startWeek = parseInt(startCell.getAttribute("startTime"));
      startWeek = Date.parse(startCell.getAttribute('starttimefull'));
      endWeek = (startWeek + len * 24 * 60 * 60 * 1000) - 1000;
      currentWeek.startWeek = startWeek;
      currentWeek.endWeek = endWeek;
      if ((eventObj.startTime >= startWeek && eventObj.startTime < endWeek) ||
      (eventObj.endTime >= startWeek && eventObj.endTime < endWeek) ||
      (eventObj.startTime <= startWeek && eventObj.endTime >= endWeek)) {
        if (eventObj.startTime > startWeek) {
          eventObj.weekStartTimeIndex[currentWeek.weekIndex] = eventObj.startTime;
        } else {
          eventObj.weekStartTimeIndex[currentWeek.weekIndex] = startWeek;
        }
        currentWeek.events.push(eventObj);
      }
    }
    this.weeks.push(currentWeek);
  }
};

EventMan.prototype.sortByWeek = function(){
  for (var i = 0; i < this.weeks.length; i++) {
    var currentWeek = this.weeks[i];
    currentWeek.sortEvents();
    currentWeek.putEvents2Days();
  }
};

function GUIMan(){
  this.EVENT_BAR_HEIGH = 17;
}

/**
 *
 * @param {EventMan} eventMan
 */
GUIMan.prototype.initMonth = function(){
  gj('div.moreEvent').css('display','none');//reset more event label to avoid overlap after resizing
  var events = _module.UICalendarMan.EventMan.events;
  if (events.length > 0) {
    if (events[0]) {
      this.EVENT_BAR_HEIGH = events[0].rootNode.offsetHeight - 1;
    }
  }
  for (var i=0; i<events.length; i++) {
    var eventObj = events[i];
    var eventLabelNode = gj(eventObj.rootNode).find('div.EventLabel')[0];
    //eventObj.rootNode.setAttribute('title', eventObj.name);
    eventObj.rootNode.setAttribute('used', 'false');
  }
  this.rowContainerDay = gj(_module.UICalendarMan.EventMan.rootNode).find('div.rowContainerDay')[0];
  var rows = _module.UICalendarMan.EventMan.UIMonthViewGrid.getElementsByTagName('tr');
  this.tableData = new Array();
  for (var i = 0; i < rows.length; i++) {
    var rowData = gj(rows[i]).find('td.uiCellBlock'); 
    this.tableData[i] = rowData;
  }
  this.paintMonth();
  this.scrollTo();
  this.initDND();
};

GUIMan.prototype.initWeek = function() {
  var EventMan = _module.UICalendarMan.EventMan;
  var events = EventMan.events;
  for (var i=0; i<events.length; i++) {
    var eventObj = events[i];
    var eventLabelNode = gj(eventObj.rootNode).find('div.eventAlldayContent')[0]; 
    eventObj.rootNode.setAttribute('used', 'false');
  }
  this.eventAlldayNode = EventMan.rootNode ;
	this.dayNodes = EventMan.dayNodes;
  this.paintWeek();
  this.initSelectionDayEvent();
  this.initSelectionDaysEvent();
};

GUIMan.prototype.paintWeek = function() {
    var weekObj = _module.UICalendarMan.EventMan.week;
    var maxEventRow = 0;
    for (var i=0; i<weekObj.days.length; i++) {
	var dayObj = weekObj.days[i];
	var dayNode = this.dayNodes[i];
	var dayInfo = {
		width : dayNode.offsetWidth ,
		top : 0,
		startTime : Date.parse(dayNode.getAttribute('starttimefull'))
	}
	dayInfo.pixelPerUnit = dayInfo.width / 100;

	for (var j=0; j<dayObj.visibleGroup.length; j++) {
	    var eventObj = dayObj.visibleGroup[j];
	    if (!eventObj ||
		    (dayObj.previousDay &&
			    dayObj.previousDay.isVisibleEventExist(eventObj) >= 0)) {
		continue;
	    }
	    var startTime = eventObj.weekStartTimeIndex[weekObj.weekIndex];
	    var endTime = eventObj.endTime;
	    if (endTime >= weekObj.endWeek) {
		endTime = weekObj.endWeek;
	    }
	    dayInfo.eventTop = dayInfo.top + ((this.EVENT_BAR_HEIGH) * j);
	    dayInfo.eventShiftRightPercent = (((new Date(startTime) - (new Date(dayInfo.startTime)))) / (1000 * 60 * 60 * 24)) * 100;
	    this.drawEventByMiliseconds(eventObj, startTime, endTime, dayInfo, i);
	}
	// update max event rows
	if (maxEventRow < dayObj.visibleGroup.length) {
	    maxEventRow = dayObj.visibleGroup.length;
	}
    }
    var allDayTable = gj(this.eventAlldayNode).find('.allDayTable');
    allDayTable.css('height', (maxEventRow > 1)?(maxEventRow * this.EVENT_BAR_HEIGH) + 'px':'17px');
};

/**
 * draws the event with given startTime and endTime
 * event starts from the column startCol
 */
GUIMan.prototype.drawEventByMiliseconds = function(eventObj, startTime, endTime, dayInfo, startCol) {
    var eventNode = eventObj.rootNode;
    var topPos = dayInfo.eventTop ;
    var leftPos = 56; // the empty td before all day event container has width = 55px;
    var eventWidth = 0;

    // get number of days
    var delta = (new Date(endTime)) - (new Date(startTime));
    delta /= (1000 * 60 * 60 * 24); // in days
    var rounded_delta = Math.floor(delta); // round downwards
    var left_hours = Math.round((delta - rounded_delta) * 24); // number of hours different

    // calculate event's width (= sum of widths of cells from startCol to startCol + delta - 1)
    for(var i = 0; i < rounded_delta; i++) { // can't multiply here, because the width of cells are not always equals.
	  eventWidth += gj(this.dayNodes[startCol++]).width();
    }

    // increase width by hours difference
    eventWidth += (gj(this.dayNodes[startCol]).width() * (left_hours / 24));
    startCol -= delta; // reset startCol

    // calculate event's left (= left of the startCol)
    for (var l = 0; l < startCol; l++) {
	  leftPos += gj(this.dayNodes[l]).width() + 1;
    }  

    leftPos += parseFloat((dayInfo.eventShiftRightPercent * dayInfo.width) / 100);
         
    eventNode.style.top = topPos + 'px';
    eventNode.style.left = leftPos +'px';
    eventNode.style.width = eventWidth + 'px';
    eventNode.style.visibility = 'visible';
    this.setOverWeek(eventNode,startTime,endTime);
};
  
GUIMan.prototype.setOverWeek = function(eventNode,startTime,endTime){
	var realStart = Date.parse(eventNode.getAttribute("startTimeFull"));
	var realEnd = Date.parse(eventNode.getAttribute("endTimeFull"));
	var eventAlldayContent = gj(eventNode).find('div.eventAlldayContent')[0]; 
	if(realStart < startTime){
		eventAlldayContent.style.marginLeft = "10px";
	}
	if(realEnd > endTime){
		eventAlldayContent.style.marginRight = "10px";
	}
};

GUIMan.prototype.initSelectionDayEvent = function() {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
  var UISelection = eXo.calendar.UISelection ;
  var container = document.getElementById("UIWeekViewGrid") ;
  UISelection.step = UICalendarPortlet.CELL_HEIGHT; 
  UISelection.block = document.createElement("div") ;
  UISelection.block.className = "userSelectionBlock" ;
  UISelection.container = container ;
  gj(container).prevAll('div')[0].appendChild(UISelection.block) ;
  gj(UISelection.container).on('mousedown',UISelection.start);
//  UISelection.container.onmousedown = UISelection.start ;
  UISelection.relativeObject = gj(UISelection.container).parents('.eventWeekContent')[0]; 
  UISelection.viewType = "UIWeekView" ;
} ;

GUIMan.prototype.initSelectionDaysEvent = function() {
  for(var i=0; i<this.dayNodes.length; i++) {
    var link = gj(this.dayNodes[i]).children("a")[0] ;    
    if (link)
    	gj(link).on('mousedown',this.cancelEvent);
//    	link.onmousedown = this.cancelEvent ;
    gj(this.dayNodes[i]).on('mousedown',eXo.calendar.UIHSelection.start);
//    this.dayNodes[i].onmousedown = eXo.calendar.UIHSelection.start ;
  }
} ;
 
GUIMan.prototype.scrollTo = function() {
  var lastUpdatedId = this.rowContainerDay.getAttribute("lastUpdatedId") ;
  var events = _module.UICalendarMan.EventMan.events; 
  for(var i=0 ; i<events.length ; i++) {
    if(events[i].eventId == lastUpdatedId) {
      this.rowContainerDay.scrollTop = events[i].rootNode.offsetTop - 17;
      return ;
    }
  }
} ;

GUIMan.prototype.initDND = function() {
  eXo.calendar.UICalendarPortlet.viewType = "UIMonthView" ;
  var events = _module.UICalendarMan.EventMan.events;
  for(var i=0 ; i<events.length ; i++) {
    var eventNode = events[i].rootNode;
    var checkbox = gj(eventNode).find('input.checkbox')[0]; 
    if (checkbox) {
    	gj(checkbox).on('mousedown',this.cancelEvent).on('click',cs.CSUtils.EventManager.cancelBubble);
    }
    gj(eventNode).on('dblclick',eXo.calendar.UICalendarPortlet.ondblclickCallback);
    gj(eventNode).on('mouseover',eXo.calendar.EventTooltip.show).on('mouseout',eXo.calendar.EventTooltip.hide);
  }
  eXo.calendar.UICalendarDragDrop = window.require("SHARED/UICalendarDragDrop");
  eXo.calendar.UICalendarDragDrop.init(this.tableData, _module.UICalendarMan.EventMan.events);
};

/**
 * 
 * @param {Event} event
 */
GUIMan.prototype.cancelEvent = function(event) {
  event = window.event || event ;
  event.cancelBubble = true ;
  //Fix bug for click checkbox event
  cs.CSUtils.EventManager.cancelBubble(event)
  if (event.preventDefault) {
    event.preventDefault();
  }
};

GUIMan.prototype.paintMonth = function(){
  var weeks = _module.UICalendarMan.EventMan.weeks;
  // Remove old more node if exist
  for (var i=0; i<weeks.length; i++) {
    var curentWeek = weeks[i];
    var eventLength = 0;
    if (curentWeek.events.length > 0) {
      for (var j=0; j<curentWeek.days.length; j++) {
        if (curentWeek.days[j].events.length > 0) {
          var dayNode = (this.tableData[curentWeek.weekIndex])[j];
          this.drawDay(curentWeek, j);
        }
      }
    }
    
  }

};

/**
 * 
 * @param {WeekMan} weekObj
 * @param {Integer} dayIndex
 */
GUIMan.prototype.drawDay = function(weekObj, dayIndex) {
    var dayObj = weekObj.days[dayIndex];
    // Pre-calculate event position
    var dayNode = (this.tableData[weekObj.weekIndex])[dayIndex];
    var dayInfo = {
	    startCol : dayIndex,
	    width : gj(dayNode).width(),
	    left : gj(dayNode).position().left,
	    top : gj(dayNode).position().top + 20,
      rindex : gj(dayNode).attr("rindex"),
      cindex : gj(dayNode).attr("cindex"),
	    beginMonth : Date.parse(this.tableData[0][0].getAttribute("startTimeFull")),
	    endMonth : Date.parse((this.tableData[this.tableData.length - 1][this.tableData[0].length -1]).getAttribute("startTimeFull")) + 24*60*60*1000
    }

    // Draw visible events
    for (var i=0; i<dayObj.visibleGroup.length; i++) {
	var eventObj = dayObj.visibleGroup[i];
	if (!eventObj || 
		(dayObj.previousDay && 
			dayObj.previousDay.isVisibleEventExist(eventObj) >= 0)) {
	    continue;
	}
	var startTime = eventObj.weekStartTimeIndex[weekObj.weekIndex];
	var endTime = eventObj.endTime > weekObj.endWeek ? weekObj.endWeek : eventObj.endTime;
	var delta = (new Date(endTime)) - (new Date(startTime));
	delta /= (1000 * 60 * 60 *24 - 1000);
	if (delta > 1 && 
		dayObj.nextDay && 
		i == (dayObj.MAX_EVENT_VISIBLE)) {
	    var tmp = dayObj.nextDay;
	    var cnt = 1;
	    while (tmp.nextDay && cnt<=delta) {
		if (tmp.isInvisibleEventExist(eventObj) >= 0) {
		    break;
		}
		cnt++;
		tmp = tmp.nextDay;
	    }
	    endTime = startTime + ((1000 * 60 * 60 * 24) * cnt) - 1000;
	}
	dayInfo.eventTop = dayInfo.top + ((this.EVENT_BAR_HEIGH) * i);
	this.drawEventByDay(eventObj, startTime, endTime, dayInfo);
    gj(eventObj.rootNode).on('mouseover', eXo.calendar.EventTooltip.show).on('mouseout', eXo.calendar.EventTooltip.hide);
    gj(eventObj.rootNode).on('dblclick',eXo.calendar.UICalendarPortlet.ondblclickCallback);
    eXo.calendar.UICalendarDragDrop = window.require("SHARED/UICalendarDragDrop");
    eXo.calendar.UICalendarDragDrop.init(this.tableData, _module.UICalendarMan.EventMan.events);
 }
    // Draw invisible events (put all into more)
    if (dayObj.invisibleGroup.length > 0) {
	var moreNode = document.createElement('div');
	moreNode.className = 'moreEvent';
	this.rowContainerDay.appendChild(moreNode);
	moreNode.style.position = 'absolute';
	moreNode.style.width = dayInfo.width + 'px';
	moreNode.style.left = dayInfo.left + 'px';
	moreNode.style.top = dayInfo.top + ((dayObj.MAX_EVENT_VISIBLE) * this.EVENT_BAR_HEIGH) + 5  + 'px';
	var moreContainerNode = document.createElement('div');
	var moreEventBar = moreContainerNode.cloneNode(true);
	var moreEventList = moreContainerNode.cloneNode(true);
	var moreEventTitleBar = moreContainerNode.cloneNode(true);
	moreEventBar.className = "moreEventBar" ;
	moreEventBar.innerHTML = "<center><a href=javascript:void(0)><i class='uiIconArrowUp uiIconLightGray'></i></a></center>" ;
	gj(moreEventBar).find('a').on('click',this.hideMore);
	moreContainerNode.className = 'moreEventContainer' ;
	// Create invisible event
	var cnt = 0
	for (var i=0; i<dayObj.invisibleGroup.length; i++) {
	    var eventObj = dayObj.invisibleGroup[i];
	    if (!eventObj) {
		continue;
	    }
	    cnt ++;
	    var eventNode = eventObj.rootNode;
	    var checkboxState = 'none';
	    if (eventNode.getAttribute('used') == 'true') {
		eventNode = eventNode.cloneNode(true);
		eventNode.setAttribute('eventclone', 'true');
		eventObj.cloneNodes.push(eventNode);
		var hasBefore = true;
		var hasAfter = true;
		if (i >= (dayObj.invisibleGroup.length - 1)) {
		    hasAfter = false;
		}
		if (cnt == 0) {
		    hasBefore = false;
		}
		eventObj.updateIndicator(eventObj.cloneNodes[eventObj.cloneNodes.length - 1], hasBefore, hasAfter);
	    } else {
		eventNode = eventNode.cloneNode(true);
		gj(eventObj.rootNode).remove();
		eventNode.setAttribute('moremaster', 'true');
		eventObj.rootNode = eventNode;
		checkboxState = "";
	    }
	    // Remove checkbox on clone event

	    var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
	    checkBoxTmp.style.display = checkboxState;
	    eventNode.ondblclick = eXo.calendar.UICalendarPortlet.ondblclickCallback ;
	    moreEventList.appendChild(eventNode);
	    var topPos = this.EVENT_BAR_HEIGH * i;
	    eventNode.style.top = topPos + 16 + 'px';
	    eventNode.setAttribute('used', 'true');
	}
	this.setWidthForMoreEvent(moreEventList,i,dayNode);
	var moreLabel = document.createElement('div');
	moreLabel.className = "moreEventLabel";
	moreLabel.innerHTML = 'more ' + cnt + '+';
	gj(moreLabel).on('click',this.showMore);
	moreNode.appendChild(moreLabel);
	moreEventList.appendChild(moreEventBar);
	moreContainerNode.appendChild(moreEventList);
	moreNode.appendChild(moreContainerNode);
	dayObj.moreNode = moreNode;
    }    
};

GUIMan.prototype.setWidthForMoreEvent = function(moreEventList,len,dayNode){
	var eventNodes = gj(moreEventList).children('div'); 
	var i = eventNodes.length ;
	if(len > 9){
		moreEventList.style.height = "200px";
		moreEventList.style.overflowY = "auto";
		moreEventList.style.overflowX = "hidden";
		
		while(i--){
			if(eXo.core.Browser.isIE6()) eventNodes[i].style.width = dayNode.offsetWidth - 15 + "px";
		    if(eXo.core.Browser.isIE7()) eventNodes[i].style.width = dayNode.offsetWidth - 17 + "px";
		}		
	}
};

GUIMan.prototype.hideMore = function(evt){
	var DOMUtil = cs.DOMUtil;
	var items = DOMUtil.hideElementList;
	var ln = items.length ;
	if (ln > 0) {
		for (var i = 0; i < ln; i++) {
			if(gj(items[i]).hasClass("moreEvent")) 
				items[i].style.zIndex = 1 ;
			items[i].style.display = "none" ;
		}
		DOMUtil.hideElementList.clear() ;
	}
	var src = cs.CSUtils.EventManager.getEventTarget(evt);
	var	moreContainerNode = gj(src).parents('.moreEventContainer')[0]; 
	if(!moreContainerNode) 
		moreContainerNode = gj(src).nextAll("div")[0];
	moreContainerNode.style.top = '0px';
	moreContainerNode.style.left = '0px';
};

GUIMan.prototype.showMore = function(evt) {
    var moreNode = this;
    var GUIMan = _module.UICalendarMan.GUIMan;

    var moreEventContainer = gj(moreNode).nextAll('div')[0];
    gj(moreEventContainer).find('div.dayContentContainer').on('mouseover', eXo.calendar.EventTooltip.show)
    .on('mouseout', eXo.calendar.EventTooltip.hide);
    if(GUIMan.lastMore) GUIMan.lastMore.style.zIndex = 1;
    cs.CSUtils.EventManager.cancelBubble(evt);
    GUIMan.hideMore(evt);
    if (!moreEventContainer.style.display || moreEventContainer.style.display == 'none') {

	moreEventContainer.style.display = 'block';

	gj(moreEventContainer).css('position','absolute');
	gj(moreEventContainer).css('top',gj(moreNode).position().top - 6);
	var moreLeft = gj(moreNode).position().left;
	if(gj.browser.webkit) {
	    moreLeft += 1;
	}
	gj(moreEventContainer).css('left', moreLeft);	cs.DOMUtil.listHideElements(moreEventContainer);
	gj(moreEventContainer).on({'click':cs.CSUtils.EventManager.cancelBubble,
	    'mousedown':function(evt){
		cs.CSUtils.EventManager.cancelEvent(evt);
		if(cs.CSUtils.EventManager.getMouseButton(evt) == 2) {
		    var index = cs.DOMUtil.hideElementList.indexOf(this);
		    cs.DOMUtil.hideElementList.splice(index,1);
		}
	    },
	    'contextmenu':function(evt){
		cs.CSUtils.EventManager.cancelEvent(evt);
		var index = cs.DOMUtil.hideElementList.indexOf(this);
		cs.DOMUtil.hideElementList.splice(index,1);
		cs.UIContextMenu.show(evt) ;
		cs.DOMUtil.hideElementList.push(this);
		return false;
	    }});

    }
    GUIMan.moreNode = moreEventContainer ;
    GUIMan.lastMore = moreEventContainer.parentNode;

};

/**
 *
 * @param {EventObject} eventObj
 * @param {Integer} startTime
 * @param {Integer} endTime
 * @param {Integer} weekIndex
 * @param {Object} dayInfo
 */
GUIMan.prototype.drawEventByDay = function(eventObj, startTime, endTime, dayInfo){
    var eventNode = eventObj.rootNode;
    if (eventNode.getAttribute('used') == 'true') {
	eventNode = eventNode.cloneNode(true);
	eventNode.setAttribute('eventclone', 'true');
	
	// Remove checkbox on clone event
	try {
	    var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
	    checkBoxTmp.style.display = 'none';
	} catch(e) {}
	this.rowContainerDay.appendChild(eventNode);
	eventObj.cloneNodes.push(eventNode);
    }
    var topPos = dayInfo.eventTop ;
    var leftPos = dayInfo.left ;
    var  cellWidth = dayInfo.width;
    var beginId = "r" + dayInfo.rindex + "c"+dayInfo.rindex;
    var delta = eXo.calendar.UICalendarPortlet.dateDiff(startTime, endTime);
    if (delta != 0) {
	   delta ++ ;
    }
    if(delta <= 0) delta = 1;
    
    var eventLen = 0;
    var cindex = 0;
    for(var i = 0; i < delta; i++ ){
     cindex = parseInt(dayInfo.cindex) + parseInt(i);
     beginId = "r"+dayInfo.rindex + "c"+cindex;
     var cellWidth = gj("td#"+beginId).width();
     
     if(gj.browser.mozilla) {
       cellWidth = gj("td#"+beginId).outerWidth(true);
     } else if (gj.browser.msie) {
       cellWidth = gj("td#"+beginId).outerWidth(true);
     }
     eventLen = eventLen + cellWidth;
    }
    var boderWidth = (delta -1);
    if( gj.browser.webkit) {
      if(delta - parseInt(dayInfo.cindex) >= 3) 
       eventLen = eventLen + delta  -2;
      else  
        eventLen = eventLen + delta -1;
    } else 
    if(gj.browser.mozilla){
      if(delta - parseInt(dayInfo.cindex) >= 3) 
       eventLen = eventLen  -2;
      else  
       eventLen = eventLen  -1;
    } else 
    if (gj.browser.msie) { 
      if(delta - parseInt(dayInfo.cindex) >= 3) 
       eventLen = eventLen  -3;
      else  
       eventLen = eventLen  -1;
    }
    eventNode.style.top = topPos + 'px';
    eventNode.style.left = leftPos + 'px';
    eventNode.style.width = eventLen + 'px';
    if(eXo.core.I18n.isRT()){
	   eventNode.style.left = (leftPos - eventLen + (cellWidth)) + 'px';
    }
    eventNode.setAttribute('used', 'true');
    eventNode.setAttribute('startTime',startTime);
    eventNode.setAttribute('endTime',endTime);
    eventObj.init(eventNode);
    this.setOverMonth(eventObj,dayInfo.beginMonth,dayInfo.endMonth);
    eXo.calendar.UICalendarPortlet.viewType = "UIMonthView" ;
};

GUIMan.prototype.setOverMonth = function(eventObj,beginMonth,endMonth){
    var eventNode = eventObj.rootNode ;
    var realStart = Date.parse(eventNode.getAttribute("startTimeFull"));
    var color = eventNode.getAttribute('color');
    if(realStart < parseInt(beginMonth)){
	var EventOnDayContent = gj(eventObj.rootNode).find('div.eventOnDayContent')[0];
	if(!gj(EventOnDayContent).find('.leftContinueEvent')[0]) {
	    var leftNode = gj('<div></div').addClass('leftContinueEvent  pull-left');
	    var icon = gj('<i></i>').addClass('uiIconMiniArrowLeft uiIconWhite');
	    leftNode.append(icon);
	    gj(EventOnDayContent).prepend(leftNode);
	}
    }
};

GUIMan.prototype.removeContinueClass = function(eventClones){
    if(!eventClones || (eventClones.length == 0)) return ;
    var i = eventClones.length;
    var leftNode = null ;
    while(i--){
	leftNode = gj(eventClones[i]).find('div.leftContinueEvent')[0]; 
	gj(leftNode).remove();
    }
}	;

GUIMan.prototype.isMultiWeek = function(eventObj){
	var startIndex = (new Date(eventObj.startTime)).getDay();
	var diff = eXo.calendar.UICalendarPortlet.dateDiff(eventObj.startTime,eventObj.endTime) - 1;
	var weekIndex = parseInt(eventObj.rootNode.getAttribute("startIndex"));
	if((diff > (7 - startIndex)) && (weekIndex < this.tableData.length) && (weekIndex != 1)) return true ;
	return false;
}	;

GUIMan.prototype.addContinueClass = function(){
    var endMonth = Date.parse((this.tableData[this.tableData.length - 1][this.tableData[0].length - 1]).getAttribute("startTimeFull")) + 24 * 60 * 60 * 1000;
    var events = _module.UICalendarMan.EventMan.events;
    var len = events.length ;
    var eventNode = null ;
    for(var i = 0 ; i<len;i++){
	var color = events[i].rootNode.getAttribute('color');
	var realEnd = Date.parse(events[i].rootNode.getAttribute("endTimeFull"));
	if (realEnd > endMonth) {
	    if(this.isMultiWeek(events[i])){
		eventNode = events[i].cloneNodes[events[i].cloneNodes.length - 1];
	    }else{
		eventNode = events[i].rootNode;
	    }

	    var EventOnDayContent = gj(eventNode).find('div.eventOnDayContent')[0];

	    if(!gj(EventOnDayContent).find('.rightContinueEvent')[0]) {
		var rightNode = gj('<div></div').addClass('rightContinueEvent  pull-right');
		var icon = gj('<i></i>').addClass('uiIconMiniArrowRight uiIconWhite');
		rightNode.append(icon);
		gj(EventOnDayContent).prepend(rightNode);
	    }
	}
    }
}	;

// Initialize  highlighter
GUIMan.prototype.initHighlighter = function() {
  for(var i=0 ; i<this.tableData.length; i++) {
    var row = this.tableData[i];
    for (var j=0; j<row.length; j++) {
    	gj(row[j]).on('mousedown',eXo.calendar.Highlighter.start);
    }
  }
} ;

GUIMan.prototype.callbackHighlighter = function() {
  var Highlighter = _module.Highlighter ;
  var startTime = parseInt(Date.parse(Highlighter.firstCell.getAttribute('startTimeFull')));
  var endTime = parseInt(Date.parse(Highlighter.lastCell.getAttribute('startTimeFull')))  + 24*60*60*1000 - 1;
  var d = new Date() ;
  var timezoneOffset = d.getTimezoneOffset() ;
  var currentTime = Highlighter.firstCell.getAttribute('startTime') ;
  eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(Highlighter.firstCell,1,startTime,endTime) ;
  //eXo.webui.UIForm.submitEvent('UIMonthView' ,'QuickAdd','&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime +'&ct='+currentTime+ '&tz=' + timezoneOffset); 
} ;

eXo.calendar.UICalendarMan = {
  addBorderControl : function(){
    var leftNode = gj('div.leftContinueEvent')[0];
    if(leftNode) {
      gj(leftNode).parents('.eventOnDayBorder').addClass('leftBorderControl');
    }

    var rightNode = gj('div.rightContinueEvent')[0];
    if(rightNode) {
      gj(rightNode).parents('.eventOnDayBorder').addClass('rightBorderControl');
    }

  },
  showMonthEvents : function() {
    var events = gj('div.dayContentContainer');
    for(var i = 0; i < events.length; i++) {
      gj(events[i]).attr('style','display:block;');
    }
    var moreNodes = gj('div.MoreEvent');
    for(var i = 0; i < moreNodes.length; i++) {
      moreNodes[i].style.display = 'block';
    }
  },
  initMonth : function(rootNode) {
    rootNode = document.getElementById('UIMonthView');
    if (!rootNode) return;
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    _module.UICalendarMan.EventMan.initMonth(rootNode);
    _module.UICalendarMan.GUIMan.initMonth();
    _module.UICalendarMan.GUIMan.initHighlighter();
		this.GUIMan.addContinueClass();
    _module.UICalendarMan.addBorderControl();
  },
  initWeek : function(rootNode) {
    rootNode = document.getElementById('UIWeekViewGridAllDay');
    if (!rootNode) return;
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    _module.UICalendarMan.EventMan.initWeek(rootNode);
    _module.UICalendarMan.GUIMan.initWeek();
  },
  EventMan: new EventMan(),
  GUIMan: new GUIMan()
}
_module.UICalendarMan = eXo.calendar.UICalendarMan;
eXo.calendar.Highlighter = Highlighter.Highlighter;
_module.Highlighter = Highlighter.Highlighter;

return _module;
})(cs, gj, Highlighter);
