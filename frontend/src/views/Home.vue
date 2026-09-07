<script setup lang="ts">
import { onMounted, ref } from 'vue'
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

/** 画作模拟数据类型 */
interface ArtworkItem {
  id: number
  title: string
  medium: string
  date: string
  palette: string[]
}

function formatDate(value: string) {
  return value.slice(0, 10)
}

const latestArticles = ref<ArticleItem[]>([])

async function loadLatestArticles() {
  const res = (await request.get('/article/list', {
    params: { page: 1, size: 3 }
  })) as {
    data: { records: ArticleItem[] }
  }
  latestArticles.value = res.data.records
}

onMounted(loadLatestArticles)

const latestArtworks = ref<ArtworkItem[]>([
  {
    id: 1,
    title: '庭院晨光',
    medium: '水彩 · 纸上',
    date: '2026-09-01',
    palette: ['#dff3e2', '#8ccb9f', '#4f9b69']
  },
  {
    id: 2,
    title: '窗边的绿植',
    medium: '数码插画',
    date: '2026-08-20',
    palette: ['#e8f6ea', '#a5d9b2', '#6bb17f']
  },
  {
    id: 3,
    title: '雨后小路',
    medium: '钢笔淡彩',
    date: '2026-08-06',
    palette: ['#f4f8e8', '#8fbf9c', '#547a63']
  }
])
</script>

<template>
  <div class="home-page">
    <div class="blob blob-1" aria-hidden="true"></div>
    <div class="blob blob-2" aria-hidden="true"></div>

    <main class="home-container">
      <!-- 顶部欢迎区域 -->
      <section class="hero glass-card">
        <p class="hero-eyebrow">欢迎来到我的主页</p>
        <h1>你好，我是 Aitor 👋</h1>
        <p class="hero-desc">这里记录我的代码、文字与画，愿每一次创作都被温柔以待。</p>
        <div class="hero-actions">
          <router-link class="hero-button primary" to="/articles">阅读文章</router-link>
          <router-link class="hero-button ghost" to="/gallery">看看画</router-link>
        </div>
      </section>

      <!-- 最新文章：来自后端已发布文章接口 -->
      <section class="content-section">
        <div class="section-header">
          <h2>最新文章</h2>
          <router-link class="section-more" to="/articles">更多 ›</router-link>
        </div>

        <div class="card-grid">
          <article
            v-for="article in latestArticles"
            :key="article.id"
            class="glass-card article-card"
          >
            <span class="card-tag">{{ article.categoryName }}</span>
            <h3>{{ article.title }}</h3>
            <p>{{ article.summary }}</p>
            <footer class="card-footer">
              <time :datetime="article.publishedAt">{{ formatDate(article.publishedAt) }}</time>
              <span>约 {{ article.readingMinutes }} 分钟</span>
            </footer>
          </article>
        </div>
      </section>

      <!-- 最新画作：先用配色模拟封面，后期替换为真实图片 -->
      <section class="content-section">
        <div class="section-header">
          <h2>最新画作</h2>
          <router-link class="section-more" to="/gallery">更多 ›</router-link>
        </div>

        <div class="card-grid">
          <article
            v-for="artwork in latestArtworks"
            :key="artwork.id"
            class="glass-card artwork-card"
          >
            <!-- TODO: 后端提供图片后，将配色渐变封面替换为 <img> 图片 -->
            <div
              class="artwork-cover"
              :style="{
                background: `linear-gradient(135deg, ${artwork.palette.join(',')})`
              }"
              aria-hidden="true"
            ></div>
            <h3>{{ artwork.title }}</h3>
            <p>{{ artwork.medium }}</p>
            <footer class="card-footer">
              <time :datetime="artwork.date">{{ artwork.date }}</time>
            </footer>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.home-page {
  position: relative;
  min-height: 100vh;
  padding: 44px 20px 160px;
  overflow: hidden;
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

.home-container {
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

.hero {
  padding: 52px 36px;
  border-radius: 28px;
  text-align: center;
}

.hero-eyebrow {
  margin: 0 0 12px;
  color: #4f9b69;
  font-size: 14px;
  letter-spacing: 4px;
}

.hero h1 {
  margin: 0 0 14px;
  color: #2f5c3d;
  font-size: clamp(28px, 5vw, 44px);
  font-weight: 700;
}

.hero-desc {
  max-width: 560px;
  margin: 0 auto 28px;
  color: rgba(60, 104, 76, 0.78);
  font-size: 16px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
}

.hero-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  height: 44px;
  padding: 0 22px;
  border-radius: 12px;
  font-size: 15px;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.hero-button.primary {
  color: #ffffff;
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 10px 24px rgba(63, 159, 98, 0.28);
}

.hero-button.ghost {
  border: 1px solid rgba(63, 159, 98, 0.35);
  color: #2f7d4a;
  background: rgba(255, 255, 255, 0.55);
}

.hero-button:hover {
  transform: translateY(-2px);
}

.content-section {
  margin-top: 44px;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 18px;
  padding: 0 4px;
}

.section-header h2 {
  margin: 0;
  color: #2f5c3d;
  font-size: 22px;
}

.section-more {
  color: #4f9b69;
  font-size: 14px;
  transition: opacity 0.2s ease;
}

.section-more:hover {
  opacity: 0.7;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.article-card,
.artwork-card {
  display: flex;
  flex-direction: column;
  padding: 24px;
  border-radius: 22px;
  transition: transform 0.25s ease;
}

.article-card:hover,
.artwork-card:hover {
  transform: translateY(-4px);
}

.card-tag {
  align-self: flex-start;
  padding: 4px 12px;
  border-radius: 999px;
  color: #2f7d4a;
  font-size: 12px;
  background: rgba(186, 226, 197, 0.65);
}

.article-card h3,
.artwork-card h3 {
  margin: 14px 0 8px;
  color: #2f5c3d;
  font-size: 18px;
  line-height: 1.5;
}

.article-card p,
.artwork-card p {
  margin: 0;
  color: rgba(60, 104, 76, 0.75);
  font-size: 14px;
  line-height: 1.75;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
  padding-top: 18px;
  color: rgba(60, 104, 76, 0.6);
  font-size: 13px;
}

.artwork-cover {
  height: 170px;
  border-radius: 16px;
  box-shadow: 0 12px 28px rgba(91, 154, 110, 0.22);
}

.artwork-card h3 {
  margin-top: 18px;
}
</style>
