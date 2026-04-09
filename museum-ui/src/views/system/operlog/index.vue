<template>
  <div class="page-container">
    <div class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="操作模块">
          <el-input v-model="queryParams.title" placeholder="请输入模块" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.businessType" placeholder="全部" clearable style="width: 120px">
            <el-option label="新增" :value="1" />
            <el-option label="修改" :value="2" />
            <el-option label="删除" :value="3" />
            <el-option label="其他" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="正常" :value="1" />
            <el-option label="异常" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">操作日志</span>
        <el-button type="danger" plain :icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除 ({{ selectedIds.length }})
        </el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="操作模块" width="120" />
        <el-table-column prop="type" label="操作类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" size="small" type="success">新增</el-tag>
            <el-tag v-else-if="row.type === 2" size="small" type="warning">修改</el-tag>
            <el-tag v-else-if="row.type === 3" size="small" type="danger">删除</el-tag>
            <el-tag v-else size="small" info>其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="Java方法" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="80" align="center" />
        <el-table-column prop="operUrl" label="请求URL" min-width="160" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="日志详情" size="500px">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item label="日志ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getTypeLabel(detailData.type) }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailData.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ detailData.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="Java方法">{{ detailData.method }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailData.operUser }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ip }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status === 1 ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.operTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre class="detail-pre">{{ detailData.requestParams }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果">
          <pre class="detail-pre">{{ detailData.responseResult }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.errorMsg" label="错误信息">
          <span style="color: var(--el-color-danger)">{{ detailData.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { getOperLogPage, deleteOperLog } from '@/api/system/operlog'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const dateRange = ref(null)

const queryParams = reactive({ page: 1, size: 10, title: '', businessType: null, status: null })

const drawerVisible = ref(false)
const detailData = ref(null)

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (dateRange.value?.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getOperLogPage(params)
    const page = res.data
    tableData.value = page.records || []
    total.value = page.total || 0
  } catch { /* handled */ }
  loading.value = false
}

function handleQuery() { queryParams.page = 1; loadData() }
function handleReset() {
  queryParams.title = ''
  queryParams.businessType = null
  queryParams.status = null
  dateRange.value = null
  handleQuery()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function getTypeLabel(type) {
  const map = { 0: '其他', 1: '新增', 2: '修改', 3: '删除' }
  return map[type] || '未知'
}

function handleDetail(row) {
  detailData.value = row
  drawerVisible.value = true
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条日志吗？`, '提示', { type: 'warning' })
  try {
    await deleteOperLog(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    loadData()
  } catch { /* handled */ }
}
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px 20px 0; margin-bottom: 16px; }
.table-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.detail-pre { margin: 0; white-space: pre-wrap; word-break: break-all; font-size: 12px; max-height: 200px; overflow-y: auto; }
</style>
