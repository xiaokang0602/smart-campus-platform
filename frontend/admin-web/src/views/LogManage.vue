<template>
  <div class="card">
    <div class="flex gap-8" style="margin-bottom:16px;">
      <el-input v-model="q.module" placeholder="模块" clearable style="width:150px" @keyup.enter="load(1)" />
      <el-select v-model="q.action" placeholder="操作类型" clearable style="width:150px" @change="load(1)">
        <el-option value="LOGIN" label="登录" />
        <el-option value="LOGOUT" label="登出" />
        <el-option value="CREATE" label="新增" />
        <el-option value="UPDATE" label="更新" />
        <el-option value="DELETE" label="删除" />
        <el-option value="APPROVE" label="审批" />
        <el-option value="EXPORT" label="导出" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>

    <el-table :data="rows" stripe>
      <el-table-column prop="username" label="操作人" width="130" />
      <el-table-column prop="module" label="模块" width="150" />
      <el-table-column prop="action" label="操作" width="110" />
      <el-table-column prop="targetType" label="对象类型" width="150" />
      <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="createTime" label="时间" width="170" />
    </el-table>
    <el-pagination class="mt-16" layout="total, prev, pager, next" :total="total" :page-size="q.pageSize"
      :current-page="q.pageNum" @current-change="load" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { logList } from '../api'

const rows = ref([])
const total = ref(0)
const q = reactive({ pageNum: 1, pageSize: 20, module: '', action: '' })

async function load(p) {
  if (p) q.pageNum = p
  const res = await logList({ pageNum: q.pageNum, pageSize: q.pageSize, module: q.module || undefined, action: q.action || undefined })
  rows.value = res.records
  total.value = res.total
}

onMounted(() => load())
</script>
