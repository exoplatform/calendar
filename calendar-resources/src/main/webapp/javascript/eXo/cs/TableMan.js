/**
 * @author uocnb
 */

function TableMan() {
} ;

TableMan.prototype.init = function(rootE) {
  rootE = (typeof(rootE) == 'string') ? document.getElementById(rootE) : rootE ;
  if (!rootE) {
    return ;
  }
  if (rootE.tagName.toLowerCase() != 'table') {
    this.tableE = rootE.getElementsByTagName('table')[0] ;
  } else {
    this.tableE = rootE ;
  }
  this.tableData = this.toTableData(this.tableE) ;
} ;

/**
 * Collect all cell in table and put it in one array
 * 
 *  Array struct:
 *    [
 *      [0] : [header array]
 *      [1] : [cell array at row 0]
 *      [2] : [cell array at row 1]
 *    ]
 * 
 * @param {Element} tabE
 */
TableMan.prototype.toArray = function(tabE) {
  var tabArray = [] ;
  var trList = tabE.getElementsByTagName('tr') ;
  if (!trList || trList.length < 0) {
    return tabArray ;
  }
  var headerArray = tabE.getElementsByTagName('th') ;
  var cellArray = [] ;
  cellArray[0] = headerArray ;
  for (var i=0; i<trList.length; i++) {
    cellArray[i + 1] = trList[i].getElementsByTagName('td') ;
  } ;
  return cellArray ;
} ;

/**
 * Collect all cell in table and put it into a TableData Object
 * 
 *  TableData {
 *    header : [],
 *    data : [
 *      0 : [],
 *      1 : []
 *    ],
 *  }
 * 
 * @param {Element} tabE
 */
TableMan.prototype.toTableData = function(tabE) {
  var tabArray = [] ;
  var trList = tabE.getElementsByTagName('tr') ;
  if (!trList || trList.length < 0) {
    return tabArray ;
  }
  var headerArray = tabE.getElementsByTagName('th') ;
  var cellArray = [] ;
  for (var i=0; i<trList.length; i++) {
    var tdLst = trList[i].getElementsByTagName('td') ;
    if (tdLst.length > 0) {
      cellArray[cellArray.length] = tdLst ;
    }
  } ;
  return {header:headerArray, data:cellArray} ;
} ;

TableMan.prototype.serialize = function() {
  var cellLst = [] ;
  for (var i=0; i<this.tableData.data; i++) {
    var row = this.tableData.data[i] ;
    for (var j=0; j<row.length; j++) {
      cellLst[cellLst.length] = row[j] ;
    }    
  }
  return cellLst ;
} ;

/**
 * 
 * @return {Array}
 */
TableMan.prototype.getData = function() {
  return this.tableData.data ;
} ;

/**
 * 
 * @return {Array}
 */
TableMan.prototype.getHeader = function() {
  return this.tableData.header ;
} ;

/**
 * 
 * @param {Integer} i
 * 
 * @return {Array}
 */
TableMan.prototype.getRowAt = function(i) {
  return this.tableData.data[i] ;
} ;

/**
 * 
 * @param {Integer} y Row index
 * @param {Integer} x Column index
 * 
 * @return {Element}
 */
TableMan.prototype.getCellAt = function(x, y) {
  return (this.tableData.data[x])[y] ;
} ;

TableMan.prototype.getCol = function(cellIndex) {
  var data = this.tableData.data ;
  var cellNodes = [] ;
  for (var i=0; i<data.length; i++) {
    if (data[i][cellIndex]) {
      cellNodes[cellNodes.length] = data[i][cellIndex]
    }
  }
  return cellNodes ;
} ;

/**
 * 
 * @param {Element} cellNode Table Cell element such as td, th
 */
TableMan.prototype.cellIndexOf = function(cellNode) {
  var nodeLst = eXo.core.DOMUtil.findAncestorByTagName(cellNode, 'tr').getElementsByTagName('td') ;
  nodeLst = nodeLst.length > 0 ? nodeLst : eXo.core.DOMUtil.findAncestorByTagName(cellNode, 'tr').getElementsByTagName('th') ;
  for (var i=0; i<nodeLst.length; i++) {
    if (nodeLst[i] == cellNode) {
      return i ;
    }
  }
} ;

TableMan.prototype.swapColumn = function(x1, x2) {
  var header = this.getHeader() ;
  var data = this.getData() ;
  
  // Swap header
  this.swapElement(header[x1], header[x2]) ;  
  // Swap Cells in column
  for (var y=0; y<data.length; y++) {
    this.swapElement(this.getCellAt(y, x1), this.getCellAt(y, x2)) ;
  }
} ;

/**
 * 
 * @param {Element} e1
 * @param {Element} e2
 */
TableMan.prototype.swapElement = function(e1, e2) {
  var e1Clone = e1.cloneNode(true) ;
  e1Clone.onmousedown = e1.onmousedown ;
  var e2Clone = e2.cloneNode(true) ;
  e2Clone.onmousedown = e2.onmousedown ;
  
  e1.parentNode.insertBefore(e2Clone, e1) ;
  e1.parentNode.removeChild(e1) ;
  
  e2.parentNode.insertBefore(e1Clone, e2) ;
  e2.parentNode.removeChild(e2) ;
} ;

TableMan.prototype.cellBold = function() {
  var header = this.getHeader() ;
  for (var i=0; i<header.length; i++) {
    var headCol = header[i] ;
    headCol.style.fontWeight = 'bold' ;
  }
  var data = this.getData() ;
  for (var i=0; i<data.length; i++) {
    for (var j=0; j<data[i].length; j++) {
      var cell = data[i][j] ;
      cell.style.fontWeight = 'bold' ;
      cell.style.color = '#f00' ;
    }
  }
} ;


if (!eXo.cs) {
  eXo.cs = {} ;
}

eXo.cs.TableMan = new TableMan() ;