<template>
  <div class="dashboard__inner">
    <div class="dashboard__head">
      <div class="dashboard__head__left">
        <h1>Favorites</h1>
      </div>
    </div>

    <div class="collection">
      <div class="collection__menu">
        <ul>
          <li class="active"><a href="#" @click.prevent="">Assets</a></li>
          <li><a href="#" @click.prevent="">Vendors</a></li>
        </ul>
      </div>

      <div class="collection__total"><p>0 assets</p></div>

      <div class="collection__main">

        <div class="more__bottom">
          <div class="more__bottom__block">
            <!-- <a href="#"> <p>LOAD MORE</p><p><svg xmlns="http://www.w3.org/2000/svg" width="17.404" height="25.65" viewBox="0 0 17.404 25.65"><path id="Path_2292" data-name="Path 2292" d="M-1105.012-7721.223l11.469 14.086 11.871-14.086" transform="translate(-7704.786 1106.175) rotate(90)" fill="none" stroke="#333" stroke-width="3"></path></svg></p> </a> -->
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import CatalogueCard from '@/components/Catalogue/Card.vue';

@Component({
  components: { CatalogueCard },
})
export default class DashboardFavorites extends Vue {
}
</script>
<style lang="scss">
  @import "@/assets/styles/_collection.scss";
  @import "@/assets/styles/_messages.scss";
</style>
