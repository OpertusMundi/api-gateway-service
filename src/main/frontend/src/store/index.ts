import Vue from 'vue';
import Vuex from 'vuex';

import User from './modules/user.module';
import Config from './modules/config.module';
import Shop from './modules/shop.module';

Vue.use(Vuex);

const debug = process.env.NODE_ENV !== 'production';

const store = new Vuex.Store({
  modules: {
    user: User,
    config: Config,
    shop: Shop,
  },
  strict: debug,
});

export default store;
