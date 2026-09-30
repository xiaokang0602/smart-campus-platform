package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.entity.AiScoreAnalysis;
import com.school.entity.ExamRecord;
import com.school.entity.ExamPaper;
import com.school.mapper.AiScoreAnalysisMapper;
import com.school.mapper.ExamRecordMapper;
import com.school.mapper.ExamPaperMapper;
import com.school.util.ArkClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 学情分析服务（调用火山方舟大模型，失败回退本地模拟）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ArkClient arkClient;
    private final AiScoreAnalysisMapper aiScoreAnalysisMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamPaperMapper examPaperMapper;

    /**
     * 考后自动生成单次学情分析
     */
    @Async
    public void generateAfterExam(Long recordId, Long studentId, String subject, BigDecimal score) {
        try {
            ExamRecord record = examRecordMapper.selectById(recordId);
            ExamPaper paper = record == null ? null : examPaperMapper.selectById(record.getPaperId());
            String paperName = paper == null ? subject : paper.getPaperName();
            String prompt = "请对一名初中生的一次考试进行学情分析。科目：" + subject
                    + "，试卷：" + paperName + "，本次得分：" + score + "。请输出：1.知识掌握情况；2.薄弱知识点；3.错题归因；4.针对性学习建议。";
            String content = arkClient.chat("你是一位专业的中学学科教师，擅长学情诊断。", prompt);
            save(studentId, recordId, 1, subject, null, content);
        } catch (Exception e) {
            log.warn("考后 AI 分析失败: {}", e.getMessage());
        }
    }

    /**
     * 教师端学生画像实时分析（跨多次考试）
     */
    @Async
    public void generatePortrait(Long studentId, String subject, Long analystId) {
        try {
            List<ExamRecord> records = examRecordMapper.selectList(
                    new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getStudentId, studentId)
                            .in(ExamRecord::getStatus, 2, 3)
                            .orderByDesc(ExamRecord::getSubmitTime));
            String history = records.stream()
                    .map(r -> "得分" + r.getTotalScore())
                    .collect(Collectors.joining("，"));
            String prompt = "请对该学生的历次成绩进行学情诊断。科目：" + subject
                    + "，历次成绩：" + (history.isEmpty() ? "暂无" : history)
                    + "。请输出：1.成绩波动原因；2.薄弱知识点；3.针对性学习建议。";
            String content = arkClient.chat("你是一位专业的中学学科教师，擅长学情诊断。", prompt);
            save(studentId, null, 2, subject, analystId, content);
        } catch (Exception e) {
            log.warn("画像 AI 分析失败: {}", e.getMessage());
        }
    }

    private void save(Long studentId, Long recordId, int scope, String subject, Long analystId, String content) {
        AiScoreAnalysis a = new AiScoreAnalysis();
        a.setStudentId(studentId);
        a.setExamRecordId(recordId);
        a.setAnalysisScope(scope);
        a.setSubject(subject);
        a.setAnalystId(analystId);
        a.setAnalysisContent(content);
        aiScoreAnalysisMapper.insert(a);
    }
}
