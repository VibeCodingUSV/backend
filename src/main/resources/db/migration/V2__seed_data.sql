INSERT INTO users (full_name, email, role) VALUES
                                               (N'Ana Popescu', 'ana.popescu@usv.ro', 'RESEARCHER'),
                                               (N'Mihai Ionescu', 'mihai.ionescu@usv.ro', 'RESEARCHER'),
                                               (N'Elena Vasilescu', 'elena.vasilescu@usv.ro', 'PROFESSOR'),
                                               (N'Radu Marinescu', 'radu.marinescu@usv.ro', 'LAB_ADMIN'),
                                               (N'Admin Sistem', 'admin@usv.ro', 'SYSTEM_ADMIN');

INSERT INTO resource_types (name, description) VALUES
                                                   (N'Robot', N'Roboți industriali și mobili'),
                                                   (N'Cluster de calcul', N'Resurse de calcul de înaltă performanță'),
                                                   (N'Sală', N'Săli de seminar și conferințe'),
                                                   (N'Echipament de laborator', N'Aparatură de măsură și prototipare');

INSERT INTO resources (name, description, resource_type_id, location, status, requires_approval) VALUES
                                                                                                     (N'Robot industrial UR5e', N'Braț robotic colaborativ', (SELECT id FROM resource_types WHERE name = N'Robot'), N'Laborator Robotică', 'ACTIVE', 0),
                                                                                                     (N'Robot mobil Husky', N'Platformă mobilă pentru teren', (SELECT id FROM resource_types WHERE name = N'Robot'), N'Laborator Robotică', 'ACTIVE', 0),
                                                                                                     (N'Cluster USV HPC', N'Cluster de calcul al universității', (SELECT id FROM resource_types WHERE name = N'Cluster de calcul'), N'Centrul de calcul', 'ACTIVE', 1),
                                                                                                     (N'Server GPU', N'Server cu GPU pentru antrenare modele', (SELECT id FROM resource_types WHERE name = N'Cluster de calcul'), N'Centrul de calcul', 'ACTIVE', 1),
                                                                                                     (N'Sala C101', N'Sală de seminar, 30 locuri', (SELECT id FROM resource_types WHERE name = N'Sală'), N'Corp C', 'ACTIVE', 0),
                                                                                                     (N'Sala de conferințe', N'Sală pentru workshop-uri, 80 locuri', (SELECT id FROM resource_types WHERE name = N'Sală'), N'Corp A', 'ACTIVE', 0),
                                                                                                     (N'Sala C204', N'Sală de seminar, în renovare', (SELECT id FROM resource_types WHERE name = N'Sală'), N'Corp C', 'MAINTENANCE', 0),
                                                                                                     (N'Microscop electronic', N'Microscop cu scanare', (SELECT id FROM resource_types WHERE name = N'Echipament de laborator'), N'Laborator Fizică', 'ACTIVE', 1),
                                                                                                     (N'Imprimantă 3D', N'Imprimantă 3D pentru prototipuri', (SELECT id FROM resource_types WHERE name = N'Echipament de laborator'), N'Atelier', 'ACTIVE', 0),
                                                                                                     (N'Osciloscop digital', N'Osciloscop 4 canale', (SELECT id FROM resource_types WHERE name = N'Echipament de laborator'), N'Laborator Electronică', 'ACTIVE', 0);

INSERT INTO reservations (resource_id, user_id, start_time, end_time, purpose, status) VALUES
                                                                                           (1, 1, '2026-11-18 09:00', '2026-11-18 11:00', N'Teste de navigare', 'CONFIRMED'),
                                                                                           (1, 2, '2026-11-18 13:00', '2026-11-18 15:00', N'Calibrare senzori', 'CONFIRMED'),
                                                                                           (1, 3, '2026-11-19 10:00', '2026-11-19 12:00', N'Demonstrație studenți', 'CONFIRMED'),
                                                                                           (2, 1, '2026-11-18 10:00', '2026-11-18 12:00', N'Test pe teren', 'CONFIRMED'),
                                                                                           (2, 2, '2026-11-20 14:00', '2026-11-20 16:00', N'Colectare date', 'CANCELLED'),
                                                                                           (3, 2, '2026-11-19 08:00', '2026-11-19 20:00', N'Antrenare model', 'PENDING'),
                                                                                           (3, 1, '2026-11-21 08:00', '2026-11-21 12:00', N'Simulări numerice', 'CONFIRMED'),
                                                                                           (4, 3, '2026-11-20 09:00', '2026-11-20 17:00', N'Experimente deep learning', 'PENDING'),
                                                                                           (5, 3, '2026-11-18 14:00', '2026-11-18 16:00', N'Seminar de cercetare', 'CONFIRMED'),
                                                                                           (5, 1, '2026-11-19 09:00', '2026-11-19 11:00', N'Întâlnire echipă', 'CONFIRMED'),
                                                                                           (6, 3, '2026-11-22 10:00', '2026-11-22 13:00', N'Workshop', 'CONFIRMED'),
                                                                                           (8, 2, '2026-11-23 09:00', '2026-11-23 12:00', N'Analiză probe', 'PENDING'),
                                                                                           (8, 1, '2026-11-24 09:00', '2026-11-24 11:00', N'Imagistică probe', 'REJECTED'),
                                                                                           (9, 2, '2026-11-18 08:00', '2026-11-18 12:00', N'Prototip carcasă', 'CONFIRMED'),
                                                                                           (10, 1, '2026-11-19 13:00', '2026-11-19 15:00', N'Măsurători semnal', 'CONFIRMED');