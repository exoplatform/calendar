<template>
  <input type="text"/>
</template>

<script>
import * as calServices from '../calServices.js';

function findUsers(query, callback) {
  if (!query.length) {
    return callback();
  }
  console.log(query);
  calServices.findParticipants(query).then(users => {
    if(users) {
      callback(users);
    }
  });
}

export default {
  props: {
    sourceProviders: {
      type: Array,
      default: []
    }
  },
  mounted() {
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
      renderMenuItem (item, escape) {
        return `<li data-value="${item.id}"><div class="avatarSmall" style="display: inline-block;"><img src="${item.avatar}"></div>${escape(item.name)} (${item.id})</li>`;
      },
      renderItem(item) {
        return `<span class="item">${item.name}</span>`;
      },
      providers: {
        'exo:calendar-participants': findUsers
      }
    };
    //init suggester
    $(this.$el).suggester(suggesterData);
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
