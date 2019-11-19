<template>
  <div class="uiForm uiRepeatEventForm">
    <div class="form-horizontal">

      <div class="control-group">
        <div class="control-label">{{ $t('UIEventForm.label.option.repeat') }}:</div>
        <div class="controls selectboxSmall">
          <span class="uiSelectbox">
            <select id="repeatType" v-model="repeatType" class="selectbox" name="repeatType">
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
            <select id="interval" v-model="interval" class="selectbox" name="interval">
              <option v-for="i in 30" :key="i" :value="i">{{ i }}</option>
            </select>
          </span>
        </div>
      </div>

      <div v-show="repeatType === 'weekly'" class="control-group weeklyByDay">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.weeklyByDay') }}:</div>
        <div class="controls">
          <div class="checkBoxArea">
            <div v-for="day in weekdays" :key="day" class="pull-left">
              <div class="pull-left checkboxContainer">
                <span class="uiCheckbox">
                  <input :id="day" :name="day" :value="day" v-model="weekly" type="checkbox" class="checkbox"><span></span>
                </span>
              </div>
              <div class="textLabel">{{ $t(`UIRepeatEventForm.label.${day}`) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div v-show="repeatType == 'monthly'" class="control-group monthlyType radioBoxArea">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.monthlyType') }}:</div>
        <div class="controls">
          <label class="uiRadio">
            <input v-model="monthly" class="radio" type="radio" name="monthlyType" value="monthlyByMonthDay"> <span>{{ $t('monthlyType.label.monthlyByMonthDay') }}</span>
          </label>
          <label class="uiRadio">
            <input v-model="monthly" class="radio" type="radio" name="monthlyType" value="monthlyByDay"> <span>{{ $t('monthlyType.label.monthlyByDay') }}</span>
          </label>
        </div>
      </div>

      <div class="control-group">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.endRepeat') }}:</div>
        <div class="controls ">
          <div class="radioBoxArea">
            <label class="uiRadio">
              <input id="endNever" v-model="endRepeat" type="radio" value="neverEnd" name="endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.neverEnd') }}</span>
            </label>
          </div>
          <div class="radioBoxArea">
            <label class="uiRadio">
              <input id="endAfter" v-model="endRepeat" type="radio" value="endAfter" name="endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.endAfter') }}</span>
            </label>
            <span class="inputMini"><input id="endAfterNumber" v-model="endAfterNumber" :disabled="endRepeat != 'endAfter'" name="endAfterNumber" type="number" min="1" step="1"></span> &nbsp; {{ $t('UIRepeatEventForm.label.occurrences') }}
          </div>
          <div id="endByDateContainer" class="radioBoxArea clearfix">
            <label class="uiRadio pull-left">
              <input id="endByDate" v-model="endRepeat" type="radio" value="endByDate" name="endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.endByDate') }}</span>
            </label>
            <div id="endDate" class="inputSmall pull-left">
              <input :value="endDate" :disabled="endRepeat != 'endByDate'" lang="en" type="date" name="endDate" @change="updateDate(endDate, $event.target.value)">
            </div>
          </div>
        </div>
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
import Recurring from '../model/recurring.js';
import Utils from '../model/utils.js';

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
    return new Recurring();
  },
  computed: {
    endDate() {
      return Utils.formatDate(this.endDate);
    }
  },
  watch: {
    recurring() {
      this.reset();
    }
  },
  methods: {
    save() {
      Utils.copyObj(this.recurring, this.$data);
      this.$emit('save');
    },
    cancel() {
      this.reset();
      this.$emit('cancel');
    },
    updateDate(date, val) {
      const parsedVal = Utils.parseDate(val, {
        year:0,
        month:1,
        date: 2,
      });
      if (parsedVal) {
        date.setFullYear(parsedVal.getFullYear(),parsedVal.getMonth(), parsedVal.getDate());
      }
    },
    reset() {
      const data = new Recurring();
      Utils.copyObj(data, this.recurring);
      Object.entries(data).forEach(entry => Vue.set(this.$data, entry[0], entry[1]));

      if (!this.recurring.isEnabled()) {
        this.repeatType = 'weekly';
      }
    }
  }
};
</script>
