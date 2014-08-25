(function() {
  var dateUtils = {
      /**
       * Convert time from milliseconds to minutes
       * @param {Int} Milliseconds Milliseconds
       */
      timeToMin : function(milliseconds) {
        if (typeof(milliseconds) == "string") milliseconds = parseInt(milliseconds);
        var d = new Date(milliseconds);
        var hour = d.getHours();
        var min = d.getMinutes();
        var min = hour * 60 + min;
        return min;
      },
      
      /**
       * Convert time from minutes to string
       * @param {Int} min  Minutes
       * @param {String} timeFormat  Format string of time
       * @return minutes
       */
      minToTime : function(min, timeFormat) {
        var UICalendarPortlet = window.require("PORTLET/calendar/CalendarPortlet").UICalendarPortlet;
        var minutes = min % 60;
        var hour = (min - minutes) / 60;
        if (hour < 10)
          hour = "0" + hour;
        if (minutes < 10)
          minutes = "0" + minutes;
        if (UICalendarPortlet.timeFormat != "hh:mm a")
          return hour + ":" + minutes;
        if(hour == 0) {
          hour = 12;
          return hour + ":" + minutes + timeFormat.am;
        }
        var time = hour + ":" + minutes;
        if (!timeFormat)
          return time;
        if (hour < 12)
          time += " " + timeFormat.am;
        else
          if (hour == 12)
            time += " " + timeFormat.pm;
          else {
            hour -= 12;
            if (hour < 10)
              hour = "0" + hour;
            time = hour + ":" + minutes;
            time += " " + timeFormat.pm;
          }
        return time;
      },
      
      /**
       * Gets begining day
       * @param {Object} millis Milliseconds
       * @return date object Date object
       */
      getBeginDay : function(millis) {
        var d = new Date(parseInt(millis));
        var date = d.getDate();
        var month = d.getMonth() + 1;
        var year = d.getFullYear();
        var strDate = month + "/" + date + "/" + year + " 00:00:00 AM";
        return Date.parse(strDate);
      },
      
      /**
       * Gets difference of two days
       * @param {Object} start Beginning date in milliseconds
       * @param {Object} end Ending date in milliseconds
       * @return Difference of two days
       */
      dateDiff : function(start, end) {
        var start = this.getBeginDay(start);
        var end = this.getBeginDay(end);
        var msDiff = end - start;
        var dateDiff = msDiff / (24 * 60 * 60 * 1000 - 1000);
        return Math.round(dateDiff);
      },
      
      /**
       * Apply time setting for Calendar portet
       * @param {Object} time Timi in milliseconds
       * @param {Object} settingTimeZone Timezone offset of user setting
       * @param {Object} severTimeZone Timezone offset of server
       */
      toSettingTime : function(time, settingTimeZone, severTimeZone) {
        var GMT = time - (3600000 * serverTimeZone);
        var settingTime = GMT + (3600000 * settingTimeZone);
        return settingTime;
      },
      
      /**
       * Gets full year from date object
       * @param {Object} date Date object
       * @return Full year
       */
      getYear : function(date) {
        x = date.getYear();
        var y = x % 100;
        y += (y < 38) ? 2000 : 1900;
        return y;
      },
      
      /**
       * Gets day from time in milliseconds
       * @param {Object} milliseconds Time in milliseconds
       * @return Day of week
       */
      getDay : function(milliseconds) {
        var d = new Date(milliseconds);
        var day = d.getDay();
        return day;
      },
      
      /**
       * Checks time is beginning of date or not
       * @param {Object} milliseconds Time in milliseconds
       * @return Boolean value
       */
      isBeginDate : function(milliseconds) {
        var d = new Date(milliseconds);
        var hour = d.getHours();
        var min = d.getMinutes();
        if ((hour == 0) && (hour == min))
          return true;
        return false;
      },
      
      /**
       * Checks time is beginning of week or not
       * @param {Object} milliseconds Time in milliseconds
       * @return Boolean value
       */
      isBeginWeek : function(milliseconds) {
        var d = new Date(milliseconds);
        var day = d.getDay();
        var hour = d.getHours();
        var min = d.getMinutes();
        if ((day == 0) && (hour == 0) && (min == 0))
          return true;
        return false;
      },
      
      /**
       * Gets number of week in current year
       * @param {Object} now Time in milliseconds
       * @return number of week
       */
      getWeekNumber : function(now) {
        var today = new Date(now);
        var Year = this.getYear(today);
        var Month = today.getMonth();
        var Day = today.getDate();
        var now = Date.UTC(Year, Month, Day + 1, 0, 0, 0);
        var Firstday = new Date();
        Firstday.setYear(Year);
        Firstday.setMonth(0);
        Firstday.setDate(1);
        var then = Date.UTC(Year, 0, 1, 0, 0, 0);
        var Compensation = Firstday.getDay();
        if (Compensation > 3)
          Compensation -= 4;
        else
          Compensation += 3;
        var NumberOfWeek = Math.round((((now - then) / 86400000) + Compensation) / 7);
        return NumberOfWeek;
      },
      
      /**
       * Localizes time
       * @param {Object} millis Time in minutes
       * @param {Object} timezoneOffset Timezone offset of current user
       * @return Time in minutes
       */
      localTimeToMin : function(millis, timezoneOffset) {
        if (typeof(millis) == "string")
          millis = parseInt(millis);
        millis += timezoneOffset * 60 * 1000;
        var d = new Date(millis);
        var hour = d.getHours();
        var min = d.getMinutes();
        var min = hour * 60 + min;
        return min;
      },
      
      /**
       * Parses time from string
       * @param {Object} string String
       * @param {Object} timezoneOffset Timezone offset of user
       * @return Object contains two properties that are from and to
       */
      parseTime : function(string, timezoneOffset) {
        var stringTime = string.split(",");
        var len = stringTime.length;
        var time = new Array();
        var tmp = null;
        for (var i = 0; i < len; i += 2) {
          tmp = {
              "from": this.localTimeToMin(stringTime[i], timezoneOffset),
              "to": this.localTimeToMin(stringTime[i + 1], timezoneOffset)
          }
          time.push(tmp);
        }
        return time;
      },
      
      dateParses : function(dateFieldValue, pattern) {
        var dateIndex =   pattern.indexOf("dd");
        var dateValue = parseInt(dateFieldValue.substring(dateIndex,dateIndex + 2), 10);
        var monthIndex =   pattern.indexOf("MM");
        var monthValue = parseInt(dateFieldValue.substring(monthIndex,monthIndex + 2) - 1, 10);
        var yearIndex =   pattern.indexOf("yyyy");
        var yearValue = parseInt(dateFieldValue.substring(yearIndex,yearIndex + 4), 10);
        var currentDate = new Date();
        currentDate.setDate(dateValue);
        currentDate.setMonth(monthValue);
        currentDate.setYear(yearValue);
        return currentDate;
      }
  };
  return dateUtils;
  
})();