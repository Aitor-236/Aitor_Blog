<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElInput, ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { renderMarkdown } from '@/utils/markdown'

/** 后台文章详情（来自 GET /admin/article/{id}） */
interface AdminArticleDetail {
  id: number
  title: string
  summary: string
  content: string
  categoryId: number | null
  categoryName: string
  categorySlug: string
  tags: string[]
  status: 'draft' | 'published'
  publishedAt: string | null
  createdAt: string | null
  updatedAt: string | null
}

/** 分类类型（来自 GET /category/list） */
interface CategoryItem {
  id: number
  name: string
  slug: string
  articleCount: number
}

/** 后台标签项（来自 GET /admin/tag/list） */
interface AdminTagItem {
  id: number
  name: string
  articleCount: number
}

/** MyBatis-Plus 分页返回结构 */
interface TagPage {
  records: AdminTagItem[]
  total: number
}

/** 正文配图上传结果（来自 POST /admin/upload/image） */
interface UploadedImage {
  url: string
  name: string
  size: number
}

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
/** 正文输入框，插入图片时要拿它内部 textarea 的光标位置 */
const contentRef = ref<InstanceType<typeof ElInput> | null>(null)
const imageInputRef = ref<HTMLInputElement | null>(null)
const form = reactive({
  title: '',
  summary: '',
  categoryName: '',
  content: '',
  tags: [] as string[]
})

const rules: FormRules<typeof form> = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  summary: [{ max: 500, message: '摘要不能超过 500 字', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请选择文章分类', trigger: 'change' }]
}

/** 新建时为空，保存草稿成功后写入后端返回的ID */
const articleId = ref<number | null>(null)
const status = ref<'draft' | 'published'>('draft')
const meta = reactive({
  createdAt: '',
  updatedAt: '',
  publishedAt: ''
})

const categories = ref<CategoryItem[]>([])
/** 标签下拉选项，取后台标签库的前 100 个；编辑器里也能直接输入新标签 */
const tagOptions = ref<string[]>([])
const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const unpublishing = ref(false)
const uploadingImage = ref(false)
const dirty = ref(false)
/** 回填表单期间不把接口返回的内容当成用户修改 */
const hydrating = ref(false)

const isEditMode = computed(() => articleId.value !== null)

const renderedContent = computed(() => {
  if (!form.content.trim()) return '<p class="preview-placeholder">正文预览会显示在这里</p>'
  return renderMarkdown(form.content)
})

const wordCount = computed(() => form.content.replace(/\s/g, '').length)

function formatDateTime(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '—'
}

async function loadCategories() {
  try {
    const res = (await request.get('/category/list')) as { data: CategoryItem[] }
    categories.value = res.data
  } catch {
    // 分类接口失败时仍可手动输入分类名，不阻塞编辑
    categories.value = []
  }
}

async function loadTags() {
  try {
    const res = (await request.get('/admin/tag/list', {
      params: { page: 1, size: 100 }
    })) as { data: TagPage }
    tagOptions.value = res.data.records.map((tag) => tag.name)
  } catch {
    // 标签接口失败时仍然可以手写标签名，不阻塞编辑
    tagOptions.value = []
  }
}

async function loadDetail(id: string) {
  loading.value = true
  hydrating.value = true
  try {
    const res = (await request.get(`/admin/article/${id}`)) as { data: AdminArticleDetail }
    const detail = res.data
    articleId.value = detail.id
    status.value = detail.status
    form.title = detail.title ?? ''
    form.summary = detail.summary ?? ''
    form.categoryName = detail.categoryName ?? ''
    form.content = detail.content ?? ''
    form.tags = detail.tags ?? []
    meta.createdAt = detail.createdAt ?? ''
    meta.updatedAt = detail.updatedAt ?? ''
    meta.publishedAt = detail.publishedAt ?? ''
  } catch {
    ElMessage.error('文章不存在或已被删除')
    await router.replace('/admin/articles')
  } finally {
    loading.value = false
  }
  // 等表单 watcher 跑完再解除标记，否则回填会被记成一次未保存修改
  await nextTick()
  hydrating.value = false
  dirty.value = false
}

function goBack() {
  void router.push('/admin/articles')
}

function buildPayload() {
  return {
    title: form.title.trim(),
    // 摘要和正文在库里是 NOT NULL，留空时提交空串而不是 undefined
    summary: form.summary ?? '',
    content: form.content ?? '',
    categoryName: form.categoryName,
    // 标签整体提交，空数组表示清空这篇文章的标签
    tags: form.tags
  }
}

/**
 * 把一段 Markdown 插到正文光标处（没有光标就追加到末尾），
 * 前后自动补齐换行，插完把光标停在片段后面，方便接着写。
 */
function insertIntoContent(snippet: string) {
  const textarea = contentRef.value?.textarea
  const source = form.content
  const start = textarea?.selectionStart ?? source.length
  const end = textarea?.selectionEnd ?? start

  const before = source.slice(0, start)
  const after = source.slice(end)
  const prefix = !before ? '' : before.endsWith('\n\n') ? '' : before.endsWith('\n') ? '\n' : '\n\n'
  const suffix = after.startsWith('\n') ? '' : '\n'
  const inserted = `${prefix}${snippet}${suffix}`

  form.content = before + inserted + after
  void nextTick(() => {
    if (!textarea) return
    textarea.focus()
    const caret = start + inserted.length
    textarea.setSelectionRange(caret, caret)
  })
}

function pickImage() {
  imageInputRef.value?.click()
}

/** 选择图片后立刻上传并插入正文，中间不弹二次确认。 */
async function onImagePick(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  // 清空 value，否则连续选同一张图片不会再触发 change
  input.value = ''
  if (file) {
    await insertImage(file)
  }
}

/** 上传图片并把 Markdown 图片语法插到光标处，预览区和前台详情页都会显示。 */
async function insertImage(file: File) {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('只能插入图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片不能超过 5MB')
    return
  }

  const formData = new FormData()
  formData.append('file', file)

  uploadingImage.value = true
  try {
    // 上传比普通请求慢，单独放宽超时（默认 5 秒）
    const res = (await request.post('/admin/upload/image', formData, { timeout: 20000 })) as {
      data: UploadedImage
    }
    const alt = file.name.replace(/\.[^.]+$/, '').replace(/[[\]]/g, '') || '图片'
    insertIntoContent(`![${alt}](${res.data.url})`)
    ElMessage.success('图片已插入正文')
  } catch {
    // 错误提示由 request 拦截器统一处理
  } finally {
    uploadingImage.value = false
  }
}

/** 截图直接粘贴时自动上传；粘贴普通文本不拦截。 */
function onPasteImage(event: Event) {
  const files = Array.from((event as ClipboardEvent).clipboardData?.files ?? [])
  const image = files.find((item) => item.type.startsWith('image/'))
  if (!image) return
  event.preventDefault()
  void insertImage(image)
}

/** 把图片文件拖到编辑区也能插入。 */
function onDropImage(event: Event) {
  const files = Array.from((event as DragEvent).dataTransfer?.files ?? [])
  const image = files.find((item) => item.type.startsWith('image/'))
  if (!image) return
  event.preventDefault()
  void insertImage(image)
}

/**
 * 保证当前文章已经落库：新建态先调 create 拿到ID，并把地址栏换成编辑态地址，
 * 这样之后的保存和发布都是对同一篇文章的更新。
 */
async function ensureArticleId() {
  if (articleId.value !== null) return articleId.value

  const res = (await request.post('/admin/article/create', buildPayload())) as {
    data: { id: number }
  }
  articleId.value = res.data.id
  // 内容已经落库，先把"未保存"标记清掉，否则下面换地址会被离开确认拦下来
  dirty.value = false
  await router.replace(`/admin/articles/${res.data.id}/edit`)
  return res.data.id
}

/** 保存草稿：新建走 create，已有ID走 update，保存后保持在本页继续写。 */
async function saveDraft() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const isNew = articleId.value === null
    const id = await ensureArticleId()
    if (!isNew) {
      await request.post('/admin/article/update', { id, ...buildPayload() })
    }
    dirty.value = false
    // 重新拉一次详情，把创建/更新时间等元信息补上
    await loadDetail(String(id))
    ElMessage.success('草稿已保存')
  } finally {
    saving.value = false
  }
}

/** 发布前先落库再调发布接口，保证发布的是当前编辑器里的内容。 */
async function publishArticle() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.summary.trim()) {
    ElMessage.warning('发布前请先填写摘要，前台文章卡片会用到')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('发布前请先填写正文')
    return
  }

  publishing.value = true
  try {
    const isNew = articleId.value === null
    const id = await ensureArticleId()
    if (!isNew) {
      await request.post('/admin/article/update', { id, ...buildPayload() })
    }

    dirty.value = false
    await request.post('/admin/article/publish', null, { params: { id } })
    await loadDetail(String(id))
    ElMessage.success('文章已发布，前台文章列表已经可以看到')
  } finally {
    publishing.value = false
  }
}

/** 取消发布：内容保留，文章从前台下线回退为草稿。 */
async function unpublishArticle() {
  if (articleId.value === null) return

  try {
    await ElMessageBox.confirm(
      '取消发布后文章会从前台列表下线，内容保留为草稿，确定取消发布吗？',
      '取消发布',
      { confirmButtonText: '取消发布', cancelButtonText: '再想想', type: 'warning' }
    )
  } catch {
    return
  }

  unpublishing.value = true
  try {
    await request.post('/admin/article/unpublish', null, { params: { id: articleId.value } })
    ElMessage.success('已取消发布，文章回退为草稿')
    await loadDetail(String(articleId.value))
  } finally {
    unpublishing.value = false
  }
}

// 自己保存后 replace 的地址不重复拉取，避免覆盖正在输入的内容
watch(
  () => route.params.id,
  (id) => {
    const next = typeof id === 'string' && id ? id : ''
    if (!next || String(articleId.value) === next) return
    void loadDetail(next)
  }
)

watch(
  form,
  () => {
    if (hydrating.value) return
    dirty.value = true
  },
  { deep: true }
)

onBeforeRouteLeave(async () => {
  if (!dirty.value) return true

  try {
    await ElMessageBox.confirm('当前修改还没保存，离开后修改会丢失，确定离开吗？', '未保存的修改', {
      confirmButtonText: '离开',
      cancelButtonText: '继续编辑',
      type: 'warning'
    })
    return true
  } catch {
    return false
  }
})

onMounted(async () => {
  await Promise.all([loadCategories(), loadTags()])
  const id = typeof route.params.id === 'string' ? route.params.id : ''
  if (id) {
    await loadDetail(id)
  }
  dirty.value = false
})
</script>

<template>
  <div v-loading="loading" class="admin-page">
    <header class="admin-page-header">
      <div>
        <h1 class="admin-page-title">
          {{ isEditMode ? '编辑文章' : '新建文章' }}
          <el-tag :type="status === 'published' ? 'success' : 'info'" effect="light" class="status-tag">
            {{ status === 'published' ? '已发布' : '草稿' }}
          </el-tag>
        </h1>
        <p class="admin-page-subtitle">
          <template v-if="isEditMode">
            最近更新 {{ formatDateTime(meta.updatedAt) }}
            <template v-if="meta.publishedAt">
              · 发布于 {{ formatDateTime(meta.publishedAt) }}
            </template>
          </template>
          <template v-else>正文用 Markdown 编写</template>
        </p>
      </div>

      <div class="admin-page-actions">
        <router-link v-if="status === 'published' && articleId" :to="`/articles/${articleId}`" target="_blank">
          <el-button>查看前台</el-button>
        </router-link>
        <el-button @click="goBack">返回列表</el-button>
        <el-button :loading="saving" @click="saveDraft">保存草稿</el-button>
        <el-button
          v-if="status === 'published'"
          :loading="unpublishing"
          class="warning-button"
          @click="unpublishArticle"
        >
          取消发布
        </el-button>
        <el-button v-else type="primary" :loading="publishing" @click="publishArticle">
          发布文章
        </el-button>
      </div>
    </header>

    <div class="editor-layout">
      <section class="admin-panel editor-panel">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
          <el-form-item label="文章标题" prop="title">
            <el-input v-model="form.title" placeholder="一句话说清这篇文章讲什么" maxlength="200" />
          </el-form-item>

          <el-form-item label="文章摘要" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="出现在文章列表卡片上的简介，发布时必填"
            />
          </el-form-item>

          <el-form-item label="文章分类" prop="categoryName">
            <el-select
              v-model="form.categoryName"
              class="category-select"
              placeholder="选择分类，也可以直接输入新分类名"
              filterable
              allow-create
              default-first-option
            >
              <el-option
                v-for="category in categories"
                :key="category.slug"
                :label="category.name"
                :value="category.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="标签" prop="tags">
            <el-select
              v-model="form.tags"
              class="tag-select"
              multiple
              filterable
              allow-create
              default-first-option
              :reserve-keyword="false"
              placeholder="选择已有标签，或输入新标签后回车"
            >
              <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
            </el-select>
            <p class="form-tip">
              标签会显示在前台文章卡片和详情页，点标签可以筛出同类文章；留空表示这篇文章没有标签
            </p>
          </el-form-item>

          <el-form-item label="正文（Markdown）" prop="content">
            <div class="content-toolbar">
              <el-button size="small" :icon="Picture" :loading="uploadingImage" @click="pickImage">
                插入图片
              </el-button>
              <span class="content-toolbar-tip">
                也可以直接粘贴或拖拽图片，支持 png / jpg / webp / gif，单张不超过 5MB
              </span>
            </div>

            <input
              ref="imageInputRef"
              class="file-input"
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif"
              @change="onImagePick"
            />

            <el-input
              ref="contentRef"
              v-model="form.content"
              type="textarea"
              :rows="20"
              resize="vertical"
              class="content-input"
              placeholder="# 标题&#10;正文支持 Markdown：列表、代码块、引用、图片等"
              @paste="onPasteImage"
              @drop="onDropImage"
              @dragover.prevent
            />
          </el-form-item>
        </el-form>
      </section>

      <aside class="editor-side">
        <section class="admin-panel preview-panel">
          <div class="panel-header">
            <h2>正文预览</h2>
            <span class="panel-meta">{{ wordCount }} 字</span>
          </div>
          <!-- 预览内容已用 DOMPurify 清洗 -->
          <div class="markdown-body" v-html="renderedContent"></div>
        </section>

        <section v-if="isEditMode" class="admin-panel meta-panel">
          <h2>文章信息</h2>
          <dl class="meta-list">
            <div>
              <dt>创建时间</dt>
              <dd>{{ formatDateTime(meta.createdAt) }}</dd>
            </div>
            <div>
              <dt>更新时间</dt>
              <dd>{{ formatDateTime(meta.updatedAt) }}</dd>
            </div>
            <div>
              <dt>发布时间</dt>
              <dd>{{ status === 'published' ? formatDateTime(meta.publishedAt) : '—' }}</dd>
            </div>
            <div>
              <dt>文章ID</dt>
              <dd>#{{ articleId }}</dd>
            </div>
          </dl>
        </section>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.status-tag {
  margin-left: 10px;
  vertical-align: middle;
}

.editor-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.editor-panel {
  padding: 22px 22px 6px;
}

.category-select {
  width: 100%;
}

.tag-select {
  width: 100%;
}

.form-tip {
  width: 100%;
  margin: 6px 0 0;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.5;
}

.content-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
  margin-bottom: 10px;
}

.content-toolbar-tip {
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.6;
}

/* 隐藏原生文件选择框，点「插入图片」按钮时由它弹出系统选择窗口 */
.file-input {
  display: none;
}

.content-input :deep(textarea) {
  font-family:
    'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, Consolas, 'PingFang SC', monospace;
  font-size: 14px;
  line-height: 1.7;
}

.editor-side {
  display: flex;
  position: sticky;
  top: 20px;
  flex-direction: column;
  gap: 18px;
}

.preview-panel,
.meta-panel {
  padding: 18px 20px 22px;
}

.panel-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-header h2,
.meta-panel h2 {
  margin: 0;
  color: var(--text-strong);
  font-size: 16px;
  font-weight: 600;
}

.panel-meta {
  color: var(--text-muted);
  font-size: 12px;
}

.markdown-body {
  max-height: 60vh;
  overflow-y: auto;
  color: var(--text-body);
  font-size: 14px;
  line-height: 1.8;
  overflow-wrap: anywhere;
}

.markdown-body :deep(.preview-placeholder) {
  color: var(--text-muted);
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 18px 0 10px;
  color: var(--text-strong);
  line-height: 1.4;
}

.markdown-body :deep(h1) {
  font-size: 20px;
}

.markdown-body :deep(h2) {
  font-size: 18px;
}

.markdown-body :deep(h3) {
  font-size: 16px;
}

.markdown-body :deep(p) {
  margin: 0 0 12px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 0 0 12px;
  padding-left: 22px;
}

.markdown-body :deep(blockquote) {
  margin: 0 0 12px;
  padding: 8px 14px;
  border-left: 3px solid var(--accent-brown-soft);
  border-radius: 0 12px 12px 0;
  color: var(--text-body);
  background: var(--panel-alt-bg);
}

.markdown-body :deep(code) {
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 13px;
  background: rgba(138, 90, 59, 0.12);
}

.markdown-body :deep(pre) {
  margin: 0 0 12px;
  padding: 14px;
  overflow-x: auto;
  border-radius: 14px;
  background: rgba(138, 90, 59, 0.1);
}

.markdown-body :deep(pre code) {
  padding: 0;
  background: transparent;
}

/* 代码块语言标签：有语言的代码块会被 .code-block 包一层 */
.markdown-body :deep(.code-block) {
  position: relative;
  margin: 0 0 12px;
}

.markdown-body :deep(.code-block pre) {
  margin: 0;
  padding-top: 34px;
}

.markdown-body :deep(.code-block-lang) {
  position: absolute;
  top: 11px;
  left: 14px;
  z-index: 1;
  color: var(--accent-brown);
  font-family:
    'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, Consolas, 'PingFang SC', monospace;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.8;
  user-select: none;
}

.markdown-body :deep(img) {
  display: block;
  max-width: 100%;
  margin: 0 auto 12px;
  border-radius: 14px;
}

.meta-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 14px 0 0;
}

.meta-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}

.meta-list dt {
  color: var(--text-muted);
}

.meta-list dd {
  margin: 0;
  color: var(--text-strong);
}

@media (max-width: 1200px) {
  .editor-layout {
    grid-template-columns: 1fr;
  }

  .editor-side {
    position: static;
  }

  .markdown-body {
    max-height: 420px;
  }
}
</style>
