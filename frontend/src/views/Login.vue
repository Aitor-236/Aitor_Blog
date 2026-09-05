<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

interface LoginResult {
  token: string
  username: string
  email: string
}

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const rules: FormRules<typeof loginForm> = {
  account: [{ required: true, message: '请输入邮箱或用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const account = loginForm.account.trim()
    const isEmail = account.includes('@')
    const payload = isEmail
      ? { email: account, password: loginForm.password }
      : { username: account, password: loginForm.password }

    const response = (await request.post('/auth/login', payload)) as unknown as {
      code: number
      message: string
      data: LoginResult
    }

    localStorage.setItem('token', response.data.token)
    localStorage.setItem('username', response.data.username)
    if (response.data.email) {
      localStorage.setItem('email', response.data.email)
    }

    ElMessage.success(`登录成功，欢迎回来，${response.data.username}`)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="blob blob-1" aria-hidden="true"></div>
    <div class="blob blob-2" aria-hidden="true"></div>
    <div class="blob blob-3" aria-hidden="true"></div>

    <main class="login-card">
      <div class="login-header">
        <div class="logo">A</div>
        <h1>登录 Aitor Blog</h1>
        <p>使用邮箱或用户名登录，欢迎回来</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="account">
          <el-input v-model.trim="loginForm.account" placeholder="邮箱 / 用户名" clearable />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-button class="login-button" type="primary" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>

      <p class="login-footer">还没有账号？请联系管理员开通</p>
    </main>
  </div>
</template>

<style>
body {
  margin: 0;
  min-width: 320px;
}
</style>

<style scoped>
.login-page {
  --el-color-primary: #66b87f;
  --el-color-primary-light-3: #8bcd9f;
  --el-color-primary-light-5: #aeddbd;
  --el-color-primary-light-7: #d0ecd8;
  --el-color-primary-light-8: #e2f4e7;
  --el-color-primary-light-9: #f1faf4;
  --el-color-primary-dark-2: #519d68;

  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  overflow: hidden;
  background:
    radial-gradient(1100px 600px at 15% 10%, rgba(255, 255, 255, 0.7), transparent 60%),
    linear-gradient(135deg, #eaf7ec 0%, #ddf2e2 45%, #e9f6ec 100%);
}

.blob {
  position: absolute;
  border-radius: 999px;
  filter: blur(80px);
  opacity: 0.55;
}

.blob-1 {
  top: -140px;
  left: -100px;
  width: 380px;
  height: 380px;
  background: rgba(153, 218, 172, 0.75);
}

.blob-2 {
  right: -120px;
  bottom: -160px;
  width: 440px;
  height: 440px;
  background: rgba(196, 233, 206, 0.8);
}

.blob-3 {
  top: 38%;
  right: 18%;
  width: 220px;
  height: 220px;
  background: rgba(255, 255, 255, 0.9);
}

.login-card {
  position: relative;
  z-index: 1;
  width: min(420px, 100%);
  padding: 40px 38px 32px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.38);
  box-shadow:
    0 18px 50px rgba(91, 154, 110, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(22px) saturate(160%);
  -webkit-backdrop-filter: blur(22px) saturate(160%);
}

.login-header {
  margin-bottom: 28px;
  text-align: center;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  margin: 0 auto 18px;
  border-radius: 18px;
  color: #ffffff;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #6bc487, #3f9f62);
  box-shadow: 0 10px 24px rgba(63, 159, 98, 0.32);
}

.login-header h1 {
  margin: 0 0 8px;
  color: #2f5c3d;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  margin: 0;
  color: rgba(60, 104, 76, 0.72);
  font-size: 14px;
}

.login-card :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-card :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow:
    0 0 0 1px rgba(102, 184, 127, 0.28) inset,
    0 4px 14px rgba(91, 154, 110, 0.08);
}

.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px var(--el-color-primary) inset,
    0 4px 16px rgba(91, 154, 110, 0.16);
}

.login-button {
  width: 100%;
  height: 46px;
  margin-top: 2px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  letter-spacing: 8px;
  box-shadow: 0 10px 24px rgba(63, 159, 98, 0.28);
}

.login-footer {
  margin: 22px 0 0;
  color: rgba(60, 104, 76, 0.6);
  font-size: 13px;
  text-align: center;
}
</style>
