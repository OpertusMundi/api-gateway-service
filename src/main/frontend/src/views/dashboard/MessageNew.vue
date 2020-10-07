<template>
  <div class="dashboard__inner">
    <div class="dashboard__head">
      <div class="dashboard__head__left">
        <h1>New Message</h1>
      </div>
    </div>
    <div class="dashboard__form">
        <div class="single-message">
          <div class="form-group">
            <input type="email" class="form-group__text" id="email" placeholder="Enter email">
            <div class="errors">Please check you input!</div>
          </div>
          <div class="form-group">
            <input type="email" class="form-group__text" id="email" placeholder="Enter email">
            <div class="errors">Please check you input!</div>
          </div>
          <div class="form-group">
            <select name="issue" id="issue">
              <option value="1">Option 1</option>
              <option value="2">Option 2</option>
              <option value="3">Option 3</option>
              <option value="4">Option 4</option>
            </select>
            <div class="errors">Please check you input!</div>
          </div>
          <div class="form-group">
            <div class="form-group__stickyright">
              <div class="asset-owner__inner__logo">
                <img src="@/assets/images/about/partners/athina_logo.svg" alt="">
              </div>
              <p class="form-group__stickyright__text">AthenaRC</p>
            </div>
            <textarea name="" id="" cols="30" rows="10" placeholder="Your message"></textarea>
            <div class="errors">Please check you input!</div>
          </div>
          <div class="form-group form-group--right">
            <button type="submit" class="btn--std btn--dark">SEND</button>
          </div>
        </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';

@Component
export default class DashboardHome extends Vue {
}
</script>
<style lang="scss">
  @import "@/assets/styles/_messages.scss";
  @import "@/assets/styles/_forms.scss";
  @import "@/assets/styles/_assets.scss";
</style>
