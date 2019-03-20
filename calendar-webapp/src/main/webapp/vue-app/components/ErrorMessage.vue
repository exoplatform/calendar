<template>
  <div class="errorMessage">
    <ul class="singleMessage popupMessage">
      <li v-for="msg in errors" :key="msg"><span class="warningIcon">{{ displayMsg(msg) }}</span></li>
    </ul>
    <div class="uiAction uiActionBorder">
      <button class="btn" type="button" @click="close">
        {{ $t('ExoCalendarEventForm.btn.close') }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  model: {
    prop: 'errors',
    event: 'change'
  },
  props: {
    'errors': {
      type: Array,
      default() {
        return [];
      }
    }
  },
  methods: {
    displayMsg(error) {
      let key = `UIEventForm.msg.${error}`;
      let msg = this.$t(key);

      if (msg === key) {
        key = `ExoCalendarEventForm.error.${error}`;
        msg = this.$t(key);
      }
      msg = msg === key ? this.$t(error) : msg;
      return msg;
    },
    close() {
      this.errors = [];
      this.$emit('change', this.errors);
    }
  }
};
</script>
