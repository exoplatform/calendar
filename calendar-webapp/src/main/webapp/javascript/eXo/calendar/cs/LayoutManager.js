(function(base, gj) {
  function LayoutManager(id) {
  	this.layoutId = id;
  }

  LayoutManager.prototype.check = function() {
  	var layoutcookie = base.Browser.getCookie(this.layoutId) ;	
  	var i = layoutcookie.length ;
  	while(i--) {
  		if(!this.layouts[parseInt(layoutcookie.charAt(i))-1]) continue ;
  		this.layouts[parseInt(layoutcookie.charAt(i))-1].style.display = "none";
  	}
  	if(this.callback) this.callback(layoutcookie) ;
  };
  
  LayoutManager.prototype.switchLayout = function(layout) {
  	arrowIcon = gj("#ShowHideAll").find('i');
  	arrowIcon.toggleClass('uiIconMiniArrowLeft').toggleClass('uiIconMiniArrowRight');
  	var layoutcookie = base.Browser.getCookie(this.layoutId) ;
  	var status = this.setValue(layout,layoutcookie);
  	if (!status) {
      if (this.layouts[layout-1]) 
        this.layouts[layout-1].style.display = "none" ;
    } else {
      if (this.layouts[layout-1]) 
        this.layouts[layout-1].style.display = "block" ;
    }
  	if(this.switchCallback) this.switchCallback(layout,status);
  };
  
  LayoutManager.prototype.setValue = function(value, str) {
  	var status = null ;
  	if(str.indexOf(value) < 0) {
  		str = str.concat(value);
  		status = false ;
  	}else {
  		str = str.replace(value,'');
  		status = true ;
  	}	
  	base.Browser.setCookie(this.layoutId,str,1);
  	return status ;
  };
  
  LayoutManager.prototype.reset = function() {
  	var i = this.layouts.length ;
  	while(i--) {
  		if(this.layouts[i]) this.layouts[i].style.display = "block";
  	}
  	base.Browser.setCookie(this.layoutId,"",1);
  	if(this.resetCallback) this.resetCallback() ;
  };

  return LayoutManager;
})(base, gj);