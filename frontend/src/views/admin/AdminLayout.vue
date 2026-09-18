<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import '@/styles/admin.css'

/** 左侧导航的一项，to 为空或 disabled 表示功能还没做，只占位不可点。 */
interface NavItem {
  label: string
  icon: string
  to?: string
  disabled?: boolean
}

interface NavGroup {
  title: string
  items: NavItem[]
}

/**
 * 后台导航配置：后续新增后台功能时，往 groups 里加一项即可，
 * 布局和样式不用改。
 */
const navGroups: NavGroup[] = [
  {
    title: '内容管理',
    items: [
      { label: '文章列表', icon: '📝', to: '/admin/articles' },
      { label: '新建文章', icon: '✍️', to: '/admin/articles/new' }
    ]
  },
  {
    title: '预留功能',
    items: [
      { label: '分类管理', icon: '🏷️', disabled: true },
      { label: '评论管理', icon: '💬', disabled: true },
      { label: '用户管理', icon: '👥', disabled: true }
    ]
  }
]

const route = useRoute()
const router = useRouter()

const username = computed(() => localStorage.getItem('username') || '管理员')
const avatarText = computed(() => username.value.trim().charAt(0).toUpperCase() || 'A')

function isActive(item: NavItem) {
  if (!item.to) return false
  // 新建页要和文章列表区分开，编辑页则归到文章列表。
  if (item.to === '/admin/articles/new') {
    return route.path === item.to
  }
  if (item.to === '/admin/articles') {
    return route.path === item.to || route.path.startsWith('/admin/articles/')
  }
  return route.path === item.to
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('退出后需要重新登录才能进入后台，确定退出吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    // 用户点了取消
    return
  }

  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('email')
  ElMessage.success('已退出登录')
  void router.push('/login')
}
</script>

<template>
  <div class="admin-shell">
    <div class="blob blob-1" aria-hidden="true"></div>
    <div class="blob blob-2" aria-hidden="true"></div>

    <aside class="admin-sidebar">
      <div class="brand">
        <div class="brand-logo">A</div>
        <div class="brand-text">
          <strong>Aitor Blog</strong>
          <span>管理后台</span>
        </div>
      </div>

      <nav class="admin-nav" aria-label="后台导航">
        <div v-for="group in navGroups" :key="group.title" class="nav-group">
          <p class="nav-group-title">{{ group.title }}</p>

          <template v-for="item in group.items" :key="item.label">
            <router-link
              v-if="item.to && !item.disabled"
              :to="item.to"
              class="nav-item"
              :class="{ 'is-active': isActive(item) }"
            >
              <span class="nav-icon" aria-hidden="true">{{ item.icon }}</span>
              <span class="nav-label">{{ item.label }}</span>
            </router-link>

            <span v-else class="nav-item is-disabled" aria-disabled="true">
              <span class="nav-icon" aria-hidden="true">{{ item.icon }}</span>
              <span class="nav-label">{{ item.label }}</span>
              <span class="nav-badge">开发中</span>
            </span>
          </template>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="account">
          <span class="account-avatar">{{ avatarText }}</span>
          <span class="account-name">{{ username }}</span>
        </div>

        <div class="footer-actions">
          <router-link to="/" class="footer-link">返回前台</router-link>
          <button type="button" class="footer-link is-danger" @click="handleLogout">
            退出登录
          </button>
        </div>
      </div>
    </aside>

    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-shell {
  --el-color-primary: #66b87f;
  --el-color-primary-light-3: #8bcd9f;
  --el-color-primary-light-5: #aeddbd;
  --el-color-primary-light-7: #d0ecd8;
  --el-color-primary-light-8: #e2f4e7;
  --el-color-primary-light-9: #f1faf4;
  --el-color-primary-dark-2: #519d68;

  position: relative;
  display: flex;
  gap: 20px;
  min-height: 100vh;
  padding: 20px;
  overflow-x: hidden;
  background:
    radial-gradient(1100px 600px at 12% 8%, rgba(255, 255, 255, 0.7), transparent 60%),
    linear-gradient(135deg, #eaf7ec 0%, #ddf2e2 45%, #e9f6ec 100%);
}

.blob {
  position: absolute;
  z-index: 0;
  border-radius: 999px;
  filter: blur(80px);
  opacity: 0.5;
  pointer-events: none;
}

.blob-1 {
  top: -160px;
  left: -120px;
  width: 380px;
  height: 380px;
  background: rgba(153, 218, 172, 0.75);
}

.blob-2 {
  right: -140px;
  bottom: -180px;
  width: 440px;
  height: 440px;
  background: rgba(196, 233, 206, 0.8);
}

.admin-sidebar {
  position: sticky;
  top: 20px;
  z-index: 1;
  display: flex;
  flex: 0 0 236px;
  flex-direction: column;
  align-self: flex-start;
  height: calc(100vh - 40px);
  padding: 22px 16px 18px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.4);
  box-shadow:
    0 18px 50px rgba(91, 154, 110, 0.16),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 6px 20px;
  border-bottom: 1px solid rgba(102, 184, 127, 0.18);
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  color: #ffffff;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 10px 22px rgba(63, 159, 98, 0.3);
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.35;
}

.brand-text strong {
  color: #2f5c3d;
  font-size: 15px;
  font-weight: 600;
}

.brand-text span {
  color: rgba(60, 104, 76, 0.62);
  font-size: 12px;
}

.admin-nav {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 22px;
  padding: 20px 0;
  overflow-y: auto;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-group-title {
  margin: 0;
  padding: 0 10px;
  color: rgba(60, 104, 76, 0.55);
  font-size: 12px;
  letter-spacing: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border-radius: 14px;
  color: rgba(47, 92, 61, 0.78);
  font-size: 14px;
  background: rgba(255, 255, 255, 0.42);
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.nav-item:hover {
  color: #2f7d4a;
  background: rgba(255, 255, 255, 0.82);
}

.nav-item.is-active {
  color: #ffffff;
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 10px 22px rgba(63, 159, 98, 0.26);
}

.nav-item.is-disabled {
  color: rgba(60, 104, 76, 0.42);
  background: rgba(255, 255, 255, 0.24);
  cursor: not-allowed;
}

.nav-icon {
  font-size: 15px;
}

.nav-label {
  flex: 1;
}

.nav-badge {
  padding: 1px 8px;
  border-radius: 999px;
  color: rgba(60, 104, 76, 0.6);
  font-size: 11px;
  background: rgba(186, 226, 197, 0.5);
}

.sidebar-footer {
  padding-top: 16px;
  border-top: 1px solid rgba(102, 184, 127, 0.18);
}

.account {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 6px 12px;
}

.account-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 10px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #8bcd9f, #59ab74);
}

.account-name {
  overflow: hidden;
  color: #2f5c3d;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.footer-link {
  flex: 1;
  padding: 8px 0;
  border: none;
  border-radius: 12px;
  color: rgba(47, 92, 61, 0.78);
  font-size: 12px;
  text-align: center;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.55);
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.footer-link:hover {
  color: #2f7d4a;
  background: rgba(255, 255, 255, 0.9);
}

.footer-link.is-danger:hover {
  color: #c45656;
}

.admin-main {
  position: relative;
  z-index: 1;
  flex: 1;
  min-width: 0;
  padding-bottom: 12px;
}

@media (max-width: 900px) {
  .admin-shell {
    flex-direction: column;
    padding: 14px;
  }

  .admin-sidebar {
    position: static;
    flex: none;
    width: 100%;
    height: auto;
  }

  .admin-nav {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 14px;
    padding: 16px 0;
  }

  .nav-group {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
  }

  .nav-group-title {
    width: 100%;
  }
}
</style>
