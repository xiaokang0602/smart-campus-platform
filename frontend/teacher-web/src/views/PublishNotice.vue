<template>
  <div class="card" style="max-width:760px;">
    <div class="card-title">发布通知（限自己任教班级）</div>
    <el-form label-width="90px">
      <el-form-item label="通知类型">
        <el-radio-group v-model="form.noticeType">
          <el-radio :value="2">考试通知</el-radio>
          <el-radio :value="4">班级通知</el-radio>
          <el-radio :value="5">个人提醒</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="标题"><el-input v-model="form.title" placeholder="通知标题" /></el-form-item>
      <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="6" placeholder="通知内容" /></el-form-item>
      <el-form-item label="接收范围">
        <el-radio-group v-model="form.targetScope">
          <el-radio :value="2">指定班级</el-radio>
          <el-radio :value="3">指定学生</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.targetScope === 2" label="选择班级">
        <el-select v-model="form.classIds" multiple placeholder="选择自己任教的班级" style="width:100%">
          <el-option v-for="c in myClasses" :key="c.classId" :value="c.classId" :label="c.className" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.targetScope === 3" label="选择学生">
        <el-select v-model="form.userIds" multiple filterable placeholder="选择学生" style="width:100%">
          <el-option v-for="s in students" :key="s.id" :value="s.id" :label="`${s.realName}（${s.className}）`" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="submit">发布通知</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publishMessage, studentList } from '../api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const myClasses = user.classes || []
const students = ref([])
const form = reactive({ noticeType: 4, title: '', content: '', targetScope: 2, classIds: [], userIds: [] })

async function submit() {
  if (!form.title) { ElMessage.warning('请输入标题'); return }
  await publishMessage({
    title: form.title, content: form.content,
    noticeType: form.noticeType, targetScope: form.targetScope,
    classIds: form.targetScope === 2 ? form.classIds : [],
    userIds: form.targetScope === 3 ? form.userIds : []
  })
  ElMessage.success('发布成功')
  router.push('/notice')
}

onMounted(async () => {
  const res = await studentList({ pageSize: 200 })
  students.value = res.records
})
</script>
