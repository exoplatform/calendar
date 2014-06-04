(function() {
  
function DayMan() {
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

DayMan.prototype.synchronizeGroups = function() {
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

return DayMan;
})();