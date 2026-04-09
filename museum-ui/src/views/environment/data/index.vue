<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">环境数据</h2>
      <div>
        <el-button @click="handleRefresh" :loading="refreshing">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>
    <el-row :gutter="16" class="sensor-cards">
      <el-col :span="6" v-for="item in sensorList" :key="item.name">
        <el-card shadow="never" class="sensor-card">
          <div class="sensor-header">
            <span class="sensor-name">{{ item.name }}</span>
            <el-tag :type="item.online ? 'success' : 'danger'" size="small">{{ item.online ? '在线' : '离线' }}</el-tag>
          </div>
          <div class="sensor-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="sensor-label">{{ item.label }}</div>
          <div class="sensor-time">更新时间：{{ item.updateTime }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card shadow="never" style="margin-top: 16px; border-radius: var(--radius-md);">
      <template #header>
        <div class="card-header">
          <span class="card-title">历史趋势</span>
          <el-select v-model="selectedStorage" placeholder="选择库房" size="small" style="width: 160px" @change="handleStorageChange">
            <el-option label="A区库房" value="A区库房" />
            <el-option label="B区库房" value="B区库房" />
            <el-option label="C区库房" value="C区库房" />
          </el-select>
        </div>
      </template>
      <div v-if="trendData.length" class="trend-list">
        <div v-for="item in trendData" :key="item.time" class="trend-item">
          <span class="trend-time">{{ item.time }}</span>
          <span class="trend-temp" :class="{ 'text-danger': item.temp > 22 }">{{ item.temp }}℃</span>
          <span class="trend-humidity" :class="{ 'text-danger': item.humidity > 60 }">{{ item.humidity }}%</span>
        </div>
      </div>
      <el-empty v-else description="暂无历史数据" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const refreshing = ref(false)
const selectedStorage = ref('A区库房')

const sensorList = ref([
  { name: 'A区库房-温湿度', label: '温度 / 湿度', value: '20.3℃ / 54.8%', color: '#4CAF50', online: true, updateTime: '2026-04-07 19:20' },
  { name: 'B区库房-温湿度', label: '温度 / 湿度', value: '20.1℃ / 50.2%', color: '#4CAF50', online: true, updateTime: '2026-04-07 19:20' },
  { name: 'C区库房-温湿度', label: '温度 / 湿度', value: '22.5℃ / 58.1%', color: '#FF9800', online: true, updateTime: '2026-04-07 19:20' },
  { name: 'D区库房-温湿度', label: '温度 / 湿度', value: '-- / --', color: '#999', online: false, updateTime: '2026-04-07 15:30' }
])

const trendData = ref([
  { time: '19:00', temp: 20.2, humidity: 54.5 },
  { time: '18:00', temp: 20.3, humidity: 54.8 },
  { time: '17:00', temp: 20.1, humidity: 55.0 },
  { time: '16:00', temp: 20.4, humidity: 54.2 },
  { time: '15:00', temp: 20.5, humidity: 53.8 },
  { time: '14:00', temp: 20.6, humidity: 53.5 },
  { time: '13:00', temp: 20.3, humidity: 54.0 },
  { time: '12:00', temp: 20.8, humidity: 53.2 }
])

async function handleRefresh() {
  refreshing.value = true
  await new Promise(r => setTimeout(r, 800))
  // 模拟数据刷新
  const now = new Date()
  const timeStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`
  sensorList.value.forEach(item => {
    if (item.online) {
      item.updateTime = timeStr
      const t = (20 + Math.random() * 2).toFixed(1)
      const h = (50 + Math.random() * 10).toFixed(1)
      item.value = `${t}℃ / ${h}%`
      item.color = t > 22 ? '#FF9800' : '#4CAF50'
    }
  })
  refreshing.value = false
  ElMessage.success('数据已刷新')
}

function handleStorageChange() {
  // 模拟切换库房的历史数据
  ElMessage.info(`已切换到 ${selectedStorage.value}`)
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.sensor-cards { margin-bottom: 0; }
.sensor-card { border-radius: var(--radius-md); text-align: center; margin-bottom: 0; }
.sensor-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.sensor-name { font-size: 13px; color: var(--color-text-light); }
.sensor-value { font-size: 28px; font-weight: 700; margin-bottom: 4px; }
.sensor-label { font-size: 12px; color: var(--color-text-muted); }
.sensor-time { font-size: 11px; color: var(--color-text-muted); margin-top: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 15px; font-weight: 600; color: var(--color-text); }
.trend-list { max-height: 300px; overflow-y: auto; }
.trend-item { display: flex; align-items: center; padding: 8px 12px; border-bottom: 1px solid var(--el-border-color-lighter); }
.trend-item:last-child { border-bottom: none; }
.trend-time { width: 80px; color: var(--color-text-muted); font-size: 13px; }
.trend-temp { width: 100px; font-size: 14px; font-weight: 500; }
.trend-humidity { font-size: 14px; font-weight: 500; }
.text-danger { color: #f56c6c; }
</style>
