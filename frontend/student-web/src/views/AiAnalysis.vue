<template>
  <div>
    <div class="card">
      <div class="card-title">AI 学情分析</div>
      <el-select v-model="recordId" placeholder="选择一次考试" style="width:320px" @change="loadAnalysis">
        <el-option v-for="s in scores" :key="s.recordId" :value="s.recordId"
          :label="`${s.paperName}（${s.score}分）`" />
      </el-select>
    </div>

    <div v-if="analysis" class="card">
      <div class="card-title">分析报告</div>
      <div class="analysis-content">{{ analysis }}</div>
    </div>
    <el-empty v-else description="选择一次考试查看 AI 分析" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myScores, scoreAnalysis } from '../api'

const scores = ref([])
const recordId = ref(null)
const analysis = ref('')

async function loadAnalysis() {
  const data = await scoreAnalysis(recordId.value)
  analysis.value = data.content
}

onMounted(async () => {
  scores.value = await myScores()
  if (scores.value.length) {
    recordId.value = scores.value[0].recordId
    loadAnalysis()
  }
})
</script>

<style scoped>
.analysis-content { white-space: pre-wrap; line-height: 2; font-size: 14px; color: #45536a; }
</style>
