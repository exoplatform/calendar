import {calConstants} from './calConstants.js';

export function findParticipants(filter, limit) {
//  if(!limit) {
//    limit = DEFAULT_USER_LIMIT;
//  }
//  return fetch(`${calConstants.CAL_SERVER_API}participants?filter=${filter}&limit=${limit}`)
//    .then(resp =>  resp.json());
  console.log(filter + limit);
  return new Promise((resolve) => {
    resolve([
      {'id': 'root', 'name': 'root name', 'avatar': ''},
      {'id': 'demo', 'name': 'demo name', 'avatar': ''},
      {'id': 'john', 'name': 'john name', 'avatar': ''},
      {'id': 'mary', 'name': 'mary name', 'avatar': ''}
    ]);
  });
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
  }

  return fetch(`${calConstants.CAL_SERVER_API}calendars`)
    .then(resp =>  resp.json()).then(json => {
      if (json && json.data) {
        let calendarGroups = [];
        //
        json.data.forEach(calendar => {
          let calGroup = calendarGroups.filter(group => group.type == calendar.type);
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