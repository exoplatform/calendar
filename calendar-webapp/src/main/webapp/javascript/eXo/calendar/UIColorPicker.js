(function(gj){

var UIColorPicker = {
  /**
   * entry point of color picker, invoked when clicking on the color picker input
   * adapt the popup to overlap the parent div (tab Details)
   * note that we use boopstrap dropdown to display the table color
   */
  adaptPopup : function(inputColorPicker) {
    // store children of color picker in property
    this.inputColorPicker = inputColorPicker;
    this.formColorPicker  = gj(inputColorPicker).parent('.uiFormColorPicker');
    this.tableColor       = gj(this.formColorPicker).children('.calendarTableColor')[0];
    this.valueColorPicker = gj(this.formColorPicker).children('input.uiColorPickerValue')[0];

    gj(this.formColorPicker).css('position', 'static');  
    var offsetLeft = gj(this.formColorPicker).position().left - 20;
    var offsetTop = gj(this.formColorPicker).position().top + 20;  
    
    // table color is positioned relative to the tab Details 
    gj(this.tableColor).css('position', 'absolute');
    gj(this.tableColor).css('left', offsetLeft + 'px');
    gj(this.tableColor).css('top', offsetTop + 'px');
  },

  /**
   * change color of current input 
   * @param a tag with colorCell class
   */
  setColor : function(colorCell) {
    var clazz = gj(colorCell).attr('class').split(' '); 
    var color = gj.trim(clazz[0]); 
    var className = 'displayValue ' + color;
    var spanDisplayedColor = gj(this.inputColorPicker).children('span.displayValue')[0];
    spanDisplayedColor.className = className; // change displayed color
    this.valueColorPicker.value  = color; // update the input 

    this.clearSelectedColor(); 
    this.updateNewSelectedColor(); 
  },


  /**
   * clear the selected cell
   */
  clearSelectedColor : function() {  
    gj(this.tableColor).find('i').each(function() {
      if (gj(this).hasClass('iconCheckBox')) {
        gj(this).removeClass('iconCheckBox');
        return false;
      }
    });
  },

  updateNewSelectedColor : function() {  
    var newColor = this.valueColorPicker.value;

    gj(this.tableColor).find('a').each(function() {
      if (gj(this).hasClass(newColor)) {
        gj(this).children('i').addClass('iconCheckBox');
        return false;
      }
    });
  }
};

return UIColorPicker;
}) (gj);