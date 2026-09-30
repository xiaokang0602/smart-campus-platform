<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">班级管理</div>
        <el-button type="primary" @click="openAdd">新增班级</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="className" label="班级名称" min-width="140" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column prop="headTeacherName" label="班主任" width="140" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑班级' : '新增班级'" width="440px">
      <el-form label-width="90px">
        <el-form-item label="班级名称"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="年级"><el-input v-model="form.grade" placeholder="如：初二" /></el-form-item>
        <el-form-item label="班主任">
          <el-select v-model="form.headTeacherId" clearable style="width:100%">
            <el-option v-for="t in teachers" :key="t.id" :value="t.id" :label="t.realName" />
          </el-select>
        </el-form-item>
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
import { classList, classAdd, classUpdate, classDelete, userList } from '../api'

const list = ref([])
const teachers = ref([])
const dialog = ref(false)
const editing = ref(false)
const form = reactive({ id: null, className: '', grade: '', headTeacherId: null })

async function load() { list.value = await classList() }

function openAdd() {
  editing.value = false
  Object.assign(form, { id: null, className: '', grade: '', headTeacherId: null })
  dialog.value = true
}

function openEdit(row) {
  editing.value = true
  Object.assign(form, { id: row.id, className: row.className, grade: row.grade, headTeacherId: row.headTeacherId || null })
  dialog.value = true
}

async function submit() {
  if (!form.className) { ElMessage.warning('请输入班级名称'); return }
  if (editing.value) {
    await classUpdate(form.id, { className: form.className, grade: form.grade, headTeacherId: form.headTeacherId })
  } else {
    await classAdd({ className: form.className, grade: form.grade, headTeacherId: form.headTeacherId })
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除班级「${row.className}」吗？`, '提示', { type: 'warning' })
  await classDelete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  load()
  const res = await userList({ pageSize: 200, role: 3 })
  teachers.value = res.records || []
})
</script>
