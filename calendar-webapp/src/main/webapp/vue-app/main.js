import {calConstants} from './calConstants.js';
import ExoEventForm from './components/ExoEventForm.vue';
import * as calServices from './calServices.js';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
const url = `${calConstants.PORTAL}/${calConstants.PORTAL_REST}/i18n/bundle/locale.portlet.calendar.CalendarPortlet-${lang}.json`;

//
let vm = null;
export function init(settings) {
  calConstants.SETTINGS = settings;

  if ($('#ExoEventForm').length && vm == null) {
    exoi18n.loadLanguageAsync(lang, url).then(i18n => {
      vm = new Vue({
        el: '#ExoEventForm',
        components: {
          'exo-event-form': ExoEventForm
        },
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
          },
          refresh() {
            window.location.reload();
          }
        },
        template: '<exo-event-form v-model="showEventForm" :initEvt="calEvt" @save="refresh"></exo-event-form>',
        i18n
      });
    });
  }
}

export function openEventForm(calEvt) {
  vm.openEventForm(calEvt);
}