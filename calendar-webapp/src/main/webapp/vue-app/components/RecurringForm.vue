<template>
  <div class="uiForm uiRepeatEventForm">
    <div class="form-horizontal">

      <div class="control-group">
        <div class="control-label">{{ $t('UIEventForm.label.option.repeat') }}:</div>
        <div class="controls selectboxSmall">
          <span class="uiSelectbox">
            <select id="repeatType" v-model="recurring.repeatType" class="selectbox" name="repeatType">
              <option value="daily">{{ $t('UIEventForm.label.option.daily') }}</option>
              <option value="weekly">{{ $t('UIEventForm.label.option.weekly') }}</option>
              <option value="monthly">{{ $t('UIEventForm.label.option.monthly') }}</option>
              <option value="yearly">{{ $t('UIEventForm.label.option.yearly') }}</option>
            </select>
          </span>
        </div>
      </div>

      <div class="control-group">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.interval') }}:</div>
        <div class="controls selectboxMini">
          <span class="uiSelectbox">
            <select id="interval" v-model="recurring.interval" class="selectbox" name="interval">
              <option v-for="i in 30" :key="i" :value="i">{{ i }}</option>
            </select>
          </span>
        </div>
      </div>

      <div v-show="recurring.repeatType == 'weekly'" class="control-group weeklyByDay">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.weeklyByDay') }}:</div>
        <div class="controls">
          <div class="checkBoxArea">
            <div v-for="day in weekdays" :key="day" class="pull-left">
              <div class="pull-left checkboxContainer">
                <span class="uiCheckbox">
                  <input :id="day" :name="day" :value="day" v-model="recurring.weekly" type="checkbox" class="checkbox"><span></span>
                </span>
              </div>
              <div class="textLabel">{{ $t(`UIRepeatEventForm.label.${day}`) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-show="recurring.repeatType == 'monthly'" class="control-group monthlyType radioBoxArea">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.monthlyType') }}:</div>
        <div class="controls">
          <label class="uiRadio">
            <input v-model="recurring.monthly" class="radio" type="radio" name="monthlyType" value="monthlyByMonthDay"> <span>{{ $t('monthlyType.label.monthlyByMonthDay') }}</span>
          </label>
          <label class="uiRadio">
            <input v-model="recurring.monthly" class="radio" type="radio" name="monthlyType" value="monthlyByDay"> <span>{{ $t('monthlyType.label.monthlyByDay') }}</span>
          </label>
        </div>
      </div>

      <div class="control-group">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.endRepeat') }}:</div>
        <div class="controls ">
          <div class="radioBoxArea">
            <label class="uiRadio">
              <input id="endNever" v-model="recurring.endRepeat" type="radio" value="neverEnd" name="endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.neverEnd') }}</span>
            </label>
          </div>
          <div class="radioBoxArea">
            <label class="uiRadio">
              <input id="endAfter" v-model="recurring.endRepeat" type="radio" value="endAfter" name="endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.endAfter') }}</span>
            </label>
            <span class="inputMini"><input id="endAfterNumber" v-model="recurring.endAfterNumber" :disabled="recurring.endRepeat != 'endAfter'" name="endAfterNumber" type="number" min="1" step="1"></span> &nbsp; {{ $t('UIRepeatEventForm.label.occurrences') }}
          </div>
          <div id="endByDateContainer" class="radioBoxArea clearfix">
            <label class="uiRadio pull-left">
              <input id="endByDate" v-model="recurring.endRepeat" type="radio" value="endByDate" name="endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.endByDate') }}</span>
            </label>
            <div id="endDate" class="inputSmall pull-left">
              <input v-model="recurring.endDate" :disabled="recurring.endRepeat != 'endByDate'" lang="en" format="MM/dd/yyyy" type="date" name="endDate">
            </div>
          </div>
        </div>
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
import {calConstants} from '../calConstants.js';

export default {
  model: {
    prop: 'recurring',
    event: 'change'
  },
  props: {
    'recurring': {
      type: Object,
      default: {}
    }
  },
  data() {
    return {
      weekdays: calConstants.WEEK_DAYS
    };
  },
  watch: {
    recurring: {
      handler() {
        this.$emit('change', this.recurring);
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
