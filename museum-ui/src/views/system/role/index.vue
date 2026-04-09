<template>
  <div class="page-container">
    <div class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-header">
        <span class="table-title">角色列表</span>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="roleCode" label="角色编码" width="140" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">修改</el-button>
            <el-button link type="success" size="small" @click="handleAssignPerm(row)">分配权限</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="分配菜单权限" width="480px" destroy-on-close>
      <el-tree
        ref="menuTreeRef"
        v-loading="permLoading"
        :data="menuTreeData"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="handleSavePerm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getRolePage, addRole, updateRole, deleteRole, getRoleMenuIds, assignRoleMenus } from '@/api/system/role'
import { getMenuList } from '@/api/system/menu'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({ page: 1, size: 10, roleName: '', roleCode: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const dialogTitle = ref('')

const formData = reactive({ roleName: '', roleCode: '', sort: 0, remark: '' })
const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

// 权限分配
const permDialogVisible = ref(false)
const permLoading = ref(false)
const menuTreeRef = ref(null)
const menuTreeData = ref([])
const checkedMenuIds = ref([])
const currentRoleId = ref(null)

onMounted(() => { loadData() })

async function loadData() {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    const page = res.data
    tableData.value = page.records || []
    total.value = page.total || 0
  } catch { /* handled */ }
  loading.value = false
}

function handleQuery() { queryParams.page = 1; loadData() }
function handleReset() { queryParams.roleName = ''; queryParams.roleCode = ''; handleQuery() }

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  Object.assign(formData, { roleName: '', roleCode: '', sort: 0, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '修改角色'
  Object.assign(formData, { roleName: row.roleName, roleCode: row.roleCode, sort: row.sort, remark: row.remark })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateRole(formData)
      ElMessage.success('修改成功')
    } else {
      await addRole(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* handled */ }
  submitLoading.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { /* handled */ }
}

async function handleAssignPerm(row) {
  currentRoleId.value = row.id
  permDialogVisible.value = true
  permLoading.value = true
  try {
    const [menuRes, idsRes] = await Promise.all([getMenuList(), getRoleMenuIds(row.id)])
    menuTreeData.value = menuRes.data || []
    checkedMenuIds.value = idsRes.data || []
    await nextTick()
    // 需要设置已选，el-tree 只设置叶子节点即可
    menuTreeRef.value?.setCheckedKeys(checkedMenuIds.value)
  } catch { /* handled */ }
  permLoading.value = false
}

async function handleSavePerm() {
  permLoading.value = true
  try {
    const checked = menuTreeRef.value.getCheckedKeys()
    const halfChecked = menuTreeRef.value.getHalfCheckedKeys()
    await assignRoleMenus(currentRoleId.value, [...checked, ...halfChecked])
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch { /* handled */ }
  permLoading.value = false
}
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px 20px 0; margin-bottom: 16px; }
.table-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
