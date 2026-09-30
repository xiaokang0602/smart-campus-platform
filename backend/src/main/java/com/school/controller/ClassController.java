package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.ClassInfo;
import com.school.entity.User;
import com.school.mapper.ClassInfoMapper;
import com.school.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassInfoMapper classInfoMapper;
    private final UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<ClassInfo> list = classInfoMapper.selectList(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getSchoolId, UserContext.schoolId())
                .orderByAsc(ClassInfo::getId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ClassInfo c : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("className", c.getClassName());
            m.put("grade", c.getGrade());
            m.put("headTeacherId", c.getHeadTeacherId());
            User ht = c.getHeadTeacherId() == null ? null : userMapper.selectById(c.getHeadTeacherId());
            m.put("headTeacherName", ht == null ? "" : ht.getRealName());
            rows.add(m);
        }
        return Result.ok(rows);
    }

    @PostMapping("/add")
    @OpLog(module = "class", action = "CREATE", targetType = "class")
    public Result<Long> add(@RequestBody Map<String, Object> body) {
        ClassInfo c = new ClassInfo();
        c.setClassName((String) body.get("className"));
        c.setGrade((String) body.get("grade"));
        c.setSchoolId(UserContext.schoolId());
        if (body.get("headTeacherId") != null) {
            c.setHeadTeacherId(Long.valueOf(String.valueOf(body.get("headTeacherId"))));
        }
        classInfoMapper.insert(c);
        return Result.ok(c.getId());
    }

    @PutMapping("/{id}")
    @OpLog(module = "class", action = "UPDATE", targetType = "class")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ClassInfo c = classInfoMapper.selectById(id);
        if (body.get("className") != null) c.setClassName((String) body.get("className"));
        if (body.get("grade") != null) c.setGrade((String) body.get("grade"));
        if (body.containsKey("headTeacherId")) {
            c.setHeadTeacherId(body.get("headTeacherId") == null ? null
                    : Long.valueOf(String.valueOf(body.get("headTeacherId"))));
        }
        classInfoMapper.updateById(c);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OpLog(module = "class", action = "DELETE", targetType = "class")
    public Result<Void> delete(@PathVariable Long id) {
        classInfoMapper.deleteById(id);
        return Result.ok();
    }
}
