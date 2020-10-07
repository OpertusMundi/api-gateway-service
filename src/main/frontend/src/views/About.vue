<template>
  <div class="page page--nooverflow about">
    <section class="page__hero page__hero--about">
      <div class="page__hero__inner">
        <div class="page__hero__head s_container">
          <h1>Search. Share. Trade. Use.</h1>
          <h2>We are here to deliver a breakthrough in the trading and sharing of commercial geospatial data assets across borders and sectors. All in an easy way; all through a single point of entry. </h2>

          <div class="about__sections">
            <div class="about__sections__row" data-aos="" data-aos-offset="350">
              <div class="about__sections__row__block about__sections__row__block--main">
                <h3 class="about__sections__row__block__title">Our vision</h3>
                <div class="about__sections__row__block__text">
                  <p>We want to provide a central one-stop hub for geospatial data that guarantees affordability and flexibility in line with the needs of all users regardless of their size, domain, and expertise. We plan to make it simple, fast, cost-effective and safe for data owners and data users alike.</p></div>
              </div>
              <div class="about__sections__row__block about__sections__row__block--image">
                <img src="@/assets/images/about/about1.svg" alt="">
              </div>
            </div>
            <div class="about__sections__row" data-aos="" data-aos-offset="350">
              <div class="about__sections__row__block about__sections__row__block--main">
                <h3 class="about__sections__row__block__title">Geodata makes the world go round</h3>
                <div class="about__sections__row__block__text">
                  <p>Geospatial data is the cornerstone of any application, service, and product even remotely related to our physical surroundings. From navigation applications and social networks to tourism and logistics, geospatial data assets constitute the core foundations, enablers, and value multipliers of our physical and digital economies.</p>
                  <p>Like the Ancient Agora of Athens, Topio will be the platform of the future; where geodata - the most valuable good of our time – will be traded. </p>
                </div>
              </div>
              <div class="about__sections__row__block about__sections__row__block--image">
                <img src="@/assets/images/about/about2.svg" alt="">
              </div>
            </div>
            <div class="about__sections__row" data-aos="" data-aos-offset="350">
              <div class="about__sections__row__block about__sections__row__block--main">
                <h3 class="about__sections__row__block__title">Our mission</h3>
                <div class="about__sections__row__block__text">
                  <p>Topio is on a mission to make the first Digital Single Market for geospatial data a reality and bridge the gap between policy intentions and industrial uptake with high-quality, value-added services. We want to put an end to the heterogeneity, disparity, and fragmentation of geospatial data products in an inclusive, cross-border manner.</p>
                  <p>Topio is a central hub and a one-stop shop for the streamlined and trusted discovery, remuneration, sharing, trading, and use of proprietary and commercial geospatial assets. We guarantee low costs and flexibility to accommodate the current and emerging needs of Data Economy stakeholders. </p>
                </div>
              </div>
              <div class="about__sections__row__block about__sections__row__block--image">
                <img src="@/assets/images/about/about3.svg" alt="">
              </div>
            </div>
            <div class="about__sections__row" data-aos="" data-aos-offset="350">
              <div class="about__sections__row__block about__sections__row__block--main">
                <h3 class="about__sections__row__block__title">The partners</h3>
                <div class="about__sections__row__block__text">
                  <p>Topio is a Horizon 2020 project being implemented by an excellence-driven multifaceted consortium. It covers aspects of innovation, technology, and the real-world challenges along the geospatial data value chain. The consortium involves innovation organizations, geospatial asset owners based in the EU, consumers and value-added service providers, a legal firm, and an SME with expertise in UI/UX design for high-impact digital marketplaces. The effort to assemble the Topio marketplace is part of the Horizon 2020 project Opertus Mundi.</p></div>
                  <ul class="about__sections__row__block__partners">
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/athina_logo.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/tudelft-logo.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/Fraunhofer-iais_grey.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/get_grey_crop.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/sinergise.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/CMG.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/AZO.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/wigeogis_black.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/georeupublic.svg" alt=""></a></li>
                    <li><a href="#" target="_blank"><img src="@/assets/images/about/partners/roleplay_logo.svg" alt=""></a></li>
                  </ul>
              </div>
              <div class="about__sections__row__block about__sections__row__block--image">
                <img src="@/assets/images/about/about4.svg" alt="">
              </div>
            </div>
          </div>
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
export default class About extends Vue {
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
