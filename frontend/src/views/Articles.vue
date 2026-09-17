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

/** MyBatis-Plus 分页返回结构 */
interface ArticlePage {
  records: ArticleItem[]
  total: number
  current: number
  size: number
  pages: number
}

const PAGE_SIZE = 6

function formatDate(value?: string | null) {
  return value ? value.slice(0, 10) : ''
}

const articles = ref<ArticleItem[]>([])
const categories = ref<CategoryItem[]>([])
const activeTag = ref('全部')
const keywordInput = ref('')
const appliedKeyword = ref('')
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')

// 当前选中的分类 slug，接口按 slug 筛选
const activeCategorySlug = computed(
  () => categories.value.find((category) => category.name === activeTag.value)?.slug ?? ''
)

const totalArticles = computed(() =>
  categories.value.reduce((sum, category) => sum + category.articleCount, 0)
)

// 分类接口不可用时退回到文章接口返回的总数，避免“全部”显示为 0
const allArticleCount = computed(() =>
  categories.value.length ? totalArticles.value : total.value
)

const tagOptions = computed(() => [
  { name: '全部', count: allArticleCount.value },
  ...categories.value.map((category) => ({
    name: category.name,
    count: category.articleCount
  }))
])

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))

async function loadCategories() {
  try {
    const res = (await request.get('/category/list')) as { data: CategoryItem[] }
    categories.value = res.data
  } catch {
    // 分类接口失败时不影响文章列表本身的加载
    categories.value = []
  }
}

async function loadArticles() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = (await request.get('/article/list', {
      params: {
        page: page.value,
        size: PAGE_SIZE,
        category: activeCategorySlug.value || undefined,
        keyword: appliedKeyword.value || undefined
      }
    })) as { data: ArticlePage }

    articles.value = res.data.records
    total.value = res.data.total
  } catch {
    articles.value = []
    total.value = 0
    errorMessage.value = '文章加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function selectTag(name: string) {
  if (activeTag.value === name) return
  activeTag.value = name
  page.value = 1
  void loadArticles()
}

function applySearch() {
  appliedKeyword.value = keywordInput.value.trim()
  page.value = 1
  void loadArticles()
}

function clearSearch() {
  keywordInput.value = ''
  appliedKeyword.value = ''
  page.value = 1
  void loadArticles()
}

function changePage(next: number) {
  if (next < 1 || next > totalPages.value || next === page.value) return
  page.value = next
  void loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
        <section class="content-column">
          <!-- 关键词搜索：对应 GET /article/list 的 keyword 参数 -->
          <div class="search-bar glass-card">
            <input
              v-model="keywordInput"
              class="search-input"
              type="search"
              placeholder="搜索文章标题或摘要"
              @keyup.enter="applySearch"
            />
            <button type="button" class="search-button" @click="applySearch">搜索</button>
            <button
              v-if="appliedKeyword"
              type="button"
              class="search-clear"
              @click="clearSearch"
            >
              清除
            </button>
          </div>

          <p v-if="appliedKeyword" class="result-hint">
            搜索“{{ appliedKeyword }}”共 {{ total }} 篇
          </p>

          <!-- 加载中 -->
          <div v-if="loading" class="state-card glass-card">
            <span aria-hidden="true">⏳</span>
            <p>正在加载文章…</p>
          </div>

          <!-- 加载失败 -->
          <div v-else-if="errorMessage" class="state-card glass-card">
            <span aria-hidden="true">🌧️</span>
            <p>{{ errorMessage }}</p>
          </div>

          <!-- 文章列表：数据来自后端文章接口，仅做展示 -->
          <div v-else-if="articles.length" class="article-list">
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
                <!-- TODO: 后端提供公开的文章详情接口后，再在这里加入“阅读全文”入口 -->
              </footer>
            </article>
          </div>

          <!-- 空状态 -->
          <div v-else class="state-card glass-card">
            <span aria-hidden="true">🍃</span>
            <p v-if="appliedKeyword">没有找到与“{{ appliedKeyword }}”相关的文章。</p>
            <p v-else>这个分类暂时还没有文章。</p>
          </div>

          <!-- 分页：对应 GET /article/list 的 page / size 参数 -->
          <div v-if="!loading && !errorMessage && totalPages > 1" class="pager">
            <button
              type="button"
              class="pager-button"
              :disabled="page <= 1"
              @click="changePage(page - 1)"
            >
              上一页
            </button>
            <span class="pager-info">第 {{ page }} / {{ totalPages }} 页</span>
            <button
              type="button"
              class="pager-button"
              :disabled="page >= totalPages"
              @click="changePage(page + 1)"
            >
              下一页
            </button>
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

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 20px;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 40px;
  padding: 0 14px;
  border: 1px solid rgba(102, 184, 127, 0.28);
  border-radius: 12px;
  color: #2f5c3d;
  font-size: 14px;
  outline: none;
  background: rgba(255, 255, 255, 0.72);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.search-input::placeholder {
  color: rgba(60, 104, 76, 0.5);
}

.search-input:focus {
  border-color: #66b87f;
  box-shadow: 0 4px 16px rgba(91, 154, 110, 0.16);
}

.search-button,
.search-clear,
.pager-button {
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.search-button {
  color: #ffffff;
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 8px 18px rgba(63, 159, 98, 0.24);
}

.search-clear {
  color: #2f7d4a;
  background: rgba(255, 255, 255, 0.6);
}

.search-button:hover,
.search-clear:hover,
.pager-button:not(:disabled):hover {
  transform: translateY(-1px);
}

.result-hint {
  margin: 14px 4px 0;
  color: rgba(60, 104, 76, 0.68);
  font-size: 13px;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 28px;
  margin-top: 20px;
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

.state-card {
  margin-top: 20px;
  padding: 60px 24px;
  border-radius: 24px;
  text-align: center;
}

.state-card span {
  display: block;
  margin-bottom: 12px;
  font-size: 40px;
}

.state-card p {
  margin: 0;
  color: rgba(60, 104, 76, 0.72);
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 28px;
}

.pager-button {
  color: #2f7d4a;
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 18px rgba(91, 154, 110, 0.14);
}

.pager-button:disabled {
  color: rgba(60, 104, 76, 0.4);
  cursor: not-allowed;
  background: rgba(255, 255, 255, 0.35);
}

.pager-info {
  color: rgba(60, 104, 76, 0.68);
  font-size: 13px;
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
