-- liquibase formatted sql
-- changeset sarah:009_create_diagnostic_result_config

CREATE TABLE IF NOT EXISTS diagnostic_result_config (
    diagnostic_result_config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    min_score INT NOT NULL,
    max_score INT NOT NULL,
    level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO diagnostic_result_config (
    min_score,
    max_score,
    level,
    message,
    is_active
)
VALUES
    (
        0,
        149,
        'FAIBLE',
        'Votre niveau de stress semble faible. Continuez à préserver votre équilibre au quotidien.',
        TRUE
    ),
    (
        150,
        299,
        'MODERE',
        'Votre niveau de stress semble modéré. Il peut être utile d''identifier les sources de tension et de mettre en place des actions de prévention.',
        TRUE
    ),
    (
        300,
        1000,
        'ELEVE',
        'Votre niveau de stress semble élevé. Ce résultat n''est pas un diagnostic médical, mais il peut être utile d''en parler à un professionnel de santé.',
        TRUE
    );