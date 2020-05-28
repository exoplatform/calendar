import {calConstants} from './calConstants.js';
import Utils from './model/utils.js';
import Reminder from './model/reminder.js';
import Recurring from './model/recurring.js';

function buildEvent(eventJSON) {
  const calendarId = eventJSON.calendarId;

  const offset = Utils.getTimezoneOffset();
  const fromDate = new Date(eventJSON.from);
  fromDate.setHours(fromDate.getHours() - offset);
  const toDate = new Date(eventJSON.to);
  toDate.setHours(toDate.getHours() - offset);

  const reminder = new Reminder();
  if (eventJSON.reminder && eventJSON.reminder.length) {
    eventJSON.reminder.forEach(r => {
      if (r.reminderType === 'email') {
        reminder.mailReminder = true;
        reminder.mailReminderTime = r.alarmBefore;
        reminder.mailReminderId = r.id;
        reminder.mailReminderFrom = r.fromDateTime;
        reminder.emailAddress = r.emailAddress;
      } else {
        reminder.popupReminder = true;
        reminder.popupReminderTime = r.alarmBefore;
        reminder.popupReminderId = r.id;
        reminder.popupReminderFrom = r.fromDateTime;
      }
    });
  }

  const recurring = new Recurring();
  if (eventJSON.repeat && eventJSON.repeat.enabled) {
    recurring.repeatType = eventJSON.repeat.type;
    recurring.interval = eventJSON.repeat.every;
    recurring.exclude = eventJSON.repeat.exclude;
    recurring.endRepeat = 'neverEnd';
    recurring.endAfterNumber = 5;
    recurring.weekly = calConstants.WEEK_DAYS[new Date().getDay()];
    recurring.monthly = 'monthlyByMonthDay';

    const type = eventJSON.repeat.type;
    if (type === 'weekly') {
      recurring.weekly = eventJSON.repeat.repeatOn.split(',');
    } else if (type === 'monthly') {
      if (eventJSON.repeat.repeateBy) {
        recurring.monthly = 'monthlyByMonthDay';
      } else {
        recurring.monthly = 'monthlyByDay';
      }
    }

    if (eventJSON.repeat.end) {
      recurring.endRepeat = eventJSON.repeat.end.type;
      if (eventJSON.repeat.end.type === 'endAfter') {
        recurring.endAfterNumber = parseInt(eventJSON.repeat.end.value);
      } else if (eventJSON.repeat.end.type === 'endByDate') {
        recurring.endDate = new Date(eventJSON.repeat.end.value);
      }
    }
  }

  let attachedFiles = [];
  if (eventJSON.attachments) {
    attachedFiles = eventJSON.attachments.map(att => {
      return {
        'name': att.name,
        'size': att.weight,
        'progress': 100
      };
    });
  }

  return {
    id: eventJSON.id,
    title: eventJSON.subject,
    calendar: calendarId,
    category: eventJSON.categoryId,
    fromDate: fromDate,
    toDate: toDate,
    location: eventJSON.location,
    participants: eventJSON.participants,
    description: eventJSON.description,
    attachedFiles: attachedFiles,
    reminder: reminder,
    recurring: recurring,
    recurrenceId: eventJSON.recurrenceId,
    isOccur: eventJSON.isOccur
  };
}

export function getEventById(eventId, isOccur, recurId, startTime, endTime) {
  if (isOccur && isOccur !== 'false' && recurId) {
    const hour = 3600000;
    const start = new Date(parseInt(startTime) - hour);
    const end = new Date(parseInt(endTime) + hour);
    return fetch(`${calConstants.CAL_SERVER_API}events/${eventId}/occurrences?start=${start.toISOString()}&end=${end.toISOString()}&expand=attachments`, {headers: calConstants.HEADER_NO_CACHE})
      .then(resp =>  resp.json())
      .then(json =>  {
        if (json && json.data && json.data.length) {
          return buildEvent(json.data[0]);
        } else {
          return null;
        }
      });
  } else {
    return fetch(`${calConstants.CAL_SERVER_API}events/${eventId}/?expand=attachments`, {headers: calConstants.HEADER_NO_CACHE})
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

export function saveEvent(form) {
  const isCurrentDST = Utils.isDST(new Date());
  const from = form.event.fromDate;
  const isFromDST = Utils.isDST(from);
  if(!isCurrentDST && isFromDST) {
    from.setHours(from.getHours() - 1);
  } else if(isCurrentDST && !isFromDST) {
    from.setHours(from.getHours() + 1);
  }
  const fromDate = new Date(Date.UTC(from.getFullYear(), from.getMonth(), from.getDate(), from.getHours() - calConstants.SETTINGS.timezone/ calConstants.ONE_HOUR_MINUTES, from.getMinutes(), from.getSeconds()));
  
  const to = form.event.toDate;
  const isToDST = Utils.isDST(to);
  if(!isCurrentDST && isToDST) {
    to.setHours(to.getHours() - 1);
  } else if(isCurrentDST && !isToDST) {
    to.setHours(to.getHours() + 1);
  }
  const toDate = new Date(Date.UTC(to.getFullYear(), to.getMonth(), to.getDate(), to.getHours() - calConstants.SETTINGS.timezone/ calConstants.ONE_HOUR_MINUTES, to.getMinutes(), to.getSeconds()));

  const event = {
    id: form.event.id,
    subject: form.event.title,
    description: form.event.description,
    from: fromDate.toISOString(),
    to: toDate.toISOString(),
    calendarId: form.event.calendar,
    categoryId: form.event.category,
    location: form.event.location,
    uploadResources: [],
    participants: form.event.participants,
    reminder: [],
    recurrenceId: form.event.recurrenceId
  };

  if (form.enableReminder) {
    const reminder = [];
    if (form.event.reminder.mailReminder) {
      reminder.push({
        id: form.event.reminder.mailReminderId,
        reminderType: 'email',
        alarmBefore: form.event.reminder.mailReminderTime,
        emailAddress: form.event.reminder.emailAddress
      });
    }
    if (form.event.reminder.popupReminder) {
      reminder.push({
        id: form.event.reminder.popupReminderId,
        reminderType: 'popup',
        alarmBefore: form.event.reminder.popupReminderTime
      });
    }
    event.reminder = reminder;
  }

  if (form.enableRecurring) {
    const repeat = {
      type: form.event.recurring.repeatType,
      every: form.event.recurring.interval,
      end: {
        type: 'neverEnd'
      }
    };

    if (form.event.recurring.repeatType === 'weekly') {
      repeat.repeatOn = form.event.recurring.weekly.join(',');
    } else if (form.event.recurring.repeatType === 'monthly') {
      if (form.event.recurring.monthly === 'monthlyByMonthDay') {
        repeat.repeateBy = fromDate.getDate();
      } else {
        repeat.repeatOn = calConstants.WEEK_DAYS[fromDate.getDay()];
      }
    }

    if (form.event.recurring.endRepeat === 'endAfter') {
      repeat.end.value = form.event.recurring.endAfterNumber;
      repeat.end.type = 'endAfter';
    } else if (form.event.recurring.endRepeat === 'endByDate') {
      repeat.end.value = form.event.recurring.endDate.toISOString();
      repeat.end.type = 'endByDate';
    }

    event.repeat = repeat;
  }

  const uploadResources = [];
  if (form.event.attachedFiles && form.event.attachedFiles.length) {
    form.event.attachedFiles.map(attachFile => {
      if (attachFile.uploadId) {
        uploadResources.push({
          id: attachFile.uploadId,
          name: attachFile.name,
          weight: attachFile.file.size,
          mimeType: attachFile.file.type
        });
      } else {
        uploadResources.push({
          name: attachFile.name,
          weight: attachFile.size
        });
      }
    });
  }
  event.uploadResources = uploadResources;

  if (form.event.id) {
    //update
    const queryString = form.recurringUpdateType ? `?recurringUpdateType=${form.recurringUpdateType}` : '';
    return fetch(`${calConstants.CAL_SERVER_API}events/${form.event.id}${queryString}`, {
      headers: new Headers({
        'Content-Type': 'application/json'
      }),
      method: 'PUT',
      body: JSON.stringify(event)
    });
  } else {
    //create
    return fetch(`${calConstants.CAL_SERVER_API}calendars/${form.event.calendar}/events`, {
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

export function getAvailabilities(usernames, fromDate, toDate) {
  const from = new Date(fromDate);
  from.setHours(0, 0, 0, 0);
  const to = new Date(toDate);
  to.setDate(to.getDate() + 1);
  to.setHours(0, 0, 0, 0);

  const miliseconds = 3600000;
  const offset = Utils.getTimezoneOffset() * miliseconds;
  fromDate = from.getTime() + offset;
  toDate = to.getTime() + offset;

  return fetch(`${calConstants.CAL_SERVER_API}availabilities?usernames=${usernames.join(',')}&fromDate=${fromDate}&toDate=${toDate}`, {headers: calConstants.HEADER_NO_CACHE})
    .then(resp =>  resp.json());
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

export function getDateForme() {
  return fetch(`${calConstants.CAL_SERVER_API}getDateFormat`, {headers: calConstants.HEADER_NO_CACHE})
    .then(resp =>  resp.json()).then(json => {
      if (json) {
        return json;
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

  return fetch(`${calConstants.CAL_SERVER_API}calendars?limit=100`, {headers: calConstants.HEADER_NO_CACHE})
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
