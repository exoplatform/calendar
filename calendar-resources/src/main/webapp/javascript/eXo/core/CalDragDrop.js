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
  eXo.core.Mouse.init(evt) ;
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
	if(evt.keyCode == 27) cs.DragDrop.onDrop(evt) ;
}

DragDrop.prototype.onMouseMove = function(evt) {
  eXo.core.Mouse.update(evt) ;
	var dndEvent = cs.DragDrop.dndEvent ;
  dndEvent.backupMouseEvent = evt ;
	var dragObject =  dndEvent.dragObject ;

	var y = parseInt(dragObject.style.top) ;
	var x = base.I18n.isRT() ? parseInt(dragObject.style.right) : parseInt(dragObject.style.left) ;

	if(base.I18n.isLT()) dragObject.style["left"] =  x + eXo.core.Mouse.deltax + "px" ;
	else dragObject.style["right"] =  x - eXo.core.Mouse.deltax + "px" ;
	dragObject.style["top"]  =  y + eXo.core.Mouse.deltay + "px" ;
	
  if(cs.DragDrop.dragCallback != null) {
    var foundTarget = cs.DragDrop.findDropableTarget(dndEvent, cs.DragDrop.dropableTargets, evt) ;
    var junkMove =  cs.DragDrop.isJunkMove(dragObject, foundTarget) ;
    dndEvent.update(foundTarget, junkMove) ;
    cs.DragDrop.dragCallback(dndEvent) ;
  }
    
	return false ;
} ;

DragDrop.prototype.onDrop = function(evt) {
  if(!evt) evt = window.event ;
  /* should not remove this or move this line to  destroy since the onMouseMove method keep calling */
  if(cs.DragDrop.dropCallback != null) {
    var dndEvent = cs.DragDrop.dndEvent ;
    dndEvent.backupMouseEvent = evt ;
    var dragObject = dndEvent.dragObject ;

    var foundTarget = cs.DragDrop.findDropableTarget(dndEvent, cs.DragDrop.dropableTargets, evt) ;
    var junkMove =  cs.DragDrop.isJunkMove(dragObject, foundTarget) ;

    dndEvent.update(foundTarget, junkMove) ;
    cs.DragDrop.dropCallback (dndEvent) ;
  }
  cs.DragDrop.destroy() ;
} ;

DragDrop.prototype.onCancel = function(evt) {
	if(cs.DragDrop.cancelCallback) cs.DragDrop.cancelCallback(cs.DragDrop.dndEvent);
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
  var mousexInPage = cs.Browser.findMouseXInPage(mouseEvent) ;
  var mouseyInPage = cs.Browser.findMouseYInPage(mouseEvent) ;
  
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
        if(_module.DOMUtil.hasAncestor(ele, foundTarget)) {
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
  var componentTop = cs.Browser.findPosY(component) ;
  var componentBottom = componentTop + component.offsetHeight ;
  var isOver = false ;

  if((componentLeft < x) && (x < componentRight)) {
    if((componentTop < y) && (y < componentBottom)) {
      isOver = true ;
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
	
//eXo.cs.DragDrop = new DragDrop() ;

_module.DragDrop = new DragDrop() ;