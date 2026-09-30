<template>
  <div>
    <div class="card">
      <div class="flex gap-8" style="flex-wrap:wrap;">
        <el-select v-model="q.subject" placeholder="科目" clearable style="width:120px" @change="load(1)">
          <el-option v-for="s in subjects" :key="s" :value="s" :label="s" />
        </el-select>
        <el-select v-model="q.questionType" placeholder="题型" clearable style="width:120px" @change="load(1)">
          <el-option v-for="(t, i) in typeText" :key="i" :value="i" :label="t" />
        </el-select>
        <el-select v-model="q.reviewStatus" placeholder="审核状态" clearable style="width:130px" @change="load(1)">
          <el-option :value="0" label="待审核" />
          <el-option :value="1" label="已入库" />
          <el-option :value="2" label="已丢弃" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="题目关键词" clearable style="width:180px" @keyup.enter="load(1)" />
        <el-button type="primary" @click="load(1)">查询</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" stripe>
        <el-table-column prop="title" label="题目" min-width="280" show-overflow-tooltip />
        <el-table-column prop="subject" label="科目" width="90" />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">{{ typeText[row.questionType] }}</template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" />
        <el-table-column label="来源" width="90">
          <template #default="{ row }">{{ row.source === 1 ? '教师' : 'AI' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['warning', 'success', 'info'][row.reviewStatus]" size="small">{{ ['待审核', '已入库', '已丢弃'][row.reviewStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="mt-16" layout="total, prev, pager, next" :total="total" :page-size="q.pageSize"
        :current-page="q.pageNum" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" title="编辑题目" width="560px">
      <el-form label-width="90px">
        <el-form-item label="题目"><el-input v-model="form.title" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="科目"><el-input v-model="form.subject" /></el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.questionType" style="width:100%">
            <el-option v-for="(t, i) in typeText" :key="i" :value="i" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-rate v-model="form.difficulty" :max="5" />
        </el-form-item>
        <el-form-item label="选项(JSON)"><el-input v-model="form.options" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="答案"><el-input v-model="form.answer" /></el-form-item>
        <el-form-item label="解析"><el-input v-model="form.analysis" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionList, questionUpdate, questionDelete } from '../api'

const subjects = ['语文', '数学', '英语', '道德与法治', '历史', '地理', '物理', '化学', '生物']
const typeText = ['', '单选题', '多选题', '判断题', '填空题', '简答题']
const rows = ref([])
const total = ref(0)
const q = reactive({ pageNum: 1, pageSize: 10, subject: '', questionType: null, reviewStatus: null, keyword: '' })
const dialog = ref(false)
const form = reactive({})

async function load(p) {
  if (p) q.pageNum = p
  const res = await questionList({
    pageNum: q.pageNum, pageSize: q.pageSize,
    subject: q.subject || undefined, questionType: q.questionType ?? undefined,
    reviewStatus: q.reviewStatus ?? undefined, keyword: q.keyword || undefined
  })
  rows.value = res.records
  total.value = res.total
}

function openEdit(row) { Object.assign(form, row); dialog.value = true }

async function submit() {
  await questionUpdate(form)
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
  await questionDelete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load())
</script>
