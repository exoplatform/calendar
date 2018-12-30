//import {calConstants} from './calConstants.js';

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