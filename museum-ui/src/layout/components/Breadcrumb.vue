<template>
  <el-breadcrumb separator="/" class="breadcrumb">
    <el-breadcrumb-item
      v-for="item in breadcrumbs"
      :key="item.path"
      :to="item.path"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  const list = [{ path: '/dashboard', title: '首页' }]
  matched.forEach(item => {
    if (item.path !== '/dashboard' && item.meta?.title) {
      list.push({
        path: item.path,
        title: item.meta.title
      })
    }
  })
  return list
})
</script>

<style scoped>
.breadcrumb {
  line-height: var(--navbar-height);
}
</style>
