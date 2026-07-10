<template>
  <div class="service-card" @click="$emit('action')">
    <div class="service-icon">
      <component :is="icon" :size="24" />
    </div>
    <div class="service-info">
      <h3>{{ title }}</h3>
      <div class="service-value">{{ value }}</div>
    </div>
    <div class="service-action">
      <span>{{ actionLabel }}</span>
      <ChevronRight :size="16" />
    </div>
  </div>
</template>

<script setup>
import { ChevronRight } from '@lucide/vue'

defineProps({
  title: String,
  value: String,
  actionLabel: String,
  icon: Object, // Lucide icon component
})
defineEmits(['action'])
</script>
