<template>
  <div id="ExoEventForm" :class="{open}" class="drawer">
    <div class="header">
      <span>{{ $t('ExoEventForm.title.addEvent') }}</span>
      <a href="javascript:void(0)" class="closebtn" @click="toggleOpen">&times;</a>
    </div>
    <div class="content">
      <form onsubmit="return false;" action="" method="post" enctype="multipart/form-data" accept-charset="utf-8">
        <div class="control-group">
          <div class="controls">
            <input :placeholder="$t('ExoEventForm.placeholder.eventTitle')" name="eventName" class="eventTitle" type="text"/>

            <span class="uiSelectbox">
              <select class="selectbox category" name="category">
                <option value="defaultEventCategoryIdAll">All</option>
                <option value="defaultEventCategoryIdMeeting">Meeting</option>
                <option value="defaultEventCategoryIdCalls">Calls</option>
                <option value="defaultEventCategoryIdClients">Clients</option>
                <option value="defaultEventCategoryIdHoliday">Holiday</option>
                <option value="defaultEventCategoryIdAnniversary">Anniversary</option>
              </select>
            </span>
          </div>
        </div>
        <div class="control-group calDate">
          <div class="control-label">{{ $t('ExoEventForm.label.from') }}</div>
          <div class="controls">
            <input format="MM/dd/yyyy" type="date" value="12/24/2018"/>
            <input type="time" min="9:00" max="18:00" step="1800"/>
          </div>
        </div>
        <div class="control-group calDate">
          <div class="control-label">{{ $t('ExoEventForm.label.to') }}</div>
          <div class="controls">
            <input format="MM/dd/yyyy" type="date" value="12/24/2018"/>
            <input type="time" min="9:00" max="18:00" step="1800"/>
          </div>
        </div>
        <div class="control-group allday">
          <div class="controls">
            <input type="checkbox"/>
            <label for="allday">{{ $t('ExoEventForm.label.allDay') }}</label>
          </div>
        </div>
        <div class="control-group">
          <div class="control-label">{{ $t('ExoEventForm.label.location') }}</div>
          <div class="controls">
            <input :placeholder="$t('ExoEventForm.placeholder.location')" type="text" class="location"/>
          </div>
        </div>
        <div class="control-group">
          <div class="control-label">{{ $t('ExoEventForm.label.participants') }}</div>
          <div class="controls">
            <input :placeholder="$t('ExoEventForm.placeholder.participants')" type="text" class="participants"/>
          </div>
        </div>
        <div class="control-group calendarSelector">
          <div class="control-label">{{ $t('ExoEventForm.label.calendar') }}</div>
          <div class="controls">
            <span class="uiSelectbox">
              <select class="selectbox" name="calendar">
                <optgroup label="Personal Calendars">
                  <option value="0:root-defaultCalendarId">Root Root</option>
                </optgroup>
                <optgroup label="Group Calendars">
                  <option value="2:space2_space_calendar">space2</option>
                  <option value="2:calendarcfaff4877f0001011b34ddb064abdf27">Administration</option>
                  <option value="2:space1_space_calendar">space1</option>
                  <option value="2:calendarcfaff60c7f0001013e5b9a310aa25ea7">Users</option>
                  <option value="2:calendarcfaffdf97f00010130765e9cda51ccd7">Executive Board</option>
                  <option value="2:space5_space_calendar">space5</option>
                  <option value="2:calendarcfafff7e7f00010109a4ccbdfb7d7d60">Employees</option>
                  <option value="2:space4_space_calendar">space4</option>
                  <option value="2:space3_space_calendar">space3</option>
                  <option value="2:calendarcfaff8767f00010117208e2d9d8a58a4">Content Management</option>
                </optgroup>
              </select>
            </span>
          </div>
        </div>
        <div class="control-group reminder">
          <div class="control-label">{{ $t('ExoEventForm.label.reminder') }}</div>
          <div class="controls">
            <input type="checkbox" class="iphone"/>
          </div>
        </div>
        <div class="control-group repeat">
          <div class="control-label">{{ $t('ExoEventForm.label.repeat') }}</div>
          <div class="controls">
            <input type="checkbox" class="iphone"/>
          </div>
        </div>
        <div class="control-group description">
          <div class="control-label">{{ $t('ExoEventForm.label.description') }}</div>
          <div class="controls">
            <textarea :placeholder="$t('ExoEventForm.placeholder.description')"></textarea>
          </div>
        </div>
        <div class="control-group attachments">
          <div class="control-label">{{ $t('ExoEventForm.label.attachments') }}</div>
          <div class="controls">
            <textarea></textarea>
          </div>
        </div>
      </form>
    </div>
    <div class="footer">
      <div class="uiAction">
        <button type="button" class="btn btn-primary">{{ $t('ExoEventForm.btn.save') }}</button>
        <button type="button" class="btn">{{ $t('ExoEventForm.btn.cancel') }}</button>
        <button type="button" class="btn clearBtn">{{ $t('ExoEventForm.btn.clear') }}</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  model: {
    prop: 'open',
    event: 'toggle-open'
  },
  props: {
    open: {
      type: Boolean,
      default: false
    }
  },
  mounted() {
    $('.drawer .iphone').iphoneStyle({
      disabledClass: 'switchBtnDisabled',
      containerClass: 'uiSwitchBtn',
      labelOnClass: 'switchBtnLabelOn',
      labelOffClass: 'switchBtnLabelOff',
      handleClass: 'switchBtnHandle',
    });
  },
  methods: {
    toggleOpen() {
      this.open = !this.open;
      this.$emit('toggle-open', this.open);
    }
  }
};
</script>

<style src="../css/ExoEventForm.less"></style>
