import {calConstants} from './calConstants.js';
import ExoEventForm from './components/ExoEventForm.vue';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
const url = `${calConstants.PORTAL}/${calConstants.PORTAL_REST}/i18n/bundle/locale.portlet.calendar.CalendarPortlet-${lang}.json`;

//
let vm = null;
export function init() {
  if ($('#ExoEventForm').length && vm == null) {
    exoi18n.loadLanguageAsync(lang, url).then(i18n => {
      vm = new Vue({
        el: '#ExoEventForm',
        components: {
          'exo-event-form': ExoEventForm
        },
        data: {
          openDrawer: false
        },
        methods: {
          toggleDrawer() {
            this.openDrawer = !this.openDrawer;
          }
        },
        template: '<exo-event-form v-model="openDrawer"></exo-event-form>',
        i18n
      });
      setTimeout(() => {
        vm.toggleDrawer();
      },0);
    });
  } else {
    vm.toggleDrawer();
  }
}

// A global data
Vue.mixin({
  data: function() {
    return {
      mq: ''
    };
  },
  created() {
    this.handleMediaQuery();
    window.addEventListener('resize', this.handleMediaQuery);
  },
  methods: {
    handleMediaQuery() {
      if (window.matchMedia('(max-width: 767px)').matches) {
        this.mq = 'mobile';
      } else if (window.matchMedia('(max-width: 1024px)').matches) {
        this.mq = 'tablet';
      } else {
        this.mq = 'desktop';
      }
    }
  }
});