<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">藏品档案</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增藏品
      </el-button>
    </div>
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="名称/编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="藏品分类">
          <el-select v-model="searchForm.category" placeholder="请选择" clearable style="width: 160px">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="借出" :value="2" />
            <el-option label="修复中" :value="3" />
            <el-option label="展出" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="collectionNo" label="藏品编号" width="140" />
        <el-table-column prop="name" label="藏品名称" min-width="160" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="era" label="年代" width="120" />
        <el-table-column prop="material" label="材质" width="100" />
        <el-table-column prop="dimensions" label="尺寸" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
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
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="藏品名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入藏品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="藏品编号" prop="collectionNo">
              <el-input v-model="formData.collectionNo" placeholder="请输入藏品编号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="formData.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年代">
              <el-input v-model="formData.era" placeholder="如：清代" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="材质">
              <el-input v-model="formData.material" placeholder="如：瓷器" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="尺寸">
              <el-input v-model="formData.dimensions" placeholder="如：高45cm 口径12cm" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="重量">
              <el-input v-model="formData.weight" placeholder="如：2.5kg" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="正常" :value="1" />
                <el-option label="借出" :value="2" />
                <el-option label="修复中" :value="3" />
                <el-option label="展出" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入藏品描述" />
        </el-form-item>
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
    <el-dialog v-model="viewVisible" title="藏品详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="藏品编号">{{ viewData.collectionNo }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ viewData.category }}</el-descriptions-item>
        <el-descriptions-item label="年代">{{ viewData.era }}</el-descriptions-item>
        <el-descriptions-item label="材质">{{ viewData.material }}</el-descriptions-item>
        <el-descriptions-item label="尺寸">{{ viewData.dimensions }}</el-descriptions-item>
        <el-descriptions-item label="重量">{{ viewData.weight }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)" size="small">
            {{ getStatusText(viewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ viewData.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCollectionList, addCollection, updateCollection, deleteCollection, getCategories } from '@/api/collection'

const loading = ref(false)
const submitLoading = ref(false)
const categories = ref(['陶瓷', '青铜器', '书画', '玉器', '金银器', '漆器', '杂项'])
const tableData = ref([])

const searchForm = reactive({ keyword: '', category: '', status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const dialogTitle = ref('')
const viewData = ref({})

const formData = reactive({
  id: null, name: '', collectionNo: '', category: '', era: '',
  material: '', dimensions: '', weight: '', status: 1, description: '', remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入藏品名称', trigger: 'blur' }],
  collectionNo: [{ required: true, message: '请输入藏品编号', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function getStatusType(status) {
  const map = { 1: 'success', 2: 'warning', 3: 'info', 4: 'primary' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { 1: '正常', 2: '借出', 3: '修复中', 4: '展出' }
  return map[status] || '未知'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCollectionList({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
      category: searchForm.category,
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

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200 && res.data.length > 0) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = null
  pagination.current = 1
  loadData()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增藏品'
  Object.assign(formData, {
    id: null, name: '', collectionNo: '', category: '', era: '',
    material: '', dimensions: '', weight: '', status: 1, description: '', remark: ''
  })
  dialogVisible.value = true
}

function handleView(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑藏品'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCollection(formData)
      ElMessage.success('修改成功')
    } else {
      await addCollection(formData)
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
  await ElMessageBox.confirm(`确定要删除藏品"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteCollection(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: var(--radius-md); }
.table-card { border-radius: var(--radius-md); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
