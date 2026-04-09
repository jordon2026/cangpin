<template>
  <el-aside :width="appStore.sidebarCollapsed ? '64px' : '240px'" class="sidebar">
    <!-- Logo 区域 -->
    <div class="sidebar-logo">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-icon" />
      <span v-show="!appStore.sidebarCollapsed" class="logo-text">藏品管理系统</span>
    </div>

    <!-- 菜单 -->
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="#3E2723"
        text-color="#D7CCC8"
        active-text-color="#C9A96E"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 无子菜单 -->
          <el-menu-item
            v-if="!route.children || route.children.length === 1"
            :index="getMenuIndex(route)"
          >
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>
              <span>{{ route.meta?.title }}</span>
            </template>
          </el-menu-item>

          <!-- 有子菜单 -->
          <el-sub-menu
            v-else
            :index="route.path"
          >
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="`${route.path}/${child.path}`"
            >
              <el-icon v-if="child.meta?.icon">
                <component :is="child.meta.icon" />
              </el-icon>
              <template #title>
                <span>{{ child.meta?.title }}</span>
              </template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import router from '@/router'

const route = useRoute()
const appStore = useAppStore()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 过滤出显示在菜单中的路由
const menuRoutes = computed(() => {
  const layoutRoute = router.options.routes.find(r => r.path === '/')
  if (!layoutRoute) return []
  return layoutRoute.children.filter(child => !child.meta?.hidden)
})

// 获取菜单索引
function getMenuIndex(route) {
  if (route.children && route.children.length === 1) {
    return `${route.path}/${route.children[0].path}`
  }
  return route.path
}
</script>

<style scoped>
.sidebar {
  background-color: var(--color-sidebar-bg);
  transition: width var(--transition-normal);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100vh;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(201, 169, 110, 0.2);
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.logo-text {
  margin-left: 10px;
  font-family: 'Noto Serif SC', '思源宋体', serif;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-gold);
  white-space: nowrap;
  overflow: hidden;
}

/* 菜单样式覆盖 */
.el-menu {
  border-right: none !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: var(--color-sidebar-hover-bg) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--color-sidebar-active-bg) !important;
  color: var(--color-gold) !important;
  position: relative;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 24px;
  background-color: var(--color-gold);
  border-radius: 0 2px 2px 0;
}

:deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  min-width: auto;
}

:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--color-gold) !important;
}
</style>
