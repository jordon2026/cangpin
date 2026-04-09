<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">修复工单</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 创建工单</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="工单编号" width="160" />
        <el-table-column prop="collectionName" label="藏品名称" min-width="140" />
        <el-table-column prop="type" label="修复类型" width="120" />
        <el-table-column prop="level" label="紧急程度" width="100">
          <template #default="{ row }">
            <el-tag :type="row.level === '紧急' ? 'danger' : 'info'" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="修复人员" width="100" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)" :disabled="row.status === '修复中'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建工单弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建修复工单" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="藏品名称" prop="collectionName">
          <el-input v-model="formData.collectionName" placeholder="请输入藏品名称" />
        </el-form-item>
        <el-form-item label="藏品编号" prop="collectionCode">
          <el-input v-model="formData.collectionCode" placeholder="请输入藏品编号" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="修复类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择" style="width: 100%">
                <el-option label="陶瓷修复" value="陶瓷修复" />
                <el-option label="书画修复" value="书画修复" />
                <el-option label="青铜器修复" value="青铜器修复" />
                <el-option label="玉器抛光" value="玉器抛光" />
                <el-option label="漆器修复" value="漆器修复" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急程度" prop="level">
              <el-select v-model="formData.level" placeholder="请选择" style="width: 100%">
                <el-option label="紧急" value="紧急" />
                <el-option label="一般" value="一般" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="损坏描述">
          <el-input v-model="formData.damage" type="textarea" :rows="2" placeholder="请描述损坏情况" />
        </el-form-item>
        <el-form-item label="修复人员">
          <el-input v-model="formData.handler" placeholder="请输入修复人员" />
        </el-form-item>
        <el-form-item label="预计工期">
          <el-input v-model="formData.duration" placeholder="如 30天" />
        </el-form-item>
        <el-form-item label="修复方案">
          <el-input v-model="formData.plan" type="textarea" :rows="3" placeholder="请描述修复方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交工单</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewVisible" title="修复工单详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号" :span="2">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.collectionName }}</el-descriptions-item>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionCode }}</el-descriptions-item>
        <el-descriptions-item label="修复类型">{{ viewData.type }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <el-tag :type="viewData.level === '紧急' ? 'danger' : 'info'" size="small">{{ viewData.level }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="修复人员">{{ viewData.handler }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ viewData.startDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(viewData.status)" size="small">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预计工期">{{ viewData.duration || '未设定' }}</el-descriptions-item>
        <el-descriptions-item label="修复方案" :span="2">{{ viewData.plan || '暂无方案' }}</el-descriptions-item>
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
  { id: 1, code: 'XF-20260407-001', collectionName: '三彩骆驼俑', collectionCode: 'ZW-2026-0005', type: '陶瓷修复', level: '一般', handler: '陈师傅', startDate: '2026-04-07', status: '修复中', duration: '45天', damage: '骆驼俑颈部断裂，四足微裂', plan: '对骆驼俑断裂处进行粘合修复，补全缺失釉面。' },
  { id: 2, code: 'XF-20260401-002', collectionName: '白玉璧', collectionCode: 'ZW-2026-0004', type: '玉器抛光', level: '紧急', handler: '刘师傅', startDate: '2026-04-01', status: '待修复', duration: '15天', damage: '玉璧表面有多处细微划痕，边缘有磕碰', plan: '对玉璧表面进行细致抛光，去除细微划痕。' }
])

const tableData = ref(mockData)
const dialogVisible = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const formRef = ref(null)

const formData = reactive({
  collectionName: '', collectionCode: '', type: '', level: '', damage: '', handler: '', duration: '', plan: ''
})

const formRules = {
  collectionName: [{ required: true, message: '请输入藏品名称', trigger: 'blur' }],
  collectionCode: [{ required: true, message: '请输入藏品编号', trigger: 'blur' }],
  type: [{ required: true, message: '请选择修复类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择紧急程度', trigger: 'change' }]
}

function statusType(s) {
  return s === '修复中' ? 'warning' : s === '已完成' ? 'success' : s === '待修复' ? 'info' : 'danger'
}

function handleAdd() {
  Object.assign(formData, { collectionName: '', collectionCode: '', type: '', level: '', damage: '', handler: '', duration: '', plan: '' })
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
  const code = `XF-${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}-${String(nextId).padStart(3,'0')}`
  mockData.value.unshift({ ...formData, id: nextId++, code, startDate: new Date().toISOString().split('T')[0], status: '待修复' })
  submitLoading.value = false
  dialogVisible.value = false
  ElMessage.success('工单已创建')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除工单"${row.code}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
</style>
