ALTER TABLE reservations ADD funded_project BIT NOT NULL CONSTRAINT DF_reservations_funded_project DEFAULT 0;
ALTER TABLE reservations ADD project_name NVARCHAR(150) NULL;
GO
