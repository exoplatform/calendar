<template>
  <div class="uiForm uiRepeatEventForm">
    <div class="form-horizontal">

      <div class="control-group">
        <div class="control-label">{{ $t('UIEventForm.label.option.repeat') }}:</div>
        <div class="controls selectboxSmall">
          <span class="uiSelectbox">
            <select class="selectbox" name="repeatType" id="repeatType" v-model="recurring.repeatType">
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
            <select class="selectbox" name="interval" id="interval" v-model="recurring.interval">
              <option v-for="i in 30" :key="i" :value="i">{{ i }}</option>
            </select>
          </span>
        </div>
      </div>

      <div class="control-group weeklyByDay" v-show="recurring.repeatType == 'weekly'">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.weeklyByDay') }}:</div>
        <div class="controls">
          <div class="checkBoxArea">
            <div class="pull-left" v-for="day in ['MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU']" :key="day">
              <div class="pull-left checkboxContainer">
                <span class="uiCheckbox">
                  <input :id="day" type="checkbox" class="checkbox" :name="day" :value="day" v-model="recurring.weekly"><span></span>
                </span>
              </div>
              <div class="textLabel">{{ $t(`UIRepeatEventForm.label.${day}`) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="control-group monthlyType radioBoxArea" v-show="recurring.repeatType == 'monthly'">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.monthlyType') }}:</div>
        <div class="controls">
          <label class="uiRadio">
            <input class="radio" type="radio" name="monthlyType" value="monthlyByMonthDay" v-model="recurring.monthly"> <span>{{ $t('monthlyType.label.monthlyByMonthDay') }}</span>
          </label>
          <label class="uiRadio">
            <input class="radio" type="radio" name="monthlyType" value="monthlyByDay" v-model="recurring.monthly"> <span>{{ $t('monthlyType.label.monthlyByDay') }}</span>
          </label>
        </div>
      </div>

      <div class="control-group">
        <div class="control-label">{{ $t('UIRepeatEventForm.label.endRepeat') }}:</div>
        <div class="controls ">
          <div class="radioBoxArea">
            <label class="uiRadio">
              <input id="endNever" type="radio" value="neverEnd" name="endRepeat" v-model="recurring.endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.neverEnd') }}</span>
            </label>
          </div>
          <div class="radioBoxArea">
            <label class="uiRadio">
              <input id="endAfter" type="radio" value="endAfter" name="endRepeat" v-model="recurring.endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.endAfter') }}</span>
            </label>
            <span class="inputMini"><input name="endAfterNumber" type="number" min="1" step="1" id="endAfterNumber" v-model="recurring.endAfterNumber" :disabled="recurring.endRepeat != 'endAfter'"></span> &nbsp; {{ $t('UIRepeatEventForm.label.occurrences') }}
          </div>
          <div class="radioBoxArea clearfix" id="endByDateContainer">
            <label class="uiRadio pull-left">
              <input id="endByDate" type="radio" value="endByDate" name="endRepeat" v-model="recurring.endRepeat">
              <span>{{ $t('UIRepeatEventForm.label.endByDate') }}</span>
            </label>
            <div id="endDate" class="inputSmall pull-left">
              <input lang="en" format="MM/dd/yyyy" type="date" name="endDate" v-model="recurring.endDate" :disabled="recurring.endRepeat != 'endByDate'">
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
  watch: {
    recurring: {
      handler(val) {
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
