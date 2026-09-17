<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import request from '@/utils/request'

/** 公开文章详情（来自 GET /article/detail/{id}） */
interface ArticleDetail {
  id: number
  title: string
  summary: string
  content: string
  categoryName: string
  categorySlug: string
  authorUsername: string | null
  readingMinutes: number
  publishedAt: string | null
  createdAt: string | null
  updatedAt: string | null
}

const route = useRoute()
const article = ref<ArticleDetail | null>(null)
const loading = ref(false)
const errorMessage = ref('')

// Markdown 在后端只存原文，这里解析后再用 DOMPurify 清洗，避免正文注入脚本
const renderedContent = computed(() => {
  const source = article.value?.content
  if (!source) return ''
  const html = marked.parse(source, { async: false }) as string
  return DOMPurify.sanitize(html)
})

function formatDate(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : ''
}

async function loadArticle(id: string) {
  loading.value = true
  errorMessage.value = ''
  article.value = null
  try {
    const res = (await request.get(`/article/detail/${id}`)) as { data: ArticleDetail }
    article.value = res.data
  } catch {
    errorMessage.value = '这篇文章不存在，或者还没有发布。'
  } finally {
    loading.value = false
  }
}

// 支持从列表进入详情以及直接刷新详情链接
watch(
  () => route.params.id,
  (id) => {
    if (typeof id === 'string' && id) {
      void loadArticle(id)
    }
  },
  { immediate: true }
)
</script>

<template>
  <div class="detail-page">
    <div class="blob blob-1" aria-hidden="true"></div>
    <div class="blob blob-2" aria-hidden="true"></div>

    <main class="detail-container">
      <div class="back-row">
        <router-link class="back-link" to="/articles">返回</router-link>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="state-card glass-card">
        <span aria-hidden="true">⏳</span>
        <p>正在加载文章…</p>
      </div>

      <!-- 加载失败或文章不可见 -->
      <div v-else-if="errorMessage" class="state-card glass-card">
        <span aria-hidden="true">🌧️</span>
        <p>{{ errorMessage }}</p>
        <router-link class="state-link" to="/articles">去看看其它文章</router-link>
      </div>

      <article v-else-if="article" class="article-detail glass-card">
        <header class="detail-header">
          <span class="card-tag">{{ article.categoryName }}</span>
          <h1>{{ article.title }}</h1>
          <p v-if="article.summary" class="detail-summary">{{ article.summary }}</p>
          <div class="detail-meta">
            <span v-if="article.authorUsername">作者：{{ article.authorUsername }}</span>
            <time :datetime="article.publishedAt || undefined">
              {{ formatDate(article.publishedAt) }}
            </time>
            <span>约 {{ article.readingMinutes }} 分钟</span>
          </div>
        </header>

        <!-- 内容已通过 DOMPurify 清洗 -->
        <div class="markdown-body" v-html="renderedContent"></div>
      </article>
    </main>
  </div>
</template>

<style scoped>
.detail-page {
  position: relative;
  min-height: 100vh;
  padding: 44px 20px 160px;
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

.detail-container {
  position: relative;
  z-index: 1;
  width: min(860px, 100%);
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

.back-row {
  margin-bottom: 18px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 12px;
  color: #2f7d4a;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.55);
  transition:
    transform 0.2s ease,
    background-color 0.2s ease;
}

.back-link:hover {
  transform: translateX(-2px);
  background: rgba(255, 255, 255, 0.85);
}

.article-detail {
  padding: 52px 56px 56px;
  border-radius: 28px;
}

.detail-header {
  padding-bottom: 26px;
  border-bottom: 1px solid rgba(102, 184, 127, 0.24);
}

.card-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  color: #2f7d4a;
  font-size: 12px;
  background: rgba(186, 226, 197, 0.65);
}

.detail-header h1 {
  margin: 18px 0 12px;
  color: #2f5c3d;
  font-size: clamp(26px, 4vw, 36px);
  line-height: 1.4;
}

.detail-summary {
  margin: 0 0 18px;
  color: rgba(60, 104, 76, 0.78);
  font-size: 15px;
  line-height: 1.8;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  color: rgba(60, 104, 76, 0.6);
  font-size: 13px;
}

.markdown-body {
  margin-top: 28px;
  color: rgba(47, 92, 61, 0.92);
  font-size: 16px;
  line-height: 1.9;
  word-break: break-word;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 32px 0 14px;
  color: #2f5c3d;
  line-height: 1.4;
}

.markdown-body :deep(h2) {
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(102, 184, 127, 0.24);
  font-size: 22px;
}

.markdown-body :deep(h3) {
  font-size: 19px;
}

.markdown-body :deep(p) {
  margin: 0 0 18px;
}

.markdown-body :deep(a) {
  color: #3f9f62;
  text-decoration: underline;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 0 0 18px;
  padding-left: 24px;
}

.markdown-body :deep(li) {
  margin-bottom: 8px;
}

.markdown-body :deep(blockquote) {
  margin: 0 0 18px;
  padding: 12px 18px;
  border-left: 4px solid #8ccb9f;
  border-radius: 0 12px 12px 0;
  color: rgba(47, 92, 61, 0.8);
  background: rgba(255, 255, 255, 0.5);
}

.markdown-body :deep(code) {
  padding: 2px 6px;
  border-radius: 6px;
  font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
  font-size: 14px;
  background: rgba(186, 226, 197, 0.45);
}

.markdown-body :deep(pre) {
  margin: 0 0 20px;
  padding: 18px 20px;
  border-radius: 16px;
  overflow-x: auto;
  background: rgba(47, 92, 61, 0.92);
  box-shadow: 0 12px 26px rgba(47, 92, 61, 0.18);
}

.markdown-body :deep(pre code) {
  padding: 0;
  color: #eaf7ec;
  background: transparent;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 16px;
  box-shadow: 0 12px 28px rgba(91, 154, 110, 0.2);
}

.markdown-body :deep(table) {
  width: 100%;
  margin-bottom: 20px;
  border-collapse: collapse;
  font-size: 14px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 10px 12px;
  border: 1px solid rgba(102, 184, 127, 0.3);
  text-align: left;
}

.markdown-body :deep(th) {
  background: rgba(186, 226, 197, 0.45);
}

.markdown-body :deep(hr) {
  margin: 28px 0;
  border: none;
  border-top: 1px solid rgba(102, 184, 127, 0.3);
}

.state-card {
  padding: 64px 24px;
  border-radius: 28px;
  text-align: center;
}

.state-card span {
  display: block;
  margin-bottom: 12px;
  font-size: 40px;
}

.state-card p {
  margin: 0 0 18px;
  color: rgba(60, 104, 76, 0.72);
}

.state-link {
  display: inline-flex;
  padding: 9px 18px;
  border-radius: 12px;
  color: #ffffff;
  font-size: 14px;
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 8px 18px rgba(63, 159, 98, 0.24);
}

@media (max-width: 720px) {
  .article-detail {
    padding: 34px 24px 40px;
  }
}
</style>
