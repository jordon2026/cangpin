<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">标签盘点</h2>
      <el-button type="primary" @click="handleStartInventory">
        <el-icon><Refresh /></el-icon> 开始盘点
      </el-button>
    </div>
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="盘点单号">
          <el-input v-model="searchForm.code" placeholder="请输入盘点单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="盘点状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="盘点中" value="盘点中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="异常" value="异常" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="盘点单号" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column prop="totalCount" label="应盘数量" width="100" align="center" />
        <el-table-column prop="scannedCount" label="已盘数量" width="100" align="center" />
        <el-table-column prop="missingCount" label="缺失数量" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.missingCount > 0 ? 'text-danger' : 'text-success'">{{ row.missingCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看详情</el-button>
            <el-button type="success" link size="small" v-if="row.status === '异常'" @click="handleProcess(row)">处理异常</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredData.length"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="盘点详情" width="800px">
      <el-descriptions :column="2" border style="margin-bottom: 16px">
        <el-descriptions-item label="盘点单号">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="盘点人">{{ viewData.operator }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ viewData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ viewData.endTime }}</el-descriptions-item>
        <el-descriptions-item label="应盘数量">{{ viewData.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="已盘数量">{{ viewData.scannedCount }}</el-descriptions-item>
        <el-descriptions-item label="缺失数量">
          <span :class="viewData.missingCount > 0 ? 'text-danger' : 'text-success'">{{ viewData.missingCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-tabs v-if="viewData.details">
        <el-tab-pane label="已扫描" name="scanned">
          <el-table :data="viewData.details.scanned" border size="small">
            <el-table-column prop="code" label="标签编号" width="160" />
            <el-table-column prop="name" label="藏品名称" />
            <el-table-column prop="location" label="位置" width="140" />
            <el-table-column prop="scanTime" label="扫描时间" width="160" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="缺失标签" name="missing">
          <el-table :data="viewData.details.missing" border size="small">
            <el-table-column prop="code" label="标签编号" width="160" />
            <el-table-column prop="name" label="藏品名称" />
            <el-table-column prop="location" label="原位置" width="140" />
            <el-table-column prop="lastScan" label="最后扫描" width="160" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, RefreshLeft } from '@element-plus/icons-vue'

const loading = ref(false)
const viewVisible = ref(false)
const viewData = ref({})

const mockData = ref([
  { 
    id: 1, code: 'PD-2026-0001', operator: '管理员', 
    startTime: '2026-03-15 09:00:00', endTime: '2026-03-15 17:30:00',
    totalCount: 156, scannedCount: 154, missingCount: 2, status: '异常',
    details: {
      scanned: [
        { code: 'RFID-00001', name: '青花瓷梅瓶', location: 'A区-1排-3列', scanTime: '2026-03-15 09:15:22' },
        { code: 'RFID-00002', name: '青铜鼎', location: 'B区-2排-1列', scanTime: '2026-03-15 09:18:45' }
      ],
      missing: [
        { code: 'RFID-00023', name: '白玉璧', location: 'A区-3排-2列', lastScan: '2026-03-10 14:30:00' },
        { code: 'RFID-00045', name: '三彩骆驼俑', location: 'A区-2排-4列', lastScan: '2026-03-12 10:00:00' }
      ]
    }
  },
  { 
    id: 2, code: 'PD-2026-0002', operator: '管理员',
    startTime: '2026-03-20 09:00:00', endTime: '2026-03-20 16:00:00',
    totalCount: 154, scannedCount: 154, missingCount: 0, status: '已完成',
    details: null
  }
])

const searchForm = reactive({ code: '', status: '' })
const pagination = reactive({ page: 1, size: 10 })

const filteredData = computed(() => {
  let data = mockData.value
  if (searchForm.code) data = data.filter(r => r.code.includes(searchForm.code))
  if (searchForm.status) data = data.filter(r => r.status === searchForm.status)
  return data
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredData.value.slice(start, start + pagination.size)
})

function getStatusType(status) {
  const map = { '盘点中': 'warning', '已完成': 'success', '异常': 'danger' }
  return map[status] || 'info'
}

function handleSearch() { pagination.page = 1 }
function handleReset() { Object.assign(searchForm, { code: '', status: '' }); pagination.page = 1 }
function handlePageChange() {}

function handleStartInventory() { ElMessage.info('开始盘点功能开发中，请使用RFID手持终端进行盘点') }
function handleView(row) { viewData.value = { ...row }; viewVisible.value = true }
function handleProcess(row) { ElMessage.info('异常处理功能开发中') }
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: var(--radius-md); }
.table-card { border-radius: var(--radius-md); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.text-danger { color: #F56C6C; font-weight: 600; }
.text-success { color: #67C23A; }
</style>
