(function(gj, base, DOMUtil) {
	/**
	 * Class control dragging selection
	 */
	function UISelection() {
		
	};
	
	/**
	 * Sets up dragging selection when mouse down on calendar event
	 * @param {Object} evt Mouse event
	 */
	UISelection.prototype.start = function(evt) {
		try {
			evt.preventDefault();
			var UISelection = eXo.calendar.UISelection;
			var src = evt.target;
			if ((src == UISelection.block) || evt.which != 1 || (gj(src).hasClass("TdTime"))) {
				return;
			}
			var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
			UISelection.startTime = parseInt(Date.parse(src.getAttribute("startFull")));//src.getAttribute("startTime");
			UISelection.startX = base.Browser.findPosXInContainer(src, UISelection.container) - UICalendarPortlet.portletNode.parentNode.scrollTop;
			UISelection.block.style.display = "block";
			UISelection.startY = base.Browser.findPosYInContainer(src, UISelection.container);
			UISelection.block.style.width = src.offsetWidth + "px";
			UISelection.block.style.left = UISelection.startX + "px";
			UISelection.block.style.top = UISelection.startY + "px";
			UISelection.block.style.height = UISelection.step + "px";
			UISelection.block.style.zIndex = 1;
			gj(document).off('mousemove mouseup').on({'mousemove':UISelection.execute,
				'mouseup':UISelection.clear});
		}
		catch (e) {
			window.status = e.message ;
		}
	};
	
	/**
	 * Executes dragging selection
	 * @param {Object} evt Mouse event
	 */
	UISelection.prototype.execute = function(evt) {
		var UISelection = eXo.calendar.UISelection;
		var _e = window.event || evt;
		var containerHeight = UISelection.container.offsetHeight;
		var scrollTop = gj(UISelection.block).scrollTop();
		var mouseY = base.Browser.findMouseRelativeY(UISelection.container, _e);// + UISelection.relativeObject.scrollTop;
		if (document.getElementById("UIPageDesktop"))
		{
			mouseY = base.Browser.findMouseRelativeY(UISelection.container, _e) + scrollTop;
		}
		var posY = UISelection.block.offsetTop;
		var height = UISelection.block.offsetHeight;
		var delta = mouseY - UISelection.startY - (mouseY - UISelection.startY) % UISelection.step;
		
		gj(UISelection.block).css('height', Math.abs(delta) + UISelection.step + "px");
		gj(UISelection.block).css('top', delta > 0 ? UISelection.startY : UISelection.startY + delta + "px");
		
	};
	
	/**
	 * Ends dragging selection, this method clean up some unused properties and execute callback function
	 */
	UISelection.prototype.clear = function() {
	  var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
		var UISelection = eXo.calendar.UISelection;
		var endTime = UICalendarPortlet.pixelsToMins(UISelection.block.offsetHeight) * 60 * 1000 + parseInt(UISelection.startTime);
		var startTime = UISelection.startTime;
		var bottom = UISelection.block.offsetHeight + UISelection.block.offsetTop;
		
		if (UISelection.block.offsetTop < UISelection.startY) {
			startTime = parseInt(UISelection.startTime) - UICalendarPortlet.pixelsToMins(UISelection.block.offsetHeight) * 60 * 1000 + UICalendarPortlet.MINUTE_PER_CELL * 60 * 1000;
			endTime = parseInt(UISelection.startTime) + UICalendarPortlet.MINUTE_PER_CELL * 60 * 1000;
		}
		if(bottom >= UISelection.container.offsetHeight) endTime -= 1;
		var container = UICalendarPortlet.getElementById("UICalendarViewContainer");
		UICalendarPortlet.addQuickShowHiddenWithTime(container, 1, startTime, endTime) ;
		DOMUtil.listHideElements(UISelection.block);
		UISelection.startTime = null;
		UISelection.startY = null;
		UISelection.startX = null;
		gj(document).off("mousemove mouseup");
	};
	eXo = eXo || {};
	eXo.calendar = eXo.calendar || {};
	eXo.calendar.UISelection = new UISelection();	
	return eXo.calendar.UISelection;
})(gj, base, DOMUtil);