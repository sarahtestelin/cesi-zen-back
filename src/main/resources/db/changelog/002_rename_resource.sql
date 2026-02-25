-- liquibase formatted sql
-- changeset sarah:002_rename_resource_table_and_columns

ALTER TABLE resource RENAME TO ressource;

ALTER TABLE ressource RENAME COLUMN resource_id TO ressource_id;
ALTER TABLE ressource RENAME COLUMN resource_is_active TO ressource_is_active;
ALTER TABLE ressource RENAME COLUMN resource_is_used TO ressource_is_used;
ALTER TABLE ressource RENAME COLUMN resource_title TO ressource_title;
ALTER TABLE ressource RENAME COLUMN resource_description TO ressource_description;
ALTER TABLE ressource RENAME COLUMN resource_created_at TO ressource_created_at;