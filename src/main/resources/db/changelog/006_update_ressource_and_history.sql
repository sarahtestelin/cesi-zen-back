-- liquibase formatted sql
-- changeset sarah:006_update_ressource_and_history

ALTER TABLE ressource
ADD COLUMN IF NOT EXISTS category VARCHAR(100) NOT NULL DEFAULT 'Général';

ALTER TABLE ressource
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE ressource
ADD COLUMN IF NOT EXISTS version INT NOT NULL DEFAULT 0;

ALTER TABLE historic_etat
ADD COLUMN IF NOT EXISTS entity_type VARCHAR(100);

ALTER TABLE historic_etat
ADD COLUMN IF NOT EXISTS entity_id UUID;