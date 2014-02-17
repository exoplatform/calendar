(function(DOMUtil, base, gj, uiRightClickPopupMenu){
var _module = {};

function UIContextMenu(){
	this.menus = new Array,
	this.attachedElement = null ;
	this.menuElement = null ;
	this.preventDefault = true ;
	this.preventForms = true ;
	this.portletCssClass = "UICalendarPortlet" ;
}
UIContextMenu.prototype.getCallback = function(menu) {
  if(!menu) return ;
	var callback = menu.getAttribute("eXoCallback") ;
	return callback ;
} ;

UIContextMenu.prototype.init = function(conf) {
	this.IE = (gj.browser.msie != undefined) ;
	try {		
		this.preventDefault = conf.preventDefault ;
		this.preventForms = conf.preventForms ;
	}catch(e) {}	
	document.getElementById(this.portletName).onmouseover = this.setup ;
	document.getElementById(this.portletName).onmouseout = this.setup ;
} ;

UIContextMenu.prototype.setup = function(evt) {
	var _e = window.event || evt ;
	var type = _e.type ;
	if(type == "mouseover") document.oncontextmenu = _module.UIContextMenu.show;
	if(type == "mouseout") document.oncontextmenu = function() { return true ;} ;
} ;

UIContextMenu.prototype.attach = function(classNames, menuId) {
	if (typeof(classNames) == "string") {
		this.menus[classNames] = menuId;
	}

	if (typeof(classNames) == "object") {
		for (x = 0; x < classNames.length; x++) {
			this.menus[classNames[x]] = menuId ;
		}
	}
} ;

UIContextMenu.prototype.getMenuElementId = function() {
	while(this.attachedElement != null) {
		var className = this.attachedElement.className;

		if (typeof(className) != "undefined") {
			className = className.replace(/^\s+/g, "").replace(/\s+$/g, "")
			var classArray = className.split(/[ ]+/g);

			for (i = 0; i < classArray.length; i++) {
				if (this.menus[classArray[i]]) {
					return this.menus[classArray[i]];
				}
			}
		}

		if (this.IE) {
			this.attachedElement = this.attachedElement.parentElement;
		} else {
			this.attachedElement = this.attachedElement.parentNode;
		}
	}

	return null;
} ;

UIContextMenu.prototype.getReturnValue = function() {
	var returnValue = true;	
	try{
		var tname = this.attachedElement.tagName.toLowerCase();
	}catch(e) {return true ;}	
	if ((tname == "input" || tname == "textarea")) {
		if (!this.preventForms) {
			returnValue = true;
		} else {
			returnValue = false;
		}
	} else {
		if (!this.preventDefault) {
			returnValue = true;
		} else {
			returnValue = false;
		}
	}
	return returnValue;
} ;

UIContextMenu.prototype.hasChild = function(root, obj) {
	if(typeof(obj) == "string") obj = document.getElementById(obj) ;
	var children = gj(root).children("div.UIRightClickPopupMenu") ;
	var len = children.length ;
  for(var i = 0 ; i < len ; i ++) {
  	if (children[i].id == obj.id) return children[i] ;    
  }
	return false ;
} ;

UIContextMenu.prototype.getSource = function(evt) {
	var _e = window.event || evt ;
	var src = _e.target || _e.srcElement ;
	return src ;
} ;

UIContextMenu.prototype.autoHide = function(evt) {
	_module.Utils = window.require("SHARED/CSUtils").Utils;
	var _e = window.event || evt ;
	var eventType = _e.type ;
	if (eventType == 'mouseout' && (this.style.display != "none")) {
		_module.Utils.contextMenuTimeout = window.setTimeout("document.getElementById('" + this.id + "').style.display='none'", 5000) ;
	} else {
		if (_module.Utils.contextMenuTimeout) {
			window.clearTimeout(_module.Utils.contextMenuTimeout) ;		
			_module.Utils.contextMenuTimeout.timeout = null ;		
		}
	}
} ;

UIContextMenu.prototype.replaceall = function(string, obj) {			
	var p = new Array() ;
	var i = 0 ;
	for(var reg in obj){
		p.push(new RegExp(reg)) ;
		string = string.replace(p[i], obj[reg]) ;
		i++ ;
	}
	if (!string) alert("Not match") ;
	return string ;
} ;

UIContextMenu.prototype.changeAction = function(obj, id) {
	var actions = gj(obj).find("a") ;
	var len = actions.length ;
	var href = "" ;
	if (typeof(id) == "string") {
		for(var i = 0 ; i < len ; i++) {
			href = String(actions[i].href) ;
			if(href.indexOf('&ajaxRequest=true') > 0){
			  if (href.indexOf('&objectId=') < 0) {
			    actions[i].href = href.replace('&ajaxRequest=true', '&objectId=' + id + '&ajaxRequest=true');
			  }
			}else if(href.indexOf('&objectId=id') > 0){
				actions[i].href = href.replace('&objectId=id', '&objectId=' + id);
			}
		}
	} else if (typeof(id) == "object") {
		for(var i = 0 ; i < len ; i++) {
			href = String(actions[i].href) ;			
			actions[i].href = this.replaceall(href, id) ;
		}
	} else {
		return  ;
	}
	
} ;

UIContextMenu.prototype.showHide = function() {
	if(!this.menuElement) return ;
	if (this.menuElement.style.display != "block") {
		DOMUtil.DOMUtil.cleanUpHiddenElements() ;
		this.menuElement.style.display = "block" ;
		DOMUtil.DOMUtil.listHideElements(this.menuElement) ;
	} else {
		this.menuElement.style.display = "none" ;
	}
} ;

UIContextMenu.prototype.swapMenu = function(oldmenu, mousePos, evt) {
  //var DOMUtil = DOMUtil.DOMUtil;
  _module.Utils = window.require("SHARED/CSUtils").Utils;
  var Browser = base.Browser;
  var browserHeight = gj(window).height() + document.documentElement.scrollTop || document.body.scrollTop;
  var browserWidth = gj(window).width() + document.documentElement.scrollLeft || document.body.scrollLeft;
  if (document.getElementById("tmpMenuElement"))
    gj("#tmpMenuElement").remove();
  var tmpMenuElement = oldmenu.cloneNode(true);
  tmpMenuElement.setAttribute("id", "tmpMenuElement");
  gj(tmpMenuElement).addClass(this.portletCssClass + " UIEmpty");
  this.menuElement = tmpMenuElement;
  var callback = this.getCallback(tmpMenuElement);  
  if (callback) {	
	//Just a workaround for this case
	//TODO: Add all "window." before all variables in callback string
	//TODO: Please find a better solution or a better action flow!  
	callback = "window."+callback + "(arguments[2])";
	eval(callback);
  }
  var uiApplication = document.getElementById("UIPortalApplication");
  if (this.menuElement) {
    document.body.insertBefore(this.menuElement, uiApplication);
    uiRightClickPopupMenu.disableContextMenu('tmpMenuElement');
    this.menuElement.onmousedown = function(e) {
      var rightclick = false;
      if (!e)
        var e = window.event;
      if (e.which)
        rightclick = (e.which == 3);
      else if (e.button)
        rightclick = (e.button == 2);
      if (rightclick) {
        document.oncontextmenu = function() {
          return false
        };
        e.cancelBubble = true;
        return false;
      }
    }
    var left = mousePos.x - 3;
    var top = mousePos.y - 3;
    if (Browser.isIE6())
      this.menuElement.style.width = "140px";
    if (base.I18n.isRT()) {
      left -= (_module.Utils.getElementWidth(this.menuElement) - 3);
      if (Browser.isIE6() || Browser.isIE7())
        left -= _module.Utils.getScrollbarWidth() + 3;
    }
    //this.menuElement.style.padding = "0px";
    this.menuElement.style.zIndex = 2000;
    this.menuElement.style.top = top + "px";
    this.menuElement.style.left = left + "px";

    /* set display to block to be able to calculate offsetHeight */
    gj(this.menuElement).css("display", "block");

    if ((this.menuElement.offsetHeight + mousePos.y) > browserHeight)
      this.menuElement.style.top = mousePos.y - this.menuElement.offsetHeight + 2 + "px";
    if ((this.menuElement.offsetWidth + mousePos.x) > browserWidth)
      this.menuElement.style.left = mousePos.x - this.menuElement.offsetWidth + 2 + "px";
    this.menuElement.style.display = "none";
    this.menuElement.style.visibility = "visible";
  }
  this.showHide(); 	
} ;

UIContextMenu.prototype.show = function(evt) {
	_module.Browser = window.require("SHARED/Browser").Browser;
	var _e = window.event || evt
	var UIContextMenu = _module.UIContextMenu ;
	UIContextMenu.attachedElement = UIContextMenu.getSource(_e) ;
	var menuPos = {
		"x": _module.Browser.findMouseXInPage(_e) ,
		"y": _module.Browser.findMouseYInPage(_e)
	} ;
	var menuElementId = UIContextMenu.getMenuElementId() ;
	var currentPortlet = gj(UIContextMenu.attachedElement).parents('.' + UIContextMenu.portletCssClass)[0];
	if (menuElementId) {
		UIContextMenu.menuElement = gj(currentPortlet).find('#' + menuElementId)[0] ; //document.getElementById(menuElementId) ;
		DOMUtil.DOMUtil.listHideElements(UIContextMenu.menuElement) ;
		DOMUtil.DOMUtil.cleanUpHiddenElements();
		UIContextMenu.swapMenu(document.getElementById(menuElementId), menuPos,_e) ;
		if(!UIContextMenu.menuElement) 
			return false;
		UIContextMenu.menuElement.onmouseover = UIContextMenu.autoHide ;
		UIContextMenu.menuElement.onmouseout = UIContextMenu.autoHide ;		
		return false ;
	}
	return UIContextMenu.getReturnValue() ;
} ;

//eXo.webui.UIContextMenu = new UIContextMenu() ;
_module.UIContextMenu = new UIContextMenu() ;
return _module;
})(DOMUtil, base, gj, uiRightClickPopupMenu);
