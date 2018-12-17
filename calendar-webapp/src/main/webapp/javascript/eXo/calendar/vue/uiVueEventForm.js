import UIVueEventForm from './components/UIVueEventForm.vue';

export function init() {
  Vue.component('ui-vue-event-form', UIVueEventForm);

  new Vue({
    el: '#UIVueEventForm',
    template: '<ui-vue-event-form></ui-vue-event-form>'
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