<template>
  <div id="errorMessage" class="errorMessage">
    <div>
      <ul>
        <li v-for="error in errors" :key="error">- <span>{{ errorMsg(error) }}</span></li>
      </ul>
    </div>
    <div class="uiAction uiActionBorder">
      <button class="btn" type="button" @click="close">
        {{ $t('ExoEventForm.btn.close') }}
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
    errorMsg(error) {
      let key = `UIEventForm.msg.${error}`;
      let msg = this.$t(key);

      if (msg === key) {
        key = `ExoEventForm.error.${error}`;
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
