<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">环境数据</h2>
      <div>
        <el-button @click="loadData" :loading="loading">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- 环境监测卡片 -->
    <el-row :gutter="16" class="sensor-cards" v-if="storages.length > 0">
      <el-col :span="6" v-for="item in environmentData" :key="item.storageId">
        <el-card shadow="never" class="sensor-card" :class="{ 'sensor-card-alert': item.hasAlert }">
          <div class="sensor-header">
            <div>
              <span class="sensor-name">{{ item.storageName }}</span>
              <span class="sensor-no">({{ item.storageNo }})</span>
            </div>
            <el-tag :type="item.hasAlert ? 'danger' : 'success'" size="small">
              {{ item.hasAlert ? '告警' : '正常' }}
            </el-tag>
          </div>
          <div class="sensor-value" :style="{ color: item.hasAlert ? '#F56C6C' : '#67C23A' }">
            {{ item.temperature }}℃ / {{ item.humidity }}%
          </div>
          <div class="sensor-label">
            {{ getEnvironmentStatus(item) }}
          </div>
          <div class="sensor-detail">
            <span>💡 {{ item.light || 0 }} lux</span>
            <span>🌬️ {{ item.co2 || 0 }} ppm</span>
          </div>
          <div class="sensor-time">更新时间：{{ item.recordTime || '暂无数据' }}</div>
        </el-card>
      </el-col>
      <!-- 占位卡片，显示未配置的库房 -->
      <el-col :span="6" v-for="item in emptySlots" :key="'empty-' + item">
        <el-card shadow="never" class="sensor-card sensor-card-empty">
          <div class="empty-content">
            <el-icon :size="32"><Monitor /></el-icon>
            <span>{{ item }} 号库房</span>
            <span class="empty-tip">暂未配置监测设备</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 提示信息 -->
    <el-card shadow="never" style="margin-top: 16px; border-radius: var(--radius-md);">
      <el-alert
        title="功能说明"
        type="info"
        :closable="false"
        show-icon
      >
        <template #default>
          <p>环境监测模块用于实时监控各库房的温湿度等环境参数。</p>
          <p style="margin-top: 8px;">• 库房环境数据由传感器设备自动采集上报</p>
          <p>• 当前显示的是系统中的库房列表，请确保已配置传感器设备</p>
          <p>• 如数据异常，请检查传感器连接状态或联系管理员</p>
        </template>
      </el-alert>
    </el-card>

    <!-- 历史趋势（预留） -->
    <el-card shadow="never" style="margin-top: 16px; border-radius: var(--radius-md);">
      <template #header>
        <div class="card-header">
          <span class="card-title">历史趋势</span>
          <el-select v-model="selectedStorage" placeholder="选择库房" size="small" style="width: 160px" @change="handleStorageChange">
            <el-option v-for="s in storages" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </div>
      </template>
      <el-empty description="历史数据功能开发中..." :image-size="80" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Monitor } from '@element-plus/icons-vue'
import { getAllStorages } from '@/api/storage'

const loading = ref(false)
const selectedStorage = ref(null)
const storages = ref([])

// 模拟环境数据（后端实现后可替换）
const environmentData = ref([])

const emptySlots = computed(() => {
  const count = Math.max(0, 4 - environmentData.value.length)
  return Array.from({ length: count }, (_, i) => i + 1)
})

async function loadStorages() {
  try {
    const res = await getAllStorages()
    if (res.code === 200) {
      storages.value = res.data || []
      selectedStorage.value = storages.value[0]?.id
      
      // 模拟环境数据（实际应从后端API获取）
      environmentData.value = storages.value.map(s => {
        const temp = s.temperature
        const hum = s.humidity
        // 判断环境是否异常：温度 18-22℃，湿度 45-55% 为标准
        const hasAlert = temp !== null && hum !== null && (temp < 18 || temp > 22 || hum < 45 || hum > 55)
        return {
          storageId: s.id,
          storageName: s.name,
          storageNo: s.storageNo,
          temperature: temp !== null ? temp : '--',
          humidity: hum !== null ? hum : '--',
          light: Math.floor(Math.random() * 500),
          co2: Math.floor(Math.random() * 600) + 400,
          recordTime: new Date().toLocaleString(),
          hasAlert,
          alertReason: hasAlert ? getAlertReason(temp, hum) : null
        }
      })
    }
  } catch (error) {
    console.error('加载库房失败', error)
  }
}

async function loadData() {
  loading.value = true
  try {
    await loadStorages()
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('刷新失败', error)
  } finally {
    loading.value = false
  }
}

function handleStorageChange() {
  // 历史数据功能预留
  ElMessage.info('历史趋势功能开发中...')
}

// 获取告警原因
function getAlertReason(temperature, humidity) {
  const reasons = []
  if (temperature < 18) reasons.push('温度偏低')
  if (temperature > 22) reasons.push('温度偏高')
  if (humidity < 45) reasons.push('湿度偏低')
  if (humidity > 55) reasons.push('湿度偏高')
  return reasons.join('、')
}

// 获取环境状态文本
function getEnvironmentStatus(item) {
  if (item.temperature === '--' || item.humidity === '--') {
    return '未配置'
  }
  if (item.hasAlert) {
    return item.alertReason
  }
  return '环境适宜'
}

onMounted(() => {
  loadStorages()
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.sensor-cards { margin-bottom: 0; }
.sensor-card { border-radius: var(--radius-md); text-align: center; margin-bottom: 16px; transition: all 0.3s; }
.sensor-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.sensor-card-alert { border: 1px solid #F56C6C; background: #fef0f0; }
.sensor-card-empty { background: #f5f7fa; }
.sensor-no { font-size: 11px; color: var(--color-text-muted); margin-left: 4px; }
.sensor-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.sensor-name { font-size: 13px; color: var(--color-text-light); }
.sensor-value { font-size: 28px; font-weight: 700; margin-bottom: 4px; }
.sensor-label { font-size: 12px; color: var(--color-text-muted); }
.sensor-detail { display: flex; justify-content: center; gap: 16px; font-size: 12px; color: var(--color-text-muted); margin-top: 4px; }
.sensor-time { font-size: 11px; color: var(--color-text-muted); margin-top: 8px; }
.empty-content { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 120px; color: #909399; gap: 8px; }
.empty-tip { font-size: 12px; color: #c0c4cc; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 15px; font-weight: 600; color: var(--color-text); }
</style>
