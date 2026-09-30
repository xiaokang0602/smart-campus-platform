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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 初始化种子数据：仅当 user 表为空时写入（学校、班级、学生、教师任职、课表）。
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
            log.info("种子数据初始化完成");
        } catch (Exception e) {
            log.error("种子数据初始化失败", e);
        }
    }

    private void seed() {
        // 学校（高中）
        School school = new School();
        school.setSchoolName("第一高级中学");
        school.setAddress("北京市海淀区学府路 1 号");
        schoolMapper.insert(school);
        Long schoolId = school.getId();

        // 超管 + 校管理员
        user("admin", superAdminPassword, "平台超级管理员", 4, 1, 0L);
        user("schooladmin", defaultPassword, "李明辉", 4, 2, schoolId);

        // 教师（两位班主任 + 七位任课教师）
        User tMath = user("wangjianguo", defaultPassword, "王建国", 3, 0, schoolId);
        User tChinese = user("lixiulan", defaultPassword, "李秀兰", 3, 0, schoolId);
        User tEnglish = user("liuyang", defaultPassword, "刘洋", 2, 0, schoolId);
        User tPhysics = user("chenming", defaultPassword, "陈明", 2, 0, schoolId);
        User tPolitics = user("zhaoguoqiang", defaultPassword, "赵国强", 2, 0, schoolId);
        User tHistory = user("sunlimei", defaultPassword, "孙丽梅", 2, 0, schoolId);
        User tGeography = user("zhouhaitao", defaultPassword, "周海涛", 2, 0, schoolId);
        User tChemistry = user("wuxueqin", defaultPassword, "吴雪琴", 2, 0, schoolId);
        User tBiology = user("zhengyawen", defaultPassword, "郑雅文", 2, 0, schoolId);

        // 两个班级（文科班 + 理科班）
        ClassInfo c1 = cls("文科班", "高一", tChinese.getId(), schoolId);
        ClassInfo c2 = cls("理科班", "高一", tMath.getId(), schoolId);
        ClassInfo[] classes = {c1, c2};

        // 学生（每班 20 人，共 40 人）
        String[] surnames = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴",
                "徐", "孙", "马", "朱", "胡", "郭", "何", "高", "林", "罗"};
        String[] givenNames = {"子豪", "文轩", "雨彤", "思涵", "志强", "晓燕", "欣怡", "嘉铭", "俊杰", "宇琪",
                "浩然", "静怡", "建国", "丽华", "永强", "雪梅", "明辉", "桂英", "建华", "秀兰",
                "国栋", "玉兰", "文博", "雅静", "天佑", "梦洁", "立军", "慧敏", "志远", "若曦",
                "冠宇", "淑芬", "睿泽", "曼婷", "昊然", "洁茹", "泽宇", "婉婷", "俊熙", "诗涵"};
        String[] duties = {"班长", "学习委员", "语文课代表", "数学课代表", "英语课代表"};

        for (int ci = 0; ci < classes.length; ci++) {
            for (int s = 0; s < 20; s++) {
                int i = ci * 20 + s;
                User u = user(String.format("stu%03d", i + 1), defaultPassword,
                        surnames[i % 20] + givenNames[i], 1, 0, schoolId);
                Student st = new Student();
                st.setId(u.getId());
                st.setClassId(classes[ci].getId());
                st.setStudentNo(String.format("2026%02d%02d", ci + 1, s + 1));
                st.setDuty(s < duties.length ? duties[s] : "无");
                st.setGender(i % 2 == 0 ? "男" : "女");
                st.setBirthday(LocalDate.of(2010, (i % 12) + 1, (i % 27) + 1));
                st.setArchiveNote("");
                studentMapper.insert(st);
            }
        }

        // 教师任职
        job(tMath.getId(), c2.getId(), "数学", 1);   // 王建国：理科班班主任
        job(tMath.getId(), c1.getId(), "数学", 0);
        job(tChinese.getId(), c1.getId(), "语文", 1); // 李秀兰：文科班班主任
        job(tChinese.getId(), c2.getId(), "语文", 0);
        job(tEnglish.getId(), c1.getId(), "英语", 0);
        job(tEnglish.getId(), c2.getId(), "英语", 0);
        job(tPhysics.getId(), c2.getId(), "物理", 0);
        job(tPolitics.getId(), c1.getId(), "政治", 0);
        job(tHistory.getId(), c1.getId(), "历史", 0);
        job(tGeography.getId(), c1.getId(), "地理", 0);
        job(tChemistry.getId(), c2.getId(), "化学", 0);
        job(tBiology.getId(), c2.getId(), "生物", 0);

        // 课表（两个班，周一~周五 每天 7 节：上午 4 节 + 下午 3 节；文科班政史地、理科班物化生）
        Map<String, Long> subjectTeacher = new LinkedHashMap<>();
        subjectTeacher.put("语文", tChinese.getId());
        subjectTeacher.put("数学", tMath.getId());
        subjectTeacher.put("英语", tEnglish.getId());
        subjectTeacher.put("政治", tPolitics.getId());
        subjectTeacher.put("历史", tHistory.getId());
        subjectTeacher.put("地理", tGeography.getId());
        subjectTeacher.put("物理", tPhysics.getId());
        subjectTeacher.put("化学", tChemistry.getId());
        subjectTeacher.put("生物", tBiology.getId());
        String[] artsSubjects = {"语文", "数学", "英语", "政治", "历史", "地理"};
        String[] sciSubjects = {"语文", "数学", "英语", "物理", "化学", "生物"};
        String[][] subjectPlan = {artsSubjects, sciSubjects};
        for (int ci = 0; ci < classes.length; ci++) {
            String[] subjects = subjectPlan[ci];
            for (int w = 0; w < 5; w++) {
                for (int p = 0; p < 7; p++) {
                    String subject = subjects[(w + p) % subjects.length];
                    Timetable t = new Timetable();
                    t.setClassId(classes[ci].getId());
                    t.setWeek(w + 1);
                    t.setPeriod(p + 1);
                    t.setSubject(subject);
                    t.setTeacherId(subjectTeacher.get(subject));
                    t.setRoom("教学楼" + classes[ci].getId() + "层" + (w + 1) + "0" + (p + 1));
                    timetableMapper.insert(t);
                }
            }
        }
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
}
