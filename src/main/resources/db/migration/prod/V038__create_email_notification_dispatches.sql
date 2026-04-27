CREATE TABLE IF NOT EXISTS email_notification_dispatches (
    id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    reference_date DATE NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_email_notification_dispatch_unique
    ON email_notification_dispatches (
        notification_type,
        entity_type,
        entity_id,
        lower(recipient_email),
        reference_date
    );
