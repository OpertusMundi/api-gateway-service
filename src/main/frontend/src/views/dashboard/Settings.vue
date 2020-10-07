<template>
    <div class="dashboard__inner">
      <div class="dashboard__head">
        <div class="dashboard__head__helpers">
          <h1>Settings</h1>
        </div>
      </div>

      <div class="settings">
        <div class="settings__grid">
          <a href="setting.html" class="settings__item">
            <h3 class="settings__item__title">Profile Information</h3>
            <div class="settings__item__details">
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Full Name: </div>
                <div class="settings__item__details__row__main">John Doe</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">username: </div>
                <div class="settings__item__details__row__main">@John.doe.athenarc</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Image:</div>
                <div class="settings__item__details__row__main"><div class="settings__item__details__row__main__logo"><img src="@/assets/images/about/partners/athina_logo.svg" alt=""></div></div>
              </div>
            </div>
          </a>
          <a href="setting.html" class="settings__item">
            <h3 class="settings__item__title">Profile Information</h3>
            <div class="settings__item__details">
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Full Name: </div>
                <div class="settings__item__details__row__main">John Doe</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">username: </div>
                <div class="settings__item__details__row__main">@John.doe.athenarc</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Image:</div>
                <div class="settings__item__details__row__main"><div class="settings__item__details__row__main__logo"><img src="@/assets/images/about/partners/athina_logo.svg" alt=""></div></div>
              </div>
            </div>
          </a>
          <a href="setting.html" class="settings__item">
            <h3 class="settings__item__title">Addresses</h3>
            <div class="settings__item__details">
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Full Name: </div>
                <div class="settings__item__details__row__main">John Doe</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">username: </div>
                <div class="settings__item__details__row__main">@John.doe.athenarc</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Address:</div>
                <div class="settings__item__details__row__main">
                  <p>17, Leocharous</p>
                  <p>Greece</p>
                  <p>Attica</p>
                </div>
              </div>
            </div>
          </a>
          <a href="setting.html" class="settings__item">
            <h3 class="settings__item__title">Addresses</h3>
            <div class="settings__item__details">
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Full Name: </div>
                <div class="settings__item__details__row__main">John Doe</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">username: </div>
                <div class="settings__item__details__row__main">@John.doe.athenarc</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Address:</div>
                <div class="settings__item__details__row__main">
                  <p>17, Leocharous</p>
                  <p>Greece</p>
                  <p>Attica</p>
                </div>
              </div>
            </div>
          </a>
          <a href="setting.html" class="settings__item">
            <h3 class="settings__item__title">Addresses</h3>
            <div class="settings__item__details">
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Full Name: </div>
                <div class="settings__item__details__row__main">John Doe</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">username: </div>
                <div class="settings__item__details__row__main">@John.doe.athenarc</div>
              </div>
              <div class="settings__item__details__row">
                <div class="settings__item__details__row__label">Address:</div>
                <div class="settings__item__details__row__main">
                  <p>17, Leocharous</p>
                  <p>Greece</p>
                  <p>Attica</p>
                </div>
              </div>
            </div>
          </a>
          <a class="settings__item settings__item--empty"></a>
          <a class="settings__item settings__item--empty"></a>
        </div>
      </div>
      <!-- <div class="dashboard__wrapper">
        <div class="dashboard__inner">
          <div class="dashboard__head">
            <div class="dashboard__head__helpers">
              <h1>Profile Information</h1>
            </div>
          </div>
          <div class="settings">
            <div class="settings__grid">
              <div class="settings__item settings__item--single">
                <div class="settings__item__details">
                  <div class="settings__item__details__row">
                    <div class="settings__item__details__row__label">Full Name: </div>
                    <div class="settings__item__details__row__main">John Doe</div>
                    <a href="#" class="settings__item__details__row__edit">CHANGE</a>
                  </div>
                  <div class="settings__item__details__row">
                    <div class="settings__item__details__row__label">username: </div>
                    <div class="settings__item__details__row__main">@John.doe.athenarc</div>
                    <a href="#" class="settings__item__details__row__edit">CHANGE</a>
                  </div>
                  <div class="settings__item__details__row">
                    <div class="settings__item__details__row__label">Image:</div>
                    <div class="settings__item__details__row__main"><div class="settings__item__details__row__main__logo"><img src="/assets/images/about/partners/athina_logo.svg" alt=""></div></div>
                    <a href="#" class="settings__item__details__row__edit">CHANGE</a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div> -->
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';

@Component
export default class DashboardHome extends Vue {
}
</script>
<style lang="scss">
  @import "@/assets/styles/_settings.scss";
</style>
