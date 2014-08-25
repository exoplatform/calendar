(function(gj) {

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

/********************* Utility function for CS ******************/
var Utils = {
    showHidePane : function(clickobj, beforeobj, afterobj) {
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
    },

    getKeynum : function(event) {
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
    },

    captureInput : function(input, action) {
      if(typeof(input) == "string") input = document.getElementById(input) ;
      input.form.onsubmit = Utils.cancelSubmit ;
      input.onkeypress= Utils.onEnter ;
    },

    onEnter : function(evt) {
      var _e = evt || window.event ;
      _e.cancelBubble = true ;
      var keynum = Utils.getKeynum(_e) ;
      if (keynum == 13) {
        Utils.doAction(this);
      }
    },

    doAction : function(obj) {
      var uiSeachForm = gj(obj).parents(".UIForm")[0];
      var actionNode = gj(uiSeachForm).find(".uiSearchInput")[0];
      var nodeName = String(actionNode.nodeName).toLowerCase();
      switch(nodeName) {
        case "a": gj.globalEval(actionNode.href);break;
        case "div": gj(actionNode).click();break;
        default:gj(actionNode).click(); 
      }
    },

    cancelSubmit : function() {
      return false ;
    },

    confirmAction : function(obj,msg,parentId) {
      // msg: Please check at least one event or task.
      // parent: UIListContainer 
      // obj : UIHeaderBar_3
      var cont = gj(obj).parents('#' + parentId)[0];
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
    },

    swapClass : function(obj,hoverClass) {
      if(gj(obj).hasClass(hoverClass)) 
        gj(obj).toggleClass(hoverClass);
      else 
        gj(obj).addClass(hoverClass);
    },
    /**
     * Gets scrollTop property of DOM element
     * @param {Object} obj DOM element
     * @return scrollTop of element
     */
    getScrollTop : function(obj) {
        var curtop = 0;
        while (obj) {
            if (obj.scrollTop) 
                curtop += obj.scrollTop;
            obj = obj.parentNode;
        }
        return curtop;
    },

    createUrl : function(href,params) {
      if(params != null) {
        var len = params.length ;
        for(var i = 0 ; i < len ; i++) {
          href += "&" +  params[i].name + "=" + params[i].value ;
        }
      }
      href += "&ajaxRequest=true";
      href = href.replace("&op=","&formOp=");
      return href;
    },

    /**
     * Gets scrollLeft property of DOM element
     * @param {Object} obj DOM element
     * @return scrollLeft of element
     */
    getScrollLeft : function(obj) {
        var curleft = 0;
        while (obj) {
            if (obj.scrollLeft) 
                curleft += obj.scrollLeft;
            obj = obj.parentNode;
        }
        return curleft;
    },
    /*  This work is licensed under Creative Commons GNU LGPL License.

      License: http://creativecommons.org/licenses/LGPL/2.1/
       Version: 0.9
      Author:  Stefan Goessner/2006
      Web:     http://goessner.net/ 
    */
    json2xml : function(o, tab) {
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
    },

    /*  This work is licensed under Creative Commons GNU LGPL License.

      License: http://creativecommons.org/licenses/LGPL/2.1/
       Version: 0.9
      Author:  Stefan Goessner/2006
      Web:     http://goessner.net/ 
    */
    xml2json : function(xml, tab) {
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
    },

    getScrollbarWidth : function() {
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
    },

    getElementWidth : function(obj) {
      var w = 0;
      if(obj.style.display == "none") {
        obj.style.display = "block";
        w = obj.offsetWidth;
        obj.style.display = "none";
        return w;
      }
      return obj.offsetWidth;
    },

    /**
     * TODO: remove this method when portal remove Cometd.js file
     */ 
    loadPlatformCometd : function() {
      //if(eXo.cs.CSCometd) return;
      //if(eXo.core.Cometd) delete eXo.core.Cometd;
      //eXo.require("eXo.core.Cometd","/cometd/javascript/");
      //eXo.cs.CSCometd = eXo.core.Cometd;
      if(_module.CSCometd) return;  
      _module.CSCometd = cometd;
    },
    
    /**
     * Get all event element
     * @param {Object} viewer DOM element contains all calendar events
     * @return All event from container
     */
    getElements : function(viewer) {
      var className = (arguments.length > 1) ? arguments[1] : "eventContainerBorder";
      var elements = gj(viewer).find("div." + className);
      var len = elements.length;
      var elems = new Array();
      for (var i = 0; i < len; i++) {
        if (elements[i].style.display != "none") {
          elements[i].style.left = "0%";
          elements[i].style.zIndex = 1;
          elems.push(elements[i]);
        }
      }
      return elems;
    },
    
    /**
     * Resets z-Index of DOM element when drag and drop calendar event
     * @param {Object} obj DOM element
     */
    resetZIndex : function(obj) {
      try {
        var maxZIndex = parseInt(obj.style.zIndex);
        var items = gj(obj.parentNode).children("div");
        var len = items.length;
        for (var i = 0; i < len; i++) {
          if (!items[i].style.zIndex)
            items[i].style.zIndex = 1;
          if (parseInt(items[i].style.zIndex) > maxZIndex) {
            maxZIndex = parseInt(items[i].style.zIndex);
          }
        }
        obj.style.zIndex = maxZIndex + 1;
      }
      catch (e) {
      }
    },
    
    /**
     * Sorts calendar event by their attribute
     * @param {Object} obj An array of calendar events
     * @param {Object} attribute A attribute to sort
     * @return An sorted array of calendar event
     */
    sortByAttribute : function(obj, attribute) {
      var len = obj.length;
      var tmp = null;
      var attribute1 = null;
      var attribute2 = null;
      for (var i = 0; i < len; i++) {
        for (var j = i + 1; j < len; j++) {
          attribute1 = parseInt(obj[i].getAttribute(attribute));
          attribute2 = parseInt(obj[j].getAttribute(attribute));
          if (attribute2 < attribute1) {
            tmp = obj[i];
            obj[i] = obj[j];
            obj[j] = tmp;
          }
          if (attribute2 == attribute1) {
            var end1 = parseInt(obj[i].getAttribute("endTime"));
            var end2 = parseInt(obj[j].getAttribute("endTime"));
            if (end2 > end1) {
              tmp = obj[i];
              obj[i] = obj[j];
              obj[j] = tmp;
            }
          }
        }
      }
      return obj;
    },
    
    attachSwapClass : function(compId,className,hoverClass) {
      var component = document.getElementById(compId);
      var items = gj(component).find('div.' + className);
      var i = items.length;
      while(i--){
          gj(items[i]).on({'mouseover':function(){
            Utils.swapClass(this,hoverClass);
          },
              'mouseout':function(){
                Utils.swapClass(this,hoverClass);
              }});
      };
  },
  
  makeRequest : function(url,callback) {
      gj.ajax({
          type: "get",
          url: url,
          cache: false,
          success: function(data, status, jqXHR) {
              if (callback) {
                  callback(jqXHR);
              }
          }
      });
  },
  
  /**
   * Ceiling round number
   * @param {Object} number Original number
   * @param {Object} dividend Divided end
   * @return rounded number
   */
  ceil : function(number, dividend) {
      var mod = number % dividend;
      if (mod != 0)
          number += dividend - mod;
      return number;
  }
};

return Utils;
})(gj);