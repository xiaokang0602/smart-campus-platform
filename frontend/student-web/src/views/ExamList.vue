<template>
  <div>
    <div class="card">
      <div class="card-title">在线考试</div>
      <div class="stat-grid">
        <div v-for="p in papers" :key="p.id" class="exam-card">
          <div class="ec-subject" :style="{ background: subjectColor(p.subject) }">{{ p.subject }}</div>
          <div class="ec-name">{{ p.paperName }}</div>
          <div class="ec-meta">
            <el-icon><Clock /></el-icon> {{ p.examTime }} 分钟
            <span style="margin-left:12px"><el-icon><Document /></el-icon> 满分 {{ p.totalScore }}</span>
          </div>
          <el-button type="primary" style="width:100%; margin-top:14px" @click="enter(p)">进入考试</el-button>
        </div>
      </div>
      <el-empty v-if="papers.length === 0" description="暂无可参加的考试" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studentPapers } from '../api'

const router = useRouter()
const papers = ref([])

const colors = {
  '语文': '#ef4444', '数学': '#2f6bff', '英语': '#8b5cf6',
  '物理': '#0ea5e9', '化学': '#f59e0b', '生物': '#12b981',
  '历史': '#d97706', '地理': '#14b8a6', '道德与法治': '#f43f5e'
}
function subjectColor(s) { return colors[s] || '#2f6bff' }
function enter(p) { router.push(`/exam/${p.id}`) }

onMounted(async () => { papers.value = await studentPapers() })
</script>

<style scoped>
.exam-card {
  background: #fff; border-radius: 14px; padding: 20px;
  box-shadow: 0 4px 18px rgba(20, 32, 63, 0.06);
  border: 1px solid var(--border); transition: all .2s;
}
.exam-card:hover { transform: translateY(-3px); box-shadow: 0 10px 26px rgba(20, 32, 63, 0.12); }
.ec-subject { display: inline-block; color: #fff; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.ec-name { font-size: 16px; font-weight: 700; margin: 12px 0 8px; }
.ec-meta { color: var(--text-2); font-size: 13px; display: flex; align-items: center; gap: 4px; }
</style>
