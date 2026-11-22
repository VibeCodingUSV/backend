ALTER TABLE resources DROP CONSTRAINT ck_resources_status;
ALTER TABLE resources ADD CONSTRAINT ck_resources_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'ARCHIVED'));
ALTER TABLE resources ADD image_url NVARCHAR(500) NULL;
ALTER TABLE resources ADD specs NVARCHAR(1000) NULL;
GO