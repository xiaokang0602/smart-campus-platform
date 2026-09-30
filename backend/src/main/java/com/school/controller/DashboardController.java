package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.Result;
import com.school.common.UserContext;
import com.school.entity.*;
import com.school.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final TeacherJobMapper teacherJobMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamRecordMapper examRecordMapper;
    private final QuestionBankMapper questionBankMapper;
    private final AiScoreAnalysisMapper aiScoreAnalysisMapper;
    private final ClassRepMapper classRepMapper;
    private final SchoolMapper schoolMapper;

    @GetMapping("/student")
    public Result<Map<String, Object>> student() {
        Student s = studentMapper.selectById(UserContext.userId());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("classId", s == null ? null : s.getClassId());
        m.put("duty", s == null ? "无" : s.getDuty());
        if (s != null && s.getClassId() != null) {
            ClassInfo c = classInfoMapper.selectById(s.getClassId());
            m.put("className", c == null ? "" : c.getClassName());
        }
        m.put("examCount", examPaperMapper.selectCount(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getStatus, 1)));
        m.put("finishedCount", examRecordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getStudentId, UserContext.userId())
                .in(ExamRecord::getStatus, 2, 3)));
        return Result.ok(m);
    }

    @GetMapping("/teacher")
    public Result<Map<String, Object>> teacher() {
        UserContext ctx = UserContext.get();
        List<TeacherJob> jobs = teacherJobMapper.selectList(new LambdaQueryWrapper<TeacherJob>()
                .eq(TeacherJob::getTeacherId, ctx.getUserId()));
        Set<Long> classIds = new LinkedHashSet<>();
        Set<String> subjects = new LinkedHashSet<>();
        for (TeacherJob j : jobs) {
            classIds.add(j.getClassId());
            subjects.add(j.getSubject());
        }
        long pendingReps = 0;
        if (ctx.getRole() == 3) {
            pendingReps = classRepMapper.selectList(new LambdaQueryWrapper<ClassRep>()
                            .eq(ClassRep::getAuditStatus, 0))
                    .stream().filter(r -> classIds.contains(r.getClassId())).count();
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("classIds", classIds);
        m.put("subjects", subjects);
        m.put("isHeadTeacher", ctx.getRole() == 3);
        m.put("pendingReps", pendingReps);
        m.put("paperCount", examPaperMapper.selectCount(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getCreateTeacher, ctx.getUserId())));
        return Result.ok(m);
    }

    /** 后台首页看板 */
    @GetMapping("/admin")
    public Result<Map<String, Object>> admin() {
        Long schoolId = UserContext.schoolId();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("studentCount", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getSchoolId, schoolId).eq(User::getRole, 1)));
        m.put("teacherCount", userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getSchoolId, schoolId).in(User::getRole, 2, 3)));
        m.put("classCount", classInfoMapper.selectCount(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getSchoolId, schoolId)));
        m.put("examCount", examPaperMapper.selectCount(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getSchoolId, schoolId)));
        m.put("questionCount", questionBankMapper.selectCount(new LambdaQueryWrapper<QuestionBank>()
                .eq(QuestionBank::getSchoolId, schoolId)));
        m.put("aiAnalysisCount", aiScoreAnalysisMapper.selectCount(new LambdaQueryWrapper<>()));

        // 分数段分布
        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .in(ExamRecord::getStatus, 2, 3));
        int[] bins = new int[5];
        for (ExamRecord r : records) {
            ExamPaper p = examPaperMapper.selectById(r.getPaperId());
            if (p == null || p.getTotalScore() == null || p.getTotalScore() == 0) continue;
            double pct = r.getTotalScore().doubleValue() / p.getTotalScore() * 100;
            int idx = Math.min(4, Math.max(0, (int) (pct / 20)));
            bins[idx]++;
        }
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0-59", bins[0] + bins[1] + (int) Math.ceil(bins[2] * 0.5));
        distribution.put("60-69", bins[2] / 2);
        distribution.put("70-79", bins[3]);
        distribution.put("80-89", bins[4]);
        distribution.put("90-100", 0);
        m.put("scoreDistribution", distribution);

        // 各科平均分
        Map<String, Double> subjectAvg = new LinkedHashMap<>();
        Map<String, List<BigDecimal>> subjectScores = new LinkedHashMap<>();
        for (ExamRecord r : records) {
            ExamPaper p = examPaperMapper.selectById(r.getPaperId());
            if (p == null) continue;
            subjectScores.computeIfAbsent(p.getSubject(), k -> new ArrayList<>()).add(r.getTotalScore());
        }
        subjectScores.forEach((k, v) -> subjectAvg.put(k, v.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0)));
        m.put("subjectAvg", subjectAvg);
        return Result.ok(m);
    }

    /** 超管跨校总览 */
    @GetMapping("/admin/global")
    public Result<Map<String, Object>> global() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("schoolCount", schoolMapper.selectCount(new LambdaQueryWrapper<>()));
        m.put("studentCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, 1)));
        m.put("teacherCount", userMapper.selectCount(new LambdaQueryWrapper<User>().in(User::getRole, 2, 3)));
        m.put("examCount", examRecordMapper.selectCount(new LambdaQueryWrapper<>()));
        m.put("questionCount", questionBankMapper.selectCount(new LambdaQueryWrapper<>()));
        m.put("aiAnalysisCount", aiScoreAnalysisMapper.selectCount(new LambdaQueryWrapper<>()));
        return Result.ok(m);
    }
}
