/**
 * 应用入口文件
 * 初始化 Vue 实例，注册全局插件
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'

// 根组件
import App from './App.vue'

// 样式导入（顺序重要）
import '@/styles/variables.css'
import '@/styles/global.css'
import '@/styles/element-override.css'

// 路由
import router from '@/router'

// 应用实例
const app = createApp(App)

// Pinia 状态管理
const pinia = createPinia()
app.use(pinia)

// Element Plus（中文语言包）
app.use(ElementPlus, {
  locale: zhCn
})

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 路由
app.use(router)

// 挂载应用
app.mount('#app')
