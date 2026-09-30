<template>
  <div class="exam-wrap">
    <!-- 顶部状态栏 -->
    <div class="exam-top">
      <div class="et-title">{{ paper.paperName }}</div>
      <div class="et-stats">
        <span class="et-item"><el-icon><Clock /></el-icon> {{ remainText }}</span>
        <span class="et-item">题号 {{ current + 1 }}/{{ questions.length }}</span>
        <span class="et-item" :class="{ danger: cheatCount >= maxSwitch }">
          <el-icon><Warning /></el-icon> 切屏 {{ cheatCount }}/{{ maxSwitch }}
        </span>
        <span class="et-item">已答 {{ answeredCount }}</span>
      </div>
      <el-button type="primary" @click="submitExam">交卷</el-button>
    </div>

    <div class="exam-body">
      <!-- 答题区 -->
      <div class="exam-question">
        <div class="eq-head">
          <span class="eq-type">{{ typeName(questions[current].questionType) }}</span>
          <span class="eq-score">（{{ questions[current].score }} 分）</span>
        </div>
        <div class="eq-title">{{ current + 1 }}. {{ questions[current].title }}</div>

        <div class="eq-options" v-if="isChoice(questions[current].questionType)">
          <el-radio-group v-if="questions[current].questionType === 1"
            v-model="answers[questions[current].questionId]" class="opt-list">
            <div v-for="o in options(current)" :key="o.key" class="opt-item">
              <el-radio :value="o.key">{{ o.key }}. {{ o.text }}</el-radio>
            </div>
          </el-radio-group>
          <el-checkbox-group v-else v-model="answers[questions[current].questionId]" class="opt-list">
            <div v-for="o in options(current)" :key="o.key" class="opt-item">
              <el-checkbox :value="o.key">{{ o.key }}. {{ o.text }}</el-checkbox>
            </div>
          </el-checkbox-group>
        </div>

        <div class="eq-options" v-else-if="questions[current].questionType === 3">
          <el-radio-group v-model="answers[questions[current].questionId]" class="opt-list">
            <div class="opt-item"><el-radio value="对">对</el-radio></div>
            <div class="opt-item"><el-radio value="错">错</el-radio></div>
          </el-radio-group>
        </div>

        <div v-else class="eq-options">
          <el-input v-model="answers[questions[current].questionId]" type="textarea" :rows="4"
            placeholder="请输入答案" />
        </div>

        <div class="eq-nav">
          <el-button :disabled="current === 0" @click="current--">上一题</el-button>
          <el-button v-if="current < questions.length - 1" type="primary" @click="current++">下一题</el-button>
          <el-button v-else type="primary" @click="submitExam">完成交卷</el-button>
        </div>
      </div>

      <!-- 答题卡 -->
      <div class="exam-sheet">
        <div class="es-title">答题卡</div>
        <div class="es-grid">
          <div v-for="(q, i) in questions" :key="q.questionId" class="es-cell"
            :class="{ done: !!answers[q.questionId], current: i === current }" @click="current = i">
            {{ i + 1 }}
          </div>
        </div>
        <el-button type="success" style="width:100%; margin-top:16px" @click="submitExam">确认交卷</el-button>
      </div>
    </div>

    <!-- 切屏警告 -->
    <el-dialog v-model="cheatVisible" title="⚠ 切屏警告" width="420px" :close-on-click-modal="false"
      :close-on-press-escape="false" :show-close="false">
      <div style="text-align:center; padding:10px 0;">
        <div style="font-size:15px; margin-bottom:12px; color:#1f2d3d;">
          检测到您已切换考试窗口，本次将被记录为第 <b style="color:#f59e0b">{{ cheatCount }}</b> 次切屏，
          累计超过 {{ maxSwitch }} 次将强制交卷！
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="cheatVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { examStart, examSubmit, examCheat } from '../api'

const route = useRoute()
const router = useRouter()
const paperId = route.params.paperId

const paper = ref({})
const recordId = ref(null)
const questions = ref([])
const answers = ref({})
const current = ref(0)
const cheatCount = ref(0)
const maxSwitch = ref(3)
const cheatVisible = ref(false)

const remainSeconds = ref(0)
let timer = null

const remainText = computed(() => {
  const m = Math.floor(remainSeconds.value / 60)
  const s = remainSeconds.value % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})
const answeredCount = computed(() => questions.value.filter(q => answers.value[q.questionId]).length)

function isChoice(t) { return t === 1 || t === 2 }
function typeName(t) { return ['', '单选题', '多选题', '判断题', '填空题', '简答题'][t] || '题目' }
function options(i) {
  const raw = questions.value[i]?.options || []
  return raw.map(o => ({ key: Object.keys(o)[0], text: Object.values(o)[0] }))
}

function startTimer() {
  timer = setInterval(() => {
    remainSeconds.value--
    if (remainSeconds.value <= 0) {
      clearInterval(timer)
      forceSubmit()
    }
  }, 1000)
}

function bindCheat() {
  document.addEventListener('visibilitychange', onVisibility)
  window.addEventListener('blur', onBlur)
}
function onVisibility() { if (document.hidden) reportCheat() }
function onBlur() { reportCheat() }

async function reportCheat() {
  if (cheatVisible.value) return
  const res = await examCheat({ recordId: recordId.value })
  cheatCount.value = res.count
  if (res.forced) {
    cheatVisible.value = false
    ElMessage.error('切屏超限，已强制交卷')
    forceSubmit()
  } else {
    cheatVisible.value = true
  }
}

async function forceSubmit() {
  try {
    const res = await examSubmit({ recordId: recordId.value, answers: answers.value })
    ElMessageBox.alert(`考试已结束，本次得分：${res.score} 分`, '交卷', { type: 'success' })
      .finally(() => router.push('/scores'))
  } catch (e) { /* ignore */ }
}

async function submitExam() {
  const pending = questions.value.length - answeredCount.value
  try {
    await ElMessageBox.confirm(`还有 ${pending} 题未作答，确定交卷吗？`, '交卷确认', { type: 'warning' })
  } catch (e) { return }
  const res = await examSubmit({ recordId: recordId.value, answers: answers.value })
  ElMessageBox.alert(`交卷成功！本次得分：${res.score} / ${res.fullScore} 分`, '考试完成', { type: 'success' })
    .finally(() => router.push('/scores'))
}

onMounted(async () => {
  const data = await examStart(paperId)
  paper.value = data.paper
  recordId.value = data.recordId
  questions.value = data.questions
  // 初始化答案容器
  data.questions.forEach(q => { if (!answers.value[q.questionId]) answers.value[q.questionId] = q.questionType === 2 ? [] : '' })
  remainSeconds.value = (data.paper.examTime || 60) * 60
  startTimer()
  bindCheat()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('visibilitychange', onVisibility)
  window.removeEventListener('blur', onBlur)
})
</script>

<style scoped>
.exam-wrap { margin: -22px -26px; display: flex; flex-direction: column; height: calc(100vh - 64px); }
.exam-top {
  background: #fff; padding: 14px 24px; display: flex; align-items: center; gap: 24px;
  border-bottom: 1px solid var(--border);
}
.et-title { font-size: 17px; font-weight: 700; flex: 1; }
.et-stats { display: flex; gap: 18px; }
.et-item { display: flex; align-items: center; gap: 5px; font-size: 14px; color: #45536a; }
.et-item.danger { color: #f59e0b; font-weight: 700; }
.exam-body { flex: 1; display: flex; overflow: hidden; }
.exam-question { flex: 1; overflow-y: auto; padding: 28px 34px; background: #fff; margin: 14px; border-radius: 14px; }
.eq-head { margin-bottom: 12px; }
.eq-type { background: var(--el-color-primary-light-9); color: var(--brand); padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.eq-score { color: var(--text-2); font-size: 13px; margin-left: 8px; }
.eq-title { font-size: 17px; font-weight: 600; line-height: 1.7; margin-bottom: 22px; }
.opt-list { display: flex; flex-direction: column; gap: 14px; }
.opt-item { font-size: 15px; }
.eq-nav { margin-top: 32px; display: flex; gap: 12px; }
.exam-sheet { width: 220px; background: #fff; margin: 14px 14px 14px 0; border-radius: 14px; padding: 18px; overflow-y: auto; }
.es-title { font-weight: 700; margin-bottom: 14px; }
.es-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; }
.es-cell {
  height: 34px; border-radius: 8px; background: #f0f4fb; display: flex; align-items: center; justify-content: center;
  font-size: 13px; cursor: pointer;
}
.es-cell.done { background: var(--el-color-primary-light-8); color: var(--brand); }
.es-cell.current { background: var(--brand-bg); color: #fff; }
</style>
