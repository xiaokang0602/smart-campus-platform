<template>
  <div>
    <div class="stat-grid">
      <div class="stat-card"><div class="icon" style="background:#2f6bff"><el-icon><School /></el-icon></div>
        <div><div class="num">{{ data.studentCount ?? '-' }}</div><div class="label">学生总数</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#12b981"><el-icon><Avatar /></el-icon></div>
        <div><div class="num">{{ data.teacherCount ?? '-' }}</div><div class="label">教师总数</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#8b5cf6"><el-icon><Collection /></el-icon></div>
        <div><div class="num">{{ data.classCount ?? '-' }}</div><div class="label">班级总数</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#f59e0b"><el-icon><EditPen /></el-icon></div>
        <div><div class="num">{{ data.examCount ?? '-' }}</div><div class="label">考试总数</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#f43f5e"><el-icon><Files /></el-icon></div>
        <div><div class="num">{{ data.questionCount ?? '-' }}</div><div class="label">题库总量</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#06b6d4"><el-icon><MagicStick /></el-icon></div>
        <div><div class="num">{{ data.aiAnalysisCount ?? '-' }}</div><div class="label">AI 分析次数</div></div></div>
    </div>

    <div class="flex" style="gap:18px; flex-wrap:wrap;">
      <div class="card" style="flex:1; min-width:360px;">
        <div class="card-title">分数段分布</div>
        <div ref="pieRef" style="height:320px"></div>
      </div>
      <div class="card" style="flex:1; min-width:360px;">
        <div class="card-title">各科平均分</div>
        <div ref="barRef" style="height:320px"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { dashboard } from '../api'

const data = ref({})
const pieRef = ref()
const barRef = ref()
let pie = null, bar = null

function renderCharts() {
  const dist = data.value.scoreDistribution || {}
  if (pieRef.value) {
    pie = pie || echarts.init(pieRef.value)
    pie.setOption({
      color: ['#f43f5e', '#f59e0b', '#06b6d4', '#2f6bff', '#12b981'],
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['42%', '68%'], center: ['50%', '45%'],
        label: { formatter: '{b}\n{c}' },
        data: Object.entries(dist).map(([k, v]) => ({ name: k, value: v }))
      }]
    })
  }
  const avg = data.value.subjectAvg || {}
  if (barRef.value) {
    bar = bar || echarts.init(barRef.value)
    bar.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 30, bottom: 40 },
      xAxis: { type: 'category', data: Object.keys(avg) },
      yAxis: { type: 'value', name: '平均分' },
      series: [{
        type: 'bar', barWidth: 40, itemStyle: { borderRadius: [6, 6, 0, 0], color: '#2f6bff' },
        data: Object.values(avg).map(v => Math.round(v * 10) / 10)
      }]
    })
  }
}

function onResize() { pie && pie.resize(); bar && bar.resize() }

onMounted(async () => {
  data.value = await dashboard()
  renderCharts()
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => { window.removeEventListener('resize', onResize) })
</script>
