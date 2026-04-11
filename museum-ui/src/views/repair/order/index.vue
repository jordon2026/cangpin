<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">修复工单</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 创建工单</el-button>
    </div>

    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="工单编号/藏品名" clearable style="width: 180px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="工单状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="待派单" :value="0" />
            <el-option label="修复中" :value="1" />
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
        <el-table-column prop="orderNo" label="工单编号" width="180" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="140" />
        <el-table-column prop="collectionNo" label="藏品编号" width="140" />
        <el-table-column prop="damageDesc" label="损坏描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="handlerName" label="处理人" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cost" label="修复费用" width="100" align="right">
          <template #default="{ row }">
            {{ row.cost ? '¥' + row.cost.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleDispatch(row)">派单</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleComplete(row)">完成</el-button>
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

    <!-- 创建工单弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建修复工单" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="选择藏品" prop="collectionId">
          <el-select v-model="formData.collectionId" placeholder="请选择藏品" style="width: 100%" filterable @change="onCollectionChange">
            <el-option v-for="c in collectionList" :key="c.id" :label="c.name + ' (' + c.collectionNo + ')'" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="损坏描述" prop="damageDesc">
          <el-input v-model="formData.damageDesc" type="textarea" :rows="3" placeholder="请描述损坏情况" />
        </el-form-item>
        <el-form-item label="修复方案">
          <el-input v-model="formData.repairPlan" type="textarea" :rows="2" placeholder="请描述修复方案" />
        </el-form-item>
        <el-form-item label="预计完成">
          <el-date-picker v-model="formData.expectDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交工单</el-button>
      </template>
    </el-dialog>

    <!-- 派单弹窗 -->
    <el-dialog v-model="dispatchVisible" title="派单" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="处理人">
          <el-select v-model="dispatchForm.handlerId" placeholder="请选择处理人" style="width: 100%" filterable>
            <el-option v-for="u in userList" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDispatch">确认派单</el-button>
      </template>
    </el-dialog>

    <!-- 完成弹窗 -->
    <el-dialog v-model="completeVisible" title="完成修复" width="480px" destroy-on-close>
      <el-form ref="completeFormRef" :model="completeForm" label-width="90px">
        <el-form-item label="修复人">
          <el-input v-model="completeForm.repairer" placeholder="请输入修复人" />
        </el-form-item>
        <el-form-item label="修复单位">
          <el-input v-model="completeForm.repairUnit" placeholder="请输入修复单位" />
        </el-form-item>
        <el-form-item label="修复费用">
          <el-input-number v-model="completeForm.cost" :min="0" :precision="2" placeholder="请输入费用" style="width: 100%" />
        </el-form-item>
        <el-form-item label="修复方案">
          <el-input v-model="completeForm.repairPlan" type="textarea" :rows="3" placeholder="请描述修复方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewVisible" title="修复工单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号" :span="2">{{ viewData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称" :span="2">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ statusText(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="损坏描述" :span="2">{{ viewData.damageDesc }}</el-descriptions-item>
        <el-descriptions-item label="修复方案" :span="2">{{ viewData.repairPlan || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ viewData.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="修复人">{{ viewData.repairer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="修复单位">{{ viewData.repairUnit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="修复费用">{{ viewData.cost ? '¥' + viewData.cost : '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ viewData.submitterName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ viewData.completeDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRepairList, getRepairDetail, addRepair, deleteRepair } from '@/api/repair/order'
import request from '@/api/request'
import { getList as getCollectionList } from '@/api/collection'
import { getUserList } from '@/api/system/user'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const collectionList = ref([])
const userList = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const dispatchVisible = ref(false)
const completeVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)
const completeFormRef = ref(null)

const searchForm = reactive({ keyword: '', status: null })
const dispatchForm = reactive({ orderId: null, handlerId: null })
const completeForm = reactive({ repairer: '', repairUnit: '', cost: null, repairPlan: '' })

const formData = reactive({
  collectionId: null,
  collectionName: '',
  collectionNo: '',
  damageDesc: '',
  repairPlan: '',
  expectDate: '',
  remark: ''
})

const formRules = {
  collectionId: [{ required: true, message: '请选择藏品', trigger: 'change' }],
  damageDesc: [{ required: true, message: '请描述损坏情况', trigger: 'blur' }]
}

function statusText(status) {
  const map = { 0: '待派单', 1: '修复中', 2: '已完成', 3: '已取消' }
  return map[status] || '未知'
}

function statusType(status) {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: '' }
  return map[status] || 'info'
}

function onCollectionChange(id) {
  const collection = collectionList.value.find(c => c.id === id)
  if (collection) {
    formData.collectionName = collection.name
    formData.collectionNo = collection.collectionNo
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRepairList({
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

async function loadCollections() {
  try {
    const res = await getCollectionList({ current: 1, size: 100 })
    if (res.code === 200) {
      collectionList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载藏品失败', error)
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
    collectionId: null,
    collectionName: '',
    collectionNo: '',
    damageDesc: '',
    repairPlan: '',
    expectDate: '',
    remark: ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    await addRepair(formData)
    ElMessage.success('工单已创建')
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
    const res = await getRepairDetail(row.id)
    if (res.code === 200) {
      viewData.value = res.data || {}
      viewVisible.value = true
    }
  } catch (error) {
    console.error('加载详情失败', error)
  }
}

function handleDispatch(row) {
  dispatchForm.orderId = row.id
  dispatchForm.handlerId = null
  dispatchVisible.value = true
}

async function confirmDispatch() {
  if (!dispatchForm.handlerId) {
    ElMessage.warning('请选择处理人')
    return
  }
  try {
    await request({ url: `/repair/order/dispatch/${dispatchForm.orderId}`, method: 'put', params: { handlerId: dispatchForm.handlerId } })
    ElMessage.success('派单成功')
    dispatchVisible.value = false
    loadData()
  } catch (error) {
    console.error('派单失败', error)
  }
}

function handleComplete(row) {
  completeForm.orderId = row.id
  completeForm.repairer = ''
  completeForm.repairUnit = ''
  completeForm.cost = null
  completeForm.repairPlan = ''
  completeVisible.value = true
}

async function confirmComplete() {
  try {
    await request({
      url: `/repair/order/complete/${completeForm.orderId}`,
      method: 'put',
      params: completeForm
    })
    ElMessage.success('修复已完成')
    completeVisible.value = false
    loadData()
  } catch (error) {
    console.error('完成失败', error)
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确定要取消工单"${row.orderNo}"吗？`, '提示', { type: 'warning' })
  try {
    await request({ url: `/repair/order/cancel/${row.id}`, method: 'put' })
    ElMessage.success('工单已取消')
    loadData()
  } catch (error) {
    console.error('取消失败', error)
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除工单"${row.orderNo}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteRepair(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

onMounted(() => {
  loadData()
  loadCollections()
  loadUsers()
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
