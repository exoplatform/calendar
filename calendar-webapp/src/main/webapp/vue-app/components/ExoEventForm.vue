<template>
  <div id="ExoEventForm">
    <div :class="{open}" class="drawer">
      <div class="header">
        <span>{{ formTitle }}</span>
        <a href="javascript:void(0)" class="closebtn" @click="toggleOpen">&times;</a>
      </div>
      <div class="content">
        <form onsubmit="return false;" action="" method="post" enctype="multipart/form-data" accept-charset="utf-8">
          <div class="control-group">
            <div class="controls">
              <input v-model="event.title" :placeholder="$t('ExoEventForm.placeholder.eventTitle')" name="eventName" class="eventTitle" type="text"/>
              <span class="uiSelectbox form-horizontal">
                <select v-model="event.category" class="selectbox category" name="category">
                  <option v-for="category in categories" :key="category.id" :value="category.id">{{ $t(`UICalendarView.label.${category.id}`) }}</option>
                </select>
              </span>
            </div>
          </div>
          <div class="control-group">
            <div class="calDate">
              <div class="control-label">{{ $t('ExoEventForm.label.from') }}</div>
              <div class="controls">
                <input :value="fromDate" class="date" type="text" @change="updateDate(event.fromDate, $event.target.value)"/>
                <span class="separator">-</span>
                <input :disabled="isAllDay" :value="fromTime" class="time" type="text" @change="updateTime(event.fromDate, $event.target.value)"/>
              </div>
            </div>
            <div class="calDate">
              <div class="control-label">{{ $t('ExoEventForm.label.to') }}</div>
              <div class="controls">
                <input :value="toDate" class="date" type="text" @change="updateDate(event.toDate, $event.target.value)"/>
                <span class="separator">-</span>
                <input :disabled="isAllDay" :value="toTime" class="time" type="text" @change="updateTime(event.toDate, $event.target.value)"/>
              </div>
            </div>
          </div>
          <div class="control-group allday">
            <div class="controls">
              <label class="uiCheckbox">
                <input id="allday" v-model="isAllDay" type="checkbox"/><span>{{ $t('ExoEventForm.label.allDay') }}</span>
              </label>
            </div>
          </div>
          <div class="control-group">
            <div class="control-label">{{ $t('ExoEventForm.label.location') }}</div>
            <div class="controls">
              <input v-model="event.location" :placeholder="$t('ExoEventForm.placeholder.location')" type="text" class="location"/>
            </div>
          </div>
          <div class="control-group">
            <div class="control-label">{{ $t('ExoEventForm.label.participants') }}</div>
            <div class="controls">
              <suggester v-model="event.participants" :placeholder="$t('ExoEventForm.placeholder.participants')" :source-providers="['exo:calendar-participants']" class="participants"/>
            </div>
          </div>
          <div class="control-group">
            <div class="calendarSelector pull-left">
              <div class="control-label">{{ $t('ExoEventForm.label.calendar') }}</div>
              <div class="controls">
                <span class="uiSelectbox form-horizontal">
                  <select v-model="event.calendar" class="selectbox" name="calendar">
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
                <exo-modal :show="showReminder" :title="$t('ExoEventForm.label.reminder')">
                  <reminder-form v-model="event.reminder" @save="closeReminderForm" @cancel="resetReminder"/>
                </exo-modal>
                <iphone-checkbox v-model="enableReminder"/>
                <a @click.prevent="showReminder = true">{{ reminderLabel }}</a>
              </div>
            </div>
            <div class="repeat">
              <div class="control-label">{{ $t('ExoEventForm.label.repeat') }}</div>
              <div class="controls">
                <exo-modal :show="showRecurringUpdateType" :title="$t('UICalendarChildPopupWindow.title.RecurringUpdateTypeForm')">
                  <recurring-update-type-form v-model="recurringUpdateType" @cancelForm="cancelRecurringUpdateTypeForm" @saveForm="saveRecurringUpdateTypeForm"/>
                </exo-modal>
                <exo-modal :show="showRecurring" :title="$t('UICalendarChildPopupWindow.title.UIRepeatEventForm')">
                  <recurring-form v-model="event.recurring" @save="closeRecurringForm" @cancel="resetRecurring"/>
                </exo-modal>
                <iphone-checkbox v-model="enableRecurring" :disabled="isExceptionOccurence"/>
                <a @click.prevent="showRecurring = true">{{ recurringLabel }}</a>
              </div>
            </div>
          </div>
          <div class="control-group description">
            <div class="control-label">{{ $t('ExoEventForm.label.description') }}</div>
            <div class="controls">
              <textarea v-model="event.description" :placeholder="$t('ExoEventForm.placeholder.description')"></textarea>
            </div>
          </div>
          <div class="control-group attachments">
            <div class="control-label">{{ $t('ExoEventForm.label.attachments') }}</div>
            <div class="controls">
              <filedrop v-model="event.attachedFiles"/>
            </div>
          </div>
        </form>
      </div>
      <div class="footer">
        <div class="uiAction">
          <button type="button" class="btn btn-primary" @click="save">{{ $t('ExoEventForm.btn.save') }}</button>
          <button type="button" class="btn" @click="toggleOpen">{{ $t('ExoEventForm.btn.cancel') }}</button>
          <button type="button" class="btn clearBtn" @click="reset">{{ $t('ExoEventForm.btn.clear') }}</button>
        </div>
      </div>
    </div>
    <div v-if="open" class="backdrop"></div>
  </div>
</template>

<script>
import * as calServices from '../calServices.js';
import CalendarEvent from '../model/event.js';
import Utils from '../model/utils.js';

import IphoneStyleCheckbox from './IphoneStyleCheckbox.vue';
import Suggester from './Suggester.vue';
import FileDrop from './FileDrop.vue';
import ExoModal from './ExoModal.vue';
import RecurringForm from './RecurringForm.vue';
import RecurringUpdateTypeForm from './RecurringUpdateTypeForm.vue';
import ReminderForm from './ReminderForm.vue';

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
  data: function() {
    return this.getDefaultData();
  },
  computed: {
    isExceptionOccurence() {
      return !this.event.isOccur && this.event.recurrenceId;
    },
    formTitle() {
      if (this.event.id) {
        return this.$t('ExoEventForm.title.editEvent');
      } else {
        return this.$t('ExoEventForm.title.addEvent');
      }
    },
    reminderLabel() {
      if (this.enableReminder && this.event.reminder) {
        return this.$t('ExoEventForm.label.reminder');
      }
    },
    recurringLabel() {
      if (this.enableRecurring && this.event.recurring) {
        return this.$t('ExoEventForm.label.repeat');
      }
    },
    fromDate() {
      return Utils.formatDate(this.event.fromDate);
    },
    fromTime() {
      return Utils.formatTime(this.event.fromDate);
    },
    toDate() {
      return Utils.formatDate(this.event.toDate);
    },
    toTime() {
      return Utils.formatTime(this.event.toDate);
    }
  },
  watch: {
    enableRecurring() {
      if (this.enableRecurring) {
        this.showRecurring = true;
      } else {
        this.resetRecurring();
      }
    },
    enableReminder() {
      if (this.enableReminder) {
        this.showReminder = true;
      } else {
        this.resetReminder();
      }
    },
    isAllDay() {
      if (this.isAllDay) {
        this.event.setAllDay();
      }
    },
    initEvt() {
      this.reset();
    }
  },
  methods: {
    getDefaultData() {
      const data = {
        recurringUpdateType: null,
        showRecurringUpdateType: false,
        enableRecurring: false,
        showRecurring: false,
        enableReminder: false,
        showReminder: false,
        isAllDay: false,

        calendarGroups: [],
        categories: []
      };

      const event = new CalendarEvent();
      event.buildFrom(this.initEvt);
      data.event = event;

      return data;
    },
    updateDate(date, val) {
      const parsedVal = Utils.parseDate(val);
      if (parsedVal) {
        date.setFullYear(parsedVal.getFullYear(),parsedVal.getMonth(), parsedVal.getDate());
      }
    },
    updateTime(date, val) {
      const parsedVal = Utils.parseTime(val);
      if (parsedVal) {
        date.setHours(parsedVal.getHours(), parsedVal.getMinutes(), parsedVal.getSeconds(), parsedVal.getMilliseconds());
      }
    },
    toggleOpen() {
      this.open = !this.open;
      this.$emit('toggle-open', this.open);
    },
    closeRecurringForm() {
      this.showRecurring = false;
    },
    resetRecurring() {
      const data = this.getDefaultData();
      this.event.recurring = data.event.recurring;
      this.closeRecurringForm();
    },
    closeReminderForm() {
      this.showReminder = false;
    },
    resetReminder() {
      const data = this.getDefaultData();
      this.event.reminder = data.event.reminder;
      this.closeReminderForm();
    },
    cancelRecurringUpdateTypeForm() {
      this.showRecurringUpdateType = false;
    },
    saveRecurringUpdateTypeForm() {
      this.showRecurringUpdateType = false;
      this.save();
    },
    reset() {
      const data = this.getDefaultData();
      Object.entries(data).forEach(entry => Vue.set(this.$data, entry[0], entry[1]));

      if (this.initEvt.reminder) {
        if (this.initEvt.reminder.mailReminder || this.initEvt.reminder.popupReminder) {
          this.enableReminder = true;
          Vue.nextTick(() => {
            this.showReminder = false;
          });
        }
      }

      if (this.initEvt.recurring) {
        this.enableRecurring = true;
        Vue.nextTick(() => {
          this.showRecurring = false;
        });
      }

      this.isAllDay = this.event.isAllDay();

      calServices.getCalendars().then(results => {
        if(results) {
          this.calendarGroups = results;
        }
      });
      calServices.getCategories().then(results => {
        if(results) {
          this.categories = results;
        }
      });
    },
    save() {
      if (this.event.id && this.event.isOccur && !this.recurringUpdateType) {
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
