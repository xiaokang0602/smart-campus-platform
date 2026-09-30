package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.common.*;
import com.school.entity.AdminAccountApply;
import com.school.entity.User;
import com.school.mapper.AdminAccountApplyMapper;
import com.school.mapper.UserMapper;
import com.school.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adminapply")
@RequiredArgsConstructor
public class AdminApplyController {

    private final AdminAccountApplyMapper applyMapper;
    private final UserMapper userMapper;

    /** 校管理员：申请新增后台账号 */
    @PostMapping("/apply")
    @OpLog(module = "account_apply", action = "CREATE", targetType = "admin_account_apply")
    public Result<Long> apply(@RequestBody Map<String, Object> body) {
        AdminAccountApply a = new AdminAccountApply();
        a.setApplySchoolId(UserContext.schoolId());
        a.setApplicantId(UserContext.userId());
        a.setNewUsername((String) body.get("newUsername"));
        a.setNewRealName((String) body.get("newRealName"));
        a.setMenuPerms((String) body.get("menuPerms"));
        a.setApplyReason((String) body.get("reason"));
        a.setAuditStatus(0);
        applyMapper.insert(a);
        return Result.ok(a.getId());
    }

    @GetMapping("/mine")
    public Result<List<AdminAccountApply>> mine() {
        return Result.ok(applyMapper.selectList(new LambdaQueryWrapper<AdminAccountApply>()
                .eq(AdminAccountApply::getApplicantId, UserContext.userId())
                .orderByDesc(AdminAccountApply::getId)));
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> pending() {
        List<AdminAccountApply> list = applyMapper.selectList(new LambdaQueryWrapper<AdminAccountApply>()
                .eq(AdminAccountApply::getAuditStatus, 0)
                .orderByDesc(AdminAccountApply::getId));
        return Result.ok(list.stream().map(this::row).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/audit")
    @OpLog(module = "account_apply", action = "APPROVE", targetType = "admin_account_apply")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = (Integer) body.getOrDefault("status", 1);
        String comment = (String) body.getOrDefault("comment", "");
        AdminAccountApply a = applyMapper.selectById(id);
        a.setAuditStatus(status);
        a.setAuditComment(comment);
        a.setAuditUser(UserContext.userId());
        applyMapper.updateById(a);

        if (status == 1) {
            User exists = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, a.getNewUsername()));
            if (exists == null) {
                User u = new User();
                u.setUsername(a.getNewUsername());
                u.setPassword(PasswordUtil.encode("123456"));
                u.setRealName(a.getNewRealName());
                u.setRole(4);
                u.setAccountLevel(3);
                u.setSchoolId(a.getApplySchoolId());
                u.setStatus(1);
                u.setFirstLogin(1);
                userMapper.insert(u);
            }
        }
        return Result.ok();
    }

    private Map<String, Object> row(AdminAccountApply a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("applySchoolId", a.getApplySchoolId());
        m.put("newUsername", a.getNewUsername());
        m.put("newRealName", a.getNewRealName());
        m.put("menuPerms", a.getMenuPerms());
        m.put("reason", a.getApplyReason());
        m.put("auditStatus", a.getAuditStatus());
        m.put("auditComment", a.getAuditComment());
        return m;
    }
}
