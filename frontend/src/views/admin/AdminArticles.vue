<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/** 后台文章卡片（来自 GET /admin/article/list） */
interface AdminArticleItem {
  id: number
  title: string
  summary: string
  categoryName: string
  status: 'draft' | 'published'
  publishedAt: string | null
  readingMinutes: number | null
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
  records: AdminArticleItem[]
  total: number
  current: number
  size: number
  pages: number
}

type StatusFilter = 'all' | 'published' | 'draft'
type ArticleAction = 'publish' | 'unpublish' | 'delete'

const PAGE_SIZE = 8

const router = useRouter()

const articles = ref<AdminArticleItem[]>([])
const categories = ref<CategoryItem[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const statusFilter = ref<StatusFilter>('all')
const categorySlug = ref('')
const keywordInput = ref('')
const appliedKeyword = ref('')
/** 正在执行操作的文章ID，用来只禁用这一行的按钮 */
const busyId = ref<number | null>(null)
const busyAction = ref<ArticleAction | null>(null)

const statusText: Record<AdminArticleItem['status'], string> = {
  draft: '草稿',
  published: '已发布'
}

function statusLabel(status: AdminArticleItem['status']) {
  return statusText[status] ?? status
}

function formatDateTime(value?: string | null) {
  if (!value) return '—'
  return value.replace('T', ' ').slice(0, 16)
}

function isBusy(id: number, action: ArticleAction) {
  return busyId.value === id && busyAction.value === action
}

function rowDisabled(id: number) {
  return busyId.value !== null && busyId.value !== id
}

async function loadCategories() {
  try {
    const res = (await request.get('/category/list')) as { data: CategoryItem[] }
    categories.value = res.data
  } catch {
    // 分类只是筛选项，接口失败不影响文章列表
    categories.value = []
  }
}

async function loadArticles() {
  loading.value = true
  try {
    const res = (await request.get('/admin/article/list', {
      params: {
        page: page.value,
        size: PAGE_SIZE,
        status: statusFilter.value === 'all' ? undefined : statusFilter.value,
        category: categorySlug.value || undefined,
        keyword: appliedKeyword.value || undefined
      }
    })) as { data: ArticlePage }

    articles.value = res.data.records
    total.value = res.data.total
  } catch {
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function reloadFromFirstPage() {
  page.value = 1
  await loadArticles()
}

function applySearch() {
  appliedKeyword.value = keywordInput.value.trim()
  void reloadFromFirstPage()
}

function clearSearch() {
  keywordInput.value = ''
  appliedKeyword.value = ''
  void reloadFromFirstPage()
}

function changePage(next: number) {
  const totalPages = Math.max(1, Math.ceil(total.value / PAGE_SIZE))
  if (next < 1 || next > totalPages || next === page.value) return
  page.value = next
  void loadArticles()
}

function goCreate() {
  void router.push('/admin/articles/new')
}

function goEdit(row: AdminArticleItem) {
  void router.push(`/admin/articles/${row.id}/edit`)
}

async function confirmAction(message: string, title: string, confirmButtonText: string) {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText,
      cancelButtonText: '取消',
      type: 'warning'
    })
    return true
  } catch {
    return false
  }
}

/** 行内动作统一收口：加按钮 loading、刷列表、统一提示。 */
async function runAction(
  row: AdminArticleItem,
  action: ArticleAction,
  url: string,
  payload: { params: { id: number } },
  successMessage: string | ((data: unknown) => string)
) {
  busyId.value = row.id
  busyAction.value = action
  try {
    const res = (await request.post(url, null, payload)) as { data: unknown }
    ElMessage.success(typeof successMessage === 'function' ? successMessage(res.data) : successMessage)
    await loadArticles()
  } finally {
    busyId.value = null
    busyAction.value = null
  }
}

async function publishArticle(row: AdminArticleItem) {
  const confirmed = await confirmAction(
    `发布后《${row.title}》会出现在前台文章列表，确定发布吗？`,
    '发布文章',
    '发布'
  )
  if (!confirmed) return

  await runAction(row, 'publish', '/admin/article/publish', { params: { id: row.id } }, '文章已发布')
}

async function unpublishArticle(row: AdminArticleItem) {
  const confirmed = await confirmAction(
    `《${row.title}》会从前台文章列表下线，内容保留为草稿，确定取消发布吗？`,
    '取消发布',
    '取消发布'
  )
  if (!confirmed) return

  await runAction(
    row,
    'unpublish',
    '/admin/article/unpublish',
    { params: { id: row.id } },
    '已取消发布，文章回退为草稿'
  )
}

async function removeArticle(row: AdminArticleItem) {
  const isPublished = row.status === 'published'
  const confirmed = await confirmAction(
    isPublished
      ? `《${row.title}》已发布，删除会先取消发布并保留为草稿，确定继续吗？`
      : `《${row.title}》删除后无法恢复，确定删除吗？`,
    isPublished ? '取消发布' : '删除文章',
    '确定'
  )
  if (!confirmed) return

  await runAction(row, 'delete', '/admin/article/delete', { params: { id: row.id } }, (data) =>
    data === 'DELETED' ? '文章已删除' : '已取消发布，文章回退为草稿'
  )
}

watch([statusFilter, categorySlug], () => {
  void reloadFromFirstPage()
})

onMounted(async () => {
  await loadCategories()
  await loadArticles()
})
</script>

<template>
  <div class="admin-page">
    <header class="admin-page-header">
      <div>
        <h1 class="admin-page-title">文章管理</h1>
        <p class="admin-page-subtitle">
          共 {{ total }} 篇文章，草稿和已发布都在这里维护
        </p>
      </div>

      <div class="admin-page-actions">
        <el-button :loading="loading" @click="loadArticles">刷新</el-button>
        <el-button type="primary" @click="goCreate">新建文章</el-button>
      </div>
    </header>

    <section class="admin-panel toolbar">
      <el-radio-group v-model="statusFilter" size="large">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="published">已发布</el-radio-button>
        <el-radio-button value="draft">草稿</el-radio-button>
      </el-radio-group>

      <el-select
        v-model="categorySlug"
        class="category-select"
        placeholder="全部分类"
        clearable
        size="large"
      >
        <el-option
          v-for="category in categories"
          :key="category.slug"
          :label="category.name"
          :value="category.slug"
        />
      </el-select>

      <!-- 输入框和搜索按钮作为一个整体换行，窄屏时不会拆散 -->
      <div class="search-group">
        <el-input
          v-model="keywordInput"
          class="keyword-input"
          placeholder="搜索标题或摘要"
          clearable
          size="large"
          @keyup.enter="applySearch"
          @clear="clearSearch"
        >
          <!-- 用内联 SVG 而不是 el-input 的 append 插槽：append 会带出 Element Plus 的
               默认灰底方角样式，和这里的薄荷毛玻璃输入框不搭 -->
          <template #prefix>
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
          </template>
        </el-input>

        <el-button type="primary" size="large" class="search-button" @click="applySearch">
          搜索
        </el-button>
      </div>
    </section>

    <section class="admin-panel list-panel">
      <el-table v-loading="loading" :data="articles" style="width: 100%">
        <el-table-column label="文章" min-width="300">
          <template #default="{ row }">
            <div class="article-cell">
              <p class="article-title">{{ row.title }}</p>
              <p class="article-summary">{{ row.summary || '暂无摘要' }}</p>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <span class="category-chip">{{ row.categoryName || '未分类' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'" effect="light">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">
            <!-- 草稿不展示发布时间，避免取消发布后残留的时间误导 -->
            <span class="time-text">
              {{ row.status === 'published' ? formatDateTime(row.publishedAt) : '—' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" align="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" :disabled="rowDisabled(row.id)" @click="goEdit(row)">
                编辑
              </el-button>

              <el-button
                v-if="row.status === 'draft'"
                link
                type="success"
                :loading="isBusy(row.id, 'publish')"
                :disabled="rowDisabled(row.id)"
                @click="publishArticle(row)"
              >
                发布
              </el-button>

              <el-button
                v-else
                link
                type="warning"
                :loading="isBusy(row.id, 'unpublish')"
                :disabled="rowDisabled(row.id)"
                @click="unpublishArticle(row)"
              >
                取消发布
              </el-button>

              <el-button
                link
                type="danger"
                :loading="isBusy(row.id, 'delete')"
                :disabled="rowDisabled(row.id)"
                @click="removeArticle(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="admin-state">
            <span class="admin-state-icon" aria-hidden="true">🗂️</span>
            <p>当前筛选下还没有文章，写一篇试试？</p>
            <el-button type="primary" @click="goCreate">新建文章</el-button>
          </div>
        </template>
      </el-table>

      <footer v-if="total > PAGE_SIZE" class="list-footer">
        <el-pagination
          :current-page="page"
          :page-size="PAGE_SIZE"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="changePage"
        />
      </footer>
    </section>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
}

.category-select {
  width: 160px;
}

.search-group {
  display: flex;
  flex: 1 1 320px;
  gap: 10px;
  min-width: 260px;
  max-width: 430px;
}

.keyword-input {
  flex: 1;
  min-width: 0;
}

.search-icon {
  width: 16px;
  height: 16px;
  color: #4f9b69;
}

.search-button {
  flex: 0 0 auto;
  padding: 0 22px;
}

.list-panel {
  padding: 6px 6px 12px;
}

.article-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 0;
}

.article-title {
  margin: 0;
  color: #2f5c3d;
  font-size: 15px;
  font-weight: 600;
}

.article-summary {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: rgba(60, 104, 76, 0.66);
  font-size: 13px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.category-chip {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  color: rgba(47, 92, 61, 0.82);
  font-size: 12px;
  background: rgba(186, 226, 197, 0.5);
}

.time-text {
  color: rgba(60, 104, 76, 0.7);
  font-size: 13px;
}

.row-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
}

.list-footer {
  padding-top: 16px;
}
</style>
