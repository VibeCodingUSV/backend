
package ro.usv.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.usv.backend.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select distinct r.resource.id from Reservation r "
            + "where r.status = ro.usv.backend.entity.ReservationStatus.CONFIRMED "
            + "and r.startTime <= :now and r.endTime > :now")
    List<Long> findBusyResourceIds(@Param("now") LocalDateTime now);

    @Query("select count(r) > 0 from Reservation r where r.resource.id = :resourceId "
            + "and r.status in (ro.usv.backend.entity.ReservationStatus.PENDING, ro.usv.backend.entity.ReservationStatus.CONFIRMED) "
            + "and r.startTime < :endTime and r.endTime > :startTime")
    boolean existsOverlap(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("select count(r) > 0 from Reservation r where r.resource.id = :resourceId and r.id <> :excludeId "
            + "and r.status in (ro.usv.backend.entity.ReservationStatus.PENDING, ro.usv.backend.entity.ReservationStatus.CONFIRMED) "
            + "and r.startTime < :endTime and r.endTime > :startTime")
    boolean existsOverlapExcluding(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId);

    @Query("select r from Reservation r join fetch r.resource join fetch r.user order by r.startTime desc")
    List<Reservation> findAllWithDetails();

    @Query("select r from Reservation r join fetch r.resource join fetch r.user "
            + "where r.endTime >= :from order by r.startTime desc")
    List<Reservation> findAllWithDetailsSince(@Param("from") LocalDateTime from);

    @Query("select r from Reservation r join fetch r.resource join fetch r.user "
            + "where r.status = ro.usv.backend.entity.ReservationStatus.CONFIRMED and r.reminderSent = false "
            + "and r.startTime > :now and r.startTime <= :until")
    List<Reservation> findDueReminders(@Param("now") LocalDateTime now, @Param("until") LocalDateTime until);

    @Query("select r from Reservation r join fetch r.resource join fetch r.user "
            + "where r.user.id = :userId order by r.startTime desc")
    List<Reservation> findByUserWithDetails(@Param("userId") Long userId);
}
