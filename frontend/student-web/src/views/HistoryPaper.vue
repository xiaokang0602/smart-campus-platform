<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">{{ paper?.paperName }}</div>
        <div v-if="record" class="text-muted">
          得分：<b style="color:var(--brand); font-size:18px">{{ record.totalScore }}</b> / {{ paper.totalScore }}
        </div>
      </div>
    </div>

    <div class="card" v-for="(q, i) in questions" :key="q.questionId">
      <div class="flex-between">
        <div class="eq-title" style="margin-bottom:12px">
          <span class="eq-type">{{ typeName(q.questionType) }}</span>
          <span style="margin-left:8px; font-weight:600">{{ i + 1 }}. {{ q.title }}</span>
          <span class="text-muted" style="font-size:12px">（{{ q.score }} 分）</span>
        </div>
        <el-tag :type="q.isCorrect ? 'success' : 'danger'" effect="dark">{{ q.isCorrect ? '正确' : '错误' }}</el-tag>
      </div>

      <div v-if="q.options && q.options.length" style="margin:8px 0">
        <div v-for="o in q.options" :key="Object.keys(o)[0]">
          {{ Object.keys(o)[0] }}. {{ Object.values(o)[0] }}
        </div>
      </div>

      <el-alert :title="`我的答案：${q.studentAnswer || '未作答'}`" type="info" :closable="false" style="margin:8px 0" />
      <el-alert :title="`正确答案：${q.answer}`" type="success" :closable="false" style="margin:8px 0" />
      <div v-if="q.analysis" class="analysis-box">
        <el-icon><InfoFilled /></el-icon> <b>解析：</b>{{ q.analysis }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { examHistory } from '../api'

const route = useRoute()
const paper = ref({})
const record = ref(null)
const questions = ref([])

function typeName(t) { return ['', '单选题', '多选题', '判断题', '填空题', '简答题'][t] || '题目' }

onMounted(async () => {
  const data = await examHistory(route.params.paperId)
  paper.value = data.paper
  record.value = data.record
  questions.value = data.questions
})
</script>

<style scoped>
.eq-type { background: var(--el-color-primary-light-9); color: var(--brand); padding: 2px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.analysis-box { background: #f7f9fc; border-radius: 10px; padding: 12px 14px; margin-top: 8px; color: #45536a; font-size: 13px; line-height: 1.7; }
</style>
