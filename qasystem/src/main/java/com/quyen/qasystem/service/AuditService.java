package com.quyen.qasystem.service;

import com.quyen.qasystem.entity.AuditLog;
import com.quyen.qasystem.entity.User;
import com.quyen.qasystem.enums.AuditAction;
import com.quyen.qasystem.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(
            User user,
            AuditAction action,
            String targetType,
            Long targetId,
            String description
    ) {
        auditLogRepository.save(
                AuditLog.builder()
                        .user(user)
                        .action(action)
                        .targetType(targetType)
                        .targetId(targetId)
                        .description(description)
                        .build()
        );
    }
}

