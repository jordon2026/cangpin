<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">入库登记</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增入库</el-button>
    </div>
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="单号/藏品名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审核" :value="0" />
            <el-option label="已入库" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="inboundNo" label="入库单号" width="160" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="140" />
        <el-table-column prop="collectionNo" label="藏品编号" width="140" />
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.source || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storageName" label="入库库房" width="120" />
        <el-table-column prop="handler" label="经办人" width="100" />
        <el-table-column prop="inboundDate" label="入库日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleApprove(row, true)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleApprove(row, false)">拒绝</el-button>
            <el-button v-if="row.status !== 1" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          background
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增入库弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增入库" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="藏品名称" prop="collectionName">
          <el-input v-model="formData.collectionName" placeholder="请输入藏品名称" />
        </el-form-item>
        <el-form-item label="藏品编号" prop="collectionNo">
          <el-input v-model="formData.collectionNo" placeholder="请输入藏品编号" />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="formData.source" placeholder="请选择来源" style="width: 100%">
            <el-option label="考古发掘" value="考古发掘" />
            <el-option label="捐赠" value="捐赠" />
            <el-option label="征集购买" value="征集购买" />
            <el-option label="调拨" value="调拨" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库库房" prop="storageId">
          <el-select v-model="formData.storageId" placeholder="请选择库房" style="width: 100%">
            <el-option v-for="s in storages" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="经办人">
          <el-input v-model="formData.handler" placeholder="请输入经办人" />
        </el-form-item>
        <el-form-item label="入库日期">
          <el-date-picker v-model="formData.inboundDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="入库详情" width="520px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入库单号" :span="2">{{ viewData.inboundNo }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionNo }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ viewData.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入库库房">{{ viewData.storageName }}</el-descriptions-item>
        <el-descriptions-item label="经办人">{{ viewData.handler }}</el-descriptions-item>
        <el-descriptions-item label="入库日期">{{ viewData.inboundDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)" size="small">{{ getStatusText(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getInboundList, addInbound, approveInbound, deleteInbound } from '@/api/inbound'
import { getAllStorages } from '@/api/storage'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const storages = ref([])

const pagination = reactive({ current: 1, size: 10, total: 0 })

const searchForm = reactive({ keyword: '', status: null })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

const formData = reactive({
  collectionName: '', collectionNo: '', source: '', storageId: null,
  handler: '', inboundDate: '', remark: ''
})

const formRules = {
  collectionName: [{ required: true, message: '请输入藏品名称', trigger: 'blur' }],
  collectionNo: [{ required: true, message: '请输入藏品编号', trigger: 'blur' }],
  storageId: [{ required: true, message: '请选择库房', trigger: 'change' }]
}

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 0: '待审核', 1: '已入库', 2: '已拒绝' }
  return map[status] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getInboundList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

async function loadStorages() {
  try {
    const res = await getAllStorages()
    if (res.code === 200) {
      storages.value = res.data
    }
  } catch (error) {
    console.error('加载库房失败', error)
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.current = 1
  loadData()
}

function handleAdd() {
  Object.assign(formData, {
    collectionName: '', collectionNo: '', source: '', storageId: null,
    handler: '', inboundDate: '', remark: ''
  })
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    await addInbound(formData)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleApprove(row, approved) {
  const action = approved ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定${action}该入库申请？`, '提示', { type: 'warning' })
  try {
    await approveInbound(row.id, approved)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    console.error(`${action}失败`, error)
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除入库记录"${row.inboundNo}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteInbound(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

onMounted(() => {
  loadData()
  loadStorages()
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
