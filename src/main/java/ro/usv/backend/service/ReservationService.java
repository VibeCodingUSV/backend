
package ro.usv.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.usv.backend.dto.ReservationRequest;
import ro.usv.backend.dto.ReservationResponse;
import ro.usv.backend.entity.Reservation;
import ro.usv.backend.entity.ReservationStatus;
import ro.usv.backend.entity.Resource;
import ro.usv.backend.entity.ResourceStatus;
import ro.usv.backend.entity.User;
import ro.usv.backend.exception.BadRequestException;
import ro.usv.backend.exception.ConflictException;
import ro.usv.backend.exception.ForbiddenException;
import ro.usv.backend.exception.NotFoundException;
import ro.usv.backend.repository.ReservationRepository;
import ro.usv.backend.repository.ResourceRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    /** Câte zile de istoric se returnează în listarea generală. */
    @Value("${app.reservations.window-days:120}")
    private long windowDays = 120;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            AuditService auditService,
            NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    private void notifyOwner(Reservation r, String subject, String intro) {
        notificationService.send(
                r.getUser(),
                subject + ": " + r.getResource().getName(),
                "Bună, " + r.getUser().getFullName() + ",\n\n" + intro
                        + "\n\nResursă: " + r.getResource().getName()
                        + "\nInterval: " + r.getStartTime().format(FMT)
                        + " – " + r.getEndTime().format(FMT)
                        + "\n\nRRM Platform");
    }

    /** Prioritate pentru proiecte finanțate. */
    private static void applyProject(Reservation reservation, ReservationRequest req) {
        boolean funded = Boolean.TRUE.equals(req.fundedProject());
        reservation.setFundedProject(funded);

        String name = req.projectName() == null ? null : req.projectName().trim();
        reservation.setProjectName(
                funded && name != null && !name.isEmpty() ? name : null);
    }

    /** scope=all: toate rezervările; altfel doar ale utilizatorului. */
    @Transactional(readOnly = true)
    public List<ReservationResponse> list(User actor, String scope) {
        if ("all".equalsIgnoreCase(scope)) {
            boolean admin = UserService.isAdmin(actor);
            return reservationRepository
                    .findAllWithDetailsSince(LocalDateTime.now().minusDays(windowDays))
                    .stream()
                    .map(r -> ReservationResponse.from(
                            r, admin || r.getUser().getId().equals(actor.getId())))
                    .toList();
        }

        return reservationRepository.findByUserWithDetails(actor.getId())
                .stream()
                .map(r -> ReservationResponse.from(r, true))
                .toList();
    }

    @Transactional
    public ReservationResponse create(User actor, ReservationRequest req) {
        Resource resource = resourceRepository
                .findByIdForUpdate(req.resourceId())
                .orElseThrow(() -> new NotFoundException(
                        "Resursa cu id " + req.resourceId() + " nu există"));

        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            throw new ConflictException(
                    "Resursa nu poate fi rezervată în acest moment (mentenanță sau arhivată)");
        }

        if (!req.endTime().isAfter(req.startTime())) {
            throw new BadRequestException(
                    "Ora de sfârșit trebuie să fie după ora de început");
        }

        if (req.startTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BadRequestException("Nu se pot face rezervări în trecut");
        }

        if (reservationRepository.existsOverlap(
                resource.getId(), req.startTime(), req.endTime())) {
            throw new ConflictException(
                    "Intervalul ales se suprapune cu o altă rezervare");
        }

        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setUser(actor);
        reservation.setStartTime(req.startTime());
        reservation.setEndTime(req.endTime());
        reservation.setPurpose(req.purpose());
        applyProject(reservation, req);
        reservation.setStatus(resource.isRequiresApproval()
                ? ReservationStatus.PENDING
                : ReservationStatus.CONFIRMED);

        Reservation saved = reservationRepository.save(reservation);

        auditService.log(actor, "RESERVATION_CREATED", "RESERVATION",
                saved.getId(), resource.getName() + " (" + saved.getStatus() + ")");

        if (saved.getStatus() == ReservationStatus.PENDING) {
            notifyOwner(saved, "Cerere înregistrată",
                    "Cererea ta a fost înregistrată și așteaptă aprobarea unui administrator.");
        } else {
            notifyOwner(saved, "Rezervare confirmată",
                    "Rezervarea ta a fost confirmată.");
        }

        return ReservationResponse.from(saved, true);
    }

    /** Modificarea unei rezervări active de către proprietar sau administrator. */
    @Transactional
    public ReservationResponse update(User actor, Long id, ReservationRequest req) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Rezervarea cu id " + id + " nu există"));

        boolean admin = UserService.isAdmin(actor);
        boolean owner = reservation.getUser().getId().equals(actor.getId());

        if (!admin && !owner) {
            throw new ForbiddenException("Poți modifica doar rezervările proprii");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ConflictException("Rezervarea nu mai poate fi modificată");
        }

        if (!reservation.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException(
                    "Rezervările care au început deja nu pot fi modificate");
        }

        Resource resource = resourceRepository.findByIdForUpdate(req.resourceId())
                .orElseThrow(() -> new NotFoundException(
                        "Resursa cu id " + req.resourceId() + " nu există"));

        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            throw new ConflictException(
                    "Resursa nu poate fi rezervată în acest moment (mentenanță sau arhivată)");
        }

        if (!req.endTime().isAfter(req.startTime())) {
            throw new BadRequestException(
                    "Ora de sfârșit trebuie să fie după ora de început");
        }

        if (!req.startTime().equals(reservation.getStartTime())
                && req.startTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BadRequestException("Nu se pot face rezervări în trecut");
        }

        if (reservationRepository.existsOverlapExcluding(
                resource.getId(), req.startTime(), req.endTime(), id)) {
            throw new ConflictException(
                    "Intervalul ales se suprapune cu o altă rezervare");
        }

        reservation.setResource(resource);
        reservation.setStartTime(req.startTime());
        reservation.setEndTime(req.endTime());
        reservation.setPurpose(req.purpose());
        applyProject(reservation, req);

        // O rezervare modificată pe o resursă cu aprobare trebuie reaprobată.
        if (resource.isRequiresApproval() && !admin) {
            reservation.setStatus(ReservationStatus.PENDING);
        }

        Reservation saved = reservationRepository.save(reservation);

        auditService.log(actor, "RESERVATION_UPDATED", "RESERVATION",
                saved.getId(), resource.getName() + " (" + saved.getStatus() + ")");

        notifyOwner(saved, "Rezervare modificată",
                saved.getStatus() == ReservationStatus.PENDING
                        ? "Rezervarea ta a fost modificată și așteaptă din nou aprobarea."
                        : "Rezervarea ta a fost modificată.");

        return ReservationResponse.from(saved, true);
    }

    @Transactional
    public ReservationResponse updateStatus(
            User actor, Long id, ReservationStatus target) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Rezervarea cu id " + id + " nu există"));

        boolean admin = UserService.isAdmin(actor);
        boolean owner = reservation.getUser().getId().equals(actor.getId());
        ReservationStatus current = reservation.getStatus();

        switch (target) {
            case CANCELLED -> {
                if (!admin && !owner) {
                    throw new ForbiddenException(
                            "Poți anula doar rezervările proprii");
                }
                if (current != ReservationStatus.PENDING
                        && current != ReservationStatus.CONFIRMED) {
                    throw new ConflictException("Rezervarea nu mai poate fi anulată");
                }
            }
            case CONFIRMED, REJECTED -> {
                if (!admin) {
                    throw new ForbiddenException(
                            "Doar administratorii pot aproba sau respinge cereri");
                }
                if (current != ReservationStatus.PENDING) {
                    throw new ConflictException(
                            "Doar cererile în așteptare pot fi aprobate sau respinse");
                }
            }
            default -> throw new BadRequestException("Status invalid");
        }

        reservation.setStatus(target);
        Reservation saved = reservationRepository.save(reservation);

        auditService.log(actor, "RESERVATION_" + target.name(), "RESERVATION",
                saved.getId(),
                saved.getResource().getName() + " - " + saved.getUser().getFullName());

        switch (target) {
            case CONFIRMED -> notifyOwner(saved, "Rezervare aprobată",
                    "Cererea ta a fost aprobată.");
            case REJECTED -> notifyOwner(saved, "Cerere respinsă",
                    "Cererea ta a fost respinsă de un administrator.");
            case CANCELLED -> notifyOwner(saved, "Rezervare anulată",
                    "Rezervarea a fost anulată.");
            default -> { }
        }

        return ReservationResponse.from(saved, true);
    }
}
