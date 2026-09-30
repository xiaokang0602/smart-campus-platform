<template>
  <div class="login-page">
    <div class="login-box">
      <div class="title">智慧校园综合管理系统</div>
      <div class="subtitle">教师端 · 登录</div>

      <el-form @submit.prevent="doLogin">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="请输入账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password
            placeholder="请输入密码" :prefix-icon="Lock" @keyup.enter="doLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="doLogin">
          登 录
        </el-button>
      </el-form>

      <div class="flex-between mt-16" style="font-size:12px;">
        <span class="text-muted">任课：lina / 123456</span>
        <span class="text-muted">班主任：chenjing / 123456</span>
      </div>
    </div>

    <el-dialog v-model="firstVisible" title="首次登录，请设置新密码" width="420px" :close-on-click-modal="false">
      <el-input v-model="newPassword" type="password" show-password placeholder="设置新密码" />
      <template #footer>
        <el-button type="primary" @click="submitFirst">保存并进入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, changePassword } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const firstVisible = ref(false)
const newPassword = ref('')

async function doLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await login({ username: form.username, password: form.password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    if (data.user.firstLogin === 1) {
      firstVisible.value = true
    } else {
      router.push('/')
    }
  } finally {
    loading.value = false
  }
}

async function submitFirst() {
  await changePassword({ oldPassword: form.password, newPassword: newPassword.value })
  ElMessage.success('设置成功')
  firstVisible.value = false
  router.push('/')
}
</script>
