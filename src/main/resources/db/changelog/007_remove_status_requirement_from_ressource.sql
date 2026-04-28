-- liquibase formatted sql
-- changeset sarah:007_remove_status_requirement_from_ressource

ALTER TABLE ressource
ALTER COLUMN status DROP NOT NULL;