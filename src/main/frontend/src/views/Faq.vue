<template>
  <div class="page terms">
    <div class="m_container">
      <div class="terms__inner">
        <div class="terms__sidebar">
            <h1 class="terms__sidebar__title">FAQ</h1>
            <div class="terms__sidebar__menu">
              <ul>
                <li><a id="side1" :class="{active: activeMenuItem == 1}" href="#" @click.prevent="scrollToSection('1')">Topio services</a></li>
                <li><a id="side2" :class="{active: activeMenuItem == 2}" href="#" @click.prevent="scrollToSection('2')">Vendors</a></li>
                <li><a id="side3" :class="{active: activeMenuItem == 3}" href="#" @click.prevent="scrollToSection('3')">Data users</a></li>
              </ul>
            </div>
        </div>
        <div id="terms-main" class="terms__main">
          <h2 class="terms__main__title" id="main1">Topio services</h2>
          <div class="terms__main__text">
            <h3>Faq</h3>
            <p>These General Terms and Conditions apply to all uses of the Service, and all contractual relationships established between the company Dawex and its Clients on the site that is accessible at the address https://www.dawex.com, between:</p>
            <p>The company Dawex Systems, a simplified joint-stock company under French law, with headquarters at 20 place Louis Pradel, 69001 LYON (France), registered at the Lyon RCS under the number 810 307 207, represented by its acting legal representatives (hereafter “Dawex”); and any legal person acting in his/her capacity as a professional governed by private or public law desiring to benefit from one or more services provided by the company Dawex.</p>
            <h3>What is the fee for these services?</h3>
            <p>These General Terms and Conditions apply to all uses of the Service, and all contractual relationships established between the company Dawex and its Clients on the site that is accessible at the address https://www.dawex.com, between:</p>
            <p>The company Dawex Systems, a simplified joint-stock company under French law, with headquarters at 20 place Louis Pradel, 69001 LYON (France), registered at the Lyon RCS under the number 810 307 207, represented by its acting legal representatives (hereafter “Dawex”); and any legal person acting in his/her capacity as a professional governed by private or public law desiring to benefit from one or more services provided by the company Dawex.</p>
            <p>To use the services provided by Dawex on the Platform, the Client must acknowledge and accept these General Terms and Conditions without limitation or reservation. This acceptance is confirmed when the Client creates his/her account on the site.</p>
          </div>
          <h2 class="terms__main__title" id="main2">Vendors</h2>
          <div class="terms__main__text">
            <h3>Who can become a Topio Vendor?</h3>
            <p>These General Terms and Conditions apply to all uses of the Service, and all contractual relationships established between the company Dawex and its Clients on the site that is accessible at the address https://www.dawex.com, between:</p>
            <p>The company Dawex Systems, a simplified joint-stock company under French law, with headquarters at 20 place Louis Pradel, 69001 LYON (France), registered at the Lyon RCS under the number 810 307 207, represented by its acting legal representatives (hereafter “Dawex”); and any legal person acting in his/her capacity as a professional governed by private or public law desiring to benefit from one or more services provided by the company Dawex.</p>
            <p>To use the services provided by Dawex on the Platform, the Client must acknowledge and accept these General Terms and Conditions without limitation or reservation. This acceptance is confirmed when the Client creates his/her account on the site.</p>
            <h3>What type of assets can I sell as a Vendor?</h3>
            <p>These General Terms and Conditions apply to all uses of the Service, and all contractual relationships established between the company Dawex and its Clients on the site that is accessible at the address https://www.dawex.com, between:</p>
            <p>The company Dawex Systems, a simplified joint-stock company under French law, with headquarters at 20 place Louis Pradel, 69001 LYON (France), registered at the Lyon RCS under the number 810 307 207, represented by its acting legal representatives (hereafter “Dawex”); and any legal person acting in his/her capacity as a professional governed by private or public law desiring to benefit from one or more services provided by the company Dawex.</p>
          </div>
          <h2 class="terms__main__title" id="main3">Data users</h2>
          <div class="terms__main__text">
            <h3>Who can become a Topio Vendor?</h3>
            <p>These General Terms and Conditions apply to all uses of the Service, and all contractual relationships established between the company Dawex and its Clients on the site that is accessible at the address https://www.dawex.com, between:</p>
            <ul>
              <li>Free Offerings or Offerings in return for payment,</li>
              <li>Free Offerings or Offerings in return for payment,</li>
              <li>Free Offerings or Offerings in return for payment,</li>
              <li>Free Offerings or Offerings in return for payment,</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import AOS from 'aos';
import 'aos/dist/aos.css';

@Component
export default class Terms extends Vue {
  activeMenuItem = 1;

  TitleOffsets = [];

  topPos = 0;

  mounted():void {
    this.initAOS();
    window.addEventListener('scroll', this.changeActiveSideItem);
  }

  changeActiveSideItem():void {
    const titles:any = document.getElementsByClassName('terms__main__title');
    // const titlesArray = Array.prototype.slice.call(titles);
    for (let i = 0; i <= titles.length; i += 1) {
      if (titles[i]) {
        if (document.documentElement.scrollTop + 245 >= titles[i].offsetTop) {
          this.activeMenuItem = i + 1;
        }
      }
    }
  }

  initAOS = (): void => {
    AOS.init({
      duration: 600,
      easing: 'ease-in-out-cubic',
    });
  }

  scrollToSection(section:number):void {
    this.activeMenuItem = section;
    const MainID = `main${section}`;
    const MainEl = document.getElementById(MainID);
    if (MainEl) {
      this.topPos = MainEl.offsetTop;
    }
    document.documentElement.scrollTop = this.topPos - 230;
  }
}
</script>
<style lang="scss">
  @import "@/assets/styles/_page.scss";
  @import "@/assets/styles/_terms.scss";
</style>
