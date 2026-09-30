package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.*;
import com.school.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableMapper timetableMapper;
    private final StudentMapper studentMapper;
    private final TeacherJobMapper teacherJobMapper;
    private final ClassInfoMapper classInfoMapper;
    private final UserMapper userMapper;

    /** 我的课表：学生按本班，教师按任课班级 */
    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> mine() {
        UserContext ctx = UserContext.get();
        Set<Long> classIds = new LinkedHashSet<>();
        if (ctx.getRole() == 1) {
            Student s = studentMapper.selectById(ctx.getUserId());
            if (s != null && s.getClassId() != null) {
                classIds.add(s.getClassId());
            }
        } else {
            teacherJobMapper.selectList(new LambdaQueryWrapper<TeacherJob>()
                    .eq(TeacherJob::getTeacherId, ctx.getUserId()))
                    .forEach(j -> classIds.add(j.getClassId()));
        }
        return Result.ok(build(classIds));
    }

    @GetMapping("/class/{classId}")
    public Result<List<Map<String, Object>>> byClass(@PathVariable Long classId) {
        return Result.ok(build(Set.of(classId)));
    }

    @PostMapping("/save")
    @OpLog(module = "timetable", action = "UPDATE", targetType = "timetable")
    public Result<Void> save(@RequestBody Map<String, Object> body) {
        Long classId = Long.valueOf(String.valueOf(body.get("classId")));
        timetableMapper.delete(new LambdaQueryWrapper<Timetable>().eq(Timetable::getClassId, classId));
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items != null) {
            for (Map<String, Object> it : items) {
                Timetable t = new Timetable();
                t.setClassId(classId);
                t.setWeek(Integer.valueOf(String.valueOf(it.get("week"))));
                t.setPeriod(Integer.valueOf(String.valueOf(it.get("period"))));
                t.setSubject((String) it.get("subject"));
                t.setRoom((String) it.get("room"));
                if (it.get("teacherId") != null) {
                    t.setTeacherId(Long.valueOf(String.valueOf(it.get("teacherId"))));
                }
                timetableMapper.insert(t);
            }
        }
        return Result.ok();
    }

    private List<Map<String, Object>> build(Set<Long> classIds) {
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Timetable> list = timetableMapper.selectList(new LambdaQueryWrapper<Timetable>()
                .in(Timetable::getClassId, classIds)
                .orderByAsc(Timetable::getWeek).orderByAsc(Timetable::getPeriod));
        return list.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("classId", t.getClassId());
            m.put("week", t.getWeek());
            m.put("period", t.getPeriod());
            m.put("subject", t.getSubject());
            m.put("room", t.getRoom());
            m.put("teacherId", t.getTeacherId());
            ClassInfo c = classInfoMapper.selectById(t.getClassId());
            m.put("className", c == null ? "" : c.getClassName());
            if (t.getTeacherId() != null) {
                User u = userMapper.selectById(t.getTeacherId());
                m.put("teacherName", u == null ? "" : u.getRealName());
            } else {
                m.put("teacherName", "");
            }
            return m;
        }).collect(Collectors.toList());
    }
}
