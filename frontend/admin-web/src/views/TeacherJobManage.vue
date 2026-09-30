<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">教师任职管理</div>
        <el-button type="primary" @click="openAdd">新增任职</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="teacherName" label="教师" width="140" />
        <el-table-column prop="className" label="班级" width="160" />
        <el-table-column prop="subject" label="科目" width="120" />
        <el-table-column label="是否班主任" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isHeadTeacher === 1 ? 'warning' : 'info'" size="small">{{ row.isHeadTeacher === 1 ? '班主任' : '任课' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑任职' : '新增任职'" width="440px">
      <el-form label-width="90px">
        <el-form-item label="教师">
          <el-select v-model="form.teacherId" :disabled="editing" style="width:100%">
            <el-option v-for="t in teachers" :key="t.id" :value="t.id" :label="t.realName" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.classId" style="width:100%">
            <el-option v-for="c in classes" :key="c.id" :value="c.id" :label="c.className" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="form.subject" style="width:100%">
            <el-option v-for="s in subjects" :key="s" :value="s" :label="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否班主任">
          <el-switch v-model="form.isHeadTeacher" :active-value="1" :inactive-value="0" />
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
import { teacherJobList, teacherJobSave, teacherJobUpdate, teacherJobDelete, userList, classList } from '../api'

const subjects = ['语文', '数学', '英语', '道德与法治', '历史', '地理', '物理', '化学', '生物']
const list = ref([])
const teachers = ref([])
const classes = ref([])
const dialog = ref(false)
const editing = ref(false)
const form = reactive({ id: null, teacherId: null, classId: null, subject: '', isHeadTeacher: 0 })

async function load() { list.value = await teacherJobList() }

function openAdd() {
  editing.value = false
  Object.assign(form, { id: null, teacherId: null, classId: null, subject: '', isHeadTeacher: 0 })
  dialog.value = true
}

function openEdit(row) {
  editing.value = true
  Object.assign(form, { id: row.id, teacherId: row.teacherId, classId: row.classId, subject: row.subject, isHeadTeacher: row.isHeadTeacher })
  dialog.value = true
}

async function submit() {
  if (!form.teacherId || !form.classId || !form.subject) { ElMessage.warning('请填写完整信息'); return }
  if (editing.value) {
    await teacherJobUpdate(form.id, { classId: form.classId, subject: form.subject, isHeadTeacher: form.isHeadTeacher })
  } else {
    await teacherJobSave({ teacherId: form.teacherId, classId: form.classId, subject: form.subject, isHeadTeacher: form.isHeadTeacher })
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除「${row.teacherName}」的任职吗？`, '提示', { type: 'warning' })
  await teacherJobDelete(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  load()
  const res = await userList({ pageSize: 200 })
  teachers.value = (res.records || []).filter(u => u.role === 2 || u.role === 3)
  classes.value = await classList()
})
</script>
