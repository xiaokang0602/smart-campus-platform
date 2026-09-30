<template>
  <div>
    <div class="card" style="background:var(--brand-bg); color:#fff; border:none;">
      <div style="font-size:22px; font-weight:700;">
        {{ user.realName }} 老师 · {{ user.isHeadTeacher ? '班主任' : '任课教师' }}
      </div>
      <div style="margin-top:8px; opacity:.9;">
        任教科目：{{ subjects.join('、') }}　·　任教班级：{{ classNames.join('、') }}
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="icon" style="background:linear-gradient(135deg,#2f6bff,#5b8cff)"><el-icon><Document /></el-icon></div>
        <div><div class="num">{{ info.paperCount || 0 }}</div><div class="label">我的试卷</div></div>
      </div>
      <div class="stat-card">
        <div class="icon" style="background:linear-gradient(135deg,#8b5cf6,#a78bfa)"><el-icon><User /></el-icon></div>
        <div><div class="num">{{ info.classIds?.length || 0 }}</div><div class="label">任教班级</div></div>
      </div>
      <div class="stat-card" v-if="info.isHeadTeacher">
        <div class="icon" style="background:linear-gradient(135deg,#f59e0b,#fbbf24)"><el-icon><Bell /></el-icon></div>
        <div><div class="num">{{ info.pendingReps || 0 }}</div><div class="label">待审批课代表</div></div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">快捷入口</div>
      <div class="stat-grid">
        <div v-for="e in entries" :key="e.path" class="stat-card" style="cursor:pointer; flex-direction:column; align-items:flex-start; gap:10px"
          @click="router.push(e.path)">
          <div class="icon" :style="{ background: e.bg }"><el-icon><component :is="e.icon" /></el-icon></div>
          <div style="font-weight:600;">{{ e.title }}</div>
          <div class="label">{{ e.desc }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dashboard } from '../api'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const info = ref({})

const subjects = computed(() => info.value.subjects ? [...info.value.subjects] : [])
const classNames = computed(() => {
  const classes = user.value.classes || []
  return classes.map(c => c.className)
})

const entries = [
  { path: '/students', title: '班级学生', desc: '查看学生与任职', icon: 'User', bg: 'linear-gradient(135deg,#2f6bff,#5b8cff)' },
  { path: '/questionbank', title: '校本题库', desc: '手动录题 + AI 出题', icon: 'Collection', bg: 'linear-gradient(135deg,#8b5cf6,#a78bfa)' },
  { path: '/papercreate', title: '组卷发布', desc: '创建并发布考试', icon: 'DocumentAdd', bg: 'linear-gradient(135deg,#12b981,#34d399)' },
  { path: '/exammanage', title: '考试管理', desc: '实时看板与解封', icon: 'Monitor', bg: 'linear-gradient(135deg,#f59e0b,#fbbf24)' },
  { path: '/scores', title: '成绩查看', desc: '成绩与学生画像', icon: 'TrendCharts', bg: 'linear-gradient(135deg,#f43f5e,#fb7185)' }
]

onMounted(async () => { try { info.value = await dashboard() } catch (e) { /* ignore */ } })
</script>
