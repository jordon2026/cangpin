import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const tagsList = ref([
    { path: '/dashboard', title: '首页', closable: false }
  ])
  const cachedViews = ref(['Dashboard'])

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function addTag(tag) {
    if (!tagsList.value.find(t => t.path === tag.path)) {
      tagsList.value.push({ ...tag, closable: true })
      if (tag.name && !cachedViews.value.includes(tag.name)) {
        cachedViews.value.push(tag.name)
      }
    }
  }

  function removeTag(path) {
    const index = tagsList.value.findIndex(t => t.path === path)
    if (index > -1 && tagsList.value[index].closable) {
      tagsList.value.splice(index, 1)
    }
  }

  function clearOtherTags(path) {
    tagsList.value = tagsList.value.filter(t => !t.closable || t.path === path)
  }

  return {
    sidebarCollapsed,
    tagsList,
    cachedViews,
    toggleSidebar,
    addTag,
    removeTag,
    clearOtherTags
  }
})
