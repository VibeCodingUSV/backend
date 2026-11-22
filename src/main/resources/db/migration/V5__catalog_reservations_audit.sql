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
INSERT INTO resources (name, description, resource_type_id, location, status, requires_approval, specs) VALUES
 (N'Cluster USV Nautic Autonom', N'Flotă de ambarcațiuni autonome pentru cercetare', (SELECT id FROM resource_types WHERE name = N'Roboți & USV'), N'Corp A - Subsol', 'ACTIVE', 1, N'3x Ambarcațiuni|Senzori LiDAR Sonar|Telecomandă RTK'),
 (N'Braț Robotic Industrial ABB IRB 120', N'Braț robotic industrial compact', (SELECT id FROM resource_types WHERE name = N'Echipament specific'), N'Corp B - Etaj 1', 'ACTIVE', 0, N'Sarcină 3kg|6 Axe Mișcare|Controler IRC5 Compact'),
 (N'Laborator AI & Viziune Artificială', N'Laborator cu stații GPU pentru AI', (SELECT id FROM resource_types WHERE name = N'Săli cercetare'), N'Corp C - Parter', 'ACTIVE', 0, N'20x Stații RTX 4090|Server Cluster GPU|Capacitate 25 locuri'),
 (N'Spectrometru de Masă High-Res', N'Spectrometru de masă de înaltă rezoluție', (SELECT id FROM resource_types WHERE name = N'Echipament specific'), N'Corp A - Subsol', 'MAINTENANCE', 0, N'Precizie PPM|Sursă Ionizare ESI|Soft Analiza Cromatografica'),
 (N'Imprimantă 3D Industrială Sinterizare', N'Imprimantă 3D SLS industrială', (SELECT id FROM resource_types WHERE name = N'Prototipare'), N'Corp B - Etaj 1', 'ACTIVE', 0, N'Tehnologie SLS|Volum 300×300×400mm|Material Poliamidă PA12');