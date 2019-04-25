<template>
  <div id="eventReminder" class="uiEventReminder">
    <div class="reminderByEmail">
      <div id="mailReminder" class="mailReminder">
        <label class="uiCheckbox">
          <input id="mailReminder" v-model="mailReminder" type="checkbox" class="checkbox" name="mailReminder"><span>{{ $t('UIEventForm.label.mailReminder') }}</span>
        </label>
      </div>
      <div v-show="mailReminder" class="selectboxSmall">
        <span class="reminderInput">
          <input id="mailReminderTimeEntry" :min="min" v-model.number="mailReminderTimeEntry" type="number" name="mailReminderTimeEntry" @keypress="isValidEntry(event)"/>
        </span>
        <span class="uiSelectbox">
          <select id="mailReminderTime" v-model="mailReminderTimeUnit" class="selectbox" name="mailReminderTime">
            <option v-for="i in ['minutes', 'hours', 'days', 'weeks']" :key="i" :value="i">{{ $t('UIEventForm.label.' + i) }}</option>
          </select>
        </span>
        {{ $t('UIEventForm.label.mailReminderTime') }}
      </div>
    </div>
    <div>
      <div class="reminderByPopup">
        <div id="popupReminder" class="popupReminder">
          <label class="uiCheckbox">
            <input id="popupReminder" v-model="popupReminder" type="checkbox" class="checkbox" name="popupReminder"><span>{{ $t('UIEventForm.label.popupReminder') }}</span>
          </label>
        </div>
      </div>
      <div>
        <table v-show="popupReminder" class="uiFormGrid">
          <tbody>
            <tr>
              <td class="fieldComponent selectboxSmall">
                <span class="reminderInput">
                  <input id="popupReminderTimeEntry" :min="min" v-model.number="popupReminderTimeEntry" type="number" name="popupReminderTimeEntry" @keypress="isValidEntry(event)"/>
                </span>
                <span class="uiSelectbox">
                  <select id="popupReminderTime" v-model="popupReminderTimeUnit" class="selectbox" name="popupReminderTime">
                    <option v-for="i in ['minutes', 'hours', 'days', 'weeks']" :key="i" :value="i">{{ $t('UIEventForm.label.' + i) }}</option>
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
        {{ $t('ExoCalendarEventForm.btn.save') }}
      </button>

      <button class="btn" type="button" @click="cancel">
        {{ $t('ExoCalendarEventForm.btn.cancel') }}
      </button>
    </div>
  </div>
</template>

<script>
import {calConstants} from '../calConstants.js';
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
    },
    min: {
      type: Number,
      default: 1
    }
  },
  data() {
    this.popupReminderTimeEntry = 10;
    this.mailReminderTimeEntry = 10;
    this.mailReminderTimeUnit = 'minutes';
    this.popupReminderTimeUnit = 'minutes';
    return new Reminder();
  },
  watch: {
    reminder() {
      this.reset();
      this.getReminderEntry();
    }
  },
  methods: {
    save() {
      if (this.mailReminder) {
        if (this.mailReminderTimeEntry > 0) {
          this.mailReminderTime = this.getReminderTime(this.mailReminderTimeEntry, this.mailReminderTimeUnit);
        } else {
          this.mailReminder = false;
        }
      }
      if (this.popupReminder) {
        if (this.popupReminderTimeEntry > 0) {
          this.popupReminderTime = this.getReminderTime(this.popupReminderTimeEntry, this.popupReminderTimeUnit);
        } else {
          this.popupReminder = false;
        }
      }
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
    },
    getReminderTime(entry, unit) {
      switch (unit) {
      case 'minutes':
        return entry;
      case 'hours':
        return entry * calConstants.ONE_HOUR_MINUTES;
      case 'days':
        return entry * calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS;
      case 'weeks':
        return entry * calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS * calConstants.ONE_WEEK_DAYS;
      }
    },
    getReminderEntry() {
      if (this.mailReminder) {
        const time = this.mailReminderTime;
        if (time % (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS * calConstants.ONE_WEEK_DAYS) === 0) {
          this.mailReminderTimeEntry = time / (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS * calConstants.ONE_WEEK_DAYS);
          this.mailReminderTimeUnit = 'weeks';
        } else if (time % (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS) === 0) {
          this.mailReminderTimeEntry = time / (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS);
          this.mailReminderTimeUnit = 'days';
        } else if (time % calConstants.ONE_HOUR_MINUTES === 0) {
          this.mailReminderTimeEntry = time / calConstants.ONE_HOUR_MINUTES;
          this.mailReminderTimeUnit = 'hours';
        } else {
          this.mailReminderTimeEntry = time;
          this.mailReminderTimeUnit = 'minutes';
        }
      }
      if (this.popupReminder) {
        const time = this.popupReminderTime;
        if (time % (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS * calConstants.ONE_WEEK_DAYS) === 0) {
          this.popupReminderTimeEntry = time / (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS * calConstants.ONE_WEEK_DAYS);
          this.popupReminderTimeUnit = 'weeks';
        } else if (time % (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS) === 0) {
          this.popupReminderTimeEntry = time / (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS);
          this.popupReminderTimeUnit = 'days';
        } else if (time % calConstants.ONE_HOUR_MINUTES === 0) {
          this.popupReminderTimeEntry = time / calConstants.ONE_HOUR_MINUTES;
          this.popupReminderTimeUnit = 'hours';
        } else {
          this.popupReminderTimeEntry = time;
          this.popupReminderTimeUnit = 'minutes';
        }
      }
    },
    isValidEntry(evt) {
      evt = evt ? evt : window.event;
      const charCode = evt.which ? evt.which : evt.keyCode;
      const KEY_CODE = 48;
      if (charCode < KEY_CODE) {
        evt.preventDefault();
      } else {
        return true;
      }
    }
  }
};
</script>
