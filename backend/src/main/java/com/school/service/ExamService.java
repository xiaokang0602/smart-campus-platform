package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.BusinessException;
import com.school.entity.*;
import com.school.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线考试核心服务：开始答题、草稿、交卷、自动判分、防切屏
 */
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamPaperMapper examPaperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionBankMapper questionBankMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final StudentMapper studentMapper;
    private final AiService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 学生开始/进入考试 */
    public Map<String, Object> start(Long paperId, Long studentId) {
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null || paper.getStatus() == null || paper.getStatus() == 0) {
            throw new BusinessException("考试不存在或未发布");
        }
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        if (!isClassAllowed(paper, student.getClassId())) {
            throw new BusinessException("您不在本次考试允许的班级范围内");
        }

        ExamRecord record = examRecordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getStudentId, studentId)
                .orderByDesc(ExamRecord::getId)
                .last("limit 1"));

        if (record == null) {
            record = new ExamRecord();
            record.setPaperId(paperId);
            record.setStudentId(studentId);
            record.setStartTime(LocalDateTime.now());
            record.setStatus(1);
            record.setTotalScore(BigDecimal.ZERO);
            record.setCheatSwitchCount(0);
            record.setMaxAllowedSwitch(3);
            record.setReopenCount(0);
            examRecordMapper.insert(record);
        } else if (record.getStatus() == 3) {
            throw new BusinessException("您因切屏超限被强制交卷，请联系教师解封");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordId", record.getId());
        result.put("paper", paper);
        result.put("questions", paperQuestions(paperId, false));
        return result;
    }

    /** 保存答题草稿 */
    public void saveDraft(Long recordId, Long studentId, Map<Long, String> answers) {
        ExamRecord record = checkOwn(recordId, studentId);
        if (record.getStatus() != 1) {
            return;
        }
        for (Map.Entry<Long, String> e : answers.entrySet()) {
            upsertAnswer(recordId, e.getKey(), e.getValue(), 0, BigDecimal.ZERO);
        }
    }

    /** 交卷（含自动判分客观题） */
    public Map<String, Object> submit(Long recordId, Long studentId, Map<Long, String> answers) {
        ExamRecord record = checkOwn(recordId, studentId);
        if (record.getStatus() == 2 || record.getStatus() == 3) {
            throw new BusinessException("本次考试已结束");
        }
        if (answers != null) {
            for (Map.Entry<Long, String> e : answers.entrySet()) {
                upsertAnswer(recordId, e.getKey(), e.getValue(), 0, BigDecimal.ZERO);
            }
        }

        // 自动判分
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, record.getPaperId()));
        BigDecimal total = BigDecimal.ZERO;
        for (PaperQuestion pq : pqs) {
            QuestionBank q = questionBankMapper.selectById(pq.getQuestionId());
            if (q == null) {
                continue;
            }
            ExamAnswer ans = examAnswerMapper.selectOne(new LambdaQueryWrapper<ExamAnswer>()
                    .eq(ExamAnswer::getExamRecordId, recordId)
                    .eq(ExamAnswer::getQuestionId, pq.getQuestionId()));
            String studentAnswer = ans == null ? "" : ans.getStudentAnswer();
            int type = q.getQuestionType() == null ? 0 : q.getQuestionType();
            if (type >= 1 && type <= 4) {
                boolean correct = grade(type, studentAnswer, q.getAnswer());
                BigDecimal score = correct ? BigDecimal.valueOf(pq.getScore()) : BigDecimal.ZERO;
                if (ans != null) {
                    ans.setIsCorrect(correct ? 1 : 0);
                    ans.setScore(score);
                    examAnswerMapper.updateById(ans);
                }
                total = total.add(score);
            } else {
                // 简答题暂不自动判分，保留待人工评阅
                if (ans != null) {
                    ans.setIsCorrect(0);
                    ans.setScore(BigDecimal.ZERO);
                    examAnswerMapper.updateById(ans);
                }
            }
        }

        record.setStatus(2);
        record.setSubmitTime(LocalDateTime.now());
        record.setTotalScore(total);
        examRecordMapper.updateById(record);

        // 异步生成考后 AI 学情分析
        aiService.generateAfterExam(recordId, studentId, paper == null ? "" : paper.getSubject(), total);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", total);
        result.put("fullScore", paper == null ? 0 : paper.getTotalScore());
        return result;
    }

    /** 切屏上报；达到上限强制交卷 */
    public Map<String, Object> cheat(Long recordId, Long studentId) {
        ExamRecord record = checkOwn(recordId, studentId);
        int count = (record.getCheatSwitchCount() == null ? 0 : record.getCheatSwitchCount()) + 1;
        int max = record.getMaxAllowedSwitch() == null ? 3 : record.getMaxAllowedSwitch();
        record.setCheatSwitchCount(count);
        boolean forced = false;
        if (count >= max) {
            record.setStatus(3);
            record.setSubmitTime(LocalDateTime.now());
            forced = true;
        }
        examRecordMapper.updateById(record);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", count);
        result.put("max", max);
        result.put("forced", forced);
        return result;
    }

    /** 教师解封：清空切屏计数、恢复答题入口 */
    public void reopen(Long recordId, Long teacherId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        record.setCheatSwitchCount(0);
        record.setStatus(1);
        record.setReopenCount((record.getReopenCount() == null ? 0 : record.getReopenCount()) + 1);
        record.setLastReopenBy(teacherId);
        record.setLastReopenTime(LocalDateTime.now());
        examRecordMapper.updateById(record);
    }

    /** 获取试卷题目（含/不含答案） */
    public List<Map<String, Object>> paperQuestions(Long paperId, boolean withAnswer) {
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId)
                        .orderByAsc(PaperQuestion::getSort));
        return pqs.stream().map(pq -> {
            QuestionBank q = questionBankMapper.selectById(pq.getQuestionId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("paperQuestionId", pq.getId());
            m.put("questionId", q == null ? null : q.getId());
            m.put("score", pq.getScore());
            m.put("sort", pq.getSort());
            if (q != null) {
                m.put("subject", q.getSubject());
                m.put("questionType", q.getQuestionType());
                m.put("title", q.getTitle());
                m.put("options", parseOptions(q.getOptions()));
                m.put("difficulty", q.getDifficulty());
                m.put("knowledgePoint", q.getKnowledgePoint());
                if (withAnswer) {
                    m.put("answer", q.getAnswer());
                    m.put("analysis", q.getAnalysis());
                }
            }
            return m;
        }).collect(Collectors.toList());
    }

    /** 学生查看历史试卷：题目 + 自己的作答 + 答案与解析 */
    public Map<String, Object> historyPaper(Long paperId, Long studentId) {
        ExamPaper paper = examPaperMapper.selectById(paperId);
        ExamRecord record = examRecordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getStudentId, studentId)
                .orderByDesc(ExamRecord::getId).last("limit 1"));
        List<Map<String, Object>> questions = paperQuestions(paperId, true);
        if (record != null) {
            for (Map<String, Object> q : questions) {
                Long qid = (Long) q.get("questionId");
                ExamAnswer ans = examAnswerMapper.selectOne(new LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getExamRecordId, record.getId())
                        .eq(ExamAnswer::getQuestionId, qid));
                q.put("studentAnswer", ans == null ? "" : ans.getStudentAnswer());
                q.put("isCorrect", ans == null ? 0 : ans.getIsCorrect());
                q.put("gotScore", ans == null ? 0 : ans.getScore());
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("paper", paper);
        result.put("record", record);
        result.put("questions", questions);
        return result;
    }

    private ExamRecord checkOwn(Long recordId, Long studentId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !studentId.equals(record.getStudentId())) {
            throw new BusinessException("考试记录不存在");
        }
        return record;
    }

    private void upsertAnswer(Long recordId, Long questionId, String answer, int correct, BigDecimal score) {
        ExamAnswer ans = examAnswerMapper.selectOne(new LambdaQueryWrapper<ExamAnswer>()
                .eq(ExamAnswer::getExamRecordId, recordId)
                .eq(ExamAnswer::getQuestionId, questionId));
        if (ans == null) {
            ans = new ExamAnswer();
            ans.setExamRecordId(recordId);
            ans.setQuestionId(questionId);
            ans.setStudentAnswer(answer);
            ans.setIsCorrect(correct);
            ans.setScore(score);
            examAnswerMapper.insert(ans);
        } else {
            ans.setStudentAnswer(answer);
            ans.setIsCorrect(correct);
            ans.setScore(score);
            examAnswerMapper.updateById(ans);
        }
    }

    /** 客观题判分 */
    private boolean grade(int type, String studentAnswer, String answer) {
        if (studentAnswer == null || answer == null) {
            return false;
        }
        String s = normalize(studentAnswer);
        String a = normalize(answer);
        if (type == 2) {
            // 多选：排序后比较
            return sortTokens(s).equals(sortTokens(a));
        }
        return s.equals(a);
    }

    private String normalize(String v) {
        return v.trim().toUpperCase()
                .replace("正确", "T").replace("对", "T").replace("TRUE", "T")
                .replace("错误", "F").replace("错", "F").replace("FALSE", "F");
    }

    private String sortTokens(String v) {
        List<String> tokens = new ArrayList<>();
        for (String part : v.split(",")) {
            if (!part.isBlank()) {
                tokens.add(part.trim());
            }
        }
        Collections.sort(tokens);
        return String.join(",", tokens);
    }

    private boolean isClassAllowed(ExamPaper paper, Long classId) {
        if (paper.getClassIds() == null || paper.getClassIds().isBlank()) {
            return true;
        }
        try {
            JsonNode node = objectMapper.readTree(paper.getClassIds());
            for (JsonNode n : node) {
                if (n.asLong() == classId.longValue()) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private List<Map<String, Object>> parseOptions(String json) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return list;
        }
        try {
            JsonNode arr = objectMapper.readTree(json);
            for (JsonNode n : arr) {
                Map<String, Object> m = new LinkedHashMap<>();
                Iterator<String> it = n.fieldNames();
                while (it.hasNext()) {
                    String k = it.next();
                    m.put(k, n.get(k).asText());
                }
                list.add(m);
            }
        } catch (Exception ignored) {
        }
        return list;
    }
}
