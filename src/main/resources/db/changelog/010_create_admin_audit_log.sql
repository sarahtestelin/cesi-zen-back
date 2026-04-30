-- liquibase formatted sql
-- changeset sarah:010_create_admin_audit_log

CREATE TABLE IF NOT EXISTS admin_audit_log (
    admin_audit_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_mail VARCHAR(150) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(100),
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);