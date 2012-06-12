/**
 * @author uocnb
 */
function FormHelper() {
}

/**
 * Check check box or radio status
 * 
 * @param {Object} element
 */
FormHelper.prototype.isSelected = function(element) {
  return (element.checked || element.selected) ;
} ;

FormHelper.prototype.checkAllElement = function(root) {
  var eList = eXo.core.DOMUtil.findDescendantsByTagName(root, 'input') ;
  for (var i=0; i<eList.length; i++) {
    eList[i].checked = true ;
  }
}
/**
 * Return all selected elements. If no element selected, return empty array.
 * 
 * @param {Element} root
 */
FormHelper.prototype.getSelectedElementByClass = function(root, klazz, e2selectIfFalse) {
  var inputEList = eXo.core.DOMUtil.findDescendantsByTagName(root, 'input') ;
  var selectedItems = [] ;
  var foundCheckObj = false ;
  for (var i=0; i<inputEList.length; i++) {
    if (inputEList[i].checked) {
      selectedItems[selectedItems.length] = eXo.core.DOMUtil.findAncestorByClass(inputEList[i], klazz) ;
      if (e2selectIfFalse == selectedItems[selectedItems.length - 1]) {
        foundCheckObj = true ;
      }
    }
  }
  if (!foundCheckObj) {
    selectedItems[selectedItems.length] = e2selectIfFalse ;
  }
  return selectedItems ;
}

if (!eXo.cs) {
  eXo.cs = {} ;
}

eXo.cs.FormHelper = new FormHelper() ;