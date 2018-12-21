import ExoEventForm from './components/ExoEventForm.vue';

export function init() {
  Vue.component('exo-event-form', ExoEventForm);

  new Vue({
    el: '#ExoEventForm',
    template: '<exo-event-form></exo-event-form>'
  });
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