package com.school.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.entity.*;
import com.school.mapper.*;
import com.school.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 演示种子数据：仅当 user 表为空时写入，保证开箱即可登录演示
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedDataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final SchoolMapper schoolMapper;
    private final ClassInfoMapper classInfoMapper;
    private final StudentMapper studentMapper;
    private final TeacherJobMapper teacherJobMapper;
    private final TimetableMapper timetableMapper;
    private final QuestionBankMapper questionBankMapper;
    private final ExamPaperMapper examPaperMapper;
    private final PaperQuestionMapper paperQuestionMapper;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    @Value("${app.seed.default-password:123456}")
    private String defaultPassword;

    @Value("${app.seed.super-admin.username:admin}")
    private String superAdminUsername;

    @Value("${app.seed.super-admin.password:admin123}")
    private String superAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            return;
        }
        try {
            seed();
            log.info("演示种子数据初始化完成");
        } catch (Exception e) {
            log.error("种子数据初始化失败", e);
        }
    }

    private void seed() {
        // 学校
        School school = new School();
        school.setSchoolName("青蓝初级中学");
        school.setAddress("北京市海淀区智慧路 88 号");
        schoolMapper.insert(school);
        Long schoolId = school.getId();

        // 超管
        User superAdmin = user("admin", superAdminPassword, "平台超级管理员", 4, 1, 0L);
        // 校管理员
        User schoolAdmin = user("schooladmin", defaultPassword, "周敏", 4, 2, schoolId);
        // 教师（任课）
        User tChinese = user("zhangwei", defaultPassword, "张伟", 2, 0, schoolId);
        User tMath = user("lina", defaultPassword, "李娜", 2, 0, schoolId);
        User tEnglish = user("wangqiang", defaultPassword, "王强", 2, 0, schoolId);
        // 班主任（任教数学）
        User headTeacher = user("chenjing", defaultPassword, "陈静", 3, 0, schoolId);

        // 班级
        ClassInfo c1 = cls("初二1班", "初二", headTeacher.getId(), schoolId);
        ClassInfo c2 = cls("初二2班", "初二", null, schoolId);

        // 学生
        String[][] stu = {
                {"stu001", "林小雨", "班长", "女"},
                {"stu002", "陈浩然", "无", "男"},
                {"stu003", "赵思琪", "语文课代表", "女"},
                {"stu004", "孙一鸣", "无", "男"},
                {"stu005", "周晓彤", "英语课代表", "女"},
                {"stu006", "吴子轩", "无", "男"},
                {"stu007", "郑雅文", "无", "女"},
                {"stu008", "冯子豪", "数学课代表", "男"},
                {"stu009", "何雨桐", "无", "女"},
                {"stu010", "蒋一帆", "无", "男"},
        };
        for (int i = 0; i < stu.length; i++) {
            User u = user("stu" + (i + 1), defaultPassword, stu[i][1], 1, 0, schoolId);
            Student s = new Student();
            s.setId(u.getId());
            s.setClassId(c1.getId());
            s.setStudentNo(stu[i][0]);
            s.setDuty(stu[i][2]);
            s.setGender(stu[i][3]);
            s.setBirthday(LocalDate.of(2010, (i % 12) + 1, (i % 27) + 1));
            s.setArchiveNote("勤奋好学，乐于助人。");
            studentMapper.insert(s);
        }

        // 教师任职
        job(tChinese.getId(), c1.getId(), "语文", 0);
        job(tMath.getId(), c1.getId(), "数学", 0);
        job(tEnglish.getId(), c1.getId(), "英语", 0);
        job(headTeacher.getId(), c1.getId(), "数学", 1);
        job(tChinese.getId(), c2.getId(), "语文", 0);

        // 课表（初二1班，周一~周五 5 节课）
        String[][] schedule = {
                {"语文", "数学", "英语", "物理", "历史"},
                {"数学", "语文", "英语", "道德与法治", "体育"},
                {"英语", "数学", "语文", "地理", "生物"},
                {"数学", "英语", "物理", "语文", "历史"},
                {"语文", "数学", "英语", "地理", "生物"},
        };
        Long[][] tIds = {
                {tChinese.getId(), tMath.getId(), tEnglish.getId(), tChinese.getId(), tChinese.getId()},
                {tMath.getId(), tChinese.getId(), tEnglish.getId(), tChinese.getId(), tMath.getId()},
                {tEnglish.getId(), tMath.getId(), tChinese.getId(), tChinese.getId(), tChinese.getId()},
                {tMath.getId(), tEnglish.getId(), tChinese.getId(), tChinese.getId(), tChinese.getId()},
                {tChinese.getId(), tMath.getId(), tEnglish.getId(), tChinese.getId(), tChinese.getId()},
        };
        for (int w = 0; w < 5; w++) {
            for (int p = 0; p < 5; p++) {
                Timetable t = new Timetable();
                t.setClassId(c1.getId());
                t.setWeek(w + 1);
                t.setPeriod(p + 1);
                t.setSubject(schedule[w][p]);
                t.setTeacherId(tIds[w][p]);
                t.setRoom("A" + (w + 1) + "0" + (p + 1));
                timetableMapper.insert(t);
            }
        }

        // 题库（数学 5 道）
        Long q1 = question(schoolId, "数学", 1, "初二", "下列图形中，是轴对称图形的是（ ）",
                "[{\"A\":\"平行四边形\"},{\"B\":\"等腰三角形\"},{\"C\":\"直角梯形\"},{\"D\":\"圆\"}]",
                "B,D", "等腰三角形与圆都是轴对称图形。", 2, "轴对称图形", tMath.getId(), 1);
        Long q2 = question(schoolId, "数学", 1, "初二", "计算 3x + 2x 的结果是（ ）",
                "[{\"A\":\"5x\"},{\"B\":\"5x²\"},{\"C\":\"6x\"},{\"D\":\"6x²\"}]",
                "A", "合并同类项：3x+2x=5x。", 1, "合并同类项", tMath.getId(), 1);
        Long q3 = question(schoolId, "数学", 1, "初二", "下列方程中，是一元一次方程的是（ ）",
                "[{\"A\":\"x²=4\"},{\"B\":\"x+y=3\"},{\"C\":\"2x+1=5\"},{\"D\":\"1/x=2\"}]",
                "C", "一元一次方程只有一个未知数且最高次数为1。", 2, "一元一次方程", tMath.getId(), 1);
        Long q4 = question(schoolId, "数学", 3, "初二", "三角形内角和等于 180 度。（判断）",
                null, "对", "三角形内角和定理。", 1, "三角形内角和", tMath.getId(), 1);
        Long q5 = question(schoolId, "数学", 4, "初二", "函数 y = 2x + 1 在 x = 3 时的值为 ______。",
                null, "7", "代入 x=3 得 y=2×3+1=7。", 2, "一次函数", tMath.getId(), 1);

        // 试卷（已发布）
        ExamPaper paper = new ExamPaper();
        paper.setSchoolId(schoolId);
        paper.setPaperName("初二数学期中测验");
        paper.setSubject("数学");
        paper.setGrade("初二");
        paper.setTotalScore(100);
        paper.setExamTime(40);
        paper.setCreateTeacher(tMath.getId());
        paper.setSource(1);
        paper.setStatus(1);
        paper.setClassIds("[" + c1.getId() + "]");
        examPaperMapper.insert(paper);
        pq(paper.getId(), q1, 20, 1);
        pq(paper.getId(), q2, 20, 2);
        pq(paper.getId(), q3, 20, 3);
        pq(paper.getId(), q4, 20, 4);
        pq(paper.getId(), q5, 20, 5);
    }

    private User user(String username, String pwd, String realName, int role, int level, Long schoolId) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(PasswordUtil.encode(pwd));
        u.setRealName(realName);
        u.setRole(role);
        u.setAccountLevel(level);
        u.setSchoolId(schoolId);
        u.setStatus(1);
        u.setFirstLogin(0);
        u.setCreateTime(LocalDateTime.now());
        userMapper.insert(u);
        return u;
    }

    private ClassInfo cls(String name, String grade, Long headTeacherId, Long schoolId) {
        ClassInfo c = new ClassInfo();
        c.setClassName(name);
        c.setGrade(grade);
        c.setHeadTeacherId(headTeacherId);
        c.setSchoolId(schoolId);
        classInfoMapper.insert(c);
        return c;
    }

    private void job(Long teacherId, Long classId, String subject, int head) {
        TeacherJob j = new TeacherJob();
        j.setTeacherId(teacherId);
        j.setClassId(classId);
        j.setSubject(subject);
        j.setIsHeadTeacher(head);
        teacherJobMapper.insert(j);
    }

    private Long question(Long schoolId, String subject, int type, String grade, String title,
                          String options, String answer, String analysis, int difficulty,
                          String kp, Long teacherId, int source) {
        QuestionBank q = new QuestionBank();
        q.setSchoolId(schoolId);
        q.setSubject(subject);
        q.setQuestionType(type);
        q.setGrade(grade);
        q.setTitle(title);
        q.setOptions(options);
        q.setAnswer(answer);
        q.setAnalysis(analysis);
        q.setDifficulty(difficulty);
        q.setKnowledgePoint(kp);
        q.setCreateTeacher(teacherId);
        q.setSource(source);
        q.setReviewStatus(1);
        questionBankMapper.insert(q);
        return q.getId();
    }

    private void pq(Long paperId, Long questionId, int score, int sort) {
        PaperQuestion p = new PaperQuestion();
        p.setPaperId(paperId);
        p.setQuestionId(questionId);
        p.setScore(score);
        p.setSort(sort);
        paperQuestionMapper.insert(p);
    }
}
