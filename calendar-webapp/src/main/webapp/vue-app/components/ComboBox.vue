<template>
  <div class="cbb">
    <input :disabled="disabled" :placeholder="placeholder" v-model="value" class="cbb_input" type="text" @click="showOptions()" @keyup.enter.prevent="select(selectedIndex)" @keydown.down.prevent="selectNext()" @keydown.up.prevent="selectPrev()" @keyup.delete="showOptions()" @keyup.esc="closeOptions()" />
    <div v-if="showAutocompleteDropdown" class="cbb_list_wrapper">
      <ul ref="optList" class="cbb_list">
        <li v-for="(value, index) in options" :key="index" :class="{'selected': isSelected(index)}" class="cbb_item" @click="select(index)">{{ value }}</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    placeholder: {
      type: String,
      default: ''
    },
    value: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    options: {
      type: Array,
      default: () => []
    }
  },
  data: function() {
    return {
      showAutocompleteDropdown: false,
      selectedIndex: 0
    };
  },
  watch: {
    value: function(val) {
      this.$emit('input', val);
    }
  },
  methods: {
    isSelected: function(index) {
      return this.selectedIndex === index;
    },
    showOptions: function() {
      for (let i = 0; i < this.options.length; i++) {
        if (this.options[i] === this.value) {
          this.selectedIndex = i;
          break;
        }
      }
      this.showAutocompleteDropdown = true;
      Vue.nextTick(function() {
        const el = this.$refs.optList.querySelector('li.selected');
        this.$refs.optList.scrollTop = el.offsetTop;
        document.addEventListener('click', this.closeOptions);
      }, this);
    },
    closeOptions: function() {
      this.showAutocompleteDropdown = false;
      Vue.nextTick(function() {
        document.removeEventListener('click', this.closeOptions);
      }, this);

    },
    select: function(index) {
      this.closeOptions();
      this.selectedIndex = index;
      this.value = this.options[index];
    },
    selectNext: function() {
      if (this.showAutocompleteDropdown) {
        if (this.selectedIndex < this.options.length - 1) {
          this.selectedIndex++;
        } else {
          this.selectedIndex = 0;
        }
      } else {
        this.showOptions();
      }
    },
    selectPrev: function() {
      if (this.selectedIndex > 0) {
        this.selectedIndex--;
      } else {
        this.selectedIndex = this.options.length - 1;
      }
    }
  }
};
</script>