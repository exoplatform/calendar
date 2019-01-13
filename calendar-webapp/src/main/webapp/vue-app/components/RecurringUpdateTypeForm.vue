<template>
  <div class="uiForm">
    <div class="form-horizontal">
      <div class="control-group">
        <div class="controls-full">
          <div id="confirm" class="confirm media">
            <i class="uiIconQuestion uiIconBlue pull-left"></i>
            <div class="media-body">
              {{ $t('UIConfirmFormUpdate.label.update-recurrence-event-confirm-msg') }}
            </div>
          </div>
        </div>
      </div>
      <div class="confirmRadio">
        <div>
          <label class="uiRadio">
            <input id="confirm" v-model="recurringUpdateType" class="radio" type="radio" name="confirm" value="ONE"> <span>{{ $t('confirm.label.save_one') }}</span>
          </label>
        </div>
        <div>
          <label class="uiRadio">
            <input id="confirm" v-model="recurringUpdateType" class="radio" type="radio" name="confirm" value="FOLLOWING"> <span>{{ $t('confirm.label.save_follow') }}</span>
          </label>
        </div>
        <div>
          <label class="uiRadio">
            <input id="confirm" v-model="recurringUpdateType" class="radio" type="radio" name="confirm" value="ALL"> <span>{{ $t('confirm.label.save_all') }}</span>
          </label>
        </div>
      </div>
    </div>
    <div class="uiAction uiActionBorder">
      <button class="btn" type="button" @click="saveForm">
        {{ $t('ExoEventForm.btn.save') }}
      </button>

      <button class="btn" type="button" @click="cancelForm">
        {{ $t('ExoEventForm.btn.cancel') }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  model: {
    prop: 'recurringUpdateType',
    event: 'change'
  },
  props: {
    'recurringUpdateType': {
      type: String,
      default: ''
    }
  },
  watch: {
    recurringUpdateType() {
      this.$emit('change', this.recurringUpdateType);
    }
  },
  methods: {
    cancelForm() {
      this.recurringUpdateType = '';
      this.$emit('cancelForm');
    },
    saveForm() {
      this.$emit('saveForm');
    }
  }
};
</script>
