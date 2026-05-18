package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.entity.MaintenanceStatus;
import com.studentplansystem.studyplangym.service.AuditLogService;
import com.studentplansystem.studyplangym.service.MaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final AuditLogService auditLogService;

    public MaintenanceController(
            MaintenanceService maintenanceService,
            AuditLogService auditLogService
    ) {
        this.maintenanceService = maintenanceService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/status")
    public MaintenanceStatus getStatus() {
        return maintenanceService.getStatus();
    }

    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(
            @RequestBody MaintenanceStatus updatedStatus,
            Authentication authentication
    ) {
        MaintenanceStatus savedStatus = maintenanceService.updateStatus(updatedStatus);

        auditLogService.log(
                authentication,
                savedStatus.isEnabled() ? "ENABLE_MAINTENANCE_MODE" : "DISABLE_MAINTENANCE_MODE",
                savedStatus.isEnabled()
                        ? "Maintenance mode enabled"
                        : "Maintenance mode disabled"
        );

        return ResponseEntity.ok(savedStatus);
    }
}