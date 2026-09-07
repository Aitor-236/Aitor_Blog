<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'

/** 文章卡片类型（来自 GET /article/list） */
interface ArticleItem {
  id: number
  title: string
  summary: string
  categoryName: string
  publishedAt: string
  readingMinutes: number
}

/** 分类类型（来自 GET /category/list） */
interface CategoryItem {
  id: number
  name: string
  slug: string
  articleCount: number
}

function formatDate(value: string) {
  return value.slice(0, 10)
}

const articles = ref<ArticleItem[]>([])
const categories = ref<CategoryItem[]>([])
const activeTag = ref('全部')

const totalArticles = computed(() =>
  categories.value.reduce((sum, category) => sum + category.articleCount, 0)
)

const tagOptions = computed(() => [
  { name: '全部', count: totalArticles.value },
  ...categories.value.map((category) => ({
    name: category.name,
    count: category.articleCount
  }))
])

async function loadCategories() {
  const res = (await request.get('/category/list')) as { data: CategoryItem[] }
  categories.value = res.data
}

async function loadArticles() {
  const activeCategory = categories.value.find((category) => category.name === activeTag.value)
  const res = (await request.get('/article/list', {
    params: { page: 1, size: 100, category: activeCategory?.slug }
  })) as {
    data: { records: ArticleItem[] }
  }
  articles.value = res.data.records
}

function selectTag(tag: string) {
  activeTag.value = tag
  void loadArticles()
}

onMounted(async () => {
  await loadCategories()
  await loadArticles()
})
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
        <!-- 文章列表：数据来自后端文章接口 -->
        <section class="content-column">
          <div v-if="articles.length" class="article-list">
            <article
              v-for="article in articles"
              :key="article.id"
              class="article-item glass-card"
            >
              <div class="article-head">
                <span class="card-tag">{{ article.categoryName }}</span>
                <time :datetime="article.publishedAt">{{ formatDate(article.publishedAt) }}</time>
              </div>
              <h2>{{ article.title }}</h2>
              <p>{{ article.summary }}</p>
              <footer class="article-footer">
                <span>约 {{ article.readingMinutes }} 分钟读完</span>
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
              v-for="option in tagOptions"
              :key="option.name"
              type="button"
              class="filter-tag"
              :class="{ 'is-active': activeTag === option.name }"
              @click="selectTag(option.name)"
            >
              <span>{{ option.name }}</span>
              <span class="filter-count">{{ option.count }}</span>
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
