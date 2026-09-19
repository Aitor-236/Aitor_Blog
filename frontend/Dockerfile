# syntax=docker/dockerfile:1
# ============================================================
# Aitor Blog 前端镜像：多阶段构建
#   1. builder  —— Node 里跑 npm ci + npm run build（含 vue-tsc 类型检查）
#   2. runtime  —— Nginx 托管 dist/，并把 /api 反代到后端容器
# 构建（在仓库根目录执行）：
#   docker build -t aitor-blog-frontend ./frontend
# ============================================================

# ---------- 1. 构建阶段 ----------
FROM node:22-alpine AS builder

WORKDIR /build

# 先装依赖：package.json / lock 没变时这层命中缓存
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- 2. 运行阶段 ----------
FROM nginx:alpine AS runtime

# 站点配置（SPA history 回退 + /api 反向代理）
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
