<template>
  <div id="eventReminder" class="uiEventReminder">
    <div class="reminderByEmail">
      <div id="mailReminder" class="mailReminder">
        <label class="uiCheckbox">
          <input id="mailReminder" type="checkbox" class="checkbox" name="mailReminder" v-model="reminder.mailReminder"><span>{{ $t('UIEventForm.label.mailReminder') }}</span>
        </label>
      </div>
      <div>
        <table class="uiFormGrid reminderTable" v-show="reminder.mailReminder">
          <tbody>
            <tr>
              <td class="fieldComponent selectboxSmall">
                <span class="uiSelectbox">
                  <select class="selectbox" name="mailReminderTime" id="mailReminderTime" v-model="reminder.mailReminderTime">
                    <option v-for="i in [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60]" :value="i">{{ i }} {{ $t('UIEventForm.label.minutes') }}</option>
                  </select>
                </span>
                {{ $t('UIEventForm.label.mailReminderTime') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div>
      <div class="reminderByPopup">
        <div id="popupReminder" class="popupReminder">
          <label class="uiCheckbox">
            <input id="popupReminder" type="checkbox" class="checkbox" name="popupReminder" v-model="reminder.popupReminder"><span>Display a notification pop-up</span>
          </label>
        </div>
      </div>
      <div>
        <table class="uiFormGrid" v-show="reminder.popupReminder">
          <tbody>
            <tr>
              <td class="fieldComponent selectboxSmall">
                <span class="uiSelectbox">
                  <select class="selectbox" name="popupReminderTime" id="popupReminderTime" v-model="reminder.popupReminderTime">
                    <option v-for="i in [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60]" :value="i">{{ i }} {{ $t('UIEventForm.label.minutes') }}</option>
                  </select>
                </span>
                {{ $t('UIEventForm.label.popupReminderTime') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <div class="uiAction uiActionBorder">
      <button class="btn" type="button" @click="closeForm">
        {{ $t('ExoEventForm.btn.save') }}
      </button>

      <button class="btn" type="button" @click="closeForm">
        {{ $t('ExoEventForm.btn.cancel') }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  model: {
    prop: 'reminder',
    event: 'change'
  },
  props: {
    'reminder': {
      type: Object,
      default: {}
    }
  },
  watch: {
    reminder: {
      handler(val) {
        this.$emit('change', this.reminder);
      },
      deep: true
    }
  },
  methods: {
    closeForm() {
      this.$emit('closeForm');
    }
  }
};
</script>
