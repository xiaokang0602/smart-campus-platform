<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <div class="mark">🎓</div>
        <span>智慧校园 · 学生端</span>
      </div>
      <nav class="menu">
        <router-link v-for="m in menus" :key="m.path" :to="m.path" class="menu-item"
          :class="{ active: route.path === m.path }">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </router-link>
      </nav>
      <div style="padding:16px 22px; border-top:1px solid rgba(255,255,255,.08); font-size:12px; color:#8a96ad;">
        青蓝初级中学
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="page-title">{{ route.meta.title }}</div>
        <div class="right">
          <el-badge :value="unread" :hidden="unread === 0">
            <el-icon style="font-size:20px;cursor:pointer" @click="router.push('/notice')"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="onCommand">
            <span class="flex gap-8" style="cursor:pointer; align-items:center;">
              <el-avatar :size="32" style="background:var(--brand-bg)">{{ avatarText }}</el-avatar>
              <span>{{ user.realName || '同学' }}</span>
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

const avatarText = computed(() => (user.value.realName || '学').slice(0, 1))

const allMenus = [
  { path: '/dashboard', title: '首页', icon: 'HomeFilled' },
  { path: '/timetable', title: '我的课表', icon: 'Calendar' },
  { path: '/exam', title: '我的考试', icon: 'EditPen' },
  { path: '/scores', title: '我的成绩', icon: 'TrendCharts' },
  { path: '/ai', title: 'AI 学情分析', icon: 'MagicStick' },
  { path: '/notice', title: '通知中心', icon: 'Bell' }
]
const menus = computed(() => {
  const list = [...allMenus]
  if (user.value.isLeader) {
    list.push({ path: '/publish', title: '发布通知', icon: 'Promotion' })
  }
  return list
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
