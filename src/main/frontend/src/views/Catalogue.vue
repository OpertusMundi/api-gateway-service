<template>
  <div class="assets">
    <div class="s_container">
      <div class="assets__head">
        <h1>Assets</h1>
      </div>
      <div class="assets__items">
        <catalogue-card v-for="asset in queryResults" v-bind:key="asset.id" :asset="asset"></catalogue-card>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import CatalogueCard from '@/components/Catalogue/Card.vue';
import CatalogueApi from '@/service/catalogue';
import {
  CatalogueQueryResponse, CatalogueQuery, CatalogueItem,
} from '@/model';
import { AxiosError } from 'axios';

@Component({
  components: { CatalogueCard },
})
export default class Catalogue extends Vue {
  catalogQuery: CatalogueQuery;

  catalogueApi: CatalogueApi;

  queryResults: CatalogueItem[];

  query:string;

  loading = false;

  constructor() {
    super();

    this.query = '';
    this.queryResults = [];
    this.catalogQuery = {
      page: 0,
      size: 6,
      query: this.query,
    };
    this.catalogueApi = new CatalogueApi();
  }

  mounted(): void {
    this.searchAssets();
  }

  searchAssets(): void {
    this.loading = true;
    this.queryResults = [];
    this.catalogQuery.query = this.query;
    this.catalogueApi.find(this.query)
      .then((queryResponse: CatalogueQueryResponse) => {
        if (queryResponse.success) {
          this.queryResults = queryResponse.result.items;
        }
        this.loading = false;
      })
      .catch((error: AxiosError) => {
        console.log(error);
        this.loading = false;
      });
  }
}
</script>
<style lang="scss">
  @import "@/assets/styles/_errorpage.scss";
</style>
