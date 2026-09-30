<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">评奖评优管理（班主任）</div>
        <el-select v-model="studentId" filterable placeholder="选择学生" style="width:260px" @change="load">
          <el-option v-for="s in students" :key="s.id" :value="s.id" :label="`${s.realName}（${s.studentNo}）`" />
        </el-select>
      </div>
    </div>

    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">{{ detail.realName }} 的获奖记录</div>
        <el-button type="primary" :icon="Plus" @click="addVisible = true">新增获奖</el-button>
      </div>
      <el-table :data="detail.awards || []" stripe style="margin-top:16px">
        <el-table-column prop="awardName" label="奖项名称" min-width="160" />
        <el-table-column prop="awardLevel" label="等级" width="120" />
        <el-table-column prop="awardTime" label="获奖时间" width="140" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="addVisible" title="新增获奖记录" width="460px">
      <el-form label-width="80px">
        <el-form-item label="奖项名称"><el-input v-model="form.awardName" /></el-form-item>
        <el-form-item label="奖项等级"><el-select v-model="form.awardLevel" style="width:100%"><el-option v-for="l in ['校级','区级','市级','省级','国家级']" :key="l" :value="l" :label="l" /></el-select></el-form-item>
        <el-form-item label="获奖时间"><el-date-picker v-model="form.awardTime" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { studentList, studentDetail, addAward, deleteAward } from '../api'

const students = ref([])
const studentId = ref(null)
const detail = ref({})
const addVisible = ref(false)
const form = reactive({ awardName: '', awardLevel: '校级', awardTime: '' })

async function load() {
  if (!studentId.value) return
  detail.value = await studentDetail(studentId.value)
}

async function submit() {
  await addAward(studentId.value, form)
  ElMessage.success('已保存')
  addVisible.value = false
  Object.assign(form, { awardName: '', awardLevel: '校级', awardTime: '' })
  load()
}

async function del(row) {
  await deleteAward(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  const res = await studentList({ pageSize: 200 })
  students.value = res.records
  if (students.value.length) {
    studentId.value = students.value[0].id
    load()
  }
})
</script>
