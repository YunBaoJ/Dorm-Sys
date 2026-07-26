package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.FeeBillService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feeBill")
public class FeeBillController {

    private final FeeBillService feeBillService;
    private final DormManagerScopeService managerScopeService;

    public FeeBillController(FeeBillService feeBillService, DormManagerScopeService managerScopeService) {
        this.feeBillService = feeBillService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result<List<FeeBill>> list(@RequestParam(required = false) Long roomId,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "100") Integer size) {
        List<FeeBill> list = feeBillService.listFeeBillsWithDetails(roomId, status,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<FeeBill> getById(@PathVariable Long id) {
        FeeBill bill = feeBillService.getFeeBillWithCheck(id,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        if (bill == null) return Result.error(403, "无权查看该宿舍账单");
        return Result.success(bill);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody FeeBill feeBill) {
        // Check dormmanager permissions
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            if (feeBill.getId() != null) {
                FeeBill existing = feeBillService.getById(feeBill.getId());
                if (existing == null) return Result.error(403, "无权修改该宿舍账单");
            }
            if (!managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), feeBill.getRoomId())) {
                return Result.error(403, "无权修改该宿舍账单");
            }
        }
        return Result.success(feeBillService.saveOrUpdate(feeBill));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        FeeBill bill = feeBillService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && bill != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), bill.getRoomId())) {
            return Result.error(403, "无权删除该宿舍账单");
        }
        return Result.success(feeBillService.removeById(id));
    }
}
