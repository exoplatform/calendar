(function(base, gj){
var _module = {};

function TimePicker() {
   
} ;

TimePicker.prototype.initTime = function(id) {
	var startTime  = gj('#' + id).find('#startTimeId'); 
	var endTime  = gj('#' + id).find('#endTimeId'); 
	var currentTime = new Date();
	var timeInterval = 30;
	if(startTime) {
		var selectBox = gj(startTime).find('select')[0];
		if(selectBox) {
			var hour =  currentTime.getHours();
			var minute = currentTime.getMinutes();
			var roundedTime = _module.TimePicker.round(minute, timeInterval);
			var value;
			if(roundedTime == 60){
				hour+=1;
				roundedTime = '00';				 
			} 
			value = hour+':'+ roundedTime;
			gj(selectBox).find('option[value="'+ value +'"]').attr('selected','true');
			gj(selectBox).change(_module.TimePicker.selectTime);
			if(endTime) {
				value = _module.TimePicker.addHour(value, 1);
				var selectBox = gj(endTime).find('select')[0];
				gj(selectBox).find('option[value="'+ value +'"]').attr('selected','true');
			}
		}
	}
	
}

TimePicker.prototype.round = function(input, outDefault) {
	if(input >= outDefault) return outDefault;
	else return outDefault*2;
}

TimePicker.prototype.selectTime = function() {
			var value =  gj(this).val();
			var endTime  = gj('#endTimeId'); 
			if(endTime) {
				if(value != 'all-day') value = _module.TimePicker.addHour(value, 1);
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
	if(hour >= 23) return "23:59";
	hour += interval;
	if(hour < 10) hour = "0" + hour;
	return  hour+':'+input.split(':')[1];
}

 _module.TimePicker = new TimePicker();
return _module.TimePicker;
})(base, gj);
