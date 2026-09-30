<template>
  <div>
    <div class="card">
      <div class="tt-head">
        <div class="card-title" style="margin-bottom:0">课表设置</div>
        <div style="display:flex; gap:12px; align-items:center;">
          <el-select v-model="classId" placeholder="选择班级" style="width:170px" @change="load">
            <el-option v-for="c in classes" :key="c.id" :value="c.id" :label="c.className" />
          </el-select>
          <el-button type="primary" :disabled="!classId" @click="save">保存课表</el-button>
        </div>
      </div>
      <div style="margin-top:10px; font-size:12px; color:#8a96ad;">
        为每个格子选择科目与任课教师，点击「保存课表」后立即生效。留空的格子表示该节无课。
      </div>
    </div>

    <div class="card" v-if="classId">
      <div class="tt-wrap">
        <table class="tt-table">
          <thead>
            <tr>
              <th class="corner">节次</th>
              <th v-for="d in days" :key="d">{{ dayName(d) }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in periods" :key="p">
              <td class="tt-period">第{{ p }}节</td>
              <td v-for="d in days" :key="d">
                <div class="cell-edit">
                  <el-select v-model="grid[d][p].subject" size="small" placeholder="科目" clearable style="width:100%">
                    <el-option v-for="s in subjects" :key="s" :value="s" :label="s" />
                  </el-select>
                  <el-select v-model="grid[d][p].teacherId" size="small" placeholder="教师" clearable filterable style="width:100%">
                    <el-option v-for="t in teachers" :key="t.id" :value="t.id" :label="t.realName" />
                  </el-select>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { classList, userList, timetableByClass, timetableSave } from '../api'

const subjects = ['语文', '数学', '英语', '政治', '历史', '地理', '物理', '化学', '生物']
const days = [1, 2, 3, 4, 5]
const periods = [1, 2, 3, 4, 5, 6, 7]
const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

const classes = ref([])
const teachers = ref([])
const classId = ref(null)

// 可编辑网格：grid[周][节] = { subject, teacherId }
const grid = reactive({})
days.forEach(d => {
  grid[d] = {}
  periods.forEach(p => { grid[d][p] = { subject: '', teacherId: null } })
})

function dayName(d) { return dayNames[d] || `周${d}` }

async function load() {
  if (!classId.value) return
  const list = await timetableByClass(classId.value)
  days.forEach(d => periods.forEach(p => { grid[d][p] = { subject: '', teacherId: null } }))
  for (const t of list) {
    if (grid[t.week]) {
      grid[t.week][t.period] = { subject: t.subject, teacherId: t.teacherId || null }
    }
  }
}

async function save() {
  const items = []
  days.forEach(d => periods.forEach(p => {
    const c = grid[d][p]
    if (c.subject) {
      items.push({ week: d, period: p, subject: c.subject, room: '', teacherId: c.teacherId || null })
    }
  }))
  await timetableSave({ classId: classId.value, items })
  ElMessage.success('课表已保存')
  await load()
}

onMounted(async () => {
  classes.value = await classList()
  const res = await userList({ pageSize: 200 })
  teachers.value = (res.records || []).filter(u => u.role === 2 || u.role === 3)
})
</script>

<style scoped>
.tt-head { display: flex; justify-content: space-between; align-items: center; }
.tt-wrap { margin-top: 16px; overflow-x: auto; }
.tt-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
.tt-table th, .tt-table td { border: 1px solid #e6ebf4; text-align: center; vertical-align: middle; }
.tt-table th { background: #f0f4fb; color: #45536a; font-weight: 600; font-size: 14px; padding: 12px 6px; }
.corner { width: 90px; }
.tt-period { width: 90px; background: #f8fafc; color: #45536a; font-size: 13px; font-weight: 600; }
.tt-table td { padding: 6px; }
.cell-edit { display: flex; flex-direction: column; gap: 6px; }
</style>
