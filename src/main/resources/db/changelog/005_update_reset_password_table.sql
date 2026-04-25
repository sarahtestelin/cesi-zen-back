-- liquibase formatted sql
-- changeset sarah:005_update_reset_password_table

ALTER TABLE reset_password
ADD COLUMN IF NOT EXISTS expiration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE reset_password
ADD COLUMN IF NOT EXISTS used BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE reset_password
ADD CONSTRAINT uk_reset_password_token_demand_reset UNIQUE (token_demand_reset);