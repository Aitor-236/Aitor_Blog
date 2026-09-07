<script setup lang="ts">
// TODO: 个人简介页面：后端资料接口完成后，将静态内容替换为接口数据
</script>

<template>
  <div class="about-page">
    <div class="glass-card placeholder-card">
      <span class="placeholder-icon" aria-hidden="true">👤</span>
      <h1>个人简介</h1>
      <p>这里将展示我的经历与联系方式，敬请期待。</p>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 60px 20px 150px;
  background:
    radial-gradient(1100px 600px at 15% 10%, rgba(255, 255, 255, 0.7), transparent 60%),
    linear-gradient(135deg, #eaf7ec 0%, #ddf2e2 45%, #e9f6ec 100%);
}

.glass-card {
  padding: 60px 50px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.38);
  box-shadow:
    0 18px 50px rgba(91, 154, 110, 0.16),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
  text-align: center;
}

.placeholder-icon {
  display: block;
  margin-bottom: 16px;
  font-size: 44px;
}

.placeholder-card h1 {
  margin: 0 0 10px;
  color: #2f5c3d;
}

.placeholder-card p {
  margin: 0;
  color: rgba(60, 104, 76, 0.72);
}
</style>
