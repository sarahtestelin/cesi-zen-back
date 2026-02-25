-- liquibase formatted sql
-- changeset sarah:001_init_schema

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE app_user (
    app_user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mail VARCHAR(150) NOT NULL UNIQUE,
    pseudo VARCHAR(150) NOT NULL UNIQUE,
    app_user_is_active BOOLEAN NOT NULL DEFAULT TRUE,
    hashed_password VARCHAR(250) NOT NULL,
    last_connection_at TIMESTAMP
);

CREATE TABLE resource (
    resource_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_is_active BOOLEAN NOT NULL,
    resource_is_used BOOLEAN NOT NULL,
    resource_title VARCHAR(150) NOT NULL,
    resource_description TEXT NOT NULL,
    status VARCHAR(150) NOT NULL,
    resource_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE survey (
    survey_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    final_score INT NOT NULL,
    app_user_id UUID,
    CONSTRAINT fk_survey_app_user
        FOREIGN KEY (app_user_id)
        REFERENCES app_user(app_user_id)
        ON DELETE CASCADE
);

CREATE TABLE refresh_token (
    refresh_token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_user_id UUID NOT NULL,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_date TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    device_info VARCHAR(255),
    CONSTRAINT fk_refresh_token_app_user
        FOREIGN KEY (app_user_id)
        REFERENCES app_user(app_user_id)
        ON DELETE CASCADE
);

CREATE TABLE historic_etat (
    historic_etat_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    modification_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_value TEXT NOT NULL,
    new_value TEXT NOT NULL,
    comment VARCHAR(255) NOT NULL,
    app_user_id UUID,
    CONSTRAINT fk_historic_etat_app_user
        FOREIGN KEY (app_user_id)
        REFERENCES app_user(app_user_id)
        ON DELETE SET NULL
);

CREATE TABLE reset_password (
    reset_password_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    token_demand_reset VARCHAR(255) NOT NULL,
    CONSTRAINT fk_reset_password_app_user
        FOREIGN KEY (app_user_id)
        REFERENCES app_user(app_user_id)
        ON DELETE CASCADE
);