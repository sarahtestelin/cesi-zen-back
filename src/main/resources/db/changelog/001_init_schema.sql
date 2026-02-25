-- liquibase formatted sql
-- changeset sarah:001_init_schema

CREATE TABLE AppUser (
    appUserId CHAR(36) NOT NULL DEFAULT (UUID()),
    mail VARCHAR(150) NOT NULL,
    pseudo VARCHAR(150) NOT NULL,
    appUserIsActive BOOLEAN NOT NULL DEFAULT TRUE,
    hashedPassword VARCHAR(250) NOT NULL,
    lastConnectionAt TIMESTAMP NULL,
    PRIMARY KEY (appUserId),
    UNIQUE KEY uq_appUser_mail (mail),
    UNIQUE KEY uq_appUser_pseudo (pseudo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Resource (
    resourceId CHAR(36) NOT NULL DEFAULT (UUID()),
    resourceIsActive BOOLEAN NOT NULL,
    resourceIsUsed BOOLEAN NOT NULL,
    resourceTitle VARCHAR(150) NOT NULL,
    resourceDescription TEXT NOT NULL,
    status VARCHAR(150) NOT NULL,
    resourceCreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resourceId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Survey (
    surveyId CHAR(36) NOT NULL DEFAULT (UUID()),
    question VARCHAR(255) NOT NULL,
    score INT NOT NULL,
    finalScore INT NOT NULL,
    appUserId CHAR(36),
    PRIMARY KEY (surveyId),
    CONSTRAINT fk_survey_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE RefreshToken (
    refreshTokenId CHAR(36) NOT NULL DEFAULT (UUID()),
    appUserId CHAR(36) NOT NULL,
    creationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expirationDate TIMESTAMP NOT NULL,
    isRevoked BOOLEAN NOT NULL DEFAULT FALSE,
    deviceInfo VARCHAR(255),
    PRIMARY KEY (refreshTokenId),
    CONSTRAINT fk_refreshToken_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE HistoricEtat (
    historicEtatId CHAR(36) NOT NULL DEFAULT (UUID()),
    modificationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    oldValue TEXT NOT NULL,
    newValue TEXT NOT NULL,
    comment VARCHAR(255) NOT NULL,
    appUserId CHAR(36),
    PRIMARY KEY (historicEtatId),
    CONSTRAINT fk_historicEtat_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE SET NULL
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ResetPassword (
    resetPasswordId CHAR(36) NOT NULL DEFAULT (UUID()),
    appUserId CHAR(36) NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tokenDemandReset VARCHAR(255) NOT NULL,
    PRIMARY KEY (resetPasswordId),
    CONSTRAINT fk_resetPassword_appUser
        FOREIGN KEY (appUserId)
            REFERENCES AppUser(appUserId)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;