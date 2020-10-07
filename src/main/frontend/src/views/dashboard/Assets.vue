<template>
  <div class="dashboard__inner">
    <div class="dashboard__head">
      <div class="dashboard__head__helpers">
        <h1>Assets</h1>
        <router-link to="/dashboard/assets/create" class="btn--std btn--blue">ADD AN ASSET</router-link>
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
