
package ro.usv.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.usv.backend.entity.Reservation;
import ro.usv.backend.repository.ReservationRepository;

/** Trimite o singură dată, prin e-mail, un reminder pentru rezervările confirmate care încep în următoarele 24 de ore. */
@Component
public class ReminderJob {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    private final long hoursBefore;

    public ReminderJob(
            ReservationRepository reservationRepository,
            NotificationService notificationService,
            @Value("${app.reminders.hours-before:24}") long hoursBefore) {
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
        this.hoursBefore = hoursBefore;
    }

    @Scheduled(fixedDelayString = "${app.reminders.interval-ms:300000}", initialDelay = 60000)
    @Transactional
    public void sendDueReminders() {
        if (!notificationService.isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> due =
                reservationRepository.findDueReminders(now, now.plusHours(hoursBefore));

        for (Reservation r : due) {
            notificationService.send(
                    r.getUser(),
                    "Reminder rezervare: " + r.getResource().getName(),
                    "Bună, " + r.getUser().getFullName() + ",\n\nRezervarea ta pentru \""
                            + r.getResource().getName()
                            + "\" începe la " + r.getStartTime().format(FMT)
                            + " și se încheie la " + r.getEndTime().format(FMT)
                            + ".\n\nRRM Platform");

            r.setReminderSent(true);
        }

        reservationRepository.saveAll(due);
    }
}
