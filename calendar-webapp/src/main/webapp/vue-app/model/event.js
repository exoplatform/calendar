import Reminder from './reminder.js';
import Recurring from './recurring.js';

const HALF_HOUR = 30;
const MID_NIGHT_HOUR = 23;
const MID_NIGHT_MINUTE = 59;

class CalendarEvent {
  constructor() {
    const fromDate = new Date();
    fromDate.setMinutes(0, 0, 0);
    const toDate = new Date();
    toDate.setMinutes(HALF_HOUR, 0, 0);


    this.id = '';
    this.title = '';
    this.calendar = '';
    this.category = '';
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.location = '';
    this.participants = [];
    this.description = '';
    this.attachedFiles = [];
    this.reminder = new Reminder();
    this.recurring = new Recurring();
    this.isOccur = false;
    this.recurrenceId = null;
  }

  setAllDay() {
    const fromDate = new Date();
    fromDate.setTime(this.fromDate.getTime());
    fromDate.setHours(0, 0, 0, 0);
    this.fromDate = fromDate;

    const toDate = new Date();
    toDate.setTime(this.toDate.getTime());
    toDate.setHours(MID_NIGHT_HOUR, MID_NIGHT_MINUTE, 0, 0);
    this.toDate = toDate;
  }

  isAllDay() {
    return this.fromDate.getHours() === 0 && this.fromDate.getMinutes() === 0 && this.toDate.getHours() === MID_NIGHT_HOUR
      && this.toDate.getMinutes() === MID_NIGHT_MINUTE;
  }

  validate() {
    const errors = [];
    if (!this.fromDate) {
      errors.push('event-fromdate-required');
    } else if (!this.toDate) {
      errors.push('event-todate-required');
    } else if (this.fromDate.getTime() > this.toDate.getTime()) {
      errors.push('event-date-time-logic');
    }

    return errors;
  }

  buildFrom(evt) {
    if (evt) {
      Object.entries(evt).forEach(entry => {
        if (entry[1] !== null) {
          if (typeof entry[1] === 'object') {
            if (entry[1].clone) {
              Vue.set(this, entry[0], entry[1].clone());
            } else if (entry[1].constructor.name === 'Date') {
              Vue.set(this, entry[0], new Date(entry[1].getTime()));
            }
          } else {
            Vue.set(this, entry[0], entry[1]);
          }
        }
      });

      if (evt.from) {
        this.fromDate = new Date(evt.from);
      }

      if (evt.to) {
        this.toDate = new Date(evt.to);
      }
    }
  }
}

export default CalendarEvent;