import {calConstants} from './calConstants.js';

function buildDate(date, time) {
  const d = new Date(date);
  const t = time.split(':');
  d.setHours(parseInt(t[0]));
  d.setMinutes(parseInt(t[1]));
  return d;
}

export function saveEvent(evt) {
  const fromDate = buildDate(evt.fromDate, evt.fromTime);
  const toDate = buildDate(evt.toDate, evt.toTime);

  const event = {
    subject: evt.title,
    description: evt.description,
    from: fromDate,
    to: toDate,
    categoryId: evt.category,
    location: evt.location,
    attachments: evt.attachedFiles,
    participants: evt.participants
  };

  if (evt.enableReminder) {
    const reminder = [];
    if (evt.reminder.mailReminder) {
      reminder.push({
        reminderType: 'TYPE_EMAIL',
        alarmBefore: evt.reminder.mailReminderTime
      });
    }
    if (evt.reminder.popupReminder) {
      reminder.push({
        reminderType: 'TYPE_POPUP',
        alarmBefore: evt.reminder.popupReminderTime
      });
    }
    event.reminder = reminder;
  }

  if (evt.enableRecurring) {
    const repeat = {
      every: evt.recurring.interval,
      end: {}
    };

    if (evt.recurring.repeatType === 'daily') {
      repeat.type = 'RP_DAILY';
      repeat.end.type = 'RP_DAILY';
    } else if (evt.recurring.repeatType === 'weekly') {
      repeat.type = 'RP_WEEKLY';
      repeat.end.type = 'RP_WEEKLY';
      repeat.repeatOn = evt.recurring.weekly;
    } else if (evt.recurring.repeatType === 'monthly') {
      repeat.type = 'RP_MONTHLY';
      repeat.end.type = 'RP_MONTHLY';
      if (evt.recurring.monthly === 'monthlyByMonthDay') {
        repeat.repeateBy = new Date().getDate();
      }
    } else if (evt.recurring.repeatType === 'yearly') {
      repeat.type = 'RP_YEARLY';
      repeat.end.type = 'RP_YEARLY';
    }

    if (evt.recurring.endRepeat === 'endAfter') {
      repeat.end.value = evt.recurring.endAfterNumber;
    } else if (evt.recurring.endRepeat === 'endByDate') {
      repeat.end.value = new Date(evt.recurring.endDate).toISOString();
    }

    event.repeat = repeat;
  }

  return fetch(`${calConstants.CAL_SERVER_API}calendars/${evt.calendar}/events`, {
    headers: new Headers({
      'Content-Type': 'application/json'
    }),
    method: 'post',
    body: JSON.stringify(event)
  });
}

const DEFAULT_LIMIT = 20;
export function findParticipants(filter, limit) {
  if(!limit) {
    limit = DEFAULT_LIMIT;
  }
  return fetch(`${calConstants.CAL_SERVER_API}participants?filter=${filter}&limit=${limit}`)
    .then(resp =>  resp.json());
}

export function getCategories() {
  return fetch(`${calConstants.CAL_SERVER_API}categories`)
    .then(resp =>  resp.json()).then(json => {
      if (json && json.data) {
        return json.data;
      } else {
        return [];
      }
    });
}

export function getCalendars() {
  const groupMap = {
    'PERSONAL': {'id': 0, 'name': 'privateCalendar'},
    'SHARED': {'id': 1, 'name': 'sharedCalendar'},
    'GROUP': {'id': 2, 'name': 'publicCalendar'}
  };

  return fetch(`${calConstants.CAL_SERVER_API}calendars`)
    .then(resp =>  resp.json()).then(json => {
      if (json && json.data) {
        const calendarGroups = [];
        //
        json.data.forEach(calendar => {
          let calGroup = calendarGroups.filter(group => group.type === calendar.type);
          //
          if (!calGroup.length) {
            calGroup = [{
              id: groupMap[calendar.type].id,
              name: groupMap[calendar.type].name,
              type: calendar.type,
              calendars: []
            }];
            calendarGroups.push(calGroup[0]);
          }
          calGroup[0].calendars.push(calendar);
        });

        return calendarGroups;
      } else {
        return [];
      }
    });
}