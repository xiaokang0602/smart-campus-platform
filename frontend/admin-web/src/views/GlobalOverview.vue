<template>
  <div>
    <div class="stat-grid">
      <div class="stat-card"><div class="icon" style="background:#2f6bff"><el-icon><OfficeBuilding /></el-icon></div>
        <div><div class="num">{{ data.schoolCount ?? '-' }}</div><div class="label">入驻学校</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#12b981"><el-icon><School /></el-icon></div>
        <div><div class="num">{{ data.studentCount ?? '-' }}</div><div class="label">学生总数</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#8b5cf6"><el-icon><Avatar /></el-icon></div>
        <div><div class="num">{{ data.teacherCount ?? '-' }}</div><div class="label">教师总数</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#f59e0b"><el-icon><EditPen /></el-icon></div>
        <div><div class="num">{{ data.examCount ?? '-' }}</div><div class="label">考试记录</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#f43f5e"><el-icon><Files /></el-icon></div>
        <div><div class="num">{{ data.questionCount ?? '-' }}</div><div class="label">题库总量</div></div></div>
      <div class="stat-card"><div class="icon" style="background:#06b6d4"><el-icon><MagicStick /></el-icon></div>
        <div><div class="num">{{ data.aiAnalysisCount ?? '-' }}</div><div class="label">AI 分析次数</div></div></div>
    </div>

    <div class="card">
      <div class="card-title">跨校数据总览</div>
      <el-empty description="以上为全平台（所有学校）汇总数据" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { globalOverview } from '../api'

const data = ref({})

onMounted(async () => {
  data.value = await globalOverview()
})
</script>
