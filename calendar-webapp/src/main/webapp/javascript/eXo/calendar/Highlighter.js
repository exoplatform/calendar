(function(base, cs, gj){

function Highlighter() {

}
var _module = {};
eXo.calendar = eXo.calendar || {};
/**
 * Gets index of cell in time table in the month view
 * @param {Object} cell A cell in time table
 * @return Object contains two properties that are cellIndex and rowIndex
 */
Highlighter.prototype.getPos = function(cell) {
	return {
		"x" : cell.cellIndex,
		"y" : cell.parentNode.rowIndex
	}
} ;

/**
 * Checks mouse over in cell or not
 * @param {Object} cell A cell in time table
 * @param {Object} _e Mouse event
 * @return Boolean value
 */
Highlighter.prototype.isInCell = function(cell, _e) {
	var Highlighter = _module.Highlighter ;
	var cellX = base.Browser.findPosX(cell) - Highlighter.container.scrollLeft ;
	var cellY = cs.Browser.Browser.findPosY(cell) - Highlighter.container.scrollTop ;
	var mouseX = cs.Browser.Browser.findMouseXInPage(_e) ;
	var mouseY = cs.Browser.Browser.findMouseYInPage(_e) ;
	if(document.getElementById("UIPageDesktop")) {
		mouseX = cs.Browser.Browser.findMouseXInPage(_e) ;
		mouseY = cs.Browser.Browser.findMouseYInPage(_e) ;
		cellX = base.Browser.findPosX(cell)() - cs.CSUtils.Utils.getScrollLeft(cell) ;
		cellY = cs.Browser.Browser.Browser.findPosY(cell) - cs.CSUtils.Utils.getScrollTop(cell) ;
	}
	var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
	if(document.all && uiControlWorkspace && (!document.getElementById("UIPageDesktop") ||  base.Browser.isIE7())) cellX -= uiControlWorkspace.offsetWidth ;
	if (
		 (mouseX > cellX) && (mouseX < (cellX + cell.offsetWidth))
	&& (mouseY > cellY) && (mouseY < (cellY + cell.offsetHeight))
	) { return true ;}
	return false ;
} ;

/**
 * Gets position of mouse in index of cell
 * @param {Object} evt Mouse event
 */
Highlighter.prototype.getMousePos = function(evt) {
	var Highlighter = _module.Highlighter ;
	var _e = window.event || evt ;
	var cell = Highlighter.cell ;
	var len = cell.length ;
	for(var i = 0 ; i < len ; i ++) {
		if (Highlighter.isInCell(cell[i], _e)) {
			Highlighter.currentCell = cell[i] ;
			return Highlighter.getPos(Highlighter.currentCell) ;
		}
	}
} ;

/**
 * Hide all selection block
 */
Highlighter.prototype.hideAll = function() {
	var obj  = (arguments.length >0) ? arguments[0]: null ;
	var blocks = _module.Highlighter.block ;
	var len = blocks.length ;
	for(var i = 0 ; i < len ; i ++ ) {
		if ((obj != null) && (obj == blocks[i])) blocks[i].style.display  = "block" ;		
		else blocks[i].style.display  = "none" ;
	}
} ;

/**
 * Hides the selection blocks out of selected period of time
 * @param {Object} start Start time
 * @param {Object} end End time
 */
Highlighter.prototype.hideBlock = function(start,end) {
	var blocks = _module.Highlighter.block ;
	var len = blocks.length ;
	for(var i = 0 ; i < len ; i ++ ) {
		if ((i < start) || (i > end)) blocks[i].style.display  = "none" ;
	}
} ;

/**
 * Creates a selection block
 * @param {Object} cell A cell in time table
 */
Highlighter.prototype.createBlock = function(cell) {
	var table = gj(cell).parents("table")[0] ;
	var tr = gj(table).find("tr") ;
	var len = tr.length ;
	var div = null ;
	var block = new Array() ;
	for(var i = 0 ; i < len ; i ++) {
		div = document.createElement("div") ;
		gj(div).on('mousedown',_module.Highlighter.hideAll);
		if(gj("#UserSelectionBlock"+i)) 
			gj("#UserSelectionBlock"+i).remove() ; 
		div.setAttribute("id", "UserSelectionBlock"+i) ;
		div.className = "userSelectionBlock" ;
		table.parentNode.appendChild(div) ;
		block.push(div) ;
	}
	_module.Highlighter.block = block ;
} ;


Highlighter.prototype.reserveDirection = function(obj, container,targetObj,extra){
		var fixleftIE = (document.all && document.getElementById("UIWeekView"))? 6 : 0 ;
		var x = base.Browser.findPosXInContainer(obj,container)  - fixleftIE;
		if (extra) x += extra ;
		if(base.I18n.isRT()){
			x = (container.offsetWidth - obj.offsetWidth - x)  - 16;
			targetObj.style.right = x + "px";
		}
		targetObj.style.left = x + "px";
};
/**
 * Sets up dragging selection when mouse down on the month view
 * @param {Object} evt Mouse event
 */
Highlighter.prototype.start = function(evt) {
	try{		
	var Highlighter = _module.Highlighter ;
	var _e = window.event || evt ;
	if(_e.button == 2) return ;
	_e.cancelBubble = true ;
	Highlighter.startCell = this ;
	var table = gj(Highlighter.startCell).parents("table")[0] ;
	var callback = table.getAttribute("eXoCallback") ;
	if (callback) Highlighter.callback = callback ;
	Highlighter.cell = gj(table).find(Highlighter.startCell.tagName.toLowerCase() + ".uiCellBlock") ;
	Highlighter.cellLength = gj(Highlighter.startCell.parentNode).find(Highlighter.startCell.tagName.toLowerCase()).length; 
	Highlighter.dimension = {"x":(Highlighter.startCell.offsetWidth), "y":(Highlighter.startCell.offsetHeight)} ;
	var pos = Highlighter.getPos(Highlighter.startCell) ;
	Highlighter.createBlock(Highlighter.startCell) ;
	Highlighter.hideAll() ;
	Highlighter.startBlock = Highlighter.block[pos.y] ;
	Highlighter.startBlock.style.display = "block" ;
	Highlighter.container = Highlighter.startBlock.offsetParent ;
	var fixleftIE = (document.all && document.getElementById("UIWeekView"))? 6 : 0 ; //TODO : No hard code 
	var x = base.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) -  fixleftIE ;
	var y = base.Browser.findPosYInContainer(Highlighter.startCell, Highlighter.container) + gj('.uiMonthView').scrollTop();
	Highlighter.reserveDirection(Highlighter.startCell, Highlighter.container,Highlighter.startBlock) ;

    var rowContainerDay = gj('div.rowContainerDay')[0];
    /* if we have scrollbar then add scrollTop */
    if (rowContainerDay.scrollTop) {
        Highlighter.startBlock.style.top = (y + rowContainerDay.scrollTop) + "px" ;
    }
    else {
        Highlighter.startBlock.style.top = y + "px" ;
    }

	Highlighter.startBlock.style.width = Highlighter.dimension.x + "px" ;
	Highlighter.startBlock.style.height = Highlighter.dimension.y + "px" ;
	gj(document).on({'mousemove':Highlighter.execute, 'mouseup':Highlighter.end});
	Highlighter.firstCell = Highlighter.startCell ;
	Highlighter.lastCell = Highlighter.startCell ;
	} catch(e) {
	}
} ;

/**
 * Executes dragging selection
 * @param {Object} evt Mouse event
 */
Highlighter.prototype.execute = function(evt) {
	var Highlighter = _module.Highlighter ;
	var _e = window.event || evt ;	
	var sPos = Highlighter.getPos(Highlighter.startCell) ;
	var fixleftIE = (document.all && document.getElementById("UIWeekView"))? 6 : 0 ; //TODO : No hard code 
	try{
		var cPos = Highlighter.getMousePos(_e) ;
		var len = cPos.y - sPos.y ;	
		var startBlock = null ;
		var endBlock = null ;
		var startIndex = null ;
		var lastIndex = null ;
		var startX = null ;
		var startY = null ;
		var endX = null ;
		var startWidth = null ;
		if(len == 0) {
			var diff = cPos.x - sPos.x ;
			startBlock = Highlighter.startBlock ;
			if (diff > 0) {
				Highlighter.reserveDirection(Highlighter.startCell, Highlighter.container,startBlock) ;
				startBlock.style.width = (diff + 1)*Highlighter.dimension.x + "px" ;
				Highlighter.firstCell = Highlighter.startCell ;
				Highlighter.lastCell  = Highlighter.currentCell ;
			} else {
			 	Highlighter.reserveDirection(Highlighter.currentCell, Highlighter.container,startBlock);
				startBlock.style.width = (1 - diff)*Highlighter.dimension.x + "px" ;
			 	Highlighter.lastCell = Highlighter.startCell ;
				Highlighter.firstCell  = Highlighter.currentCell ;
			}
			
		} else {
			if (len > 0) {
				startIndex = sPos.y ;
				lastIndex = startIndex + len ;
				startBlock = Highlighter.startBlock ;
				endBlock = Highlighter.block[lastIndex] ;
				startX = base.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) ;
				startY = base.Browser.findPosYInContainer(Highlighter.startCell, Highlighter.container) ;
				endX = (cPos.x + 1)*Highlighter.dimension.x ;
				startWidth = (Highlighter.cellLength - sPos.x)*Highlighter.dimension.x ;
				Highlighter.firstCell = Highlighter.startCell ;
				Highlighter.lastCell  = Highlighter.currentCell ;
				Highlighter.reserveDirection(Highlighter.startCell, Highlighter.container,startBlock) ;
			} else {
				startIndex = sPos.y  + len ;
				lastIndex = sPos.y ;
				startBlock = Highlighter.block[startIndex] ;
				endBlock = Highlighter.block[lastIndex] ;
				startX = base.Browser.findPosXInContainer(Highlighter.currentCell, Highlighter.container) ;
				startY = base.Browser.findPosYInContainer(Highlighter.currentCell, Highlighter.container) ;
				endX = (sPos.x + 1)*Highlighter.dimension.x ;
				startWidth = (Highlighter.cellLength - cPos.x)*Highlighter.dimension.x ;
				Highlighter.lastCell = Highlighter.startCell ;
				Highlighter.firstCell  = Highlighter.currentCell ;
				Highlighter.reserveDirection(Highlighter.currentCell, Highlighter.container,startBlock) ;
			}
			startBlock.style.display = "block" ;
			startBlock.style.top = startY + gj('.rowContainerDay').scrollTop() + "px" ;
			startBlock.style.width = startWidth + "px" ;
			startBlock.style.height = Highlighter.dimension.y + "px" ;
			if(Math.abs(len) >= 1) {
				for(var i = startIndex + 1 ; i < (startIndex + Math.abs(len)); i ++) {
					Highlighter.block[i].style.display  = "block" ;
					Highlighter.block[i].style.top  = parseInt(Highlighter.block[i - 1].style.top) + Highlighter.dimension.y + "px" ;
					Highlighter.reserveDirection(Highlighter.cell[0], Highlighter.container,Highlighter.block[i]) ;
					Highlighter.block[i].style.width = Highlighter.cellLength*Highlighter.dimension.x + "px" ;
					Highlighter.block[i].style.height = Highlighter.dimension.y + "px" ;
				}
			}
			endBlock.style.display  = "block" ;
			endBlock.style.top  = parseInt(Highlighter.block[lastIndex - 1].style.top) + Highlighter.dimension.y + "px" ;
			Highlighter.reserveDirection(Highlighter.cell[0], Highlighter.container,endBlock) ;
			endBlock.style.width = endX + "px" ;
			endBlock.style.height = Highlighter.dimension.y + "px" ;
            Highlighter.hideBlock(startIndex, lastIndex) ;
		}
	} catch(e){
			window.status = e.message ;
	}			
} ;

/**
 * Ends dragging selection, this method clean up some unused properties and execute callback function
 * @param {Object} evt Mouse event
 */
Highlighter.prototype.end = function(evt) {
	var Highlighter = _module.Highlighter;
	if (Highlighter.callback) {
		eval(Highlighter.callback) ;
		_module.Highlighter.hideAll();		
	}
	gj(document).off("mousemove mouseup");
    if (Highlighter.startBlock) {
        Highlighter.startBlock.style.display = "none";
    }
} ;

Highlighter.prototype.setCallback = function(str) {
	this.container.setAttribute("eXoCallback",str) ;
} ;

_module.Highlighter = new Highlighter() ;
eXo.calendar.Highlighter = _module.Highlighter;

/**
 * Class control horizontal dragging selection in the month view
 * @author <a href="mailto:dung14000@gmail.com">Hoang Manh Dung</a>
 * @constructor
 */
function UIHSelection() {
} ;

/**
 * Checks mouse over in cell or not
 * @param {Object} cell A cell in the dragging table
 * @param {Object} _e Mouse event
 * @return Boolean value
 */
UIHSelection.prototype.isInCell = function(cell, _e) {
	var UIHSelection = eXo.calendar.UIHSelection ;
	var cellX = base.Browser.findPosX(cell) - UIHSelection.container.scrollLeft ;
	var cellY = cs.Browser.Browser.findPosY(cell) - UIHSelection.container.scrollTop ;
	var mouseX = cs.Browser.Browser.findMouseXInPage(_e) ;
	var mouseY = cs.Browser.Browser.findMouseYInPage(_e) ;
	if(document.getElementById("UIPageDesktop")) {
		mouseX = cs.Browser.Browser.findMouseXInPage(_e) ;
		mouseY = cs.Browser.Browser.findMouseYInPage(_e) ;
		cellX = base.Browser.findPosX(cell) - cs.CSUtils.Utils.getScrollLeft(cell) ;
		cellY = cs.Browser.Browser.findPosY(cell) - cs.CSUtils.Utils.getScrollTop(cell) ;
	}
	var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
	if(document.all && uiControlWorkspace && (!document.getElementById("UIPageDesktop") || base.Browser.isIE7())) cellX -= uiControlWorkspace.offsetWidth ;
	if (
		 (mouseX > cellX) && (mouseX < (cellX + cell.offsetWidth))
	&& (mouseY > cellY) && (mouseY < (cellY + cell.offsetHeight))
	) { return true ;}
	return false ;
} ;

/**
 * Gets cellIndex of cell
 * @param {Object} evt Mouse event
 * return cellIndex of current cell
 */
UIHSelection.prototype.getCurrentIndex = function(evt){
	var cells = eXo.calendar.UIHSelection.cells ;
	var len = cells.length ;
	for(var i = 0 ; i < len ; i++) {
		var isCell = eXo.calendar.UIHSelection.isInCell(cells[i],evt) ;
		if(isCell) return cells[i].cellIndex ;
	}
} ;

/**
 * Sets attribute for cells
 * @param {Object} sIndex Start cellIndex
 * @param {Object} eIndex End cellIndex
 * @param {Object} cells A cell in time table
 */
UIHSelection.prototype.setAttr = function(sIndex, eIndex, cells){
    for(var i = sIndex; i <= eIndex ; i++) {
	if(gj(cells[i]).hasClass("busyTime"))
	    gj(cells[i]).addClass("busySelected");
	else 
	    gj(cells[i]).addClass("userSelection") ;
    }
} ;

/**
 * Removes attribute for cellsD:\java\eXoProjects\git-project\calendar\calendar-webapp\src\main\webapp\javascript\eXo\calendar
 * @param {Object} sIndex Start cellIndex
 * @param {Object} eIndex End cellIndex
 * @param {Object} cells A cell in time table
 */
UIHSelection.prototype.removeAttr = function(sIndex, eIndex, cells){
    var len = cells.length ;
    for(var i = 0; i < len ; i++) {
	if((i>=sIndex) && (i<=eIndex)) continue ;
	gj(cells[i]).removeClass("userSelection") ;
	gj(cells[i]).removeClass("busySelected") ;
    }
} ;

/**
 * Sets all attribute for cells
 */
UIHSelection.prototype.removeAllAttr = function(){
    var cells = this.cells ;
    var len = cells.length ;
    for(var i = 0; i < len ; i++) {
	gj(cells[i]).removeClass("userSelection") ;
	gj(cells[i]).removeClass("busySelected") ;
    }
} ;

/**
 * Sets up horizontal dragging selection when mouse down on time table
 * @param {Object} evt Mouse event
 */
UIHSelection.prototype.start = function(){
	var UIHSelection = eXo.calendar.UIHSelection ;
	var table = gj(this).parents("table")[0] ;
	var callback = table.getAttribute("eXoCallback") ;
	if (callback) 
	UIHSelection.callback = callback ;
	UIHSelection.startIndex = this.cellIndex ;
	UIHSelection.cells = gj(this.parentNode).children("td") ;
	UIHSelection.container = this.parentNode ;
	UIHSelection.removeAllAttr() ;
	gj(this).addClass("userSelection") ;
	gj(document).on({'mousemove':UIHSelection.execute,'mouseup':UIHSelection.end});
	UIHSelection.firstCell = UIHSelection.cells[UIHSelection.startIndex] ;
	UIHSelection.lastCell = UIHSelection.cells[UIHSelection.startIndex] ;
} ;

/**
 * Executes horizontal dragging selection
 */
UIHSelection.prototype.execute = function(evt){
	var _e = window.event || evt ;
	_e.preventDefault();
	var UIHSelection = eXo.calendar.UIHSelection ;
	var sIndex = UIHSelection.startIndex ;
	var eIndex = UIHSelection.getCurrentIndex(_e) ;
	var cells = UIHSelection.cells ;
	if(eIndex) {
		if (eIndex < sIndex) {
			UIHSelection.setAttr(eIndex, sIndex, cells) ;
			UIHSelection.removeAttr(eIndex, sIndex, cells) ;
			UIHSelection.firstCell = cells[eIndex] ;
			UIHSelection.lastCell = cells[sIndex] ;
		}else {
			UIHSelection.setAttr(sIndex, eIndex, cells) ;
			UIHSelection.removeAttr(sIndex, eIndex, cells) ;
			UIHSelection.firstCell = cells[sIndex] ;
			UIHSelection.lastCell = cells[eIndex] ;
		}
	}
} ;

/**
 * Ends horizontal dragging selection, this method clean up some unused properties and execute callback function
 */
UIHSelection.prototype.end = function(){
	var UIHSelection = eXo.calendar.UIHSelection ;
	UIHSelection.removeAllAttr() ;
	UIHSelection.startIndex = null ;
	UIHSelection.endIndex = null ;
	UIHSelection.cells = null ;
	UIHSelection.container = null ;
	gj(document).off("mousemove mouseup")
	if (UIHSelection.callback) eval(UIHSelection.callback) ;
} ;

_module.UIHSelection = new UIHSelection() ;
eXo.calendar.UIHSelection = _module.UIHSelection;
return _module;

})(base, cs, gj);
