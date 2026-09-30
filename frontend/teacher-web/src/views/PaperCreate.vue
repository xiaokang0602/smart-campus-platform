<template>
  <div>
    <div class="card">
      <div class="card-title">试卷信息</div>
      <el-form :inline="true" label-width="70px">
        <el-form-item label="试卷名称"><el-input v-model="paper.paperName" style="width:220px" placeholder="如 初二数学期中测验" /></el-form-item>
        <el-form-item label="科目"><el-select v-model="paper.subject" style="width:130px"><el-option v-for="s in subjects" :key="s" :value="s" :label="s" /></el-select></el-form-item>
        <el-form-item label="年级"><el-select v-model="paper.grade" style="width:110px"><el-option v-for="g in ['初一','初二','初三']" :key="g" :value="g" :label="g" /></el-select></el-form-item>
        <el-form-item label="时长(分)"><el-input-number v-model="paper.examTime" :min="5" :max="180" /></el-form-item>
        <el-form-item label="班级"><el-select v-model="paper.classIds" multiple style="width:220px" placeholder="可参加班级"><el-option v-for="c in classes" :key="c.id" :value="c.id" :label="c.className" /></el-select></el-form-item>
      </el-form>
    </div>

    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">选择题目</div>
        <el-radio-group v-model="mode">
          <el-radio-button :value="1">从题库挑题</el-radio-button>
          <el-radio-button :value="2">AI 实时出题</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 题库挑题 -->
      <div v-if="mode === 1" style="margin-top:16px">
        <el-form :inline="true">
          <el-form-item label="科目"><el-select v-model="bankFilter.subject" style="width:120px" @change="loadBank"><el-option v-for="s in subjects" :key="s" :value="s" :label="s" /></el-select></el-form-item>
          <el-form-item label="知识点"><el-input v-model="bankFilter.keyword" style="width:160px" placeholder="搜索知识点/题干" @keyup.enter="loadBank" /></el-form-item>
          <el-button type="primary" @click="loadBank">查询</el-button>
        </el-form>
        <el-table :data="bankRows" height="320" @selection-change="onSelectBank">
          <el-table-column type="selection" width="45" />
          <el-table-column prop="subject" label="科目" width="80" />
          <el-table-column prop="title" label="题干" min-width="240" show-overflow-tooltip />
          <el-table-column prop="knowledgePoint" label="知识点" width="120" />
          <el-table-column prop="difficulty" label="难度" width="120">
            <template #default="{ row }"><el-rate :model-value="row.difficulty" disabled /></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- AI 出题 -->
      <div v-if="mode === 2" style="margin-top:16px">
        <el-form :inline="true">
          <el-form-item label="科目"><el-select v-model="aiForm.subject" style="width:120px"><el-option v-for="s in subjects" :key="s" :value="s" :label="s" /></el-select></el-form-item>
          <el-form-item label="知识点"><el-input v-model="aiForm.knowledgePoint" style="width:160px" placeholder="如 一次函数" /></el-form-item>
          <el-form-item label="题型"><el-select v-model="aiForm.questionType" style="width:120px"><el-option v-for="(t,i) in types" :key="i" :value="i" :label="t" /></el-select></el-form-item>
          <el-form-item label="数量"><el-input-number v-model="aiForm.count" :min="1" :max="50" /></el-form-item>
          <el-button type="success" :loading="aiLoading" @click="generate">生成候选题</el-button>
        </el-form>
        <el-table v-if="aiList.length" :data="aiList" height="260" @selection-change="onSelectAi">
          <el-table-column type="selection" width="45" />
          <el-table-column prop="title" label="题干" min-width="260" show-overflow-tooltip />
          <el-table-column prop="answer" label="答案" width="70" />
        </el-table>
      </div>
    </div>

    <div class="card">
      <div class="card-title">已选题目（{{ selected.length }} 题）</div>
      <el-table :data="selected">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="题干" min-width="240" show-overflow-tooltip />
        <el-table-column label="分值" width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.score" :min="1" :max="100" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <div class="flex-between mt-16">
        <div>总分：<b style="color:var(--brand); font-size:20px">{{ totalScore }}</b> 分</div>
        <div>
          <el-button type="primary" @click="create(false)">保存草稿</el-button>
          <el-button type="success" @click="create(true)">创建并发布</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { questionList, aiGenerate, adoptQuestions, paperCreate, paperPublish, classList } from '../api'

const router = useRouter()
const subjects = ['语文', '数学', '英语', '道德与法治', '历史', '地理', '物理', '化学', '生物']
const types = ['', '单选题', '多选题', '判断题', '填空题', '简答题']

const paper = reactive({ paperName: '', subject: '数学', grade: '初二', examTime: 40, classIds: [] })
const classes = ref([])
const mode = ref(1)

const bankRows = ref([])
const bankFilter = reactive({ subject: '数学', keyword: '' })
const selected = ref([])

const aiForm = reactive({ subject: '数学', grade: '初二', questionType: 1, knowledgePoint: '', count: 5 })
const aiList = ref([])
const aiLoading = ref(false)

const totalScore = computed(() => selected.value.reduce((s, q) => s + (q.score || 0), 0))

async function loadBank() {
  const data = await questionList({ pageNum: 1, pageSize: 100, ...bankFilter })
  bankRows.value = data.records
}

function onSelectBank(selection) {
  selection.forEach(q => {
    if (!selected.value.find(s => s.questionId === q.id)) {
      selected.value.push({ questionId: q.id, title: q.title, score: 10, source: 1 })
    }
  })
}
function onSelectAi(selection) {
  selection.forEach(q => {
    if (!selected.value.find(s => s.questionId === q.id)) {
      selected.value.push({ questionId: q.id, title: q.title, score: 10, source: 2 })
    }
  })
}

async function generate() {
  aiLoading.value = true
  try { aiList.value = await aiGenerate({ ...aiForm, grade: paper.grade }) } finally { aiLoading.value = false }
}

async function create(publish) {
  if (!paper.paperName) { ElMessage.warning('请输入试卷名称'); return }
  if (!selected.value.length) { ElMessage.warning('请选择题目'); return }
  if (!paper.classIds.length) { ElMessage.warning('请选择可参加班级'); return }

  // AI 出的题一并收入题库
  const aiIds = selected.value.filter(q => q.source === 2).map(q => q.questionId)
  if (aiIds.length) await adoptQuestions(aiIds)

  const id = await paperCreate({
    ...paper,
    totalScore: totalScore.value,
    source: mode.value,
    questions: selected.value.map(q => ({ questionId: q.questionId, score: q.score }))
  })
  if (publish) {
    await paperPublish(id)
    ElMessage.success('试卷已创建并发布')
  } else {
    ElMessage.success('试卷草稿已保存')
  }
  router.push('/exammanage')
}

onMounted(async () => {
  classes.value = await classList()
  loadBank()
})
</script>
