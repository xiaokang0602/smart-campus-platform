import request from './request'

// 登录鉴权
export const login = (data) => request.post('/auth/login', data)
export const getMe = () => request.get('/auth/me')
export const changePassword = (data) => request.post('/auth/changePassword', data)
export const firstLogin = (data) => request.post('/auth/firstLogin', data)
export const recoverQuestions = (data) => request.post('/auth/recover/questions', data)
export const recoverAnswers = (data) => request.post('/auth/recover/answers', data)
export const recoverVerify = (data) => request.post('/auth/recover/verify', data)

// 看板
export const dashboard = () => request.get('/dashboard/student')

// 课表
export const myTimetable = () => request.get('/timetable/mine')

// 考试
export const studentPapers = () => request.get('/paper/student/list')
export const examStart = (paperId) => request.post(`/exam/start/${paperId}`)
export const examDraft = (data) => request.post('/exam/draft', data)
export const examSubmit = (data) => request.post('/exam/submit', data)
export const examCheat = (data) => request.post('/exam/cheat', data)
export const examHistory = (paperId) => request.get(`/exam/history/${paperId}`)

// 成绩
export const myScores = () => request.get('/score/student/mine')
export const scoreAnalysis = (recordId) => request.get(`/score/analysis/${recordId}`)

// 通知
export const myMessages = (noticeType) => request.get('/message/mine', { params: { noticeType } })
export const unreadCount = () => request.get('/message/unread-count')
export const readMessage = (receiverId) => request.post(`/message/${receiverId}/read`)
export const publishMessage = (data) => request.post('/message/publish', data)

// 同学列表（班委发布通知用）
export const classStudents = (classId) => request.get('/student/list', { params: { classId, pageSize: 200 } })
