<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">库房信息</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增库房</el-button>
    </div>
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="storageNo" label="库房编号" width="110" />
        <el-table-column prop="name" label="库房名称" min-width="120" />
        <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
        <el-table-column prop="area" label="面积" width="90">
          <template #default="{ row }">{{ row.area ? row.area + ' ㎡' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="safetyLevel" label="安全等级" width="90">
          <template #default="{ row }">
            <el-tag :type="getSafetyLevelType(row.safetyLevel)" size="small">
              {{ getSafetyLevelText(row.safetyLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="90">
          <template #default="{ row }">{{ row.capacity || 0 }} 件</template>
        </el-table-column>
        <el-table-column prop="currentCount" label="当前数量" width="90">
          <template #default="{ row }">
            <el-tooltip :content="`使用率: ${row.capacity ? Math.round((row.currentCount||0)/row.capacity*100) : 0}%`" placement="top">
              <span :style="{ color: row.currentCount >= row.capacity ? '#E6A23C' : '#67C23A' }">
                {{ row.currentCount || 0 }} 件
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="环境参数" width="120">
          <template #default="{ row }">
            <div v-if="row.temperature !== null || row.humidity !== null">
              <div>🌡️ {{ row.temperature !== null ? row.temperature + '℃' : '-' }}</div>
              <div>💧 {{ row.humidity !== null ? row.humidity + '%' : '-' }}</div>
            </div>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="负责人" width="90" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库房编号" prop="storageNo">
              <el-input v-model="formData.storageNo" placeholder="如 KC001" :disabled="isEdit" />
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
            <el-form-item label="位置">
              <el-input v-model="formData.location" placeholder="如 地下一层A区" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(㎡)">
              <el-input-number v-model="formData.area" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="安全等级">
              <el-select v-model="formData.safetyLevel" placeholder="请选择" style="width: 100%">
                <el-option label="一级（核心）" :value="1" />
                <el-option label="二级（重要）" :value="2" />
                <el-option label="三级（一般）" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容量(件)">
              <el-input-number v-model="formData.capacity" :min="1" :max="99999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 环境参数 -->
        <el-divider content-position="left">环境参数</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="温度(℃)">
              <el-input-number v-model="formData.temperature" :min="-50" :max="100" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="湿度(%)">
              <el-input-number v-model="formData.humidity" :min="0" :max="100" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert
          v-if="formData.temperature || formData.humidity"
          :title="`环境状态：${getEnvironmentStatus(formData.temperature, formData.humidity)}`"
          :type="getEnvironmentType(formData.temperature, formData.humidity)"
          :closable="false"
          style="margin-bottom: 16px"
        />

        <!-- 管理信息 -->
        <el-divider content-position="left">管理信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="formData.manager" placeholder="请输入负责人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="库房详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="库房编号">{{ viewData.storageNo }}</el-descriptions-item>
        <el-descriptions-item label="库房名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="位置" :span="2">{{ viewData.location || '无' }}</el-descriptions-item>
        <el-descriptions-item label="面积">{{ viewData.area ? viewData.area + ' ㎡' : '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="安全等级">
          <el-tag :type="getSafetyLevelType(viewData.safetyLevel)" size="small">
            {{ getSafetyLevelText(viewData.safetyLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="容量">{{ viewData.capacity }} 件</el-descriptions-item>
        <el-descriptions-item label="当前数量">{{ viewData.currentCount || 0 }} 件</el-descriptions-item>
        <el-descriptions-item label="温度">{{ viewData.temperature !== null ? viewData.temperature + '℃' : '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="湿度">{{ viewData.humidity !== null ? viewData.humidity + '%' : '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ viewData.manager || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ viewData.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'" size="small">
            {{ viewData.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="环境状态" v-if="viewData.temperature || viewData.humidity">
          <el-alert
            :title="getEnvironmentStatus(viewData.temperature, viewData.humidity)"
            :type="getEnvironmentType(viewData.temperature, viewData.humidity)"
            :closable="false"
            style="padding: 4px 8px"
          />
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
import { getStorageList, addStorage, updateStorage, deleteStorage } from '@/api/storage'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])

const pagination = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const viewData = ref({})
const formRef = ref(null)

const formData = reactive({
  id: null, storageNo: '', name: '', location: '', area: null, safetyLevel: 1,
  capacity: 100, temperature: 20, humidity: 50,
  manager: '', phone: '', status: 1, remark: ''
})

const formRules = {
  storageNo: [{ required: true, message: '请输入库房编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入库房名称', trigger: 'blur' }]
}

// 获取安全等级文本
function getSafetyLevelText(level) {
  const map = { 1: '一级', 2: '二级', 3: '三级' }
  return map[level] || '未知'
}

// 获取安全等级标签类型
function getSafetyLevelType(level) {
  const map = { 1: 'danger', 2: 'warning', 3: 'info' }
  return map[level] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getStorageList({
      current: pagination.current,
      size: pagination.size
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

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增库房'
  Object.assign(formData, {
    id: null, storageNo: '', name: '', location: '', area: null, safetyLevel: 1,
    capacity: 100, temperature: 20, humidity: 50,
    manager: '', phone: '', status: 1, remark: ''
  })
  dialogVisible.value = true
}

// 获取环境状态描述
function getEnvironmentStatus(temperature, humidity) {
  if (!temperature && !humidity) return '未设置'
  const temp = parseFloat(temperature) || 0
  const hum = parseFloat(humidity) || 0
  
  // 博物馆标准环境：温度 18-22℃，湿度 45-55%
  const tempOk = temp >= 18 && temp <= 22
  const humOk = hum >= 45 && hum <= 55
  
  if (tempOk && humOk) return '适宜（符合博物馆标准）'
  if (tempOk && !humOk) return `湿度${hum > 55 ? '偏高' : '偏低'}（建议45%-55%）`
  if (!tempOk && humOk) return `温度${temp > 22 ? '偏高' : '偏低'}（建议18℃-22℃）`
  return '温湿度均不适宜（建议温度18℃-22℃，湿度45%-55%）'
}

// 获取环境状态类型
function getEnvironmentType(temperature, humidity) {
  if (!temperature && !humidity) return 'info'
  const temp = parseFloat(temperature) || 0
  const hum = parseFloat(humidity) || 0
  const tempOk = temp >= 18 && temp <= 22
  const humOk = hum >= 45 && hum <= 55
  return (tempOk && humOk) ? 'success' : 'warning'
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
  try {
    if (isEdit.value) {
      await updateStorage(formData)
      ElMessage.success('修改成功')
    } else {
      await addStorage(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除库房"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteStorage(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
