package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.ClassInfo;
import com.school.entity.TeacherChangeApply;
import com.school.entity.TeacherJob;
import com.school.entity.User;
import com.school.mapper.ClassInfoMapper;
import com.school.mapper.TeacherChangeApplyMapper;
import com.school.mapper.TeacherJobMapper;
import com.school.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacherchange")
@RequiredArgsConstructor
public class TeacherChangeController {

    private final TeacherChangeApplyMapper applyMapper;
    private final TeacherJobMapper teacherJobMapper;
    private final UserMapper userMapper;
    private final ClassInfoMapper classInfoMapper;

    @PostMapping("/apply")
    @OpLog(module = "teacher_change", action = "CREATE", targetType = "teacher_change_apply")
    public Result<Long> apply(@RequestBody Map<String, Object> body) {
        TeacherChangeApply a = new TeacherChangeApply();
        a.setApplyClassId(Long.valueOf(String.valueOf(body.get("classId"))));
        a.setSubject((String) body.get("subject"));
        a.setOldTeacherId(Long.valueOf(String.valueOf(body.get("oldTeacherId"))));
        a.setNewTeacherId(Long.valueOf(String.valueOf(body.get("newTeacherId"))));
        a.setApplyUser(UserContext.userId());
        a.setApplyReason((String) body.get("reason"));
        a.setAuditStatus(0);
        applyMapper.insert(a);
        return Result.ok(a.getId());
    }

    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> mine() {
        List<TeacherChangeApply> list = applyMapper.selectList(new LambdaQueryWrapper<TeacherChangeApply>()
                .eq(TeacherChangeApply::getApplyUser, UserContext.userId())
                .orderByDesc(TeacherChangeApply::getId));
        return Result.ok(list.stream().map(this::row).collect(Collectors.toList()));
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> pending() {
        List<TeacherChangeApply> list = applyMapper.selectList(new LambdaQueryWrapper<TeacherChangeApply>()
                .eq(TeacherChangeApply::getAuditStatus, 0)
                .orderByDesc(TeacherChangeApply::getId));
        return Result.ok(list.stream().map(this::row).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/audit")
    @OpLog(module = "teacher_change", action = "APPROVE", targetType = "teacher_change_apply")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = (Integer) body.getOrDefault("status", 1);
        String comment = (String) body.getOrDefault("comment", "");
        TeacherChangeApply a = applyMapper.selectById(id);
        a.setAuditStatus(status);
        a.setAuditComment(comment);
        applyMapper.updateById(a);

        // 通过后更新教师任职
        if (status == 1) {
            TeacherJob old = teacherJobMapper.selectOne(new LambdaQueryWrapper<TeacherJob>()
                    .eq(TeacherJob::getTeacherId, a.getOldTeacherId())
                    .eq(TeacherJob::getClassId, a.getApplyClassId())
                    .eq(TeacherJob::getSubject, a.getSubject()));
            if (old != null) {
                old.setTeacherId(a.getNewTeacherId());
                teacherJobMapper.updateById(old);
            }
        }
        return Result.ok();
    }

    private Map<String, Object> row(TeacherChangeApply a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("classId", a.getApplyClassId());
        m.put("subject", a.getSubject());
        m.put("oldTeacherId", a.getOldTeacherId());
        m.put("newTeacherId", a.getNewTeacherId());
        m.put("reason", a.getApplyReason());
        m.put("auditStatus", a.getAuditStatus());
        m.put("auditComment", a.getAuditComment());
        User o = userMapper.selectById(a.getOldTeacherId());
        User n = userMapper.selectById(a.getNewTeacherId());
        m.put("oldTeacherName", o == null ? "" : o.getRealName());
        m.put("newTeacherName", n == null ? "" : n.getRealName());
        ClassInfo c = classInfoMapper.selectById(a.getApplyClassId());
        m.put("className", c == null ? "" : c.getClassName());
        return m;
    }
}
