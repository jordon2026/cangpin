<template>
  <div class="page-container">
    <div class="search-card">
      <div class="table-header">
        <span class="table-title">部门列表</span>
        <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增部门</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="deptData" border stripe row-key="id" :tree-props="{ children: 'children' }" default-expand-all>
        <el-table-column prop="deptName" label="部门名称" width="200" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAdd(row)">新增</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">修改</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="formData.parentId"
            :data="[{id: 0, deptName: '根部门', children: deptData}]"
            :props="{ label: 'deptName', children: 'children', value: 'id' }"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="formData.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getDeptList, addDept, updateDept, deleteDept } from '@/api/system/dept'

const loading = ref(false)
const submitLoading = ref(false)
const deptData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const dialogTitle = ref('')
const editId = ref(null)

const formData = reactive({
  parentId: 0, deptName: '', leader: '', phone: '', email: '', sort: 0, status: 1
})

const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    const res = await getDeptList()
    deptData.value = res.data || []
  } catch { /* handled */ }
  loading.value = false
}

function handleAdd(row) {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增部门'
  Object.assign(formData, { parentId: row ? row.id : 0, deptName: '', leader: '', phone: '', email: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '修改部门'
  Object.assign(formData, { parentId: row.parentId, deptName: row.deptName, leader: row.leader || '', phone: row.phone || '', email: row.email || '', sort: row.sort, status: row.status })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateDept(editId.value, formData)
      ElMessage.success('修改成功')
    } else {
      await addDept(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* handled */ }
  submitLoading.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除部门"${row.deptName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteDept(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { /* handled */ }
}
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px; margin-bottom: 16px; }
.table-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.table-title { font-size: 16px; font-weight: 600; }
</style>
