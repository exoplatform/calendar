<template>
  <div id="confirmClose" class="uiEventReminder">
    <span>{{ $t('UIConfirmForm.label.confirmMessage') }}</span>
    <div class="uiAction uiActionBorder">
      <button class="btn" type="button" @click="confirm">
        {{ $t('ExoCalendarEventForm.btn.confirm') }}
      </button>
      <button class="btn" type="button" @click="cancel">
        {{ $t('ExoCalendarEventForm.btn.cancel') }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    confirm() {
      this.$emit('confirm');
    },
    cancel() {
      this.$emit('cancel');
    }
  }
};
</script>
