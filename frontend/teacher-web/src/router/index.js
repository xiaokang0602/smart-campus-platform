import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'students', component: () => import('../views/StudentList.vue'), meta: { title: '班级学生' } },
      { path: 'reps', component: () => import('../views/RepManage.vue'), meta: { title: '课代表管理' } },
      { path: 'questionbank', component: () => import('../views/QuestionBank.vue'), meta: { title: '校本题库' } },
      { path: 'papercreate', component: () => import('../views/PaperCreate.vue'), meta: { title: '组卷发布' } },
      { path: 'exammanage', component: () => import('../views/ExamManage.vue'), meta: { title: '考试管理' } },
      { path: 'scores', component: () => import('../views/ExamScore.vue'), meta: { title: '成绩查看' } },
      { path: 'archive', component: () => import('../views/StudentArchive.vue'), meta: { title: '学生档案' } },
      { path: 'awards', component: () => import('../views/AwardManage.vue'), meta: { title: '评奖评优' } },
      { path: 'teacherchange', component: () => import('../views/TeacherChangeApply.vue'), meta: { title: '任课更换申请' } },
      { path: 'notice', component: () => import('../views/NoticeCenter.vue'), meta: { title: '通知中心' } },
      { path: 'publish', component: () => import('../views/PublishNotice.vue'), meta: { title: '发布通知' } },
      { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人信息' } }
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
