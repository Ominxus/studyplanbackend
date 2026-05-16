package com.studentplansystem.studyplangym.controller;

import com.studentplansystem.studyplangym.entity.AuditLog;
import com.studentplansystem.studyplangym.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public List<AuditLog> getLatestAuditLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }
}