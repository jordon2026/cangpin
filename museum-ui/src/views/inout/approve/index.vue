<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">出库审批</h2>
    </div>
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="藏品名称">
          <el-input v-model="searchForm.collectionName" placeholder="请输入" clearable style="width: 180px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="待审批" value="待审批" />
            <el-option label="已通过" value="已通过" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="出库单号" width="160" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="140" />
        <el-table-column prop="reason" label="出库原因" width="140" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="date" label="申请日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待审批'" type="success" link size="small" @click="handleApprove(row, true)">通过</el-button>
            <el-button v-if="row.status === '待审批'" type="danger" link size="small" @click="handleReject(row)">驳回</el-button>
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50]" :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background />
      </div>
    </el-card>

    <!-- 审批意见弹窗 -->
    <el-dialog v-model="approveVisible" :title="approveTitle" width="480px" destroy-on-close>
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="80px">
        <el-form-item label="审批意见" prop="opinion">
          <el-input v-model="approveForm.opinion" type="textarea" :rows="3" :placeholder="approvePlaceholder" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button :type="approveType" :loading="submitLoading" @click="submitApprove">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewVisible" title="出库申请详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="出库单号" :span="2">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="出库原因">{{ viewData.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ viewData.applicant }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ viewData.date }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批意见">{{ viewData.approveOpinion || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

const submitLoading = ref(false)

const mockData = ref([
  { id: 1, code: 'CK-20260407-001', collectionName: '山水图卷', reason: '展览借展', applicant: '李馆长', date: '2026-04-07', status: '待审批', approveOpinion: '' },
  { id: 2, code: 'CK-20260406-002', collectionName: '白玉璧', reason: '修复保养', applicant: '张主管', date: '2026-04-06', status: '已通过', approveOpinion: '同意，注意保护' },
  { id: 3, code: 'CK-20260405-003', collectionName: '三彩骆驼俑', reason: '学术研究', applicant: '王教授', date: '2026-04-05', status: '已通过', approveOpinion: '批准' }
])

const approveVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const approveFormRef = ref(null)
const approveTitle = ref('')
const approvePlaceholder = ref('')
const approveType = ref('success')
const currentRow = ref(null)

const searchForm = reactive({ collectionName: '', status: '' })
const pagination = reactive({ page: 1, size: 10 })

const filteredData = computed(() => {
  let data = mockData.value
  if (searchForm.collectionName) data = data.filter(r => r.collectionName.includes(searchForm.collectionName))
  if (searchForm.status) data = data.filter(r => r.status === searchForm.status)
  return data
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredData.value.slice(start, start + pagination.size)
})

const approveForm = reactive({ opinion: '' })

const approveRules = {
  opinion: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }]
}

function statusType(s) {
  return s === '待审批' ? 'warning' : s === '已通过' ? 'success' : s === '已驳回' ? 'danger' : 'info'
}

function handleSearch() { pagination.page = 1 }
function handleReset() { Object.assign(searchForm, { collectionName: '', status: '' }); pagination.page = 1 }

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

function handleApprove(row, isPass) {
  currentRow.value = row
  if (isPass) {
    approveTitle.value = '审批通过'
    approvePlaceholder.value = '请输入审批意见（可选）'
    approveType.value = 'success'
  }
  approveForm.opinion = ''
  approveVisible.value = true
}

function handleReject(row) {
  currentRow.value = row
  approveTitle.value = '驳回申请'
  approvePlaceholder.value = '请输入驳回原因（必填）'
  approveType.value = 'danger'
  approveForm.opinion = ''
  approveVisible.value = true
}

async function submitApprove() {
  const isReject = approveType.value === 'danger'
  // 驳回时校验必填
  if (isReject) {
    try { await approveFormRef.value.validate() } catch { return }
  }
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  const item = mockData.value.find(r => r.id === currentRow.value.id)
  if (item) {
    item.status = isReject ? '已驳回' : '已通过'
    item.approveOpinion = approveForm.opinion || '同意'
  }
  submitLoading.value = false
  approveVisible.value = false
  ElMessage.success(isReject ? '已驳回' : '审批通过')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
