(function(gj, uiCalendar){

var UIDateTimePicker = {
  dateField : null,
  currentDate : null,   // Datetime value base of selectedDate for displaying calendar below
                              // if selectedDate is invalid, currentDate deals with system time;
  selectedDate : null, //Datetime value of input date&time field
  months : ['${months.jan}','${months.feb}','${months.mar}','${months.apr}','${months.may}','${months.jun}','${months.jul}','${months.aug}','${months.sep}','${months.oct}','${months.nov}','${months.dec}'],
  weekdays : ['${weekdays.sun}','${weekdays.mon}','${weekdays.tue}','${weekdays.wed}','${weekdays.thu}','${weekdays.fri}','${weekdays.sat}'],
  tooltip : ['${PreviousYear}', '${PreviousMonth}', '${NextMonth}', '${NextYear}'],

init : function(field, isDisplayTime) {
  UIDateTimePicker.isDisplayTime = isDisplayTime ;
  if (UIDateTimePicker.dateField) {
    UIDateTimePicker.dateField.parentNode.style.position = '' ;
  }
  UIDateTimePicker.dateField = field ;
  if (!document.getElementById(UIDateTimePicker.calendarId)) {
	  UIDateTimePicker.create();
  }
  UIDateTimePicker.show() ;

  // fix bug for IE 6
  var cld = document.getElementById(UIDateTimePicker.calendarId);  
  field.parentNode.insertBefore(cld, field) ;
},

show : function() {
  gj(document).off('mousedown.calendar').on('mousedown.calendar', new Function('eXo.cs.UIDateTimePicker.hide()'));
  
  var str = UIDateTimePicker.dateField.getAttribute("format") ;
  str = str.replace(/d{2}/,"(\\d{1,2}\\") ;
  str = str.replace(/M{2}/,"\\d{1,2}\\") ;
  str = str.replace(/y{2,4}/,"\\d{1,4})") ;
  if(UIDateTimePicker.isDisplayTime) {
    str = str.replace(/\s+/,"\\s*") ;
    str = str.replace(/H{2}/,"(\\d{1,2}\\") ;
    str = str.replace(/m{2}/,"\\d{1,2}\\") ;
    str = str.replace(/s{2}/,"\\d{1,2})") ;    
  }
  str = "^" + str + "?$" ;
  re = new RegExp(str,'i') ;
  UIDateTimePicker.selectedDate = new Date() ;
  if (re.test(UIDateTimePicker.dateField.value)) {
    var dateParts = UIDateTimePicker.dateField.value.split(" ") ;
    var spLine = dateParts[0].match(/\W{1}/) ;
    var arr = dateParts[0].split(spLine) ;
    var type = UIDateTimePicker.getTypeFormat() ;
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
    
    UIDateTimePicker.selectedDate.setMonth(parseInt(month,10) - 1) ;
    UIDateTimePicker.selectedDate.setDate(parseInt(date,10)) ;
    UIDateTimePicker.selectedDate.setFullYear(parseInt(arr[2],10)) ;
    if (dateParts.length > 1 && dateParts[dateParts.length - 1] != "") {
      spLine = dateParts[dateParts.length - 1].match(/\W{1}/) ;
      arr = dateParts[dateParts.length - 1].split(spLine) ;
      UIDateTimePicker.selectedDate.setHours(arr[0], 10) ;
      UIDateTimePicker.selectedDate.setMinutes(arr[1], 10) ;
      UIDateTimePicker.selectedDate.setSeconds(arr[2], 10) ;
    }
  }
  
  UIDateTimePicker.currentDate = new Date(UIDateTimePicker.selectedDate.getTime()) ;
  var clndr = document.getElementById(UIDateTimePicker.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = UIDateTimePicker.renderCalendar() ;
  var x = 0 ;
  var y = UIDateTimePicker.dateField.offsetHeight ;
  with (clndr.firstChild.style) {
    display = 'block' ;
    left = x + "px" ;
    top = y + "px" ;
  }
},

getTypeFormat : function() {
  var dateMask = ["dd/MM/yyyy","dd-MM-yyyy","MM/dd/yyyy","MM-dd-yyyy"] ;
  var dateTimeFormat = UIDateTimePicker.dateField.getAttribute("format") ;
  var dateFormat = (UIDateTimePicker.isDisplayTime)?dateTimeFormat.split(' ')[0].trim() : dateTimeFormat ;
  var len = dateMask.length ;
  for(var i = 0 ; i < len ; i ++) {
    if (dateMask[i] == dateFormat) return i ;
  }
  return false ;
},

setDate : function(year, month, day) {
  if (UIDateTimePicker.dateField) {
    if (month < 10) month = "0" + month ;
    if (day < 10) day = "0" + day ;
    var dateString = UIDateTimePicker.dateField.getAttribute("format") ;
    yearString = new String(dateString.match(/y{2,4}/)) ;
    year = year.toString() ;
    if(yearString.length < 4) year = year.charAt(year.length - 2) + year.charAt(year.length - 1) ;
    dateString = dateString.replace(/d{2}/, day) ;
    dateString = dateString.replace(/M{2}/, month) ;
    dateString = dateString.replace(/y{2,4}/, year) ;
    UIDateTimePicker.currentHours = new Date().getHours() ;
    UIDateTimePicker.currentMinutes = new Date().getMinutes() ;
    UIDateTimePicker.currentSeconds = new Date().getSeconds() ;
    if (UIDateTimePicker.isDisplayTime) {
      var currentHours = (UIDateTimePicker.currentHours < 10) ? "0" + UIDateTimePicker.currentHours : UIDateTimePicker.currentHours;
      var currentMinutes = (UIDateTimePicker.currentMinutes < 10) ? "0" + UIDateTimePicker.currentMinutes : UIDateTimePicker.currentMinutes;
      var currentSeconds = (UIDateTimePicker.currentSeconds < 10) ? "0" + UIDateTimePicker.currentSeconds : UIDateTimePicker.currentSeconds;
      dateString = dateString.replace(/H{2}/, currentHours) ;
      dateString = dateString.replace(/m{2}/, currentMinutes) ;
      dateString = dateString.replace(/s{2}/, currentSeconds) ;
    } else {
      dateString = dateString.split(' ') ;
      UIDateTimePicker.dateField.value = dateString[0] ;
      UIDateTimePicker.hide() ;
      return ;
    }
    
    UIDateTimePicker.dateField.value = dateString ;
    UIDateTimePicker.hide() ;
  }
  return ;
},

renderCalendar : function() {
  var dayOfMonth = 1 ;
  var validDay = 0 ;
  var startDayOfWeek = UIDateTimePicker.getDayOfWeek(UIDateTimePicker.currentDate.getFullYear(), UIDateTimePicker.currentDate.getMonth() + 1, dayOfMonth) ;
  var daysInMonth = UIDateTimePicker.getDaysInMonth(UIDateTimePicker.currentDate.getFullYear(), UIDateTimePicker.currentDate.getMonth()) ;
  var clazz = null;
  var table = '<div ' + 'relId=' + gj(UIDateTimePicker.dateField).attr('name');
  table += ' class="uiCalendarComponent uiBox" onmousedown="event.cancelBubble = true">';
  table += '<h5 class="title clearfix">';
  table += '<a data-placement="right" rel="tooltip" onclick="eXo.cs.UIDateTimePicker.changeMonth(-1);" class="actionIcon pull-left" data-original-title="'+ UIDateTimePicker.tooltip[1]+ '"><i class="uiIconMiniArrowLeft uiIconLightGray"></i></a>';
  table += '<span>'+ UIDateTimePicker.months[UIDateTimePicker.currentDate.getMonth()] +', '+ UIDateTimePicker.currentDate.getFullYear() + '</span>';
  table += '<a data-placement="right" rel="tooltip" onclick="eXo.cs.UIDateTimePicker.changeMonth(1);" class="actionIcon pull-right" data-original-title="'+ UIDateTimePicker.tooltip[2]+ '"><i class="uiIconMiniArrowRight uiIconLightGray"></i></a>';
  table += '</h5>';
  
  table += '<table class="weekList">';
  table += '  <tr>';  
  table +=    '       <td><font color="red">' + UIDateTimePicker.weekdays[0] + '</font></td><td>' + UIDateTimePicker.weekdays[1] + '</td><td>' + UIDateTimePicker.weekdays[2] + '</td><td>' + UIDateTimePicker.weekdays[3] + '</td><td>' + UIDateTimePicker.weekdays[4] + '</td><td>' + UIDateTimePicker.weekdays[5] + '</td><td>' + UIDateTimePicker.weekdays[6] + '</td>' ;  
  table += '  </tr>';
  table += '</table>';
  table += '<hr>';

var _pyear, _pmonth, _pday, _nyear, _nmonth, _nday, _weekend;
    var _today = new Date();
    var tableRow='';
    if(startDayOfWeek==0) startDayOfWeek = 7;
    _pyear = (UIDateTimePicker.currentDate.getMonth() == 0) ? UIDateTimePicker.currentDate.getFullYear() - 1 : UIDateTimePicker.currentDate.getFullYear();
    _pmonth = (UIDateTimePicker.currentDate.getMonth() == 0) ? 11 : UIDateTimePicker.currentDate.getMonth() - 1;
    _pday = UIDateTimePicker.getDaysInMonth(_pyear, _pmonth) - ((startDayOfWeek + ((8 - UIDateTimePicker.firstDayOfWeek) % 7)) % 7) + 1;
    
    _nmonth = (UIDateTimePicker.currentDate.getMonth() == 11) ? 0 : UIDateTimePicker.currentDate.getMonth() + 1;
    _nyear = (UIDateTimePicker.currentDate.getMonth() == 11) ? UIDateTimePicker.currentDate.getFullYear() + 1 : UIDateTimePicker.currentDate.getFullYear();
    _nday = 1;
    
    table += '<table cellspacing="0" cellpadding="0" id="" class="weekDays">';
      for ( var week = 0; week < 6; week++) {
      tableRow += '<tr {{week'+week+'}}>';
        for ( var dayOfWeek = 0; dayOfWeek <= 6; dayOfWeek++) {
          if (week == 0
              && dayOfWeek == (startDayOfWeek + ((8 - UIDateTimePicker.firstDayOfWeek) % 7)) % 7) {
            validDay = 1;
          } else if (validDay == 1 && dayOfMonth > daysInMonth) {
            validDay = 0;
          }
          if (validDay) {
            if (dayOfMonth == UIDateTimePicker.selectedDate.getDate()
                && UIDateTimePicker.currentDate.getFullYear() == UIDateTimePicker.selectedDate
                    .getFullYear()
                && UIDateTimePicker.currentDate.getMonth() == UIDateTimePicker.selectedDate.getMonth()) {
              clazz = 'selected';
            } else {
              clazz = '';
            }
        if(_today.getDate() == dayOfMonth
                && UIDateTimePicker.currentDate.getFullYear() == _today.getFullYear()
                && UIDateTimePicker.currentDate.getMonth() == _today.getMonth()) {
        clazz = 'highLight today';
        tableRow = tableRow.replace('{{week'+week+'}}','class="currentWeek"');
        }
            tableRow = tableRow + '<td><a class="' + clazz
                + '" href="#SelectDate" onclick="eXo.cs.UIDateTimePicker.setDate('
                + UIDateTimePicker.currentDate.getFullYear() + ','
                + (UIDateTimePicker.currentDate.getMonth() + 1) + ',' + dayOfMonth + ')">'
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
},

hide : function() {
  if (UIDateTimePicker.dateField) {
    document.getElementById(UIDateTimePicker.calendarId).firstChild.style.display = 'none';
    UIDateTimePicker.dateField = null;
  }
  gj(document).off('mousedown.calendar');
}

};
var tmp = {};
gj.extend(tmp, uiCalendar, UIDateTimePicker);
UIDateTimePicker = tmp;

window.eXo.cs = window.eXo.cs || {};
window.eXo.cs.UIDateTimePicker = UIDateTimePicker;
return UIDateTimePicker;
})(gj, uiCalendar);