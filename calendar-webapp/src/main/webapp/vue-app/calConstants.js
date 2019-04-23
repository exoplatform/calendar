const headers = new Headers();
headers.append('pragma', 'no-cache');
headers.append('cache-control', 'no-cache');

export const calConstants = {
  PORTAL: eXo.env.portal.context || '',
  PORTAL_NAME: eXo.env.portal.portalName || '',
  PORTAL_REST: eXo.env.portal.rest,
  CAL_SERVER_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/calendar/`,
  UPLOAD_API: `${eXo.env.portal.context}/upload`,
  HEADER_NO_CACHE: headers,
  SETTINGS: {},
  NO_REPEAT: 'norepeat',
  WEEK_DAYS: ['SU', 'MO', 'TU', 'WE', 'TH', 'FR', 'SA'],
  MAX_UPLOAD_FILES: 10,
  MAX_UPLOAD_SIZE: 10,
  ONE_HOUR_MINUTES: 60,
  ONE_DAY_HOURS: 24,
  ONE_WEEK_DAYS: 7
};