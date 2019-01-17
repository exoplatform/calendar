import Utils from './utils.js';

class Reminder {
  constructor() {
    this.mailReminder = false;
    this.popupReminder = false;
    this.mailReminderTime = 5;
    this.popupReminderTime = 5;
  }

  isEnabled() {
    return this.mailReminder || this.popupReminder;
  }

  clone() {
    const data = new Reminder();
    Utils.copyObj(data, this);
    return data;
  }
}

export default Reminder;