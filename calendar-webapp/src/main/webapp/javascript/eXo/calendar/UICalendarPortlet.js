(function(base, CalendarLayout, UIWeekView, UICalendarMan, gj, Reminder, UICalendars, uiForm, uiPopupWindow, 
		ScheduleSupport, CSUtils, DOMUtil, UIContextMenu, CalDateTimePicker, DateTimeFormatter, UIHSelection, UISelection, UIDayView, dateUtils, cometd) {
    var _module = {};
    eXo.calendar = eXo.calendar || {};
    function UICalendarPortlet() {
        this.clickone = 0 ;
        this.portletId = "calendars";
        this.currentDate = 0;
        this.CELL_HEIGHT = 20;
        this.MINUTE_PER_CELL = 30;
        this.PIXELS_PER_MINUTE = this.CELL_HEIGHT / this.MINUTE_PER_CELL;
        this.MINUTES_PER_PIXEL = this.MINUTE_PER_CELL / this.CELL_HEIGHT;
        this.originalHeightOfEventDayContent = null;
        this.timeShiftE = 0;
        this.timeShiftT = 0;
        this.dayDiff = 0;
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
    }

    UICalendarPortlet.prototype.notify = function(eventObj) {
        var uiCalendarWorkingContainer = gj(eventObj).parents("#UICalendarWorkingContainer")[0];
        var msg = "<div style='padding:3px;color:red;'>" + uiCalendarWorkingContainer.getAttribute("msg") + "</div>";
        var html = Reminder.generateHTML(msg) ;
        var popup = gj(Reminder.createMessage(html, msg)).find('div.UIPopupNotification')[0];
        eXo.calendar.Box.config(popup,popup.offsetHeight, 5, Reminder.openCallback, Reminder.closeBox) ;
        window.focus() ;
        return ;
    };

    UICalendarPortlet.prototype.getOrginalPosition = function(eventObj) {
        if(eventObj.getAttribute("orginalSize")){
            return eventObj.getAttribute("orginalSize");
        }
    };

    /**
     *
     * @param {Object} calendarForm : calendar form DOM node
     * @return {Object} a checked calendar id
     */
    UICalendarPortlet.prototype.getCheckedCalendar = function(calendarForm) {
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
    UICalendarPortlet.prototype.addQuickShowHidden = function(obj, type, startTime) {
        if(startTime == undefined || startTime <= 0) {
            startTime = _module.UICalendarPortlet.getCurrenTimeWithTimeZone();
        } else {
            //Correct startTime with user's timezone
            var d = new Date();
            startTime = startTime + d.getTimezoneOffset() * 60 * 1000  + _module.UICalendarPortlet.settingTimezone * 60 * 1000;
            startTime = _module.UICalendarPortlet.ceil(startTime, 30 * 60 * 1000);
        }
        if(parseInt(type) ==1) {
            this.timeShiftE = parseInt(gj("#UIQuickAddEvent").parents("#QuickAddEventContainer").attr("timeshift"));
            this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 30 * this.timeShiftE * 60 * 1000) ;
        }
        else if(parseInt(type) ==2) {
            this.timeShiftT = parseInt(gj("#UIQuickAddTask").parents("#QuickAddEventContainer").attr("timeshift"));
            this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 30 * this.timeShiftT * 60 * 1000) ;
        } else this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 30 * 60 * 1000) ;
    } ;

    /**
     * Show Quick add event and task form
     * @param {obj, type} has action object, type of form : event 1 | task 2 | calendarId selected calendar
     */
    UICalendarPortlet.prototype.addQuickShowHiddenWithId = function(obj, type, id) {
        var startTime = _module.UICalendarPortlet.getCurrenTimeWithTimeZone();
        /**
         * @since relooking
         * for relooking, we change default calendar Id(calendar created when new user is created) to username
         * so we need to change the way to get calendar Id
         * id is in form: 'objectId=X&calType=Y&...
         * get calType and calId by splitting
         */
        var calType = id.split('&')[1].split('=')[1];
        var calId = id.split('&')[0].split('=')[1];
        var selectedCalId = calType + ":" + calId;
        if(parseInt(type) ==1) {
            this.timeShiftE = parseInt(gj("#UIQuickAddEvent").parents("#QuickAddEventContainer").attr("timeshift"));
            this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 30*this.timeShiftE*60*1000, selectedCalId) ;
        }
        else if(parseInt(type) ==2) {
            this.timeShiftT = parseInt(gj("#UIQuickAddTask").parents("#QuickAddEventContainer").attr("timeshift"));
            this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 30*this.timeShiftT*60*1000, selectedCalId) ;
        }

    } ;

    UICalendarPortlet.prototype.getCurrenTimeWithTimeZone = function() {
        var d = new Date();
        var startTime = d.getTime() + d.getTimezoneOffset() * 60 * 1000  + _module.UICalendarPortlet.settingTimezone * 60 * 1000;
        return CSUtils.ceil(startTime, 30 * 60 * 1000);
    }

    /**
     * Show Quick add event and task form with selected time
     * @param {obj, type, from, to} has action object, type of form : event 1 | task 2, from in milliseconds, to in milliseconds
     * @param {type} 1: event, 2:task
     * @param {obj} HTML element of 'UIWeekViewGridAllDay' => create all day event
     */
    UICalendarPortlet.prototype.addQuickShowHiddenWithTime = function(obj, type, fromMilli, toMilli, id) {
        var tempTimeShift = ((toMilli - fromMilli)/30/60/1000);
        if(parseInt(type) == 1) {
            if(tempTimeShift > 2 && tempTimeShift < 47) this.timeShiftE = tempTimeShift;
        } else if(parseInt(type) == 2) {
            if(tempTimeShift > 2 && tempTimeShift < 47) this.timeShiftT = tempTimeShift;
        }
        var CalendarWorkingWorkspace =  _module.UICalendarPortlet.getElementById("UICalendarWorkingContainer");
        var id = (id)?id:this.getCheckedCalendar(this.filterForm);
        DOMUtil.cleanUpHiddenElements();
        var UIQuickAddEventPopupWindow = gj(CalendarWorkingWorkspace).find("#UIQuickAddEventPopupWindow")[0];
        var UIQuickAddTaskPopupWindow = gj(CalendarWorkingWorkspace).find("#UIQuickAddTaskPopupWindow")[0];
        var selectedCategory = (_module.UICalendarPortlet.filterSelect) ? _module.UICalendarPortlet.filterSelect : null;
        if((selectedCategory != null) && (selectedCategory.options.length < 1)) {
            var divEventCategory = gj(_module.UICalendarPortlet.filterSelect).parents(".EventCategory")[0] ;
            return;
        }

        var tmpMenuElement = document.getElementById("tmpMenuElement");
        if (tmpMenuElement) uiPopup.hide(tmpMenuElement) ;
        var formater = DateTimeFormatter ;
        var data = {
            from:parseInt(fromMilli),
            fromTime:parseInt(fromMilli),
            to:parseInt(toMilli),
            toTime:parseInt(toMilli),
            isAllday:false,
            calendar:id,
            category:(selectedCategory)? selectedCategory.value : null
        };


        var fromD = new Date(fromMilli);
        var toD = new Date(toMilli);
        data.isAllday = ((fromD.getHours() == 0 && fromD.getMinutes() == 0) && ( toD.getHours() == 23 && toD.getMinutes() == 59));
        if(data.isAllday && tempTimeShift > 46) {

            if(parseInt(type) ==1) {
                this.timeShiftE = parseInt(gj("#UIQuickAddEvent").parents("#QuickAddEventContainer").attr("timeshift"));
                data.fromTime = parseInt(fromMilli + 10*60*60*1000);
                data.toTime =  parseInt(fromMilli + 10*60*60*1000 + 30*60*this.timeShiftE*1000);
            }
            else if(parseInt(type) ==2) {
                this.timeShiftT = parseInt(gj("#UIQuickAddTask").parents("#QuickAddEventContainer").attr("timeshift"));
                data.fromTime = parseInt(fromMilli + 10*60*60*1000);
                data.toTime =  parseInt(fromMilli + 10*60*60*1000 + 30*60*this.timeShiftT*1000);
            }

        }
        if(parseInt(type) == 1) {
            var uiform = gj(UIQuickAddEventPopupWindow).find("#UIQuickAddEvent")[0] ;
            uiform.reset();
            this.fillData(uiform, data, data.isAllday) ;
            uiPopupWindow.show("UIQuickAddEventPopupWindow");
            uiPopup.hide("UIQuickAddTaskPopupWindow") ;
        } else if(parseInt(type) == 2) {
            var uiform = gj(UIQuickAddTaskPopupWindow).find("#UIQuickAddTask")[0] ;
            uiform.reset() ;
            this.fillData(uiform, data, data.isAllday);
            uiPopupWindow.show("UIQuickAddTaskPopupWindow");
            uiPopup.hide("UIQuickAddEventPopupWindow");
        }
        gj('input#eventName').focus(); //autofocus the event summary input field
    }
    
    /**
     * fill data to quick event/task form
     * @param {uiform, data} uifrom obj or id, data is array of value for each element of form
     */
    UICalendarPortlet.prototype.fillData = function(uiform, data, isAllDayEvent) {
        this.dayDiff = dateUtils.dateDiff(data.from, data.to);
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

        var formater = DateTimeFormatter ;
        var timeType = "HH:MM" ;
        var dateType = fromField.getAttribute("format").replace("MM","mm") ;
        if(this.timeFormat == "hh:mm a")  timeType = formater.masks.shortTime ;
        eventName.value = "";
        description.value = "";
        fromField.value = formater.format(data.from, dateType);
        fromFieldTime.value = formater.format(data.fromTime, timeType);
        gj(fromFieldTime).nextAll("input")[0].value = formater.format(data.fromTime, timeType);
        toField.value = formater.format(data.to, dateType);
        toFieldTime.value = formater.format(data.toTime, timeType);
        gj(toFieldTime).nextAll("input")[0].value = formater.format(data.toTime, timeType);
        isAllday.checked = isAllDayEvent;
        if(isAllDayEvent) this.showHideTime(isAllday);
        if(data.calendar)
            for(i=0; i < calendar.options.length;  i++) {
                var value = calendar.options[i].value ;
                if(value.match(data.calendar) != null){
                    calendar.options[i].selected = true;
                    break;
                }
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
    

    UICalendarPortlet.prototype.setTimeValue = function(event, start,end,currentCol) {
        event.setAttribute("startTime",start);
        event.setAttribute("endTime",end);
        if(currentCol) event.setAttribute("eventindex",currentCol.getAttribute("eventindex"));
    };

    /**
     * Gets working days of week from user setting then overrides weekdays property of UICalendarPorlet object
     * @param {Object} weekdays
     */
    UICalendarPortlet.prototype.getWorkingdays = function(weekdays) {
        this.weekdays = weekdays;
    };

    /**
     * Apply common setting for portlet
     * @param param1 Time interval in minutes
     * @param param2 User working time in minutes
     * @param param3 User time format
     * @param param4 Portlet id
     */
    UICalendarPortlet.prototype.setting = function() {
        var UICalendarPortlet = _module.UICalendarPortlet;
        this.interval = ((arguments.length > 0) && (isNaN(parseInt(arguments[0])) == false)) ? parseInt(arguments[0]) : parseInt(15);
        this.interval = this.minsToPixels(this.interval);
        var workingStart = ((arguments.length > 1) && (isNaN(parseInt(arguments[1])) == false) && (arguments[1] != "null")) ? arguments[1] : "";
        workingStart = Date.parse("1/1/2007 " + workingStart);
        this.workingStart = dateUtils.timeToMin(workingStart);
        this.timeFormat = (arguments.length > 2) ? gj.trim(new String(arguments[2])) : null;
        this.portletName = arguments[3];
        this.portletNode = gj("#" + this.portletName).parents(".PORTLET-FRAGMENT")[0];
    };

    /**
     * Scroll vertical scrollbar to position of active calendar event
     * @param {Object} obj DOM element
     * @param {Object} container DOM element contains all calendar events
     */
    UICalendarPortlet.prototype.setFocus = function() {
        if(_module.UICalendarPortlet.getElementById("UIWeekView")){
            var obj = _module.UICalendarPortlet.getElementById("UIWeekViewGrid") ;
            var container = gj(obj).parents(".eventWeekContent")[0] ;
        }
        else if(_module.UICalendarPortlet.getElementById("UIDayView")){
            var obj = _module.UICalendarPortlet.getElementById("UIDayView") ;
            obj = gj(obj).find("div.eventBoardContainer")[0];
            var container = gj(obj).parents(".eventDayContainer")[0];
        } else return ;

        var events = gj(obj).find("div.eventContainerBorder");
        events = this.getBlockElements(events);
        var len = events.length;

        var scrollTop =  dateUtils.timeToMin((new Date()).getTime());
        if(this.workingStart){
            if(len == 0) {
                scrollTop = (this.workingStart - 100);
            }
            else {
                scrollTop = (this.hasEventThrough(scrollTop,events))? scrollTop : (this.workingStart - 100) ;
            }
        }

        var lastUpdatedId = obj.getAttribute("lastUpdatedId");
        if (lastUpdatedId && (lastUpdatedId != "null") && (len !== 0)) {
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
    UICalendarPortlet.prototype.hasEventThrough = function(min,events) {
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
    UICalendarPortlet.prototype.autoHide = function(evt) {
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
    UICalendarPortlet.prototype.showHide = function(obj) {
        if (obj.style.display != "block") {
            DOMUtil.cleanUpHiddenElements();
            obj.style.display = "block";
            DOMUtil.listHideElements(obj);
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
    };

    UICalendarPortlet.prototype.resetSpaceDefaultLayout = function(){
        _module.UICalendarPortlet.switchLayout(1);
    };

    UICalendarPortlet.prototype.resetLayoutCallback = function(){
        var UICalendarPortlet = _module.UICalendarPortlet;
        var isSpace = UICalendarPortlet.isSpace;
        if(isSpace != null && isSpace != "") {
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
    UICalendarPortlet.prototype.checkLayout = function() {
        var isSpace = _module.UICalendarPortlet.isSpace;
        if(isSpace != null && isSpace != "") {
          base.Browser.setCookie(_module.LayoutManager.layoutId,"1",1);
        }
        _module.LayoutManager.layouts = [] ;
        _module.LayoutManager.switchCallback = _module.UICalendarPortlet.switchLayoutCallback;
        _module.LayoutManager.resetCallback = _module.UICalendarPortlet.resetLayoutCallback;
        _module.LayoutManager.check();
        gj('#ShowHideAll').find('i').css('display','block');
    };

    /**
     * Switch among types of layout
     * @param {int} layout Layout value in order number
     */
    UICalendarPortlet.prototype.switchLayout = function(layout) {
        var layoutMan = _module.LayoutManager ;
        if(layout == 0){
            layoutMan.reset();
            return ;
        }
        layoutMan.switchLayout(layout);
        _module.UICalendarPortlet.resortEvents();

        if (layout === 1) {
            UICalendars.init("UICalendars");
        }
    };

    /**
     * Checks a DOM element is visible or hidden
     * @param {Object} obj DOM element
     * @return Boolean value
     */
    UICalendarPortlet.prototype.isShow = function(obj) {
        if (obj.style.display != "none")
            return true;
        return false;
    };

    /**
     * Gets all visible event element
     * @param {Object} elements All calendar event
     * @return An array of DOM element
     */
    UICalendarPortlet.prototype.getBlockElements = function(elements) {
        var el = new Array();
        var len = elements.length;
        for (var i = 0; i < len; i++) {
            if (this.isShow(elements[i]))
                el.push(elements[i]);
        }
        return el;
    };

    /**
     * Sets width for a DOM element in percent unit
     * @param {Object} element A DOM element
     * @param {Object} width Width of element
     */
    UICalendarPortlet.prototype.setWidth = function(element, width) {
        element.style.width = width + "%";
    };

    /**
     * Gets starttime and endtime attribute of a calendar event element
     * @param {Object} el A calendar event element
     * @return A array includes two elements that are start and end time
     */
    UICalendarPortlet.prototype.getSize = function(el) {
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
    UICalendarPortlet.prototype.getInterval = function(el) {
        var bottom = new Array();
        var interval = new Array();
        var size = null;
        if (!el || (el.length <= 0)) {
            return null;
        }
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
     * Deal with incorrect event sorting when portlet loads in the first times
     */
    UICalendarPortlet.prototype.onLoad = function() {
        eXo.calendar.UICalendarPortlet.checkFilter() ;
//        window.setTimeout("eXo.calendar.UICalendarPortlet.checkFilter() ;", 2000);
    };

    /**
     * Scroll to last update event in list view
     * @param {Object} uiListContainer DOM element
     */
    UICalendarPortlet.prototype.scrollToActiveEventInListView = function(uiListContainer) {
        var events = gj(uiListContainer).find("tr.uiListViewRow");
        events = this.getBlockElements(events);
        var len = events.length;
        var scrollTop;

        var lastUpdatedId = gj(uiListContainer).attr("lastUpdatedId");
        if (lastUpdatedId && (lastUpdatedId != "null")) {
            for (var i = 0; i < len; i++) {
                if (events[i].getAttribute("eventId") == lastUpdatedId) {
                    scrollTop = gj(events[i]).offset().top;
                    break;
                }
            }
        }

        uiListContainer.scrollTop = scrollTop - 145;
    };

    /**
     * Show notification for Edit Calendar Popup
     */
    UICalendarPortlet.prototype.showEditCalNotif = function (calendarName,userName,keyMsg1,keyMsg2) {
        var $notif = gj('#editCalendarNotification');
        $notif.find('.calendarName')[0].innerHTML = calendarName;
        var message = $notif.find('div#msg1')[0].innerHTML +" <strong>" + userName + "</strong> " + $notif.find('div#msg2')[0].innerHTML;
        $notif.find('.message')[0].innerHTML = message;
        $notif.show().delay(5000).fadeOut();
    };

    /**
     * Updates title of event when dragging calendar event
     * @param {Object} events DOM element contains a calendar event
     * @param {Object} posY Position of the event
     */
    UICalendarPortlet.prototype.updateTitle = function(events, posY, type) {
        var min = this.pixelsToMins(posY);
        var timeFormat = events.getAttribute("timeFormat");
        var title = gj(events).find("div.eventTitle")[0];
        var html = gj(title).html();
        var arr = html.split("</i>");
        var str = "";
        for(var j = 0; j < arr.length - 1; j++) {
            str += arr[j] + "</i>";
        }

        var delta = parseInt(events.getAttribute("endTime")) - parseInt(events.getAttribute("startTime")) ;
        timeFormat = (timeFormat) ? gj.globalEval(timeFormat) : {
            am: "AM",
            pm: "PM"
        };

        var timeValue;
        if (type == 1) {
            timeValue = dateUtils.minToTime(min, timeFormat) + " - " + dateUtils.minToTime(min + this.pixelsToMins(events.offsetHeight), timeFormat);
            title.innerHTML = str + timeValue;
            events.setAttribute('titleHTML', timeValue);
        }
        else {
            if (delta > 30) {
                timeValue = dateUtils.minToTime(min, timeFormat) + " - " + dateUtils.minToTime(min + delta, timeFormat);
                str += timeValue;
                title.innerHTML = str;
                events.setAttribute('titleHTML', timeValue);
            } else {
                timeValue = dateUtils.minToTime(min,timeFormat);
                str += timeValue;
                title.innerHTML = str;
                events.setAttribute('titleHTML', timeValue);
            }
        }
    };

    /**
     * Callback method when right click in list view
     * @param {Object} evt Mouse event
     */
    UICalendarPortlet.prototype.listViewCallback = function(evt) {
        var _e = window.event || evt;
        var src = _e.srcElement || _e.target;
        if (!gj(src).hasClass("uiListViewRow"))
            src = gj(src).parents(".uiListViewRow")[0];
        var eventId = src.getAttribute("eventid");
        var calendarId = src.getAttribute("calid");
        var calType = src.getAttribute("calType");
        var isOccur = src.getAttribute("isOccur");
        var recurId = src.getAttribute("recurId");
        var isEditable = src.getAttribute("isEditable");

        map = {
            "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
            "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
            "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType,
            "isOccur\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "isOccur=" + isOccur,
            "recurId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "recurId=" + recurId
        };

        var $event = gj(src);
        var $checkbox = $event.find('input[type="checkbox"][name^="Event"]');
        var $form = $event.closest('form.UIForm');
        var $checked = $form.find('input[type="checkbox"][name^="Event"]:checked');
        if($checked.length > 0 && !$checkbox.is(':checked')) {
          $checked.attr('checked',false);
          $checked = $form.find('input[type="checkbox"][name^="Event"]:checked');
        }
        $checkbox.attr('checked', true);

        var items = gj(UIContextMenu.menuElement).find("a");
        for (var i = 0; i < items.length; i++) {
            if (gj(items[i]).hasClass("eventAction")) {
                items[i].parentNode.style.display = "block";

                if (isEditable && (isEditable == "false"))
                {
                    if ((items[i].href.indexOf("Edit") >= 0) ||
                        (items[i].href.indexOf("Delete") >= 0) ||
                        (items[i].href.indexOf("ExportEvent") >= 0))
                    {
                        items[i].parentNode.style.display = "none";
                    }
                } else if($checked.length > 1 && items[i].href.indexOf("Delete") > 0) {
                  items[i].href = items[i].getAttribute('deleteActionLink');
                }
            }
        }

        UIContextMenu.changeAction(UIContextMenu.menuElement, map);
    };

    /**
     * Callback method when right click in day view
     * @param {Object} evt Mouse event
     */
    UICalendarPortlet.prototype.dayViewCallback = function(evt) {

        var _e = window.event || evt;
        var src = _e.srcElement || _e.target;
        var isEditable;
        var map = null;
        var timeShiftE = parseInt(gj("#UIQuickAddEvent").closest("#QuickAddEventContainer").attr("timeshift"));
        var timeShiftT = parseInt(gj("#UIQuickAddTask").closest("#QuickAddEventContainer").attr("timeshift"));

        if (src.nodeName == "TD") {
            src = gj(src).parents("tr")[0];
            var startTime = parseInt(Date.parse(src.getAttribute('startfull')));
            var endTime = startTime + 30*60*1000 ;
            var items = gj(UIContextMenu.menuElement).find("a");

            for(var i = 0; i < items.length; i++){
                var aTag = items[i];
                if(gj(aTag).hasClass("createEvent")) {
                    endTime = startTime + timeShiftE * 30 * 60 * 1000;
                    aTag.href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");"
                } else if(gj(aTag).hasClass("createTask")) {
                    endTime = startTime + timeShiftT * 30 * 60 * 1000;
                    aTag.href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");"
                }
            }

        }
        else {
            src = (gj(src).hasClass("eventBoxes")) ? src : gj(src).parents(".eventBoxes")[0];
            var eventId = src.getAttribute("eventid");
            var calendarId = src.getAttribute("calid");
            var calType = src.getAttribute("calType");
            var isOccur = src.getAttribute("isOccur");
            var recurId = src.getAttribute("recurId");
            isEditable  = src.getAttribute("isEditable");

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

        var items = gj(UIContextMenu.menuElement).find("a");
        for (var i = 0; i < items.length; i++) {
            if (gj(items[i]).hasClass("eventAction")) {
                items[i].parentNode.style.display = "block";

                if (isEditable && (isEditable == "false"))
                {
                    if ((items[i].href.indexOf("Edit") >= 0) ||
                        (items[i].href.indexOf("Delete") >= 0) ||
                        (items[i].href.indexOf("ExportEvent") >= 0))
                    {
                        items[i].parentNode.style.display = "none";
                    }
                }
            }
        }

        UIContextMenu.changeAction(UIContextMenu.menuElement, map);
    };

    /**
     * Callback method when right click in week view
     * @param {Object} evt Mouse event
     */
    UICalendarPortlet.prototype.weekViewCallback = function(evt) {
        var src = evt.target;
        var map = null;
        var obj = gj(src).closest(".weekViewEventBoxes").get(0);
        var items = gj(UIContextMenu.menuElement).find("a");
        if (obj) {
            var eventId = obj.getAttribute("eventid");
            var calendarId = obj.getAttribute("calid");
            var calType = obj.getAttribute("calType");
            var isOccur = obj.getAttribute("isOccur");
            var recurId = obj.getAttribute("recurId");
            var isEditable = obj.getAttribute("isEditable");

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

            if(!gj(obj).hasClass("eventAlldayContainer")){
                var container = gj(src).parents(".eventWeekContent")[0];
                var mouseY = (base.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
                obj =parseInt(gj(src).parents("td")[0].getAttribute("startTime")) + mouseY;
            } else obj = null;

            for (var i = 0; i < items.length; i++) {
                if (gj(items[i]).hasClass("eventAction")) {
                    items[i].parentNode.style.display = "block";
                    items[i].href = UIContextMenu.replaceall(String(items[i].href), map);

                    if (isEditable && (isEditable == "false"))
                    {
                        if ((items[i].href.indexOf("Edit") >= 0) ||
                            (items[i].href.indexOf("Delete") >= 0) ||
                            (items[i].href.indexOf("ExportEvent") >= 0))
                        {
                            items[i].parentNode.style.display = "none";
                        }
                    }

                }
                else {
                    if(gj(items[i]).hasClass("createEvent")){
                        items[i].style.display="none" ;
                    } else if (gj(items[i]).hasClass("createTask")) {
                        items[i].style.display="none" ;
                    }
                }
            }

        } else {
            var container = gj(src).parents(".eventWeekContent")[0];
            var timeShiftE = parseInt(gj("#UIQuickAddEvent").closest("#QuickAddEventContainer").attr("timeshift"));
            var timeShiftT = parseInt(gj("#UIQuickAddTask").closest("#QuickAddEventContainer").attr("timeshift"));
            var mouseY = (base.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
            obj = gj(evt.target).closest("td").get(0);
            map = Date.parse(obj.getAttribute("startFull"));
            for (var i = 0; i < items.length; i++) {
                if (items[i].style.display == "block") {
                    items[i].style.display = "none";
                }
                else {
                    items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/, "startTime=" + map);
                    var fTime = parseInt(map);
                    var tTime = fTime + 30*60*1000 ;

                    if(gj(items[i]).hasClass("createEvent")){
                        tTime = fTime + timeShiftE*30*60*1000;
                        items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 1,"+fTime+","+tTime+");"
                        if(isNaN(fTime)) {
                            items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 1);" ;
                        }
                    } else if (gj(items[i]).hasClass("createTask")) {
                        tTime = fTime + timeShiftT*30*60*1000;
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
    UICalendarPortlet.prototype.monthViewCallback = function(evt) {
        var _e = window.event || evt;
        var src = _e.srcElement || _e.target;
        var objvalue = "";
        var links = gj(UIContextMenu.menuElement).find("a");
        var isEditable;
        var $checked = new Array();

        if (!gj(src).parents(".eventBoxes")[0]) {
            var eventCell = gj(src);
            if (gj(src).hasClass('dayBox')) {
                eventCell = gj(src).parent('td');
            }

            if (eventCell.attr('startTime')) {
                var startTime = parseInt(Date.parse(eventCell.attr('startTimeFull')));
                var endTime = startTime  + 24*60*60*1000 - 1;
                for(var i = 0; i < links.length; i++){
                    if (gj(links[i]).hasClass("createEvent")) {
                        links[i].href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");";
                    } else if(gj(links[i]).hasClass("createTask")) {
                        links[i].href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");";
                    }
                }
            }
        }
        else {
            if (objvalue = gj(src).parents(".dayContentContainer")[0]) {
                var eventId = objvalue.getAttribute("eventId");
                var calendarId = objvalue.getAttribute("calId");
                var calType = objvalue.getAttribute("calType");
                var isOccur = objvalue.getAttribute("isOccur");
                var recurId = objvalue.getAttribute("recurId");
                isEditable  = src.getAttribute("isEditable");
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

            var $event = gj(src).closest('.dayContentContainer');
            var $checkbox = $event.find('input[type="checkbox"][name^="Event"]');
            var $monthViewForm = gj('form#UIMonthView');
            $checked = $monthViewForm.find('input[type="checkbox"][name^="Event"]:checked');
            if($checked.length > 0 && !$checkbox.is(':checked')) {
                $checked.attr('checked',false);
                $checked = $monthViewForm.find('input[type="checkbox"][name^="Event"]:checked');
            }
            $checkbox.attr('checked', true);
        }

        var items = gj(UIContextMenu.menuElement).find("a");
        for (var i = 0; i < items.length; i++) {
            if (gj(items[i]).hasClass("eventAction")) {
                items[i].parentNode.style.display = "block";

                if (isEditable && (isEditable == "false")) {
                    if ((items[i].href.indexOf("Edit") >= 0) ||
                        (items[i].href.indexOf("Delete") >= 0) ||
                        (items[i].href.indexOf("ExportEvent") >= 0))
                    {
                        items[i].parentNode.style.display = "none";
                    }
                } else if($checked.length > 1 && items[i].href.indexOf("Delete") > 0) {
                    items[i].href = items[i].getAttribute('deleteActionLink');
                }
            }
        }
    };

    UICalendarPortlet.prototype.topbarDeleteAction = function(formId, elementId) {
        var $form = gj('form#' + formId);
        var $element = $form.find('#' + elementId);

        var $checked = $form.find('input[type="checkbox"][name^="Event"]:checked');
        if($checked.length > 1) {
            var action = $element.attr('multiDeleteAction');
            if(action) {
                gj.globalEval(action);
            }
        } else {
            var $event = $checked.closest('.eventBoxes, .uiListViewRow');
            var action = $element.attr('singleDeleteAction');
            if(action) {
                var eventId = $event.attr("eventId");
                var calendarId = $event.attr("calId");
                var calType = $event.attr("calType");
                var isOccur = $event.attr("isOccur");
                var recurId = $event.attr("recurId");
                if (recurId == "null") recurId = "";
                var map = {
                  "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
                  "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
                  "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType,
                  "isOccur\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "isOccur=" + isOccur,
                  "recurId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "recurId=" + recurId
                };
                action = UIContextMenu.replaceall(action, map);
                gj.globalEval(action);
            }
        }
    };

    /**
     * Gets all calendar events of a calendar by its id
     * @param {Object} events All calendar events
     * @param {Object} calid Calendar id
     * @return All events of certain calendar
     * @deprecated not used anymore
     */
    UICalendarPortlet.prototype.getEventsByCalendar = function(events, calid) {
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
     * @deprecated not used any more
     */
    UICalendarPortlet.prototype.getEventsForFilter = function(events) {
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
    UICalendarPortlet.prototype.filterByGroup = function() {
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
     * Make events disappear in the view if event belong to calendar matching the calendar id parameter
     * @param {Object} calid Calendar id
     * @param {Boolean} checked Status of calendar(activated or disactivated)
     */
    UICalendarPortlet.prototype.runFilterByCalendar = function(calid, checked) {
        var uiCalendarViewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer"),
            UICalendarPortlet       = _module.UICalendarPortlet;
        if (!uiCalendarViewContainer) { return; }

        var className = (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) ? "weekViewEventBoxes" : "eventBoxes",
            events    = gj(uiCalendarViewContainer).find("div." + className);
        if (!events) { return ; }
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
     * Gets events
     */
    UICalendarPortlet.prototype.getEvents = function() {
        var viewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer");
        if (!viewContainer) { return ; }

        var eventClass = (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) ? "weekViewEventBoxes" : "eventBoxes";
        return gj(viewContainer).find("div." + eventClass);
    };


    /**
     * Filter events according to selected event category and calendar checkboxes
     */
    UICalendarPortlet.prototype.filterEvents = function () {

        selectBox = gj(_module.UICalendarPortlet.filterSelect);
        _module.lastSelectedCategory = selectBox.val();

        if(gj('#UIListContainer').size() == 0) { //except List View
            var calendarsFiltered = new Array(),
                UICalendarPortlet = _module.UICalendarPortlet,
                uiCalendars       = UICalendarPortlet.filterForm,
                events            = UICalendarPortlet.getEvents();

            if (!events) { return ; }

            if (uiCalendars) {
                var checkboxes = gj(uiCalendars).find("input.checkbox"),
                    len        = checkboxes.length;
                for (var i = 0; i < len; i++) {
                    if (checkboxes[i].checked) {
                        calendarsFiltered.push(checkboxes[i].name);
                    }
                }
            }

            if (selectBox) {
                selectedCategory = selectBox.val();
            }

            var length = events.length,
                eventCategory,
                calendarId;

            for (var i = 0; i < length; i++) {
                events[i].style.display = "none";
                eventCategory = events[i].getAttribute("eventCat");
                calendarId    = events[i].getAttribute("calId");

                if (selectedCategory === "defaultEventCategoryIdAll") {
                    if (gj.inArray(calendarId,  calendarsFiltered) > -1) {
                        events[i].style.display = "block";
                    }
                }
                else {
                    if ((gj.inArray(calendarId, calendarsFiltered) > -1) && (eventCategory === selectedCategory)) {
                        events[i].style.display = "block";
                    }
                }
            }
            _module.UICalendarPortlet.resortEvents();
        }
    };


    /**
     * Resort event after doing something
     */
    UICalendarPortlet.prototype.resortEvents = function() {
        try {
            if (_module.UICalendarPortlet.getElementById("UIMonthView"))
                _module.UICalendarMan.initMonth();
            if (_module.UICalendarPortlet.getElementById("UIDayViewGrid"))
                UIDayView.showEvent();
            if (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) {
                _module.UICalendarMan.initWeek();
                _module.UIWeekView.init();
            }
        }
        catch (e) {

        };

    };


    /**
     * Filters calendar event by calendar
     */
    UICalendarPortlet.prototype.filterByCalendar = function() {
        var calid      = this.getAttribute("calId"),
            styleEvent = "none",
            checkBox   = gj(this).find('input.checkbox')[0],
            checked    = checkBox.checked,
            imgChk     = gj(this).find('span.checkbox')[0],
            events     = _module.UICalendarPortlet.getEvents();

        if (checked) {
            styleEvent = "none";
            checkBox.checked = false;
            imgChk.className = "iconUnCheckBox checkbox";
        } else {
            styleEvent = "block";
            checkBox.checked = true;
            imgChk.className = "iconCheckBox checkbox";
        }

        if ((!events || events.length == 0) && _module.UICalendarPortlet.getElementById("UIListView")) {
            uiForm.submitForm('UICalendars','Tick', true);
        }
        if (!events) return;
        var len = events.length;

        for (var i = 0; i < len; i++) {
            if (events[i].getAttribute("calId") == calid) {
                events[i].style.display = styleEvent;
                var chkEvent = gj(events[i]).find('input.checkbox')[0];
                if (chkEvent) {
                    chkEvent.checked = false;
                    chkEvent.setAttribute('value', false);
                }
            }
        }

        _module.UICalendarPortlet.filterEvents();
    };

    /**
     * Filters events by event category
     */
    UICalendarPortlet.prototype.filterByCategory = function() {
        var uiCalendarViewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer");
        if (!uiCalendarViewContainer) {  return ;  }

        var className = (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) ? "weekViewEventBoxes" : "eventBoxes",
            events    = gj(uiCalendarViewContainer).find("div." + className),
            len       = events.length,
            category  = this.options[this.selectedIndex].value;

        _module.UICalendarPortlet.selectedCategory = category;

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

        _module.UICalendarPortlet.filterEvents();
    };

    /**
     * Filters event by event category
     */
    UICalendarPortlet.prototype.runFilterByCategory = function() {
        var uiCalendarViewContainer = _module.UICalendarPortlet.getElementById("UICalendarViewContainer"),
            selectBox               = gj(uiCalendarViewContainer).find('select.selectbox')[0];

        if (!selectBox) { return ; }
        var category  = (selectBox.selectedIndex >= 0 ) ? selectBox.options[selectBox.selectedIndex].value : null,
            className = (_module.UICalendarPortlet.getElementById("UIWeekViewGrid")) ? "weekViewEventBoxes" : "eventBoxes",
            events    = gj(uiCalendarViewContainer).find("div." + className),
            len       = events.length;

        for (var i = 0; i < len; i++) {
            if (category == events[i].getAttribute("eventCat")) {
                events[i].style.display = "block";
            }
            else {
                if (category == "" || category == "all" || category == "defaultEventCategoryIdAll") {
                    events[i].style.display = "block";
                }
                else
                    events[i].style.display = "none";
            }
        }
    };

    UICalendarPortlet.prototype.runAction = function(obj) {
        var actionLink = obj.getAttribute("actionLink");
        var categoryId = this.filterSelect.options[this.filterSelect.selectedIndex].value;
        actionLink = actionLink.replace("')","&categoryId="+categoryId+"')");
        gj.globalEval(actionLink);
    };

    /**
     * Gets the <select> element that contains event category and sets up onchange action
     * @param {Object} formId Id of form contains event category select element
     */
    UICalendarPortlet.prototype.getFilterSelect = function(formId) {
        var form;
        if (typeof(formId) === "string") {
            form = _module.UICalendarPortlet.getElementById(formId);
        }
        else { return ; }

        var eventCategory = gj(form).find("div.eventCategory")[0];
        if (!eventCategory) { return ; }

        var select = gj(eventCategory).find("select")[0];
        gj(select).on('change', _module.UICalendarPortlet.filterEvents);

        this.filterSelect = select;
    };

    UICalendarPortlet.prototype.listViewDblClick = function(form) {
        form = (typeof(form) == "string")? _module.UICalendarPortlet.getElementById(form):form ;
        if(!form) return ;
        var tr = gj(form).find("tr.uiListViewRow");
        var i = tr.length ;
        _module.UICalendarPortlet.viewType = "UIListView";
        var chk = null ;
        while(i--){
            tr[i].ondblclick = this.listViewDblClickCallback;
        }
    };

    UICalendarPortlet.prototype.doClick = function() {
        if(_module.UICalendarPortlet.dblDone){
            delete _module.UICalendarPortlet.dblDone;
            window.clearTimeout(_module.UICalendarPortlet.clickone);
            return ;
        }
        gj.globalEval(_module.UICalendarPortlet.listViewAction);
    };

    UICalendarPortlet.prototype.listViewClickCallback = function(obj) {
        this.listViewAction = obj.getAttribute("actionLink");
        this.clickone = setTimeout(this.doClick,200);
        return false ;
    };

    UICalendarPortlet.prototype.ondblclickCallbackInListView = function(obj) {
        var eventId = obj.getAttribute("eventid");
        var calendarId = obj.getAttribute("calid");
        var calendarType = obj.getAttribute("caltype");
        var recurid = obj.getAttribute("recurid");
        var isoccur = obj.getAttribute('isoccur');
        uiForm.submitEvent(_module.UICalendarPortlet.portletId+'#' + _module.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + _module.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType + '&isOccur=' + isoccur + '&recurId='+recurid);
    };

    UICalendarPortlet.prototype.listViewDblClickCallback = function() {
        _module.UICalendarPortlet.dblDone = true;
        _module.UICalendarPortlet.ondblclickCallbackInListView(this);
    };


    /**
     * Filter event when page load
     *
     * invoked from template
     * @see UIWeekView.gtmpl
     */
    UICalendarPortlet.prototype.checkFilter = function() {
        var UICalendarPortlet = _module.UICalendarPortlet;

        if (UICalendarPortlet.selectedCategory) {
            for ( var i = 0; i < UICalendarPortlet.filterSelect.options.length; i++) {
                if (UICalendarPortlet.filterSelect.options[i].value == UICalendarPortlet.selectedCategory) {
                    UICalendarPortlet.filterSelect.options[i].selected = true;
                }
            }
        }

        _module.UICalendarPortlet.filterEvents();
        UICalendarPortlet.setFocus();
        if (_module.UICalendarPortlet.firstLoadTimeout) {
            delete _module.UICalendarPortlet.firstLoadTimeout;
        }
    };

    /**
     * Get filters for calendar
     *
     * @deprecated
     */
    UICalendarPortlet.prototype.checkCalendarFilter = function() {
        if (!this.filterForm) { return ; }
        var checkboxes = gj(this.filterForm).find("input.checkbox"),
            len        = checkboxes.length;
        for (var i = 0; i < len; i++) {
            this.runFilterByCalendar(checkboxes[i].name, checkboxes[i].checked);
        }
        this.runFilterByCategory();
    };

    /**
     * Filter event by event category when page load
     * @deprecated not used anymore
     */
    UICalendarPortlet.prototype.checkCategoryFilter = function() {
        if (this.filterSelect)
            _module.UICalendarPortlet.runFilterByCategory();
    };

    /**
     * Shows view menu
     * @param {Object} obj DOM element
     * @param {Object} evt Mouse event
     */
    UICalendarPortlet.prototype.showView = function(obj, evt) {
        evt.stopPropagation();
        var oldmenu = gj(obj).find('div.uiRightClickPopupMenu')[0];
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
    UICalendarPortlet.prototype.swapIeMenu = function(menu, clickobj) {
        var Browser = base.Browser;
        var x = Browser.findPosXInContainer(clickobj, menu.offsetParent) - CSUtils.getScrollLeft(clickobj);
        var y = Browser.findPosYInContainer(clickobj, menu.offsetParent) - CSUtils.getScrollTop(clickobj) + clickobj.offsetHeight;
        var browserHeight = document.documentElement.clientHeight;
        var uiRightClickPopupMenu = (!gj(menu).hasClass("uiRightClickPopupMenu")) ? gj(menu).find('div.uiRightClickPopupMenu')[0] : menu;
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
    UICalendarPortlet.prototype.swapMenu = function(oldmenu, clickobj) {
        if (document.getElementById("tmpMenuElement"))
            gj("#tmpMenuElement").remove();
        var tmpMenuElement = gj(oldmenu).clone(true,true);
        tmpMenuElement.attr("id","tmpMenuElement");
        var style = tmpMenuElement.attr("style") + "zIndex = 1;";

        tmpMenuElement.attr("style",style) ;

        gj('body').append(tmpMenuElement);

        this.menuElement = gj("#tmpMenuElement")[0];

        gj(this.menuElement).addClass("UICalendarPortlet UIEmpty");

        var menuTop = gj(clickobj).offset().top - 40;

        var d = menuTop + gj(this.menuElement).height() - gj(document).scrollTop() - gj(window).height();
        if(d > 0) {
            menuTop -= d;
        }

        var menuLeft = gj(clickobj).offset().left + gj(clickobj).width();
        gj(this.menuElement).css('left', menuLeft + 'px');
        gj(this.menuElement).css('top', menuTop + 'px');

        this.showHide(this.menuElement);

    };

    UICalendarPortlet.prototype.initDetailTab = function(form,selecedCalendarID) {
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

            var UIComboboxInputs = gj(form).find("input.UIComboboxInput");
            for(var i = 0; i < UIComboboxInputs.length; i++) {
                gj(UIComboboxInputs[i]).live('change', function() {
                    _module.ScheduleSupport.syncTimeBetweenEventTabs();
                    _module.ScheduleSupport.applyPeriod();
                });
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
    UICalendarPortlet.prototype.showHideTime = function(chk) {
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

        var dateAll = gj('#dateAll')[0];
        if(dateAll) {
            dateAll.checked = chk.checked;
            var timeField = gj(dateAll.form).find('div.timeField')[0];
            if (dateAll.checked) {
                timeField.style.display = "none";
            }
            else {
                timeField.style.display = "block";
            }
        }
        _module.ScheduleSupport.applyPeriod();
    };

    /**
     * Show/hide field in form
     * @param {Object} chk Checkbox element
     * @param {Object} fields Input field in form
     */
    UICalendarPortlet.prototype.showHideField = function(chk, fields) {
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

    UICalendarPortlet.prototype.showHideRepeat = function(chk) {
        var checkbox = gj(chk).find('input.checkbox')[0];
        var fieldCom =gj(chk).parents(".fieldComponent")[0];
        var repeatField = gj(fieldCom).find('div.repeatInterval')[0];
        if (checkbox.checked) {
            repeatField.style.display = "block";
        } else {
            repeatField.style.display = "none";
        }
    };

    UICalendarPortlet.prototype.autoShowRepeatEvent = function() {
        var divEmailObject = document.getElementById("IsEmailRepeatEventReminderTab");
        var checkboxEmail = gj(divEmailObject).find('input.checkbox')[0];
        var fieldComEmail = gj(divEmailObject).parents(".fieldComponent")[0];
        var repeatFieldEmail = gj(fieldComEmail).find('div.repeatInterval')[0];
        if (checkboxEmail.checked) {
            repeatFieldEmail.style.display = "block";
        } else {
            repeatFieldEmail.style.display = "none";
        }

        var divObjectPopup = document.getElementById("IsPopupRepeatEventReminderTab");
        var checkboxPopup = gj(divObjectPopup).find('input.checkbox')[0];
        var fieldComPopup = gj(divObjectPopup).parents(".fieldComponent")[0];
        var repeatFieldPopup = gj(fieldComPopup).find('div.repeatInterval')[0];
        if (checkboxPopup.checked) {
            repeatFieldPopup.style.display = "block";
        } else {
            repeatFieldPopup.style.display = "none";
        }
    };

    /**
     * Sets up dragging selection for calendar view
     */
    UICalendarPortlet.prototype.initSelection = function() {
        var UICalendarPortlet = _module.UICalendarPortlet;
        var container = gj(_module.UICalendarPortlet.getElementById("UIDayViewGrid")).find('div.eventBoard')[0];
        UISelection.step = UICalendarPortlet.CELL_HEIGHT;
        UISelection.container = container;
        UISelection.block = document.createElement("div");
        UISelection.block.className = "userSelectionBlock";
        UISelection.container.appendChild(UISelection.block);
        gj(UISelection.container).on('mousedown',UISelection.start);
        UISelection.relativeObject = gj(UISelection.container).parents(".eventDayContainer")[0];
        UICalendarPortlet.viewType = "UIDayView";
    };

    /**
     * Checks free/busy in day of an user
     * @param {Object} chk Checkbox element
     */
    UICalendarPortlet.prototype.checkAllInBusy = function(chk) {
        var UICalendarPortlet = _module.UICalendarPortlet;
        var isChecked = chk.checked;
        var timeField = gj(chk.form).find('div.timeField')[0];
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
     * Init scripts for Schedule tab
     */
    UICalendarPortlet.prototype.initCheck = function(container, userSettingTimezone) {
        if (typeof(container) == "string")
            container = document.getElementById(container);
        var dateAll = gj(container).find("input.checkbox")[1];
        var table = gj(container).find('table.uiGrid')[0];
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
            _module.ScheduleSupport.applyPeriod();
        });
        var UIComboboxInputs = gj(container).find("input.UIComboboxInput");
        for(var i = 0; i < UIComboboxInputs.length; i++) {
            gj(UIComboboxInputs[i]).live('change', function() {
                _module.ScheduleSupport.applyPeriod();
                _module.ScheduleSupport.syncTimeBetweenEventTabs();
            });
        }
        _module.UICalendarPortlet.initSelectionX(firstTr);
    };

    /**
     * Shows free/busy on UI
     * @param {Object} tr Tr tag contains event data
     * @param {Object} serverTimezone Server timezone
     */
    UICalendarPortlet.prototype.showBusyTime = function(tr, userSettingTimezoneOffset) {
        var stringTime = tr.getAttribute("busytime");
        var browserTimezone = (new Date).getTimezoneOffset();
        var extraTime = browserTimezone - userSettingTimezoneOffset;
        if (!stringTime)
            return;
        var time = dateUtils.parseTime(stringTime, extraTime);
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
    UICalendarPortlet.prototype.setBusyTime = function(from, to, tr) {
        var cell = gj(tr).find("td").slice(1);
        var start = CSUtils.ceil(from, 15) / 15;
        var end = CSUtils.ceil(to, 15) / 15;
        for (var i = start; i < end; i++) {
            cell[i].className = "busyDotTime";
            gj(this.busyCell[i]).addClass("busyTime");
        }
    };

    /**
     * Sets up dragging selection for free/busy time table
     * @param {Object} tr Tr tag contains event data
     */
    UICalendarPortlet.prototype.initSelectionX = function(tr) {
        cell = gj(tr).find("td.uiCellBlock");
        var len = cell.length;
        for (var i = 0; i < len; i++) {
            gj(cell[i]).on('mousedown', UIHSelection.start);
        }
    };

    /**
     * Gets AM/PM from input value
     * @param {Object} input Input contains time
     * @return Object contains two properties that are AM and PM
     */
    UICalendarPortlet.prototype.getTimeFormat = function(input) {
        return {
            "am": gj("#AMString")[0].getAttribute("name"),
            "pm": gj("#PMString")[0].getAttribute("name")
        };
    };

    /**
     * Callback method when dragging selection end
     */
    UICalendarPortlet.prototype.callbackSelectionX = function() {
        var len = Math.abs(UIHSelection.firstCell.cellIndex - UIHSelection.lastCell.cellIndex - 1);
        var start = (UIHSelection.firstCell.cellIndex - 1) * 15;
        var end = start + len * 15;
        var timeTable = gj(UIHSelection.firstCell).parents("table")[0];
        var dateValue = timeTable.getAttribute("datevalue");
        var uiTabContentContainer = gj('#eventAttender-tab')[0];
        var UIComboboxInputs = gj(uiTabContentContainer).find("input.UIComboboxInput");
        len = UIComboboxInputs.length;
        var name = null;
        var timeFormat = this.getTimeFormat(null);
        start = dateUtils.minToTime(start, timeFormat);
        end = dateUtils.minToTime(end, timeFormat);
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
        var cells = gj(UIHSelection.firstCell.parentNode).children("td");
        UIHSelection.setAttr(UIHSelection.firstCell.cellIndex, UIHSelection.lastCell.cellIndex, cells);
        _module.ScheduleSupport.syncTimeBetweenEventTabs();
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
    UICalendarPortlet.prototype.initSettingTab = function(cpid) {
        var cp = _module.UICalendarPortlet.getElementById(cpid);
        var ck = gj(cp).find('input.checkbox')[0];
        var div = gj(ck).parents("div")[0];
        _module.UICalendarPortlet.workingSetting = gj(div).nextAll("div")[0];
        gj(ck).on('click',_module.UICalendarPortlet.showHideWorkingSetting);
        _module.UICalendarPortlet.checkWorkingSetting(ck);
    }

    /**
     * Check status of working time checkbox
     * @param {Object} ck Working time checkbox
     */
    UICalendarPortlet.prototype.checkWorkingSetting = function(ck) {
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
    UICalendarPortlet.prototype.showHideWorkingSetting = function() {
        var isCheck = this.checked;
        if (isCheck) {
            _module.UICalendarPortlet.workingSetting.style.visibility = "visible";
        }
        else {
            _module.UICalendarPortlet.workingSetting.style.visibility = "hidden";
        }
    };

    UICalendarPortlet.prototype.showImagePreview = function(obj) {
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

    UICalendarPortlet.prototype.showHideSetting = function(obj) {
        var checkbox = gj(obj).find('input.checkbox')[0];
        var uiFormGrid = gj(obj.parentNode.parentNode).find('table.uiFormGrid')[0];
        if(checkbox.checked) {
            checkbox.checked = true;
            uiFormGrid.style.display = "";
        }
        else{
            checkbox.checked = false;
            uiFormGrid.style.display = "none";
        }
    };

    UICalendarPortlet.prototype.autoShowHideSetting = function() {
        var eventReminder = document.getElementById("eventReminder");
        var checkboxEmail = gj(eventReminder).find('input.checkbox')[0];
        var uiFormGrid = gj(eventReminder).find('table.uiFormGrid') [0];
        if(checkboxEmail.checked) {
            uiFormGrid.style.display = "";
        }
        else{
            uiFormGrid.style.display = "none";
        }
        var popupReminder = gj(eventReminder).find('div.reminderByPopup') [0];
        var checkboxPopup = gj(popupReminder).find('input.checkbox')[0];
        var uiFormGridPopup = gj(popupReminder).find('table.uiFormGrid')[0];
        if(checkboxPopup.checked) {
            uiFormGridPopup.style.display = "";
        }
        else{
            uiFormGridPopup.style.display = "none";
        }
    };

    UICalendarPortlet.prototype.removeEmailReminder = function(obj) {
        var uiEmailAddressLabel = gj(obj).parent().prev()[0];
        var uiEmailInput = gj(obj).parents(".uiEmailInput")[0];
        uiEmailInput = gj(uiEmailInput).children("input")[0];
        uiEmailAddressLabel = uiEmailAddressLabel.innerHTML.toString().trim();
        uiEmailInput.value = this.removeItem(uiEmailInput.value,uiEmailAddressLabel);

        if(gj(obj).parents(".UIEventForm")[0]) {
            uiForm.submitForm('UIEventForm','RemoveEmail', true);
        } else if(_module.UICalendarPortlet.getElementById("UITaskForm")) {
            uiForm.submitForm('UITaskForm','RemoveEmail', true);
        }

        gj(obj).parent().prev().remove();
        gj(obj).remove();

    }

    UICalendarPortlet.prototype.removeItem = function(str,removeValue) {
        if(str.indexOf(",") <= 0) return "";
        var list = str.split(",");
        var index = list.indexOf(removeValue);
        list.splice(index, 1);
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
    
    /**
     * Callback method when double click on a calendar event
     */
    UICalendarPortlet.prototype.ondblclickCallback = function(evt) {
      evt.stopPropagation();
      var UICalendarPortlet = _module.UICalendarPortlet;
      var eventId = this.getAttribute("eventId");
      var calendarId = this.getAttribute("calid");
      var calendarType = this.getAttribute("caltype");
      var isOccur = this.getAttribute("isOccur");
      var recurId = this.getAttribute("recurId");
      if (recurId == "null") recurId = "";
      uiForm.submitEvent(UICalendarPortlet.portletId + '#' + UICalendarPortlet.viewType, 
          'Edit', '&subComponentId=' + UICalendarPortlet.viewType + '&objectId=' + eventId + 
          '&calendarId=' + calendarId + '&calType=' + calendarType + '&isOccur=' + isOccur + '&recurId=' + recurId);
    }
    
    /**
     * Sets size for a DOM element that includes height and top properties
     * @param {Object} obj Calendar event element
     */
    UICalendarPortlet.prototype.setSize = function(obj) {
      var UICalendarPortlet = _module.UICalendarPortlet;
      var start = parseInt(obj.getAttribute("startTime"));
      var topY = UICalendarPortlet.minsToPixels(start);
      var end = parseInt(obj.getAttribute("endTime"));
      var eventContainer = gj(obj).find('div.eventContainer')[0];
      if (end == 0)
        end = 1440;
      end = (end != 0) ? end : 1440;
      height = UICalendarPortlet.minsToPixels(Math.abs(start - end));
      if (height <= UICalendarPortlet.CELL_HEIGHT) {
        height = UICalendarPortlet.CELL_HEIGHT;
      }
      var styles = {
          "top": topY + "px",
          "height": height + "px"
      };
      gj(obj).css(styles);
      var busyIcon = gj(obj).children("div")[0] ;
      if(!busyIcon ||  (busyIcon.offsetHeight <= 5))
        busyIcon = gj(obj).find("div.eventContainerBar")[0] ;
      var extraHeight = busyIcon.offsetHeight + gj(obj).find("div.resizeEventContainer")[0].offsetHeight;
      height -= (extraHeight + 5);
      
      // IE8 fix - does not allow negative height
      if (height > 0) {
        eventContainer.style.height = height + "px";
      }
    }

    UICalendarPortlet.prototype.isAllday = function(eventObject) {
        var startDate = new Date(parseInt(eventObject.startDateTime) + parseInt(eventObject.startTimeOffset));
        var endDate = new Date(parseInt(eventObject.endDateTime) + parseInt(eventObject.endTimeOffset));
        var delta = eventObject.endDateTime - eventObject.startDateTime;
        if(startDate.getUTCDate() == endDate.getUTCDate() && startDate.getUTCMonth() == endDate.getUTCMonth()) {
            return delta == (24*60 - 1)*60*1000 ? 1 : 2;
        } else {
            return 3;
        }
    };

    UICalendarPortlet.prototype.getFormattedHour = function(date) {
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
    };

    UICalendarPortlet.prototype.getDateString = function(date) {
        var dateString = "";
        var type = _module.UICalendarPortlet.isAllday(date);
        var dayNameIndex = (date.getUTCDay() - parseInt(_module.UICalendarPortlet.weekStartOn) + 1) % 7; //CAL-575
        var dayName = gj(".ShortDayName").get(dayNameIndex).getAttribute("name");
        var monthName = gj(".LocalizedMonthName").get(date.getUTCMonth()).getAttribute("name");
        var dateInMonth = date.getUTCDate() < 10 ? '0' + date.getUTCDate() : date.getUTCDate();

        return dayName + ", " + monthName + " " + dateInMonth;
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
        var repeatCheck = gj('input[name="isRepeat"]')[0];
        var editButton = gj('.checkBoxArea').find('a')[0];
        if(repeatCheck) {
            var summary = gj('.repeatSummary')[0];
            if (repeatCheck.checked) {
                editButton.style.display="inline-block";
            } else {
                editButton.style.display="none";
            }

            gj(repeatCheck).on('click', function() {
                if (repeatCheck.checked) {
                    repeatCheck.checked = false;
                    uiForm.submitForm(portletFragment.parentNode.id + '#UIEventForm','EditRepeat', true);
                } else {
                    summary.innerHTML = "";
                    editButton.style.display = "none";
                }
            });

        }

    }

    UICalendarPortlet.prototype.changeRepeatType = function(id) {
        var weeklyByDayClass = "weeklyByDay";
        var monthlyTypeClass = "monthlyType";
        var RP_END_AFTER = "endAfter";
        var RP_END_NEVER = "neverEnd";
        var RP_END_BYDATE = "endByDate";

        var repeatingEventForm = _module.UICalendarPortlet.getElementById(id);
        var weeklyByDay = gj(repeatingEventForm).find('div.' + weeklyByDayClass)[0];
        var monthlyType = gj(repeatingEventForm).find('div.' + monthlyTypeClass)[0];
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

    UICalendarPortlet.prototype.loadTitle = function(id) {
        try{
            gj(document).ready(
                function(){
                    if(id) {
                        gj("#"+id).find("*[rel=tooltip]").tooltip();
                    }
                    else {
                        gj("*[rel=tooltip]").tooltip();
                    }
                }
            );
        } catch (e) {
        }
    };

    UICalendarPortlet.prototype.overidePopUpClose = function() {
        gj('.UICalendarPortlet .uiIconClose').attr('onclick','');
        gj('.UICalendarPortlet .uiIconClose').click(function(){gj(this).parents()[1].style.display = 'none';});
    }

    UICalendarPortlet.prototype.checkEventCategoryName = function(textFieldId) {
        var txtField = gj("input#"+textFieldId);
        var val = txtField.attr("value");
        var btn = gj("button#btnEventCategoryFormContainer");
        if (val == null || val == "") {
            btn.attr("disabled", "disabled");
        } else {
            btn.removeAttr("disabled");
        }
    }

//CAL-516: show last selected category after creating a new event
    UICalendarPortlet.prototype.showLastSelectedCategory = function() {
        if(_module.lastSelectedCategory) {
            selectBox = gj(_module.UICalendarPortlet.filterSelect);
            selectBox.val(_module.lastSelectedCategory);
            //re-filter
            if(gj('#UIListContainer').size() > 0) {//list view
                uiForm.submitEvent(_module.UICalendarPortlet.portletId +'#UIListView','Onchange','&objectId=eventCategories');
            }
        }
    }

    UICalendarPortlet.prototype.loadMenu = function() {
        try {
            var uiActionBar = gj('#UIActionBar');
            var label = uiActionBar.attr("morelabel");
            uiActionBar.loadMoreItem({
                loadMoreLabel : label,
                liMoreClass : 'btn moreItem',
                moreIsActionIcon : false,
                processContainerWidth : function() {
                    var pr = gj('#UIActionBar');
                    var btnRight = pr.find('.btnRight:first');
                    var btnLeft = pr.find('.btnLeft:first');
                    var w = pr.width() - btnRight.outerWidth() - btnLeft.outerWidth() - 20;
                    return w;
                }
            });

        } catch(e) {

        }
    };


    UICalendarPortlet.prototype.dateSuggestion = function(isNew, compid, timeShift) {
        var form = gj("#"+compid);
        var eFromDate = form.find('input[name="from"]');
        var eFromTime = form.find('input[name="fromTime"]');
        var eToDate = form.find('input[name="to"]');
        var eToTime = form.find('input[name="toTime"]');
        var values = gj(eFromTime).next("input.UIComboboxInput").attr("options");
        var arr = eval(values);
        if(isNew == "false") this.dayDiff = dateUtils.dateDiff(new Date(eFromDate.val()).getTime(), new Date(eToDate.val()).getTime());
        if(compid == "UIEventForm") {        	
            var fromIndex = this.getTimeIndex(eFromTime.val());
            var toIndex = this.getTimeIndex(eToTime.val(), true);
            
            this.timeShiftE = toIndex - fromIndex ;
        } else if(compid == "UITaskForm") {
            var fromIndex = arr.indexOf(eFromTime.val());
            var toIndex = arr.indexOf(eToTime.val()) ;
            this.timeShiftT = toIndex - fromIndex ;
        } else if(compid == "UIQuickAddEvent" ) {
            this.timeShiftE = parseInt(timeShift);
        } else if(compid == "UIQuickAddTask" ) {
            this.timeShiftT = parseInt(timeShift);
        }
        form.on('click','a[href="#SelectDate"]', function() {_module.UICalendarPortlet.suggestDate(eFromDate, eToDate)});
        eFromDate.on('blur', function() {_module.UICalendarPortlet.suggestDate(eFromDate, eToDate)});
        eToDate.on('blur', function() {_module.UICalendarPortlet.suggestDate(eFromDate, eToDate)});
        gj(eFromTime).prev().on('click','a.UIComboboxItem', function(){_module.UICalendarPortlet.suggestTime(compid, isNew, eFromDate, eToDate, eFromTime, eToTime, timeShift)});
        gj(eFromTime).next().on('keydown', function(event){_module.UICalendarPortlet.suggestTime(compid, isNew, eFromDate, eToDate, eFromTime, eToTime, timeShift, event)});
        gj(eToTime).prev().on('click','a.UIComboboxItem', function(){_module.UICalendarPortlet.updateShifTime(compid, isNew, eFromDate, eToDate, eFromTime, eToTime, timeShift)});
        gj(eToTime).next().on('keydown', function(event){_module.UICalendarPortlet.updateShifTime(compid, isNew, eFromDate, eToDate, eFromTime, eToTime, timeShift, event)});
    }
    
    UICalendarPortlet.prototype.getTimeIndex = function(time, roundUp) {
    	var t = time.split(":");
    	var minutes = parseInt(gj.trim(t[0])) * 60 + parseInt(gj.trim(t[1]));
    	if (roundUp) {
    		return Math.ceil(minutes/30);
    	} else {
    		return Math.floor(minutes/30);
    	}
    }

    UICalendarPortlet.prototype.suggestTime = function(compid, isNew, eFromDate, eToDate, eFromTime, eToTime, timeShift, event) {
        if(event != null) {
            if(event.keyCode !== 13) return;
        }
        var format = gj(eFromDate).attr("format");
        var values = gj(eFromTime).next("input.UIComboboxInput").attr("options");
        var arr = eval(values);
        var start = eFromTime.val();
        var size = arr.length ;
        var index = this.getTimeIndex(start);
        if(compid == "UIEventForm"){
            if((index + this.timeShiftE)>= size){
                this.addDay(eFromDate, this.dayDiff + 1, eToDate, format);
                value = arr[(index + this.timeShiftE) - (size -1)];
            } else {
                this.addDay(eFromDate, this.dayDiff, eToDate, format);
                value = arr[index+this.timeShiftE];
            }
        } else if(compid == "UITaskForm"){
            if((index + this.timeShiftT)>= size){
                this.addDay(eFromDate, this.dayDiff + 1, eToDate, format);
                value = arr[(index + this.timeShiftT) - (size -1)];
            } else {
                this.addDay(eFromDate, this.dayDiff, eToDate, format);
                value = arr[index+this.timeShiftT];
            }
        } else if(compid == "UIQuickAddEvent"){
            if((index + this.timeShiftE)>= size){
                this.addDay(eFromDate, this.dayDiff + 1, eToDate, format);
                value = arr[(index + this.timeShiftE) - (size -1)];
            } else {
                this.addDay(eFromDate, this.dayDiff, eToDate, format);
                value = arr[index+this.timeShiftE];
            }
        } else if(compid == "UIQuickAddTask") {
            if((index + this.timeShiftT)>= size){
                this.addDay(eFromDate, this.dayDiff + 1, eToDate, format);
                value = arr[(index + this.timeShiftT) - (size -1)];
            } else {
                this.addDay(eFromDate, this.dayDiff, eToDate, format);
                value = arr[index+this.timeShiftT];
            }
        }
        eToTime.val(value);
        gj(eToTime).next('input.UIComboboxInput').val(value);
        _module.ScheduleSupport.syncTimeBetweenEventTabs();
        _module.ScheduleSupport.applyPeriod();
    };

    UICalendarPortlet.prototype.updateShifTime = function(compid, isNew, eFromDate, eToDate, eFromTime, eToTime, timeShift, event) {
        if(event != null) {
            if(event.keyCode !== 13) return;
        }
        var format = gj(eFromDate).attr("format");
        var values = gj(eFromTime).next("input.UIComboboxInput").attr("options");
        var arr = eval(values);
        var start = eFromTime.val();
        var size = arr.length ;
        var indexs = this.getTimeIndex(start);
        var end = eToTime.val();
        var indexe = this.getTimeIndex(end, true);
        if(compid == "UIEventForm"){
            if((indexe - indexs) > 0) {
                this.timeShiftE = indexe - indexs;
            }
        } else if(compid == "UITaskForm"){
            if((indexe - indexs) > 0) {
                this.timeShiftT = indexe - indexs;
            }
        } else if(compid == "UIQuickAddEvent"){
            if((indexe - indexs) > 0) {
                this.timeShiftE = indexe - indexs;
            }
        } else if(compid == "UIQuickAddTask") {
            if((indexe - indexs) > 0) {
                this.timeShiftT = indexe - indexs;
            }
        }
    };

  /**
   * When user input an incorrect date, we will set current date for that field.
   * @param $eDate
   */
    UICalendarPortlet.prototype.correctDate = function($eDate) {
        $eDate = gj($eDate);
        var format = $eDate.attr("format");
        var val = $eDate.val();
        var pattern = format;
        pattern = pattern.replace('dd', '\\d{1,2}');
        pattern = pattern.replace('MM', '\\d{1,2}');
        pattern = pattern.replace('yyyy', '\\d{4}');
        var p = new RegExp('^'+pattern+'$');
        var valid = p.test(val);
        if(valid) {
            var date = dateUtils.dateParses(val, format);
            valid = !isNaN(date.getTime());
        }

        // If it is not validate set the date is current date
        if(!valid) {
            cs.CalDateTimePicker.currentDate = new Date();
            cs.CalDateTimePicker.datePattern = format;
            var value = cs.CalDateTimePicker.getDateTimeString();
            $eDate.val(value);
        }
    }

    UICalendarPortlet.prototype.suggestDate = function(eFromDate, eToDate) {
        var divCal = gj('div.uiCalendarComponent[relId="'+gj(eFromDate).attr('name')+'"]');
        if(divCal.length > 0){
            var format = gj(eFromDate).attr("format");
            this.correctDate(eFromDate);
            this.addDay(eFromDate,this.dayDiff, eToDate, format);
        }
        var endDivCal = gj('div.uiCalendarComponent[relId="'+gj(eToDate).attr('name')+'"]');
        if (endDivCal.length > 0) {
            this.correctDate(eToDate);
            var dayDiff = dateUtils.dateDiff(new Date(eFromDate.val()).getTime(), new Date(eToDate.val()).getTime());
            if(dayDiff >= 0) this.dayDiff = dayDiff;
        };
    };

    UICalendarPortlet.prototype.addDay = function(eFromDate, dayNum, eToDate, datePattern) {
        var dateValue = eFromDate.val();
        CalDateTimePicker.currentDate = dateUtils.dateParses(dateValue, datePattern);
        CalDateTimePicker.currentDate.setDate(CalDateTimePicker.currentDate.getDate()+dayNum);
        CalDateTimePicker.datePattern = datePattern;
        var value = CalDateTimePicker.getDateTimeString();
        eToDate.val(value);
    }
    
    /**
     * Resize content container to stop at the bottom of the page
     * @param {Object} contentContainer DOM element to be resized
     * @param {int}    deltaHeight      additional height to add
     * @param {int}    originalHeight   original height of content container
     */
    UICalendarPortlet.prototype.resizeHeight = function(contentContainer, deltaHeight, originalHeight) {
      var viewPortHeight    = gj(window).height(),
      positionYofContentContainer = gj(contentContainer).offset().top,
      height,
      totalYofContainer = gj(contentContainer).offset().top + contentContainer.offsetHeight,
      originalTotalY    = gj(contentContainer).offset().top + originalHeight;
      var actionBarHeight = 70 ;
      if(gj("#LeftNavigation") != null && gj("#LeftNavigation").offset() != null) {
        var leftNavigationY   = gj("#LeftNavigation").height() + gj("#LeftNavigation").offset().top,
        maxHeight         = (leftNavigationY > viewPortHeight) ? leftNavigationY : viewPortHeight;
        if (maxHeight > originalTotalY) {
          gj(contentContainer).css("height", originalHeight);
        }
        else {
          height =  maxHeight - positionYofContentContainer - deltaHeight;
          gj(contentContainer).css("height", height);
          gj(contentContainer).css("overflow", "auto");
          
          if (gj.browser.mozilla) {
            gj(contentContainer).css("overflow-x", "hidden");
            if(gj("#LeftNavigation").height() > viewPortHeight) { //CAL-541
              gj(contentContainer).css('height', height - 15);
            }
          }
        }
      } else {
        maxHeight = (gj("form#UIMiniCalendar").height() + gj("form#UICalendars").height()) - actionBarHeight;
        gj(".eventWeekContent").css("height", maxHeight);
        gj(contentContainer).css("overflow", "auto");
        if (gj.browser.mozilla) {
          gj(contentContainer).css("overflow-x", "hidden");
        }
      }
    }
    
    UICalendarPortlet.prototype.setPosition = function(eventObj) {
      this.activeEventObject = eventObj;
      var cTop = gj(eventObj).top;
      var cLeft = gj(eventObj).offset().left;
      var cWdith = gj(eventObj).width();
      var cHeight =gj(eventObj).height();
      var cTitle = gj(eventObj).find('div.eventTitle')[0].innerHTML;
      var cInnerHeight = gj(eventObj).find('div.eventContainer').height();
      this.restoreTitle = cTitle;
      this.restoreContainerHeight = cInnerHeight;
      this.restoreSize = {
          "top": cTop,
          "left": cLeft,
          "width": cWdith,
          "height": cHeight
      };
    }
    
    UICalendarPortlet.prototype.checkPermission = function(eventObj) {
      var calId = eventObj.getAttribute("calid");
      var calType = eventObj.getAttribute("calType");
      var baseURL  = (_module.restContext)?eXo.env.portal.context+ '/' + _module.restContext +'/cs/calendar/checkPermission/':'portal/rest/cs/calendar/checkPermission/';
      var url = baseURL + cometd.exoId +"/"+ calId +"/"+ calType +"/";
      CSUtils.makeRequest(url,this.postCheck);
    }
    
    UICalendarPortlet.prototype.restorePosition = function(eventObj) {
      gj(eventObj).css(this.restoreSize);
      var eventTitle = gj(eventObj).find('div.eventTitle')[0];
      var eventContainer = gj(eventObj).find('div.eventContainer')[0];
      if(this.restoreTitle && eventTitle) eventTitle.innerHTML = this.restoreTitle;
      if(this.restoreContainerHeight && eventContainer) eventContainer.style.height = this.restoreContainerHeight;
      this.restoreSize = null;
      this.activeEventObject = null;
      this.dropCallback = null;
      this.restoreTitle = null;
    }
    
    UICalendarPortlet.prototype.postCheck = function(response) {
      var UICalendarPortlet = _module.UICalendarPortlet;
      gj.globalEval("var data = " + response.responseText);
      var isEdit = data.permission;
      if(!isEdit){
        UICalendarPortlet.notify(UICalendarPortlet.activeEventObject);
        UICalendarPortlet.restorePosition(UICalendarPortlet.activeEventObject);
      } else{
        if(UICalendarPortlet.dropCallback) UICalendarPortlet.dropCallback();
        delete UICalendarPortlet.activeEventObject ;
        delete UICalendarPortlet.restoreSize;
      }
    };

    UICalendarPortlet.prototype.addHour = function(input, interval) {
        var hourStr =  input.split(':')[0];
        var hour;
        if(hourStr[0] == '0')
            hour = parseInt(hourStr[1]);
        else
            hour = parseInt(hourStr);
        if(hour >= 23) {
            return "23:59";
        }
        hour += interval;
        if(hour < 10) hour = "0" + hour;
        return  hour+':'+input.split(':')[1];
    }

    UICalendarPortlet.prototype.confirmOption = function(compid){
        var list = gj('#'+compid).find('div.confirmRadio').find('div.actionLinks').find('a') ;
        var btn = gj('#'+compid).find('div.uiAction').find('button')[0];
        gj('#'+compid).find('div.confirmRadio').find('input.radio').off('click').on('click', function(){
            if(this.value === 'save_one') {
                gj(btn).attr('onclick',gj(list[0]).attr('href'));
            } else if (this.value === 'save_follow') {
                gj(btn).attr('onclick',gj(list[1]).attr('href'));
            } else if(this.value === 'save_all') {
                gj(btn).attr('onclick',gj(list[2]).attr('href'));
            }
        });
    }

//CAL-626 : autofocus the first input
//because 'autofocus' attribute is not supported in IE9, we must use js to do this
    UICalendarPortlet.prototype.autoFocusFirstInput = function(formId) {
        inputs = gj('#'+formId).find('input[type="text"]');
        textareas = gj('#'+formId).find('textarea');
        //get the first input
        input = (inputs.length > 0 ) ? inputs.eq(0) : textareas.eq(0); //input text has higher priority than text area

        if(input.length > 0) {
            input.focus();//focus
            tmp = input.val();
            input.val('');
            input.val(tmp); //move the cursor to the end
        }
    }

    UICalendarPortlet.prototype.confirmOption = function(compid) {
        var list = gj('#'+compid).find('div.confirmRadio').find('div.actionLinks').find('a') ;
        var btn = gj('#'+compid).find('div.uiAction').find('button')[0];
        gj('#'+compid).find('div.confirmRadio').find('input.radio').off('click').on('click', function(){
            if(this.value === 'save_one') {
                gj(btn).attr('onclick',gj(list[0]).attr('href'));
            } else if (this.value === 'save_follow') {
                gj(btn).attr('onclick',gj(list[1]).attr('href'));
            } else if(this.value === 'save_all') {
                gj(btn).attr('onclick',gj(list[2]).attr('href'));
            }
        });
    }
    UICalendarPortlet.prototype.resizeSubscribeForm = function(formId) {
        gj('.' + formId + ' .control-label').css('width', '15%');
        gj('.' + formId + ' .controls').css('margin-left', '20%');
    }

    UICalendarPortlet.prototype.toggleEventPreview = function(arrowObj) {
        var arrowIcon       = gj(arrowObj).children('i')[0]
            , collapsePreview = (arrowIcon.className.indexOf('uiIconArrowUp') != -1)
            , uiPreview       = gj(arrowObj).parents('form#UIPreview')[0]
            , uiListView      = gj(uiPreview).siblings('form#UIListView')[0]
            , workingPanel    = gj(uiListView).children('.mainWorkingPanel')[0];

        if (collapsePreview) {
            gj(workingPanel).hide();
            gj(arrowObj).attr('data-original-title', gj(arrowObj).attr('viewTitle'));
        }
        else {
            gj(workingPanel).show();
            gj(arrowObj).attr('data-original-title', gj(arrowObj).attr('hideTitle'));
        }
        gj(arrowIcon).toggleClass('uiIconArrowUp uiIconArrowDown');
    }

    _module.ScheduleSupport = ScheduleSupport;
    _module.LayoutManager = CalendarLayout.LayoutManager;
    _module.CalendarLayout = CalendarLayout.CalendarLayout;
    _module.UIWeekView = UIWeekView ;
    _module.UICalendarMan = UICalendarMan;
    _module.UICalendarPortlet = new UICalendarPortlet();
    eXo.calendar.UICalendarPortlet = _module.UICalendarPortlet;
    var uiPopup = uiPopupWindow ;
    return _module;
})(base, CalendarLayout, UIWeekView, UICalendarMan, gj, Reminder, UICalendars, uiForm, 
		uiPopupWindow, ScheduleSupport, CSUtils, DOMUtil, UIContextMenu, CalDateTimePicker, DateTimeFormatter, UIHSelection, UISelection, UIDayView, DateUtils, cometd);
