-- Dane startowe aplikacji. Tabele tworzy Hibernate z encji, a ten plik Spring Boot uruchamia zaraz potem.
-- LAB 01: dopiszcie tu konta wszystkich osób z zespołu (reguła R1.03).
-- LAB 02: dopiszcie dane do swoich tabel (kolejność: najpierw tabele, na które wskazują klucze obce).
insert into user_account (email, display_name, role, created_at) values
    ('wrx85921@student.wroclaw.merito.pl',   'Damian',   'USER',    current_timestamp),
    ('wrx82077@student.wroclaw.merito.pl',  'Mateusz',  'USER',    current_timestamp),
    ('s608714@student.wroclaw.merito.pl',       'Eliasz', 'USER', current_timestamp),
    ('admin@example.com',       'Admin', 'ADMIN',   current_timestamp);

-- LAB 01: Wasze konta (zmieńcie na swoje dane, rola USER)
-- insert into user_account (email, display_name, role, created_at) values
--     ('imie.nazwisko@student.merito.pl', 'Imię', 'USER', current_timestamp);
-- ===== LAB 02 · book =====

INSERT INTO book
(title, author, isbn, category, available)
VALUES
    ('Wiedzmin: Ostatnie zyczenie',
     'Andrzej Sapkowski', '9788375780635', 'FANTASY', true),

    ('Lalka',
     'Boleslaw Prus', '9788373271890', 'FICTION', true),

    ('Krotka historia czasu',
     'Stephen Hawking', '9788328701861', 'SCIENCE', false);


-- ===== LAB 02 · reader =====

INSERT INTO reader
(first_name, last_name, email,
 registered_on, active, street, city, postal_code)
VALUES
    ('Jan', 'Kowalski', 'jan@example.com',
     '2026-10-01', true, 'Grabiszynska 10', 'Wroclaw', '53-500'),

    ('Anna', 'Nowak', 'anna@example.com',
     '2026-10-05', true, 'Legnicka 25', 'Wroclaw', '54-204'),

    ('Piotr', 'Wisniewski', 'piotr@example.com',
     '2026-10-10', false, 'Dluga 15', 'Wroclaw', '53-633');


-- ===== LAB 02 · loan =====

INSERT INTO loan
(loan_date, due_date, return_date, status, renewal_count)
VALUES
    ('2026-10-01', '2026-10-15', '2026-10-12',
     'RETURNED', 0),

    ('2026-10-10', '2026-11-10', NULL,
     'ACTIVE', 0),

    ('2026-09-01', '2026-09-30', NULL,
     'OVERDUE', 1);