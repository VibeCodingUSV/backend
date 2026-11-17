UPDATE users
SET email = REPLACE(email, '@usv.ro', '@student.usv.ro')
WHERE email LIKE '%@usv.ro';