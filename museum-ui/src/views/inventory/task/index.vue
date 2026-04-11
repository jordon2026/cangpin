<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">盘点任务</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 创建任务</el-button>
    </div>

    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="任务编号/名称/库房" clearable style="width: 180px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="待盘点" :value="0" />
            <el-option label="盘点中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="taskNo" label="任务编号" width="180" />
        <el-table-column prop="name" label="任务名称" min-width="180" />
        <el-table-column prop="storageName" label="盘点库房" width="120" />
        <el-table-column prop="handlerName" label="负责人" width="100" />
        <el-table-column prop="planCount" label="计划数量" width="90" align="center" />
        <el-table-column prop="actualCount" label="实际盘点" width="90" align="center" />
        <el-table-column prop="diffCount" label="差异数" width="80" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.diffCount > 0 }">{{ row.diffCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleStart(row)">开始盘点</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleComplete(row)">完成盘点</el-button>
            <el-button v-if="row.status === 1" type="info" link size="small" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="row.status !== 1" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 创建任务弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建盘点任务" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="formData.name" placeholder="如 A区库房季度盘点" />
        </el-form-item>
        <el-form-item label="盘点库房" prop="storageId">
          <el-select v-model="formData.storageId" placeholder="请选择库房" style="width: 100%" filterable>
            <el-option v-for="s in storageList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期" prop="planDate">
          <el-date-picker v-model="formData.planDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人" prop="handlerId">
          <el-select v-model="formData.handlerId" placeholder="请选择负责人" style="width: 100%" filterable>
            <el-option v-for="u in userList" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="补充说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">创建任务</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewVisible" title="盘点任务详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务编号" :span="2">{{ viewData.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="任务名称" :span="2">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="盘点库房">{{ viewData.storageName }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ viewData.handlerName }}</el-descriptions-item>
        <el-descriptions-item label="计划数量">{{ viewData.planCount }}</el-descriptions-item>
        <el-descriptions-item label="实际盘点">{{ viewData.actualCount }}</el-descriptions-item>
        <el-descriptions-item label="差异数量">{{ viewData.diffCount }}</el-descriptions-item>
        <el-descriptions-item label="盘点结果">
          <el-tag v-if="viewData.result === 1" type="danger" size="small">有差异</el-tag>
          <el-tag v-else type="success" size="small">正常</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ statusText(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ viewData.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ viewData.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTaskList, addTask, getTaskById, deleteTask } from '@/api/inventory/index'
import { getList as getStorageList } from '@/api/storage'
import { getUserList } from '@/api/system/user'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const storageList = ref([])
const userList = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

const searchForm = reactive({ keyword: '', status: null })

const formData = reactive({
  name: '',
  storageId: null,
  storageName: '',
  planDate: '',
  handlerId: null,
  handlerName: '',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  storageId: [{ required: true, message: '请选择库房', trigger: 'change' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }]
}

function statusText(status) {
  const map = { 0: '待盘点', 1: '盘点中', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

function statusType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: '' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTaskList({
      current: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

async function loadStorages() {
  try {
    const res = await getStorageList({ current: 1, size: 100 })
    if (res.code === 200) {
      storageList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载库房失败', error)
  }
}

async function loadUsers() {
  try {
    const res = await getUserList({ current: 1, size: 100 })
    if (res.code === 200) {
      userList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载用户失败', error)
  }
}

function handleSearch() { pagination.page = 1; loadData() }
function handleReset() {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  loadData()
}

function handleAdd() {
  Object.assign(formData, {
    name: '',
    storageId: null,
    storageName: '',
    planDate: '',
    handlerId: null,
    handlerName: '',
    remark: ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    // 设置库房名称
    const storage = storageList.value.find(s => s.id === formData.storageId)
    if (storage) formData.storageName = storage.name

    await addTask(formData)
    ElMessage.success('盘点任务已创建')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('创建失败', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleView(row) {
  try {
    const res = await getTaskById(row.id)
    if (res.code === 200) {
      viewData.value = res.data || {}
      viewVisible.value = true
    }
  } catch (error) {
    console.error('加载详情失败', error)
  }
}

async function handleStart(row) {
  await ElMessageBox.confirm(`确定要开始盘点任务"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    // 调用开始盘点接口
    await request({ url: `/inventory/task/start/${row.id}`, method: 'put' })
    ElMessage.success('盘点已开始')
    loadData()
  } catch (error) {
    console.error('操作失败', error)
  }
}

async function handleComplete(row) {
  await ElMessageBox.confirm(`确定要完成盘点任务"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await request({ url: `/inventory/task/complete/${row.id}`, method: 'put' })
    ElMessage.success('盘点已完成')
    loadData()
  } catch (error) {
    console.error('操作失败', error)
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确定要取消任务"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await request({ url: `/inventory/task/cancel/${row.id}`, method: 'put' })
    ElMessage.success('任务已取消')
    loadData()
  } catch (error) {
    console.error('操作失败', error)
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除任务"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

import request from '@/api/request'

onMounted(() => {
  loadData()
  loadStorages()
  loadUsers()
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.text-danger { color: var(--el-color-danger); }
</style>
