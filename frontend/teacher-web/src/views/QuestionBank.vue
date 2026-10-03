<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">校本题库</div>
        <div>
          <el-button type="primary" :icon="Plus" @click="addVisible = true">手动录题</el-button>
          <el-button type="success" :icon="MagicStick" @click="aiVisible = true">AI 批量出题</el-button>
        </div>
      </div>

      <el-form :inline="true" style="margin-top:14px">
        <el-form-item label="科目">
          <el-select v-model="filter.subject" clearable placeholder="全部" style="width:120px" @change="load">
            <el-option v-for="s in subjects" :key="s" :value="s" :label="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="filter.grade" clearable placeholder="全部" style="width:110px" @change="load">
            <el-option v-for="g in ['高一','高二','高三']" :key="g" :value="g" :label="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="filter.questionType" clearable placeholder="全部" style="width:120px" @change="load">
            <el-option v-for="(t,i) in types" :key="i" :value="i" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" clearable placeholder="题干关键词" style="width:180px" @keyup.enter="load" />
        </el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form>

      <el-table :data="rows" stripe>
        <el-table-column prop="subject" label="科目" width="90" />
        <el-table-column prop="questionType" label="题型" width="90">
          <template #default="{ row }">{{ types[row.questionType] }}</template>
        </el-table-column>
        <el-table-column prop="title" label="题干" min-width="260" show-overflow-tooltip />
        <el-table-column prop="knowledgePoint" label="知识点" width="130" />
        <el-table-column label="难度" width="140">
          <template #default="{ row }">
            <el-rate :model-value="row.difficulty" disabled />
          </template>
        </el-table-column>
        <el-table-column label="来源" width="90">
          <template #default="{ row }">
            <el-tag :type="row.source === 2 ? 'success' : 'info'" size="small">{{ row.source === 2 ? 'AI' : '手动' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" style="margin-top:16px; justify-content:flex-end" @current-change="load" />
    </div>

    <!-- 手动录题 -->
    <el-dialog v-model="addVisible" title="手动录题" width="640px">
      <el-form label-width="90px">
        <el-form-item label="科目"><el-select v-model="addForm.subject" style="width:100%"><el-option v-for="s in subjects" :key="s" :value="s" :label="s" /></el-select></el-form-item>
        <el-form-item label="年级"><el-select v-model="addForm.grade" style="width:100%"><el-option v-for="g in ['高一','高二','高三']" :key="g" :value="g" :label="g" /></el-select></el-form-item>
        <el-form-item label="题型"><el-select v-model="addForm.questionType" style="width:100%"><el-option v-for="(t,i) in types" :key="i" :value="i" :label="t" /></el-select></el-form-item>
        <el-form-item label="题干"><el-input v-model="addForm.title" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="选项" v-if="addForm.questionType === 1 || addForm.questionType === 2">
          <el-input v-model="addForm.options" type="textarea" :rows="2" placeholder='[{"A":"..."},{"B":"..."},{"C":"..."},{"D":"..."}]' />
        </el-form-item>
        <el-form-item label="答案"><el-input v-model="addForm.answer" placeholder="如 A 或 A,D 或 对/错" /></el-form-item>
        <el-form-item label="解析"><el-input v-model="addForm.analysis" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="知识点"><el-input v-model="addForm.knowledgePoint" /></el-form-item>
        <el-form-item label="难度"><el-rate v-model="addForm.difficulty" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">保存</el-button>
      </template>
    </el-dialog>

    <!-- AI 批量出题 -->
    <el-dialog v-model="aiVisible" title="AI 批量出题" width="760px">
      <el-form :inline="true">
        <el-form-item label="科目"><el-select v-model="aiForm.subject" style="width:130px"><el-option v-for="s in subjects" :key="s" :value="s" :label="s" /></el-select></el-form-item>
        <el-form-item label="年级"><el-select v-model="aiForm.grade" style="width:110px"><el-option v-for="g in ['高一','高二','高三']" :key="g" :value="g" :label="g" /></el-select></el-form-item>
        <el-form-item label="题型"><el-select v-model="aiForm.questionType" style="width:120px"><el-option v-for="(t,i) in types" :key="i" :value="i" :label="t" /></el-select></el-form-item>
        <el-form-item label="知识点"><el-input v-model="aiForm.knowledgePoint" style="width:140px" placeholder="如 一次函数" /></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="aiForm.count" :min="1" :max="50" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="aiLoading" @click="generate">生成候选题</el-button></el-form-item>
      </el-form>

      <el-table v-if="aiList.length" :data="aiList" height="300" style="margin-top:12px" @selection-change="onSelect">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="title" label="题干" min-width="220" show-overflow-tooltip />
        <el-table-column prop="answer" label="答案" width="70" />
        <el-table-column prop="knowledgePoint" label="知识点" width="120" />
      </el-table>
      <div v-if="aiList.length" style="margin-top:14px; text-align:right">
        <el-button @click="discardAi">全部丢弃</el-button>
        <el-button type="primary" @click="adoptAi">采纳入库</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import { questionList, questionAdd, questionDelete, aiGenerate, adoptQuestions, discardQuestions } from '../api'

const subjects = ['语文', '数学', '英语', '物理', '化学', '生物', '政治', '历史', '地理']
const types = ['', '单选题', '多选题', '判断题', '填空题', '简答题']

const rows = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 20
const filter = reactive({ subject: '', grade: '', questionType: null, keyword: '' })

const addVisible = ref(false)
const addForm = reactive({ subject: '数学', grade: '高一', questionType: 1, title: '', options: '', answer: '', analysis: '', knowledgePoint: '', difficulty: 3 })

const aiVisible = ref(false)
const aiLoading = ref(false)
const aiForm = reactive({ subject: '数学', grade: '高一', questionType: 1, knowledgePoint: '', count: 10 })
const aiList = ref([])
const aiSelected = ref([])

async function load() {
  const data = await questionList({ pageNum: pageNum.value, pageSize, ...filter })
  rows.value = data.records
  total.value = data.total
}

async function submitAdd() {
  await questionAdd(addForm)
  ElMessage.success('保存成功')
  addVisible.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm('确定删除该题吗？', '提示', { type: 'warning' })
  await questionDelete(row.id)
  ElMessage.success('已删除')
  load()
}

async function generate() {
  aiLoading.value = true
  try {
    aiList.value = await aiGenerate(aiForm)
    aiSelected.value = []
  } finally {
    aiLoading.value = false
  }
}

function onSelect(selection) {
  aiSelected.value = selection
}

async function adoptAi() {
  if (!aiSelected.value.length) { ElMessage.warning('请先勾选题目'); return }
  await adoptQuestions(aiSelected.value.map(q => q.id))
  ElMessage.success(`已采纳 ${aiSelected.value.length} 道题目入库`)
  aiVisible.value = false
  aiList.value = []
  load()
}

async function discardAi() {
  await discardQuestions(aiList.value.map(q => q.id))
  ElMessage.info('已丢弃该批次')
  aiList.value = []
}

onMounted(load)
</script>
