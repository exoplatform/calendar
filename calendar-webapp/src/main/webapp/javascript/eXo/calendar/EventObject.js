(function(gj) {
/**
 * Contains information about an event
 */
function EventObject() {
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

/**
 * Parse information from the HTML element to initialize attributes of EventObject
 * @param rootNode
 */
EventObject.prototype.init = function(rootNode) {
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

EventObject.prototype.normalizeDate = function(dateStr) {
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
EventObject.prototype.compare = function(event1, event2) {
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

return EventObject;
})(gj);