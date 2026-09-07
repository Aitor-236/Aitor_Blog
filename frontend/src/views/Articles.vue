<script setup lang="ts">
import { computed, ref } from 'vue'

/** 文章数据类型 */
interface ArticleItem {
  id: number
  title: string
  summary: string
  tag: string
  date: string
  minutes: number
}

// TODO: 后端接口就绪后，将 articles 初始模拟数据替换为接口返回数据
const articles = ref<ArticleItem[]>([
  {
    id: 1,
    title: '从零开始搭建 Vue 3 个人博客',
    summary:
      '记录开发中遇到的 Element Plus 组件注册、路由出口缺失以及登录页样式问题，算是一份小小的踩坑笔记。',
    tag: '前端',
    date: '2026-09-03',
    minutes: 8
  },
  {
    id: 2,
    title: '水彩练习：雨后的绿色庭院',
    summary:
      '尝试以低饱和的浅绿色调表现雨后庭院的湿润感，分享配色过程与留白的小心得。',
    tag: '绘画',
    date: '2026-08-28',
    minutes: 6
  },
  {
    id: 3,
    title: 'Spring Boot + JWT 登录鉴权小结',
    summary: '梳理用户名或邮箱登录、密码校验、JWT 签发以及拦截器鉴权的完整流程。',
    tag: '后端',
    date: '2026-08-15',
    minutes: 10
  },
  {
    id: 4,
    title: '一文读懂 CSS 毛玻璃效果',
    summary:
      '从 backdrop-filter 到饱和度的调节，拆解浅色玻璃卡片在日常页面中的落地技巧。',
    tag: '前端',
    date: '2026-07-30',
    minutes: 7
  },
  {
    id: 5,
    title: '六月书单：慢慢读，慢慢画',
    summary: '分享最近在阅读的几本书，以及它们如何悄悄影响了我的画面与文字。',
    tag: '生活',
    date: '2026-07-08',
    minutes: 5
  },
  {
    id: 6,
    title: '数码插画里如何画好绿色植物',
    summary:
      '整理常用的绿色配色方案、笔刷选择与图层习惯，让植物看起来柔软又有生命力。',
    tag: '绘画',
    date: '2026-06-18',
    minutes: 9
  }
])

// 分类筛选：目前先在前端筛选模拟数据
const tags = ['全部', '前端', '后端', '绘画', '生活']
const activeTag = ref('全部')

const filteredArticles = computed(() => {
  if (activeTag.value === '全部') {
    return articles.value
  }
  return articles.value.filter((article) => article.tag === activeTag.value)
})

function selectTag(tag: string) {
  activeTag.value = tag
}

/** 计算每个分类下的文章数量，用于右侧分类面板展示 */
function countByTag(tag: string) {
  if (tag === '全部') {
    return articles.value.length
  }
  return articles.value.filter((article) => article.tag === tag).length
}
</script>

<template>
  <div class="articles-page">
    <div class="blob blob-1" aria-hidden="true"></div>
    <div class="blob blob-2" aria-hidden="true"></div>

    <main class="articles-container">
      <!-- 页面标题区 -->
      <header class="page-hero glass-card">
        <p class="hero-eyebrow">写点什么</p>
        <h1>文章</h1>
        <p>记录代码与生活里那些值得慢慢回味的片刻。</p>
      </header>

      <!-- 左侧内容 + 右侧分类 -->
      <div class="articles-layout">
        <!-- 文章列表：由模拟数据驱动，仅做展示，不提供添加功能 -->
        <section class="content-column">
          <div v-if="filteredArticles.length" class="article-list">
            <article
              v-for="article in filteredArticles"
              :key="article.id"
              class="article-item glass-card"
            >
              <div class="article-head">
                <span class="card-tag">{{ article.tag }}</span>
                <time :datetime="article.date">{{ article.date }}</time>
              </div>
              <h2>{{ article.title }}</h2>
              <p>{{ article.summary }}</p>
              <footer class="article-footer">
                <span>约 {{ article.minutes }} 分钟读完</span>
                <!-- TODO: 后续增加文章详情路由后，在这里放“阅读全文”入口 -->
                <span class="read-more" aria-hidden="true">阅读全文 ›</span>
              </footer>
            </article>
          </div>

          <div v-else class="empty-state glass-card">
            <span aria-hidden="true">🍃</span>
            <p>这个分类暂时还没有文章。</p>
          </div>
        </section>

        <!-- 右侧分类面板 -->
        <aside class="sidebar glass-card" aria-label="文章分类">
          <h2 class="sidebar-title">分类</h2>
          <div class="filter-list">
            <button
              v-for="tag in tags"
              :key="tag"
              type="button"
              class="filter-tag"
              :class="{ 'is-active': activeTag === tag }"
              @click="selectTag(tag)"
            >
              <span>{{ tag }}</span>
              <span class="filter-count">{{ countByTag(tag) }}</span>
            </button>
          </div>
        </aside>
      </div>
    </main>
  </div>
</template>

<style scoped>
.articles-page {
  position: relative;
  min-height: 100vh;
  padding: 44px 20px 160px;
  /* 使用 clip 裁剪装饰光斑，同时保留 position: sticky 的生效条件 */
  overflow: clip;
  background:
    radial-gradient(1100px 600px at 15% 10%, rgba(255, 255, 255, 0.7), transparent 60%),
    linear-gradient(135deg, #eaf7ec 0%, #ddf2e2 45%, #e9f6ec 100%);
}

.blob {
  position: fixed;
  border-radius: 999px;
  filter: blur(80px);
  opacity: 0.5;
  pointer-events: none;
}

.blob-1 {
  top: -140px;
  left: -100px;
  width: 380px;
  height: 380px;
  background: rgba(153, 218, 172, 0.7);
}

.blob-2 {
  right: -120px;
  bottom: 60px;
  width: 440px;
  height: 440px;
  background: rgba(196, 233, 206, 0.8);
}

.articles-container {
  position: relative;
  z-index: 1;
  width: min(1080px, 100%);
  margin: 0 auto;
}

.glass-card {
  border: 1px solid rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.38);
  box-shadow:
    0 18px 50px rgba(91, 154, 110, 0.16),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
}

.page-hero {
  padding: 44px 36px;
  border-radius: 28px;
  text-align: center;
}

.hero-eyebrow {
  margin: 0 0 10px;
  color: #4f9b69;
  font-size: 14px;
  letter-spacing: 4px;
}

.page-hero h1 {
  margin: 0 0 12px;
  color: #2f5c3d;
  font-size: clamp(28px, 5vw, 40px);
  font-weight: 700;
}

.page-hero p {
  margin: 0;
  color: rgba(60, 104, 76, 0.75);
  font-size: 15px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.article-item {
  padding: 32px 34px;
  border-radius: 24px;
  transition: transform 0.25s ease;
}

.article-item:hover {
  transform: translateY(-4px);
}

.article-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-tag {
  padding: 4px 12px;
  border-radius: 999px;
  color: #2f7d4a;
  font-size: 12px;
  background: rgba(186, 226, 197, 0.65);
}

.article-head time {
  color: rgba(60, 104, 76, 0.6);
  font-size: 13px;
}

.article-item h2 {
  margin: 16px 0 10px;
  color: #2f5c3d;
  font-size: 21px;
  line-height: 1.5;
}

.article-item p {
  margin: 0;
  color: rgba(60, 104, 76, 0.75);
  font-size: 14px;
  line-height: 1.8;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(102, 184, 127, 0.22);
  color: rgba(60, 104, 76, 0.6);
  font-size: 13px;
}

.read-more {
  color: #4f9b69;
}

.empty-state {
  margin-top: 24px;
  padding: 60px 24px;
  border-radius: 24px;
  text-align: center;
}

.empty-state span {
  display: block;
  margin-bottom: 12px;
  font-size: 40px;
}

.empty-state p {
  margin: 0;
  color: rgba(60, 104, 76, 0.72);
}

.articles-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  align-items: start;
  gap: 28px;
  margin-top: 28px;
}

.content-column {
  min-width: 0;
}

.sidebar {
  position: sticky;
  top: 28px;
  align-self: start;
  max-height: calc(100vh - 56px);
  padding: 24px 18px;
  border-radius: 24px;
}

.sidebar-title {
  margin: 0 0 16px;
  padding: 0 8px;
  color: #2f5c3d;
  font-size: 17px;
}

.filter-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-tag {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 10px 14px;
  border: none;
  border-radius: 14px;
  color: rgba(47, 92, 61, 0.75);
  font-size: 14px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.5);
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.filter-tag:hover {
  color: #2f7d4a;
  background: rgba(255, 255, 255, 0.82);
}

.filter-tag.is-active {
  color: #ffffff;
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 8px 18px rgba(63, 159, 98, 0.24);
}

.filter-count {
  min-width: 26px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  text-align: center;
  background: rgba(186, 226, 197, 0.65);
}

.filter-tag.is-active .filter-count {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.22);
}

@media (max-width: 860px) {
  .articles-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
  }

  .filter-list {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .filter-tag {
    width: auto;
    min-width: 92px;
  }
}
</style>
