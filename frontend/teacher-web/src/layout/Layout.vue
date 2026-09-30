<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <div class="mark">🎓</div>
        <span>智慧校园 · 教师端</span>
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
          <el-tag v-if="user.isHeadTeacher" type="warning" effect="dark" size="small">班主任</el-tag>
          <el-badge :value="unread" :hidden="unread === 0">
            <el-icon style="font-size:20px;cursor:pointer" @click="router.push('/notice')"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="onCommand">
            <span class="flex gap-8" style="cursor:pointer; align-items:center;">
              <el-avatar :size="32" style="background:var(--brand-bg)">{{ avatarText }}</el-avatar>
              <span>{{ user.realName || '老师' }}</span>
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

const avatarText = computed(() => (user.value.realName || '师').slice(0, 1))

const baseMenus = [
  { path: '/dashboard', title: '首页', icon: 'HomeFilled' },
  { path: '/students', title: '班级学生', icon: 'User' },
  { path: '/reps', title: '课代表管理', icon: 'Avatar' },
  { path: '/questionbank', title: '校本题库', icon: 'Collection' },
  { path: '/papercreate', title: '组卷发布', icon: 'DocumentAdd' },
  { path: '/exammanage', title: '考试管理', icon: 'Monitor' },
  { path: '/scores', title: '成绩查看', icon: 'TrendCharts' },
  { path: '/archive', title: '学生档案', icon: 'Folder' },
  { path: '/notice', title: '通知中心', icon: 'Bell' },
  { path: '/publish', title: '发布通知', icon: 'Promotion' }
]
const headMenus = [
  { path: '/awards', title: '评奖评优', icon: 'Medal' },
  { path: '/teacherchange', title: '任课更换申请', icon: 'Switch' }
]
const menus = computed(() => user.value.isHeadTeacher ? [...baseMenus, ...headMenus] : baseMenus)

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

onMounted(async () => {
  try {
    const me = await getMe()
    user.value = me
    localStorage.setItem('user', JSON.stringify(me))
  } catch (e) { /* ignore */ }
  try { unread.value = await unreadCount() } catch (e) { /* ignore */ }
})
</script>
