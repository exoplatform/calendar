<template>
  <div class="cbb">
    <input :disabled="disabled" :placeholder="placeholder" v-model="value" class="cbb_input" type="text" @click="showOptions()" @keyup.enter.prevent="select(selectedIndex)" @keydown.down.prevent="selectNext()" @keydown.up.prevent="selectPrev()" @keyup.8="handleBackspace()" />
    <div class="cbb_list_wrapper">
      <ul v-if="showAutocompleteDropdown" class="cbb_list">
        <li v-for="(value, index) in options" :key="value" :class="{'selected': isSelected(index)}" class="cbb_item" @click="select(index)">{{ value }}</li>
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
  methods: {
    isSelected: function(index) {
      return this.value === this.options[index];
    },
    showOptions: function() {
      this.showAutocompleteDropdown = true;
    },
    handleBackspace: function () {
      this.showAutocompleteDropdown = true;
    },
    select: function(index) {
      this.showAutocompleteDropdown = false;
      this.value = this.options[index];
      this.$emit('input', this.value);
    },
    selectNext: function() {
      if (this.showAutocompleteDropdown) {
        if (this.selectedIndex < this.options.length - 1) {
          this.selectedIndex++;
        } else {
          this.selectedIndex = 0;
        }
      } else {
        this.showAutocompleteDropdown = true;
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