<template>
  <div class="login-container">
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>
    <div class="bg-circle circle-3"></div>

    <div class="login-wrapper">
      <div class="login-brand">
        <div class="brand-logo">
          <span class="logo-main">🐾</span>
          <div class="logo-accent"></div>
        </div>
        <h1 class="brand-title">宠物门店 SaaS</h1>
        <p class="brand-subtitle">平台管理端</p>
        <div class="brand-stats">
          <div class="stat-item">
            <span class="stat-icon">🏪</span>
            <span class="stat-label">多租户管理</span>
          </div>
          <div class="stat-item">
            <span class="stat-icon">📊</span>
            <span class="stat-label">数据看板</span>
          </div>
          <div class="stat-item">
            <span class="stat-icon">⚙️</span>
            <span class="stat-label">系统配置</span>
          </div>
        </div>
      </div>

      <div class="login-box">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的平台管理账号</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入账号"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="btn-text">登录</span>
              <span class="btn-arrow">→</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <router-link to="/shop/login">
            <span class="link-icon">🏠</span>
            门店管理端登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('role')
        localStorage.removeItem('tenantId')
        await userStore.loginPlatform(form.username, form.password)
        ElMessage.success('登录成功')
        router.push('/platform/dashboard')
      } catch (e) {
        ElMessage.error('登录失败，请检查账号密码')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  position: relative;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;

  &.circle-1 {
    width: 600px;
    height: 600px;
    background: linear-gradient(135deg, #38ef7d 0%, #11998e 100%);
    top: -200px;
    left: -200px;
  }

  &.circle-2 {
    width: 400px;
    height: 400px;
    background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
    bottom: -100px;
    right: -100px;
  }

  &.circle-3 {
    width: 200px;
    height: 200px;
    background: linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%);
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.login-wrapper {
  display: flex;
  width: 950px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

.login-brand {
  flex: 1;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.brand-logo {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #38ef7d 0%, #11998e 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  box-shadow: 0 10px 30px rgba(17, 153, 142, 0.4);
  position: relative;
}

.logo-main {
  font-size: 48px;
  position: relative;
  z-index: 2;
}

.logo-accent {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 24px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  opacity: 0;
  animation: logoPulse 3s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% {
    opacity: 0;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.1);
  }
}

.brand-title {
  font-size: 28px;
  color: #f8fafc;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.brand-subtitle {
  font-size: 16px;
  color: #94a3b8;
  margin: 0 0 40px 0;
}

.brand-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #cbd5e1;
  font-size: 14px;
}

.stat-icon {
  font-size: 20px;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-label {
  font-weight: 500;
}

.login-box {
  flex: 0 0 420px;
  padding: 60px 50px;
  background: #fff;
}

.login-header {
  margin-bottom: 40px;

  h2 {
    font-size: 26px;
    color: #1e293b;
    margin: 0 0 8px 0;
    font-weight: 600;
  }

  p {
    font-size: 14px;
    color: #64748b;
    margin: 0;
  }
}

.login-form {
  margin-bottom: 24px;

  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 15px rgba(17, 153, 142, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(17, 153, 142, 0.4);
  }
}

.btn-arrow {
  transition: transform 0.3s ease;
  font-size: 18px;
}

.login-btn:hover .btn-arrow {
  transform: translateX(4px);
}

.login-footer {
  text-align: center;

  a {
    color: #11998e;
    font-size: 14px;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: color 0.3s ease;

    &:hover {
      color: #38ef7d;
      text-decoration: underline;
    }
  }
}

.link-icon {
  font-size: 14px;
}

@media (max-width: 950px) {
  .login-wrapper {
    width: 95%;
    max-width: 480px;
  }

  .login-brand {
    display: none;
  }

  .login-box {
    flex: 1;
  }
}
</style>
