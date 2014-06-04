(function(gj, EventObject, WeekMan) {  
  var EventMan = {
    originalHeightOfEventMonthContent : null,
    
    /**
    *
    * @param {Object} rootNode <form> element with id UIMonthView
    */
   initMonth : function(rootNode) {
       var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
       var rowContainerDay   = gj(rootNode).find(".rowContainerDay")[0];
       this.cleanUp();
       rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  
       /** <form> element with id UIMonthView */
       this.rootNode = rootNode;
  
       /** contains all event objects */
       this.events = new Array();
       this.weeks = new Array();
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
       gj(allEvents).off('mouseover mouseout').on('mouseover',eXo.calendar.EventTooltip.show).on('mouseout',eXo.calendar.EventTooltip.hide);
       gj(allEvents).off('dblclick').on('dblclick',UICalendarPortlet.ondblclickCallback);
  
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
  
   },
  
   /**
    * Increase the width to include the scrollbar
    */
   increaseWidth : function(contentContainer) {
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
   },
  
   /**
    * Resize width for month view to include the scrollbar
    * @param {Object} contentContainer DOM element
    */
   resizeWidth : function(contentContainer) {
     var eventMonthContainer = gj(contentContainer).parents(".eventMonthContainer")[0],
         dayTitleBar         = gj(eventMonthContainer).siblings(".dayTitleBar")[0],
         resizedWidth        = gj(dayTitleBar).width(),
         eventTable          = gj(contentContainer).children("table#UIMonthViewGrid")[0];
     
     gj(eventTable).css("width", resizedWidth);
     gj(contentContainer).css("width", (resizedWidth + 20));
   },  
  
   cleanUp : function() {
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
   },
  
   /**
    * 
    * @param {Element} rootNode
    */
   initWeek : function(rootNode) {
     var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
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
     this.week = new WeekMan(this);
     this.week.weekIndex = 0;
     this.week.startWeek = Date.parse(this.dayNodes[0].getAttribute('starttimefull'));
     var len = (UICalendarPortlet.weekdays && document.getElementById("UIWeekView"))?UICalendarPortlet.weekdays: 7 ;
     this.week.endWeek = this.week.startWeek + (1000 * 60 * 60 * 24 * len) - 1000;
     this.week.events = this.events;
     this.week.resetEventWeekIndex();
     // Set unlimited event visible for all days
     this.week.MAX_EVENT_VISIBLE = -1;
     this.week.putEvents2Days();
   },
  
   groupByWeek : function() {
     var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
     var weekNodes = gj(this.UIMonthViewGrid).find('tr');
     var startWeek = 0;
     var endWeek = 0;
     var startCell = null;
     var len = (UICalendarPortlet.weekdays && document.getElementById("UIWeekView"))?UICalendarPortlet.weekdays: 7 ;
     for (var i = 0; i < weekNodes.length; i++) {
       var currentWeek = new WeekMan(this);
       currentWeek.weekIndex = i;
       for (var j = 0; j < this.events.length; j++) {
         var eventObj = this.events[j];
         startCell = gj(weekNodes[i]).find('td.uiCellBlock')[0]; 
  //       startWeek = parseInt(startCell.getAttribute("startTime"));
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
   },
  
   sortByWeek : function() {
     for (var i = 0; i < this.weeks.length; i++) {
       var currentWeek = this.weeks[i];
       currentWeek.sortEvents();
       currentWeek.putEvents2Days();
     }
   }
 };

 return EventMan;
})(gj, EventObject, WeekMan);