
package ro.usv.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.usv.backend.dto.UserResponse;
import ro.usv.backend.entity.Role;
import ro.usv.backend.entity.User;
import ro.usv.backend.exception.ForbiddenException;
import ro.usv.backend.exception.UnauthorizedException;
import ro.usv.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    /** Utilizatorul curent vine din header-ul X-User-Id (simulare SSO). */
    @Transactional(readOnly = true)
    public User require(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("Lipsește utilizatorul curent (header X-User-Id)");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Utilizator necunoscut"));
    }

    @Transactional(readOnly = true)
    public User requireAdmin(Long userId) {
        User user = require(userId);
        if (!isAdmin(user)) {
            throw new ForbiddenException("Acțiune permisă doar administratorilor");
        }
        return user;
    }

    public static boolean isAdmin(User user) {
        return user.getRole() == Role.LAB_ADMIN || user.getRole() == Role.SYSTEM_ADMIN;
    }
}
