<template>
  <div class="page page--nooverflow">
    <section class="page__hero">
      <div class="page__hero__inner">
        <h1>Share. Trade. Grow.</h1>
        <h2>Benefits of becoming a Topio vendor.</h2>
      </div>
    </section>
    <section class="servicelist">
      <div class="servicelist__row">
        <div class="servicelist__row__line"></div>
        <div class="servicelist__text" data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>Chart your own course</h3>
            <p>As a vendor on Topio, the pan-European platform, you’ll be free to forge down your own path while choosing between multiple options that fit your needs. With full control over your data in our secure data lake, you’ll expand your market share.</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-left">
          <img src="@/assets/images/why_sell/chart.jpg" alt="">
        </div>
      </div>
      <div class="servicelist__row">
        <div class="servicelist__row__line"></div>
        <div class="servicelist__text" data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>Size doesn’t matter</h3>
            <p>Whether you want to start small or think BIG, we are here to help you thrive. After choosing your pricing and contracting model, you’ll set your own terms and you’ll be ready to reach an international audience. Oh, did we mention that there are no fees?</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-right">
          <img src="@/assets/images/why_sell/size.jpg" alt="">
        </div>
      </div>
      <div class="servicelist__row">
        <div class="servicelist__row__line"></div>
        <div class="servicelist__text" data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>What we offer for free</h3>
            <p>Rich analytics, concise notifications all in one (yes, one!) place, secure storage, helpdesk support, and SLA contracting will all make your life easier.</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-left">
          <img src="@/assets/images/why_sell/offer_free.jpg" alt="">
        </div>
      </div>
      <div class="servicelist__row">
        <div class="servicelist__row__line"></div>
        <div class="servicelist__text" data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>Seeds of growth</h3>
            <p>We want to see you grow, so Topio is focused on making this process smooth, easy, and stress-free. Whether your interests lie in aerial imagery, geospatial databases, maps, web GIS, or geospatial assets, we can help you grab some of the spotlight in the magical world of geodata.</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-right">
          <img src="@/assets/images/why_sell/seed_growth.jpg" alt="">
        </div>
      </div>
    </section>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import AOS from 'aos';
import 'aos/dist/aos.css';

@Component
export default class VendorBenefits extends Vue {
  mounted():void {
    this.initAOS();
  }

  initAOS = (): void => {
    AOS.init({
      duration: 600,
      easing: 'ease-in-out-cubic',
    });
  }
}
</script>
<style lang="scss">
  @import "@/assets/styles/_page.scss";
</style>
