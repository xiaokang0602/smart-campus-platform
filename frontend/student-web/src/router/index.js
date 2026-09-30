import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'timetable', component: () => import('../views/Timetable.vue'), meta: { title: '我的课表' } },
      { path: 'exam', component: () => import('../views/ExamList.vue'), meta: { title: '我的考试' } },
      { path: 'exam/:paperId', component: () => import('../views/ExamDo.vue'), meta: { title: '在线答题' } },
      { path: 'history/:paperId', component: () => import('../views/HistoryPaper.vue'), meta: { title: '历史试卷' } },
      { path: 'scores', component: () => import('../views/ScoreList.vue'), meta: { title: '我的成绩' } },
      { path: 'ai', component: () => import('../views/AiAnalysis.vue'), meta: { title: 'AI 学情分析' } },
      { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人信息' } },
      { path: 'notice', component: () => import('../views/NoticeCenter.vue'), meta: { title: '通知中心' } },
      { path: 'publish', component: () => import('../views/PublishNotice.vue'), meta: { title: '发布通知' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
