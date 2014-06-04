(function(gj) {
/**
relooking support for schedule tab
*/
var ScheduleSupport = {
	//returns index of the cell in the first row with the time given in format : HH:MM (AM/PM)
	indexFromTime : function(time) {
	    var hourStr = time.split(':')[0];
	    var minStr = time.split(':')[1].slice(0,2);
	    var hours,mins;
	    if(hourStr[0] == '0') {
		hours = parseInt(hourStr[1]);
	    } else {
		hours = parseInt(hourStr);
	    }
	    if(minStr[0] == '0') {
		mins = parseInt(minStr[1]);
	    } else {
		mins = parseInt(minStr);
	    }
	    if(time.indexOf('AM') > -1) {
		hours = (hours == 12) ? 0 : hours; // 12:00 AM -> 00:00 AM to calculate index
	    }
	    if(time.indexOf('PM') > -1) {
		hours += 12;
	    }
	    return (hours * 60 + mins)/15 + 1; // index start from 1
	},

	//apply green period in schedule tab 
	applyPeriod : function() {
	    var scheduleTab = gj('#eventAttender-tab')[0];
	    if(scheduleTab) {
		// row for drag
		var dragRow = gj(scheduleTab).find('tr').get(1);
		var cells = gj(dragRow).find('td');
		// check box for all day
		var dateAll = gj(scheduleTab).find('[name="dateAll"]')[0];
		if(dateAll.checked) {
		    for(var i = 1; i < cells.length; i++) {
			gj(cells.get(i)).removeClass("userSelection"); // reset the color of cells
			gj(cells.get(i)).removeClass("busySelected");
			if(gj(cells.get(i)).hasClass("busyTime")) 
			    gj(cells.get(i)).addClass("busySelected");
			else
			    gj(cells.get(i)).addClass("userSelection");
		    }
		} else {
		    var UIComboboxInputs = gj(scheduleTab).find("input.UIComboboxInput");
		    len = UIComboboxInputs.length;
		    var name = gj(UIComboboxInputs[0]).prevAll('input')[0].name.toLowerCase();
		    if (name.indexOf("from") >= 0) {
			start = UIComboboxInputs[0].value;
			end = UIComboboxInputs[1].value;
		    }
		    else {
			start = UIComboboxInputs[1].value;
			end = UIComboboxInputs[0].value;
		    }

		    var startIndex, endIndex;
		    try {
		    	startIndex = _module.ScheduleSupport.indexFromTime(start);
		    	endIndex = _module.ScheduleSupport.indexFromTime(end);		    	
		    } catch (e) {
		    	return;
		    }

		    // add UserSelection class to have green color
		    for(var i = 1; i < cells.length; i++) {
			gj(cells.get(i)).removeClass("userSelection");
			gj(cells.get(i)).removeClass("busySelected");// reset the color of cells
			if(i < endIndex && i >= startIndex) {
			    if(gj(cells.get(i)).hasClass("busyTime")) 
				gj(cells.get(i)).addClass("busySelected");
			    else
				gj(cells.get(i)).addClass("userSelection");
			}
		    }	
		}

	    }
	       
	},
	//synchronize time of UIComboBox in Schedule Tab and Detail Tab
	syncTimeBetweenEventTabs : function() {
	   var scheduleTab = gj('#eventAttender-tab')[0];
	   if(scheduleTab){
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
	
}

return ScheduleSupport;
})(gj);
