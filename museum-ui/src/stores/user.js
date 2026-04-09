import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo, getCaptcha as getCaptchaApi, getMenus as getMenusApi } from '../api/auth'
import router from '../router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const menus = ref([])

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.realName || userInfo.value?.username || '')

  // 获取验证码
  async function getCaptcha() {
    const res = await getCaptchaApi()
    return res.data
  }

  // 登录
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const data = res.data
    token.value = data.token
    userInfo.value = {
      id: data.id,
      username: data.username,
      realName: data.realName,
      avatar: data.avatar
    }
    roles.value = data.roles || []
    permissions.value = data.permissions || []

    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))

    // 登录后获取菜单
    await fetchMenus()

    return data
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfo()
    const data = res.data
    userInfo.value = {
      id: data.id,
      username: data.username,
      realName: data.realName,
      avatar: data.avatar
    }
    roles.value = data.roles || []
    permissions.value = data.permissions || []

    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))

    return data
  }

  // 获取用户菜单
  async function fetchMenus() {
    try {
      const res = await getMenusApi()
      menus.value = res.data || []
    } catch (e) {
      console.error('获取菜单失败:', e)
      menus.value = []
    }
  }

  // 退出登录
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // 忽略退出登录的错误
    }
    resetState()
    router.push('/login')
  }

  // 重置状态
  function resetState() {
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    menus.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    menus,
    isLoggedIn,
    username,
    getCaptcha,
    login,
    fetchUserInfo,
    fetchMenus,
    logout,
    resetState
  }
})
