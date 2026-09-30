package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.ClassInfo;
import com.school.entity.TeacherJob;
import com.school.entity.User;
import com.school.mapper.ClassInfoMapper;
import com.school.mapper.TeacherJobMapper;
import com.school.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacherjob")
@RequiredArgsConstructor
public class TeacherJobController {

    private final TeacherJobMapper teacherJobMapper;
    private final UserMapper userMapper;
    private final ClassInfoMapper classInfoMapper;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<TeacherJob> jobs = teacherJobMapper.selectList(new LambdaQueryWrapper<TeacherJob>()
                .orderByDesc(TeacherJob::getIsHeadTeacher));
        return Result.ok(jobs.stream().map(this::row).collect(Collectors.toList()));
    }

    @PostMapping("/save")
    @OpLog(module = "teacher_job", action = "CREATE", targetType = "teacher_job")
    public Result<Long> save(@RequestBody Map<String, Object> body) {
        TeacherJob j = new TeacherJob();
        j.setTeacherId(Long.valueOf(String.valueOf(body.get("teacherId"))));
        j.setClassId(Long.valueOf(String.valueOf(body.get("classId"))));
        j.setSubject((String) body.get("subject"));
        j.setIsHeadTeacher((Integer) body.getOrDefault("isHeadTeacher", 0));
        teacherJobMapper.insert(j);
        return Result.ok(j.getId());
    }

    @PutMapping("/{id}")
    @OpLog(module = "teacher_job", action = "UPDATE", targetType = "teacher_job")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TeacherJob j = teacherJobMapper.selectById(id);
        if (body.get("subject") != null) j.setSubject((String) body.get("subject"));
        if (body.get("classId") != null) j.setClassId(Long.valueOf(String.valueOf(body.get("classId"))));
        if (body.get("isHeadTeacher") != null) j.setIsHeadTeacher((Integer) body.get("isHeadTeacher"));
        teacherJobMapper.updateById(j);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OpLog(module = "teacher_job", action = "DELETE", targetType = "teacher_job")
    public Result<Void> delete(@PathVariable Long id) {
        teacherJobMapper.deleteById(id);
        return Result.ok();
    }

    private Map<String, Object> row(TeacherJob j) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", j.getId());
        m.put("teacherId", j.getTeacherId());
        m.put("classId", j.getClassId());
        m.put("subject", j.getSubject());
        m.put("isHeadTeacher", j.getIsHeadTeacher());
        User u = userMapper.selectById(j.getTeacherId());
        m.put("teacherName", u == null ? "" : u.getRealName());
        ClassInfo c = classInfoMapper.selectById(j.getClassId());
        m.put("className", c == null ? "" : c.getClassName());
        return m;
    }
}
