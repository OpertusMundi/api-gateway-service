import { Cart } from '@/model';

interface State {
  cart: Cart | null;
}

const initialState: State = {
  cart: null,
};

const getters = {
  getCart: (state: State): Cart | null => state.cart,
};

const actions = {

};

const mutations = {
  setCartItems(state: State, cart: Cart): void {
    state.cart = cart;
  },
};

export default {
  namespaced: false,
  state: initialState,
  getters,
  actions,
  mutations,
};
