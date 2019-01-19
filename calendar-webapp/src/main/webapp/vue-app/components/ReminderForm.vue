<template>
  <div id="eventReminder" class="uiEventReminder">
    <div class="reminderByEmail">
      <div id="mailReminder" class="mailReminder">
        <label class="uiCheckbox">
          <input id="mailReminder" v-model="mailReminder" type="checkbox" class="checkbox" name="mailReminder"><span>{{ $t('UIEventForm.label.mailReminder') }}</span>
        </label>
      </div>
      <div v-show="mailReminder" class="selectboxSmall">
        <span class="uiSelectbox">
          <select id="mailReminderTime" v-model="mailReminderTime" class="selectbox" name="mailReminderTime">
            <option v-for="i in [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60]" :key="i" :value="i">{{ i }} {{ $t('UIEventForm.label.minutes') }}</option>
          </select>
        </span>
        {{ $t('UIEventForm.label.mailReminderTime') }}
      </div>
    </div>
    <div>
      <div class="reminderByPopup">
        <div id="popupReminder" class="popupReminder">
          <label class="uiCheckbox">
            <input id="popupReminder" v-model="popupReminder" type="checkbox" class="checkbox" name="popupReminder"><span>Display a notification pop-up</span>
          </label>
        </div>
      </div>
      <div>
        <table v-show="popupReminder" class="uiFormGrid">
          <tbody>
            <tr>
              <td class="fieldComponent selectboxSmall">
                <span class="uiSelectbox">
                  <select id="popupReminderTime" v-model="popupReminderTime" class="selectbox" name="popupReminderTime">
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
import Utils from '../model/utils.js';
import Reminder from '../model/reminder.js';

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
  data() {
    return new Reminder();
  },
  watch: {
    reminder() {
      this.reset();
    }
  },
  methods: {
    save() {
      Utils.copyObj(this.reminder, this.$data);
      this.$emit('save');
    },
    cancel() {
      this.reset();
      this.$emit('cancel');
    },
    reset() {
      const data = new Reminder();
      Utils.copyObj(data, this.reminder);
      Object.entries(data).forEach(entry => Vue.set(this.$data, entry[0], entry[1]));

      if (!this.reminder.isEnabled()) {
        this.mailReminder = true;
      }
    }
  }
};
</script>
