<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">学生档案（只读）</div>
        <el-select v-model="studentId" filterable placeholder="选择学生" style="width:260px" @change="load">
          <el-option v-for="s in students" :key="s.id" :value="s.id" :label="`${s.realName}（${s.studentNo}）`" />
        </el-select>
      </div>

      <el-descriptions :column="2" border style="margin-top:16px">
        <el-descriptions-item label="姓名">{{ detail.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detail.className }}</el-descriptions-item>
        <el-descriptions-item label="任职">{{ detail.duty }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.gender }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ detail.birthday }}</el-descriptions-item>
        <el-descriptions-item label="档案备注" :span="2">{{ detail.archiveNote }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card">
      <div class="card-title">评奖评优记录</div>
      <el-table :data="detail.awards || []" stripe>
        <el-table-column prop="awardName" label="奖项名称" min-width="160" />
        <el-table-column prop="awardLevel" label="等级" width="120" />
        <el-table-column prop="awardTime" label="获奖时间" width="140" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { studentList, studentDetail } from '../api'

const route = useRoute()
const students = ref([])
const studentId = ref(null)
const detail = ref({})

async function load() {
  if (!studentId.value) return
  detail.value = await studentDetail(studentId.value)
}

onMounted(async () => {
  const res = await studentList({ pageSize: 200 })
  students.value = res.records
  if (route.query.studentId) {
    studentId.value = Number(route.query.studentId)
    load()
  } else if (students.value.length) {
    studentId.value = students.value[0].id
    load()
  }
})
</script>
