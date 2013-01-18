(function(gj){
var _module = {};

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
  var eList = gj(root).find('input'); 
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
  var inputEList = gj(root).find('input') ;
  var selectedItems = [] ;
  var foundCheckObj = false ;
  for (var i=0; i<inputEList.length; i++) {
    if (inputEList[i].checked) {
      selectedItems[selectedItems.length] = gj(inputEList[i]).parents('.' + klazz)[0] ;
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

//eXo.cs.FormHelper = new FormHelper() ;
_module.FormHelper = new FormHelper() ;
return _module.FormHelper;
})(gj);