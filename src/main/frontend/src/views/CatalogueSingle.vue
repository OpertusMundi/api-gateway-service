<template>
  <div class="asset" v-show='itemLoaded'>
    <div class="asset__map">
      <div class="asset__map__inner">
        <l-map
          v-if="map.show"
          :zoom="map.zoom"
          :center="map.center"
          :options="map.mapOptions"
        >
        <l-tile-layer
          :url="map.url"
          :attribution="map.attribution"/>
        <l-polygon v-if="map.coordinates_type === 'Polygon'"
          :latLngs="this.catalogueItem.geometry.coordinates[0]"
          color="#190AFF"
        />
        </l-map>
      </div>
    </div>
    <div class="s_container">
      <div class="asset__inner">
        <div class="asset__content">
          <div class="asset__head">
            <a href="#" class="asset__head__breadcrumps"><img src="@/assets/images/icons/back_icon.svg" alt="">BACK</a>
            <topic-category-icon :category="catalogueItem.topicCategory"/>
            <div class="asset__head__title">
              <h1>{{ catalogueItem.title }}</h1>
              <a href="#" class="asset__head__favorites">
                <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 33 31" style="enable-background:new 0 0 33 31;" xml:space="preserve">
                  <path style="fill:#FFFFFF;"
                        d="M16.5,30.7l-3.2-2.9C5.6,20.8,0.4,16.1,0.4,9.9c0-5.2,4.1-9.3,9.3-9.3c2.5,0,4.9,1,6.8,2.7c1.8-1.7,4.2-2.7,6.8-2.7c5.2,0,9.3,4.1,9.3,9.3c0,6.1-5.1,10.8-12.9,18L16.5,30.7z M9.7,2.6c-4.1,0-7.3,3.2-7.3,7.3c0,5.2,4.9,9.7,12.2,16.5l1.8,1.6l1.8-1.6c7.4-6.8,12.2-11.2,12.2-16.5c0-4.1-3.2-7.3-7.3-7.3c-2.3,0-4.5,1.1-6,2.8l-0.8,0.9l-0.8-0.9C14.2,3.7,12,2.6,9.7,2.6z"/>
                  <path style="fill:#FFFFFF;" d="M16.5,29.4l-2.2-2C6.6,20.3,1.4,15.7,1.4,9.9c0-4.7,3.6-8.3,8.3-8.3c2.6,0,5.1,1.2,6.8,3.2c1.7-2,4.1-3.2,6.8-3.2c4.7,0,8.3,3.6,8.3,8.3c0,5.8-5.1,10.4-12.9,17.5L16.5,29.4z"/>
                </svg>
              </a>
            </div>
            <div class="asset__head__version">
              <div class="custom-select">
                <select name="version">
                  <option value="" default selected>VERSION 1.3</option>
                  <option value="">VERSION 1.2</option>
                  <option value="">VERSION 1.1</option>
                </select>
              </div>
              <div class="asset__head__rating rating"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
            </div>
            <div class="asset__head__data">
              <ul>
                <li><strong>Last updated:</strong>{{ formatDate(catalogueItem.revisionDate) }}</li>
                <li><strong>Created:</strong>{{ formatDate(catalogueItem.publicationDate) }}</li>
                <li><strong>Topic:</strong>{{ catalogueItem.topicCategory }}</li>
                <li><strong>Format:</strong>{{ catalogueItem.format }}</li>
                <li><strong>CRS:</strong>{{ catalogueItem.referenceSystem }}</li>
                <li><strong>Scale:</strong> 1:5,000 urban areas, 1:10,000 provincial areas</li>
                <li><strong>Coverage:</strong>97% of Greece</li>
              </ul>
            </div>
          </div>
          <div class="asset__sections">
            <section class="asset__section asset__section__overview">
              <div class="asset__section__head">
                <h4>Overview</h4>
                <a href="#" class="asset__section__head__toggle"><img src="@/assets/images/icons/arrow_down.svg" alt=""></a>
              </div>
              <div class="asset__section__content">
                <div class="asset__section__content__inner">
                  <div class="asset__section__overview__left">
                    <h5>Description</h5>
                    <p>Study and analyze the geospatial data value chain in <a href="#">EU to suitably</a> align and position the platform with current and emerging needs of owners and consumers from a technical, organizational, legal, and business perspective.</p>
                    <h5>Suitable for:</h5>
                    <ul>
                      <li>Thematic mapping</li>
                      <li>Land management and property development</li>
                      <li>Environmental monitoring</li>
                    </ul>
                    <h5>Additional resources</h5>
                    <a href="#">additional-1.pdf</a> <br>
                  </div>
                  <div class="asset__section__overview__right">
                    <h5>Asset Info</h5>
                    <p><strong>Language:</strong> English <br>
                      <strong>Temporal extent:</strong>  Nov. 2019 - Oct. 2020 <br>
                      <strong>Compatibility:</strong> example</p>
                    <h5>Tags</h5>
                    <p>vector, greece, agriculture,
                      commercial, map, analytics</p>
                  </div>
                </div>
              </div>
              <a href="#" class="asset__section__toggle"><img src="@/assets/images/icons/arrow_down.svg" alt=""></a>
            </section>
            <section class="asset__section">
              <div class="asset__section__head">
                <h4>Terms & Restrictions</h4>
                <a href="#" class="asset__section__head__toggle"><img src="@/assets/images/icons/arrow_down.svg" alt=""></a>
                <ul class="asset__section__head__tabs">
                  <li><a href="#" class="active">Type of licence</a></li>
                  <li><a href="#">Countries</a></li>
                  <li><a href="#">Duration</a></li>
                  <li><a href="#">Vocabulary</a></li>
                </ul>
              </div>
              <div class="asset__section__content">
                <div class="asset__section__content__inner">
                  <ul class="asset__section__tabs">
                    <li>
                      <p>Study and analyze the geospatial data value chain in EU to suitably align and position the platform with current and emerging needs of owners and consumers from a technical, organizational, legal, and business perspective.
                        Study and analyze the geospatial data value chain in EU to suitably align and position the platform with current and emerging needs of owners and consumers from a technical, organizational, legal, and business perspective.
                        Study and analyze the geospatial data value chain in EU to suitably align and position the platform.</p>
                    </li>
                    <li>
                      <p>Countries content</p>
                    </li>
                    <li>
                      <p>Duration content</p>
                    </li>
                    <li>
                      <p>Vocabulary content</p>
                    </li>
                  </ul>
                </div>
              </div>
              <a href="#" class="asset__section__toggle"><img src="@/assets/images/icons/arrow_down.svg" alt=""></a>
            </section>
          </div>
        </div>
        <div class="asset__sidebar">
          <div class="asset__shopcard">
            <div class="asset__shopcard__variations">
              <div class="asset__shopcard__variations__row" v-for="pr_model in catalogueItem.pricingModels" :key="pr_model.id">
                <input type="radio" name="variations" :id="`p_variation_${pr_model.id}`" v-model="selectedPricingModel" :value="pr_model.id">
                <label :for="`p_variation_${pr_model.id}`">{{ pr_model.totalPrice === 0 ? 'FREE' : pr_model.totalPrice + prModelCurrencyFormat(pr_model.currency) }} <span v-if="pr_model.includesUpdates">({{ pr_model.yearsOfUpdates }} year{{pr_model.yearsOfUpdates > 1 ? 's' : ''}} of updates)</span><span v-else>(no updates)</span></label>
              </div>
            </div>
            <ul class="asset__shopcard__priceoptions">
              <li>+ 24% VAT</li>
              <li>+ 5,99€ delivery to Poland</li>
            </ul>
            <div class="asset__shopcard__addtocart"><a href="#" @click.prevent="addToCart" class="btn btn--std btn--blue">ADD TO CART</a></div>
            <transition name="fade" mode="out-in"><div class="asset__shopcard__errors" v-if="cartErrors">{{ cartErrors }}</div></transition>
            <ul class="asset__shopcard__buyinfo">
              <li><strong>Expected delivery:</strong>Available immediately</li>
              <li><strong>Payment:</strong> Secure, escrow account</li>
              <li><strong>Payment methods:</strong> </li>
            </ul>
            <div class="asset__shopcard__pid">PID: 300.994.567 </div>
            <div class="asset-owner">
              <div class="asset-owner__inner">
                <div class="asset-owner__inner__logo"><img src="@/assets/images/about/partners/athina_logo.svg" alt=""></div>
                <div class="asset-owner__inner__info">
                  <div class="asset-owner__inner__info__name">
                    <a href="#">AthenaRC</a>
                    <a href="#">
                      <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 33 31" style="enable-background:new 0 0 33 31;" xml:space="preserve">
                        <path style="fill:#FFFFFF;"
                        d="M16.5,30.7l-3.2-2.9C5.6,20.8,0.4,16.1,0.4,9.9c0-5.2,4.1-9.3,9.3-9.3c2.5,0,4.9,1,6.8,2.7c1.8-1.7,4.2-2.7,6.8-2.7c5.2,0,9.3,4.1,9.3,9.3c0,6.1-5.1,10.8-12.9,18L16.5,30.7z M9.7,2.6c-4.1,0-7.3,3.2-7.3,7.3c0,5.2,4.9,9.7,12.2,16.5l1.8,1.6l1.8-1.6c7.4-6.8,12.2-11.2,12.2-16.5c0-4.1-3.2-7.3-7.3-7.3c-2.3,0-4.5,1.1-6,2.8l-0.8,0.9l-0.8-0.9C14.2,3.7,12,2.6,9.7,2.6z"/>
                        <path style="fill:#FFFFFF;" d="M16.5,29.4l-2.2-2C6.6,20.3,1.4,15.7,1.4,9.9c0-4.7,3.6-8.3,8.3-8.3c2.6,0,5.1,1.2,6.8,3.2c1.7-2,4.1-3.2,6.8-3.2c4.7,0,8.3,3.6,8.3,8.3c0,5.8-5.1,10.4-12.9,17.5L16.5,29.4z"/>
                      </svg>
                    </a>
                  </div>
                  <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                  <div class="asset-owner__inner__info__country">Athens, Greece</div>
                  <div class="asset-owner__inner__info__date">Joined February 2020</div>
                </div>
              </div>
            </div>
          </div>
          <div class="asset__otheroptions">
            <h5>Asset also available as:</h5>
            <a href="#" class="asset_card asset_card--sm">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece</div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                  </div>
                </div>
              </div>
            </a>
            <a href="#" class="asset_card asset_card--sm">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece lsadfasdjhf lasdkjfh </div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                  </div>
                </div>
              </div>
            </a>
            <a href="#" class="asset_card asset_card--sm">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece lsadfasdjhf lasdkjfh </div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span class="active">★</span><span class="active">★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                  </div>
                </div>
              </div>
            </a>
          </div>
        </div>
      </div>


      <!-- <a href="#" class="asset_card">
        <div class="asset_card__view"><span>VIEW</span></div>
          <div class="asset_card__inner">
          <div class="asset_card__top">
            <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span><span>Environment, Natural resources</span></div>
            <div class="asset_card__top__right"><span>@provider</span></div>
          </div>
          <div class="asset_card__center">
            <div class="asset_card__title">Lakes of Greece</div>
            <div class="asset_card__price">300€</div>
          </div>
          <div class="asset_card__bottom">
            <div class="asset_card__bottom__left">
              <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
              <div class="asset_card__bottom__left__info">
                <span><strong>Version: </strong>1.2</span><span><strong>Last updated: </strong>20 Nov. 2020 </span>
              </div>
            </div>
            <div class="asset_card__bottom__right">
              <span>26</span><img src="@/assets/images/icons/bag-icon.svg" alt="">
            </div>
          </div>
        </div>
      </a> -->

    </div>
    <div class="asset__related">
      <div class="s_container">
        <div class="asset__related__content">
          <div class="asset__related__content__left">
            <h5>Related Assets</h5>
            <a href="#" class="asset_card">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span><span>Environment, Natural resources</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece</div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                    <div class="asset_card__bottom__left__info">
                      <span><strong>Version: </strong>1.2</span><span><strong>Last updated: </strong>20 Nov. 2020 </span>
                    </div>
                  </div>
                  <div class="asset_card__bottom__right">
                    <span>26</span><img src="@/assets/images/icons/bag-icon.svg" alt="">
                  </div>
                </div>
              </div>
            </a>
            <a href="#" class="asset_card">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span><span>Environment, Natural resources</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece</div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                    <div class="asset_card__bottom__left__info">
                      <span><strong>Version: </strong>1.2</span><span><strong>Last updated: </strong>20 Nov. 2020 </span>
                    </div>
                  </div>
                  <div class="asset_card__bottom__right">
                    <span>26</span><img src="@/assets/images/icons/bag-icon.svg" alt="">
                  </div>
                </div>
              </div>
            </a>
            <a href="#" class="asset_card">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span><span>Environment, Natural resources</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece</div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                    <div class="asset_card__bottom__left__info">
                      <span><strong>Version: </strong>1.2</span><span><strong>Last updated: </strong>20 Nov. 2020 </span>
                    </div>
                  </div>
                  <div class="asset_card__bottom__right">
                    <span>26</span><img src="@/assets/images/icons/bag-icon.svg" alt="">
                  </div>
                </div>
              </div>
            </a>

          </div>
          <div class="asset__related__content__right">
            <h6>Asset also available as:</h6>
            <a href="#" class="asset_card asset_card--sm">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece</div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                  </div>
                </div>
              </div>
            </a>
            <a href="#" class="asset_card asset_card--sm">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece</div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                  </div>
                </div>
              </div>
            </a>
            <a href="#" class="asset_card asset_card--sm">
              <div class="asset_card__view"><span>VIEW</span></div>
                <div class="asset_card__inner">
                <div class="asset_card__top">
                  <div class="asset_card__top__left"><img src="@/assets/images/icons/vector_icon.svg" alt=""><span>Vector Dataset</span></div>
                  <div class="asset_card__top__right"><span>@provider</span></div>
                </div>
                <div class="asset_card__center">
                  <div class="asset_card__title">Lakes of Greece</div>
                  <div class="asset_card__price">300€</div>
                </div>
                <div class="asset_card__bottom">
                  <div class="asset_card__bottom__left">
                    <div class="rating rating--dark"><span class="active">★</span><span>★</span><span>★</span><span>★</span><span>★</span><i>4.8/5</i></div>
                  </div>
                </div>
              </div>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import {
  CatalogueItem, ServerResponse, CartAddItemCommand, Cart,
} from '@/model';
import store from '@/store';
import moment from 'moment';
import CatalogueApi from '@/service/catalogue';
import CartApi from '@/service/cart';

import TopicCategoryIcon from '@/components/Catalogue/TopicCategoryIcon.vue';

import L, { latLng } from 'leaflet';
import { LMap, LTileLayer, LPolygon } from 'vue2-leaflet';
import 'leaflet/dist/leaflet.css';

@Component({
  components: {
    TopicCategoryIcon,
    LMap,
    LTileLayer,
    LPolygon,
  },
})
export default class CatalogueSingle extends Vue {
  catalogueApi: CatalogueApi;

  catalogueItem: CatalogueItem;

  itemLoaded = false;

  selectedPricingModel: string;

  map:any;

  cartErrors: string;

  cartApi: CartApi;


  constructor() {
    super();

    this.catalogueItem = {} as CatalogueItem;
    this.catalogueApi = new CatalogueApi();
    this.selectedPricingModel = '';
    this.cartErrors = '';
    this.cartApi = new CartApi();
    this.map = {
      show: false,
      zoom: 7,
      center: latLng(37.9782553, 23.7263485),
      mapOptions: {
        zoomSnap: 0.5,
        zoomControl: false,
        dragging: false,
        scrollWheelZoom: false,
        doubleClickZoom: false,
      },
      coordinates_type: '',
      url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
    };
  }

  mounted():void {
    this.calcAssetHeaderTitle();
    window.addEventListener('resize', () => {
      this.calcAssetHeaderTitle();
    });
    this.loadAsset();
  }

  @Watch('selectedPricingModel')
  selectedPricingModelChanged(newVal: string):void {
    if (newVal !== '') {
      this.cartErrors = '';
    }
  }

  beforeDestroy():void {
    window.removeEventListener('resize', this.calcAssetHeaderTitle);
  }

  formatDate(date:string): string {
    return moment(date).format('DD MMM. YYYY');
  }

  prModelCurrencyFormat(currency:string): string {
    switch (currency) {
      case 'EUR':
        return '€';
      case 'USD':
        return '$';

      default:
        return '';
    }
  }

  loadAsset(): void {
    const catalogueItemID = this.$route.params.id;
    this.catalogueApi.findOne(catalogueItemID)
      .then((queryResponse: ServerResponse<CatalogueItem>) => {
        if (queryResponse.success === false && queryResponse.messages[0].code === 'BasicMessageCode.NotFound') {
          this.$router.push('/errors/404');
        }
        this.itemLoaded = true;
        this.catalogueItem = queryResponse.result;
        setTimeout(() => {
          this.calcAssetHeaderTitle();
          this.initMap();
        }, 200);
      });
  }

  initMap():void {
    this.map.show = true;
    this.map.coordinates_type = this.catalogueItem.geometry.type;
    if (this.catalogueItem.geometry) {
      this.polygonCenter();
    }
  }

  polygonCenter():void {
    if (!this.catalogueItem.geometry.coordinates) return;
    const coordinates:any = this.catalogueItem.geometry.coordinates[0];
    const polygon = L.polygon(coordinates);
    this.map.center = polygon.getBounds().getCenter();
  }

  addToCart():void {
    if (this.selectedPricingModel === '') {
      this.cartErrors = 'Please select a pricing model!';
      return;
    }
    this.cartErrors = '';
    // TODO: add to cart functions
    const cartItem:CartAddItemCommand = {
      productId: this.catalogueItem.id,
      pricingModelId: this.selectedPricingModel,
    };
    this.cartApi.addItem(cartItem)
      .then((cartResponse: ServerResponse<Cart>) => {
        if (cartResponse.success) {
          store.commit('setCartItems', cartResponse.result);
        } else {
          // TODO: Handle error
          console.error('cannot add item to cart!');
        }
      });
  }

  calcAssetHeaderTitle():void {
    const assetHead:HTMLElement|null = document.querySelector('.asset__head');
    let assetHeadHeight = 0;
    if (assetHead) {
      assetHeadHeight = assetHead.offsetHeight;
    }
    document.documentElement.style.setProperty('--asset-head-height', `${assetHeadHeight}px`);
  }
}
</script>
<style lang="scss">
  @import "@/assets/styles/_assets.scss";
</style>
