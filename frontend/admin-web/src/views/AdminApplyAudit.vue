<template>
  <div class="card">
    <div class="card-title">后台账号申请审核（超级管理员）</div>
    <el-table :data="list" stripe>
      <el-table-column prop="applySchoolId" label="学校ID" width="90" />
      <el-table-column prop="newUsername" label="申请账号" width="150" />
      <el-table-column prop="newRealName" label="姓名" width="120" />
      <el-table-column prop="menuPerms" label="申请权限" min-width="180" />
      <el-table-column prop="reason" label="理由" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="audit(row, 1)">通过</el-button>
          <el-button size="small" type="danger" plain @click="audit(row, 2)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="list.length === 0" description="暂无待审核申请" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApplyPending, adminApplyAudit } from '../api'

const list = ref([])

async function load() { list.value = await adminApplyPending() }

async function audit(row, status) {
  const isPass = status === 1
  await ElMessageBox.confirm(
    isPass ? `通过后将为「${row.newRealName}」创建后台账号（初始密码 123456）` : '确定驳回该申请吗？',
    isPass ? '通过确认' : '驳回确认', { type: isPass ? 'success' : 'warning' })
  await adminApplyAudit(row.id, { status, comment: isPass ? '同意开通' : '不同意' })
  ElMessage.success(isPass ? '已通过' : '已驳回')
  load()
}

onMounted(load)
</script>
