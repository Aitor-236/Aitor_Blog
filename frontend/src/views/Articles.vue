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
  <div class="page-shell">
    <main class="page-container">
      <!-- 左侧文章列表 + 右侧分类面板 -->
      <div class="articles-layout">
        <section class="content-column">
          <!-- 关键词搜索：对应 GET /article/list 的 keyword 参数 -->
          <div class="search-bar surface-panel">
            <svg
              class="search-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              aria-hidden="true"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-3.6-3.6" />
            </svg>
            <input
              v-model="keywordInput"
              class="search-input"
              type="text"
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
          <div v-if="loading" class="state-card">
            <span class="state-spinner" aria-hidden="true"></span>
            <p>正在加载文章…</p>
          </div>

          <!-- 加载失败 -->
          <div v-else-if="errorMessage" class="state-card">
            <p>{{ errorMessage }}</p>
          </div>

          <!-- 文章列表：数据来自后端文章接口，仅做展示 -->
          <div v-else-if="articles.length" class="article-list">
            <router-link
              v-for="article in articles"
              :key="article.id"
              class="article-item surface-panel"
              :to="`/articles/${article.id}`"
            >
              <div class="article-head">
                <span class="card-tag">{{ article.categoryName }}</span>
                <time :datetime="article.publishedAt">{{ formatDate(article.publishedAt) }}</time>
              </div>
              <h2>{{ article.title }}</h2>
              <p>{{ article.summary }}</p>
              <footer class="article-footer">
                <span>约 {{ article.readingMinutes }} 分钟读完</span>
              </footer>
            </router-link>
          </div>

          <!-- 空状态 -->
          <div v-else class="state-card">
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
        <aside class="sidebar surface-panel" aria-label="文章分类">
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
.articles-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  align-items: start;
  gap: 28px;
}

.content-column {
  min-width: 0;
}

/* ---------- 搜索栏 ---------- */

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px 8px 18px;
  border-radius: 999px;
  transition: border-color 0.2s ease;
}

.search-bar:focus-within {
  border-color: rgba(138, 90, 59, 0.45);
}

.search-icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  color: var(--accent-brown);
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 42px;
  padding: 0;
  border: none;
  border-radius: 0;
  color: var(--text-strong);
  font-size: 14px;
  outline: none;
  background: transparent;
  box-shadow: none;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-input:focus {
  border: none;
  box-shadow: none;
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
  color: #fdf9f2;
  background: linear-gradient(135deg, #b98a5e, #8a5a3b);
  box-shadow: 0 8px 18px rgba(138, 90, 59, 0.24);
}

.search-clear {
  color: var(--accent-brown);
  background: var(--panel-alt-bg);
}

.search-button:hover,
.search-clear:hover,
.pager-button:not(:disabled):hover {
  transform: translateY(-1px);
}

.result-hint {
  margin: 14px 4px 0;
  color: var(--text-muted);
  font-size: 13px;
}

/* ---------- 文章列表 ---------- */

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.article-item {
  display: block;
  padding: 30px 32px;
  border-radius: 24px;
  cursor: pointer;
  transition:
    transform 0.25s ease,
    border-color 0.25s ease,
    box-shadow 0.25s ease;
}

.article-item:hover {
  transform: translateY(-4px);
  border-color: rgba(138, 90, 59, 0.34);
  box-shadow: 0 20px 44px rgba(120, 88, 58, 0.16);
}

.article-item:focus-visible {
  outline: 2px solid var(--accent-brown);
  outline-offset: 2px;
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
  color: #7a5436;
  font-size: 12px;
  background: #ecdec5;
}

.article-head time {
  color: var(--text-muted);
  font-size: 13px;
}

.article-item h2 {
  margin: 16px 0 10px;
  color: var(--text-strong);
  font-size: 21px;
  line-height: 1.5;
}

.article-item p {
  margin: 0;
  color: var(--text-body);
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
  border-top: 1px solid rgba(138, 90, 59, 0.14);
  color: var(--text-muted);
  font-size: 13px;
}

/* ---------- 加载 / 空状态 ---------- */

.state-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
  padding: 60px 24px;
  border: 1px dashed rgba(138, 90, 59, 0.22);
  border-radius: 24px;
  background: var(--panel-bg);
  text-align: center;
}

.state-card p {
  margin: 0;
  color: var(--text-muted);
}

.state-spinner {
  width: 26px;
  height: 26px;
  border: 2px solid rgba(138, 90, 59, 0.22);
  border-top-color: var(--accent-brown);
  border-radius: 50%;
  animation: state-spin 0.8s linear infinite;
}

@keyframes state-spin {
  to {
    transform: rotate(360deg);
  }
}

/* ---------- 分页 ---------- */

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 28px;
}

.pager-button {
  color: var(--accent-brown);
  background: var(--panel-alt-bg);
  box-shadow: 0 8px 18px rgba(120, 88, 58, 0.12);
}

.pager-button:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
  background: var(--panel-bg);
  opacity: 0.6;
}

.pager-info {
  color: var(--text-muted);
  font-size: 13px;
}

/* ---------- 右侧分类 ---------- */

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
  color: var(--text-strong);
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
  color: var(--text-body);
  font-size: 14px;
  cursor: pointer;
  background: var(--panel-alt-bg);
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.filter-tag:hover {
  color: var(--accent-brown);
  background: #fbf6ec;
}

.filter-tag.is-active {
  color: #fdf9f2;
  background: linear-gradient(135deg, #b98a5e, #8a5a3b);
  box-shadow: 0 8px 18px rgba(138, 90, 59, 0.24);
}

.filter-count {
  min-width: 26px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  text-align: center;
  background: #ecdec5;
}

.filter-tag.is-active .filter-count {
  color: #fdf9f2;
  background: rgba(255, 255, 255, 0.24);
}

@media (max-width: 860px) {
  .articles-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    max-height: none;
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
