<template>
  <div id="ExoEventForm" :class="{open}" class="drawer">
    <div class="header">
      <span>{{ $t('ExoEventForm.title.addEvent') }}</span>
      <a href="javascript:void(0)" class="closebtn" @click="toggleOpen">&times;</a>
    </div>
    <div class="content">
      <form onsubmit="return false;" action="" method="post" enctype="multipart/form-data" accept-charset="utf-8">
        <div class="control-group">
          <div class="controls">
            <input :placeholder="$t('ExoEventForm.placeholder.eventTitle')" name="eventName" class="eventTitle" type="text" v-model="title"/>

            <span class="uiSelectbox">
              <select class="selectbox category" name="category" v-model="category">
                <option v-for="category in categories" :key="category.id" :value="category.id">{{ $t(`UICalendarView.label.${category.id}`) }}</option>
              </select>
            </span>
          </div>
        </div>
        <div class="control-group calDate">
          <div class="control-label">{{ $t('ExoEventForm.label.from') }}</div>
          <div class="controls">
            <input format="MM/dd/yyyy" type="date" value="12/24/2018" v-model="fromDate"/>
            <input type="time" min="0:00" max="23:30" step="1800" v-model="fromTime"/>
          </div>
        </div>
        <div class="control-group calDate">
          <div class="control-label">{{ $t('ExoEventForm.label.to') }}</div>
          <div class="controls">
            <input format="MM/dd/yyyy" type="date" value="12/24/2018" v-model="toDate"/>
            <input type="time" min="0:00" max="23:30" step="1800" v-model="toTime"/>
          </div>
        </div>
        <div class="control-group allday">
          <div class="controls">
            <input type="checkbox" v-model="isAllday"/>
            <label for="allday">{{ $t('ExoEventForm.label.allDay') }}</label>
          </div>
        </div>
        <div class="control-group">
          <div class="control-label">{{ $t('ExoEventForm.label.location') }}</div>
          <div class="controls">
            <input :placeholder="$t('ExoEventForm.placeholder.location')" type="text" class="location" v-model="location"/>
          </div>
        </div>
        <div class="control-group">
          <div class="control-label">{{ $t('ExoEventForm.label.participants') }}</div>
          <div class="controls">
            <suggester :placeholder="$t('ExoEventForm.placeholder.participants')" :source-providers="['exo:calendar-participants']" class="participants" v-model="participants"/>
          </div>
        </div>
        <div class="control-group calendarSelector">
          <div class="control-label">{{ $t('ExoEventForm.label.calendar') }}</div>
          <div class="controls">
            <span class="uiSelectbox">
              <select class="selectbox" name="calendar" v-model="calendar">
                <optgroup v-for="group in calendarGroups" :key="group.id" :label="$t(`UICalendarSettingForm.label.${group.name}`)">
                  <option v-for="calendar in group.calendars" :key="calendar.id" :value="`${calendar.id}`">{{ calendar.name }}</option>
                </optgroup>
              </select>
            </span>
          </div>
        </div>
        <div class="control-group reminder">
          <div class="control-label">{{ $t('ExoEventForm.label.reminder') }}</div>
          <div class="controls">
            <exo-modal :show="enableReminder && showReminder" :title="$t('UICalendarChildPopupWindow.title.UIRepeatEventForm')">
              <reminder-form @closeForm="closeReminderForm" v-model="reminder"/>
            </exo-modal>
            <iphone-checkbox v-model="enableReminder"/>
          </div>
        </div>
        <div class="control-group repeat">
          <div class="control-label">{{ $t('ExoEventForm.label.repeat') }}</div>
          <div class="controls">
            <exo-modal :show="enableRecurring && showRecurring" :title="$t('UICalendarChildPopupWindow.title.UIRepeatEventForm')">
              <recurring-form @closeForm="closeRecurringForm" v-model="recurring"/>
            </exo-modal>
            <iphone-checkbox v-model="enableRecurring"/>
          </div>
        </div>
        <div class="control-group description">
          <div class="control-label">{{ $t('ExoEventForm.label.description') }}</div>
          <div class="controls">
            <textarea :placeholder="$t('ExoEventForm.placeholder.description')" v-model="description"></textarea>
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
</template>

<script>
import * as calServices from '../calServices.js';
import IphoneStyleCheckbox from './IphoneStyleCheckbox.vue';
import Suggester from './Suggester.vue';
import FileDrop from './FileDrop.vue';
import ExoModal from './ExoModal.vue';
import RecurringForm from './RecurringForm.vue';
import ReminderForm from './ReminderForm.vue';

function formatDate(date) {
  return `${date.getFullYear()}-${("0" + (date.getMonth() + 1)).slice(-2)}-${("0" + date.getDate()).slice(-2)}`;
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

  date.setDate(date.getDate() + 7);
  const endRecurring = formatDate(date);

  return {
    title: "",
    calendar: "",
    category: "",
    fromDate: fromDate,
    fromTime: '01:00',
    toDate: fromDate,
    toTime: '01:30',
    isAllday: false,
    location: "",
    participants: [],
    description: "",
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

    enableRecurring: false,
    showRecurring: false,
    enableReminder: false,
    showReminder: false,
    categories: categories,
    calendarGroups: calendarGroups
  }
}

export default {
  components: {
    'iphone-checkbox': IphoneStyleCheckbox,
    'suggester': Suggester,
    'filedrop': FileDrop,
    'exo-modal': ExoModal,
    'recurring-form': RecurringForm,
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
      default: {}
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
    initEvt() {
      this.clear();
    }
  },
  methods: {
    toggleOpen() {
      this.open = !this.open;
      this.$emit('toggle-open', this.open);
    },
    closeRecurringForm() {
      this.showRecurring = false;
    },
    closeReminderForm() {
      this.showReminder = false;
    },
    clear() {
      const data = getDefaultData();
      Object.entries(data).forEach(entry => Vue.set(this.$data, entry[0], entry[1]));

      const evt = this.initEvt;
      if (evt.calendar) {
        this.calendar = evt.calendar;
      }
      if (evt.category) {
        this.category = evt.category;
      }
      if (evt.from) {
        const fromDate = new Date(evt.from);
        this.fromDate = formatDate(fromDate);
        this.fromTime = `${("0" + (fromDate.getHours())).slice(-2)}:${("0" + (fromDate.getMinutes())).slice(-2)}`;
      }
      if (evt.to) {
        const toDate = new Date(evt.to);
        this.toDate = formatDate(toDate);
        this.toTime = `${("0" + (toDate.getHours())).slice(-2)}:${("0" + (toDate.getMinutes())).slice(-2)}`;
      }
      if (evt.isAllday) {
        this.isAllday = evt.isAllday;
      }
    },
    save() {
      calServices.saveEvent(this.$data).then(() => {
        this.$emit('save');
      });
    }
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
