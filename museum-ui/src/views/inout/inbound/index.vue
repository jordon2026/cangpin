<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">入库登记</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增入库</el-button>
    </div>
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="藏品名称">
          <el-input v-model="searchForm.collectionName" placeholder="请输入" clearable style="width: 180px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="searchForm.source" placeholder="全部" clearable style="width: 150px" @change="handleSearch">
            <el-option label="考古发掘" value="考古发掘" />
            <el-option label="捐赠" value="捐赠" />
            <el-option label="征集购买" value="征集购买" />
            <el-option label="调拨" value="调拨" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="已完成" value="已完成" />
            <el-option label="入库中" value="入库中" />
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
        <el-table-column prop="code" label="入库单号" width="160" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="140" />
        <el-table-column prop="collectionCode" label="藏品编号" width="140" />
        <el-table-column prop="source" label="来源" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.source }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storage" label="入库库房" width="120" />
        <el-table-column prop="operator" label="经办人" width="100" />
        <el-table-column prop="date" label="入库日期" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '已完成' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50]" :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background />
      </div>
    </el-card>

    <!-- 新增入库弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增入库" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="藏品名称" prop="collectionName">
          <el-input v-model="formData.collectionName" placeholder="请输入藏品名称" />
        </el-form-item>
        <el-form-item label="藏品编号" prop="collectionCode">
          <el-input v-model="formData.collectionCode" placeholder="请输入藏品编号" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-select v-model="formData.source" placeholder="请选择来源" style="width: 100%">
            <el-option label="考古发掘" value="考古发掘" />
            <el-option label="捐赠" value="捐赠" />
            <el-option label="征集购买" value="征集购买" />
            <el-option label="调拨" value="调拨" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库库房" prop="storage">
          <el-select v-model="formData.storage" placeholder="请选择库房" style="width: 100%">
            <el-option label="A区库房" value="A区库房" />
            <el-option label="B区库房" value="B区库房" />
            <el-option label="C区库房" value="C区库房" />
          </el-select>
        </el-form-item>
        <el-form-item label="经办人">
          <el-input v-model="formData.operator" placeholder="请输入经办人" />
        </el-form-item>
        <el-form-item label="入库日期" prop="date">
          <el-date-picker v-model="formData.date" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定入库</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="入库详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入库单号" :span="2">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionCode }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ viewData.source }}</el-descriptions-item>
        <el-descriptions-item label="入库库房">{{ viewData.storage }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ viewData.operator }}</el-descriptions-item>
        <el-descriptions-item label="入库日期">{{ viewData.date }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === '已完成' ? 'success' : 'warning'" size="small">{{ viewData.status }}</el-tag>
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
  { id: 1, code: 'RK-20260407-001', collectionName: '青花瓷梅瓶', collectionCode: 'ZW-2026-0001', source: '考古发掘', storage: 'A区库房', operator: '李明', date: '2026-04-07', status: '已完成', remark: '' },
  { id: 2, code: 'RK-20260406-002', collectionName: '青铜鼎', collectionCode: 'ZW-2026-0002', source: '捐赠', storage: 'B区库房', operator: '张华', date: '2026-04-06', status: '已完成', remark: '张先生捐赠' },
  { id: 3, code: 'RK-20260405-003', collectionName: '山水图卷', collectionCode: 'ZW-2026-0003', source: '征集购买', storage: 'C区库房', operator: '王芳', date: '2026-04-05', status: '已完成', remark: '' }
])

const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

const searchForm = reactive({ collectionName: '', source: '', status: '' })
const pagination = reactive({ page: 1, size: 10 })

const filteredData = computed(() => {
  let data = mockData.value
  if (searchForm.collectionName) data = data.filter(r => r.collectionName.includes(searchForm.collectionName))
  if (searchForm.source) data = data.filter(r => r.source === searchForm.source)
  if (searchForm.status) data = data.filter(r => r.status === searchForm.status)
  return data
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredData.value.slice(start, start + pagination.size)
})

const formData = reactive({
  collectionName: '', collectionCode: '', source: '', storage: '', operator: '', date: '', remark: ''
})

const formRules = {
  collectionName: [{ required: true, message: '请输入藏品名称', trigger: 'blur' }],
  collectionCode: [{ required: true, message: '请输入藏品编号', trigger: 'blur' }],
  source: [{ required: true, message: '请选择来源', trigger: 'change' }],
  storage: [{ required: true, message: '请选择库房', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

function handleSearch() {
  pagination.page = 1
}

function handleReset() {
  Object.assign(searchForm, { collectionName: '', source: '', status: '' })
  pagination.page = 1
}

function handleAdd() {
  Object.assign(formData, { collectionName: '', collectionCode: '', source: '', storage: '', operator: '', date: '', remark: '' })
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
  const code = `RK-${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}-${String(nextId).padStart(3,'0')}`
  mockData.value.unshift({ ...formData, id: nextId++, code, status: '已完成' })
  submitLoading.value = false
  dialogVisible.value = false
  ElMessage.success('入库登记成功')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除入库记录"${row.code}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
