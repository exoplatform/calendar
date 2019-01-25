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

<style>
  .cbb input {
    width: 100%;
  }

  .cbb_list_wrapper {
    position: absolute;
    z-index: 9;
  }

  .cbb_input {
    padding: 0.5rem;
    width: 100%;
    font-size: 15px;
    -webkit-appearance: none;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 3px;
    text-align: left;
    background: white;
  }

  .cbb_list:focus {
    box-shadow: none;
    -webkit-appearance: none;
    outline: 0;
  }

  .cbb_list {
    background: white;
    border: 1px solid rgba(0, 0, 0, 0.08);
    overflow-y: auto;
    max-height: 150px;
    min-width: 85px;
    color: #333;
  }

  .cbb_item {
    margin: 0;
    list-style-type: none;
    padding: 8px 10px;
    font-size: 14px;
    display: block;
    cursor: pointer;
  }

  .cbb_item:hover, .cbb_item.selected {
    background-color: #82aad7;
    color: #fff;
  }
</style>