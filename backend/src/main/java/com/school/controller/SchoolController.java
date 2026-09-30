package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.School;
import com.school.entity.User;
import com.school.mapper.SchoolMapper;
import com.school.mapper.UserMapper;
import com.school.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolMapper schoolMapper;
    private final UserMapper userMapper;

    @GetMapping("/info")
    public Result<School> info() {
        School s = schoolMapper.selectById(UserContext.schoolId());
        return Result.ok(s);
    }

    @PutMapping("/info")
    @OpLog(module = "school", action = "UPDATE", targetType = "school")
    public Result<Void> updateInfo(@RequestBody School body) {
        School s = schoolMapper.selectById(UserContext.schoolId());
        if (s != null) {
            s.setSchoolName(body.getSchoolName());
            s.setAddress(body.getAddress());
            schoolMapper.updateById(s);
        }
        return Result.ok();
    }

    /** 超管：全部学校列表（含学生数/教师数） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<School> schools = schoolMapper.selectList(new LambdaQueryWrapper<School>());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (School s : schools) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("schoolName", s.getSchoolName());
            m.put("address", s.getAddress());
            m.put("studentCount", userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getSchoolId, s.getId()).eq(User::getRole, 1)));
            m.put("teacherCount", userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getSchoolId, s.getId()).in(User::getRole, 2, 3)));
            rows.add(m);
        }
        return Result.ok(rows);
    }

    @PostMapping("/create")
    @OpLog(module = "school", action = "CREATE", targetType = "school")
    public Result<Long> create(@RequestBody Map<String, Object> body) {
        School s = new School();
        s.setSchoolName((String) body.get("schoolName"));
        s.setAddress((String) body.get("address"));
        schoolMapper.insert(s);

        // 同时创建该校首个校管理员
        User admin = new User();
        admin.setUsername((String) body.getOrDefault("adminUsername", "admin_" + s.getId()));
        admin.setPassword(PasswordUtil.encode((String) body.getOrDefault("adminPassword", "123456")));
        admin.setRealName((String) body.getOrDefault("adminRealName", "校管理员"));
        admin.setRole(4);
        admin.setAccountLevel(2);
        admin.setSchoolId(s.getId());
        admin.setStatus(1);
        admin.setFirstLogin(1);
        userMapper.insert(admin);
        return Result.ok(s.getId());
    }

    @PutMapping("/{id}/disable")
    @OpLog(module = "school", action = "UPDATE", targetType = "school")
    public Result<Void> disable(@PathVariable Long id) {
        userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getSchoolId, id))
                .forEach(u -> {
                    u.setStatus(0);
                    userMapper.updateById(u);
                });
        return Result.ok();
    }
}
