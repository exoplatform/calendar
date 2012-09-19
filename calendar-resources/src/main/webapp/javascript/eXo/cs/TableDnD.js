/**
 * @author uocnb
 */
function TableDnD() {
  this.scKey = 'border' ;
  this.scValue = 'solid 1px #000' ;
  this.DragDrop = _module.DragDrop ;
  this.tableMan = _module.TableMan ;
  this.dropableLst = [] ;                                      
} ;

TableDnD.prototype.init = function(root){
  this.root = (typeof(root) == 'string') ? document.getElementById(root) : root;
  this.tableMan.init(this.root) ;
  if (!this.tableMan.tableData || 
      !this.tableMan.tableData.data || 
      this.tableMan.tableData.data.length <= 0) {
    return ;
  }
  this.dropableLst = this.tableMan.getHeader() ;
  var dragableLst = this.tableMan.getHeader() ; 
  for (var i=0; i<dragableLst.length; i++) {
    dragableLst[i].onmousedown = this.dndTrigger ;
    dragableLst[i].style['cursor'] = 'pointer' ;
  }
} 

TableDnD.prototype.dndTrigger = function(e) {
  e = e ? e : window.event ;
  var srcE = _module.Browser.getEventSource(e) ;
  if ((srcE.tagName + '') != 'th' && (srcE.onclick)) {
    return true ;
  }
  return _module.TableDnD.initDnD(_module.TableDnD.dropableLst, this, this, e) ;
} ;

TableDnD.prototype.initDnD = function(dropableObjs, clickObj, dragObj, e) {
  var clickBlock = (clickObj && clickObj.tagName) ? clickObj : document.getElementById(clickObj) ;
  var dragBlock = (dragObj && dragObj.tagName) ? dragObj : document.getElementById(dragObj) ;
//  debugger;
  var tableNode = document.createElement('table') ;
  var tableWidth = clickBlock.offsetWidth ;
  with (tableNode.style)  {
    display = 'none' ;
    position = 'absolute' ;
    top = '0px' ;
    left = '0px' ;
    background = '#fff' ;
    opacity = '0.8' ;
    filter = 'alpha(opacity=80)' ;
    width = tableWidth + 'px' ;
  }
  var headerRow = document.createElement('tr') ;
  var tableData = this.tableMan.tableData ;
  var cellIndex = this.tableMan.cellIndexOf(clickBlock) ;
  headerRow.appendChild(tableData.header[cellIndex].cloneNode(true)) ;
  tableNode.appendChild(headerRow) ;
  var colNodes = this.tableMan.getCol(cellIndex) ;
//  for (var i=0; i<colNodes.length; i++) {
//    var rowNode = document.createElement('tr') ;
//    rowNode.appendChild(colNodes[i].cloneNode(true)) ;
//    tableNode.appendChild(rowNode) ;
//  }
  
  document.body.appendChild(tableNode) ;
  
  this.DragDrop.initCallback = this.initCallback ;
  this.DragDrop.dragCallback = this.dragCallback ;
  this.DragDrop.dropCallback = this.dropCallback ;
  this.DragDrop.init(dropableObjs, clickBlock, tableNode, e) ;
  return false ;
} ;

TableDnD.prototype.synDragObjectPos = function(dndEvent) {
  if (!dndEvent.backupMouseEvent) {
    dndEvent.backupMouseEvent = window.event ;
    if (!dndEvent.backupMouseEvent) {
      return ;
    }
  }
  var dragObject = dndEvent.dragObject ;
  var mouseX = _module.Browser.findMouseXInPage(dndEvent.backupMouseEvent) + 1 ;
  var mouseY = base.Browser.findPosYInContainer(dndEvent.clickObject, document.body)  ;
  dragObject.style.top = mouseY + 'px' ;
  dragObject.style.left = mouseX + 'px' ;
} ;

TableDnD.prototype.initCallback = function(dndEvent) {
  _module.TableDnD.synDragObjectPos(dndEvent) ;
} ;

TableDnD.prototype.dragCallback = function(dndEvent) {
  var dragObject = dndEvent.dragObject ;
  if (dragObject.style['display'] || dragObject.style['display'] != 'none') {
    dragObject.style['display'] = 'table' ;
  }
  _module.TableDnD.synDragObjectPos(dndEvent) ;
  if (dndEvent.foundTargetObject) {
    if (this.foundTargetObjectCatch != dndEvent.foundTargetObject) {
      if(this.foundTargetObjectCatch) {
        this.foundTargetObjectCatch.style[_module.TableDnD.scKey] = this.foundTargetObjectCatchStyle ;
      }
      this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
      this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style[_module.TableDnD.scKey] ;
      this.foundTargetObjectCatch.style[_module.TableDnD.scKey] = _module.TableDnD.scValue ;
    }
  } else {
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style[_module.TableDnD.scKey] = this.foundTargetObjectCatchStyle ;
    }
    this.foundTargetObjectCatch = null ;
  }
} ;

TableDnD.prototype.dropCallback = function(dndEvent) {
  if (dndEvent.dragObject) {
    dndEvent.dragObject.parentNode.removeChild(dndEvent.dragObject) ;
  }
  try {
    
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style[_module.TableDnD.scKey] = this.foundTargetObjectCatchStyle ;
    }
    this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
    if (this.foundTargetObjectCatch) {
      var tableMan = _module.TableMan ;    
      var x1 = tableMan.cellIndexOf(this.foundTargetObjectCatch) ;
      var x2 = tableMan.cellIndexOf(dndEvent.clickObject) ;
      tableMan.swapColumn(x1, x2) ;
    }
  } catch(e) {}
  _module.DragDrop.destroy() ;
  return true ;
} ;


if (!eXo.cs) {
  eXo.cs = {} ;
}

//eXo.cs.TableDnD = new TableDnD();
_module.TableDnD = new TableDnD();