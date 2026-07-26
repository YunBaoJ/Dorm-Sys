package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.OperationLog;
import com.dorm.backend.service.OperationLogService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operationLog")
public class OperationLogController {
    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping("/list")
    public Result<List<OperationLog>> list(@RequestParam(required = false) String module,
                                           @RequestParam(required = false) String result,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "100") Integer size) {
        if (!"admin".equals(AuthUtils.getCurrentUserRole())) return Result.error(403, "仅管理员可查看操作日志");
        QueryWrapper<OperationLog> query = new QueryWrapper<>();
        if (StringUtils.hasText(module)) query.eq("module", module);
        if (StringUtils.hasText(result)) query.eq("result", result);
        if (StringUtils.hasText(keyword)) query.and(item -> item.like("operator_name", keyword).or().like("summary", keyword));
        Page<OperationLog> pageResult = operationLogService.page(new Page<>(page, size), query.orderByDesc("create_time"));
        return Result.success(pageResult.getRecords());
    }
}
