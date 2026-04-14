<template>
  <div class="backup-container">
    <el-card class="header-card">
      <div class="header-actions">
        <div class="header-left">
          <h2>数据备份管理</h2>
          <span class="subtitle">数据库备份、恢复与导出</span>
        </div>
        <div class="header-right">
          <el-button type="primary" :icon="DocumentAdd" @click="handleBackup" :loading="backupLoading">
            创建备份
          </el-button>
          <el-button type="success" :icon="Upload" @click="showUploadDialog = true">
            上传恢复
          </el-button>
          <el-button :icon="Refresh" @click="loadBackups">刷新列表</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table :data="backupList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="fileName" label="文件名" min-width="280">
          <template #default="{ row }">
            <el-icon class="file-icon"><Document /></el-icon>
            {{ row.fileName }}
          </template>
        </el-table-column>
        <el-table-column prop="backupTime" label="备份时间" width="180" />
        <el-table-column prop="sizeDesc" label="文件大小" width="120" align="center" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Download" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button type="warning" link :icon="RefreshLeft" @click="handleRestore(row)">
              恢复
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无备份记录">
            <el-button type="primary" @click="handleBackup">立即备份</el-button>
          </el-empty>
        </template>
      </el-table>
    </el-card>

    <!-- 上传恢复对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传备份文件恢复" width="500px">
      <el-alert type="warning" :closable="false" class="mb-20">
        <template #title>
          <strong>⚠️ 风险提示</strong>
        </template>
        <ul class="warning-list">
          <li>恢复操作将覆盖当前数据库数据</li>
          <li>恢复前请确保已做好当前数据的备份</li>
          <li>恢复过程可能需要几分钟时间，请耐心等待</li>
        </ul>
      </el-alert>

      <el-form>
        <el-form-item label="选择备份文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".sql"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">将SQL文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">只能上传 .sql 文件，建议文件大小不超过 100MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUploadRestore" :loading="restoreLoading" :disabled="!uploadFile">
          开始恢复
        </el-button>
      </template>
    </el-dialog>

    <!-- 确认恢复对话框 -->
    <el-dialog v-model="showConfirmDialog" title="确认恢复" width="450px">
      <el-alert type="error" :closable="false" class="mb-20">
        <strong>⚠️ 警告：此操作不可逆！</strong>
        <p class="mt-10">恢复操作将使用备份文件覆盖当前数据库，请谨慎操作！</p>
      </el-alert>

      <div class="restore-info">
        <p><strong>备份文件：</strong>{{ restoreTarget?.fileName }}</p>
        <p><strong>备份时间：</strong>{{ restoreTarget?.backupTime }}</p>
        <p><strong>文件大小：</strong>{{ restoreTarget?.sizeDesc }}</p>
      </div>

      <div class="confirm-checkbox">
        <el-checkbox v-model="confirmRestore">我已了解恢复操作的风险，确认继续</el-checkbox>
      </div>

      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmRestoreAction" :loading="restoreLoading" :disabled="!confirmRestore">
          确认恢复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentAdd,
  Upload,
  Refresh,
  Download,
  RefreshLeft,
  Delete,
  Document
} from '@element-plus/icons-vue'
import {
  createBackup,
  listBackups,
  downloadBackup,
  restoreFromUpload,
  restoreFromFile,
  deleteBackup
} from '@/api/system/backup'
import request from '@/api/request'

// 数据
const loading = ref(false)
const backupLoading = ref(false)
const restoreLoading = ref(false)
const backupList = ref([])
const showUploadDialog = ref(false)
const showConfirmDialog = ref(false)
const uploadFile = ref(null)
const restoreTarget = ref(null)
const confirmRestore = ref(false)

// 加载备份列表
async function loadBackups() {
  loading.value = true
  try {
    const res = await listBackups()
    backupList.value = res.data || []
  } catch (error) {
    console.error('加载备份列表失败', error)
  } finally {
    loading.value = false
  }
}

// 创建备份
async function handleBackup() {
  backupLoading.value = true
  try {
    const res = await createBackup()
    if (res.code === 200) {
      ElMessage.success('备份创建成功')
      loadBackups()
    }
  } catch (error) {
    console.error('备份失败', error)
  } finally {
    backupLoading.value = false
  }
}

// 下载备份
async function handleDownload(row) {
  try {
    ElMessage.info('正在下载...')
    
    // 使用 axios 下载，自动携带 Authorization Header
    const response = await request({
      url: '/backup/download',
      method: 'get',
      params: { fileName: row.fileName },
      responseType: 'blob'
    })
    
    // axios 拦截器已经处理了响应，response 就是 blob 数据
    const blob = response
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载完成')
  } catch (error) {
    console.error('下载失败', error)
    ElMessage.error(error.message || '下载失败')
  }
}

// 准备恢复
function handleRestore(row) {
  restoreTarget.value = row
  confirmRestore.value = false
  showConfirmDialog.value = true
}

// 确认恢复操作
async function confirmRestoreAction() {
  if (!confirmRestore.value) {
    ElMessage.warning('请勾选确认复选框')
    return
  }

  restoreLoading.value = true
  try {
    const res = await restoreFromFile(restoreTarget.value.fileName, true)
    if (res.code === 200) {
      ElMessage.success('数据恢复成功')
      showConfirmDialog.value = false
    }
  } catch (error) {
    console.error('恢复失败', error)
  } finally {
    restoreLoading.value = false
  }
}

// 删除备份
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除备份文件 "${row.fileName}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteBackup(row.fileName)
    ElMessage.success('删除成功')
    loadBackups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

// 文件选择
function handleFileChange(file) {
  uploadFile.value = file.raw
}

// 文件移除
function handleFileRemove() {
  uploadFile.value = null
}

// 上传恢复
async function handleUploadRestore() {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择备份文件')
    return
  }

  try {
    await ElMessageBox.confirm(
      '恢复操作将覆盖当前数据库数据，确定要继续吗？',
      '确认恢复',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    restoreLoading.value = true
    const res = await restoreFromUpload(uploadFile.value, true)
    if (res.code === 200) {
      ElMessage.success('数据恢复成功')
      showUploadDialog.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复失败', error)
    }
  } finally {
    restoreLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadBackups()
})
</script>

<style scoped>
.backup-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  color: #909399;
  font-size: 13px;
}

.header-right {
  display: flex;
  gap: 10px;
}

.table-card {
  min-height: 500px;
}

.file-icon {
  color: #409eff;
  margin-right: 5px;
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-10 {
  margin-top: 10px;
}

.warning-list {
  margin: 10px 0 0 0;
  padding-left: 20px;
}

.warning-list li {
  line-height: 1.8;
}

.restore-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin: 15px 0;
}

.restore-info p {
  margin: 5px 0;
  line-height: 1.6;
}

.confirm-checkbox {
  margin-top: 15px;
}
</style>
