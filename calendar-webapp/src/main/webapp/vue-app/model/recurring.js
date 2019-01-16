class Recurring {
  constructor() {
    const endRecurring = new Date();
    const oneWeek = 7;
    endRecurring.setDate(endRecurring.getDate() + oneWeek);

    this.repeatType = 'weekly';
    this.interval = 1;
    this.weekly = ['TU'];
    this.monthly = 'monthlyByMonthDay';
    this.endRepeat = 'neverEnd';
    this.endAfterNumber = 5;
    this.endDate = endRecurring;
  }
}

export default Recurring;