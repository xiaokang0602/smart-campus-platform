<template>
  <div>
    <div class="card">
      <div class="card-title">申请新增后台账号（提交后由超级管理员审核）</div>
      <el-form label-width="110px" style="max-width:520px">
        <el-form-item label="新账号"><el-input v-model="form.newUsername" placeholder="登录账号" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.newRealName" /></el-form-item>
        <el-form-item label="菜单权限">
          <el-checkbox-group v-model="perms">
            <el-checkbox value="user">账号管理</el-checkbox>
            <el-checkbox value="class">班级管理</el-checkbox>
            <el-checkbox value="question">题库管理</el-checkbox>
            <el-checkbox value="exam">考试统计</el-checkbox>
            <el-checkbox value="notice">通知管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="申请理由"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
        <el-button type="primary" @click="submit">提交申请</el-button>
      </el-form>
    </div>

    <div class="card">
      <div class="card-title">我的申请记录</div>
      <el-table :data="list" stripe>
        <el-table-column prop="newUsername" label="申请账号" width="150" />
        <el-table-column prop="newRealName" label="姓名" width="120" />
        <el-table-column prop="menuPerms" label="权限" min-width="180" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="['warning', 'success', 'danger'][row.auditStatus]">{{ ['待审核', '已通过', '已驳回'][row.auditStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditComment" label="审核意见" width="140" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApply, adminApplyMine } from '../api'

const list = ref([])
const perms = ref([])
const form = reactive({ newUsername: '', newRealName: '', reason: '' })

async function submit() {
  if (!form.newUsername || !form.newRealName) { ElMessage.warning('请填写账号和姓名'); return }
  await adminApply({ newUsername: form.newUsername, newRealName: form.newRealName, menuPerms: perms.value.join(','), reason: form.reason })
  ElMessage.success('申请已提交')
  form.newUsername = form.newRealName = form.reason = ''
  perms.value = []
  load()
}

async function load() { list.value = await adminApplyMine() }

onMounted(load)
</script>
