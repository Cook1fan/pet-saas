<template>
  <div class="login-container">
    <div class="paw-decoration paw-1">🐾</div>
    <div class="paw-decoration paw-2">🐾</div>
    <div class="paw-decoration paw-3">🐾</div>
    <div class="paw-decoration paw-4">🐾</div>

    <div class="login-wrapper">
      <div class="login-illustration">
        <div class="pet-card">
          <div class="pet-avatar">
            <span class="pet-icon">🐕</span>
          </div>
          <div class="pet-avatar pet-avatar-2">
            <span class="pet-icon">🐈</span>
          </div>
        </div>
        <div class="welcome-text">
          <h2>欢迎回来</h2>
          <p>继续管理您的萌宠小店</p>
        </div>
        <div class="decorative-bones">
          <span class="bone">🦴</span>
          <span class="bone bone-2">🦴</span>
        </div>
      </div>

      <div class="login-box">
        <div class="login-header">
          <div class="logo-section">
            <span class="logo-icon">🐾</span>
            <h1>宠物门店 SaaS</h1>
          </div>
          <p>门店管理端</p>
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
              <span class="btn-paw">🐾</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer" v-if="isDev">
          <router-link to="/platform/login">
            <span class="link-icon">🏠</span>
            平台管理端登录
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
const isDev = import.meta.env.DEV

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
        await userStore.loginShop(form.username, form.password)
        ElMessage.success('登录成功')
        router.push('/shop/dashboard')
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
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 50%, #ffeaa7 100%);
  position: relative;
  overflow: hidden;
}

.paw-decoration {
  position: absolute;
  font-size: 48px;
  opacity: 0.15;
  animation: float 6s ease-in-out infinite;

  &.paw-1 {
    top: 10%;
    left: 5%;
    animation-delay: 0s;
  }

  &.paw-2 {
    top: 20%;
    right: 10%;
    animation-delay: 1.5s;
  }

  &.paw-3 {
    bottom: 15%;
    left: 15%;
    animation-delay: 3s;
  }

  &.paw-4 {
    bottom: 25%;
    right: 5%;
    animation-delay: 4.5s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}

.login-wrapper {
  display: flex;
  width: 900px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(252, 182, 159, 0.4);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.login-illustration {
  flex: 1;
  background: linear-gradient(180deg, #fff5e6 0%, #ffe4cc 100%);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.pet-card {
  position: relative;
  margin-bottom: 32px;
}

.pet-avatar {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #ffd89b 0%, #fcb69f 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(252, 182, 159, 0.4);

  &.pet-avatar-2 {
    position: absolute;
    width: 80px;
    height: 80px;
    right: -50px;
    top: 20px;
    background: linear-gradient(135deg, #a8e6cf 0%, #88d8b0 100%);
    box-shadow: 0 8px 20px rgba(136, 216, 176, 0.4);
  }
}

.pet-icon {
  font-size: 60px;

  .pet-avatar-2 & {
    font-size: 40px;
  }
}

.welcome-text {
  text-align: center;

  h2 {
    font-size: 28px;
    color: #5d4e37;
    margin-bottom: 8px;
    font-weight: 600;
  }

  p {
    font-size: 16px;
    color: #8b7355;
  }
}

.decorative-bones {
  position: absolute;
  bottom: 40px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 60px;
  opacity: 0.3;
}

.bone {
  font-size: 32px;
  animation: wobble 3s ease-in-out infinite;

  &.bone-2 {
    animation-delay: 1.5s;
  }
}

@keyframes wobble {
  0%, 100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(5deg);
  }
}

.login-box {
  flex: 0 0 400px;
  padding: 50px 40px;
  background: #fff;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-icon {
  font-size: 32px;
}

.login-header h1 {
  font-size: 22px;
  color: #5d4e37;
  margin: 0;
  font-weight: 600;
}

.login-header p {
  font-size: 14px;
  color: #a89880;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;

  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b35 100%);
  border: none;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 107, 53, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.btn-paw {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.login-btn:hover .btn-paw {
  transform: rotate(15deg);
}

.login-footer {
  text-align: center;

  a {
    color: #ff6b35;
    font-size: 14px;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: color 0.3s ease;

    &:hover {
      color: #ff9a56;
      text-decoration: underline;
    }
  }
}

.link-icon {
  font-size: 14px;
}

@media (max-width: 900px) {
  .login-wrapper {
    width: 95%;
    max-width: 480px;
  }

  .login-illustration {
    display: none;
  }

  .login-box {
    flex: 1;
  }
}
</style>
