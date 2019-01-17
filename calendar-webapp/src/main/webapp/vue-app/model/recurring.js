import {calConstants} from '../calConstants.js';
import Utils from './utils.js';

class Recurring {
  constructor() {
    const endRecurring = new Date();
    const oneWeek = 7;
    endRecurring.setDate(endRecurring.getDate() + oneWeek);

    this.repeatType = calConstants.NO_REPEAT;
    this.interval = 1;
    this.weekly = [calConstants.WEEK_DAYS[new Date().getDay()]];
    this.monthly = 'monthlyByMonthDay';
    this.endRepeat = 'neverEnd';
    this.endAfterNumber = 5;
    this.endDate = endRecurring;
    this.weekdays = calConstants.WEEK_DAYS;
  }

  isEnabled() {
    return this.repeatType !== calConstants.NO_REPEAT;
  }

  clone() {
    const data = new Recurring();
    Utils.copyObj(data, this);
    return data;
  }
}

export default Recurring;