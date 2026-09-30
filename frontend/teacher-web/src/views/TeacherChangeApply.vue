<template>
  <div>
    <div class="card">
      <div class="card-title">发起调课</div>
      <el-form :inline="true">
        <el-form-item label="调出课程">
          <el-select v-model="form.course" placeholder="选择我的一门课" style="width:200px">
            <el-option v-for="c in myCourses" :key="c.classId + ':' + c.subject" :value="c"
              :label="`${c.className} · ${c.subject}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="调给老师">
          <el-select v-model="form.newTeacherId" placeholder="选择接收老师" style="width:170px" filterable>
            <el-option v-for="t in teachers" :key="t.id" :value="t.id" :label="t.realName" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="调课理由（选填）" style="margin-bottom:14px" />
      <el-button type="primary" @click="submit">提交调课申请</el-button>
      <div style="margin-top:10px; font-size:12px; color:#8a96ad;">
        提交后需对方老师同意；对方同意后，该课程的任课教师将更换为对方。
      </div>
    </div>

    <div class="card">
      <div class="card-title">收到的调课请求（需我同意）</div>
      <el-table :data="incoming" stripe>
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="subject" label="科目" width="90" />
        <el-table-column prop="oldTeacherName" label="发起教师" width="110" />
        <el-table-column prop="reason" label="理由" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="audit(row, 1)">同意</el-button>
            <el-button size="small" type="danger" plain @click="audit(row, 2)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="incoming.length === 0" description="暂无收到的调课请求" />
    </div>

    <div class="card">
      <div class="card-title">我的调课申请</div>
      <el-table :data="mine" stripe>
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="subject" label="科目" width="90" />
        <el-table-column prop="newTeacherName" label="接收教师" width="110" />
        <el-table-column prop="reason" label="理由" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="['info', 'success', 'danger'][row.auditStatus]">
              {{ ['待同意', '已同意', '已拒绝'][row.auditStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditComment" label="对方意见" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userList, changeApply, changeMine, changeIncoming, changeAudit } from '../api'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const myCourses = (user.classes || []).filter(c => c.classId != null)

const teachers = ref([])
const incoming = ref([])
const mine = ref([])
const form = reactive({ course: null, newTeacherId: null, reason: '' })

async function submit() {
  if (!form.course || !form.newTeacherId) {
    ElMessage.warning('请选择调出课程和接收老师')
    return
  }
  await changeApply({
    classId: form.course.classId,
    subject: form.course.subject,
    oldTeacherId: user.id,
    newTeacherId: form.newTeacherId,
    reason: form.reason
  })
  ElMessage.success('调课申请已提交，等待对方老师同意')
  form.course = null
  form.newTeacherId = null
  form.reason = ''
  load()
}

async function audit(row, status) {
  const agree = status === 1
  await ElMessageBox.confirm(
    agree ? `同意后您将接收「${row.className} · ${row.subject}」的授课，确定吗？` : '确定拒绝该调课请求吗？',
    agree ? '同意确认' : '拒绝确认',
    { type: agree ? 'success' : 'warning' }
  )
  await changeAudit(row.id, { status, comment: agree ? '同意调课' : '不同意调课' })
  ElMessage.success(agree ? '已同意' : '已拒绝')
  load()
}

async function load() {
  mine.value = await changeMine()
  incoming.value = await changeIncoming()
}

onMounted(async () => {
  const res = await userList({ pageSize: 200 })
  teachers.value = (res.records || []).filter(u => (u.role === 2 || u.role === 3) && u.id !== user.id)
  load()
})
</script>
