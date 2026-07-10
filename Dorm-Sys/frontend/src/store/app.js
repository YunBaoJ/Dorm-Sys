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
  if (newTheme === 'dark') {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
}, { immediate: true });
