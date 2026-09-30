<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <div class="mark">🏫</div>
        <span>智慧校园 · 后台管理</span>
      </div>
      <nav class="menu">
        <router-link v-for="m in menus" :key="m.path" :to="m.path" class="menu-item"
          :class="{ active: route.path === m.path }">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </router-link>
      </nav>
      <div style="padding:16px 22px; border-top:1px solid rgba(255,255,255,.08); font-size:12px; color:#8a96ad;">
        {{ roleText }}
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="page-title">{{ route.meta.title }}</div>
        <div class="right">
          <el-badge :value="unread" :hidden="unread === 0">
            <el-icon style="font-size:20px;cursor:pointer" @click="router.push('/notice-manage')"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="onCommand">
            <span class="flex gap-8" style="cursor:pointer; align-items:center;">
              <el-avatar :size="32" style="background:var(--brand-bg)">{{ avatarText }}</el-avatar>
              <span>{{ user.realName || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getMe, unreadCount } from '../api'

const route = useRoute()
const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const unread = ref(0)
const level = computed(() => user.value.accountLevel || 0)

const avatarText = computed(() => (user.value.realName || '管').slice(0, 1))
const roleText = computed(() => ({ 1: '超级管理员', 2: '学校管理员', 3: '后台操作员' }[level.value] || '后台'))

// 超级管理员菜单
const superMenus = [
  { path: '/global', title: '全局总览', icon: 'DataLine' },
  { path: '/school-manage', title: '学校管理', icon: 'OfficeBuilding' },
  { path: '/admin-apply-audit', title: '后台账号审核', icon: 'UserFilled' },
  { path: '/logs', title: '操作日志', icon: 'Document' }
]
// 学校管理员/操作员菜单
const schoolMenus = [
  { path: '/dashboard', title: '数据看板', icon: 'HomeFilled' },
  { path: '/school', title: '学校信息', icon: 'OfficeBuilding', minLevel: 2 },
  { path: '/users', title: '账号管理', icon: 'User', minLevel: 2 },
  { path: '/classes', title: '班级管理', icon: 'Collection', minLevel: 2 },
  { path: '/teacher-job', title: '教师任职', icon: 'Avatar', minLevel: 2 },
  { path: '/timetable', title: '课表设置', icon: 'Calendar', minLevel: 2 },
  { path: '/students', title: '学生总览', icon: 'School' },
  { path: '/teacher-apply', title: '任课更换审核', icon: 'Switch', minLevel: 2 },
  { path: '/questions', title: '题库管理', icon: 'Files' },
  { path: '/exam-stats', title: '考试统计', icon: 'TrendCharts' },
  { path: '/notice-manage', title: '通知管理', icon: 'Bell' },
  { path: '/logs', title: '操作日志', icon: 'Document' },
  { path: '/admin-apply', title: '申请后台账号', icon: 'Plus', minLevel: 2 },
  { path: '/system', title: '系统参数', icon: 'Setting', minLevel: 2 }
]

const menus = computed(() => {
  if (level.value === 1) {
    return [...superMenus, { path: '/profile', title: '个人信息', icon: 'UserFilled' }]
  }
  return [...schoolMenus.filter(m => !m.minLevel || m.minLevel <= level.value),
    { path: '/profile', title: '个人信息', icon: 'UserFilled' }]
})

function onCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    })
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}

async function loadMe() {
  try {
    const me = await getMe()
    user.value = me
    localStorage.setItem('user', JSON.stringify(me))
  } catch (e) { /* ignore */ }
}

onMounted(async () => {
  await loadMe()
  try { unread.value = await unreadCount() } catch (e) { /* ignore */ }
})
</script>
