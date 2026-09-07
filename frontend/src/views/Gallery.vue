<script setup lang="ts">
import { computed, ref } from 'vue'

/** 画作数据类型 */
interface ArtworkItem {
  id: number
  title: string
  medium: string
  date: string
  category: string
  palette: string[]
}

// TODO: 后端接口就绪后，将 artworks 初始模拟数据替换为接口返回数据
const artworks = ref<ArtworkItem[]>([
  {
    id: 1,
    title: '庭院晨光',
    medium: '水彩 · 纸上',
    date: '2026-09-01',
    category: '水彩',
    palette: ['#dff3e2', '#8ccb9f', '#4f9b69']
  },
  {
    id: 2,
    title: '窗边的绿植',
    medium: '数码插画',
    date: '2026-08-20',
    category: '数码插画',
    palette: ['#e8f6ea', '#a5d9b2', '#6bb17f']
  },
  {
    id: 3,
    title: '雨后小路',
    medium: '钢笔淡彩',
    date: '2026-08-06',
    category: '速写',
    palette: ['#f4f8e8', '#8fbf9c', '#547a63']
  },
  {
    id: 4,
    title: '海边的白色房子',
    medium: '水彩 · 纸上',
    date: '2026-07-18',
    category: '水彩',
    palette: ['#eaf8f3', '#9fd3c2', '#5c9c88']
  },
  {
    id: 5,
    title: '秋天的第一片落叶',
    medium: '速写 · 钢笔淡彩',
    date: '2026-06-29',
    category: '速写',
    palette: ['#f9efdf', '#d8c07f', '#8a7a4d']
  },
  {
    id: 6,
    title: '月光下的远山',
    medium: '数码插画',
    date: '2026-06-05',
    category: '数码插画',
    palette: ['#e8eef4', '#a9bfcf', '#556f7f']
  }
])

// 画作媒介分类：目前先在前端筛选模拟数据
const artTags = ['全部', '水彩', '数码插画', '速写']
const activeTag = ref('全部')

const filteredArtworks = computed(() => {
  if (activeTag.value === '全部') {
    return artworks.value
  }
  return artworks.value.filter((artwork) => artwork.category === activeTag.value)
})

function selectTag(tag: string) {
  activeTag.value = tag
}

/** 计算每个分类下的画作数量，用于右侧分类面板展示 */
function countByTag(tag: string) {
  if (tag === '全部') {
    return artworks.value.length
  }
  return artworks.value.filter((artwork) => artwork.category === tag).length
}
</script>

<template>
  <div class="gallery-page">
    <div class="blob blob-1" aria-hidden="true"></div>
    <div class="blob blob-2" aria-hidden="true"></div>

    <main class="gallery-container">
      <!-- 页面标题区 -->
      <header class="page-hero glass-card">
        <p class="hero-eyebrow">笔下的光与色</p>
        <h1>画</h1>
        <p>一些水彩、速写与数码插画练习，慢慢积累成自己的小花园。</p>
      </header>

      <!-- 左侧画作网格 + 右侧分类 -->
      <div class="gallery-layout">
        <section class="content-column">
          <!-- 画作网格：先用配色渐变模拟封面，仅做展示，不提供添加功能 -->
          <div v-if="filteredArtworks.length" class="artwork-grid">
            <figure
              v-for="artwork in filteredArtworks"
              :key="artwork.id"
              class="artwork-card glass-card"
            >
              <!-- TODO: 后端提供图片地址后，将渐变封面替换为 <img> 图片 -->
              <div
                class="artwork-cover"
                :style="{
                  background: `linear-gradient(135deg, ${artwork.palette.join(',')})`
                }"
                aria-hidden="true"
              ></div>
              <figcaption>
                <div class="artwork-title-row">
                  <h2>{{ artwork.title }}</h2>
                  <span class="artwork-category">{{ artwork.category }}</span>
                </div>
                <p>{{ artwork.medium }}</p>
                <time :datetime="artwork.date">{{ artwork.date }}</time>
              </figcaption>
            </figure>
          </div>

          <div v-else class="empty-state glass-card">
            <span aria-hidden="true">🖼️</span>
            <p>这个分类暂时还没有作品。</p>
          </div>
        </section>

        <!-- 右侧分类面板 -->
        <aside class="sidebar glass-card" aria-label="画作分类">
          <h2 class="sidebar-title">分类</h2>
          <div class="filter-list">
            <button
              v-for="tag in artTags"
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
.gallery-page {
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

.gallery-container {
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

.artwork-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  align-items: start;
  gap: 28px;
}

.artwork-card {
  margin: 0;
  padding: 14px 14px 18px;
  border-radius: 24px;
  overflow: hidden;
  transition: transform 0.25s ease;
}

.artwork-card:hover {
  transform: translateY(-5px);
}

.artwork-cover {
  height: 220px;
  border-radius: 17px;
  box-shadow: 0 14px 30px rgba(91, 154, 110, 0.24);
  transition: transform 0.4s ease;
}

.artwork-card:hover .artwork-cover {
  transform: scale(1.02);
}

.artwork-card figcaption {
  padding: 16px 6px 0;
}

.artwork-card h2 {
  margin: 0 0 6px;
  color: #2f5c3d;
  font-size: 18px;
}

.artwork-card p {
  margin: 0 0 8px;
  color: rgba(60, 104, 76, 0.72);
  font-size: 13px;
}

.artwork-card time {
  color: rgba(60, 104, 76, 0.55);
  font-size: 12px;
}

.artwork-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.artwork-category {
  flex-shrink: 0;
  padding: 3px 10px;
  border-radius: 999px;
  color: #2f7d4a;
  font-size: 11px;
  background: rgba(186, 226, 197, 0.65);
}

/* 让封面高度略有变化，版面更松弛一些 */
.artwork-card:nth-child(3n + 2) .artwork-cover {
  height: 250px;
}

.artwork-card:nth-child(3n) .artwork-cover {
  height: 200px;
}

.gallery-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  align-items: start;
  gap: 28px;
  margin-top: 30px;
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

.empty-state {
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

@media (max-width: 860px) {
  .gallery-layout {
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
