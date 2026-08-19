package com.payverse.ledgerservice.controller;

import com.payverse.ledgerservice.dto.AdminStatsResponse;
import com.payverse.ledgerservice.service.AdminStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminStatsService adminStatsService;

    public AdminController(
            AdminStatsService adminStatsService) {

        this.adminStatsService = adminStatsService;
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminStatsService.getStats();
    }
}
