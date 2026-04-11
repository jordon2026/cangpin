<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">外借申请</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增申请</el-button>
    </div>

    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="申请编号/藏品名/借方单位" clearable style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="申请状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已撤回" :value="3" />
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
        <el-table-column prop="applyNo" label="申请编号" width="180" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="140" />
        <el-table-column prop="collectionNo" label="藏品编号" width="140" />
        <el-table-column prop="borrowerOrg" label="借方单位" width="140" />
        <el-table-column prop="purpose" label="借展目的" width="100" />
        <el-table-column prop="startDate" label="借出日期" width="120" />
        <el-table-column prop="endDate" label="归还日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returned" label="归还" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" :type="row.returned === 1 ? 'success' : 'warning'" size="small">
              {{ row.returned === 1 ? '已还' : '未还' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="success" link size="small" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleReject(row)">拒绝</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="handleCancel(row)">撤回</el-button>
            <el-button v-if="row.status === 1 && row.returned === 0" type="primary" link size="small" @click="handleReturn(row)">归还</el-button>
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

    <!-- 新增申请弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增外借申请" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="选择藏品" prop="collectionId">
          <el-select v-model="formData.collectionId" placeholder="请选择藏品" style="width: 100%" filterable @change="onCollectionChange">
            <el-option v-for="c in collectionList" :key="c.id" :label="c.name + ' (' + c.collectionNo + ')'" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="借方单位" prop="borrowerOrg">
          <el-input v-model="formData.borrowerOrg" placeholder="请输入借方单位名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="formData.borrowerContact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.borrowerPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="借展目的" prop="purpose">
          <el-select v-model="formData.purpose" placeholder="请选择" style="width: 100%">
            <el-option label="特展借展" value="特展借展" />
            <el-option label="联展交流" value="联展交流" />
            <el-option label="巡回展览" value="巡回展览" />
            <el-option label="学术研究" value="学术研究" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="借出日期" prop="startDate">
              <el-date-picker v-model="formData.startDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="借出日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归还日期" prop="endDate">
              <el-date-picker v-model="formData.endDate" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="归还日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="补充说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewVisible" title="外借申请详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请编号" :span="2">{{ viewData.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称" :span="2">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ statusText(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="借方单位" :span="2">{{ viewData.borrowerOrg }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ viewData.borrowerContact }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewData.borrowerPhone }}</el-descriptions-item>
        <el-descriptions-item label="借展目的">{{ viewData.purpose }}</el-descriptions-item>
        <el-descriptions-item label="归还状态">
          <el-tag v-if="viewData.status === 1" :type="viewData.returned === 1 ? 'success' : 'warning'" size="small">
            {{ viewData.returned === 1 ? '已归还' : '未归还' }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="借出日期">{{ viewData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="归还日期">{{ viewData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ viewData.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ viewData.approverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ viewData.approvalTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ viewData.approvalOpinion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审批意见弹窗 -->
    <el-dialog v-model="opinionVisible" :title="opinionType === 'approve' ? '审批通过' : '审批拒绝'" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="opinion" type="textarea" :rows="3" :placeholder="opinionType === 'approve' ? '请输入审批意见' : '请输入拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="opinionVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOpinion">确认</el-button>
      </template>
    </el-dialog>

    <!-- 归还弹窗 -->
    <el-dialog v-model="returnVisible" title="归还藏品" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="归还人">
          <el-input v-model="returnForm.returner" placeholder="请输入归还人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="returnForm.remark" type="textarea" :rows="3" placeholder="请输入归还备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReturn">确认归还</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getLoanList, getLoanDetail, addLoan, cancelLoan } from '@/api/loan/apply'
import request from '@/api/request'
import { getList as getCollectionList } from '@/api/collection'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const collectionList = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const opinionVisible = ref(false)
const returnVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

const opinionType = ref('approve')
const opinion = ref('')
const opinionId = ref(null)

const returnForm = reactive({ returner: '', remark: '' })

const searchForm = reactive({ keyword: '', status: null })

const formData = reactive({
  collectionId: null,
  collectionName: '',
  collectionNo: '',
  borrowerOrg: '',
  borrowerContact: '',
  borrowerPhone: '',
  purpose: '',
  startDate: '',
  endDate: '',
  remark: ''
})

const formRules = {
  collectionId: [{ required: true, message: '请选择藏品', trigger: 'change' }],
  borrowerOrg: [{ required: true, message: '请输入借方单位', trigger: 'blur' }],
  purpose: [{ required: true, message: '请选择借展目的', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择借出日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择归还日期', trigger: 'change' }]
}

function statusText(status) {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝', 3: '已撤回' }
  return map[status] || '未知'
}

function statusType(status) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
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
    const res = await getLoanList({
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
    borrowerOrg: '',
    borrowerContact: '',
    borrowerPhone: '',
    purpose: '',
    startDate: '',
    endDate: '',
    remark: ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    await addLoan(formData)
    ElMessage.success('申请已提交')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleView(row) {
  try {
    const res = await getLoanDetail(row.id)
    if (res.code === 200) {
      viewData.value = res.data || {}
      viewVisible.value = true
    }
  } catch (error) {
    console.error('加载详情失败', error)
  }
}

function handleApprove(row) {
  opinionType.value = 'approve'
  opinionId.value = row.id
  opinion.value = ''
  opinionVisible.value = true
}

function handleReject(row) {
  opinionType.value = 'reject'
  opinionId.value = row.id
  opinion.value = ''
  opinionVisible.value = true
}

async function confirmOpinion() {
  try {
    const url = opinionType.value === 'approve' ? '/loan/apply/approve' : '/loan/apply/reject'
    await request({ url: `${url}/${opinionId.value}`, method: 'put', params: { opinion: opinion.value } })
    ElMessage.success(opinionType.value === 'approve' ? '审批已通过' : '已拒绝申请')
    opinionVisible.value = false
    loadData()
  } catch (error) {
    console.error('审批失败', error)
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确定要撤回申请"${row.applyNo}"吗？`, '提示', { type: 'warning' })
  try {
    await cancelLoan(row.id)
    ElMessage.success('申请已撤回')
    loadData()
  } catch (error) {
    console.error('撤回失败', error)
  }
}

function handleReturn(row) {
  returnForm.returner = ''
  returnForm.remark = ''
  returnForm.id = row.id
  returnVisible.value = true
}

async function confirmReturn() {
  try {
    await request({
      url: `/loan/apply/return/${returnForm.id}`,
      method: 'put',
      params: { returner: returnForm.returner, remark: returnForm.remark }
    })
    ElMessage.success('藏品已归还')
    returnVisible.value = false
    loadData()
  } catch (error) {
    console.error('归还失败', error)
  }
}

onMounted(() => {
  loadData()
  loadCollections()
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
