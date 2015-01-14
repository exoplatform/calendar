(function(gj){
    var uiCalendarSettingForm = {

	    /*
	     * check or un-check the checkbox image when clicking on the span tag 'checkbox'
	     */
	    clickOnCalendarCheckbox : function(checkboxBlock) {
    		var iconCheckBox = gj(checkboxBlock).find('span.checkbox');
    
    		/* check or un-check the checkbox icon */
    		if (gj(iconCheckBox).hasClass('iconCheckBox')) {
    		    gj(iconCheckBox).removeClass('iconCheckBox').addClass('iconUnCheckBox');
    		    gj(checkboxBlock).find('input.checkbox').removeAttr('checked');
    		}
    		else {
    		    gj(iconCheckBox).removeClass('iconUnCheckBox').addClass('iconCheckBox');
    		    gj(checkboxBlock).find('input.checkbox').attr('checked', true);
    		}
	    }
    }
    return uiCalendarSettingForm;
})(gj);