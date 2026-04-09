<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">标签管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增标签</el-button>
    </div>
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="EPC编码">
          <el-input v-model="searchForm.epc" placeholder="请输入" clearable style="width: 240px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="使用状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="已绑定" value="已绑定" />
            <el-option label="未绑定" value="未绑定" />
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
        <el-table-column prop="epc" label="EPC编码" width="240" show-overflow-tooltip />
        <el-table-column prop="type" label="标签类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collectionName" label="关联藏品" min-width="140">
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.collectionName }">{{ row.collectionName || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="使用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已绑定' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bindDate" label="绑定日期" width="120" />
        <el-table-column prop="remark" label="备注" min-width="100" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === '已绑定'" type="warning" link size="small" @click="handleUnbind(row)">解绑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50]" :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="EPC编码" prop="epc">
          <el-input v-model="formData.epc" placeholder="扫描或手动输入EPC编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="标签类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="抗金属标签" value="抗金属标签" />
            <el-option label="普通标签" value="普通标签" />
            <el-option label="高温标签" value="高温标签" />
            <el-option label="防水标签" value="防水标签" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="标签备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
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
  { id: 1, epc: 'E200001B05150136202022C8', type: '抗金属标签', collectionName: '青花瓷梅瓶', status: '已绑定', bindDate: '2026-03-15', remark: 'A区库房' },
  { id: 2, epc: 'E200001B05150136202022D1', type: '抗金属标签', collectionName: '青铜鼎', status: '已绑定', bindDate: '2026-03-15', remark: 'B区库房' },
  { id: 3, epc: 'E200001B05150136202022E4', type: '普通标签', collectionName: '', status: '未绑定', bindDate: '-', remark: '库存标签' }
])

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const searchForm = reactive({ epc: '', status: '' })
const pagination = reactive({ page: 1, size: 10 })

const filteredData = computed(() => {
  let data = mockData.value
  if (searchForm.epc) data = data.filter(r => r.epc.includes(searchForm.epc))
  if (searchForm.status) data = data.filter(r => r.status === searchForm.status)
  return data
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredData.value.slice(start, start + pagination.size)
})

const formData = reactive({ id: null, epc: '', type: '', remark: '' })

const formRules = {
  epc: [{ required: true, message: '请输入EPC编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择标签类型', trigger: 'change' }]
}

function handleSearch() { pagination.page = 1 }
function handleReset() { Object.assign(searchForm, { epc: '', status: '' }); pagination.page = 1 }

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增标签'
  Object.assign(formData, { id: null, epc: '', type: '', remark: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑标签'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  if (isEdit.value) {
    const idx = mockData.value.findIndex(r => r.id === formData.id)
    if (idx !== -1) mockData.value.splice(idx, 1, { ...formData })
    ElMessage.success('修改成功')
  } else {
    mockData.value.unshift({ ...formData, id: nextId++, collectionName: '', status: '未绑定', bindDate: '-' })
    ElMessage.success('标签已添加')
  }
  submitLoading.value = false
  dialogVisible.value = false
}

async function handleUnbind(row) {
  await ElMessageBox.confirm(`确定要解绑标签"${row.epc}"吗？`, '提示', { type: 'warning' })
  const item = mockData.value.find(r => r.id === row.id)
  if (item) {
    item.status = '未绑定'
    item.collectionName = ''
    item.bindDate = '-'
  }
  ElMessage.success('标签已解绑')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除标签"${row.epc}"吗？`, '提示', { type: 'warning' })
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
.text-muted { color: var(--el-text-color-placeholder); }
</style>
