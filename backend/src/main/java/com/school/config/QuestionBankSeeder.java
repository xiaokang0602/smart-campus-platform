package com.school.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.entity.QuestionBank;
import com.school.entity.School;
import com.school.entity.User;
import com.school.mapper.QuestionBankMapper;
import com.school.mapper.SchoolMapper;
import com.school.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用题库初始化：当 question_bank 表为空时，为 9 个高中学科各写入 100 道题（共 900 道）。
 * 题型分布：第 1-40 单选、41-60 多选、61-75 判断、76-90 填空、91-100 简答。
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class QuestionBankSeeder implements ApplicationRunner {

    private final QuestionBankMapper questionBankMapper;
    private final SchoolMapper schoolMapper;
    private final UserMapper userMapper;

    private static final String GRADE = "高一";
    private static final String[] SUBJECTS = {"语文", "数学", "英语", "物理", "化学", "生物", "政治", "历史", "地理"};
    private static final Map<String, String[]> KPS = new LinkedHashMap<>();

    static {
        KPS.put("语文", new String[]{"字音字形", "成语运用", "病句辨析", "文言实词", "文言虚词", "古诗词鉴赏", "文学常识", "现代文阅读", "语言表达", "写作立意"});
        KPS.put("数学", new String[]{"集合与逻辑", "函数与导数", "三角函数", "数列", "不等式", "平面向量", "解析几何", "立体几何", "概率统计", "复数"});
        KPS.put("英语", new String[]{"词汇辨析", "时态语态", "非谓语动词", "定语从句", "名词性从句", "情态动词", "阅读理解", "完形填空", "语法填空", "书面表达"});
        KPS.put("物理", new String[]{"运动学", "力学", "牛顿运动定律", "功与能量", "动量守恒", "电场", "磁场", "电磁感应", "光学", "原子物理"});
        KPS.put("化学", new String[]{"物质的量", "化学方程式", "元素周期律", "化学键", "化学反应速率", "化学平衡", "电解质溶液", "氧化还原反应", "金属及其化合物", "有机化学"});
        KPS.put("生物", new String[]{"细胞结构", "细胞代谢", "光合作用", "呼吸作用", "遗传规律", "基因表达", "生物进化", "生态系统", "稳态调节", "免疫调节"});
        KPS.put("政治", new String[]{"经济生活", "政治生活", "文化生活", "生活与哲学", "认识论", "唯物辩证法", "历史唯物主义", "中国特色社会主义", "法治意识", "社会主义核心价值观"});
        KPS.put("历史", new String[]{"中国古代史", "中国近代史", "中国现代史", "世界古代史", "世界近代史", "世界现代史", "政治制度", "经济变革", "思想文化", "国际关系"});
        KPS.put("地理", new String[]{"地球运动", "大气环流", "气候类型", "水循环", "地表形态", "人口与城市", "农业区位", "工业区位", "区域发展", "可持续发展"});
    }

    @Override
    public void run(ApplicationArguments args) {
        Long count = questionBankMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            log.info("题库已有 {} 道题，跳过通用题库初始化", count);
            return;
        }
        School school = schoolMapper.selectOne(new LambdaQueryWrapper<School>().orderByAsc(School::getId).last("LIMIT 1"));
        if (school == null) {
            log.warn("未找到学校，跳过通用题库初始化");
            return;
        }
        List<User> teachers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getRole, 2, 3).orderByAsc(User::getId));
        Long creator = teachers.isEmpty() ? null : teachers.get(0).getId();
        LocalDateTime now = LocalDateTime.now();

        int total = 0;
        for (String subject : SUBJECTS) {
            String[] kps = KPS.getOrDefault(subject, new String[]{"综合"});
            for (int i = 1; i <= 100; i++) {
                questionBankMapper.insert(build(subject, kps, i, creator, school.getId(), now));
                total++;
            }
        }
        log.info("通用题库初始化完成：共写入 {} 道题（9 学科 × 100）", total);
    }

    private QuestionBank build(String subject, String[] kps, int seq, Long creator, Long schoolId, LocalDateTime now) {
        int type;
        if (seq <= 40) type = 1;           // 单选
        else if (seq <= 60) type = 2;      // 多选
        else if (seq <= 75) type = 3;      // 判断
        else if (seq <= 90) type = 4;      // 填空
        else type = 5;                     // 简答

        String kp = kps[(seq - 1) % kps.length];
        QuestionBank q = new QuestionBank();
        q.setSchoolId(schoolId);
        q.setSubject(subject);
        q.setQuestionType(type);
        q.setGrade(GRADE);
        q.setKnowledgePoint(kp);
        q.setDifficulty((seq % 5) + 1);
        q.setCreateTeacher(creator);
        q.setSource(1);
        q.setReviewStatus(1);
        q.setCreateTime(now);

        if (type == 1) {
            q.setTitle("【" + subject + "·" + kp + "】第 " + seq + " 题：下列关于「" + kp + "」的说法，正确的是（　）");
            q.setOptions("[{\"A\":\"" + kp + "是" + subject + "学科的重要内容，需结合具体情境理解与运用\"}," +
                    "{\"B\":\"" + kp + "与其他知识没有关联，可孤立记忆\"}," +
                    "{\"C\":\"" + kp + "在实际问题中无关紧要\"}," +
                    "{\"D\":\"以上说法均不正确\"}]");
            q.setAnswer("A");
            q.setAnalysis("本题考查「" + kp + "」的基础辨析。A 项表述正确；B、C 项表述片面，D 项不成立。");
        } else if (type == 2) {
            q.setTitle("【" + subject + "·" + kp + "】第 " + seq + " 题：关于「" + kp + "」，下列说法正确的有（　）");
            q.setOptions("[{\"A\":\"需要掌握其基本概念\"},{\"B\":\"需要理解其典型应用\"},{\"C\":\"无需任何练习即可掌握\"},{\"D\":\"与相关知识点存在内在联系\"}]");
            q.setAnswer("A,B,D");
            q.setAnalysis("本题考查「" + kp + "」的多角度理解，A、B、D 正确，C 项错误。");
        } else if (type == 3) {
            boolean correct = seq % 2 == 0;
            q.setTitle("【" + subject + "·" + kp + "】第 " + seq + " 题：判断正误——「" + kp + "」是" + subject + "学科需要重点掌握的内容。（　）");
            q.setOptions(null);
            q.setAnswer(correct ? "对" : "错");
            q.setAnalysis(correct ? "该说法正确。" + kp + "是" + subject + "学科的基础内容。" : "该说法错误，应结合教材对「" + kp + "」的具体表述作出判断。");
        } else if (type == 4) {
            q.setTitle("【" + subject + "·" + kp + "】第 " + seq + " 题：在" + subject + "学科中，「" + kp + "」的核心内容可以概括为______。");
            q.setOptions(null);
            q.setAnswer(kp);
            q.setAnalysis("填空题考查对「" + kp + "」概念的识记，参考答案为「" + kp + "」及其相关要点。");
        } else {
            q.setTitle("【" + subject + "·" + kp + "】第 " + seq + " 题：请结合所学知识，简述「" + kp + "」的主要内容。");
            q.setOptions(null);
            q.setAnswer("围绕「" + kp + "」作答：说明其定义与核心要点，并结合" + subject + "学科的典型例子展开。");
            q.setAnalysis("简答题考查对「" + kp + "」的归纳与表达能力，答案应条理清晰、要点完整。");
        }
        return q;
    }
}
