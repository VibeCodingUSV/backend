
package ro.usv.backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.usv.backend.dto.ReservationResponse;
import ro.usv.backend.dto.UserResponse;
import ro.usv.backend.entity.User;
import ro.usv.backend.service.AuditService;
import ro.usv.backend.service.ReservationService;
import ro.usv.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;
    private final AuditService auditService;
    private final boolean sso;

    public UserController(
            UserService userService,
            ReservationService reservationService,
            AuditService auditService,
            @Value("${app.auth.mode:dev}") String authMode) {
        this.userService = userService;
        this.reservationService = reservationService;
        this.auditService = auditService;
        this.sso = "sso".equalsIgnoreCase(authMode);
    }

    /**
     * În modul dev returnează toți utilizatorii (simulatorul de roluri). În modul sso returnează DOAR utilizatorul
     * autentificat, ca să nu se expună lista de utilizatori și adresele lor de e-mail (GDPR).
     */
    @GetMapping
    public List<UserResponse> getAll(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (sso) {
            return List.of(UserResponse.from(userService.require(userId)));
        }
        return userService.findAll();
    }

    /** GDPR – dreptul de acces: utilizatorul își poate descărca propriile date (profil + rezervări). */
    @GetMapping("/me/export")
    public Map<String, Object> exportMyData(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        User me = userService.require(userId);
        List<ReservationResponse> reservations = reservationService.list(me, "mine");
        auditService.log(me, "DATA_EXPORT", "USER", me.getId(), "Export date personale");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("profil", UserResponse.from(me));
        out.put("rezervari", reservations);
        return out;
    }
}
