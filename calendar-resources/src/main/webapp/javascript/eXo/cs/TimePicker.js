(function(uiCalendar, base, gj){
var _module = {};
function TimePicker() {
   
} ;


TimePicker.prototype.initTime = function(id) {
	var createForm = gj('#'+ id);
	if(createForm){
		var startTime  = gj(createForm).find('#startTimeId'); 
		var currentTime = new Date();
		var timeInterval = 30;
		if(startTime) {
			var selectBox = gj(startTime).find('select')[0];
			if(selectBox) {
				var hour =  currentTime.getHours();
				var minute = currentTime.getMinutes();
				var roundedTime = _module.TimePicker.round(minute, timeInterval);
				var value;
				if(roundedTime == 0) {
					roundedTime = '00';	
				} else if(roundedTime == 60){
					hour+=1;
					roundedTime = '00';				 
				}
				if(hour < 10) hour = "0" + hour;
				value = hour+':'+ roundedTime;
				gj(selectBox).find('option[value="'+ value +'"]').attr('selected','true');
				gj(selectBox).change(_module.TimePicker.selectTime);
				_module.TimePicker.updateEndInput(value);
			}
		}
		var dateDiv = gj('#startDateId input');
		dateDiv.on('click',_module.TimePicker.selectDate);
	}

}

TimePicker.prototype.updateEndInput = function(value) {
	var endTime  = gj('#endTimeId'); 
	var startTime  = gj('#startTimeId'); 
	if(startTime) {
		var selectBox = gj(startTime).find('select')[0];
		var start = gj(selectBox).val();
		 if(start == '23:00') {
		 	_module.TimePicker.addDay(1);
		 	value = '00:00';
		 } else if(start == '23:30') {
		 	_module.TimePicker.addDay(1);
		 	value = '00:30';
		 } else if(start == '23:59') {
		 	_module.TimePicker.addDay(1);
		 	value = '01:00';
		 } else {
	 		_module.TimePicker.addDay(0);	
			value = _module.TimePicker.addHour(value, 1);
		 }
		var selectBox = gj(endTime).find('select')[0];
		gj(selectBox).find('option[value="'+ value +'"]').attr('selected','true');
	}
	
}


TimePicker.prototype.round = function(input, outDefault) {
	if(input == 0) return input;
	if(input <= outDefault) return outDefault;
	else return outDefault*2;
}

TimePicker.prototype.selectTime = function() {
	var value =  gj(this).val();
	var endTime  = gj('#endTimeId'); 
	if(endTime) {
		if(value != 'all-day') value = _module.TimePicker.updateEndInput(value);
		var selectBox = gj(endTime).find('select')[0];
		gj(selectBox).find('option[value="'+value+'"]').attr('selected','true');
	}
}

TimePicker.prototype.addHour = function(input, interval) {
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

TimePicker.prototype.selectDate = function() {
	 gj('#UICalendarControl div.CalendarGrid a').on('click', _module.TimePicker.addDate);
}

TimePicker.prototype.addDate = function() {
	 var startDateInput = gj('#startDateId input');
	 var endDateInput = gj('#endDateId input');
	 var startTime  = gj('#startTimeId'); 
		if(startTime) {
			var selectBox = gj(startTime).find('select')[0];
			if(selectBox) {
				var start = gj(selectBox).val();
				_module.TimePicker.updateEndInput(start);

			}

		}
	 
}

TimePicker.prototype.addDay = function(dayNum) {
	 var startDateInput = gj('#startDateId input');
	 var endDateInput = gj('#endDateId input');
	 var dateValue = startDateInput.val();
	 uiCalendar.currentDate = _module.TimePicker.dateParses(dateValue, _module.TimePicker.datePattern);
	 uiCalendar.currentDate.setDate(uiCalendar.currentDate.getDate()+dayNum);
	 uiCalendar.datePattern = _module.TimePicker.datePattern;
	 var value = uiCalendar.getDateTimeString();
	 endDateInput.val(value);	 
}

TimePicker.prototype.dateParses = function(dateFieldValue, pattern) {
	var dateIndex =   pattern.indexOf("dd");
	var dateValue = parseInt(dateFieldValue.substring(dateIndex,dateIndex + 2), 10);
	var monthIndex =   pattern.indexOf("MM");
	var monthValue = parseInt(dateFieldValue.substring(monthIndex,monthIndex + 2) - 1, 10);
	var yearIndex =   pattern.indexOf("yyyy");
	var yearValue = parseInt(dateFieldValue.substring(yearIndex,yearIndex + 4), 10);
	var currentDate = new Date();
	currentDate.setDate(dateValue);
	currentDate.setMonth(monthValue);
	currentDate.setYear(yearValue);
	return currentDate;
}

 _module.TimePicker = new TimePicker();
return _module.TimePicker;

})(uiCalendar, base, gj);
