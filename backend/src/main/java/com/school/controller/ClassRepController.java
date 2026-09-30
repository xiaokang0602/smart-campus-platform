package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.ClassRep;
import com.school.entity.Student;
import com.school.entity.TeacherJob;
import com.school.entity.User;
import com.school.mapper.ClassRepMapper;
import com.school.mapper.StudentMapper;
import com.school.mapper.TeacherJobMapper;
import com.school.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classrep")
@RequiredArgsConstructor
public class ClassRepController {

    private final ClassRepMapper classRepMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final TeacherJobMapper teacherJobMapper;

    /** 教师：我提交的课代表申请 */
    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> mine() {
        List<ClassRep> list = classRepMapper.selectList(new LambdaQueryWrapper<ClassRep>()
                .eq(ClassRep::getTeacherId, UserContext.userId())
                .orderByDesc(ClassRep::getCreateTime));
        return Result.ok(list.stream().map(this::row).collect(Collectors.toList()));
    }

    /** 教师提交课代表申请 */
    @PostMapping("/apply")
    @OpLog(module = "class_rep", action = "CREATE", targetType = "class_rep")
    public Result<Long> apply(@RequestBody Map<String, Object> body) {
        ClassRep rep = new ClassRep();
        rep.setStudentId(Long.valueOf(String.valueOf(body.get("studentId"))));
        rep.setTeacherId(UserContext.userId());
        rep.setSubject((String) body.get("subject"));
        rep.setClassId(Long.valueOf(String.valueOf(body.get("classId"))));
        rep.setAuditStatus(0);
        rep.setCreateTime(LocalDateTime.now());
        classRepMapper.insert(rep);
        return Result.ok(rep.getId());
    }

    /** 班主任：本班待审批课代表 */
    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> pending() {
        // 班主任本人任教班级
        Set<Long> classIds = teacherJobMapper.selectList(new LambdaQueryWrapper<TeacherJob>()
                        .eq(TeacherJob::getTeacherId, UserContext.userId()))
                .stream().map(TeacherJob::getClassId).collect(Collectors.toSet());
        List<ClassRep> list = classRepMapper.selectList(new LambdaQueryWrapper<ClassRep>()
                .eq(ClassRep::getAuditStatus, 0)
                .orderByDesc(ClassRep::getCreateTime));
        List<ClassRep> filtered = list.stream().filter(r -> classIds.contains(r.getClassId())).collect(Collectors.toList());
        return Result.ok(filtered.stream().map(this::row).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/audit")
    @OpLog(module = "class_rep", action = "APPROVE", targetType = "class_rep")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = (Integer) body.getOrDefault("status", 1);
        ClassRep rep = classRepMapper.selectById(id);
        rep.setAuditStatus(status);
        rep.setAuditTime(LocalDateTime.now());
        classRepMapper.updateById(rep);

        // 通过后更新学生任职
        if (status == 1) {
            Student s = studentMapper.selectById(rep.getStudentId());
            if (s != null && "无".equals(s.getDuty())) {
                s.setDuty(rep.getSubject() + "课代表");
                studentMapper.updateById(s);
            }
        }
        return Result.ok();
    }

    private Map<String, Object> row(ClassRep r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("studentId", r.getStudentId());
        m.put("subject", r.getSubject());
        m.put("classId", r.getClassId());
        m.put("auditStatus", r.getAuditStatus());
        m.put("createTime", r.getCreateTime());
        User stu = userMapper.selectById(r.getStudentId());
        m.put("studentName", stu == null ? "" : stu.getRealName());
        User tea = userMapper.selectById(r.getTeacherId());
        m.put("teacherName", tea == null ? "" : tea.getRealName());
        return m;
    }
}
