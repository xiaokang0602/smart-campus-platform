<template>
  <div>
    <div class="card">
      <div class="card-title">选拔课代表</div>
      <el-form :inline="true">
        <el-form-item label="学生">
          <el-select v-model="apply.studentId" placeholder="选择本班学生" style="width:180px">
            <el-option v-for="s in students" :key="s.id" :value="s.id" :label="s.realName" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="apply.subject" placeholder="选择科目" style="width:140px">
            <el-option v-for="s in subjects" :key="s" :value="s" :label="s" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitApply">提交申请</el-button>
        </el-form-item>
      </el-form>
      <div class="text-muted" style="font-size:12px">课代表申请提交后需班主任审批通过方可生效。</div>
    </div>

    <div class="card">
      <div class="card-title">我的申请记录</div>
      <el-table :data="myRepsList" stripe>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="subject" label="科目" width="120" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="['info', 'success', 'danger'][row.auditStatus]">{{ ['待审批', '已通过', '已驳回'][row.auditStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" />
      </el-table>
    </div>

    <div class="card" v-if="user.isHeadTeacher">
      <div class="card-title">待审批（班主任）</div>
      <el-table :data="pending" stripe>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="subject" label="科目" width="120" />
        <el-table-column prop="teacherName" label="申请教师" width="120" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="audit(row, 1)">通过</el-button>
            <el-button type="danger" size="small" @click="audit(row, 2)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { studentList, myReps, applyRep, pendingReps, auditRep } from '../api'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const students = ref([])
const myRepsList = ref([])
const pending = ref([])
const apply = ref({ studentId: null, subject: '' })

const subjects = ['语文', '数学', '英语', '道德与法治', '历史', '地理', '物理', '化学', '生物']

async function submitApply() {
  if (!apply.value.studentId || !apply.value.subject) {
    ElMessage.warning('请选择学生和科目')
    return
  }
  const classId = user.classes[0]?.classId
  await applyRep({ studentId: apply.value.studentId, subject: apply.value.subject, classId })
  ElMessage.success('申请已提交，等待班主任审批')
  load()
}

async function audit(row, status) {
  await auditRep(row.id, { status })
  ElMessage.success(status === 1 ? '已通过' : '已驳回')
  load()
}

async function load() {
  myRepsList.value = await myReps()
  if (user.isHeadTeacher) {
    pending.value = await pendingReps()
  }
}

onMounted(async () => {
  const res = await studentList({ pageSize: 200 })
  students.value = res.records
  load()
})
</script>
