(function(DOMUtil, gj, Highlighter, UIHSelection, UICalendarDragDrop, EventMan) {
var GUIMan = {
  EVENT_BAR_HEIGH : 17,
  
  /**
  *
  * @param {EventMan} eventMan
  */
 initMonth : function() {
     /** Reset 'more event' label to avoid overlap after resizing */
     gj('div.moreEvent').css('display','none');

     var events = EventMan.events;
     if (events.length > 0) {
         if (events[0]) {
             this.EVENT_BAR_HEIGH = events[0].rootNode.offsetHeight - 1;
         }
     }

     /** Initially set 'used' to false */
     for (var i=0; i < events.length; i++) {
         //eventObj.rootNode.setAttribute('title', eventObj.name);
         events[i].rootNode.setAttribute('used', 'false');
     }

     this.rowContainerDay = gj(EventMan.rootNode).find('div.rowContainerDay')[0];
     var rows = EventMan.UIMonthViewGrid.getElementsByTagName('tr');
     this.tableData = new Array();
     for (var i = 0; i < rows.length; i++) {
         var rowData = gj(rows[i]).find('td.uiCellBlock');
         this.tableData[i] = rowData;
     }
     this.paintMonth();
     this.scrollTo();
     this.initDND();
 },

 initWeek : function() {
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
 },

 paintWeek : function() {
     var weekObj = EventMan.week;
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
     allDayTable.css('height', ((maxEventRow +1) * this.EVENT_BAR_HEIGH + 'px'));
     allDayTable.css('cursor','pointer');
 },

 /**
  * draws the event with given startTime and endTime
  * event starts from the column startCol
  */
 drawEventByMiliseconds : function(eventObj, startTime, endTime, dayInfo, startCol) {
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
 },
   
 setOverWeek : function(eventNode,startTime,endTime) {
   var realStart = Date.parse(eventNode.getAttribute("startTimeFull"));
   var realEnd = Date.parse(eventNode.getAttribute("endTimeFull"));
   var eventAlldayContent = gj(eventNode).find('div.eventAlldayContent')[0]; 
   if(realStart < startTime){
     eventAlldayContent.style.marginLeft = "10px";
   }
   if(realEnd > endTime){
     eventAlldayContent.style.marginRight = "10px";
   }
 },

 initSelectionDayEvent : function() {
   var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
   var UISelection = eXo.calendar.UISelection ;
   var container = document.getElementById("UIWeekViewGrid") ;
   UISelection.step = UICalendarPortlet.CELL_HEIGHT; 
   UISelection.block = document.createElement("div") ;
   UISelection.block.className = "userSelectionBlock" ;
   UISelection.container = container ;
   gj(container).prevAll('div')[0].appendChild(UISelection.block) ;
   gj(UISelection.container).off('mousedown').on('mousedown',UISelection.start);
//   UISelection.container.onmousedown = UISelection.start ;
   UISelection.relativeObject = gj(UISelection.container).parents('.eventWeekContent')[0]; 
   UISelection.viewType = "UIWeekView" ;
 },

 initSelectionDaysEvent : function() {
   for(var i=0; i<this.dayNodes.length; i++) {
     var link = gj(this.dayNodes[i]).children("a")[0] ;    
     if (link) {
       gj(link).off('mousedown').on('mousedown', false);
     }

     gj(this.dayNodes[i]).off('mousedown').on('mousedown', UIHSelection.start);
   }
 },
  
 scrollTo : function() {
   var lastUpdatedId = this.rowContainerDay.getAttribute("lastUpdatedId") ;
   var events = EventMan.events; 
   for(var i=0 ; i<events.length ; i++) {
     if(events[i].eventId == lastUpdatedId) {
       this.rowContainerDay.scrollTop = events[i].rootNode.offsetTop - 17;
       return ;
     }
   }
 },

 /**
  * Init drag and drop
  */
 initDND : function() {
     eXo.calendar.UICalendarPortlet.viewType = "UIMonthView";
     var events = EventMan.events;
     for (var i=0 ; i < events.length ; i++) {
         var eventNode = events[i].rootNode;
         var checkbox = gj(eventNode).find('input.checkbox')[0];
         if (checkbox) {
             gj(checkbox).off('mousedown click').on('mousedown', false).on('click', function(e) {e.stopPropagation();});
         }
         gj(eventNode).off('dblclick').on('dblclick',eXo.calendar.UICalendarPortlet.ondblclickCallback);
         gj(eventNode).off('mouseover mouseout').on('mouseover',eXo.calendar.EventTooltip.show).on('mouseout',eXo.calendar.EventTooltip.hide);
     }
     UICalendarDragDrop.init(this.tableData, EventMan.events);
 },

 paintMonth : function() {
   var weeks = EventMan.weeks;
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

 },

 /**
  * 
  * @param {WeekMan} weekObj
  * @param {Integer} dayIndex
  */
 drawDay : function(weekObj, dayIndex) {
     var dayObj = weekObj.days[dayIndex];
     // Pre-calculate event position
     var dayNode = (this.tableData[weekObj.weekIndex])[dayIndex];
     var dayInfo = {
         startCol   : dayIndex,
         width      : gj(dayNode).width(),
         left       : gj(dayNode).position().left,
         top        : gj(dayNode).position().top + 20,
         rindex     : gj(dayNode).attr("rindex"),
         cindex     : gj(dayNode).attr("cindex"),
         beginMonth : Date.parse(this.tableData[0][0].getAttribute("startTimeFull")),
         endMonth   : Date.parse((this.tableData[this.tableData.length - 1][this.tableData[0].length -1]).getAttribute("startTimeFull")) + 24*60*60*1000
     }

     // Draw visible events
     for (var i=0; i<dayObj.visibleGroup.length; i++) {
         var eventObj = dayObj.visibleGroup[i];
         if (!eventObj || (dayObj.previousDay && dayObj.previousDay.isVisibleEventExist(eventObj) >= 0)) {
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

         gj(eventObj.rootNode).off('mouseover mouseout').on('mouseover', eXo.calendar.EventTooltip.show).on('mouseout', eXo.calendar.EventTooltip.hide);
         gj(eventObj.rootNode).off('dblclick').on('dblclick',eXo.calendar.UICalendarPortlet.ondblclickCallback);
         UICalendarDragDrop.init(this.tableData, EventMan.events);
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
         gj(moreEventBar).find('a').off('click').on('click',this.hideMore);
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
         gj(moreLabel).off('click').on('click',this.showMore);
         moreNode.appendChild(moreLabel);
         moreEventList.appendChild(moreEventBar);
         moreContainerNode.appendChild(moreEventList);
         moreNode.appendChild(moreContainerNode);
         dayObj.moreNode = moreNode;
     }
 },

 setWidthForMoreEvent : function(moreEventList,len,dayNode) {
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
 },

 hideMore : function(evt) {
   var items = DOMUtil.hideElementList;
   var ln = items.length ;
   if (ln > 0) {
     for (var i = 0; i < ln; i++) {
       if(gj(items[i]).hasClass("moreEvent")) 
         items[i].style.zIndex = 1 ;
       items[i].style.display = "none" ;
     }
     DOMUtil.hideElementList = [];
   }
   var src = evt.target;
   var moreContainerNode = gj(src).parents('.moreEventContainer')[0]; 
   if(!moreContainerNode) 
     moreContainerNode = gj(src).nextAll("div")[0];
   moreContainerNode.style.top = '0px';
   moreContainerNode.style.left = '0px';
 },

 showMore : function(evt) {
     var moreNode = this;

     var moreEventContainer = gj(moreNode).nextAll('div')[0];
     gj(moreEventContainer).find('div.dayContentContainer').off('mouseover mouseout').on('mouseover', eXo.calendar.EventTooltip.show)
     .on('mouseout', eXo.calendar.EventTooltip.hide);
     if(GUIMan.lastMore) GUIMan.lastMore.style.zIndex = 1;
     evt.stopPropagation();
     GUIMan.hideMore(evt);
     if (!moreEventContainer.style.display || moreEventContainer.style.display == 'none') {

   moreEventContainer.style.display = 'block';

   gj(moreEventContainer).css('position','absolute');
   gj(moreEventContainer).css('top',gj(moreNode).position().top - 6);
   var moreLeft = gj(moreNode).position().left;
   if(gj.browser.webkit) {
       moreLeft += 1;
   }
   gj(moreEventContainer).css('left', moreLeft);
   DOMUtil.listHideElements(moreEventContainer);
   gj(moreEventContainer).off('click mousedown contextmenu').on({'click' : function(e) {e.stopPropagation();},
       'mousedown':function(evt) {
         if(evt.button == 2) {
             var index = DOMUtil.hideElementList.indexOf(this);
             DOMUtil.hideElementList.splice(index,1);
         }
         return false;
       },
       'contextmenu':function(evt) {
         var index = DOMUtil.hideElementList.indexOf(this);
         DOMUtil.hideElementList.splice(index,1);
         UIContextMenu.show(evt) ;
         DOMUtil.hideElementList.push(this);
         return false;
       }});

     }
     GUIMan.moreNode = moreEventContainer ;
     GUIMan.lastMore = moreEventContainer.parentNode;

 },

 /**
  *
  * @param {EventObject} eventObj
  * @param {Integer} startTime
  * @param {Integer} endTime
  * @param {Integer} weekIndex
  * @param {Object} dayInfo
  */
 drawEventByDay : function(eventObj, startTime, endTime, dayInfo) {
     var eventNode = eventObj.rootNode;

     // The event is a cloned event
     if (eventNode.getAttribute('used') == 'true') {
         eventNode = eventNode.cloneNode(true);
         eventNode.setAttribute('eventclone', 'true');
         // Remove checkbox
         gj(eventNode).find('.uiCheckbox')[0].style.display='none';
         // Add left margin
         gj(eventNode).find('.eventSummary')[0].style.marginLeft='5px';
         this.rowContainerDay.appendChild(eventNode);
         eventObj.cloneNodes.push(eventNode);
     }

     var topPos = dayInfo.eventTop ;
     var leftPos = dayInfo.left ;
     var cellWidth = dayInfo.width;
     var beginId = "r" + dayInfo.rindex + "c"+dayInfo.rindex;
     var delta = eXo.calendar.UICalendarPortlet.dateDiff(startTime, endTime);
     if (delta != 0) {
         delta ++ ;
     }
     if(delta <= 0) delta = 1;

     var eventLen = 0;
     var cindex = 0;
     for (var i = 0; i < delta; i++ ){
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
     //eventObj.init(eventNode);
     this.setOverMonth(eventObj,dayInfo.beginMonth,dayInfo.endMonth);
     eXo.calendar.UICalendarPortlet.viewType = "UIMonthView" ;
 },

 setOverMonth : function(eventObj,beginMonth,endMonth) {
     var eventNode = eventObj.rootNode ;
     var realStart = Date.parse(eventNode.getAttribute("startTimeFull"));
     var color = eventNode.getAttribute('color');
     if(realStart < parseInt(beginMonth)){
   var EventOnDayContent = gj(eventObj.rootNode).find('div.eventOnDayContent')[0];
   if(!gj(EventOnDayContent).find('.leftContinueEvent')[0]) {
       var leftNode = gj('<div></div>').addClass('leftContinueEvent  pull-left');
       var icon = gj('<i></i>').addClass('uiIconMiniArrowLeft uiIconWhite');
       leftNode.append(icon);
       gj(EventOnDayContent).prepend(leftNode);
   }
     }
 },

 removeContinueClass : function(eventClones) {
     if(!eventClones || (eventClones.length == 0)) return ;
     var i = eventClones.length;
     var leftNode = null ;
     while(i--){
   leftNode = gj(eventClones[i]).find('div.leftContinueEvent')[0]; 
   gj(leftNode).remove();
     }
 },

 isMultiWeek : function(eventObj) {
   var startIndex = (new Date(eventObj.startTime)).getDay();
   var diff = eXo.calendar.UICalendarPortlet.dateDiff(eventObj.startTime,eventObj.endTime) - 1;
   var weekIndex = parseInt(eventObj.rootNode.getAttribute("startIndex"));
   if((diff > (7 - startIndex)) && (weekIndex < this.tableData.length) && (weekIndex != 1)) return true ;
   return false;
 },

 addContinueClass : function() {
     var endMonth = Date.parse((this.tableData[this.tableData.length - 1][this.tableData[0].length - 1]).getAttribute("startTimeFull")) + 24 * 60 * 60 * 1000;
     var events = EventMan.events;
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
     var rightNode = gj('<div></div>').addClass('rightContinueEvent  pull-right');
     var icon = gj('<i></i>').addClass('uiIconMiniArrowRight uiIconWhite');
     rightNode.append(icon);
     gj(EventOnDayContent).prepend(rightNode);
       }
   }
     }
 },

 // Initialize  highlighter
 initHighlighter : function() {
   for(var i=0 ; i<this.tableData.length; i++) {
     var row = this.tableData[i];
     for (var j=0; j<row.length; j++) {
       gj(row[j]).off('mousedown').on('mousedown', Highlighter.start);
     }
   }
 },

 callbackHighlighter : function() {
   var startTime = parseInt(Date.parse(Highlighter.firstCell.getAttribute('startTimeFull')));
   var endTime = parseInt(Date.parse(Highlighter.lastCell.getAttribute('startTimeFull')))  + 24*60*60*1000 - 1;
   var d = new Date() ;
   var timezoneOffset = d.getTimezoneOffset() ;
   var currentTime = Highlighter.firstCell.getAttribute('startTime') ;
   eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(Highlighter.firstCell,1,startTime,endTime) ;
   //eXo.webui.UIForm.submitEvent('UIMonthView' ,'QuickAdd','&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime +'&ct='+currentTime+ '&tz=' + timezoneOffset); 
 }
}

eXo = eXo || {};
eXo.calendar = eXo.calendar || {};
eXo.calendar.GUIMan = GUIMan;
return GUIMan;
})(DOMUtil, gj, Highlighter, UIHSelection, UICalendarDragDrop, EventMan);