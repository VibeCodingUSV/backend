
package ro.usv.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.usv.backend.dto.AuditLogResponse;
import ro.usv.backend.entity.AuditLog;
import ro.usv.backend.entity.User;
import ro.usv.backend.repository.AuditLogRepository;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User user, String action, String entityType, Long entityId, String details) {
        auditLogRepository.save(new AuditLog(user, action, entityType, entityId, details));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> latest() {
        return auditLogRepository.findTop200ByOrderByCreatedAtDesc()
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
