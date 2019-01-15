import {calConstants} from './calConstants.js';

function getTimezoneOffset() {
  const date = new Date();
  const minute = 60;
  const offset = (date.getTimezoneOffset() * -1 - calConstants.SETTINGS.timezone) / minute;
  return offset;
}

function buildDate(date, time) {
  const minute = 60;
  const d = new Date(date);
  const t = time.split(':');
  d.setUTCHours(parseInt(t[0]) - calConstants.SETTINGS.timezone / minute);
  d.setUTCMinutes(parseInt(t[1]));
  return d;
}

function buildEvent(event) {
  let calendarId = event.calendar;
  const idx = calendarId.lastIndexOf('/');
  if (idx !== -1) {
    calendarId = calendarId.slice(idx + 1);
  }

  const offset = getTimezoneOffset();
  const fromDate = new Date(event.from);
  fromDate.setHours(fromDate.getHours() - offset);
  const toDate = new Date(event.to);
  toDate.setHours(toDate.getHours() - offset);

  let reminder = null;
  if (event.reminder && event.reminder.length) {
    reminder = {};
    event.reminder.forEach(r => {
      if (r.reminderType === 'email') {
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

  let recurring = null;
  if (event.repeat && event.repeat.enabled) {
    recurring = {
      repeatType: event.repeat.type,
      interval: event.repeat.every,
      exclude: event.repeat.exclude,
      endRepeat: 'neverEnd',
      endAfterNumber: 5,
      weekly: calConstants.WEEK_DAYS[new Date().getDay()],
      monthly: 'monthlyByMonthDay'
    };

    const type = event.repeat.type;
    if (type === 'weekly') {
      recurring.weekly = event.repeat.repeatOn.split(',');
    } else if (type === 'monthly') {
      if (event.repeat.repeateBy) {
        recurring.monthly = 'monthlyByMonthDay';
      } else {
        recurring.monthly = 'monthlyByDay';
      }
    }

    if (event.repeat.end) {
      recurring.endRepeat = event.repeat.end.type;
      if (event.repeat.end.type === 'endAfter') {
        recurring.endAfterNumber = parseInt(event.repeat.end.value);
      } else if (event.repeat.end.type === 'endByDate') {
        recurring.endDate = new Date(event.repeat.end.value);
      }
    }
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
    recurring: recurring,
    recurrenceId: event.recurrenceId,
    isOccur: event.isOccur
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

export function getEventById(eventId, isOccur, recurId, startTime, endTime) {
  if (isOccur && isOccur !== 'false' && recurId) {
    const start = new Date(parseInt(startTime));
    const end = new Date(parseInt(endTime));
    return fetch(`${calConstants.CAL_SERVER_API}events/${eventId}/occurrences?start=${start.toISOString()}&end=${end.toISOString()}`, {headers: calConstants.HEADER_NO_CACHE})
      .then(resp =>  resp.json())
      .then(json =>  {
        if (json && json.data && json.data.length) {
          return buildEvent(json.data[0]);
        } else {
          return null;
        }
      });
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
    participants: evt.participants,
    reminder: [],
    recurrenceId: evt.recurrenceId
  };

  if (evt.enableReminder) {
    const reminder = [];
    if (evt.reminder.mailReminder) {
      reminder.push({
        id: evt.reminder.mailReminderId,
        reminderType: 'email',
        alarmBefore: evt.reminder.mailReminderTime,
        fromDateTime: evt.reminder.mailReminderFrom
      });
    }
    if (evt.reminder.popupReminder) {
      reminder.push({
        id: evt.reminder.popupReminderId,
        reminderType: 'popup',
        alarmBefore: evt.reminder.popupReminderTime,
        fromDateTime: evt.reminder.popupReminderFrom
      });
    }
    event.reminder = reminder;
  }

  if (evt.enableRecurring) {
    const repeat = {
      type: evt.recurring.repeatType,
      every: evt.recurring.interval,
      end: {
        type: 'neverEnd'
      }
    };

    if (evt.recurring.repeatType === 'weekly') {
      repeat.repeatOn = evt.recurring.weekly.join(',');
    } else if (evt.recurring.repeatType === 'monthly') {
      if (evt.recurring.monthly === 'monthlyByMonthDay') {
        repeat.repeateBy = fromDate.getDate();
      } else {
        repeat.repeatOn = calConstants.WEEK_DAYS[fromDate.getDay()];
      }
    }

    if (evt.recurring.endRepeat === 'endAfter') {
      repeat.end.value = evt.recurring.endAfterNumber;
      repeat.end.type = 'endAfter';
    } else if (evt.recurring.endRepeat === 'endByDate') {
      repeat.end.value = new Date(evt.recurring.endDate).toISOString();
      repeat.end.type = 'endByDate';
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
    //update
    const queryString = evt.recurringUpdateType ? `?recurringUpdateType=${evt.recurringUpdateType}` : '';
    return fetch(`${calConstants.CAL_SERVER_API}events/${evt.id}${queryString}`, {
      headers: new Headers({
        'Content-Type': 'application/json'
      }),
      method: 'PUT',
      body: JSON.stringify(event)
    });
  } else {
    //create
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