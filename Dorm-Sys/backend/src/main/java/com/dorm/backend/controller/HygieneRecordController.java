package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.HygieneRecord;
import com.dorm.backend.service.HygieneRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hygieneRecord")
public class HygieneRecordController {

    private final HygieneRecordService hygieneRecordService;
    private final DormManagerScopeService managerScopeService;

    public HygieneRecordController(HygieneRecordService hygieneRecordService, DormManagerScopeService managerScopeService) {
        this.hygieneRecordService = hygieneRecordService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result<List<HygieneRecord>> list(@RequestParam(required = false) Long roomId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "100") Integer size) {
        List<HygieneRecord> list = hygieneRecordService.listHygieneRecordsWithDetails(roomId,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<HygieneRecord> getById(@PathVariable Long id) {
        HygieneRecord record = hygieneRecordService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), record.getRoomId())) {
            return Result.error(403, "无权查看该卫生记录");
        }
        return Result.success(record);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody HygieneRecord hygieneRecord) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && hygieneRecord.getId() != null) {
            HygieneRecord existing = hygieneRecordService.getById(hygieneRecord.getId());
            if (existing == null
                    || !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), existing.getRoomId())) {
                return Result.error(403, "无权修改该卫生记录");
            }
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), hygieneRecord.getRoomId())) {
            return Result.error(403, "无权修改该卫生记录");
        }
        return Result.success(hygieneRecordService.saveOrUpdate(hygieneRecord));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        HygieneRecord record = hygieneRecordService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), record.getRoomId())) {
            return Result.error(403, "无权删除该卫生记录");
        }
        return Result.success(hygieneRecordService.removeById(id));
    }
}
