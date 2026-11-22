ALTER TABLE resources DROP CONSTRAINT ck_resources_status;
ALTER TABLE resources ADD CONSTRAINT ck_resources_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'ARCHIVED'));
ALTER TABLE resources ADD image_url NVARCHAR(500) NULL;
ALTER TABLE resources ADD specs NVARCHAR(1000) NULL;
GO
CREATE TABLE audit_log (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT NULL,
    user_name   NVARCHAR(150) NULL,
    action      NVARCHAR(50) NOT NULL,
    entity_type NVARCHAR(50) NOT NULL,
    entity_id   BIGINT NULL,
    details     NVARCHAR(500) NULL,
    created_at  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
GO