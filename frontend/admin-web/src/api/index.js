import request from './request'

// 鉴权
export const login = (data) => request.post('/auth/login', data)
export const getMe = () => request.get('/auth/me')
export const changePassword = (data) => request.post('/auth/changePassword', data)

// 看板
export const dashboard = () => request.get('/dashboard/admin')
export const globalOverview = () => request.get('/dashboard/admin/global')

// 学校
export const schoolInfo = () => request.get('/school/info')
export const schoolUpdate = (data) => request.put('/school/info', data)
export const schoolList = () => request.get('/school/list')
export const schoolCreate = (data) => request.post('/school/create', data)
export const schoolDisable = (id) => request.put(`/school/${id}/disable`)

// 用户/账号
export const userList = (params) => request.get('/user/list', { params })
export const userAdd = (data) => request.post('/user/add', data)
export const userUpdate = (id, data) => request.put(`/user/${id}`, data)
export const userStatus = (id, data) => request.put(`/user/${id}/status`, data)
export const userResetPwd = (id, data) => request.put(`/user/${id}/resetPassword`, data)
export const userDelete = (id) => request.delete(`/user/${id}`)

// 班级
export const classList = () => request.get('/class/list')
export const classAdd = (data) => request.post('/class/add', data)
export const classUpdate = (id, data) => request.put(`/class/${id}`, data)
export const classDelete = (id) => request.delete(`/class/${id}`)

// 教师任职
export const teacherJobList = () => request.get('/teacherjob/list')
export const teacherJobSave = (data) => request.post('/teacherjob/save', data)
export const teacherJobUpdate = (id, data) => request.put(`/teacherjob/${id}`, data)
export const teacherJobDelete = (id) => request.delete(`/teacherjob/${id}`)

// 学生
export const studentList = (params) => request.get('/student/list', { params })
export const studentDetail = (id) => request.get(`/student/${id}`)
export const studentUpdate = (id, data) => request.put(`/student/${id}`, data)
export const addAward = (id, data) => request.post(`/student/${id}/award`, data)
export const deleteAward = (awardId) => request.delete(`/student/award/${awardId}`)

// 任课更换审核
export const changePending = () => request.get('/teacherchange/pending')
export const changeAudit = (id, data) => request.post(`/teacherchange/${id}/audit`, data)

// 题库
export const questionList = (params) => request.get('/question/list', { params })
export const questionUpdate = (data) => request.put('/question/update', data)
export const questionDelete = (id) => request.delete(`/question/${id}`)

// 试卷/考试/成绩
export const paperList = (params) => request.get('/paper/list', { params })
export const paperScores = (paperId) => request.get(`/score/paper/${paperId}`)
export const scoreExport = (paperId) => `/api/score/export${paperId ? `?paperId=${paperId}` : ''}`

// 通知
export const myMessages = (noticeType) => request.get('/message/mine', { params: { noticeType } })
export const unreadCount = () => request.get('/message/unread-count')
export const readMessage = (receiverId) => request.post(`/message/${receiverId}/read`)
export const publishMessage = (data) => request.post('/message/publish', data)
export const sentMessages = () => request.get('/message/sent')
export const messageReaders = (id) => request.get(`/message/${id}/readers`)
export const messageWithdraw = (id) => request.delete(`/message/${id}`)
export const messageTop = (id) => request.post(`/message/${id}/top`)

// 后台账号申请
export const adminApply = (data) => request.post('/adminapply/apply', data)
export const adminApplyMine = () => request.get('/adminapply/mine')
export const adminApplyPending = () => request.get('/adminapply/pending')
export const adminApplyAudit = (id, data) => request.post(`/adminapply/${id}/audit`, data)

// 操作日志
export const logList = (params) => request.get('/log/list', { params })
