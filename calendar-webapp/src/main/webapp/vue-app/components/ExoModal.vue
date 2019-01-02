<template>
  <div class="uiPopup" v-show="show">
    <div class="popupHeader clearfix">
      <a class="uiIconClose pull-right"></a>
      <span class="popupTitle">{{ title }}</span>
    </div>
    <div class="popupContent">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: ''
    },
    show: {
      type: Boolean,
      default: false
    }
  }
};
</script>
