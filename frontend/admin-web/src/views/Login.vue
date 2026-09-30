<template>
  <div class="login-page">
    <div class="login-box">
      <div class="title">智慧校园 · 后台管理</div>
      <div class="subtitle">Smart Campus Admin Console</div>
      <el-form @keyup.enter="submit">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
      <div class="text-muted" style="text-align:center; margin-top:18px; font-size:12px;">
        默认超管：admin / admin123
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function submit() {
  if (!form.username || !form.password) { ElMessage.warning('请输入账号和密码'); return }
  loading.value = true
  try {
    const res = await login(form)
    if (res.user.role !== 4) {
      ElMessage.error('该账号不是后台管理账号')
      return
    }
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res.user))
    ElMessage.success('登录成功')
    if (res.user.accountLevel === 1) {
      router.push('/global')
    } else {
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}
</script>
