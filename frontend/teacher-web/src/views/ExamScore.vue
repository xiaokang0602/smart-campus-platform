<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">成绩查看</div>
        <el-select v-model="paperId" placeholder="选择试卷" style="width:300px" @change="load">
          <el-option v-for="p in papers" :key="p.id" :value="p.id" :label="p.paperName" />
        </el-select>
      </div>

      <el-table :data="rows" stripe style="margin-top:16px">
        <el-table-column prop="rank" label="排名" width="70" />
        <el-table-column label="姓名" width="130">
          <template #default="{ row }">
            <a class="link" @click="openPortrait(row)">{{ row.studentName }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column label="得分" width="100">
          <template #default="{ row }"><b style="color:var(--brand)">{{ row.score }}</b> / {{ row.fullScore }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="交卷时间" width="180" />
        <el-table-column prop="cheatSwitchCount" label="切屏" width="80" />
      </el-table>
    </div>

    <!-- 学生画像抽屉 -->
    <el-drawer v-model="drawerVisible" size="56%">
      <template #header>
        <div class="flex gap-8" style="align-items:center">
          <el-avatar :size="48" style="background:var(--brand-bg); font-size:20px">{{ (portrait.student?.realName || '生').slice(0,1) }}</el-avatar>
          <div>
            <div style="font-weight:700; font-size:16px">{{ portrait.student?.realName }}</div>
            <div class="text-muted" style="font-size:12px">{{ portrait.studentNo }} · {{ portrait.className }} · {{ portrait.duty }}</div>
          </div>
        </div>
      </template>

      <div ref="chartRef" style="height:260px;"></div>

      <div class="card" style="box-shadow:none; border:1px solid var(--border)">
        <div class="card-title">AI 实时分析</div>
        <div v-if="portrait.aiAnalysis" style="white-space:pre-wrap; line-height:1.9; font-size:14px; color:#45536a;">{{ portrait.aiAnalysis }}</div>
        <el-empty v-else description="暂无分析" :image-size="60" />
        <el-button type="primary" :loading="aiLoading" style="width:100%" @click="genAi">
          <el-icon><MagicStick /></el-icon> AI 实时分析
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { paperList, paperScores, studentPortrait, aiPortrait } from '../api'

const papers = ref([])
const paperId = ref(null)
const rows = ref([])

const drawerVisible = ref(false)
const portrait = ref({})
const chartRef = ref(null)
const aiLoading = ref(false)

async function load() {
  if (!paperId.value) return
  rows.value = await paperScores(paperId.value)
}

async function openPortrait(row) {
  drawerVisible.value = true
  portrait.value = await studentPortrait(row.studentId)
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!chartRef.value) return
  const trend = portrait.value.trend || []
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: trend.map(t => t.paperName || t.subject), axisLabel: { rotate: 20 } },
    yAxis: { type: 'value' },
    series: [{ name: '分数', type: 'line', smooth: true, data: trend.map(t => t.score), areaStyle: { opacity: 0.12 }, itemStyle: { color: '#2f6bff' }, lineStyle: { color: '#2f6bff', width: 3 } }]
  })
}

async function genAi() {
  aiLoading.value = true
  try {
    await aiPortrait(portrait.value.student.id, null)
    portrait.value = await studentPortrait(portrait.value.student.id)
    ElMessage.success('AI 分析完成')
  } finally {
    aiLoading.value = false
  }
}

onMounted(async () => {
  papers.value = (await paperList({ pageSize: 100 })).records
  if (papers.value.length) {
    paperId.value = papers.value[0].id
    load()
  }
})
</script>
