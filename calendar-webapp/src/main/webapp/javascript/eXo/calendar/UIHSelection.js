(function(base, CSUtils, gj) {
/**
 * Class control horizontal dragging selection in the month view
 * @author <a href="mailto:dung14000@gmail.com">Hoang Manh Dung</a>
 * @constructor
 */
var UIHSelection = {
  /**
   * Checks mouse over in cell or not
   * @param {Object} cell A cell in the dragging table
   * @param {Object} _e Mouse event
   * @return Boolean value
   */
  isInCell : function(cell, _e) {
    var cellX = base.Browser.findPosX(cell) - UIHSelection.container.scrollLeft ;
    var cellY = gj(cell).offset().top - UIHSelection.container.scrollTop ;
    var mouseX = _e.pageX;
    var mouseY = _e.pageY;
    if(document.getElementById("UIPageDesktop")) {
      cellX = base.Browser.findPosX(cell) - CSUtils.getScrollLeft(cell) ;
      cellY = gj(cell).offset().top - CSUtils.getScrollTop(cell) ;
    }
    var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
    if(document.all && uiControlWorkspace && (!document.getElementById("UIPageDesktop") || base.Browser.isIE7())) cellX -= uiControlWorkspace.offsetWidth ;
    if (
       (mouseX > cellX) && (mouseX < (cellX + cell.offsetWidth))
    && (mouseY > cellY) && (mouseY < (cellY + cell.offsetHeight))
    ) { return true ;}
    return false ;
  },

  /**
   * Gets cellIndex of cell
   * @param {Object} evt Mouse event
   * return cellIndex of current cell
   */
  getCurrentIndex : function(evt) {
    var cells = UIHSelection.cells ;
    var len = cells.length ;
    for(var i = 0 ; i < len ; i++) {
      var isCell = UIHSelection.isInCell(cells[i],evt) ;
      if(isCell) return cells[i].cellIndex ;
    }
  },

  /**
   * Sets attribute for cells
   * @param {Object} sIndex Start cellIndex
   * @param {Object} eIndex End cellIndex
   * @param {Object} cells A cell in time table
   */
  setAttr : function(sIndex, eIndex, cells) {
      for(var i = sIndex; i <= eIndex ; i++) {
    if(gj(cells[i]).hasClass("busyTime"))
        gj(cells[i]).addClass("busySelected");
    else 
        gj(cells[i]).addClass("userSelection") ;
      }
  },

  /**
   * Removes attribute for cellsD:\java\eXoProjects\git-project\calendar\calendar-webapp\src\main\webapp\javascript\eXo\calendar
   * @param {Object} sIndex Start cellIndex
   * @param {Object} eIndex End cellIndex
   * @param {Object} cells A cell in time table
   */
  removeAttr : function(sIndex, eIndex, cells) {
      var len = cells.length;
      for(var i = 0; i < len ; i++) {
    if((i>=sIndex) && (i<=eIndex)) continue ;
    gj(cells[i]).removeClass("userSelection") ;
    gj(cells[i]).removeClass("busySelected") ;
      }
  },

  /**
   * Sets all attribute for cells
   */
  removeAllAttr : function() {
      var cells = this.cells ;
      var len = cells.length ;
      for(var i = 0; i < len ; i++) {
    gj(cells[i]).removeClass("userSelection") ;
    gj(cells[i]).removeClass("busySelected") ;
      }
  },

  /**
   * Sets up horizontal dragging selection when mouse down on time table
   * @param {Object} evt Mouse event
   */
  start : function() {
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
  },

  /**
   * Executes horizontal dragging selection
   */
  execute : function(evt) {
    var _e = window.event || evt ;
    _e.preventDefault();
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
  },

  /**
   * Ends horizontal dragging selection, this method clean up some unused properties and execute callback function
   */
  end : function() {
    UIHSelection.removeAllAttr() ;
    UIHSelection.startIndex = null ;
    UIHSelection.endIndex = null ;
    UIHSelection.cells = null ;
    UIHSelection.container = null ;
    gj(document).off("mousemove mouseup")
    if (UIHSelection.callback) eval(UIHSelection.callback) ;
  }
};

return UIHSelection;
})(base, CSUtils, gj);