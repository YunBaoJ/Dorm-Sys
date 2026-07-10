<template>
  <div class="hero">
    <div class="hero-bg">
      <div class="glass-orb orb-1"></div>
      <div class="glass-orb orb-2"></div>
    </div>
    <div class="hero-content">
      <div class="hero-main">
        <h1>{{ title }}</h1>
        <p>{{ subtitle }}</p>
      </div>
      <div v-if="actionLabel" class="hero-action">
        <button class="primary-btn" @click="$emit('action')">
          <component v-if="actionIcon" :is="actionIcon" :size="18" />
          {{ actionLabel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  subtitle: String,
  actionLabel: String,
  actionIcon: Object, // Lucide icon component
})
defineEmits(['action'])
</script>
