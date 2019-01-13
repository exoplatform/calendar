<template>
  <div class="iphoneCheckbox" @click.stop.prevent="onChange">
    <input v-iphone :checked="checked && !disabled" :disabled="disabled" type="checkbox" class="iphone"/>
  </div>
</template>

<script>
export default {
  directives: {
    'iphone': {
      inserted(el) {
        $(el).iphoneStyle({
          disabledClass: 'switchBtnDisabled',
          containerClass: 'uiSwitchBtn',
          labelOnClass: 'switchBtnLabelOn',
          labelOffClass: 'switchBtnLabelOff',
          handleClass: 'switchBtnHandle',
        });
      }
    }
  },
  model: {
    prop: 'checked',
    event: 'change'
  },
  props: {
    checked: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    checked() {
      $(this.$el).find('input').iphoneStyle().prop('checked', this.checked).iphoneStyle('refresh');
    },
    disabled() {
      $(this.$el).find('input').iphoneStyle().prop('disabled', this.disabled).iphoneStyle('refresh');
    }
  },
  methods: {
    onChange() {
      const value = $(this.$el).find('input').is(':checked');
      this.$emit('change', value);
    }
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
