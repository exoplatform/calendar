(function(wx, base, gj, uiCalendar){
var _module = {};

function UIDateTimePicker() {
  this.dateField = null ;
  this.currentDate = null ;   // Datetime value base of selectedDate for displaying calendar below
                              // if selectedDate is invalid, currentDate deals with system time;
  this.selectedDate = null ; //Datetime value of input date&time field
  this.months = ['January','February','March','April','May','June','July','August','September','October','November','December'] ;
  this.weekdays = ['S','M','T','W','T','F','S'] ;
  this.tooltip = ['Previous Year', 'Previous Month', 'Next Month', 'Next Year'];
  this.pathResource = "/csResources/javascript/eXo/cs/lang/";




this.getLang = function() {
  try {
    var lang = this.dateField.lang;
    if (this.lang == lang) 
      return;
    this.lang = lang;
    var languages = eval(ajaxAsyncGetRequest(this.pathResource + this.lang.toLowerCase() + ".js", false));
    if (!languages || (typeof(languages) != "object")) 
      return;
  

  this.months = languages[0];

  this.weekdays = languages[1];
  this.tooltip = languages[2];
  } 
  catch (e) {}
} ;

this.init = function(field, isDisplayTime) {
  this.isDisplayTime = isDisplayTime ;
  if (this.dateField) {
    this.dateField.parentNode.style.position = '' ;
  }
  this.dateField = field ;
  if (!document.getElementById(this.calendarId)) this.create();
    this.show() ;

  // fix bug for IE 6
  var cld = document.getElementById(this.calendarId);
  if(base.Browser.isIE6())  {
    var blockClnd = document.getElementById('blockCaledar') ;
    var iframe = document.getElementById(this.calendarId + 'IFrame') ;
    iframe.style.height = blockClnd.offsetHeight + "px";
  }
  field.parentNode.insertBefore(cld, field) ;
};

this.show = function() {
  _module.UIDateTimePicker.getLang() ;
  gj(document).on('mousedown.calendar', new Function('eXo.cs.UIDateTimePicker.hide()'));
  
  var str = this.dateField.getAttribute("format") ;
  str = str.replace(/d{2}/,"(\\d{1,2}\\") ;
  str = str.replace(/M{2}/,"\\d{1,2}\\") ;
  str = str.replace(/y{2,4}/,"\\d{1,4})") ;
  if(this.isDisplayTime) {
    str = str.replace(/\s+/,"\\s*") ;
    str = str.replace(/H{2}/,"(\\d{1,2}\\") ;
    str = str.replace(/m{2}/,"\\d{1,2}\\") ;
    str = str.replace(/s{2}/,"\\d{1,2})") ;    
  }
  str = "^" + str + "?$" ;
  re = new RegExp(str,'i') ;
  this.selectedDate = new Date() ;
  if (re.test(this.dateField.value)) {
    var dateParts = this.dateField.value.split(" ") ;
    var spLine = dateParts[0].match(/\W{1}/) ;
    var arr = dateParts[0].split(spLine) ;
    var type = this.getTypeFormat() ;
    var month = 0 ;
    var date = 0 ;
    switch(type) {
      case 0 :
      case 1 : 
        date = arr[0] ;
        month = arr[1] ;
      break ;
      case 2 :
      case 3 : 
        date = arr[1] ;
        month = arr[0] ;
      break ;
      default : 
        date = arr[0] ;
        month = arr[1] ;
    }
    
    this.selectedDate.setMonth(parseInt(month,10) - 1) ;
    this.selectedDate.setDate(parseInt(date,10)) ;
    this.selectedDate.setFullYear(parseInt(arr[2],10)) ;
    if (dateParts.length > 1 && dateParts[dateParts.length - 1] != "") {
      spLine = dateParts[dateParts.length - 1].match(/\W{1}/) ;
      arr = dateParts[dateParts.length - 1].split(spLine) ;
      this.selectedDate.setHours(arr[0], 10) ;
      this.selectedDate.setMinutes(arr[1], 10) ;
      this.selectedDate.setSeconds(arr[2], 10) ;
    }
  }
  
  this.currentDate = new Date(this.selectedDate.getTime()) ;
  var clndr = document.getElementById(this.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
  var x = 0 ;
  var y = this.dateField.offsetHeight ;
  with (clndr.firstChild.style) {
    display = 'block' ;
    left = x + "px" ;
    top = y + "px" ;
  }
  if(base.Browser.isIE6()){
    var ifr = gj(clndr).find('#' + this.calendarId + "IFrame")[0] ;
    ifr.style.height = (gj(ifr).nextAll("div")[0].offsetHeight - 5) + "px";
  }
  var drag = document.getElementById("blockCaledar");   
  drag.onmousedown = this.initDND ;
} ;

this.initDND = function(evt) {
  var _e = evt || window.event;
  _e.cancelBubble = true ;
  _module.DragDrop.init(null, this, this.parentNode.parentNode, evt);
} ;

this.getTypeFormat = function() {
  var dateMask = ["dd/MM/yyyy","dd-MM-yyyy","MM/dd/yyyy","MM-dd-yyyy"] ;
  var dateTimeFormat = this.dateField.getAttribute("format") ;
  var dateFormat = (this.isDisplayTime)?dateTimeFormat.split(' ')[0].trim() : dateTimeFormat ;
  var len = dateMask.length ;
  for(var i = 0 ; i < len ; i ++) {
    if (dateMask[i] == dateFormat) return i ;
  }
  return false ;
}

this.setDate = function(year, month, day) {
  if (this.dateField) {
    if (month < 10) month = "0" + month ;
    if (day < 10) day = "0" + day ;
    var dateString = this.dateField.getAttribute("format") ;
    yearString = new String(dateString.match(/y{2,4}/)) ;
    year = year.toString() ;
    if(yearString.length < 4) year = year.charAt(year.length - 2) + year.charAt(year.length - 1) ;
    dateString = dateString.replace(/d{2}/, day) ;
    dateString = dateString.replace(/M{2}/, month) ;
    dateString = dateString.replace(/y{2,4}/, year) ;
    this.currentHours = new Date().getHours() ;
    this.currentMinutes = new Date().getMinutes() ;
    this.currentSeconds = new Date().getSeconds() ;
    if (this.isDisplayTime) {
      var currentHours = (this.currentHours < 10) ? "0" + this.currentHours : this.currentHours;
      var currentMinutes = (this.currentMinutes < 10) ? "0" + this.currentMinutes : this.currentMinutes;
      var currentSeconds = (this.currentSeconds < 10) ? "0" + this.currentSeconds : this.currentSeconds;
      dateString = dateString.replace(/H{2}/, currentHours) ;
      dateString = dateString.replace(/m{2}/, currentMinutes) ;
      dateString = dateString.replace(/s{2}/, currentSeconds) ;
    } else {
      dateString = dateString.split(' ') ;
      this.dateField.value = dateString[0] ;
      this.hide() ;
      return ;
    }
    
    this.dateField.value = dateString ;
    this.hide() ;
  }
  return ;
} ;

this.renderCalendar = function() {
  var dayOfMonth = 1 ;
  var validDay = 0 ;
  var startDayOfWeek = this.getDayOfWeek(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, dayOfMonth) ;
  var daysInMonth = this.getDaysInMonth(this.currentDate.getFullYear(), this.currentDate.getMonth()) ;
  var clazz = null;
  var table = '<div id="blockCaledar"><span></span></div>' ;
  table += '<div ' + 'relId=' + gj(this.dateField).attr('name');
  table += ' class="uiCalendarComponent uiBox" onmousedown="event.cancelBubble = true">';
  table += '<h5 class="title clearfix">';
  table += '<a data-placement="right" rel="tooltip" onclick="eXo.cs.UIDateTimePicker.changeMonth(-1);" class="actionIcon pull-left" data-original-title="'+ this.tooltip[1]+ '"><i class="uiIconMiniArrowLeft uiIconLightGray"></i></a>';
  table += '<span>'+ this.months[this.currentDate.getMonth()] +', '+ this.currentDate.getFullYear() + '</span>';
  table += '<a data-placement="right" rel="tooltip" onclick="eXo.cs.UIDateTimePicker.changeMonth(1);" class="actionIcon pull-right" data-original-title="'+ this.tooltip[2]+ '"><i class="uiIconMiniArrowRight uiIconLightGray"></i></a>';
  table += '</h5>';
  
  table += '<table class="weekList">';
  table += '  <tr>';  
  table +=    '       <td><font color="red">' + this.weekdays[0] + '</font></td><td>' + this.weekdays[1] + '</td><td>' + this.weekdays[2] + '</td><td>' + this.weekdays[3] + '</td><td>' + this.weekdays[4] + '</td><td>' + this.weekdays[5] + '</td><td>' + this.weekdays[6] + '</td>' ;  
  table += '  </tr>';
  table += '</table>';
  table += '<hr>';

var _pyear, _pmonth, _pday, _nyear, _nmonth, _nday, _weekend;
    var _today = new Date();
    var tableRow='';
    if(startDayOfWeek==0) startDayOfWeek = 7;
    _pyear = (this.currentDate.getMonth() == 0) ? this.currentDate.getFullYear() - 1 : this.currentDate.getFullYear();
    _pmonth = (this.currentDate.getMonth() == 0) ? 11 : this.currentDate.getMonth() - 1;
    _pday = this.getDaysInMonth(_pyear, _pmonth) - ((startDayOfWeek + ((8 - this.firstDayOfWeek) % 7)) % 7) + 1;
    
    _nmonth = (this.currentDate.getMonth() == 11) ? 0 : this.currentDate.getMonth() + 1;
    _nyear = (this.currentDate.getMonth() == 11) ? this.currentDate.getFullYear() + 1 : this.currentDate.getFullYear();
    _nday = 1;
    
    table += '<table cellspacing="0" cellpadding="0" id="" class="weekDays">';
      for ( var week = 0; week < 6; week++) {
      tableRow += '<tr {{week'+week+'}}>';
        for ( var dayOfWeek = 0; dayOfWeek <= 6; dayOfWeek++) {
          if (week == 0
              && dayOfWeek == (startDayOfWeek + ((8 - this.firstDayOfWeek) % 7)) % 7) {
            validDay = 1;
          } else if (validDay == 1 && dayOfMonth > daysInMonth) {
            validDay = 0;
          }
          if (validDay) {
            if (dayOfMonth == this.selectedDate.getDate()
                && this.currentDate.getFullYear() == this.selectedDate
                    .getFullYear()
                && this.currentDate.getMonth() == this.selectedDate.getMonth()) {
              clazz = 'selected';
            } else {
              clazz = '';
            }
        if(_today.getDate() == dayOfMonth
                && this.currentDate.getFullYear() == _today.getFullYear()
                && this.currentDate.getMonth() == _today.getMonth()) {
        clazz = 'highLight today';
        tableRow = tableRow.replace('{{week'+week+'}}','class="currentWeek"');
        }
            tableRow = tableRow + '<td><a class="' + clazz
                + '" href="#SelectDate" onclick="eXo.cs.UIDateTimePicker.setDate('
                + this.currentDate.getFullYear() + ','
                + (this.currentDate.getMonth() + 1) + ',' + dayOfMonth + ')">'
                + dayOfMonth + '</a></td>';
            dayOfMonth++;
        _weekend = week;
          } else if(validDay == 0 && week == 0) {
            tableRow = tableRow + '<td><a href="#SelectDate" class="otherMonth" onclick="eXo.cs.UIDateTimePicker.setDate('
                + _pyear + ','
                + (_pmonth + 1) + ',' + _pday + ')">'
                + _pday + '</a></td>';
        _pday++;
          } else if(validDay == 0 && week != 0 && _weekend==week){
        tableRow = tableRow + '<td><a href="#SelectDate" class="otherMonth" onclick="eXo.cs.UIDateTimePicker.setDate('
                + _nyear + ','
                + (_nmonth + 1) + ',' + _nday + ')">'
                + _nday + '</a></td>';
        _nday++;
      }
        }
        tableRow += "</tr>";
      tableRow = tableRow.replace('{{week'+week+'}}','');
      }
      table += tableRow + '</table>';

 
  table +=    '</div>' ;
  return table ;
} ;

this.hide = function() {
  if (this.dateField) {
    document.getElementById(this.calendarId).firstChild.style.display = 'none';
    this.dateField = null;
  }
  gj(document).off('mousedown.calendar');
}

};

window.eXo.cs = window.eXo.cs || {};  
UIDateTimePicker.prototype = uiCalendar;
window.eXo.cs.UIDateTimePicker = new UIDateTimePicker();
_module.UIDateTimePicker = window.eXo.cs.UIDateTimePicker;
return _module.UIDateTimePicker;
})(wx, base, gj, uiCalendar);
