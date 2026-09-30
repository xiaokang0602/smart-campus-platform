<template>
  <div>
    <div class="card">
      <div class="flex gap-8" style="flex-wrap:wrap;">
        <el-select v-model="q.classId" placeholder="全部班级" clearable style="width:150px" @change="load(1)">
          <el-option v-for="c in classes" :key="c.id" :value="c.id" :label="c.className" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="姓名/学号" clearable style="width:180px" @keyup.enter="load(1)" />
        <el-button type="primary" @click="load(1)">查询</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="className" label="班级" width="140" />
        <el-table-column prop="duty" label="任职" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="mt-16" layout="total, prev, pager, next" :total="total" :page-size="q.pageSize"
        :current-page="q.pageNum" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" title="学生档案" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ detail.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detail.className }}</el-descriptions-item>
        <el-descriptions-item label="任职">{{ detail.duty }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px" style="margin-top:16px">
        <el-form-item label="档案备注">
          <el-input v-model="detail.archiveNote" type="textarea" :rows="3" />
        </el-form-item>
        <el-button type="primary" @click="saveNote">保存备注</el-button>
      </el-form>
      <div class="card-title" style="margin-top:16px">获奖记录</div>
      <el-table :data="detail.awards || []" size="small" stripe>
        <el-table-column prop="awardName" label="奖项" />
        <el-table-column prop="awardTime" label="时间" width="130" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { studentList, studentDetail, studentUpdate, classList } from '../api'

const rows = ref([])
const total = ref(0)
const classes = ref([])
const q = reactive({ pageNum: 1, pageSize: 10, classId: null, keyword: '' })
const dialog = ref(false)
const detail = ref({})

async function load(p) {
  if (p) q.pageNum = p
  const res = await studentList({ pageNum: q.pageNum, pageSize: q.pageSize, classId: q.classId ?? undefined, keyword: q.keyword || undefined })
  rows.value = res.records
  total.value = res.total
}

async function openDetail(row) {
  detail.value = await studentDetail(row.id)
  dialog.value = true
}

async function saveNote() {
  await studentUpdate(detail.value.id, { archiveNote: detail.value.archiveNote, duty: detail.value.duty })
  ElMessage.success('保存成功')
}

onMounted(async () => {
  load()
  classes.value = await classList()
})
</script>
