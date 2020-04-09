<template>
  <v-app id="events" class="VuetifyApp" flat>
    <v-container pa-0>
      <v-layout row mx-0 class="white">
        <v-flex d-flex xs12 px-3>
          <v-layout row mx-0 align-center>
            <v-flex d-flex xs12>
              <v-card flat color="transparent">
                <a href="#">
                  <v-card-text class="body-1 text-uppercase color-title px-0">
                    {{ this.$t('Events.header.title') }}
                  </v-card-text>
                </a>
              </v-card>
            </v-flex>
          </v-layout>
        </v-flex>
        <v-flex d-flex xs12 px-3>
          <v-layout row>
            <v-flex d-flex xs12 pl-3>
              <v-flex d-flex xs2>
                <v-layout column>
                  <v-avatar color="#568DC9" class="pa-2" size="25">
                    <span class="white--text font-weight-bold" style="font-size: large">19</span>
                  </v-avatar>
                  <v-avatar size="20">
                    <span class="ml-5 blueText font-weight-bold">OCT</span>
                  </v-avatar>
                </v-layout>
              </v-flex>
              <v-flex d-flex xs10 row>
                <v-list-item v-for="item in items1" :key="item.title">
                  <v-list-item-content class="py-0">
                    <v-card
                      :color="item.color"
                      flat
                      class="mb-3 py-1 pl-0"
                      dark
                      @click.stop="getEventDrawer()">
                      <v-list-item-title class="ml-2">{{ item.title }}</v-list-item-title>
                      <v-list-item-title class="ml-2">{{ item.date }}</v-list-item-title>
                    </v-card>
                  </v-list-item-content>
                </v-list-item>
              </v-flex>
            </v-flex>
            <v-flex
              d-flex
              xs12
              pl-3>
              <v-flex
                d-flex
                xs2>
                <v-layout column>
                  <v-avatar size="27" class="px-2">
                    <span class="blueGrey-Color font-weight-bold" style="font-size: large">20</span>
                  </v-avatar>
                  <v-avatar size="20" width="50">
                    <span class="mr-1 blueGrey-Color font-weight-bold">OCT</span>
                  </v-avatar>
              </v-layout></v-flex>
              <v-flex
                d-flex
                xs10
                row><v-list-item
                  v-for="item in items2"
                  :key="item.title">
                  <v-list-item-content class="py-0">
                    <v-card
                      :color="item.color"
                      flat
                      class="mb-3 py-1 pl-0"
                      dark
                      @click="getEventDrawer()">
                      <v-list-item-title class="ml-2">{{ item.title }}</v-list-item-title>
                      <v-list-item-title class="ml-2">{{ item.date }}</v-list-item-title>
                    </v-card>
                  </v-list-item-content>
              </v-list-item></v-flex>
            </v-flex>
          </v-layout>
        </v-flex>
    </v-layout></v-container>
    <exo-event-form
      :open="drawer"
      :init-evt="calEvt"
      @toggle-open="onCloseDrawer"/>
  </v-app>
</template>
<script>
export default {
  data () {
    return {
      calEvt: {
        id: '55',
        title: 'Training Javascript',
      },
      drawer: false,
      items1: [
        { title: 'Training Javascript', date: '8:00 - 9:00' ,color:'#52B68D'},
        { title: 'Home page Meeting', date: '12:00 - 13:30' ,color:'#D07B7B'},
        { title: 'Product meeting', date: '15:00 - 16:30' ,color:'#FBB340'},
      ],
      items2: [
        { title: 'Feature Team Gamification', date: '11:00 - 11:15' ,color:'#999999'},
        { title: 'Feature Team News', date: '08:00 - 09:30' ,color:'#A7B3C5'},
      ],
    };
  },
  methods: {
    getEventDrawer() {
      this.drawer = !this.drawer;
      document.body.style.overflow = this.drawer ? 'hidden' : 'auto';
    },
    onCloseDrawer: function(){
      this.drawer = !this.drawer;
      document.body.style.overflow = 'auto';
    }
  }    
};
</script>