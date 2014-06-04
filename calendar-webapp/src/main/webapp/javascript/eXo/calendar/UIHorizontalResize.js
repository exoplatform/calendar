(function(gj) {	

var UIHorizontalResize = {
  start : function(evt, outer, inner) {
    var _e = window.event || evt ;
    this.outerElement = outer ;
    this.innerElement = inner ;
    if(arguments.length > 3) {
      this.outerElement.style.left = this.outerElement.offsetLeft + "px" ;
      this.isLeft = true ;
      this.beforeLeft = this.outerElement.offsetLeft ;
    } else {
      this.isLeft = false ;
    }
    this.mouseX = _e.clientX ;
    this.outerBeforeWidth = this.outerElement.offsetWidth - 2 ;
    this.innerBeforeWidth = this.innerElement.offsetWidth - 2 ;
    this.beforeWidth = this.outerElement.offsetWidth ;
    gj(document).off('mousemove mouseup').on({'mousemove':UIHorizontalResize.execute,
      'mouseup':UIHorizontalResize.end});
  },

  execute : function(evt) {
    var _e = window.event || evt ;
    var delta = _e.clientX - UIHorizontalResize.mouseX ;
    if(UIHorizontalResize.isLeft == true) {
      UIHorizontalResize.outerElement.style.left = UIHorizontalResize.beforeLeft + delta + "px" ;
      if (parseInt(UIHorizontalResize.outerElement.style.left) > 0){
        UIHorizontalResize.outerElement.style.width = UIHorizontalResize.outerBeforeWidth - delta + "px" ;
        UIHorizontalResize.innerElement.style.width = UIHorizontalResize.innerBeforeWidth - delta + "px" ;      
      }
    } else {
      UIHorizontalResize.outerElement.style.width = UIHorizontalResize.outerBeforeWidth + delta + "px" ;
      UIHorizontalResize.innerElement.style.width = UIHorizontalResize.innerBeforeWidth + delta + "px" ;    
    }
    if(typeof(UIHorizontalResize.dragCallback) == "function") {
      UIHorizontalResize.dragCallback(_e) ;
    }
  },

  end : function(evt) {
    if (typeof(UIHorizontalResize.callback) == "function") UIHorizontalResize.callback() ;
    delete UIHorizontalResize.outerElement ;
    delete UIHorizontalResize.innerElement ;
    delete UIHorizontalResize.outerBeforeWidth ;
    delete UIHorizontalResize.innerBeforeWidth ;
    delete UIHorizontalResize.beforeWidth ;
    delete UIHorizontalResize.callback ;
    delete UIHorizontalResize.mouseX ;
    delete UIHorizontalResize.isLeft ;
    delete UIHorizontalResize.beforeLeft ;
    gj(document).off("mousemove mouseup");
  }
};

return UIHorizontalResize;
})($);