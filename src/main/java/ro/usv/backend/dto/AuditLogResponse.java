
package ro.usv.backend.dto;

import java.time.LocalDateTime;
import ro.usv.backend.entity.AuditLog;

public record AuditLogResponse(
        Long id, String userName, String action, String entityType, Long entityId,
        String details, LocalDateTime createdAt) {

    public static AuditLogResponse from(AuditLog a) {
        return new AuditLogResponse(a.getId(), a.getUserName(), a.getAction(), a.getEntityType(),
                a.getEntityId(), a.getDetails(), a.getCreatedAt());
    }
}
