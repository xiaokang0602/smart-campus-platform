package com.school.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.school.common.PageResult;
import com.school.common.Result;
import com.school.common.UserContext;
import com.school.entity.OperationLog;
import com.school.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogMapper operationLogMapper;

    @GetMapping("/list")
    public Result<PageResult<OperationLog>> list(@RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "20") long pageSize,
                                                 @RequestParam(required = false) String module,
                                                 @RequestParam(required = false) String action) {
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<>();
        if (!UserContext.isSuperAdmin()) {
            qw.eq(OperationLog::getSchoolId, UserContext.schoolId());
        }
        if (module != null && !module.isBlank()) qw.eq(OperationLog::getModule, module);
        if (action != null && !action.isBlank()) qw.eq(OperationLog::getAction, action);
        qw.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> page = operationLogMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        return Result.ok(PageResult.of(page, page.getRecords()));
    }
}
