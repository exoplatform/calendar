import {calConstants} from './calConstants.js';

function buildDate(date, time) {
  const d = new Date(date);
  const t = time.split(':');
  d.setHours(parseInt(t[0]));
  d.setMinutes(parseInt(t[1]));
  return d;
}

function buildEvent(event) {
  let calendarId = event.calendar;
  const idx = calendarId.lastIndexOf('/');
  if (idx !== -1) {
    calendarId = calendarId.slice(idx + 1);
  }

  const minute = 60;
  const fromDate = new Date(event.from);
  const timezone = (fromDate.getTimezoneOffset() * -1 - calConstants.SETTINGS.timezone) / minute;
  fromDate.setHours(fromDate.getHours() + timezone);
  const toDate = new Date(event.to);
  toDate.setHours(toDate.getHours() + timezone);

  const reminder = {
    mailReminderTime: 5,
    popupReminderTime: 5
  };
  if (event.reminder && event.reminder.length) {
    event.reminder.forEach(r => {
      if (r.reminderType === 'TYPE_EMAIL') {
        reminder.mailReminder = true;
        reminder.mailReminderTime = r.alarmBefore;
        reminder.mailReminderId = r.id;
        reminder.mailReminderFrom = r.fromDateTime;
      } else {
        reminder.popupReminder = true;
        reminder.popupReminderTime = r.alarmBefore;
        reminder.popupReminderId = r.id;
        reminder.popupReminderFrom = r.fromDateTime;
      }
    });
  }

  return {
    id: event.id,
    title: event.subject,
    calendar: calendarId,
    category: event.categoryId,
    from: fromDate.getTime(),
    to: toDate.getTime(),
    location: event.location,
    participants: event.participants,
    description: event.description,
    attachedFiles: [],
    reminder: reminder,
    recurring: {
    }
  };
}

export function getCalendarById(calendarId) {
  return fetch(`${calConstants.CAL_SERVER_API}calendars/${calendarId}`, {headers: calConstants.HEADER_NO_CACHE})
    .then(resp =>  resp.json()).then(calendar => {
      if (calendar) {
        return {
          id: calendar.id,
          name: calendar.name,
          description: calendar.description,
          type: calendar.type,
          timeZone: calendar.timeZone
        };
      } else {
        return null;
      }
    });
}

export function getEventById(eventId, recurId, startTime, endTime) {
  if (recurId) {
    const start = new Date(parseInt(startTime));
    const end = new Date(parseInt(endTime));
    return fetch(`${calConstants.CAL_SERVER_API}events/${eventId}/occurrences?start=${start.toISOString()}&end=${end.toISOString()}`, {headers: calConstants.HEADER_NO_CACHE})
      .then(resp =>  resp.json())
      .then(event =>  {
        if (event) {
          return buildEvent(event);
        } else {
          return null;
        }
      }).then(resp => resp.json());
  } else {
    return fetch(`${calConstants.CAL_SERVER_API}events/${eventId}/`, {headers: calConstants.HEADER_NO_CACHE})
      .then(resp =>  resp.json())
      .then(event =>  {
        if (event) {
          return buildEvent(event);
        } else {
          return null;
        }
      });
  }
}

export function saveEvent(evt) {
  const fromDate = buildDate(evt.fromDate, evt.fromTime);
  const toDate = buildDate(evt.toDate, evt.toTime);

  const event = {
    subject: evt.title,
    description: evt.description,
    from: fromDate.toISOString(),
    to: toDate.toISOString(),
    categoryId: evt.category,
    location: evt.location,
    uploadResources: [],
    participants: evt.participants
  };

  if (evt.enableReminder) {
    const reminder = [];
    if (evt.reminder.mailReminder) {
      reminder.push({
        id: evt.reminder.mailReminderId,
        reminderType: 'TYPE_EMAIL',
        alarmBefore: evt.reminder.mailReminderTime,
        fromDateTime: evt.reminder.mailReminderFrom
      });
    }
    if (evt.reminder.popupReminder) {
      reminder.push({
        id: evt.reminder.popupReminderId,
        reminderType: 'TYPE_POPUP',
        alarmBefore: evt.reminder.popupReminderTime,
        fromDateTime: evt.reminder.popupReminderFrom
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

  if (evt.attachedFiles && evt.attachedFiles.length) {
    event.uploadResources = evt.attachedFiles.map(attachFile => {
      return {
        id: attachFile.uploadId,
        name: attachFile.name,
        weight: attachFile.file.size,
        mimeType: attachFile.file.type
      };
    });
  }

  if (evt.id) {
    return fetch(`${calConstants.CAL_SERVER_API}events/${evt.id}`, {
      headers: new Headers({
        'Content-Type': 'application/json'
      }),
      method: 'PUT',
      body: JSON.stringify(event)
    });
  } else {
    return fetch(`${calConstants.CAL_SERVER_API}calendars/${evt.calendar}/events`, {
      headers: new Headers({
        'Content-Type': 'application/json'
      }),
      method: 'POST',
      body: JSON.stringify(event)
    });
  }
}

const DEFAULT_LIMIT = 30;
export function findParticipants(filter, limit) {
  if(!limit) {
    limit = DEFAULT_LIMIT;
  }
  return fetch(`${calConstants.CAL_SERVER_API}participants?name=${filter}&limit=${limit}`, {headers: calConstants.HEADER_NO_CACHE})
    .then(resp =>  resp.json()).then(json => json.data);
}

export function getCategories() {
  return fetch(`${calConstants.CAL_SERVER_API}categories`, {headers: calConstants.HEADER_NO_CACHE})
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

  return fetch(`${calConstants.CAL_SERVER_API}calendars`, {headers: calConstants.HEADER_NO_CACHE})
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