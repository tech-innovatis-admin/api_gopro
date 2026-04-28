package br.com.gopro.api.repository;

import br.com.gopro.api.model.EmailNotificationDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EmailNotificationDispatchRepository extends JpaRepository<EmailNotificationDispatch, Long> {

    boolean existsByNotificationTypeAndEntityTypeAndEntityIdAndRecipientEmailIgnoreCaseAndReferenceDate(
            String notificationType,
            String entityType,
            Long entityId,
            String recipientEmail,
            LocalDate referenceDate
    );
}
