
package ro.usv.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ro.usv.backend.entity.Role;
import ro.usv.backend.entity.User;
import ro.usv.backend.repository.UserRepository;

/**
 * Identitatea utilizatorului curent.
 *
 * <ul>
 *   <li><b>app.auth.mode=dev</b> (implicit): se acceptă header-ul X-User-Id trimis de frontend (simulator de roluri).
 *       Folosiți DOAR pentru dezvoltare/demo — oricine poate trimite orice X-User-Id.
 *   <li><b>app.auth.mode=sso</b>: aplicația rulează în spatele unui gateway SSO/LDAP al universității
 *       (ex. oauth2-proxy, Shibboleth, Keycloak) care autentifică utilizatorul și trimite e-mailul acestuia în
 *       header-ul {@code app.auth.email-header}. Orice X-User-Id venit de la client este ignorat; utilizatorul se
 *       identifică după e-mail și, dacă nu există, este creat cu rolul RESEARCHER. Cererile fără identitate primesc 401.
 *       Gateway-ul trebuie să elimine acest header din cererile externe, iar backend-ul să nu fie expus direct.
 * </ul>
 */
@Component
public class AuthHeaderFilter extends OncePerRequestFilter {

    private static final String USER_ID = "X-User-Id";

    private final UserRepository userRepository;
    private final boolean sso;
    private final String emailHeader;
    private final String nameHeader;

    public AuthHeaderFilter(
            UserRepository userRepository,
            @Value("${app.auth.mode:dev}") String mode,
            @Value("${app.auth.email-header:X-Auth-Request-Email}") String emailHeader,
            @Value("${app.auth.name-header:X-Auth-Request-User}") String nameHeader) {
        this.userRepository = userRepository;
        this.sso = "sso".equalsIgnoreCase(mode);
        this.emailHeader = emailHeader;
        this.nameHeader = nameHeader;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !sso || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String email = request.getHeader(emailHeader);
        if (email == null || email.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/problem+json;charset=UTF-8");
            response.getWriter().write("{\"status\":401,\"detail\":\"Autentificare SSO necesară.\"}");
            return;
        }
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseGet(() -> provision(request, email.trim()));
        chain.doFilter(new IdentityRequest(request, String.valueOf(user.getId())), response);
    }

    private User provision(HttpServletRequest request, String email) {
        String name = request.getHeader(nameHeader);
        User user = new User();
        user.setEmail(email);
        user.setFullName(name == null || name.isBlank()
                ? email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length())
                : name.trim());
        user.setRole(Role.RESEARCHER);
        return userRepository.save(user);
    }

    /** Înlocuiește orice X-User-Id venit de la client cu identitatea validată de gateway-ul SSO. */
    private static final class IdentityRequest extends HttpServletRequestWrapper {
        private final String userId;

        IdentityRequest(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            return USER_ID.equalsIgnoreCase(name) ? userId : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return USER_ID.equalsIgnoreCase(name)
                    ? Collections.enumeration(Set.of(userId)) : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new HashSet<>(Collections.list(super.getHeaderNames()));
            names.add(USER_ID);
            return Collections.enumeration(names);
        }
    }
}
