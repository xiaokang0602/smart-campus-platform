package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.BusinessException;
import com.school.common.UserContext;
import com.school.entity.*;
import com.school.mapper.*;
import com.school.util.JwtUtil;
import com.school.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录鉴权与用户信息
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherJobMapper teacherJobMapper;
    private final ClassInfoMapper classInfoMapper;
    private final SecurityQuestionMapper securityQuestionMapper;
    private final FriendGuardianMapper friendGuardianMapper;
    private final MessageMapper messageMapper;
    private final MessageReceiverMapper messageReceiverMapper;
    private final CacheService cacheService;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getAccountLevel(),
                user.getSchoolId(), user.getUsername());
        cacheService.set("token:" + user.getId(), token, 24 * 3600L);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", buildProfile(user));
        return result;
    }

    public Map<String, Object> profile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return buildProfile(user);
    }

    /** 构建用户完整画像（含角色扩展信息） */
    public Map<String, Object> buildProfile(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("role", user.getRole());
        map.put("accountLevel", user.getAccountLevel());
        map.put("schoolId", user.getSchoolId());
        map.put("phone", user.getPhone());
        map.put("firstLogin", user.getFirstLogin());

        // 学生：班级、学号、任职
        if (user.getRole() == 1) {
            Student s = studentMapper.selectById(user.getId());
            if (s != null) {
                map.put("classId", s.getClassId());
                map.put("studentNo", s.getStudentNo());
                map.put("duty", s.getDuty());
                map.put("gender", s.getGender());
                map.put("isLeader", !"无".equals(s.getDuty()));
                ClassInfo c = classInfoMapper.selectById(s.getClassId());
                if (c != null) {
                    map.put("className", c.getClassName());
                }
            }
        }

        // 教师/班主任：任教班级与科目
        if (user.getRole() == 2 || user.getRole() == 3) {
            List<TeacherJob> jobs = teacherJobMapper.selectList(
                    new LambdaQueryWrapper<TeacherJob>().eq(TeacherJob::getTeacherId, user.getId()));
            List<Map<String, Object>> classes = jobs.stream().map(j -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("classId", j.getClassId());
                m.put("subject", j.getSubject());
                m.put("isHeadTeacher", j.getIsHeadTeacher());
                ClassInfo c = classInfoMapper.selectById(j.getClassId());
                m.put("className", c == null ? "" : c.getClassName());
                return m;
            }).collect(Collectors.toList());
            map.put("classes", classes);
            map.put("isHeadTeacher", user.getRole() == 3);
        }

        // 未读数
        map.put("unread", messageReceiverMapper.selectCount(
                new LambdaQueryWrapper<MessageReceiver>()
                        .eq(MessageReceiver::getReceiverId, user.getId())
                        .eq(MessageReceiver::getIsRead, 0)));
        return map;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null || !PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPassword(PasswordUtil.encode(newPassword));
        user.setFirstLogin(0);
        userMapper.updateById(user);
        cacheService.delete("token:" + userId);
    }

    /** 首次登录：设置新密码 + 密保问题 + 双好友担保 */
    public void firstLogin(Long userId, String newPassword, List<String> questions,
                           List<String> answers, List<Long> guardianIds) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(PasswordUtil.encode(newPassword));
        user.setFirstLogin(0);
        userMapper.updateById(user);

        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                SecurityQuestion q = new SecurityQuestion();
                q.setUserId(userId);
                q.setQuestion(questions.get(i));
                q.setAnswerHash(PasswordUtil.encode(answers.get(i)));
                securityQuestionMapper.insert(q);
            }
        }
        if (guardianIds != null) {
            for (int i = 0; i < guardianIds.size(); i++) {
                FriendGuardian g = new FriendGuardian();
                g.setUserId(userId);
                g.setGuardianUserId(guardianIds.get(i));
                g.setIsPrimary(i == 0 ? 1 : 0);
                friendGuardianMapper.insert(g);
            }
        }
    }

    /** 找回密码第一步：返回该账号的密保问题 */
    public List<String> recoverQuestions(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        List<SecurityQuestion> list = securityQuestionMapper.selectList(
                new LambdaQueryWrapper<SecurityQuestion>().eq(SecurityQuestion::getUserId, user.getId()));
        if (list.isEmpty()) {
            throw new BusinessException("该账号尚未设置密保，请联系管理员重置");
        }
        return list.stream().map(SecurityQuestion::getQuestion).collect(Collectors.toList());
    }

    /** 找回密码第二步：校验密保答案，生成 6 位临时密码并通知两位担保人 */
    public void recoverVerifyAnswers(String username, List<String> answers) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        List<SecurityQuestion> list = securityQuestionMapper.selectList(
                new LambdaQueryWrapper<SecurityQuestion>().eq(SecurityQuestion::getUserId, user.getId()));
        if (list.size() < 2) {
            throw new BusinessException("密保信息不足");
        }
        // 校验答案（回答顺序按问题顺序）
        for (int i = 0; i < list.size() && i < answers.size(); i++) {
            if (!PasswordUtil.matches(answers.get(i), list.get(i).getAnswerHash())) {
                throw new BusinessException("第 " + (i + 1) + " 道密保答案不正确");
            }
        }
        String code = PasswordUtil.randomTempPassword();
        cacheService.set("recover:" + username, code, 30 * 60L);
        cacheService.set("recover:done:" + username, 0, 30 * 60L);

        // 通知两位担保人
        List<FriendGuardian> guardians = friendGuardianMapper.selectList(
                new LambdaQueryWrapper<FriendGuardian>().eq(FriendGuardian::getUserId, user.getId()));
        for (FriendGuardian g : guardians) {
            sendTempCode(g.getGuardianUserId(), user.getRealName(), code);
        }
    }

    /** 找回密码第三步：担保人确认，两人都确认后重置密码 */
    public boolean recoverVerifyGuardian(String username, String code, Long guardianUserId) {
        String cached = (String) cacheService.get("recover:" + username);
        if (cached == null || !cached.equals(code)) {
            throw new BusinessException("验证码已失效或错误");
        }
        Integer done = (Integer) cacheService.get("recover:done:" + username);
        done = (done == null ? 0 : done) + 1;
        cacheService.set("recover:done:" + username, done, 30 * 60L);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (done >= 2) {
            user.setPassword(PasswordUtil.encode(code));
            user.setFirstLogin(1);
            userMapper.updateById(user);
            cacheService.delete("recover:" + username);
            cacheService.delete("recover:done:" + username);
            return true;
        }
        return false;
    }

    private void sendTempCode(Long receiverId, String ownerName, String code) {
        Message msg = new Message();
        msg.setSchoolId(0L);
        msg.setTitle("账号找回验证码");
        msg.setContent("你的同学「" + ownerName + "」正在找回密码，验证码为：" + code + "（30 分钟内有效）。请前往对方处或按约定方式确认。");
        msg.setNoticeType(5);
        msg.setPublisherId(0L);
        msg.setPublisherRole(4);
        msg.setTargetScope(3);
        msg.setTargetUserIds("[" + receiverId + "]");
        messageMapper.insert(msg);

        MessageReceiver mr = new MessageReceiver();
        mr.setMessageId(msg.getId());
        mr.setReceiverId(receiverId);
        mr.setIsRead(0);
        messageReceiverMapper.insert(mr);
    }
}
