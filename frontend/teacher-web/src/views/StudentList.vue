<template>
  <div class="card">
    <div class="flex-between">
      <div class="card-title" style="margin-bottom:0">班级学生</div>
      <el-input v-model="keyword" placeholder="搜索姓名/学号" style="width:240px" clearable :prefix-icon="Search" @input="filter" />
    </div>

    <el-table :data="rows" stripe style="margin-top:16px">
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="className" label="班级" width="140" />
      <el-table-column prop="duty" label="任职">
        <template #default="{ row }">
          <el-tag v-if="row.duty !== '无'" type="warning" size="small">{{ row.duty }}</el-tag>
          <span v-else class="text-muted">无</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/archive?studentId=${row.id}`)">查看档案</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
      layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="load" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { studentList } from '../api'

const router = useRouter()
const rows = ref([])
const all = ref([])
const keyword = ref('')
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)

function filter() {
  rows.value = all.value.filter(r =>
    r.realName.includes(keyword.value) || r.studentNo.includes(keyword.value))
}

async function load() {
  const data = await studentList({ pageNum: pageNum.value, pageSize })
  all.value = data.records
  total.value = data.total
  filter()
}

onMounted(load)
</script>
