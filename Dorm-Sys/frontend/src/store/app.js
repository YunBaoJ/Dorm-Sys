import { reactive, watch } from 'vue'

const storedTheme = localStorage.getItem("dorm-theme") || "light";

export const appStore = reactive({
  role: "student",
  theme: storedTheme,
  sidebarCollapsed: false,
});

watch(() => appStore.theme, (newTheme) => {
  localStorage.setItem("dorm-theme", newTheme);
  document.documentElement.dataset.theme = newTheme;
}, { immediate: true });
