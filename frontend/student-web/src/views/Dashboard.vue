<template>
  <div>
    <!-- 欢迎横幅 -->
    <div class="card" style="background:var(--brand-bg); color:#fff; border:none;">
      <div style="font-size:22px; font-weight:700;">你好，{{ user.realName }} 👋</div>
      <div style="margin-top:8px; opacity:.9;">
        班级：{{ info.className || '—' }}　·　任职：{{ info.duty || '无' }}　·　学号：{{ user.studentNo || '—' }}
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="icon" style="background:linear-gradient(135deg,#2f6bff,#5b8cff)"><el-icon><EditPen /></el-icon></div>
        <div><div class="num">{{ info.examCount || 0 }}</div><div class="label">可参加考试</div></div>
      </div>
      <div class="stat-card">
        <div class="icon" style="background:linear-gradient(135deg,#12b981,#34d399)"><el-icon><Finished /></el-icon></div>
        <div><div class="num">{{ info.finishedCount || 0 }}</div><div class="label">已完成考试</div></div>
      </div>
      <div class="stat-card">
        <div class="icon" style="background:linear-gradient(135deg,#f59e0b,#fbbf24)"><el-icon><TrendCharts /></el-icon></div>
        <div><div class="num">9</div><div class="label">中考科目</div></div>
      </div>
      <div class="stat-card">
        <div class="icon" style="background:linear-gradient(135deg,#8b5cf6,#a78bfa)"><el-icon><MagicStick /></el-icon></div>
        <div><div class="num">AI</div><div class="label">学情分析</div></div>
      </div>
    </div>

    <!-- 快捷入口 -->
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dashboard } from '../api'

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const info = ref({})

const entries = [
  { path: '/timetable', title: '我的课表', desc: '查看本周课程安排', icon: 'Calendar', bg: 'linear-gradient(135deg,#2f6bff,#5b8cff)' },
  { path: '/exam', title: '在线考试', desc: '参加教师发布的考试', icon: 'EditPen', bg: 'linear-gradient(135deg,#8b5cf6,#a78bfa)' },
  { path: '/scores', title: '我的成绩', desc: '历次考试成绩', icon: 'TrendCharts', bg: 'linear-gradient(135deg,#12b981,#34d399)' },
  { path: '/ai', title: 'AI 学情分析', desc: '智能诊断薄弱知识点', icon: 'MagicStick', bg: 'linear-gradient(135deg,#f59e0b,#fbbf24)' },
  { path: '/notice', title: '通知中心', desc: '查看学校与班级通知', icon: 'Bell', bg: 'linear-gradient(135deg,#f43f5e,#fb7185)' }
]

onMounted(async () => {
  try { info.value = await dashboard() } catch (e) { /* ignore */ }
})
</script>
