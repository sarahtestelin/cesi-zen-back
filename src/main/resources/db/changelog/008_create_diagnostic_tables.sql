-- liquibase formatted sql
-- changeset sarah:008_create_diagnostic_tables

CREATE TABLE IF NOT EXISTS diagnostic_question (
    diagnostic_question_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS diagnostic_result (
    diagnostic_result_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    final_score INT NOT NULL,
    level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    app_user_id UUID,
    CONSTRAINT fk_diagnostic_result_app_user
        FOREIGN KEY (app_user_id)
        REFERENCES app_user(app_user_id)
        ON DELETE CASCADE
);

INSERT INTO diagnostic_question (
    diagnostic_question_id,
    question,
    score,
    is_active,
    created_at,
    updated_at
)
SELECT
    survey_id,
    question,
    score,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM survey
WHERE NOT EXISTS (
    SELECT 1
    FROM diagnostic_question dq
    WHERE dq.diagnostic_question_id = survey.survey_id
);

DROP TABLE IF EXISTS survey;