<template>
  <div class="tags-view" v-if="appStore.tagsList.length > 0">
    <el-scrollbar class="tags-scroll">
      <div class="tags-list">
        <router-link
          v-for="tag in appStore.tagsList"
          :key="tag.path"
          :to="tag.path"
          class="tag-item"
          :class="{ active: isActive(tag) }"
          @contextmenu.prevent="openContextMenu($event, tag)"
        >
          <span class="tag-dot" v-if="isActive(tag)"></span>
          {{ tag.title }}
          <el-icon
            v-if="tag.closable"
            class="tag-close"
            @click.prevent.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '../../stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

function isActive(tag) {
  return tag.path === route.path
}

function closeTag(tag) {
  appStore.removeTag(tag.path)
  if (tag.path === route.path) {
    const lastTag = appStore.tagsList[appStore.tagsList.length - 1]
    router.push(lastTag.path)
  }
}

function openContextMenu(e, tag) {
  // 简化：右键不做操作
}
</script>

<style scoped>
.tags-view {
  height: 34px;
  background: #fff;
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.tags-scroll {
  flex: 1;
}

.tags-list {
  display: flex;
  gap: 4px;
  padding: 0 8px;
  white-space: nowrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 12px;
  border: 1px solid var(--color-border-light);
  border-radius: 4px;
  color: var(--color-text-regular);
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
}

.tag-item:hover {
  color: var(--color-primary);
  border-color: var(--color-border);
}

.tag-item.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

.tag-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #FFD54F;
}

.tag-close {
  font-size: 12px;
  border-radius: 50%;
  transition: background 0.2s;
  padding: 1px;
}

.tag-close:hover {
  background: rgba(0,0,0,0.15);
}
</style>
