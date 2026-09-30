<template>
  <div>
    <div class="card">
      <div class="card-title">发布通知</div>
      <el-form label-width="90px">
        <el-form-item label="通知类型">
          <el-radio-group v-model="form.noticeType">
            <el-radio :value="1">系统公告</el-radio>
            <el-radio :value="2">考试通知</el-radio>
            <el-radio :value="4">班级通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="通知标题" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="通知内容" /></el-form-item>
        <el-form-item label="接收范围">
          <el-radio-group v-model="form.targetScope">
            <el-radio :value="1">全校</el-radio>
            <el-radio :value="2">指定班级</el-radio>
            <el-radio :value="3">指定学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetScope === 2" label="选择班级">
          <el-select v-model="form.classIds" multiple style="width:100%">
            <el-option v-for="c in classes" :key="c.id" :value="c.id" :label="c.className" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.targetScope === 3" label="选择学生">
          <el-select v-model="form.userIds" multiple filterable style="width:100%">
            <el-option v-for="s in students" :key="s.id" :value="s.id" :label="`${s.realName}（${s.className}）`" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="publish">发布</el-button>
      </el-form>
    </div>

    <div class="card">
      <div class="card-title">已发布通知</div>
      <el-table :data="sent" stripe>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ ['', '系统公告', '考试通知', '审批结果', '班级通知', '个人提醒'][row.noticeType] }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="已读" width="100">
          <template #default="{ row }">{{ readStat[row.id]?.read ?? '-' }}/{{ readStat[row.id]?.total ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" :type="row.isTop === 1 ? 'warning' : 'default'" @click="top(row)">
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="withdraw(row)">撤回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { publishMessage, sentMessages, messageReaders, messageWithdraw, messageTop, classList, studentList } from '../api'

const classes = ref([])
const students = ref([])
const sent = ref([])
const readStat = reactive({})
const form = reactive({ noticeType: 1, title: '', content: '', targetScope: 1, classIds: [], userIds: [] })

async function publish() {
  if (!form.title) { ElMessage.warning('请输入标题'); return }
  await publishMessage({
    title: form.title, content: form.content, noticeType: form.noticeType, targetScope: form.targetScope,
    classIds: form.targetScope === 2 ? form.classIds : [],
    userIds: form.targetScope === 3 ? form.userIds : []
  })
  ElMessage.success('发布成功')
  form.title = form.content = ''
  loadSent()
}

async function loadSent() {
  sent.value = await sentMessages()
  for (const m of sent.value) {
    readStat[m.id] = await messageReaders(m.id)
  }
}

async function withdraw(row) {
  await ElMessageBox.confirm('确定撤回该通知吗？', '提示', { type: 'warning' })
  await messageWithdraw(row.id)
  ElMessage.success('已撤回')
  loadSent()
}

async function top(row) {
  await messageTop(row.id)
  loadSent()
}

onMounted(async () => {
  classes.value = await classList()
  const res = await studentList({ pageSize: 500 })
  students.value = res.records || []
  loadSent()
})
</script>
