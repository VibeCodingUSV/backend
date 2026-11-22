
package ro.usv.backend.dto;

import ro.usv.backend.entity.Role;
import ro.usv.backend.entity.User;

public record UserResponse(Long id, String fullName, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
