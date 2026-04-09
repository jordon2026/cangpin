<template>
  <div class="page-container">
    <div class="search-card">
      <div class="table-header">
        <span class="table-title">菜单列表</span>
        <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增菜单</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="menuData" border stripe row-key="id" :tree-props="{ children: 'children' }" default-expand-all>
        <el-table-column prop="name" label="菜单名称" width="180" />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1" size="small" type="warning">目录</el-tag>
            <el-tag v-else-if="row.type === 2" size="small" type="primary">菜单</el-tag>
            <el-tag v-else size="small" info>按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column prop="permission" label="权限标识" min-width="160" show-overflow-tooltip />
        <el-table-column prop="path" label="路由路径" min-width="140" show-overflow-tooltip />
        <el-table-column prop="visible" label="可见" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">{{ row.visible === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.type !== 3" link type="primary" size="small" @click="handleAdd(row)">新增</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">修改</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="formData.parentId"
            :data="[{id: 0, name: '根目录', children: menuData}]"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item v-if="formData.type !== 3" label="路由路径">
          <el-input v-model="formData.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item v-if="formData.type === 2" label="组件路径">
          <el-input v-model="formData.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item v-if="formData.type === 3" label="权限标识">
          <el-input v-model="formData.permission" placeholder="如 system:user:add" />
        </el-form-item>
        <el-form-item v-if="formData.type !== 3" label="图标">
          <el-input v-model="formData.icon" placeholder="请输入图标名称" />
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
import { getMenuList, addMenu, updateMenu, deleteMenu } from '@/api/system/menu'

const loading = ref(false)
const submitLoading = ref(false)
const menuData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const dialogTitle = ref('')
const editId = ref(null)

const formData = reactive({
  parentId: 0, name: '', type: 2, path: '', component: '',
  permission: '', icon: '', sort: 0, visible: 1, status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
}

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuList()
    menuData.value = res.data || []
  } catch { /* handled */ }
  loading.value = false
}

function handleAdd(row) {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增菜单'
  Object.assign(formData, { parentId: row ? row.id : 0, name: '', type: 2, path: '', component: '', permission: '', icon: '', sort: 0, visible: 1, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '修改菜单'
  Object.assign(formData, { parentId: row.parentId, name: row.name, type: row.type, path: row.path, component: row.component, permission: row.permission, icon: row.icon, sort: row.sort, visible: row.visible, status: row.status })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateMenu(editId.value, formData)
      ElMessage.success('修改成功')
    } else {
      await addMenu(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* handled */ }
  submitLoading.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除菜单"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteMenu(row.id)
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
