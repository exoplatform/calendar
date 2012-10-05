click clientX=200, clientY=226
17645791



require([ "SHARED/base", "SHARED/navigation",
		"PORTLET/calendar/CalendarPortlet", "SHARED/jquery",
		"SHARED/csResources", "SHARED/webui", "SHARED/webui-ext" ], 
	function(base, nav, cal, gj, cs, webui, webuiExt) {
	nav.UIPortalNavigation.onLoad('UIStarToolbarPortlet');
	nav.UIPortalNavigation.onLoad('UIUserToolBarSitePortlet');
	nav.UIPortalNavigation.onLoad('UIUserToolBarGroupPortlet');
	nav.UIPortalNavigation.onLoad('UIUserToolBarDashboardPortlet');
	nav.UIPortalNavigation.onLoad('UIAdminToolbarPortlet');
	nav.UIPortalNavigation
			.onLoad('a312d49c-d631-45ea-a96f-848651967e90-portlet');
	cal.restContext = "rest-csdemo";
	cal.settingTimezone = "60";
	cal.UICalendarPortlet.setting(30, "null", "HH:mm", "UICalendarPortlet");
	cal.UICalendarPortlet.showContextMenu("UICalendarPortlet");
	cal.Reminder.init("root", "gftbqb136p40", "cometd-csdemo");
	cal.UICalendarPortlet.portletId = "UICalendarPortlet";
	cal.UICalendarPortlet.settingTimezone = "60";
	cal.UICalendarPortlet.isSpace = "null";
	cal.LayoutManager.layoutId = "calendarlayout-root";
	cal.UICalendarPortlet.checkLayout();
	cal.CalendarLayout.init();
	cal.UICalendarPortlet.attachSwapClass("UIActionBar", "ControlButton",
			"ControlButtonHover");
	gj('div#Event').click(function() {
		cal.UICalendarPortlet.addQuickShowHidden(this, 1);
	});
	gj('div#Task').click(function() {
		cal.UICalendarPortlet.addQuickShowHidden(this, 2);
	});
	gj('div#CustomLayout').click(function(event) {
		cal.UICalendarPortlet.showView(this, event);
	});
	gj('div#DefaultLayout').click(function() {
		cal.UICalendarPortlet.switchLayout(0);
	});
	gj('div#ShowHideAll').click(function() {
		cal.UICalendarPortlet.switchLayout(1);
	});
	gj('div#ShowHideMinicalendar').click(function() {
		cal.UICalendarPortlet.switchLayout(2);
	});
	gj('div#ShowHideCalendarList').click(function() {
		cal.UICalendarPortlet.switchLayout(3);
	});
	cs.Utils.captureInput("value");
	cal.CalendarLayout.updateMiniCalendarLayout();
	gj('div#UIMiniCalendar_1').click(function() {
		cal.UICalendarPortlet.switchLayout(2);
	});
	cal.CalendarLayout.updateUICalendarsLayout();
	cal.UICalendars.init("UICalendars");
	gj('div#UICalendars_calendarActions').click(
			function(event) {
				cal.UICalendars.showMenu(this, event, 'CalendarMainPopupMenu',
						cal.UICalendars.mainMenuCallback);
			});
	gj('div#UICalendars_CalendarGroupPopupMenu').click(
			function(event) {
				cal.UICalendars.showMenu(this, event, 'CalendarGroupPopupMenu',
						cal.UICalendars.calendarMenuCallback);
			});
	gj('div#UICalendars_CalendarPopupMenu1').click(
			function(event) {
				cal.UICalendars.showMenu(this, event, 'CalendarPopupMenu',
						cal.UICalendars.calendarMenuCallback);
			});
	gj('div#UICalendars_CalendarPopupMenu2').click(
			function(event) {
				cal.UICalendars.showMenu(this, event, 'CalendarPopupMenu2',
						cal.UICalendars.calendarMenuCallback);
			});
	gj('div#UICalendars_CalendarPopupMenu3').click(
			function(event) {
				cal.UICalendars.showMenu(this, event, 'CalendarPopupMenu',
						cal.UICalendars.calendarMenuCallback);
			});
	gj('div#UICalendars_toggle-calendars').click(function() {
		cal.UICalendars.UICalendarPortlet.switchLayout(3);
	});
	cal.CalendarLayout.updateCalendarContainerLayout();
	cal.UICalendarPortlet.onLoad();
	gj('table#UIMonthViewGrid.UIGrid').parent().resize(function() {
		eXo.calendar.UICalendarMan.initMonth();
	});
	cal.UICalendarPortlet.currentDate = 1348737650284;
	cal.UICalendarPortlet.getFilterSelect("UIMonthView");
	gj('div#UIHeaderBar_1').click(function(event) {
		cal.UICalendarPortlet.switchListView(this, event);
	});
	gj('div#UIHeaderBar_2').click(
			function() {
				cs.Utils.confirmAction(this,
						'Please check at least one event or task.',
						'UICalendarViewContainer');
			});
	gj('div#UIHeaderBar_3').click(
			function() {
				cs.Utils.confirmAction(this,
						'Please check at least one event or task.',
						'UICalendarViewContainer');
			});
	gj('a#UIPopupWindowQuick_UIPopupWindow_Close').click(function() {
		base.UIPopup.hide('UIQuckAddEventPopupWindow');
	});
	cal.UICalendarPortlet.isAllday('QuickAddEventContainer', '');
	gj('td#allDay').click(function() {
		cal.UICalendarPortlet.showHideTime(this);
	});
	gj('input#DateTimePicker-uniq-2a9f2ead-ca67-414e-b5c2-63bd68923572').focus(
			function() {
				cs.UIDateTimePicker.init(this, false);
			});
	gj('input#DateTimePicker-uniq-2a9f2ead-ca67-414e-b5c2-63bd68923572').keyup(
			function() {
				cs.UIDateTimePicker.show();
			});
	gj('input#DateTimePicker-uniq-2a9f2ead-ca67-414e-b5c2-63bd68923572').focus(
			function(event) {
				event.cancelBubble = true
			});
	webuiExt.UICombobox.init('fromTime');
	gj('input#DateTimePicker-uniq-9f2a6820-213c-499e-a2e9-be63a2e43503').focus(
			function() {
				cs.UIDateTimePicker.init(this, false);
			});
	gj('input#DateTimePicker-uniq-9f2a6820-213c-499e-a2e9-be63a2e43503').keyup(
			function() {
				cs.UIDateTimePicker.show();
			});
	gj('input#DateTimePicker-uniq-9f2a6820-213c-499e-a2e9-be63a2e43503').focus(
			function(event) {
				event.cancelBubble = true
			});
	webuiExt.UICombobox.init('toTime');
	gj('a#UIPopupWindowQuick_UIPopupWindow_Close').click(function() {
		base.UIPopup.hide('UIQuckAddTaskPopupWindow');
	});
	cal.UICalendarPortlet.isAllday('QuickAddEventContainer', '');
	gj('td#allDay').click(function() {
		cal.UICalendarPortlet.showHideTime(this);
	});
	gj('input#DateTimePicker-uniq-fea13176-d4a0-495e-ab37-cbb90be13d3d').focus(
			function() {
				cs.UIDateTimePicker.init(this, false);
			});
	gj('input#DateTimePicker-uniq-fea13176-d4a0-495e-ab37-cbb90be13d3d').keyup(
			function() {
				cs.UIDateTimePicker.show();
			});
	gj('input#DateTimePicker-uniq-fea13176-d4a0-495e-ab37-cbb90be13d3d').focus(
			function(event) {
				event.cancelBubble = true
			});
	webuiExt.UICombobox.init('fromTime');
	gj('input#DateTimePicker-uniq-4d809ccd-40cc-430a-9257-f6db944f800b').focus(
			function() {
				cs.UIDateTimePicker.init(this, false);
			});
	gj('input#DateTimePicker-uniq-4d809ccd-40cc-430a-9257-f6db944f800b').keyup(
			function() {
				cs.UIDateTimePicker.show();
			});
	gj('input#DateTimePicker-uniq-4d809ccd-40cc-430a-9257-f6db944f800b').focus(
			function(event) {
				event.cancelBubble = true
			});
	webuiExt.UICombobox.init('toTime');
	base.Browser.onLoad();
	cal.UICalendarPortlet.checkFilter;
	cal.CalendarLayout.updateUICalendarViewLayout;
});