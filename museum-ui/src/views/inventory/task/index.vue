<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">盘点任务</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 创建任务</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="任务编号" width="160" />
        <el-table-column prop="name" label="任务名称" min-width="180" />
        <el-table-column prop="storage" label="盘点范围" width="120" />
        <el-table-column prop="method" label="盘点方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.method === 'RFID' ? 'primary' : 'info'" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="100" />
        <el-table-column prop="date" label="创建日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)" :disabled="row.status === '进行中'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建任务弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建盘点任务" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="formData.name" placeholder="如 A区库房季度盘点" />
        </el-form-item>
        <el-form-item label="盘点范围" prop="storage">
          <el-select v-model="formData.storage" placeholder="请选择范围" style="width: 100%">
            <el-option label="A区库房" value="A区库房" />
            <el-option label="B区库房" value="B区库房" />
            <el-option label="C区库房" value="C区库房" />
            <el-option label="全部库房" value="全部库房" />
          </el-select>
        </el-form-item>
        <el-form-item label="盘点方式" prop="method">
          <el-radio-group v-model="formData.method">
            <el-radio value="RFID">RFID扫描</el-radio>
            <el-radio value="手工">手工盘点</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划日期" prop="planDate">
          <el-date-picker v-model="formData.planDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="formData.startDate" type="date" value-format="YYYY-MM-DD" placeholder="计划开始" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="formData.endDate" type="date" value-format="YYYY-MM-DD" placeholder="计划结束" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
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
    <el-dialog v-model="viewVisible" title="盘点任务详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务编号" :span="2">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="任务名称" :span="2">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="盘点范围">{{ viewData.storage }}</el-descriptions-item>
        <el-descriptions-item label="盘点方式">{{ viewData.method }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ viewData.creator }}</el-descriptions-item>
        <el-descriptions-item label="创建日期">{{ viewData.date }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const submitLoading = ref(false)
let nextId = 4

const mockData = ref([
  { id: 1, code: 'PD-20260407-001', name: 'A区库房季度盘点', storage: 'A区库房', method: 'RFID', creator: '李主管', date: '2026-04-07', status: '进行中', remark: '' },
  { id: 2, code: 'PD-20260401-002', name: 'B区库房月度盘点', storage: 'B区库房', method: 'RFID', creator: '张主管', date: '2026-04-01', status: '已完成', remark: '' },
  { id: 3, code: 'PD-20260325-003', name: '临时抽盘-陶瓷区', storage: 'A区-1排', method: '手工', creator: '王助理', date: '2026-03-25', status: '已完成', remark: '' }
])

const tableData = ref(mockData)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

const formData = reactive({ name: '', storage: '', method: 'RFID', planDate: '', startDate: '', endDate: '', remark: '' })

const formRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  storage: [{ required: true, message: '请选择盘点范围', trigger: 'change' }],
  method: [{ required: true, message: '请选择盘点方式', trigger: 'change' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

function statusType(s) {
  return s === '进行中' ? 'warning' : s === '已完成' ? 'success' : s === '待开始' ? 'info' : 'danger'
}

function handleAdd() {
  Object.assign(formData, { name: '', storage: '', method: 'RFID', planDate: '', startDate: '', endDate: '', remark: '' })
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  const now = new Date()
  const code = `PD-${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}-${String(nextId).padStart(3,'0')}`
  mockData.value.unshift({ ...formData, id: nextId++, code, creator: '管理员', date: formData.planDate, status: '待开始' })
  submitLoading.value = false
  dialogVisible.value = false
  ElMessage.success('盘点任务已创建')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除任务"${row.name}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
</style>
