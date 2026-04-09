<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
            <rect x="2" y="14" width="28" height="16" rx="2" fill="#C9A96E"/>
            <rect x="5" y="6" width="22" height="11" rx="1.5" fill="#D4B896"/>
            <rect x="8" y="3" width="16" height="6" rx="1" fill="#FFE0B2"/>
            <rect x="9" y="20" width="5" height="8" rx="0.5" fill="#F5F0EB"/>
            <rect x="18" y="20" width="5" height="8" rx="0.5" fill="#F5F0EB"/>
          </svg>
        </div>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">藏品管理系统</span>
      </div>

      <!-- 菜单 -->
      <el-scrollbar class="sidebar-menu-wrap">
        <el-menu
          :default-active="currentRoute"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#D7CCC8"
          active-text-color="#FFD54F"
        >
          <!-- 首页 -->
          <el-menu-item index="/dashboard" @click="router.push('/dashboard')">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <!-- 各分组菜单 -->
          <el-sub-menu v-for="menu in menuList.filter(m => m.children)" :key="menu.path" :index="menu.path">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.path"
              :index="child.path"
              @click="router.push(child.path)"
            >
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </div>

    <!-- 右侧主区域 -->
    <div class="main-area">
      <!-- 顶栏 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-icon class="toggle-btn" @click="appStore.toggleSidebar">
            <Expand v-if="appStore.sidebarCollapsed" />
            <Fold v-else />
          </el-icon>
          <Breadcrumb />
        </div>
        <div class="navbar-right">
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="navbar-icon" @click="toggleFullscreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="30" class="user-avatar" :src="topAvatarUrl">
                <template v-if="!topAvatarUrl">{{ userStore.username?.charAt(0) || '管' }}</template>
              </el-avatar>
              <span class="user-name">{{ userStore.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 标签页 -->
      <TagsView />

      <!-- 内容区 -->
      <div class="main-content">
        <router-view />
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" destroy-on-close>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码（6-20位）" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handleSubmitPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, watch, ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import Breadcrumb from './components/Breadcrumb.vue'
import TagsView from './components/TagsView.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)
const topAvatarUrl = ref(localStorage.getItem('userAvatar') || '')

// 图标映射（Element Plus 图标名称）
const iconMap = {
  'setting': 'Setting',
  'user': 'User',
  'peoples': 'UserFilled',
  'tree-table': 'Grid',
  'tree': 'OfficeBuilding',
  'log': 'Notebook',
  'backup': 'Box',
  'box': 'Box',
  'nested': 'FolderOpened',
  'documentation': 'Document',
  'shopping': 'ShoppingCart',
  'eye-open': 'View',
  'clipboard': 'Tickets',
  'cpu': 'Connection',
  'chart': 'DataLine',
  'monitor': 'Monitor',
  'HomeFilled': 'HomeFilled',
  'Collection': 'Collection',
  'Document': 'Document',
  'FolderOpened': 'FolderOpened',
  'OfficeBuilding': 'OfficeBuilding',
  'House': 'House',
  'Grid': 'Grid',
  'Sort': 'Sort',
  'Tickets': 'Tickets',
  'List': 'List',
  'Finished': 'Finished',
  'SetUp': 'Setting',
  'Promotion': 'Promotion',
  'DocumentAdd': 'DocumentAdd',
  'Sunny': 'Sunny',
  'DataLine': 'DataLine',
  'Connection': 'Connection',
  'PriceTag': 'PriceTag',
  'Monitor': 'Monitor',
  'User': 'User',
  'UserFilled': 'UserFilled',
  'Menu': 'Menu',
  'Notebook': 'Notebook',
  'Box': 'Box'
}

// 将后端菜单数据转换为前端格式
const menuList = computed(() => {
  // 调试：检查菜单数据
  console.log('userStore.menus:', userStore.menus)
  
  if (!userStore.menus || userStore.menus.length === 0) {
    console.warn('菜单数据为空，需要从后端获取')
    return []
  }
  
  // 获取所有顶级菜单（type=1）
  const topMenus = userStore.menus.filter(m => m.parentId === 0 && m.type === 1)
  console.log('顶级菜单数量:', topMenus.length, topMenus)
  
  return topMenus.map(menu => {
    // 获取子菜单
    const children = userStore.menus
      .filter(m => m.parentId === menu.id && m.type === 2 && m.status === 1)
      .map(child => ({
        path: child.path || '/' + child.name,
        title: child.name,
        icon: iconMap[child.icon] || 'Document',
        permission: child.permission
      }))
    console.log(`菜单[${menu.name}]的子菜单数量:`, children.length)
    
    // 顶级菜单路径处理
    const menuPath = menu.path || menu.name
    const fullPath = menuPath.startsWith('/') ? menuPath : '/' + menuPath
    
    return {
      path: fullPath,
      title: menu.name,
      icon: iconMap[menu.icon] || 'Setting',
      children: children.length > 0 ? children : undefined
    }
  })
})

// 初始化时获取菜单
onMounted(async () => {
  console.log('MainLayout mounted, token:', !!userStore.token, 'menus:', userStore.menus.length)
  if (userStore.token && userStore.menus.length === 0) {
    console.log('需要获取菜单...')
    await userStore.fetchMenus()
  }
})

// 监听 token 变化
watch(() => userStore.token, async (newToken) => {
  if (newToken && userStore.menus.length === 0) {
    console.log('token 变化，重新获取菜单...')
    await userStore.fetchMenus()
  }
})

// 监听路由变化，添加标签
watch(() => route.path, () => {
  if (route.meta?.title && route.path !== '/login') {
    appStore.addTag({
      path: route.path,
      title: route.meta.title,
      name: route.name
    })
  }
}, { immediate: true })

// 全屏切换
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 用户下拉菜单
const pwdDialogVisible = ref(false)
const pwdFormRef = ref(null)
const pwdLoading = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function handleUserCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'password') {
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdDialogVisible.value = true
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
    })
  }
}

async function handleSubmitPwd() {
  try { await pwdFormRef.value.validate() } catch { return }
  pwdLoading.value = true
  // 模拟修改密码
  setTimeout(() => {
    pwdLoading.value = false
    pwdDialogVisible.value = false
    ElMessage.success('密码修改成功')
  }, 500)
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  background: var(--color-sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0,0,0,0.15);
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  flex-shrink: 0;
}

.logo-icon {
  flex-shrink: 0;
}

.logo-icon svg {
  width: 32px;
  height: 32px;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: #D4B896;
  white-space: nowrap;
  letter-spacing: 2px;
}

.sidebar-menu-wrap {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu-wrap :deep(.el-menu) {
  border-right: none;
}

.sidebar-menu-wrap :deep(.el-menu-item),
.sidebar-menu-wrap :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.sidebar-menu-wrap :deep(.el-menu-item:hover),
.sidebar-menu-wrap :deep(.el-sub-menu__title:hover) {
  background-color: var(--color-sidebar-hover-bg) !important;
}

.sidebar-menu-wrap :deep(.el-menu-item.is-active) {
  background-color: rgba(139, 69, 19, 0.3) !important;
  color: #FFD54F !important;
}

.sidebar-menu-wrap :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  min-width: auto;
}

/* 右侧主区域 */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* 顶栏 */
.navbar {
  height: var(--navbar-height);
  background: var(--color-navbar-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid var(--color-navbar-border);
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
  z-index: 10;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--color-text-regular);
  transition: color 0.2s;
}

.toggle-btn:hover {
  color: var(--color-primary);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.navbar-icon {
  font-size: 18px;
  cursor: pointer;
  color: var(--color-text-light);
  transition: color 0.2s;
}

.navbar-icon:hover {
  color: var(--color-primary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-info:hover {
  background: var(--color-primary-bg);
}

.user-avatar {
  background: linear-gradient(135deg, #8B4513, #C9A96E);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: var(--color-text-regular);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 内容区 */
.main-content {
  flex: 1;
  padding: 16px;
  background: var(--color-bg-page);
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
