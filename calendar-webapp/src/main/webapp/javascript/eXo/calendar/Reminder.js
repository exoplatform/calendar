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
}

//@since CS-5722 popup notification for calendar sharing and unsharing job 
Reminder.prototype.notifyShareCalendar = function(eventObj){
    var message = eventObj.data;
    var popup = gj('#shareCalendarNotification');
    popup.find('.text').text(message);
    popup.css('display','block');
} ;

Reminder.prototype.alarm = function(eventObj){
    var event = gj.parseJSON(eventObj.data);
    var popupReminder = gj('#popupReminder');
    popupReminder.find('.title').text(event.summary);
    var fromTime = new Date(event.fromDateTime);
    var toTime = new Date(event.toDateTime);
    popupReminder.find('p.fromTime').text(fromTime.toDateString());
    popupReminder.find('p.toTime').text(toTime.toDateString());
    popupReminder.find('p.location').text(event.location);
    popupReminder.find('p.description').text(event.description);
    popupReminder.css('display','block');
} ;

eXo.calendar.Reminder = new Reminder() ;

_module.Reminder = eXo.calendar.Reminder;
return _module;
})(cs, gj,cometd);