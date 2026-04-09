<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">库房信息</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增库房</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="库房编号" width="120" />
        <el-table-column prop="name" label="库房名称" width="140" />
        <el-table-column prop="area" label="面积(㎡)" width="100" />
        <el-table-column prop="level" label="安全等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.level === '一级' ? 'danger' : row.level === '二级' ? 'warning' : 'info'" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="temp" label="温度(℃)" width="100" />
        <el-table-column prop="humidity" label="湿度(%)" width="100" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库房编号" prop="code">
              <el-input v-model="formData.code" placeholder="如 KF-E" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库房名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入库房名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="面积(㎡)">
              <el-input-number v-model="formData.area" :min="1" :max="99999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全等级" prop="level">
              <el-select v-model="formData.level" placeholder="请选择" style="width: 100%">
                <el-option label="一级" value="一级" />
                <el-option label="二级" value="二级" />
                <el-option label="三级" value="三级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="温度(℃)">
              <el-input v-model="formData.temp" placeholder="如 20℃" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="湿度(%)">
              <el-input v-model="formData.humidity" placeholder="如 55%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="负责人">
          <el-input v-model="formData.manager" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="库房详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="库房编号">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="库房名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="面积">{{ viewData.area }} ㎡</el-descriptions-item>
        <el-descriptions-item label="安全等级">{{ viewData.level }}</el-descriptions-item>
        <el-descriptions-item label="温度">{{ viewData.temp }}</el-descriptions-item>
        <el-descriptions-item label="湿度">{{ viewData.humidity }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ viewData.manager }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'" size="small">{{ viewData.status === 1 ? '正常' : '停用' }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const submitLoading = ref(false)
let nextId = 5

const mockData = ref([
  { id: 1, code: 'KF-A', name: 'A区库房', area: 500, level: '一级', temp: '20℃', humidity: '55%', manager: '王馆长', status: 1 },
  { id: 2, code: 'KF-B', name: 'B区库房', area: 380, level: '一级', temp: '20℃', humidity: '50%', manager: '李主任', status: 1 },
  { id: 3, code: 'KF-C', name: 'C区库房', area: 260, level: '二级', temp: '22℃', humidity: '55%', manager: '张主管', status: 1 },
  { id: 4, code: 'KF-D', name: '临时库房', area: 120, level: '三级', temp: '25℃', humidity: '60%', manager: '赵助理', status: 0 }
])

const tableData = ref(mockData)

const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const viewData = ref({})
const formRef = ref(null)

const formData = reactive({ id: null, code: '', name: '', area: 100, level: '', temp: '', humidity: '', manager: '', status: 1 })

const formRules = {
  code: [{ required: true, message: '请输入库房编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入库房名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择安全等级', trigger: 'change' }]
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增库房'
  Object.assign(formData, { id: null, code: '', name: '', area: 100, level: '', temp: '', humidity: '', manager: '', status: 1 })
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑库房'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  if (isEdit.value) {
    const idx = mockData.value.findIndex(r => r.id === formData.id)
    if (idx !== -1) mockData.value.splice(idx, 1, { ...formData })
    ElMessage.success('修改成功')
  } else {
    mockData.value.push({ ...formData, id: nextId++ })
    ElMessage.success('新增成功')
  }
  submitLoading.value = false
  dialogVisible.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除库房"${row.name}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
</style>
