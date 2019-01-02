<template>
  <div class="iphoneCheckbox" @click.stop.prevent="onChange">
    <input v-iphone type="checkbox" class="iphone" :checked="checked"/>
  </div>
</template>

<script>
export default {
  model: {
    prop: 'checked',
    event: 'change'
  },
  props: {
    checked: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    checked() {
      $(this.$el).find('input').iphoneStyle().prop('checked', this.checked).iphoneStyle("refresh");
    }
  },
  methods: {
    onChange() {
      const value = $(this.$el).find('input').is(':checked');
      this.$emit('change', value);
    }
  },
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
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
