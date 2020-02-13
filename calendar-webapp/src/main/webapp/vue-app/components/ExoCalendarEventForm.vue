<template>
  <div id="ExoCalendarEventForm">
    <div :class="{open}" class="drawer">
      <div class="header">
        <span>{{ formTitle }}</span>
        <a href="javascript:void(0)" class="closebtn" @click="toggleOpen">&times;</a>
      </div>
      <div class="content">
        <form onsubmit="return false;" action="" method="post" enctype="multipart/form-data" accept-charset="utf-8">
          <div class="control-group mobile-header">
            <div class="uiAction clearfix">
              <button :disabled="!isAllowToSave" type="button" class="btn btn-primary" @click="save">{{ $t('ExoCalendarEventForm.btn.save') }}</button>
              <button type="button" class="btn cancel" @click="toggleOpen">{{ $t('ExoCalendarEventForm.btn.cancel') }}</button>
            </div>
            <div class="controls">
              <input ref="title" v-model="event.title" :placeholder="$t('ExoCalendarEventForm.placeholder.eventTitle')" name="eventName" class="eventTitle" type="text"/>
              <span class="uiSelectbox form-horizontal">
                <select v-model="event.category" class="selectbox category" name="category">
                  <option v-for="category in categories" :key="category.id" :value="category.id">{{ trim(translateCatName(category), 16) }}</option>
                </select>
              </span>
            </div>
          </div>
          <div class="mobile-content">
            <exo-modal :show="showConfirmClose" :title="$t('ExoCalendarEventForm.label.cancel')" @close="cancelConfirm">
              <confirm-form @confirm="confirmClose" @cancel="cancelConfirm"/>
            </exo-modal>
            <div class="control-group">
              <div class="calDate">
                <div class="control-label">{{ $t('ExoCalendarEventForm.label.from') }}</div>
                <div class="controls clearfix">
                  <div class="periodDate pull-left"><input :value="fromDate()" class="date" type="text" format="MM-dd-yyyy" @change="updateDate(event.fromDate, $event.target.value)"/></div>
                  <div class="separator pull-left">-</div>
                  <combobox :disabled="isAllDay" :value="fromTime()" :options="times" class="time pull-left" @input="updateTime(event.fromDate, $event)" />
                </div>
              </div>
              <div class="calDate">
                <div class="control-label">{{ $t('ExoCalendarEventForm.label.to') }}</div>
                <div class="controls clearfix">
                  <div class="periodDate pull-left"><input :value="toDate()" class="date" type="text" format="MM-dd-yyyy" @change="updateDate(event.toDate, $event.target.value)"/></div>
                  <div class="separator pull-left">-</div>
                  <combobox :disabled="isAllDay" :value="toTime()" :options="times" class="time pull-left" @input="updateTime(event.toDate, $event)" />
                </div>
              </div>
            </div>
            <div class="control-group allday">
              <div class="controls">
                <label class="uiCheckbox">
                  <input id="allday" v-model="isAllDay" type="checkbox"/><span>{{ $t('ExoCalendarEventForm.label.allDay') }}</span>
                </label>
                <exo-modal :show="showFindTime" :title="$t('ExoCalendarEventForm.btn.findTime')" class="findtime-popup" @close="cancelFindTime()">
                  <findtime-form v-if="showFindTime" :from="event.fromDate" :to="event.toDate" :participants="event.participants" @save="saveFindTime($event)" @cancel="cancelFindTime"/>
                </exo-modal>
                <a class="findtime" @click.prevent="showFindTime = true">{{ $t('ExoCalendarEventForm.btn.findTime') }}</a>
              </div>
            </div>
            <div class="control-group">
              <div class="control-label">{{ $t('ExoCalendarEventForm.label.location') }}</div>
              <div class="controls">
                <input v-model="event.location" :placeholder="$t('ExoCalendarEventForm.placeholder.location')" type="text" class="location"/>
              </div>
            </div>
            <div class="control-group">
              <div class="control-label">{{ $t('ExoCalendarEventForm.label.participants') }}</div>
              <div class="controls">
                <suggester v-model="event.participants" :placeholder="$t('ExoCalendarEventForm.placeholder.participants')" :source-providers="['exo:calendar-participants']" class="participants"/>
              </div>
            </div>
            <div class="control-group">
              <div class="calendarSelector pull-left">
                <div class="control-label">{{ $t('ExoCalendarEventForm.label.calendar') }}</div>
                <div class="controls">
                  <calendar-selector v-model="event.calendar" :groups="calendarGroups"/>
                </div>
              </div>
              <div class="reminder pull-left">
                <div class="control-label">{{ $t('ExoCalendarEventForm.label.reminder') }}</div>
                <div class="controls">
                  <exo-modal :show="showReminder" :title="$t('ExoCalendarEventForm.label.reminder')" @close="cancelReminder">
                    <reminder-form v-model="event.reminder" @save="saveReminder" @cancel="cancelReminder"/>
                  </exo-modal>
                  <iphone-checkbox v-model="enableReminder"/>
                  <a @click.prevent="showReminder = true">{{ reminderLabel() }}</a>
                </div>
              </div>
              <div class="repeat">
                <div class="control-label">{{ $t('ExoCalendarEventForm.label.repeat') }}</div>
                <div class="controls">
                  <exo-modal :show="showRecurringUpdateType" :title="$t('ExoCalendarEventForm.title.RecurringUpdateTypeForm')" @close="cancelRecurringUpdateTypeForm">
                    <recurring-update-type-form v-model="recurringUpdateType" @cancelForm="cancelRecurringUpdateTypeForm" @saveForm="saveRecurringUpdateTypeForm"/>
                  </exo-modal>
                  <exo-modal :show="showRecurring" :title="$t('UICalendarChildPopupWindow.title.UIRepeatEventForm')" class="recurring-popup" @close="cancelRecurring">
                    <recurring-form v-model="event.recurring" :repeat-day="event.fromDate.getDay()" @save="saveRecurring" @cancel="cancelRecurring"/>
                  </exo-modal>
                  <iphone-checkbox v-model="enableRecurring" :disabled="isExceptionOccurence"/>
                  <a @click.prevent="showRecurring = true">{{ recurringLabel() }}</a>
                </div>
              </div>
            </div>
            <div class="control-group description">
              <div class="control-label">{{ $t('ExoCalendarEventForm.label.description') }}</div>
              <div class="controls">
                <textarea v-model="event.description" :placeholder="$t('ExoCalendarEventForm.placeholder.description')" @focus="$event.target.placeholder = ''" @blur="$event.target.placeholder = $t('ExoCalendarEventForm.placeholder.description')"></textarea>
              </div>
            </div>
            <div class="control-group attachments">
              <div class="control-label">{{ $t('ExoCalendarEventForm.label.attachments') }}</div>
              <div class="controls">
                <filedrop v-model="event.attachedFiles"/>
              </div>
            </div>
          </div>
        </form>
        <exo-modal :show="hasErrors" :title="$t('ExoCalendarEventForm.title.message')" class="popup-message" @close="errors = []">
          <error-message v-model="errors"/>
        </exo-modal>
      </div>
      <div class="footer">
        <div class="uiAction">
          <button :disabled="!isAllowToSave" type="button" class="btn btn-primary" @click="save">{{ $t('ExoCalendarEventForm.btn.save') }}</button>
          <button type="button" class="btn" @click="toggleOpen">{{ $t('ExoCalendarEventForm.btn.cancel') }}</button>
          <button type="button" class="btn clearBtn" @click="reset">{{ $t('ExoCalendarEventForm.btn.clear') }}</button>
        </div>
      </div>
    </div>
    <div v-if="open" class="drawer-backdrop"></div>
  </div>
</template>

<script>
import {calConstants} from '../calConstants.js';
import * as calServices from '../calServices.js';
import CalendarEvent from '../model/event.js';
import Utils from '../model/utils.js';

import IphoneStyleCheckbox from './IphoneStyleCheckbox.vue';
import Suggester from './Suggester.vue';
import FileDrop from './ExoCalendarFileDrop.vue';
import ExoModal from './ExoModal.vue';
import RecurringForm from './ExoCalendarRecurringForm.vue';
import RecurringUpdateTypeForm from './ExoCalendarRecurringUpdateTypeForm.vue';
import ReminderForm from './ExoCalendarReminderForm.vue';
import FindTimeForm from './ExoCalendarFindTimeForm.vue';
import ConfirmForm from './ExoCalendarConfirmForm.vue';
import CalendarSelector from './ExoCalendarSelector.vue';
import ErrorMessage from './ErrorMessage.vue';
import ComboBox from './ComboBox.vue';
import moment from 'moment';

export default {
  components: {
    'iphone-checkbox': IphoneStyleCheckbox,
    'suggester': Suggester,
    'filedrop': FileDrop,
    'exo-modal': ExoModal,
    'recurring-form': RecurringForm,
    'recurring-update-type-form': RecurringUpdateTypeForm,
    'reminder-form': ReminderForm,
    'findtime-form': FindTimeForm,
    'confirm-form': ConfirmForm,
    'calendar-selector': CalendarSelector,
    'error-message': ErrorMessage,
    'combobox': ComboBox
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
    isAllowToSave() {
      return this.event.title && this.event.title.trim() && this.event.calendar;
    },
    hasErrors() {
      return this.errors && this.errors.length;
    },
    formTitle() {
      if (this.event.id) {
        return this.$t('ExoCalendarEventForm.title.editEvent');
      } else {
        return this.$t('UICalendars.label.AddEvent');
      }
    },
    times: function() {
      const a = [];
      const ampm = calConstants.SETTINGS.timeFormat === 'hh:mm a';

      const MID_DAY = 12;
      const maxTime = 24;
      const percent = 10;
      for (let i = 0; i < maxTime; i++) {
        let n = i;
        let s = '';
        if (ampm) {
          n = n % MID_DAY;
          if (i < MID_DAY) {
            s = ' AM';
          } else {
            s = ' PM';
          }
        }
        if (n < percent) {
          n = `0${n}`;
        }
        a.push(`${n}:00${s}`);
        a.push(`${n}:15${s}`);
        a.push(`${n}:30${s}`);
        a.push(`${n}:45${s}`);
      }
      return a;     
    }
  },
  watch: {
    enableRecurring() {
      if (this.enableRecurring) {
        this.isFilled = true;
        this.showRecurring = true;
        if (this.showReminder) {
          if (this.reminderLabel()) {
            this.showReminder = false;
          } else {
            this.enableReminder = false;
          }
        }
      } else {
        this.resetRecurring();
        this.showRecurring = false;
      }
    },
    enableReminder() {
      if (this.enableReminder) {
        this.isFilled = true;
        this.showReminder = true;
        if (this.showRecurring) {
          if (this.recurringLabel()) {
            this.showRecurring = false;
          } else {
            this.enableRecurring = false;
          }
        }
      } else {
        this.resetReminder();
        this.showReminder = false;
      }
    },
    isAllDay() {
      if (this.isAllDay) {
        this.isFilled = true;
        this.event.setAllDay();
      }
    },
    initEvt() {
      this.reset();
      this.$refs.title.focus();
    }
  },
  mounted: function() {
    const dateInput = $(this.$el).find('input.date');
    dateInput.focus(function() {
      timePicker.init(this, false);
    });
    dateInput.keyup(function() {
      timePicker.show();
    });
    dateInput.focus(function(event) {
      event.cancelBubble = true;
    });
    timePicker.addListener('setDate', function() {
      this.dispatchEvent(new Event('change'));
    });
    window.addEventListener('keyup', this.handleKeyup);
    window.addEventListener('click', this.handleClick);
  },
  destroyed: function() {
    window.removeEventListener('click');
    window.removeEventListener('keyup');
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
        showFindTime: false,
        isFilled: false,
        showConfirmClose: false,

        calendarGroups: [],
        categories: [],
        LANG: typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en',
        errors: []
      };

      const event = new CalendarEvent();
      //add event creator as default participants
      event.participants.push(eXo.env.portal.userName);
      //add event space as default value in space case
      if (eXo.env.portal.spaceName) {
        event.calendar = eXo.env.portal.spaceName.concat('_space_calendar');
      }
      //if initEvt has participants, it will override above default value
      event.buildFrom(this.initEvt);
      data.event = event;

      return data;
    },
    translateCatName(category) {
      let name = this.$t(`UICalendarView.label.${category.id}`);
      if (name.startsWith('UICalendarView.label.')) {
        name = category.name;
      }
      return name;
    },
    refreshDate() {
      this.event.fromDate = new Date(this.event.fromDate.getTime());
      this.event.toDate = new Date(this.event.toDate.getTime());
    },
    updateDate(date, val) {
      const parsedVal = Utils.parseDate(val);
      if (parsedVal) {
        date.setFullYear(parsedVal.getFullYear(),parsedVal.getMonth(), parsedVal.getDate());
      }
      this.refreshDate();
    },
    updateTime(date, val) {
      if (!this.isAllDay) {
        const parsedVal = Utils.parseTime(val);
        if (parsedVal) {
          date.setHours(parsedVal.getHours(), parsedVal.getMinutes(), parsedVal.getSeconds(), parsedVal.getMilliseconds());
        }
      }
    },
    fromDate() {
      return moment(this.event.fromDate).toDate().toLocaleDateString(this.LANG);
    },
    fromTime() {
      return Utils.formatTime(this.event.fromDate);
    },
    toDate() {
      return moment(this.event.toDate).toDate().toLocaleDateString(this.LANG);
    },
    toTime() {
      return Utils.formatTime(this.event.toDate);
    },
    reminderLabel() {
      if (this.enableReminder && this.event.reminder.isEnabled()) {
        const time = this.event.reminder.getNearest();
        if (time % (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS * calConstants.ONE_WEEK_DAYS) === 0) {
          return this.$t('ExoCalendarEventForm.label.reminderTime.weeks', [time / (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS * calConstants.ONE_WEEK_DAYS)]);
        } else if (time % (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS) === 0) {
          return this.$t('ExoCalendarEventForm.label.reminderTime.days', [time / (calConstants.ONE_HOUR_MINUTES * calConstants.ONE_DAY_HOURS)]);
        } else if (time % calConstants.ONE_HOUR_MINUTES === 0) {
          return this.$t('ExoCalendarEventForm.label.reminderTime.hours', [time / calConstants.ONE_HOUR_MINUTES]);
        }
        return this.$t('ExoCalendarEventForm.label.reminderTime.minutes', [time]);
      }
    },
    recurringLabel() {
      if (this.enableRecurring && this.event.recurring.isEnabled()) {
        return this.$t('ExoCalendarEventForm.label.repeat');
      }
    },
    toggleOpen() {
      this.open = !this.open;
      this.cancelRecurring();
      this.cancelReminder();
      this.cancelFindTime();
      this.errors = [];
      this.$emit('toggle-open', this.open);
    },
    saveRecurring() {
      this.showRecurring = false;
    },
    cancelRecurring() {
      if (!this.event.recurring.isEnabled()) {
        this.enableRecurring = false;
      }
      this.showRecurring = false;
    },
    resetRecurring() {
      const data = this.getDefaultData();
      this.event.recurring = data.event.recurring;
    },
    saveReminder() {
      if (!this.event.reminder.isEnabled()) {
        this.enableReminder = false;
      }
      this.showReminder = false;
    },
    cancelReminder() {
      if (!this.event.reminder.isEnabled()) {
        this.enableReminder = false;
      }
      this.showReminder = false;
    },
    resetReminder() {
      const data = this.getDefaultData();
      this.event.reminder = data.event.reminder;
    },
    cancelRecurringUpdateTypeForm() {
      this.showRecurringUpdateType = false;
    },
    saveRecurringUpdateTypeForm() {
      this.showRecurringUpdateType = false;
      this.save();
    },
    saveFindTime(evt) {
      this.event.fromDate = new Date(evt.from.getTime());
      this.event.toDate = new Date(evt.to.getTime());
      //allDay value is not stored in the database and therefore must be calculated from the from and to date
      this.isAllDay = this.event.isAllDay();
      this.event.participants = evt.participants.slice();
      this.showFindTime = false;
    },
    cancelFindTime() {
      this.showFindTime = false;
    },
    trim(str, length) {
      if (str && str.length > length) {
        str = `${str.slice(0, length)}...`;
      }
      return str;
    },
    reset() {
      const data = this.getDefaultData();
      Object.entries(data).forEach(entry => Vue.set(this.$data, entry[0], entry[1]));

      if (this.initEvt.reminder) {
        if (this.initEvt.reminder.isEnabled()) {
          this.enableReminder = true;
          Vue.nextTick(() => {
            this.showReminder = false;
          });
        }
      }

      if (this.initEvt.recurring) {
        if (this.initEvt.recurring.isEnabled()) {
          this.enableRecurring = true;
          Vue.nextTick(() => {
            this.showRecurring = false;
          });
        }
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
        const errors = this.event.validate();
        if (errors && errors.length) {
          this.errors = errors;
        } else {
          this.toggleOpen();

          calServices.saveEvent(this.$data).then(resp => {
            if (resp && resp.bodyUsed) {
              try {
                return resp.json();
              } catch (err) {
                return null;
              }
            }
            return null;
          }).then((data) => {
            if (data && data.developerMessage) {
              this.errors.push(data.developerMessage);
            } else {
              //This should be improved by
              //only update the calendar portlet
              window.location.reload();
              this.$emit('save');
            }
          }).catch((err) => {
            this.errors.push(err.message);
          });
        }
      }
    },
    confirmClose() {
      if (this.open) {
        this.toggleOpen();
      }
      this.showConfirmClose = false;
    },
    cancelConfirm() {
      this.showConfirmClose = false;
    },
    filled() {
      return this.isFilled
          || this.$data.event.title !== ''
          || this.$data.event.category !== 'defaultEventCategoryIdAll'
          || this.$data.event.location !== ''
          || this.$data.event.attachedFiles.length > 0
          || this.$data.event.description !== ''
          || this.$data.event.participants.length !== 1;
    },
    handleKeyup() {
      const escapeKeyCode = 27;
      if (this.open && event.keyCode === escapeKeyCode) {
        if (this.filled()) {
          this.showConfirmClose = true;
        } else {
          this.toggleOpen();
        }
      }
    },
    handleClick() {
      if (this.open && event.srcElement.className === 'backdrop') {
        if (this.filled()) {
          this.showConfirmClose = true;
        } else {
          this.toggleOpen();
        }
      }
    }
  }
};
</script>
