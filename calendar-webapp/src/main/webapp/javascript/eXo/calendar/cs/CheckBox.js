(function(gj) {  

  var CheckBox = {
    init : function(cont) {
      if(typeof(cont) == "string") 
        cont = document.getElementById(cont) ;
      var checkboxes = gj(cont).find('input.checkbox'); 
      if(checkboxes.length <=0) return ;
      checkboxes[0].onclick = this.checkAll ;
      var len = checkboxes.length ;
      for(var i = 1 ; i < len ; i ++) {
        checkboxes[i].onclick = this.check ;
      }
    },

    checkAll : function() {
      CheckBox.checkAllItem(this);
    },
  
    getItems : function(obj) {
      var table = gj(obj).parents('table')[0];
      var checkboxes = gj(table).find('input.checkbox');
      return checkboxes;
    },
  
    check : function() {
      CheckBox.checkItem(this);
    },
  
    checkAllItem : function(obj) {
      var checked = obj.checked;
      var items = CheckBox.getItems(obj);
      var len = items.length;
      for (var i = 1 ; i < len ; i ++) {
        if (items[i].disabled === false) /* only check if checkbox enabled */
        {
          items[i].checked = checked;
          this.highlight(items[i],checked);
        }
      } 
    },
  
    checkItem : function(obj) {
      var checkboxes = CheckBox.getItems(obj);
      var len = checkboxes.length; 
  
      var state = true;
      if (!obj.checked) {
        checkboxes[0].checked = false;
      } else {
        for (var i = 1; i < len; i++) {
          state = state && checkboxes[i].checked;
        }
        checkboxes[0].checked = state;
      }
  
      this.highlight(obj,obj.checked);
    },
  
    highlight : function(obj,isChecked) {
      obj = gj(obj).parents('tr')[0];
      if(!obj) return;
      
      if(isChecked) 
        gj(obj).addClass("UIHightLight");
      else 
        gj(obj).toggleClass("UIHightLight","");
    }
  };

  return CheckBox;
})(gj);