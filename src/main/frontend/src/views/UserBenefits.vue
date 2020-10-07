<template>
  <div class="page">
    <section class="page__hero">
      <div class="page__hero__inner">
        <h1>Explore. Create. Expand.</h1>
        <h2>Benefits of purchasing geodata on Topio.</h2>
      </div>
    </section>
    <section class="servicelist">
      <div class="servicelist__row">
        <div class="servicelist__row__line" ></div>
        <div class="servicelist__text" data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>Anything you’re looking for</h3>
            <p>As a data user on Topio, you’ll be able to find and buy high-quality geospatial data assets in a one-stop shop. Explore, compare, and purchase what you need, regardless the type or size of the asset. It’s all there in our secure data lake.</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-left">
          <img src="@/assets/images/why_buy/anything.jpg" alt="">
        </div>
      </div>
      <div class="servicelist__row">
        <div class="servicelist__row__line" ></div>
        <div class="servicelist__text"  data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>A metadata extravaganza</h3>
            <p>You’ll gain access to a constant stream of assets with rich, detailed, and accurate metadata. We’ll be on your side, with better and better analysis for you to scale your database and go big!</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-right">
          <img src="@/assets/images/why_buy/metadata.jpg" alt="">
        </div>
      </div>
      <div class="servicelist__row">
        <div class="servicelist__row__line" ></div>
        <div class="servicelist__text"  data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>Safety and clarity first</h3>
            <p>With clear terms and licensing, streamlined and secure payment, and safe delivery methods, you’ll feel right at home by being part of our community.</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-left">
          <img src="@/assets/images/why_buy/safety.jpg" alt="">
        </div>
      </div>
      <div class="servicelist__row">
        <div class="servicelist__row__line" ></div>
        <div class="servicelist__text"  data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>More than just another dataset</h3>
            <p>Topio is the marketplace where open and proprietary geospatial data meet. Save, manage, and use your new assets by making the most out of them with our top-class value-added services. On top of this, with concise notifications all in one (yes, one!) place, secure storage, and comprehensive helpdesk support, you’ll see that the workflow on Topio is smoother than any other.</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-right">
          <img src="@/assets/images/why_buy/more_than.jpg" alt="">
        </div>
      </div>
      <div class="servicelist__row">
        <div class="servicelist__row__line" ></div>
        <div class="servicelist__text"  data-aos="fade-up">
          <div class="servicelist__text__inner">
            <h3>What we offer for free</h3>
            <p>With concise notifications all in one (yes, one!) place, secure storage, and comprehensive helpdesk support, you’ll see that the workflow on Topio is smoother than any other.</p>
          </div>
        </div>
        <div class="servicelist__image" data-aos="fade-left">
          <img src="@/assets/images/why_buy/offer_free.jpg" alt="">
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
export default class UserBenefits extends Vue {
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
