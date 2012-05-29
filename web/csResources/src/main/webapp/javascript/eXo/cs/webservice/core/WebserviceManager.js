/**
 * @author Uoc Nguyen
 */
// For debug only. -> will remove
if (!window.console) {
  window.console = {
    info:function(){},
    warn:function(){},
    error: function(){},
    dir: function(){}
  }
}

/**
 * Create namespaces.
 */
if (!eXo.cs.webservice) {
  eXo.cs.webservice = {};
}

if (!eXo.cs.webservice.core) {
  eXo.cs.webservice.core = {};
}

/**
 * WebserviceHandler
 * @param {Object} manager
 * @param {String} action
 */
function WebserviceHandler() {
  this.HTTP_GET = 'GET';
  this.HTTP_POST = 'POST';
  this.LOADING_STATE = 'Loading';
  this.SUCCESS_STATE = 'Success';
  this.ERROR_STATE = 'Error';
  this.TIMEOUT_STATE = 'Timeout';
  this.action = false;
  this.manager = false;
  this.action = false;
  this.debugFlag = false;
}

WebserviceHandler.prototype = {
  
  /**
   * 
   * @param {eXo.cs.webservice.core.WebserviceManager} manager
   * @param {String} action
   */
  init : function(manager) {
    if (!manager) {
      throw (new Message('Can not initialize WebserviceHandler instance without manager'));
    }
    this.manager = manager;
  }
  ,
  
  /**
   * 
   * @param {String} url
   */
  makeRequest : function(url, method, data, action) {
    if (!this.manager) {
      throw (new Message("Can not make request without manager object."));
    }
    this.action = action;
    var request = new eXo.portal.AjaxRequest(method, url, data);
    this.manager.initRequest(request, this);
    request.process() ;
  }
  ,
  
  /**
   * 
   * @param {eXo.portal.AjaxRequest} requestObj
   */
  onLoading : function(requestObj) {
    if (this.debugFlag) {
      console.info('[' + this.handler.action + '] ' + this.handler.LOADING_STATE);
    }
    this.handler.update(this.handler.LOADING_STATE, requestObj, this.handler.action);
  }
  ,
  
  /**
   * 
   * @param {eXo.portal.AjaxRequest} requestObj
   */
  onSuccess : function(requestObj) {
    if (this.debugFlag) {
      console.info('[' + this.handler.action + '] ' + this.handler.SUCCESS_STATE);
    }
    this.handler.update(this.handler.SUCCESS_STATE, requestObj, this.handler.action);
  }
  ,
  
  /**
   * 
   * @param {eXo.portal.AjaxRequest} requestObj
   */
  onError : function(requestObj) {
    if (this.debugFlag) {
      console.info('[' + this.handler.action + '] ' + this.handler.ERROR_STATE);
    }
    this.handler.update(this.handler.ERROR_STATE, requestObj, this.handler.action);
  }
  ,
  
  /**
   * 
   * @param {eXo.portal.AjaxRequest} requestObj
   */
  onTimeout : function(requestObj){
    if (this.debugFlag) {
      console.info('[' + this.handler.action + '] ' + this.handler.TIMEOUT_STATE);
    }
    this.handler.update(this.handler.TIMEOUT_STATE, requestObj, this.handler.action);
  }
  ,
  
  /**
   * This method must be overwrite
   * 
   * @param {Integer} state
   * @param {eXo.portal.AjaxRequest} requestObj
   * @param {String} action
   */
  update : function(state, requestObj, action) {}
};


eXo.cs.webservice.core.WebserviceHandler = WebserviceHandler;

/**
 * WebserviceManager
 */
function WebserviceManager() {}

WebserviceManager.prototype.init = function() {};

/**
 * 
 * @param {String} contentType
 * @param {eXo.portal.AjaxRequest}} ajaxRequest
 */
WebserviceManager.prototype.ajaxProcessOverwrite = function(contentType, ajaxRequest) {
	if (ajaxRequest.request == null) return ;
	ajaxRequest.request.open(ajaxRequest.method, ajaxRequest.url, true) ;		
	if (!contentType) {
    if (ajaxRequest.method == "POST") {
  		ajaxRequest.request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") ;
  	} else {
  		ajaxRequest.request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8") ;
  	}
  } else {
		ajaxRequest.request.setRequestHeader("Content-Type", contentType) ;
  }
	
	if (ajaxRequest.timeout > 0) setTimeout(ajaxRequest.onTimeoutInternal, ajaxRequest.timeout) ;
	
	ajaxRequest.request.send(ajaxRequest.queryString) ;
};

/**
 * 
 * @param {eXo.portal.AjaxRequest}} ajaxRequest
 */
WebserviceManager.prototype.ajaxProcessXmlContent = function(ajaxRequest) {
  this.ajaxProcessOverwrite("text/xml;charset=UTF-8", ajaxRequest);
};

/**
 * 
 * @param {AjaxRequest} ajaxRequest
 * @param {Object} handler
 */
WebserviceManager.prototype.initRequest = function(ajaxRequest, handler) {
  ajaxRequest.onSuccess = handler.onSuccess ;
  ajaxRequest.onLoading = handler.onLoading ;
  ajaxRequest.onTimeout = handler.onTimeout ;
  ajaxRequest.onError = handler.onError ;
  ajaxRequest.callBack = handler.callBack ;
  ajaxRequest.handler = handler;
  this.currentRequest = ajaxRequest ;
};

eXo.cs.webservice.core.WebserviceManager = new WebserviceManager();
