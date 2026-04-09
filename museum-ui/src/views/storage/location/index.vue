<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">库位管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增库位</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="storage" label="所属库房" width="120" />
        <el-table-column prop="code" label="库位编码" width="140" />
        <el-table-column prop="row" label="排" width="60" />
        <el-table-column prop="col" label="列" width="60" />
        <el-table-column prop="layer" label="层" width="60" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="used" label="已用" width="80">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.used >= row.capacity }">{{ row.used }}/{{ row.capacity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.used === 0 ? 'success' : row.used >= row.capacity ? 'danger' : 'warning'" size="small">
              {{ row.used === 0 ? '空闲' : row.used >= row.capacity ? '已满' : '使用中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="所属库房" prop="storage">
          <el-select v-model="formData.storage" placeholder="请选择库房" style="width: 100%">
            <el-option v-for="s in storageOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="库位编码" prop="code">
          <el-input v-model="formData.code" placeholder="如 A-01-01-01" />
        </el-form-item>
        <el-form-item label="排" prop="row">
          <el-input-number v-model="formData.row" :min="1" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="列" prop="col">
          <el-input-number v-model="formData.col" :min="1" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="层" prop="layer">
          <el-input-number v-model="formData.layer" :min="1" :max="99" controls-position="right" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="标准架" value="标准架" />
            <el-option label="密集架" value="密集架" />
            <el-option label="恒温柜" value="恒温柜" />
            <el-option label="展柜" value="展柜" />
          </el-select>
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="formData.capacity" :min="1" :max="999" />
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const submitLoading = ref(false)
let nextId = 5

const storageOptions = ['A区库房', 'B区库房', 'C区库房', '临时库房']

const mockData = ref([
  { id: 1, storage: 'A区库房', code: 'A-01-01-01', row: 1, col: 1, layer: 1, type: '标准架', capacity: 10, used: 6 },
  { id: 2, storage: 'A区库房', code: 'A-01-02-01', row: 1, col: 2, layer: 1, type: '标准架', capacity: 10, used: 3 },
  { id: 3, storage: 'A区库房', code: 'A-02-01-01', row: 2, col: 1, layer: 1, type: '恒温柜', capacity: 4, used: 0 },
  { id: 4, storage: 'B区库房', code: 'B-01-01-01', row: 1, col: 1, layer: 1, type: '密集架', capacity: 20, used: 15 }
])

const tableData = ref(mockData)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const formData = reactive({ id: null, storage: '', code: '', row: 1, col: 1, layer: 1, type: '', capacity: 10 })

const formRules = {
  storage: [{ required: true, message: '请选择所属库房', trigger: 'change' }],
  code: [{ required: true, message: '请输入库位编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增库位'
  Object.assign(formData, { id: null, storage: '', code: '', row: 1, col: 1, layer: 1, type: '', capacity: 10 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑库位'
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
    mockData.value.push({ ...formData, id: nextId++, used: 0 })
    ElMessage.success('新增成功')
  }
  submitLoading.value = false
  dialogVisible.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除库位"${row.code}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.text-danger { color: #f56c6c; font-weight: 600; }
</style>
