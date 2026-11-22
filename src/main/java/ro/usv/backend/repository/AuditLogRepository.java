
package ro.usv.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.usv.backend.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
}
