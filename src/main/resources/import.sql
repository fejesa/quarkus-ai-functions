-- ===============================
-- NO MATCH CASE (institute only)
-- ===============================
INSERT INTO institute_user (id, first_name, last_name, birth_date, country, city, zip_code, street, house_number)
VALUES (nextval('institute_user_seq'), 'Alice', 'Johnson', '1990-05-21', 'Germany', 'Berlin', '10115', 'Invalidenstrasse', '10');

--------------------------------------------------
-- EXACT MATCH CASES (5 cases)
--------------------------------------------------
-- Case 1
INSERT INTO institute_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, id)
VALUES ('Clara', 'Meier', '2000-07-21', 'Hamburg', 'Germany', 'Sample Str.', '10', '20095', nextval('institute_user_seq'));

INSERT INTO statistic_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, external_id, id)
VALUES ('Clara', 'Meier', '2000-07-21', 'Hamburg', 'Germany', 'Sample Str.', '10', '20095', 'EXT-1001', nextval('statistic_user_seq'));

-- Case 2
INSERT INTO institute_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, id)
VALUES ('Markus', 'Fischer', '1992-11-10', 'Munich', 'Germany', 'Hauptstrasse', '5', '80331', nextval('institute_user_seq'));

INSERT INTO statistic_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, external_id, id)
VALUES ('Markus', 'Fischer', '1992-11-10', 'Munich', 'Germany', 'Hauptstrasse', '5', '80331', 'EXT-1002', nextval('statistic_user_seq'));

-- Case 3
INSERT INTO institute_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, id)
VALUES ('Anna', 'Schneider', '1988-05-14', 'Berlin', 'Germany', 'Alexanderplatz', '22', '10115', nextval('institute_user_seq'));

INSERT INTO statistic_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, external_id, id)
VALUES ('Anna', 'Schneider', '1988-05-14', 'Berlin', 'Germany', 'Alexanderplatz', '22', '10115', 'EXT-1003', nextval('statistic_user_seq'));

-- Case 4
INSERT INTO institute_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, id)
VALUES ('Jonas', 'Becker', '1995-02-28', 'Cologne', 'Germany', 'Domstrasse', '7', '50667', nextval('institute_user_seq'));

INSERT INTO statistic_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, external_id, id)
VALUES ('Jonas', 'Becker', '1995-02-28', 'Cologne', 'Germany', 'Domstrasse', '7', '50667', 'EXT-1004', nextval('statistic_user_seq'));

-- Case 5
INSERT INTO institute_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, id)
VALUES ('Laura', 'Hoffmann', '1999-09-09', 'Stuttgart', 'Germany', 'Königsstrasse', '15', '70173', nextval('institute_user_seq'));

INSERT INTO statistic_user (first_name, last_name, birth_date, city, country, street, house_number, zip_code, external_id, id)
VALUES ('Laura', 'Hoffmann', '1999-09-09', 'Stuttgart', 'Germany', 'Königsstrasse', '15', '70173', 'EXT-1005', nextval('statistic_user_seq'));


--- ===============================
-- SIMILAR MATCH CASES (5 persons, 5 variants each → 25 rows in statistic_user)
-- ===============================
-- Person A: Anna Schmidt
INSERT INTO institute_user (id, first_name, last_name, birth_date, country, city, zip_code, street, house_number)
VALUES (nextval('institute_user_seq'), 'Anna', 'Schmidt', '1993-09-12', 'Germany', 'Berlin', '10117', 'Unter den Linden', '77');
-- Variants in statistic_user
INSERT INTO statistic_user (id, first_name, last_name, birth_date, external_id, country, city, zip_code, street, house_number) VALUES
   (nextval('statistic_user_seq'), 'Anna', 'Schmidt', '1993-09-12', 'EXT101', 'Germany', 'Berlin', '10117', 'Mozart strasse', '78A'), -- house no diff
   (nextval('statistic_user_seq'), 'Anna', 'Schmidt', '1993-09-12', 'EXT104', 'Germany', 'Potsdam', '10117', 'Unter den Linden', '56'), -- city diff
   (nextval('statistic_user_seq'), 'Anna', 'Schmidt', '1993-09-12', 'EXT105', 'Austria', 'Vienna', '10117', 'Unter den Linden', '13'); -- country diff

-- Person B: Peter Weber
INSERT INTO institute_user (id, first_name, last_name, birth_date, country, city, zip_code, street, house_number)
VALUES (nextval('institute_user_seq'), 'Peter', 'Weber', '1982-04-08', 'Germany', 'Munich', '80331', 'Sendlinger Strasse', '48');
INSERT INTO statistic_user (id, first_name, last_name, birth_date, external_id, country, city, zip_code, street, house_number) VALUES
   (nextval('statistic_user_seq'), 'Peter', 'Weber', '1982-04-08', 'EXT201', 'Germany', 'Munich', '80332', 'Sendlinger Strasse', '56'),
   (nextval('statistic_user_seq'), 'Peter', 'Weber', '1982-04-08', 'EXT202', 'Germany', 'Munich', '80331', 'Sendlinger Straße', '12A'),
   (nextval('statistic_user_seq'), 'Peter', 'Weber', '1982-04-08', 'EXT204', 'Germany', 'Augsburg', '80331', 'Sendlinger Strasse', '11'),
   (nextval('statistic_user_seq'), 'Peter', 'Weber', '1982-04-08', 'EXT205', 'Austria', 'Salzburg', '80331', 'Sendlinger Strasse', '12');

-- Person C: Julia Fischer
INSERT INTO institute_user (id, first_name, last_name, birth_date, country, city, zip_code, street, house_number)
VALUES (nextval('institute_user_seq'), 'Julia', 'Fischer', '1991-06-05', 'Germany', 'Hamburg', '20095', 'Spitalerstrasse', '9');
INSERT INTO statistic_user (id, first_name, last_name, birth_date, external_id, country, city, zip_code, street, house_number) VALUES
   (nextval('statistic_user_seq'), 'Julia', 'Fischer', '1991-06-05', 'EXT301', 'Germany', 'Hamburg', '20096', 'Spitalerstrasse', '91'),
   (nextval('statistic_user_seq'), 'Julia', 'Fischer', '1991-06-05', 'EXT302', 'Germany', 'Hamburg', '20095', 'Spitaler Strasse', '19'),
   (nextval('statistic_user_seq'), 'Julia', 'Fischer', '1991-06-05', 'EXT303', 'Germany', 'Hamburg', '20095', 'Spitalerstr.', '99'),
   (nextval('statistic_user_seq'), 'Julia', 'Fischer', '1991-06-05', 'EXT304', 'Germany', 'Bremen', '20095', 'Spitalerstrasse', '89'),
   (nextval('statistic_user_seq'), 'Julia', 'Fischer', '1991-06-05', 'EXT305', 'Austria', 'Vienna', '20095', 'Spitalerstrasse', '9');

-- Person D: Markus Keller
INSERT INTO institute_user (id, first_name, last_name, birth_date, country, city, zip_code, street, house_number)
VALUES (nextval('institute_user_seq'), 'Markus', 'Keller', '1987-02-17', 'Germany', 'Cologne', '50667', 'Schildergasse', '33');
INSERT INTO statistic_user (id, first_name, last_name, birth_date, external_id, country, city, zip_code, street, house_number) VALUES
   (nextval('statistic_user_seq'), 'Markus', 'Keller', '1987-02-17', 'EXT401', 'Germany', 'Cologne', '50668', 'Schildergasse', '33'),
   (nextval('statistic_user_seq'), 'Markus', 'Keller', '1987-02-17', 'EXT402', 'Germany', 'Cologne', '50667', 'Schilderg.', '33'),
   (nextval('statistic_user_seq'), 'Markus', 'Keller', '1987-02-17', 'EXT403', 'Germany', 'Cologne', '50667', 'Schildergasse', '34'),
   (nextval('statistic_user_seq'), 'Markus', 'Keller', '1987-02-17', 'EXT404', 'Germany', 'Düsseldorf', '50667', 'Schildergasse', '33'),
   (nextval('statistic_user_seq'), 'Markus', 'Keller', '1987-02-17', 'EXT405', 'Austria', 'Linz', '50667', 'Schildergasse', '33');

-- Person E: Laura Wagner
INSERT INTO institute_user (id, first_name, last_name, birth_date, country, city, zip_code, street, house_number)
VALUES (nextval('institute_user_seq'), 'Laura', 'Wagner', '1994-08-28', 'Germany', 'Frankfurt', '60311', 'Fressgass', '5');
INSERT INTO statistic_user (id, first_name, last_name, birth_date, external_id, country, city, zip_code, street, house_number) VALUES
   (nextval('statistic_user_seq'), 'Laura', 'Wagner', '1994-08-28', 'EXT501', 'Germany', 'Frankfurt', '60312', 'Fressgass', '5'),
   (nextval('statistic_user_seq'), 'Laura', 'Wagner', '1994-08-28', 'EXT502', 'Germany', 'Frankfurt', '60311', 'Fressgasse', '5'),
   (nextval('statistic_user_seq'), 'Laura', 'Wagner', '1994-08-28', 'EXT503', 'Germany', 'Frankfurt', '60311', 'Fressg.', '5'),
   (nextval('statistic_user_seq'), 'Laura', 'Wagner', '1994-08-28', 'EXT504', 'Germany', 'Mainz', '60311', 'Fressgass', '5'),
   (nextval('statistic_user_seq'), 'Laura', 'Wagner', '1994-08-28', 'EXT505', 'Austria', 'Innsbruck', '60311', 'Fressgass', '5');
