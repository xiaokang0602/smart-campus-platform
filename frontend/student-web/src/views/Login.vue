<template>
  <div class="login-page">
    <div class="login-box">
      <div class="title">智慧校园综合管理系统</div>
      <div class="subtitle">学生端 · 登录</div>

      <el-form :model="form" @submit.prevent="doLogin">
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
        <span class="link" @click="recoverVisible = true">忘记密码？</span>
        <span class="text-muted">演示账号：stu1 / 123456</span>
      </div>
    </div>

    <!-- 首次登录完善信息 -->
    <el-dialog v-model="firstVisible" title="首次登录，请完善信息" width="460px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="新密码">
          <el-input v-model="firstForm.newPassword" type="password" show-password placeholder="设置新密码" />
        </el-form-item>
        <el-form-item label="密保问题">
          <el-input v-model="firstForm.question" placeholder="如：你的小学母校是？" />
        </el-form-item>
        <el-form-item label="密保答案">
          <el-input v-model="firstForm.answer" placeholder="答案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitFirst">保存并进入</el-button>
      </template>
    </el-dialog>

    <!-- 找回密码 -->
    <el-dialog v-model="recoverVisible" title="找回密码" width="460px">
      <el-steps :active="recoverStep" finish-status="success" simple style="margin-bottom:20px">
        <el-step title="验证密保" />
        <el-step title="好友确认" />
      </el-steps>
      <div v-if="recoverStep === 0">
        <el-form label-width="70px">
          <el-form-item label="账号">
            <el-input v-model="recoverForm.username" placeholder="登录账号" />
          </el-form-item>
          <el-form-item label="密保答案">
            <el-input v-model="recoverForm.answer" placeholder="回答你的密保问题" />
          </el-form-item>
        </el-form>
        <el-button type="primary" style="width:100%" @click="submitRecoverAnswer">提交验证</el-button>
      </div>
      <div v-else>
        <el-alert title="验证码已发送给两位认证同学，请两位同学在各自通知中心输入 6 位验证码确认后，账号密码将重置。" type="success" :closable="false" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login, firstLogin, recoverAnswers } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const firstVisible = ref(false)
const firstForm = reactive({ newPassword: '', question: '', answer: '' })

const recoverVisible = ref(false)
const recoverStep = ref(0)
const recoverForm = reactive({ username: '', answer: '' })

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
  await firstLogin({
    newPassword: firstForm.newPassword,
    questions: [firstForm.question],
    answers: [firstForm.answer],
    guardianIds: []
  })
  ElMessage.success('设置成功')
  firstVisible.value = false
  router.push('/')
}

async function submitRecoverAnswer() {
  await recoverAnswers({ username: recoverForm.username, answers: [recoverForm.answer] })
  ElMessage.success('验证码已发送给认证同学')
  recoverStep.value = 1
}
</script>
