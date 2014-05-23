(function(base, gj){
var _module = {};
eXo = eXo || {}
eXo.cs = eXo.cs || {} ;

/*!
 * Cross-Browser Split 1.1.1
 * Copyright 2007-2012 Steven Levithan <stevenlevithan.com>
 * Available under the MIT License
 * ECMAScript compliant, uniform cross-browser split method
 */

var split;

// Avoid running twice; that would break the `nativeSplit` reference
split = split || function (undef) {

    var nativeSplit = String.prototype.split,
        compliantExecNpcg = /()??/.exec("")[1] === undef, // NPCG: nonparticipating capturing group
        self;

    self = function (str, separator, limit) {
        // If `separator` is not a regex, use `nativeSplit`
        if (Object.prototype.toString.call(separator) !== "[object RegExp]") {
            return nativeSplit.call(str, separator, limit);
        }
        var output = [],
            flags = (separator.ignoreCase ? "i" : "") +
                    (separator.multiline  ? "m" : "") +
                    (separator.extended   ? "x" : "") + // Proposed for ES6
                    (separator.sticky     ? "y" : ""), // Firefox 3+
            lastLastIndex = 0,
            // Make `global` and avoid `lastIndex` issues by working with a copy
            separator = new RegExp(separator.source, flags + "g"),
            separator2, match, lastIndex, lastLength;
        str += ""; // Type-convert
        if (!compliantExecNpcg) {
            // Doesn't need flags gy, but they don't hurt
            separator2 = new RegExp("^" + separator.source + "$(?!\\s)", flags);
        }
        /* Values for `limit`, per the spec:
         * If undefined: 4294967295 // Math.pow(2, 32) - 1
         * If 0, Infinity, or NaN: 0
         * If positive number: limit = Math.floor(limit); if (limit > 4294967295) limit -= 4294967296;
         * If negative number: 4294967296 - Math.floor(Math.abs(limit))
         * If other: Type-convert, then use the above rules
         */
        limit = limit === undef ?
            -1 >>> 0 : // Math.pow(2, 32) - 1
            limit >>> 0; // ToUint32(limit)
        while (match = separator.exec(str)) {
            // `separator.lastIndex` is not reliable cross-browser
            lastIndex = match.index + match[0].length;
            if (lastIndex > lastLastIndex) {
                output.push(str.slice(lastLastIndex, match.index));
                // Fix browsers whose `exec` methods don't consistently return `undefined` for
                // nonparticipating capturing groups
                if (!compliantExecNpcg && match.length > 1) {
                    match[0].replace(separator2, function () {
                        for (var i = 1; i < arguments.length - 2; i++) {
                            if (arguments[i] === undef) {
                                match[i] = undef;
                            }
                        }
                    });
                }
                if (match.length > 1 && match.index < str.length) {
                    Array.prototype.push.apply(output, match.slice(1));
                }
                lastLength = match[0].length;
                lastLastIndex = lastIndex;
                if (output.length >= limit) {
                    break;
                }
            }
            if (separator.lastIndex === match.index) {
                separator.lastIndex++; // Avoid an infinite loop
            }
        }
        if (lastLastIndex === str.length) {
            if (lastLength || !separator.test("")) {
                output.push("");
            }
        } else {
            output.push(str.slice(lastLastIndex));
        }
        return output.length > limit ? output.slice(0, limit) : output;
    };

    // For convenience
    String.prototype.split = function (separator, limit) {
        return self(this, separator, limit);
    };

    return self;

}();
/********************* Checkbox Manager ******************/
function CheckBoxManager() {
} ;

CheckBoxManager.prototype.init = function(cont) {
	if(typeof(cont) == "string") 
		cont = document.getElementById(cont) ;
	var checkboxes = gj(cont).find('input.checkbox'); 
	if(checkboxes.length <=0) return ;
	checkboxes[0].onclick = this.checkAll ;
	var len = checkboxes.length ;
	for(var i = 1 ; i < len ; i ++) {
		checkboxes[i].onclick = this.check ;
	}
} ;

CheckBoxManager.prototype.checkAll = function() {
	_module.CheckBox.checkAllItem(this);
} ;

CheckBoxManager.prototype.getItems = function(obj) {
  var table = gj(obj).parents('table')[0];
	var checkboxes = gj(table).find('input.checkbox');
	return checkboxes ;
} ;

CheckBoxManager.prototype.check = function() {
	_module.CheckBox.checkItem(this);
} ;

CheckBoxManager.prototype.checkAllItem = function(obj){
	var checked = obj.checked ;
	var items = _module.CheckBox.getItems(obj) ;
	var len = items.length ;
	for (var i = 1 ; i < len ; i ++) {
    if (items[i].disabled === false) /* only check if checkbox enabled */
    {
		  items[i].checked = checked ;
		  this.highlight(items[i],checked);
    }
	}	
} ;

CheckBoxManager.prototype.checkItem = function(obj){  

	var checkboxes = _module.CheckBox.getItems(obj);
	var len = checkboxes.length; 

  var state = true;
	if (!obj.checked) {
	  checkboxes[0].checked = false;
	}
	else {
	  for (var i = 1; i < len; i++) {
		  state = state && checkboxes[i].checked;
		}
	  checkboxes[0].checked = state;
	}

  this.highlight(obj,obj.checked);
} ;

CheckBoxManager.prototype.highlight = function(obj,isChecked){
	obj = gj(obj).parents('tr')[0];
	if(!obj) return ;
	if(isChecked) 
		gj(obj).addClass("UIHightLight");
	else 
		gj(obj).toggleClass("UIHightLight","");
} ;

//eXo.cs.CheckBox = new CheckBoxManager() ;
_module.CheckBox = new CheckBoxManager() ;

/********************* Pane Spliter ******************/

function LayoutSpliter() {
} ;

/**
 * 
 * @param {Object} e : Event Object
 * @param {Object} markerobj : Click object
 * This function to resize pane
 */
LayoutSpliter.prototype.doResize = function(e , markerobj) {
  _e = (window.event) ? window.event : e ;
  this.posY = _e.pageY;// browser undefined
  var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
  var container = marker.parentNode ;
  var areas = gj(container).find('div.spliterResizableListArea'); 
  if((areas.length < 2) || (areas[0].style.display=="none")) return ;
  this.beforeArea = areas[0] ;
  this.afterArea = areas[1] ;
  this.beforeArea.style.overflowY = "auto" ;
  this.afterArea.style.overflowY = "auto" ;
  this.beforeY = this.beforeArea.offsetHeight ;
  this.afterY = this.afterArea.offsetHeight ;
  document.onmousemove = _module.Spliter.adjustHeight ;
  document.onmouseup = _module.Spliter.clear ;
} ;

LayoutSpliter.prototype.adjustHeight = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var Spliter = _module.Spliter ;
  var delta = evt.pageY - Spliter.posY ;
  var afterHeight = Spliter.afterY - delta ;
  var beforeHeight = Spliter.beforeY + delta ;
  if (beforeHeight <= 0  || afterHeight <= 0) return ;
  Spliter.beforeArea.style.height =  beforeHeight + "px" ;
  if(Spliter.afterY > 0) Spliter.afterArea.style.height =  afterHeight + "px" ;
} ;



LayoutSpliter.prototype.clear = function() {
  try {
    var Spliter = _module.Spliter ;
    document.onmousemove = null ;
    delete Spliter.beforeY ;
    delete Spliter.afterY ;
    delete Spliter.beforeArea ;
    delete Spliter.afterArea ;
    delete Spliter.posY ;
  } catch(e) {window.statuts = "Message : " + e.message ;} ;
} ;

_module.Spliter = new LayoutSpliter() ;

/********************* Utility function for CS ******************/

function Utils() {}

Utils.prototype.showHidePane = function(clickobj, beforeobj, afterobj) {
  var container = gj(clickobj).parents('.SpliterContainer')[0]; 
  var areas = gj(container).find('div.spliterResizableListArea'); 
  var uiGrid = gj(areas[1]).find('table.UIGrid')[0]; 
  var uiPreview = gj(areas[1]).parents(".UIPreview")[0] ;
  if(areas.length < 2) return ;
	if(areas[0].style.display != "none") {
		clickobj.className = "MinimizeButton";
    areas[1].style.height = (areas[1].offsetHeight  + areas[0].offsetHeight - 4) + "px" ;
		areas[0].style.display = "none" ;
	} else {
		areas[0].style.display = "block" ;
		clickobj.className = "MaximizeButton";
    areas[1].style.height = (areas[1].offsetHeight - areas[0].offsetHeight + 4 ) + "px" ;
	}
} ;

Utils.prototype.getKeynum = function(event) {
  var keynum = false ;
  if(window.event) { /* IE */
    keynum = window.event.keyCode;
    event = window.event ;
  } else if(event.which) { /* Netscape/Firefox/Opera */
    keynum = event.which ;
  }
  if(keynum == 0) {
    keynum = event.keyCode ;
  }
  return keynum ;
} ;

Utils.prototype.captureInput = function(input, action) {
  if(typeof(input) == "string") input = document.getElementById(input) ;
	input.form.onsubmit = _module.Utils.cancelSubmit ;
  input.onkeypress= _module.Utils.onEnter ;
} ;

Utils.prototype.onEnter = function(evt) {
  var _e = evt || window.event ;
  _e.cancelBubble = true ;
  var keynum = _module.Utils.getKeynum(_e) ;
  if (keynum == 13) {
		_module.Utils.doAction(this);
  }
} ;

Utils.prototype.doAction = function(obj){
	var uiSeachForm = gj(obj).parents(".UIForm")[0];
	var actionNode = gj(uiSeachForm).find(".uiSearchInput")[0];
	var nodeName = String(actionNode.nodeName).toLowerCase();
	switch(nodeName){
		case "a":	gj.globalEval(actionNode.href);break;
		case "div": gj(actionNode).click();break;
		default:gj(actionNode).click(); 
	}
};

Utils.prototype.getElementByClass = function(parentNode,clazz){
	var nodeList = parentNode.getElementsByTagName("*");
	var i = nodeList.length;
	while(i--){
		if(gj(nodeList[i]).hasClass(clazz)) 
			return nodeList[i];
	}
};

Utils.prototype.cancelSubmit = function() {
  return false ;
} ;

Utils.prototype.confirmAction = function(obj,msg,parentId){
  // msg: Please check at least one event or task.
  // parent: UIListContainer 
  // obj : UIHeaderBar_3
  var	cont = gj(obj).parents('#' + parentId)[0];
  var eventBoxClass = (gj(cont).find("form#UIListView").length > 0) ? ".uiListViewRow" : ".eventBoxes";
	var checkboxes = gj(cont).find("input.checkbox");
	var i = checkboxes.length;
	var actionLink = obj.getAttribute("actionLink");
  var isEditable;
	var check = false ;
	var n = 1;
	if(parentId == "UICalendarViewContainer") n = 0;

  /* loop through the checkboxes - one checkbox = 1 selected event */
  while (i>n) {
		i--;
    if (checkboxes[i].checked) {
      var eventBoxes = gj(checkboxes[i]).parents(eventBoxClass)[0];
      isEditable = eventBoxes.getAttribute("isEditable");

      /* check permission of user to event */
      if (isEditable && (isEditable === "true")) {
        check = true;
			  break ;
      }
		}
	}

  if (check) gj.globalEval(actionLink);
  else {
    var alertDialog = gj("#bts_alert");
    gj("#bts_alert").show();
    gj("#bts_alert").children("strong").html(msg);
    gj("#bts_alert").children("#bts_close").click(function() { gj(this).parent().hide(); }  );
  }
};

Utils.prototype.swapClass = function(obj,hoverClass){
	if(gj(obj).hasClass(hoverClass)) 
		gj(obj).toggleClass(hoverClass);
	else 
		gj(obj).addClass(hoverClass);
};
/**
 * Gets scrollTop property of DOM element
 * @param {Object} obj DOM element
 * @return scrollTop of element
 */
Utils.prototype.getScrollTop = function(obj){
    var curtop = 0;
    while (obj) {
        if (obj.scrollTop) 
            curtop += obj.scrollTop;
        obj = obj.parentNode;
    }
    return curtop;
};

Utils.prototype.createUrl = function(href,params){
	if(params != null) {
		var len = params.length ;
		for(var i = 0 ; i < len ; i++) {
			href += "&" +  params[i].name + "=" + params[i].value ;
		}
	}
	href += "&ajaxRequest=true";
	href = href.replace("&op=","&formOp=");
	return href;
};

/**
 * Gets scrollLeft property of DOM element
 * @param {Object} obj DOM element
 * @return scrollLeft of element
 */
Utils.prototype.getScrollLeft = function(obj){
    var curleft = 0;
    while (obj) {
        if (obj.scrollLeft) 
            curleft += obj.scrollLeft;
        obj = obj.parentNode;
    }
    return curleft;
};
/*	This work is licensed under Creative Commons GNU LGPL License.

	License: http://creativecommons.org/licenses/LGPL/2.1/
   Version: 0.9
	Author:  Stefan Goessner/2006
	Web:     http://goessner.net/ 
*/
Utils.prototype.json2xml = function(o, tab) {
   var toXml = function(v, name, ind) {
      var xml = "";
      if (v instanceof Array) {
         for (var i=0, n=v.length; i<n; i++)
            xml += ind + toXml(v[i], name, ind+"\t") + "\n";
      }
      else if (typeof(v) == "object") {
         var hasChild = false;
         xml += ind + "<" + name;
         for (var m in v) {
            if (m.charAt(0) == "@")
               xml += " " + m.substr(1) + "=\"" + v[m].toString() + "\"";
            else
               hasChild = true;
         }
         xml += hasChild ? ">" : "/>";
         if (hasChild) {
            for (var m in v) {
               if (m == "#text") {
                  xml += v[m];
	       }
               else if (m == "#cdata") {
                  xml += "<![CDATA[" + v[m] + "]]>";
	       }
               else if (m.charAt(0) != "@") {
                  xml += toXml(v[m], m, ind+"\t");
	       }
            }
            xml += (xml.charAt(xml.length-1)=="\n"?ind:"") + "</" + name + ">";
         }
      }
      else {
         xml += ind + "<" + name + ">" + v.toString() +  "</" + name + ">";
      }
      return xml;
   }, xml="";
   for (var m in o)
      xml += toXml(o[m], m, "");
   return tab ? xml.replace(/\t/g, tab) : xml.replace(/\t|\n/g, "");
}

/*	This work is licensed under Creative Commons GNU LGPL License.

	License: http://creativecommons.org/licenses/LGPL/2.1/
   Version: 0.9
	Author:  Stefan Goessner/2006
	Web:     http://goessner.net/ 
*/
Utils.prototype.xml2json = function(xml, tab) {
   var X = {
      toObj: function(xml) {
         var o = {};
         if (xml.nodeType==1) {   // element node ..
            if (xml.attributes.length)   // element with attributes  ..
               for (var i=0; i<xml.attributes.length; i++)
                  o["@"+xml.attributes[i].nodeName] = (xml.attributes[i].nodeValue||"").toString();
            if (xml.firstChild) { // element has child nodes ..
               var textChild=0, cdataChild=0, hasElementChild=false;
               for (var n=xml.firstChild; n; n=n.nextSibling) {
                  if (n.nodeType==1) hasElementChild = true;
                  else if (n.nodeType==3 && n.nodeValue.match(/[^ \f\n\r\t\v]/)) textChild++; // non-whitespace text
                  else if (n.nodeType==4) cdataChild++; // cdata section node
               }
               if (hasElementChild) {
                  if (textChild < 2 && cdataChild < 2) { // structured element with evtl. a single text or/and cdata node ..
                     X.removeWhite(xml);
                     for (var n=xml.firstChild; n; n=n.nextSibling) {
                        if (n.nodeType == 3)  // text node
                           o["#text"] = X.escape(n.nodeValue);
                        else if (n.nodeType == 4)  // cdata node
                           o["#cdata"] = X.escape(n.nodeValue);
                        else if (o[n.nodeName]) {  // multiple occurence of element ..
                           if (o[n.nodeName] instanceof Array)
                              o[n.nodeName][o[n.nodeName].length] = X.toObj(n);
                           else
                              o[n.nodeName] = [o[n.nodeName], X.toObj(n)];
                        }
                        else  // first occurence of element..
                           o[n.nodeName] = X.toObj(n);
                     }
                  }
                  else { // mixed content
                     if (!xml.attributes.length)
                        o = X.escape(X.innerXml(xml));
                     else
                        o["#text"] = X.escape(X.innerXml(xml));
                  }
               }
               else if (textChild) { // pure text
                  if (!xml.attributes.length)
                     o = X.escape(X.innerXml(xml));
                  else
                     o["#text"] = X.escape(X.innerXml(xml));
               }
               else if (cdataChild) { // cdata
                  if (cdataChild > 1)
                     o = X.escape(X.innerXml(xml));
                  else
                     for (var n=xml.firstChild; n; n=n.nextSibling)
                        o["#cdata"] = X.escape(n.nodeValue);
               }
            }
            if (!xml.attributes.length && !xml.firstChild) o = null;
         }
         else if (xml.nodeType==9) { // document.node
            o = X.toObj(xml.documentElement);
         }
         else
            alert("unhandled node type: " + xml.nodeType);
         return o;
      },
      toJson: function(o, name, ind) {
         var json = name ? ("\""+name+"\"") : "";
         if (o instanceof Array) {
            for (var i=0,n=o.length; i<n; i++)
               o[i] = X.toJson(o[i], "", ind+"\t");
            json += (name?":[":"[") + (o.length > 1 ? ("\n"+ind+"\t"+o.join(",\n"+ind+"\t")+"\n"+ind) : o.join("")) + "]";
         }
         else if (o == null)
            json += (name&&":") + "null";
         else if (typeof(o) == "object") {
            var arr = [];
            for (var m in o)
               arr[arr.length] = X.toJson(o[m], m, ind+"\t");
            json += (name?":{":"{") + (arr.length > 1 ? ("\n"+ind+"\t"+arr.join(",\n"+ind+"\t")+"\n"+ind) : arr.join("")) + "}";
         }
         else if (typeof(o) == "string")
            json += (name&&":") + "\"" + o.toString() + "\"";
         else
            json += (name&&":") + o.toString();
         return json;
      },
      innerXml: function(node) {
         var s = ""
         if ("innerHTML" in node)
            s = node.innerHTML;
         else {
            var asXml = function(n) {
               var s = "";
               if (n.nodeType == 1) {
                  s += "<" + n.nodeName;
                  for (var i=0; i<n.attributes.length;i++)
                     s += " " + n.attributes[i].nodeName + "=\"" + (n.attributes[i].nodeValue||"").toString() + "\"";
                  if (n.firstChild) {
                     s += ">";
                     for (var c=n.firstChild; c; c=c.nextSibling)
                        s += asXml(c);
                     s += "</"+n.nodeName+">";
                  }
                  else
                     s += "/>";
               }
               else if (n.nodeType == 3)
                  s += n.nodeValue;
               else if (n.nodeType == 4)
                  s += "<![CDATA[" + n.nodeValue + "]]>";
               return s;
            };
            for (var c=node.firstChild; c; c=c.nextSibling)
               s += asXml(c);
         }
         return s;
      },
      escape: function(txt) {
         return txt.replace(/[\\]/g, "\\\\")
                   .replace(/[\"]/g, '\\"')
                   .replace(/[\n]/g, '\\n')
                   .replace(/[\r]/g, '\\r');
      },
      removeWhite: function(e) {
         e.normalize();
         for (var n = e.firstChild; n; ) {
            if (n.nodeType == 3) {  // text node
               if (!n.nodeValue.match(/[^ \f\n\r\t\v]/)) { // pure whitespace text node
                  var nxt = n.nextSibling;
                  e.removeChild(n);
                  n = nxt;
               }
               else
                  n = n.nextSibling;
            }
            else if (n.nodeType == 1) {  // element node
               X.removeWhite(n);
               n = n.nextSibling;
            }
            else                      // any other node
               n = n.nextSibling;
         }
         return e;
      }
   };
   if (xml.nodeType == 9) // document node
      xml = xml.documentElement;
   var json = X.toJson(X.toObj(X.removeWhite(xml)), xml.nodeName, "\t");
   return "{\n" + tab + (tab ? json.replace(/\t/g, tab) : json.replace(/\t|\n/g, "")) + "\n}";
};

Utils.prototype.getScrollbarWidth = function() {
	var inner = document.createElement("p");
	inner.style.width = "100%";
	inner.style.height = "200px";
	
	var outer = document.createElement("div");
	outer.style.position = "absolute";
	outer.style.top = "0px";
	outer.style.left = "0px";
	outer.style.visibility = "hidden";
	outer.style.width = "200px";
	outer.style.height = "150px";
	outer.style.overflow = "hidden";
	outer.appendChild (inner);
	
	document.body.appendChild (outer);
	var w1 = inner.offsetWidth;
	outer.style.overflow = "scroll";
	var w2 = inner.offsetWidth;
	if (w1 == w2) w2 = outer.clientWidth;
	
	document.body.removeChild (outer);
	
	return (w1 - w2);
};

Utils.prototype.getElementWidth = function(obj){
	var w = 0;
	if(obj.style.display == "none") {
		obj.style.display = "block";
		w = obj.offsetWidth;
		obj.style.display = "none"
		return w;
	}
	return obj.offsetWidth;
};

/**
 * TODO: remove this method when portal remove Cometd.js file
 */ 

Utils.prototype.loadPlatformCometd = function(){
	//if(eXo.cs.CSCometd) return;
	//if(eXo.core.Cometd) delete eXo.core.Cometd;
	//eXo.require("eXo.core.Cometd","/cometd/javascript/");
	//eXo.cs.CSCometd = eXo.core.Cometd;
	if(_module.CSCometd) return;	
	_module.CSCometd = cometd;
}

_module.Utils = new Utils() ;
/**
 * TODO: remove method call when portal remove Cometd.js file
 */

/********************* Event Manager ******************/

function EventManager(){
	
}


EventManager.prototype.removeEvent = function( obj, type, fn ) {
  if ( obj.detachEvent ) {
    obj.detachEvent( 'on'+type, obj[type+fn] );
    obj[type+fn] = null;
  } else
    obj.removeEventListener( type, fn, false );
};

EventManager.prototype.getMouseButton = function(evt) {
	var evt = evt || window.event;
	return evt.button ;
};

EventManager.prototype.getEventTarget = function(evt){
	var evt = evt || window.event;
	var target = evt.target || evt.srcElement;
	if (target.nodeType == 3) { // check textNode
		target = target.parentNode; 
	}
	return target; 
};

EventManager.prototype.getEventTargetByClass = function(evt, className){
	var target = this.getEventTarget(evt);
	if (gj(target).hasClass(className))
		return target ;
	else
		return gj(target).parents('.' + className)[0] ;
};

EventManager.prototype.getEventTargetByTagName = function(evt, tagName){
	var target = this.getEventTarget(evt);
	if (target.tagName.toLowerCase() == gj.trim(tagName))
		return target ;
	else
		return gj(target).parents(tagName)[0] ;
};

EventManager.prototype.cancelBubble = function(evt) {
  if(gj.browser.msie != undefined)
    window.event.cancelBubble = true ;
  else 
    evt.stopPropagation() ;		  
};

EventManager.prototype.cancelEvent = function(evt) {
	_module.EventManager.cancelBubble(evt) ;
  if(gj.browser.msie != undefined)
    window.event.returnValue = true ;
  else
    evt.preventDefault() ;
};

_module.EventManager = new EventManager() ;

/********************* Scroll Manager ******************/

function UINavigation() {
  this.scrollManagerLoaded = false ;
} ;

UINavigation.prototype.loadScroll = function() {
  var uiNav = _module.UINavigation ;
  var container = document.getElementById("uiActionBar") ;
  if(container) {    
    this.scrollMgr = eXo.portal.UIPortalControl.newScrollManager("uiActionBar") ;
    this.scrollMgr.initFunction = uiNav.iniScroll ;
    
    this.scrollMgr.mainContainer = gj(container).find('div.CenterBar')[0];
    this.scrollMgr.arrowsContainer = gj(container).find('div.ScrollButtons')[0];
    this.scrollMgr.loadElements("ControlButton", true) ;
    
    var button = gj(this.scrollMgr.arrowsContainer).find("div");
    if(button.length >= 2) {    
      this.scrollMgr.initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
      this.scrollMgr.initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
    }
    
    this.scrollMgr.callback = uiNav.scrollCallback ;
    uiNav.scrollManagerLoaded = true;
    uiNav.initScroll() ;
  }
} ;

UINavigation.prototype.initScroll = function() {
  var uiNav = _module.UINavigation ;
  if(!uiNav.scrollManagerLoaded) uiNav.loadScroll() ;
  var elements = uiNav.scrollMgr.elements ;
  uiNav.scrollMgr.init() ;
  uiNav.scrollMgr.checkAvailableSpace() ;
  uiNav.scrollMgr.renderElements() ;
} ;

UINavigation.prototype.scrollCallback = function() {

} ;

//eXo.cs.UINavigation = new UINavigation() ;
_module.UINavigation = new UINavigation() ;

function LayoutManager(id){
	this.layoutId = id ;
}

LayoutManager.prototype.check = function(){
	var layoutcookie = base.Browser.getCookie(this.layoutId) ;	
	var i = layoutcookie.length ;
	while(i--){
		if(!this.layouts[parseInt(layoutcookie.charAt(i))-1]) continue ;
		this.layouts[parseInt(layoutcookie.charAt(i))-1].style.display = "none";
	}
	if(this.callback) this.callback(layoutcookie) ;
};

LayoutManager.prototype.switchLayout = function(layout){
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

LayoutManager.prototype.setValue = function(value, str){
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

LayoutManager.prototype.reset = function(){
	var i = this.layouts.length ;
	while(i--){
		if(this.layouts[i]) this.layouts[i].style.display = "block";
	}
	base.Browser.setCookie(this.layoutId,"",1);
	if(this.resetCallback) this.resetCallback() ;
};

//eXo.cs.UINavigation = new UINavigation() ;
_module.LayoutManager = function(id){
	return new LayoutManager(id);
}

/*
 * Date Format 1.2.2
 * (c) 2007-2008 Steven Levithan <stevenlevithan.com>
 * MIT license
 * Includes enhancements by Scott Trenda <scott.trenda.net> and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */
function DateTimeFormatter(){
};
DateTimeFormatter.prototype.masks = {
	"default":      "ddd mmm dd yyyy HH:MM:ss",
	shortDate:      "m/d/yy",
	mediumDate:     "mmm d, yyyy",
	longDate:       "mmmm d, yyyy",
	fullDate:       "dddd, mmmm d, yyyy",
	shortTime:      "hh:MM TT",
	mediumTime:     "hh:MM:ss TT",
	longTime:       "hh:MM:ss TT Z",
	isoDate:        "yyyy-mm-dd",
	isoTime:        "HH:MM:ss",
	isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
	isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
};
DateTimeFormatter.prototype.token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g;
DateTimeFormatter.prototype.timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g;
DateTimeFormatter.prototype.timezoneClip = /[^-+\dA-Z]/g;
DateTimeFormatter.prototype.pad = function(val, len) {
	val = String(val);
	len = len || 2;
	while (val.length < len) val = "0" + val;
	return val;
};

DateTimeFormatter.prototype.i18n = {
	dayNames: [
		"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
		"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
	],
	monthNames: [
		"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	]
};

DateTimeFormatter.prototype.format = function (date, mask, utc) {
	var dF = _module.DateTimeFormatter;

	// You can't provide utc if you skip other args (use the "UTC:" mask prefix)
	if (arguments.length == 1 && (typeof date == "string" || date instanceof String) && !/\d/.test(date)) {
		mask = date;
		date = undefined;
	}

	// Passing date through Date applies Date.parse, if necessary
	date = date ? new Date(date) : new Date();
	if (isNaN(date)) throw new SyntaxError("invalid date");

	mask = String(dF.masks[mask] || mask || dF.masks["default"]);

	// Allow setting the utc argument via the mask
	if (mask.slice(0, 4) == "UTC:") {
		mask = mask.slice(4);
		utc = true;
	}

	var	_ = utc ? "getUTC" : "get",
		d = date[_ + "Date"](),
		D = date[_ + "Day"](),
		m = date[_ + "Month"](),
		y = date[_ + "FullYear"](),
		H = date[_ + "Hours"](),
		M = date[_ + "Minutes"](),
		s = date[_ + "Seconds"](),
		L = date[_ + "Milliseconds"](),
		o = utc ? 0 : date.getTimezoneOffset(),
		flags = {
			d:    d,
			dd:   dF.pad(d),
			ddd:  dF.i18n.dayNames[D],
			dddd: dF.i18n.dayNames[D + 7],
			m:    m + 1,
			mm:   dF.pad(m + 1),
			mmm:  dF.i18n.monthNames[m],
			mmmm: dF.i18n.monthNames[m + 12],
			yy:   String(y).slice(2),
			yyyy: y,
			h:    H % 12 || 12,
			hh:   dF.pad(H % 12 || 12),
			H:    H,
			HH:   dF.pad(H),
			M:    M,
			MM:   dF.pad(M),
			s:    s,
			ss:   dF.pad(s),
			l:    dF.pad(L, 3),
			L:    dF.pad(L > 99 ? Math.round(L / 10) : L),
			t:    H < 12 ? "a"  : "p",
			tt:   H < 12 ? "am" : "pm",
			T:    H < 12 ? "A"  : "P",
			TT:   H < 12 ? "AM" : "PM",
			Z:    utc ? "UTC" : (String(date).match(dF.timezone) || [""]).pop().replace(dF.timezoneClip, ""),
			o:    (o > 0 ? "-" : "+") + dF.pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
			S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
		};

	return mask.replace(dF.token, function ($0) {
		return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
	});
};

_module.DateTimeFormatter = new DateTimeFormatter();
return _module;
})(base, gj);
