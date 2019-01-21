<template>
  <div id="ExoEventForm">
    <div :class="{open}" class="drawer">
      <div class="header">
        <span>{{ formTitle }}</span>
        <a href="javascript:void(0)" class="closebtn" @click="toggleOpen">&times;</a>
      </div>
      <div class="content">
        <form onsubmit="return false;" action="" method="post" enctype="multipart/form-data" accept-charset="utf-8">
          <div class="control-group mobile-header">
            <div class="uiAction clearfix">
              <button :disabled="!isAllowToSave" type="button" class="btn btn-primary" @click="save">{{ $t('ExoEventForm.btn.save') }}</button>
              <button type="button" class="btn cancel" @click="toggleOpen">{{ $t('ExoEventForm.btn.cancel') }}</button>
            </div>
            <div class="controls">
              <input v-model="event.title" :placeholder="$t('ExoEventForm.placeholder.eventTitle')" name="eventName" class="eventTitle" type="text"/>
              <span class="uiSelectbox form-horizontal">
                <select v-model="event.category" class="selectbox category" name="category">
                  <option v-for="category in categories" :key="category.id" :value="category.id">{{ trim($t(`UICalendarView.label.${category.id}`), 16) }}</option>
                </select>
              </span>
            </div>
          </div>
          <div class="mobile-content">
            <div class="control-group">
              <div class="calDate clearfix">
                <div class="control-label">{{ $t('ExoEventForm.label.from') }}</div>
                <div class="controls">
                  <input :value="fromDate" class="date" type="text" format="MM-dd-yyyy" @change="updateDate(event.fromDate, $event.target.value)"/>
                  <span class="separator">-</span>
                  <input :disabled="isAllDay" :value="fromTime" class="time" type="text" @change="updateTime(event.fromDate, $event.target.value)"/>
                </div>
              </div>
              <div class="calDate clearfix">
                <div class="control-label">{{ $t('ExoEventForm.label.to') }}</div>
                <div class="controls">
                  <input :value="toDate" class="date" type="text" format="MM-dd-yyyy" @change="updateDate(event.toDate, $event.target.value)"/>
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
                  <calendar-selector v-model="event.calendar" :groups="calendarGroups"/>
                </div>
              </div>
              <div class="reminder pull-left">
                <div class="control-label">{{ $t('ExoEventForm.label.reminder') }}</div>
                <div class="controls">
                  <exo-modal :show="showReminder" :title="$t('ExoEventForm.label.reminder')" @close="cancelReminder">
                    <reminder-form v-model="event.reminder" @save="saveReminder" @cancel="cancelReminder"/>
                  </exo-modal>
                  <iphone-checkbox v-model="enableReminder"/>
                  <a @click.prevent="showReminder = true">{{ reminderLabel }}</a>
                </div>
              </div>
              <div class="repeat">
                <div class="control-label">{{ $t('ExoEventForm.label.repeat') }}</div>
                <div class="controls">
                  <exo-modal :show="showRecurringUpdateType" :title="$t('ExoEventForm.title.RecurringUpdateTypeForm')" @close="cancelRecurringUpdateTypeForm">
                    <recurring-update-type-form v-model="recurringUpdateType" @cancelForm="cancelRecurringUpdateTypeForm" @saveForm="saveRecurringUpdateTypeForm"/>
                  </exo-modal>
                  <exo-modal :show="showRecurring" :title="$t('UICalendarChildPopupWindow.title.UIRepeatEventForm')" @close="cancelRecurring">
                    <recurring-form v-model="event.recurring" @save="saveRecurring" @cancel="cancelRecurring"/>
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
          </div>
        </form>
        <exo-modal :show="hasErrors" :title="$t('ExoEventForm.title.message')" @close="errors = []" class="popup-message">
          <error-message v-model="errors"/>
        </exo-modal>
      </div>
      <div class="footer">
        <div class="uiAction">
          <button :disabled="!isAllowToSave" type="button" class="btn btn-primary" @click="save">{{ $t('ExoEventForm.btn.save') }}</button>
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
import CalendarSelector from './CalendarSelector.vue';
import ErrorMessage from './ErrorMessage.vue';

export default {
  components: {
    'iphone-checkbox': IphoneStyleCheckbox,
    'suggester': Suggester,
    'filedrop': FileDrop,
    'exo-modal': ExoModal,
    'recurring-form': RecurringForm,
    'recurring-update-type-form': RecurringUpdateTypeForm,
    'reminder-form': ReminderForm,
    'calendar-selector': CalendarSelector,
    'error-message': ErrorMessage
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
      return this.event.title && this.event.title.trim();
    },
    hasErrors() {
      return this.errors && this.errors.length;
    },
    formTitle() {
      if (this.event.id) {
        return this.$t('ExoEventForm.title.editEvent');
      } else {
        return this.$t('ExoEventForm.title.addEvent');
      }
    },
    reminderLabel() {
      if (this.enableReminder && this.event.reminder.isEnabled()) {
        return this.$t('ExoEventForm.label.reminderTime', [this.event.reminder.getNearest()]);
      }
    },
    recurringLabel() {
      if (this.enableRecurring && this.event.recurring.isEnabled()) {
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
        if (this.showReminder) {
          if (this.reminderLabel) {
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
        this.showReminder = true;
        if (this.showRecurring) {
          if (this.recurringLabel) {
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
        this.event.setAllDay();
      }
    },
    initEvt() {
      this.reset();
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
        categories: [],

        errors: []
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
              this.$emit('save');
            }
          }).catch((err) => {
            this.errors.push(err.message);
          });
        }
      }
    }
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
