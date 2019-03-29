<template>
  <div class="uiUserSelector">
    <div class="selectUserContainer">
      <div class="uiSearch clearfix">
        <span class="searchByUser">
          <span class="inputXLarge">
            <a :title="$t('ExoCalendarEventForm.btn.quickSearch')" class="btnSearchUser" data-placement="left" rel="tooltip" href="javascript:void(0);">
              <i class="uiIconSearch uiIconLightGray">
                <span class="skipHidden">{{ $t('ExoCalendarEventForm.btn.quickSearch') }}</span>
              </i>
            </a>
            <input v-model="username" type="text" placeholder="Search">
          </span>
          <span class="selectboxMedium"></span>
        </span>
      </div>

      <div class="list-user-container">
        <table id="UIListUsers" class="uiGrid table table-hover table-striped">
          <thead>
            <tr>
              <th class="center">
                <span class="uiCheckbox">
                  <input v-model="checkall" type="checkbox" class="checkbox" name="selectall">
                  <span></span>
                </span>
              </th>

              <th>{{ $t('ExoCalendarEventForm.label.username') }}</th>
              <th>{{ $t('ExoCalendarEventForm.label.firstName') }}</th>
              <th>{{ $t('ExoCalendarEventForm.label.lastName') }}</th>
              <th>{{ $t('ExoCalendarEventForm.label.email') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td class="center">
                <span class="uiCheckbox">
                  <input v-model="selectedUsers" :id="user.id" :value="user.id" type="checkbox" class="checkbox">
                  <span></span>
                </span>
              </td>

              <td>
                <span class="text">{{ user.id }}</span>
              </td>
              <td>
                <span class="text">{{ parseName(user.name)[0] }}</span>
              </td>
              <td>
                <span class="text">{{ parseName(user.name)[1] }}</span>
              </td>
              <td>
                <a href="javascript:void(0);" class="text">{{ user.email }}</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="uiAction uiActionBorder">
      <a href="javascript:void(0);" class="btn" @click="save">{{ $t('ExoCalendarEventForm.btn.add') }}</a>
      <a href="javascript:void(0);" class="btn" @click="cancel">{{ $t('ExoCalendarEventForm.btn.close') }}</a>
    </div>
  </div>
</template>

<script>
import * as calServices from '../calServices.js';

export default {  
  data() {
    return {
      username: '',
      users: [],
      checkall: false,
      selectedUsers: []
    };
  },
  watch: {
    checkall() {
      if (this.checkall) {
        this.selectedUsers = this.users.map(u => u.id);
      }
    },
    username() {
      this.findPars();
    }
  },
  mounted() {
    this.findPars();
  },
  methods: {
    findPars() {
      const limit = 20;
      calServices.findParticipants(this.username, limit).then(pars => {
        this.users = [];
        if (pars) {
          pars.forEach(par => {
            this.users.push({
              id: par.id,
              name: par.name,
              email: par.email
            });
          });
        }
      });
    },
    parseName(name) {
      if (name) {
        const parts = name.split(' ').map(p => p.trim()).filter(p => p !== '');
        if (parts.length > 1) {
          return [parts[0], parts.slice(1).join(' ')];
        } else {
          return [parts[0], ''];
        }
      } else {
        return ['', ''];
      }
    },
    save() {
      const addUsers = this.selectedUsers.filter(username => {
        return this.users.some(u => u.id === username);
      });
      this.selectedUsers = [];
      this.$emit('save', addUsers);
    },
    cancel() {
      this.$emit('cancel');
    }
  }  
};
</script>
