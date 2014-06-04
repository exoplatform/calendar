(function(uiCalendar, gj) {
	var TimePicker = {
	    // init the form
	    init : function(id) {
	      var createForm = gj('#' + id);

	      TimePicker.startDateInput = createForm.find('input[name="StartEvent"]');
	      TimePicker.endDateInput = createForm.find('input[name="EndEvent"]');

	      TimePicker.startTimeInput = createForm.find('select[name="start_time"]');
	      TimePicker.endTimeInput = createForm.find('select[name="end_time"]');

	      TimePicker.startTimeInput.change(TimePicker.selectTime);
	      TimePicker.startDateInput.on('click',TimePicker.selectDate);
	      // auto input FromTime and ToTime
	      TimePicker.initTime(id);
	    },

	    // take current time, round it, and auto input to FromTime the rounded time and ToTime 1 hour later 
	    initTime : function(id) {
	      var currentTime = new Date();
	      var timeInterval = 30;
	      var hour =  currentTime.getHours();
	      var minute = currentTime.getMinutes();
	      var roundedTime = TimePicker.round(minute, timeInterval);
	      var value;
	      if(roundedTime == 0) {
	        roundedTime = '00';
	      } else if(roundedTime == 60) {
	        hour+=1;
	        roundedTime = '00';
	      }
	      if(hour < 10) hour = "0" + hour;
	      value = hour+':'+ roundedTime;
	      TimePicker.startTimeInput.val(value);
	      TimePicker.updateEndInput(value);
	    },

	    // synchronize between FromTime and ToTime input
	    // the ToTime is automatically 1 hour after FromTime
	    updateEndInput : function(value) {
	      var start = TimePicker.startTimeInput.val();
	      if(start == '23:00') {
	        TimePicker.addDay(1);
	        value = '00:00';
	      } else if(start == '23:30') {
	        TimePicker.addDay(1);
	        value = '00:30';
	      } else if(start == '23:59') {
	        TimePicker.addDay(1);
	        value = '01:00';
	      } else {
	        TimePicker.addDay(0); 
	        value = TimePicker.addHour(value, 1);
	      }
	      TimePicker.endTimeInput.val(value);
	    },

	    round : function(input, outDefault) {
	      if(input == 0) return input;
	      if(input <= outDefault) return outDefault;
	      else return outDefault*2;
	    },

	    selectTime : function() {
	      var value =  gj(this).val();
	      if(value != 'all-day') {
	        value = TimePicker.updateEndInput(value);
	      } else {
	        TimePicker.endTimeInput.val('all-day');
	        TimePicker.syncDate();
	      }
	    },

	    addHour : function(input, interval) {
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
	    },

	    // add 1 onclink event when user selects date from Gatein UICalendar component
	    // this event will synchronize FromDate and EndDate
	    selectDate : function() {
	      gj('.uiCalendarComponent a[href="#SelectDate"]').on('click', TimePicker.syncDate);
	    },

	    // synchronize FromDate end EndDate
	    syncDate : function() {
	      var start = TimePicker.startTimeInput.val();
	      TimePicker.updateEndInput(start);
	    },

	    addDay : function(dayNum) {
	      var startDateInput = TimePicker.startDateInput;
	      var endDateInput = TimePicker.endDateInput;

	      var dateValue = startDateInput.val();
	      uiCalendar.currentDate = TimePicker.dateParses(dateValue, TimePicker.datePattern);
	      uiCalendar.currentDate.setDate(uiCalendar.currentDate.getDate()+dayNum);
	      uiCalendar.datePattern = TimePicker.datePattern;
	      var value = uiCalendar.getDateTimeString();
	      endDateInput.val(value);
	    },

	    dateParses : function(dateFieldValue, pattern) {
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
	};

	return TimePicker;
})(uiCalendar, gj);