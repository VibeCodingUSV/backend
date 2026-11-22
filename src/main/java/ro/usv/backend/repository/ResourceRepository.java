package ro.usv.backend.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.usv.backend.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * Blochează rândul resursei până la finalul tranzacției, astfel încât două cereri simultane
     * pentru aceeași resursă să fie procesate pe rând.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resource r where r.id = :id")
    Optional<Resource> findByIdForUpdate(@Param("id") Long id);
}