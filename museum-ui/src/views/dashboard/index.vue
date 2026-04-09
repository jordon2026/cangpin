<template>
  <div class="dashboard">
    <!-- 欢迎语 -->
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎回来，{{ userStore.username }}！</h1>
      <p class="welcome-desc">今天是 {{ currentDate }}，祝您工作愉快。</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="item in statList" :key="item.title">
        <div class="stat-icon-wrap" :style="{ background: item.bgColor }">
          <el-icon :size="24" :color="item.iconColor"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-title">{{ item.title }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-section">
      <h3 class="section-title">快捷操作</h3>
      <div class="quick-grid">
        <div
          v-for="item in quickActions"
          :key="item.title"
          class="quick-item"
          @click="item.action"
        >
          <el-icon :size="28" :color="item.color"><component :is="item.icon" /></el-icon>
          <span class="quick-label">{{ item.title }}</span>
        </div>
      </div>
    </div>

    <!-- 系统信息 -->
    <div class="info-section">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="never" class="info-card">
            <template #header>
              <span class="card-title">系统公告</span>
            </template>
            <div class="notice-list">
              <div class="notice-item" v-for="(notice, i) in notices" :key="i">
                <el-tag :type="notice.type" size="small">{{ notice.tag }}</el-tag>
                <span class="notice-text">{{ notice.text }}</span>
                <span class="notice-date">{{ notice.date }}</span>
              </div>
              <el-empty v-if="!notices.length" description="暂无公告" :image-size="60" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="info-card">
            <template #header>
              <span class="card-title">最近操作</span>
            </template>
            <div class="notice-list">
              <div class="notice-item" v-for="(log, i) in recentLogs" :key="i">
                <span class="log-dot" :style="{ background: log.color }"></span>
                <span class="notice-text">{{ log.text }}</span>
                <span class="notice-date">{{ log.time }}</span>
              </div>
              <el-empty v-if="!recentLogs.length" description="暂无记录" :image-size="60" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const currentDate = computed(() => {
  const d = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekDays[d.getDay()]}`
})

const statList = ref([
  { title: '藏品总数', value: '1,286', icon: 'Files', bgColor: 'rgba(139,69,19,0.1)', iconColor: '#8B4513' },
  { title: '在库数量', value: '1,058', icon: 'Box', bgColor: 'rgba(76,175,80,0.1)', iconColor: '#4CAF50' },
  { title: '外借数量', value: '128', icon: 'Promotion', bgColor: 'rgba(255,152,0,0.1)', iconColor: '#FF9800' },
  { title: '待处理工单', value: '7', icon: 'Bell', bgColor: 'rgba(194,53,49,0.1)', iconColor: '#C23531' }
])

const quickActions = ref([
  { title: '藏品登记', icon: 'Plus', color: '#8B4513', action: () => router.push('/collection/info') },
  { title: '入库管理', icon: 'Bottom', color: '#4CAF50', action: () => router.push('/inout/inbound') },
  { title: '出库申请', icon: 'Top', color: '#FF9800', action: () => router.push('/inout/outbound') },
  { title: '发起盘点', icon: 'Tickets', color: '#2196F3', action: () => router.push('/inventory/task') },
  { title: '修复工单', icon: 'SetUp', color: '#9C27B0', action: () => router.push('/repair/order') },
  { title: '标签管理', icon: 'PriceTag', color: '#C9A96E', action: () => router.push('/rfid/tag') }
])

const notices = ref([
  { tag: '系统', type: 'info', text: '系统已升级至 v1.0.0 版本', date: '04-07' },
  { tag: '通知', type: 'warning', text: '定于本周五进行系统维护，届时暂停服务', date: '04-06' },
  { tag: '通知', type: 'success', text: '新藏品入库流程已优化，请查看操作指南', date: '04-05' }
])

const recentLogs = ref([
  { text: '管理员 登录系统', time: '09:17', color: '#4CAF50' },
  { text: '藏品「青铜鼎」入库登记', time: '昨天', color: '#8B4513' },
  { text: '盘点任务「库房A区」已完成', time: '昨天', color: '#2196F3' },
  { text: '用户「张三」修改密码', time: '04-05', color: '#FF9800' }
])
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}

/* 欢迎 */
.welcome-section {
  margin-bottom: 24px;
  animation: fadeInUp 0.4s ease;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 6px;
}

.welcome-desc {
  font-size: 14px;
  color: var(--color-text-light);
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s;
  animation: fadeInUp 0.4s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-card:nth-child(1) { animation-delay: 0s; }
.stat-card:nth-child(2) { animation-delay: 0.05s; }
.stat-card:nth-child(3) { animation-delay: 0.1s; }
.stat-card:nth-child(4) { animation-delay: 0.15s; }

.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
}

.stat-title {
  font-size: 13px;
  color: var(--color-text-light);
  margin-top: 2px;
}

/* 快捷入口 */
.quick-section {
  margin-bottom: 24px;
  animation: fadeInUp 0.5s ease 0.2s both;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 3px solid var(--color-gold);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.quick-item {
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s;
}

.quick-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  background: var(--color-primary-bg);
}

.quick-label {
  font-size: 13px;
  color: var(--color-text-regular);
}

/* 信息区 */
.info-section {
  animation: fadeInUp 0.5s ease 0.3s both;
}

.info-card {
  border-radius: var(--radius-md);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.notice-list {
  display: flex;
  flex-direction: column;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-text {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-regular);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-date {
  font-size: 12px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.log-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@media screen and (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .quick-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }
  .quick-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
