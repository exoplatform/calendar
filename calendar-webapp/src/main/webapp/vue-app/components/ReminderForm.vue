<template>
  <div id="eventReminder" class="uiEventReminder">
    <div class="reminderByEmail">
      <div id="mailReminder" class="mailReminder">
        <label class="uiCheckbox">
          <input id="mailReminder" v-model="reminder.mailReminder" type="checkbox" class="checkbox" name="mailReminder"><span>{{ $t('UIEventForm.label.mailReminder') }}</span>
        </label>
      </div>
      <div>
        <table v-show="reminder.mailReminder" class="uiFormGrid reminderTable">
          <tbody>
            <tr>
              <td class="fieldComponent selectboxSmall">
                <span class="uiSelectbox">
                  <select id="mailReminderTime" v-model="reminder.mailReminderTime" class="selectbox" name="mailReminderTime">
                    <option v-for="i in [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60]" :key="i" :value="i">{{ i }} {{ $t('UIEventForm.label.minutes') }}</option>
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
            <input id="popupReminder" v-model="reminder.popupReminder" type="checkbox" class="checkbox" name="popupReminder"><span>Display a notification pop-up</span>
          </label>
        </div>
      </div>
      <div>
        <table v-show="reminder.popupReminder" class="uiFormGrid">
          <tbody>
            <tr>
              <td class="fieldComponent selectboxSmall">
                <span class="uiSelectbox">
                  <select id="popupReminderTime" v-model="reminder.popupReminderTime" class="selectbox" name="popupReminderTime">
                    <option v-for="i in [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60]" :key="i" :value="i">{{ i }} {{ $t('UIEventForm.label.minutes') }}</option>
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
      <button class="btn" type="button" @click="save">
        {{ $t('ExoEventForm.btn.save') }}
      </button>

      <button class="btn" type="button" @click="cancel">
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
      handler() {
        this.$emit('change', this.reminder);
      },
      deep: true
    }
  },
  methods: {
    save() {
      this.$emit('save');
    },
    cancel() {
      this.$emit('cancel');
    }
  }
};
</script>
