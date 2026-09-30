import request from './request'

// 鉴权
export const login = (data) => request.post('/auth/login', data)
export const getMe = () => request.get('/auth/me')
export const changePassword = (data) => request.post('/auth/changePassword', data)

// 看板
export const dashboard = () => request.get('/dashboard/teacher')

// 学生
export const studentList = (params) => request.get('/student/list', { params })
export const studentDetail = (id) => request.get(`/student/${id}`)
export const studentUpdate = (id, data) => request.put(`/student/${id}`, data)
export const addAward = (id, data) => request.post(`/student/${id}/award`, data)
export const deleteAward = (awardId) => request.delete(`/student/award/${awardId}`)

// 课代表
export const myReps = () => request.get('/classrep/mine')
export const applyRep = (data) => request.post('/classrep/apply', data)
export const pendingReps = () => request.get('/classrep/pending')
export const auditRep = (id, data) => request.post(`/classrep/${id}/audit`, data)

// 题库
export const questionList = (params) => request.get('/question/list', { params })
export const questionAdd = (data) => request.post('/question/add', data)
export const questionUpdate = (data) => request.put('/question/update', data)
export const questionDelete = (id) => request.delete(`/question/${id}`)
export const aiGenerate = (data) => request.post('/question/ai-generate', data)
export const adoptQuestions = (ids) => request.post('/question/adopt', { ids })
export const discardQuestions = (ids) => request.post('/question/discard', { ids })

// 试卷
export const paperList = (params) => request.get('/paper/list', { params })
export const paperDetail = (id) => request.get(`/paper/${id}`)
export const paperCreate = (data) => request.post('/paper/create', data)
export const paperPublish = (id) => request.post(`/paper/${id}/publish`)
export const paperEnd = (id) => request.post(`/paper/${id}/end`)
export const paperDelete = (id) => request.delete(`/paper/${id}`)

// 考试/成绩
export const paperScores = (paperId) => request.get(`/score/paper/${paperId}`)
export const examReopen = (recordId) => request.post(`/exam/reopen/${recordId}`)
export const studentPortrait = (studentId) => request.get(`/score/student/${studentId}/portrait`)
export const aiPortrait = (studentId, subject) => request.post(`/score/student/${studentId}/ai`, { subject })

// 任课更换
export const changeApply = (data) => request.post('/teacherchange/apply', data)
export const changeMine = () => request.get('/teacherchange/mine')

// 班级
export const classList = () => request.get('/class/list')

// 用户（教师列表）
export const userList = (params) => request.get('/user/list', { params })

// 通知
export const myMessages = (noticeType) => request.get('/message/mine', { params: { noticeType } })
export const unreadCount = () => request.get('/message/unread-count')
export const readMessage = (receiverId) => request.post(`/message/${receiverId}/read`)
export const publishMessage = (data) => request.post('/message/publish', data)
export const sentMessages = () => request.get('/message/sent')
