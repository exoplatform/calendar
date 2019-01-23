<template>
  <div id="eventAttender-tab" class="findTimeForm">
    
    <div class="uiAddNewEvent">
      <div class="newEventAttendee">
        <div class="uiFormInputSet">
          <div class="uiFormInputSetWithAction">
            <div class="formContainer">
              <div class="dateTimeContainer clearfix">
                <div :title="$t('UIEventForm.label.checkTimeHelp')" class="applySelectedDate pull-left" rel="tooltip" data-placement="bottom">
                  <div class="pull-right">
                    <span class="uiCheckbox">
                      <input id="checkTime" type="checkbox" class="checkbox" name="checkTime" @click="checkTime"><span></span>
                    </span>
                  </div>
                  <span class="checktime-lbl">
                    {{ $t('UIEventForm.label.checkTime') }}:
                  </span>
                </div>
                <div class="pull-right allDay">
                  <div class="pull-left allday-lbl">{{ $t('UIEventForm.label.dateAll') }}:</div>
                  <div class="pull-left">
                    <span class="uiCheckbox">
                      <input id="dateAll" v-model="isAllDay" type="checkbox" class="checkbox" name="dateAll"><span></span>
                    </span>
                  </div>
                </div>
                <div class="pull-right fromTo">
                  <div class="pull-left timeField">				
                    <div class="pull-left">
                      <div class="pull-left title">
                        {{ $t('UIEventForm.label.timeTo') }}:
                      </div>
                      <div class="pull-left inputSmall ">
                        <input :disabled="isAllDay" :value="fromTime()" class="time fromTime UIComboboxInput" type="text" @change="updateTime(fromDate, $event.target.value)"/>  
                      </div>
                    </div>
                    <div class=" pull-left">
                      <div class="pull-left title">
                        {{ $t('UIEventForm.label.timeFrom') }}:
                      </div>
                      <div class="pull-left inputSmall ">
                        <input :disabled="isAllDay" :value="toTime()" class="time toTime UIComboboxInput" type="text" @change="updateTime(toDate, $event.target.value)"/>
                      </div>
                    </div>
                  </div> 
                </div>                
              </div>
            </div>

            <table id="RowContainerDay" :dateValue="fromDate.toISOString()" class="uiGrid" cellspacing="0" borderspacing="0" exocallback="eXo.calendar.UICalendarPortlet.callbackSelectionX() ;">
              <tbody>
                <tr class="titleBar">
                  <td style="width:28%;">
                    <div class="leftSide">
                      <a :title="$t('UIDayView.label.previousDay')" rel="tooltip" data-placement="bottom" @click="moveDatePrev">
                        <i class="uiIconMiniArrowLeft uiIconLightGray"></i>
                      </a>
                      <span class="title">
                        {{ currDate() }}
                      </span>
                      <a :title="$t('UIDayView.label.nextDay')" rel="tooltip" data-placement="bottom" @click="moveDateNext">
                        <i class="uiIconMiniArrowRight uiIconLightGray"></i>
                      </a>
                    </div>
                  </td>
                  <td v-for="hour in 24" :key="hour" colspan="4" class="timeNumber">{{ formatHour(hour - 1) }}</td>
                </tr>
                <tr>
                  <td>
                    <div class="leftSide">
                      <span class="title">
                        {{ $t('UIEventForm.label.Participants') }}
                      </span>					
                      <a :title="$t('UIEventForm.label.action.AddUser')" class="actionIcon" rel="tooltip" data-placement="bottom" @click="showAddParticipant">
                        <i class="uiIconCalInviteUser uiIconLightGray" ></i>
                      </a>
                      <a :title="$t('UIEventForm.label.DeleteUsername')" class="actionIcon" rel="tooltip" data-placement="bottom" @click="removeParticipant">
                        <i class="uiIconCalRejectUser uiIconLightGray" ></i>
                      </a>
                    </div>
                  </td>                 
                  <td v-for="i in 96" :key="i" class="participantsFreeTime uiCellBlock" >
                    <span :title="$t('UIEventForm.label.ScheduleDragDrop')" rel="tooltip" data-placement="bottom">&nbsp;</span>
                  </td>
                </tr>
                
                <tr v-for="user in users" :key="user.id" :busytime="user.availability">
                  <td>
                    <div class="leftSide">
                      <div class="input">
                        <label class="uiCheckbox">
                          <input :id="user.id" :name="user.id" type="checkbox" class="checkbox"><span>{{ user.name }}</span>
                        </label>
                      </div>
                    </div>
                  </td>                  
                  <td v-for="j in 96" :key="j" class="freeTime">
                    &nbsp;
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="thiefBox">
              <span class="freeTime ">
                <span></span>{{ $t('UIEventForm.label.FreeTimes') }}
              </span>	
              <span class="busyTime ">
                <span></span>{{ $t('UIEventForm.label.BusyTimes') }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="uiAction uiActionBorder">
      <button class="btn" type="button" @click="save">
        {{ $t('ExoEventForm.btn.save') }}
      </button>

      <button class="btn" type="button" @click="cancel">
        {{ $t('ExoEventForm.btn.cancel') }}
      </button>
    </div>
  </div>
</template>

<script>
import {calConstants} from '../calConstants.js';
import Utils from '../model/utils.js';
import * as calServices from '../calServices.js';

const MID_NIGHT_HOUR = 23;
const MID_NIGHT_MINUTE = 59;

export default {
  props: {
    'participants': {
      type: Array,
      default: []
    },
    'from': {
      type: Object,
      default: new Date()
    },
    'to': {
      type: Object,
      default: new Date()
    }
  },
  data() {
    return {
      fromDate: new Date(),
      toDate: new Date(),
      usernames: [],
      users: [],
      isAllDay: false
    };
  },
  watch: {    
    participants() {
      this.usernames = this.participants.slice();
    },
    usernames() {
      this.users = [];

      this.usernames.forEach(username => {
        calServices.findParticipants(username).then(resp => {
          if(resp) {
            resp.forEach(user => {
              if (user.id === username) {
                const par = this.users.find(u => {
                  return u.id = user.id;
                });
                if (par) {
                  Vue.set(par, 'name', user.name);     
                } else {
                  this.users.push({
                    id: user.id,
                    name: user.name,
                    availability: ''
                  });
                }
              }
            });       
          }
        });
      });
      
      this.checkTime();
    },
    from() {
      this.fromDate = new Date(this.from.getTime());
      this.toDate = new Date(this.from.getTime());
    },
    to() {
      const hours = this.to.getHours();
      const minutes = this.to.getMinutes();
      const seconds = this.to.getSeconds();
      const miliseconds = this.to.getMilliseconds();
      this.toDate.setHours(hours, minutes, seconds, miliseconds);
      this.refreshDate();
    },
    isAllDay() {
      if (this.isAllDay) {
        const fromDate = new Date();
        fromDate.setTime(this.fromDate.getTime());
        fromDate.setHours(0, 0, 0, 0);
        this.fromDate = fromDate;

        const toDate = new Date();
        toDate.setTime(this.toDate.getTime());
        toDate.setHours(MID_NIGHT_HOUR, MID_NIGHT_MINUTE, 0, 0);
        this.toDate = toDate;
      }
    }
  },
  mounted() {
    const thiss = this;
    $('.findTimeForm .time').each(function(idx, input) {
      $(input).on('change', function() {
        if ($(input).hasClass('fromTime')) {
          thiss.updateTime(thiss.fromDate, $(input).val());
        } else {
          thiss.updateTime(thiss.toDate, $(input).val());
        }
      });
    });
  },
  methods: {
    fromTime() {
      return Utils.formatTime(this.fromDate);
    },
    toTime() {
      return Utils.formatTime(this.toDate);
    },
    updateTime(date, val) {
      const parsedVal = Utils.parseTime(val);
      if (parsedVal) {
        date.setHours(parsedVal.getHours(), parsedVal.getMinutes(), parsedVal.getSeconds(), parsedVal.getMilliseconds());
      }
      ScheduleSupport.applyPeriod();
      this.refreshDate();
    },
    currDate() {
      return Utils.formatDate(this.fromDate);
    },
    moveDatePrev() {
      this.fromDate.setDate(this.fromDate.getDate() - 1);      
      this.toDate.setDate(this.toDate.getDate() - 1);
      this.refreshDate();      
    },
    moveDateNext() {
      this.fromDate.setDate(this.fromDate.getDate() + 1);
      this.toDate.setDate(this.toDate.getDate() + 1);
      this.refreshDate();
    },
    refreshDate() {
      this.fromDate = new Date(this.fromDate.getTime());
      this.toDate = new Date(this.toDate.getTime());
    },
    formatHour(hour) {
      const sub = -2;
      return `0${hour}`.slice(sub);
    },    
    showAddParticipant() {
      // console.log('show');
    },
    removeParticipant() {
      // console.log('remove');
    },
    checkTime() {
      calServices.getAvailability(this.usernames, this.fromDate.getTime(), this.toDate.getTime()).then(availMap => {
        if (availMap) {
          Object.entries(availMap).forEach(entry => {
            const username = entry[0];
            const avail = entry[1];

            const par = this.users.find(u => {
              return u.id = username;
            });
            if (par) {
              Vue.set(par, 'availability', avail);
            } else {
              this.users.push({
                id: username,
                name: '',
                availability: avail
              });
            }
          });
        }

        Vue.nextTick(() => {
          const timezone = calConstants.SETTINGS.timezone;           
          eXo.calendar.UICalendarPortlet.initCheck('eventAttender-tab', timezone * -1);
          ScheduleSupport.applyPeriod();
        });
      });
    },
    save() {
      this.from.setTime(this.fromDate.getTime());
      this.to.setTime(this.toDate.getTime());
      this.$emit('save', {from: this.from, to: this.to});
    },
    cancel() {
      this.fromDate.setTime(this.from.getTime());
      this.toDate.setTime(this.to.getTime());
      this.refreshDate();
      this.$emit('cancel');
    }
  }  
};
</script>
