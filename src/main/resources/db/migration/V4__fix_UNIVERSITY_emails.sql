UPDATE users
SET email = REPLACE(email, '@student.usv.ro', '@usv.ro')
WHERE role IN ('PROFESSOR', 'LAB_ADMIN', 'SYSTEM_ADMIN');