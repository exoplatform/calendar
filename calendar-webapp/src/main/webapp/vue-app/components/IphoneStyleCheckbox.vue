<template>
  <div class="iphoneCheckbox">
    <input v-iphone :checked="checked && !disabled" :disabled="disabled" type="checkbox" class="iphone"/>
  </div>
</template>

<script>
export default {
  directives: {
    'iphone': {
      inserted(el, binding, vnode) {
        $(el).iphoneStyle({
          disabledClass: 'switchBtnDisabled',
          containerClass: 'uiSwitchBtn',
          labelOnClass: 'switchBtnLabelOn',
          labelOffClass: 'switchBtnLabelOff',
          handleClass: 'switchBtnHandle',
          onChange: function() {
            vnode.context.onChange();
          }
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
      if (this.checked !== value) {
        this.$emit('change', value);
      }
    }
  }
};
</script>
