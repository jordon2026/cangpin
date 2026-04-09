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
        <el-form-item label="藏品名称">
          <el-input v-model="searchForm.name" placeholder="请输入藏品名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="藏品编号">
          <el-input v-model="searchForm.code" placeholder="请输入藏品编号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="藏品分类">
          <el-select v-model="searchForm.category" placeholder="请选择" clearable style="width: 160px">
            <el-option label="陶瓷" value="陶瓷" />
            <el-option label="书画" value="书画" />
            <el-option label="青铜器" value="青铜器" />
            <el-option label="玉器" value="玉器" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
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
        <el-table-column prop="code" label="藏品编号" width="140" />
        <el-table-column prop="name" label="藏品名称" min-width="160" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="level" label="级别" width="100" />
        <el-table-column prop="era" label="年代" width="120" />
        <el-table-column prop="storage" label="存放位置" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '在库' ? 'success' : row.status === '外借' ? 'warning' : 'info'" size="small">
              {{ row.status }}
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
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredData.length"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="藏品名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入藏品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="藏品编号" prop="code">
              <el-input v-model="formData.code" placeholder="请输入藏品编号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="formData.category" placeholder="请选择分类" style="width: 100%">
                <el-option label="陶瓷" value="陶瓷" />
                <el-option label="书画" value="书画" />
                <el-option label="青铜器" value="青铜器" />
                <el-option label="玉器" value="玉器" />
                <el-option label="漆器" value="漆器" />
                <el-option label="杂项" value="杂项" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="级别" prop="level">
              <el-select v-model="formData.level" placeholder="请选择级别" style="width: 100%">
                <el-option label="一级" value="一级" />
                <el-option label="二级" value="二级" />
                <el-option label="三级" value="三级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年代">
              <el-input v-model="formData.era" placeholder="请输入年代" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置">
              <el-input v-model="formData.storage" placeholder="请输入存放位置" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入藏品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="藏品详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="藏品编号">{{ viewData.code }}</el-descriptions-item>
        <el-descriptions-item label="藏品名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ viewData.category }}</el-descriptions-item>
        <el-descriptions-item label="级别">{{ viewData.level }}</el-descriptions-item>
        <el-descriptions-item label="年代">{{ viewData.era }}</el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ viewData.storage }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === '在库' ? 'success' : viewData.status === '外借' ? 'warning' : 'info'" size="small">
            {{ viewData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ viewData.description || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)

const mockData = ref([
  { id: 1, code: 'ZW-2026-0001', name: '青花瓷梅瓶', category: '陶瓷', level: '一级', era: '明代', storage: 'A区-1排-3列', status: '在库', description: '明代景德镇窑青花瓷梅瓶，高38cm，口径6cm。' },
  { id: 2, code: 'ZW-2026-0002', name: '青铜鼎', category: '青铜器', level: '一级', era: '西周', storage: 'B区-2排-1列', status: '在库', description: '西周早期青铜礼器，通高45cm。' },
  { id: 3, code: 'ZW-2026-0003', name: '山水图卷', category: '书画', level: '二级', era: '宋代', storage: 'C区-1排-5列', status: '外借', description: '宋代绢本设色山水画，纵120cm，横380cm。' },
  { id: 4, code: 'ZW-2026-0004', name: '白玉璧', category: '玉器', level: '二级', era: '战国', storage: 'A区-3排-2列', status: '在库', description: '战国时期和田白玉璧，直径15cm。' },
  { id: 5, code: 'ZW-2026-0005', name: '三彩骆驼俑', category: '陶瓷', level: '三级', era: '唐代', storage: 'A区-2排-4列', status: '修复中', description: '唐三彩骆驼载乐俑，高58cm。' }
])

let nextId = 6

const searchForm = reactive({ name: '', code: '', category: '' })
const pagination = reactive({ page: 1, size: 10 })

const dialogVisible = ref(false)
const viewVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const dialogTitle = ref('')
const viewData = ref({})

const formData = reactive({
  id: null, name: '', code: '', category: '', level: '', era: '', storage: '', description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入藏品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入藏品编号', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择级别', trigger: 'change' }]
}

const filteredData = computed(() => {
  let data = mockData.value
  if (searchForm.name) data = data.filter(r => r.name.includes(searchForm.name))
  if (searchForm.code) data = data.filter(r => r.code.includes(searchForm.code))
  if (searchForm.category) data = data.filter(r => r.category === searchForm.category)
  return data
})

const tableData = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  return filteredData.value.slice(start, start + pagination.size)
})

function handleSearch() {
  pagination.page = 1
}

function handleReset() {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.category = ''
  pagination.page = 1
}

function handlePageChange() {
  // 分页已通过 v-model 绑定自动更新
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增藏品'
  Object.assign(formData, { id: null, name: '', code: '', category: '', level: '', era: '', storage: '', description: '' })
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
  await new Promise(r => setTimeout(r, 300))
  if (isEdit.value) {
    const idx = mockData.value.findIndex(r => r.id === formData.id)
    if (idx !== -1) mockData.value.splice(idx, 1, { ...formData })
    ElMessage.success('修改成功')
  } else {
    mockData.value.unshift({ ...formData, id: nextId++, status: '在库' })
    ElMessage.success('新增成功')
  }
  submitLoading.value = false
  dialogVisible.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除藏品"${row.name}"吗？`, '提示', { type: 'warning' })
  mockData.value = mockData.value.filter(r => r.id !== row.id)
  ElMessage.success('删除成功')
}
</script>

<style scoped>
.page-container { max-width: 1400px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: var(--color-text); margin: 0; }
.search-card { margin-bottom: 16px; border-radius: var(--radius-md); }
.table-card { border-radius: var(--radius-md); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
