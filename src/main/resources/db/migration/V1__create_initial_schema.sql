CREATE TABLE users (
                       id            BIGINT IDENTITY(1,1) PRIMARY KEY,
                       full_name     NVARCHAR(150) NOT NULL,
                       email         NVARCHAR(255) NOT NULL,
                       password_hash NVARCHAR(255) NULL,
                       role          NVARCHAR(30)  NOT NULL,
                       created_at    DATETIME2     NOT NULL DEFAULT SYSUTCDATETIME(),
                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT ck_users_role CHECK (role IN ('RESEARCHER', 'PROFESSOR', 'LAB_ADMIN', 'SYSTEM_ADMIN'))
);

CREATE TABLE resource_types (
                                id          BIGINT IDENTITY(1,1) PRIMARY KEY,
                                name        NVARCHAR(100) NOT NULL,
                                description NVARCHAR(500) NULL,
                                CONSTRAINT uq_resource_types_name UNIQUE (name)
);

CREATE TABLE resources (
                           id                BIGINT IDENTITY(1,1) PRIMARY KEY,
                           name              NVARCHAR(150) NOT NULL,
                           description       NVARCHAR(1000) NULL,
                           resource_type_id  BIGINT NOT NULL,
                           location          NVARCHAR(150) NULL,
                           status            NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           requires_approval BIT NOT NULL DEFAULT 0,
                           created_at        DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                           updated_at        DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                           CONSTRAINT fk_resources_type FOREIGN KEY (resource_type_id) REFERENCES resource_types (id),
                           CONSTRAINT ck_resources_status CHECK (status IN ('ACTIVE', 'MAINTENANCE'))
);

CREATE TABLE reservations (
                              id          BIGINT IDENTITY(1,1) PRIMARY KEY,
                              resource_id BIGINT NOT NULL,
                              user_id     BIGINT NOT NULL,
                              start_time  DATETIME2 NOT NULL,
                              end_time    DATETIME2 NOT NULL,
                              purpose     NVARCHAR(500) NULL,
                              status      NVARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
                              created_at  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                              updated_at  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                              CONSTRAINT fk_reservations_resource FOREIGN KEY (resource_id) REFERENCES resources (id),
                              CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id),
                              CONSTRAINT ck_reservations_status CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED')),
                              CONSTRAINT ck_reservations_interval CHECK (end_time > start_time)
);

CREATE INDEX ix_reservations_resource_time ON reservations (resource_id, start_time, end_time);
CREATE INDEX ix_reservations_user ON reservations (user_id);