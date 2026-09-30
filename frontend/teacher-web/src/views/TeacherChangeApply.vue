<template>
  <div>
    <div class="card">
      <div class="card-title">提交任课教师更换申请（班主任）</div>
      <el-form :inline="true">
        <el-form-item label="班级"><el-select v-model="form.classId" style="width:150px"><el-option v-for="c in classes" :key="c.id" :value="c.id" :label="c.className" /></el-select></el-form-item>
        <el-form-item label="科目"><el-select v-model="form.subject" style="width:130px"><el-option v-for="s in subjects" :key="s" :value="s" :label="s" /></el-select></el-form-item>
        <el-form-item label="原教师"><el-select v-model="form.oldTeacherId" style="width:150px"><el-option v-for="t in teachers" :key="t.id" :value="t.id" :label="t.realName" /></el-select></el-form-item>
        <el-form-item label="新教师"><el-select v-model="form.newTeacherId" style="width:150px"><el-option v-for="t in teachers" :key="t.id" :value="t.id" :label="t.realName" /></el-select></el-form-item>
      </el-form>
      <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="申请理由" style="margin-bottom:14px" />
      <el-button type="primary" @click="submit">提交申请</el-button>
    </div>

    <div class="card">
      <div class="card-title">我的申请记录</div>
      <el-table :data="list" stripe>
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="subject" label="科目" width="100" />
        <el-table-column prop="oldTeacherName" label="原教师" width="110" />
        <el-table-column prop="newTeacherName" label="新教师" width="110" />
        <el-table-column prop="reason" label="理由" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="['info', 'success', 'danger'][row.auditStatus]">{{ ['待审核', '已通过', '已驳回'][row.auditStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditComment" label="审核意见" width="140" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { classList, userList, changeApply, changeMine } from '../api'

const subjects = ['语文', '数学', '英语', '道德与法治', '历史', '地理', '物理', '化学', '生物']
const classes = ref([])
const teachers = ref([])
const list = ref([])
const form = reactive({ classId: null, subject: '', oldTeacherId: null, newTeacherId: null, reason: '' })

async function submit() {
  if (!form.classId || !form.subject || !form.oldTeacherId || !form.newTeacherId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await changeApply(form)
  ElMessage.success('申请已提交')
  load()
}

async function load() { list.value = await changeMine() }

onMounted(async () => {
  classes.value = await classList()
  const res = await userList({ pageSize: 200 })
  teachers.value = (res.records || []).filter(u => u.role === 2 || u.role === 3)
  load()
})
</script>
