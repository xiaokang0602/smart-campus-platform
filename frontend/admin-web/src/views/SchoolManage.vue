<template>
  <div>
    <div class="card">
      <div class="flex-between">
        <div class="card-title" style="margin-bottom:0">学校管理（超级管理员）</div>
        <el-button type="primary" @click="dialog = true">新增学校</el-button>
      </div>
    </div>

    <div class="card">
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="schoolName" label="学校名称" min-width="180" />
        <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="studentCount" label="学生数" width="100" />
        <el-table-column prop="teacherCount" label="教师数" width="100" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="disable(row)">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" title="新增学校" width="460px">
      <el-form label-width="110px">
        <el-form-item label="学校名称"><el-input v-model="form.schoolName" /></el-form-item>
        <el-form-item label="学校地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="管理员账号"><el-input v-model="form.adminUsername" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="管理员姓名"><el-input v-model="form.adminRealName" placeholder="校管理员" /></el-form-item>
        <el-form-item label="初始密码"><el-input v-model="form.adminPassword" placeholder="默认 123456" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { schoolList, schoolCreate, schoolDisable } from '../api'

const list = ref([])
const dialog = ref(false)
const form = reactive({ schoolName: '', address: '', adminUsername: '', adminRealName: '', adminPassword: '' })

async function load() { list.value = await schoolList() }

async function submit() {
  if (!form.schoolName) { ElMessage.warning('请输入学校名称'); return }
  await schoolCreate(form)
  ElMessage.success('创建成功，该校管理员已同步创建')
  dialog.value = false
  load()
}

async function disable(row) {
  await ElMessageBox.confirm(`确定停用「${row.schoolName}」吗？该校所有账号将被禁用。`, '提示', { type: 'warning' })
  await schoolDisable(row.id)
  ElMessage.success('已停用')
}

onMounted(load)
</script>
