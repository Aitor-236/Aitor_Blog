<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DockNav from '@/components/DockNav.vue'

const route = useRoute()

// 登录页作为独立入口，不显示全局底部导航
const showDock = computed(() => route.path !== '/login')
</script>

<template>
  <router-view />
  <DockNav v-if="showDock" />
</template>

<style>
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
  color: #2f5c3d;
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
  background: #eaf7ec;
}

a {
  color: inherit;
  text-decoration: none;
}
</style>
