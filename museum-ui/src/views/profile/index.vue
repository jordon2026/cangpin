<template>
  <div class="profile-page">
    <el-card shadow="never" class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" size="small" @click="showPasswordDialog = true">
            <el-icon><Lock /></el-icon> 修改密码
          </el-button>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            :on-change="handleAvatarChange"
          >
            <el-avatar :size="100" class="profile-avatar" :src="avatarUrl">
              <template v-if="!avatarUrl">
                {{ userStore.userInfo?.realName?.charAt(0) || userStore.userInfo?.username?.charAt(0) || '管' }}
              </template>
            </el-avatar>
            <div class="avatar-mask">
              <el-icon :size="24"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
          <div class="avatar-actions">
            <el-button size="small" plain type="danger" @click="handleRemoveAvatar" v-if="avatarUrl">移除头像</el-button>
          </div>
        </div>

        <!-- 信息表单 -->
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="80px"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input :value="formData.username" disabled />
          </el-form-item>
          <el-form-item label="昵称" prop="realName">
            <el-input v-model="formData.realName" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="部门">
            <el-input :value="formData.deptName" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag v-for="role in formData.roles" :key="role" class="role-tag">{{ role }}</el-tag>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-input :value="formData.createTime" disabled />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saveLoading" @click="handleSave">保存修改</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handleUpdatePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { updatePassword, updateProfile } from '@/api/system/user'
import { getUserInfo } from '@/api/auth'

const userStore = useUserStore()
const formRef = ref(null)
const pwdFormRef = ref(null)
const saveLoading = ref(false)
const pwdLoading = ref(false)
const avatarUrl = ref(localStorage.getItem('userAvatar') || '')
const showPasswordDialog = ref(false)

// 修改密码表单
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 修改密码
async function handleUpdatePassword() {
  try {
    await pwdFormRef.value.validate()
  } catch {
    return
  }
  
  pwdLoading.value = true
  try {
    await updatePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败', error)
  } finally {
    pwdLoading.value = false
  }
}

// 保存头像到 localStorage
function saveAvatar(url) {
  avatarUrl.value = url
  localStorage.setItem('userAvatar', url)
}

// 头像上传
function handleAvatarChange(file) {
  if (!file.raw.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.raw.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    saveAvatar(e.target.result)
    ElMessage.success('头像已更新')
  }
  reader.readAsDataURL(file.raw)
}

function handleRemoveAvatar() {
  saveAvatar('')
  ElMessage.success('头像已移除')
}

const formData = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  deptName: '',
  roles: [],
  createTime: ''
})

const rules = {
  realName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 页面加载时获取用户信息
onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      const user = res.data
      formData.username = user.username || ''
      formData.realName = user.realName || ''
      formData.phone = user.phone || ''
      formData.email = user.email || ''
      formData.roles = user.roles || []
      avatarUrl.value = user.avatar || localStorage.getItem('userAvatar') || ''
      // 更新 store 中的用户信息
      userStore.setUserInfo(user)
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
})

async function handleSave() {
  try { await formRef.value.validate() } catch { return }
  saveLoading.value = true
  try {
    await updateProfile({
      realName: formData.realName,
      phone: formData.phone,
      email: formData.email,
      avatar: avatarUrl.value
    })
    ElMessage.success('个人信息保存成功')
    // 更新本地存储的头像
    if (avatarUrl.value) {
      localStorage.setItem('userAvatar', avatarUrl.value)
    }
    // 更新 store 中的用户信息
    userStore.userInfo.realName = formData.realName
    userStore.userInfo.avatar = avatarUrl.value
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    saveLoading.value = false
  }
}

function handleReset() {
  formData.realName = userStore.userInfo?.realName || ''
  formData.phone = userStore.userInfo?.phone || ''
  formData.email = userStore.userInfo?.email || ''
  avatarUrl.value = userStore.userInfo?.avatar || localStorage.getItem('userAvatar') || ''
  formRef.value?.resetFields()
}
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 8px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.profile-content {
  padding: 20px 40px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
}

.avatar-uploader :deep(.el-upload) {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 12px;
}

.avatar-uploader:hover .avatar-mask {
  opacity: 1;
}

.profile-avatar {
  background: linear-gradient(135deg, #8B4513, #C9A96E);
  color: #fff;
  font-size: 36px;
  font-weight: 600;
}

.avatar-actions {
  margin-top: 12px;
}

.profile-form {
  max-width: 500px;
}

.role-tag {
  margin-right: 8px;
}
</style>
