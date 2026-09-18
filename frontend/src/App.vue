<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DockNav from '@/components/DockNav.vue'

const route = useRoute()

// 登录页和后台管理都有独立布局，不显示前台底部导航
const showDock = computed(() => route.path !== '/login' && !route.path.startsWith('/admin'))
</script>

<template>
  <router-view />
  <DockNav v-if="showDock" />
</template>

<style>
:root {
  /* 浅色米白 + 棕色：整体主题变量，前台页面统一复用 */
  --bg-cream: #f6f1e7;
  --accent-brown: #8a5a3b;
  --accent-brown-soft: #b98a5e;
  --text-strong: #3f2e22;
  --text-body: rgba(74, 54, 41, 0.78);
  --text-muted: rgba(74, 54, 41, 0.58);
  --panel-bg: #fffdf9;
  --panel-alt-bg: #f8f2e7;
  --panel-border: rgba(138, 90, 59, 0.14);
  --panel-shadow: 0 18px 44px rgba(120, 88, 58, 0.12);
}

* {
  box-sizing: border-box;
}

html,
body,
#app {
  min-height: 100%;
}

body {
  margin: 0;
  color: var(--text-strong);
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    Roboto,
    'PingFang SC',
    'Hiragino Sans GB',
    'Microsoft YaHei',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  background: var(--bg-cream);
}

a {
  color: inherit;
  text-decoration: none;
}
</style>
