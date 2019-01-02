<template>
  <input type="text"/>
</template>

<script>
import * as calServices from '../calServices.js';

function findUsers(query, callback) {
  if (!query || !query.length) {
    callback([]);
  } else {
    calServices.findParticipants(query).then(users => {
      if(users) {
        callback(users);
      }
    });
  }
}

export default {
  model: {
    prop: 'participants',
    event: 'change'
  },
  props: {
    sourceProviders: {
      type: Array,
      default: []
    },
    participants: {
      type: Array,
      default: []
    }
  },
  watch: {
    participants() {
      this.$emit('change', this.participants);
    }
  },
  mounted() {
    const thiss = this;

    const suggesterData = {
      type: 'tag',
      create: false,
      createOnBlur: false,
      highlight: false,
      openOnFocus: false,
      sourceProviders: this.sourceProviders,
      valueField: 'id',
      labelField: 'name',
      searchField: ['name'],
      closeAfterSelect: true,
      dropdownParent: 'body',
      hideSelected: true,
      plugins: ['remove_button'],
      providers: {
        'exo:calendar-participants': findUsers
      },
      onChange(items) {
        this.participants = items.split(',');
      }
    };
    //init suggester
    $(this.$el).suggester(suggesterData);

    const selectize = $(this.$el)[0].selectize;
    findUsers(this.participants, users => {
      users.forEach(user => {
        selectize.addOption(user);
        selectize.addItem(user.id);
      });
    });
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
