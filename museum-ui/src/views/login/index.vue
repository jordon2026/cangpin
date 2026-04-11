<template>
  <div class="login-container">
    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="login-left-content">
        <div class="login-decor-line"></div>
        <h1 class="login-title">RFID 智能博物馆</h1>
        <h2 class="login-subtitle">藏品管理系统</h2>
        <p class="login-desc">藏品数字化 · 智慧化守护文化遗产</p>
        <div class="login-decor-bottom">
          <div class="login-decor-icon">&#9670;</div>
        </div>
        <p class="login-copyright">聚景科技 出品</p>
      </div>
      <!-- 装饰性图案 -->
      <svg class="login-pattern" viewBox="0 0 400 600" xmlns="http://www.w3.org/2000/svg">
        <!-- 回纹装饰 -->
        <defs>
          <linearGradient id="goldGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#C9A96E;stop-opacity:0.3"/>
            <stop offset="100%" style="stop-color:#8B4513;stop-opacity:0.15"/>
          </linearGradient>
        </defs>
        <!-- 回纹边框 -->
        <rect x="20" y="20" width="360" height="560" rx="8" fill="none" stroke="url(#goldGrad)" stroke-width="1"/>
        <rect x="30" y="30" width="340" height="540" rx="4" fill="none" stroke="url(#goldGrad)" stroke-width="0.5"/>
        <!-- 回纹角饰 - 左上 -->
        <polyline points="20,60 50,60 50,30" fill="none" stroke="#C9A96E" stroke-width="1.5" opacity="0.4"/>
        <polyline points="20,80 70,80 70,30" fill="none" stroke="#C9A96E" stroke-width="1" opacity="0.25"/>
        <!-- 回纹角饰 - 右上 -->
        <polyline points="380,60 350,60 350,30" fill="none" stroke="#C9A96E" stroke-width="1.5" opacity="0.4"/>
        <polyline points="380,80 330,80 330,30" fill="none" stroke="#C9A96E" stroke-width="1" opacity="0.25"/>
        <!-- 回纹角饰 - 左下 -->
        <polyline points="20,540 50,540 50,570" fill="none" stroke="#C9A96E" stroke-width="1.5" opacity="0.4"/>
        <polyline points="20,520 70,520 70,570" fill="none" stroke="#C9A96E" stroke-width="1" opacity="0.25"/>
        <!-- 回纹角饰 - 右下 -->
        <polyline points="380,540 350,540 350,570" fill="none" stroke="#C9A96E" stroke-width="1.5" opacity="0.4"/>
        <polyline points="380,520 330,520 330,570" fill="none" stroke="#C9A96E" stroke-width="1" opacity="0.25"/>
        <!-- 云纹装饰 -->
        <path d="M100,400 Q120,380 140,400 Q160,420 180,400 Q190,390 200,400" fill="none" stroke="#C9A96E" stroke-width="0.8" opacity="0.2"/>
        <path d="M200,420 Q220,400 240,420 Q260,440 280,420 Q290,410 300,420" fill="none" stroke="#C9A96E" stroke-width="0.8" opacity="0.15"/>
        <!-- 中心菱形装饰 -->
        <polygon points="200,250 220,270 200,290 180,270" fill="none" stroke="#C9A96E" stroke-width="1" opacity="0.2"/>
        <polygon points="200,240 230,270 200,300 170,270" fill="none" stroke="#C9A96E" stroke-width="0.5" opacity="0.12"/>
      </svg>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <!-- Logo 和标题 -->
        <div class="login-logo-area">
          <svg class="login-logo-icon" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="20" width="40" height="24" rx="3" fill="#8B4513" opacity="0.9"/>
            <rect x="8" y="8" width="32" height="16" rx="2" fill="#C9A96E" opacity="0.8"/>
            <rect x="12" y="4" width="24" height="8" rx="1.5" fill="#D4B896" opacity="0.6"/>
            <rect x="14" y="28" width="8" height="12" rx="1" fill="#F5F0EB" opacity="0.7"/>
            <rect x="26" y="28" width="8" height="12" rx="1" fill="#F5F0EB" opacity="0.7"/>
            <rect x="18" y="30" width="12" height="8" rx="0.5" fill="#D4B896" opacity="0.4"/>
          </svg>
          <h3 class="login-logo-title">博物馆藏品管理系统</h3>
          <p class="login-logo-subtitle">Museum Collection Management System</p>
        </div>

        <!-- 表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              clearable
            >
              <template #suffix>
                <el-icon class="password-toggle" @click="showPassword = !showPassword">
                  <View v-if="showPassword" />
                  <Hide v-else />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captchaCode"
                placeholder="请输入验证码"
                prefix-icon="Key"
                size="large"
                clearable
                class="captcha-input"
              />
              <div class="captcha-img" @click="refreshCaptcha" :title="captchaError ? '点击重新加载验证码' : '点击刷新验证码'">
                <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
                <div v-else-if="captchaLoading" class="captcha-placeholder">
                  <el-icon class="captcha-spin"><Loading /></el-icon>
                  <span>加载中</span>
                </div>
                <div v-else class="captcha-placeholder captcha-error">
                  <el-icon><RefreshRight /></el-icon>
                  <span>点击重试</span>
                </div>
              </div>
            </div>
          </el-form-item>

          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>默认账号：admin / admin123</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { Loading, RefreshRight } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const rememberMe = ref(false)
const captchaImg = ref('')
const captchaUuid = ref('')
const captchaLoading = ref(false)
const captchaError = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  uuid: '',
  captchaCode: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 获取验证码
async function refreshCaptcha() {
  if (captchaLoading.value) return
  captchaLoading.value = true
  captchaError.value = false
  captchaImg.value = ''
  try {
    const data = await userStore.getCaptcha()
    captchaImg.value = data.img
    captchaUuid.value = data.uuid
    loginForm.uuid = data.uuid
    loginForm.captchaCode = ''
  } catch (e) {
    captchaError.value = true
    ElMessage.warning('验证码加载失败，请点击图片区域重试')
  } finally {
    captchaLoading.value = false
  }
}

// 登录
async function handleLogin() {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (e) {
      // 登录失败时刷新验证码
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  refreshCaptcha()
  // 读取记住的用户名
  const saved = localStorage.getItem('remembered_username')
  if (saved) {
    loginForm.username = saved
    rememberMe.value = true
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* 左侧装饰 */
.login-left {
  flex: 0 0 45%;
  background: linear-gradient(135deg, #3E2723 0%, #5D4037 50%, #4E342E 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-left-content {
  text-align: center;
  color: #D7CCC8;
  z-index: 2;
  animation: fadeInUp 0.8s ease;
}

.login-decor-line {
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #C9A96E, transparent);
  margin: 0 auto 30px;
}

.login-title {
  font-size: 36px;
  font-weight: 700;
  color: #FFD54F;
  letter-spacing: 6px;
  margin-bottom: 12px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.login-subtitle {
  font-size: 24px;
  font-weight: 400;
  color: #D4B896;
  letter-spacing: 10px;
  margin-bottom: 20px;
}

.login-desc {
  font-size: 15px;
  color: #BCAAA4;
  letter-spacing: 3px;
  margin-top: 10px;
}

.login-decor-bottom {
  margin: 40px auto 0;
}

.login-decor-icon {
  width: 12px;
  height: 12px;
  background: #C9A96E;
  transform: rotate(45deg);
  opacity: 0.6;
  margin: 0 auto;
}

.login-copyright {
  font-size: 12px;
  color: #8D6E63;
  margin-top: 60px;
  letter-spacing: 2px;
}

.login-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.6;
}

/* 右侧表单 */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #FFFFF0 0%, #F5F0EB 100%);
}

.login-form-wrapper {
  width: 400px;
  padding: 40px;
  animation: fadeInUp 0.6s ease 0.2s both;
}

.login-logo-area {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
}

.login-logo-title {
  font-size: 22px;
  font-weight: 700;
  color: #3E2723;
  letter-spacing: 3px;
  margin-bottom: 6px;
}

.login-logo-subtitle {
  font-size: 12px;
  color: #BCAAA4;
  letter-spacing: 1px;
}

/* 表单样式 */
.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #D7CCC8 inset;
  background: #fff;
  padding: 4px 12px;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #C9A96E inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #8B4513 inset;
}

.login-form :deep(.el-input__prefix .el-icon) {
  color: #8D6E63;
}

.password-toggle {
  cursor: pointer;
  color: #BCAAA4;
}
.password-toggle:hover {
  color: #8B4513;
}

/* 验证码行 */
.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.captcha-img {
  flex: 0 0 130px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #D7CCC8;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FAF6F1;
  transition: border-color 0.2s;
}

.captcha-img:hover {
  border-color: #C9A96E;
}

.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 11px;
  color: #BCAAA4;
  line-height: 1.2;
}

.captcha-placeholder .el-icon {
  font-size: 16px;
}

.captcha-error {
  color: #C23531;
  cursor: pointer;
}

.captcha-error:hover {
  color: #a02020;
}

.captcha-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 登录选项 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-options :deep(.el-checkbox__label) {
  color: #8D6E63;
  font-size: 13px;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 6px;
  border-radius: 8px;
  background: linear-gradient(135deg, #8B4513, #A0522D);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #A0522D, #B8652B);
  box-shadow: 0 4px 16px rgba(139, 69, 19, 0.35);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

/* 底部提示 */
.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 12px;
  color: #BCAAA4;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media screen and (max-width: 900px) {
  .login-left {
    display: none;
  }
  .login-form-wrapper {
    width: 100%;
    max-width: 400px;
    padding: 20px;
  }
}
</style>
