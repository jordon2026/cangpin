<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">出库申请</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增申请</el-button>
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
            <el-option label="已撤回" value="已撤回" />
            <el-option label="已出库" value="已出库" />
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
        <el-table-column prop="reason" label="出库原因" width="120" />
        <el-table-column prop="destination" label="去向" width="140" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="date" label="申请日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === '待审批'" type="warning" link size="small" @click="handleCancel(row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50]" :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background />
      </div>
    </el-card>

    <!-- 新增出库申请弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增出库申请" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="藏品名称" prop="collectionName">
          <el-input v-model="formData.collectionName" placeholder="请输入藏品名称" />
        </el-form-item>
        <el-form-item label="藏品编号" prop="collectionCode">
          <el-input v-model="formData.collectionCode" placeholder="请输入藏品编号" />
        </el-form-item>
        <el-form-item label="出库原因" prop="reason">
          <el-select v-model="formData.reason" placeholder="请选择" style="width: 100%">
            <el-option label="展览借展" value="展览借展" />
            <el-option label="修复保养" value="修复保养" />
            <el-option label="学术研究" value="学术研究" />
            <el-option label="调拨交换" value="调拨交换" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="去向" prop="destination">
          <el-input v-model="formData.destination" placeholder="请输入去向" />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="formData.applicant" placeholder="请输入申请人" />
        </el-form-item>
        <el-form-item label="申请日期" prop="date">
          <el-date-picker v-model="formData.date" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="补充说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewVisible" title="出库申请详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="出库单号" :span="2">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionCode }}</el-descriptions-item>
        <el-descriptions-item label="出库原因">{{ viewData.reason }}</el-descriptions-item>
        <el-descriptions-item label="去向">{{ viewData.destination }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ viewData.applicant }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ viewData.date }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const submitLoading = ref(false)
let nextId = 4

const mockData = ref([
  { id: 1, code: 'CK-20260407-001', collectionName: '山水图卷', collectionCode: 'ZW-2026-0003', reason: '展览借展', destination: '国家博物馆', applicant: '李馆长', date: '2026-04-07', status: '待审批', remark: '' },
  { id: 2, code: 'CK-20260406-002', collectionName: '白玉璧', collectionCode: 'ZW-2026-0004', reason: '修复保养', destination: '修复中心', applicant: '张主管', date: '2026-04-06', status: '已通过', remark: '' },
  { id: 3, code: 'CK-20260405-003', collectionName: '三彩骆驼俑', collectionCode: 'ZW-2026-0005', reason: '学术研究', destination: '考古研究所', applicant: '王教授', date: '2026-04-05', status: '已出库', remark: '研究周期3个月' }
])

const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

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

const formData = reactive({
  collectionName: '', collectionCode: '', reason: '', destination: '', applicant: '', date: '', remark: ''
})

const formRules = {
  collectionName: [{ required: true, message: '请输入藏品名称', trigger: 'blur' }],
  collectionCode: [{ required: true, message: '请输入藏品编号', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择出库原因', trigger: 'change' }],
  destination: [{ required: true, message: '请输入去向', trigger: 'blur' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

function statusType(s) {
  return s === '待审批' ? 'warning' : s === '已通过' ? 'success' : s === '已驳回' ? 'danger' : s === '已出库' ? 'primary' : 'info'
}

function handleSearch() { pagination.page = 1 }
function handleReset() { Object.assign(searchForm, { collectionName: '', status: '' }); pagination.page = 1 }

function handleAdd() {
  Object.assign(formData, { collectionName: '', collectionCode: '', reason: '', destination: '', applicant: '', date: '', remark: '' })
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  const now = new Date()
  const code = `CK-${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}-${String(nextId).padStart(3,'0')}`
  mockData.value.unshift({ ...formData, id: nextId++, code, status: '待审批' })
  submitLoading.value = false
  dialogVisible.value = false
  ElMessage.success('申请已提交')
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确定要撤回申请"${row.code}"吗？`, '提示', { type: 'warning' })
  const item = mockData.value.find(r => r.id === row.id)
  if (item) item.status = '已撤回'
  ElMessage.success('申请已撤回')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
