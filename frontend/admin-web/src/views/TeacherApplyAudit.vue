<template>
  <div class="card">
    <div class="card-title">任课教师更换申请审核</div>
    <el-table :data="list" stripe>
      <el-table-column prop="className" label="班级" width="130" />
      <el-table-column prop="subject" label="科目" width="100" />
      <el-table-column prop="oldTeacherName" label="原教师" width="110" />
      <el-table-column prop="newTeacherName" label="新教师" width="110" />
      <el-table-column prop="reason" label="理由" min-width="180" show-overflow-tooltip />
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
import { changePending, changeAudit } from '../api'

const list = ref([])

async function load() { list.value = await changePending() }

async function audit(row, status) {
  const isPass = status === 1
  await ElMessageBox.confirm(
    isPass ? `通过后「${row.className}·${row.subject}」将更换为 ${row.newTeacherName}` : '确定驳回该申请吗？',
    isPass ? '通过确认' : '驳回确认', { type: isPass ? 'success' : 'warning' })
  await changeAudit(row.id, { status, comment: isPass ? '同意更换' : '不同意' })
  ElMessage.success(isPass ? '已通过' : '已驳回')
  load()
}

onMounted(load)
</script>
