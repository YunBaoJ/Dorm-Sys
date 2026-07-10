package com.dorm.backend.controller;

import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.service.TransferRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transfer-request")
public class TransferRequestController {

    @Autowired
    private TransferRequestService transferRequestService;

    @GetMapping("/list")
    public List<TransferRequest> list() {
        return transferRequestService.list();
    }

    @GetMapping("/{id}")
    public TransferRequest getById(@PathVariable Long id) {
        return transferRequestService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody TransferRequest transferRequest) {
        return transferRequestService.saveOrUpdate(transferRequest);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return transferRequestService.removeById(id);
    }
}
