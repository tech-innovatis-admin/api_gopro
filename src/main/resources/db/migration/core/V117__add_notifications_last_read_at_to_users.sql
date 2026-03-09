ALTER TABLE users
    ADD COLUMN IF NOT EXISTS notifications_last_read_at timestamp;
