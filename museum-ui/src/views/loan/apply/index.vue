<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">外借申请</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增申请</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="申请编号" width="160" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="140" />
        <el-table-column prop="borrower" label="借方单位" width="160" />
        <el-table-column prop="purpose" label="借展目的" width="120" />
        <el-table-column prop="startDate" label="借出日期" width="120" />
        <el-table-column prop="endDate" label="归还日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === '待审批'" type="warning" link size="small" @click="handleCancel(row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增申请弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增外借申请" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="藏品名称" prop="collectionName">
          <el-input v-model="formData.collectionName" placeholder="请输入藏品名称" />
        </el-form-item>
        <el-form-item label="藏品编号" prop="collectionCode">
          <el-input v-model="formData.collectionCode" placeholder="请输入藏品编号" />
        </el-form-item>
        <el-form-item label="借方单位" prop="borrower">
          <el-input v-model="formData.borrower" placeholder="请输入借方单位名称" />
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
        <el-form-item label="经办人">
          <el-input v-model="formData.operator" placeholder="请输入经办人" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="借出日期" prop="startDate">
              <el-date-picker v-model="formData.startDate" type="date" value-format="YYYY-MM-DD" placeholder="借出日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归还日期" prop="endDate">
              <el-date-picker v-model="formData.endDate" type="date" value-format="YYYY-MM-DD" placeholder="归还日期" style="width: 100%" />
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
    <el-dialog v-model="viewVisible" title="外借申请详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请编号" :span="2">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionCode }}</el-descriptions-item>
        <el-descriptions-item label="借方单位" :span="2">{{ viewData.borrower }}</el-descriptions-item>
        <el-descriptions-item label="借展目的">{{ viewData.purpose }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="借出日期">{{ viewData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="归还日期">{{ viewData.endDate }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const submitLoading = ref(false)
let nextId = 3

const mockData = ref([
  { id: 1, code: 'WJ-20260407-001', collectionName: '山水图卷', collectionCode: 'ZW-2026-0003', borrower: '国家博物馆', purpose: '特展借展', startDate: '2026-04-10', endDate: '2026-07-10', status: '待审批', remark: '' },
  { id: 2, code: 'WJ-20260320-002', collectionName: '青花瓷梅瓶', collectionCode: 'ZW-2026-0001', borrower: '上海博物馆', purpose: '联展交流', startDate: '2026-03-25', endDate: '2026-06-25', status: '借出中', remark: '联展期间注意防震' }
])

const tableData = ref(mockData)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

const formData = reactive({
  collectionName: '', collectionCode: '', borrower: '', purpose: '', operator: '', startDate: '', endDate: '', remark: ''
})

const formRules = {
  collectionName: [{ required: true, message: '请输入藏品名称', trigger: 'blur' }],
  collectionCode: [{ required: true, message: '请输入藏品编号', trigger: 'blur' }],
  borrower: [{ required: true, message: '请输入借方单位', trigger: 'blur' }],
  purpose: [{ required: true, message: '请选择借展目的', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择借出日期', trigger: 'change' }],
  endDate: [
    { required: true, message: '请选择归还日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && formData.startDate && value <= formData.startDate) {
          callback(new Error('归还日期必须晚于借出日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

function statusType(s) {
  return s === '待审批' ? 'warning' : s === '借出中' ? 'primary' : s === '已归还' ? 'success' : s === '已驳回' ? 'danger' : 'info'
}

function handleAdd() {
  Object.assign(formData, { collectionName: '', collectionCode: '', borrower: '', purpose: '', operator: '', startDate: '', endDate: '', remark: '' })
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  // 二次校验日期范围
  if (formData.endDate && formData.startDate && formData.endDate <= formData.startDate) {
    ElMessage.warning('归还日期必须晚于借出日期')
    return
  }
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  const now = new Date()
  const code = `WJ-${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}-${String(nextId).padStart(3,'0')}`
  mockData.value.unshift({ ...formData, id: nextId++, code, status: '待审批' })
  submitLoading.value = false
  dialogVisible.value = false
  ElMessage.success('申请已提交')
}

async function handleCancel(row) {
  await ElMessageBox.confirm(`确定要撤回申请"${row.code}"吗？`, '提示', { type: 'warning' })
  const item = mockData.value.find(r => r.id === row.id)
  if (item) item.status = '已撤回'
  ElMessage.success('申请已撤回')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
</style>
