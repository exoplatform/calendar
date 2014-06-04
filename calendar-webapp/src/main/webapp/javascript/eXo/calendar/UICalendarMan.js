(function(gj, GUIMan, EventMan) {

var UICalendarMan = {
  addBorderControl : function() {
    var leftNode = gj('div.leftContinueEvent')[0];
    if(leftNode) {
      gj(leftNode).parents('.eventOnDayBorder').addClass('leftBorderControl');
    }

    var rightNode = gj('div.rightContinueEvent')[0];
    if(rightNode) {
      gj(rightNode).parents('.eventOnDayBorder').addClass('rightBorderControl');
    }
  },
  showMonthEvents : function() {
    var events = gj('div.dayContentContainer');
    for(var i = 0; i < events.length; i++) {
      gj(events[i]).attr('style','display:block;');
    }
    var moreNodes = gj('div.MoreEvent');
    for(var i = 0; i < moreNodes.length; i++) {
      moreNodes[i].style.display = 'block';
    }
  },
  initMonth : function(rootNode) {
    rootNode = document.getElementById('UIMonthView');
    if (!rootNode) return;
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    EventMan.initMonth(rootNode);
    GUIMan.initMonth();
    GUIMan.initHighlighter();
		GUIMan.addContinueClass();
    UICalendarMan.addBorderControl();
  },
  initWeek : function(rootNode) {
    rootNode = document.getElementById('UIWeekViewGridAllDay');
    if (!rootNode) return;
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    EventMan.initWeek(rootNode);
    GUIMan.initWeek();
  }
};

return UICalendarMan;
})(gj, GUIMan, EventMan);