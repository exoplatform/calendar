(function(gj) {
Array.prototype.pushAll = function (array) {
	if (array != null) {
		for (var i = 0; i < array.length; i++) {
			this.push(array[i]) ;
		}
	}
};

var domUtil = {
	hideElementList : new Array(),

	cleanUpHiddenElements : function() {
		gj.each(domUtil.hideElementList, function(idx, elem) {
			gj(elem).hide();
		});
		domUtil.hideElementList = [];
	},

	/**
	 * Adds an element to the hideElementList array
	 * Should only contain elements from a popup menu
	 */
	listHideElements : function(object) {
		var container = domUtil.hideElementList;
		if (gj.inArray(object, container) == -1) {
			container.push(object);
		}
	}
};

var jDoc = gj(document);
//Register closing contextual menu callback on document
jDoc.on("click.hideElements.calendar", function()
{
	domUtil.cleanUpHiddenElements();
});

return domUtil;
})(gj);