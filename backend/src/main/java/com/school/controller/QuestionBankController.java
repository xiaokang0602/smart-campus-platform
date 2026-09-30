package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.*;
import com.school.entity.QuestionBank;
import com.school.mapper.QuestionBankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankMapper questionBankMapper;

    /** 题库列表（严格按 school_id 校际隔离） */
    @GetMapping("/list")
    public Result<PageResult<QuestionBank>> list(@RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "10") long pageSize,
                                                 @RequestParam(required = false) String subject,
                                                 @RequestParam(required = false) String grade,
                                                 @RequestParam(required = false) Integer questionType,
                                                 @RequestParam(required = false) Integer difficulty,
                                                 @RequestParam(required = false) Integer reviewStatus,
                                                 @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<QuestionBank> qw = new LambdaQueryWrapper<>();
        if (!UserContext.isSuperAdmin()) {
            qw.eq(QuestionBank::getSchoolId, UserContext.schoolId());
        }
        if (subject != null && !subject.isBlank()) qw.eq(QuestionBank::getSubject, subject);
        if (grade != null && !grade.isBlank()) qw.eq(QuestionBank::getGrade, grade);
        if (questionType != null) qw.eq(QuestionBank::getQuestionType, questionType);
        if (difficulty != null) qw.eq(QuestionBank::getDifficulty, difficulty);
        if (reviewStatus != null) qw.eq(QuestionBank::getReviewStatus, reviewStatus);
        if (keyword != null && !keyword.isBlank()) qw.like(QuestionBank::getTitle, keyword);
        qw.orderByDesc(QuestionBank::getCreateTime);
        Page<QuestionBank> page = questionBankMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return Result.ok(PageResult.of(page, page.getRecords()));
    }

    @PostMapping("/add")
    @OpLog(module = "question_bank", action = "CREATE", targetType = "question_bank")
    public Result<Long> add(@RequestBody QuestionBank q) {
        q.setId(null);
        q.setSchoolId(UserContext.schoolId());
        q.setCreateTeacher(UserContext.userId());
        q.setSource(1);
        q.setReviewStatus(1);
        q.setCreateTime(LocalDateTime.now());
        questionBankMapper.insert(q);
        return Result.ok(q.getId());
    }

    @PutMapping("/update")
    @OpLog(module = "question_bank", action = "UPDATE", targetType = "question_bank")
    public Result<Void> update(@RequestBody QuestionBank q) {
        questionBankMapper.updateById(q);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OpLog(module = "question_bank", action = "DELETE", targetType = "question_bank")
    public Result<Void> delete(@PathVariable Long id) {
        questionBankMapper.deleteById(id);
        return Result.ok();
    }

    /** AI 批量出题：生成候选题（review_status=0，待教师勾选入库） */
    @PostMapping("/ai-generate")
    public Result<List<QuestionBank>> aiGenerate(@RequestBody Map<String, Object> body) {
        String subject = (String) body.getOrDefault("subject", "数学");
        String grade = (String) body.getOrDefault("grade", "初二");
        String kp = (String) body.getOrDefault("knowledgePoint", "综合");
        int type = (Integer) body.getOrDefault("questionType", 1);
        int count = (Integer) body.getOrDefault("count", 10);
        if (count < 1) count = 1;
        if (count > 50) count = 50;

        String batchNo = "AI" + System.currentTimeMillis();
        List<QuestionBank> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            QuestionBank q = new QuestionBank();
            q.setSchoolId(UserContext.schoolId());
            q.setSubject(subject);
            q.setQuestionType(type);
            q.setGrade(grade);
            q.setKnowledgePoint(kp);
            q.setTitle("【AI】" + subject + "（" + kp + "）第 " + i + " 题：请作答。");
            q.setOptions(type == 1 || type == 2 ? "[{\"A\":\"选项A\"},{\"B\":\"选项B\"},{\"C\":\"选项C\"},{\"D\":\"选项D\"}]" : null);
            q.setAnswer("A");
            q.setAnalysis("由 AI 生成的演示题目，可编辑后入库。");
            q.setDifficulty(3);
            q.setCreateTeacher(UserContext.userId());
            q.setSource(2);
            q.setAiBatchNo(batchNo);
            q.setReviewStatus(0);
            q.setCreateTime(LocalDateTime.now());
            questionBankMapper.insert(q);
            list.add(q);
        }
        return Result.ok(list);
    }

    /** 采纳入库 */
    @PostMapping("/adopt")
    public Result<Void> adopt(@RequestBody Map<String, Object> body) {
        List<Long> ids = idsOf(body.get("ids"));
        for (Long id : ids) {
            QuestionBank q = questionBankMapper.selectById(id);
            if (q != null) {
                q.setReviewStatus(1);
                questionBankMapper.updateById(q);
            }
        }
        return Result.ok();
    }

    /** 丢弃 */
    @PostMapping("/discard")
    public Result<Void> discard(@RequestBody Map<String, Object> body) {
        List<Long> ids = idsOf(body.get("ids"));
        for (Long id : ids) {
            QuestionBank q = questionBankMapper.selectById(id);
            if (q != null) {
                q.setReviewStatus(2);
                questionBankMapper.updateById(q);
            }
        }
        return Result.ok();
    }

    private List<Long> idsOf(Object o) {
        List<Long> ids = new ArrayList<>();
        if (o instanceof List<?> list) {
            for (Object x : list) {
                ids.add(Long.valueOf(String.valueOf(x)));
            }
        }
        return ids;
    }
}
