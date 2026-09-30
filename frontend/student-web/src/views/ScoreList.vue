<template>
  <div>
    <div class="card">
      <div class="card-title">成绩趋势</div>
      <div ref="chartRef" style="height: 300px;"></div>
    </div>

    <div class="card">
      <div class="card-title">历次成绩</div>
      <el-table :data="scores" stripe>
        <el-table-column prop="paperName" label="试卷名称" min-width="180" />
        <el-table-column prop="subject" label="科目" width="110" />
        <el-table-column label="分数" width="120">
          <template #default="{ row }">
            <b style="color:var(--brand)">{{ row.score }}</b> / {{ row.fullScore }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="交卷时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'danger'">{{ row.status === 2 ? '正常交卷' : '切屏交卷' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="showAnalysis(row)">AI 分析</el-button>
            <el-button link type="primary" @click="router.push(`/history/${row.paperId}`)">看试卷</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="analysisVisible" title="AI 学情分析" width="560px">
      <div style="white-space:pre-wrap; line-height:1.9;">{{ analysisContent }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { myScores, scoreAnalysis } from '../api'

const router = useRouter()
const scores = ref([])
const chartRef = ref(null)
const analysisVisible = ref(false)
const analysisContent = ref('')

function renderChart() {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  const reversed = [...scores.value].reverse()
  chart.setOption({
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: reversed.map(s => s.paperName), axisLabel: { interval: 0, rotate: 20 } },
    yAxis: { type: 'value', name: '分数' },
    series: [{
      name: '成绩', type: 'line', smooth: true, data: reversed.map(s => s.score),
      areaStyle: { opacity: 0.15 }, itemStyle: { color: '#2f6bff' },
      lineStyle: { width: 3, color: '#2f6bff' }
    }]
  })
}

async function showAnalysis(row) {
  const data = await scoreAnalysis(row.recordId)
  analysisContent.value = data.content
  analysisVisible.value = true
}

onMounted(async () => {
  scores.value = await myScores()
  await nextTick()
  renderChart()
})
</script>
