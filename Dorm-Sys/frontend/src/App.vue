<template>
  <el-container class="app-layout" v-if="!isLoginRoute">
    <Sidebar />
    <el-container direction="vertical">
      <Topbar />
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
  
  <div v-else class="login-layout">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Topbar from './components/layout/Topbar.vue'
import Sidebar from './components/layout/Sidebar.vue'

const route = useRoute()
const isLoginRoute = computed(() => route.path === '/login')
</script>

<style scoped>
.app-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
.app-main {
  background-color: #f4f7f9;
  padding: 24px;
  overflow-y: auto;
}
.login-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
</style>
