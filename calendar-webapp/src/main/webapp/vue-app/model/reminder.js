import Utils from './utils.js';

class Reminder {
  constructor() {
    this.mailReminder = false;
    this.popupReminder = false;
    this.mailReminderTime = 10;
    this.popupReminderTime = 10;
  }

  isEnabled() {
    return this.mailReminder || this.popupReminder;
  }

  getNearest() {
    if (this.mailReminder && this.popupReminder) {
      return Math.min(this.mailReminderTime, this.popupReminderTime);
    } else if (this.mailReminder) {
      return this.mailReminderTime;
    } else {
      return this.popupReminderTime;
    }
  }

  clone() {
    const data = new Reminder();
    Utils.copyObj(data, this);
    return data;
  }
}

export default Reminder;