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
            <input :placeholder="$t('ExoEventForm.placeholder.eventTitle')" name="eventName" class="eventTitle" type="text"/>

            <span class="uiSelectbox">
              <select class="selectbox category" name="category">
                <option v-for="category in categories" :key="category.id" :value="category.id">{{ $t(`UICalendarView.label.${category.id}`) }}</option>
              </select>
            </span>
          </div>
        </div>
        <div class="control-group calDate">
          <div class="control-label">{{ $t('ExoEventForm.label.from') }}</div>
          <div class="controls">
            <input format="MM/dd/yyyy" type="date" value="12/24/2018"/>
            <input type="time" min="9:00" max="18:00" step="1800"/>
          </div>
        </div>
        <div class="control-group calDate">
          <div class="control-label">{{ $t('ExoEventForm.label.to') }}</div>
          <div class="controls">
            <input format="MM/dd/yyyy" type="date" value="12/24/2018"/>
            <input type="time" min="9:00" max="18:00" step="1800"/>
          </div>
        </div>
        <div class="control-group allday">
          <div class="controls">
            <input type="checkbox"/>
            <label for="allday">{{ $t('ExoEventForm.label.allDay') }}</label>
          </div>
        </div>
        <div class="control-group">
          <div class="control-label">{{ $t('ExoEventForm.label.location') }}</div>
          <div class="controls">
            <input :placeholder="$t('ExoEventForm.placeholder.location')" type="text" class="location"/>
          </div>
        </div>
        <div class="control-group">
          <div class="control-label">{{ $t('ExoEventForm.label.participants') }}</div>
          <div class="controls">
            <suggester :placeholder="$t('ExoEventForm.placeholder.participants')" :source-providers="['exo:calendar-participants']" class="participants"/>
          </div>
        </div>
        <div class="control-group calendarSelector">
          <div class="control-label">{{ $t('ExoEventForm.label.calendar') }}</div>
          <div class="controls">
            <span class="uiSelectbox">
              <select class="selectbox" name="calendar">
                <optgroup v-for="group in calendarGroups" :key="group.id" :label="$t(`UICalendarSettingForm.label.${group.name}`)">
                  <option v-for="calendar in group.calendars" :key="calendar.id" :value="`${group.id}:${calendar.id}`">{{ calendar.name }}</option>
                </optgroup>
              </select>
            </span>
          </div>
        </div>
        <div class="control-group reminder">
          <div class="control-label">{{ $t('ExoEventForm.label.reminder') }}</div>
          <div class="controls">
            <iphone-checkbox/>
          </div>
        </div>
        <div class="control-group repeat">
          <div class="control-label">{{ $t('ExoEventForm.label.repeat') }}</div>
          <div class="controls">
            <iphone-checkbox/>
          </div>
        </div>
        <div class="control-group description">
          <div class="control-label">{{ $t('ExoEventForm.label.description') }}</div>
          <div class="controls">
            <textarea :placeholder="$t('ExoEventForm.placeholder.description')"></textarea>
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
        <button type="button" class="btn btn-primary">{{ $t('ExoEventForm.btn.save') }}</button>
        <button type="button" class="btn">{{ $t('ExoEventForm.btn.cancel') }}</button>
        <button type="button" class="btn clearBtn">{{ $t('ExoEventForm.btn.clear') }}</button>
      </div>
    </div>
  </div>
</template>

<script>
import * as calServices from '../calServices.js';
import IphoneStyleCheckbox from './IphoneStyleCheckbox.vue';
import Suggester from './Suggester.vue';
import FileDrop from './FileDrop.vue';

export default {
  components: {
    'iphone-checkbox': IphoneStyleCheckbox,
    'suggester': Suggester,
    'filedrop': FileDrop
  },
  model: {
    prop: 'open',
    event: 'toggle-open'
  },
  props: {
    open: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      categories: [],
      calendarGroups: [],
      attachedFiles: []
    }
  },
  methods: {
    toggleOpen() {
      this.open = !this.open;
      this.$emit('toggle-open', this.open);
    }
  },
  mounted() {
    calServices.getCategories().then(categories => {
      if(categories) {
        this.categories = categories;
      }
    });
    calServices.getCalendars().then(calendarGroups => {
      if(calendarGroups) {
        this.calendarGroups = calendarGroups;
      }
    });
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
