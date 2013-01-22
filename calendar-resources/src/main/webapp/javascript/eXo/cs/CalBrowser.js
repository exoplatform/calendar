(function(){
eXo.cs = eXo.cs || {};
var _module = {} ;
function MouseObject() {
  this.init(null) ;
} ;

MouseObject.prototype.init = function(mouseEvent) {
  this.mousexInPage = null ;
  this.mouseyInPage = null ;

  this.lastMousexInPage = null ;
  this.lastMouseyInPage = null ;

  this.mousexInClient = null ;
  this.mouseyInClient = null ;

  this.lastMousexInClient = null ;
  this.lastMouseyInClient = null ;

  this.deltax = null ;
  this.deltay = null ;
  if(mouseEvent != null) this.update(mouseEvent) ;
} ;

MouseObject.prototype.update = function(mouseEvent) {
  var  x = _module.Browser.findMouseXInPage(mouseEvent) ;
  var  y = _module.Browser.findMouseYInPage(mouseEvent) ;

  this.lastMousexInPage =  this.mousexInPage != null ? this.mousexInPage : x ;
  this.lastMouseyInPage =  this.mouseyInPage != null ? this.mouseyInPage : y ;

  this.mousexInPage = x ;
  this.mouseyInPage = y ;

  x  =  _module.Browser.findMouseXInClient(mouseEvent) ;
  y  =  _module.Browser.findMouseYInClient(mouseEvent) ;

  this.lastMousexInClient =  this.mousexInClient != null ? this.mousexInClient : x ;
  this.lastMouseyInClient =  this.mouseyInClient != null ? this.mouseyInClient : y ;

  this.mousexInClient = x ;
  this.mouseyInClient = y ;

  this.deltax = this.mousexInClient - this.lastMousexInClient ;
  this.deltay = this.mouseyInClient - this.lastMouseyInClient ;
} ;

function Browser() {
	
};
/**
 * Returns the vertical position of an object relative to the window
 */
Browser.prototype.findPosY = function(obj) {
  var curtop = 0 ;
  while (obj) {
    curtop += obj.offsetTop ;
    obj = obj.offsetParent ;
  }
  return curtop ;
} ;

Browser.prototype.findMouseXInClient = function(e) {
  if (!e) e = window.event ;
  return e.clientX ;
} ;

Browser.prototype.findMouseYInClient = function(e) {
  if (!e) e = window.event ;
  return e.clientY ;
} ;

/**
 * find the x position of the mouse in the page
 */
Browser.prototype.findMouseXInPage = function(e) {
  var posx = -1 ;
  if (!e) e = window.event ;
  if (e.pageX || e.pageY) {
    posx = e.pageX ;
  } else if (e.clientX || e.clientY) {
    posx = e.clientX + document.body.scrollLeft ;
  }
  return posx ;
} ;
/**
 * find the y position of the mouse in the page
 */
Browser.prototype.findMouseYInPage = function(e) {
  var posy = -1 ;
  if (!e) e = window.event ;
  if (e.pageY) {
    posy = e.pageY ;
  } else if (e.clientX || e.clientY) {
    //IE 6
    if (document.documentElement && document.documentElement.scrollTop) {
      posy = e.clientY + document.documentElement.scrollTop ;
    } else {
      posy = e.clientY + document.body.scrollTop ;
    }
  }
  return  posy ;
} ;

Browser.prototype.getEventSource = function(e) {
	var targ;
	if (e.target) targ = e.target;
	else if (e.srcElement) targ = e.srcElement;
	if (targ.nodeType == 3) // defeat Safari bug
		targ = targ.parentNode;
	return targ;
}
/************************************************************************************/
//eXo.cs.Browser = new Browser() ;
//eXo.core.Mouse = new MouseObject() ;
_module.Browser = new Browser() ;
_module.Mouse = new MouseObject() ;

return _module;
})();