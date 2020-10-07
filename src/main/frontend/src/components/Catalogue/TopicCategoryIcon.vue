<template>
  <div class="asset__head__type">
    <img src="@/assets/images/icons/vector_icon.svg" alt="">
    {{category}}
  </div>
</template>
<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';

@Component
export default class TopicCategoryIcon extends Vue {
  @Prop({ required: true }) readonly category!: string;
}
</script>
