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
      default: () => []
    },
    participants: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    participants() {
      this.$emit('change', this.participants);
      this.bindParticipants();
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
        thiss.participants = items.split(',');
      }
    };
    //init suggester
    $(this.$el).suggester(suggesterData);
  },
  methods: {
    bindParticipants() {
      const selectize = $(this.$el)[0].selectize;
      //
      this.participants.forEach(par => {
        if (!selectize.items.includes(par)) {
          findUsers(par, users => {
            users.forEach(user => {
              if (user.id === par) {
                selectize.addOption(user);
                selectize.addItem(user.id);
              }
            });
          });
        }
      });


      const removeItems = [];
      selectize.items.forEach(item => {
        if (!this.participants.includes(item)) {
          removeItems.push(item);
        }
      });
      removeItems.forEach(item => {
        selectize.removeItem(item, true);
      });
    }
  }
};
</script>
