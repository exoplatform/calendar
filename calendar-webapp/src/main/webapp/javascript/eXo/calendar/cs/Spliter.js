(function(gj) {

  /********************* Pane Spliter ******************/
  var Spliter = {
    /**
     * 
     * @param {Object} e : Event Object
     * @param {Object} markerobj : Click object
     * This function to resize pane
     */
    doResize : function(e , markerobj) {
      this.posY = _e.pageY;
      var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj;
      var container = marker.parentNode;
      var areas = gj(container).find('div.spliterResizableListArea'); 
      if((areas.length < 2) || (areas[0].style.display=="none")) return;
      this.beforeArea = areas[0];
      this.afterArea = areas[1];
      this.beforeArea.style.overflowY = "auto";
      this.afterArea.style.overflowY = "auto";
      this.beforeY = this.beforeArea.offsetHeight;
      this.afterY = this.afterArea.offsetHeight;

      var jDoc = gj(document);
      jDoc.off('mousemove.Spliter').on('mousemove.Spliter', Spliter.adjustHeight);
      jDoc.off('mouseup.Spliter').on('mouseup.Spliter', Spliter.clear);
    },

    adjustHeight : function(evt) {
      var delta = evt.pageY - Spliter.posY;
      var afterHeight = Spliter.afterY - delta;
      var beforeHeight = Spliter.beforeY + delta;
      if (beforeHeight <= 0  || afterHeight <= 0) return;
      Spliter.beforeArea.style.height =  beforeHeight + "px";
      if(Spliter.afterY > 0) Spliter.afterArea.style.height =  afterHeight + "px";
    },
  
    clear : function() {
      try {
        var Spliter = Spliter ;
        gj('document').off('mousemove.Spliter');
        delete Spliter.beforeY ;
        delete Spliter.afterY ;
        delete Spliter.beforeArea ;
        delete Spliter.afterArea ;
        delete Spliter.posY ;
      } catch(e) {window.statuts = "Message : " + e.message ;} ;
    }
  };
  
  return Spliter;
})(gj);
