package com.marketplace.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.admin.dto.AuditLogResponse;
import com.marketplace.admin.entity.AuditLog;
import com.marketplace.admin.repository.AuditLogRepository;
import com.marketplace.common.dto.PageResponse;
import com.marketplace.user.entity.User;
import com.marketplace.user.service.UserService;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, UserService userService, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(UUID actorId, String action, Instant from, Instant to, Pageable pageable) {
        Page<AuditLog> page;
        if (actorId != null) {
            page = auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable);
        } else if (action != null) {
            page = auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
        } else if (from != null && to != null) {
            page = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageable);
        } else {
            page = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        Map<String, Object> actor = new HashMap<>();
        if (log.getActorId() != null) {
            try {
                User user = userService.getById(log.getActorId());
                actor.put("id", user.getId());
                actor.put("email", user.getEmail());
            } catch (IllegalArgumentException ignored) {
                actor.put("id", log.getActorId());
            }
        }
        Map<String, Object> metadata = parseMetadata(log.getMetadataJson());
        return new AuditLogResponse(
                log.getId(),
                actor,
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                metadata,
                log.getCreatedAt()
        );
    }

    private Map<String, Object> parseMetadata(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
