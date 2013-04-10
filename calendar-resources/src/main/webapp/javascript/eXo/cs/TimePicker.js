(function(uiCalendar, base, gj){
	var _module = {};
	function TimePicker() {

	} ;
	// init the form
	TimePicker.prototype.init = function(id) {
		var createForm = gj('#' + id);

		this.startDateInput = createForm.find('input[name="StartEvent"]');
		this.endDateInput = createForm.find('input[name="EndEvent"]');

		this.startTimeInput = createForm.find('select[name="start_time"]');
		this.endTimeInput = createForm.find('select[name="end_time"]');

		this.startTimeInput.change(_module.TimePicker.selectTime);
		this.startDateInput.on('click',_module.TimePicker.selectDate);
		// auto input FromTime and ToTime
		_module.TimePicker.initTime(id);
	}

	// take current time, round it, and auto input to FromTime the rounded time and ToTime 1 hour later 
	TimePicker.prototype.initTime = function(id) {
		var currentTime = new Date();
		var timeInterval = 30;
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
		_module.TimePicker.startTimeInput.val(value);
		_module.TimePicker.updateEndInput(value);
	}

	// synchronize between FromTime and ToTime input
	// the ToTime is automatically 1 hour after FromTime
	TimePicker.prototype.updateEndInput = function(value) {
		var start = _module.TimePicker.startTimeInput.val();
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
		_module.TimePicker.endTimeInput.val(value);
	}

	TimePicker.prototype.round = function(input, outDefault) {
		if(input == 0) return input;
		if(input <= outDefault) return outDefault;
		else return outDefault*2;
	}

	TimePicker.prototype.selectTime = function() {
		var value =  gj(this).val();
		if(value != 'all-day') {
			value = _module.TimePicker.updateEndInput(value);
		} else {
			_module.TimePicker.endTimeInput.val('all-day');
			_module.TimePicker.syncDate();
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

	// add 1 onclink event when user selects date from Gatein UICalendar component
	// this event will synchronize FromDate and EndDate
	TimePicker.prototype.selectDate = function() {
		gj('.uiCalendarComponent a[href="#SelectDate"]').on('click', _module.TimePicker.syncDate);
	}

	// synchronize FromDate end EndDate
	TimePicker.prototype.syncDate = function() {
		var start = _module.TimePicker.startTimeInput.val();
		_module.TimePicker.updateEndInput(start);
	}

	TimePicker.prototype.addDay = function(dayNum) {
		var startDateInput = _module.TimePicker.startDateInput;
		var endDateInput = _module.TimePicker.endDateInput;

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
