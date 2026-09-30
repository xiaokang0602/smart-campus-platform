package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.*;
import com.school.entity.Student;
import com.school.entity.User;
import com.school.mapper.StudentMapper;
import com.school.mapper.UserMapper;
import com.school.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(defaultValue = "1") long pageNum,
                                                        @RequestParam(defaultValue = "10") long pageSize,
                                                        @RequestParam(required = false) Integer role,
                                                        @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (!UserContext.isSuperAdmin()) {
            qw.eq(User::getSchoolId, UserContext.schoolId());
        }
        if (role != null) qw.eq(User::getRole, role);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(User::getRealName, keyword).or().like(User::getUsername, keyword));
        }
        qw.orderByAsc(User::getRole).orderByDesc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : page.getRecords()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("role", u.getRole());
            m.put("accountLevel", u.getAccountLevel());
            m.put("schoolId", u.getSchoolId());
            m.put("phone", u.getPhone());
            m.put("status", u.getStatus());
            m.put("createTime", u.getCreateTime());
            rows.add(m);
        }
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), rows));
    }

    @PostMapping("/add")
    @OpLog(module = "user", action = "CREATE", targetType = "user")
    public Result<Long> add(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exists != null) {
            throw new BusinessException("账号已存在");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(PasswordUtil.encode((String) body.getOrDefault("password", "123456")));
        u.setRealName((String) body.get("realName"));
        u.setRole((Integer) body.get("role"));
        u.setAccountLevel(body.get("accountLevel") == null ? 0 : (Integer) body.get("accountLevel"));
        u.setSchoolId(UserContext.schoolId());
        u.setPhone((String) body.get("phone"));
        u.setStatus(1);
        u.setFirstLogin(1);
        userMapper.insert(u);

        if (u.getRole() == 1) {
            Student s = new Student();
            s.setId(u.getId());
            s.setClassId(body.get("classId") == null ? null : Long.valueOf(String.valueOf(body.get("classId"))));
            s.setStudentNo((String) body.get("studentNo"));
            s.setDuty("无");
            s.setGender((String) body.get("gender"));
            studentMapper.insert(s);
        }
        return Result.ok(u.getId());
    }

    @PutMapping("/{id}")
    @OpLog(module = "user", action = "UPDATE", targetType = "user")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = userMapper.selectById(id);
        if (body.get("realName") != null) u.setRealName((String) body.get("realName"));
        if (body.get("phone") != null) u.setPhone((String) body.get("phone"));
        userMapper.updateById(u);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @OpLog(module = "user", action = "UPDATE", targetType = "user")
    public Result<Void> status(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = userMapper.selectById(id);
        u.setStatus((Integer) body.getOrDefault("status", 0));
        userMapper.updateById(u);
        return Result.ok();
    }

    @PutMapping("/{id}/resetPassword")
    @OpLog(module = "user", action = "UPDATE", targetType = "user")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = userMapper.selectById(id);
        u.setPassword(PasswordUtil.encode((String) body.getOrDefault("password", "123456")));
        u.setFirstLogin(1);
        userMapper.updateById(u);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @OpLog(module = "user", action = "DELETE", targetType = "user")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        studentMapper.deleteById(id);
        return Result.ok();
    }
}
