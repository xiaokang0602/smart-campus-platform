<template>
  <div class="card">
    <div class="flex-between">
      <div class="card-title" style="margin-bottom:0">我的课表</div>
      <el-radio-group v-model="week" size="small">
        <el-radio-button v-for="w in 7" :key="w" :value="w">{{ ['一','二','三','四','五','六','日'][w-1] }}</el-radio-button>
      </el-radio-group>
    </div>

    <div class="timetable" style="margin-top:16px">
      <div class="tt-header">
        <div class="tt-cell">节次</div>
        <div class="tt-cell" v-for="p in 8" :key="p">第{{ p }}节</div>
      </div>
      <div class="tt-row" v-for="p in 8" :key="p">
        <div class="tt-cell tt-period">第{{ p }}节</div>
        <div class="tt-cell" v-for="q in 8" :key="q">
          <template v-if="q === 1 && cell(week, p)">
            <div class="lesson" :style="{ background: subjectColor(cell(week, p).subject) }">
              <div class="ls-sub">{{ cell(week, p).subject }}</div>
              <div class="ls-t">{{ cell(week, p).teacherName }}</div>
              <div class="ls-r">{{ cell(week, p).room }}</div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myTimetable } from '../api'

const week = ref(1)
const list = ref([])

const colors = {
  '语文': '#ef4444', '数学': '#2f6bff', '英语': '#8b5cf6',
  '物理': '#0ea5e9', '化学': '#f59e0b', '生物': '#12b981',
  '历史': '#d97706', '地理': '#14b8a6', '道德与法治': '#f43f5e',
  '体育': '#6b7280', '音乐': '#ec4899', '美术': '#f97316'
}

function subjectColor(s) { return colors[s] || '#2f6bff' }
function cell(w, p) { return list.value.find(t => t.week === w && t.period === p) }

onMounted(async () => { list.value = await myTimetable() })
</script>

<style scoped>
.timetable { display: flex; flex-direction: column; gap: 8px; }
.tt-header, .tt-row { display: grid; grid-template-columns: 64px repeat(8, 1fr); gap: 8px; }
.tt-cell { min-height: 54px; display: flex; align-items: center; justify-content: center; }
.tt-header .tt-cell { background: #f0f4fb; border-radius: 8px; font-weight: 600; color: #45536a; min-height: 38px; }
.tt-period { font-size: 13px; color: #8a96ad; }
.lesson { width: 100%; height: 54px; border-radius: 10px; color: #fff; padding: 5px 8px; display: flex; flex-direction: column; justify-content: center; box-shadow: 0 4px 10px rgba(0,0,0,.12); }
.ls-sub { font-weight: 700; font-size: 14px; }
.ls-t { font-size: 11px; opacity: .9; }
.ls-r { font-size: 10px; opacity: .75; }
</style>
