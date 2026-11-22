
package ro.usv.backend.service;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ro.usv.backend.entity.User;

/**
 * Notificări prin e-mail (confirmare, reminder, anulare). E-mailurile se trimit doar după commit-ul tranzacției
 * și doar dacă app.mail.enabled=true și serverul SMTP este configurat.
 * Eșecul trimiterii nu afectează niciodată rezervarea.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean enabled;
    private final String from;

    public NotificationService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.from:no-reply@usv.ro}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public boolean isEnabled() {
        return enabled && mailSender.getIfAvailable() != null;
    }

    public void send(User to, String subject, String body) {
        if (!isEnabled() || to == null || to.getEmail() == null || to.getEmail().isBlank()) {
            return;
        }

        final String address = to.getEmail();
        Runnable task = () -> CompletableFuture.runAsync(() -> deliver(address, subject, body));

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private void deliver(String address, String subject, String body) {
        try {
            JavaMailSender sender = mailSender.getIfAvailable();
            if (sender == null) {
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(address);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
        } catch (Exception e) {
            log.warn("Nu s-a putut trimite e-mailul către {}: {}", address, e.getMessage());
        }
    }
}
