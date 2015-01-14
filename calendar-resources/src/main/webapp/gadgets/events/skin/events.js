function DateTimeFormater(){
};
DateTimeFormater.prototype.masks = {
	"default":      "ddd mmm dd yyyy HH:MM:ss",
	shortDate:      "m/d/yy",
	mediumDate:     "mmm d, yyyy",
	longDate:       "mmmm d, yyyy",
	fullDate:       "dddd, mmmm d, yyyy",
	shortTime:      "h:MM TT",
	mediumTime:     "h:MM:ss TT",
	longTime:       "h:MM:ss TT Z",
	isoDate:        "yyyy-mm-dd",
	isoTime:        "HH:MM:ss",
	isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
	isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
};
DateTimeFormater.prototype.token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g;
DateTimeFormater.prototype.timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g;
DateTimeFormater.prototype.timezoneClip = /[^-+\dA-Z]/g;
DateTimeFormater.prototype.pad = function(val, len) {
	val = String(val);
	len = len || 2;
	while (val.length < len) val = "0" + val;
	return val;
};

DateTimeFormater.prototype.i18n = {
	dayNames: [
		"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
		"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
	],
	monthNames: [
		"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	]
};

DateTimeFormater.prototype.format = function (date, mask, utc) {
	var dF = DateTimeFormater;

	// You can't provide utc if you skip other args (use the "UTC:" mask prefix)
	if (arguments.length == 1 && (typeof date == "string" || date instanceof String) && !/\d/.test(date)) {
		mask = date;
		date = undefined;
	}

	// Passing date through Date applies Date.parse, if necessary
	date = date ? new Date(date) : new Date();
	if (isNaN(date)) throw new SyntaxError("invalid date");

	mask = String(dF.masks[mask] || mask || dF.masks["default"]);

	// Allow setting the utc argument via the mask
	if (mask.slice(0, 4) == "UTC:") {
		mask = mask.slice(4);
		utc = true;
	}

	var	_ = utc ? "getUTC" : "get",
		d = date[_ + "Date"](),
		D = date[_ + "Day"](),
		m = date[_ + "Month"](),
		y = date[_ + "FullYear"](),
		H = date[_ + "Hours"](),
		M = date[_ + "Minutes"](),
		s = date[_ + "Seconds"](),
		L = date[_ + "Milliseconds"](),
		o = utc ? 0 : date.getTimezoneOffset(),
		flags = {
			d:    d,
			dd:   dF.pad(d),
			ddd:  dF.i18n.dayNames[D],
			dddd: dF.i18n.dayNames[D + 7],
			m:    m + 1,
			mm:   dF.pad(m + 1),
			mmm:  dF.i18n.monthNames[m],
			mmmm: dF.i18n.monthNames[m + 12],
			yy:   String(y).slice(2),
			yyyy: y,
			h:    H % 12 || 12,
			hh:   dF.pad(H % 12 || 12),
			H:    H,
			HH:   dF.pad(H),
			M:    M,
			MM:   dF.pad(M),
			s:    s,
			ss:   dF.pad(s),
			l:    dF.pad(L, 3),
			L:    dF.pad(L > 99 ? Math.round(L / 10) : L),
			t:    H < 12 ? "a"  : "p",
			tt:   H < 12 ? "am" : "pm",
			T:    H < 12 ? "A"  : "P",
			TT:   H < 12 ? "AM" : "PM",
			Z:    utc ? "UTC" : (String(date).match(dF.timezone) || [""]).pop().replace(dF.timezoneClip, ""),
			o:    (o > 0 ? "-" : "+") + dF.pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
			S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
		};

	return mask.replace(dF.token, function ($0) {
		return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
	});
};

DateTimeFormater = new DateTimeFormater();

var DOMUtil = {
	findNextElementByTagName : function(element, tagName) {
		var nextElement = element.nextSibling;
		if(!nextElement) return null;
		var nodeName = nextElement.nodeName.toLowerCase();
		if(nodeName != tagName) return null;
		return nextElement;
	}
};

function eXoEventGadget() {
};

eXoEventGadget.prototype.getPrefs = function() {
	var setting = (new gadgets.Prefs()).getString("setting");
	if(setting =="") setting = ["","/rest/cs/calendar/getissues","10","AM/PM","defaultCalendarName"];
	else {
		setting = setting.split(";");
	}
	this.prefs = {
		"url"  : setting[0],
		"subscribeurl"  : setting[1],
		"limit": setting[2],
		"timeformat" : setting[3],
		"calendars"  : setting[4]
	}
	return this.prefs;
}

//TODO: Need a new solution for creating url replace for using parent 
eXoEventGadget.prototype.setLink = function(){
	var prefs = new gadgets.Prefs();
	var url   = prefs.getString("url");
	if(parent.eXo){
		 baseUrl = parent.eXo.env.server.context + "/" + parent.eXo.env.portal.accessMode + "/" + parent.eXo.env.portal.portalName;
	}
	var a = document.getElementById("ShowAll");
	url = (url)?baseUrl + url: baseUrl + "/calendar";
	a.href = url;
}
eXoEventGadget.prototype.createRequestUrl = function(){
	var prefs = eXoEventGadget.getPrefs();
	var limit = (prefs.limit && (parseInt(prefs.limit) > 0))? prefs.limit:0;
	var subscribeurl = (prefs.subscribeurl)?prefs.subscribeurl: "/rest/cs/calendar/getissues" ;
	subscribeurl +=  "/" + DateTimeFormater.format((new Date()),"yyyymmdd") + "/Event/" + limit ;
	subscribeurl += "?rnd=" + (new Date()).getTime();
	return subscribeurl;
}
eXoEventGadget.prototype.getData = function(){					 
	var url = eXoEventGadget.createRequestUrl();					
	eXoEventGadget.ajaxAsyncGetRequest(url,eXoEventGadget.render);
	if(typeof(requestInterval) == "undefined") requestInterval = setInterval(eXoEventGadget.getData,300000);
}				
eXoEventGadget.prototype.render =  function(data){
	var userTimezoneOffset = data.userTimezoneOffset;
	data = data.info;
	if(!data || data.length == 0){
		eXoEventGadget.notify();
		return;
	}
  var cont = document.getElementById("ItemContainer");	
	var prefs = eXoEventGadget.getPrefs();
	var gadgetPref = new gadgets.Prefs();
	var timemask = "h:MM TT";
  var html = '';
	var len = (prefs.limit && (parseInt(prefs.limit) > 0) &&  (parseInt(prefs.limit) < data.length))? prefs.limit:data.length;
	if(prefs.timeformat == "24h") timemask = "HH:MM";
  for(var i = 0 ; i < len; i++){	
    var item = data[i];
		var time = parseInt(item.fromDateTime.time) + parseInt(userTimezoneOffset) + (new Date()).getTimezoneOffset()*60*1000;					
		time = DateTimeFormater.format(new Date(time),timemask);
		html += '<a href="javascript:void(0);" class="IconLink" onclick="eXoEventGadget.showDetail(this);"><i class="uiIconMiniArrowRight uiIconLightGray caretIcon"></i>' + time + '<span>'+ item.summary +'</span></a>';
		if(item.description) html += '<div class="EventDetail">' + item.description + '</div>';
  }
  html += '';
  cont.innerHTML = html;
	eXoEventGadget.setLink();
}

eXoEventGadget.prototype.showDetail = function(obj){
	var detail = DOMUtil.findNextElementByTagName(obj,"div");
	if(!detail) return;
	var condition = this.lastShowItem && (this.lastShowItem != detail) && (this.lastShowItem.style.display == "block"); 
	if(condition) this.lastShowItem.style.display = "none";
	if(detail.style.display == "block") detail.style.display = "none";
	else detail.style.display = "block";
	this.lastShowItem = detail;
	eXoEventGadget.adjustHeight();
}

eXoEventGadget.prototype.onLoadHander = function(){
	eXoEventGadget.getPrefs();
	eXoEventGadget.getCalendars();
	setTimeout(eXoEventGadget.adjustHeight,500);
}
eXoEventGadget.prototype.ajaxAsyncGetRequest = function(url, callback) {
	/*	
	var params = {};  
  params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
  gadgets.io.makeRequest(url, callback, params);
	return;
*/	
//	var request =  parent.eXo.core.Browser.createHttpRequest() ;
	if (!parent.eXo.core.Browser.isIE())
		var request =  new XMLHttpRequest();
	else 
		var request =  new ActiveXObject("Msxml2.XMLHTTP");
  	request.open('GET', url, true) ;
  	request.setRequestHeader("Cache-Control", "max-age=86400") ;
  	request.send(null) ;
	request.onreadystatechange = function(){
		if (request.readyState == 4) {
			if (request.status == 200) {
				var data = gadgets.json.parse(request.responseText);
				callback(data);
			}
			//IE treats a 204 success response status as 1223. This is very annoying
			if (request.status == 404  || request.status == 204  || request.status == 1223) {
				eXoEventGadget.notify();
	  	}
		}
	}					
}
eXoEventGadget.prototype.notify = function(){
	var msg = gadgets.Prefs().getMsg("noevent");
	document.getElementById("ItemContainer").innerHTML = '<div class="light_message">' + msg + '</div>';
	eXoEventGadget.setLink();
}

eXoEventGadget.prototype.adjustHeight = function(){
	setTimeout(function(){
		var frmSetting = document.getElementById("Setting");
		var gadgetCont = document.getElementById("ItemContainer").parentNode;
		var height = frmSetting.offsetHeight + gadgetCont.offsetHeight;
		gadgets.window.adjustHeight(height);		
	},500);
}

eXoEventGadget.prototype.getCalendars = function(){
	var url = eXoEventGadget.createRequestUrl();
	url = url.replace(/calendar.*$/ig,"calendar/getcalendars/");
	eXoEventGadget.ajaxAsyncGetRequest(url,eXoEventGadget.write2Setting);
}

eXoEventGadget.prototype.write2Setting = function(data){
	var frmSetting = document.getElementById("Setting");
	data = eXoEventGadget.convertCalendar(data.calendars);
	var html = '';
	for(var i=0,len = data.length; i < len;i++){
		html += '<option value="' + data[i].id + '">' + data[i].name + '</option>';
	}
	frmSetting["calendars"].innerHTML = html;
	eXoEventGadget.getData();
}

eXoEventGadget.prototype.convertCalendar = function(data){
	var arr = new Array();
	var len = data.length;
	for(var i = 0; i < len; i++){
		arr.push({"name":data[i].name,"id":data[i].calendarId});
	}
	return arr;
}

eXoEventGadget.prototype.showHideSetting = function(isShow){
	var frmSetting = document.getElementById("Setting");
	var display = "";
	if(isShow) {
		eXoEventGadget.loadSetting();
		display = "block";
	}	else display = "none";
	frmSetting.style.display = display;
	eXoEventGadget.adjustHeight();
}

eXoEventGadget.prototype.saveSetting = function(){
	var prefs = new gadgets.Prefs();
	var frmSetting = document.getElementById("Setting");
	var setting = eXoEventGadget.createSetting(frmSetting);
	prefs.set("setting",setting);	
	return false;
}

eXoEventGadget.prototype.createSetting = function(frmSetting){
	var setting = "";
	setting += frmSetting["url"].value + ";";
	setting += frmSetting["subscribeurl"].value + ";";
	setting += frmSetting["limit"].value + ";";
	setting += frmSetting["timeformat"].options[frmSetting["timeformat"].selectedIndex].text + ";";
	setting += frmSetting["calendars"].options[frmSetting["calendars"].selectedIndex].text;
	return setting;
}

eXoEventGadget.prototype.loadSetting = function(){
	var frmSetting = document.getElementById("Setting");
	frmSetting["url"].value = eXoEventGadget.prefs.url;
	frmSetting["subscribeurl"].value = eXoEventGadget.prefs.subscribeurl;
	frmSetting["limit"].value = eXoEventGadget.prefs.limit;
	eXoEventGadget.selectedValue(frmSetting["timeformat"],eXoEventGadget.prefs.timeformat);
	eXoEventGadget.selectedValue(frmSetting["calendars"],eXoEventGadget.prefs.calendars);
}

eXoEventGadget.prototype.selectedValue = function(selectbox,value){
	for(var i = 0, len = selectbox.options.length; i < len; i++){
		if(value == selectbox.options[i].text) selectbox.selectedIndex = i;
	}
}


eXoEventGadget =  new eXoEventGadget();

gadgets.util.registerOnLoadHandler(eXoEventGadget.onLoadHander);