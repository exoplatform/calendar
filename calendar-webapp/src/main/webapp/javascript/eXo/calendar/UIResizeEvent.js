(function(base, gj, CSUtils) {

	var _module = {};	
	
	var UIResizeEvent = {
		/**
		 * Initialize some properties of UIResizeEvent
		 * @param {Object} evt Mouse event
		 */
		init : function(evt) {
		  _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
		    var _e = window.event || evt;
		    if (_e.stopPropagation) {
		        _e.stopPropagation();
		    } else {
		        // IE8 fix
		        _e.returnValue = false;
		        _e.cancelBubble = true;
		    }

		    var outerElement = gj(this).parents('.eventBoxes')[0];
		    var innerElement = gj(this).prevAll("div")[0];
		    var container = gj(outerElement).parents('.eventDayContainer')[0];
		    gj(container).css({
		        '-moz-user-select'   :'none',
		        '-o-user-select'     :'none',
		        '-khtml-user-select' :'none', /* you could also put this in a class */
		        '-webkit-user-select':'none', /* and add the CSS class here instead */
		        '-ms-user-select'    :'none',
		        'user-select'        :'none'}).bind('selectstart', function(){ return false; });
		    var minHeight = 15;
		    var interval = _module.UICalendarPortlet.interval;
		    UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval);
		    //UIResizeEvent.callback = UIResizeEvent.resizeCallback;
			//TODO: This is recursive dependency, we should remove this		    
		    _module.UICalendarPortlet.dropCallback = UIResizeEvent.resizeCallback;
		    _module.UICalendarPortlet.setPosition(outerElement);
		    eXo.calendar.EventTooltip.disable(evt);
		},

		getOriginalHeight : function(obj) {
		    var paddingTop = Number(gj(obj).css('paddingTop').match(/\d+/));
		    var paddingBottom = Number(gj(obj).css('paddingBottom').match(/\d+/));
		    var originalHeight = obj.offsetHeight - (paddingTop + paddingBottom);
		    return originalHeight;
		},

		/**
		 * Sets up calendar event resizing when mouse down on it
		 * @param {Object} evt Mouse event
		 * @param {Object} innerElement DOM element before maker element
		 * @param {Object} outerElement DOM element after maker element
		 * @param {Object} container DOM element contains all events
		 * @param {Object} minHeight Minimum height to resize
		 * @param {Object} interval Resizing step( default is 30 minutes)
		 */
		start : function(evt, innerElement, outerElement, container, minHeight, interval) {
		    var _e = window.event || evt;
		    this.innerElement = innerElement;
		    this.outerElement = outerElement;
		    this.container = container;
		    CSUtils.resetZIndex(this.outerElement);
		    this.minHeight = (minHeight) ? parseInt(minHeight) : 15;
		    this.interval = (interval != "undefined") ? parseInt(interval) : 15;
		    gj(document).on({'mousemove':UIResizeEvent.execute,
		        'mouseup':UIResizeEvent.end});
		    this.beforeHeight = this.getOriginalHeight(this.outerElement);
		    this.innerElementHeight = this.getOriginalHeight(this.innerElement);
		    this.posY = _e.clientY;
		    this.uppermost = outerElement.offsetTop + minHeight - container.scrollTop;
		    if (document.getElementById("UIPageDesktop")) {
		        var uiWindow = gj(container).parents(".UIResizableBlock")[0];
		        this.uppermost -= uiWindow.scrollTop;
		    }
		},

		/**
		 * Executes calendar event resizing
		 * @param {Object} evt Mouse event
		 */
		execute : function(evt) {
		    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
		    eXo.calendar.EventTooltip.disable(evt);
		    var _e = window.event || evt;
		    var mouseY = base.Browser.findMouseRelativeY(UIResizeEvent.container, _e);
		    var mDelta = _e.clientY - UIResizeEvent.posY;
		    if (mouseY <= UIResizeEvent.uppermost) {
		        return;
		    }
		    else {
		        var maxDelta = 1440 - UIResizeEvent.outerElement.offsetTop - UIResizeEvent.beforeHeight;
		        if(mDelta > maxDelta) {
		            return;
		        }
		        if (mDelta % UIResizeEvent.interval == 0) {
		            UIResizeEvent.outerElement.style.height = UIResizeEvent.beforeHeight - 2 + mDelta + "px";
		            UIResizeEvent.innerElement.style.height = UIResizeEvent.innerElementHeight + mDelta + "px";
		        }
		    }
		    var min = (base.Browser.isIE6())?(UIResizeEvent.outerElement.offsetTop - 1) : UIResizeEvent.outerElement.offsetTop;
		    _module.UICalendarPortlet.updateTitle(UIResizeEvent.outerElement, UIResizeEvent.outerElement.offsetTop, 1);
		},

		/**
		 * End calendar event resizing, this method clean up some unused properties and execute callback function
		 * @param {Object} evt Mouse event
		 */
		end : function(evt) {
		    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
		    gj(document).off("mousemove mouseup");
		    var _e = window.event || evt;
		    _module.UICalendarPortlet.checkPermission(UIResizeEvent.outerElement) ;
		    eXo.calendar.EventTooltip.enable();
		},

		/**
		 * Resizing callback method
		 * @param {Object} evt Mouse object
		 */
		resizeCallback : function(evt) {
		    _module.UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
		    var UICalendarPortlet = _module.UICalendarPortlet;
		    var eventBox = UIResizeEvent.outerElement;

		    if (!eventBox) { return ; }

		    var start = parseInt(eventBox.getAttribute("startTime"));
		    var calType = eventBox.getAttribute("calType");
		    var isOccur = eventBox.getAttribute("isoccur");
		    var eventId = eventBox.getAttribute("eventid");
		    var recurId = eventBox.getAttribute("recurid");
		    if (recurId == "null") recurId = "";
		    var end = start + UICalendarPortlet.pixelsToMins(eventBox.offsetHeight);
		    if (eventBox.offsetHeight != UIResizeEvent.beforeHeight) {
		        var actionLink = eventBox.getAttribute("actionLink");
		        var form = gj(eventBox).parents("form")[0];
		        form.elements[eventId + "startTime"].value = start;
		        form.elements[eventId + "finishTime"].value = end;
		        form.elements[eventId + "isOccur"].value = isOccur;
		        form.elements[eventId + "recurId"].value = recurId;
		        UICalendarPortlet.setTimeValue(eventBox,start,end);
		        var uiDayView = window.require("SHARED/UIDayView");
		        uiDayView.showEvent();
		        gj.globalEval(actionLink);
		    }
		    UIResizeEvent.innerElement = null;
		    UIResizeEvent.outerElement = null;
		    UIResizeEvent.posY = null;
		    UIResizeEvent.minHeight = null;
		    UIResizeEvent.interval = null;
		    UIResizeEvent.innerElementHeight = null;
		    UIResizeEvent.outerElementHeight = null;
		    UIResizeEvent.container = null;
		    UIResizeEvent.innerElementHeight = null;
		    UIResizeEvent.beforeHeight = null;
		    UIResizeEvent.posY = null;
		    UIResizeEvent.uppermost = null;
		}
	};
	return UIResizeEvent;
})(base, $, CSUtils);