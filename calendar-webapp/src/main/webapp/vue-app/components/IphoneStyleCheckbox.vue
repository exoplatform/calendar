<template>
  <input v-iphone type="checkbox" class="iphone"/>
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
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
