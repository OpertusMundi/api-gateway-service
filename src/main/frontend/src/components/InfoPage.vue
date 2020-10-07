<template>
  <div class="infopage">
    <shape></shape>
    <div class="infopage__content">
      <h1>{{ title }}</h1>
      <p v-html="text"></p>
      <router-link :to="btnLink" class="btn btn--std btn--teal">{{ btnText }}</router-link>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import Shape from '@/components/Shape.vue';

@Component({
  components: { Shape },
})
export default class InfoPage extends Vue {
  @Prop({ required: true }) readonly title!: string;

  @Prop({ required: true }) readonly text!: string;

  @Prop({ required: false }) readonly btnText!: string;

  @Prop({ required: false }) readonly btnLink!: string;
}
</script>
<style lang="scss">
  @import "@/assets/styles/_infopage.scss";
</style>
