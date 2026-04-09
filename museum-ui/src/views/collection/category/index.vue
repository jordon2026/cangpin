<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">藏品分类</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增分类</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="分类编码" width="140" />
        <el-table-column prop="name" label="分类名称" width="160" />
        <el-table-column prop="parent" label="上级分类" width="140" />
        <el-table-column prop="count" label="藏品数量" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入分类编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="formData.parent" placeholder="无（顶级分类）" clearable style="width: 100%">
            <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
let nextId = 7

const mockData = ref([
  { id: 1, code: 'TC', name: '陶瓷', parent: '-', count: 326, sort: 1, status: 1 },
  { id: 2, code: 'SH', name: '书画', parent: '-', count: 215, sort: 2, status: 1 },
  { id: 3, code: 'QT', name: '青铜器', parent: '-', count: 158, sort: 3, status: 1 },
  { id: 4, code: 'YQ', name: '玉器', parent: '-', count: 142, sort: 4, status: 1 },
  { id: 5, code: 'QQ', name: '漆器', parent: '-', count: 89, sort: 5, status: 1 },
  { id: 6, code: 'ZW', name: '杂项', parent: '-', count: 356, sort: 6, status: 1 }
])

const tableData = computed(() => [...mockData.value].sort((a, b) => a.sort - b.sort))

const categoryOptions = computed(() => mockData.value.map(r => r.name))

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const formData = reactive({ id: null, code: '', name: '', parent: '', sort: 1, status: 1 })

const formRules = {
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增分类'
  Object.assign(formData, { id: null, code: '', name: '', parent: '', sort: nextId, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  Object.assign(formData, { ...row, parent: row.parent === '-' ? '' : row.parent })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  const parentName = formData.parent || '-'
  if (isEdit.value) {
    const idx = mockData.value.findIndex(r => r.id === formData.id)
    if (idx !== -1) mockData.value.splice(idx, 1, { ...formData, parent: parentName })
    ElMessage.success('修改成功')
  } else {
    mockData.value.push({ ...formData, id: nextId++, parent: parentName, count: 0 })
    ElMessage.success('新增成功')
  }
  submitLoading.value = false
  dialogVisible.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
</style>
