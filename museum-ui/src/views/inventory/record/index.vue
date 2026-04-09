<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">盘点记录</h2>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="taskCode" label="任务编号" width="160" />
        <el-table-column prop="taskName" label="任务名称" min-width="180" />
        <el-table-column prop="total" label="应盘数量" width="100" />
        <el-table-column prop="actual" label="实盘数量" width="100" />
        <el-table-column prop="diff" label="差异数量" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.diff !== 0, 'text-success': row.diff === 0 }">
              {{ row.diff === 0 ? '0' : (row.diff > 0 ? '+' : '') + row.diff }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="completer" label="盘点人" width="100" />
        <el-table-column prop="date" label="完成日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '一致' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 盘点详情弹窗 -->
    <el-dialog v-model="viewVisible" title="盘点记录详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务编号" :span="2">{{ viewData.taskCode }}</el-descriptions-item>
        <el-descriptions-item label="任务名称" :span="2">{{ viewData.taskName }}</el-descriptions-item>
        <el-descriptions-item label="应盘数量">{{ viewData.total }}</el-descriptions-item>
        <el-descriptions-item label="实盘数量">{{ viewData.actual }}</el-descriptions-item>
        <el-descriptions-item label="差异数量">
          <span :class="{ 'text-danger': viewData.diff !== 0 }">
            {{ viewData.diff === 0 ? '0（一致）' : (viewData.diff > 0 ? '+' : '') + viewData.diff + '（不一致）' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === '一致' ? 'success' : 'danger'" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="盘点人">{{ viewData.completer }}</el-descriptions-item>
        <el-descriptions-item label="完成日期">{{ viewData.date }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">差异明细</el-divider>
      <el-table :data="diffList" border size="small" max-height="200">
        <el-table-column prop="code" label="藏品编号" width="140" />
        <el-table-column prop="name" label="藏品名称" min-width="140" />
        <el-table-column prop="expected" label="应有" width="80" />
        <el-table-column prop="actual" label="实有" width="80" />
        <el-table-column prop="diff" label="差异" width="80">
          <template #default="{ row: r }">
            <span class="text-danger">{{ r.diff > 0 ? '多' + r.diff : '少' + Math.abs(r.diff) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="diffList.length === 0" description="无差异" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const mockData = ref([
  { id: 1, taskCode: 'PD-20260401-002', taskName: 'B区库房月度盘点', total: 320, actual: 319, diff: -1, completer: '张主管', date: '2026-04-03', status: '不一致' },
  { id: 2, taskCode: 'PD-20260325-003', taskName: '临时抽盘-陶瓷区', total: 86, actual: 86, diff: 0, completer: '王助理', date: '2026-03-26', status: '一致' }
])

const tableData = ref(mockData)
const viewVisible = ref(false)
const viewData = ref({})
const diffList = ref([])

const diffDetailMap = {
  1: [
    { code: 'ZW-2026-0042', name: '青花小碗', expected: 1, actual: 0, diff: -1 }
  ]
}

function handleView(row) {
  viewData.value = { ...row }
  diffList.value = diffDetailMap[row.id] || []
  viewVisible.value = true
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.text-danger { color: #f56c6c; font-weight: 600; }
.text-success { color: #67c23a; font-weight: 600; }
</style>
