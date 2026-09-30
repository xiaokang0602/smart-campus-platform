<template>
  <div class="card" style="max-width:680px;">
    <div class="card-title">系统参数</div>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="当前学校">{{ schoolName }}</el-descriptions-item>
      <el-descriptions-item label="新账号默认密码">123456</el-descriptions-item>
      <el-descriptions-item label="超级管理员账号">admin / admin123</el-descriptions-item>
      <el-descriptions-item label="AI 出题引擎">火山方舟（Doubao-Seed-2.1-pro），未配置 Key 时自动使用模拟数据</el-descriptions-item>
      <el-descriptions-item label="前端服务">学生端 5173 · 教师端 5174 · 后台 5175</el-descriptions-item>
      <el-descriptions-item label="后端服务">Spring Boot 3 · 8080</el-descriptions-item>
    </el-descriptions>
    <el-alert style="margin-top:16px" title="说明：以上为平台运行默认配置。系统参数如需动态化，可在后续版本接入配置中心。" type="info" :closable="false" show-icon />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { schoolInfo } from '../api'

const schoolName = ref('')
onMounted(async () => {
  try {
    const info = await schoolInfo()
    schoolName.value = info?.schoolName || '—'
  } catch (e) { /* ignore */ }
})
</script>
