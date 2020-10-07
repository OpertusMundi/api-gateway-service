<template>
  <div class="errorpage">
    <div class="errorpage__inner">
      <div class="errorpage__code" v-if="this.$route.params.error == '404'">
        <img src="@/assets/images/404.svg" alt="">
      </div>
      <div class="errorpage__code" v-else>
        <h1>{{$route.params.error}}</h1>
      </div>
      <div class="errorpage__buttons">
        <router-link to="/" class="btn btn--std btn--blue">HOMEPAGE</router-link>
        <router-link to="/" class="btn btn--std btn--blue">CATALOGUE</router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';

@Component
export default class ErrorPage extends Vue {
  // constructor() {
  //   super();
  // }
  mounted():void {
    console.log(this.$route.params.error);
  }
}
</script>
<style lang="scss">
  @import "@/assets/styles/_errorpage.scss";
</style>
