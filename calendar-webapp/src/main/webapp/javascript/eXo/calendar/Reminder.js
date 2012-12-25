(function(cs, gj){
function Reminder() {} ;

_module = {};
eXo.calendar = {};

Reminder.prototype.init = function(eXoUser, eXoToken, cometdContextName){
	if(!cs.CSCometd) cs.CSCometd = cometd;
	cs.CSCometd.exoId = eXoUser;
	cs.CSCometd.exoToken = eXoToken;
	if(cometdContextName)
		cs.CSCometd.url = '/' + cometdContextName + '/cometd';
	cs.CSCometd.subscribe('/eXo/Application/Calendar/messages', function(eventObj) {		
		eXo.calendar.Reminder.alarm(eventObj) ;
  });
	if (!cs.CSCometd.isConnected()) {
		cs.CSCometd.init();
  }
} ;

Reminder.prototype.initCometd = function() {
	cs.CSCometd.subscribe('/eXo/Application/Calendar/messages', function(eventObj) {		
		eXo.calendar.Reminder.alarm(eventObj) ;
  });
}

Reminder.prototype.alarm = function(eventObj){
	var a = gj.parseJSON(eventObj.data);	
	var message = '<a class="Item" href="#">('+ a.fromDateTime.hours + ':' + a.fromDateTime.minutes + ') ' +a.summary+'</a>' ;
	var html = this.generateHTML(message) ;
	var popup = gj(this.createMessage(html, message)).find('div.UIPopupNotification')[0]; 
	eXo.calendar.Box.config(popup,popup.offsetHeight, 5, this.openCallback, this.closeBox) ;
	window.focus() ;
	return ;
} ;

Reminder.prototype.openCallback = function(obj){
	gj(obj).on('click',function(){
		this.style.visibility = "hidden" ;
	}) ;
}
Reminder.prototype.closeBox = function(obj){
	obj.style.visibility = "hidden" ;
}

Reminder.prototype.createMessage = function(html, message){
	var msgBox = null ;
	if(document.getElementById("msgBox")) {
		msgBox = document.getElementById("msgBox") ;
		var directChildNode = gj(msgBox).find('div.UIPopupNotification')[0]; 
		if(directChildNode.style.visibility == 'hidden')
			msgBox.innerHTML = html ;
		else {
		//For CS-1397
			var contentBox = gj(msgBox).find('div.MCPopupNotification')[0];
			var childrens = gj(contentBox).find('a.Item');
  		var ln = childrens.length ;	
  		var child = null ;
  		var hasExist = false;
  		for (var k = 0; k < ln; k++){
  			child = childrens[k] ;
  			var msgChild = child.innerHTML;
  			msgChild = '<a class="Item" href="#">'+ msgChild + '</a>';
  			if(msgChild == message){
  				hasExist = true;
  				break;
  			}
  		}
  		if(!hasExist){
				var tmp = document.createElement("div") ;
				tmp.innerHTML = message;
				contentBox.appendChild(tmp);
			}
		}
	} else {
		msgBox = document.createElement("div") ;
		msgBox.id = "msgBox" ;
		msgBox.className = "UINotification" ;
		msgBox.innerHTML = html ;
		document.body.appendChild(msgBox) ;
	}
	return msgBox ;
} ;

Reminder.prototype.generateHTML = function(message){
	var html = '' ;
	html += '<div class="UIPopupNotification">';
	html += '	<div class="TLPopupNotification">';
	html += '		<div class="TRPopupNotification">';
	html += '			<div class="TCPopupNotification"><span></span></div>';
	html += '		</div>';
	html += '	</div>';
	html += '	<div class="MLPopupNotification">';
	html += '		<div class="MRPopupNotification">';
	html += '			<div class="MCPopupNotification">';
	html += '				<div class="TitleNotification">';
	html += '					<a class="ItemTitle" href="#">Notification</a>';
	html += '					<a class="Close" href="#"><span></span></a>';
	html += '				</div>';
	html += 				message;
	html += '			</div>';
	html += '		</div>';
	html += '	</div>';
	html += '	<div class="BLPopupNotification">';
	html += '		<div class="BRPopupNotification">';
	html += '			<div class="BCPopupNotification"><span></span></div>';
	html += '		</div>';
	html += '	</div>';
	html += '</div>';
	return html ;
} ;

// Box effect
function Box(){
	this.speed = 4 ;
	this.tmpHeight = 0 ;
	this.autoClose = true ;
	this.closeInterval = 10 ;
};

Box.prototype.config = function(obj, height, speed, openCallback, closeCallback) {
	this.object = obj;
	this.maxHeight = height ;
	if(speed) this.speed = speed ;
	this.open() ;
	if(openCallback) this.openCallback = openCallback ;
	if(closeCallback) this.closeCallback = closeCallback ;
};

Box.prototype.open = function(){
	var Box = eXo.calendar.Box ;
	Box.object.parentNode.style.top = Box.calculateY() + "px" ;
	if(Box.tmpHeight < Box.maxHeight){
		Box.object.style.overflow = "hidden" ;
		Box.object.style.visibility = "visible" ;
		Box.object.style.height = Box.tmpHeight + "px" ;
		Box.tmpHeight += Box.speed ;
		Box.timer = window.setTimeout(Box.open,10) ;
	} else {
		Box.floatingBox("msgBox",0);
		Box.object.style.overflow = "visible" ;
		Box.tmpHeight = Box.maxHeight ;
		if(Box.timer) window.clearTimeout(Box.timer) ;
		if(Box.closeTimer)  window.clearInterval(Box.closeTimer) ;
		if(Box.autoClose) Box.closeTimer = window.setInterval(Box.close,Box.closeInterval*1000) ;
		Box.openCallback(Box.object) ;
		return ;
	}
};

Box.prototype.close = function(){
	var Box = eXo.calendar.Box ;	
	if(Box.tmpHeight >= 0){
		Box.object.style.overflow = "hidden" ;
		Box.object.style.height = Box.tmpHeight + "px" ;
		Box.tmpHeight -= Box.speed ;
		Box.timer = window.setTimeout(Box.close,10) ;
	} else {
		Box.object.style.overflow = "visible" ;
		Box.object.style.visibility = "hidden" ;
		Box.tmpHeight = 0 ;
		Box.object.style.height = Box.tmpHeight + "px" ;
		if(Box.timer) window.clearTimeout(Box.timer) ;
		if(Box.closeTimer)  window.clearInterval(Box.closeTimer) ;
		Box.closeCallback(Box.object) ;
		return ;
	}
};

Box.prototype.calculateY = function() {
	var posY = 0;
	if(document.documentElement && document.documentElement.scrollTop){
		posY = document.documentElement.scrollTop;
	} else if(document.body && document.body.scrollTop) {
		posY = document.body.scrollTop;
	} else if(window.pageYOffset) {
		posY = window.pageYOffset;
	} else if(window.scrollY) {
		posY = window.scrollY;
	}
	return posY ;
};

Box.prototype.floatingBox = function(objID, posTop){
	var obj = document.getElementById(objID);
	var currentTop = this.calculateY();
	obj.style.top = (currentTop < posTop)? posTop + "px": currentTop + "px";
	window.setTimeout('eXo.calendar.Box.floatingBox("'+objID+'",'+posTop+')', 50);
};

//eXo.webui.Box = new Box() ;
eXo.calendar.Box = new Box() ;
eXo.calendar.Reminder = new Reminder() ;

_module.Reminder = eXo.calendar.Reminder;
return _module.Reminder;
})(cs, gj);