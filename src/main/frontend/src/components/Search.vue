<template>
  <div class="asset_search">
    <transition name="fade" mode="out-in">
      <div class="asset_search__backdrop" v-if="searchResultsActive" @click="hideSearchResults"></div>
    </transition>
    <div class="asset_search__upper" v-bind:class="{'open':searchResultsActive}">
      <!-- <input type="text" name="" id="" placeholder="Search Assets" class="asset_search__upper__input" @focus="showSearchResults" @blur="hideSearchResults"> -->
      <form v-on:submit.prevent="searchAssets">
        <input type="text" name="query" autocomplete="off" v-model="query" id="" placeholder="Search Assets" class="asset_search__upper__input" @focus="showSearchResults" @input="debouncedInput">
      </form>
      <div class="asset_search__upper__icon" @click.prevent="searchAssets" v-if="!searchResultsActive && !loading"><img src="@/assets/images/icons/search_black.svg" alt=""></div>
      <div class="asset_search__upper__icon" v-if="searchResultsActive && !loading" @click.prevent="clearInput"><img src="@/assets/images/icons/close_icon.svg" alt=""></div>
      <div class="asset_search__upper__icon" v-if="loading"><div class="loader_icon"></div></div>
    </div>
    <div class="asset_search__resultscont" v-if="searchResultsActive">
      <ul class="asset_search__resultscont__filters" >
        <li><a href="#" @click.prevent="showRecent = true; showPopular = false;" :class="{active: showRecent}">Recent Searches</a></li>
        <li><a href="#" @click.prevent="showRecent = false; showPopular = true;" :class="{active: showPopular}">Popular Searches</a></li>
      </ul>
      <ul class="asset_search__resultscont__results" v-if="queryHasResults">
        <li v-for="item in queryResults" :key="item.id"><router-link :to="`/catalogue/${item.id}`"><h5>{{ item.title }}</h5><span>Country: Greece, Language: {{item.language}}, Price: {{showItemPrice(item)}}</span></router-link></li>
      </ul>
      <ul class="asset_search__resultscont__results" v-if="!queryHasResults && !showRecent && !showPopular">
        <li class="no_results"><i>No results found for your query</i></li>
      </ul>
      <ul class="asset_search__resultscont__results related" v-if="showRecent">
        <li><a href="#"><h5>Road network</h5><span>Country: Greece, Language: English, Price: 800-2500€</span></a></li>
        <li><a href="#"><h5>Road network</h5><span>Country: Greece, Language: English, Price: 800-2500€</span></a></li>
        <li><a href="#"><h5>Road network</h5><span>Country: Greece, Language: English, Price: 800-2500€</span></a></li>
      </ul>
      <ul class="asset_search__resultscont__results related" v-if="showPopular">
        <li><a href="#"><h5>Road network Popular</h5><span>Country: Greece, Language: English, Price: 800-2500€</span></a></li>
        <li><a href="#"><h5>Road network</h5><span>Country: Greece, Language: English, Price: 800-2500€</span></a></li>
      </ul>
      <a href="" class="asset_search__resultscont__action">Delete search history</a>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import CatalogueApi from '@/service/catalogue';
import {
  CatalogueQueryResponse, CatalogueQuery, CatalogueItem,
} from '@/model';
import { AxiosError } from 'axios';
import { Debounce } from 'vue-debounce-decorator';

@Component
export default class Search extends Vue {
  searchResultsActive = false;

  catalogQuery: CatalogueQuery;

  query: '';

  queryHasResults = false;

  loading = false;

  showPopular = false;

  showRecent = true;

  catalogueApi: CatalogueApi;

  queryResults: CatalogueItem[];

  @Debounce(500)
  debouncedInput():void {
    this.searchAssets();
  }

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

  clearInput():void {
    this.query = '';
    this.queryResults = [];
    this.showRecent = true;
    this.showPopular = false;
  }

  showItemPrice(item:CatalogueItem): string {
    let lowestPrice:number|string = 999999;
    let highestPrice = 0;
    item.pricingModels.forEach((pricingModel) => {
      if (lowestPrice > pricingModel.totalPrice) {
        lowestPrice = pricingModel.totalPrice;
      }
      if (highestPrice < pricingModel.totalPrice) {
        highestPrice = pricingModel.totalPrice;
      }
    });
    if (lowestPrice === 0) lowestPrice = 'FREE';
    return `${lowestPrice} - ${highestPrice}€`;
  }

  showSearchResults():void {
    if (this.searchResultsActive) return;
    this.searchResultsActive = true;
    // this.slideToggle('asset_search__resultscont');
  }

  hideSearchResults():void {
    this.searchResultsActive = false;
    this.queryHasResults = false;
    // this.slideToggle('asset_search__resultscont');
  }

  // slideToggle(selectorClass:string):void {
  //   const selector = document.getElementsByClassName(selectorClass)[0] as HTMLElement;
  //   if (!selector) return;
  //   if (!selector.classList.contains('active')) {
  //     selector.classList.add('active');
  //     selector.style.height = 'auto';
  //     const height = `${selector.clientHeight}px`;
  //     selector.style.height = '0px';
  //     setTimeout(() => {
  //       selector.style.height = height;
  //     }, 0);
  //   } else {
  //     selector.style.height = '0px';
  //     selector.addEventListener('transitionend', () => {
  //       selector.classList.remove('active');
  //     }, {
  //       once: true,
  //     });
  //   }
  // }

  searchAssets(): void {
    if (this.query.length <= 2) return;
    this.loading = true;
    this.queryResults = [];
    this.catalogQuery.query = this.query;
    this.catalogueApi.find(this.query)
      .then((queryResponse: CatalogueQueryResponse) => {
        this.showRecent = false;
        this.showPopular = false;
        if (!queryResponse.success) {
          this.queryHasResults = false;
          this.showRecent = false;
          this.showPopular = false;
        } else {
          this.queryHasResults = true;
          this.queryResults = queryResponse.result.items;
        }
        if (!this.searchResultsActive) {
          setTimeout(() => {
            this.showSearchResults();
          }, 200);
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
  @import "@/assets/styles/_search.scss";
</style>
