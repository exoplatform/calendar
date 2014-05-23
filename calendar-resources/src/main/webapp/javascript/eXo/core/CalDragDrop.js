(function(base, gj) {
var _module = {};

function MouseObject() {
  this.init(null) ;
};

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
  var  x = mouseEvent.pageX;
  var  y = mouseEvent.pageY;

  this.lastMousexInPage =  this.mousexInPage != null ? this.mousexInPage : x ;
  this.lastMouseyInPage =  this.mouseyInPage != null ? this.mouseyInPage : y ;

  this.mousexInPage = x ;
  this.mouseyInPage = y ;

  x  =  mouseEvent.clientX;
  y  =  mouseEvent.clientY;

  this.lastMousexInClient =  this.mousexInClient != null ? this.mousexInClient : x ;
  this.lastMouseyInClient =  this.mouseyInClient != null ? this.mouseyInClient : y ;

  this.mousexInClient = x ;
  this.mouseyInClient = y ;

  this.deltax = this.mousexInClient - this.lastMousexInClient ;
  this.deltay = this.mouseyInClient - this.lastMouseyInClient ;
};

function DragDropEvent(clickObject, dragObject) {
  this.clickObject = clickObject ;
  if (dragObject && dragObject != null) {
 		this.dragObject = dragObject ;
  } else {
  	this.dragObject = clickObject ;
  }
  this.foundTargetObject = null ;
  this.lastFoundTargetObject = null ;
  this.junkMove = false ;
  
 	if(base.I18n.isLT() && isNaN(parseInt(this.dragObject.style.left))) this.dragObject.style.left = "0px" ;
 	if(base.I18n.isRT() && isNaN(parseInt(this.dragObject.style.right))) this.dragObject.style.right = "0px" ;
	if(isNaN(parseInt(this.dragObject.style.top))) this.dragObject.style.top = "0px" ;
} ;

DragDropEvent.prototype.update = function(foundTargetObject, junkMove) {
  this.lastFoundTargetObject = this.foundTargetObject ;
  this.foundTargetObject = foundTargetObject ;
  this.junkMove = junkMove ;
}

DragDropEvent.prototype.isJunkMove = function() {
  return this.junkMove ;
} ;

/*************************************************************************************/

function DragDrop() {
  this.dropableTargets = null ;
  this.dndEvent = null ;

  this.initCallback = null ;
  this.dragCallback = null ;
  this.dropCallback = null ;
  this.destroyCallback = null ;
  this.isJunkMoveCallback = null ;
} ;

DragDrop.prototype.init = function(dropableTargets, clickObject, dragObject, evt) {
	if(evt && evt.preventDefault) evt.preventDefault();
	_module.Mouse.init(evt) ;
  this.dropableTargets = dropableTargets ;
  
  var dndEvent = this.dndEvent = new DragDropEvent(clickObject, dragObject) ;
	document.onmousemove	= this.onMouseMove ;
	document.onmouseup		= this.onDrop ;
	document.onmouseout = this.onCancel ;
	document.onkeypress = this.onKeyPressEvt ;
	
  if(this.initCallback != null) {
    this.initCallback(dndEvent) ;
  }
} ;

DragDrop.prototype.onKeyPressEvt = function(evt) {
	if(!evt) evt = window.event ;
	if(evt.keyCode == 27) _module.DragDrop.onDrop(evt) ;
}

DragDrop.prototype.onMouseMove = function(evt) {
	_module.Mouse.update(evt) ;
	var dndEvent = _module.DragDrop.dndEvent ;
  dndEvent.backupMouseEvent = evt ;
	var dragObject =  dndEvent.dragObject ;

	var y = parseInt(dragObject.style.top) ;
	var x = base.I18n.isRT() ? parseInt(dragObject.style.right) : parseInt(dragObject.style.left) ;

	if(base.I18n.isLT()) dragObject.style["left"] =  x + _module.Mouse.deltax + "px" ;
	else dragObject.style["right"] =  x - _module.Mouse.deltax + "px" ;
	dragObject.style["top"]  =  y + _module.Mouse.deltay + "px" ;
	
  if(_module.DragDrop.dragCallback != null) {
    var foundTarget = _module.DragDrop.findDropableTarget(dndEvent, _module.DragDrop.dropableTargets, evt) ;
    var junkMove =  _module.DragDrop.isJunkMove(dragObject, foundTarget) ;
    dndEvent.update(foundTarget, junkMove) ;
    _module.DragDrop.dragCallback(dndEvent) ;
  }
    
	return false ;
} ;

DragDrop.prototype.onDrop = function(evt) {
  if(!evt) evt = window.event ;
  /* should not remove this or move this line to  destroy since the onMouseMove method keep calling */
  if(_module.DragDrop.dropCallback != null) {
    var dndEvent = _module.DragDrop.dndEvent ;
    dndEvent.backupMouseEvent = evt ;
    var dragObject = dndEvent.dragObject ;

    var foundTarget = _module.DragDrop.findDropableTarget(dndEvent, _module.DragDrop.dropableTargets, evt) ;
    var junkMove =  _module.DragDrop.isJunkMove(dragObject, foundTarget) ;

    dndEvent.update(foundTarget, junkMove) ;
    _module.DragDrop.dropCallback (dndEvent) ;
  }
  _module.DragDrop.destroy() ;
} ;

DragDrop.prototype.onCancel = function(evt) {
	if(_module.DragDrop.cancelCallback) _module.DragDrop.cancelCallback(_module.DragDrop.dndEvent);
} ;

DragDrop.prototype.destroy = function() {
  if(this.destroyCallback != null) {
    this.destroyCallback(this.dndEvent) ;
  }

	document.onmousemove	= null ;
  document.onmouseup = null ;
  document.onmouseout = null ;
  document.onkeypress = null ;

  this.dndEvent = null ;
  this.dropableTargets = null ;

  this.initCallback = null ;
  this.dragCallback = null ;
  this.dropCallback = null ;
  this.destroyCallback = null ;
  this.isJunkMoveCallback = null ;
} ;
  
DragDrop.prototype.findDropableTarget = function(dndEvent, dropableTargets, mouseEvent) {
  if(dropableTargets == null) return null ;
  var mousexInPage = mouseEvent.pageX;
  var mouseyInPage = mouseEvent.pageY;
  
	var clickObject = dndEvent.clickObject ;
	var dragObject = dndEvent.dragObject ;
  var foundTarget = null ;
  var len = dropableTargets.length ;
  for(var i = 0 ; i < len ; i++) {
    var ele =  dropableTargets[i] ;

    if(dragObject != ele && this.isIn(mousexInPage, mouseyInPage, ele)) {
      if(foundTarget == null) {
        foundTarget = ele ;
      } else {
        if(gj(ele).closest(foundTarget)) {
          foundTarget = ele ;
        }
      } 
    }
  }
 	
  return foundTarget ;
} ;
  
DragDrop.prototype.isIn = function(x, y, component) {
  var componentLeft = base.Browser.findPosX(component);
  var componentRight = componentLeft + component.offsetWidth ;
  var componentTop = gj(component).offset().top;
  var componentBottom = componentTop + component.offsetHeight ;
  var isOver = false ;

  if((componentLeft < x) && (x < componentRight)) {
    if((componentTop < y) && (y < componentBottom)) {
      isOver = true;
    }
  }
  return isOver ;
} ;

DragDrop.prototype.isJunkMove = function(src, target) {
  if(this.isJunkMoveCallback != null) {
    return this.isJunkMoveCallback(src, target) ;
  }
  if(target == null) return true ;
  return false ;
} ;

_module.Mouse = new MouseObject() ;
_module.DragDrop = new DragDrop() ;
return _module.DragDrop;
})(base, gj);