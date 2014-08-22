(function(gj, base) {
  eXo = eXo || {};
  eXo.calendar = eXo.calendar || {};
  eXo.calendar.EventTooltip = {
      UTC_0: "UTC:0",
      isDnD: false,
      timer: 500,
      getContainer: function(evt) {
        var self = eXo.calendar.EventTooltip;
        if(self._container) delete self._container;
        if(!self._container){
          var eventNode = evt.target;
          eventNode = gj(eventNode).parents('.UICalendarPortlet')[0];
          self._container = gj(eventNode).find('div.uiCalPopover')[0];
          gj(self._container).off('mouseover mouseout click').on({'mouseover':function(evt){
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
        var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
        var eventId = eventNode.getAttribute("eventId");
        var calendarId = eventNode.getAttribute("calid");
        var calendarType = eventNode.getAttribute("caltype");
        var isOccur = eventNode.getAttribute("isOccur");
        var recurId = eventNode.getAttribute("recurId");
        if (recurId == "null") recurId = "";
        uiForm.submitEvent(UICalendarPortlet.portletId + '#' + UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + 
            UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType + '&isOccur=' + 
            isOccur + '&recurId=' + recurId);
      },
      show: function(evt) {
        var _module = window.require("PORTLET/calendar/CalendarPortlet");
        var UICalendarPortlet = _module.UICalendarPortlet;
        var self = eXo.calendar.EventTooltip;
        self.currentEvent = this;
        self.cleanupTimer(evt);
        if(eXo.calendar.EventTooltip.isDnD == true) return;
        self.getContainer(evt);
        self.overTimer = setTimeout(function() {
          var url = eXo.env.portal.context + "/" + _module.restContext;
          var eventId = self.currentEvent.getAttribute("eventid");
          var recurId = self.currentEvent.getAttribute("recurid");
          var isOccur = self.currentEvent.getAttribute("isoccur");
          if(isOccur == "true" && recurId != "null") { // if the event is
            // belong to a
            // repetitive event
            url += "/cs/calendar/getoccurrence/" + self.currentEvent.getAttribute("eventid") + "/" + recurId;
          } else {
            url += "/cs/calendar/geteventbyid/" + eventId;
          }
          self.makeRequest("GET",url);
        },self.timer);
      },
      hide: function(evt) {
        var self = eXo.calendar.EventTooltip;
        self.cleanupTimer(evt);
        self.outTimer = setTimeout(function(){
          self.hideElement();
        },self.timer);
        eXo.calendar.EventTooltip.isDnD == false;
      },
      hideElement: function() {
        gj(eXo.calendar.EventTooltip._container).css('display','none');
      },
      disable: function(evt) {
        this.hideElement();
        if(evt && evt.button != 2) this.isDnD = true;
      },
      enable: function() {
        this.isDnD = false;
      },
      cleanupTimer:function(evt){
        if(this.outTimer) clearTimeout(this.outTimer);
        if(this.overTimer) clearTimeout(this.overTimer);
      },
      makeRequest: function(method, url, queryString) {
        var request = new eXo.portal.AjaxRequest(method, url, queryString) ;
        request.onSuccess = this.render ;
        request.onLoading = function() {
          gj(eXo.calendar.EventTooltip._container).css('display','none');
        } ;
        eXo.portal.CurrentRequest = request ;
        request.process() ;
      },
      parseData: function(req){
        var data = gj.parseJSON(req.responseText);
        var time = this.getRealTime(data);
        return {
          occurrence: data.occurrence,
          virtual: data.virtual,
          event: data.event,
          title: data.summary,
          description: data.description,
          time:time,
          location: data.location
        }
      },
      
      getRealTime: function(data) {
        var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
        var time = "";
        var type = UICalendarPortlet.isAllday(data);
        var startDate = new Date(parseInt(data.startDateTime) + parseInt(data.startTimeOffset));
        var endDate = new Date(parseInt(data.endDateTime) + parseInt(data.endTimeOffset));
        
        if(type == 1 ) {
          return UICalendarPortlet.getDateString(startDate);
        }
        if(type == 2) {
          time = UICalendarPortlet.getDateString(startDate);
          time += ', ' + UICalendarPortlet.getFormattedHour(startDate) + ' - ' + UICalendarPortlet.getFormattedHour(endDate);
          return time;
        }
        else {
          time = UICalendarPortlet.getDateString(startDate) + ', ' + UICalendarPortlet.getFormattedHour(startDate);
          time += ' - ' + UICalendarPortlet.getDateString(endDate) + ', ' + UICalendarPortlet.getFormattedHour(endDate);
          return time;
        }
      },
      
      convertTimezone: function(datetime) {
        var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
        var time = parseInt(datetime.time);
        var eventTimezone = parseInt(datetime.timezoneOffset);
        var settingTimezone = parseInt(UICalendarPortlet.settingTimezone);
        time += (eventTimezone + settingTimezone)*60*1000;
        return time;
      },
      
      render: function(req) {
        var self = eXo.calendar.EventTooltip;
        var data = self.parseData(req);
        if(!data) return ;
        var color = gj(self.currentEvent).attr('class').split(' ')[2];
        if(!color) {
          color = gj(self.currentEvent).find('.eventOnDayBorder').attr('class').split(' ')[1];
        }
        if(color === 'clearfix') {
          color = gj(self.currentEvent).find('.eventContainer').attr('class').split(' ')[1];
        } else if(color == 'weekViewEventBoxes') {
          color = gj(self.currentEvent).find('.eventAlldayContent').attr('class').split(' ')[1];
        }
        var typeIcon = 'uiIconCalTaskMini';
        if(data.event) typeIcon = 'uiIconCalClockMini';
        var html = '<div class="title clearfix"><div class="pull-left"><span class="colorBox ' + color + '"></span></div><div class="text">'  + data.title + '</div></div>';
        html += '<div class="time clearfix"><div class="pull-left"><i class="'+typeIcon+'"></i></div><div class="text">' + data.time + '</div></div>';
        if(data.event && data.occurrence) {
          var className = 'uiIconCalEditRecurring';
          var info = gj(self._container).find(".resourceInfo").find("#edited")[0].innerHTML;
          if (!data.virtual) {
            className = 'uiIconCalRecurring';
            info = gj(self._container).find(".resourceInfo").find("#repeating")[0].innerHTML;
          }
          html += '<div class="time clearfix"><div class="pull-left"><i class="'+className+'"></i></div><div class="text">' + info + '</div></div>';
        }
        if(data.location)    html += '<div class="location clearfix"><div class="pull-left"><i class="uiIconCalCheckinMini"></i></div><div class="text">' + data.location + '</div></div>';
        if(data.description) html += '<div class="description ">' + data.description + '</div>';
        self._container.style.display = "block";
        var popoverContent = gj(self._container).find('.popover-content');
        popoverContent.text('');
        popoverContent.append(html);
        self._container.style.zIndex = 1000;
        self.positioning();
      },
      
      positioning: function() {
        var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
        var offsetTooltip = this._container.offsetParent;
        var offsetEvent = this.currentEvent.offsetParent;
        
        gj(this._container).removeClass("left").addClass("top");
        var extraX = (this.currentEvent.offsetWidth - this._container.offsetWidth)/2;
        var extraY = 0;
        var y = base.Browser.findPosYInContainer(this.currentEvent,offsetTooltip) - this._container.offsetHeight;
        var x = base.Browser.findPosXInContainer(this.currentEvent,offsetTooltip) + extraX;
        this._container.style.top = y + "px";
        
        /* re-set top of popover in case of scroll hidden */
        if(UICalendarPortlet.viewType == "UIDayView") {
          var eventDayTop = gj(offsetEvent).offset().top
          , bottomPopup = (gj(this._container).offset().top + gj(this._container).height() + 14); // increases
          // 14
          // for
          // arrow
          // and
          // margins
          if (eventDayTop > bottomPopup) {
            this._container.style.top = (eventDayTop - (gj(this._container).height() + 6)) + 'px';
          }
        }
        
        if(UICalendarPortlet.viewType == "UIWeekView") {
          var eventWeekTop = gj(offsetEvent).offset().top
          , bottomPopup  = (gj(this._container).offset().top + gj(this._container).height() + 14);
          if (eventWeekTop > bottomPopup) {
            this._container.style.top = (eventWeekTop - (gj(this._container).height() + 6)) + 'px';
          }
        }
        
        this._container.style.left = x + "px";
        var relativeX = base.Browser.findPosX(this._container) + this._container.offsetWidth;
        if(relativeX > document.documentElement.offsetWidth) {
          extraX = document.documentElement.offsetWidth - relativeX;
          x += extraX;
          this._container.style.left = x + "px";
        }
        if(document.body.offsetWidth - Math.round(gj(this.currentEvent).offset().left + gj(this._container).width()) < 0 ) {
          gj(this._container).removeClass("top").addClass("left");
          this._container.style.top = gj(this.currentEvent).offset().top  - (gj(this._container).height() /2 ) + (gj(this.currentEvent).height()/2) + 'px';
          this._container.style.left = gj(this.currentEvent).offset().left - (gj(this._container).width() + 5) + 'px';
        }
        this._container.style.left = x + "px";
        var relativeX = base.Browser.findPosX(this._container) + this._container.offsetWidth;
        if(relativeX > document.documentElement.offsetWidth) {
          extraX = document.documentElement.offsetWidth - relativeX;
          x += extraX;
          this._container.style.left = x + "px";
        }
        if(document.body.offsetWidth - Math.round(gj(this.currentEvent).offset().left + gj(this._container).width()) < 0 ) {
          gj(this._container).removeClass("top").addClass("left");
          this._container.style.top = gj(this.currentEvent).offset().top  - (gj(this._container).height() /2 ) + (gj(this.currentEvent).height()/2) + 'px';
          this._container.style.left = gj(this.currentEvent).offset().left - (gj(this._container).width() + 5) + 'px';
        }
      }
  };
  return eXo.calendar.EventTooltip;
})($, base);