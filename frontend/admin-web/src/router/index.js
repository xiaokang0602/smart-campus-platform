import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'global', component: () => import('../views/GlobalOverview.vue'), meta: { title: '全局总览' } },
      { path: 'school', component: () => import('../views/SchoolInfo.vue'), meta: { title: '学校信息' } },
      { path: 'school-manage', component: () => import('../views/SchoolManage.vue'), meta: { title: '学校管理' } },
      { path: 'users', component: () => import('../views/UserManage.vue'), meta: { title: '账号管理' } },
      { path: 'classes', component: () => import('../views/ClassManage.vue'), meta: { title: '班级管理' } },
      { path: 'teacher-job', component: () => import('../views/TeacherJobManage.vue'), meta: { title: '教师任职' } },
      { path: 'timetable', component: () => import('../views/TimetableManage.vue'), meta: { title: '课表设置' } },
      { path: 'students', component: () => import('../views/StudentList.vue'), meta: { title: '学生总览' } },
      { path: 'teacher-apply', component: () => import('../views/TeacherApplyAudit.vue'), meta: { title: '任课更换审核' } },
      { path: 'questions', component: () => import('../views/QuestionManage.vue'), meta: { title: '题库管理' } },
      { path: 'exam-stats', component: () => import('../views/ExamStats.vue'), meta: { title: '考试统计' } },
      { path: 'notice-manage', component: () => import('../views/NoticeManage.vue'), meta: { title: '通知管理' } },
      { path: 'logs', component: () => import('../views/LogManage.vue'), meta: { title: '操作日志' } },
      { path: 'admin-apply', component: () => import('../views/AdminApply.vue'), meta: { title: '申请后台账号' } },
      { path: 'admin-apply-audit', component: () => import('../views/AdminApplyAudit.vue'), meta: { title: '后台账号审核' } },
      { path: 'system', component: () => import('../views/SystemParams.vue'), meta: { title: '系统参数' } },
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
