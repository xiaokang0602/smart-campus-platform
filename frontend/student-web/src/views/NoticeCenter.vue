<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">通知中心</div>
        <el-radio-group v-model="type" size="small" @change="load">
          <el-radio-button :value="0">全部</el-radio-button>
          <el-radio-button v-for="t in types" :key="t.value" :value="t.value">{{ t.label }}</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-for="m in messages" :key="m.receiverId" class="notice-card" :class="{ unread: m.isRead === 0 }"
      @click="open(m)">
      <div class="nc-icon" :style="{ background: typeColor(m.noticeType) }">
        <el-icon><Bell /></el-icon>
      </div>
      <div style="flex:1">
        <div class="flex-between">
          <b>{{ m.title }}</b>
          <el-tag v-if="m.isTop === 1" size="small" type="danger">置顶</el-tag>
        </div>
        <div class="text-muted" style="font-size:13px; margin-top:6px; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">{{ m.content }}</div>
        <div class="text-muted" style="font-size:12px; margin-top:6px;">{{ m.publisher }} · {{ m.createTime }}</div>
      </div>
      <span v-if="m.isRead === 0" class="nc-dot"></span>
    </div>
    <el-empty v-if="messages.length === 0" description="暂无通知" />

    <el-dialog v-model="detailVisible" title="通知详情" width="560px">
      <h3>{{ detail.title }}</h3>
      <div style="white-space:pre-wrap; line-height:1.9; color:#45536a;">{{ detail.content }}</div>
      <div class="text-muted" style="margin-top:14px; font-size:12px">{{ detail.publisher }} · {{ detail.createTime }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myMessages, readMessage } from '../api'

const type = ref(0)
const messages = ref([])
const detailVisible = ref(false)
const detail = ref({})

const types = [
  { value: 1, label: '系统公告' }, { value: 2, label: '考试通知' },
  { value: 3, label: '审批结果' }, { value: 4, label: '班级通知' }, { value: 5, label: '个人提醒' }
]

function typeColor(t) {
  return ['', '#2f6bff', '#12b981', '#8b5cf6', '#f59e0b', '#f43f5e'][t] || '#2f6bff'
}

async function load() {
  messages.value = await myMessages(type.value === 0 ? null : type.value)
}

async function open(m) {
  detail.value = m
  detailVisible.value = true
  if (m.isRead === 0) {
    await readMessage(m.receiverId)
    m.isRead = 1
  }
}

onMounted(load)
</script>

<style scoped>
.nc-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 20px; flex-shrink: 0; }
.nc-dot { width: 9px; height: 9px; border-radius: 50%; background: #f43f5e; flex-shrink: 0; margin-top: 8px; }
</style>
