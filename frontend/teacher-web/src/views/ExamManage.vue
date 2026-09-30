<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">我的考试</div>
        <el-button type="primary" :icon="Plus" @click="router.push('/papercreate')">新建试卷</el-button>
      </div>

      <el-table :data="papers" stripe style="margin-top:16px">
        <el-table-column prop="paperName" label="试卷名称" min-width="200" />
        <el-table-column prop="subject" label="科目" width="90" />
        <el-table-column prop="grade" label="年级" width="90" />
        <el-table-column label="总分" width="90"><template #default="{ row }">{{ row.totalScore }}</template></el-table-column>
        <el-table-column label="时长" width="90"><template #default="{ row }">{{ row.examTime }}分</template></el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="['info', 'success', 'danger'][row.status]">{{ ['未发布', '进行中', '已结束'][row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button link type="primary" @click="openBoard(row)">实时看板</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="publish(row)">发布</el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="end(row)">结束</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 实时看板 -->
    <el-drawer v-model="boardVisible" :title="`${currentPaper?.paperName} · 实时看板`" size="72%">
      <div class="stat-grid" style="grid-template-columns:repeat(4,1fr); padding:0 20px">
        <div class="stat-card"><div class="icon" style="background:#2f6bff"><el-icon><User /></el-icon></div><div><div class="num">{{ records.length }}</div><div class="label">已交卷</div></div></div>
        <div class="stat-card"><div class="icon" style="background:#f59e0b"><el-icon><Warning /></el-icon></div><div><div class="num">{{ abnormal.length }}</div><div class="label">异常学生</div></div></div>
      </div>

      <el-table :data="records" stripe style="margin:16px 20px; width:calc(100% - 40px)">
        <el-table-column prop="rank" label="排名" width="70" />
        <el-table-column prop="studentName" label="姓名" width="110" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column label="得分" width="100"><template #default="{ row }"><b style="color:var(--brand)">{{ row.score }}</b></template></el-table-column>
        <el-table-column label="切屏" width="90">
          <template #default="{ row }">
            <span :style="{ color: row.cheatSwitchCount > 0 ? '#f59e0b' : '' }">{{ row.cheatSwitchCount }}/3</span>
          </template>
        </el-table-column>
        <el-table-column prop="reopenCount" label="解封次数" width="90" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'danger'" size="small">{{ row.status === 2 ? '正常' : '切屏交卷' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button v-if="row.status === 3" type="warning" size="small" @click="reopen(row)">重新开放考试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { paperList, paperPublish, paperEnd, paperScores, examReopen } from '../api'

const router = useRouter()
const papers = ref([])
const boardVisible = ref(false)
const currentPaper = ref(null)
const records = ref([])

const abnormal = computed(() => records.value.filter(r => r.status === 3))

async function load() { papers.value = (await paperList({ pageSize: 100 })).records }

async function publish(row) { await paperPublish(row.id); ElMessage.success('已发布'); load() }
async function end(row) { await paperEnd(row.id); ElMessage.success('已结束'); load() }

async function openBoard(row) {
  currentPaper.value = row
  records.value = await paperScores(row.id)
  boardVisible.value = true
}

async function reopen(row) {
  await examReopen(row.recordId)
  ElMessage.success('已重新开放考试')
  records.value = await paperScores(currentPaper.value.id)
}

onMounted(load)
</script>
