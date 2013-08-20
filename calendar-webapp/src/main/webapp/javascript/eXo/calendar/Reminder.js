(function(cs, gj, cometd){
	function Reminder() {} ;

	_module = {};
	eXo.calendar = {};

	Reminder.prototype.init = function(eXoUser, eXoToken, cometdContextName){
		if(!eXo.cs.CSCometd) eXo.cs.CSCometd = cometd;
		eXo.cs.CSCometd.exoId = eXoUser;
		eXo.cs.CSCometd.exoToken = eXoToken;
		if(cometdContextName)
			eXo.cs.CSCometd.url = '/' + cometdContextName + '/cometd';
		eXo.cs.CSCometd.subscribe('/eXo/Application/Calendar/messages', function(eventObj) {		
			eXo.calendar.Reminder.alarm(eventObj) ;
		});
		eXo.cs.CSCometd.subscribe('/eXo/Application/Calendar/notifyShareCalendar',
				function(eventObj) {
			eXo.calendar.Reminder.notifyShareCalendar(eventObj);
		});
		eXo.cs.CSCometd.subscribe('/eXo/Application/Calendar/notifyImportCalendar', function(eventObj){
			eXo.calendar.Reminder.notifyImportCalendar(eventObj);
		});
		if (!eXo.cs.CSCometd.isConnected()) {
			eXo.cs.CSCometd.init();
		}
	} ;

	Reminder.prototype.initCometd = function() {
		eXo.cs.CSCometd.subscribe('/eXo/Application/Calendar/messages', function(eventObj) {		
			eXo.calendar.Reminder.alarm(eventObj) ;
		});
		eXo.cs.CSCometd.subscribe('/eXo/Application/Calendar/notifyShareCalendar',
				function(eventObj) {
			eXo.calendar.Reminder.notifyShareCalendar(eventObj);
		});
		eXo.cs.CSCometd.subscribe('/eXo/Application/Calendar/notifyImportCalendar', function(eventObj){
			eXo.calendar.Reminder.notifyImportCalendar(eventObj);
		});
	}

	Reminder.prototype.notifyImportCalendar = function(eventObj) {
		var data = eventObj.data;
		var infos = data.split(':');
		var type =infos[0];
		var calendarName = infos[1];

		var popup = gj('#importCalendarNotification');
		var label = popup.find('.resourceBundle ').find('.' + type).text();;
		label = label.replace("{0}","<strong>" + calendarName + "</strong>");
		popup.find('.notificationBox').html(label);
		popup.css('display','block');
		setTimeout(function(){popup.css('display','none')},5000);
		if(type == "finishImport") {
			window.location.reload();
		}
	};


    //@since CS-5722 popup notification for calendar sharing and unsharing job 
	Reminder.prototype.notifyShareCalendar = function(eventObj){
		var data = eventObj.data;
		var popup = gj('#shareCalendarNotification');
		var params = data.split(',');
		var type = params[0]; // startShare || startUnShare || finishShare || finishUnShare
		var calendarName = params[1];
		var groups = "";
		//get string for shared groups
		for(var i = 2; i < params.length; i++) {
			if(i == params.length - 1) groups += params[i];
			else groups += params[i] + ',';
		}
		var label = popup.find('.resourceBundle').find('.' + type).text();
		label = label.replace("{0}","<strong>" + calendarName + "</strong>");
		label += "<br/>" + groups;
		popup.find('.notificationBox').html(label);
		popup.css('display','block');
		setTimeout(function(){popup.css('display','none')},5000);
	} ;
    //display popup reminder
	Reminder.prototype.alarm = function(eventObj){
		var eventId = eventObj.data;
		var popupReminder = gj('#popupReminder');
		var url = eXo.env.portal.context + "/" + gj("#restContext").attr("value") + "/cs/calendar/geteventbyid/" + eventId;
		gj.getJSON(url, function(data) {
			popupReminder.find('.title').html(data.summary);
			popupReminder.find('p.time').html(_module.Reminder.getTimeString(data));
			if(data.location) {
				popupReminder.find('p.location').html(data.location);
				popupReminder.find('.uiIconCheckin').css('display','block');	
			} else {
				popupReminder.find('p.location').html('');
				popupReminder.find('.uiIconCheckin').css('display','none');	
			}
			if(data.description) popupReminder.find('p.description').html(_module.Reminder.truncateString(data.description));
			popupReminder.fadeIn().delay(5000).fadeOut('slow');
		});
	} ;
    //returns string for fromtime - endtime in form: Tue, February 19, 03:00 (PM) - 05:00 (PM)
	Reminder.prototype.getTimeString = function(data){
		_module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
		var time = "";
		var type = _module.UICalendarPortlet.isAllday(data);
		var startDate = new Date(parseInt(data.startDateTime) + parseInt(data.startTimeOffset));
		var endDate = new Date(parseInt(data.endDateTime) + parseInt(data.endTimeOffset));

		if(type == 1 ) {
			return _module.UICalendarPortlet.getDateString(startDate);
		}
		if(type == 2) {
			time = _module.UICalendarPortlet.getDateString(startDate);
			time += ', ' + _module.UICalendarPortlet.getFormattedHour(startDate) + ' - ' + _module.UICalendarPortlet.getFormattedHour(endDate);
			return time;
		}
		else {
			time = _module.UICalendarPortlet.getDateString(startDate) + ', ' + _module.UICalendarPortlet.getFormattedHour(startDate);
			time += '<br />' + _module.UICalendarPortlet.getDateString(endDate) + ', ' + _module.UICalendarPortlet.getFormattedHour(endDate);
			return time;
		}
	};

	Reminder.prototype.truncateString = function(str) {
		return str.length > 33 ? str.substring(0,33) + ' ...' : str;
	}
	eXo.calendar.Reminder = new Reminder() ;

	_module.Reminder = eXo.calendar.Reminder;
	return _module;
})(cs, gj,cometd);