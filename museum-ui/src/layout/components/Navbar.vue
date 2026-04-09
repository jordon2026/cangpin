<template>
  <div class="navbar">
    <div class="navbar-left">
      <!-- 折叠按钮 -->
      <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
        <Expand v-if="appStore.sidebarCollapsed" />
        <Fold v-else />
      </el-icon>
      <!-- 面包屑 -->
      <Breadcrumb />
    </div>
    <div class="navbar-right">
      <!-- 消息 -->
      <el-tooltip content="消息" placement="bottom">
        <el-icon class="navbar-icon">
          <Bell />
        </el-icon>
      </el-tooltip>
      <!-- 全屏 -->
      <el-tooltip content="全屏" placement="bottom">
        <el-icon class="navbar-icon" @click="toggleFullscreen">
          <FullScreen />
        </el-icon>
      </el-tooltip>
      <!-- 用户下拉 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" src="" class="user-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="user-name">{{ userStore.realName || userStore.username || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { ElMessageBox } from 'element-plus'
import Breadcrumb from './Breadcrumb.vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 全屏切换
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 用户下拉菜单操作
function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'password':
      // TODO: 打开修改密码弹窗
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.navbar {
  height: var(--color-navbar-height);
  background-color: var(--color-navbar-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  position: relative;
  z-index: 10;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--color-text-light);
  transition: color var(--transition-fast);
  padding: 4px;
}

.collapse-btn:hover {
  color: var(--color-primary);
}

.navbar-icon {
  font-size: 18px;
  cursor: pointer;
  color: var(--color-text-light);
  padding: 6px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.navbar-icon:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-bg);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background-color var(--transition-fast);
  margin-left: 8px;
}

.user-info:hover {
  background-color: var(--color-primary-bg);
}

.user-avatar {
  background-color: var(--color-primary);
  color: #fff;
}

.user-name {
  font-size: 14px;
  color: var(--color-text);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
