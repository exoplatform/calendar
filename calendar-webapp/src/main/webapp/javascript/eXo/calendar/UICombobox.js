(function(gj, wx, ScheduleSupport) {
  /**
   * Override Combobox
   * TODO : remove this method when portal fix it
   * REQUIREJS: UICombobox in webui-ext module : wx.UICombobox; dont use global variable eXo.webui
   */
  wx.UICombobox.init = function() {
    var uiWorkingWorkspace = gj("#UIWorkingWorkspace")[0];
    var uiCombobox = wx.UICombobox ;
    var comboList = gj(uiWorkingWorkspace).find('input.UIComboboxInput');
    var i = comboList.length ;
    while(i--) {
      if (!gj(comboList[i]).data("initialized")) {
        comboList[i].value = gj(comboList[i]).prevAll('input')[0].value;
        
        var onfocus = comboList[i].getAttribute("onfocus");
        var onclick = comboList[i].getAttribute("onclick");
        if(!onfocus) gj(comboList[i]).on('focus.tryShow', uiCombobox.tryShow);
        if(!onclick) gj(comboList[i]).on('click.tryShow', uiCombobox.tryShow);
        
        //workround to clear registered event in combobox template
        comboList[i].onkeyup = null;
        //register by jquery instead
        gj(comboList[i]).off('keyup').on('keyup.combo', uiCombobox.onKeyUp);
        gj(comboList[i]).data("initialized", true);
      }
    }
  };
  
  wx.UICombobox.tryShow = function() {
    if (gj(this).parent().find(".UIComboboxContainer").css('display') === 'none') {
      wx.UICombobox.show.apply(this, arguments);
    }
  };
  
  wx.UICombobox.onKeyUp = function(e) {
    if (e.keyCode == 38 || e.keyCode == 40) {
//        wx.UICombobox.tryShow.call(this);
      
      var jInput = gj(this);
      var hiddenInput = jInput.prev('input');      
      var data = eval(this.getAttribute("options"));
      
      var idx = 0;
      var val = hiddenInput.val();
      if (val && val !== '') {
        idx = gj.inArray(val, data);
      }
      idx = idx != -1 ? idx : 0;
      
      if(e.keyCode == 38) {
        //Up arrow key
        idx = idx > 0 ? idx - 1 : 0;
      } else if(e.keyCode = 40) {
        //Down arrow key
        idx = idx < data.length - 1 ? idx + 1 : idx;
      }
      
      var item = jInput.parent().find('.UIComboboxLabel').get(idx);
      jInput.attr('value', gj(item).html());
      wx.UICombobox.setSelectedItem(jInput.get(0));
    } else {
      wx.UICombobox.complete(this, e);
    }
    
  };
  
  wx.UICombobox.setSelectedItem = function (textbox) {
    if (this.lastSelectedItem)
      gj(this.lastSelectedItem).removeClass("UIComboboxSelectedItem");
    var selectedIndex = parseInt(this.getSelectedItem(textbox));
    if (selectedIndex >= 0) {
      gj(this.items[selectedIndex]).addClass("UIComboboxSelectedItem");
      this.lastSelectedItem = this.items[selectedIndex];
      
      var container = gj(this.list).find('.UIComboboxItemContainer'); 
      var currPos = gj(this.lastSelectedItem).height() * selectedIndex;
      if (currPos < container.scrollTop() || currPos > container.scrollTop() + container.height()) {
        container.scrollTop(currPos);
      }
      var hidden = gj(textbox).prev("input")[0];
      hidden.value = this.items[selectedIndex].getAttribute("value");
    }
  };
  
  /**
 Override combobox onchange
   */
  
  wx.UICombobox.getValue = function(obj){
    var UICombobox = eXo.webui.UICombobox;
    var val = obj.getAttribute("value");
    var hiddenField = gj(UICombobox.list.parentNode).next("input");
    hiddenField.attr("value", val);
    var text = hiddenField.next("input");
    text.attr("value", gj(obj).find(".UIComboboxLabel").first().html());
    ScheduleSupport.syncTimeBetweenEventTabs();
    
    ScheduleSupport.applyPeriod();
    UICombobox.list.style.display = "none";
  };
  return wx.UICombobox;
})($, wx, ScheduleSupport);