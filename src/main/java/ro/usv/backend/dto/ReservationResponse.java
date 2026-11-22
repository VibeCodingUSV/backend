package ro.usv.backend.dto;

import java.time.LocalDateTime;
import ro.usv.backend.entity.Reservation;
import ro.usv.backend.entity.ReservationStatus;

public record ReservationResponse(
        Long id,
        Long resourceId,
        String resourceName,
        Long userId,
        String userName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String purpose,
        boolean fundedProject,
        String projectName,
        ReservationStatus status) {

    /** showDetails=false ascunde utilizatorul și scopul (rezervări ale altor persoane). */
    public static ReservationResponse from(Reservation r, boolean showDetails) {
        return new ReservationResponse(
                r.getId(),
                r.getResource().getId(),
                r.getResource().getName(),
                r.getUser().getId(),
                showDetails ? r.getUser().getFullName() : null,
                r.getStartTime(),
                r.getEndTime(),
                showDetails ? r.getPurpose() : null,
                r.isFundedProject(),
                showDetails ? r.getProjectName() : null,
                r.getStatus());
    }
}