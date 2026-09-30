<template>
  <div>
    <div class="card">
      <div class="card-title">个人信息</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ user.realName }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ user.studentNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ user.className || '—' }}</el-descriptions-item>
        <el-descriptions-item label="任职">{{ user.duty || '无' }}</el-descriptions-item>
        <el-descriptions-item label="角色">学生</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card">
      <div class="card-title">修改密码</div>
      <el-form label-width="100px" style="max-width:420px">
        <el-form-item label="原密码">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwd.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="pwd.confirm" type="password" show-password />
        </el-form-item>
        <el-button type="primary" @click="submitPwd">保存修改</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '../api'

const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function submitPwd() {
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
  ElMessage.success('密码修改成功')
  pwd.oldPassword = pwd.newPassword = pwd.confirm = ''
}
</script>
