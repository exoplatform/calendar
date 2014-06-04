(function(base, CSUtils, gj) {

var Highlighter = {
  /**
   * Gets index of cell in time table in the month view
   * @param {Object} cell A cell in time table
   * @return Object contains two properties that are cellIndex and rowIndex
   */
  getPos : function(cell) {
    return {
      "x" : cell.cellIndex,
      "y" : cell.parentNode.rowIndex
    }
  },

  /**
   * Checks mouse over in cell or not
   * @param {Object} cell A cell in time table
   * @param {Object} _e Mouse eventg
   * @return Boolean value
   */
  isInCell : function(cell, _e) {
    var cellX = base.Browser.findPosX(cell) - Highlighter.container.scrollLeft ;
    var cellY = gj(cell).offset().top - Highlighter.container.scrollTop ;
    var mouseX = _e.pageX;
    var mouseY = _e.pageY;
    if(document.getElementById("UIPageDesktop")) {
      cellX = base.Browser.findPosX(cell)() - CSUtils.getScrollLeft(cell) ;
      cellY = gj(cell).offset(top) - CSUtils.getScrollTop(cell) ;
    }
    var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
    if(document.all && uiControlWorkspace && (!document.getElementById("UIPageDesktop") ||  base.Browser.isIE7())) cellX -= uiControlWorkspace.offsetWidth ;
    if (
       (mouseX > cellX) && (mouseX < (cellX + cell.offsetWidth))
    && (mouseY > cellY) && (mouseY < (cellY + cell.offsetHeight))
    ) { return true ;}
    return false ;
  },

  /**
   * Gets position of mouse in index of cell
   * @param {Object} evt Mouse event
   */
  getMousePos : function(evt) {
    var _e = window.event || evt ;
    var cell = Highlighter.cell ;
    var len = cell.length ;
    for(var i = 0 ; i < len ; i ++) {
      if (Highlighter.isInCell(cell[i], _e)) {
        Highlighter.currentCell = cell[i] ;
        return Highlighter.getPos(Highlighter.currentCell) ;
      }
    }
  },

  /**
   * Hide all selection block
   */
  hideAll : function() {
    var obj  = (arguments.length >0) ? arguments[0]: null ;
    var blocks = Highlighter.block ;
    var len = blocks.length ;
    for(var i = 0 ; i < len ; i ++ ) {
      if ((obj != null) && (obj == blocks[i])) blocks[i].style.display  = "block" ;   
      else blocks[i].style.display  = "none" ;
    }
  },

  /**
   * Hides the selection blocks out of selected period of time
   * @param {Object} start Start time
   * @param {Object} end End time
   */
  hideBlock : function(start,end) {
    var blocks = Highlighter.block ;
    var len = blocks.length ;
    for(var i = 0 ; i < len ; i ++ ) {
      if ((i < start) || (i > end)) blocks[i].style.display  = "none" ;
    }
  },

  /**
   * Creates a selection block
   * @param {Object} cell A cell in time table
   */
  createBlock : function(cell) {
    var table = gj(cell).parents("table")[0] ;
    var tr = gj(table).find("tr") ;
    var len = tr.length ;
    var div = null ;
    var block = new Array() ;
    for(var i = 0 ; i < len ; i ++) {
      div = document.createElement("div") ;
      gj(div).on('mousedown',Highlighter.hideAll);
      if(gj("#UserSelectionBlock"+i)) 
        gj("#UserSelectionBlock"+i).remove() ; 
      div.setAttribute("id", "UserSelectionBlock"+i) ;
      div.className = "userSelectionBlock" ;
      table.parentNode.appendChild(div) ;
      block.push(div) ;
    }
    Highlighter.block = block ;
  },

  reserveDirection : function(obj, container,targetObj,extra) {
      var fixleftIE = (document.all && document.getElementById("UIWeekView"))? 6 : 0 ;
      var x = base.Browser.findPosXInContainer(obj,container)  - fixleftIE;
      if (extra) x += extra ;
      if(base.I18n.isRT()){
        x = (container.offsetWidth - obj.offsetWidth - x)  - 16;
        targetObj.style.right = x + "px";
      }
      targetObj.style.left = x + "px";
  },
  
  /**
   * Sets up dragging selection when mouse down on the month view
   * @param {Object} evt Mouse event
   */
  start : function(evt) {
    try {    
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
  },

  /**
   * Executes dragging selection
   * @param {Object} evt Mouse event
   */
  execute : function(evt) {
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
    } catch(e) {
        window.status = e.message ;
    }     
  },

  /**
   * Ends dragging selection, this method clean up some unused properties and execute callback function
   * @param {Object} evt Mouse event
   */
  end : function(evt) {
    if (Highlighter.callback) {
      eval(Highlighter.callback) ;
      Highlighter.hideAll();    
    }
    gj(document).off("mousemove mouseup");
      if (Highlighter.startBlock) {
          Highlighter.startBlock.style.display = "none";
      }
  },

  setCallback : function(str) {
    this.container.setAttribute("eXoCallback",str) ;
  }
};

return Highlighter;
})(base, CSUtils, gj);