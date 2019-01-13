<template>
  <div id="ExoEventForm">
    <div :class="{open}" class="drawer">
      <div class="header">
        <span>{{ getFormTitle() }}</span>
        <a href="javascript:void(0)" class="closebtn" @click="toggleOpen">&times;</a>
      </div>
      <div class="content">
        <form onsubmit="return false;" action="" method="post" enctype="multipart/form-data" accept-charset="utf-8">
          <div class="control-group">
            <div class="controls">
              <input v-model="title" :placeholder="$t('ExoEventForm.placeholder.eventTitle')" name="eventName" class="eventTitle" type="text"/>
              <span class="uiSelectbox form-horizontal">
                <select v-model="category" class="selectbox category" name="category">
                  <option v-for="category in categories" :key="category.id" :value="category.id">{{ $t(`UICalendarView.label.${category.id}`) }}</option>
                </select>
              </span>
            </div>
          </div>
          <div class="control-group">
            <div class="calDate">
              <div class="control-label">{{ $t('ExoEventForm.label.from') }}</div>
              <div class="controls">
                <input v-model="fromDate" type="text"/>
                <input :disabled="isAllDay" v-model="fromTime" type="text"/>
              </div>
            </div>
            <div class="calDate">
              <div class="control-label">{{ $t('ExoEventForm.label.to') }}</div>
              <div class="controls">
                <input v-model="toDate" type="text"/>
                <input :disabled="isAllDay" v-model="toTime" type="text"/>
              </div>
            </div>
          </div>
          <div class="control-group allday">
            <div class="controls">
              <input id="allday" v-model="isAllDay" type="checkbox"/>
              <label for="allday">{{ $t('ExoEventForm.label.allDay') }}</label>
            </div>
          </div>
          <div class="control-group">
            <div class="control-label">{{ $t('ExoEventForm.label.location') }}</div>
            <div class="controls">
              <input v-model="location" :placeholder="$t('ExoEventForm.placeholder.location')" type="text" class="location"/>
            </div>
          </div>
          <div class="control-group">
            <div class="control-label">{{ $t('ExoEventForm.label.participants') }}</div>
            <div class="controls">
              <suggester v-model="participants" :placeholder="$t('ExoEventForm.placeholder.participants')" :source-providers="['exo:calendar-participants']" class="participants"/>
            </div>
          </div>
          <div class="control-group">
            <div class="calendarSelector pull-left">
              <div class="control-label">{{ $t('ExoEventForm.label.calendar') }}</div>
              <div class="controls">
                <span class="uiSelectbox form-horizontal">
                  <select v-model="calendar" class="selectbox" name="calendar">
                    <optgroup v-for="group in calendarGroups" :key="group.id" :label="$t(`UICalendarSettingForm.label.${group.name}`)">
                      <option v-for="calendar in group.calendars" :key="calendar.id" :value="`${calendar.id}`">{{ calendar.name }}</option>
                    </optgroup>
                  </select>
                </span>
              </div>
            </div>
            <div class="reminder pull-left">
              <div class="control-label">{{ $t('ExoEventForm.label.reminder') }}</div>
              <div class="controls">
                <exo-modal :show="enableReminder && showReminder" :title="$t('UICalendarChildPopupWindow.title.UIRepeatEventForm')">
                  <reminder-form v-model="reminder" @closeForm="closeReminderForm"/>
                </exo-modal>
                <iphone-checkbox v-model="enableReminder"/>
              </div>
            </div>
            <div class="repeat">
              <div class="control-label">{{ $t('ExoEventForm.label.repeat') }}</div>
              <div class="controls">
                <exo-modal :show="showRecurringUpdateType" :title="$t('UICalendarChildPopupWindow.title.RecurringUpdateTypeForm')">
                  <recurring-update-type-form v-model="recurringUpdateType" @cancelForm="cancelRecurringUpdateTypeForm" @saveForm="saveRecurringUpdateTypeForm"/>
                </exo-modal>
                <exo-modal :show="enableRecurring && showRecurring" :title="$t('UICalendarChildPopupWindow.title.UIRepeatEventForm')">
                  <recurring-form v-model="recurring" @closeForm="closeRecurringForm"/>
                </exo-modal>
                <iphone-checkbox v-model="enableRecurring" :disabled="isOccur === false"/>
              </div>
            </div>
          </div>
          <div class="control-group description">
            <div class="control-label">{{ $t('ExoEventForm.label.description') }}</div>
            <div class="controls">
              <textarea v-model="description" :placeholder="$t('ExoEventForm.placeholder.description')"></textarea>
            </div>
          </div>
          <div class="control-group attachments">
            <div class="control-label">{{ $t('ExoEventForm.label.attachments') }}</div>
            <div class="controls">
              <filedrop v-model="attachedFiles"/>
            </div>
          </div>
        </form>
      </div>
      <div class="footer">
        <div class="uiAction">
          <button type="button" class="btn btn-primary" @click="save">{{ $t('ExoEventForm.btn.save') }}</button>
          <button type="button" class="btn" @click="toggleOpen">{{ $t('ExoEventForm.btn.cancel') }}</button>
          <button type="button" class="btn clearBtn" @click="clear">{{ $t('ExoEventForm.btn.clear') }}</button>
        </div>
      </div>
    </div>
    <div v-if="open" class="backdrop"></div>
  </div>
</template>

<script>
import * as calServices from '../calServices.js';
import IphoneStyleCheckbox from './IphoneStyleCheckbox.vue';
import Suggester from './Suggester.vue';
import FileDrop from './FileDrop.vue';
import ExoModal from './ExoModal.vue';
import RecurringForm from './RecurringForm.vue';
import RecurringUpdateTypeForm from './RecurringUpdateTypeForm.vue';
import ReminderForm from './ReminderForm.vue';

function formatDate(date) {
  const sub = -2;
  const month = `0${date.getMonth() + 1}`.slice(sub);
  const day = `0${date.getDate()}`.slice(sub);
  return `${date.getFullYear()}-${month}-${day}`;
}

function formatTime(date) {
  const sub = -2;
  const hours = `0${date.getHours()}`.slice(sub);
  const minutes = `0${date.getMinutes()}`.slice(sub);
  return `${hours}:${minutes}`;
}

let categories = [];
calServices.getCategories().then(results => {
  if(results) {
    categories = results;
  }
});
let calendarGroups = [];
calServices.getCalendars().then(results => {
  if(results) {
    calendarGroups = results;
  }
});

function getDefaultData() {
  const date = new Date();
  const fromDate = formatDate(date);
  const oneWeek = 7;
  date.setDate(date.getDate() + oneWeek);
  const endRecurring = formatDate(date);

  return {
    id: '',
    title: '',
    calendar: '',
    category: '',
    fromDate: fromDate,
    fromTime: '01:00',
    toDate: fromDate,
    toTime: '01:30',
    isAllDay: false,
    location: '',
    participants: [],
    description: '',
    attachedFiles: [],
    reminder: {
      mailReminder: true,
      popupReminder: false,
      mailReminderTime: 5,
      popupReminderTime: 5
    },
    recurring: {
      repeatType: 'weekly',
      interval: 1,
      weekly: ['TU'],
      monthly: 'monthlyByMonthDay',
      endRepeat: 'neverEnd',
      endAfterNumber: 5,
      endDate: endRecurring
    },
    isOccur: null,
    recurrenceId: null,
    recurringUpdateType: '',

    showRecurringUpdateType: false,
    enableRecurring: false,
    showRecurring: false,
    enableReminder: false,
    showReminder: false,
    categories: categories,
    calendarGroups: calendarGroups
  };
}

export default {
  components: {
    'iphone-checkbox': IphoneStyleCheckbox,
    'suggester': Suggester,
    'filedrop': FileDrop,
    'exo-modal': ExoModal,
    'recurring-form': RecurringForm,
    'recurring-update-type-form': RecurringUpdateTypeForm,
    'reminder-form': ReminderForm
  },
  model: {
    prop: 'open',
    event: 'toggle-open'
  },
  props: {
    open: {
      type: Boolean,
      default: false
    },
    initEvt: {
      type: Object,
      default: () => {return {};}
    }
  },
  data: getDefaultData,
  watch: {
    enableRecurring() {
      if (this.enableRecurring) {
        this.showRecurring = true;
      }
    },
    enableReminder() {
      if (this.enableReminder) {
        this.showReminder = true;
      }
    },
    isAllDay() {
      if (this.isAllDay) {
        this.fromTime = '00:00';
        this.toTime = '23:59';
      }
    },
    initEvt() {
      this.clear();
    }
  },
  methods: {
    toggleOpen() {
      this.open = !this.open;
      this.$emit('toggle-open', this.open);
    },
    getFormTitle() {
      if (this.id) {
        return this.$t('ExoEventForm.title.editEvent');
      } else {
        return this.$t('ExoEventForm.title.addEvent');
      }
    },
    closeRecurringForm() {
      this.showRecurring = false;
    },
    closeReminderForm() {
      this.showReminder = false;
    },
    cancelRecurringUpdateTypeForm() {
      this.showRecurringUpdateType = false;
    },
    saveRecurringUpdateTypeForm() {
      this.showRecurringUpdateType = false;
      this.save();
    },
    clear() {
      const data = getDefaultData();
      Object.entries(data).forEach(entry => Vue.set(this.$data, entry[0], entry[1]));

      const evt = this.initEvt;
      Object.entries(evt).forEach(entry => {
        if (entry[1] !== null) {
          Vue.set(this.$data, entry[0], entry[1]);
        }
      });

      if (evt.from) {
        const fromDate = new Date(evt.from);
        this.fromDate = formatDate(fromDate);
        this.fromTime = formatTime(fromDate);
      }
      if (evt.to) {
        const toDate = new Date(evt.to);
        this.toDate = formatDate(toDate);
        this.toTime = formatTime(toDate);
      }

      if (evt.reminder) {
        if (evt.reminder.mailReminder || evt.reminder.popupReminder) {
          this.enableReminder = true;
          Vue.nextTick(() => {
            this.showReminder = false;
          });
        }
      }

      if (evt.recurring) {
        this.enableRecurring = true;
        if (this.recurring.endDate && typeof this.recurring.endDate !== 'string') {
          this.recurring.endDate = formatDate(this.recurring.endDate);
        }
        Vue.nextTick(() => {
          this.showRecurring = false;
        });
      }

      if (this.fromTime === '00:00' && this.toTime === '23:59') {
        this.isAllDay = true;
      }
    },
    save() {
      if (this.id && this.isOccur && !this.recurringUpdateType) {
        this.showRecurringUpdateType = true;
      } else {
        this.toggleOpen();
        calServices.saveEvent(this.$data).then(() => {
          this.$emit('save');
        });
      }
    }
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
