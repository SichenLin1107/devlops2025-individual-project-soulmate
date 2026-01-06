<template>
  <Layout>
    <div class="profile-page" v-loading="loading">
      <!-- 用户信息卡片 -->
      <div class="profile-header">
        <div class="avatar-section">
          <div class="avatar-uploader" @click="handleAvatarClick">
            <el-avatar
              :src="data?.avatar || defaultAvatar"
              :size="120"
              class="user-avatar"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
        </div>
        <div class="user-info">
          <h1 class="user-name">{{ data?.nickname || data?.username || '用户' }}</h1>
          <p class="user-username">@{{ data?.username || '-' }}</p>
          <p class="user-bio" v-if="data?.bio">{{ data?.bio }}</p>
          <p class="user-bio placeholder" v-else>还没有个性签名，点击编辑资料添加吧~</p>
          <div class="user-actions">
            <el-button type="primary" @click="openEdit">
              <el-icon><Edit /></el-icon>
              编辑资料
            </el-button>
            <el-button @click="openPwd">
              <el-icon><Lock /></el-icon>
              修改密码
            </el-button>
          </div>
        </div>
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-value">{{ data?.role === 'admin' ? '管理员' : '普通用户' }}</div>
            <div class="stat-label">账户类型</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ formatDate(data?.createdAt) }}</div>
            <div class="stat-label">注册时间</div>
          </div>
        </div>
      </div>

      <!-- 账户信息 -->
      <div class="account-info-section">
        <div class="info-grid">
          <div class="info-card">
            <div class="card-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">用户名</div>
              <div class="card-value">{{ data?.username || '-' }}</div>
            </div>
          </div>
          
          <div class="info-card">
            <div class="card-icon nickname">
              <el-icon><EditPen /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">昵称</div>
              <div class="card-value">{{ data?.nickname || '-' }}</div>
            </div>
          </div>
          
          <div class="info-card">
            <div class="card-icon role">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">角色</div>
              <div class="card-value">
                <el-tag :type="data?.role === 'admin' ? 'danger' : 'primary'" size="default">
                  {{ data?.role === 'admin' ? '管理员' : '普通用户' }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="info-card">
            <div class="card-icon status">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">账户状态</div>
              <div class="card-value">
                <el-tag :type="data?.status === 1 ? 'success' : 'info'" size="default">
                  {{ data?.status === 1 ? '已启用' : '停用' }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="info-card">
            <div class="card-icon time">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">注册时间</div>
              <div class="card-value">{{ formatDate(data?.createdAt) }}</div>
            </div>
          </div>
          
          <div class="info-card">
            <div class="card-icon update">
              <el-icon><Refresh /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-label">最后更新</div>
              <div class="card-value">{{ formatDate(data?.updatedAt) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 编辑资料对话框 -->
      <el-dialog
        v-model="editVisible"
        width="600px"
        :close-on-click-modal="false"
        title="编辑资料"
      >
        <el-form
          ref="editRef"
          :model="editForm"
          :rules="editRules"
          label-width="100px"
        >
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="editForm.nickname" maxlength="20" show-word-limit placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="个性签名" prop="bio">
            <el-input
              v-model="editForm.bio"
              type="textarea"
              :rows="4"
              maxlength="120"
              show-word-limit
              placeholder="向大家介绍一下你自己~"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingEdit" @click="submitEdit">保存</el-button>
        </template>
      </el-dialog>

      <!-- 修改密码对话框 -->
      <el-dialog
        v-model="pwdVisible"
        width="500px"
        :close-on-click-modal="false"
        title="修改密码"
      >
        <el-form
          ref="pwdRef"
          :model="pwdForm"
          :rules="pwdRules"
          label-width="100px"
        >
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input
              v-model="pwdForm.oldPassword"
              type="password"
              show-password
              placeholder="请输入当前密码"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="pwdForm.newPassword"
              type="password"
              show-password
              placeholder="至少6位，建议包含字母与数字"
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="pwdForm.confirmPassword"
              type="password"
              show-password
              placeholder="再次输入新密码"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="pwdVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingPwd" @click="submitPwd">确认修改</el-button>
        </template>
      </el-dialog>

    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Edit,
  Lock,
  Camera,
  EditPen,
  UserFilled,
  CircleCheck,
  Calendar,
  Refresh
} from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()
const data = ref(null)
const loading = ref(false)

const defaultAvatar = computed(() => 
  `https://api.dicebear.com/7.x/avataaars/svg?seed=${data.value?.username || 'user'}`
)

const editVisible = ref(false)
const editRef = ref()
const savingEdit = ref(false)
const editForm = reactive({
  nickname: '',
  bio: ''
})

const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '1~20个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 120, message: '最多120个字符', trigger: 'blur' }
  ]
}

const pwdVisible = ref(false)
const pwdRef = ref()
const savingPwd = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_, v, cb) => {
        v !== pwdForm.newPassword ? cb(new Error('两次输入不一致')) : cb()
      },
      trigger: 'blur'
    }
  ]
}

// 头像点击处理
const handleAvatarClick = () => {
  ElMessage.info('头像上传功能正在开发中，敬请期待')
}

const fetchProfile = async () => {
  loading.value = true
  try {
    data.value = await userApi.getProfile()
    userStore.setUserInfo(data.value)
  } catch (e) {
    ElMessage.error(e?.message || '获取用户信息失败')
  } finally {
    loading.value = false
  }
}
const openEdit = () => {
  editForm.nickname = data.value?.nickname || ''
  editForm.bio = data.value?.bio || ''
  editVisible.value = true
}

const submitEdit = () => {
  editRef.value?.validate(async (ok) => {
    if (!ok) return
    savingEdit.value = true
    try {
      await userApi.updateProfile({
        nickname: editForm.nickname || null,
        bio: editForm.bio || null
      })
      await fetchProfile()
      ElMessage.success('资料已更新')
      editVisible.value = false
    } catch (e) {
      ElMessage.error(e?.message || '更新失败')
    } finally {
      savingEdit.value = false
    }
  })
}

const openPwd = () => {
  Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  pwdVisible.value = true
}

const submitPwd = () => {
  pwdRef.value?.validate(async (ok) => {
    if (!ok) return
    savingPwd.value = true
    try {
      await userApi.changePassword(pwdForm.oldPassword, pwdForm.newPassword)
      ElMessage.success('密码修改成功')
      pwdVisible.value = false
    } catch (e) {
      ElMessage.error(e?.message || '修改失败')
    } finally {
      savingPwd.value = false
    }
  })
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 用户信息头部 */
.profile-header {
  background: var(--app-card-bg);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  gap: 32px;
  align-items: flex-start;
  box-shadow: var(--el-box-shadow-light);
}

.avatar-section {
  position: relative;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  border: 4px solid var(--el-border-color-light);
  transition: all 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
}

.user-username {
  font-size: 16px;
  color: var(--el-text-color-regular);
  margin: 0 0 12px 0;
}

.user-bio {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.user-bio.placeholder {
  color: var(--el-text-color-placeholder);
  font-style: italic;
}

.user-actions {
  display: flex;
  gap: 12px;
}

.user-stats {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 150px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 账户信息 */
.account-info-section {
  background: var(--app-card-bg);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--el-box-shadow-light);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-card {
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(38, 166, 154, 0.3) 0%, rgba(77, 182, 172, 0.3) 100%);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.info-card:hover {
  border-color: rgba(38, 166, 154, 0.3);
  box-shadow: 0 4px 16px rgba(38, 166, 154, 0.1);
  transform: translateY(-2px);
  background: var(--el-fill-color-light);
}

.info-card:hover::before {
  transform: scaleX(1);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: rgba(38, 166, 154, 0.8);
  background: rgba(38, 166, 154, 0.08);
  border: 1px solid rgba(38, 166, 154, 0.15);
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.info-card:hover .card-icon {
  background: rgba(38, 166, 154, 0.12);
  border-color: rgba(38, 166, 154, 0.25);
  color: rgba(38, 166, 154, 0.9);
  transform: scale(1.05);
}

.card-icon.nickname {
  color: rgba(255, 107, 107, 0.8);
  background: rgba(255, 107, 107, 0.08);
  border-color: rgba(255, 107, 107, 0.15);
}

.info-card:hover .card-icon.nickname {
  background: rgba(255, 107, 107, 0.12);
  border-color: rgba(255, 107, 107, 0.25);
  color: rgba(255, 107, 107, 0.9);
}

.card-icon.role {
  color: rgba(78, 205, 196, 0.8);
  background: rgba(78, 205, 196, 0.08);
  border-color: rgba(78, 205, 196, 0.15);
}

.info-card:hover .card-icon.role {
  background: rgba(78, 205, 196, 0.12);
  border-color: rgba(78, 205, 196, 0.25);
  color: rgba(78, 205, 196, 0.9);
}

.card-icon.status {
  color: rgba(103, 194, 58, 0.8);
  background: rgba(103, 194, 58, 0.08);
  border-color: rgba(103, 194, 58, 0.15);
}

.info-card:hover .card-icon.status {
  background: rgba(103, 194, 58, 0.12);
  border-color: rgba(103, 194, 58, 0.25);
  color: rgba(103, 194, 58, 0.9);
}

.card-icon.time {
  color: rgba(144, 147, 153, 0.8);
  background: rgba(144, 147, 153, 0.08);
  border-color: rgba(144, 147, 153, 0.15);
}

.info-card:hover .card-icon.time {
  background: rgba(144, 147, 153, 0.12);
  border-color: rgba(144, 147, 153, 0.25);
  color: rgba(144, 147, 153, 0.9);
}

.card-icon.update {
  color: rgba(64, 158, 255, 0.8);
  background: rgba(64, 158, 255, 0.08);
  border-color: rgba(64, 158, 255, 0.15);
}

.info-card:hover .card-icon.update {
  background: rgba(64, 158, 255, 0.12);
  border-color: rgba(64, 158, 255, 0.25);
  color: rgba(64, 158, 255, 0.9);
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.card-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  word-break: break-word;
}

.card-value :deep(.el-tag) {
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 6px;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-stats {
    flex-direction: row;
    justify-content: space-around;
    width: 100%;
  }

}
</style>
