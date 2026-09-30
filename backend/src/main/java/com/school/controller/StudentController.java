package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.*;
import com.school.entity.*;
import com.school.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final ClassInfoMapper classInfoMapper;
    private final StudentAwardMapper studentAwardMapper;
    private final TeacherJobMapper teacherJobMapper;

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(defaultValue = "1") long pageNum,
                                                        @RequestParam(defaultValue = "20") long pageSize,
                                                        @RequestParam(required = false) Long classId,
                                                        @RequestParam(required = false) String keyword) {
        UserContext ctx = UserContext.get();
        Set<Long> allowedClassIds = allowedClasses(ctx);

        LambdaQueryWrapper<Student> qw = new LambdaQueryWrapper<>();
        if (!allowedClassIds.isEmpty()) {
            qw.in(Student::getClassId, allowedClassIds);
        }
        if (classId != null) {
            qw.eq(Student::getClassId, classId);
        }
        Page<Student> page = studentMapper.selectPage(new Page<>(pageNum, pageSize), qw);

        List<Map<String, Object>> rows = page.getRecords().stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            User u = userMapper.selectById(s.getId());
            ClassInfo c = classInfoMapper.selectById(s.getClassId());
            m.put("id", s.getId());
            m.put("studentNo", s.getStudentNo());
            m.put("realName", u == null ? "" : u.getRealName());
            m.put("username", u == null ? "" : u.getUsername());
            m.put("gender", s.getGender());
            m.put("duty", s.getDuty());
            m.put("className", c == null ? "" : c.getClassName());
            m.put("classId", s.getClassId());
            m.put("birthday", s.getBirthday());
            return m;
        }).collect(Collectors.toList());

        if (keyword != null && !keyword.isBlank()) {
            rows = rows.stream().filter(r ->
                    r.get("realName").toString().contains(keyword) ||
                            r.get("studentNo").toString().contains(keyword)).collect(Collectors.toList());
        }
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), rows));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Student s = studentMapper.selectById(id);
        User u = userMapper.selectById(id);
        ClassInfo c = s == null ? null : classInfoMapper.selectById(s.getClassId());
        List<StudentAward> awards = studentAwardMapper.selectList(new LambdaQueryWrapper<StudentAward>()
                .eq(StudentAward::getStudentId, id).orderByDesc(StudentAward::getAwardTime));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("realName", u == null ? "" : u.getRealName());
        m.put("username", u == null ? "" : u.getUsername());
        m.put("studentNo", s == null ? "" : s.getStudentNo());
        m.put("gender", s == null ? "" : s.getGender());
        m.put("birthday", s == null ? null : s.getBirthday());
        m.put("duty", s == null ? "" : s.getDuty());
        m.put("archiveNote", s == null ? "" : s.getArchiveNote());
        m.put("className", c == null ? "" : c.getClassName());
        m.put("awards", awards);
        return Result.ok(m);
    }

    @PutMapping("/{id}")
    @OpLog(module = "student", action = "UPDATE", targetType = "student")
    public Result<Void> update(@PathVariable Long id, @RequestBody Student body) {
        Student s = studentMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("学生不存在");
        }
        s.setArchiveNote(body.getArchiveNote());
        s.setDuty(body.getDuty());
        s.setBirthday(body.getBirthday());
        s.setGender(body.getGender());
        studentMapper.updateById(s);
        return Result.ok();
    }

    @PostMapping("/{id}/award")
    @OpLog(module = "award", action = "CREATE", targetType = "student_award")
    public Result<Void> addAward(@PathVariable Long id, @RequestBody StudentAward body) {
        body.setId(null);
        body.setStudentId(id);
        body.setCreateBy(UserContext.userId());
        if (body.getAwardTime() == null) {
            body.setAwardTime(LocalDate.now());
        }
        studentAwardMapper.insert(body);
        return Result.ok();
    }

    @DeleteMapping("/award/{awardId}")
    @OpLog(module = "award", action = "DELETE", targetType = "student_award")
    public Result<Void> deleteAward(@PathVariable Long awardId) {
        studentAwardMapper.deleteById(awardId);
        return Result.ok();
    }

    /** 当前角色可看的学生班级范围 */
    private Set<Long> allowedClasses(UserContext ctx) {
        Set<Long> ids = new LinkedHashSet<>();
        if (ctx.getRole() == 4) {
            return ids; // 后台可看全部，空集合表示不过滤
        }
        teacherJobMapper.selectList(new LambdaQueryWrapper<TeacherJob>()
                .eq(TeacherJob::getTeacherId, ctx.getUserId()))
                .forEach(j -> ids.add(j.getClassId()));
        return ids;
    }
}
