package com.studentplansystem.studyplangym.service;

import com.studentplansystem.studyplangym.entity.AuditLog;
import com.studentplansystem.studyplangym.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Authentication authentication, String action, String details) {
        AuditLog auditLog = new AuditLog();

        if (authentication != null) {
            auditLog.setUsername(authentication.getName());

            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .orElse("UNKNOWN");

            auditLog.setRole(role);
        } else {
            auditLog.setUsername("UNKNOWN");
            auditLog.setRole("UNKNOWN");
        }

        auditLog.setAction(action);
        auditLog.setDetails(details);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }
}