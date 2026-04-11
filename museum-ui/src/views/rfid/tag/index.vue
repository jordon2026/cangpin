<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">标签管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增标签</el-button>
    </div>
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="标签编码/EPC/藏品名" clearable style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="使用状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px" @change="handleSearch">
            <el-option label="未绑定" :value="0" />
            <el-option label="已绑定" :value="1" />
            <el-option label="已挂失" :value="2" />
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
        <el-table-column prop="tagCode" label="标签编码" width="180" show-overflow-tooltip />
        <el-table-column prop="epc" label="EPC编码" width="200" show-overflow-tooltip />
        <el-table-column prop="collectionName" label="关联藏品" min-width="140">
          <template #default="{ row }">
            <span :class="{ 'text-muted': !row.collectionId }">{{ row.collectionName || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="使用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bindTime" label="绑定日期" width="160" />
        <el-table-column prop="remark" label="备注" min-width="100" />
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="warning" link size="small" @click="handleUnbind(row)">解绑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="标签编码" prop="tagCode">
          <el-input v-model="formData.tagCode" placeholder="请输入标签编码" />
        </el-form-item>
        <el-form-item label="EPC编码" prop="epc">
          <el-input v-model="formData.epc" placeholder="请输入EPC编码" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="标签备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTagList, addTag, updateTag, unbindTag, deleteTag } from '@/api/rfid/tag'

const loading = ref(false)
const submitLoading = ref(false)

const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const searchForm = reactive({ keyword: '', status: null })

const formData = reactive({ id: null, tagCode: '', epc: '', remark: '' })

const formRules = {
  tagCode: [{ required: true, message: '请输入标签编码', trigger: 'blur' }],
  epc: [{ required: true, message: '请输入EPC编码', trigger: 'blur' }]
}

function statusText(status) {
  const map = { 0: '未绑定', 1: '已绑定', 2: '已挂失' }
  return map[status] || '未知'
}

function statusType(status) {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTagList({
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

function handleSearch() { pagination.page = 1; loadData() }
function handleReset() {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.page = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增标签'
  Object.assign(formData, { id: null, tagCode: '', epc: '', remark: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑标签'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateTag(formData)
      ElMessage.success('修改成功')
    } else {
      await addTag(formData)
      ElMessage.success('标签已添加')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败', error)
  } finally {
    submitLoading.value = false
  }
}

async function handleUnbind(row) {
  await ElMessageBox.confirm(`确定要解绑标签"${row.tagCode}"吗？`, '提示', { type: 'warning' })
  try {
    await unbindTag(row.id)
    ElMessage.success('标签已解绑')
    loadData()
  } catch (error) {
    console.error('解绑失败', error)
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除标签"${row.tagCode}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteTag(row.id)
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
.search-card { margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.text-muted { color: var(--el-text-color-placeholder); }
</style>
