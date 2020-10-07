import { Configuration } from '@/model';

interface State {
  configuration: Configuration | null;
  loading: boolean;
}

const initialState: State = {
  configuration: null,
  loading: true,
};

const getters = {
  getConfig: (state: State): Configuration | null => state.configuration,
  isLoading: (state: State): boolean => state.loading,
};

const actions = {

};

const mutations = {
  setConfiguration(state: State, configuration: Configuration): void {
    state.configuration = configuration;
  },
  setLoading(state: State, loading: boolean): void {
    state.loading = loading;
  },
};

export default {
  namespaced: false,
  state: initialState,
  getters,
  actions,
  mutations,
};
