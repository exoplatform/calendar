(function($) {
  var defaultSettings = {
      ulContainerClass: 'containerMoreItem',
      liMoreClass : 'moreItem',
      paddingLRMenu : 20,
      loadMoreLabel : 'More',
      loadMoreIcon : 'uiIconMiniArrowDown',
      moreIsActionIcon : true,
      processContainerWidth : function() {}
  };

  function log(v) {
    if(window.console && window.console.log) {
      window.console.log(v);
    }
  }

  var loadMoreItem = function(settings) {
    settings = $.extend(true, {}, defaultSettings, settings);
    
    var parentContainer, containerMoreItem = '', widthMoreAction = 0, moreClass = "moreItem";
    
    var moreItemDefault = $('<li class="dropdown ' + settings.liMoreClass + ' " data-toggle="dropdown"></li>');
    var clazz = (settings.moreIsActionIcon) ? 'actionIcon' : '';
    //var div = $('<div data-toggle="dropdown" class="' + clazz + '"></div>').html(settings.loadMoreLabel);
    var i = ($('<i class="' + settings.loadMoreIcon + '"></i>'));
    moreItemDefault.html(settings.loadMoreLabel).append(i);
    
    function processResizeWindow () {
      if(parentContainer.length > 0) {
        if(containerMoreItem.length > 0) {
          var maxWidth = settings.processContainerWidth(parentContainer);
          var tmpUl = parentContainer.find('ul.fakeContainer:first');
          var itemsMore = null;
          if(containerMoreItem.find('li.' + moreClass + ':first').length > 0) {
              itemsMore = containerMoreItem.find('ul.menuMore').find(' > li');
          }
          tmpUl.append(containerMoreItem.find(' > li'));
          if(itemsMore != null) {
            tmpUl.append(itemsMore);
            tmpUl.find('li.' + moreClass).remove();
          }
          tmpUl.find('li').addClass('btn');
          var items = tmpUl.find(' > li');
          //
          processRender(items, maxWidth);
        }
      }
    }
    
    function processRender (items, maxWidth) {
      var itemDisplay = [];
      var itemMenu = [];
      var lengthItem = 0;
      var minWidthMenu = 100;
      
      $.each(items, function(index, elm) {
        var it = $(elm);
        lengthItem += (it.outerWidth());
        if((lengthItem + widthMoreAction) < maxWidth) {
          itemDisplay.push(it);
        } else {
          if(itemMenu.length === 0) {
            itemDisplay.push(moreItemDefault.clone());
          }
          
          itemMenu.push(it);
          if((it.width() + settings.paddingLRMenu) > minWidthMenu) {
            minWidthMenu = it.width() + settings.paddingLRMenu;
          }
        }
      });
      
      containerMoreItem.empty();
      for(var i = 0; i < itemDisplay.length; ++i) {
        containerMoreItem.append(itemDisplay[i]);
      }

      var moreItem = containerMoreItem.find('li.' + moreClass + ':first');
      if(moreItem.length >0) {
        var ulMore = $('<ul class="dropdown-menu menuMore pull-right"></ul>');
        ulMore.css('min-width', minWidthMenu + 'px');
        for(var i = 0; i < itemMenu.length; ++i) {
          ulMore.append(itemMenu[i]);
        }
        ulMore.find('li').removeClass('btn');
        moreItem.after(ulMore);
      }
      
    }
    
    return {
      init : function(parent) {
        parentContainer = $(parent);
        containerMoreItem = parentContainer.find('ul.' + settings.ulContainerClass + ':first');
        if(containerMoreItem.length > 0) {
          var fakeContainer = parentContainer.find('ul.fakeContainer:first');
          if(fakeContainer.length === 0) {
            var allMoreClass = containerMoreItem.attr('class').replace(settings.ulContainerClass, '');
            fakeContainer = $('<ul class="fakeContainer ' + allMoreClass + '"></ul>');
            fakeContainer.insertAfter(containerMoreItem)
            fakeContainer = parentContainer.find('ul.fakeContainer:first');
            fakeContainer.append(containerMoreItem.find(' > li'));
            fakeContainer.css({'visibility': 'hidden', 'position' : 'absolute'});
          }
          
          containerMoreItem.append(moreItemDefault.clone());
          moreClass = settings.liMoreClass.replace('btn ', '');
          widthMoreAction = containerMoreItem.find('li.' + moreClass + ':first').width();
          containerMoreItem.empty();

          var maxWidth = settings.processContainerWidth(parentContainer);
          var items = fakeContainer.find(' > li');
          //
          processRender(items, maxWidth);
          //
          $(window).on('resize', processResizeWindow);
        }
      }
    };
  };
  
  $.fn.loadMoreItem = function(method, settings) {

    var outerArguments = arguments;

    if (typeof method === 'object' || !method) {
      settings = method;
    }

    return this.each(function() {
      var instance = $.data(this, 'loadMoreItem') || $.data(this, 'loadMoreItem', new loadMoreItem(settings));
      if ($.isFunction(instance[method])) {
        return instance[method].apply(this, Array.prototype.slice.call(outerArguments, 1));
      } else if (typeof method === 'object' || !method) {
        return instance.init.call(this, this);
      } else {
        $.error('Method ' + method + ' does not exist');
      }
    });
  };
  
})($);
