(function(DOMUtil, base, gj, uiRightClickPopupMenu) {

var UIContextMenu = {
	menus : new Array,
	attachedElement : null,
	menuElement : null,
	preventDefault : true,
	preventForms : true,
	portletCssClass : "UICalendarPortlet",
	
	/**
     * Sets up context menu for Calendar portlet
     * @param {Object} compid Portlet id
     */
    showContextMenu : function(compid) {
        this.portletNode = gj(document.getElementById(compid)).parents(".PORTLET-FRAGMENT")[0];
        this.portletName = compid;
        UIContextMenu.portletName = this.portletName;
        var config = {
            'preventDefault': false,
            'preventForms': false
        };
        UIContextMenu.init(config);
        UIContextMenu.attach("calendarContentNomal", "UIMonthViewRightMenu");
        UIContextMenu.attach("eventOnDayContent", "UIMonthViewEventRightMenu");
        UIContextMenu.attach("TimeRule", "UIDayViewRightMenu");
        UIContextMenu.attach("eventBoxes", "UIDayViewEventRightMenu");
        UIContextMenu.attach(["Weekday","Weekend","today", "eventAlldayContainer"], "UIWeekViewRightMenu");
        UIContextMenu.attach("uiListViewRow", "uiListViewEventRightMenu");
        this.fixIE();
    },

    /**
     * Fixs relative positioning problems in IE
     */
    fixIE : function() {
        var isDesktop = document.getElementById("UIPageDesktop");
        if ((gj.browser.msie != undefined) && isDesktop) {
            var portlet = this.portletNode;
            var uiResizeBlock = gj(portlet).parents(".UIResizableBlock")[0];
            var relative = gj(uiResizeBlock).find("div.FixIE")[0];
            if (!relative)
                return;
            relative.className = "UIResizableBlock";
            var style = {
                position: "relative",
                height: uiResizeBlock.offsetHeight + 'px',
                width: "100%",
                overflow: "auto"
            };
            gj(relative).css(style);
        }
    },
    
	getCallback : function(menu) {
		  if(!menu) return ;
			var callback = menu.getAttribute("eXoCallback") ;
			return callback ;
	},

	init : function(conf) {
		this.IE = (gj.browser.msie != undefined) ;
		try {		
			this.preventDefault = conf.preventDefault ;
			this.preventForms = conf.preventForms ;
		}catch(e) {}	
		gj("#" + this.portletName).off().on("mouseover mouseout", this.setup);
	},

	setup : function(evt) {
		var type = evt.type ;
		if(type == "mouseover") document.oncontextmenu = UIContextMenu.show;
		if(type == "mouseout") document.oncontextmenu = function() { return true ;} ;
	},

	attach : function(classNames, menuId) {
		if (typeof(classNames) == "string") {
		  UIContextMenu.menus[classNames] = menuId;
		}

		if (typeof(classNames) == "object") {
			for (x = 0; x < classNames.length; x++) {
			  UIContextMenu.menus[classNames[x]] = menuId ;
			}
		}
	},

	getMenuElementId : function() {
		while(UIContextMenu.attachedElement != null) {
			var className = UIContextMenu.attachedElement.className;

			if (typeof(className) != "undefined") {
				className = className.replace(/^\s+/g, "").replace(/\s+$/g, "")
				var classArray = className.split(/[ ]+/g);

				for (i = 0; i < classArray.length; i++) {
					if (UIContextMenu.menus[classArray[i]]) {
						return UIContextMenu.menus[classArray[i]];
					}
				}
			}

			var elem = gj(UIContextMenu.attachedElement).parent();
			UIContextMenu.attachedElement = elem.length > 0 ? elem[0] : null;
		}

		return null;
	},

	getReturnValue : function() {
		var returnValue = true;	
		try{
			var tname = UIContextMenu.attachedElement.tagName.toLowerCase();
		}catch(e) {return true ;}	
		if ((tname == "input" || tname == "textarea")) {
			if (!UIContextMenu.preventForms) {
				returnValue = true;
			} else {
				returnValue = false;
			}
		} else {
			if (!UIContextMenu.preventDefault) {
				returnValue = true;
			} else {
				returnValue = false;
			}
		}
		return returnValue;
	},

	hasChild : function(root, obj) {
		if(typeof(obj) == "string") obj = document.getElementById(obj) ;
		var children = gj(root).children("div.UIRightClickPopupMenu") ;
		var len = children.length ;
	  for(var i = 0 ; i < len ; i ++) {
	  	if (children[i].id == obj.id) return children[i] ;    
	  }
		return false ;
	},

	getSource : function(evt) {
		var _e = window.event || evt ;
		var src = _e.target || _e.srcElement ;
		return src ;
	},

	autoHide : function(evt) {
		Utils = window.require("SHARED/CSUtils");
		var _e = window.event || evt ;
		var eventType = _e.type ;
		if (eventType == 'mouseout' && (this.style.display != "none")) {
			Utils.contextMenuTimeout = window.setTimeout("document.getElementById('" + this.id + "').style.display='none'", 5000) ;
		} else {
			if (Utils.contextMenuTimeout) {
				window.clearTimeout(Utils.contextMenuTimeout) ;		
				Utils.contextMenuTimeout.timeout = null ;		
			}
		}
	},

	replaceall : function(string, obj) {			
		var p = new Array() ;
		var i = 0 ;
		for(var reg in obj) {
			p.push(new RegExp(reg)) ;
			string = string.replace(p[i], obj[reg]) ;
			i++ ;
		}
		if (!string) alert("Not match") ;
		return string ;
	},

	changeAction : function(obj, id) {
		var actions = gj(obj).find("a") ;
		var len = actions.length ;
		var href = "" ;
		if (typeof(id) == "string") {
			for(var i = 0 ; i < len ; i++) {
				href = String(actions[i].href);
				if(href.indexOf('&ajaxRequest=true') > 0) {
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
				actions[i].href = UIContextMenu.replaceall(href, id);
			}
		} else {
			return  ;
		}		
	},

	showHide : function() {
		if(!UIContextMenu.menuElement) return ;
		if (UIContextMenu.menuElement.style.display != "block") {
			DOMUtil.cleanUpHiddenElements();
			UIContextMenu.menuElement.style.display = "block";
			DOMUtil.listHideElements(UIContextMenu.menuElement);
		} else {
		  UIContextMenu.menuElement.style.display = "none";
		}
	},

	swapMenu : function(oldmenu, mousePos, evt) {
	  Utils = window.require("SHARED/CSUtils");
	  var browserHeight = gj(window).height();
	  var browserWidth = gj(window).width();
	  gj("#tmpMenuElement").remove();
	  var tmpMenuElement = oldmenu.cloneNode(true);
	  tmpMenuElement.setAttribute("id", "tmpMenuElement");
	  gj(tmpMenuElement).addClass(UIContextMenu.portletCssClass + " UIEmpty");
	  UIContextMenu.menuElement = tmpMenuElement;
	  var callback = UIContextMenu.getCallback(tmpMenuElement);  
	  if (callback) {	
		//Just a workaround for this case
		//TODO: Add all "window." before all variables in callback string
		//TODO: Please find a better solution or a better action flow!  
		callback = "window."+callback + "(arguments[2])";
		eval(callback);
	  }
	  var uiApplication = document.getElementById("UIPortalApplication");
	  if (UIContextMenu.menuElement) {
	    document.body.insertBefore(UIContextMenu.menuElement, uiApplication);
	    uiRightClickPopupMenu.disableContextMenu('tmpMenuElement');
	    UIContextMenu.menuElement.onmousedown = function(e) {
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
	    if (base.I18n.isRT()) {
	      left -= (Utils.getElementWidth(UIContextMenu.menuElement) - 3);
	    }
	    //this.menuElement.style.padding = "0px";
	    UIContextMenu.menuElement.style.zIndex = 2000;
	    UIContextMenu.menuElement.style.top = top + "px";
	    UIContextMenu.menuElement.style.left = left + "px";

	    /* set display to block to be able to calculate offsetHeight */
	    gj(UIContextMenu.menuElement).css("display", "block");

	    if ((UIContextMenu.menuElement.offsetHeight + mousePos.y) > browserHeight)
	      UIContextMenu.menuElement.style.top = mousePos.y - UIContextMenu.menuElement.offsetHeight + 2 + "px";
	    if ((UIContextMenu.menuElement.offsetWidth + mousePos.x) > browserWidth)
	      UIContextMenu.menuElement.style.left = mousePos.x - UIContextMenu.menuElement.offsetWidth + 2 + "px";
	    UIContextMenu.menuElement.style.display = "none";
	    UIContextMenu.menuElement.style.visibility = "visible";
	  }
	  UIContextMenu.showHide(); 	
	},

	show : function(evt) {
		var _e = window.event || evt;
		UIContextMenu.attachedElement = UIContextMenu.getSource(_e) ;
		var menuPos = {
			"x": _e.pageX,
			"y": _e.pageY
		} ;
		var menuElementId = UIContextMenu.getMenuElementId() ;
		var currentPortlet = gj(UIContextMenu.attachedElement).parents('.' + UIContextMenu.portletCssClass)[0];
		if (menuElementId) {
			UIContextMenu.menuElement = gj(currentPortlet).find('#' + menuElementId)[0] ; //document.getElementById(menuElementId) ;
			DOMUtil.listHideElements(UIContextMenu.menuElement) ;
			DOMUtil.cleanUpHiddenElements();
			UIContextMenu.swapMenu(document.getElementById(menuElementId), menuPos,_e) ;
			if(!UIContextMenu.menuElement) 
				return false;
			UIContextMenu.menuElement.onmouseover = UIContextMenu.autoHide ;
			UIContextMenu.menuElement.onmouseout = UIContextMenu.autoHide ;		
			return false ;
		}
		return UIContextMenu.getReturnValue() ;
	}
}

return UIContextMenu;
})(DOMUtil, base, gj, uiRightClickPopupMenu);