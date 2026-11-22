
package ro.usv.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.usv.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
}
