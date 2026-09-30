<template>
  <div>
    <div class="card">
      <div class="flex gap-8" style="flex-wrap:wrap; align-items:center;">
        <el-select v-model="paperId" placeholder="选择试卷" clearable style="width:240px" @change="onPaperChange">
          <el-option v-for="p in papers" :key="p.id" :value="p.id" :label="`${p.paperName}（${p.subject}）`" />
        </el-select>
        <el-button v-if="paperId" type="success" plain @click="exportExcel">导出 Excel</el-button>
      </div>
    </div>

    <div class="card">
      <div class="card-title">成绩明细</div>
      <el-table :data="scores" stripe>
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="score" label="分数" width="90" />
        <el-table-column prop="fullScore" label="满分" width="90" />
        <el-table-column prop="cheatSwitchCount" label="切屏次数" width="100" />
        <el-table-column prop="reopenCount" label="解封次数" width="100" />
        <el-table-column prop="submitTime" label="提交时间" min-width="160" />
      </el-table>
      <el-empty v-if="!paperId" description="请先选择试卷" />
      <el-empty v-else-if="scores.length === 0" description="暂无成绩数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { paperList, paperScores, scoreExport } from '../api'

const papers = ref([])
const paperId = ref(null)
const scores = ref([])

async function onPaperChange() {
  if (!paperId.value) { scores.value = []; return }
  scores.value = await paperScores(paperId.value)
}

function exportExcel() {
  window.open(scoreExport(paperId.value), '_blank')
}

onMounted(async () => {
  const res = await paperList({ pageSize: 200 })
  papers.value = res.records || []
})
</script>
