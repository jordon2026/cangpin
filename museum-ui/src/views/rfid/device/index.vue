<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">设备管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增设备</el-button>
    </div>
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable style="width: 180px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="在线" value="在线" />
            <el-option label="离线" value="离线" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="code" label="设备编号" width="130" />
        <el-table-column prop="name" label="设备名称" min-width="160" />
        <el-table-column prop="type" label="设备类型" width="130">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="安装位置" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '在线' ? 'success' : 'danger'" size="small">
              <span class="status-dot" :class="row.status === '在线' ? 'dot-online' : 'dot-offline'"></span>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="handleDiagnose(row)">诊断</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50]" :total="filteredData.length" layout="total, sizes, prev, pager, next, jumper" background />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="设备编号" prop="code">
          <el-input v-model="formData.code" placeholder="如 RFID-R-003" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="固定式读写器" value="固定式读写器" />
            <el-option label="手持读写器" value="手持读写器" />
            <el-option label="通道天线" value="通道天线" />
            <el-option label="温湿度传感器" value="温湿度传感器" />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="formData.ip" placeholder="如 192.168.1.103" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="设备备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设备诊断弹窗 -->
    <el-dialog v-model="diagnoseVisible" title="设备诊断" width="480px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="设备名称">{{ diagnoseData.name }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ diagnoseData.code }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="diagnoseData.status === '在线' ? 'success' : 'danger'" size="small">{{ diagnoseData.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="连接状态">
          <el-tag :type="diagnoseData.status === '在线' ? 'success' : 'danger'" size="small">
            {{ diagnoseData.status === '在线' ? '连接正常' : '连接失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="信号强度">{{ diagnoseData.status === '在线' ? '强 (-35dBm)' : '无信号' }}</el-descriptions-item>
        <el-descriptions-item label="固件版本">v2.1.3</el-descriptions-item>
        <el-descriptions-item label="最后心跳">{{ diagnoseData.heartbeat || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const submitLoading = ref(false)
let nextId = 5

const mockData = ref([
  { id: 1, code: 'RFID-R-001', name: 'A区库房固定读写器', type: '固定式读写器', location: 'A区入口', ip: '192.168.1.101', status: '在线', remark: '', heartbeat: '2026-04-07 19:20' },
  { id: 2, code: 'RFID-R-002', name: 'B区库房固定读写器', type: '固定式读写器', location: 'B区入口', ip: '192.168.1.102', status: '在线', remark: '', heartbeat: '2026-04-07 19:20' },
  { id: 3, code: 'RFID-H-001', name: '手持终端-01', type: '手持读写器', location: '移动设备', ip: '-', status: '离线', remark: '电量不足', heartbeat: '2026-04-07 14:00' },
  { id: 4, code: 'RFID-A-001', name: '大门通道门禁', type: '通道天线', location: '主入口', ip: '192.168.1.201', status: '在线', remark: '', heartbeat: '2026-04-07 19:20' }
])

const dialogVisible = ref(false)
const diagnoseVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const diagnoseData = ref({})

const searchForm = reactive({ name: '', status: '' })
const pagination = reactive({ page: 1, size: 10 })

const filteredData = computed(() => {
  let data = mockData.value
  if (searchForm.name) data = data.filter(r => r.name.includes(searchForm.name))
  if (searchForm.status) data = data.filter(r => r.status === searchForm.status)
  return data
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredData.value.slice(start, start + pagination.size)
})

const formData = reactive({ id: null, code: '', name: '', type: '', location: '', ip: '', remark: '' })

const formRules = {
  code: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入安装位置', trigger: 'blur' }]
}

function handleSearch() { pagination.page = 1 }
function handleReset() { Object.assign(searchForm, { name: '', status: '' }); pagination.page = 1 }

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增设备'
  Object.assign(formData, { id: null, code: '', name: '', type: '', location: '', ip: '', remark: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑设备'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  await new Promise(r => setTimeout(r, 300))
  if (isEdit.value) {
    const idx = mockData.value.findIndex(r => r.id === formData.id)
    if (idx !== -1) mockData.value.splice(idx, 1, { ...formData })
    ElMessage.success('修改成功')
  } else {
    mockData.value.push({ ...formData, id: nextId++, status: '离线', heartbeat: '-' })
    ElMessage.success('设备已添加')
  }
  submitLoading.value = false
  dialogVisible.value = false
}

function handleDiagnose(row) {
  diagnoseData.value = { ...row }
  diagnoseVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除设备"${row.name}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
.dot-online { background: #67c23a; }
.dot-offline { background: #f56c6c; }
</style>
