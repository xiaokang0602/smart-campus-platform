<template>
  <div class="card">
    <div class="card-title">我的课表</div>

    <div class="tt-wrap">
      <table class="tt-table">
        <thead>
          <tr>
            <th class="corner">节次</th>
            <th v-for="d in days" :key="d">{{ dayName(d) }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in periods" :key="p">
            <td class="tt-period">第{{ p }}节</td>
            <td v-for="d in days" :key="d">
              <div v-if="cell(d, p)" class="lesson" :style="{ background: subjectColor(cell(d, p).subject) }">
                <div class="ls-sub">{{ cell(d, p).subject }}</div>
                <div class="ls-t">{{ cell(d, p).teacherName }}</div>
                <div class="ls-r">{{ cell(d, p).room }}</div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { myTimetable } from '../api'

const list = ref([])

const days = computed(() => [...new Set(list.value.map(t => t.week))].sort((a, b) => a - b))
const periods = computed(() => [...new Set(list.value.map(t => t.period))].sort((a, b) => a - b))

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
function dayName(d) { return dayNames[d] || `周${d}` }

const colors = {
  '语文': '#ef4444', '数学': '#2f6bff', '英语': '#8b5cf6',
  '物理': '#0ea5e9', '化学': '#f59e0b', '生物': '#12b981',
  '历史': '#d97706', '地理': '#14b8a6', '政治': '#f43f5e',
  '体育': '#6b7280', '音乐': '#ec4899', '美术': '#f97316'
}

function subjectColor(s) { return colors[s] || '#2f6bff' }
function cell(d, p) { return list.value.find(t => t.week === d && t.period === p) }

onMounted(async () => { list.value = await myTimetable() })
</script>

<style scoped>
.tt-wrap { margin-top: 16px; overflow-x: auto; }
.tt-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
.tt-table th, .tt-table td { border: 1px solid #e6ebf4; text-align: center; vertical-align: middle; }
.tt-table th { background: #f0f4fb; color: #45536a; font-weight: 600; font-size: 14px; padding: 12px 6px; }
.corner { width: 64px; }
.tt-period { width: 64px; background: #f8fafc; color: #8a96ad; font-size: 13px; font-weight: 600; }
.tt-table td { height: 64px; padding: 4px; }
.lesson {
  height: 56px; border-radius: 8px; color: #fff; padding: 6px 8px;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  box-shadow: 0 3px 8px rgba(0, 0, 0, .12);
}
.ls-sub { font-weight: 700; font-size: 14px; line-height: 1.2; }
.ls-t { font-size: 11px; opacity: .9; margin-top: 2px; }
.ls-r { font-size: 10px; opacity: .75; margin-top: 1px; }
</style>
