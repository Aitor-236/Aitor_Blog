<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

// 底部全局导航配置：每个路由的长度一致
const navItems = [
  { to: '/', label: '主页', icon: '🏠' },
  { to: '/articles', label: '文章', icon: '📝' },
  { to: '/gallery', label: '画', icon: '🎨' },
  { to: '/about', label: '个人简介', icon: '👤' }
]

function isActive(path: string) {
  return route.path === path
}
</script>

<template>
  <nav class="dock" aria-label="全局导航">
    <router-link
      v-for="item in navItems"
      :key="item.to"
      :to="item.to"
      class="dock-item"
      :class="{ 'is-active': isActive(item.to) }"
    >
      <span class="dock-icon" aria-hidden="true">{{ item.icon }}</span>
      <span class="dock-label">{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<style scoped>
.dock {
  position: fixed;
  bottom: 26px;
  left: 50%;
  z-index: 100;
  display: flex;
  gap: 8px;
  padding: 10px;
  transform: translateX(-50%);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.42);
  box-shadow:
    0 18px 45px rgba(91, 154, 110, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
}

.dock-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 86px;
  height: 70px;
  gap: 5px;
  border-radius: 16px;
  color: rgba(47, 92, 61, 0.72);
  transition:
    color 0.25s ease,
    background-color 0.25s ease,
    transform 0.25s ease;
}

.dock-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  font-size: 20px;
  line-height: 1;
  background: rgba(255, 255, 255, 0.55);
  box-shadow: 0 6px 16px rgba(91, 154, 110, 0.14);
  transition: transform 0.25s ease;
}

.dock-label {
  font-size: 12px;
  white-space: nowrap;
}

.dock-item:hover {
  color: #2f7d4a;
  background: rgba(255, 255, 255, 0.55);
}

.dock-item:hover .dock-icon {
  transform: translateY(-6px) scale(1.08);
}

.dock-item.is-active {
  color: #1f6b38;
  background: rgba(255, 255, 255, 0.68);
  box-shadow: 0 8px 22px rgba(91, 154, 110, 0.18);
}

.dock-item.is-active .dock-icon {
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 8px 18px rgba(63, 159, 98, 0.28);
}
</style>
