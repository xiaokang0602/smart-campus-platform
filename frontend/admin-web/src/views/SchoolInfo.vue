<template>
  <div class="card" style="max-width:680px;">
    <div class="card-title">学校信息</div>
    <el-form label-width="90px">
      <el-form-item label="学校名称">
        <el-input v-model="form.schoolName" placeholder="学校名称" />
      </el-form-item>
      <el-form-item label="学校地址">
        <el-input v-model="form.address" placeholder="学校地址" />
      </el-form-item>
      <el-form-item label="学校ID">
        <el-input :model-value="info.id ?? '-'" disabled />
      </el-form-item>
      <el-button type="primary" @click="submit">保存</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { schoolInfo, schoolUpdate } from '../api'

const info = ref({})
const form = reactive({ schoolName: '', address: '' })

async function submit() {
  await schoolUpdate({ schoolName: form.schoolName, address: form.address })
  ElMessage.success('保存成功')
}

onMounted(async () => {
  info.value = await schoolInfo() || {}
  form.schoolName = info.value.schoolName || ''
  form.address = info.value.address || ''
})
</script>
