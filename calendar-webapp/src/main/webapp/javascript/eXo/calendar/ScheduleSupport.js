/**
relooking support for schedule tab
*/
eXo.calendar.ScheduleSupport = {
	//returns index of the cell in the first row with the time given in format : HH:MM (AM/PM)
	indexFromTime : function(time) {
	   
	   var hours = parseInt(time.split(':')[0]);
	   var mins = parseInt(time.split(':')[1].slice(0,2));
	   if(time.indexOf('AM') > -1) {
	       hours = (hours == 12) ? 0 : hours; // 12:00 AM -> 00:00 AM to calculate index
	   }
	   if(time.indexOf('PM') > -1) {
	     hours += 12;
	   }
	   return (hours * 60 + mins)/15 + 1; // index start from 1
	},

	//apply green period in schedule tab 
	applyPeriod : function(){
	   var Highlighter = eXo.calendar.UIHSelection;
	   var scheduleTab = gj('#eventAttender-tab')[0];
	   if(gj(scheduleTab).hasClass("active")) { // if schedule tab is currently selected
	   var UIComboboxInputs = gj(scheduleTab).find("input.UIComboboxInput");
	   len = UIComboboxInputs.length;
	   var name = null;
	   var timeFormat = _module.UICalendarPortlet.getTimeFormat(null);
	   for (var i = 0; i < len; i++) {
	       name = gj(UIComboboxInputs[i]).prevAll('input')[0].name.toLowerCase();
	       if (name.indexOf("from") >= 0) {
	           start = UIComboboxInputs[i].value;
	               }
	       else {
	           end = UIComboboxInputs[i].value;
	               }
	   }
	   var startIndex = _module.ScheduleSupport.indexFromTime(start);
	   var endIndex = _module.ScheduleSupport.indexFromTime(end);

	   // row for drag
	   var dragRow = gj(scheduleTab).find('tr').get(1);
	   var cells = gj(dragRow).find('td');
	   // add UserSelection class to have green color
	   for(var i = 1; i < cells.length; i++) {
	       gj(cells.get(i)).removeClass("UserSelection"); // reset the color of cells
	       if(i < endIndex && i >= startIndex) {
	           gj(cells.get(i)).addClass("UserSelection");
	       }
	   }
	  }
	},
	//synchronize time of UIComboBox in Schedule Tab and Detail Tab
	syncTimeBetweenEventTabs : function() {
	   var scheduleTab = gj('#eventAttender-tab')[0];;
	   var detailsTab = gj('#eventDetail-tab')[0];
	   var detailsCombos = gj(detailsTab).find('input.UIComboboxInput');
	   var scheduleCombos = gj(scheduleTab).find('input.UIComboboxInput');
	   var start = null;
	   var end = null;
	   var hiddenInput = null;
	   // sync schedule tab to detail tab
	   if(gj(scheduleTab).hasClass('active')) {
	       hiddenInput = gj(scheduleCombos[0]).prevAll('input')[0];
	       if(hiddenInput.name.toLowerCase().indexOf('from') > -1) {
	           start = scheduleCombos[0].value;
	           end = scheduleCombos[1].value;
	       } else {
	           start = scheduleCombos[1].value;
	           end = scheduleCombos[0].value;
	       }

	       hiddenInput0 = gj(detailsCombos[0]).prevAll('input')[0];
	       hiddenInput1 = gj(detailsCombos[1]).prevAll('input')[0];
	       if(hiddenInput0.name.toLowerCase().indexOf('from') > -1) {
	           gj(detailsCombos[0]).attr("value",start);
	           gj(hiddenInput0).attr("value",start);
	           gj(detailsCombos[1]).attr("value",end);
	           gj(hiddenInput1).attr("value",end);

	       } else {
	           gj(detailsCombos[1]).attr("value",start);
	           gj(hiddenInput1).attr("value",start);
	           gj(detailsCombos[0]).attr("value",end);
	           gj(hiddenInput0).attr("value",start);
	       }
	   }
	   // sync detail tab to schedule tab
	   if(gj(detailsTab).hasClass('active')) {
	   hiddenInput = gj(detailsCombos[0]).prevAll('input')[0];
	       if(hiddenInput.name.toLowerCase().indexOf('from') > -1) {
	           start = detailsCombos[0].value;
	           end = detailsCombos[1].value;
	       } else {
	           start = detailsCombos[1].value;
	           end = detailsCombos[0].value;
	       }

	       hiddenInput0 = gj(scheduleCombos[0]).prevAll('input')[0];
	       hiddenInput1 = gj(scheduleCombos[1]).prevAll('input')[0];
	       if(hiddenInput0.name.toLowerCase().indexOf('from') > -1) {
	           gj(scheduleCombos[0]).attr("value",start);
	           gj(hiddenInput0).attr("value",start);
	           gj(scheduleCombos[1]).attr("value",end);
	           gj(hiddenInput1).attr("value",end);

	       } else {
	           gj(scheduleCombos[1]).attr("value",start);
	           gj(hiddenInput1).attr("value",start);
	           gj(scheduleCombos[0]).attr("value",end);
	           gj(hiddenInput0).attr("value",start);
	       }
	   }
	}
	
}
_module.ScheduleSupport = eXo.calendar.ScheduleSupport;
