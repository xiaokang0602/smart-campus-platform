package com.school.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.*;
import com.school.mapper.*;
import com.school.service.AiService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ExamRecordMapper examRecordMapper;
    private final ExamPaperMapper examPaperMapper;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final TeacherJobMapper teacherJobMapper;
    private final AiScoreAnalysisMapper aiScoreAnalysisMapper;
    private final AiService aiService;

    /** 学生：我的成绩列表 + 趋势 */
    @GetMapping("/student/mine")
    public Result<List<Map<String, Object>>> mine() {
        Long studentId = UserContext.userId();
        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getStudentId, studentId)
                .in(ExamRecord::getStatus, 2, 3)
                .orderByDesc(ExamRecord::getSubmitTime));
        List<Map<String, Object>> list = records.stream().map(this::row).collect(Collectors.toList());
        return Result.ok(list);
    }

    /** 教师：某次考试成绩（任课看本科，班主任看全科） */
    @GetMapping("/paper/{paperId}")
    public Result<List<Map<String, Object>>> paperScores(@PathVariable Long paperId) {
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        checkScorePermission(paper);
        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getPaperId, paperId)
                .in(ExamRecord::getStatus, 2, 3)
                .orderByDesc(ExamRecord::getTotalScore));
        List<Map<String, Object>> rows = records.stream().map(this::row).collect(Collectors.toList());
        // 班级排名
        int rank = 1;
        for (Map<String, Object> r : rows) {
            r.put("rank", rank++);
        }
        return Result.ok(rows);
    }

    /** 学生画像 */
    @GetMapping("/student/{studentId}/portrait")
    public Result<Map<String, Object>> portrait(@PathVariable Long studentId) {
        Student s = studentMapper.selectById(studentId);
        User u = userMapper.selectById(studentId);
        ClassInfo c = s == null ? null : classInfoMapper.selectById(s.getClassId());

        List<ExamRecord> records = examRecordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getStudentId, studentId)
                .in(ExamRecord::getStatus, 2, 3)
                .orderByAsc(ExamRecord::getSubmitTime));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (ExamRecord r : records) {
            ExamPaper p = examPaperMapper.selectById(r.getPaperId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("time", r.getSubmitTime());
            m.put("subject", p == null ? "" : p.getSubject());
            m.put("score", r.getTotalScore());
            m.put("paperName", p == null ? "" : p.getPaperName());
            trend.add(m);
        }

        AiScoreAnalysis latest = aiScoreAnalysisMapper.selectOne(new LambdaQueryWrapper<AiScoreAnalysis>()
                .eq(AiScoreAnalysis::getStudentId, studentId)
                .orderByDesc(AiScoreAnalysis::getCreateTime)
                .last("limit 1"));

        Map<String, Object> result = new LinkedHashMap<>();
        if (u != null) {
            Map<String, Object> su = new LinkedHashMap<>();
            su.put("id", u.getId());
            su.put("realName", u.getRealName());
            result.put("student", su);
        }
        result.put("studentNo", s == null ? "" : s.getStudentNo());
        result.put("className", c == null ? "" : c.getClassName());
        result.put("duty", s == null ? "" : s.getDuty());
        result.put("trend", trend);
        result.put("aiAnalysis", latest == null ? "" : latest.getAnalysisContent());
        return Result.ok(result);
    }

    /** 触发画像 AI 分析 */
    @PostMapping("/student/{studentId}/ai")
    public Result<Void> aiPortrait(@PathVariable Long studentId, @RequestBody(required = false) Map<String, String> body) {
        String subject = body == null ? null : body.get("subject");
        aiService.generatePortrait(studentId, subject, UserContext.userId());
        return Result.ok();
    }

    /** 学生：某次考试 AI 学情分析 */
    @GetMapping("/analysis/{recordId}")
    public Result<Map<String, Object>> analysis(@PathVariable Long recordId) {
        AiScoreAnalysis a = aiScoreAnalysisMapper.selectOne(new LambdaQueryWrapper<AiScoreAnalysis>()
                .eq(AiScoreAnalysis::getExamRecordId, recordId)
                .orderByDesc(AiScoreAnalysis::getCreateTime).last("limit 1"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("content", a == null ? "暂无分析结果" : a.getAnalysisContent());
        m.put("createTime", a == null ? null : a.getCreateTime());
        return Result.ok(m);
    }

    /** 导出 Excel */
    @GetMapping("/export")
    @OpLog(module = "score", action = "EXPORT", targetType = "score")
    public void export(@RequestParam(required = false) Long paperId, HttpServletResponse response) throws Exception {
        List<Map<String, Object>> rows;
        String name;
        if (paperId != null) {
            ExamPaper paper = examPaperMapper.selectById(paperId);
            checkScorePermission(paper);
            rows = paperScores(paperId).getData();
            name = paper.getPaperName();
        } else {
            rows = mine().getData();
            name = "我的成绩";
        }
        List<List<String>> head = List.of(
                List.of("姓名"), List.of("学号"), List.of("科目"), List.of("试卷"),
                List.of("分数"), List.of("排名"), List.of("切屏次数"), List.of("解封次数"));
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            data.add(List.of(
                    str(r.get("studentName")), str(r.get("studentNo")), str(r.get("subject")),
                    str(r.get("paperName")), str(r.get("score")), str(r.get("rank")),
                    str(r.get("cheatSwitchCount")), str(r.get("reopenCount"))));
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fn = URLEncoder.encode(name + "_" + System.currentTimeMillis(), StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fn + ".xlsx");
        EasyExcel.write(response.getOutputStream()).head(head).sheet("成绩").doWrite(data);
    }

    private Map<String, Object> row(ExamRecord r) {
        ExamPaper p = examPaperMapper.selectById(r.getPaperId());
        User u = userMapper.selectById(r.getStudentId());
        Student s = studentMapper.selectById(r.getStudentId());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("recordId", r.getId());
        m.put("paperId", r.getPaperId());
        m.put("paperName", p == null ? "" : p.getPaperName());
        m.put("subject", p == null ? "" : p.getSubject());
        m.put("studentId", r.getStudentId());
        m.put("studentName", u == null ? "" : u.getRealName());
        m.put("studentNo", s == null ? "" : s.getStudentNo());
        m.put("score", r.getTotalScore());
        m.put("fullScore", p == null ? 0 : p.getTotalScore());
        m.put("submitTime", r.getSubmitTime());
        m.put("cheatSwitchCount", r.getCheatSwitchCount());
        m.put("reopenCount", r.getReopenCount());
        m.put("status", r.getStatus());
        return m;
    }

    private void checkScorePermission(ExamPaper paper) {
        UserContext ctx = UserContext.get();
        if (ctx.getRole() == 4) {
            if (!UserContext.isSuperAdmin() && !ctx.getSchoolId().equals(paper.getSchoolId())) {
                throw new BusinessException(403, "无权查看其他学校数据");
            }
            return;
        }
        // 教师：任课教师仅本科目，班主任全科
        if (ctx.getRole() == 2) {
            long count = teacherJobMapper.selectCount(new LambdaQueryWrapper<TeacherJob>()
                    .eq(TeacherJob::getTeacherId, ctx.getUserId())
                    .eq(TeacherJob::getSubject, paper.getSubject()));
            if (count == 0) {
                throw new BusinessException(403, "您只能查看自己任教科目的成绩");
            }
        }
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
