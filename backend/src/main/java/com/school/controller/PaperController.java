package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.*;
import com.school.entity.ExamPaper;
import com.school.entity.PaperQuestion;
import com.school.entity.Student;
import com.school.mapper.ExamPaperMapper;
import com.school.mapper.PaperQuestionMapper;
import com.school.mapper.StudentMapper;
import com.school.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/paper")
@RequiredArgsConstructor
public class PaperController {

    private final ExamPaperMapper examPaperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final StudentMapper studentMapper;
    private final ExamService examService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 教师端/后台：试卷列表 */
    @GetMapping("/list")
    public Result<PageResult<ExamPaper>> list(@RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize,
                                              @RequestParam(required = false) String subject,
                                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ExamPaper> qw = new LambdaQueryWrapper<>();
        UserContext ctx = UserContext.get();
        if (ctx.getRole() == 4) {
            if (!UserContext.isSuperAdmin()) {
                qw.eq(ExamPaper::getSchoolId, ctx.getSchoolId());
            }
        } else {
            qw.eq(ExamPaper::getCreateTeacher, ctx.getUserId());
        }
        if (subject != null && !subject.isBlank()) {
            qw.eq(ExamPaper::getSubject, subject);
        }
        if (status != null) {
            qw.eq(ExamPaper::getStatus, status);
        }
        qw.orderByDesc(ExamPaper::getCreateTime);
        Page<ExamPaper> page = examPaperMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return Result.ok(PageResult.of(page, page.getRecords()));
    }

    /** 学生端：可参加的考试列表 */
    @GetMapping("/student/list")
    public Result<List<Map<String, Object>>> studentList() {
        Student student = studentMapper.selectById(UserContext.userId());
        if (student == null) {
            return Result.ok(Collections.emptyList());
        }
        List<ExamPaper> papers = examPaperMapper.selectList(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getSchoolId, UserContext.schoolId())
                .eq(ExamPaper::getStatus, 1)
                .orderByDesc(ExamPaper::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamPaper p : papers) {
            if (!isClassAllowed(p, student.getClassId())) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("paperName", p.getPaperName());
            m.put("subject", p.getSubject());
            m.put("totalScore", p.getTotalScore());
            m.put("examTime", p.getExamTime());
            m.put("createTime", p.getCreateTime());
            result.add(m);
        }
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("paper", paper);
        m.put("questions", examService.paperQuestions(id, true));
        return Result.ok(m);
    }

    @PostMapping("/create")
    @OpLog(module = "exam", action = "CREATE", targetType = "exam_paper")
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        UserContext ctx = UserContext.get();
        ExamPaper paper = new ExamPaper();
        paper.setSchoolId(ctx.getSchoolId());
        paper.setPaperName((String) body.get("paperName"));
        paper.setSubject((String) body.get("subject"));
        paper.setGrade((String) body.get("grade"));
        paper.setTotalScore((Integer) body.getOrDefault("totalScore", 0));
        paper.setExamTime((Integer) body.getOrDefault("examTime", 60));
        paper.setCreateTeacher(ctx.getUserId());
        paper.setSource((Integer) body.getOrDefault("source", 1));
        paper.setStatus(0);
        paper.setClassIds(toJson(body.get("classIds")));
        examPaperMapper.insert(paper);

        List<Map<String, Object>> questions = (List<Map<String, Object>>) body.get("questions");
        if (questions != null) {
            int sort = 1;
            for (Map<String, Object> q : questions) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(Long.valueOf(String.valueOf(q.get("questionId"))));
                pq.setScore(Integer.valueOf(String.valueOf(q.get("score"))));
                pq.setSort(sort++);
                paperQuestionMapper.insert(pq);
            }
        }
        return Result.ok(paper.getId());
    }

    @PostMapping("/{id}/publish")
    @OpLog(module = "exam", action = "UPDATE", targetType = "exam_paper")
    public Result<Void> publish(@PathVariable Long id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        paper.setStatus(1);
        examPaperMapper.updateById(paper);
        return Result.ok();
    }

    @PostMapping("/{id}/end")
    @OpLog(module = "exam", action = "UPDATE", targetType = "exam_paper")
    public Result<Void> end(@PathVariable Long id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        paper.setStatus(2);
        examPaperMapper.updateById(paper);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OpLog(module = "exam", action = "DELETE", targetType = "exam_paper")
    public Result<Void> delete(@PathVariable Long id) {
        examPaperMapper.deleteById(id);
        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id));
        return Result.ok();
    }

    private String toJson(Object o) {
        if (o == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }

    private boolean isClassAllowed(ExamPaper paper, Long classId) {
        if (paper.getClassIds() == null || paper.getClassIds().isBlank()) {
            return true;
        }
        try {
            var node = objectMapper.readTree(paper.getClassIds());
            for (var n : node) {
                if (n.asLong() == classId.longValue()) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
