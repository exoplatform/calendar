import {calConstants} from '../calConstants.js';

export default {
  formatDate(date) {
    const sub = -2;
    const month = `0${date.getMonth() + 1}`.slice(sub);
    const day = `0${date.getDate()}`.slice(sub);
    return `${month}-${day}-${date.getFullYear()}`;
  },

  formatTime(date) {
    const sub = -2;
    let hours = `0${date.getHours()}`.slice(sub);
    const minutes = `0${date.getMinutes()}`.slice(sub);

    const MID_DAY = 12;
    const ampm = calConstants.SETTINGS.timeFormat === 'hh:mm a';
    let s = '';
    if (ampm) {
      if (hours < MID_DAY) {
        s = ' AM';
      } else {
        s = ' PM';
      }
      hours = hours % MID_DAY;
      hours = `0${hours}`.slice(sub);
    }

    return `${hours}:${minutes}${s}`;
  },

  parseDate(dateStr, parseOptions) {
    if (!dateStr) {
      return null;
    }

    const col = parseOptions || {
      month: 0,
      date: 1,
      year: 2,
    };
    const partNum = 3;

    const parts = dateStr.split('-').map(str => {
      return parseInt(str.trim());
    }).filter(d => {
      return typeof d === 'number' && !isNaN(d);
    });

    if (parts.length === partNum) {
      const date = new Date();
      date.setFullYear(parts[col.year], parts[col.month] - 1, parts[col.date]);
      return date;
    } else {
      return null;
    }
  },

  parseTime(timeStr) {
    if (!timeStr) {
      return null;
    }

    const col = {
      hour: 0,
      minute: 1
    };
    const partNum = 2;

    const parts = timeStr.split(':').map(str => {
      return parseInt(str.trim());
    }).filter(d => {
      return typeof d === 'number' && !isNaN(d);
    });

    if (parts.length === partNum) {
      const ampm = calConstants.SETTINGS.timeFormat === 'hh:mm a';
      const date = new Date();

      const MID_DAY = 12;
      let hour = parts[col.hour];
      const minute = parts[col.minute];
      if (ampm) {
        if (timeStr.includes('PM') && hour < MID_DAY) {
          hour += MID_DAY;
        }
      }
      date.setHours(hour, minute, 0, 0);
      return date;
    } else {
      return null;
    }
  },
  
  //  Since no country observes DST that lasts for 7 months, in an area that observes DST the offset from UTC time in January will be different to the one in July.
  // So we use the timezone offset between January and July to determine the DST since JavaScript always returns a greater value during Standard Time.
  isDST(date) {
    const jan = 0;
    const jul = 6;
    const janOffset = new Date(date.getFullYear(), jan, 1).getTimezoneOffset();
    const julOffset = new Date(date.getFullYear(), jul, 1).getTimezoneOffset();
    return Math.max(janOffset, julOffset) !== date.getTimezoneOffset();
  },

  getTimezoneOffset() {
    const date = new Date();
    const minute = 60;
    const offset = (date.getTimezoneOffset() * -1 - calConstants.SETTINGS.timezone) / minute;
    return offset;
  },

  copyObj(target, source) {
    $.extend(true, target, source);
  }
};