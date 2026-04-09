import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', affix: true }
      },
      // 藏品管理
      {
        path: 'collection/info',
        name: 'CollectionInfo',
        component: () => import('../views/collection/info/index.vue'),
        meta: { title: '藏品档案', icon: 'Document', parent: '藏品管理' }
      },
      {
        path: 'collection/category',
        name: 'CollectionCategory',
        component: () => import('../views/collection/category/index.vue'),
        meta: { title: '藏品分类', icon: 'FolderOpened', parent: '藏品管理' }
      },
      {
        path: 'collection/borrow',
        name: 'CollectionBorrow',
        component: () => import('../views/collection/borrow/index.vue'),
        meta: { title: '藏品借出', icon: 'Goods', parent: '藏品管理' }
      },
      // 库房管理
      {
        path: 'storage/info',
        name: 'StorageInfo',
        component: () => import('../views/storage/info/index.vue'),
        meta: { title: '库房信息', icon: 'House', parent: '库房管理' }
      },
      {
        path: 'storage/location',
        name: 'StorageLocation',
        component: () => import('../views/storage/location/index.vue'),
        meta: { title: '库位管理', icon: 'Grid', parent: '库房管理' }
      },
      // 入出库管理
      {
        path: 'inout/inbound',
        name: 'Inbound',
        component: () => import('../views/inout/inbound/index.vue'),
        meta: { title: '入库登记', icon: 'Bottom', parent: '入出库管理' }
      },
      {
        path: 'inout/outbound',
        name: 'Outbound',
        component: () => import('../views/inout/outbound/index.vue'),
        meta: { title: '出库申请', icon: 'Top', parent: '入出库管理' }
      },
      {
        path: 'inout/approve',
        name: 'InOutApprove',
        component: () => import('../views/inout/approve/index.vue'),
        meta: { title: '出库审批', icon: 'Select', parent: '入出库管理' }
      },
      // 盘点管理
      {
        path: 'inventory/task',
        name: 'InventoryTask',
        component: () => import('../views/inventory/task/index.vue'),
        meta: { title: '盘点任务', icon: 'List', parent: '盘点管理' }
      },
      {
        path: 'inventory/record',
        name: 'InventoryRecord',
        component: () => import('../views/inventory/record/index.vue'),
        meta: { title: '盘点记录', icon: 'Finished', parent: '盘点管理' }
      },
      // 修复管理
      {
        path: 'repair/order',
        name: 'RepairOrder',
        component: () => import('../views/repair/order/index.vue'),
        meta: { title: '修复工单', icon: 'Tickets', parent: '修复管理' }
      },
      // 外借管理
      {
        path: 'loan/apply',
        name: 'LoanApply',
        component: () => import('../views/loan/apply/index.vue'),
        meta: { title: '外借申请', icon: 'DocumentAdd', parent: '外借管理' }
      },
      // 环境监测
      {
        path: 'environment/data',
        name: 'EnvironmentData',
        component: () => import('../views/environment/data/index.vue'),
        meta: { title: '环境数据', icon: 'DataLine', parent: '环境监测' }
      },
      // RFID管理
      {
        path: 'rfid/tag',
        name: 'RfidTag',
        component: () => import('../views/rfid/tag/index.vue'),
        meta: { title: '标签管理', icon: 'PriceTag', parent: 'RFID管理' }
      },
      {
        path: 'rfid/device',
        name: 'RfidDevice',
        component: () => import('../views/rfid/device/index.vue'),
        meta: { title: '设备管理', icon: 'Monitor', parent: 'RFID管理' }
      },
      {
        path: 'rfid/inventory',
        name: 'RfidInventory',
        component: () => import('../views/rfid/inventory/index.vue'),
        meta: { title: '标签盘点', icon: 'Search', parent: 'RFID管理' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true }
      },
      // 系统管理
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('../views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', parent: '系统管理' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('../views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', parent: '系统管理' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('../views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu', parent: '系统管理' }
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('../views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding', parent: '系统管理' }
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('../views/system/operlog/index.vue'),
        meta: { title: '操作日志', icon: 'Notebook', parent: '系统管理' }
      },
      {
        path: 'system/backup',
        name: 'SystemBackup',
        component: () => import('../views/system/backup/index.vue'),
        meta: { title: '数据备份', icon: 'Backup', parent: '系统管理' }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '博物馆藏品管理系统'

  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
