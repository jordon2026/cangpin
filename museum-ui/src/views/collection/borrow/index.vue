<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">藏品借出</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增借出
      </el-button>
    </div>
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="借出单号">
          <el-input v-model="searchForm.code" placeholder="请输入借出单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="藏品名称">
          <el-input v-model="searchForm.name" placeholder="请输入藏品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="借出状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待审批" value="待审批" />
            <el-option label="已借出" value="已借出" />
            <el-option label="已归还" value="已归还" />
            <el-option label="已拒绝" value="已拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="借出单号" width="150" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="160" />
        <el-table-column prop="borrower" label="借展单位" width="140" />
        <el-table-column prop="startDate" label="借出日期" width="120" />
        <el-table-column prop="endDate" label="归还日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" link size="small" v-if="row.status === '待审批'" @click="handleApprove(row)">审批</el-button>
            <el-button type="warning" link size="small" v-if="row.status === '已借出'" @click="handleReturn(row)">归还</el-button>
            <el-button type="danger" link size="small" v-if="row.status !== '已归还'" @click="handleCancel(row)">取消</el-button>
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
    <el-dialog v-model="viewVisible" title="借出详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="借出单号">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="借展单位">{{ viewData.borrower }}</el-descriptions-item>
        <el-descriptions-item label="借展联系人">{{ viewData.contact }}</el-descriptions-item>
        <el-descriptions-item label="借出日期">{{ viewData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="计划归还">{{ viewData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实际归还" :span="2">{{ viewData.returnDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="借出原因" :span="2">{{ viewData.reason }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ viewData.approveRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveVisible" title="审批借出申请" width="500px">
      <el-form ref="approveFormRef" :model="approveForm" label-width="100px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveForm.result">
            <el-radio label="通过">通过</el-radio>
            <el-radio label="拒绝">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.remark" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApproveSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const viewVisible = ref(false)
const approveVisible = ref(false)
const viewData = ref({})
const approveFormRef = ref(null)
const approveForm = reactive({ result: '通过', remark: '' })

const mockData = ref([
  { id: 1, code: 'JC-2026-0001', collectionName: '青花瓷梅瓶', borrower: '国家博物馆', contact: '张主任', startDate: '2026-03-01', endDate: '2026-06-01', status: '已借出', reason: '明代瓷器专题展', returnDate: '', approveRemark: '同意出借' },
  { id: 2, code: 'JC-2026-0002', collectionName: '山水图卷', borrower: '故宫博物院', contact: '李研究员', startDate: '2026-02-15', endDate: '2026-05-15', status: '已归还', reason: '宋代书画精品展', returnDate: '2026-05-10', approveRemark: '批准出借' },
  { id: 3, code: 'JC-2026-0003', collectionName: '青铜鼎', borrower: '上海博物馆', contact: '王教授', startDate: '2026-04-10', endDate: '2026-07-10', status: '待审批', reason: '青铜器学术研讨会', returnDate: '', approveRemark: '' }
])

const searchForm = reactive({ code: '', name: '', status: '' })
const pagination = reactive({ page: 1, size: 10 })

const filteredData = computed(() => {
  let data = mockData.value
  if (searchForm.code) data = data.filter(r => r.code.includes(searchForm.code))
  if (searchForm.name) data = data.filter(r => r.collectionName.includes(searchForm.name))
  if (searchForm.status) data = data.filter(r => r.status === searchForm.status)
  return data
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredData.value.slice(start, start + pagination.size)
})

function getStatusType(status) {
  const map = { '待审批': 'warning', '已借出': 'primary', '已归还': 'success', '已拒绝': 'danger' }
  return map[status] || 'info'
}

function handleSearch() { pagination.page = 1 }
function handleReset() { Object.assign(searchForm, { code: '', name: '', status: '' }); pagination.page = 1 }
function handlePageChange() {}

function handleAdd() { ElMessage.info('新增借出功能开发中') }
function handleView(row) { viewData.value = { ...row }; viewVisible.value = true }
function handleApprove(row) { approveForm.result = '通过'; approveForm.remark = ''; approveVisible.value = true }

async function handleApproveSubmit() {
  const idx = mockData.value.findIndex(r => r.id === viewData.value.id)
  if (idx !== -1) {
    mockData.value[idx].status = approveForm.result === '通过' ? '已借出' : '已拒绝'
    mockData.value[idx].approveRemark = approveForm.remark
  }
  approveVisible.value = false
  ElMessage.success('审批完成')
}

async function handleReturn(row) {
  await ElMessageBox.confirm('确认该藏品已归还入库？', '提示', { type: 'success' })
  const idx = mockData.value.findIndex(r => r.id === row.id)
  if (idx !== -1) {
    mockData.value[idx].status = '已归还'
    mockData.value[idx].returnDate = new Date().toISOString().split('T')[0]
  }
  ElMessage.success('归还登记成功')
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确认取消该借出申请？', '提示', { type: 'warning' })
  const idx = mockData.value.findIndex(r => r.id === row.id)
  if (idx !== -1) { mockData.value[idx].status = '已拒绝' }
  ElMessage.success('已取消')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: var(--radius-md); }
.table-card { border-radius: var(--radius-md); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
