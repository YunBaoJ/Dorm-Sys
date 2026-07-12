// Workaround for Edge browser minimize bug with vue-router
const originalReplaceState = window.history.replaceState;
window.history.replaceState = function(state, title, url) {
  if (document.visibilityState === 'hidden') {
    return originalReplaceState.call(this, null, '', '');
  }
  return originalReplaceState.apply(this, arguments);
};

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './assets/css/style.css'
import './assets/css/theme.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
