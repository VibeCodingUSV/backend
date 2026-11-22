
package ro.usv.backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.usv.backend.dto.AuditLogResponse;
import ro.usv.backend.service.AuditService;
import ro.usv.backend.service.UserService;

@RestController
@RequestMapping("/api/audit-log")
public class AuditLogController {

    private final AuditService auditService;
    private final UserService userService;

    public AuditLogController(AuditService auditService, UserService userService) {
        this.auditService = auditService;
        this.userService = userService;
    }

    @GetMapping
    public List<AuditLogResponse> latest(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        userService.requireAdmin(userId);
        return auditService.latest();
    }
}
