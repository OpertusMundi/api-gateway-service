<template>
  <div class="login">
    <div class="login__content">
      <router-link to="/" class="login__content__logo"><img src="@/assets/images/logo_blue.svg" alt=""></router-link>
      <h1>Login</h1>
      <validation-observer ref="loginForm">
      <form class="login__form" @submit.prevent="submitLogin()">
          <validation-provider name="email" mode="passive" rules="required|email" v-slot="{ errors }">
            <div class="login__form__group">
              <input type="email" name="email" id="" v-model="email" placeholder="email">
              <span class="login__form__group__error">{{ errors[0] }}</span>
            </div>
          </validation-provider>
          <validation-provider name="password"  mode="passive" rules="required" v-slot="{ errors }">
            <div class="login__form__group">
              <input type="password" name="password" v-model="password" id="" placeholder="password">
              <span class="login__form__group__error">{{ errors[0] }}</span>
            </div>
          </validation-provider>
          <div class="login__form__errors" v-if="formErrors">{{ formErrors }}</div>
          <button type="submit" class="btn btn--std btn--blue" :disabled="loading" v-html="loading ? 'PLEASE WAIT..' : 'LOGIN'"></button>
      </form>
      </validation-observer>
      <div class="login__helpers">
        <router-link to="/forgot-password">Forgot your password?</router-link><br>
        <router-link to="/login">Don’t have an account yet? Register</router-link>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { required, email } from 'vee-validate/dist/rules';
import { ValidationProvider, ValidationObserver, extend } from 'vee-validate';
import store from '@/store';
import AccountApi from '@/service/account';
import { ServerResponse, LoginResult } from '@/model';
import { Account } from '@/model/account';
import { AxiosError } from 'axios';

extend('required', required);
extend('email', email);


@Component({
  components: { ValidationProvider, ValidationObserver },
})
export default class Login extends Vue {
  $refs!: {
    loginForm: InstanceType<typeof ValidationObserver>
  }

  email:string;

  password:string;

  loading: boolean;

  accountApi: AccountApi;

  formErrors: string;

  constructor() {
    super();
    // Default credentials for MPV
    this.email = 'admin@opertus-mundi.eu';
    this.password = 'enaelafaki';
    this.loading = false;
    this.formErrors = '';

    this.accountApi = new AccountApi();
  }

  mounted(): void {
    console.log('ok');
  }

  async submitLogin():Promise<void> {
    this.loading = true;
    this.formErrors = '';
    const valid = await this.$refs.loginForm.validate();
    if (valid) {
      this.accountApi
        .login(this.email, this.password)
        .then((loginResponse: ServerResponse<LoginResult>) => {
          if (loginResponse.success) {
            // Set CSRF Token
            const { csrfToken: token, csrfHeader: header } = loginResponse.result;
            store.commit('setCsrfToken', { token, header });
            // Load user data
            this.accountApi
              .getUserData()
              .then((accountResponse: ServerResponse<Account>) => {
                if (accountResponse.success) {
                  store.commit('setUserData', accountResponse.result);
                  this.loading = false;
                  this.$router.push('/');
                } else {
                  // TODO: Handle error
                  this.loading = false;
                }
              });
          } else {
            // TODO: Hanlde error
            this.loading = false;
          }
        })
        .catch((error: AxiosError) => {
          this.loading = false;
          if (error.response) {
            if (error.response.status === 401) {
              this.formErrors = 'Wrong username or password';
            }
          }
        });
    }
  }
}
</script>
<style lang="scss">
  @import "@/assets/styles/_login.scss";
</style>
