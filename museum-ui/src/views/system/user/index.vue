<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <div class="table-header">
        <span class="table-title">用户列表</span>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="name in row.roleNames" :key="name" size="small" class="role-tag">{{ name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(val) => handleStatusChange(row, val ? 1 : 0)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">修改</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="formData.deptId" placeholder="请选择部门" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="formData.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, resetPwd, updateUserStatus } from '@/api/system/user'
import { getRoleList } from '@/api/system/role'
import { getDeptList } from '@/api/system/dept'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const roleOptions = ref([])
const deptOptions = ref([])

const queryParams = reactive({ page: 1, size: 10, username: '', phone: '', status: null })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const dialogTitle = ref('')

const formData = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  deptId: null,
  phone: '',
  email: '',
  roleIds: []
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

onMounted(() => {
  loadData()
  loadOptions()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    const page = res.data
    tableData.value = page.records || []
    total.value = page.total || 0
  } catch { /* handled by interceptor */ }
  loading.value = false
}

async function loadOptions() {
  try {
    const [roleRes, deptRes] = await Promise.all([getRoleList(), getDeptList()])
    roleOptions.value = roleRes.data || []
    // 扁平化部门树供下拉使用
    deptOptions.value = flattenTree(deptRes.data || [])
  } catch { /* ignore */ }
}

function flattenTree(nodes) {
  const result = []
  nodes.forEach(n => {
    result.push(n)
    if (n.children?.length) result.push(...flattenTree(n.children))
  })
  return result
}

function handleQuery() {
  queryParams.page = 1
  loadData()
}

function handleReset() {
  queryParams.username = ''
  queryParams.phone = ''
  queryParams.status = null
  handleQuery()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(formData, { id: null, username: '', password: '', realName: '', deptId: null, phone: '', email: '', roleIds: [] })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '修改用户'
  Object.assign(formData, { id: row.id, username: row.username, password: '', realName: row.realName, deptId: row.deptId, phone: row.phone, email: row.email, roleIds: row.roleIds || [] })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(formData)
      ElMessage.success('修改成功')
    } else {
      await addUser(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch { /* handled by interceptor */ }
  submitLoading.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { /* handled */ }
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm(`确定要重置用户"${row.username}"的密码吗？`, '提示', { type: 'warning' })
  try {
    await resetPwd(row.id)
    ElMessage.success('密码已重置，请查看管理员显示的临时密码')
  } catch { /* handled */ }
}

async function handleStatusChange(row, status) {
  try {
    await updateUserStatus(row.id, status)
    row.status = status
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
  } catch {
    /* handled */
  }
}
</script>

<style scoped>
.page-container { padding: 16px; }
.search-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px 20px 0; margin-bottom: 16px; }
.table-card { background: var(--el-bg-color); border-radius: 8px; padding: 20px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.role-tag { margin-right: 4px; margin-bottom: 2px; }
</style>
