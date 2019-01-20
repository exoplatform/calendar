<template>
  <div class="dropdown">
    <div class="dropdown-toggle" data-toggle="dropdown" aria-haspopup="true">
      <span :class="selectedCal.color" class="color"></span> {{ trim(selectedCal.name, 18) }}
      <span class="caret"></span>
    </div>
    <ul class="dropdown-menu">
      <li v-for="group in groups" :key="group.id" class="dropdown-header">
        {{ trim($t(`UICalendarSettingForm.label.${group.name}`), 21) }}
        <ul class="dropdown-submenu">
          <li v-for="cal in group.calendars" :key="cal.id" @click="select(cal.id)">
            <a href="javascript:void(0)"><span :class="cal.color" class="color"></span> {{ trim(cal.name, 18) }}</a>
          </li>
        </ul>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  model: {
    prop: 'selectedId',
    event: 'change'
  },
  props: {
    'selectedId': {
      type: String,
      default() {
        return '';
      }
    },
    'groups': {
      type: Array,
      default() {
        return [];
      }
    }
  },
  computed: {
    selectedCal() {
      let selectedCal = {};
      this.groups.forEach(group => {
        group.calendars.find(cal => {
          if (cal.id === this.selectedId) {
            selectedCal = cal;
          }
        });
      });
      return selectedCal;
    }
  },
  methods: {
    trim(str, length) {
      if (str && str.length > length) {
        str = `${str.slice(0, length)}...`;
      }
      return str;
    },
    select(calId) {
      this.selectedId = calId;
      this.$emit('change', this.selectedId);
    }
  }
};
</script>