(function(QuickSortObject, DayMan) { 
function WeekMan(EventMan) {
  this.startWeek = false;
  this.endWeek = false;
  this.weekIndex = false;
  this.events = new Array();
  this.days = new Array();
  this.isEventsSorted = false;
  this.MAX_EVENT_VISIBLE = 3;
  this.EventMan = EventMan;
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
      QuickSortObject.doSort(this.events, false, this.compareEventByWeek, this.weekIndex);
    } else {
      QuickSortObject.doSort(this.events, false, this.events[0].compare);
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
  var weekObj = EventMan.weeks[weekIndex];
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

return WeekMan;
})(QuickSortObject, DayMan);