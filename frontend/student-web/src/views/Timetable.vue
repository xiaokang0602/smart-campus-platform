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
          <template v-for="(row, idx) in schedule" :key="idx">
            <tr v-if="row.type === 'period'">
              <td class="tt-period">
                <div class="p-label">{{ row.label }}</div>
                <div class="p-time">{{ row.time }}</div>
              </td>
              <td v-for="d in days" :key="d">
                <div v-if="cell(d, row.period)" class="lesson" :style="{ background: subjectColor(cell(d, row.period).subject) }">
                  <div class="ls-sub">{{ cell(d, row.period).subject }}</div>
                  <div class="ls-t">{{ cell(d, row.period).teacherName }}</div>
                  <div class="ls-r">{{ cell(d, row.period).room }}</div>
                </div>
              </td>
            </tr>
            <tr v-else class="break-row">
              <td class="tt-period break-label">{{ row.label }}</td>
              <td class="break-cell" :colspan="days.length">{{ row.time }}</td>
            </tr>
          </template>
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

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
function dayName(d) { return dayNames[d] || `周${d}` }

// 作息：上午 4 节 + 下午 3 节，每节 45 分钟，课间 10 分钟，第 2 节后大课间 30 分钟，第 4 节后午休
const CLASS_MIN = 45
const BREAK_MIN = 10
const BIG_BREAK_MIN = 30
const LUNCH_MIN = 130
const DAY_START = 8 * 60

function toHHMM(min) {
  const h = Math.floor(min / 60)
  const m = min % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

const schedule = (() => {
  const rows = []
  let cursor = DAY_START
  for (let p = 1; p <= 7; p++) {
    rows.push({ type: 'period', period: p, label: `第${p}节`, time: `${toHHMM(cursor)}-${toHHMM(cursor + CLASS_MIN)}` })
    cursor += CLASS_MIN
    if (p === 2) {
      rows.push({ type: 'break', label: '大课间', time: `${toHHMM(cursor)}-${toHHMM(cursor + BIG_BREAK_MIN)}` })
      cursor += BIG_BREAK_MIN
    } else if (p === 4) {
      rows.push({ type: 'break', label: '午休', time: `${toHHMM(cursor)}-${toHHMM(cursor + LUNCH_MIN)}` })
      cursor += LUNCH_MIN
    } else if (p < 7) {
      cursor += BREAK_MIN
    }
  }
  return rows
})()

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
.corner { width: 110px; }
.tt-period { width: 110px; background: #f8fafc; color: #45536a; }
.p-label { font-size: 13px; font-weight: 600; }
.p-time { font-size: 11px; color: #8a96ad; margin-top: 2px; }
.tt-table td { height: 64px; padding: 4px; }
.lesson {
  height: 56px; border-radius: 8px; color: #fff; padding: 6px 8px;
  display: flex; flex-direction: column; justify-content: center; align-items: center;
  box-shadow: 0 3px 8px rgba(0, 0, 0, .12);
}
.ls-sub { font-weight: 700; font-size: 14px; line-height: 1.2; }
.ls-t { font-size: 11px; opacity: .9; margin-top: 2px; }
.ls-r { font-size: 10px; opacity: .75; margin-top: 1px; }
.break-row .tt-period { background: #eef3fb; }
.break-label { font-size: 12px; font-weight: 600; color: #6b7a94; }
.break-cell { height: 40px; background: #f7f9fd; color: #9aa7bb; font-size: 12px; }
</style>
