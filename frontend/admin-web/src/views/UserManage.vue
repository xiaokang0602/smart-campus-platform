<template>
  <div>
    <div class="card">
      <div class="flex-between" style="flex-wrap:wrap; gap:12px;">
        <div class="flex gap-8">
          <el-select v-model="q.role" placeholder="全部角色" clearable style="width:130px" @change="load(1)">
            <el-option :value="1" label="学生" />
            <el-option :value="2" label="任课教师" />
            <el-option :value="3" label="班主任" />
            <el-option :value="4" label="后台" />
          </el-select>
          <el-input v-model="q.keyword" placeholder="姓名/账号" clearable style="width:180px" @keyup.enter="load(1)" />
          <el-button type="primary" @click="load(1)">查询</el-button>
        </div>
        <el-button type="primary" @click="openAdd">新增账号</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="rows" stripe>
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="['info', 'success', 'warning', 'primary'][row.role - 1]">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="层级" width="120">
          <template #default="{ row }">{{ row.role === 4 ? (levelText[row.accountLevel] || '-') : '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" plain @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" plain @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="mt-16" layout="total, prev, pager, next" :total="total" :page-size="q.pageSize"
        :current-page="q.pageNum" @current-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑账号' : '新增账号'" width="520px">
      <el-form label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" :disabled="editing" /></el-form-item>
        <el-form-item v-if="!editing" label="密码"><el-input v-model="form.password" placeholder="默认 123456" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item v-if="!editing" label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option :value="1" label="学生" />
            <el-option :value="2" label="任课教师" />
            <el-option :value="3" label="班主任" />
            <el-option :value="4" label="后台" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editing && form.role === 4" label="后台层级">
          <el-select v-model="form.accountLevel" style="width:100%">
            <el-option :value="2" label="学校管理员" />
            <el-option :value="3" label="普通后台" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editing && form.role === 1" label="班级">
          <el-select v-model="form.classId" clearable style="width:100%">
            <el-option v-for="c in classes" :key="c.id" :value="c.id" :label="c.className" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editing && form.role === 1" label="学号"><el-input v-model="form.studentNo" /></el-form-item>
        <el-form-item v-if="!editing && form.role === 1" label="性别">
          <el-select v-model="form.gender" style="width:100%">
            <el-option value="男" label="男" /><el-option value="女" label="女" />
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
import { userList, userAdd, userUpdate, userStatus, userResetPwd, classList } from '../api'

const rows = ref([])
const total = ref(0)
const classes = ref([])
const q = reactive({ pageNum: 1, pageSize: 10, role: null, keyword: '' })
const dialog = ref(false)
const editing = ref(false)
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', role: 1, accountLevel: 3, classId: null, studentNo: '', gender: '男' })
const levelText = { 1: '超级管理员', 2: '学校管理员', 3: '普通后台' }

function roleText(r) { return ['', '学生', '任课教师', '班主任', '后台'][r] }

async function load(p) {
  if (p) q.pageNum = p
  const res = await userList({ pageNum: q.pageNum, pageSize: q.pageSize, role: q.role ?? undefined, keyword: q.keyword || undefined })
  rows.value = res.records
  total.value = res.total
}

function openAdd() {
  editing.value = false
  Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', role: 1, accountLevel: 3, classId: null, studentNo: '', gender: '男' })
  dialog.value = true
}

function openEdit(row) {
  editing.value = true
  Object.assign(form, { id: row.id, username: row.username, realName: row.realName, phone: row.phone })
  dialog.value = true
}

async function submit() {
  if (editing.value) {
    await userUpdate(form.id, { realName: form.realName, phone: form.phone })
  } else {
    if (!form.username) { ElMessage.warning('请输入账号'); return }
    await userAdd({ ...form, password: form.password || '123456' })
  }
  ElMessage.success('保存成功')
  dialog.value = false
  load()
}

async function toggleStatus(row) {
  await userStatus(row.id, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success('已更新')
  load()
}

async function resetPwd(row) {
  await ElMessageBox.confirm(`确定将「${row.realName}」密码重置为 123456 吗？`, '提示', { type: 'warning' })
  await userResetPwd(row.id, { password: '123456' })
  ElMessage.success('密码已重置')
}

onMounted(async () => {
  load()
  classes.value = await classList()
})
</script>
