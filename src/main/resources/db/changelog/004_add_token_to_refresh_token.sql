-- liquibase formatted sql
-- changeset sarah:004_add_token_to_refresh_token

ALTER TABLE refresh_token
ADD COLUMN token VARCHAR(255);

UPDATE refresh_token
SET token = gen_random_uuid()::text
WHERE token IS NULL;

ALTER TABLE refresh_token
ALTER COLUMN token SET NOT NULL;

ALTER TABLE refresh_token
ADD CONSTRAINT uk_refresh_token_token UNIQUE (token);