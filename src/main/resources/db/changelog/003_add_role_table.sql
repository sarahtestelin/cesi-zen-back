-- liquibase formatted sql

-- changeset sarah:003_add_role_table

CREATE TABLE role (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO role (role_name) VALUES ('USER');
INSERT INTO role (role_name) VALUES ('ADMIN');

ALTER TABLE app_user
ADD COLUMN role_id UUID;

UPDATE app_user
SET role_id = (SELECT role_id FROM role WHERE role_name = 'USER')
WHERE role_id IS NULL;

ALTER TABLE app_user
ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE app_user
ADD CONSTRAINT fk_app_user_role
FOREIGN KEY (role_id)
REFERENCES role(role_id);