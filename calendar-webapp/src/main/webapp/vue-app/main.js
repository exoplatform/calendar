import {calConstants} from './calConstants.js';
import ExoCalendarEventForm from './components/ExoCalendarEventForm.vue';
import * as calServices from './calServices.js';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
const url = `${calConstants.PORTAL}/${calConstants.PORTAL_REST}/i18n/bundle/locale.portlet.calendar.CalendarPortlet-${lang}.json`;

Vue.component('exo-event-form', ExoCalendarEventForm);
//
let vm = null;
export function init(settings) {
  calConstants.SETTINGS = settings;

  if ($('#ExoCalendarEventForm').length) {
    exoi18n.loadLanguageAsync(lang, url).then(i18n => {
      vm = new Vue({
        el: '#ExoCalendarEventForm',
        data: {
          calEvt: {},
          showEventForm: false
        },
        watch: {
          showEventForm() {
            $(document.body).toggleClass('hide-scroll');
          }
        },
        methods: {
          openEventForm(calEvt) {
            if (calEvt.id) {
              calServices.getEventById(calEvt.id, calEvt.isOccur, calEvt.recurId, calEvt.startTime, calEvt.endTime).then(evt => {
                if (evt) {
                  this.calEvt = evt;
                  this.showEventForm = true;
                }
              });
            } else {
              this.calEvt = calEvt;
              this.showEventForm = true;
            }
          }
        },
        template: '<exo-event-form v-model="showEventForm" :initEvt="calEvt"></exo-event-form>',
        i18n
      });
    });
  }
}

export function openEventForm(calEvt) {
  vm.openEventForm(calEvt);
}

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('calendar');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}